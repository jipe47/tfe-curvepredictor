package be.tfe.android.grid;

import java.util.ArrayList;
import java.util.Arrays;

import be.tfe.android.curve.Point;
import be.tfe.android.curveviewer.CurveView;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class ClassicGrid extends Grid {

	@SuppressWarnings("unused")
	private static String TAG = "CG";
	private static float MIN_DELTAX = 20.f;
	private static float DELTAY = 25;
	private float deltaX = MIN_DELTAX;
	
	private Paint paint_background;
	
	public ClassicGrid(float width, float height, CurveView cv) {
		super(width, height, cv);
		
		paint_background = new Paint();
		paint_background.setColor(Color.RED);
		
		xMin = curve.getMinX();
				
		// Find the most common delta x in the curve, by taking the median
		ArrayList<Point> points = curve.getPoints();
		
		if(points.size() == 0)
			return;
		
		float[] array_deltax = new float[points.size() - 1];
		
		for(int i = 1 ; i < points.size() ; i++)
			array_deltax[i - 1] = points.get(i).getX() - points.get(i - 1).getX();
		
		Arrays.sort(array_deltax);
		
		int middle = points.size() / 2;
		float dx = 0;

		if(points.size() % 2 == 0)
			dx = (array_deltax[middle] + array_deltax[middle + 1])/2;
		else
			dx = array_deltax[middle];
		if(dx > MIN_DELTAX)
			this.deltaX = dx;
	}

	public void draw(Canvas canvas, float offsetX) {
		
		float deltaXTmp = deltaX * this.curveView.getZoomLevel();
		float x = deltaXTmp - (offsetX % deltaXTmp);
		for(; x < width ; x += deltaXTmp)
			drawYAxis(canvas, x);
		
		for(float y = DELTAY ; y < height ; y += DELTAY)
			drawXAxis(canvas, y);
	}
}
