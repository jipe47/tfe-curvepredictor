package be.tfe.android.tasks.containers;

import java.util.Set;

import be.tfe.android.curve.Curve;

public class CurveResponse {
	public Set<Curve> curves;
	public boolean isFinished;
	public int nbr_group_after = 0;
	public int groupPosition;
}
