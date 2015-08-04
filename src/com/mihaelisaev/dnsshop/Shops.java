package com.mihaelisaev.dnsshop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mihaelisaev.dnsshop.R;
import com.mihaelisaev.helper.CommonHelper;

public class Shops {

	private View result = null;
	private Root root;
	@SuppressWarnings("unused")
	private ScrollView sv;
    public LinearLayout lv;
	
	public Shops(Root root){
		this.root = root;
		LayoutInflater li = (LayoutInflater)root.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        result = li.inflate(R.layout.catalog, null);
        sv = (ScrollView) result.findViewById(R.id.scrollView);
		lv = (LinearLayout) result.findViewById(R.id.listView);
	}
	
	public View getView(){
		return result;
	}
	
	private void clear(){
		lv.removeAllViews();
	}
	
	public void showShops(){
		clear();
		final List<NameValuePair> 	params = new ArrayList<NameValuePair>(2);
									params.add(new BasicNameValuePair("what", 	"shops"));
									params.add(new BasicNameValuePair("city", 	root.db.getKey(Settings.KEY_DEF_CITY)));
		new Thread(new Runnable(){
			public void run(){
				final String shopsJSON = root.db.server.executeHTTPRequest(root.db.server.buildURL(), params);
				root.runOnUiThread(new Runnable(){
					public void run(){
						try{
							JSONObject jShops = new JSONObject(shopsJSON);
							List<String> jShopsKeyNames = new ArrayList<String>();
							Map<String, String> jShopsJSONByKey = new HashMap<String,String>();
							root.db.parser.extractKeysFromJSONObject(jShops, jShopsKeyNames, jShopsJSONByKey);
							for (String name : jShopsKeyNames) {
								String json = jShopsJSONByKey.get(name);
								JSONObject jShopJSON = new JSONObject(json);
								
								LayoutInflater li = (LayoutInflater) root.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						        View shopRow = li.inflate(R.layout.shop_row, null);
								TextView nameView = (TextView) shopRow.findViewById(R.id.name);
								TextView addressView = (TextView) shopRow.findViewById(R.id.address);
								TextView workTimeView = (TextView) shopRow.findViewById(R.id.workTime);
								CommonHelper.setText(root, nameView, jShopJSON.getString("Name"));
								CommonHelper.setText(root, addressView, jShopJSON.getString("Address"));
								CommonHelper.setText(root, workTimeView, jShopJSON.getString("WorkTime"));
								
								lv.addView(shopRow);
							}
						} catch(Exception e) {  } finally {  }
					} 
				});
			}
		}).start();
	}
}
