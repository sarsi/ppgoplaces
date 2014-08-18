/* **************************************************************************
 * Application: 	PPGoPlaces
 * Category: 		Travel/Lifestyle/Photo
 * 
 * Developer: 		Lawrence L.A. Tan
 * Date: 				May 3rd, 2012 (first documented)
 * 
 * Last Updated: 	September 13, 2012
 * Last Release:  	September 18, 2012 (version 1.1)
 * 
 * HISTORY
 * First Beta Release v1.0: August 22nd, 2012
 * 
 * Beta Releases 
 * Beta v1.1: 	August 25th, 2012			(build 138)
 * Beta v1.2: 	August 30th, 2012			(build 146)
 * Beta v0.5: 	September 4th, 2012		(build 148)	//--changed version to sub 1.0 
 * Beta v0.6 	September 6th, 2012		(build 149)	//--compatibility issues with added sharedUserId
 * Beta v0.7  	September 9th, 2012 		(build 151)
 * 
 * Release
 * Stable v1.0	September 13th, 2012	(build 153)
 * Stable v1.1	September 18th, 2012 	(build 155)
 * 
 * FUTURE
 * Project 2.0:		September 17, 2013 (started)
 * Last Build: 		October 15th, 2013
 * Last Build: 		159
 * 
 * This Updates:  October 16th, 2013
 * Current Build:  161
 * 
 * Build 156 Changes:
 * 		- Developer refresh of codes, housekeeping. Reorganize documentation.
 * 		- Fixed 
 * 			- ShoppingList, picture display crash and add AsyncTask for bitmap
 * 			- TravelLite, computeScaleFactor 
 * Build 157 Changes:
 * 		- Fragment implementation
 * 			- added FragMain as fragment Main Activity
 * 			- completed migrating to fragment PPGoPlaces, HolidayList, HolidayAdd
 * Build 158 Changes
 *      - Completed migrated all to fragments
 *      - Added ActionBar menu
 *      - Migrated to 4.x default theme
 *      - Cleaned out HolidayCheckList.java as reference boilerplate
 * Build 159 Changes
 *      - Migrated CheckList, 
 * Build 160 Changes
 * 		- Global Action Bar and local variation
 * 		- Fixed Settings, backup and restore
 * 		- Fixed Fx and Tip Dialog
 * Build 161 Changes			
 * 	    - Migrated date picker dialog to DateDialogFragment, DialogFragment DatePicker
 * 		- Clean up Holiday view, add and edit
 * 		- IMPORTANT changed line 509 getObject to return this.object
 * 
 * 	Target Next Release features: 
 *		Refocus shopping list and todo as main screen, and holiday/location as sub program
 *		Add location filtering to shopping and todo list
 *
 * This Application:
 * 		Main Application for PPGoPlaces as declared in AndroidManifest.xml. 
 * 		Maintain Global States, and instantiated when application is created.
 *
 *	=====APPLICATION FILES STRUCTURE======================
 * Main:
 * Application: 
 * 		PPGoPlacesApplication 	- db access and common menu
 * Activity:		
 * 		TravelLiteActivity 			- shared utilities, methods and device checks
 * 		PPGoPlaces 					- main menu
 * 		MyPreferences 				- settings and backup menu
 * 		TipAndFxDialog 				- setup for tips and fx
 * 
 * Programs:
 * ViewLocation 						- listview of locations, entry point for location and item CRUD
 *    	AddLocation 					- add record	
 *    	EditLocation 					- edit record
 *    	ReceiveLocation 				- intent filter for location, shared with edit
 *    	MapLocation 					- find map use by add and edit
 *    	MapDisplayLocation 		-  display map for view map function
 *    	AddPhoto 						- for adding photo from user gallery
 *    	TravelLiteDBAdapter 		- DBHelper and SQLite main
 *    
 *ViewHoliday 						-  listview of holiday, entry point for holiday and item CRUD
 *   	AddHoliday 						- add record
 *   	EditHoliday 						- edit record
 *   	MapHolidayLocations 		- display locations map for view map function 
 *   
 *HolidayCheckList 				- listview of checklist and main CRUD
 *  	AddTask 							- add and edit
 * 
 *ShoppingList 						- listview of shopping list and main CRUD
 *  	AddShopping 					- add and edit
 *  
 * Shared and support Programs:
 * 		PhotoAlbum 					- photos viewer used by holiday and location
 * 		SimpleHelp 						-  webview help screens
 * 		Eula 								- end user licence agreement screen
 * 		TripCalculator 					- simple calculator program 
 * 
 * Class support programs:
 * 		AddPhotoViewAdapter 	- view pager adapter for AddPhoto
 * 		HolidayListAdapter 		- listview adapter for ViewHoliday
 * 		LocationListAdapter 		- listview adapter for ViewLocation
 * 		TaskListAdapter 				- listview adapter for HolidayCheckList
 * 		ItemListAdapter 				- listview adapter for ShoppingList
 * 		MyBackupTask 				- backup agent for backup
 * 
 * Class object:
 * 		HLocation 						- location object
 * 		Holiday 							- holiday object
 * 		LPicture 							- picture object
 * 		Shopping 						- shopping object
 * 		TaskList 							- checklist object
 * 		MyDate 							- date object to resolve various format
 * 		MapOverlay 					- map overlay for MapLocation and MapDisplayLocation
 * 
 * Files embedded in program:
 * Default Checklist 				- /res/raw/herchecklist.txt and /raw/hischecklist.txt
 * HTML Help files 					- /assets
 * 
 * Files external to program:
 * Main database 					-  /data/data/com.liseem..travel/databases/myholiday.db
 * SharedPreferences 				- /data/data/com.liseem..travel/shared_prefs/ppPrefs.xml
 * Eula 										- /data/data/com.liseem..travel/shared_prefs/eula.xml
 * User default and backup 
 * 												- /data/data/com.liseem..travel/files/userchecklist.txt
 * 												- /data/data/com.liseem..travel/files/myholiday.db
 * Files action 							- /mnt/sdcard/cache/ppgoplaces
 * Factory default checklist 	- /data/data/com.liseem..travel/
 * 
 * =====ACTIVITY GENERAL FLOW===============================
 * 
 * 1.  Constants and variables declaration
 * 2.  Activity lifecycle methods and persist state
 * 3.  setupView() - main activity declaration, load data, shared preferences and views setup
 * 4.  addAdapter() - setup adapter
 * 5.  setListener() - setup view listeners call
 * 6.  listener methods - onClick, onChecked, etc. methods
 * 7.  helper methods - change date, image rotation, write to file, file to bitmap, etc.
 * 8.  menus and dialog methods - menu, context menu and dialogs
 * 9.  AsyncTask methods - group all async task methods here
 * 10. SandBox - where on trial methods stay here until stable for deployment.
 * 
 * =====PPGoPlaces Activity===================================
 */


