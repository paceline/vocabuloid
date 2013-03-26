package me.vocabulario.vocabuloid;

import java.util.Enumeration;
import java.util.Hashtable;

import me.vocabulario.vocabuloid.R;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * Flashcard is the heart of the Vocabuloid application. It loads vocabularies and their translations from vocabulario.me
 * and displays them as flippable flash cards. 
 *
 * @author Ulf Moehring
 * @version 0.3
 */
public class Flashcard extends Activity {

	/** Container for the individual flash cards */
	private ViewFlipper mCardholder;
	/** Old touch value when flipping to next flash card */
	private float mOldTouchValue;
	/** Name for progress dialog shown when loading vocabularies */
	public static final int DIALOG_LOADING_VOCABULARIES = 0;
	/** Progress dialog object */
	ProgressDialog mProgressDialog;
	/** Numbers of vocabularies in current list */
	int listSize;
 	
	/** 
     * Called when the activity is first created.
     *
     * @param Bundle Default Bundle information (inherited)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.flashcard);
    	int listId = this.getIntent().getExtras().getInt("me.vocabulario.vocabuloid.listId");
    	listSize = this.getIntent().getExtras().getInt("me.vocabulario.vocabuloid.listSize");
    	int tenseId = this.getIntent().getExtras().getInt("me.vocabulario.vocabuloid.tenseId");
        VocabularyList list = new VocabularyList(this);
        list.setId(listId);
        if (tenseId > 0) { list.setSelectedTense(tenseId); }
        new LoadVocabularies().execute(list);
    }
    
    /** 
     * Called by {@link LoadVocabularies LoadVocabularies} task when it starts loading vocabularies. Configures
     * and constructs a {@link Dialog ProgressDialog}.
     *
     * @param id Unique name of the dialog (used as reference)
     */
    @Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_LOADING_VOCABULARIES:
				mProgressDialog = new ProgressDialog(this);
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				mProgressDialog.setMessage(this.getString(R.string.message_dialog_loading_vocabularies));
				mProgressDialog.setCancelable(true);
				mProgressDialog.show();
				return mProgressDialog;
			default:
				return null;
		}
	}

    /** 
     * Called when user hits the back button. Quits activity with RESULT_OK.
     *
     * @param keyCode Key hit by user (inherited)
     * @param keyEvent Key event (inherited)
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	setResult(RESULT_OK);
       	 	finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    /** 
     * Called when user flips the flash card. Shows previous or next flash card, depending on the user motion.
     *
     * @param touchevent User gesture (inherited)
     */
    public boolean onTouchEvent(MotionEvent touchevent) {
    	switch (touchevent.getAction()) {
        	case MotionEvent.ACTION_DOWN: {
        		mOldTouchValue = touchevent.getX();
                break;
            }
            case MotionEvent.ACTION_UP: {
            	float currentX = touchevent.getX();
                if (mOldTouchValue < currentX) {
                	mCardholder.setInAnimation(inFromLeftAnimation());
                	mCardholder.setOutAnimation(outToRightAnimation());
                	mCardholder.showPrevious();
                }
                if (mOldTouchValue > currentX) {
                	mCardholder.setInAnimation(inFromRightAnimation());
                	mCardholder.setOutAnimation(outToLeftAnimation());
                	mCardholder.showNext();
               }
               break;
            }
        }
    	return false;
    }
    
    /** 
     * Helper method: Called by {@link LoadVocabularies LoadVocabularies} task to render output as flash cards.
     *
     * @param heading Language of the vocabulary, translations or conjugation
     * @param text Formatted string, containing vocabulary, translations or conjugation
     */
    private void renderFlashcard(String heading, String text, String page) {
    	LinearLayout content = new LinearLayout(this);
    	content.setGravity(Gravity.FILL_VERTICAL | Gravity.CENTER_HORIZONTAL);
    	content.setOrientation(LinearLayout.VERTICAL);

    	TextView pageLabel = new TextView(this);
    	pageLabel.setGravity(Gravity.RIGHT);
    	pageLabel.setPadding(0, 10, 20, 75);
    	pageLabel.setTextAppearance(this, R.style.FlashcardPageNumber);
        pageLabel.setText(page);
        content.addView(pageLabel, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    	
    	TextView headingLabel = new TextView(this);
    	headingLabel.setGravity(Gravity.CENTER_HORIZONTAL);
        headingLabel.setTextAppearance(this, R.style.FlashcardHeading);
        headingLabel.setText(heading);
        content.addView(headingLabel, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        
        TextView textLabel = new TextView(this);
        textLabel.setGravity(Gravity.CENTER_HORIZONTAL);
        textLabel.setTextAppearance(this, R.style.FlashcardFont);
        textLabel.setText(text);
        content.addView(textLabel, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        
        mCardholder.addView(content, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
    }
    
    /** 
     * Helper method: Called by {@link onTouchEvent(MotionEvent touchevent) onTouchEvent} to animate switching flash cards.
     */
    private Animation inFromRightAnimation() {
    	Animation inFromRight = new TranslateAnimation(
    			Animation.RELATIVE_TO_PARENT,  +1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
    			Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
    	);
    	inFromRight.setDuration(500);
    	inFromRight.setInterpolator(new AccelerateInterpolator());
    	return inFromRight;
    }
    
    /** 
     * Helper method: Called by {@link onTouchEvent(MotionEvent touchevent) onTouchEvent} to animate switching flash cards.
     */
    private Animation outToLeftAnimation() {
    	Animation outtoLeft = new TranslateAnimation(
    			Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  -1.0f,
    			Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
    	);
    	outtoLeft.setDuration(500);
    	outtoLeft.setInterpolator(new AccelerateInterpolator());
    	return outtoLeft;
    }
    
    /** 
     * Helper method: Called by {@link onTouchEvent(MotionEvent touchevent) onTouchEvent} to animate switching flash cards.
     */
    private Animation inFromLeftAnimation() {
    	Animation inFromLeft = new TranslateAnimation(
    			Animation.RELATIVE_TO_PARENT,  -1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
    			Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
    	);
    	inFromLeft.setDuration(500);
    	inFromLeft.setInterpolator(new AccelerateInterpolator());
    	return inFromLeft;
    }
    
    /** 
     * Helper method: Called by {@link onTouchEvent(MotionEvent touchevent) onTouchEvent} to animate switching flash cards.
     */
    private Animation outToRightAnimation() {
    	Animation outtoRight = new TranslateAnimation(
    			Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  +1.0f,
    			Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
    	);
    	outtoRight.setDuration(500);
    	outtoRight.setInterpolator(new AccelerateInterpolator());
    	return outtoRight;
    }
    
    /**
     * Asynchronous task used by {@link Flashcard Flashcard} to execute the lengthy task of loading vocabularies and
     * their translations from vocabulario.me
     *
     * @author Ulf Moehring
     * @version 0.2
     */
 	class LoadVocabularies extends AsyncTask<VocabularyList, Void, Hashtable<String,String>> {
 		
 		/** List to flash cards out of  */
 		private VocabularyList list;

 		/** Set up {@link ProgressDialog ProgressDialog} before executing self  */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(DIALOG_LOADING_VOCABULARIES);
		}
		
		/** 
	     * Main method: Load vocabularies and their translations into a {@link Hashtable Hashtable}
	     *
	     * @param lists VocabularyList to use (currently only one) 
	     */
		@Override
		protected Hashtable<String,String> doInBackground(VocabularyList... lists) {
			try {
				this.list = lists[0];
				Hashtable<String,String> data = new Hashtable<String,String>();
				Vocabulary[] vocabularies = list.getVocabularies();
				for(int i=0; i<vocabularies.length; i++) {
					data.put(vocabularies[i].getWord(), vocabularies[i].getResultAsFormattedString(list.getSelector(), list.isVerbList()));
				}
				return data;
			}
			catch (Exception e) { e.printStackTrace(); return null; }
		}
		
		/** 
	     * Takes end result from {@link doInBackground(VocabularyList... lists) doInBackground(...)} and updates
	     * UI in main thread. Also dismisses {@link ProgressDialog ProgressDialog} when done.
	     *
	     * @param result Hastable generated by main method
	     */
		@Override
		protected void onPostExecute(Hashtable<String,String> result) {
			dismissDialog(DIALOG_LOADING_VOCABULARIES);
			mCardholder = (ViewFlipper)findViewById(R.id.cardholder);
			int i = 1;
			for (Enumeration<String> e = result.keys() ; e.hasMoreElements() ;) {
				String key = e.nextElement();
				renderFlashcard(list.getFrom(), key, i + "/" + result.size());
				renderFlashcard(list.getTo(), result.get(key), i + "/" + result.size());
				i++;
		    }
		}
	}
}