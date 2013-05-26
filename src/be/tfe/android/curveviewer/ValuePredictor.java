package be.tfe.android.curveviewer;

import be.tfe.android.misc.enu.PredictionInput;
import be.tfe.android.misc.utils.AppConfig;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;

public class ValuePredictor extends CurvePredictor {
	private Paint paint_prediction, paint_predictionValue;
	private final float PREDICTION_THICKNESS = 5;
	private final float PREDICTIONVALUE_SIZE = 20;
	private final int CURSOR_RADIUS = 8;
	private final String TAG = "ValuePredictor";
	
	private float prediction = 0;
	private float drawPrediction = 0;
	
	public ValuePredictor(CurveView cv) {
		super(cv);
		init();
	}
	private void init()
	{
		paint_prediction = new Paint();
		paint_prediction.setColor(Color.RED);
	    paint_prediction.setStrokeWidth(PREDICTION_THICKNESS);
	    
	    paint_predictionValue = new Paint();
		paint_predictionValue.setColor(Color.BLACK);
	    paint_predictionValue.setTextSize(PREDICTIONVALUE_SIZE);
	}

	public void draw(Canvas canvas, float start, float end) {
		// Draw the line and the prediction value
		float start_y = canvas.getHeight() - (cv.getCurve().getPointByX(cv.getCurve().getEndzone().getStart()).getY() - cv.getYMin()) / cv.getHeightRatio();
		canvas.drawLine(start, start_y, end, drawPrediction, paint_prediction);
		canvas.drawCircle(end - CURSOR_RADIUS/2, drawPrediction, CURSOR_RADIUS, paint_prediction);
	}
	
	// Handling a touch
	public void onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_UP)
			return;
    	float y = event.getY();
    	
    	drawPrediction = y;
	}

	public void reset() {
		if(!cv.getCurve().hasUnpredictedZone())
		{
			if(AppConfig.DEBUG)
				Log.e(TAG, "NO UNPREDICTED ZONE");
			return;
		}
		prediction = cv.getCurve().getPointByX(cv.getCurve().getEndzone().getStart()).getY();
		drawPrediction = cv.getHeight() - (prediction - cv.getYMin()) / cv.getHeightRatio();

		if(AppConfig.DEBUG)
		{
			Log.i(TAG, "-------------------------------");
			Log.i(TAG, "----- RESET VALUEPREDICTOR ----");
			Log.i(TAG, "-------------------------------");
			Log.i(TAG, "yMin = " + String.valueOf(cv.getYMin()));
			Log.i(TAG, "yMax = " + String.valueOf(cv.getYMax()));
			Log.i(TAG, "heightRatio = " + String.valueOf(cv.getHeightRatio()));
			Log.i(TAG, "Prediction = " + String.valueOf(prediction));
		}
		cv.redraw();
		cv.invalidate();
	}

	public Prediction getPrediction() {
		Prediction p = new Prediction(this.cv.getCurve(), this.cv.getCurve().getEndzone());
		p.setPrediction(String.valueOf((cv.getHeight() - drawPrediction)*cv.getHeightRatio() + cv.getYMin()));
		p.setPredictionInput(PredictionInput.VALUE);
		return p;
	}

	public boolean hasPrediction() {
		return true;
	}
	public Prediction getRawPrediction() {
		return getPrediction();
	}
}
