package com.tuvocabulario.vocabuloid;

import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;

/**
 * Provides access to tuvocabulario.com Vocabulary model. 
 *
 * @author Ulf Mšhring
 * @version 0.1
 */
public class Vocabulary extends RestClient {
	
	/** Maps to id */
	private int id;
	/** Maps to word */
	private String word;
	/** Maps to gender */
	private String gender;
	/** Maps to type */
	private String type;
	/** Maps to language (field id) */
	private int language_id;
	/** Maps to language (field word) */
	private String language_name;
	/** Collection of contained vocabularies */
	private Vocabulary[] translations;
	
	/** 
     * Initializes RESTful connection
     *
     * @param ctx Context needed for assessing shared resources
     */
	public Vocabulary(Context context) {
		super(context);
	}
	
	/** 
     *  Returns a formatted String with all translations (as words)
     *
     * @param languageId ID of language the translations should be in
     */
	public String getTranslationsAsFormattedString(int languageId) {
		if (translations == null) { getRemoteTranslations(languageId); }
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
     *  Fetches new translations from the server
     *
     * @param languageId ID of language the translations should be in
     */
	protected void getRemoteTranslations(int languageId) {
		try {
			JSONArray result = getCollection("/vocabularies/" + id + "/translate.json?language_id=" + languageId);
			translations = new Vocabulary[result.length()];
			for (int i=0;i<result.length();i++) {
				JSONObject remote_vocabulary = result.getJSONObject(i);
				String root = remote_vocabulary.names().getString(0);
				Vocabulary vocabulary = new Vocabulary(mContext);
				vocabulary.initialize(remote_vocabulary.getJSONObject(root).getInt("id"), type, remote_vocabulary.getJSONObject(root).getString("word"), remote_vocabulary.getJSONObject(root).getString("gender"), remote_vocabulary.getJSONObject(root).getJSONObject("language").getInt("id"), remote_vocabulary.getJSONObject(root).getJSONObject("language").getString("word"));
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
     * @param gender gender Field
     * @param languageId language_id Field
     * @param languageName language_name Field
     */
	public void initialize(int id, String type, String word, String gender, int languageId, String languageName) {
		this.id = id;
		this.type = type;
		this.word = word;
		this.gender = gender;
		this.language_name = languageName;
		this.language_id = languageId;
	}

	public int getId() { return id; }
	
	public String getWord() { return word; }

	public String getGender() { return gender; }
	
	public String getType() { return type; }
	
	public int getLanguageId() { return language_id; }
	
	public String getLanguageName() { return language_name; }
}
