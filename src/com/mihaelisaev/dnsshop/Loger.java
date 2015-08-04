package com.mihaelisaev.dnsshop;




import android.util.Log;

public class Loger {
	public static void i(String tag, String message) {
		if(Settings.DEBUG_MODE)
			Log.i(tag, message);
	}

	public static void d(String tag, String message) {
		if(Settings.DEBUG_MODE)
			Log.d(tag, message);
	}

	public static void e(String tag, String message) {
		if(Settings.DEBUG_MODE)
			Log.e(tag, message);
	}
}
