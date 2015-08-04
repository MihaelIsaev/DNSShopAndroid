package com.mihaelisaev.dnsshop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mihaelisaev.dnsshop.R;
import com.mihaelisaev.dnsshop.ViewSwitcher.WHAT_VIEW;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.ClipboardManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.mihaelisaev.helper.CommonHelper;

@SuppressLint({ "UseSparseArrays", "UseSparseArrays" })
@SuppressWarnings("deprecation")
public class Root extends Activity {
	public ViewSwitcher viewSwitcher;
	public SelectCity 	selectCity;
	public Home 		home;
	public Catalog 		catalog;
	public Cart 		cart;
	public Options 		options;
	public Items 		items;
	public Item 		item;
	public Filters 		filters;
	public Shops 		shops;
	public TextView		topLineTitle;
	public Button 		topLineBack, topLineFilter, topLineAccept, topLineDiscard, topLineSave, topLineDelete, topLineCopy, topLineLink, buttonShops, buttonCatalog, buttonContact;
	public LinearLayout tab_home, tab_catalog, tab_cart, tab_options;
	public ProgressBar 	pb;
	public Database 	db;
	public ClipboardManager clipboard;
	@SuppressWarnings("unused")
	private String tag = "Activity ROOT";
	
	private List<Integer> 				listLayoutIds 	= new ArrayList<Integer>();
	public  Map<Integer, LinearLayout> 	mapLinearLayout = new HashMap<Integer, LinearLayout>();
	public	Map<Integer, WHAT_VIEW>		mapHistory		= new HashMap<Integer, WHAT_VIEW>();
	
