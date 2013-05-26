package be.tfe.android.curveviewer;

import be.tfe.android.curve.Point;
import be.tfe.android.misc.enu.PredictionInput;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

public class PMPredictor extends CurvePredictor {

	private float hWidth = 0;
	private float hHeight = 0;
	
	private enum Area {NONE, MINUS, PLUS}

	private final float LINE_THICKNESS = 4;
	private final float TEXT_SIZE = 40;
	private Area selectedZone;
	
	private Paint paint_line, paint_text;
	private Paint paint_selectedZone;
	
	@SuppressWarnings("unused")
	private String TAG = "PMPV";
	
	public PMPredictor(CurveView cv) {
		super(cv);
		init();
	}
	
	private void init() {
		paint_line = new Paint();
		paint_line.setColor(Color.BLACK);
	    paint_line.setStrokeWidth(LINE_THICKNESS);
	    
	    paint_text= new Paint();
		paint_text.setColor(Color.BLACK);
	    paint_text.setTextSize(TEXT_SIZE);
	    
	    paint_selectedZone = new Paint();
	    paint_selectedZone.setColor(Color.rgb(119, 181, 254));
	    paint_selectedZone.setAlpha(200);
	    
	    this.selectedZone = Area.NONE;
	}

	public void draw(Canvas canvas, float start, float end) {
		if(cv == null || cv.getCurve().getNbrUnpredictedZone() == 0)
			return;
		Point startpoint = cv.getCurve().getPointByX(cv.getCurve().getEndzoneOffset());
		
		if(startpoint == null)
			return;
		width = end - start;
		height = canvas.getHeight();
		
		// Compute half-width and half-height
		hWidth = (float) (width / 2.);
	    hHeight = (float) (height / 2.);
		
		// Draw the selected zone
		switch(selectedZone)
		{
		case PLUS:
			canvas.drawRect(start, 0, end, hHeight, paint_selectedZone);
			break;
		case MINUS:
			canvas.drawRect(start, hHeight, end, height, paint_selectedZone);
			break;
		}
		
		// Draw "+" zone
		canvas.drawLine(start, hHeight, end, hHeight, paint_line);
		canvas.drawText("+", start + hWidth - paint_text.measureText("+") / 2, (hHeight / 2) - ((paint_text.descent() + paint_text.ascent()) / 2), paint_text);
		
		// Draw "-" zone
		canvas.drawLine(start, hHeight, end, hHeight, paint_line);
		canvas.drawText("-", start + hWidth - paint_text.measureText("-") / 2, hHeight + ((height - hHeight) / 2) - ((paint_text.descent() + paint_text.ascent()) / 2), paint_text);
	}
	
	// Handling a touch
	public void onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_UP)
			return;
    	float y = event.getY();
    	
    	if(y < hHeight)
    		selectedZone = Area.PLUS;
    	else
    		selectedZone = Area.MINUS;
	}

	public void reset() {
		this.selectedZone = Area.NONE;		
	}

	public Prediction getPrediction() {
		Prediction p = new Prediction(this.cv.getCurve(), this.cv.getCurve().getEndzone());
		p.setPrediction(selectedZone.toString());
		p.setPredictionInput(PredictionInput.PM);
		return p;
	}

	public boolean hasPrediction() {
		return this.selectedZone != Area.NONE;
	}

	public Prediction getRawPrediction() {
		return getPrediction();
	}
}
