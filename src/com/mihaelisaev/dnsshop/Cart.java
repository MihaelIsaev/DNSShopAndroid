package com.mihaelisaev.dnsshop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.mihaelisaev.dnsshop.R;
import com.mihaelisaev.dnsshop.ViewSwitcher.WHAT_VIEW;
import com.mihaelisaev.helper.CommonHelper;

@SuppressLint("UseSparseArrays")
public class Cart {

	private Root root;
	private View result = null;
	public 	WHAT_VIEW 				from 			= WHAT_VIEW.ITEMS;
	private Map<Integer,Integer> 	mapItemKeys 	= new HashMap<Integer, Integer>();
    private Map<Integer,String> 	mapItemCode 	= new HashMap<Integer, String>();
    private Map<Integer,String> 	mapItemName 	= new HashMap<Integer, String>();
    private Map<Integer,String> 	mapItemPrice 	= new HashMap<Integer, String>();
    private Map<Integer,String> 	mapItemCount 	= new HashMap<Integer, String>();
    public String title = "Корзина 0р.";
    @SuppressWarnings("unused")
	private ScrollView sv;
    public LinearLayout lv;
    private String tag = "Activity CART";
	
	
	public Cart(Root root){
		this.root = root;
		LayoutInflater li = (LayoutInflater)root.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        result = li.inflate(R.layout.catalog, null);
        sv = (ScrollView) result.findViewById(R.id.scrollView);
		lv = (LinearLayout) result.findViewById(R.id.listView);
	}
	
	public View getView(){
		return result;
	}
	
	public void truncate(){
		root.db.deleteFavs();
	}
	
	public void save(){
		Cursor cursor = root.db.getFavs();
		cursor.moveToFirst();
		int indexCode  = cursor.getColumnIndex("Code");
		int indexName  = cursor.getColumnIndex("Name");
		int indexPrice = cursor.getColumnIndex("Price");
		int indexCount = cursor.getColumnIndex("Count");
		if(cursor.getCount() == 0){
			cursor.close();
			Toast.makeText(root.getApplicationContext(), root.db.getResourse(R.string.emptyCartCantSave), Toast.LENGTH_SHORT).show(); 
			return;
		}
		
		
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df3 = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");  
		String currentDateTimeString = df3.format(c.getTime());
		String fileName = "sdcard/cart_"+currentDateTimeString+".html";
		File file = new File(fileName);
		if (!file.exists())
			try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
		try {
			BufferedWriter buf = new BufferedWriter(new FileWriter(file, true)); 
			buf.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body><table><thead><tr><td>Код</td><td>Наименование</td><td>Цена</td><td>Количество</td></tr></thead><tbody>");
			buf.newLine();
			
			for(int i=0; i<cursor.getCount(); i++){
				buf.append("<tr><td>");
				buf.append(cursor.getString(indexCode));
				buf.append("</td><td>");
				buf.append(cursor.getString(indexName));
				buf.append("</td><td>");
				buf.append(cursor.getString(indexPrice));
				buf.append("</td><td>");
				buf.append(cursor.getString(indexCount));
				buf.append("</td></tr>");
				buf.newLine();
				cursor.moveToNext();
			}
			cursor.close();
			
			buf.append("</tbody></table>");
			buf.append("<br><h3>Сумма = ");
			buf.append(root.db.getFavSumm()+"руб.");
			buf.append("</h3></body></html>");
			buf.newLine();
			
			buf.close();
			Toast.makeText(root.getApplicationContext(), "Успешно сохранено в "+fileName, Toast.LENGTH_SHORT).show(); 
		} catch (IOException e) { Toast.makeText(root.getApplicationContext(), "Ошибка записи на SD-карту", Toast.LENGTH_SHORT).show();  } catch(Exception e){ Toast.makeText(root.getApplicationContext(), "Ошибка", Toast.LENGTH_SHORT).show(); }
	}
	
	public void updateSumm(){
		title = "Сумма "+root.db.getFavSumm()+"р.";
		if(root.viewSwitcher.currentView.equals(WHAT_VIEW.CART))
			CommonHelper.setText(root, root.topLineTitle, title);
	}
	
