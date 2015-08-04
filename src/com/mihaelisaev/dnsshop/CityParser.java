package com.mihaelisaev.dnsshop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.mihaelisaev.helper.CommonHelper;

import android.database.DatabaseUtils.InsertHelper;
import android.os.AsyncTask;

public class CityParser extends AsyncTask<String, String, String> {
	
	private Root root;
	
	public CityParser(Root root) 
	{
	    this.root = root;
	}
	
	@Override
	protected void onPreExecute() {
	    super.onPreExecute();
	    CommonHelper.visible(root, root.pb);
	}
	
	@Override
	protected String doInBackground(String... params) {
		try{
			JSONObject jCity = new JSONObject(params[0]);
			List<String> jCityKeyNames = new ArrayList<String>();
			Map<String, String> jCityJSONByKey = new HashMap<String,String>();
			root.db.parser.extractKeysFromJSONObject(jCity, jCityKeyNames, jCityJSONByKey);
			
			InsertHelper iHelpCity 	= new InsertHelper(root.db.getDB(), "`City`");
			int city_cell_id 		= iHelpCity.getColumnIndex("Id");
			int city_cell_name 		= iHelpCity.getColumnIndex("Name");
			int city_cell_phone 	= iHelpCity.getColumnIndex("Phone");
			int city_cell_long 		= iHelpCity.getColumnIndex("Longitude");
			int city_cell_lat 		= iHelpCity.getColumnIndex("Latitude");
			root.db.getDB().beginTransaction();
			
			for (String name : jCityKeyNames) {
				String json = jCityJSONByKey.get(name);
				JSONObject jCategoryJSON = new JSONObject(json);
				
				iHelpCity.prepareForReplace();

				iHelpCity.bind(city_cell_id, 	jCategoryJSON.getString("Id"));
				iHelpCity.bind(city_cell_name, 	jCategoryJSON.getString("Name"));
				iHelpCity.bind(city_cell_phone, jCategoryJSON.getString("Phone"));
				iHelpCity.bind(city_cell_long, jCategoryJSON.getString("Longitude"));
				iHelpCity.bind(city_cell_lat, jCategoryJSON.getString("Latitude"));
				
				iHelpCity.execute();
			}
			
			root.db.getDB().setTransactionSuccessful();
		} catch(Exception e) { } finally { root.db.getDB().endTransaction(); }
		
		return null;
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		root.selectCity.showCityes();
		CommonHelper.hide(root, root.pb);
	}
}