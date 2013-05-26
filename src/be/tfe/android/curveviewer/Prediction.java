package be.tfe.android.curveviewer;

import java.util.ArrayList;
import java.util.HashMap;

import be.tfe.android.curve.Curve;
import be.tfe.android.curve.Point;
import be.tfe.android.curve.Zone;
import be.tfe.android.misc.enu.GameMode;
import be.tfe.android.misc.enu.PredictionInput;
import be.tfe.android.misc.enu.PredictionType;
import be.tfe.android.misc.utils.AppConfig;

public class Prediction {

	private Curve curve;
	private Zone zone;

	@SuppressWarnings("unused")
	private static final String TAG = "Prediction";

	public int id_zone, id_user;
	public String id_serie;
	public int time_spent, confidence;
	public String prediction;
	public PredictionInput predictionInput;
	public PredictionType predictionType;
	public int area;
	public boolean training;
	public int level;
	public GameMode gameMode;

	private boolean pm_isRight = false;
	private String pm_answer = "";
	private boolean hasEnteredConfidence = false;

	public GameMode getGameMode()
	{
		return gameMode;
	}
	
	public void setGameMode(GameMode g)
	{
		this.gameMode = g;
	}
	
	public void setLevel(int l) {
		this.level = l;
	}

	public int getLevel() {
		return this.level;
	}

	public boolean isTraining() {
		return training;
	}

	public void setTraining(boolean training) {
		this.training = training;
	}

	public PredictionType getPredictionType() {
		return predictionType;
	}

	public void setPredictionType(PredictionType predictionType) {
		this.predictionType = predictionType;
	}

	public int getIdZone() {
		return id_zone;
	}

	public void setIdZone(int id_zone) {
		this.id_zone = id_zone;
	}

	public int getIdUser() {
		return id_user;
	}

	public void setIdUser(int id_user) {
		this.id_user = id_user;
	}

	public int getTimeSpent() {
		return time_spent;
	}

	public void setTimeSpent(int time_spent) {
		this.time_spent = time_spent;
	}

	public int getConfidence() {
		return confidence;
	}

	public void setConfidence(int confidence) {
		this.confidence = confidence;
	}
	
	public String getRawPrediction()
	{
		return this.prediction;
	}

	public String getPrediction() 
	{
		return this.getPrediction(true);
	}
	
	public String getPrediction(boolean trendProcessed){
		if(trendProcessed && this.predictionInput == PredictionInput.TREND)
		{
			String[] pred = this.prediction.split(";");
			String output = "";
			for(int i = 1 ; i < pred.length ; i++)
			{
				String[] point = pred[i].split(",");
				float x = Float.valueOf(point[0]);
				if(x != Math.floor(x))
					continue;
				output += point[1];
				if(i != pred.length - 1)
					output += ";";
			}
			return output;
		}
		else
			return prediction;
	}

	public void setPrediction(String prediction) {
		this.prediction = prediction;

		if (this.getPredictionInput() == PredictionInput.PM)
			this.pm_isRight = this.pmGetAnswer() == prediction;
	}

	public PredictionInput getPredictionInput() {
		return predictionInput;
	}

	public void setPredictionInput(PredictionInput predictionInput) {
		this.predictionInput = predictionInput;
	}

	public int getArea() {
		return area;
	}

	public Zone getZone() {
		return zone;
	}

	public void setZone(int zone) {
		this.area = zone;
	}

	public Prediction(Curve c, Zone z) {
		this.curve = c;
		this.zone = z;
	}

