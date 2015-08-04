package com.mihaelisaev.helper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.mihaelisaev.dnsshop.Loger;
import com.mihaelisaev.dnsshop.R;
import com.mihaelisaev.dnsshop.Settings;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

public class CommonHelper {
	public static int randomColor(){
		Random color = new Random();
		int randomColor = Color.argb(255, color.nextInt(256), color.nextInt(256), color.nextInt(256));
		return randomColor;
	}
	
	public static String readRawTextFile(Context ctx, int resId) {
		InputStream inputStream = ctx.getResources().openRawResource(resId);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int i;
		try {
			i = inputStream.read();
			while (i != -1) {
				byteArrayOutputStream.write(i);
				i = inputStream.read();
			}
			inputStream.close();
		} catch (IOException e) {return null;}
		return byteArrayOutputStream.toString();
	}
	
	@SuppressWarnings("deprecation")
	public static void loadAds(Activity act){
		if(Settings.ADS_MODE){
			try{
				AdView adView = (AdView) act.findViewById(R.id.adView);
				AdRequest request = new AdRequest();
				if(Settings.DEBUG_MODE){
					request.setTesting(true);
					request.addTestDevice(AdRequest.TEST_EMULATOR);
					request.addTestDevice("emulator-5554");
				}
				adView.loadAd(request);
			}catch(Exception e){
				Loger.d("ADMOB", "error: "+e.toString());
			}
		}
	}
	
	public static boolean checkInternetConnection(Context context){
		boolean result = false;
		try{
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		    if (cm.getActiveNetworkInfo() != null
		            && cm.getActiveNetworkInfo().isAvailable()
		            && cm.getActiveNetworkInfo().isConnected()) {
		        result = true;
		    }
		}
		catch(Exception e){
			e.printStackTrace();
		}
		Loger.d("InternetConnection", "status = "+result);
		return result;
	}
	
	public static String httpResponseHTML(HttpResponse response){
		String html = null;
		try{
			InputStream in = response.getEntity().getContent();
			Header contentEncoding = response.getFirstHeader("Content-Encoding");
			if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip"))
				in = new GZIPInputStream(in);
			html = inputStreamToString(in);
			in.close();
		}catch(Exception e){}
		return html;
	}
	
	public static String inputStreamToString(InputStream in) {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			String line = null;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringBuilder.toString();
	}
	
	public static String getCachePath(){
		File sdcard = Environment.getExternalStorageDirectory();
		return sdcard.getAbsolutePath() + File.separator+ Settings.CACHE_IMAGE_FOLDER + File.separator;
	}
	
	public static void setText(Activity r, final TextView v, final String text){
		r.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try{
					v.setSingleLine(false);
					v.setText(Html.fromHtml(text));
				}catch(Exception e){}
			}
		});
	}
	
	public static void setButtonText(Activity r, final Button v, final String text){
		r.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try{
					v.setSingleLine(false);
					v.setText(Html.fromHtml(text));
				}catch(Exception e){}
			}
		});
	}
	
	public static void setRaiting(Activity r, final RatingBar v, final String value){
		r.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try{
					v.setRating(new Float(value));
				}catch(Exception e){}
			}
		});
	}
	
	public static void clearText(Activity r, final TextView v){
		r.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try{
					v.setText("");
				}catch(Exception e){}
			}
		});
	}
	
	public static void setTransparent(Activity r, final View v){
		r.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				v.setBackgroundColor(Color.TRANSPARENT);
			}
		});
	}
	
	public static void visible(Activity r, final View v){
		r.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				v.setVisibility(View.VISIBLE);
			}
		});
	}
	
	public static void hide(Activity r, final View v){
		r.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				v.setVisibility(View.INVISIBLE);
			}
		});
	}
	
	public static void gone(Activity r, final View v){
		r.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				v.setVisibility(View.GONE);
			}
		});
	}
	
	public static void checkValueForTextView(Activity r, View t, String v, String may, String alt){
		checkValueForTextView(r, t, v, may, alt, "");
	}
	
	public static void checkValueForTextView(Activity r, final View t, final String v, final String may, final String alt, final String suffix){
		r.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TextView tv = (TextView) t;
				try{
					if(!v.equals(may)){
						tv.setSingleLine(false);
						tv.setText(Html.fromHtml(v+suffix));
					}else
						tv.setText(alt);
				}catch(Exception e){}
			}
		});
	}

	public static String getImageFileName(String id) {
		return CommonHelper.getCachePath() + Settings.CACHE_IMAGE_PREFIX + id + Settings.CACHE_IMAGE_FORMAT;
	}
}