/*
 * ==================== IMPORTANT !!!!! ======================
 * Pre-release activities:
 * 		-  Turn off developer mode, in this program:
 * 			private static final boolean 		DEVELOPER_MODE = false;		line 247 HERE
 * 			
 * 		- 	Update Manifest with next version info
 * 			android:versionCode="12"
 * 			android:versionName="1.2"
 * 
 * 		- 	res/layout/map_location.xml
 * 			android:apiKey="0riAp5Zuv0TkZdjlxCJFN2pLBGZlgsu5d2okf5g"
 * 				debug key: "0riAp5Zuv0TmIB7ooEPC6CSbVDCDEgFPy7rE6Zw"
 * 				release key: "0riAp5Zuv0TkZdjlxCJFN2pLBGZlgsu5d2okf5g"
 * 
 * 		-	FragMain
 * 			need to set to false
 * 			FragmentManager.enableDebugLogging(true);								line 245
 *==================== IMPORTANT !!!!! ======================= 
 */


package com.liseem.android.travel;


import static com.liseem.android.travel.TravelLiteActivity.*;					//--import static strings


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.liseem.android.travel.items.MyDate;
import com.liseem.android.travel.items.TaskList;
import com.liseem.android.travel.items.Holiday;
import com.liseem.android.travel.items.HLocation;
import com.liseem.android.travel.items.LPicture;
import com.liseem.android.travel.items.Shopping;
import com.liseem.android.travel.items.TravelLiteDBAdapter;
import com.liseem.android.travel.TravelLiteActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Application;
import android.app.FragmentManager;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.hardware.Camera;
import android.location.Address;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;

public class PPGoPlacesApplication extends Application implements Serializable {
	
	private static final String 			TAG="PPGoPlacesApplication";
	
	private final static String 			HOLIDAY="DATABASE_HOLIDAY_TABLE";
	private final static String 			LOCATION="DATABASE_LOCATION_TABLE";
	private final static String 			SHOPPING="DATABASE_SHOPPING_TABLE";
	
	private static final int 					READ_BLOCK_SIZE=100;
	private static final int 					IO_BUFFER_SIZE=4*1024;
	
	//*****MUST TURN OFF BEFORE RELEASE************************************
	private static final boolean 		DEVELOPER_MODE = true;
	//*************************************************************************
	
	private SQLiteDatabase 				db;
	private TravelLiteDBAdapter 		dbHelper;
	private SharedPreferences 			prefs;
	private SharedPreferences 			countryList;
	private SharedPreferences 			category;
	private SharedPreferences.Editor 	editor;
	private TravelLiteActivity 				activityHelper;
	public 	  BackupManager 				backupManager;					//--google backup manager cloud
	public   TelephonyManager 			tm;										//--telcos and roaming information
	//public   PackageManager				pm;										//--device hardware information - FIRSTRUN only
		
	private ArrayList<Holiday> 			holidayList;
	private ArrayList<HLocation>		locationList;
	private ArrayList<Shopping> 		shoppingList;
	private ArrayList<TaskList> 		checkList;
	private ArrayList<TaskList> 		selectedList;
	private ArrayList<LPicture> 			locPictureList;
	private ArrayList<LPicture> 			pictureList;

	private Context 								context;
	private Activity 								activeActivity;

	//private AlertDialog 						loadDefaultList;
	private String 								deviceName;

	//--share instance and variables------------------
	private boolean							hasCountryFile;					//--country sharedPreferences if available
	private boolean 							hasLocation;
	private Location 							currentLocation;
	private Address 								currentAddress;
	private TaskList								task;
	private Shopping							item;
	private HLocation							lastLocation;
	private Object								object;
	private MyDate								today;
	private String 								isoCode;
	private String 								countryCode;
	private String 								netOperator;
	private boolean							networkRoam;
	private int										lastItemSelected;			//--for viewlocation distancehere position

	
	
