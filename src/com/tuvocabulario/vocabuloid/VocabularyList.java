package com.tuvocabulario.vocabuloid;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

/**
 * Provides access to tuvocabulario.com VocabularyList model. 
 *
 * @author Ulf Mšhring
 * @version 0.1
 */
public class VocabularyList extends RestClient {
	
	/** Maps to id */
	private int id;
	/** Maps to name */
	private String name;
	/** Maps to type */
	private String type;
	/** Maps to source language (id) */
	private int languageFromId;
	/** Maps to source language (name) */
	private String languageFromName;
	/** Maps to target language (id) */
	private int languageToId;
	/** Maps to target language (name) */
	private String languageToName;
	/** Number of vocabularies contained in list */
	private int size;
	/** Collection of contained vocabularies */	
	private Vocabulary[] vocabularies;
	
	/** 
     * Initializes RESTful connection
     *
     * @param ctx Context needed for assessing shared resources
     */
	public VocabularyList(Context context) {
		super(context);
	}
	
	/** 
     * Returns a specific {@link Vocabulary Vocabulary} from {@link vocabularies vocabularies} Array
     *
     * @param position Position of VocabularyList in Array
     */
	public Vocabulary getVocabulary(int position) {
		if (vocabularies == null) { getRemoteData(); }
		return vocabularies[position];
	}
	
	/** 
     * Returns contained vocabularies as {@link Vocabulary Vocabulary} Array
     */
	public Vocabulary[] getVocabularies() {
		if (vocabularies == null) { getRemoteData(); }
		return vocabularies;
	}
	
	/** 
     * Returns a String Array with the words of all contained vocabularies
     */
	public String[] getVocabulariesAsStrings() {
		if (vocabularies == null) { getRemoteData(); }
		String[] values = new String[vocabularies.length];
		for (int i=0; i<vocabularies.length; i++) {
			values[i] = vocabularies[i].getWord();
		}
		return values;
	}
	
	/** 
     * Fetches new vocabularies from server
     */
	protected void getRemoteData() {
		try {
			JSONObject response = getObject("/lists/" + id + ".json");
			JSONObject root = response.getJSONObject(response.names().getString(0));
			type = response.names().getString(0);
			name = root.getString("name");
			size = root.getInt("size");
			languageFromId = root.getJSONObject("language_from").getInt("id");
			languageFromName = root.getJSONObject("language_from").getString("word");
			if (root.has("language_to")) { languageToId = root.getJSONObject("language_to").getInt("id"); languageToName = root.getJSONObject("language_to").getString("word"); };
			JSONArray result = root.getJSONArray("vocabularies");
			vocabularies = new Vocabulary[result.length()];
			for (int i=0;i<result.length();i++) {
				JSONObject remote_vocabulary = result.getJSONObject(i);
				Vocabulary vocabulary = new Vocabulary(mContext);
				vocabulary.initialize(remote_vocabulary.getInt("id"), "Vocabulary", remote_vocabulary.getString("word"), remote_vocabulary.getString("gender"), root.getJSONObject("language_from").getInt("id"), root.getJSONObject("language_from").getString("word"));
				vocabularies[i] = vocabulary;
            }
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	/** 
     * Determines whether list is empty
     */
	public Boolean empty() {
		return vocabularies == null || vocabularies.length == 0;
	}

	public int getId() { return id; }
	
	public void setId(int id) { this.id = id; }
	
	public String getType() { return type; }
	
	public void setType(String type) { this.type = type; }

	public String getName() { return name; }

	public void setName(String name) { this.name = name; }
	
	public int getLanguageFromId() { return languageFromId; }
	
	public String getLanguageFromName() { return languageFromName; }
	
	public int getLanguageToId() { return languageToId; }
	
	public String getLanguageToName() { return languageToName; }
	
	public int getSize() { return size; }
	
	public void setSize(int size) { this.size = size; }
}
