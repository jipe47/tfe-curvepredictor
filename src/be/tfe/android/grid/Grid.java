package be.tfe.android.grid;

import be.tfe.android.curve.Curve;
import be.tfe.android.curveviewer.CurveView;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public abstract class Grid {
	// Drawing related variables and constants

	private static final float XAXIS_THICKNESS = 2;
	private static final float YAXIS_THICKNESS = 1;
	protected Paint red, blue, white, black, green;
	protected Paint paint_xaxis, paint_yaxis;
	protected Curve curve;
	protected CurveView curveView;
	protected float width, height, xMax, yMax, xMin, yMin;
	
	public Grid(float width, float height, CurveView curveView) {
		
		this.curveView = curveView;
		this.curve = curveView.getCurve();
		
		this.width = width;
		this.height = height;
		this.xMin = curve.getMinX();
		this.yMin = curve.getMinY();
		this.xMax = curve.getMaxX();
		this.yMax = curve.getMaxY();
		
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
	    
	    paint_yaxis = new Paint();
	    paint_yaxis.setColor(Color.GRAY);
	    paint_yaxis.setStrokeWidth(YAXIS_THICKNESS);
	    
	    paint_xaxis = new Paint();
	    paint_xaxis.setColor(Color.LTGRAY);
	    paint_xaxis.setStrokeWidth(XAXIS_THICKNESS);
	}
	
	// Drawing methods
	public abstract void draw(Canvas canvas, float offsetX);
	
	public void drawXAxis(Canvas canvas, float y)
	{
		canvas.drawLine(0, y, width, y, paint_xaxis);
	}
	public void drawYAxis(Canvas canvas, float x)
	{
		canvas.drawLine(x, 0, x, height, paint_yaxis);
	}
}
