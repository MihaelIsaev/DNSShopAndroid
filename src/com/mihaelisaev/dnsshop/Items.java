package com.mihaelisaev.dnsshop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.mihaelisaev.dnsshop.R;
import com.mihaelisaev.dnsshop.ViewSwitcher.WHAT_VIEW;
import com.mihaelisaev.helper.CommonHelper;
import com.mihaelisaev.helper.ImageHelper;

import android.app.Activity;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class Items {
    private	String 			tag = "Activity ITEMS";
    private Root 			root;
    private View 			result;
	private ScrollView 		sv;
	private	boolean 		nowLoading 	= false;
	private	int 			showed 		= 0;
    public 	LinearLayout 	lv;
    public 	String 			title;
    public 	Map<String,View> 		mapItemView = new HashMap<String, View>();
    public 	Map<Integer, Bitmap>	mapPreviews = new HashMap<Integer, Bitmap>();
    public 	String 	parent_id 		= "";
    public 	int 	countFilterFind = 0;
    private AsyncTask<String, Void, String> DownloadItemsTask;
    
    public Items(Activity root){
    	this.root = (Root)root;
    	LayoutInflater li = root.getLayoutInflater();
		result = li.inflate(R.layout.catalog, null);
		sv = (ScrollView) result.findViewById(R.id.scrollView);
		lv = (LinearLayout) result.findViewById(R.id.listView);
		sv.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				View view = (View) sv.getChildAt(sv.getChildCount()-1);
				int diff = (view.getBottom()-(sv.getHeight()+sv.getScrollY()));
                if( diff == 0 || diff <=300)
                	if(!nowLoading && showed<Items.this.root.db.getItemsCount(parent_id))
                		new LoadCachedItemsTask().execute();
	            return false;
			}
		});
		DownloadItemsTask = new DownloadItemsTask();
    }
    
    public View getView(){
		return result;
	}
    
    public String getFilter(){
    	StringBuffer result = new StringBuffer();
    	Cursor cursor = root.db.getTempFilters();
    	if(cursor.getCount()>0){
			cursor.moveToFirst();
			int indexKey	= cursor.getColumnIndex("Id");
			int indexValue	= cursor.getColumnIndex("Value");
			for(int i=0;i<cursor.getCount();i++){
				String Key 			= cursor.getString(indexKey);
				String Value 		= cursor.getString(indexValue);
				result.append("&");
				result.append(Key);
				result.append("=");
				result.append(Value);
				cursor.moveToNext();
			}
    	}
    	cursor.close();
    	Loger.d(tag, "Filter: "+result.toString());
    	return result.toString();
    }
    
    public boolean isFiltered(){
    	if(getFilter().length()>2)
    		return true;
    	else
    		return false;
    }
    
	private void addView(final LinearLayout lv, final String code, final String comments, final String name, final String price, final String grade, final String preview, final int i){
			LayoutInflater 	li 				= root.getLayoutInflater();
	        View 			itemRow 		= li.inflate(R.layout.item_row, null);
			TextView 		nameView 		= (TextView) itemRow.findViewById(R.id.text);
			TextView 		commentsView 	= (TextView) itemRow.findViewById(R.id.comments);
			TextView 		priceView 		= (TextView) itemRow.findViewById(R.id.price);
			CommonHelper.setText(root, nameView, name);
			CommonHelper.setText(root, commentsView, "("+comments+")");
			CommonHelper.setText(root, priceView, price+"р.");
			itemRow.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					root.item.from = WHAT_VIEW.ITEMS;
					root.item.title = name;
					if(!root.item.id.equals(code)){
						root.item.id = code;
						root.item.load();
					}
					root.viewSwitcher.switchTo(WHAT_VIEW.ITEM);
					Loger.d(tag, "click item Code:"+code);
				}
			});
			try{
				Integer gradeInt = Integer.valueOf(grade);
				if(gradeInt>0){
					RatingBar ratingView = (RatingBar) itemRow.findViewById(R.id.rating);
					CommonHelper.visible(root, ratingView);
					ratingView.setRating(gradeInt);
				}
			}catch(Exception e){}
			boolean viewAlreadyInMap = false;
			if(mapItemView.containsKey(code))
				viewAlreadyInMap = true;
			mapItemView.put(code, itemRow);
			if(!viewAlreadyInMap)
				lv.addView(mapItemView.get(code));
			loadPreview(itemRow, code, preview, i);
	}
	
	private void loadPreview(View itemRow, String code, final String preview, final int i){
		if(root.db.getKey(Settings.KEY_SHOW_PREVIEW).equals(Settings.KEY_VALUE_ON)){
			ImageHelper.loadImageFromCache(root, (ImageView) itemRow.findViewById(R.id.preview), code, i, mapPreviews, Settings.CACHE_IMAGE_PREVIEW_PREFIX);
			ImageHelper.loadImageFromServer(root, (ImageView) itemRow.findViewById(R.id.preview), code, preview, Settings.CACHE_IMAGE_PREVIEW_PREFIX);
		}
	}
	
	public void showItems(){
		DownloadItemsTask.cancel(true);
		showLoadingMessage(true);
		showProgressBar(true);
		clear();
		if(!isFiltered())
			new LoadCachedItemsTask().execute();
		DownloadItemsTask = new DownloadItemsTask().execute();
		Loger.d(tag, "showItems parent = "+parent_id);
	}
	
	public void clear(){
		lv.removeAllViews();
		mapItemView.clear();
		showed = 0;
	}
	
	private void showLoadingMessage(final boolean on){
		if(!on){
			TextView mess = (TextView) sv.findViewById(R.id.message);
			CommonHelper.clearText(root, mess);
			CommonHelper.gone(root, mess);
    		Button back = (Button) sv.findViewById(R.id.back);
			CommonHelper.gone(root, back);
		}else{
			TextView mess = (TextView) sv.findViewById(R.id.message);
			CommonHelper.setText(root, mess, "Загрузка");
			CommonHelper.visible(root, mess);
    		Button back = (Button) sv.findViewById(R.id.back);
			CommonHelper.gone(root, back);
		}
	}
	
	private void showProgressBar(final boolean on){
		if(!on)
			CommonHelper.hide(root, root.pb);
		else
			CommonHelper.visible(root, root.pb);
	}
	
	private void showErrorMessage(final boolean on){
		TextView mess = (TextView) sv.findViewById(R.id.message);
		Button back = (Button) sv.findViewById(R.id.back);
		if(!on){
			CommonHelper.clearText(root, mess);
			CommonHelper.clearText(root, back);
			CommonHelper.gone(root, back);
			CommonHelper.gone(root, mess);
		}else{
			CommonHelper.visible(root, back);
			CommonHelper.visible(root, mess);
			if(getFilter().length()<2){
				CommonHelper.setText(root, mess, "Ничего нет или не загрузилось");
				CommonHelper.setText(root, back, "Повторить");
			}else{
				if(countFilterFind==0){
					CommonHelper.setText(root, mess, "Видимо ничего не найдено");
					CommonHelper.setText(root, back, "Поискать получше");
				}else if(countFilterFind==1){
					CommonHelper.setText(root, mess, "Поискали по-лучше, но нет");
					CommonHelper.setText(root, back, "Там должно что-то быть!");
				}else if(countFilterFind==2){
					CommonHelper.setText(root, mess, "А Вы настойчивы :)");
					CommonHelper.setText(root, back, "Поищите еще разок");
				}else{
					CommonHelper.setText(root, mess, "Увы, ничего нет");
					CommonHelper.clearText(root, back);
					CommonHelper.gone(root, back);
				}
				countFilterFind++;
			}
			back.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showItems();
					CommonHelper.gone(root, v);
				}
			});
		}
	}
	
	/**
	 * Класс быстрого вывода товаров в категории из кэша 
	 **/
	private class LoadCachedItemsTask extends AsyncTask<Void, Void, Void> {
	    @Override
	    protected void onPreExecute() {
	    	nowLoading = true;
	    	super.onPreExecute();
	    }
		
		protected Void doInBackground(Void... params) {
	    	Log.d("COUNT", "Количество товара: "+root.db.getItemsCount(parent_id)+"шт.");
	    	Cursor cursor = root.db.getItems(parent_id, showed, Settings.SCROLL_ITEMS_SHOW, isFiltered());
			if(cursor.getCount()>0){
				cursor.moveToFirst();
				int indexCode  		= cursor.getColumnIndex("Code");
				int indexComments  	= cursor.getColumnIndex("Comments");
				int indexName  		= cursor.getColumnIndex("Name");
				int indexPrice 		= cursor.getColumnIndex("Price");
				int indexGrade 		= cursor.getColumnIndex("Grade");
				int indexPreview 	= cursor.getColumnIndex("Preview");
				for(int i=0;i<cursor.getCount();i++){
					final String code 		= cursor.getString(indexCode);
					String comments 	= cursor.getString(indexComments);
					//TODO
					if(comments.equals("null") || comments == null)
						comments = "нет комментариев";
					final String comment = comments;
					final String name 		= cursor.getString(indexName);
					final String price 		= cursor.getString(indexPrice);
					final String grade 		= cursor.getString(indexGrade);
					final String preview 	= cursor.getString(indexPreview);
					final int ii = i;
					root.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							addView(lv, code, comment, name, price, grade, preview, ii);
						}
					});
					cursor.moveToNext();
				}
				showLoadingMessage(false);
				showErrorMessage(false);
				showed = showed+Settings.SCROLL_ITEMS_SHOW;
			}
			cursor.close();
			return null;
	    }
	    
	    @Override
	    protected void onPostExecute(Void result) {
	    	nowLoading = false;
	    	super.onPostExecute(result);
	    }
	}
	
	private class DownloadItemsTask extends AsyncTask<String, Void, String> {
	    protected String doInBackground(String... urls) {
	    	List<NameValuePair> 	params = new ArrayList<NameValuePair>(2);
									params.add(new BasicNameValuePair("what",		"items"));
									params.add(new BasicNameValuePair("city",		root.db.getKey(Settings.KEY_DEF_CITY)));
									params.add(new BasicNameValuePair("id",			parent_id));
									params.add(new BasicNameValuePair("filters",	getFilter()));
			return root.db.server.executeHTTPRequest(root.db.server.buildURL(), params);
	    }

	    protected void onPostExecute(String result) {
	    	new ParseDownloadedItemsTask().execute(result, parent_id);
	    }
	}
	
	private class ParseDownloadedItemsTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			String parentId = params[1];
			
			try{
				final JSONObject jAnswer 	= new JSONObject(params[0]);
				root.filters.json 				= jAnswer.getString("Filters");
				root.filters.alreadyParsed 		= false;
				JSONObject jItems = new JSONObject(jAnswer.getString("Items"));
				List<String> jItemsKeyNames = new ArrayList<String>();
				Map<String, String> jItemsJSONByKey = new HashMap<String,String>();
				root.db.parser.extractKeysFromJSONObject(jItems, jItemsKeyNames, jItemsJSONByKey);
				if(jItemsKeyNames.size()>0)
					root.db.deleteItems(parentId, root.items.isFiltered());
				
				String table = "`Items`";
				if(root.items.isFiltered())
					table = "`ItemsSearch`";	
				InsertHelper iHelpItem 	= new InsertHelper(root.db.getDB(), table);
				for (String name : jItemsKeyNames){
					try{
						String json = jItemsJSONByKey.get(name);
						JSONObject jItemJSON = new JSONObject(json);
						if(!parentId.equals(jItemJSON.getString("Parent")))
							break;
						else{
							iHelpItem.prepareForReplace();
							iHelpItem.bind(iHelpItem.getColumnIndex("Code"), 	jItemJSON.getString("Code"));
							iHelpItem.bind(iHelpItem.getColumnIndex("Comments"),jItemJSON.getString("Comments"));
							iHelpItem.bind(iHelpItem.getColumnIndex("City"), 	root.db.getKey(Settings.KEY_DEF_CITY));
							iHelpItem.bind(iHelpItem.getColumnIndex("Name"), 	jItemJSON.getString("Name"));
							iHelpItem.bind(iHelpItem.getColumnIndex("Price"), 	jItemJSON.getString("Price"));
							iHelpItem.bind(iHelpItem.getColumnIndex("Grade"), 	jItemJSON.getString("Grade"));
							iHelpItem.bind(iHelpItem.getColumnIndex("Preview"), jItemJSON.getString("Preview"));
							iHelpItem.bind(iHelpItem.getColumnIndex("Parent"), 	parentId);
							iHelpItem.execute();
							//root.db.addItem(root.items.isFiltered(), jItemJSON.getString("Code"), jItemJSON.getString("Name"), jItemJSON.getString("Price"), jItemJSON.getString("Grade"), jItemJSON.getString("Preview"), parentId);
						}
					}catch(Exception e){
						Loger.d(tag, "Error parse items: "+e.toString());
					}
				}
				root.db.getDB().setTransactionSuccessful();
			}catch(Exception e){
				Loger.d(tag, "Error parse items: "+e.toString());
			}
			return parentId;
		}
		
		@Override
		protected void onPostExecute(String result) {
			showProgressBar(false);
			if(result.equals(root.items.parent_id)){
				new LoadCachedItemsTask().execute();
				if(!root.filters.parent_id.equals(parent_id)){
					root.filters.parent_id = parent_id;
					root.filters.parseFilters();
				}
			}
			super.onPostExecute(result);
		}
	}
}