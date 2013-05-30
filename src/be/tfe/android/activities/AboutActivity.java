package be.tfe.android.activities;

import be.tfe.android.R;
import be.tfe.android.misc.utils.AppConfig;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;

public class AboutActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_about);
		
		WebView wv = (WebView)findViewById(R.id.webViewAbout);
        wv.loadUrl("http://"+AppConfig.SERVER_IP+AppConfig.PATH_MANAGER+"index.php?About/android");
	}
}
