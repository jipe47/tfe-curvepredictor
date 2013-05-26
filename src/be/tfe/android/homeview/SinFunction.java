package be.tfe.android.homeview;

import java.util.Random;

public class SinFunction implements HomeFunction {

	private float rand;
	
	public float f(float x, float maxHeight) {
		float val = (float) Math.sin(x*5);
		val += 1;
		val *= (maxHeight / 2);
		val *= this.rand;
		
		val += ((1 - this.rand)/2) * maxHeight;
		return val;
	}
	
	public void init() {
		Random rnd = new Random();
		this.rand = rnd.nextFloat();
		if(this.rand < 0.4)
			this.rand = 0.4f;
	}
}
