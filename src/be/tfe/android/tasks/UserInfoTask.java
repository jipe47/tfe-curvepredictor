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
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.google.gson.Gson;

import be.tfe.android.misc.utils.AppConfig;
import be.tfe.android.misc.utils.AppUtils;
import be.tfe.android.tasks.containers.UserRequest;
import be.tfe.android.tasks.primitives.UserInfoPrimitive;
import android.os.AsyncTask;
import android.util.Log;

public class UserInfoTask extends AsyncTask<UserRequest, Void, UserInfoPrimitive> {
	private final String TAG = "UIT";
	private final int TIMEOUT = 2000;
	
	protected UserInfoPrimitive doInBackground(UserRequest... arg0) {
		// From http://stackoverflow.com/questions/5769717/how-can-i-get-an-http-response-body-as-a-string-in-java
		UserRequest ur = arg0[0];
		String id_user = String.valueOf(ur.id);
		String country = ur.country;
		String account = ur.account;
		String reset = ur.reset ? "yes" : "no";
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
		nameValuePairs.add(new BasicNameValuePair("id_user", id_user));   
		nameValuePairs.add(new BasicNameValuePair("country", country));   
		nameValuePairs.add(new BasicNameValuePair("account", account));
		nameValuePairs.add(new BasicNameValuePair("reset", reset));

		if(AppConfig.DEBUG)
			Log.w(TAG, "Sent account: " + account);
		
		final HttpParams httpParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(httpParams, this.TIMEOUT);
	    
		HttpClient httpclient = new DefaultHttpClient(httpParams);
		HttpPost httppost = null;
		String url = "http://"+AppConfig.SERVER_IP+AppConfig.PATH_MANAGER+"index.php?Ajax/UserAndroid/android/getInfo";
		httppost = new HttpPost(url);
		HttpResponse response = null;
        
		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			response = httpclient.execute(httppost);
		} catch (Exception e) {
			e.printStackTrace();
			if(AppConfig.DEBUG)
				Log.e(TAG, "Server not reachable");
			return null;
		}
		
		// Processing the response
		String body = AppUtils.getResponseBody(response);
		if(AppConfig.DEBUG)
			Log.i(TAG, "body = " + body);
		Gson gson = new Gson();
		UserInfoPrimitive cpa = gson.fromJson(body, UserInfoPrimitive.class);
		return cpa;
	}
	
}
