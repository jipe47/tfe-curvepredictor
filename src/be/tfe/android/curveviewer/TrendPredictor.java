package be.tfe.android.curveviewer;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import be.tfe.android.curve.Curve;
import be.tfe.android.curve.Point;
import be.tfe.android.curve.Zone;
import be.tfe.android.misc.enu.PredictionInput;
import be.tfe.android.misc.utils.AppConfig;

public class TrendPredictor extends CurvePredictor {

	private static final float CURVE_THICKNESS = 5;
	private static final float MIN_DENSITY = 0.2f; // Number of point for 1px
	private static final float EXTRAPOLATION_THRESHOLD = 0.85f;
	private Curve prediction;
	private String TAG = "TrendPredictor";
	
	private Paint paint_curve;
	
	private ArrayList<Point> allowedPoints;
	private int id_curve;
	private float xThreshold, xStep;
	
	public TrendPredictor(CurveView cv) {
		super(cv);
		init();
	}
	
	private void init() {
		prediction = new Curve();
		
		paint_curve = new Paint();
	    paint_curve.setColor(Color.RED);
	    paint_curve.setStrokeWidth(CURVE_THICKNESS);
	    
	    id_curve = -1;
	}

	public void draw(Canvas canvas, float start, float end) {
		if(canvas == null || prediction.getPoints().size() <= 1)
			return;
		
		// Draw the user's prediction
		ArrayList<Point> points = prediction.getPoints();
		if(points.size() > 1)
			for(int i = 1 ; i < points.size() ; i++)
	    	{
	    		Point p1 = points.get(i);
	    		Point p2 = points.get(i - 1);
	    		
	    		float x1 = (p1.getX() - cv.getCurve().getMinX()) * cv.getZoom() - cv.getXOffset();
	    		float y1 = canvas.getHeight() - (p1.getY() - cv.getYMin()) / cv.getHeightRatio();
	    		
	    		float x2 = (p2.getX() - cv.getCurve().getMinX()) * cv.getZoom() - cv.getXOffset();
	    		float y2 = canvas.getHeight() - (p2.getY() - cv.getYMin()) / cv.getHeightRatio();
	    		
	    		canvas.drawLine(	x1, 
	    							y1, 
	    							x2, 
	    							y2, 
	    							paint_curve);
	    	}
	}

	
	// Handling a touch
	public void onTouchEvent(MotionEvent event) {
		// Load curve's properties
		if(id_curve != cv.getCurve().getId())
		{
			id_curve = cv.getCurve().getId();
			Zone z = cv.getCurve().getEndzone();
			allowedPoints = cv.getCurve().getPointsBetween(z.getStart(), z.getEnd());
			
			// Add points if the density if too small
			float zoneWidth = (z.getEnd() - z.getStart()) * cv.getZoom()/cv.getZoomLevel();
			float density = allowedPoints.size() / zoneWidth;

			if(density < TrendPredictor.MIN_DENSITY)
			{
				float nbr_point = (float) Math.ceil(zoneWidth * TrendPredictor.MIN_DENSITY - 1);

				allowedPoints = new ArrayList<Point>();
				allowedPoints.add(new Point(z.getStart(), 42));
				for(float i = 1 ; i < nbr_point ; i++)
				{
					float newX = z.getStart() + i * (z.getEnd() - z.getStart())/nbr_point;
					//Log.i(TAG, "Adding " + String.valueOf(newX));
					allowedPoints.add(new Point(newX, 42));
				}
				allowedPoints.add(new Point(z.getEnd(), 42));
			}
			
			// Computing the threshold
			xStep = allowedPoints.get(1).getX() - allowedPoints.get(0).getX();
			xThreshold = xStep * 0.5f;
			
			if(AppConfig.DEBUG)
				for(int i = 0 ; i < allowedPoints.size() ; i++)
					Log.i(TAG, "-> " + allowedPoints.get(i).toString());
		}
		
		float x = (event.getX() + cv.getXOffset()) / cv.getZoom() + cv.getCurve().getMinX();
    	float y = (cv.getHeight() - event.getY()) * cv.getHeightRatio() + cv.getYMin();
    	
    	float approxX = -10;
		try {
			approxX = approxX(x);
			//Log.w(TAG, String.valueOf(x) + " => " + String.valueOf(approxX));
			prediction.addPoint(approxX, y);
		} catch (BadXException e) {
		}
		if(AppConfig.DEBUG)
			Log.i(TAG, "-----------------------------");
		
		if(event.getAction() == MotionEvent.ACTION_UP)
		{
			//Log.i(TAG, "TrendPredictor ACTION_UP ON ("+String.valueOf(event.getX())+","+String.valueOf(event.getY())+") -> ("+String.valueOf(x)+","+String.valueOf(y)+") - approxX = " + String.valueOf(approxX));
			interpolateCurve();
			extrapolateCurve();
			return;
		}
	}
	