	public float getScore() {
		if (this.getPredictionInput() == PredictionInput.VALUE) {
			float pred = Float.valueOf(this.prediction);
			float answer = this.valueGetAnswer();
			float diff = Math.abs(pred - answer);

			float gain = this.zone.value_gain;
			float loss = this.zone.value_loss;
			float allowederror = this.zone.value_allowederror;
			float maxerror = this.zone.value_maxerror;

			if (diff == 0 || diff < allowederror)
				return gain;
			else if (diff > this.zone.value_maxerror || allowederror == 0)
				return -loss;
			else
				return (-(gain + loss)) / (maxerror - allowederror)
						* (diff - allowederror) + gain;

		} else if (this.getPredictionInput() == PredictionInput.PM) {
			return this.prediction == this.pmGetAnswer() ? this.zone.pm_gain
					: -this.zone.pm_loss;
		} else if (this.getPredictionInput() == PredictionInput.TREND) {
			String[] pred = this.prediction.split(";");

			ArrayList<Point> answerSmoothed = curve.getSmoothed(zone.getStart(), zone.getEnd(), AppConfig.ALPHA);
			HashMap<Float, Float> answerSmoothedIndexed = new HashMap<Float, Float>();
			ArrayList<Point> answerRaw = curve.getPointsBetween(zone.getStart(), zone.getEnd(), true);
			HashMap<Float, Float> answerRawIndexed = new HashMap<Float, Float>();
			
			for(int i = 0 ; i < answerSmoothed.size() ; i++)
			{
				answerSmoothedIndexed.put(answerSmoothed.get(i).getX(), answerSmoothed.get(i).getY());
				answerRawIndexed.put(answerRaw.get(i).getX(), answerRaw.get(i).getY());
			}
			
			float diffSmoothed = 0, diffRaw = 0;

			for(int i = 1 ; i < pred.length ; i++)
			{
				String[] point = pred[i].split(",");
				
				float xPred = Float.valueOf(point[0]);
				float yPred = Float.valueOf(point[1]);

				if(Math.floor(xPred) != Math.ceil(xPred)) // We must interpolate the answer curve
				{
					float xPrev = (float) Math.floor(xPred);
					float xNext = (float) Math.ceil(xPred);

					float yPrevSmoothed = answerSmoothedIndexed.get(xPrev);
					float yNextSmoothed = answerSmoothedIndexed.get(xNext);
					float yPrevRaw = answerRawIndexed.get(xPrev);
					float yNextRaw = answerRawIndexed.get(xNext);

					float yInterpolatedSmoothed = (xNext - xPred)*(yPrevSmoothed - yNextSmoothed)/(xNext - xPrev) + yNextSmoothed; 
					float yInterpolatedRaw = (xNext - xPred)*(yPrevRaw - yNextRaw)/(xNext - xPrev) + yNextRaw; 
					
					diffSmoothed += Math.abs(yInterpolatedSmoothed - yPred);
					diffRaw += Math.abs(yInterpolatedRaw - yPred);
				}
				else // The prediction is an integer
				{
					diffSmoothed += Math.abs(answerSmoothedIndexed.get(xPred) - yPred);
					diffRaw += Math.abs(answerRawIndexed.get(xPred) - yPred);
				}
				
			}
			diffSmoothed /= (pred.length - 1);
			diffRaw /= (pred.length - 1);
			
			float diff = Math.min(diffSmoothed, diffRaw);

			float gain = this.zone.trend_gain;
			float loss = this.zone.trend_loss;
			float allowederror = this.zone.trend_allowederror;
			float maxerror = this.zone.trend_maxerror;
			
			if (diff == 0 || diff < allowederror)
				return gain;
			else if (diff > maxerror || allowederror == 0)
				return -loss;
			else
				return (-(gain + loss)) / (maxerror - allowederror)
						* (diff - allowederror) + gain;
		}
		return 0;
	}

	public boolean pmIsRight() {
		return pm_isRight;
	}

	public float valueGetAnswer() {
		return curve.getPointByX(zone.getEnd()).getY();
	}

	public ArrayList<Point> trendGetAnswer() {
		return curve.getPointsBetween(zone.getStart(), zone.getEnd(), true);
	}

	public String pmGetAnswer() {
		if (this.pm_answer == "") {
			Point p1 = curve.getPointByX(zone.getStart());
			Point p2 = curve.getPointByX(zone.getEnd());

			if (p2.getY() > p1.getY())
				pm_answer = "PLUS";
			else
				pm_answer = "MINUS";
		}
		return pm_answer;
	}

	public void setIdSerie(String id_serie) {
		this.id_serie = id_serie;
	}

	public String getIdSerie() {
		return this.id_serie;
	}

	public void setHasEnteredConfidence(boolean hasEnteredConfidence) {
		this.hasEnteredConfidence  = hasEnteredConfidence;
	}
	
	public boolean hasEnteredConfidence()
	{
		return this.hasEnteredConfidence;
	}
}
