package be.tfe.android.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import be.tfe.android.misc.utils.AppConfig;
import be.tfe.android.misc.utils.AppUtils;
import be.tfe.android.tasks.containers.UserInfo;
import android.os.AsyncTask;
import android.util.Log;

public class ResetStatTask extends AsyncTask<UserInfo, Void, Boolean> {
	private final String TAG = "RST";
	
	protected Boolean doInBackground(UserInfo... arg0) {
		// Retreiving the prediction information
		UserInfo userinfo = arg0[0];
		
		String id_user = String.valueOf(userinfo.getId());
		if(AppConfig.DEBUG)
			Log.i(TAG, "id_user = " + id_user);
		
		// Sending the prediction
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = null;
		String url = "http://"+AppConfig.SERVER_IP+AppConfig.PATH_MANAGER+"index.php?Ajax/UserAndroid/android/resetStat";
		httppost = new HttpPost(url);
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);   
	    nameValuePairs.add(new BasicNameValuePair("id_user", id_user));
	    
		HttpResponse response = null;
		
		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			response = httpclient.execute(httppost);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String body = AppUtils.getResponseBody(response);
		if(AppConfig.DEBUG)
			Log.i(TAG, "ResetStat body = " + body);
		return true;
	}
	
}
