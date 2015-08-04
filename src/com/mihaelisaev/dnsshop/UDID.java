package com.mihaelisaev.dnsshop;



import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class UDID {
	
	private static final String DEFAULT_MAC = "0";
	
	public static String getUDIDInSha1(Context context){
		String mac = getMac(context);
		String androidId = getAndroidId(context);
		String udid = mac+androidId;
		String encodedUdid = Sha1.encode(udid);
		return encodedUdid;
	}

	private static String getMac(Context context) {
		try {
			WifiManager wifiMan = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInf = wifiMan.getConnectionInfo();
			return wifiInf.getMacAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return DEFAULT_MAC;
	}
	
	private static String getAndroidId(Context context) {
        String id = android.provider.Settings.System.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        return id;
    }
}
