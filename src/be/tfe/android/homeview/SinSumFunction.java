package be.tfe.android.homeview;

import java.util.Random;

public class SinSumFunction implements HomeFunction {

	private float rand;
	private float[] VALUES = {10f, 15f, 25f, 20f};

	public float f(float x, float maxHeight) {
		float val = (float) Math.sin(x*5) + (float) Math.sin(x*this.rand);
		val += 2;
		val *= (maxHeight / 4);
		return val;
	}
	
	public void init() {
		Random rnd = new Random();
		this.rand = VALUES[rnd.nextInt(VALUES.length)];
	}
}
