package be.tfe.android.curveviewer;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import be.tfe.android.curve.Curve;
import be.tfe.android.curve.Point;
import be.tfe.android.curve.Zone;
import be.tfe.android.grid.ClassicGrid;
import be.tfe.android.grid.Grid;
import be.tfe.android.misc.utils.AppConfig;
import be.tfe.android.misc.utils.AppUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CurveView extends View {
	/********************/
	/** CONFIG **/
	/********************/

	@SuppressWarnings("unused")
	private String TAG = "CV";

	public final static float CURVE_THICKNESS = 5;
	public final static float SCROLLINDICATOR_THICKNESS = 8;
	public final static float DEATHZONE_PERCENTAGE = .3f;

	public final static float MAX_ZOOM = 2.5f;
	public final static float MIN_ZOOM = 0.8f;

	private Curve curve;
	private Grid grid = null;

	private CurvePredictor predictor = null;

	private Paint paint_red, paint_blue, paint_white, paint_black;
	private Paint paint_line_red, paint_line_blue, paint_line_green,
			paint_line_yellow, paint_line_orange;
	private Paint paint_curve, paint_scrollindicator;
	private Paint paint_predictionzone, paint_deadzone;
	private Canvas canvas;
	private Bitmap bitmap;

	private float scroll_prevX;
	private float scale_factor = 1, zoom = MIN_ZOOM;
	private boolean inEndZone = false;
	private boolean wasInEndZone = false;

	private float pinch_olddist;
	private float pinchZoom;

	private boolean isZooming = false;

	/*
	 * View rendering variables NB : the "c" prefix stands for "curve" ;
	 * variables with this prefix uses the curve's "temporal units". On the
	 * other hand, the prefix "d" stands for "display" and are expressed in
	 * pixels. The "s" prefix stands for "screen" and is also expressed in
	 * pixels.
	 */

	private float cXMin, cYMin, cXMax, cYMax;
	private float dYMax;

	private float sWidth, sHeight;
	private float cWidth;

	private float cHeightRatio, cWidthRatio;

	private ArrayList<Point> processedPoints;

	private float dOffsetX = 0;
	private float dOffsetXMax = 0;
	private float dOffsetXMin = 0;

	// In case of predraw without zone to predict
	private float previous_cHeightRatio;
	private float previous_cYMin;

	public CurveView(Context context) {
		super(context);
		init();
	}

	public CurveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CurveView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		paint_red = new Paint();
		paint_red.setColor(Color.RED);

		paint_blue = new Paint();
		paint_blue.setColor(Color.BLUE);

		paint_white = new Paint();
		paint_white.setColor(Color.WHITE);

		paint_black = new Paint();
		paint_black.setColor(Color.BLACK);

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

		// User's curve's styles
		paint_curve = new Paint();
		paint_curve.setColor(Color.rgb(0, 200, 0));
		paint_curve.setStrokeWidth(CURVE_THICKNESS);

		paint_scrollindicator = new Paint();
		paint_scrollindicator.setColor(Color.RED);
		paint_scrollindicator.setStrokeWidth(SCROLLINDICATOR_THICKNESS);

		// Da predictionzone
		paint_predictionzone = new Paint();
		paint_predictionzone.setColor(Color.LTGRAY);
		paint_predictionzone.setAlpha(100);

		// The Zone of The Death
		paint_deadzone = new Paint();
		paint_deadzone.setColor(Color.DKGRAY);
		paint_deadzone.setAlpha(100);

		curve = new Curve();
	}

	public void setPredictor(CurvePredictor p) {
		this.predictor = p;
	}

	public void setCurve(Curve c) {
		this.curve = c;
		this.dOffsetX = 0;
		//this.zoomIndex = 0;
		this.pinchZoom = 1;
		predraw(); // Compute necessary data for the rendering
		this.predictor.reset();
		redraw();
		invalidate();
	}

	public Curve getCurve() {
		return this.curve;
	}

	public Prediction getPrediction() {
		return this.predictor.getPrediction();
	}

	public Prediction getRawPrediction() {
		return this.predictor.getRawPrediction();
	}

	@SuppressLint("DrawAllocation")
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		sHeight = View.MeasureSpec.getSize(heightMeasureSpec);
		sWidth = View.MeasureSpec.getSize(widthMeasureSpec);

		setMeasuredDimension((int) sWidth, (int) sHeight);

		// TODO Move these instantiations to an "init" method
		bitmap = Bitmap.createBitmap((int) sWidth, (int) sHeight,
				Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		predraw();
		resetPredictor(); // Crappy fix so that the first displayed VALUE
							// prediction is not null
		draw();
	}

	public boolean onTouchEvent(MotionEvent event) {

		if (event.getPointerCount() == 2) {
			scroll_prevX = -1;

			// From http://www.zdnet.com/blog/burnette/how-to-use-multi-touch-in-android-2-part-6-implementing-the-pinch-zoom-gesture/1847
			switch (event.getAction()) {
			case MotionEvent.ACTION_POINTER_DOWN:
			case MotionEvent.ACTION_POINTER_2_DOWN:
				pinch_olddist = AppUtils.spacing(event);
				if (pinch_olddist > 10f) {
					isZooming = true;
					this.predraw();
				}
				break;

			case MotionEvent.ACTION_MOVE:
				if(isZooming)
				{
					float newDist = AppUtils.spacing(event);
					if (newDist > 10f && Math.abs(pinch_olddist - newDist) > 10f) {
						float scale = newDist / pinch_olddist;
						
						if(scale < 1)
							scale = - 1 / scale;
						scale /= 20;
						float oldPinchZoom = pinchZoom;
						pinchZoom = Math.max(Math.min(pinchZoom + scale, MAX_ZOOM), MIN_ZOOM);
						
						PointF midPoint = AppUtils.midPoint(event);
						
						this.dOffsetX += ((this.dOffsetX/ oldPinchZoom + midPoint.x) * pinchZoom) - this.dOffsetX - midPoint.x*pinchZoom;
						if (dOffsetX < dOffsetXMin) {
							dOffsetX = dOffsetXMin;
						}
						if (dOffsetX > dOffsetXMax) {
							dOffsetX = dOffsetXMax;
						}
						this.pinch_olddist = newDist;
						this.predraw();
					}
				}
				break;
			}
			
		} else {
			isZooming = false;
			boolean newInEndZone = this.getEndzoneOffset() < event.getRawX()
					&& event.getRawX() <= this.getEndzoneOffset()
							+ curve.getEndzoneLength() * zoom;
			if (newInEndZone != inEndZone) {
				wasInEndZone = inEndZone;
			}

			inEndZone = newInEndZone;
			switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				if (inEndZone) {
					this.predictor.onTouchEvent(event);
					break;
				} else if (wasInEndZone) {
					break;
				}
				int numPointers = event.getPointerCount();
				if (numPointers == 1) {
					float currX = event.getX();
					
					if(scroll_prevX >= 0)
					{
						float deltaX = -(currX - scroll_prevX);
						dOffsetX += deltaX;
	
						if (dOffsetX < dOffsetXMin) {
							dOffsetX = dOffsetXMin;
						}
						if (dOffsetX > dOffsetXMax) {
							dOffsetX = dOffsetXMax;
						}
					}
					scroll_prevX = currX;
				}
				break;

			case MotionEvent.ACTION_UP:
				inEndZone = false;
				wasInEndZone = false;
				this.predictor.onTouchEvent(event);
				break;

			case MotionEvent.ACTION_DOWN:
				float rawX = event.getRawX();

				// If the motionevent is situated in the endzone (<->
				// prediction), use the predictor's handler
				if (isEndzoneVisible() && inEndZone) {
					this.predictor.onTouchEvent(event);
					break;
				}

				scroll_prevX = rawX;
				break;
			}
		}
		this.redraw();
		return true;
	}

	public void predraw() {
		cXMin = curve.getMinX();
		cXMax = curve.getMaxX();
		cYMin = curve.getMinY();
		cYMax = curve.getMaxY();

		cWidth = curve.getEndzoneLength() == -1 ? cXMax - cXMin : curve
				.getEndzoneOffset() + curve.getEndzoneLength() - cXMin;

		scale_factor = 1 / cWidthRatio; // if scale = 1, the whole curve is
										// visible on the screen

		zoom = scale_factor * pinchZoom;

		dYMax = cYMax;

		// Scale factors
		// cHeightRatio = (float) ((cHeight) / sHeight);
		cWidthRatio = cWidth / sWidth;

		// "Center the curve" : the start of the endzone is in the middle of the
		// screen.
		if (curve.hasUnpredictedZone()) {
			Zone z = curve.getEndzone();
			Point startpoint = curve.getPointByX(z.getStart());
			float hHeight = this.getHeight() / 2;

			// startpoint / stuff = hHeight

			cHeightRatio = (startpoint.getY() - curve.getMinY()) / hHeight;

			float maxAdjustedY = (curve.getMaxY() - curve.getMinY())
					/ cHeightRatio;

			if (maxAdjustedY > this.getHeight()) {
				float len = cYMax - startpoint.getY();
				cHeightRatio = (float) ((len) / (sHeight / 2));
				maxAdjustedY = (curve.getMaxY() - curve.getMinY())
						/ cHeightRatio;
				cYMin -= (sHeight - maxAdjustedY) * cHeightRatio;
				// Log.i(TAG, "Here !!!");
			}
			/*
			 * else Log.e(TAG, "Pas here !!!");
			 */

			previous_cYMin = cYMin;
			previous_cHeightRatio = cHeightRatio;
		} else {
			cYMin = previous_cYMin;
			cHeightRatio = previous_cHeightRatio;
		}

		// Compute offset extrema
		dOffsetXMin = -sWidth * CurveView.DEATHZONE_PERCENTAGE;

		dOffsetXMax = cWidth * zoom - sWidth; // Offset max for the display
		// Log.i(TAG, "temporary offsetxMax = " + String.valueOf(dOffsetXMax));
		if (dOffsetXMax < 0)
			dOffsetXMax = 0;
		dOffsetXMax += sWidth * CurveView.DEATHZONE_PERCENTAGE;

		// Adapte curve's points to the screen
		processedPoints = new ArrayList<Point>();

		// Process the curve points
		ArrayList<Point> pointsOriginal = curve.getPoints();

		for (int i = 0; i < pointsOriginal.size(); i++) {
			Point c = pointsOriginal.get(i);

			// Convert from "units" to pixel
			float dX = (c.getX() - cXMin) * zoom;
			float dY = (c.getY() - cYMin) / cHeightRatio;
			processedPoints.add(new Point(dX, dY));
		}

	}

	private void draw() {
		if (sHeight == 0 || sWidth == 0 || canvas == null)
			return;
		Date startRender = new Date();
		if (curve == null) {
			canvas.drawRect(0, 0, sWidth, sHeight, paint_black);
			canvas.drawText("No curve loaded.", sWidth / 2, sHeight / 2,
					paint_red);
			return;
		}

		// Clear the screen
		canvas.drawRect(0, 0, sWidth, sHeight, paint_white);

		/*************************************************/
		/** Step 1: compute data used during the render **/
		/**************************************************/

		// Cf predraw

		float offset_endzone = this.getEndzoneOffset(); // Offset max for the
														// points

		/***************************/
		/** Step 2: draw the grid **/
		/***************************/

		if (grid == null) {
			grid = new ClassicGrid(sWidth, sHeight, this);
		}
		grid.draw(canvas, dOffsetX);

		/*********************************************/
		/** Step 3: draw the zone outside the curve **/
		/*********************************************/

		if (dOffsetX < 0) {
			canvas.drawRect(0, 0, -dOffsetX, sHeight, paint_deadzone);
		}

		/***********************************************************/
		/** Step 4: background of the zone that must be predicted **/
		/***********************************************************/

		if (offset_endzone < sWidth) // If the prediction zone can be seen
		{
			float endzone_end = offset_endzone
					+ (curve.getEndzoneLength() * zoom);

			canvas.drawRect(offset_endzone, 0, endzone_end, sHeight,
					this.paint_predictionzone);

			if (endzone_end < sWidth) {
				canvas.drawRect(endzone_end, 0, sWidth, sHeight, paint_deadzone);
			}
		}

		/**********************************************/
		/** Step 5: draw the already predicted zones **/
		/**********************************************/
		Iterator<Zone> it = curve.getPredictedZones().iterator();

		while (it.hasNext()) {
			Zone z = it.next();

			float startAdj = (z.getStart() - cXMin) * zoom;
			float endAdj = z.isEndzone() ? cWidth : (z.getEnd() - cXMin) * zoom;

			if (endAdj < dOffsetX) // If the zone is not visible, stop the
									// traversal
				continue; // TODO Should be break
			else if (startAdj > dOffsetX + sWidth) // If the zone is not
													// visible, skip it
				continue;
			// Log.i(TAG, "Not skipped !");
			z.underdraw(canvas, startAdj - dOffsetX, endAdj - dOffsetX, curve,
					cHeightRatio, zoom, dOffsetX, sHeight, cYMin,
					this.getContext());
		}

		/****************************/
		/** Step 6: draw the curve **/
		/****************************/

		/*
		 * Step 6a: process the points : select the displayed points and convert
		 * their values to pixels.
		 */

		for (int i = 0; i < processedPoints.size(); i++) {
			Point c = processedPoints.get(i);

			// Convert from "units" to pixel
			float dX = c.getX();
			float dY = c.getY();

			if (i > 0) {
				// If the point and the next point are not displayed
				if (dX <= dOffsetX
						&& i < processedPoints.size() - 1
						&& ((processedPoints.get(i + 1).getX() - cXMin) * zoom < dOffsetX)) {
					continue;
				}
				// If the point is not displayed as well as the previous point
				// and both are situated on the right of the screen, we can stop
				else if (dX > dOffsetX + sWidth
						&& processedPoints.get(i - 1).getX() > dOffsetX
								+ sWidth) {
					break;
				} else if (dX - dOffsetX > offset_endzone) {
					break;
				}
			}

			if (i > 0) {
				Point prev = processedPoints.get(i - 1);
				canvas.drawLine(prev.getX() - dOffsetX, sHeight - prev.getY(),
						dX - dOffsetX, sHeight - dY, paint_curve);
			}
		}

		it = curve.getPredictedZones().iterator();
		while (it.hasNext()) {
			Zone z = it.next();

			float startAdj = (z.getStart() - cXMin) * zoom;
			float endAdj = z.isEndzone() ? cWidth : (z.getEnd() - cXMin) * zoom;

			if (endAdj < dOffsetX) // If the zone is not visible, stop the
									// traversal
				continue; // TODO Should be break
			else if (startAdj > dOffsetX + sWidth) // If the zone is not
													// visible, skip it
				continue;
			// Log.i(TAG, "Not skipped !");
			z.overdraw(canvas, startAdj - dOffsetX, endAdj - dOffsetX, curve,
					cHeightRatio, zoom, dOffsetX, sHeight, cYMin,
					this.getContext());
		}

		/***********************************/
		/** Step 7: draw scroll indicator **/
		/***********************************/
		/*
		 * float cWidthScaled = cWidth * zoom ; if (cWidthScaled > sWidth) //
		 * The curve is too big for the screen { float percent = sWidth /
		 * (cWidthScaled + 2*DEATHZONE_PERCENTAGE*sWidth); float barlength =
		 * percent * sWidth; float startBar = dOffsetX +
		 * DEATHZONE_PERCENTAGE*sWidth;
		 * 
		 * Log.i(TAG, "---------------------"); Log.i(TAG, "offsetX =  "+
		 * String.valueOf(dOffsetX)); Log.i(TAG, "cWidthScaled=  "+
		 * String.valueOf(cWidthScaled)); Log.i(TAG, "percentage=  "+
		 * String.valueOf(percent)); Log.i(TAG, "startBar =  "+
		 * String.valueOf(startBar));
		 * 
		 * canvas.drawLine(startBar, sHeight - 10, startBar + barlength, sHeight
		 * - 10, paint_scrollindicator); }
		 */

		/********************************/
		/** Step 8: draw the predictor **/
		/********************************/

		if (this.isEndzoneVisible() && this.predictor != null
				&& curve.hasUnpredictedZone()) {
			float start = (curve.getEndzoneOffset() - cXMin) * zoom - dOffsetX;
			float end = start + (curve.getEndzoneLength() * zoom);
			predictor.draw(canvas, start, end);
		}

		/*******************/
		/** Step 9: debug **/
		/*******************/

		// Log.w(TAG, " = "+String.valueOf());

		// Log.w(TAG, "-----------------");
		// Log.w(TAG, "cXMin = " + String.valueOf(cXMin));
		// Log.w(TAG, "cXMax = " + String.valueOf(cXMax));
		// Log.w(TAG, "cYMin = " + String.valueOf(cYMin));
		// Log.w(TAG, "cYMax = " + String.valueOf(cYMax));
		// Log.w(TAG, "cWidth = " + String.valueOf(cWidth));
		// Log.w(TAG, "cHeight = " + String.valueOf(cHeight));

		// Log.w(TAG, "scale = " + String.valueOf(scale));
		// Log.w(TAG, "scale_factor = " + String.valueOf(scale_factor));
		// Log.w(TAG, "zoom = " + String.valueOf(zoom));

		// Log.w(TAG, "cHeightRatio = "+String.valueOf(cHeightRatio));
		// Log.w(TAG, "dOffetX = "+String.valueOf(dOffsetX));
		// Log.w(TAG, "sWidth = "+String.valueOf(sWidth));
		//
		// Log.w(TAG, "offset_endzone = " + String.valueOf(offset_endzone));
		// Log.w(TAG, "length_endzone = " +
		// String.valueOf(curve.getEndzoneLength()));

		// Draw frames per second
		Date newTime = new Date();
		long timeBetweenFrames = newTime.getTime() - startRender.getTime();

		double fps = timeBetweenFrames != 0 ? 1000 / timeBetweenFrames : 99999;
		String string_fps = "fps: " + String.valueOf(fps);
		float string_width = paint_black.measureText(string_fps);
		if (AppConfig.DEBUG) {
			// Draw the "FPS"
			canvas.drawText(string_fps, sWidth - string_width, sHeight - 50,
					paint_black);

			// Draw the curve's name
			String name = this.curve.getName();
			if (name != null) {
				float namelength = this.paint_black.measureText(name);
				float textheight = this.paint_black.getTextSize();
				canvas.drawRect(10, sHeight - 10 - textheight, 10 + namelength,
						sHeight - 10, paint_white);
				canvas.drawText(name, 10, sHeight - 10, paint_black);
			}
		}

		invalidate();
	}

	private boolean isEndzoneVisible() {
		return this.getEndzoneOffset() < sWidth;
	}

	protected void onDraw(Canvas canvas) {
		if (bitmap != null)
			canvas.drawBitmap(bitmap, 0, 0, paint_red);
	}

	public void redraw() {
		draw();
		invalidate();
	}

	public void zoomIn() {
		//zoomIndex = Math.min(ZOOMS.length - 1, zoomIndex + 1);
		pinchZoom = (float) Math.min(MAX_ZOOM, pinchZoom + 0.2);
		predraw();
	}

	public void zoomOut() {
		//zoomIndex = Math.max(0, zoomIndex - 1);
		pinchZoom = (float) Math.max(MIN_ZOOM, pinchZoom - 0.2);
		predraw();
	}

	/*************************/
	/** Getters and Setters **/
	/*************************/

	public float getZoom() {
		return this.zoom;
	}

	public float getHeightRatio() {
		return this.cHeightRatio;
	}

	public float getYMin() {
		return this.cYMin;
	}

	public float getYMax() {
		return this.dYMax;
	}

	public float getXOffset() {
		return this.dOffsetX;
	}

	public float getEndzoneOffset() {
		return (curve.getEndzoneOffset() - cXMin) * zoom - dOffsetX;
	}

	public void resetPredictor() {
		if (predictor != null)
			predictor.reset();
	}

	public float getZoomLevel() {
		return pinchZoom;
	}
}
