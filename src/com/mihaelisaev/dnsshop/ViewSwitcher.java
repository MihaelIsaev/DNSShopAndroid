package com.mihaelisaev.dnsshop;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mihaelisaev.dnsshop.R;
import com.mihaelisaev.helper.CommonHelper;

public class ViewSwitcher {
	private Root root;
	public enum WHAT_VIEW {	SELECT_CITY,
							CHANGE_CITY,
							HOME,
							CATALOG,
							ITEMS,
							ITEM,
							CART,
							CART_LIST,
							SHOPS,
							SEARCH,
							REVIEWS,
							COMMENTS,
							OPTIONS,
							FILTERS}
	
	public WHAT_VIEW currentView;
	public WHAT_VIEW backToView;
	
	public ViewSwitcher(Root root){
		this.root = root;
	}
	
	public void currentView(WHAT_VIEW view, boolean fromBack){
		if(!fromBack && null!=currentView)
			if(!currentView.equals(view)){
				root.addToHistory(view);
				Loger.d("switcher", "add to history: "+view);
			}
		Loger.d("switcher", "current view: "+view);
		currentView = view;
	}
	
	public void switchTo(WHAT_VIEW to){
		switchTo(to, false);
	}
	
	public void switchTo(WHAT_VIEW to, boolean fromBack){
		if(null!=currentView)
			if(currentView.equals(WHAT_VIEW.FILTERS) || currentView.equals(WHAT_VIEW.COMMENTS) || currentView.equals(WHAT_VIEW.REVIEWS))
				fromBack = true;		
		
		root.topLineBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { switchTo(root.getHistoryPrev(), true); }
		});
		
		switch(to){
			case SELECT_CITY:{
				hideAllLinears();
				currentView(WHAT_VIEW.SELECT_CITY, fromBack);
				hideHead();
				hideBottom();
				hideBack();
				hideFilter();
				hideAccept();
				hideDiscard();
				hideSave();
				hideDelete();
				hideCopy();
				hideLink();
				CommonHelper.clearText(root, root.topLineTitle);
				root.selectCity.showCityes();
				CommonHelper.visible(root, root.mapLinearLayout.get(R.id.selectCity));				
				root.tab_home.setBackgroundResource(R.drawable.tab_passive);
				root.tab_catalog.setBackgroundResource(R.drawable.tab_passive);
				root.tab_cart.setBackgroundResource(R.drawable.tab_passive);
				root.tab_options.setBackgroundResource(R.drawable.tab_passive);
				break;
			}
			case CHANGE_CITY:{
				hideAllLinears();
				currentView(WHAT_VIEW.CHANGE_CITY, fromBack);
				showHead();
				showBottom();
				showBack();
				hideFilter();
				hideAccept();
				hideDiscard();
				hideSave();
				hideDelete();
				hideCopy();
				hideLink();
				root.topLineTitle.setText("");
				root.selectCity.showCityes();
				CommonHelper.visible(root, root.mapLinearLayout.get(R.id.selectCity));
				root.tab_home.setBackgroundResource(R.drawable.tab_passive);
				root.tab_catalog.setBackgroundResource(R.drawable.tab_passive);
				root.tab_cart.setBackgroundResource(R.drawable.tab_passive);
				root.tab_options.setBackgroundResource(R.drawable.tab_active);
				break;
			}
			case HOME:{
				hideAllLinears();
				currentView(WHAT_VIEW.HOME, fromBack);
				showBottom();
				hideHead();
				hideBack();
				hideFilter();
				hideAccept();
				hideDiscard();
				hideSave();
				hideDelete();
				hideCopy();
				hideLink();
				CommonHelper.clearText(root, root.topLineTitle);
				root.updateCity();
				CommonHelper.visible(root, root.mapLinearLayout.get(R.id.home));				
				root.tab_home.setBackgroundResource(R.drawable.tab_active);
				root.tab_catalog.setBackgroundResource(R.drawable.tab_passive);
				root.tab_cart.setBackgroundResource(R.drawable.tab_passive);
				root.tab_options.setBackgroundResource(R.drawable.tab_passive);
				break;
			}
			case CATALOG:{
				hideAllLinears();
				currentView(WHAT_VIEW.CATALOG, fromBack);
				showHead();
				showBottom();
				showBack();
				hideFilter();
				hideAccept();
				hideDiscard();
				hideSave();
				hideDelete();
				hideCopy();
				hideLink();
				CommonHelper.setText(root, root.topLineTitle, "Каталог");
				CommonHelper.visible(root, root.mapLinearLayout.get(R.id.catalog));				
				root.tab_home.setBackgroundResource(R.drawable.tab_passive);
				root.tab_catalog.setBackgroundResource(R.drawable.tab_active);
				root.tab_cart.setBackgroundResource(R.drawable.tab_passive);
				root.tab_options.setBackgroundResource(R.drawable.tab_passive);
				break;
			}
			case ITEMS:{
				hideAllLinears();
				root.items.mapPreviews.clear();
				currentView(WHAT_VIEW.ITEMS, fromBack);
				showHead();
				showBottom();
				showBack();
				showFilter();
				hideAccept();
				hideDiscard();
				hideSave();
				hideDelete();
				hideCopy();
				hideLink();
				CommonHelper.setText(root, root.topLineTitle, root.items.title);
				root.topLineFilter.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) { switchTo(WHAT_VIEW.FILTERS); }
				});
				CommonHelper.visible(root, root.mapLinearLayout.get(R.id.items));				
				root.tab_home.setBackgroundResource(R.drawable.tab_passive);
				root.tab_catalog.setBackgroundResource(R.drawable.tab_active);
				root.tab_cart.setBackgroundResource(R.drawable.tab_passive);
				root.tab_options.setBackgroundResource(R.drawable.tab_passive);
				break;
			}
			case ITEM:{
				hideAllLinears();
				currentView(WHAT_VIEW.ITEM, fromBack);
				CommonHelper.setText(root, root.topLineTitle, "Просмотр товара");
				showHead();
				showBottom();
				showBack();
				hideFilter();
				hideAccept();
				hideDiscard();
				hideSave();
				hideDelete();
				showCopy();
				showLink();
				root.topLineCopy.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) { root.item.copy(); }
				});
				root.topLineLink.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) { root.item.link(); }
				});
				root.cart.showList();
				CommonHelper.visible(root, root.mapLinearLayout.get(R.id.item));				
				root.tab_home.setBackgroundResource(R.drawable.tab_passive);
				root.tab_catalog.setBackgroundResource(R.drawable.tab_active);
				root.tab_cart.setBackgroundResource(R.drawable.tab_passive);
				root.tab_options.setBackgroundResource(R.drawable.tab_passive);
				break;
			}
			case CART:{
				hideAllLinears();
				currentView(WHAT_VIEW.CART, fromBack);
				CommonHelper.setText(root, root.topLineTitle, "Сумма 0р.");
				showHead();
				showBottom();
				showBack();
				hideFilter();
				hideAccept();
				hideDiscard();
				showSave();
				showDelete();
				hideCopy();
				hideLink();
				root.topLineSave.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						root.cart.save();
					}
				});
				root.topLineDelete.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						root.cart.truncate();
						root.cart.showList();
						Toast.makeText(root.getApplicationContext(), "Закладки очищены", Toast.LENGTH_SHORT).show(); 
						return true;
					}
				});
				root.topLineDelete.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Toast.makeText(root.getApplicationContext(), "Удерживайте долго для очистки закладок", Toast.LENGTH_SHORT).show(); 
					}
				});
				root.cart.showList();
				CommonHelper.visible(root, root.mapLinearLayout.get(R.id.cart));
				root.tab_home.setBackgroundResource(R.drawable.tab_passive);
				root.tab_catalog.setBackgroundResource(R.drawable.tab_passive);
				root.tab_cart.setBackgroundResource(R.drawable.tab_active);
				root.tab_options.setBackgroundResource(R.drawable.tab_passive);
				break;
			}
			case CART_LIST:{
				currentView(WHAT_VIEW.CART_LIST, fromBack);
				CommonHelper.setText(root, root.topLineTitle, "Списки покупок");
				
				break;
			}
			case SHOPS:{
				hideAllLinears();
				currentView(WHAT_VIEW.SHOPS, fromBack);
				showHead();
				showBottom();
				showBack();
				hideFilter();
				hideAccept();
				hideDiscard();
				hideSave();
				hideDelete();
				hideCopy();
				hideLink();
				CommonHelper.setText(root, root.topLineTitle, "Магазины");
				CommonHelper.visible(root, root.mapLinearLayout.get(R.id.shops));
				root.tab_home.setBackgroundResource(R.drawable.tab_active);
				root.tab_catalog.setBackgroundResource(R.drawable.tab_passive);
				root.tab_cart.setBackgroundResource(R.drawable.tab_passive);
				root.tab_options.setBackgroundResource(R.drawable.tab_passive);
				break;
			}
			case SEARCH:{
				hideAllLinears();
				currentView(WHAT_VIEW.SEARCH, fromBack);
				CommonHelper.setText(root, root.topLineTitle, "Результаты поиска");
				showHead();
				showBottom();
				showBack();
				hideFilter();
				hideAccept();
				hideDiscard();
				hideSave();
				hideDelete();
				hideCopy();
				hideLink();
				CommonHelper.visible(root, root.mapLinearLayout.get(R.id.search));
				root.tab_home.setBackgroundResource(R.drawable.tab_active);
				root.tab_catalog.setBackgroundResource(R.drawable.tab_passive);
				root.tab_cart.setBackgroundResource(R.drawable.tab_passive);
				root.tab_options.setBackgroundResource(R.drawable.tab_passive);
				break;
			}
			case REVIEWS:{
				hideAllLinears();
				currentView(WHAT_VIEW.REVIEWS, fromBack);
				CommonHelper.setText(root, root.topLineTitle, "Отзывы");
				showHead();
				showBottom();
				showBack();
				hideFilter();
				hideAccept();
				hideDiscard();
				hideSave();
				hideDelete();
				hideCopy();
				hideLink();
				CommonHelper.visible(root, root.mapLinearLayout.get(R.id.reviews));
				root.tab_home.setBackgroundResource(R.drawable.tab_passive);
				root.tab_catalog.setBackgroundResource(R.drawable.tab_active);
				root.tab_cart.setBackgroundResource(R.drawable.tab_passive);
				root.tab_options.setBackgroundResource(R.drawable.tab_passive);
				break;
			}
			case COMMENTS:{
				hideAllLinears();
				currentView(WHAT_VIEW.COMMENTS, fromBack);
				CommonHelper.setText(root, root.topLineTitle, "Комментарии");
				showHead();
				showBottom();
				showBack();
				hideFilter();
				hideAccept();
				hideDiscard();
				hideSave();
				hideDelete();
				hideCopy();
				hideLink();
				CommonHelper.visible(root, root.mapLinearLayout.get(R.id.comments));
				root.tab_home.setBackgroundResource(R.drawable.tab_passive);
				root.tab_catalog.setBackgroundResource(R.drawable.tab_active);
				root.tab_cart.setBackgroundResource(R.drawable.tab_passive);
				root.tab_options.setBackgroundResource(R.drawable.tab_passive);
				break;
			}
			case OPTIONS:{
				hideAllLinears();
				currentView(WHAT_VIEW.OPTIONS, fromBack);
				showHead();
				showBottom();
				showBack();
				hideFilter();
				hideAccept();
				hideDiscard();
				hideSave();
				hideDelete();
				hideCopy();
				hideLink();
				CommonHelper.setText(root, root.topLineTitle, "Опции");
				CommonHelper.visible(root, root.mapLinearLayout.get(R.id.options));
				root.tab_home.setBackgroundResource(R.drawable.tab_passive);
				root.tab_catalog.setBackgroundResource(R.drawable.tab_passive);
				root.tab_cart.setBackgroundResource(R.drawable.tab_passive);
				root.tab_options.setBackgroundResource(R.drawable.tab_active);
				break;
			}
			case FILTERS:{
				hideAllLinears();
				currentView(WHAT_VIEW.FILTERS, true);
				showHead();
				showBottom();
				showBack();
				hideFilter();
				showAccept();
				showDiscard();
				hideSave();
				hideDelete();
				hideCopy();
				hideLink();
				CommonHelper.setText(root, root.topLineTitle, "Фильтры");
				root.topLineAccept.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						root.imm.hideSoftInputFromWindow(root.home.search.getWindowToken(), 0);
						root.items.countFilterFind = 0;
						root.items.showItems();
						switchTo(WHAT_VIEW.ITEMS); }
				});
				root.topLineDiscard.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						root.imm.hideSoftInputFromWindow(root.home.search.getWindowToken(), 0);
						root.db.clearFilterValues();
						root.filters.parseFilters();
						root.items.countFilterFind 	= 0;
						root.items.parent_id 		= root.filters.parent_id;
						root.items.showItems();
						switchTo(WHAT_VIEW.ITEMS); }
				});
				CommonHelper.visible(root, root.mapLinearLayout.get(R.id.filters));
				root.tab_home.setBackgroundResource(R.drawable.tab_passive);
				root.tab_catalog.setBackgroundResource(R.drawable.tab_active);
				root.tab_cart.setBackgroundResource(R.drawable.tab_passive);
				root.tab_options.setBackgroundResource(R.drawable.tab_passive);
				break;
			}
		}
	}
	
	public void hideBack(){ CommonHelper.gone(root, root.topLineBack); }
	public void showBack(){ CommonHelper.visible(root, root.topLineBack); }
	public void hideFilter(){ CommonHelper.gone(root, root.topLineFilter); }
	public void showFilter(){ CommonHelper.visible(root, root.topLineFilter); }
	public void hideAccept(){ CommonHelper.gone(root, root.topLineAccept); }
	public void showAccept(){ CommonHelper.visible(root, root.topLineAccept); }
	public void hideDiscard(){ CommonHelper.gone(root, root.topLineDiscard); }
	public void showDiscard(){ CommonHelper.visible(root, root.topLineDiscard); }
	public void hideSave(){ CommonHelper.gone(root, root.topLineSave); }
	public void showSave(){ CommonHelper.visible(root, root.topLineSave); }
	public void hideDelete(){ CommonHelper.gone(root, root.topLineDelete); }
	public void showDelete(){ CommonHelper.visible(root, root.topLineDelete); }
	public void hideCopy(){ CommonHelper.gone(root, root.topLineCopy); }
	public void showCopy(){ CommonHelper.visible(root, root.topLineCopy); }
	public void hideLink(){ CommonHelper.gone(root, root.topLineLink); }
	public void showLink(){ CommonHelper.visible(root, root.topLineLink); }
	
	public void hideHead(){ CommonHelper.gone(root, root.mapLinearLayout.get(R.id.topLine)); }
	public void showHead(){ CommonHelper.visible(root, root.mapLinearLayout.get(R.id.topLine)); }
	
	public void hideBottom(){ CommonHelper.gone(root, root.mapLinearLayout.get(R.id.tab_host)); }
	public void showBottom(){ CommonHelper.visible(root, root.mapLinearLayout.get(R.id.tab_host)); }
	
	public void hideAllLinears(){
		for (LinearLayout layout : root.mapLinearLayout.values())
			CommonHelper.gone(root, layout);
	}
}
