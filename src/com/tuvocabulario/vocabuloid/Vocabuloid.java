package com.tuvocabulario.vocabuloid;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import com.tuvocabulario.vocabuloid.R;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Vocabuloid is a simple flash card application for Android-based mobile phones. It uses the tuvocabulario.com
 * service as JSON data back end. 
 *
 * @author Ulf Mšhring
 * @version 0.1
 */
public class Vocabuloid extends ListActivity {
	
	/** Identifier for flash card sub-activity */
	private static final int FLASHCARD = 0;
	/** Name of application-wide preference store (used mainly for OAuth data) */
	private static final String PREFERENCES = "VOCABULOID_PREFERENCES";
	/** Application-wide preference store object (used mainly for OAuth data) */
	private SharedPreferences mSettings;
	/** Consumer for issuing OAuth requests to web service */
	private CommonsHttpOAuthConsumer mConsumer;
	/** Provider for issuing OAuth requests to web service */
	private CommonsHttpOAuthProvider mProvider;
	/** Global tuvocabulario.com user object (the root for all further requests) */
	private User mUser;
	/** Global ArrayAdapter object for handling the list of vocabulary lists */
	private ArrayAdapter<String> mAdapter;
	
	/** 
     * Called when the activity is first created.
     *
     * @param Bundle Default Bundle information (inherited)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mSettings = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        initializeAdapter();
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(mListsListener);
    }
    
    /** 
     * Called when the activity is resumed (primary use: save OAuth tokens when coming back in from initial authorization
     * process.
     */
    public void onResume() {
    	super.onResume();
    	if (this.getIntent().getScheme() != null && this.getIntent().getScheme().startsWith("vocabulario-android-app")) {
    		Uri uri = this.getIntent().getData();
        	if(uri != null) {
        		initializeHandlers();
        		mSettings = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        		mConsumer.setTokenWithSecret(mSettings.getString("requestToken",null), mSettings.getString("requestSecret",null));
        		mProvider.setOAuth10a(true);
        		try {
        			mProvider.retrieveAccessToken(mConsumer, uri.getQueryParameter("oauth_verifier"));
        			SharedPreferences.Editor editor = mSettings.edit();
        			editor.remove("requestToken");
        			editor.remove("requestSecret");
        			editor.putString("accessToken", mConsumer.getToken());
        			editor.putString("accessSecret", mConsumer.getTokenSecret());
        			editor.commit();
        			initializeAdapter();
        			toast(R.string.message_signed_in);
        		}
        		catch (Exception e) { e.printStackTrace(); }
        	}
    	}
    }
    
    /** 
     * Called when user hits the phone's menu button. Basically displays menu/options.xml
     *
     * @param Menu Default menu information (inherited)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }
    
    /** 
     * Called when user selects one menu item. Checks which one and launches appropriate action
     *
     * @param MenuItem Selected menu item (inherited)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.sign_in:
        		signIn();
        		return true;
        	case R.id.sign_out:
        		signOut();
        		return true;
        	case R.id.quit:
        		finish();
        		return true;
        	default:
        		return super.onContextItemSelected(item);
        }
    }
    
    /** 
     * Called when user selects a {@link VocabularyList VocabularyList} from {@link ListView ListView}. Kicks off
     * {@link Flashcard Flashcard}, provided that selected {@link VocabularyList VocabularyList} a.) isn't empty and b.) isn't a
     * verb list, which is not yet supported. 
     */
    private OnItemClickListener mListsListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	VocabularyList selected = mUser.getLists()[position];
        	if (!isOnline() || selected.getSize() == 0 || selected.getType().endsWith("_verb_list")) {
        		toast(R.string.message_no_vocabularies);
        	}
        	else {
        		Intent i = new Intent(getBaseContext(), Flashcard.class);
        		i.putExtra("com.tuvocabulario.vocabuloid.listId", selected.getId());
        		i.putExtra("com.tuvocabulario.vocabuloid.listSize", selected.getSize());
        		startActivityForResult(i, FLASHCARD);
        	}
        }
    };
    
    /** 
     * Helper method: Called by {@link onCreate(Bundle savedInstanceState) onCreate} to initialize or refresh list displaying all of the {@link User User's}
     * {@link VocabularyList VocabularyLists}
     */
    private void initializeAdapter() {
    	String[] data = new String[] { };
        if (isOnline() && mSettings.contains("accessToken") && mSettings.contains("accessSecret")) {
        	mUser = new User(this);
        	mUser.getRemoteData();
        	data = mUser.getListsAsStrings();
        }
        mAdapter = new ArrayAdapter<String>(this, R.layout.row, data);
        setListAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }
    
    /** 
     * Helper method: Initializes the OAuth consumer and provider objects to talk with tuvocabulario.com web service
     */
    private void initializeHandlers() {
    	String mBaseUrl = this.getString(R.string.base_url);
		mConsumer = new CommonsHttpOAuthConsumer(this.getString(R.string.consumer_key), this.getString(R.string.consumer_secret));
		mProvider = new CommonsHttpOAuthProvider(mBaseUrl + this.getString(R.string.request_token), mBaseUrl + this.getString(R.string.access_token), mBaseUrl + this.getString(R.string.authorize));
	}
    
    /** 
     * Helper method: Called by {@link onOptionsItemSelected(MenuItem item) onOptionsItemSelecte} when user clicks on sign in menu item.
     */
    private void signIn() {
    	String requestUrl = null;
        initializeHandlers();
        try {
        	requestUrl = mProvider.retrieveRequestToken(mConsumer, "vocabulario-android-app:///");
        	SharedPreferences.Editor editor = mSettings.edit();
            editor.putString("requestToken", mConsumer.getToken());
            editor.putString("requestSecret", mConsumer.getTokenSecret());
            editor.commit();
        }
        catch (Exception e) { e.printStackTrace(); }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(requestUrl));
        startActivity(i);
    }
    
    /** 
     * Helper method: Called by {@link onOptionsItemSelected(MenuItem item) onOptionsItemSelecte} when user clicks on sign out menu item.
     */
    private void signOut() {
    	SharedPreferences.Editor editor = mSettings.edit();
		if (mSettings.contains("accessToken")) { editor.remove("accessToken"); editor.commit(); }
		if (mSettings.contains("accessSecret")) { editor.remove("accessSecret"); editor.commit(); }
		initializeAdapter();
		toast(R.string.message_signed_out);
    }
    
    /** 
     * Helper method: Called whenever the user needs to be notified of whatever
     * 
     * @param messageId Reference to a String resource
     */
    private void toast(int messageId) {
    	Toast toast = Toast.makeText(this, this.getString(messageId), Toast.LENGTH_LONG);
		toast.show();
    }
    
    /** 
     * Helper method: Checks if device is currently online (a requirement for using this app)
     */
    public boolean isOnline() {
    	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}