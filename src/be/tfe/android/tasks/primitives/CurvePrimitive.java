package be.tfe.android.tasks.primitives;


public class CurvePrimitive {
	public int id;
	public int id_category;
	public String name, descriptions, tag, difficulty;
	public String points;
	public float offset_endzone;
	public float x_scale;
	
	public ZonePrimitive[] zones;
	
	public ZonePrimitive[] getZones()
	{
		return zones;
	}
	
	public float getXScale()
	{
		return x_scale;
	}
	
	public int getId() {
		return id;
	}

	public int getIdCategory() {
		return id_category;
	}

	public String getName() {
		return name;
	}

	public String getDescriptions() {
		return descriptions;
	}

	public String getTag() {
		return tag;
	}

	public String getDifficulty() {
		return difficulty;
	}

	public String getPoints() {
		return points;
	}
	
	public float getOffsetEndzone()
	{
		return offset_endzone;
	}

	public CurvePrimitive()
	{
		
	}
}
