package be.tfe.android.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import be.tfe.android.misc.utils.AppConfig;
import be.tfe.android.misc.utils.UserInfo;
import android.os.AsyncTask;

public class UserUpdateTask extends AsyncTask<UserInfo, Void, Boolean> {
	@SuppressWarnings("unused")
	private final String TAG = "UUT";
	
	protected Boolean doInBackground(UserInfo... arg0) {
		// Retrieving the prediction information
		UserInfo userinfo = arg0[0];
		
		String nickname = userinfo.getNickname();
		String id_user = String.valueOf(userinfo.getId());
		
		// Sending the prediction
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = null;
		String url = "http://"+AppConfig.SERVER_IP+AppConfig.PATH_MANAGER+"index.php?Ajax/UserAndroid/android/updateNickname";
		httppost = new HttpPost(url);
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);   
		nameValuePairs.add(new BasicNameValuePair("nickname", nickname));
		nameValuePairs.add(new BasicNameValuePair("id_user", id_user));
	    
		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			httpclient.execute(httppost);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
}
