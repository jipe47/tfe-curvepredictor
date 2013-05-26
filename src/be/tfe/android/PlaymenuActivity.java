package be.tfe.android;

import java.util.ArrayList;
import java.util.List;

import be.tfe.android.misc.enu.GameMode;
import be.tfe.android.misc.enu.PredictionInput;
import be.tfe.android.misc.enu.PredictionType;
import be.tfe.android.misc.utils.AppConfig;
import be.tfe.android.misc.utils.AppUtils;
import be.tfe.android.misc.utils.ButtonGroup;


import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

@SuppressWarnings("unused")
public class PlaymenuActivity extends Activity {
	
	private ArrayList<ButtonGroup> buttonGroups;
	
	private TextView tv1, tv3;
	
	private PredictionInput predictioninput = PredictionInput.TREND;
	
	// Should has been used for a progressive mode
	private PredictionType predictiontype = PredictionType.ATTHEEND; 
	private GameMode gamemode = GameMode.NORMAL;
	
	private Dialog serverUnavailableDialog;
	
	private Button startButton;
	private TextView textviewLevel;
	private Spinner spinnerLevel;
	
	private String TAG = "PMA";
	
	private int position_trend, position_pm, position_value;
	
	private Button b1, b2, b5, b6, b7;
	
    private void refreshLevelSpinner() {
    	
    	int position = getPosition();
    	
    	if(this.gamemode == GameMode.AGAINSTCLOCK)
    		position--;
    	
    	List<String> array = new ArrayList<String>();
    	array.add(getString(R.string.playmenu_spinner_random));
    	for(; position >= 0 ; position--)
    		array.add(String.format(getString(R.string.playmenu_spinner_level), String.valueOf(position + 1)));
    	
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array);
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spinnerLevel.setAdapter(adapter);
    	spinnerLevel.setSelection(array.size() == 1 ? 0 : 1);
    	adapter.notifyDataSetChanged();
    	spinnerLevel.invalidate();
	}

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_playmenu);
        
        // Fetch description textviews
        tv1 = (TextView) findViewById(R.id.description_gamemode);
        tv3 = (TextView) findViewById(R.id.description_predictioninput);
        spinnerLevel = (Spinner) findViewById(R.id.spinnerLevel);
        textviewLevel = (TextView) findViewById(R.id.textViewLevel);
        
        // Creating button groups with only one checked button per group
    	buttonGroups = new ArrayList<ButtonGroup>();
    	
    	b1 = (Button) findViewById(R.id.button_gamemode_normal);
    	b2 = (Button) findViewById(R.id.button_gamemode_againstclock);
    	
		b5 = (Button) findViewById(R.id.button_predictioninput_value);
		b6 = (Button) findViewById(R.id.button_predictioninput_pm);
		b7 = (Button) findViewById(R.id.button_predictioninput_trend);
		
		ButtonGroup g1 = new ButtonGroup(this.getResources());
    	g1.addButton(b1);
    	g1.addButton(b2);
    	buttonGroups.add(g1);
    	
    	ButtonGroup g3 = new ButtonGroup(this.getResources());
    	g3.addButton(b7);
    	g3.addButton(b6);
    	g3.addButton(b5);
    	buttonGroups.add(g3);
    	
    	b1.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		       buttonGroups.get(0).selectButton(0);
		       tv1.setText(getString(R.string.playmenu_description_gamemode_normal));
		       gamemode = GameMode.NORMAL;
		       refreshInterface();
		    }
		});
    	b2.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		       buttonGroups.get(0).selectButton(1);
		       tv1.setText(getString(R.string.playmenu_description_gamemode_againstclock));
		       gamemode = GameMode.AGAINSTCLOCK;
		       refreshInterface();
		    }
		});
    	b5.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		       buttonGroups.get(1).selectButton(2);
		       tv3.setText(getString(R.string.playmenu_description_predictioninput_value));
		       predictioninput = PredictionInput.VALUE;
		       refreshInterface();
		    }
		});
    	b6.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		       buttonGroups.get(1).selectButton(1);
		       tv3.setText(getString(R.string.playmenu_description_predictioninput_pm));
		       predictioninput = PredictionInput.PM;
		       refreshInterface();
		    }
		});
    	b7.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		       buttonGroups.get(1).selectButton(0);
		       tv3.setText(getString(R.string.playmenu_description_predictioninput_trend));
		       predictioninput = PredictionInput.TREND;
		       refreshInterface();
		    }
		});
    	
    	startButton = (Button) findViewById(R.id.button_menu_play);
    	
    	startButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
			       startGame();
			    }
			});
    }
    
    public void onResume()
    {
    	super.onResume();
		SharedPreferences preferences = getSharedPreferences(AppConfig.PREFS_NAME, 0);
		
		position_trend 	= preferences.getInt("position_trend", 0);
		position_value 	= preferences.getInt("position_value", 0);
		position_pm 	= preferences.getInt("position_pm", 0);
		refreshInterface();
    }
    
    void refreshInterface()
    {
    	refreshLevelSpinner();
    	
    	if(gamemode == GameMode.AGAINSTCLOCK)
    	{
    		spinnerLevel.setVisibility(View.GONE);
    		textviewLevel.setVisibility(View.GONE);
    	}
    	else
    	{
    		spinnerLevel.setVisibility(View.VISIBLE);
    		textviewLevel.setVisibility(View.VISIBLE);
    	}
    }
    
    void goToStartScreen()
    {
    	this.finish();
    }
    
    private void showServerUnavailableDialog()
    {
    	// Build the dialog that tells the user the server is not available
     	Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.error_servernotavailable_title));
		builder.setMessage(getString(R.string.error_servernotavailable));
		
		builder.setPositiveButton(getString(R.string.button_ok), new android.content.DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				serverUnavailableDialog.hide();
			}});
		
		builder.setNegativeButton(getString(R.string.button_retry), new android.content.DialogInterface.OnClickListener(){
			public void onClick(DialogInterface arg0, int arg1) {
				serverUnavailableDialog.hide();
				startGame();
			}});
		
		serverUnavailableDialog = builder.create();
		serverUnavailableDialog.show();
    }
    
    private void startGame()
    {
    	
    	if(!AppUtils.findServer())
    	{
    		showServerUnavailableDialog();
    		return;
    	}
    	
    	Intent intent = null;
    	
    	// Determine the activity based on the gamemode
		intent = new Intent(this, AgainstclockGamemodeActivity.class);
    	
    	// Communicate the prediction type and input and if the training mode is enabled (or not)
		
    	//intent.putExtra("predictionType", this.predictiontype.ordinal());
    	intent.putExtra("predictionType", PredictionType.ATTHEEND.ordinal());
    	intent.putExtra("predictionInput", this.predictioninput.ordinal());
    	intent.putExtra("gameMode", this.gamemode.ordinal());
    	intent.putExtra("training", ((CheckBox) findViewById(R.id.checkbox_training)).isChecked());
    	
    	int level = this.spinnerLevel.getSelectedItemPosition() == 0 ? -1 : this.getPosition() + 1 - this.spinnerLevel.getSelectedItemPosition();
    	
    	intent.putExtra("level", level);
    	
    	startActivity(intent);
    }
    
    private int getPosition()
    {
    	// TODO Use an array of integers?
    	switch(this.predictioninput)
    	{
    	case TREND:
    		return position_trend;
    	case PM:
    		return position_pm;
    	case VALUE:
    		return position_value;
    	}
    	return -1;
    }
}
