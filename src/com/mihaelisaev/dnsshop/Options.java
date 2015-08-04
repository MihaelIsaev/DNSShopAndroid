package com.mihaelisaev.dnsshop;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.mihaelisaev.dnsshop.R;
import com.mihaelisaev.dnsshop.ViewSwitcher.WHAT_VIEW;

public class Options {

	private View result = null;
	private Root root;
	
	public Options(final Root root){
		this.root = root;
		LayoutInflater li = (LayoutInflater)root.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        result = li.inflate(R.layout.options, null);
        
        LinearLayout 	changeCityLinear 	= (LinearLayout) 	result.findViewById(R.id.changeCityLinear);
        TextView 		changeCityText 		= (TextView) 		result.findViewById(R.id.changeCityText);
        changeCityLinear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { Options.this.root.viewSwitcher.switchTo(WHAT_VIEW.CHANGE_CITY); }
		});
        changeCityText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { Options.this.root.viewSwitcher.switchTo(WHAT_VIEW.CHANGE_CITY); }
		});
        
        LinearLayout 	updateCityLinear 	= (LinearLayout) 	result.findViewById(R.id.updateCityLinear);
        TextView 		updateCityText 		= (TextView) 		result.findViewById(R.id.updateCityText);
        updateCityLinear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new CityDownloader(root).execute();
			}
		});
        updateCityText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new CityDownloader(root).execute();
			}
		});
        
        LinearLayout 	clearCacheLinear 	= (LinearLayout) 	result.findViewById(R.id.clearCacheLinear);
        TextView 		clearCacheText 		= (TextView) 		result.findViewById(R.id.clearCacheText);
        clearCacheLinear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { Options.this.root.db.clearCache(); }
		});
        clearCacheText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { Options.this.root.db.clearCache(); }
		});
        
        CheckBox 		changePreview 		= (CheckBox) 		result.findViewById(R.id.changePreview);
        if(null!=root.db.getKey(Settings.KEY_SHOW_PREVIEW))
        	if(root.db.getKey(Settings.KEY_SHOW_PREVIEW).equals(Settings.KEY_VALUE_ON))
        		changePreview.setChecked(true);
        changePreview.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(!isChecked)
					Options.this.root.db.setKey(Settings.KEY_SHOW_PREVIEW, Settings.KEY_VALUE_OFF);
				else
					Options.this.root.db.setKey(Settings.KEY_SHOW_PREVIEW, Settings.KEY_VALUE_ON);
			}
		});
        
        CheckBox changeBackgroundDescription = (CheckBox) result.findViewById(R.id.changeDescriptionBackground);
        if(null!=root.db.getKey(Settings.KEY_SHOW_DESCR_BACK))
        	if(root.db.getKey(Settings.KEY_SHOW_DESCR_BACK).equals(Settings.KEY_VALUE_ON))
        		changeBackgroundDescription.setChecked(true);
        changeBackgroundDescription.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(!isChecked){
					Options.this.root.db.setKey(Settings.KEY_SHOW_DESCR_BACK, Settings.KEY_VALUE_OFF);
					Options.this.root.item.itemFeatures.setBackgroundColor(Color.TRANSPARENT);
				}else{
					Options.this.root.db.setKey(Settings.KEY_SHOW_DESCR_BACK, Settings.KEY_VALUE_ON);
					Options.this.root.item.itemFeatures.setBackgroundColor(Color.BLACK);
				}
			}
		});
        
        final Spinner 	spinnerView 		= (Spinner) 		result.findViewById(R.id.changeSort);
		
		List<String> list = new ArrayList<String>();
		list.add("По цене");
		list.add("По названию");
		list.add("По рейтингу");
		
		final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(root, android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerView.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int val = spinnerView.getSelectedItemPosition();
				if(val == 0)
					Options.this.root.db.setKey(Settings.KEY_SORTBY, "Price");
				else if(val == 1)
					Options.this.root.db.setKey(Settings.KEY_SORTBY, "Name");
				else if(val == 2)
					Options.this.root.db.setKey(Settings.KEY_SORTBY, "Grade");
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				Options.this.root.db.setKey(Settings.KEY_SORTBY, "Price");
			}
		});
		spinnerView.setAdapter(dataAdapter);
		
		if(root.db.getKey(Settings.KEY_SORTBY).equals("Price") || root.db.getKey(Settings.KEY_SORTBY).equals(""))
			spinnerView.setSelection(0);
		else if(root.db.getKey(Settings.KEY_SORTBY).equals("Name"))
			spinnerView.setSelection(1);
		else if(root.db.getKey(Settings.KEY_SORTBY).equals("Grade"))
			spinnerView.setSelection(2);
		
		final Spinner 	spinnerViewSkin 		= (Spinner) 		result.findViewById(R.id.changeSkin);
		
		List<String> listSkins = new ArrayList<String>();
		listSkins.add("Черный");
		listSkins.add("Синий");
		
		final ArrayAdapter<String> dataAdapterSkin = new ArrayAdapter<String>(root, android.R.layout.simple_spinner_item, listSkins);
		dataAdapterSkin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerViewSkin.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int val = spinnerViewSkin.getSelectedItemPosition();
				if(val == 0){
					if(!Options.this.root.db.getKey(Settings.KEY_THEME).equals(Settings.THEME_BLACK) && !Options.this.root.db.getKey(Settings.KEY_THEME).equals(""))
						Options.this.root.switchTheme();
					Options.this.root.db.setKey(Settings.KEY_THEME, Settings.THEME_BLACK);
				}else if(val == 1){
					if(!Options.this.root.db.getKey(Settings.KEY_THEME).equals(Settings.THEME_BLUE))
						Options.this.root.switchTheme();
					Options.this.root.db.setKey(Settings.KEY_THEME, Settings.THEME_BLUE);
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				//Options.this.root.db.setKey("sortBy", "Price");
			}
		});
		spinnerViewSkin.setAdapter(dataAdapterSkin);
		
		if(root.db.getKey(Settings.KEY_THEME).equals(Settings.THEME_BLACK) || root.db.getKey(Settings.KEY_THEME).equals(""))
			spinnerViewSkin.setSelection(0);
		else if(root.db.getKey(Settings.KEY_THEME).equals(Settings.THEME_BLUE))
			spinnerViewSkin.setSelection(1);
		
		/*if(!Settings.ADS_MODE){
			TextView sorry = (TextView) result.findViewById(R.id.sorryText);
			sorry.setVisibility(View.VISIBLE);
		}*/
	}
	
	public View getView(){
		return result;
	}
}
