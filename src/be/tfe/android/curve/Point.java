package be.tfe.android.curve;

public class Point {
	private float x, y;
	
	public Point(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	
	public float getX()
	{
		return x;
	}
	
	public float getY()
	{
		return y;
	}
	
	public void setX(float x)
	{
		this.x = x;
	}
	
	public void setY(float y)
	{
		this.y = y;
	}
	
	public String toString()
	{
		return String.valueOf(x) + ","+String.valueOf(y);
	}
	public String toString(float yMin, float curveRatio)
	{
		return this.toString(yMin, curveRatio, 0);
	}
	public String toString(float yMin, float curveRatio, float xOffset)
	{
		return String.valueOf((x - xOffset)*curveRatio) + ","+String.valueOf(y*curveRatio + yMin);
	}
}
