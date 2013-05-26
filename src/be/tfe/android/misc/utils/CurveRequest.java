package be.tfe.android.misc.utils;

import be.tfe.android.misc.enu.GameMode;
import be.tfe.android.misc.enu.PredictionInput;
import be.tfe.android.misc.enu.PredictionType;

public class CurveRequest {
	public int id_user;
	public PredictionInput predictionInput;
	public boolean training;
	public PredictionType predictionType;
	public GameMode gameMode;
	public int level;
	public boolean random;
}
