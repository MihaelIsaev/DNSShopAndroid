package com.mihaelisaev.dnsshop;

import android.app.Activity;
import android.content.res.Configuration;

public class CommonHelper {
	private Activity root;
	
	public CommonHelper(Activity root){
		this.root = root;
	}
	
	public int getDisplayLarge(){
		if ((root.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE)
			return Configuration.SCREENLAYOUT_SIZE_XLARGE;
		else if ((root.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE)
			return Configuration.SCREENLAYOUT_SIZE_LARGE;
		else if ((root.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL)
			return Configuration.SCREENLAYOUT_SIZE_NORMAL;
		else
			return Configuration.SCREENLAYOUT_SIZE_SMALL;
	}
}
