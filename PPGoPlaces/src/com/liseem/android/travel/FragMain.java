/* 
 * Program: 			FragMain
 * Description:     This menu replaced PPGoPlaces as main activity. Currently, the ONLY activity mainly managing 
 *   						swapping of various "activities" as fragments.
 * 							Key version 2.0 Fragment implementation and moving app to ver 4.X and above
 * 							Learning Android Programming, my first homework
 *                         Lifestyle application for managing holiday todo list, packing list, shopping list  and candid photos, 
 *                         holiday locations and albums.
 * Summary: 		First Screen, main program
 * 
 * Created:			September 23, 2013
 * Last Release:  	September 18, 2012 (version 1.1)
 * Last Updated: 	September 25, 2012
 * This Updates: 	September 26, 2013
 * 
 * Notes: Refer to PPGoPlacesApplication for release schedule and updates.
 * 
 * Changes since last official release:
 * 	- Create main Activity for fragment implementation, i.e. all other activities moving to fragments
 * - Method for detecting screen size and orientation for single or dual fragments activity (not probably coded yet)
 * - Fixed back key to exit dialog with exception class error if back key from other fragement non-PPGoPlaces
 * - Create global setting and help action bar menu
 * - eula.show fixed
 * 
 * 
 * Outstanding fixes
 * 
 * Version 2.0 Outstanding Coding required:
 * - methods to handle screen size, resolution and orientation changes, including screen design.
 * - optimize code for resources utilization beyond multi-threading.
 * - 
 *   
 * 
 */

package com.liseem.android.travel;

//import static android.content.Context.MODE_PRIVATE;
//import static com.liseem.android.travel.TravelLiteActivity.PREFNAME;

//import java.io.IOException;

import com.liseem.android.travel.items.HLocation;
//import com.liseem.android.travel.items.MyDate;
//import com.liseem.android.travel.items.Shopping;
import com.liseem.android.travel.TravelLiteActivity;

//import android.app.ActionBar;
import android.app.AlertDialog;
//import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Matrix;
import android.location.Address;
//import android.media.ExifInterface;
//import android.os.AsyncTask;
import android.os.Bundle;
//import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.Toast;

