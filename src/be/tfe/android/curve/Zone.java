package be.tfe.android.curve;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import be.tfe.android.curveviewer.CurveView;
import be.tfe.android.curveviewer.Prediction;
import be.tfe.android.misc.enu.PredictionInput;
import be.tfe.android.misc.utils.AppConfig;
import be.tfe.android.tasks.primitives.ZonePrimitive;

public class Zone {
	@SuppressWarnings("unused")
	private static final String TAG = "Zone";
	private int id;
	private float start, end;
	private boolean endzone = false;
	private Prediction prediction = null, rawPrediction = null;
	
	public float value_allowederror, value_maxerror, value_gain, value_loss, pm_gain, pm_loss, pm_equalheight, trend_maxerror, trend_gain, trend_loss, trend_allowederror;

	private Paint paint_zone_bck;
	private Paint paint_value_line;
	private Paint paint_trend_line;
	private Paint paint_smooth_line;
	private Paint paint_pm_line;

	private Paint paint_score_positive;
	private Paint paint_score_negative;
	
	protected Paint background_red, background_green, background_orange;

	private static float VALUE_LINE_THICKNESS = CurveView.CURVE_THICKNESS;
	private static float TREND_LINE_THICKNESS = CurveView.CURVE_THICKNESS;
	private static float PM_LINE_THICKNESS = 4;
	private static float SCORE_SIZE = 30;
	private static float SCORE_THICKNESS = 2;
	
	private float score = 0; // The score after prediction
	
	public Zone(ZonePrimitive zp)
	{
		paint_zone_bck = new Paint();
	    paint_zone_bck.setColor(Color.GRAY);
	    paint_zone_bck.setAlpha(100);

	    paint_value_line = new Paint();
	    paint_value_line.setColor(Color.BLUE);
	    paint_value_line.setStrokeWidth(VALUE_LINE_THICKNESS);
	    

	    paint_pm_line = new Paint();
	    paint_pm_line.setColor(Color.BLACK);
	    paint_pm_line.setStrokeWidth(PM_LINE_THICKNESS);

	    paint_trend_line = new Paint();
	    paint_trend_line.setColor(Color.BLUE);
	    paint_trend_line.setStrokeWidth(TREND_LINE_THICKNESS);
	    

	    paint_smooth_line = new Paint();
	    paint_smooth_line.setColor(Color.RED);
	    paint_smooth_line.setStrokeWidth(TREND_LINE_THICKNESS);
	    
	    paint_score_positive = new Paint();
	    paint_score_positive.setColor(Color.rgb(20, 147, 20));
	    paint_score_positive.setTextSize(SCORE_SIZE);
	    paint_score_positive.setStrokeWidth(SCORE_THICKNESS);
	    
	    paint_score_negative = new Paint();
	    paint_score_negative.setColor(Color.rgb(183, 13, 13));
	    paint_score_negative.setTextSize(SCORE_SIZE);
	    paint_score_negative.setStrokeWidth(SCORE_THICKNESS);
	    
	    background_red = new Paint();
	    background_red.setColor(Color.RED);
	    background_red.setAlpha(100);
	    
	    background_green = new Paint();
	    background_green.setColor(Color.GREEN);
	    background_green.setAlpha(100);
	    
	    background_orange = new Paint();
	    background_orange.setColor(Color.rgb(237, 127, 16));
	    background_orange.setAlpha(100);
	    
		this.id = zp.id;
		this.start = (float) zp.start;
		this.endzone = zp.endzone;
		this.end = (float) (zp.endzone ? -1 : Double.valueOf(zp.end));

		this.value_maxerror	 	= (float) zp.value_maxerror;
		this.value_allowederror	= (float) zp.value_allowederror;
		this.value_gain 		= (float) zp.value_gain;
		this.value_loss			= (float) zp.value_loss;
		this.pm_gain 			= (float) zp.pm_gain;
		this.pm_loss 			= (float) zp.pm_loss;
		this.pm_equalheight 	= (float) zp.pm_equalheight;
		this.trend_gain 		= (float) zp.trend_gain;
		this.trend_loss 		= (float) zp.trend_loss;
		this.trend_maxerror 	= (float) zp.trend_maxerror;
		this.trend_allowederror 	= (float) zp.trend_allowederror;
		
		// TODO trend_loss
	}
	
	public void resetPrediction()
	{
		this.prediction = null;
	}
	public boolean isEndzone()
	{
		return endzone;
	}
	
	public void setPrediction(Prediction p)
	{
		this.prediction = p;
	}
	
	public void setRawPrediction(Prediction p)
	{
		this.rawPrediction = p;
	}
	
	public Prediction getPrediction()
	{
		return this.prediction;
	}
	
