package be.tfe.android.tasks;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

import be.tfe.android.curve.Curve;
import be.tfe.android.curve.CurveResponsePrimitive;
import be.tfe.android.misc.utils.AppConfig;
import be.tfe.android.misc.utils.CurveRequest;
import be.tfe.android.misc.utils.CurveResponse;
import android.util.Log;

public class GetPredictionTask extends AppAsyncTask<CurveRequest, Void, CurveResponse> {
	private final String TAG = "GPT";
	private final int INTERNET_CURVEFETCHING_TIMEOUT = 2000;
	
	protected CurveResponse doInBackground(CurveRequest... arg0) {
		String id_user 			= String.valueOf(arg0[0].id_user);
		String predictionInput 	= arg0[0].predictionInput.toString();
		String predictionType 	= arg0[0].predictionType.toString();
		String gameMode 		= arg0[0].gameMode.toString();
		String training 		= arg0[0].training ? "training" : "normal";
		String random			= arg0[0].random ? "random" : "level";
		String level			= String.valueOf(arg0[0].level);
		
		String args = id_user+"/"+predictionInput+"/"+training+"/"+predictionType+"/"+gameMode+"/"+random+"/"+level;
		
		if(AppConfig.DEBUG)
			Log.i(TAG, "Args = " + args);
		
		final HttpParams httpParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(httpParams, this.INTERNET_CURVEFETCHING_TIMEOUT);
		
		HttpClient httpclient = new DefaultHttpClient(httpParams);
		HttpPost httpget = null;
		String url = "http://"+AppConfig.SERVER_IP+AppConfig.PATH_MANAGER+"index.php?Ajax/CurveDev/android/getCurve/"+args;
		httpget = new HttpPost(url);
		HttpResponse response = null;
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);   
	    nameValuePairs.add(new BasicNameValuePair("onlyendzone", String.valueOf(false)));
        
		try {
			httpget.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			response = httpclient.execute(httpget);
		} catch (Exception e) {
			e.printStackTrace();
			if(AppConfig.DEBUG)
				Log.w(TAG, "Server not reachable");
			return null;
		}
		
		// Processing the response
		String body = getResponseBody(response);
		
		if(AppConfig.DEBUG)
			Log.i(TAG, "Body = " + body);
		
		CurveResponse cr = new CurveResponse();
		
		Set<Curve> set = new LinkedHashSet<Curve>();
		cr.curves = set;
		cr.isFinished = false;
		
		if(body.equals("FINISH"))
		{
			cr.isFinished = true;
			cr.nbr_group_after = 0;
			cr.groupPosition = -1;
			return cr;
		}
		
		Gson gson = new Gson();
		CurveResponsePrimitive cpa = gson.fromJson(body, CurveResponsePrimitive.class);
		for(int i = 0 ; i < cpa.curves.length ; i++)
		{
			Curve c = new Curve(cpa.curves[i]);
			set.add(c);
		}
		if(AppConfig.DEBUG)
			Log.i(TAG, String.valueOf(set.size()) + " loaded.");
		cr.nbr_group_after = cpa.nbr_group_after;
		cr.groupPosition = cpa.groupPosition;
		return cr;
	}
}
