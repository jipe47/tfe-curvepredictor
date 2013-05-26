package be.tfe.android.homeview;

import java.util.ArrayList;
import java.util.Random;

import be.tfe.android.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class HomeLogo extends View {
	
	private String TAG = "HomeLogo";
	private Canvas canvas;
	private Bitmap bitmap;
	private Paint black, white, transp, stroke;
	private Bitmap logo;
	private CountDownTimer cdt;
	private static int REFRESH_RATE = 50;
	private static float OFFSET_STEP = .003f;
	private static float STROKE_WIDTH = 3;
	private static int X_STEP = 5;
	
	private ArrayList<HomeFunction> functions;
	private int currentFunction = 0;
	
	float logo_width, logo_height;
	float offsetX = 0;
	
	public HomeLogo(Context context) {
		super(context);
		init();
	}
	public HomeLogo(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public HomeLogo(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init()
	{
		functions = new ArrayList<HomeFunction>();
		functions.add(new SinFunction());
		functions.add(new CosFunction());
		functions.add(new SinSumFunction());
		functions.get(0).init();
		
		stroke = new Paint();
		stroke.setColor(Color.rgb(0, 200, 0));
		stroke.setStrokeWidth(STROKE_WIDTH);
		
		white = new Paint();
		white.setARGB(255, 255, 255, 255);
		
		black = new Paint();
		black.setARGB(255, 0, 0, 0);
		
		transp = new Paint();
		transp.setARGB(0, 255, 255, 255);
		
		transp.setXfermode(new PorterDuffXfermode(Mode.CLEAR)); // From  http://stackoverflow.com/questions/5115375/draw-transparent-shape-onto-canvas
		logo = BitmapFactory.decodeResource(getResources(), R.drawable.logo_textonly);
		predraw();
		cdt = new CountDownTimer(5000, REFRESH_RATE){

		     public void onTick(long millisUntilFinished) {
		    	 redraw();
		    	 offsetX += OFFSET_STEP;
		    	 //Log.i(TAG, "TICK");
		     }

		     public void onFinish() {
		    	 cdt.cancel();
		    	 cdt.start();
		     }
		  };
		  cdt.start();

	}
	
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() != MotionEvent.ACTION_UP)
        	return true;

        Random rnd = new Random();
        int previousFunction = currentFunction;
        
        if(functions.size() >  1)
        	while(previousFunction == currentFunction)
        		currentFunction = rnd.nextInt(functions.size());
        functions.get(currentFunction).init();
        redraw();
        return true;
    }
	
	@SuppressLint("DrawAllocation")
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		float sHeight = View.MeasureSpec.getSize(heightMeasureSpec);
	    float sWidth = View.MeasureSpec.getSize(widthMeasureSpec);
	    
	    setMeasuredDimension((int)sWidth, (int)sHeight);
	    
	    bitmap = Bitmap.createBitmap((int)sWidth, (int)sHeight, Bitmap.Config.ARGB_8888);
	    canvas = new Canvas(bitmap);
	    predraw();
	    redraw();
	}
	private void predraw()
	{
		if(this.getHeight() == 0)
			return;

		float logo_ratio = logo.getWidth() / (float)logo.getHeight();
		
		logo_height = this.getHeight();
		logo_width = logo_height * logo_ratio;
		
		if(logo_width > logo.getWidth())
		{
			logo_width = this.getWidth();
			logo_height = logo_width / logo_ratio;
		}
		
	}
	private void draw()
    {
		if(canvas == null || canvas.getHeight() == 0)
			return;
		float width = canvas.getWidth();
		float height = canvas.getHeight();
		
		canvas.drawRect(0, 0, width, height, transp);
		float prev = -1;
		
		for(int i = 0 ; i < width + X_STEP ; i+= X_STEP)
		{
			float val = functions.get(currentFunction).f(i + offsetX, height);
			
			if(prev != -1)
				canvas.drawLine(i - X_STEP, prev, i, val, stroke);
			
			prev = val;
		}
		
		if(logo == null)
			Log.e(TAG, "null logo");
		else
		{
			float hWidth = this.getWidth()/2;
			float logo_start = hWidth - logo_width/2;
			float logo_end = logo_start + logo_width;
			canvas.drawBitmap(logo, new Rect(0, 0, logo.getWidth(), logo.getHeight()), new Rect((int)logo_start, 0, (int)logo_end, (int)logo_height), black);
		}
    }
	public void redraw()
    {
    	draw();
    	invalidate();
    }
	
	protected void onDraw(Canvas canvas) {
    	if(bitmap != null)
    		canvas.drawBitmap(bitmap, 0, 0, black);
    }

}
