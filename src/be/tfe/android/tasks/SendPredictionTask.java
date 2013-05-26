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
import com.google.gson.Gson;

import be.tfe.android.curveviewer.Prediction;
import be.tfe.android.misc.utils.AppConfig;
import be.tfe.android.misc.utils.AppUtils;
import android.os.AsyncTask;
import android.util.Log;

public class SendPredictionTask extends AsyncTask<Prediction, Void, Void> {
	private final String TAG = "SPT";
	
	protected Void doInBackground(Prediction... arg0) {
		// Retrieving the prediction information
		Prediction p = arg0[0];
		String confidence = String.valueOf(p.getConfidence());
		String id_zone = String.valueOf(p.getIdZone());
		String id_user = String.valueOf(p.getIdUser());
		String id_serie = p.getIdSerie();
		String predictionType = p.getPredictionType().toString();
		String predictionInput = p.getPredictionInput().toString();
		String training = String.valueOf(p.isTraining());
		String prediction = p.getPrediction();
		String level = String.valueOf(p.getLevel());
		String gameMode = p.getGameMode().toString();
		String hasEnteredConfidence = p.hasEnteredConfidence() ? "true" : "false";
		
		String score = String.valueOf(p.getZone().getScore());
		
		String[] data = {confidence, id_zone, predictionType, predictionInput, String.valueOf(training), prediction, id_user, score, level, id_serie, gameMode, hasEnteredConfidence};
		Gson gson = new Gson();
		String gsonnedData = gson.toJson(data);
		
		
		// Sending the prediction
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = null;
		String url = "http://"+AppConfig.SERVER_IP+AppConfig.PATH_MANAGER+"index.php?Ajax/Curve/android/sendPrediction";
		httppost = new HttpPost(url);
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);   
	    nameValuePairs.add(new BasicNameValuePair("data", gsonnedData));
	    
		HttpResponse response = null;
		
		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			response = httpclient.execute(httppost);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String body = AppUtils.getResponseBody(response);
		if(AppConfig.DEBUG)
			Log.i(TAG, "Sendprediction body = " + body);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
