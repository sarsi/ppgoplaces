/* 
 * Program: 			PPGoPlaces
 * Description:     Learning Android Programming, my first homework
 *                         Lifestyle application for managing shopping list packing list, candid photos, 
 *                         holiday locations and albums.
 * Summary: 		First Screen, main program
 * 
 * Last Release:  	September 18, 2012 (version 1.1)
 * Last Updated: 	September 17, 2012
 * This Updates: 	October 12, 2013
 * 
 * Notes: Refer to PPGoPlacesApplication for release schedule and updates.
 * 
 * Changes since last official release:
 * - Fragment trial Sep 23, 2013
 * 	- Tidy codes and documentation
 * - Moved Shopping List, Check List, Quick Text and Home Location to front
 * - Moved Locations and Holidays Lists to Sliding Drawer
 * - Add Fragment
 * 
 * 
 * Outstanding fixes
 * - Harden codes for Location listeners 
 * - Clean out unused, duplicate and redundant codes
 * - eula.show
 * - key
 * - need to fix date picker showDialog deprecated
 * - need to fix the common menu
 * 
 * 
 * 
 * =====ACTIVITY GENERAL FLOW===================Sept 13, 2012=====
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
 * 10. SandBox - trial methods stay here until stable for deployment into main codes.
 * 
 * =====PPGoPlaces Activity===================================
 * 
 * 
 * 
 */

package com.liseem.android.travel;

import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;
import static com.liseem.android.travel.TravelLiteActivity.*;

import com.liseem.android.travel.adapter.MyBackupTask;
import com.liseem.android.travel.items.HLocation;
//import com.liseem.android.travel.items.Holiday;
import com.liseem.android.travel.items.LPicture;
import com.liseem.android.travel.items.MyDate;
//import com.liseem.android.travel.items.Shopping;
//import com.liseem.android.travel.items.TaskList;

import android.content.SharedPreferences;

//import java.io.File;
//import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
//import java.util.concurrent.ExecutionException;
//import java.util.List;

//import android.app.Dialog;
import android.app.Activity;
import android.app.Fragment;
import android.app.PendingIntent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.os.Environment;
//import android.os.Parcelable;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
//import android.content.IntentFilter;

import android.provider.MediaStore;
import android.util.Log;
//import android.view.ContextMenu;
import android.view.KeyEvent;

import android.view.Menu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.ViewGroup;
//import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
//import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;


import android.content.Context;
import android.content.pm.PackageManager;


//--set background method----------------------
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Matrix;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;


import android.view.inputmethod.InputMethodManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.webkit.WebSettings;
import android.webkit.WebView;
//import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
//import android.widget.Toast;



public class PPGoPlaces extends Fragment implements MyBackupTask.CompletionListener  {	
	
	
	//=====SECTION 1==DECLARATION===================================


	//--menu selection callback listener to FragMain activity----
	private OnMenuSelectedListener 	mCallback;

	
	//--constants-----------------------------------------
	private static final String 			TAG="PPGoPlaces";
	private final static int 					TAKE_PICTURE=101;
	private static final int 					TIME_LAPSE=1000*60*2;		//two minutes
	private final static String				FILTER_TYPE="filteredView";
	private final static int					FILTER_NONE=31;
	private final static int					FILTER_HOLIDAY=32;
	
	//--system-------------------------------------------
	private PPGoPlacesApplication 	app;
	private SharedPreferences 			prefs;
	private SharedPreferences.Editor editor;
	private String 								fxRate;
	private String 								tipPercent;	
	private boolean 							isDualPane;
	private int										versionCode;

	private MyDate 								today=new MyDate();
	private MyDate 								lastDate=new MyDate();

	private AlertDialog 						quitPPGP;
	private long									dbRowId;					//use to persist for samsung S2 onActivityResult

	
	//--views setup--------------------------------------
	private TextView 							todayDate;
	private EditText 							quickText;
	private Button 								quickButton;
	private Button								handle;
	private ImageButton 						holidayView;
	private ImageButton 						locationView;
	private ImageButton 						checkList;
	private ImageButton 						shopList;
	private ImageButton 						homeList;
	private ImageButton 						addLoc;
	private ImageButton						sayCheese;				//camera
	private ImageButton						viewGallery;			//album
	private TextView							locationInfo;
	private SlidingDrawer 					lastLocation;

	//--variables-----------------------------------------
	private String 								smartInput;				//quick text
	private boolean 							isDBEmpty;				//check location db empty
	
	//--default location display-----------------------
	private HLocation 							hLocation=new HLocation();
	private LPicture 								newPicture;
	private long									defaultLocation;
	
	//--Location Listener, Manager and Provider----------------
	private PendingIntent 			 		singleUpatePI;
	private Location 					 		currentLocation; 			//currently use for geoFence locationView
	private MyLocationListener 			locationListener;
	private LocationManager 				locationManager;
	

	
	

	
	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================
		
	//--part 1 of 2 - container fragment interface for activity callback-----
	public interface OnMenuSelectedListener {
		public void OnMenuSelected(int func, Bundle bundle);
	}	

