package me.vocabulario.vocabuloid;

import org.json.JSONArray;
import org.json.JSONObject;

import me.vocabulario.vocabuloid.R;
import android.content.Context;

/**
 * Provides access to vocabulario.me Vocabulary model. 
 *
 * @author Ulf Moehring
 * @version 0.3
 */
public class Vocabulary {
	
	/** Context shared across application */
	protected Context mContext;
	/** Maps to id */
	private int id;
	/** Maps to word */
	private String word;
	/** Maps to type */
	private String type;
	/** Maps to language (field id) */
	private int languageId;
	/** Maps to language (field word) */
	private String languageName;
	/** Collection of contained vocabularies */
	private Vocabulary[] translations;
	/** Collection of conjugations */
	private String[] conjugation;
	
	/** 
     * Initializes RESTful connection
     *
     * @param ctx Context needed for assessing shared resources
     */
	public Vocabulary(Context context) {
		mContext = context;
	}
	
	/** 
     *  Returns a formatted String with either translations or conjugation set (wrapper method)
     *
     * @param selectorId ID of language the translations should be in or tense for conjugations
     * @param verblist Determines if verb list (true) or vocabulary list (false)
     */
	public String getResultAsFormattedString(int selectorId, Boolean verblist) {
		if (verblist) {
			return getConjugationsAsFormattedString(selectorId);
		}
		return getTranslationsAsFormattedString(selectorId);
	}
	
	/** 
     *  Returns a formatted String with all translations (as words)
     *
     * @param languageId ID of language the translations should be in
     */
	public String getConjugationsAsFormattedString(int tenseId) {
		if (conjugation != null && conjugation.length > 0) {
			String result = "";
			for (String word : conjugation) {
				result += word + "\n";
			}
			return result.substring(0,result.length()-1);
		}
		else {
			return mContext.getString(R.string.message_no_conjugations);
		}		
	}
	
	/** 
     *  Load translations for self from passed JSONArray
     *
     * @param result JSONArray with translation data
     */
	public void setConjugations(JSONArray result) {
		try {
			conjugation = new String[result.length()];
			for (int i=0;i<result.length();i++) {
				JSONObject remote_conjugation = result.getJSONObject(i);
				conjugation[i] = remote_conjugation.getString("person").split("/")[0] + " - " + remote_conjugation.getString("verb");
			}
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	/** 
     *  Returns a formatted String with all translations (as words)
     *
     * @param languageId ID of language the translations should be in
     */
	public String getTranslationsAsFormattedString(int languageId) {
		if (translations != null && translations.length > 0) {
			String result = "";
			for (Vocabulary vocabulary : translations) {
				result += vocabulary.getWord() + "\n";
			}
			return result.substring(0,result.length()-1);
		}
		else {
			return mContext.getString(R.string.message_no_translations);
		}		
	}
	
	/** 
     *  Load translations for self from passed JSONArray
     *
     * @param result JSONArray with translation data
     */
	public void setTranslations(JSONArray result) {
		try {
			translations = new Vocabulary[result.length()];
			for (int i=0;i<result.length();i++) {
				JSONObject remote_vocabulary = result.getJSONObject(i);
				Vocabulary vocabulary = new Vocabulary(mContext);
				vocabulary.initialize(remote_vocabulary.getInt("id"), type, remote_vocabulary.getString("word"), remote_vocabulary.getJSONObject("language").getInt("id"), remote_vocabulary.getJSONObject("language").getString("word"));
				translations[i] = vocabulary;
			}
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	/** 
     *  Initializes a new vocabulary based on remote data
     *
     * @param id id Field
     * @param type type Field
     * @param word word Field
     * @param languageId language_id Field
     * @param languageName language_name Field
     */
	public void initialize(int id, String type, String word, int languageId, String languageName) {
		this.id = id;
		this.type = type;
		this.word = word;
		this.languageName = languageName;
		this.languageId = languageId;
	}

	public int getId() { return id; }
	
	public String getWord() { return word; }
	
	public String getType() { return type; }
	
	public int getLanguageId() { return languageId; }
	
	public String getLanguageName() { return languageName; }
}
