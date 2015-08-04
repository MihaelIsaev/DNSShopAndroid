package com.mihaelisaev.dnsshop;

import java.util.HashMap;
import java.util.Map;

import com.mihaelisaev.dnsshop.R;
import com.mihaelisaev.dnsshop.ViewSwitcher.WHAT_VIEW;
import com.mihaelisaev.helper.CommonHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

@SuppressLint("UseSparseArrays")
public class Catalog {
	
	private View result = null;
	private Root		root;
	@SuppressWarnings("unused")
	private String 		tag 	= "Activity CATALOG";
	private Map<String, View> mapSubCats = new HashMap<String, View>();
	private Map<Integer, String> mapSubCatsIDs = new HashMap<Integer, String>();
	@SuppressWarnings("unused")
	private ScrollView sv;
	private LinearLayout lv;
	@SuppressWarnings("unused")
	private Handler handler;
	
	public Catalog(Activity root){
		this.root = (Root)root;
		handler = new Handler();
		LayoutInflater li = (LayoutInflater)root.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		result = li.inflate(R.layout.catalog, null);
		sv = (ScrollView) result.findViewById(R.id.scrollView);
		lv = (LinearLayout) result.findViewById(R.id.listView);
        showCategory();
	}
	
	public View getView(){
		return result;
	}
	
	@SuppressWarnings("unused")
	private void showCategory(){
		final Cursor cursor = root.db.getCategory("0");
		cursor.moveToFirst();
		final int indexId   = cursor.getColumnIndex("Id");
		final int indexName = cursor.getColumnIndex("Name");
		int colorKey = 0;
		for(int i=0; i<cursor.getCount(); i++){
			colorKey++;
			
			final int scrollPosition = 61*i;
			
			final String id = cursor.getString(indexId);
			final String namePar = cursor.getString(indexName);
			LayoutInflater li = (LayoutInflater)root.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        final View categoryRow = li.inflate(R.layout.categoty_row, null);
	        if(colorKey==1)
	        	categoryRow.setBackgroundResource(R.drawable.item_row_light);
	        else if(colorKey==2)
	        	categoryRow.setBackgroundResource(R.drawable.item_row);
	        TextView countv = (TextView) categoryRow.findViewById(R.id.count);
	        TextView tv 	= (TextView) categoryRow.findViewById(R.id.text);
	        CommonHelper.setText(root, tv, namePar);			
			
			//Create sub category container
			final LinearLayout sub = new LinearLayout(root);
			final LinearLayout.LayoutParams sublp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			sub.setOrientation(LinearLayout.VERTICAL);
			CommonHelper.gone(root, sub);
			mapSubCats.put(id, sub);
			mapSubCatsIDs.put(i, id);
			
			//GetSubCats
			final Cursor cursorSub = root.db.getCategory(id);
			if(cursorSub.getCount()>0){
				CommonHelper.setText(root, countv, "("+cursorSub.getCount()+")");
				cursorSub.moveToFirst();
				final int indexIdSub   = cursorSub.getColumnIndex("Id");
				final int indexNameSub = cursorSub.getColumnIndex("Name");
				for(int ii=0; ii<cursorSub.getCount(); ii++){
					final String idSub = cursorSub.getString(indexIdSub);
					final String nameSub = cursorSub.getString(indexNameSub);
					LayoutInflater lisub = (LayoutInflater)root.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			        View subcategoryRow = lisub.inflate(R.layout.subcategoty_row, null);
					TextView tvsub = (TextView) subcategoryRow.findViewById(R.id.text);
					CommonHelper.setText(root, tvsub, nameSub);
					
					subcategoryRow.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							root.items.parent_id = idSub;
							root.items.countFilterFind = 0;
							root.items.showItems();
							root.items.title = nameSub;
							root.viewSwitcher.switchTo(WHAT_VIEW.ITEMS);
						}
					});
					
					sub.addView(subcategoryRow);
					cursorSub.moveToNext();
				}
				
				categoryRow.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if(!mapSubCats.get(id).isShown()){
							CommonHelper.visible(root, mapSubCats.get(id));
							categoryRow.requestFocus();
							/*handler.postDelayed(new Runnable() { 
					            public void run() { 
					            	root.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											sv.scrollTo(0, scrollPosition);
										}
									});
					            }
					        }, 200);*/
							hideSubCats();
							CommonHelper.visible(root, mapSubCats.get(id));
						}else
							hideSubCats();
					}
				});
				lv.addView(categoryRow);
				lv.addView(sub, sublp);
			}else{
				CommonHelper.clearText(root, countv);
				categoryRow.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						root.items.parent_id = id;
						root.items.countFilterFind = 0;
						root.items.showItems();
						root.items.title = namePar;
						root.viewSwitcher.switchTo(WHAT_VIEW.ITEMS);
					}
				});
				lv.addView(categoryRow);
			}
			cursorSub.close();
			cursor.moveToNext();
			if(colorKey==2)
				colorKey=0;
		}
		cursor.close();
	}
	
	private void hideSubCats(){
		for(int i=0;i<mapSubCats.size();i++)
			CommonHelper.gone(root, mapSubCats.get(mapSubCatsIDs.get(i)));
	}
}
