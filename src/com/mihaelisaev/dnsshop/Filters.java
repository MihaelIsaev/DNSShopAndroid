package com.mihaelisaev.dnsshop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mihaelisaev.dnsshop.R;
import com.mihaelisaev.dnsshop.ViewSwitcher.WHAT_VIEW;
import com.mihaelisaev.helper.CommonHelper;

public class Filters {

	private View 			result = null;
	public 	ScrollView 		sv;
	public 	LinearLayout 	lv;
	private Root 			root;
	public String parent_id = "";
	public String json;
	private String tag = "Activity FILTERS";
	public boolean alreadyParsed = false;
	
	public Filters(Root root){
		this.root = root;
		LayoutInflater li = root.getLayoutInflater();
        result = li.inflate(R.layout.catalog, null);
        sv = (ScrollView) 	result.findViewById(R.id.scrollView);
		lv = (LinearLayout) result.findViewById(R.id.listView);
	}
	
	public void addView(final View view){
		root.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				lv.addView(view);
			}
		});
	}
	
	public void clear(){
		root.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				lv.removeAllViews();
			}
		});		
	}
	
	public View getView(){
		return result;
	}
	
	@SuppressWarnings("unused")
	public void getFromDB(){
		Cursor cursor = root.db.getFilters(parent_id);
		if(cursor.getCount()>0){
			cursor.moveToFirst();
			int indexId		= cursor.getColumnIndex("Id");
			int indexParent	= cursor.getColumnIndex("Parent");
			int indexNum	= cursor.getColumnIndex("Num");
			int indexType	= cursor.getColumnIndex("Type");
			int indexKey	= cursor.getColumnIndex("Key");
			int indexLabel	= cursor.getColumnIndex("Label");
			for(int i=0;i<cursor.getCount();i++){
				String Id 			= cursor.getString(indexId);
				String Parent 		= cursor.getString(indexParent);
				String Num 			= cursor.getString(indexNum);
				String Type 		= cursor.getString(indexType);
				String Key 	= cursor.getString(indexKey);
				String Label 		= cursor.getString(indexLabel);
				try{
					if(Type.equals("select"))
						typeSelect(Label, Key);
					else if(Type.equals("one_checkbox"))
						typeOneCheckbox(Label, Key);
					else if(Type.equals("multi_checkbox"))
						typeMultiCheckbox(Label, Id);
					else if(Type.equals("two_inputs"))
						typeTwoInputs(Label, Num);
				}catch(Exception e){
					Loger.d(tag, "Error filter Type = "+Type+" error: "+e.toString());
				}
				cursor.moveToNext();
			}
			showMessage(false);
		}else
			showMessage(true);
		cursor.close();
	}
	
	private void showMessage(final boolean on){
		root.runOnUiThread(new Runnable(){
			public void run(){
				if(!on){
					TextView mess 	= (TextView) sv.findViewById(R.id.message);
		    		Button back 	= (Button) sv.findViewById(R.id.back);
		    		CommonHelper.gone(root, back);
		    		CommonHelper.gone(root, mess);
		    		CommonHelper.clearText(root, mess);
				}else{
					TextView mess 	= (TextView) sv.findViewById(R.id.message);
		    		Button back 	= (Button) sv.findViewById(R.id.back);
		    		back.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) { root.viewSwitcher.switchTo(WHAT_VIEW.ITEMS); }
					});
		    		CommonHelper.visible(root, back);
		    		CommonHelper.visible(root, mess);
		    		CommonHelper.setText(root, mess, "Нет фильтров");
				}
			}
		});
	}
	
	public void parseFilters(){
		root.filters.clear();
		new Thread(new Runnable(){
			public void run(){	
				root.db.clearFilterValues();
				if(!alreadyParsed)
					root.db.parser.filters(json, parent_id);
				else
					getFromDB();
			}
		}).start();
	}
	
	private void typeSelect(String Label, final String Key){
		LayoutInflater li = root.getLayoutInflater();
        final View filterLabelRow = li.inflate(R.layout.filter_label, null);
		final View filterValueRow = li.inflate(R.layout.filter_select, null);
		final Map<String, String> mapSpinnerKeys = new HashMap<String, String>();
		final Spinner spinnerView = (Spinner) filterValueRow.findViewById(R.id.select);
		TextView nameView = (TextView) filterLabelRow.findViewById(R.id.text);
		CommonHelper.setText(root, nameView, Label);
		
		List<String> list = new ArrayList<String>();
		
		Cursor cursor_v = root.db.getFilterValues(parent_id+Key);
		cursor_v.moveToFirst();
		Loger.d(tag, Label+" count:"+cursor_v.getCount());
		for(int k=0;k<cursor_v.getCount();k++){
			//String vId 		= cursor_v.getString(cursor_v.getColumnIndex("Id"));
			String vKey 	= cursor_v.getString(cursor_v.getColumnIndex("Key"));
			String vLabel 	= cursor_v.getString(cursor_v.getColumnIndex("Label"));
			//String vFromKey = cursor_v.getString(cursor_v.getColumnIndex("FromKey"));
			//String vToKey 	= cursor_v.getString(cursor_v.getColumnIndex("ToKey"));
			String vNum 	= cursor_v.getString(cursor_v.getColumnIndex("Num"));
			mapSpinnerKeys.put(vLabel, vKey);
			list.add(vLabel);
			Loger.d(tag, Label+" NUM: "+vNum);
			cursor_v.moveToNext();
		}
		cursor_v.close();
		final ArrayAdapter<String>	dataAdapter = new ArrayAdapter<String>(spinnerView.getContext(), android.R.layout.simple_spinner_item, list);
									dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerView.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				String val = dataAdapter.getItem(spinnerView.getSelectedItemPosition());
				root.db.addTempFilterValues(Key, mapSpinnerKeys.get(val));
				Loger.d(tag, "selected: "+val);
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				root.db.addTempFilterValues(Key, "");
				Loger.d(tag, "selected nothing");
			}
		});
		
		spinnerView.setAdapter(dataAdapter);
		if(dataAdapter.getCount()>0)
			root.runOnUiThread(new Runnable(){
				public void run(){
					lv.addView(filterLabelRow);
					lv.addView(filterValueRow);
				}
			});
	}
	
	private void typeOneCheckbox(String Label, final String Key){
		LayoutInflater li = root.getLayoutInflater();
        final View filterCheckboxRow = li.inflate(R.layout.filter_checkbox, null);
		CheckBox checkBoxView = (CheckBox) filterCheckboxRow.findViewById(R.id.checkbox);
		CommonHelper.setText(root, checkBoxView, Label);
		checkBoxView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Loger.d(tag, "checkBox: "+isChecked);
				if(!isChecked)
					root.db.addTempFilterValues(Key, "");
				else
					root.db.addTempFilterValues(Key, "on");
			}
		});
		root.runOnUiThread(new Runnable(){
			public void run(){
				lv.addView(filterCheckboxRow);
			}
		});
	}
	
	private void typeMultiCheckbox(String Label, String Id){
		LayoutInflater li = root.getLayoutInflater();
        final View filterLabelRow = li.inflate(R.layout.filter_label, null);
		TextView nameView = (TextView) filterLabelRow.findViewById(R.id.text);
		CommonHelper.setText(root, nameView, Label);
		root.runOnUiThread(new Runnable(){
			public void run(){
				lv.addView(filterLabelRow);
			}
		});
		
		Cursor cursor_v = root.db.getFilterValues(Id);
		int indexVKey 		= cursor_v.getColumnIndex("Key");
		int indexVLabel 	= cursor_v.getColumnIndex("Label");
		cursor_v.moveToFirst();
		Loger.d(tag, Label+" count multi:"+cursor_v.getCount());
		for(int k=0;k<cursor_v.getCount();k++){
			final String vKey 	= cursor_v.getString(indexVKey);
			String vLabel 	= cursor_v.getString(indexVLabel);
			
			final View filterCheckboxRow = li.inflate(R.layout.filter_checkbox, null);
			CheckBox checkBoxView = (CheckBox) filterCheckboxRow.findViewById(R.id.checkbox);
			CommonHelper.setText(root, checkBoxView, vLabel);
			checkBoxView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					Loger.d(tag, "checkBox: "+isChecked);
					if(!isChecked)
						root.db.addTempFilterValues(vKey, "");
					else
						root.db.addTempFilterValues(vKey, "on");
				}
			});
			root.runOnUiThread(new Runnable(){
				public void run(){
					lv.addView(filterCheckboxRow);
				}
			});
			
			cursor_v.moveToNext();
		}
		cursor_v.close();
	}
	
	private void typeTwoInputs(String Label, String Num){
		LayoutInflater li = root.getLayoutInflater();
        final View filterLabelRow = li.inflate(R.layout.filter_label, null);
		TextView nameView = (TextView) filterLabelRow.findViewById(R.id.text);
		CommonHelper.setText(root, nameView, Label);
		root.runOnUiThread(new Runnable(){
			public void run(){
				lv.addView(filterLabelRow);
			}
		});
		
		final View filterInputsRow = li.inflate(R.layout.filter_inputs, null);
		final EditText FromView 	= (EditText) filterInputsRow.findViewById(R.id.from);
		final EditText ToView 		= (EditText) filterInputsRow.findViewById(R.id.to);
		Cursor cursor_v = root.db.getFilterValues(parent_id+Num);
		cursor_v.moveToFirst();
		int indexFrom 	= cursor_v.getColumnIndex("FromKey");
		int indexTo		= cursor_v.getColumnIndex("ToKey");
		final String from 	= cursor_v.getString(indexFrom);
		final String to 	= cursor_v.getString(indexTo);
		cursor_v.close();
		FromView.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Loger.d(tag, "FromKey: "+FromView.getText().toString());
				root.db.addTempFilterValues(from, 	FromView.getText().toString());
			}
		});
		ToView.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Loger.d(tag, "ToKey: "+ToView.getText().toString());
				root.db.addTempFilterValues(to, 	ToView.getText().toString());
			}
		});
		root.runOnUiThread(new Runnable(){
			public void run(){
				lv.addView(filterInputsRow);
			}
		});
	}
}
