package be.tfe.android.curveviewer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;


public abstract class CurvePredictor{
	
	// Drawing related variables and constants
	protected Canvas canvas;
	protected Paint red, blue, white, black, green;
	protected Paint paint_line_red, paint_line_blue, paint_line_green, paint_line_yellow, paint_line_orange;
	protected float width, height;
	protected float zoom = 1.0f, heightRatio = 1.0f;
	protected float yMin = 0;
	protected CurveView cv;
	
	private final float CURVE_THICKNESS = 15;
	
	public CurvePredictor(CurveView cv) {
		init();
		this.cv = cv;
		this.zoom = cv.getZoom();
		this.width = cv.getWidth();
		this.height = cv.getHeight();
	}
	
	public void setZoom(float zoom) {
		this.zoom = zoom;
	}
	
	public void setHeightRatio(float r)
	{
		this.heightRatio = r;
	}
	public void setYMin(float yMin)
	{
		this.yMin = yMin;
	}

	public CurvePredictor(Context context, AttributeSet attrs) {
		init();
	}
	
	public CurvePredictor(Context context, AttributeSet attrs, int defStyle) {
		init();
	}
	
	private void init() {
	    red = new Paint();
	    red.setColor(Color.RED);
	    
	    blue = new Paint();
	    blue.setColor(Color.BLUE);
	    
	    white = new Paint();
	    white.setColor(Color.WHITE);

	    black = new Paint();
	    black.setColor(Color.BLACK);

	    green = new Paint();
	    green.setColor(Color.GREEN);
	    
	    paint_line_red = new Paint();
	    paint_line_red.setColor(Color.RED);
	    paint_line_red.setStrokeWidth(CURVE_THICKNESS);
	    
	    paint_line_blue = new Paint();
	    paint_line_blue.setColor(Color.BLUE);
	    paint_line_blue.setStrokeWidth(CURVE_THICKNESS);

	    paint_line_green = new Paint();
	    paint_line_green.setColor(Color.GREEN);
	    paint_line_green.setStrokeWidth(CURVE_THICKNESS);
	    
	    paint_line_yellow = new Paint();
	    paint_line_yellow.setColor(Color.YELLOW);
	    paint_line_yellow.setStrokeWidth(CURVE_THICKNESS);
	    
	    paint_line_orange = new Paint();
	    paint_line_orange.setColor(Color.rgb(237, 127, 16));
	    paint_line_orange.setStrokeWidth(CURVE_THICKNESS);
	}
	
	// Interface to interact with the prediction
	public abstract void reset();
	
	// Drawing methods
	public abstract void draw(Canvas canvas, float start, float end);
	
	public abstract void onTouchEvent(MotionEvent event);
	
	public abstract Prediction getPrediction();
	public abstract Prediction getRawPrediction();
	public abstract boolean hasPrediction();

}