	//--part 2 of 2 - container fragment interface for activity callback-----
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		//Log.d(TAG,"329 onAttach");
		try {
			mCallback=(OnMenuSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException (activity.toString()
					+"must implement OnMenuSelectedListener");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;

		//--inflate layout for fragment 1------------------------------------------------
		View view=inflater.inflate(R.layout.ppgoplaces_main, container, false);
		//Log.d (TAG, "228 View onCreateView");
		setHasOptionsMenu(true);
        setRetainInstance(true);
		
		//good reference how to force set screen orientation
		/*if (getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE) {
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}*/		
		return view;
		
	}
 	
	@Override
	public void onStart() {
		super.onStart();
        setupPreferences();														//--Need app for setupPreferences() method
		Log.d(TAG,"261 onStart()");
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.d (TAG, "247 onCreate");
    }  //--END onCreate()
 
    
	@Override
    public void onResume() {
    	super.onResume();
		Log.d (TAG, "275 onResume");
              
    	//fbSetup();																	//--Future fbSetup() method
        setupView(); 
        setListener();
        setupLocation();
       
        if (quickText.length()>1)											//--Quick button for exchange conversion
        	quickButton.setClickable(true);      
        
    }	//--END onResume()

	
	@Override
	public void onPause() {

		//locationManager.removeUpdates(singleUpatePI);
		//unregisterReceiver(singleUpdateReceiver);
		super.onPause();
		//Log.d (TAG, "281 onPause");

	}	//END onPause()
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		//Log.d (TAG, "289 onDestroy");

	}	//END onDestroy()
	
	
	/** Persist state of variables avoid losing transient variables value between activities switch */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		//--Save vulnerable row id for Samsung S2--
		outState.putLong("dbRowId", dbRowId);
		//Log.d (TAG, "301 onSaveInstanceState");

	}
	

	/*@Override			//--pre Fragment
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		//--retrieve vulnerable row if for Samsung S2--
		dbRowId=savedInstanceState.getLong("dbRowId");
	}*/
	

	//--Create Options menu---------------------------------
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		CreateMenu(menu);
	}
	
	//--Return menu choice on menu selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		return MenuChoice(item);
	}
	
	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 3==SETUP VIEWS====================================
	
	private void setupPreferences() {

		//-- COPY COUNTRY.XML IF NOT EXISTS------------------------------------------
        /*if (app.getFirstRun()) {
        	 app.setRunned();
        } */
 	}	//--END setPreferences

	
	 //---setUpView---------------------------------------------------------------------------------
	private void setupView() {
		
         //--shared preferences-----------------------------------------------------------------
		app					= (PPGoPlacesApplication)getActivity().getApplication();
 		prefs				=getActivity().getSharedPreferences (PREFNAME, MODE_PRIVATE);
 		
		//boolean dontShowNew = prefs.getBoolean("dontShowNew", false);
		versionCode	=prefs.getInt("versionCode", 10);
		fxRate				=prefs.getString("fxRate","1");
		tipPercent		=prefs.getString("tipPercent", "0");
		

		
		//--setup views and butons----------------------------------
        //ppgp=(ImageButton)findViewById(R.id.ppgpView);
        holidayView	=(ImageButton)getActivity().findViewById(R.id.holidayView);
        locationView	=(ImageButton)getActivity().findViewById(R.id.locationView);
        handle				=(Button)getActivity().findViewById(R.id.handle);
              
        //--location setup------------------------------------------------       
        addLoc				=(ImageButton)getActivity().findViewById(R.id.addButton);
        sayCheese		=(ImageButton)getActivity().findViewById(R.id.sayCheese);
        viewGallery		=(ImageButton)getActivity().findViewById(R.id.viewButton);
        locationInfo		=(TextView)getActivity().findViewById(R.id.infoText);
        sayCheese.setVisibility(View.INVISIBLE);
       
        locationInfo.setMovementMethod(ScrollingMovementMethod.getInstance());
        
        //--sliding drawer view setup---------------------------------
        lastLocation		=(SlidingDrawer)getActivity().findViewById(R.id.slidingDrawer1);
        shopList			=(ImageButton)getActivity().findViewById(R.id.shopView);
        checkList			=(ImageButton)getActivity().findViewById(R.id.checkView);       
        homeList			=(ImageButton)getActivity().findViewById(R.id.homeView);       

        todayDate		=(TextView)getActivity().findViewById(R.id.todayDate);
        quickText		=(EditText)getActivity().findViewById(R.id.quickText);
        quickButton		=(Button)getActivity().findViewById(R.id.quickButton);
        quickButton.setClickable(false);
        quickButton.setText(R.string.convert);
        
        lastLocation.close();
        handle.setText("Slide Up To Open");

        //v1.0

        isDBEmpty		=app.isLocationDBEmpty();

        /* if (isDBEmpty) {
        	lastLocation.setVisibility(View.INVISIBLE);
        } else {
        	lastLocation.setVisibility(View.VISIBLE);
        }*/
        
		//--setup user selectable background, check empty background-------------------------------
		//myBackground=prefs.getString("myBackground", "");
		//orientInfo=prefs.getInt("orientInfo", 0);
		//if (myBackground !="none")  {			//i.e. not empty
    	//  setupBackground();
       //}
       
		Log.d(TAG,"415 setupView before setCalDate");
		//--crashed app because the country file is still copying in background thread
		//Log.d(TAG,"416 Sim Country lookup from code "+app.simCountry().toString());
		//Log.d(TAG,"417 Current Country lookup from code "+app.currentCountry().toString());
		
        
        //--display today date----------------------------------------
		today.setCalDate(Calendar.getInstance());		
		((TravelLiteActivity) getActivity()).displayTodayDate(todayDate, today);												
      		
		
		//--add text watcher for quick button, clickable only with number input--------------------------
		quickText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				
				int lastCount=quickText.length();			
				if (s.length()>0 || lastCount >0) 
					quickButton.setClickable(true);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start,
					int count, int after) {
				
				int lastCount=quickText.length();			
				if (count>0 || lastCount >0) 
					quickButton.setClickable(true);
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				
					int lastCount=quickText.length();
					if (count < 1 || lastCount <1)
						quickButton.setClickable(false);

			}
		});
		
		
	    //--forcefully close soft keyboard by default----------------------------------
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(quickText.getWindowToken(),0);
	    
		//=====SANDY===============================
		/*criteria.setAccuracy(Criteria.ACCURACY_FINE);
		passiveProvider = LocationManager.PASSIVE_PROVIDER;
		locationManager.requestLocationUpdates(
								passiveProvider, 
								LISTEN_TIME,
								LISTEN_DISTANCE, locationListener);*/
		
	    //--test what is new message, removed before ship---
	    
	    //--if first time using the latest PPGoPlaces release, display dialog on new features--------
	    //versionCode=10; 													//force old version to test dialog working only, !!!DISABLE AT ALL TIME
	    if (versionCode < app.getVersionCode()) {
			whatsNewThisRelease(); 									
			editor = prefs.edit();
			editor.putInt("versionCode", app.getVersionCode());
			editor.commit();
	    }
	    //Log.d(TAG,"480 leaving setupView");
	} //END setupview
	
	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================
	protected void setupLocation() {
		
		Log.d(TAG,"488 arrived setupLocation");
		
		//--initialised objects---------------
		ArrayList<LPicture> pictureList=new ArrayList<LPicture>();
		StringBuilder tempStr=new StringBuilder();
		new MyDate();
		
		//--retrieve default or last location set in view locations------------------------------
        //--need to check if db is empty before execute-----------------------------------------
        if (!isDBEmpty) {
        	
        	//--LOCATION AVAILABLE-------------------
			defaultLocation=prefs.getLong("defaultLocation", 0);
			
				if (defaultLocation==0) {

					hLocation=app.getLastLocation();								//retrieve last location
					dbRowId=hLocation.getId();
					tempStr.append("Last Location: \n").append(hLocation.getName().toString());
					//handle.setText("Last Location");
					app.setDefaultLocation(0);

					if (hLocation.getLatitude() > 0)
						app.setDefaultLat(hLocation.getLatitude());
					if (hLocation.getLongitude() >0)
						app.setDefaultLong(hLocation.getLongitude());				
				} else {
					hLocation=app.getHLocation(defaultLocation);			//retrieve default location
					dbRowId=hLocation.getId();
					tempStr.append("Default Location: \n").append(hLocation.getName().toString());
					//handle.setText("Default Location");
					app.setDefaultLocationObject(hLocation);
				}
				tempStr.append("\n").append(hLocation.getAddress().toString());				
				//thisDate.setStrDate(hLocation.getLdate().toString());
				//tempStr.append("\nDate: ").append(thisDate.getDMYDate());
			
	            if (hLocation.hasPicture()) {
	            	viewGallery.setVisibility(View.VISIBLE); 			//show album button
	    			pictureList=app.getLocationPictures(hLocation.getId());
	    			tempStr.append("\n").append("Album: ").append(pictureList.size()).append(" pictures");
	            } else {
	            	viewGallery.setVisibility(View.INVISIBLE);
	            }
	            sayCheese.setVisibility(View.VISIBLE);
	            locationInfo.setText(tempStr.toString());				//--display location details
	            locationInfo.setMovementMethod(ScrollingMovementMethod.getInstance());
            
        } else {		//--LOCATION DB EMPTY
        	
        	//--NO LOCATION FOUND---------------
        	sayCheese.setVisibility(View.INVISIBLE);
        	viewGallery.setVisibility(View.INVISIBLE);
        	locationInfo.setText("No Location Information Available");
        }

        Log.d(TAG, "544 leaving setupLocation");
	}		//END setupLocation()
	
	//--setup view of last location or default location--------------------------------------
	protected void displayLocation() {
		ArrayList<LPicture> pictureList=new ArrayList<LPicture>();
		
		if (hLocation !=null) {
			sayCheese.setVisibility(View.VISIBLE);
			//Log.d(TAG, "displayLocation line 516 rowid  "+hLocation.getId());
			
			MyDate locDate=new MyDate();
			locDate.setStrDate(hLocation.getLdate().toString());
			
			StringBuilder tempStr=new StringBuilder();
			tempStr.append(hLocation.getName().toString());
			tempStr.append("\n").append(hLocation.getAddress().toString()).append("\n");
			//tempStr.append("\nDate: ").append(locDate.getDMYDate());
			
			if (hLocation.hasPicture()) {
				viewGallery.setVisibility(View.VISIBLE); 			//--show album button
				//Log.d(TAG, "displayLocation line 529 hasPicture  "+hLocation.hasPicture());
				pictureList=app.getLocationPictures(hLocation.getId());							
				tempStr.append("Album: ").append(pictureList.size()).append(" pictures");
			}
				
			locationInfo.setText(tempStr.toString());				//--display location details
		} else {
			locationInfo.setText(R.string.no_location_found);
		}
	}
	
	
	//=====SECTION 4==ADD ADAPTER===================================
		
	//=====SECTION 5==SET LISTENER====================================
	
	protected void setListener() {
		
	    //--set up listeners for menu-----------------------------------------------------
        lastLocation.setOnDrawerOpenListener(new DrawerOpenListener());
        lastLocation.setOnDrawerCloseListener(new DrawerCloseListener());
        viewGallery.setOnClickListener(new viewGalleryButtonListener());
        sayCheese.setOnClickListener(new sayCheeseClickListener());
        quickButton.setOnClickListener(new quickConvertListener());
		
		holidayView.setOnClickListener(new holidayListOnClick());
		locationView.setOnClickListener(new locationListOnClick());
		checkList.setOnClickListener(new checkListOnClick());
		shopList.setOnClickListener(new shopListOnClick());
		homeList.setOnClickListener(new homeListOnClick());
		Log.d(TAG,"596 arrived at setListener just before addLoc.setOnClickListener");
		addLoc.setOnClickListener(new addLocationOnClick());
		
		//****NEED REVIEW OR DELETE************SEP 20 2013******************************
		//------pending single pending intent to return a location--------------------------------------------
		//--attempt to obtain initiate location information via passive location listener and
		//--via intent listener
		//locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);	
		//criteria=new Criteria();
		//criteria.setAccuracy(Criteria.ACCURACY_COARSE);	
		
	    //-- Pending Intent that will be broadcast by the ones hotlocation update.
	    //Intent updateIntent = new Intent(SINGLE_LOCATION_UPDATE_ACTION);  
	   // singleUpatePI = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		/*if (locationListener != null) { 
				   //(bestTime < maxTime || bestAccuracy > maxDistance)) { 
				  IntentFilter locationtFilter = new IntentFilter(SINGLE_LOCATION_UPDATE_ACTION);
				  context.registerReceiver(singleUpdateReceiver, locationtFilter );      
				  locationManager.requestSingleUpdate(criteria, singleUpatePI);
		}*/
		//------pending single pending intent to return a location--------------------------------------------
		//****NEED REVIEW OR DELETE************SEP 20 2013*********************END******
		
	}		//END Setlistener
	
	
	
	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================
	//--OnMenuSelected(String param, long id, int func)
	//--args putString(INTENT, param), putLong (LOC_ROWID, id), putLong(HOL_REFID, id)
		
	//--quick convert for fx and tips------------------------
	private class quickConvertListener implements OnClickListener {
		
		@Override
		//public void quickButtonClick(View v) {
		public void onClick(View v) {
			//Log.d(TAG,"638 quickButtonClick");
			smartInput=quickText.getText().toString();
		
			String checkTip=((TravelLiteActivity) getActivity()).for2home(smartInput, tipPercent);
			String checkFx=((TravelLiteActivity) getActivity()).for2home(smartInput, fxRate);
			todayDate.setText("Tips: "+checkTip+"  Fx: "+checkFx);
		}
	}
	
	//--holiday list---------------------------------------------------------------------------
	private class holidayListOnClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			
			//--get fragment holidayList------------------
			//mCallback.OnMenuSelected("", false, 0, 1);				//--HolidayView case 1
			Bundle jextras = new Bundle();
			mCallback.OnMenuSelected(1, jextras);				//--HolidayView case 1

			Log.d(TAG, "643 onListItemClick() HolidayList");
			
			//Intent holiday=new Intent(PPGoPlaces.this, HolidayView.class);
			//startActivity(holiday);
		}
	}

	//--lcoation list--------------------------------------------------------------------------
	private class locationListOnClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			
			//--goto location list------------------
			//mCallback.OnMenuSelected("", false, 0, 4);				//--LocationView case 4
			Bundle lextras=new Bundle();
			lextras.putBoolean(FILTER_LOCATION, false);
			lextras.putInt(FILTER_TYPE, FILTER_NONE);
			lextras.putInt(HOL_REFID, 0);
			mCallback.OnMenuSelected(4, lextras);				//--LocationView case 4
			
			//Intent hlocation=new Intent(PPGoPlaces.this, LocationView.class);	//LocationFragment
			//Bundle lextras=new Bundle();
			//lextras.putBoolean(FILTER_LOCATION, false);
			//lextras.putInt(FILTER_TYPE, FILTER_NONE);
			//lextras.putInt(HOL_REFID, 0);
			//hlocation.putExtras(lextras);
			//startActivity(hlocation);
		}
	}

	//--holiday checklist--------------------------------------------------------------------
	private class checkListOnClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			
			//--goto holiday checklist--------------
			Bundle hextras = new Bundle();
			hextras.putString("nothing", "nothing");
			mCallback.OnMenuSelected(9, hextras);				//--HolidayCheckList case 9
			
			//Intent taskList=new Intent(PPGoPlaces.this, HolidayCheckList.class);
			//startActivity(taskList);
		}
	}
	
	//--holiday shoppinglist-----------------------------------------------------------------
	private class shopListOnClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			
			//--goto shopping list----------------
			//mCallback.OnMenuSelected("", false, 0, 11);				//--Shopping List case 11		
			Bundle textras = new Bundle();
			mCallback.OnMenuSelected(11, textras);				//--Shopping List case 11		
			
			//Intent shopping=new Intent(PPGoPlaces.this, ShoppingList.class);
			//startActivity(shopping);
			}
	}

	//--home location view list-------------------------------------------------------------
	private class homeListOnClick implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			
			//--goto location list------------------
			//mCallback.OnMenuSelected("", false, 0, 4);			//--LocationView case 1 HOLID
			Bundle aextras=new Bundle();
			aextras.putBoolean(FILTER_LOCATION, true);
			aextras.putInt(FILTER_TYPE, FILTER_HOLIDAY);
			aextras.putLong(HOL_REFID, 0);
			mCallback.OnMenuSelected(4, aextras);			//--LocationView case 1 HOLID

			//Intent homeLoc=new Intent(PPGoPlaces.this, LocationView.class);
			//Bundle aextras=new Bundle();
			//aextras.putBoolean(FILTER_LOCATION, true);
			//aextras.putInt(FILTER_TYPE, FILTER_HOLIDAY);
			//aextras.putLong(HOL_REFID, 0);
			//homeLoc.putExtras(aextras);
			//startActivity(homeLoc);
		}		
	}


	//--shortcut add new location---------------------------------------------------------
	private class addLocationOnClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			
			Log.d(TAG, "750 addLocationOnClick at onClick before bundle");
			//--goto location add-------------------
			Long noHol			= (long) 0;
			Bundle mextras	=new Bundle();
			mextras.putBoolean(HAS_HOLIDAY, false);
			mextras.putLong(HOL_REFID,noHol);
			mCallback.OnMenuSelected(5, mextras);			//--LocationAdd case 5 
		}				
	}

	//--sliding drawer for last location added-------------------------------------------
	private class DrawerOpenListener implements OnDrawerOpenListener {

		@Override
		public void onDrawerOpened() {
			handle.setText("Slide Down To Close");
			//displayLocation();
		}
	}
	private class DrawerCloseListener implements OnDrawerCloseListener {
		
		@Override
		public void onDrawerClosed() {
			handle.setText("Slide Up To Open");			
		}
	}
	
	//--view last location photo album---------------------------------------------------
	protected class viewGalleryButtonListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (((TravelLiteActivity) getActivity()).checkExternalStorageWriteWrite()) {
				
				//--goto location add-------------------
				//mCallback.OnMenuSelected("", false, hLocation.getId(), 7);			//--LocationAlbum case 7 
				Bundle zextras=new Bundle();
				zextras.putLong(LOC_ROWID, hLocation.getId());
				//Log.d(TAG, "viewGalleryButton line 774, rowid "+hLocation.getId() );
				zextras.putBoolean("holidayAlbum", false);
				mCallback.OnMenuSelected(7, zextras);			//--LocationAlbum case 7 

				/*Intent myAlbum=new Intent(PPGoPlaces.this, PhotoAlbum.class);
				Bundle local=new Bundle();
				local.putLong(LOC_ROWID, hLocation.getId());
				//Log.d(TAG, "viewGalleryButton line 774, rowid "+hLocation.getId() );
				local.putBoolean("holidayAlbum", false);
				myAlbum.putExtras(local);
				startActivity(myAlbum);			 */
			} else {
				((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.sdcard_is_not_mounted_please_check_and_retry));
			}
		}
	}
	
	//--camera result for adding into picture db---------------------------------------------------

	//--take photo with intent camera----------------------------------------------------------
	protected class sayCheeseClickListener implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			Log.d(TAG, "812 say cheese land..");
			startActivityForResult(intent, TAKE_PICTURE);				
			
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {	
		
		if (resultCode==Activity.RESULT_OK) {
			newPicture=new LPicture();
			String imagePath;
			if(data.getData()!=null) {
				imagePath=((TravelLiteActivity) getActivity()).getPath(data.getData());
			} else {
				imagePath=((TravelLiteActivity) getActivity()).getLastImagePath();			//--for samsung phone return data null
				//imagePath=getPath(imageUri);				
			}
			
			//--to solve Samsung S2 problem, work around--------------------
			int testRow=(int)hLocation.getId();
			if (testRow==0) {
				defaultLocation=prefs.getLong("defaultLocation", 0);
				hLocation=new HLocation();
				if (defaultLocation==0) {
					hLocation=app.getLastLocation();								//--retrieve last location
					dbRowId=hLocation.getId();
				} else {
					hLocation=app.getHLocation(defaultLocation);			//--retrieve default location
					dbRowId=hLocation.getId();
				}	
			}
			//-------------------------------------------------------------------------------
			
			newPicture.setRefid(hLocation.getId());
			
			newPicture.setLocation(hLocation.getName());		
			if (hLocation.getAddress() !=null)
				newPicture.setAddress(hLocation.getAddress().toString());
			newPicture.setPicPath(imagePath.toString());
			if (hLocation.getLatitude() >0 && hLocation.getLongitude()>0) {
				newPicture.setLatitude(hLocation.getLatitude());
				newPicture.setLongitude(hLocation.getLongitude());
			}
			newPicture.setPicPath(imagePath.toString());			
			app.insertPicture(newPicture);
			
			hLocation.setPicture(true);
			app.updateHLocation(hLocation);

			((TravelLiteActivity) getActivity()).makeShortToast(getString(R.string.nice_shot));				//--nice shot message :)
		}
	}
	
	//****NEED MORE WORK, WONDER IS IT WORKING*******SEP 20 2013*******************
	//------pending single pending intent to return a location--------------------------------------------
	//--setup broadcast receiver for singleUpdate of location-------------
	protected BroadcastReceiver singleUpdateReceiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		       context.unregisterReceiver(singleUpdateReceiver);
		      
		       String key = LocationManager.KEY_LOCATION_CHANGED;
		       Location location = (Location)intent.getExtras().get(key);
		       if (currentLocation==null && location != null) {
		    	   currentLocation=location;
		       }
		      
		      if (locationListener != null && location != null) {
		    	  locationListener.onLocationChanged(location);
		      }
		      
		      locationManager.removeUpdates(singleUpatePI);
		   }
	};

	public void cancel() {
	    locationManager.removeUpdates(singleUpatePI);
	 }
	//------pending single pending intent to return a location--------------------------------------------
	//****NEED MORE WORK, WONDER IS IT WORKING************************END
	
	
	//=====SECTION 6==LISTENER METHODS================================

	//=====SECTION 7==HELPER METHODS==================================	
	
	//--backup routine listeners-----------------------------------------------------------------------------
   @Override
	public void onBackupComplete() {
		prefs=getActivity().getSharedPreferences (PREFNAME, MODE_PRIVATE);
		SharedPreferences.Editor editor=prefs.edit();
		editor.putString("lastBackup",today.getStrDate().toString());
		editor.commit();
		((TravelLiteActivity) getActivity()).makeLongToast( "Database Backup Successfully");
	}


	@Override
	public void onRestoreComplete() {
		((TravelLiteActivity) getActivity()).makeLongToast( "Database Successfully Restored");
	}


	@Override
	public void onError(int errorCode) {
		if(errorCode == MyBackupTask.RESTORE_NOFILEERROR) {
			((TravelLiteActivity) getActivity()).makeLongToast("No Backup Found to Restore");
		} else {
			((TravelLiteActivity) getActivity()).makeLongToast("Error During Operation: "+errorCode);
		}	
	}
	
	
	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================	

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (!isDBEmpty && hLocation != null ) {
			long geoDist=app.getGeoFence()/1000;
			menu.removeItem(6);		
			MenuItem mnu6=menu.add(0, 6, 6, "Locations Within "+geoDist+"km");
			{ mnu6.setIcon(android.R.drawable.ic_menu_myplaces); }	
		}
		menu.removeItem(10);			//--keep setting above about in menu
		//return true;
	} 
	
	
	private void CreateMenu(Menu menu) {

		//MenuItem mnu1=menu.add(0, 1 , 1, R.string.settings);
		//	{ mnu1.setIcon(android.R.drawable.ic_menu_preferences); }
		
		MenuItem mnu2=menu.add(0, 2 , 2, R.string.setup_fx_rate);
			{ mnu2.setIcon(android.R.drawable.ic_menu_agenda); }
			
		/*MenuItem mnu3=menu.add(0, 3 , 3, R.string.trip_calculator);
			{ mnu3.setIcon(android.R.drawable.ic_dialog_dialer); }		*/
			
		MenuItem mnu4=menu.add(0, 4 , 4, R.string.about);
			{ mnu4.setIcon(android.R.drawable.ic_input_get); }
		
		MenuItem mnu5=menu.add(0, 10 , 10, R.string.settings);			//--call FragMain menu
			{ 	mnu5.setIcon(android.R.drawable.ic_menu_preferences); 			//--ic_menu_manage
				mnu5.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}		
			
		MenuItem mnu11=menu.add(0, 11, 11, R.string.help);					//--call FragMain menu
			{ 	mnu11.setIcon(android.R.drawable.ic_menu_help); }		
	}
		
	private boolean MenuChoice(MenuItem item)
	{
		Log.d(TAG,"977 arrived at MenuChoice "+item);
		switch (item.getItemId()) {

		/*case 1:			
			Log.d(TAG,"981 arrived at case 1 ");

			//--goto setting---------------
			Bundle sextras = new Bundle();
			sextras.putString("nothing", "nothing");
			mCallback.OnMenuSelected(13, sextras);						//--Settings case 13		
			//((FragMain) this.getActivity()).callMyPreferences();
			
			//Intent myPrefs=new Intent (PPGoPlaces.this, MyPreferences.class);
			//startActivity(myPrefs);
			return true;*/
			
		case 2:			
			//--goto tip and fx dialog---------------
			//mCallback.OnMenuSelected("", false, 0, 15);				//--tip and fx case 15		
			Bundle kextras = new Bundle();
			mCallback.OnMenuSelected(15, kextras);						//--tip and fx case 15		

			//Intent convFx=new Intent (PPGoPlaces.this, TipAndFxDialog.class);
			//startActivity(convFx);	
			return true;
			
		case 3:			
			//--goto trip calculator---------
			//mCallback.OnMenuSelected("", false, 0, 14);				//--Trip calculator case 14
			Bundle qextras = new Bundle();
			mCallback.OnMenuSelected(14, qextras);						//--Trip calculator case 14
			
			//Intent tripCal=new Intent(PPGoPlaces.this, TripCalculator.class); 	
			//startActivity(tripCal);
			return true;
			
		case 4:	
			//--goto about----------------
			//mCallback.OnMenuSelected("about.html", false, 0, 18);				//--About case 18	
			Bundle pextras = new Bundle();
			pextras.putString("helpPage", "about.html");
			mCallback.OnMenuSelected(18, pextras);						//--About case 18	

			
			//Intent  aboutppgp=new Intent(PPGoPlaces.this, SimpleHelp.class);
			//aboutppgp.putExtra("helpPage", "about.html");
			//startActivity(aboutppgp);
			return true;
			
		/*case 5:	
			//--goto help---------
			//mCallback.OnMenuSelected("mainmenu.html", false, 0, 18);				//--Help case 19
			Bundle oextras  = new Bundle();
			oextras.putString("helpPage", "mainmenu.html");
			mCallback.OnMenuSelected(18, oextras);				//--Help case 19
			
			//Intent  simplehelp=new Intent(PPGoPlaces.this, SimpleHelp.class);
			//simplehelp.putExtra("helpPage", "mainmenu.html");
			//startActivity(simplehelp);
			return true; */
			
		/*case 6:
			
			//--view location filter by geo fence--------------------
			String provider=null;
			Location loc=new Location(provider);
			loc.setLatitude(hLocation.getLatitude());
			loc.setLongitude(hLocation.getLongitude());
			Intent locationView=new Intent(PPGoPlaces.this, LocationView.class);
			Bundle hextras=new Bundle();
			hextras.putBoolean(FILTER_LOCATION, true);
			hextras.putInt(FILTER_TYPE, FILTER_GEO);
			hextras.putLong(HOL_REFID, (long)0);
			locationView.putExtra("MyLocation", loc);
			locationView.putExtras(hextras);
			startActivity(locationView);		
			return true;*/
		}
		return false;
	}
		

	//--display what's new in this release-------------------------------------
	private void whatsNewThisRelease() {	
		
		AlertDialog.Builder whatsNew= new AlertDialog.Builder(getActivity());
		//--create checkbox for don't show this message in again-----------
		LayoutInflater adbInflater = LayoutInflater.from(getActivity());							
        View WhatIsNew = adbInflater.inflate(R.layout.checkbox, null);
       // dontShow = (CheckBox)WhatIsNew.findViewById(R.id.skipCheck);	//--redundant with ver. code check routine
        WebView webview=(WebView)WhatIsNew.findViewById(R.id.webView1);
        WebSettings settings = webview.getSettings();
		settings.setJavaScriptEnabled(true);
		//Log.d(TAG,"1091 in whatsNewThisRelease before webview load uri");

		webview.loadUrl("file:///android_asset/whatisnew.html");
		//Log.d(TAG,"1094 in whatsNewThisRelease after webview load uri");

		whatsNew.setView(WhatIsNew);
		whatsNew.setIcon(R.drawable.ppgp_icon);
		whatsNew.setTitle("What's New");
		//Log.d(TAG,"1099 in whatsNewThisRelease after setTitle");

		//listenForLocation.setMessage("The program will continue to listen " +
		//		"for new location update."+
		//		"\n\nNOTE: \nTo preserve battery life, \"Stop Location Updates\" when listening for new location and address is no " +
		//		"longer required. ");
		whatsNew.setPositiveButton("OK", new AlertDialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {		
				Log.d(TAG,"1109 in whatsNew onClick");
				//boolean checkBoxResult=false;
				/*if (dontShow.isChecked()) {
					Toast.makeText(getBaseContext(), "\"What's New\" message will not show again.", Toast.LENGTH_SHORT).show();
					dontShowNew=true;
					SharedPreferences.Editor editor=prefs.edit();
					editor.putBoolean("dontShowNew", true);
					editor.commit();
				}*/
			}
		});
		whatsNew.create().show();
	}
	
	//=====SECTION 8==MENU AND DIALOG METHODS===========================
	
	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//=====SECTION 10==SANDBOX======================================

	//***FOUND DUPLICATE IN APPLICATION, need to verified and delete one************************
	//--copy country file lookup table from raw to shared prefs 
	/*private class copyCountryFile extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			//File countryList=new File(getResources().getResourceName(R.raw.country));
			InputStream raw=getResources().openRawResource(R.raw.country);
			InputStream is=new BufferedInputStream(raw);
			
			File prefDir=new File("/data/data/com.liseem.android.travel/shared_prefs/");
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
				((TravelLiteActivity) getActivity()).showOkAlertDialog("Country file copy not successful");
				return false;
			}
		}
	}*/
	
	//--Called from onResume() locationManager.request --
	private class MyLocationListener implements LocationListener {
		boolean checkWhichBetter;

		//--Upon location information acquired--
		@Override
		public void onLocationChanged(Location location) {
			
			//--if no current locationavailable--------------
			if (currentLocation==null && location !=null) {
				currentLocation=location;
			      app.setLocationState(true);
			      app.setLocation(location);
				//updateStatus();
			} 
			else if (currentLocation !=null && location !=null) {

			//--check which is better before update--------
			//--isBetterLocation method in TravelLiteActivity, current timeDelta set to 2 mins--
			checkWhichBetter=isBetterLocation(location, currentLocation);
			
				//--use if new location is better------------------
				if (checkWhichBetter) {
					currentLocation=location;
					//updateStatus(); //look 5 lines down below				
				}
			}
		}

		@Override
		public void onProviderDisabled(String provider) { }

		@Override
		public void onProviderEnabled(String provider) { }

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) { }
		
	}	//END 7.4 LOCATIONLISTENER()
	
	//==GPS, LOCATION METHODS============================	
	//--test whether new location is better than current
 	//--http://developer.android.com/guide/topics/location/strategies.html
	//--isBetterLocation() Part 1 of 2
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		
		if (currentBestLocation==null) {
			//--a new location is always better than no location
			return true;
		}
		
		//--check the new location fix is new or older
		long timeDelta=location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer=timeDelta > TIME_LAPSE;		//time lapse more than 2 mins past
		boolean isSignificantlyOlder=timeDelta < -TIME_LAPSE;
		boolean isNewer=timeDelta >0;							
		
		//--if more than two minutes use new location
		if (isSignificantlyNewer) {
			return true;
		} else if (isSignificantlyOlder) {
			return false;
		}
		
		//--if new location fix is more or less accurate
		int accuracyDelta=(int) (location.getAccuracy()-currentBestLocation.getAccuracy());
		boolean isLessAccurate=accuracyDelta>0;
		boolean isMoreAccurate=accuracyDelta <0;
		boolean isSignificantLessAccurate=accuracyDelta >200; //more than 200 meters
		
		//--check if old and new location are from same provider
		boolean isFromSameProvider=isSameProvider(location.getProvider(), currentBestLocation.getProvider());
		
		//--determine location quality both time and accuracy
		if (isMoreAccurate) {
			return true;		//location accuracy
		} else if (isNewer && !isLessAccurate) {
			return true;		//both time newer and NOT less accuracy
			//--finally below, newer, NOT significant less accurange and from same provider
		} else if (isNewer && !isSignificantLessAccurate && isFromSameProvider) {
			return true;
		}
		
		//--all other condition return false
		return false;
	}
	
	//--isBetterLocation() Part 2 of 2
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1==null) {
			return provider2==null;
		}
		return provider1.equals(provider2);	//compare two string return true if same
	}

	//==========SANDY==========================================
	
   	
}		//--MAIN CLASS END

//=====SECTION TEMPLATE BELOW===========

	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================

	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================

	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//=====SECTION 10==SANDBOX======================================

//=====SECTION TEMPLATE END===========

	//=====SECTION 1==DECLARATION===================================
	
	//=====SECTION 2==LIFECYCLE METHODS===============================
	
	//=====SECTION 3==SETUP VIEWS====================================
	
	//=====SECTION 4==ADD ADAPTER===================================
	
	//=====SECTION 5==SET LISTENER====================================
	
	//=====SECTION 6==LISTENER METHODS================================
	
	//=====SECTION 7==HELPER METHODS==================================
	
	//=====SECTION 8==MENU AND DIALOG METHODS===========================
	
	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================
	
	//=====SECTION 10==SANDBOX======================================

