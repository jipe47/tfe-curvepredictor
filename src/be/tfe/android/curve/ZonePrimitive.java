package be.tfe.android.curve;

public class ZonePrimitive {
	public int id;
	public double start;
	public String end;
	public boolean endzone;
	
	public double value_allowederror, value_maxerror, value_gain, pm_gain, pm_loss, pm_equalheight, trend_maxerror, trend_gain, value_loss, trend_allowederror, trend_loss;
	
	public boolean isEndzone()
	{
		return endzone;
	}
	public int getId() {
		return id;
	}
	public double getStart() {
		return start;
	}
	public String getEnd() {
		return end;
	}
}
