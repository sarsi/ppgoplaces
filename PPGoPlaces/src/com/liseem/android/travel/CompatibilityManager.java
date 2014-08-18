package com.liseem.android.travel;

import android.os.Build;

public class CompatibilityManager extends TravelLiteActivity {
	
	    public static final String KINDLE_FIRE_MODEL = "Kindle Fire";

	    /**
	     * Get the current Android API level.
	     */
	    public static int getSdkVersion() {
	        return android.os.Build.VERSION.SDK_INT;
	    }

	    /**
	     * Determine if the device is running API level 11 or higher.
	     */
	    public static boolean isHoneycomb() {
	        return getSdkVersion() >= Build.VERSION_CODES.HONEYCOMB;
	    }

	    /**
	     * Determine if the device is running API level 14 or higher.
	     */
	    public static boolean isIceCreamSandwich() {
	        return getSdkVersion() >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	    }

	    /**
	     * Determine if the current device is a first generation Kindle Fire.
	     * @return true if the device model is equal to "Kindle Fire", false if otherwise.
	     */
	    public static boolean isKindleFire() {
	        return Build.MODEL.equals(KINDLE_FIRE_MODEL);
	    }
	

}
