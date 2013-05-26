package be.tfe.android;

import java.util.concurrent.ExecutionException;

import be.tfe.android.misc.utils.AppConfig;
import be.tfe.android.misc.utils.AppUtils;
import be.tfe.android.misc.utils.UserInfo;
import be.tfe.android.misc.utils.UserInfoPrimitive;
import be.tfe.android.tasks.ResetStatTask;
import be.tfe.android.tasks.UserInfoTask;
import be.tfe.android.tasks.UserRequest;
import be.tfe.android.tasks.UserUpdateTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

public class StatActivity extends Activity {
	
	private EditText edUserNickname;
	private String nickname;
	private int id_user, rank, nbr_user;
	private float score;
	private Button buttonEdit, buttonResetProgress, buttonResetAccount;

	private Dialog confirmResetProgressDialog, confirmResetAccountDialog;
	
	 public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_stat);
        
        SharedPreferences preferences = getSharedPreferences(AppConfig.PREFS_NAME, 0);
        id_user = preferences.getInt("id_user", -1);
        
        // Creates tabs
        TabHost tabHost=(TabHost)findViewById(R.id.tabhost);
        tabHost.setup();

        TabSpec specHighscore = tabHost.newTabSpec(getString(R.string.stat_tab_top_title));
        specHighscore.setContent(R.id.tabHighscore);
        specHighscore.setIndicator(getString(R.string.stat_tab_top_title), this.getResources().getDrawable(R.drawable.icon_top));

        TabSpec specProgression = tabHost.newTabSpec(getString(R.string.stat_tab_progression_title));
        specProgression.setIndicator(getString(R.string.stat_tab_progression_title), this.getResources().getDrawable(R.drawable.icon_progress));
        specProgression.setContent(R.id.tabProgression);
        
        TabSpec specOption = tabHost.newTabSpec(getString(R.string.stat_tab_option_title));
        specOption.setIndicator(getString(R.string.stat_tab_option_title), this.getResources().getDrawable(R.drawable.icon_option));
        specOption.setContent(R.id.tabOption);

        tabHost.addTab(specProgression);
        tabHost.addTab(specHighscore);
        tabHost.addTab(specOption);

        WebView wv = (WebView) findViewById(R.id.webTabHighscore);
        wv.loadUrl("http://"+AppConfig.SERVER_IP+AppConfig.PATH_MANAGER+"index.php?Highscore/android/"+String.valueOf(id_user));
        
        wv = (WebView) findViewById(R.id.webTabProgression);
        wv.loadUrl("http://"+AppConfig.SERVER_IP+AppConfig.PATH_MANAGER+"index.php?Progress/android/"+String.valueOf(id_user));
        
        // User update form
        edUserNickname = (EditText) findViewById(R.id.editText_nickname);
        edUserNickname.setText(nickname);
        
        buttonEdit = (Button) findViewById(R.id.stat_button_update);
        buttonEdit.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				updateUser();
			}});
        
        buttonResetProgress = (Button) findViewById(R.id.stat_button_resetprogress);
        buttonResetProgress.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				showResetProgressDialog();
			}});
        
        buttonResetAccount = (Button) findViewById(R.id.stat_button_resetaccount);
        buttonResetAccount.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				showResetAccountDialog();
			}});
        
 		
    }
	 
	 private void showResetProgressDialog()
	 {
		 Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(getString(R.string.stat_modal_confirmresetprogress_title));
 		builder.setMessage(getString(R.string.stat_modal_confirmresetprogress_message));

 		builder.setPositiveButton(getString(R.string.button_yes), new android.content.DialogInterface.OnClickListener(){
 			public void onClick(DialogInterface arg0, int arg1) {
 				confirmResetProgressDialog.hide();
 				resetProgress();
 			}});
 		
 		builder.setNegativeButton(getString(R.string.button_no), new android.content.DialogInterface.OnClickListener(){
 			public void onClick(DialogInterface arg0, int arg1) {
 				confirmResetProgressDialog.hide();
 			}});
 		
 		confirmResetProgressDialog = builder.create();
 		confirmResetProgressDialog.show();
	 }
	 
	 private void showResetAccountDialog()
	 {
		 Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(getString(R.string.stat_modal_confirmresetaccount_title));
 		builder.setMessage(getString(R.string.stat_modal_confirmresetaccount_message));

 		builder.setPositiveButton(getString(R.string.button_yes), new android.content.DialogInterface.OnClickListener(){
 			public void onClick(DialogInterface arg0, int arg1) {
 				confirmResetAccountDialog.hide();
 				resetAccount();
 			}});
 		
 		builder.setNegativeButton(getString(R.string.button_no), new android.content.DialogInterface.OnClickListener(){
 			public void onClick(DialogInterface arg0, int arg1) {
 				confirmResetAccountDialog.hide();
 			}});
 		
 		confirmResetAccountDialog = builder.create();
 		confirmResetAccountDialog.show();
	 }
	 
	 public void onResume()
	 {
		 super.onResume();
		 loadStat();
		 refreshStat();
	 }
	 
	private void loadStat()
	{
        UserRequest ur = new UserRequest();
        ur.id = id_user;
        
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
    	
    	if(info_user == null)
    		return;
    	
    	rank 		= info_user.rank;
    	nbr_user 	= info_user.nbr_user;
    	nickname 	= info_user.nickname;
    	score 		= info_user.score;
	}
	 
	private void refreshStat()
	{
		 // Showing these data
        TextView tv = (TextView) findViewById(R.id.stat_score);
        tv.setText(String.valueOf(score)+"€");
        
        tv = (TextView) findViewById(R.id.stat_rank);
        tv.setText(String.valueOf(rank) + " / " + String.valueOf(nbr_user));
	}
	private void updateUser()
	{
		String newNickname = edUserNickname.getText().toString();
		if(newNickname.length() == 0)
		{
			Toast.makeText(this, getString(R.string.stat_toast_emptynickname), Toast.LENGTH_LONG).show();
			return;
		}
		else if(newNickname.length() < 3)
		{
			Toast.makeText(this, getString(R.string.stat_toast_smallnickname), Toast.LENGTH_LONG).show();
			return;
		}
		UserInfo userInfo = new UserInfo();
		userInfo.setIdUser(id_user);
		userInfo.setNickname(newNickname);
		
		UserUpdateTask uut = new UserUpdateTask();
        uut.execute(userInfo);
        
        boolean success = false;
        try {
        	success = uut.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
        
        String message = success ? getString(R.string.stat_toast_updatednickname) : getString(R.string.stat_toast_errornickname);
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		
		if(success)
		{
			// Update the stored nickname in the phone memory
			SharedPreferences preferences = getSharedPreferences(AppConfig.PREFS_NAME, 0);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString("nickname", newNickname);
			editor.commit();
			
			// Update the webview with highscores so that the new nickname is used (if the user is in the top) 
			WebView wv = (WebView) findViewById(R.id.webTabHighscore);
			wv.reload();
		}
	}
	 
	private void resetProgress()
	{
		UserInfo userInfo = new UserInfo();
		userInfo.setIdUser(id_user);
		
		ResetStatTask rst = new ResetStatTask();
        rst.execute(userInfo);
        
        boolean success = false;
        try {
        	success = rst.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
        
        String message = success ? getString(R.string.stat_toast_resettedprogress) : getString(R.string.stat_toast_errorprogress);
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		
		if(success)
		{
			// Update the stored nickname in the phone memory
			SharedPreferences preferences = getSharedPreferences(AppConfig.PREFS_NAME, 0);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putInt("position_trend", 0);
			editor.putInt("position_pm", 0);
			editor.putInt("position_value", 0);
			editor.commit();
			
			// Update the webview with highscores and progression so that the new nickname is used (if the user is in the top) 
			WebView wv = (WebView) findViewById(R.id.webTabProgression);
			wv.reload();
			
	        refreshStat();
		}
	}
	
	private void resetAccount() {
		// Fetch a new account
        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        String country = tm.getNetworkCountryIso();
        
        UserRequest ur = new UserRequest();
        ur.id = this.id_user;
        ur.country = country;
        ur.reset = true;
        ur.account = AppUtils.getGoogleAccount(this);
        
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
        

    	String message = info_user != null ? getString(R.string.stat_toast_resettedaccount) : getString(R.string.stat_toast_erroraccount);
 		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        
 		// Update the stored nickname in the phone memory
        if(info_user != null)
        {
			SharedPreferences preferences = getSharedPreferences(AppConfig.PREFS_NAME, 0);
			SharedPreferences.Editor editor = preferences.edit();
			 
			editor.putInt("id_user", info_user.getId());
			editor.putFloat("score", info_user.getScore());
			editor.putInt("rank", info_user.getRank());
			editor.putInt("nbr_user", info_user.getNbrUser());
			editor.putString("nickname", info_user.getNickname());
			editor.putBoolean("hasReadHelp", false);
			 
			editor.commit();
			
			id_user		= info_user.getId();
			score 		= info_user.getScore();
			rank 		= info_user.getRank();
			nbr_user	= info_user.getNbrUser();
			
			refreshStat();
			
			WebView wv = (WebView) findViewById(R.id.webTabHighscore);
			wv.loadUrl("http://"+AppConfig.SERVER_IP+AppConfig.PATH_MANAGER+"index.php?Highscore/android/"+String.valueOf(id_user));
      
			wv = (WebView) findViewById(R.id.webTabProgression);
			wv.loadUrl("http://"+AppConfig.SERVER_IP+AppConfig.PATH_MANAGER+"index.php?Progress/android/"+String.valueOf(id_user));
			
			Toast.makeText(this, getString(R.string.stat_toast_resettedaccount), Toast.LENGTH_LONG).show();
        }
	}
}
