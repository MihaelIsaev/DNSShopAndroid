package com.mihaelisaev.dnsshop;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.mihaelisaev.helper.CommonHelper;

import android.os.AsyncTask;

public class CityDownloader extends AsyncTask<String, String, String> {
	
	private Root root;
	
	public CityDownloader(Root root) 
	{
	    this.root = root;
	}
	
	@Override
	protected void onPreExecute() {
	    super.onPreExecute();
	    CommonHelper.visible(root, root.pb);
	}
	
	@Override
	protected String doInBackground(String... aurl) {
		List<NameValuePair> 	params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("what",		"cityes"));
		publishProgress("100");
		return root.db.server.executeHTTPRequest(root.db.server.buildURL(), params);
	}
	
	@Override
	protected void onPostExecute(String result) {
		CommonHelper.hide(root, root.pb);
		new CityParser(root).execute(result);
	}
}