package be.tfe.android.misc.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.protocol.HTTP;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import be.tfe.android.curve.Point;

public class AppUtils {
	private static String TAG = "AppUtils";
	// From http://answers.yahoo.com/question/index?qid=20100401175631AAtN1Ez
	public static ArrayList<Point> getReversed(ArrayList<Point> original)
	{
		ArrayList<Point> copy = new ArrayList<Point>(original);
		Collections.reverse(copy);
		return copy;
	}
		
	// From
	// http://thinkandroid.wordpress.com/2009/12/30/getting-response-body-of-httpresponse/
	public static String getResponseBody(HttpResponse response) {

		String response_text = null;

		HttpEntity entity = null;
		try {

			entity = response.getEntity();

			response_text = _getResponseBody(entity);

		} catch (ParseException e) {

			e.printStackTrace();

		} catch (IOException e) {

			if (entity != null) {

				try {

					entity.consumeContent();

				} catch (IOException e1) {

				}

			}

		}

		return response_text;

	}

	public static String _getResponseBody(final HttpEntity entity)
			throws IOException, ParseException {

		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}

		InputStream instream = entity.getContent();

		if (instream == null) {
			return "";
		}

		if (entity.getContentLength() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(

			"HTTP entity too large to be buffered in memory");
		}

		String charset = getContentCharSet(entity);

		if (charset == null) {

			charset = HTTP.DEFAULT_CONTENT_CHARSET;

		}

		Reader reader = new InputStreamReader(instream, charset);

		StringBuilder buffer = new StringBuilder();

		try {

			char[] tmp = new char[1024];

			int l;

			while ((l = reader.read(tmp)) != -1) {

				buffer.append(tmp, 0, l);

			}

		} finally {

			reader.close();

		}

		return buffer.toString();

	}

	public static String getContentCharSet(final HttpEntity entity)
			throws ParseException {

		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}

		String charset = null;

		if (entity.getContentType() != null) {

			HeaderElement values[] = entity.getContentType().getElements();

			if (values.length > 0) {

				NameValuePair param = values[0].getParameterByName("charset");

				if (param != null) {

					charset = param.getValue();

				}

			}

		}
		return charset;
	}

	public static String generateUID(int id_user) {
		Calendar c = Calendar.getInstance(); 
		return String.valueOf(id_user)+"-"+String.valueOf(c.get(Calendar.YEAR))+String.valueOf(c.get(Calendar.MONTH))+String.valueOf(c.get(Calendar.DAY_OF_YEAR))+String.valueOf(c.get(Calendar.HOUR))+String.valueOf(c.get(Calendar.MINUTE))+String.valueOf(c.get(Calendar.SECOND))+String.valueOf(c.get(Calendar.MILLISECOND));
	}
	public static boolean findServer()
	{
		int nbr_fail = AppConfig.NBR_FAIL;
		String[] ips = AppConfig.SERVERS;
		String[] paths = AppConfig.PATHS;
		for(int i = 0 ; i < ips.length ; i++)
		{
			if(AppConfig.DEBUG)
				Log.i(TAG, "Testing IP " + ips[i] + " - nbr_fail = " + String.valueOf(nbr_fail));
			if(isServerAvailable(ips[i]) && nbr_fail == 0)
			{
				AppConfig.SERVER_IP = ips[i];
				AppConfig.PATH_MANAGER = paths[i];
				if(AppConfig.DEBUG)
					Log.i(TAG, "Ok !!");
				return true;
			}
			nbr_fail--;
		}
		return false;
	}
	public static boolean isServerAvailable(String ip)
	{
		if(AppConfig.FORCE_SERVERUNAVAILABLE)
			return false;
		else
			return ping("http://"+ip, AppConfig.PING_TIMEOUT);
	}
	
	// From http://stackoverflow.com/questions/3584210/preferred-java-way-to-ping-a-http-url-for-availability
	@SuppressLint("NewApi")
	public static boolean ping(String url, int timeout) {
		if(AppConfig.DEBUG)
			Log.i(TAG, "Pinging " + url);
		ThreadPolicy tp = ThreadPolicy.LAX;
		StrictMode.setThreadPolicy(tp);
	    url = url.replaceFirst("https", "http"); // Otherwise an exception may be thrown on invalid SSL certificates.

	    try {
	        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
	        connection.setConnectTimeout(timeout);
	        connection.setReadTimeout(timeout);
	        connection.setRequestMethod("POST");
	        int responseCode = connection.getResponseCode();
	        return (200 <= responseCode && responseCode <= 399);
	    } catch (IOException exception) {
	        return false;
	    }
	}
	
	public static String getGoogleAccount(Context context)
	{
		// Get the user's Google account
        Account[] accounts = AccountManager.get(context).getAccounts();
        
        for(int i = 0 ; i < accounts.length ; i++)
        {
        	if(!accounts[i].type.equals("com.google"))
        		continue;
        	return accounts[i].name;
        }
        return "";
	}
	
	// From http://www.zdnet.com/blog/burnette/how-to-use-multi-touch-in-android-2-part-6-implementing-the-pinch-zoom-gesture/1847
	@SuppressLint("FloatMath")
	public static float spacing(MotionEvent event) {
	float x = event.getX(0) - event.getX(1);
	float y = event.getY(0) - event.getY(1);
	return FloatMath.sqrt(x * x + y * y);
	}
	
	// From http://www.zdnet.com/blog/burnette/how-to-use-multi-touch-in-android-2-part-6-implementing-the-pinch-zoom-gesture/1847
	public static PointF midPoint(MotionEvent event) {
	float x = event.getX(0) + event.getX(1);
	float y = event.getY(0) + event.getY(1);
	return new PointF(x, y);
	}
	
}
