package be.tfe.android;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import be.tfe.android.curve.Curve;
import be.tfe.android.curve.Zone;
import be.tfe.android.curveviewer.*;
import be.tfe.android.misc.enu.GameMode;
import be.tfe.android.misc.enu.PredictionInput;
import be.tfe.android.misc.enu.PredictionType;
import be.tfe.android.misc.utils.AppConfig;
import be.tfe.android.misc.utils.AppUtils;
import be.tfe.android.misc.utils.Callback;
import be.tfe.android.misc.utils.CircularBuffer;
import be.tfe.android.misc.utils.CurveRequest;
import be.tfe.android.misc.utils.CurveResponse;
import be.tfe.android.tasks.GetPredictionTask;
import be.tfe.android.tasks.SendPredictionTask;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class AgainstclockGamemodeActivity extends Activity {

	private PredictionType predictionType;
	private PredictionInput predictionInput;
	private GameMode gameMode;
	private boolean trainingMode;
	
	private float score = 0;
	private String id_serie = "";

	private CurveView curveView;
	private CurvePredictor curvePredictor = null;

	private String TAG = "ACGA";
	private ProgressBar progressBar;
	private CountDownTimer countDownTimer;
	private int currentGameLength = GAMELENGTH;
	private TextView tvScore, tvCurveRemaining, tvLevel;
	
	private ImageButton btn_skip;
	Drawable draw_skip_red, draw_skip_green;
	
	private static final int GAMELENGTH = 60000;
	
	// Curve
	private ArrayList<Curve> predictedCurves;
	private CircularBuffer<Curve> unpredictedCurves;

	private Dialog nopredictionDialog, endOfGameDialog, endOfGroupDialog, prepareDialog, timeoutDialog, quitDialog, endOfGroupTrainingDialog, endOfRandomGroupDialog, levelDialog, serverUnavailableDialog;
	
	private int id_user = -1;
	
	private SeekBar confidence_seekBar;
	private TextView confidence_textview_gain, confidence_textview_loss;
	private boolean isLastGroup, isFinished = false;
	private int currentGroupPosition;
	private boolean isRandom;
	
	private int nbrPredicted;
	
	private boolean hasEnteredConfidence = false;

	/***********************************/
	/***  Activity Life Cycle Methods  */
	/***********************************/
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_againstclock_gamemode);
		
		predictedCurves 	= new ArrayList<Curve>();
		unpredictedCurves 	= new CircularBuffer<Curve>();

		// Retreiving the main layout components
		//predictorLayout = (FrameLayout) findViewById(R.id.predictorFrame);
		curveView 		= (CurveView) findViewById(R.id.curveView);
		progressBar 	= (ProgressBar) findViewById(R.id.progressBar);
		tvScore 		= (TextView) findViewById(R.id.tv_score);
		tvCurveRemaining= (TextView) findViewById(R.id.tv_curveremaining);
		tvLevel 		= (TextView) findViewById(R.id.tv_level);
		
		refreshScore();
		
		// Listeners on the buttons
		btn_skip = (ImageButton) findViewById(R.id.button_skip);
		btn_skip.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				nextCurve();
			}});
		
		ImageButton btn_reset = (ImageButton) findViewById(R.id.button_reset);
		btn_reset.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				curveView.resetPredictor();
				curveView.redraw();
			}});

		ImageButton btn_zoomin = (ImageButton) findViewById(R.id.button_zoomin);
		btn_zoomin.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				curveView.zoomIn();
				curveView.redraw();
			}});
		
		ImageButton btn_zoomout = (ImageButton) findViewById(R.id.button_zoomout);
		btn_zoomout.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				curveView.zoomOut();
				curveView.redraw();
			}});
		
		ImageButton btn_valid = (ImageButton) findViewById(R.id.button_valid);
		btn_valid.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				if(!unpredictedCurves.top().hasUnpredictedZone())
					return;
				if(!curvePredictor.hasPrediction())
				{
					showNopredictionDialog();
					return;
				}
				
				// Send the prediction and load the next zone
				sendPrediction();
				nextZone();
			}});
		
		
		Builder builder = new AlertDialog.Builder(this);
				
		// Create the quit dialog
		builder.setTitle(getString(R.string.play_modal_quit_title));
		builder.setMessage(getString(R.string.play_modal_quit_message));
		
		builder.setPositiveButton(getString(R.string.play_modal_quit_yes), new android.content.DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				quitDialog.hide();
				goToPlayScreen();
			}});
		builder.setNegativeButton(getString(R.string.play_modal_quit_no), new android.content.DialogInterface.OnClickListener(){

			public void onClick(DialogInterface arg0, int arg1) {
				quitDialog.hide();
				startCountDownTimer();
			}});
		
		quitDialog = builder.create();
		
		// Create the endofgame dialog
		builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.play_modal_endofgame_title));
		builder.setMessage(getString(R.string.play_modal_endofgame_message));
		
		builder.setPositiveButton(getString(R.string.play_modal_endofgame_back), new android.content.DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				endOfGameDialog.hide();
				goToPlayScreen();
			}});
		endOfGameDialog = builder.create();
		
		// Create the endofgroup dialog
		builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.play_modal_endofgroup_title));
		builder.setMessage(getString(R.string.play_modal_endofgroup_message));
		
		builder.setPositiveButton(getString(R.string.play_modal_endofgroup_nextlevel), new android.content.DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				endOfGroupDialog.hide();
				
				// Update the stored current level
				SharedPreferences preferences = getSharedPreferences(AppConfig.PREFS_NAME, 0);
				
				String suffix = predictionInput.toString().toLowerCase(Locale.ENGLISH);
				int storedLevel = preferences.getInt("position_"+suffix, 0);
				
				if(storedLevel == currentGroupPosition)
				{
					SharedPreferences.Editor editor = preferences.edit();
		        	editor.putInt("position_"+suffix, currentGroupPosition + 1);
		            editor.commit();
				}
				currentGroupPosition++;
				
				Callback cb = new Callback()
				{
					public void exec() {
						refreshLevel();
						showLevelDialog();
						refreshScore();
						loadTopCurve();
					}
				};
				try {
					fetchCurves();
				} catch (ServerUnavailableException e) {
					showServerUnavailableDialog(cb);
					return;
				}
				cb.exec();
			}});
		
		builder.setNeutralButton(getString(R.string.play_modal_endofgroup_samelevel), new android.content.DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				endOfGroupDialog.hide();
				
				Callback cb = new Callback()
				{
					public void exec() {
						showLevelDialog();
						refreshScore();
						loadTopCurve();
					}
				};
				try {
					fetchCurves();
				} catch (ServerUnavailableException e) {
					showServerUnavailableDialog(cb);
					return;
				}
				cb.exec();
				
			}});
		
		builder.setNegativeButton(getString(R.string.play_modal_endofgroup_back), new android.content.DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				endOfGroupDialog.hide();
				goToPlayScreen();
			}});
		endOfGroupDialog = builder.create();
		
		
		// Create the endofgroup dialog specific to the training mode
		builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.play_modal_endofgrouptraining_title));
		builder.setMessage(getString(R.string.play_modal_endofgrouptraining_message));
		
		builder.setPositiveButton(getString(R.string.play_modal_endofgrouptraining_disable), new android.content.DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				endOfGroupTrainingDialog.hide();
				trainingMode = false;
				Callback cb = new Callback()
				{
					public void exec() {
						loadTopCurve();
					}
				};
				try {
					fetchCurves();
				} catch (ServerUnavailableException e) {
					showServerUnavailableDialog(cb);
					return;
				}
				cb.exec();
			}
			});
		
		builder.setNeutralButton(getString(R.string.play_modal_endofgrouptraining_continue), new android.content.DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				endOfGroupTrainingDialog.hide();
				Callback cb = new Callback()
				{
					public void exec() {
						loadTopCurve();
					}
				};
				try {
					fetchCurves();
				} catch (ServerUnavailableException e) {
					showServerUnavailableDialog(cb);
					return;
				}
				cb.exec();
			}});
		
		builder.setNegativeButton(getString(R.string.play_modal_endofgrouptraining_back), new android.content.DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				endOfGroupTrainingDialog.hide();
				goToPlayScreen();
			}});
		endOfGroupTrainingDialog = builder.create();
		
		// Create the endofgroup dialog specific to the random mode
		builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.play_modal_endofgrouprandom_title));
		builder.setMessage(getString(R.string.play_modal_endofgrouprandom_message));
		
		builder.setPositiveButton(getString(R.string.play_modal_endofgrouprandom_stay), new android.content.DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				endOfRandomGroupDialog.hide();
				Callback cb = new Callback()
				{
					public void exec() {
						loadTopCurve();
					}
				};
				try {
					fetchCurves();
				} catch (ServerUnavailableException e) {
					showServerUnavailableDialog(cb);
					return;
				}
				cb.exec();
			}
			});
		
		builder.setNegativeButton(getString(R.string.play_modal_endofgrouprandom_back), new android.content.DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				endOfRandomGroupDialog.hide();
				goToPlayScreen();
			}});
		endOfRandomGroupDialog = builder.create();

		
		// Initialize the confidence part of the interface
		confidence_seekBar = (SeekBar) findViewById(R.id.seekbar_confidence);
		confidence_textview_gain = (TextView) findViewById(R.id.confidence_gain);
		confidence_textview_loss = (TextView) findViewById(R.id.confidence_loss);
		
		confidence_seekBar.setProgress(50);
		confidence_seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{

			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				confidenceRefreshTextview();
				hasEnteredConfidence = true;
			}

			public void onStartTrackingTouch(SeekBar arg0) { }

			public void onStopTrackingTouch(SeekBar arg0) {	}
			
		});
		
		ShapeDrawable thumb = new ShapeDrawable( new RectShape() );
		thumb.getPaint().setColor( 0xFF0000 );
		thumb.setIntrinsicHeight( 80 );
		thumb.setIntrinsicWidth( 30 );
		confidence_seekBar.setThumb( thumb );
		//confidence_seekBar.setThumb(getResources().getDrawable(R.drawable.cursor));
		
		SharedPreferences preferences = getSharedPreferences(AppConfig.PREFS_NAME, 0);
        id_user = preferences.getInt("id_user", -1);
        
        // Load bitmaps for the skip button's behavior
        draw_skip_red = this.getResources().getDrawable(R.drawable.button_skip_red);
        draw_skip_green = this.getResources().getDrawable(R.drawable.button_skip_green);
	}
	
	private void showServerUnavailableDialog(final Callback cbIfRetryOk)
	{
		// Build the dialog that tells the user the server is not available
	 	Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.error_servernotavailable_title));
		builder.setMessage(getString(R.string.error_servernotavailable));
		
		builder.setPositiveButton(getString(R.string.button_ok), new android.content.DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				serverUnavailableDialog.hide();
				goToPlayScreen();
			}});
		
		builder.setNegativeButton(getString(R.string.button_retry), new android.content.DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				serverUnavailableDialog.hide();
				try {
					fetchCurves();
				} catch (ServerUnavailableException e) {
					// TODO Auto-generated catch block
					showServerUnavailableDialog(cbIfRetryOk);
					return;
				}
				cbIfRetryOk.exec();
			}});
		
		serverUnavailableDialog = builder.create();
		serverUnavailableDialog.show();
	}
	
	public void onPause() {
		super.onPause();
		//Log.e(TAG, "On pause");
		if(this.gameMode == GameMode.AGAINSTCLOCK)
			cancelCountDownTimer();
	}
	
	public void onBackPressed() {
	    this.quitDialog.show();
	}

	public void onResume() {
		super.onResume();
		//Log.e(TAG, "On resume");
		
		// Fetching the intent to get infos for the type of predictor
		Intent intent = getIntent();
		this.currentGroupPosition = intent.getIntExtra("level", -1);
		
		this.isRandom = this.currentGroupPosition == -1;
		this.trainingMode = intent.getBooleanExtra("training", false);
		this.predictionInput = PredictionInput.values()[intent.getIntExtra(
				"predictionInput", PredictionInput.VALUE.ordinal())];
		this.predictionType = PredictionType.values()[intent.getIntExtra(
				"predictionType", PredictionType.ATTHEEND.ordinal())];
		
		this.gameMode = GameMode.values()[intent.getIntExtra(
				"gameMode", GameMode.NORMAL.ordinal())];
		
		if(this.gameMode != GameMode.AGAINSTCLOCK)
			this.progressBar.setVisibility(View.GONE);
		else
			this.progressBar.setVisibility(View.VISIBLE);

		// Determining the class of the adequate view and instantiate it
		switch (this.predictionInput) {
		case TREND:
			curvePredictor = new TrendPredictor(this.curveView);
			break;
		case PM:
			curvePredictor = new PMPredictor(this.curveView);
			break;
		case VALUE:
		default:
			curvePredictor = new ValuePredictor(this.curveView);
			break;

		}
		this.curveView.setPredictor(curvePredictor);
		
		// Fetch curves, according to the game and prediction types
		Callback cb = new Callback(){
			public void exec() {
				if(isFinished)
				{
					showEndOfGameWindow();
					return;
				}
				else if(gameMode == GameMode.AGAINSTCLOCK)
					showPrepareDialog();
				else
					showLevelDialog();
				loadTopCurve();
				refreshLevel();
				confidenceRefreshTextview();
				refreshScore();
			}
		};
		
		try {
			this.fetchCurves();
		} catch (ServerUnavailableException e) {
			showServerUnavailableDialog(cb);
			return;
		}
		cb.exec();
		
	}

	public void onRestart() {
		super.onRestart();
		//Log.e(TAG, "On restart");
	}
	
	public void onStop() {
		super.onStop();
		//Log.e(TAG, "On stop");
	}
	
	/********************************/
	/***  Curve Navigation Methods  */
	/********************************/
	
	public void nextZone()
	{
		if(AppConfig.DEBUG)
			Log.i(TAG, "nextPrediction");
				
		Curve cc = unpredictedCurves.top();
		cc.nextZone(this.curveView.getPrediction(), this.curveView.getRawPrediction());

		if(!cc.hasUnpredictedZone())
		{
			btn_skip.setImageDrawable(draw_skip_green);
			stopCountDownTimer();
			if(this.predictionType == PredictionType.PROGRESSIVE)
				Toast.makeText(this, getString(R.string.play_toast_allzonepredicted), Toast.LENGTH_SHORT).show();
		}
		else
		{
			btn_skip.setImageDrawable(draw_skip_red);
			startCountDownTimer();
		}
		
		curveView.redraw();
		curvePredictor.reset();
		confidence_seekBar.invalidate();
		confidenceRefreshTextview();
		refreshScore();
	}
	

	private void confidenceRefreshTextview()
	{
		if(!curveView.getCurve().hasUnpredictedZone())
			return;
		Zone z = this.curveView.getCurve().getEndzone();
		
		float gain = 0;
		float loss = 0;
		
		if(this.predictionInput == PredictionInput.VALUE)
		{
			gain = z.value_gain;
			loss = z.value_loss;
		}
		else if(this.predictionInput == PredictionInput.PM)
		{
			gain = z.pm_gain;
			loss = z.pm_loss;
		}
		else if(this.predictionInput == PredictionInput.TREND)
		{
			gain = z.trend_gain;
			loss = z.trend_loss;
		}
		
		float factor = confidenceGetFactor();
		
		gain = (float) (Math.floor(gain * factor * 100) / 100);
		loss = (float) (Math.floor(loss * factor * 100) / 100);
		
		String string_loss = loss == 0 ? "0" : "- " + String.valueOf(loss);
		confidence_textview_gain.setText(Html.fromHtml(String.format(getString(R.string.play_confidence_gain), String.valueOf(gain))));
		confidence_textview_loss.setText(Html.fromHtml(String.format(getString(R.string.play_confidence_loss), string_loss)));
	}
	
	private float confidenceGetFactor()
	{
		float minFactor = 0.1f;
		float maxFactor = 2.0f;
		
		float length = 100;
		float conf = (float) confidence_seekBar.getProgress();
		
		return (conf * (maxFactor - minFactor) / length + minFactor);
	}
	
	private Prediction getPrediction()
	{
		this.curvePredictor.setZoom(curveView.getZoom());
		this.curvePredictor.setHeightRatio(curveView.getHeightRatio());
		this.curvePredictor.setYMin(curveView.getYMin());
		
		Prediction p = this.curvePredictor.getPrediction();
		
		p.setConfidence(confidence_seekBar.getProgress());
		p.setIdZone(curveView.getCurve().getZoneId());
		p.setPredictionType(this.predictionType);
		p.setPredictionInput(this.predictionInput);
		p.setTraining(this.trainingMode);
		p.setIdUser(id_user);
		p.setIdSerie(id_serie);
		p.setGameMode(this.gameMode);
		p.setLevel(this.currentGroupPosition);
		p.setHasEnteredConfidence(this.hasEnteredConfidence);
		
		if(AppConfig.DEBUG)
			Log.e(TAG, "id_user = " + String.valueOf(id_user));
		
		return p;
	}
	private void sendPrediction()
	{
		Prediction p = getPrediction();
		float gain = (float) (Math.floor(p.getScore() * confidenceGetFactor() * 100) / 100);
		this.curveView.getCurve().getEndzone().setScore(gain);
		score += gain;
		refreshScore();
		
		// Store the new score in the shared preferences
     	SharedPreferences preferences = getSharedPreferences(AppConfig.PREFS_NAME, 0);
     	float globalScore = preferences.getFloat("score", 0) + gain;
		SharedPreferences.Editor editor = preferences.edit();
	   	editor.putFloat("score", globalScore);
        editor.commit();
		
        // Send the prediction to the server
		new SendPredictionTask().execute(p);
		nbrPredicted++;
	}
	private void refreshScore()
	{
		double roundedScore = Math.round(this.score * 100)/100.;
		tvScore.setText(String.format(getString(R.string.play_status_score, String.valueOf(roundedScore))));
		
		if(this.gameMode == GameMode.AGAINSTCLOCK)
			this.tvCurveRemaining.setText("");
		else if(unpredictedCurves.size() == 1) 
			this.tvCurveRemaining.setText(getString(R.string.play_status_curveremaining_one));
		else
			this.tvCurveRemaining.setText(String.format(getString(R.string.play_status_curveremaining_more), String.valueOf(unpredictedCurves.size())));
	}
	
	private void refreshLevel()
	{
		if(this.gameMode == GameMode.AGAINSTCLOCK)
		{
			tvLevel.setText("");
			return;
		}
		String levelText = this.isRandom ? getString(R.string.play_status_random) : String.format(getString(R.string.play_status_level), String.valueOf(this.currentGroupPosition+1));
		tvLevel.setText(levelText);
	}
	
	
	public void nextCurve()
	{
		Curve cur = unpredictedCurves.top();
		
		if(cur.getNbrUnpredictedZone() == 0)
		{
			if(unpredictedCurves.size() == 1)
			{
				if(this.isRandom)
				{
					showEndOfRandomGroupWindow();
				}
				else if(this.isLastGroup)
				{
					showEndOfGameWindow();
					return;
				}
				else
					showEndOfGroupWindow();
			}
			else
			{
				this.predictedCurves.add(unpredictedCurves.removeTop());
				loadTopCurve();
			}
		}
		else
			curveView.setCurve(unpredictedCurves.next());
		curveView.predraw();
		curveView.redraw();
		curvePredictor.reset();
		refreshScore();
		
		btn_skip.setImageDrawable(draw_skip_red);
		confidence_seekBar.setProgress(50);
		confidence_seekBar.invalidate();
		confidenceRefreshTextview();

		this.startCountDownTimer();
	}
	
	private void showLevelDialog()
	{
		// Make the window that shows the current level.
		Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("");
		
		String message = this.isRandom 	? String.format(getString(R.string.play_modal_level_random), String.valueOf(this.unpredictedCurves.size()))
										: String.format(getString(R.string.play_modal_level_message), String.valueOf(this.currentGroupPosition + 1), String.valueOf(this.unpredictedCurves.size()));
		builder.setMessage(message);
		
		builder.setPositiveButton(getString(R.string.play_modal_level_ok), new android.content.DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				levelDialog.hide();
			}});
		levelDialog = builder.create();
		levelDialog.show();
	}
	
	private void showNopredictionDialog()
	{
		// Create the "no prediction entered" dialog
		Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.play_modal_noprediction_title));
		builder.setMessage(getString(R.string.play_modal_noprediction_message));
		
		builder.setPositiveButton(getString(R.string.button_ok), new android.content.DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				nopredictionDialog.hide();
			}});

		nopredictionDialog = builder.create();
		nopredictionDialog.show();
	}
	
	
	private void showPrepareDialog() {
		//Log.e(TAG, "SHOWPREPAREDIALOG");
		this.id_serie = AppUtils.generateUID(id_user);
		nbrPredicted = 0;
		this.score = 0;
		this.progressBar.setProgress(0);
		this.refreshScore();
		
		// Create the "prepare to predict" dialog for the againsttheclock gamemode
		Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.play_modal_prepare_title));
		builder.setMessage(getString(R.string.play_modal_prepare_message));
		
		builder.setPositiveButton(getString(R.string.button_ok), new android.content.DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				prepareDialog.hide();
				restartCountDownTimer();
			}});
		
		prepareDialog = builder.create();
		prepareDialog.show();
	}
	
	public boolean loadTopCurve()
	{
		if(unpredictedCurves.size() == 0)
			return false;
		
		curveView.setCurve(unpredictedCurves.top());
		// Redraw
		curveView.predraw();
		curveView.redraw();
		this.hasEnteredConfidence = false;
		return true;
	}
	
	/*****************************/
	/***  Modal Windows Methods  */
	/*****************************/
	
	private void showEndOfGroupWindow() {
		stopCountDownTimer();
		
		if(trainingMode)
			endOfGroupTrainingDialog.show();
		else
			endOfGroupDialog.show();
	}
	
	private void showEndOfGameWindow() {
		stopCountDownTimer();
		endOfGameDialog.show();
	}
	
	private void showEndOfRandomGroupWindow() {
		stopCountDownTimer();
		endOfRandomGroupDialog.show();
	}
	
	private void showTimeoutWindow()
	{
		
		String message = nbrPredicted == 1 	? getString(R.string.play_modal_timeout_message_one)
						: (nbrPredicted == 0) ? getString(R.string.play_modal_timeout_message_none) 
												: String.format(getString(R.string.play_modal_timeout_message_multiple), String.valueOf(nbrPredicted));
		
		// Create the timeout dialog
		Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.play_modal_timeout_title));
		builder.setMessage(message);
		
		builder.setPositiveButton(getString(R.string.play_modal_timeout_tryagain), new android.content.DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				timeoutDialog.hide();
				
				Callback cb = new Callback()
				{
					public void exec() {
						
						if(isFinished)
						{
							showEndOfGameWindow();
							return;
						}
						refreshScore();
						loadTopCurve();
						showPrepareDialog();
					}
					
				};
				
				try {
					fetchCurves();
				} catch (ServerUnavailableException e) {
					showServerUnavailableDialog(cb);
					return;
				}
				
				cb.exec();
				
				
			}
			});
		builder.setNegativeButton(getString(R.string.play_modal_timeout_quit), new android.content.DialogInterface.OnClickListener(){

			public void onClick(DialogInterface arg0, int arg1) {
				timeoutDialog.hide();
				goToPlayScreen();
			}});
		
		timeoutDialog = builder.create();
		timeoutDialog.show();
	}
	
	/*****************************/
	/***  Go to Screen-Methods  */
	/*****************************/
	
	public void goToPlayScreen()
	{
		/*
		Intent intent = new Intent(this, PlaymenuActivity.class);
    	startActivity(intent);
    	*/
		if(timeoutDialog != null)
			timeoutDialog.hide();
		this.finish();
	}
	
	/*********************************/
	/***  CountDown Related Methods  */
	/*********************************/
	public void startCountDownTimer()
	{
		if(this.gameMode != GameMode.AGAINSTCLOCK)
			return;
		//Log.i(TAG, "------------ STARTING COUNTDOWN TIMER");
		countDownTimer = new CountDownTimer(currentGameLength, (int) GAMELENGTH / 100)
		{
			public void onFinish() {
				progressBar.setProgress(100);
				showTimeoutWindow();
			}
			public void onTick(long millisUntilFinished) {
				int percent = (int)((GAMELENGTH - millisUntilFinished) / ((float)GAMELENGTH) * 100);
				progressBar.setProgress(percent);
				currentGameLength = (int) millisUntilFinished;
			}
		};
		countDownTimer.start();
	}
	
	public void restartCountDownTimer()
	{
		if(this.gameMode != GameMode.AGAINSTCLOCK)
			return;
		stopCountDownTimer();
		currentGameLength = GAMELENGTH;
		startCountDownTimer();
	}
	
	public void stopCountDownTimer()
	{
		if(this.gameMode != GameMode.AGAINSTCLOCK)
			return;
		cancelCountDownTimer();
	}
	
	private void cancelCountDownTimer()
	{
		if(countDownTimer == null)
			return;
		countDownTimer.cancel();
	}
	/**************************************/
	/***  Curve Fetching-Related Methods  */
	/**
	 * @throws ServerUnavailableException ************************************/
	
	private void fetchCurves() throws ServerUnavailableException
	{
		if(!this.isNetworkAvailable() || !AppUtils.findServer())
		{
			//this.showServerUnavailableDialog();
			throw new ServerUnavailableException();
		}
		
		if(AppConfig.DEBUG)
			Log.i(TAG, "Network available");
		loadCurvesFromInternet();
	}
	
	private void loadCurvesFromInternet()
	{
		// Retreiving a curve
		// From http://stackoverflow.com/questions/5769717/how-can-i-get-an-http-response-body-as-a-string-in-java
		if(AppConfig.DEBUG)
			Log.i(TAG, "Loading curves from the internet");
		
		CurveRequest cr 	= new CurveRequest();
		cr.id_user 			= this.id_user;
		cr.predictionInput 	= this.predictionInput;
		cr.training 		= this.trainingMode;
		cr.predictionType 	= this.predictionType;
		cr.gameMode 		= this.gameMode;
		cr.level			= this.currentGroupPosition;
		cr.random			= this.isRandom;
		
		GetPredictionTask spt = new GetPredictionTask();
		spt.execute(cr);
		CurveResponse response = null;
		try {
			response = spt.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		if(response == null || response.isFinished) 
		{
			this.isFinished = true;
			return;
		}
		else
			this.isFinished = false;
		
		this.isLastGroup = response.nbr_group_after == 0;
		
		Iterator<Curve> it = response.curves.iterator();
		unpredictedCurves = new CircularBuffer<Curve>();
		while(it.hasNext())
		{
			Curve c = it.next();
			//Log.i(TAG, "Inserting " + c.getName());
			
			if(this.predictionType == PredictionType.ATTHEEND)
				c.removeZonesButLast();
			this.unpredictedCurves.add(c);
		}
	}
	
	public boolean isNetworkAvailable()
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}
	
}
