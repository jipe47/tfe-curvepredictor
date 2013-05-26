package be.tfe.android;

import java.util.concurrent.ExecutionException;

import be.tfe.android.homeview.HomeLogo;
import be.tfe.android.misc.utils.AppConfig;
import be.tfe.android.misc.utils.AppUtils;
import be.tfe.android.misc.utils.UserInfoPrimitive;
import be.tfe.android.tasks.UserInfoTask;
import be.tfe.android.tasks.UserRequest;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class MainActivity extends Activity {
	
	private static final String TAG = "Main";
	private Button playButton, statButton, aboutButton, helpButton, exitButton;
	private boolean hasReadHelp;
	private HomeLogo logo;
	
	private Dialog serverUnavailableDialog, firstPlayDialog;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_home);
        
        logo = (HomeLogo) findViewById(R.id.textView1);
        logo.redraw();
        
        
        playButton = (Button) findViewById(R.id.button_play);
    	
    	playButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	
				if(hasReadHelp)
					goToPlayScreen();
				else
					showFirstPlayDialog();
			    }
			});
    	
    	statButton = (Button) findViewById(R.id.button_stat);
     	statButton.setOnClickListener(new View.OnClickListener() {
 		    public void onClick(View v) {
 			       goToStatScreen();
 			    }
 			});
     	
     	aboutButton = (Button) findViewById(R.id.button_about);
     	aboutButton.setOnClickListener(new View.OnClickListener() {
 		    public void onClick(View v) {
 			       goToAboutScreen();
 			    }
 			});
     	
     	helpButton = (Button) findViewById(R.id.button_help);
     	helpButton.setOnClickListener(new View.OnClickListener() {
 		    public void onClick(View v) {
 			       goToHelpScreen();
 			    }
 			});
     	
     	exitButton = (Button) findViewById(R.id.button_exit);
     	exitButton.setOnClickListener(new View.OnClickListener(){
     		public void onClick(View v)
     		{
     			finish();
     		}
     	});
    }
    
    public void onResume()
    {
    	super.onResume();
    	
    	// Retreive the boolean indicating if the user read at least one time the help
 		SharedPreferences preferences = getSharedPreferences(AppConfig.PREFS_NAME, 0);
        hasReadHelp = preferences.getBoolean("hasReadHelp", false);
    	        
    	connectToServer();
    }
    
    private void markHelpAsRead() {
		hasReadHelp = true;
		SharedPreferences preferences = getSharedPreferences(AppConfig.PREFS_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hasReadHelp", true);
        editor.commit();
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
				connectToServer();
			}});
		
		serverUnavailableDialog = builder.create();
		serverUnavailableDialog.show();
    }
    
    private void showFirstPlayDialog()
    {
    	// Create the "no prediction entered" dialog
 		Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(getString(R.string.home_modal_title));
 		builder.setMessage(getString(R.string.home_modal_message));

 		builder.setPositiveButton(getString(R.string.home_modal_play), new android.content.DialogInterface.OnClickListener(){
 			public void onClick(DialogInterface arg0, int arg1) {
 				firstPlayDialog.hide();
 				markHelpAsRead();
 				goToPlayScreen();
 			}
			});
 		

 		builder.setNegativeButton(getString(R.string.home_modal_help), new android.content.DialogInterface.OnClickListener(){
 			public void onClick(DialogInterface arg0, int arg1) {
 				firstPlayDialog.hide();
 				goToHelpScreen();
 				markHelpAsRead();
 				hasReadHelp = true;
 			}});

 		firstPlayDialog = builder.create();
    	firstPlayDialog.show();
    }
    
    private void connectToServer()
    {
    	// Check if the server is available
    	if(!AppUtils.findServer())
        {
    		playButton.setEnabled(false);
    		statButton.setEnabled(false);
    		aboutButton.setEnabled(false);
    		helpButton.setEnabled(false);
    		showServerUnavailableDialog();
    		return;
        }
		 else
		 {
			loadUserInfo();
			playButton.setEnabled(true);
    		statButton.setEnabled(true);
    		aboutButton.setEnabled(true);
    		helpButton.setEnabled(true);
		 }
    }
    
    private void loadUserInfo()
    {

     	// Load user info
     	SharedPreferences preferences = getSharedPreferences(AppConfig.PREFS_NAME, 0);
     	
        int id_user = preferences.getInt("id_user", -1);
        
        // Get the user's country
        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        String country = tm.getNetworkCountryIso();

        UserRequest ur = new UserRequest();
        ur.id = id_user;
        ur.country = country;
        ur.account = AppUtils.getGoogleAccount(this);
        
        if(AppConfig.DEBUG)
        	Log.i(TAG, "Found id : " + String.valueOf(id_user));
        UserInfoTask uit = new UserInfoTask();
        UserInfoPrimitive info_user = null;
        uit.execute(ur);
        try {
        	info_user = uit.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
        
        if(info_user != null)
        {
        	 SharedPreferences.Editor editor = preferences.edit();
        	 
        	 editor.putInt("id_user", info_user.getId());
        	 editor.putFloat("score", info_user.getScore());
        	 editor.putInt("rank", info_user.getRank());
        	 editor.putInt("nbr_user", info_user.getNbrUser());
        	 editor.putString("nickname", info_user.getNickname());
        	 editor.putInt("position_trend", info_user.position_trend);
        	 editor.putInt("position_pm", info_user.position_pm);
        	 editor.putInt("position_value", info_user.position_value);
        	 editor.putInt("nbr_group", info_user.nbr_group);
        	 
             editor.commit();
             //Toast.makeText(this, "id_user = " + String.valueOf(info_user.getId()) + ", score = " + String.valueOf(info_user.getScore()), 5000);
             //Log.i(TAG, "Ok for " + String.valueOf(info_user.getId()));
        }
        else
        	if(AppConfig.DEBUG)
        		Log.e(TAG, "info_user == null");
    }
    void goToPlayScreen()
    {
    	Intent intent = new Intent(this, PlaymenuActivity.class);
    	startActivity(intent);
    }
    
    void goToStatScreen()
    {
    	Intent intent = new Intent(this, StatActivity.class);
    	startActivity(intent);
    }
    
    private void goToAboutScreen() {
    	Intent intent = new Intent(this, AboutActivity.class);
    	startActivity(intent);
	}
    
    private void goToHelpScreen() {
    	Intent intent = new Intent(this, HelpActivity.class);
    	startActivity(intent);
	}
}