	//=====1.0 SYSTEM ACTIVITY======================================START====
	@Override
	public void onCreate() {
		super.onCreate();
		
		//--return the context of the global Application context, require for global or static data
		//--instead of just the lifetime of that activity.
		context					=this.getApplicationContext();   						
		
		//--0 (zero)=mode private, only this app can read these preferences
		prefs						=getSharedPreferences(PREFNAME, MODE_PRIVATE);
		category					=getSharedPreferences (CHECKLIST, MODE_PRIVATE);
		editor						=prefs.edit();														//--editor for editing share preferences
		
		//--load country preference if it exists---------------------------------------------
		hasCountryFile		=prefs.getBoolean("hasCountryFile", false); 
	 	if (hasCountryFile) {														
	 		countryList			=getSharedPreferences(COUNTRY, MODE_PRIVATE);
	 	}		
		
		//--setup db helper for db access----------------------------------------------------
		dbHelper					=new TravelLiteDBAdapter(this);
		dbHelper.open();
		
		//--initialize new arraylist object----------------------------------------------------
		holidayList				= new ArrayList<Holiday>();
		locationList				= new ArrayList<HLocation>();
		shoppingList			= new ArrayList<Shopping>();
		checkList					= new ArrayList<TaskList>();
		selectedList				= new ArrayList<TaskList>();
		locPictureList			= new ArrayList<LPicture>();
		pictureList				= new ArrayList<LPicture>();
		
 		//--initialize framework components---------------------------------
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        
        //--obsolete codes, call via getter methods
        //PackageManager pm = this.getPackageManager();
        
        //--build 155 change, remarked out with direct return to calling---------------
        //editor.putString("isoCode", isoCode=tm.getSimCountryIso().toString());			//mobile country code and mobile network code of SIM provider
        //editor.putString("countryCode", tm.getNetworkCountryIso().toString());			//current network country
        //editor.putString("netOperator", netOperator=tm.getNetworkOperatorName().toString()); 	//numeric name (MCC+MNC) of current registered operator
        //editor.putBoolean("networkRoam", networkRoam=tm.isNetworkRoaming()); 	//check is network roaming
        //editor.commit();
		
        //--initialize backup manager-----------------------------------------------------
		backupManager=new BackupManager(this);

		//--initialized date object with today date-------------------------------------
		today = new MyDate();
		today.setCalDate(Calendar.getInstance());
		setThisLogin();									//--update prefs with login information
		
		//--turn on strict mode if  developer mode is true--------------------------
		if (DEVELOPER_MODE) {
	         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
	                 .detectDiskReads()
	                 .detectDiskWrites()
	                 .detectNetwork()   					//--or .detectAll() for all detectable problems
	                 .penaltyLog()
	                 .build());
	         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
	                 .detectLeakedSqlLiteObjects()
	                 //.detectLeakedClosableObjects()
	                 .penaltyLog()
	                 .penaltyDeath()
	                 .build());
	     }		
		
		//--LogCat to get the telephony information
		Log.d(TAG, "346 Sim Country Code "+tm.getSimCountryIso().toString());
		Log.d(TAG, "347 Current Country Code "+tm.getNetworkCountryIso().toString());
		Log.d(TAG, "348 Network Roaming "+tm.isNetworkRoaming());
		//Log.d(TAG, "349 Network Data Connection "+TelephonyManager.DATA_CONNECTED);
		Log.d(TAG, "350 Phone Model "+android.os.Build.MODEL);
		Log.d(TAG, "351 Android OS "+android.os.Build.VERSION.SDK_INT);

		
	}	//--END onCreate()
		
	
	public void onResume() {
		dbHelper.open();								//--open database
	}	//--END onResume()
	
	
	public void onStop() {
		db.close();										//--ensure database is closed
	}		//--END onStop()
	
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		db.close();										//--ensure database is closed
	}	//--END onTerminate()
	
	//=====2.0 SHARE INSTANCE, SHARED PREFERENCES AND VARIABLES======================

	//--device hardware and telephony status-------------------------------------
	public boolean hasCamera() {
		//return prefs.getBoolean("hasCamera", false);
		return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}
	
	public boolean hasGPS() {
		//return prefs.getBoolean("hasGPS", false);
		return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
	}
	
	
	//--Phone status and general information------------------------------------
	// ********* NEED MORE WORKS ************************
	
	public boolean hasTelephony() {									//--return data or data roaming status
		int status=tm.getDataState();
		if (status==TelephonyManager.DATA_CONNECTED) {
			return true;
		} else {
			return false;
		}
	}
	
	public String simCountryCode() {									//--sim country code
		return tm.getSimCountryIso().toString();
	}
	
	public String countryCode() {										//--country code network, not reliable for CDMA
		//int networkType = tm.getPhoneType(); 					//--CDMA is 2 and GSM is 1
		return tm.getNetworkCountryIso().toString();
		//return prefs.getString("countryCode", "unknown");
	}
	
	public String operatorName() {										//--current network operator name
		return tm.getNetworkOperatorName().toString();
		//return prefs.getString("netOperator", "unknown");
	}
	
	public boolean networkRoam() {
		return tm.isNetworkRoaming();
		//return prefs.getBoolean("networkRoam", false);
	}
	
	
	//--check location information available------------------------------------
	//-- I think redundant since boolean hasCountryFile is checked at line 308
	/*public boolean hasCountryFile() {
		return hasCountryFile;												//--return boolean info stored in ln 308
		//return prefs.getBoolean("hasCountryFile", false);
	}*/

	public String simCountry() {											//--get subscriber country from sim country code
		String isoCode=this.simCountryCode();
		String simCountry="unknown";
		//boolean hasFile=hasCountryFile();
		if (hasCountryFile && isoCode !="unknown" ) {
			simCountry=countryList.getString(isoCode.toString(),"unknown" );
			//Log.d(TAG, "430 convert Sim Country code to country string "+simCountry.toString());
		}
		return simCountry;	
	}
	
	public String currentCountry() {									//--get current country from network code
		String currentCode=this.countryCode();
		String thisCountry="unkown";
		//boolean hasFile=hasCountryFile();
		//Log.d(TAG,"438 hasCountryFile() "+hasFile);
		if (hasCountryFile && currentCode !="unknown") {
			thisCountry=countryList.getString(currentCode.toString(), "unknown");
			//Log.d(TAG, "442 convert country code to country string "+thisCountry.toString());
		}
		return thisCountry;
	}
	
	
	//--Device and Manufacturer information-----------------------------------------
	public String getDeviceName() {
		return android.os.Build.MANUFACTURER;
		//return prefs.getString(deviceName, "unknown");
	}
	
	public int getOSBuild() {
		return android.os.Build.VERSION.SDK_INT;
	}
	
	public String getPhoneMode() {
		return android.os.Build.MODEL;
	}
	
	public int getVersionCode() {
		PackageInfo pInfo = null;
		int version=0;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		version= pInfo.versionCode;
		return version;
	}
	
	public String getVersionName() {
		PackageInfo pInfo = null;
		String versionName="v1.0";
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		versionName = pInfo.versionName;
		Log.d(TAG, "504 try to retrieve package information from getPackageManager");
		Log.d(TAG, "505 package information "+pInfo.toString());
		return versionName;
	}

	//=====3.0 THIS LOGIN INFORMATION=======================================

	//--setup login and last login information in prefs called from onCreate()-------------------
	public void setThisLogin() {
		String this_login=prefs.getString("this_login", "");
		String last_login=prefs.getString("last_login", "");
		if(this_login.isEmpty()) {
			editor.putString("this_login", today.getStrDate().toString());
			editor.putString("last_login", this_login.toString());
			editor.putInt("days_ago", 0);
			editor.commit();
		} else {
			//--compute last login days-------------------
			MyDate lastDate=new MyDate();
			lastDate.setStrDate(this_login);		//this_login is the last login until overwritten by shared preferences below
			int daysBeforeLast=today.daysBetween(lastDate.getCalDate());				
			
			editor.putString("before_last_login",last_login.toString());
			editor.putString("last_login", this_login.toString());
			editor.putString("this_login", today.getStrDate().toString());
			editor.putInt("days_ago", daysBeforeLast);
			editor.commit();			
		}
	}

	public String lastLogin() {
		return prefs.getString("last_login", null);
	}
	
	public int lastLoginDays() {
		return prefs.getInt("days_ago", 0);
	}

	

	//=====4.0 GETTERS AND SETTERS PROGRAM OBJECTS==============================

	//--return display width from firstRunned, ln 320
	public int displayW() {
		int dispW = prefs.getInt("dispW", 480);
		return dispW;
	}

	//--return display height from firstRunned, ln 321
	public int displayH() {
		int dispH = prefs.getInt("dispH", 800);
		return dispH;
	}

	
	public boolean hasLocation () {
		return hasLocation;
	}
	
	//--update boolean that location info is available------------------------
	public void setLocationState(boolean state) {
		hasLocation=state;
	}
	
	//--setter and getter for location information-------------------------------
	public Object getObject() {
		//object = new Object();
		return this.object;
	}
	
	public void setObject(Object object) {
		this.object = object;
	}
	
	public Location getLocation() {
		return currentLocation;
	}
	
	public void setLocation(Location location) {
		currentLocation=location;
	}
	
	public int getLastPosition() {
		return lastItemSelected;
	}
	
	public void setLastPosition(int position) {
		lastItemSelected=position;
	}
	
	public Location getCurrentLocation() {
		return currentLocation;
	}
	
	public void setCurrentLocation(Location location) {
		currentLocation=location;
	}
	
	public void setLastTask (TaskList lastTask) {
		this.task=lastTask;
	}
	
	public TaskList getLastTask () {
		return task;
	}
	
	public void setLastHLocation (HLocation hLocation) {
		this.lastLocation = hLocation;
	}
	
	public HLocation getLastHLocation() {
		return lastLocation;
	}
	
	public void setLastItem (Shopping shopItem) {
		this.item=shopItem;
	}
	
	public Shopping getLastItem () {
		return item;
	}
	
	//--setter and getter for address----------------------------------------------
	public Address getAddress() {
		return currentAddress;
	}
	
	public void setAddress (Address address) {
		currentAddress=address;
	}
	
	//--default location----------------------------------
	//--1.317385,103.807451, 
	public double getDefaultLat() {
		double latitude=prefs.getFloat("defLat",(float)1.311054);
		return latitude;
	}

	//--
	//--used by MapLocation, 320 if Lat and Long not available
	public double getDefaultLong() {
		double longitude=prefs.getFloat("defLong",(float)103.794989);
		return longitude;
	}

	//--
	//--used by MapLocation, 320 if Lat and Long not available
	public long getDefaultLocation() {
		long locRowId=prefs.getLong("defaultLocation",0);
		return locRowId;
	}
	
	//--
	//--used by LocationAdd, 1004 updateStatus()
	public void setDefaultLat(double latitude) {
		editor.putFloat("defLat", (float)latitude);
		editor.commit();
	}
	//--
	//--used by LocationAdd, 1004 updateStatus()	
	public void setDefaultLong(double longitude) {
		editor.putFloat("defLat", (float)longitude);
		editor.commit();		
	}
	
	public void setDefaultLocation(long locRowId) {
		editor.putLong("defaultLocation", locRowId);
		editor.commit();		
	}
	
	public void setDefaultLocationObject (HLocation location) {
		if (location.getLatitude() >0)
			editor.putFloat("defLat", (float)location.getLatitude());
		if(location.getLongitude() >0)
			editor.putFloat("defLong", (float)location.getLongitude());
		editor.putLong("defaultLocation", location.getId());
		editor.commit();
	}
		
	public void setGeoFence(long distance) {
		editor.putLong("geoFence", distance);
		editor.commit();
	}
	
	public long getGeoFence() {
		return prefs.getLong("geoFence", 5000);
	}
	
	public String getSignature() {
		String emailSignature=prefs.getString("emailSignature", "sent from \"PPGoPlaces\"");
		return emailSignature;
	}

	//=====5.0 APPLICATION FIRST RUN ROUTINE, RUN ONCE============================
	
	//=================FIRST RUN, RUN ONCE==============================
	
		public boolean getFirstRun() {
			return prefs.getBoolean("firstRun", true);		//--sample is default true, don't understand
		}
		
		//--firstRunned() in FragMain Activity due to computeCameraScaleFactor method call to TravelLiteActivity

		//--first run routine----------------------------------------------------------------
		public void setRunned() {
			//Log.d(TAG, "1406 landed in setRunned() Application ");
		
			//--Package Manager provide basic device hardware information----------
			//PackageManager pm = this.getPackageManager();
	    	//Log.d(TAG, "passed packageManager");
	
	        //--import country xml into shared preference if not exists---------------------------
	        File prefDir=new File("/data/data/com.liseem.android.travel/shared_prefs/");
	        //File prefDir=new File(context.getFilesDir().getPath()+"/shared_prefs/"); //not the same, ../files/shared_prefs??
	        Log.d(TAG,"1413 try Environment.getFilesDir in setRunned "+prefDir.toString());
	        File countryFile=new File(prefDir, "country.xml");
	        if (!countryFile.exists()) {
				try {
					boolean hasCountryFile=new copyCountryFile().execute().get();
					//Log.d(TAG,"1418 Done copying country file ");
					editor.putBoolean("hasCountryFile", hasCountryFile);
					editor.putString("fxRate", "1.0");
					editor.putString("tipPercent", "0.1");
					editor.putString("amtPlusTip", "1.1");
					editor.commit();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
	        } 
		}
		
		public void firstRunPreferences() {
			Context myContext = this.getApplicationContext();
			prefs=myContext.getSharedPreferences(PREFNAME,0);	//0=mode private only this app can read these preferences
			
		}
		
		//=================FIRST RUN, RUN ONCE==============================
		
	
	//=====SANDBOX===================================================
	
	//--return true if the intent action such as camera or gps is available
	//--
	public static boolean isIntentAvailable(Context context, String action) {
	    final PackageManager packageManager = context.getPackageManager();
	    final Intent intent = new Intent(action);
	    List<ResolveInfo> list =
	            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
	    return list.size() > 0;
	}
	
	//=====SANDBOX===================================================

	
	//=====6.0 DATA HELPERS====================================START======

	//--load checklist category for spinner--------------------------------------------------------------------------------------
	protected ArrayList<String> loadCheckListChoice() {
		
		//--load checklistitems from sharedPreferences----------------
		ArrayList<String> catList = new ArrayList<String>();
		String cat1=category.getString("TaskCat1",TASK_MENU_1);
		catList.add(cat1.toString());		
		String cat2=category.getString("TaskCat2",TASK_MENU_2);
		catList.add(cat2.toString());
		String cat3=category.getString("TaskCat3",TASK_MENU_3);
		catList.add(cat3.toString());
		String cat4=category.getString("TaskCat4",TASK_MENU_4);
		catList.add(cat4.toString());
		catList.add("Show All");										
		String cat8=category.getString("TaskCat8",TASK_MENU_8);
		catList.add(cat8.toString());			
		
		return catList;
		//--String array used by spinner data adapter-------------------
		//checkListItems=catList.toArray(new String[catList.size()]);
	}
	
	//--load checklist category for add and edit mode spinner----------
	protected ArrayList<String> loadCheckListCat() {
		
		//--load checklistitems from sharedPreferences----------------
		ArrayList<String> catList = new ArrayList<String>();
		String cat1=category.getString("TaskCat1",TASK_MENU_1);
		catList.add(cat1.toString());		
		String cat2=category.getString("TaskCat2",TASK_MENU_2);
		catList.add(cat2.toString());
		String cat3=category.getString("TaskCat3",TASK_MENU_3);
		catList.add(cat3.toString());
		String cat4=category.getString("TaskCat4",TASK_MENU_4);
		catList.add(cat4.toString());			
		String cat8=category.getString("TaskCat8",TASK_MENU_8);
		catList.add(cat8.toString());			
		
		return catList;
		//--String array used by spinner data adapter-------------------
		//checkListItems=catList.toArray(new String[catList.size()]);
	}

	//--load shopping list category for add and edit mode spinner----------
	//--call from Shopping programs, retrieve from shared preferences "checklist"
	protected ArrayList<String> loadShopListCat() {
		
		//--load shopListCats from sharedPreferences----------------
		ArrayList<String> catList = new ArrayList<String>();
		String cat1=category.getString("ShopCat1", SHOP_CAT_1);
		String cat2=category.getString("ShopCat2", SHOP_CAT_2);
		String cat3=category.getString("ShopCat3", SHOP_CAT_3);
		String cat4=category.getString("ShopCat4", SHOP_CAT_4);
		String cat5=category.getString("ShopCat5", SHOP_CAT_5);
		String cat6=category.getString("ShopCat6", SHOP_CAT_6);
		String cat7=category.getString("ShopCat7", SHOP_CAT_7);
		String cat8=category.getString("ShopCat8", SHOP_CAT_8);
		catList.add(cat1.toString());
		catList.add(cat2.toString());
		catList.add(cat3.toString());
		catList.add(cat4.toString());
		catList.add(cat5.toString());
		catList.add(cat6.toString());
		catList.add(cat7.toString());
		catList.add(cat8.toString());
		
		return catList;
	}
	
	
	
	//=====6.0 DATA HELPERS========================================END======

	//=====7.0 DB QUERY METHODS===================================START======
	
	protected void setActiveActivity(Activity activity) {
		activeActivity=activity;
	}
	
	protected Activity getActiveActivity() {
		return activeActivity;
	}
	
	
	//=====DATABASE LOADLIST===============================================
	
	//====HOLIDAY CRUB=========================================
	protected ArrayList<Holiday> loadHolidays() {				//--load all holidays
		holidayList=new ArrayList<Holiday>();
		holidayList=dbHelper.getAllHolidays();
		return holidayList;
	}
	
	public long addHoliday(Holiday addHol) {						//--insert return rowid
		long rowIdAdded;
		rowIdAdded=dbHelper.insertHoliday(addHol);
		if (rowIdAdded>0) {
			return rowIdAdded;
		} else {
			return 0;
		}
	}

	public Holiday getHoliday(long rowid) {							//--get return object
		Holiday newHoliday=new Holiday();
		newHoliday=dbHelper.getHoliday(rowid);
		return newHoliday;
	}
	
	public boolean isHolidayDBEmpty() {
		return dbHelper.isHolidayDBEmpty();
	}
	
	public boolean updateHoliday(Holiday updateHol) {		//--update return boolean
		boolean yesUpdated;
		yesUpdated=dbHelper.updateHoliday(updateHol);
		return yesUpdated;
	}	
	
	public boolean deleteHoliday(long rowid) {					//--delete return boolean
		boolean yesDeleted;
		yesDeleted=dbHelper.deleteHoliday(rowid);
		return yesDeleted;
	}
	
	public boolean deleteHoliday(Holiday holiday) {			//--delete return boolean
		boolean yesDeleted;
		yesDeleted=dbHelper.deleteHoliday(holiday.getId());
		return yesDeleted;
	}
	
	
	
	//====LOCATION CRUB=========================================

	//--retrieve all locations
 	public ArrayList<HLocation> getAllLocations() {			
		locationList=new ArrayList<HLocation>();
		locationList=dbHelper.getAllLocations();
		return locationList;
	}
	
	//--retrieve list of locations by holiday id with filter
	protected ArrayList<HLocation> loadLocations(long holrowid, boolean filterLocation) {			
		locationList=new ArrayList<HLocation>();
		if (holrowid==0 && !filterLocation) {
			locationList=dbHelper.getAllLocations();
		}else{
			locationList=dbHelper.getHolidayLocations(holrowid);
		}
		return locationList;
	}
	
 	//--retrieve list of locations by holiday id
 	public ArrayList<HLocation> getHolidayLocations(long holRefId) {			
 		locationList=new ArrayList<HLocation>();
 		locationList=dbHelper.getHolidayLocations(holRefId);
 		return locationList;
 	}
 	
 	//--retrieve list of locations not tagged to any holiday, i.e. holiday id = 0
	protected ArrayList<HLocation> getHomeLocations() {
		locationList=new ArrayList<HLocation>();
		locationList=dbHelper.getHolidayLocations(0);
		return locationList;
	}
	
	//--insert new location return rowid information
 	public long insertHLocation(HLocation addLoc) {					
		long rowIdAdded;
		rowIdAdded=dbHelper.insertLocation(addLoc);
		if (rowIdAdded>0) {
			return rowIdAdded;
		} else {
			return 0;
		}
	}
 	
 	//--insert new location return boolean status
 	public boolean addHLocation(HLocation addLoc) {					
 		boolean insertLoc=false;
 		insertLoc=dbHelper.addLocation(addLoc);
 		return insertLoc;
 	}

	//--get location object
	public HLocation getHLocation(long getLoc) {						
		HLocation locHelp=new HLocation();
		locHelp=dbHelper.getLocation(getLoc);
		return locHelp;
	}
	
	//--get last location information entered
	public HLocation getLastLocation() {
		HLocation locHelp=new HLocation();
		locHelp=dbHelper.getLastLocation();
		return locHelp;
	}
	
	//--check if the location database is empty
	public boolean isLocationDBEmpty() {
		boolean isEmpty=dbHelper.isLocationDBEmpty();
		return isEmpty;
	}

	//--update location with picture status
	public boolean updateLocationHasPicture(long rowId, boolean hasPicture) {
		return dbHelper.updateLocationHasPicture(rowId, hasPicture);
	}
	
	//--update location with holiday tagging status
	public boolean updateLocationHasHoliday(long rowId, boolean hasHoliday) {
		return dbHelper.updateLocationHasHoliday(rowId, hasHoliday);
	}
	
	//--update location information change
	public boolean updateHLocation(HLocation locationChange) {	 
		boolean yesUpdated;
		yesUpdated=dbHelper.updateLocation(locationChange);
		return yesUpdated;
	}
	
	//--update list of locations
	public void updateLocationArrayList(ArrayList<HLocation> tempHelp) {
		for (HLocation h : tempHelp) {
			this.updateHLocation(h);
		}
	}
	
	//--delete location by rowid and return boolean status
	public boolean deleteLocation(long rowid) {					
		boolean yesDeleted;
		yesDeleted=dbHelper.deleteLocation(rowid);
		return yesDeleted;
	}
	
	//--delete location object and return boolean status
	public boolean deleteLocation(HLocation locName) {					
		boolean yesDeleted;
		yesDeleted=dbHelper.deleteLocation(locName.getId());
		return yesDeleted;
	}
	
	//====LOCATION CRUB=============================END==
	
	
	//====SHOPPING CRUB==================================
	
	protected ArrayList<Shopping> getAllItems() {					//--load all shoppings
		shoppingList=new ArrayList<Shopping>();
		shoppingList=dbHelper.getAllItems();
		return shoppingList;
	}
	
	protected ArrayList<Shopping> getByRefId(long refid) {	//--get shopping list by holiday
		shoppingList=new ArrayList<Shopping>();
		shoppingList=dbHelper.getByRefId(refid);
		return shoppingList;	}
	

	public long addItem(Shopping addShop) {						 	//--insert return rowid
		long rowIdAdded;
		rowIdAdded=dbHelper.insertItem(addShop);
		if (rowIdAdded>0) {
			return rowIdAdded;
		} else {
			return 0;
		}
	}
		
	public Shopping getItem(long rowid) {									//--get return object
		Shopping newItem=new Shopping();
		newItem=dbHelper.getItem(rowid);
		return newItem;
	}

	public ArrayList<Shopping> getShoppings() {
		shoppingList=new ArrayList<Shopping>();
		shoppingList=dbHelper.getAllItems();
		return shoppingList;
	}
	
	public boolean updateItem(Shopping bbItem) {					//--update return boolean
		boolean updateItem;
		updateItem=dbHelper.updateItem(bbItem);
		return updateItem;
	}
	
	
	public boolean deleteItem(long rowid) {							//--delete return boolean
		boolean yesDeleted;
		yesDeleted=dbHelper.deleteItem(rowid);
		return yesDeleted;
	}
	
	public boolean deleteItem(Shopping item) {						//--delete return boolean
		boolean yesDeleted;
		yesDeleted=dbHelper.deleteItem(item.getId());
		return yesDeleted;
	}
		
	public void removeItems(ArrayList<Long> itemsList) {		//--delete list of item no returns
		dbHelper.removeItems(itemsList);
	}
	
	
	//====CHECKLIST CRUB==================================
	
	protected ArrayList<TaskList> loadAllTasks() {					//--load all tasks
		checkList=new ArrayList<TaskList>();
		checkList=dbHelper.getAllTasks();
		return checkList;
	}
	
	protected ArrayList<TaskList> loadSelectedTasks(long refid) {		//--load tasks by category id
		selectedList=new ArrayList<TaskList>();
		selectedList=dbHelper.getSelectedTasks(refid);
		return selectedList;
	}
	
	
	public long addTask(TaskList addTask) {							//--insert return rowid
		long rowIdAdded;
		rowIdAdded=dbHelper.insertTask(addTask);
		if (rowIdAdded>0) {
			return rowIdAdded;
		} else {
			return 0;
		}
	}
	
	public ArrayList<Integer> addTaskList(ArrayList<TaskList> newTaskList) {		//--add list of tasks
		ArrayList<Integer> insertedList = new ArrayList<Integer>();
		return insertedList=dbHelper.insertTaskList(newTaskList);
	}
	
	public TaskList getTask(long rowid) {									//--get return object
		TaskList newTask=new TaskList();
		newTask=dbHelper.getTask(rowid);
		return newTask;
	}
	
	public boolean isTaskDBEmpty() {										//--check is checklist empty
		boolean isEmpty=dbHelper.isTaskDBEmpty();
		return isEmpty;
	}
	
	public ArrayList<TaskList>getAllTasks() {
		checkList=new ArrayList<TaskList>();
		checkList=dbHelper.getAllTasks();
		return checkList;
	}
	
	public void updateTask(TaskList task) {								//--update return boolean
		dbHelper.updateTask(task);
	}
	
	public boolean deleteTask(long rowid) {							//--delete return boolean
		boolean yesDeleted;
		yesDeleted=dbHelper.deleteTask(rowid);
		return yesDeleted;
	}
	
	public boolean deleteTask(TaskList task) {						//--delete return boolean
		boolean yesDeleted;
		yesDeleted=dbHelper.deleteTask(task.getId());
		return yesDeleted;
	}
	
	public void removeSelectedTasks(ArrayList<Integer> selection) {		//--delete list of item no returns
		dbHelper.removeSelectedTasks(selection);
	}
	
	public boolean removeCompletedTasks() {
		boolean removeCompleted = dbHelper.removeCompletedTasks();	//--delete all completed tasks
		return removeCompleted;
	}
	
	public boolean removeAllTasks() {
		boolean removeAll= dbHelper.removeAllTasks();
		return removeAll;
	}
	//====PICTURES CRUB================================
	
	protected ArrayList<LPicture> loadAllPictures() {				//--load all pictures
		pictureList=new ArrayList<LPicture>();
		pictureList=dbHelper.getAllPictures();
		return pictureList;
	}
	

	public long insertPicture(LPicture pic) {								//--insert return rowid
		long rowIdAdded;
		rowIdAdded=dbHelper.insertPicture(pic);
		if (rowIdAdded>0) {
			return rowIdAdded;
		} else {
			return 0;
		}
	}
	
	public void addPictureList(ArrayList<LPicture> pics) {		//--insert batch of pictures
		dbHelper.addPictureList(pics);
	}

	public LPicture getPicture(long rowid) {								//--get return a picture
		LPicture getPic=new LPicture();
		getPic=dbHelper.getPicture(rowid);
		return getPic;
	}
	
	public ArrayList<LPicture> getLocationPictures(long locid) {		//--get list of pictures by location
		ArrayList<LPicture> getPics=new ArrayList<LPicture>();
		getPics=dbHelper.getLocationPictures(locid);
		return getPics;
	}
	
	public boolean queryPictureAvailable(long locId) {
		return dbHelper.queryPictureAvailable(locId);
	}
	
	public String queryPictureExists(String picpath) {
		String picPath=picpath;
		String gotPic;
		gotPic=dbHelper.queryPictureExists(picPath);
		return gotPic;
	}
	
	//--NOT USE----------------------------------------------------
	public boolean checkHasPicture(long dbRowId) {
		Cursor cursor;
		cursor=dbHelper.checkHasPicture(dbRowId);
		if (cursor !=null) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean updatePicture(LPicture pic) {						//--update return boolean
		boolean yesUpdated;
		yesUpdated=dbHelper.updatePicture(pic);
		return yesUpdated;
	}	
	
	public void updatePictures(ArrayList<LPicture> pics) {		//--Batch delete
		dbHelper.updatePictures(pics);
	}	
	
	public boolean deletePicture(long picid) {							//--delete return boolean
		return dbHelper.deletePicture(picid);
	}
	
	public void deletePictureList(ArrayList<Long> selection) {		//--delete list of row ids
		dbHelper.deletePictureList(selection);
	}
	
	public void deletePictures(ArrayList<LPicture> selection) {		//--delete list of pictures
		dbHelper.deletePictures(selection);
	}
	
	public LPicture pictureSetLocation(HLocation locLoad) {
		HLocation location=new HLocation();
		LPicture oneShot=new LPicture();
		MyDate myDate=new MyDate();
		
		location=locLoad;
		oneShot.setRefid(location.getId());									//--set location ref id
				
		Calendar pDate=Calendar.getInstance();
		myDate.setCalDate(pDate);
		oneShot.setDate(myDate.getStrDate().toString());			//--set date
		
		if (location.hasAddress())
			oneShot.setAddress(location.getAddress());					//--set address
		
		if(location.hasLocation()) {
			oneShot.setLatitude(location.getLatitude());					//--set geo info
			oneShot.setLongitude(location.getLongitude()); }
		
		return oneShot;
		
	}
	//=====7.0 DB QUERY METHODS====================================END======

	//=====8.0 ACTIVITIES HELPERS ====================================START=====

	//--SIMILAR to TravelLiteActivity for use with MapActivity activities that does not extend TravelLiteActivity
	
	//--Convert Address object to String, and return null if all field is empty--
	//--use to return address to address string for location add/edit address field---------
	protected String add2String(Address tempAdd) {
		boolean gotCountry=false;
		boolean gotPostal=false;
		StringBuilder useAddress=new StringBuilder();
		if (tempAdd.getFeatureName() !=null && tempAdd.getSubThoroughfare() !=null)	{		//--e.g. Villa Rosa
			if (!tempAdd.getFeatureName().toString().
					equalsIgnoreCase(tempAdd.getSubThoroughfare().toString())) {
				useAddress.append(tempAdd.getFeatureName().toString()).append("\n");	
			}
		}

		if (tempAdd.getSubThoroughfare() !=null)	//--e.g. 127
			useAddress.append(tempAdd.getSubThoroughfare().toString()).append(" ");		
		if (tempAdd.getThoroughfare() !=null)			//--e.g. Via Cristoforo Colombo
			useAddress.append(tempAdd.getThoroughfare().toString()).append("\n");		
		if (tempAdd.getLocality() !=null)					//--e.g. Positano
			useAddress.append(tempAdd.getLocality().toString()).append("\n");
		if (tempAdd.getCountryName() != null) {
			useAddress.append(tempAdd.getCountryName().toString()).append(" ");
			gotCountry=true; }
		if (tempAdd.getPostalCode() !=null)	{			//--e.g. 84017
			useAddress.append(tempAdd.getPostalCode());	
			gotPostal=true; }
		//if (gotCountry && gotPostal) {
		if(useAddress !=null) {
			return useAddress.toString();
		} else {
			return null;
		}
	}



	//=====8.0 ACTIVITIES HELPERS ====================================END======

	//--check for network connection-----------------------------------------------------
	//--user: LocationAdd
	protected static boolean hasNetwork(Context context) {
	    ConnectivityManager connectivityManager = (ConnectivityManager)
	        context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = null;
	    if (connectivityManager != null) {
	        networkInfo =
	            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    }
	    
		//--set WIFI as prefer data connection if is available----------
		if (networkInfo.isConnected()) {
			connectivityManager.setNetworkPreference(ConnectivityManager.TYPE_WIFI); }

	    return networkInfo == null ? false : networkInfo.isConnected();
	}
	
	//--check for mobile connection-----------------------------------------------------
	//--user: LocationAdd
	protected static boolean hasMobile(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager)
				context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = null;
		if (connectivityManager != null) {
			networkInfo =
					connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		}
		return networkInfo == null ? false : networkInfo.isConnected();
	}
	
	//--check for totally no network connection----------------------------------------
	//--user: LocationAdd
	protected static boolean hasCoverage(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager)
				context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = null;
		Boolean hasCoverage=false;
		if (connectivityManager != null) {
			networkInfo =
					connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			boolean mobile=networkInfo.isConnected();
			networkInfo =
					connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			boolean wifi=networkInfo.isConnectedOrConnecting();
			if (mobile || wifi) hasCoverage=true;		//either available will be true
		}
		//return networkInfo == null ? false : networkInfo.isConnected();
		return networkInfo == null ? false : hasCoverage;
		
	}
	
	/*protected boolean noConnectivity() {
	  	Boolean noNetwork;
		noNetwork=intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
		return noNetwork;
	}*/
		
	
	//--check for data network connection-----------------------------------------------
	//--user: LocationAdd
	protected boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    return cm.getActiveNetworkInfo() != null && 
	    	       cm.getActiveNetworkInfo().isConnected();

	}
	

	//=====10.0 SANDBOX=========================================START======
	

	//==========10.1 SERIALIZATION AND DESERIALIZATION OF OBJECT==================
	//--Serialization method from object to byte for sqlite blob--
	public static byte[] serializableObject(Object obj) {
		ByteArrayOutputStream baos= new ByteArrayOutputStream();
		
		try {
			ObjectOutput out=new ObjectOutputStream(baos);
			out.writeObject(obj);
			out.close();
			
			//Get the bytes of the serialized object
			byte[] buf=baos.toByteArray();
			return buf;
		} catch (IOException e) {
		}
		return null;
	}
	
	//--Deserialization method from byte back to object from sqlit blob--
	public static Object deserializeObject(byte[] bye) {
		try {
			ObjectInputStream ois=new ObjectInputStream(new ByteArrayInputStream(bye));
			Object object=ois.readObject();
			return object;
			
		} catch (ClassNotFoundException cnf) {
		} catch (IOException e) {
		}
		return null;
	}


	
	
	//=====10.0 SANDBOX=================================================END=======

	//--copy country file lookup table from raw to shared prefs
	private class copyCountryFile extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			//File countryList=new File(getResources().getResourceName(R.raw.country));
			InputStream raw=getResources().openRawResource(R.raw.country);
			InputStream is=new BufferedInputStream(raw);
			
			//--error for using hardcode for external directory, sugguest to use Context.getFilesDir().getPath() 
			//--or Environment.getExternalStorageDirectory().getPath()
			File prefDir=new File("/data/data/com.liseem.android.travel/shared_prefs/");
			//File prefDir=new File(context.getFilesDir().getPath()+"/shared_prefs/");			//--Sep 20, 2013
			if (!prefDir.exists()) {
				prefDir.mkdirs();
			}
			
			File countryFile=new File(prefDir, "country.xml");
			FileOutputStream fout;
			try {
				fout = new FileOutputStream(countryFile);
				OutputStream os=new BufferedOutputStream(fout);
			
				byte[] buffer=new byte[1024];
				int length;
		
				while ((length=is.read(buffer)) > 0) {
						os.write(buffer, 0, length);
				}
				is.close();
				os.close();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				//showOkAlertDialog("Country file copy not successful");
				return false;
			}
		}
	}
	
	//============================================================
	
	//=====UNUSED CODES========================================
	//--SANDY------------------------COMMON CREATE MENU----------------
	/*public void CreateMenu(Menu menu) {
		MenuItem mnu1=menu.add(0,0,0, "Location List");
			{   mnu1.setIcon(android.R.drawable.ic_dialog_map);  }
		
		MenuItem mnu2=menu.add(0,1,1, "Holiday List");
			{   mnu2.setIcon(android.R.drawable.ic_menu_camera); }
		
		MenuItem mnu3=menu.add(0,2,2, "Shopping List");
			{	mnu3.setIcon(android.R.drawable.ic_menu_agenda);  }
			
		MenuItem mnu4=menu.add(0,3,3, "Holiday Checklist");
			{   mnu4.setIcon(android.R.drawable.ic_menu_sort_by_size); }
	} 
	
	//@TargetApi(11)
	public void CreateActionBarMenu(Menu menu) {
		MenuItem mnu1=menu.add(0,0,0, "Location List");
		{   mnu1.setIcon(android.R.drawable.ic_dialog_map);  
		mnu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);}
		
		MenuItem mnu2=menu.add(0,1,1, "Holiday List");
		{   mnu2.setIcon(android.R.drawable.ic_menu_camera); 
		mnu2.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);}
		
		MenuItem mnu3=menu.add(0,2,2, "Shopping List");
		{	mnu3.setIcon(android.R.drawable.ic_menu_agenda);  
		mnu3.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);}
		
		MenuItem mnu4=menu.add(0,3,3, "Holiday Checklist");
		{   mnu4.setIcon(android.R.drawable.ic_menu_sort_by_size); 
		mnu4.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);}
	} 
	
	public void CheckListCreateMenu(Menu menu) {
		MenuItem mnu1=menu.add(0,0,0, R.string.my_holiday_checklist);
			{   mnu1.setIcon(android.R.drawable.ic_menu_agenda);  }
		
		MenuItem mnu2=menu.add(0,1,1, R.string.shopping_list_for_holiday);
			{   mnu2.setIcon(android.R.drawable.ic_menu_compass); }
		
		MenuItem mnu3=menu.add(0,2,2, R.string.settings);
			{	mnu3.setIcon(android.R.drawable.ic_menu_manage);  }
			
		MenuItem mnu4=menu.add(0,3,3, R.string.not_use);
			{   mnu4.setIcon(android.R.drawable.ic_menu_help); }
	} */
	
}		//--END MAIN