	public boolean hasPrediction()
	{
		return this.prediction != null;
	}
	
	public int getId() {
		return id;
	}
	public float getStart() {
		return start;
	}
	public float getEnd() {
		return end;
	}
	
	@SuppressWarnings("unused")
	public void overdraw(Canvas canvas, float start, float end, Curve curve, float heightRatio, float zoom, float xOffset, float sRHeight, float yMin, Context context)
	{
		if(AppConfig.DEBUG && prediction.getPredictionInput() == PredictionInput.TREND)
		{
			ArrayList<Point> smoothed = curve.getSmoothed(this.start, this.end, 0.6f);
			ArrayList<Point> smoothedAdj = new ArrayList<Point>();
			for(int i = 0 ; i < smoothed.size() ; i++)
			{
				Point p = smoothed.get(i);
				
				float x = (Float.valueOf(this.start + i) - curve.getMinX()) * zoom - xOffset;
	    		float y = canvas.getHeight() - (p.getY() - yMin) / heightRatio;
				
				smoothedAdj.add(new Point(x, y));
			}
			
			for(int i = 1 ; i < smoothedAdj.size() ; i++)
	    	{
	    		Point p1 = smoothedAdj.get(i);
	    		Point p2 = smoothedAdj.get(i - 1);
	    		canvas.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY(), paint_smooth_line);
	    	}
		}
	}
	
	public void underdraw(Canvas canvas, float start, float end, Curve curve, float heightRatio, float zoom, float xOffset, float sRHeight, float yMin, Context context)
	{
		canvas.drawRect(start, 0, end, canvas.getHeight(), paint_zone_bck);
		
		String pred = prediction.getPrediction(false);
		
		// TODO Switch ?!		
		if(prediction.getPredictionInput() == PredictionInput.VALUE)
		{
			// Draw the background
			float value = Float.valueOf(pred);
			float answer = curve.getPointByX(getEnd()).getY();
			
			float diff = Math.abs(value - answer);
			Paint backgroundColor = background_orange;
			
			if(diff <= value_allowederror)
				backgroundColor = background_green;
			else if(diff > value_maxerror)
				backgroundColor = background_red;
			
			canvas.drawRect(start, 0, end, canvas.getHeight(), backgroundColor);
			
			// Draw the user's answer
			float valueAdj = canvas.getHeight() - (value - yMin) / heightRatio;
			float startValue = canvas.getHeight() - (curve.getPointByX(this.start).getY() - yMin) / heightRatio;
			canvas.drawLine(start, startValue, end, valueAdj, paint_value_line);
			
		}
		else if(prediction.getPredictionInput() == PredictionInput.PM)
		{
			String answer = prediction.pmGetAnswer();
			boolean isRight = answer == pred;
			
			float height = canvas.getHeight();
			float hHeight = height / 2;
			
		    Paint backgroundAnswer = isRight ? background_green : background_red;
			
		    canvas.drawRect(start, 0, end, height, backgroundAnswer);
		    
		    // Draw the separation line
			canvas.drawLine(start, hHeight, end, hHeight, paint_pm_line);
		}
		else if(prediction.getPredictionInput() == PredictionInput.TREND)
		{
			pred = rawPrediction.getRawPrediction();
			ArrayList<Point> points = new ArrayList<Point>();
			
			String[] pp = pred.split(";");

			String[] pp0 = pp[0].split(",");
			String[] pp1 = pp[1].split(",");
						
			float xStep = Float.valueOf(pp1[0]) - Float.valueOf(pp0[0]);
			for(int p = 0 ; p < pp.length ; p++)
			{
				String[] ppp = pp[p].split(",");
				float x = (Float.valueOf(this.start + p*xStep) - curve.getMinX()) * zoom - xOffset;
	    		float y = canvas.getHeight() - (Float.valueOf(ppp[1]) - yMin) / heightRatio;

				points.add(new Point(x, y));
			}
			
			for(int i = 1 ; i < points.size() ; i++)
	    	{
	    		Point p1 = points.get(i);
	    		Point p2 = points.get(i - 1);
	    		canvas.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY(), paint_trend_line);
	    	}
		}
		
		// Draw the final score for the prediction
		String score_string = score < 0 ? String.valueOf(score) : "+" + String.valueOf(score);

		// Center the score
		float score_x = start + (end - start) / 2 - paint_score_positive.measureText(score_string) / 2;
		canvas.drawText(score_string, score_x, 30, score <= 0 ? paint_score_negative : paint_score_positive);
	}
	
	public void setScore(float s)
	{
		this.score = s;
	}
	
	public float getScore()
	{
		return this.score;
	}
}
