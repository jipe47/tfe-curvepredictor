package be.tfe.android.curve;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

import android.util.Log;
import be.tfe.android.curveviewer.Prediction;
import be.tfe.android.misc.utils.AppConfig;

public class Curve {

	private TreeMap<Float, Point> points;
	private int id;
	private float offset_endzone = -2;
	private float length_endzone = -2;
	private String TAG = "Curve";
	private LinkedList<Zone> unpredictedZones;
	private LinkedList<Zone> predictedZones;
	
	private ArrayList<Point> smoothed = null;
	
	private float xMin, xMax, yMin = Integer.MAX_VALUE, yMax = Integer.MIN_VALUE;
	private String name;

	public Curve() {
		this(-1);
	}

	public Curve(CurvePrimitive cp) {
		this(cp.getId());
		this.name = cp.name;
		String[] points = cp.getPoints().split(";");
		xMin = 0;
		xMax = points.length - 1;
		for (int j = 0; j < points.length; j++) {
			float y = Float.valueOf(points[j]);
			
			if(y < yMin)
				yMin = y;
			if(y > yMax)
				yMax = y;
			
			this.addPoint(j, y);
		}

		ZonePrimitive[] zones = cp.zones;
		for (int j = 0; j < zones.length; j++) {
			Zone z = new Zone(zones[j]);
			if(AppConfig.DEBUG)
				Log.i(TAG, "Adding zone : " + String.valueOf(z.getStart()) +
			 " -> " + String.valueOf(z.getEnd()));
			this.unpredictedZones.add(z);
		}
		loadZone();
	}

	public Curve(int id) {
		this.points = new TreeMap<Float, Point>();
		this.id = id;

		unpredictedZones = new LinkedList<Zone>();
		predictedZones = new LinkedList<Zone>();
	}
	
	public ArrayList<Point> getSmoothed(float start, float end, float alpha)
	{
		ArrayList<Point> ts = this.getPointsBetween(start, end, true);
		Point p0 = ts.get(0);
		smoothed = new ArrayList<Point>();
		smoothed.add(new Point(p0.getX(), p0.getY()));
		float yPrev = p0.getY();
		for(int i = 1 ; i < ts.size() ; i++)
		{
			Point p = points.get(ts.get(i).getX());
			yPrev = (1 - alpha) * p.getY() + alpha * yPrev;
			
			smoothed.add(new Point(start + i, yPrev));
		}
		
		return smoothed;
	}
	public String getName()
	{
		return this.name;
	}
	
	public void resetPrediction()
	{
		while(this.predictedZones.size() != 0)
			this.unpredictedZones.add(predictedZones.removeLast());
		
		for(int i = 0 ; i < this.unpredictedZones.size() ; i++)
			this.unpredictedZones.get(i).resetPrediction();
	}

	public void addPoint(float x, float y) {
		if (points.containsKey(x))
			return;
		points.put(x, new Point(x, y));
	}

	public ArrayList<Point> getPoints() {
		ArrayList<Point> list = new ArrayList<Point>();
		
		Iterator<Float> it = points.keySet().iterator();
		while (it.hasNext())
		{
			Point p = points.get(it.next());
			list.add(p);
		}
		return list;
	}
	
	public int getNbrPoint() {
		return points.size();
	}

	public Point getPointByX(float x) {
		return points.get(x);
	}
	
	public boolean hasPointByX(float x)
	{
		return points.containsKey(x);
	}

	public void deletePoint(Point p) {
		points.remove(p);
	}

	public Point getMinPoint() {
		return points.firstEntry().getValue();
	}

	public Point getMaxPoint() {
		return points.lastEntry().getValue();
	}
	public int getId() {
		return id;
	}
	
	public void removeZonesButLast()
	{
		while(unpredictedZones.size() != 1)
		{
			if(AppConfig.DEBUG)
				Log.i(TAG, "Removing one zone!");
			unpredictedZones.removeFirst();
		}
		loadZone();
	}

	public String toString() {
		ArrayList<Point> ap = this.getPoints();
		String output = "";
		for (int i = 0; i < ap.size(); i++)
			output += ap.get(i).toString() + ";";
		return output;
	}

	/***********************/
	/** Points Management **/
	/***********************/

	public float getMinX() {
		return xMin;
	}

	public float getMinY() {
		return yMin;
	}

	public float getMaxX() {
		return xMax;
	}

	public float getMaxY() {
		return yMax;
	}
	
	public ArrayList<Point> getPointsBetween(float from, float to)
	{
		return getPointsBetween(from, to, true);
	}
	public ArrayList<Point> getPointsBetween(float from, float to, boolean includeTo)
	{
		ArrayList<Point> points = new ArrayList<Point>();
		
		Iterator<Entry<Float, Point>> it = this.points.subMap(from, true, to, includeTo).entrySet().iterator();
		
		while(it.hasNext())
			points.add(it.next().getValue());
		
		return points;
	}

	/*******************/
	/** Zones Methods **/
	/*******************/

	public float getEndzoneOffset() {
		return offset_endzone;
	}

	public float getEndzoneLength() {
		return length_endzone;
	}

	public int getZoneId() {
		return this.unpredictedZones.getFirst().getId();
	}

	public LinkedList<Zone> getPredictedZones() {
		return this.predictedZones;
	}

	public boolean hasUnpredictedZone() {
		return this.unpredictedZones.size() > 0;
	}

	public void nextZone(Prediction pred, Prediction rawPred) {
		unpredictedZones.getFirst().setPrediction(pred);
		unpredictedZones.getFirst().setRawPrediction(rawPred);
		this.predictedZones.add(unpredictedZones.removeFirst());
		loadZone();
	}

	private void loadZone() {
		if (!hasUnpredictedZone()) {
			this.offset_endzone = xMax;
			this.length_endzone = 0;
			return;
		}
		this.offset_endzone = (float) unpredictedZones.getFirst().getStart();
		this.length_endzone = unpredictedZones.getFirst().isEndzone() ? -1
				: (float) (unpredictedZones.getFirst().getEnd() - unpredictedZones
						.getFirst().getStart());
	}
	
	public Zone getEndzone()
	{
		return unpredictedZones.getFirst();
	}
	
	public int getNbrUnpredictedZone()
	{
		return unpredictedZones.size();
	}
}