	private void extrapolateCurve()
	{
		// If the prediction is "filled" enough, we extrapolate it
		if(prediction.getNbrPoint() == allowedPoints.size() || prediction.getNbrPoint() < 2 || prediction.getNbrPoint() / (float) allowedPoints.size() < EXTRAPOLATION_THRESHOLD)
			return;
		ArrayList<Point> points = prediction.getPoints();
		
		Point pPrev = points.get(points.size() - 2);
		Point pCurrent = points.get(points.size() - 1);
		
		float deltaX = pCurrent.getX() - pPrev.getX();
		float deltaY = pCurrent.getY() - pPrev.getY();
		
		for(int i = points.size() ; i < allowedPoints.size() ; i++)
		{
			float newX = pCurrent.getX() + deltaX;
			
			if(newX - Math.floor(newX) < 0.001)
				newX = (float) Math.floor(newX);
			else if(Math.ceil(newX) - newX < 0.001)
				newX = (float) Math.ceil(newX);
			Point np = new Point(newX, pCurrent.getY() + deltaY);
			prediction.addPoint(np.getX(), np.getY());
			pCurrent = np;
		}
	}

	private void interpolateCurve()
	{
		if(prediction.getNbrPoint() < 2 || allowedPoints == null)
			return;
		
		if(AppConfig.DEBUG)
			Log.i(TAG, "Prediction's points:");
		
		ArrayList<Point> pp = prediction.getPoints();
		if(AppConfig.DEBUG)
		{
			for(int i = 0 ; i < pp.size() ; i++)
				Log.i(TAG, " -> " + pp.get(i).toString());
			Log.i(TAG, "--------------------------");
		}
		
		Curve newPred = new Curve();
		
		for(int i = 0, pIndex = 0 ; i < allowedPoints.size() ; i++)
		{
			Point p = allowedPoints.get(i);
			
			if(AppConfig.DEBUG)
				Log.i(TAG, "Processing " + p.toString() + ", pIndex = " + String.valueOf(pIndex));
			if(prediction.hasPointByX(p.getX()))
			{
				p = prediction.getPointByX(p.getX());
				newPred.addPoint(p.getX(), p.getY());
				if(AppConfig.DEBUG)
					Log.i(TAG, "-> continue");
				pIndex++;
				continue;
			}
			// Get the next known point
			Point p2 = null;
			for(int j = i + 1 ; j < allowedPoints.size() ; j++)
			{
				Point tmp = allowedPoints.get(j);
				if(prediction.hasPointByX(tmp.getX()))
				{
					p2 = tmp;
					p2.setY(prediction.getPointByX(tmp.getX()).getY());
					break;
				}
			}
			
			// If p2 is null, we can stop interpolation: there is no point after the ith point
			if(p2 == null)
			{
				if(AppConfig.DEBUG)
					Log.w(TAG, "-> No next point");
				break;
			}
			
			// Get the previous known point: i-1
			Point p1 = pp.get(pIndex-1);
			
			if(AppConfig.DEBUG)
			{
				Log.i(TAG, "p1 = " + p1.toString());
				Log.i(TAG, "p2 = " + p2.toString());
			}
			float y = ((p.getX() - p1.getX()) * (p2.getY() - p1.getY()))/(p2.getX() - p1.getX()) + p1.getY();
			newPred.addPoint(p.getX(), y);
			if(AppConfig.DEBUG)
				Log.i(TAG, "New interpolated point: " + String.valueOf(p.getX()) + ", " + String.valueOf(y));
				
		}
		prediction = newPred;
		
		if(AppConfig.DEBUG)
			Log.w(TAG, "Interpolated prediction's points:");
		
		pp = prediction.getPoints();
		if(AppConfig.DEBUG)
		{
			for(int i = 0 ; i < pp.size() ; i++)
				Log.w(TAG, " -> " + pp.get(i).toString());
			Log.w(TAG, "--------------------------");
		}
	}
	public void reset() {
		
		if(!cv.getCurve().hasUnpredictedZone())
			return;
		Zone z = cv.getCurve().getEndzone();
				
		Point lastPoint = cv.getCurve().getPointByX(z.getStart());
		prediction = new Curve();
		prediction.addPoint(lastPoint.getX(), lastPoint.getY());
	}
	