public class FragMain extends TravelLiteActivity 
									implements 		PPGoPlaces.OnMenuSelectedListener, 
																LocationView.OnMenuSelectedListener,
																HolidayView.OnMenuSelectedListener, 
																HolidayCheckList.OnMenuSelectedListener, 
																ShoppingList.OnMenuSelectedListener
																{
	
	private static final String 			TAG ="FragMain";							//for debugging use
	public static final String 				INTENT_NAME="intent_name";		//bundle kinda args to new fragment
	
	//--fragment arguments bundle----------------------------------------------------
	public static String 					LOC_ROWID="loc_rowId";
	public static String 					HOL_REFID="hol_refId";
	public static String					INTENT_BOL="intent_Bol";
	
	
	private final static String 			MAIN_MENU="fragment_0";
	private final static String 			HOLIDAY_LIST="fragment_1";
	private final static String 			HOLIDAY_ADD="fragment_2";
	private final static String 			HOLIDAY_EDIT="fragment_3";
	private final static String 			LOCATION_LIST="fragment_4";
	private final static String 			LOCATION_ADD="fragment_5";
	private final static String 			LOCATION_EDIT="fragment_6";
	//private final static String 				LOCATION_ALBUM="fragment_7";
	//private final static String 				LOCATION_CAMERA="fragment_8";
	private final static String 			HOLIDAY_CHECKLIST="fragment_9";
	private final static String 			HOLIDAY_CHECKLIST_ADD="fragment_10";
	private final static String 			SHOPPING_LIST="fragment_11";
	private final static String 			SHOPPING_ADD="fragment_12";
	private final static String 			MY_SETTINGS="fragment_13";
	private final static String 			TRIP_CALCULATOR="fragment_14";
	private final static String 			TIPANDFX="fragment_15";
	//private final static String 				LOAD_DEFAULT_LIST="fragment_16";
	private final static String 			PHOTO_ALBUM="fragment_17";
	private final static String 			SIMPLE_HELP="fragment_18";
	private final static String 			MAP_HOL_LOC="fragment_19";
	private final static String 			MAP_DIS_LOC="fragment_20";
	private final static String 			PHOTO_ADD="fragment_21";
	private final static String 			SHOPPING_EDIT_CAT="fragment_22";
	private final static String 			CHECK_EDIT_CAT="fragment_23";
	private final static String 			MAP_LOC="fragment_24";

	private PPGoPlacesApplication 	app;
	private SharedPreferences 			prefs;
	//private Editor								edit;
	
	//--fragment management----------------------------------------------------------
	public boolean 								isDualPane;
	private FragmentManager 			fragmentManager;
	//private FragmentTransaction 		fragmentTransaction;
	//private Context 							context;
	//private Configuration 					config;
	//private LocationView 					locationView;
	//private Bundle 								args;
	private int										menuSelect;
	private int										mainFrag;
	private int										subFrag;
	private String									helpPage="mainmenu.html";	
	public boolean								atMain;
	public Address								newAddress;
		
	
	//--shared preferences configuration information for device-------------
	//private boolean 							firstRun;
	//private boolean 							osVersion;
	//private boolean 							hasCamera;
	//private boolean 							hasGPS;
	//private boolean 							isTablet;
	//private int 										scaleFactor;
	//private boolean 							menuMain;
	private AlertDialog						quitPPGP;
	
	//--last login days--------------------------------------------------------------------
	//private String 								this_login;
	//private String 								last_login;
	//private String 								before_last_login;
	//private int 										days_ago;
	//private MyDate 							today=new MyDate();
	//private MyDate 							lastDate=new MyDate();
	//private int 										daysBeforeLast;
	
	//--myBackupTask--------------------------------------------------------------------
	//private AlertDialog 						restoreDB;
	
	
	
	//=====1.0 SYSTEM LIFECYCLE ACTIVITY=====================================START====
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "139 onCreate");

		//--First time Enduser Agreement before proceed-------------------------------
        Eula.show(this);											//--RUN ONCE--launch eula.java interface for EULA
	
		//--Fragment----------------------------------------------
		//ActionBar actionBar 					= getActionBar();		//--sandy try master menu
		
		//--setup path to application---------------------------------------------------------
		app												=(PPGoPlacesApplication)getApplication();	
		prefs											=getSharedPreferences(PREFNAME, MODE_PRIVATE);
		//edit=prefs.edit();
		
		
		setupView();
		
	}		//--END onCreate
	
	@Override
	public void onResume() {
		super.onResume();

	}		//--END onResume
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		super.onCreateOptionsMenu(menu);
		CreateMenu(menu);
		return true;
	}		//--END onCreateOptionsMenu

	
	//=====1.0 SYSTEM LIFECYCLE ACTIVITY====================================END======
	
	//=====2.0 MAIN APPLICATION SETUP====================================START======
	//--main setup view routine
	public void setupView() {
		Log.d(TAG, "175 landed in setupView");
		
		//--check first fun----------------------------------------------------------------------
		if (app.getFirstRun()) {
			firstRunned();
			app.setRunned();
			Log.d(TAG,  "194 done getFirstRun");
		}
	
	   //--retreive package name
        PackageManager pm 					= this.getPackageManager();
		Log.d(TAG, "package name is "+ getPackageName());
	
		//*****WORK IN PROGRESS******************************************
		//--check for big screen dual pane--------------------
		//--retrieve isTablet for isDualPane, need to add rotational
		isDualPane									= prefs.getBoolean("isDualPane", false);
		Log.d(TAG, "201 program get isDualPane, before temporary hardcode "+isDualPane);
		isDualPane									= false;			//--temporary hardcode for test  dual screen config

		View fragSpace2							= findViewById(R.id.fragSpace2);
		/*if (isDualPane) { 
			fragSpace2.setVisibility(View.VISIBLE);
		} else {
			fragSpace2.setVisibility(View.GONE); 
		}*/
		//isDualPane=fragSpace2 !=null && fragSpace2.getVisibility()==View.VISIBLE;
		Log.d(TAG, "211 IsDualPane is available, after hardcode "+isDualPane);
		
		
		//--leave listLocation intact and other view to fragment 2 if dual panes available--
		if (isDualPane) {
			mainFrag 								= R.id.fragSpace;
			subFrag									= R.id.fragSpace2;
			if (getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE) {
				setContentView(R.layout.main_twopanes);								//--tab landscape
			} else {									
				Log.d(TAG, "186  get screen orientation is it true ? ");
				setContentView(R.layout.main_twopanes_narrow);					//--tab portrait
			}
		} else {										
			mainFrag 								= R.id.fragSpace;
			subFrag									= R.id.fragSpace;			
			setContentView(R.layout.main_onepane);										//--small screen
	}
	
	
	//=====2.0 MAIN APPLICATION SETUP=====================================END======
	
	//=====3.0 FRAGMENT MANAGEMENT====================================START======
	
	
	//--start main PPGoPlaces fragment--------------------------
	fragmentManager							= getFragmentManager();
	FragmentManager.enableDebugLogging(true);		//for debugging, remove before ship
	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	
	PPGoPlaces ppgoplaces					= new PPGoPlaces();
	Log.d(TAG, "240 mainFrag add ppgoplaces");
	//fragmentTransaction.add(R.id.fragSpace, ppgoplaces);
	fragmentTransaction.add(mainFrag, ppgoplaces, MAIN_MENU);
	fragmentTransaction.commit();    	
	
	/* if (null==fragmentManager.findFragmentByTag(MAIN_MENU)) {  
	 * if (menuMain) {} else {
		NewMenu mainMenu=new NewMenu();
		fragmentTransaction.add(R.id.fragSpace, mainMenu, MAIN_MENU);
	}*/
	
	/*if (isDualPane) {
	 TripCalculator tripCalculator=new TripCalculator();
	 fragmentTransaction.add(R.id.fragSpace2, tripCalculator, TRIP_CALCULATOR);
	}*/
	
	//fragmentTransaction.addToBackStack(null);
	
	}	//END onCreate()	
	
	//--first run routine, RUN ONCE ONLY------------------------------------------------------
	public void firstRunned() {
		
		//--hardware detection---------------------------------------------------
		boolean hasCamera					= true;
		boolean hasGPS							= true;
		boolean hasLocationNetwork;
		boolean isDualPane					= false;
		//boolean hasCamera=checkCameraHardware(context);
		//boolean hasGPS=checkGPSHardware(context);
		Log.d(TAG, "285 in firstRunned()");
		//--check os version-------------------------------------------------------
		boolean newAPI 						= android.os.Build.VERSION.SDK_INT > 16;
		
		//--check if istable----------------------------------------------------------
		//Configuration config					= getResources().getConfiguration();
		boolean xlarge							= Configuration.SCREENLAYOUT_SIZE_XLARGE>4;		//constant 4
		boolean large								= Configuration.SCREENLAYOUT_SIZE_LARGE>2;			//constant 3 normal is 2
		Log.d(TAG, "293 Test screen size xLarge "+ xlarge +", or large "+large);
		boolean isTablet;
		if(xlarge || large) {
			isTablet=true;
		}else{ 
			isTablet=false;}
		Log.d(TAG, "299 is Tablet "+isTablet);
		
		hasCamera 								= this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
		Log.d(TAG, "302 context.getPackageManager detect camera "+hasCamera);
		
		hasGPS 									= this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
		Log.d(TAG, "305 context.getPackageManager detect gps "+hasGPS);
		
		hasLocationNetwork				= this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK);
		
		int scaleFactor						= 5;
		if (hasCamera) {
			scaleFactor							= computeCameraScaleFactor();
			Log.d(TAG, "312 hasCamera done "+hasCamera+", scaleFactor "+scaleFactor);
		}
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int dispW = size.x;
		int dispH = size.y;
		
		SharedPreferences.Editor edit=prefs.edit();
		edit.putBoolean("firstRun", false);
		edit.putBoolean("osVersion", newAPI);
		edit.putBoolean("hasCamera", hasCamera);
		edit.putBoolean("hasGPS", hasGPS);
		edit.putBoolean("isDualPane", isTablet);
		edit.putInt("scaleFactor", scaleFactor);
		edit.putInt("scaleFactorS", scaleFactor+2);
		edit.putString("deviceName", android.os.Build.MANUFACTURER);		
		edit.putInt("dispW", dispW);
		edit.putInt("dispH",dispH);
		edit.commit();
	}
		
	//=====3.0 FRAGMENT MANAGEMENT======================================END======
	
	
	//=====4.0 FRAGMENT ACTIVITIES LISTENERS=================================START======
	
	@Override
	//--callback method from fragment with int for new target fragment and bundle to past
	//--used by PPGoPlaces, HolidayView, LocationView, HolidayCheckList, ShoppingList
	public void OnMenuSelected(int frag, Bundle bundle) {
		menuSelect = frag;
		Log.d(TAG, "336 onMenuSelected, \"i am back\", fragment selection "+frag);
		FragmentTransaction fragmentTransaction;
	
	switch (frag) {
	case 1:		//--HolidayList - R.id.fragSpace
		HolidayView holidayView=new HolidayView();
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(mainFrag, holidayView, HOLIDAY_LIST);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();
		break;
		
	case 2:		//--HolidayAdd - R.id.fragSpace2
		HolidayAdd holidayAdd=new HolidayAdd();
		holidayAdd.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, holidayAdd, HOLIDAY_ADD);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();
		break;
		
	case 3:		//--HolidayEdit - R.id.fragSpace2
		HolidayEdit holidayEdit=new HolidayEdit();
		holidayEdit.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, holidayEdit, HOLIDAY_EDIT);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();
		break;
		
	case 4:		//--LocationList - R.id.fragSpace
		LocationView locationView=new LocationView();
		locationView.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(mainFrag, locationView, LOCATION_LIST);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();				
		break;
		
	case 5:		//--LocationAdd - R.id.fragSpace2
		Log.d(TAG, "354 landed case 5");
		LocationAdd locationAdd=new LocationAdd();
		locationAdd.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, locationAdd, LOCATION_ADD);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();				
		break;
		
	case 6:		//--LocationEdit - R.id.fragSpace2
		LocationEdit locationEdit=new LocationEdit();
		locationEdit.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, locationEdit, LOCATION_EDIT);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();				
		break;
		
	/*case 7:		//LocationAlbum
		LocationAlbum locationAlbum=new LocationAlbum();
		locationAlbum.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(fragmentResName, locationAlbum, LOCATION_ALBUM);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();				
		break;*/
		
	/*case 8:		//LocationCamera
		LocationCamera locationCamera=new LocationCamera();
		locationCamera.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(fragmentResName, locationCamera, LOCATION_CAMERA);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();				
		break;*/
		
	case 9:		//--HolidayCheckList - R.id.fragSpace
		//--<<<<<Boilerplate for Fragment LifeCycle Template>>>>>
		HolidayCheckList holidayCheckList=new HolidayCheckList();
		holidayCheckList.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(mainFrag, holidayCheckList, HOLIDAY_CHECKLIST); 
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();
		break;
		
	case 10:		//--HolidayCheckListAdd - R.id.fragSpace2
		CheckAddTask checkAddTask=new CheckAddTask();
		checkAddTask.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, checkAddTask, HOLIDAY_CHECKLIST_ADD);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();
		break;
		
	case 11:		//--ShoppingList - R.id.fragSpace
		ShoppingList shoppingList=new ShoppingList();
		shoppingList.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(mainFrag, shoppingList, SHOPPING_LIST);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();
		break;
		
	case 12:		//--ShoppingAdd - R.id.fragSpace2
		ShoppingAdd shoppingAdd=new ShoppingAdd();
		shoppingAdd.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, shoppingAdd, SHOPPING_ADD);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();
		break;
		
	case 13:		//--MyPreferences - R.id.fragSpace2
		MySettings myPreferences=new MySettings();
		//myPreferences.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, myPreferences, MY_SETTINGS);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();
		break;
		
	case 14:		//--TripCalculator - R.id.fragSpace2
		TripCalculator tripCalculator=new TripCalculator();
		tripCalculator.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, tripCalculator, TRIP_CALCULATOR);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();
		break;
		
	case 15:		//--TipAndFxDialog - R.id.fragSpace2
		TipAndFxDialog tipAndFxDialog=new TipAndFxDialog();
		tipAndFxDialog.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, tipAndFxDialog, TIPANDFX);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();				
		break;
		
	/*case 16:		//--LoadDefaultCheckList - R.id.fragSpace2
		LoadDefaultCheckList loadDefaultCheckList=new LoadDefaultCheckList();
		loadDefaultCheckList.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, loadDefaultCheckList, LOAD_DEFAULT_LIST);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();				
		break;		*/
		
	case 17:		//--PhotoAlbum - R.id.fragSpace2
		PhotoAlbum photoAlbum=new PhotoAlbum();
		photoAlbum.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, photoAlbum, PHOTO_ALBUM);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
				FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();		
		break;				
		
	case 18:		//--Help and About - R.id.fragSpace2
		SimpleHelp simpleHelp=new SimpleHelp();
		simpleHelp.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, simpleHelp, SIMPLE_HELP);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
				FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();				
		break;		
		
	case 19:		//--MapHolidayLocations - R.id.fragSpace2
		MapHolidayLocations mapHolidayLocations=new MapHolidayLocations();
		mapHolidayLocations.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, mapHolidayLocations, MAP_HOL_LOC);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
				FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();				
		break;					

	case 20:		//--MapDisplayLocations - R.id.fragSpace2
		MapDisplayLocation mapDisplayLocation=new MapDisplayLocation();
		mapDisplayLocation.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, mapDisplayLocation, MAP_DIS_LOC);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
				FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();				
		break;		

	case 21:		//--Add Picture From Gallery - R.id.fragSpace2
		PhotoAdd photoAdd=new PhotoAdd();
		photoAdd.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, photoAdd, PHOTO_ADD);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
				FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();				
		break;		
		
	case 22:		//--ShoppingEdit - R.id.fragSpace2
		ShoppingEditCat shoppingEditCat=new ShoppingEditCat();
		shoppingEditCat.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, shoppingEditCat, SHOPPING_EDIT_CAT);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();
		break;
		
	case 23:		//--CheckCatEdit - R.id.fragSpace2
		CheckCatEdit checkCatEdit = new CheckCatEdit();
		checkCatEdit.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, checkCatEdit, CHECK_EDIT_CAT);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();
		break;

	case 24:		//--MapLocation - R.id.fragSpace2
		MapLocation mapLocation = new MapLocation();
		mapLocation.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, mapLocation, MAP_LOC);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();
		break;
		
	default:
		break;
	}
	}   //--END OnMenuSelected Listener
	
	//=====4.0 FRAGMENT ACTIVITIES LISTENERS=================================END======
	
	
	//=====5.0 METHODS TO CALL FRAGMENT WITH NO CALLBACK======================START======


	//--calling from fragment via menu option without mCallback-----------------------
	
	
	//--call change preference or setting
	public void callMyPreferences() {
		FragmentTransaction fragmentTransaction;
		MySettings myPreferences=new MySettings();
		//myPreferences.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, myPreferences, MY_SETTINGS);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();
	}
	
	public void callCheckCatEdit() {
		FragmentTransaction fragmentTransaction;		
		CheckCatEdit checkCatEdit = new CheckCatEdit();
		//checkCatEdit.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, checkCatEdit, CHECK_EDIT_CAT);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();
	}

	public void callTipAndFx() {
		FragmentTransaction fragmentTransaction;		
		TipAndFxDialog tipAndFxDialog=new TipAndFxDialog();
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, tipAndFxDialog, TIPANDFX);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
	    		FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();				
	}
	
	public void callMapLocation(Bundle bundle, MapLocation mapLoc) {
		FragmentTransaction fragmentTransaction;	
		//MapLocation mapLocation = new MapLocation();
		mapLoc.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		fragmentTransaction.replace(subFrag, mapLoc, MAP_LOC);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
				FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();				
	}
	
	public void callLocationAdd(Bundle bundle) {
		FragmentTransaction fragmentTransaction;	
		LocationAdd locationAdd = new LocationAdd();
		locationAdd.setArguments(bundle);
		fragmentTransaction=getFragmentManager().beginTransaction();
		int fromMapLocation = bundle.getInt(ADD_CODE, MAP_LOCATION);
		fragmentTransaction.replace(subFrag, locationAdd, LOCATION_ADD);
		//fragmentTransaction.addToBackStack(null);
		fragmentTransaction.setTransition(
				FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.commit();				
	}
	
	//=====5.0 HELPER METHODS===========================================END======

	//=====6.0 SETTERS AND GETTERS=======================================START======

	//--Getters and Setters use for fragment communication
	private Address 					thisAddress;				
	private int							requestCode;				//--invoke fragement code, call other fragment for result
	private int							resultCode = 0;					//--callback fragment code, provide result
	//private double					cLatitude;
	//private double					cLongitude;
	//private boolean				bolResult;					//--true if result code is succesful
	//private boolean				locationAvailable;		//--true if location is available
	private HLocation				tempLocation;
	

	//--set request  code of calling fragment
	//--similar to onActivityResult, int == requestCode such as MAP_LOCATION, HOLIDAY_VIEW
	public void setRequestCode(int requester) {
		requestCode 		= requester;
	}
	
	//--get request code of calling fragment
	public int getRequestCode() {
		return requestCode;
	}
	
	//--set result code of callback fragment
	//--similar to onActivityResult, true == resultCode of 1 success, default 0 fail
	public void setResultCode(int result) {
		resultCode			= result;
	}
	
	//--get result code of callback fragment 
	public int getResultCode() {
		int replyCode = resultCode;
		resultCode = 0;
		return replyCode;
	}
	
	//--set Address from MapLocation
	//--return data
	public void setAddress (Address address) {
		thisAddress			= address;
	}
	
	//--return Address
	public Address getAddress() {
		return thisAddress;
	}
	
	//--set Location from LocationEdit
	public void setHLocation (HLocation location) {
		tempLocation		= new HLocation();
		tempLocation		= location;
	}
	
	//--return HLocation
	public HLocation getHLocation() {
		return tempLocation;
	}
	

	
	//=====6.0 SETTERS AND GETTERS========================================END======
	

	
	//==SANDY==========================================
	
	//- return true if this is the first run-------------------------------------	
	public boolean getFirstRun() {
		return prefs.getBoolean("firstRun", true);
	}
	
	
	public void firstRunPreferences() {
		Context myContext = this.getApplicationContext();
		prefs=myContext.getSharedPreferences(PREFNAME,0);	
		//0=mode private only this app can read these preferences
	}   

	//=====6.0 MENU METHODS===========================================END======
	
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {		//--use to be boolean
		super.onCreateOptionsMenu(menu);
		CreateMenu(menu);
		return true;
	}*/
	
	/*@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (!isDBEmpty && hLocation != null ) {
			long geoDist=app.getGeoFence()/1000;
			menu.removeItem(6);		
			MenuItem mnu6=menu.add(0, 6, 6, "Locations Within "+geoDist+"km");
			{ mnu6.setIcon(android.R.drawable.ic_menu_myplaces); }	
		}
		//return true;
	}*/
	
	//--Return menu choice on menu selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		return MenuChoice(item); 
	}
	
	private void CreateMenu(Menu menu) {

		MenuItem mnu9=menu.add(0, 9 , 9, R.string.trip_calculator);
		{ 	mnu9.setIcon(android.R.drawable.ic_dialog_dialer); 
			mnu9.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}		

				
		MenuItem mnu10=menu.add(0, 10 , 10, R.string.settings);
			{ 	mnu10.setIcon(android.R.drawable.ic_menu_preferences); 
				mnu10.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}		

		MenuItem mnu11=menu.add(0, 11, 11, R.string.help);
			{ 	mnu11.setIcon(android.R.drawable.ic_menu_help); 
				mnu11.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}		

	}
		
	private boolean MenuChoice(MenuItem item)
	{
		switch (item.getItemId()) {

			
		/*case 1:			
			//--goto tip and fx dialog---------------
			Bundle kextras = new Bundle();
			this.OnMenuSelected(15, kextras);				//--tip and fx case 15		
			return true;
			
		case 2:			
			//--goto about----------------
			Bundle pextras = new Bundle();
			pextras.putString("helpPage", "about.html");
			this.OnMenuSelected(18, pextras);				//--About case 18	
			return true;*/

		case 9:	
			//--goto trip calculator---------
			Bundle qextras = new Bundle();
			this.OnMenuSelected(14, qextras);				//--Trip calculator case 14
			return true;

			
		case 10:	
			//--goto setting---------------
			Bundle extras = new Bundle();
			this.OnMenuSelected(13, extras);					//--Settings case 13		
			return true;
			
		case 11:			
			//--goto help---------
			//mCallback.OnMenuSelected("mainmenu.html", false, 0, 18);				//--Help case 19
			Bundle oextras  = new Bundle();
			oextras.putString("helpPage", this.helpPage.toString());
			this.OnMenuSelected(18, oextras);				//--Help case 19
			return true;
			
		}
		return false;
	}
	
	//==========SANDY==========================================

	//--QUIT PPGoPlaces Application
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//super.onKeyDown(keyCode, event);
		
		try {
		PPGoPlaces ppgoplaces = (PPGoPlaces)getFragmentManager().findFragmentById(mainFrag);
		//Log.d(TAG, "756 before if isVisible() ");
			if (ppgoplaces.isVisible()) {
			   atMain=true;
			} 
		} catch (ClassCastException e) {			//--use class exception error i.e. not in PPGoPlaces to run exit dialog
			atMain=false;
		}
		
		//Log.d(TAG, "765 current fragment name ");
		
		if (keyCode == KeyEvent.KEYCODE_BACK && atMain) {
			quitPPGP= new AlertDialog.Builder(this)
			.setIcon(R.drawable.ppgp_icon)
			.setTitle("PPGoPlaces ...")
			.setMessage("Quit Application?")
			.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
	    				finish();
				}
			})
			.setNegativeButton("Cancel", new AlertDialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
						quitPPGP.cancel();
				}
			})
			.create();
			quitPPGP.show();
       } 
		return super.onKeyDown(keyCode, event);
	}


	
	//=====5.0 METHODS TO CALL FRAGMENT====================================END======
	
	//=====11.0 USEFUL NOTES FOR FRAGMENT===========================================
	/*
	 -replace finish() and hide soft keyboard
	 		//--hide soft keyboard and exit
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 
		    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),      
		    InputMethodManager.HIDE_NOT_ALWAYS);
			//getActivity().finish();
			getFragmentManager().popBackStackImmediate();
	 
	 - to use options menu, either in onCreateView() or onResume()
	 		setHasOptionsMenu(true);
	 
	 - to retain instance
	 		setRetainInstance(true);
	 
	 - to access travel lite methods
	 		private TravelLiteActivity		travelLite
	        travelLite=(TravelLiteActivity)context.getApplicationContext();
	 
	  - replace "context" with "Context", import android.content.Context
	  e.g. InputMethodManager imm = (InputMethodManager)
	  		getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	 
	 */

	

}		//--MAIN END CLASS
