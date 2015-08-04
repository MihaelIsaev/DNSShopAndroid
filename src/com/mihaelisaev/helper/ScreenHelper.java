package com.mihaelisaev.helper;

import java.util.HashMap;
import java.util.Map;

import com.mihaelisaev.dnsshop.Loger;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.WindowManager;


public class ScreenHelper {
	public static final int SCREEN_WIDTH_DIP 	= 0;
	public static final int SCREEN_HEIGHT_DIP 	= 1;
	public static final int SCREEN_WIDTH_PX 	= 2;
	public static final int SCREEN_HEIGHT_PX 	= 3;
	
	public static Map<Integer, Integer> getScreenSize(Activity activity){
		WindowManager wm=activity.getWindowManager();
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		int screenWidthInPx=dm.widthPixels;
		int screenHeightInPx=dm.heightPixels;
		int screenWidthInDip=(int) (dm.widthPixels*dm.density);
		int screenHeightInDip=(int) (dm.heightPixels*dm.density);
		Loger.d("screen size", "screenWidthInPx"+screenWidthInPx);
		Loger.d("screen size", "screenHeightInPx"+screenHeightInPx);
		Loger.d("screen size", "screenWidthInDip"+screenWidthInDip);
		Loger.d("screen size", "screenHeightInDip"+screenHeightInDip);
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		result.put(SCREEN_WIDTH_DIP, screenWidthInDip);
		result.put(SCREEN_HEIGHT_DIP, screenHeightInDip);
		result.put(SCREEN_WIDTH_PX, screenWidthInPx);
		result.put(SCREEN_HEIGHT_PX, screenHeightInPx);
		return result;
	}
}