	private float approxX(float x) throws BadXException
    {
		for(int i = 0 ; i < allowedPoints.size() ; i++)
		{
			Point p = allowedPoints.get(i);
			if(p.getX() == x || Math.abs(p.getX() - x) <= this.xThreshold)
				return p.getX();
		}
		
		
		throw new BadXException();
    }
	public Prediction getRawPrediction() {
		ArrayList<Point> ap = prediction.getPoints();
		String output = "";
		for(int i = 0 ; i < ap.size() ; i++)
		{
			Point p = ap.get(i);
			if(AppConfig.DEBUG)
				Log.w(TAG, "Add point in full prediction: " + p + " (index = " + String.valueOf(i));
			float x = p.getX();
			float y = p.getY();
			
			output += String.valueOf(x)+","+String.valueOf(y);
			if(i != ap.size() - 1)
				output += ";";
		}

		if(AppConfig.DEBUG)
			Log.e(TAG, "output = " + output);
		Prediction p = new Prediction(this.cv.getCurve(), this.cv.getCurve().getEndzone());
		p.setPrediction(output);
		p.setPredictionInput(PredictionInput.TREND);
		return p;
	}
	public Prediction getPrediction() {
		ArrayList<Point> ap = prediction.getPoints();
		String output = "";
		float xAbsolute = this.cv.getCurve().getEndzone().getStart();
		for(int i = 0 ; i < ap.size() ; i++)
		{
			Point p = ap.get(i);
			if(AppConfig.DEBUG)
				Log.w(TAG, "Add point in prediction: " + p + " (index = " + String.valueOf(xAbsolute));
			float x = p.getX();
			float y = p.getY();
			
			if(x < xAbsolute)
			{
				if(AppConfig.DEBUG)
					Log.i(TAG, "Skipping " + p);
				continue;
			}
			else if(x > xAbsolute)
			{
				// Get points before and after the current point
				Point p1 = ap.get(i - 1); // Before xAbsolute
				Point p2 = ap.get(i); // After xAbsolute
				
				if(AppConfig.DEBUG)
				{
					Log.i(TAG, "-> Interpolation with p1 = " + p1.toString());
					Log.i(TAG, "-> Interpolation with p2 = " + p2.toString());
				}
				
				y = ((x - p1.getX()) * (p2.getY() - p1.getY()))/(p2.getX() - p1.getX()) + p1.getY();
			}
			output += String.valueOf(xAbsolute)+","+String.valueOf(y);
			if(i != ap.size() - 1)
				output += ";";
			xAbsolute++;
		}

		if(AppConfig.DEBUG)
			Log.e(TAG, "output = " + output);
		Prediction p = new Prediction(this.cv.getCurve(), this.cv.getCurve().getEndzone());
		p.setPrediction(output);
		p.setPredictionInput(PredictionInput.TREND);
		return p;
	}

	public boolean hasPrediction() {
		
		if(allowedPoints == null)
			return false;
		
		for(int i = 0 ; i < allowedPoints.size() ; i++)
		{
			Point p = allowedPoints.get(i);
			if(Math.floor(p.getX()) == p.getX() && !prediction.hasPointByX(p.getX()))
				return false;
		}
		return true;
	}

}
