package me.vocabulario.vocabuloid;

import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;

/**
 * Provides access to vocabulario.me User model. 
 *
 * @author Ulf Moehring
 * @version 0.3
 */
public class User extends RestClient {
	
	/** Maps to id */
	private int id;
	/** Maps to name */
	private String name;
	/** Maps to admin (is user admin or no) */
	private Boolean admin;
	/** Collection of contained vocabularies */
	private VocabularyList[] lists;
	
	/** 
     * Initializes RESTful connection
     *
     * @param ctx Context needed for assessing shared resources
     */
	public User(Context context) {
		super(context);
	}
	
	/** 
     * Fetches user data from server
     */
	protected void getRemoteData() {
		try {
			JSONObject response = getObject("/users/current.json");
			JSONObject root = response.getJSONObject(response.names().getString(0));
			id = root.getInt("id");
			name = root.getString("name");
			admin = root.getBoolean("admin");
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	/** 
     * Returns a specific list from {@link VocabularyList VocabularyList} Array
     *
     * @param position Position of VocabularyList in Array
     */
	public VocabularyList getList(int position) {
		if (lists == null) { getRemoteLists(); }
		return lists[position];
	}
	
	/** 
     * Returns lists as VocabularyList Array
     */
	public VocabularyList[] getLists() {
		if (lists == null) { getRemoteLists(); }
		return lists;
	}
	
	/** 
     * Returns a String Array with the name of all lists
     */
	public String[] getListsAsStrings() {
		if (lists == null) { getRemoteLists(); }
		String[] values = new String[lists.length];
		for (int i=0; i<lists.length; i++) {
			values[i] = lists[i].getName();
		}
		return values;
	}
	
	/** 
     * Fetches new Lists from server
     */
	protected void getRemoteLists() {
		JSONArray result = getCollection("/users/" + id + "/lists.json");
		lists = new VocabularyList[result.length()];
		try {
			for (int i=0;i<result.length();i++) {
				JSONObject list = result.getJSONObject(i);
				VocabularyList vocabularylist = new VocabularyList(mContext);
				String root = list.names().getString(0);
				vocabularylist.setId(list.getJSONObject(root).getInt("id"));
				vocabularylist.setName(list.getJSONObject(root).getString("name"));
				vocabularylist.setSize(list.getJSONObject(root).getInt("size"));
				vocabularylist.setLanguageFromId(list.getJSONObject(root).getJSONObject("language_from").getInt("id"));
				vocabularylist.setLanguageFromName(list.getJSONObject(root).getJSONObject("language_from").getString("word"));
				vocabularylist.setType(root);
				lists[i] = vocabularylist;
            }
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	public int getId() { return id; }

	public String getName() { return name; }

	public Boolean getAdmin() { return admin; }

}