	public void showList(){
		updateSumm();
		
		lv.removeAllViews();
		final Cursor cursor = root.db.getFavs();
		cursor.moveToFirst();
		int indexCode  = cursor.getColumnIndex("Code");
		int indexName  = cursor.getColumnIndex("Name");
		int indexPrice = cursor.getColumnIndex("Price");
		int indexCount = cursor.getColumnIndex("Count");
		if(cursor.getCount() == 0){
			cursor.close();
			return;
		}
		
		for(int i=0; i<cursor.getCount(); i++){
			mapItemKeys.put(i, i);
			mapItemCode.put(i, cursor.getString(indexCode));
			mapItemName.put(i, cursor.getString(indexName));
			mapItemPrice.put(i, cursor.getString(indexPrice));
			mapItemCount.put(i, cursor.getString(indexCount));
			cursor.moveToNext();
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		List mpKeys = new ArrayList(mapItemPrice.keySet());
		@SuppressWarnings({ "rawtypes", "unchecked" })
		List mpValues = new ArrayList(mapItemPrice.values());

		@SuppressWarnings({ "rawtypes", "unused" })
		HashMap map = new LinkedHashMap();
		@SuppressWarnings({ "rawtypes", "unchecked" })
		TreeSet sortedSet = new TreeSet(mpValues);
		Object[] sortedArray = sortedSet.toArray();
		int size = sortedArray.length;
		
		for (int i=size-1; i>=0; i--){
			final String code = mapItemCode.get(mpKeys.get(mpValues.indexOf(sortedArray[i])));
			final String name = mapItemName.get(mpKeys.get(mpValues.indexOf(sortedArray[i])));
			final String price = mapItemPrice.get(mpKeys.get(mpValues.indexOf(sortedArray[i])));
			final String count = mapItemCount.get(mpKeys.get(mpValues.indexOf(sortedArray[i])));
			
			LayoutInflater li = (LayoutInflater) root.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        final View itemRow = li.inflate(R.layout.list_row, null);
			TextView nameView = (TextView) itemRow.findViewById(R.id.text);
			final TextView priceView = (TextView) itemRow.findViewById(R.id.price);
			LinearLayout itemView = (LinearLayout) itemRow.findViewById(R.id.item);
			LinearLayout plusView = (LinearLayout) itemRow.findViewById(R.id.plus);
			LinearLayout minusView = (LinearLayout) itemRow.findViewById(R.id.minus);
			Button plusBview = (Button) itemRow.findViewById(R.id.plusB);
			Button minusBview = (Button) itemRow.findViewById(R.id.minusB);
			CommonHelper.setText(root, nameView, "Код: "+code+"\n"+name);
			CommonHelper.setText(root, priceView, count+" x "+price+"р.");
			itemView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Loger.d(tag, "click item Code:"+code);
					root.item.from = WHAT_VIEW.CART;
					root.item.title = name;
					if(!root.item.id.equals(code)){
						root.item.id = code;
						root.item.load();
					}
					root.viewSwitcher.switchTo(WHAT_VIEW.ITEM);
				}
			});
			
			itemView.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					editComment(code);
					return false;
				}
			});
			
			plusView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					root.db.updateFavCount(code, root.db.getFavCount(code)+1);
					CommonHelper.setText(root, priceView, root.db.getFavCount(code)+" x "+price+"р.");
					updateSumm();
				}
			});
			minusView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(root.db.getFavCount(code)==1)
						root.db.deleteFav(code);
					else
						root.db.updateFavCount(code, root.db.getFavCount(code)-1);
					if(root.db.getFavCount(code)>=1)
						CommonHelper.setText(root, priceView, root.db.getFavCount(code)+" x "+price+"р.");
					else
						CommonHelper.gone(root, itemRow);
					
					updateSumm();
				}
			});
			minusView.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					root.db.deleteFav(code);
					CommonHelper.gone(root, itemRow);
					updateSumm();
					return false;
				}
			});
			
			plusBview.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					root.db.updateFavCount(code, root.db.getFavCount(code)+1);
					CommonHelper.setText(root, priceView, root.db.getFavCount(code)+" x "+price+"р.");
					updateSumm();
				}
			});
			minusBview.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(root.db.getFavCount(code)==1)
						root.db.deleteFav(code);
					else
						root.db.updateFavCount(code, root.db.getFavCount(code)-1);
					if(root.db.getFavCount(code)>=1)
						CommonHelper.setText(root, priceView, root.db.getFavCount(code)+" x "+price+"р.");
					else
						CommonHelper.gone(root, itemRow);
					
					updateSumm();
				}
			});
			minusBview.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					root.db.deleteFav(code);
					CommonHelper.gone(root, itemRow);
					updateSumm();
					return false;
				}
			});
			
			lv.addView(itemRow);
		}
		
		cursor.close();
	}
	
	public void editComment(final String code){
		final EditText et = new EditText(root);
		et.setHint("напишите комментарий к товару");
		et.setMinLines(4);
		et.setText(root.db.getFavComment(code));
		
		AlertDialog.Builder deleteAlert = new AlertDialog.Builder(root)
		.setCancelable(true)
		.setTitle("Комментарий")		
		.setView(et)
		.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				root.db.setFavComment(code, et.getText().toString());
			}
		})
		.setNegativeButton("отмена", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		
		deleteAlert.show();
	}
}
