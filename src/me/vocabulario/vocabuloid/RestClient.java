package me.vocabulario.vocabuloid;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import me.vocabulario.vocabuloid.R;

import android.content.Context;
import android.content.SharedPreferences;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

/**
 * Abstract base class for communicating with the tuvocabulario.com web service. 
 *
 * @author Ulf Mšhring
 * @version 0.3
 */
public abstract class RestClient {

	/** Context shared across application */
	protected Context mContext;
	/** Common preferences store */
	private static final String PREFERENCES = "VOCABULOID_PREFERENCES";
	/** OAuth consumer for communication with remote service */
	private CommonsHttpOAuthConsumer mConsumer;
	/** Base url of remote service */
	private String mBaseUrl;
	
	/** 
     * Initializes RESTful connection
     *
     * @param ctx Context needed for assessing shared resources
     */
	public RestClient(Context ctx) {
		mContext = ctx;
		mBaseUrl = mContext.getString(R.string.base_url);
		mConsumer = new CommonsHttpOAuthConsumer(mContext.getString(R.string.consumer_key), mContext.getString(R.string.consumer_secret));
		SharedPreferences settings = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		mConsumer.setTokenWithSecret(settings.getString("accessToken",null), settings.getString("accessSecret",null));
	}
	
	/** 
     * Execute a GET request on a collection object
     *
     * @param path Relative path to resource
     */
	protected JSONArray getCollection(String path) {
		HttpGet request = new HttpGet(mBaseUrl + path);
		DefaultHttpClient httpclient = new DefaultHttpClient();
		ResponseHandler<String> handler = new BasicResponseHandler();
		try {
			mConsumer.sign(request);
			String result = httpclient.execute(request, handler);
			if (result != "") {
				JSONArray json = new JSONArray(result);
				return json;
			}
		}
		catch (Exception e) { e.printStackTrace(); }
		return null;
	}

	/** 
     * Execute a GET request on an object
     *
     * @param path Relative path to resource
     */
	protected JSONObject getObject(String path) {
		HttpGet request = new HttpGet(mBaseUrl + path);
		DefaultHttpClient httpclient = new DefaultHttpClient();
		ResponseHandler<String> handler = new BasicResponseHandler();
		try {
			mConsumer.sign(request);
			String result = httpclient.execute(request, handler);
			if (result != "") {
				JSONObject json = new JSONObject(result);
				return json;
			}
		}
		catch (Exception e) { e.printStackTrace(); }
		return null;
	}
}
