package com.mihaelisaev.dnsshop;

import com.mihaelisaev.dnsshop.R;
import com.mihaelisaev.dnsshop.ViewSwitcher.WHAT_VIEW;
import com.mihaelisaev.helper.CommonHelper;

import android.content.Context;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class SelectCity  {
    @SuppressWarnings("unused")
	private String tag = "Activity SelectCity";
    private View result = null;
    private Root root;
    private ScrollView sv; 
    private LinearLayout lv;
    private TextView mess;
    private EditText search;
	
	public SelectCity(Root root){
    	this.root = root;
    	LayoutInflater li = (LayoutInflater)root.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		result = li.inflate(R.layout.select_city, null);
    	sv = (ScrollView) result.findViewById(R.id.scrollView);
    	mess = (TextView) sv.findViewById(R.id.message);
    	lv = (LinearLayout) result.findViewById(R.id.listView);
		search = (EditText) result.findViewById(R.id.search);
		search.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					try{
						SelectCity.this.root.imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
						search.getText().clear();
					}catch(Exception e){
						
					}
				}else{
					try{
						SelectCity.this.root.imm.showSoftInput(search, InputMethodManager.SHOW_IMPLICIT);
					}catch(Exception e){
						
					}
				}
			}
		});
		search.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				showCityes(search.getText().toString());
			}
		});
    }
	
	public View getView(){
		return result;
	}
	
	public void showCityes(){
		lv.removeAllViews();
		CommonHelper.gone(root, mess);
		final Cursor cursor = root.db.getCityes();
		cursor.moveToFirst();
		final int indexId   = cursor.getColumnIndex("Id");
		int indexName = cursor.getColumnIndex("Name");
		for(int i=0; i<cursor.getCount(); i++){
			final String id = cursor.getString(indexId);
			LayoutInflater li = (LayoutInflater) root.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        View cityRow = li.inflate(R.layout.city_row, null);
			TextView tv = (TextView) cityRow.findViewById(R.id.text);
			CommonHelper.setText(root, tv, cursor.getString(indexName));
			cityRow.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					search.getText().clear();
					root.db.setKey("DefaultCity", id);
					root.viewSwitcher.switchTo(WHAT_VIEW.HOME);
				}
			});
			lv.addView(cityRow);
			cursor.moveToNext();
		}
		cursor.close();
	}
	
	public void showCityes(final String keyword){
		lv.removeAllViews();
		final Cursor cursor = root.db.getCityesByKeyword(keyword);
		if(cursor.getCount()>0){
			CommonHelper.gone(root, mess);
			cursor.moveToFirst();
			final int indexId   = cursor.getColumnIndex("Id");
			int indexName = cursor.getColumnIndex("Name");
			for(int i=0; i<cursor.getCount(); i++){
				final String id = cursor.getString(indexId);
				LayoutInflater li = (LayoutInflater) root.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		        View cityRow = li.inflate(R.layout.city_row, null);
				TextView tv = (TextView) cityRow.findViewById(R.id.text);
				CommonHelper.setText(root, tv, cursor.getString(indexName));
				cityRow.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						search.getText().clear();
						root.db.setKey("DefaultCity", id);
						root.viewSwitcher.switchTo(WHAT_VIEW.HOME);
					}
				});
				lv.addView(cityRow);
				cursor.moveToNext();
			}
		}else{
			CommonHelper.setText(root, mess, "город не найден");
			CommonHelper.visible(root, mess);
		}
		cursor.close();
	}
}