	public InputMethodManager imm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		deletePrevDB();
		db = new Database(this);
		changeSkinOnStartup();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.root);
		
		new Handler().postDelayed(new Runnable() { 
            public void run() { 
            	runOnUiThread(new Runnable() {
					@Override
					public void run() {
						includeClasses();
		        		setElementLinks();
						fillSQL();		        		
		        		fillViews();
		        		checkDefaultCity();
		        		bindButtons();
		        		viewSwitcher.currentView(WHAT_VIEW.HOME, false);
		        		addToHistory(WHAT_VIEW.HOME);
		        		TextView stv = (TextView) findViewById(R.id.startUpText);
						CommonHelper.gone(Root.this, stv);
						viewSwitcher.showBottom();
					}
				});
            }
        }, 300);
	}
	
	private void changeSkinOnStartup(){
		if(db.getKey(Settings.KEY_THEME).equals(Settings.THEME_BLACK) || db.getKey(Settings.KEY_THEME).equals(""))
			setTheme(R.style.Theme_Black);
		else if(db.getKey(Settings.KEY_THEME).equals(Settings.THEME_BLUE))
			setTheme(R.style.Theme_Blue);
	}
	
	public void switchTheme(){
		finish();
        startActivity(getIntent());
	}
	
	private void deletePrevDB(){
		try{
			if(Settings.CLEAR_DATA)
				this.deleteDatabase(Database.DB_NAME_PREV);
		}catch(Exception e){}
	}
	
	private void checkDefaultCity(){
		if(!db.checkKey("DefaultCity"))
			viewSwitcher.switchTo(WHAT_VIEW.SELECT_CITY);
		else
			updateCity();
	}
	
	private void setElementLinks(){
		viewSwitcher = new ViewSwitcher(this);
		
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		
		listLayoutIds.add(R.id.selectCity);
		listLayoutIds.add(R.id.search);
		listLayoutIds.add(R.id.home);
		listLayoutIds.add(R.id.catalog);
		listLayoutIds.add(R.id.cart);
		listLayoutIds.add(R.id.options);
		listLayoutIds.add(R.id.items);
		listLayoutIds.add(R.id.filters);
		listLayoutIds.add(R.id.item);
		listLayoutIds.add(R.id.reviews);
		listLayoutIds.add(R.id.comments);
		listLayoutIds.add(R.id.filter);
		listLayoutIds.add(R.id.shops);
		listLayoutIds.add(R.id.tab_host);
		listLayoutIds.add(R.id.topLine);
		
		for (Integer id : listLayoutIds)
			mapLinearLayout.put(id, (LinearLayout) findViewById(id));
		
		topLineTitle	= (TextView)		findViewById(R.id.title);
		topLineBack 	= (Button)			findViewById(R.id.buttonBack);
		topLineFilter 	= (Button)			findViewById(R.id.buttonFilter);
		topLineAccept 	= (Button)			findViewById(R.id.buttonAccept);
		topLineDiscard 	= (Button)			findViewById(R.id.buttonDiscard);
		topLineSave 	= (Button)			findViewById(R.id.buttonSave);
		topLineDelete 	= (Button)			findViewById(R.id.buttonDelete);
		topLineCopy 	= (Button)			findViewById(R.id.buttonCopy);
		topLineLink 	= (Button)			findViewById(R.id.buttonLink);
		buttonShops 	= (Button)			home.getView().findViewById(R.id.buttonShops);
		buttonCatalog 	= (Button)			home.getView().findViewById(R.id.buttonCatalog);
		buttonContact 	= (Button)			home.getView().findViewById(R.id.buttonContact);
		pb 				= (ProgressBar) 	findViewById(R.id.topProgress);
		
		tab_home 		= (LinearLayout)findViewById(R.id.tab_home);
		tab_catalog 	= (LinearLayout)findViewById(R.id.tab_catalog);
		tab_cart 		= (LinearLayout)findViewById(R.id.tab_cart);
		tab_options 	= (LinearLayout)findViewById(R.id.tab_more);
	}
	
	private void includeClasses(){
		clipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
		
		selectCity	= new SelectCity(this);
		home 		= new Home(this);
		catalog 	= new Catalog(this);
		cart 		= new Cart(this);
		options 	= new Options(this);
		items 		= new Items(this);
		item 		= new Item(this);
		filters 	= new Filters(this);
		shops 		= new Shops(this);
	}
	
	private void fillViews(){
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		
		mapLinearLayout.get(R.id.selectCity).addView(selectCity.getView(), lp);
		mapLinearLayout.get(R.id.search).addView(home.getSearchView(), lp);
		mapLinearLayout.get(R.id.home).addView(home.getView(), lp);		
		mapLinearLayout.get(R.id.catalog).addView(catalog.getView(), lp);
		mapLinearLayout.get(R.id.cart).addView(cart.getView(), lp);
		mapLinearLayout.get(R.id.options).addView(options.getView(), lp);
		mapLinearLayout.get(R.id.items).addView(items.getView(), lp);
		mapLinearLayout.get(R.id.item).addView(item.getView(), lp);
		mapLinearLayout.get(R.id.reviews).addView(item.getReviewsView(), lp);
		mapLinearLayout.get(R.id.comments).addView(item.getCommentsView(), lp);
		mapLinearLayout.get(R.id.filters).addView(filters.getView(), lp);
		mapLinearLayout.get(R.id.shops).addView(shops.getView(), lp);
	}
	
	public void updateCity(){
		TextView 	city 		= (TextView) 	findViewById(R.id.city);
		TextView 	cityHome	= (TextView) 	home.getView().findViewById(R.id.city);
		TextView 	phone 		= (TextView) 	home.getView().findViewById(R.id.phone);
    	Map<String, String> cityInfo = db.getCityNameAndPhone(db.getKey(Settings.KEY_DEF_CITY));
    	CommonHelper.setText(this, city, "г."+cityInfo.get("Name"));
    	CommonHelper.setText(this, cityHome, cityInfo.get("Name"));
    	CommonHelper.setText(this, phone, cityInfo.get("Phone"));
    	shops.showShops();
	}
	
	@Override
	protected void onDestroy() {
		if(db!=null){
			db.clearFilterValues();
			db.destroy();
		}
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		CommonHelper.loadAds(this);
		super.onResume();
	}
	
	@Override
    public boolean onSearchRequested() {
    	viewSwitcher.switchTo(WHAT_VIEW.HOME);
		if(!home.search.isFocused())
    		home.search.requestFocus();
    	else
    		imm.showSoftInput(home.search, InputMethodManager.SHOW_IMPLICIT);
        Loger.d("SEARCH", "search clicked");
        return false;
    }
	
	private void bindButtons(){
		buttonShops.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { viewSwitcher.switchTo(WHAT_VIEW.SHOPS); }
		});
		buttonCatalog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { viewSwitcher.switchTo(WHAT_VIEW.CATALOG); }
		});
		buttonContact.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/* Create the Intent */
				final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

				/* Fill it with Data */
				emailIntent.setType("plain/text");
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"me@mihaelisaev.com"});
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "DNS Shop отзыв");
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");

				/* Send it off to the Activity-Chooser */
				startActivity(Intent.createChooser(emailIntent, "Отправка отзыва DNS Shop"));
			}
		});
		
		tab_home.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { viewSwitcher.switchTo(WHAT_VIEW.HOME); }
		});
		
		tab_catalog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { viewSwitcher.switchTo(WHAT_VIEW.CATALOG); }
		});
		
		tab_cart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { viewSwitcher.switchTo(WHAT_VIEW.CART); }
		});
		
		tab_options.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { viewSwitcher.switchTo(WHAT_VIEW.OPTIONS); }
		});
	}
	
	private void fillSQL(){
		db.parser.category();
		new CityDownloader(this).execute();
		//db.parser.city();
		if(!db.checkKey("AlreadyLaunched"))
			db.setKey("AlreadyLaunched", "yes");
    }
	
	public void addToHistory(WHAT_VIEW view){
		int current = mapHistory.size();
		mapHistory.put(current, view);
	}
	
	public WHAT_VIEW getHistoryPrev(){
		if(mapHistory.size()>=2){
			int last 	= mapHistory.size()-2;
			int current = mapHistory.size()-1;
			if(mapHistory.get(current).equals(WHAT_VIEW.HOME))
				return null;
			else{
				mapHistory.remove(current);
				return mapHistory.get(last);
			}
		}else
			return null;
	}
	
	@Override
	public void onBackPressed() {
		WHAT_VIEW go = getHistoryPrev();
		if(null!=go)
			viewSwitcher.switchTo(go, true);
		else
			finish();
	}
}