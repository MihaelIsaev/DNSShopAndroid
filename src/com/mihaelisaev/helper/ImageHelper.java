package com.mihaelisaev.helper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.widget.ImageView;

import com.mihaelisaev.dnsshop.Loger;
import com.mihaelisaev.dnsshop.Settings;

public class ImageHelper {
	public static final String tag = "ImageHelper.java"; 
	
	public static Bitmap getImageFromWEB(String url){
		Bitmap result = null;
		try{
			URL ulrn = new URL(url);
		    HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
		    InputStream is = con.getInputStream();
		    result = BitmapFactory.decodeStream(is);
		}catch(Exception e){}
		return result;
	}
	
	public static void saveImageToCache(Bitmap bmp, String filename){
		if(null != bmp){
		    try{
		    	File outputDir	= new File(CommonHelper.getCachePath());
		    	outputDir.mkdirs();
				BitmapFactory.Options 	options	=	new BitmapFactory.Options();
										options.inSampleSize = 5;
				File 					outputFile			=	new File(filename);
				FileOutputStream 		fileOutputStream	=	new FileOutputStream(outputFile);
				BufferedOutputStream 	bos					=	new BufferedOutputStream(fileOutputStream);
				bmp.compress(CompressFormat.JPEG, Settings.CACHE_IMAGE_QUALITY, bos);
				bos.flush();
				bos.close();
		    }catch(Exception e){
		    	Loger.d(tag, "Write file error: "+e.toString());
		    }
	    }
	}
	
	public static void loadImageFromServer(final Activity activity, final ImageView imageView, final String code, final String url, final String prefix){
		new Thread(new Runnable(){
			public void run(){
				String filename = buildCacheImageURI(code, prefix);
				File imageFile = new File(filename);
				if(!imageFile.exists()){
					Bitmap bmp = getImageFromWEB(url);
					saveImageToCache(bmp, filename);
					setImage(activity, bmp, imageView);
				}
			}
		}).start();
	}
	
	public static void loadImageFromCache(final Activity activity, final ImageView imageView, final String code, final int i, final Map<Integer, Bitmap> map, final String prefix){
		new Thread(new Runnable(){
			public void run(){
				String filename = buildCacheImageURI(code, prefix);
				File imageFile = new File(filename);
				if(imageFile.exists()){
					final Bitmap bmp = BitmapFactory.decodeFile(filename);
					map.put(i, bmp);
					setImage(activity, bmp, imageView);
				}
			}
		}).start();
	}
	
	public static String buildCacheImageURI(String code, String prefix){
		return CommonHelper.getCachePath() + prefix + code + ".jpg";
	}
	
	public static void setImage(final Activity r, final Bitmap bmp, final ImageView image){
    	r.runOnUiThread(new Runnable(){
			public void run(){
		    	if (null != bmp){
			    	image.setImageBitmap(bmp);
			    	CommonHelper.visible(r, image);
			    }else
			    	CommonHelper.hide(r, image);
			}
    	});
	}
}
