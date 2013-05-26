package be.tfe.android.homeview;

import java.util.Random;

public class CosFunction implements HomeFunction {
	private float rand;

	private float[] VALUES = {0.4f, 0.5f, 1f, 1.5f, 2f};
	
	public float f(float x, float maxHeight) {
		float val = (float) Math.cos(5*x*rand);
		val += 1;
		val *= (maxHeight / 2);
		return val;
	}

	public void init() {
		Random rnd = new Random();
		this.rand = VALUES[rnd.nextInt(VALUES.length)];
	}
}
