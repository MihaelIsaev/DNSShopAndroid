package com.mihaelisaev.dnsshop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mihaelisaev.dnsshop.R;
import com.mihaelisaev.dnsshop.ViewSwitcher.WHAT_VIEW;
import com.mihaelisaev.helper.CommonHelper;

public class Home {

	private View 			result 		= null;
	private Root 			root;
	private String 			tag 		= "Activity HOME";
	private View 			searchView	= null;
	private ScrollView 		search_sv;
    public 	LinearLayout 	search_lv;
	
	public EditText search;
	
	public Home(Root root){
		this.root = root;
		LayoutInflater li = (LayoutInflater)root.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        result = li.inflate(R.layout.home, null);
        searchView 	= li.inflate(R.layout.catalog, null);
        search_sv = (ScrollView) searchView.findViewById(R.id.scrollView);
        search_lv = (LinearLayout) searchView.findViewById(R.id.listView);
        bindSearch(result);
	}
	
	public View getView(){
		return result;
	}
	
	public View getSearchView(){
		return searchView;
	}
	
	private void bindSearch(final View view){
		
		ImageView doSearch = (ImageView) view.findViewById(R.id.dosearch);
		doSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				search(view);
			}
		});
		
    	search = (EditText) view.findViewById(R.id.inSearch);
    	search.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					try{
						root.imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
					}catch(Exception e){
						
					}
				}else{
					try{
						root.imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
					}catch(Exception e){
						
					}
				}
			}
		});
    	
    	search.setOnKeyListener(new OnKeyListener() {
    	    public boolean onKey(View v, int keyCode, KeyEvent event) {
    	        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
    	        	search(view);
    	          return true;
    	        }
    	        return false;
    	    }
    	});
	}
	
	public void search(View view){
    	search_lv.removeAllViews();
		
		if(search.getText().length()==0)
    		return;
    	
    	TextView mess = (TextView) search_sv.findViewById(R.id.message);
		Button back = (Button) search_sv.findViewById(R.id.back);
		CommonHelper.gone(root, mess);
		CommonHelper.gone(root, back);
    	
    	final ProgressBar 	searchProgress = (ProgressBar) view.findViewById(R.id.searchProgress);
    	CommonHelper.visible(root, searchProgress);
    	final List<NameValuePair> 	params = new ArrayList<NameValuePair>(2);
									params.add(new BasicNameValuePair("what", 	"search"));
									params.add(new BasicNameValuePair("city", 	root.db.getKey(Settings.KEY_DEF_CITY)));
									params.add(new BasicNameValuePair("search", search.getText().toString()));
		
		new Thread(new Runnable(){
			public void run(){
				final String itemsJSON = root.db.server.executeHTTPRequest(root.db.server.buildURL(), params);
				Loger.d(tag, "answer: "+itemsJSON);
				root.runOnUiThread(new Runnable(){
					public void run(){
						try{
							JSONObject jSearch = new JSONObject(itemsJSON);
							
							String type = jSearch.getString("Type");
							
							if(type.equals("item")){
								String code = jSearch.getString("Code");
								root.item.from = WHAT_VIEW.HOME;
								root.item.title = "";
								if(!root.item.id.equals(code)){
									root.item.id = code;
									root.item.load();
								}
								root.viewSwitcher.switchTo(WHAT_VIEW.ITEM);
							}
							else if(type.equals("no")){
								TextView mess = (TextView) search_sv.findViewById(R.id.message);
					    		Button back = (Button) search_sv.findViewById(R.id.back);
					    		back.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) { root.viewSwitcher.switchTo(WHAT_VIEW.HOME); }
								});
					    		CommonHelper.visible(root, back);
					    		CommonHelper.setText(root, mess, "Не найдено");
					    		CommonHelper.visible(root, mess);
					    		root.viewSwitcher.switchTo(WHAT_VIEW.SEARCH);
							}
							else if(type.equals("items")){
								String items = jSearch.getString("Items");
								showSearchItems(items);
								root.viewSwitcher.switchTo(WHAT_VIEW.SEARCH);
							}
						}catch(Exception e){
							Loger.d(tag, e.toString());
							TextView mess = (TextView) search_sv.findViewById(R.id.message);
				    		Button back = (Button) search_sv.findViewById(R.id.back);
				    		back.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) { root.viewSwitcher.switchTo(WHAT_VIEW.HOME); }
							});
				    		CommonHelper.visible(root, back);
				    		CommonHelper.setText(root, mess, "Ошибка запроса на сервер");
				    		CommonHelper.visible(root, mess);
				    		root.viewSwitcher.switchTo(WHAT_VIEW.SEARCH);
						} finally {
							search.getText().clear();
							CommonHelper.hide(root, searchProgress);
						}
					}
				});
			}
		}).start();
    }
	
	private void showSearchItems(final String message){
		new Thread(new Runnable(){
			public void run(){
				root.runOnUiThread(new Runnable(){
					public void run(){
						try{
							root.db.clearSearchResults();
							CommonHelper.visible(root, root.pb);
							JSONObject jItems = new JSONObject(message);
							List<String> jItemsKeyNames = new ArrayList<String>();
							Map<String, String> jItemsJSONByKey = new HashMap<String,String>();
							root.db.parser.extractKeysFromJSONObject(jItems, jItemsKeyNames, jItemsJSONByKey);
							for (String name : jItemsKeyNames) {
								String json = jItemsJSONByKey.get(name);
								JSONObject jItemJSON = new JSONObject(json);
								root.db.addSearchItem(jItemJSON.getString("Code"), jItemJSON.getString("Name"), jItemJSON.getString("Price"), name);
							}
							
							Cursor searchResults = root.db.getSearchResults();
							
							int indexCode 	= searchResults.getColumnIndex("Code");
							int indexName 	= searchResults.getColumnIndex("Name");
							int indexPrice 	= searchResults.getColumnIndex("Price");
							searchResults.moveToFirst();
							for(int i=0;i<searchResults.getCount();i++){
								final 	String 			code 		= searchResults.getString(indexCode);
										String 			name 		= searchResults.getString(indexName);
										String 			price 		= searchResults.getString(indexPrice);
										LayoutInflater 	li 			= (LayoutInflater) root.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
										View 			itemRow 	= li.inflate(R.layout.item_row, null);
										TextView 		nameView	= (TextView) itemRow.findViewById(R.id.text);
										TextView 		priceView 	= (TextView) itemRow.findViewById(R.id.price);
								CommonHelper.setText(root, nameView, name);
								CommonHelper.setText(root, priceView, price);
								itemRow.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										root.item.from = WHAT_VIEW.SEARCH;
										root.item.title = "";
										if(!root.item.id.equals(code)){
											root.item.id = code;
											root.item.load();
										}
										root.viewSwitcher.switchTo(WHAT_VIEW.ITEM);
									}
								});
								search_lv.addView(itemRow);
								searchResults.moveToNext();
							}
							searchResults.close();
						} catch(Exception e) {  } finally { CommonHelper.hide(root, root.pb); }
					} 
				});
			}
		}).start();
	}
}

