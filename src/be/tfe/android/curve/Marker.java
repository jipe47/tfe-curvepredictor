package be.tfe.android.curve;

public class Marker {
	private float position;
	private Type type;
	private Orientation orientation;
	
	public enum Type
	{
		MIN, MAX
	}
	
	public enum Orientation
	{
		VERTICAL, HORIZONTAL
	}
	
	public Marker(float position, Type marker, Orientation orientation)
	{
		this.position = position;
		this.type = marker;
		this.orientation = orientation;
	}

	public float getPosition() {
		return position;
	}

	public Type getType() {
		return type;
	}
	
	public Orientation getOrientation()
	{
		return orientation;
	}
}
