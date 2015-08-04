package com.mihaelisaev.dnsshop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import com.mihaelisaev.helper.CommonHelper;

import com.mihaelisaev.dnsshop.R;

import android.content.Context;
import android.database.DatabaseUtils.InsertHelper;

public class Parser {
	private Root		root;
	private Context 	context;
	private String 		tag 		=	"Parser.java";
	
	public Parser(Root root){
		this.root 		= root;
		this.context 	= root.getApplicationContext();
	}
	
	public void category(){
		String categoryJSON = CommonHelper.readRawTextFile(context, R.raw.category);
		try{
			JSONObject jCategory = new JSONObject(categoryJSON);
			List<String> jCategoryKeyNames = new ArrayList<String>();
			Map<String, String> jCategoryJSONByKey = new HashMap<String,String>();
			extractKeysFromJSONObject(jCategory, jCategoryKeyNames, jCategoryJSONByKey);
			
			InsertHelper iHelpCat 	= new InsertHelper(root.db.getDB(), "`Category`");
			int cat_cell_id 		= iHelpCat.getColumnIndex("Id");
			int cat_cell_name 		= iHelpCat.getColumnIndex("Name");
			int cat_cell_parent 	= iHelpCat.getColumnIndex("Parent");
			root.db.getDB().beginTransaction();
			
			for (String name : jCategoryKeyNames) {
				String json = jCategoryJSONByKey.get(name);
				JSONObject jCategoryJSON = new JSONObject(json);

				iHelpCat.prepareForReplace();

				iHelpCat.bind(cat_cell_id, 		jCategoryJSON.getString("Id"));
				iHelpCat.bind(cat_cell_name, 	jCategoryJSON.getString("Name"));
				iHelpCat.bind(cat_cell_parent, 	jCategoryJSON.getString("Parent"));
				
				iHelpCat.execute();
			}
			
			root.db.getDB().setTransactionSuccessful();
		} catch(Exception e) { } finally { root.db.getDB().endTransaction(); }
	}
	
	public void extractKeysFromJSONObject(JSONObject jObject, List<String> listNames, Map<String,String> mapKeys){
		@SuppressWarnings("rawtypes")
		Iterator iter = jObject.keys();
		while(jObject.keys().hasNext())
	        try{
				String key = (String)iter.next();
		        String value = jObject.getString(key);
		        listNames.add(key);
		        mapKeys.put(key, value);
	        }
	        catch(Exception e){break;}
	}
	
	public void filters(String json, String parentId){
		try{
			JSONObject jFilters = new JSONObject(json);
			List<String> jFilterKeyNames = new ArrayList<String>();
			Map<String, String> jFiltersJSONByKey = new HashMap<String,String>();
			root.db.parser.extractKeysFromJSONObject(jFilters, jFilterKeyNames, jFiltersJSONByKey);
			
			for (String name : jFilterKeyNames){
				String jsonI = jFiltersJSONByKey.get(name);
				JSONObject jFilterJSON = new JSONObject(jsonI);
				
				if(jFilterJSON.getString("Type").equals("select")){
					String label 	= jFilterJSON.getString("Label");
					String key 		= jFilterJSON.getString("Key");
					String type 	= jFilterJSON.getString("Type");
					String values =  jFilterJSON.getString("Values");
					
					root.db.addFilter(parentId+key, parentId, name.replace("n", ""), type, key, label);
					
					JSONObject jFilterValues = new JSONObject(values);
					List<String> jFilterValuesKeyNames = new ArrayList<String>();
					Map<String, String> jFilterValuesJSONByKey = new HashMap<String,String>();
					root.db.parser.extractKeysFromJSONObject(jFilterValues, jFilterValuesKeyNames, jFilterValuesJSONByKey);
					for (String nameValues : jFilterValuesKeyNames){
						String jsonValues = jFilterValuesJSONByKey.get(nameValues);
						JSONObject jFilterValuesJSON = new JSONObject(jsonValues);
						String nm = jFilterValuesJSON.getString("Name");
						String vl = jFilterValuesJSON.getString("Value");
						
						root.db.addFilterValues(parentId+key+nameValues, parentId+key, nameValues.replace("n", ""), vl, nm, "", "");
					}
				}
				else if(jFilterJSON.getString("Type").equals("one_checkbox")){
					String label 	= jFilterJSON.getString("Label");
					String key 		= jFilterJSON.getString("Key");
					String type 	= jFilterJSON.getString("Type");
					
					root.db.addFilter(parentId+key, parentId, name.replace("n", ""), type, key, label);
				}
				else if(jFilterJSON.getString("Type").equals("multi_checkbox")){
					String label 	= jFilterJSON.getString("Label");
					String type 	= jFilterJSON.getString("Type");
					String values =  jFilterJSON.getString("Values");
					
					root.db.addFilter(parentId+name.replace("n", ""), parentId, name.replace("n", ""), type, "", label);
					
					JSONObject jFilterValues = new JSONObject(values);
					List<String> jFilterValuesKeyNames = new ArrayList<String>();
					Map<String, String> jFilterValuesJSONByKey = new HashMap<String,String>();
					root.db.parser.extractKeysFromJSONObject(jFilterValues, jFilterValuesKeyNames, jFilterValuesJSONByKey);
					for (String nameValues : jFilterValuesKeyNames){
						String jsonValues = jFilterValuesJSONByKey.get(nameValues);
						JSONObject jFilterValuesJSON = new JSONObject(jsonValues);
						String vName = jFilterValuesJSON.getString("Name");
						String vKey = jFilterValuesJSON.getString("Key");
						
						if(!vName.equals(""))
							root.db.addFilterValues(parentId+vKey, parentId+name.replace("n", ""), nameValues.replace("n", ""), vKey, vName, "", "");
					}
				}
				else if(jFilterJSON.getString("Type").equals("two_inputs")){
					String label 	= jFilterJSON.getString("Label");
					String type 	= jFilterJSON.getString("Type");
					String from 	= jFilterJSON.getString("From");
					String to 		= jFilterJSON.getString("To");
					
					root.db.addFilter(parentId+name, parentId, name.replace("n", ""), type, "", label);
					root.db.addFilterValues(parentId+name.replace("n", ""), parentId+name.replace("n", ""), name.replace("n", ""), "", "", from, to);
				}							
			}
		}catch(Exception e){
			Loger.d(tag, "Error parse filters: "+e.toString());
		}finally{
			root.filters.alreadyParsed = true;
			root.filters.getFromDB();
		}
	}
	
	public void reviews(final String message, final boolean more, final String id){
		Loger.d(tag, "parse reviews JSON: "+message);
		new Thread(new Runnable(){
			public void run(){
				try{
					JSONObject jReviews = new JSONObject(message);
					List<String> jReviewKeyNames = new ArrayList<String>();
					Map<String, String> jReviewsJSONByKey = new HashMap<String,String>();
					root.db.parser.extractKeysFromJSONObject(jReviews, jReviewKeyNames, jReviewsJSONByKey);
					for (String name : jReviewKeyNames) {
						String json = jReviewsJSONByKey.get(name);
						JSONObject jReviewJSON = new JSONObject(json);
						root.db.addReview(more, id, name, jReviewJSON.getString("Plus"), jReviewJSON.getString("Minus"), jReviewJSON.getString("Comment"), jReviewJSON.getString("User"), jReviewJSON.getString("City"), jReviewJSON.getString("Date"), jReviewJSON.getString("Grade"));
					}
				}catch(Exception e){
					Loger.d(tag, "error parse rewiews more="+more+" error: "+e.toString());
				}finally{
					root.item.checkMoreReviews();
				}
			}
		}).start();
	}
}
