package be.tfe.android.activities;

import be.tfe.android.R;
import be.tfe.android.misc.utils.AppConfig;
import be.tfe.android.misc.utils.MyWebViewClient;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;

public class HelpActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_help);
		
		WebView wv = (WebView)findViewById(R.id.webViewHelp);
		wv.setWebViewClient(new MyWebViewClient());
        wv.loadUrl("http://"+AppConfig.SERVER_IP+AppConfig.PATH_MANAGER+"index.php?Help/android");
        this.markHelpAsRead();
	}
	
	private void markHelpAsRead() {
		SharedPreferences preferences = getSharedPreferences(AppConfig.PREFS_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hasReadHelp", true);
        editor.commit();
	}
}
