/*
 * Fragment: 					LocationAdd.java
 * Description:					Add location action activity capturing locations, gpx info, picture
 * 										and tagging to holiday.
 * 
 * Created: 						May 3, 2012
 * Last release:				September 6, 2012
 * Last updated: 				November 18, 2013
 * Clean up:						December 2, 2013
 * 
 * 
 * Changes:
 * - tidy up old codes and added setResult return to viewlocation
 * - changes to mylocation listener algorithms to on status changed and providers changed
 * to enhance location listener.
 * - managed to pass maplocation address object via fragmain setters and getters
 * - tweak isBetterLocation logics improve location get (TravelLiteActivity)
 * 
 * To do:
 * - tidy codes and documentation
 * - clean up Log.d
 * - centralized final static to TravelLiteActivity   
 * - try check and removes onActivityResult
 * 
 * 
 */


package com.liseem.android.travel;

import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;
import static com.liseem.android.travel.TravelLiteActivity.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.liseem.android.travel.items.AsyncTask;
import com.liseem.android.travel.items.HLocation;
import com.liseem.android.travel.items.Holiday;
import com.liseem.android.travel.items.LPicture;
import com.liseem.android.travel.items.MyDate;
import com.liseem.android.travel.MapLocation;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.AdapterView.OnItemSelectedListener;

public class LocationAdd extends Fragment 
											implements 	DateDialogFragment.DateDialogFragmentListener {
	
	//=====SECTION 1==DECLARATION===================================
	
	private final static String 			TAG="LocationAdd";
	
	protected static final int 			LOCATIONDATE_DIALOG_ID			=0;
	protected static final int 			REQUEST_CHOOSE_ADDRESS		=2;
	protected static final int 			REQUEST_CHOOSE_MYLOCATION	=3;	
	protected static final int 			TAKE_PICTURE								=4;
	protected static final int 			IMAGE_GALLERY								=5;
	protected static final int 			SETTINGS_CHANGE							=6;	
	
	protected final static String 		FILTER_TYPE									="filteredView";
	protected final static int 			FILTER_GEO										=33;
	
	protected final static int	 			ADD_LOCATION								=103;				//--from PPGoPlaces
	public static final String 				ADDRESS_RESULT 							="address";
	
	protected static final long 			CLOSE_BY										=20;					//--geofence for status update to trigger toast		
	private static final int 					LISTEN_TIME									=10000;			//--15 secs, shld change to 60,000millisecs
	private static final int 					LISTEN_DISTANCE							=20;					//--50 meters shld change to 200m

	private PPGoPlacesApplication 	app;
	private FragMain							act;
	private SharedPreferences 			prefs;
	private SharedPreferences.Editor	editor;
	//private Context 							context;
	//private volatile Thread 				threaker;
	
	//--VIEW SPECIFIC--
	private TextView 							locationDate;
	private TextView 							locationAddress;
	private EditText 							locationName;
	private TextView 							locationInfo;
	//private TextView 							locationText;
	private RadioGroup 						radioGroup;
	private RadioButton 						rb0;
	private RadioButton 						rb1;
	//private AlertDialog.Builder 			noLocation;	
	//private AlertDialog 						foundAddress;	
	private Spinner 								holidaySpinner;
	private ImageButton 						mapIT;									//--search via maplocation button
	private ImageButton 						useThis;								//--use address found
	private Button								locUpdate;
	private TextView 							emptyList;
	//private long 									geoDist;
    private String									actionText;


	//--DB SPECIFIC--
	private ArrayList<Holiday> 			holidayList;
	private ArrayList<LPicture> 			imagesList;
	private ArrayList<String> 			listItems;	
	private Holiday								holiday;
	private boolean							noHolidayList;
	private HLocation							hLocation;
	private LPicture								oneShot;
	private MyDate								lDate;
	private MyDate								startDate, endDate;
	private Calendar 							locDate;
	private boolean 							changesPending=false;
	private String 								whoCalling;
	private ArrayList<Long> 				questFRP;	
	private long 									dbRefId;

	//--LOCATION SPECIFIC--GEOCODER
	private List<Address> 					caddresses;	
	private Address 								address; 
	//private Address 							caddress;	
	private Geocoder 							geocoder;
	private Handler 								handler;		
	private String 								useAddress;							//--string to hold runOnUIThread return
	private Location 							currentLocation;					//--new current location
	private Location 							lastLocation;						//--previous location
	private boolean							hasHoliday=false;
	private boolean							indoor=true;						
	private AlertDialog						turnOnGPS;
	
	//--LOCATION SPECIFIC--LOCATION MANAGER
	private LocationManager 				locationManager; 	
	private MyLocationListener			locationListener;
	//private LocationListener 				locationListener;					//--Sept 1 need to investigate why not the former
	private String 								locationProvider;
	private Criteria 								criteria;
	private double								cLatitude=0.0;
	private double								cLongitude=0.0;
	private double								cAccuracy=0;
	private boolean 							locationAvailable=false;
	private boolean 							addressAvailable=false;	
	
	//--check gps availability
	private boolean 							gpsAvail;								//--check for gps enabled
		
	//--check network and data availability
	private boolean 							networkAvail;						//--check for network enabled
	private boolean							mobileAvail;						//--check for mobile enabled
	private boolean							dataConnection;					//--data network available, i.e 3G or WiFi
	private boolean							hasCoverage;						//--no cell or wifi coverage even for location
	
	private int										howAccurate;						//--set criteria to fine if less than 50m-updateStatus
	private boolean 							LocNameExist;
	private boolean 							mapAvail=true;
	private boolean 							gpsInfo;	
	private boolean							updateAdd=true	;				//--stop runonui update after user accept address			
	private boolean							stopListen;							//--stop/resume location listener
	private boolean 							mapAccurMode;					//--set Accuracy option, true FINE and false COARSE
	private boolean							listenMessage;		
	private CheckBox							dontShow;							//--checkbox in dialog, do not show in again
	private boolean							skipLocMsg;							//--shared pref not show listenForLocation msg again
	private String									locationString;

	//--NETWORK CONNECTIVITY
	private BroadcastReceiver 			networkStateReceiver;
	private ConnectivityManager 		connectivityManager;
	
	//--Fragment callback
	private int										fragmentResultCode;
	private int										fragmentRequestCode;
	
	//--isBetterLocation store lowest accuracy
	private Location								bestLocationFound;				//--bestLocationFound
	
	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;

		//--inflate layout for fragment--------------
		View v=inflater.inflate(R.layout.add_location, container, false);
		
		//--set environment---------------------------
		setHasOptionsMenu(true);
		setRetainInstance(true);
		
		return v;			
		
	}  //--END onCreateView

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}	//--END onCreate()
	
	@Override
	public void onStart() {
		super.onStart();
		
		//--call ActionBar------------------------------------------------------------------
		ActionBar actionBar 	= getActivity().getActionBar();
		actionBar.show();
		actionBar.setDisplayShowTitleEnabled(false);
		
		setupView();										//--308 view layout and bundle
		setNetworks();									//--415 network and network state receiver listener
		findMyLocation();								//--924 location manager and location listener		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		locationManager.requestLocationUpdates(							//--resume location listening
				locationProvider, 
				LISTEN_TIME, 
				LISTEN_DISTANCE, 
				locationListener);	

		setListeners();										//--570 view listener		
		
		//--listen to network change broadcast receiver----------
		IntentFilter filter 			= new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);        
		getActivity().registerReceiver(networkStateReceiver, filter);
		Log.d(TAG, "265 onResume(), after registerReceiver");

	} //--END onResume()

	
	@Override
	public void onPause() {
		locationManager.removeUpdates(locationListener); 				//--stop location listening
		getActivity().unregisterReceiver(networkStateReceiver);			//--stop listening to broadcast
		super.onPause();
	}
	
	/*@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		//--Save vulnerable information--
		outState.putBoolean("locationInfo", locationAvailable);
		outState.putBoolean("updateAdd", updateAdd);
		if(locationAvailable) {
			outState.putDouble("latitude", cLatitude);
			outState.putDouble("longitude",cLongitude);
		}
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

	public void setupView() {		//--onResume()
		
		//--path to PPGoPlacesApplication------------------------------
		app							= (PPGoPlacesApplication)getActivity().getApplication();	
		act							= (FragMain)getActivity();
		//Log.d(TAG, "316 setupView reach here");
		
		//--get SharedPreferences object--------------------------------
		prefs						= getActivity().getSharedPreferences(PREFNAME, MODE_PRIVATE);
		editor						= prefs.edit();
		indoor						= prefs.getBoolean("indoor", true);
		mapAccurMode		= prefs.getBoolean("mapAccurMode", false);			//--findLocation 2.2
		
			
		//--initialize instances---------------------------------------------
		holidayList				=new ArrayList<Holiday>();
		listItems					=new ArrayList<String>();
		holiday					=new Holiday();
		hLocation					=new HLocation();
		imagesList				=new ArrayList<LPicture>();
		hLocation.setPicture(false);																	//--prevent getAllLocation null exception
		hLocation.setHoliday(false);																	//--prevent getAllLocation null exception

		questFRP=new ArrayList<Long>();														//--KIV
		
		
		//--Initialize Variables and Object--------------------------------
		//this.context=context;
		geocoder					=new Geocoder(getActivity());								//--for reverse geocoder
		handler					=new Handler();														//--for location listener background thread
		caddresses				=new ArrayList<Address>();
		address					=new Address(null);
		//caddress					=new Address(null);
				
		//--setup views--------------------------------------------------------
		locationDate			=(TextView)getActivity().findViewById(R.id.locDate);
		locationAddress		=(EditText)getActivity().findViewById(R.id.locAdd);
		locationName			=(EditText)getActivity().findViewById(R.id.locName);
		//locationNotes		=(TextView)getActivity().findViewById(R.id.locNotes);
		locationInfo				=(TextView)getActivity().findViewById(R.id.locInfo);
		holidaySpinner		=(Spinner)getActivity().findViewById(R.id.holidaySpinner);
		emptyList				=(TextView)getActivity().findViewById(R.id.emptyList);
        radioGroup 				=(RadioGroup)getActivity().findViewById(R.id.radioGroup1);
        rb0 							=(RadioButton) getActivity().findViewById(R.id.radio0);
        rb1 							=(RadioButton) getActivity().findViewById(R.id.radio1);
        
        //--see setNetworks for default radio button setting
        locUpdate				=(Button)getActivity().findViewById(R.id.locUpdate);
        locUpdate.setVisibility(View.INVISIBLE);

		mapIT						=(ImageButton)getActivity().findViewById(R.id.mapIT);
		mapIT.setVisibility(View.INVISIBLE);
		useThis					=(ImageButton)getActivity().findViewById(R.id.useThis);
		useThis.setVisibility(View.INVISIBLE);
		
		holidaySpinner.setEmptyView(emptyList);						//--set Spinner to empty textview
		
		//--check requestCode for various action, i.e. reply from MapLocation or HolidayView
		fragmentRequestCode			= act.getRequestCode();	//--retrieve requestCode
		fragmentResultCode				= act.getResultCode();		//--resultcode reset upon retrieve
		
		switch(fragmentRequestCode) {
			case MAP_LOCATION:
				Log.d(TAG,"373 if replyCode is MAP_LOCATION");				
				if (fragmentResultCode==1) {									//--only if result code is successful, i.e. 1
					address						= act.getAddress();				//--retreive address from mapLocation
					if (address != null) {
						addressAvailable	= true;
						locationAvailable	= true;
						cLatitude 				= address.getLatitude();
						cLongitude				= address.getLongitude();
					}
					displayAddress();													//--ln 829
				}
				break;
			
			case HOLIDAY_VIEW:
				//--Holiday RowId pass from ViewHoliday or PPGoPlaces add location--
				Bundle bundle 		= this.getArguments();
				if (bundle != null) 
				{
					hasHoliday			= bundle.getBoolean(FILTER_LOCATION);
					dbRefId				= 	bundle.getLong(HOL_REFID);
					if (hasHoliday)														//--v1.0		(dbRefid>0)
						hLocation.setRefid(dbRefId);								//--v1.0 .set(lextras.getLong(HOL_REFID));
				}
				break;
				
			default:	//--not call from any fragment
				hLocation.setRefid(0);													//--hasHoliday false
				break;
		}

		//--set focus on locationName field--
		locationName.requestFocus();

		//--loadList, if there is holiday--
		loadList();																			//--497
		//v1.0 addItemsOnSpinner();												//--see 4.4 move to end of loadList()
		
		lDate					=new MyDate();
		lDate.setCalDate(Calendar.getInstance());
		String todayDate=lDate.getStrDate();
		
		//locationDate.setText(((TravelLiteActivity) getActivity()).displayShortDMY(todayDate));
		//--display date to text view, using myDate object passing into method
		((TravelLiteActivity)getActivity()).displayTodayDate(locationDate, lDate);							
		
	} 	//--END 3.1 SetupView

	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==SETUP LISTENERS=================================

	//--Setup Network and Broadcast Receiver--------------------------------------------------
	private void setNetworks() {		//--onResume()
		//--setup connectivity and connectivity condition
		//--check for wifi and dataconnection		
		dataConnection	=app.isOnline();
		hasCoverage		=PPGoPlacesApplication.hasCoverage(getActivity());
		//Log.d(TAG, "424 is dataConnection available "+dataConnection+", coverage "+hasCoverage);
		//Log.d(TAG, "indoor "+indoor);
		
		//--display Google map only if Geocoder is present
		/*if (app.isOnline() && locationAvailable) { 
			Log.d(TAG, "427 before setting mapIT to visible, location available "+locationAvailable);
			mapIT.setVisibility(View.VISIBLE);
		}		*/
		
		//--set default to indoor if hasCoverage is true, for quicker discovery-----
        if (hasCoverage) {
        	rb0.setChecked(true);
        	indoor=true;
        } else if (indoor) {
        	rb0.setChecked(true);
        } else {
        	rb1.setChecked(true);
        }
        
        if (stopListen) {
        	actionText = getActivity().getString(R.string.update);
        } else {
        	actionText = getActivity().getString(R.string.stop);
        }
		
		//--setup network broadcast listener--------------
		networkStateReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				boolean oldState=dataConnection;
				Log.d(TAG, "452 arrived networkStateReceiver onReceive");
				Log.d(TAG, "455 locationAvailable "+locationAvailable+" is addressAvailable "+addressAvailable);
				dataConnection	= app.isOnline();
				//networkAvail		= PPGoPlacesApplication.hasNetwork(context);
				//mobileAvail			= PPGoPlacesApplication.hasMobile(context);	
				//hasCoverage		= PPGoPlacesApplication.hasCoverage(context);				
				
				//--geocode for address if it is not already found---------------
				//--too many folks finding address!!!
				if (dataConnection && locationAvailable && !addressAvailable) {
					//---FIND ADDRESS CALL
					try {
						Log.d(TAG, "465 just before goFindAddress in onReceive");
						address=new goFindAddress().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, currentLocation).get();
					} catch (InterruptedException e) {	
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
				
				//--if data network no longer exist---------------------------------
				if (oldState && !dataConnection) {
						Toast.makeText(getActivity().getBaseContext(), 
								R.string.data_network_is_no_longer_available_, 
								Toast.LENGTH_SHORT).show();
						
						mapIT.setVisibility(View.INVISIBLE);
				} 
			
				if (dataConnection) {											//--Nov 17, 13 (!oldState && dataConnection)
						mapIT.setVisibility(View.VISIBLE);
				} 
			}
		};
		
	}	//--END setNetworks
	
	//=====SECTION 4==SETUP LISTENERS=================================

	//=====SECTION 5==ADD ADAPTER===================================

	//--5.1 loadlist-- Query db and Load Holiday Information----------------------------------------------------
	//--for creating Holiday spinner list---------------------
	public void loadList() {	//--398 setupView
		holidayList.clear();
		listItems.clear();
		questFRP.clear();
		
		//--get all holiday info and holiday instance pass from intent-----
		noHolidayList=app.isHolidayDBEmpty();
		
		if (!noHolidayList || dbRefId==0) {							//--v1.1 check for empty holiday table
			holidayList=app.loadHolidays();							//--return AllHolidays ArrayList
			
			if (hasHoliday && dbRefId !=0) {							//--retreive holiday info, v1.0 hLocation.getRefid()>0, 
				//long tempId=hLocation.getRefid();
				holiday=app.getHoliday(hLocation.getRefid());
			}
	
			//--add "Home, General" to spinner----------------------------------------
			listItems.add(getString(R.string.home_general));
			questFRP.add((long)0);
		
		 	//--Need to do it this way to add "Home, General" to Spinner--
		 	//if(!holidayList.isEmpty()) {									//--v1.0 redundant now since I do check above
		 		for (Holiday h: holidayList) {
		 			questFRP.add(h.getId());
		 			listItems.add(h.getHoliday());
		 		//}
		 	}
		 	addItemsOnSpinner();												//--533 if Holiday Table is not empty
		} else {
			//--empty holiday table, default to home, general-----
			hLocation.setRefid(0);
		}
	}	 //--END 5.1 loadList()
		
	//--5.2 Add Holiday Items To Spinner-----------------------------------------------------------------
	//--Add holiday list to drop down menu for tagging to location--	 
	public void addItemsOnSpinner() {							//--524 loadList
			final ArrayAdapter<String> dataAdapter=new ArrayAdapter<String>(
					getActivity(), android.R.layout.simple_spinner_item, listItems);		
			dataAdapter.setDropDownViewResource(R.layout.dropdown_item); //custom to remove radio button
			holidaySpinner.setAdapter(dataAdapter);
			
			if (hasHoliday) {
				int listPosition=questFRP.indexOf(dbRefId);
				holidaySpinner.setSelection(listPosition); 
				//holidaySpinner.setSelection(questFRP.indexOf(hLocation.getRefid())); }
			}
			//Set on ItemSelectedListner
			holidaySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				
				hLocation.setRefid(questFRP.get(position));
				if (position > 0)
					{ holiday =app.getHoliday(hLocation.getRefid()); }
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
			
		}); 	//--END addItemsOnSpinner
	} 	//--END 5.2 addItemsOnSpinner()

	//=====SECTION 5==ADD ADAPTER===================================
	
	//=====SECTION 6==SET LISTENERS===================================
	
	//-- To be implemented to store file directory here and filename will go into new db tables
	//-- along with address or geo info	
	private void setListeners() {
		
		useThis.setOnClickListener(new useThisOnClick());								//--626
		radioGroup.setOnCheckedChangeListener(new IndoorRadioGroupChecked());	//--695
		locUpdate.setOnClickListener(new continueListenForLocaiton());		//--641

		//--change location date
		locationDate.setOnClickListener (new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				//Log.d(TAG,"552 onClick locationDate onClickListerer");
				changeLocationDate();																	//--605
			} 			
		});		//--END locationDate
		
		//--Call mapLocationClick  launch MapLocation fragment
		mapIT.setOnClickListener(new OnClickListener() {	

			@Override
			public void onClick(View arg0) {
				mapLocationClick();																		//--667
			} 			
		});		//--END mapIT		


	}	//END setListeners
	
	
	//=====SECTION 6==SET LISTENERS===================================

	//=====SECTION 7==LISTENER METHODS================================

	
	//--Part 1 of 2 -- call change date dialog fragment to show dialog with preset date-----------------
	public void changeLocationDate() {
		
		//--call date dialog fragment for date change dialog
		DateDialogFragment ddf = DateDialogFragment.newInstance(getActivity(), 0, lDate.getCalDate());
		ddf.setDateDialogFragmentListener(this);
		DialogFragment newFragment = ddf;
		newFragment.show(getFragmentManager(), "DatePicker");	
	}
	
	//--Part 2 of 2 -- equivalent to OnActivityResult I guess
	//--result from DateDialogFragment callback, i.e. calling back this fragment to give 
	//--the new date selected on the datePickerDialog.
	@Override
	public void dateDialogFragmentDateSet(Calendar date, int sWhichDate) {
		lDate.setCalDate(date);
		String todayDate=lDate.getStrDate();		
		locationDate.setText(((TravelLiteActivity) getActivity()).displayShortDMY(todayDate));		
	}
	
	//--Use Address Found-------------------------------------------------------------------------------
	//--Part 1 of 2 - use address found from reverse geo coding, async task below-------
	private class useThisOnClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			setAddressToLocation(address);															//--v2, findAddress return address
			locationAddress.setText(useAddress.toString());
			if (address == null)
				locationInfo.setText(R.string.no_new_update_);
		}
	}
	
	//--Part 2 of 2 - show button for Stop/Resume location listener--------------------------
	private class continueListenForLocaiton implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (!stopListen) {
				stopListen=!stopListen;
				locUpdate.setText(R.string.resume_location_updates);
				updateAdd=false;
				locationManager.removeUpdates(locationListener); 						//--stop location listening
			} else {
				stopListen=!stopListen;
				locUpdate.setText(R.string.stop_location_updates);
				updateAdd=true;
				locationManager.requestLocationUpdates(										//--resume location listening
						locationProvider, 
						LISTEN_TIME, 
						LISTEN_DISTANCE, 
						locationListener);
			}
		}
	}
	
	
	//--alternative address and location search via google map-------------------------------
	//--Google Map onClick Listener--
	//--onActivityResult at 3.4 for Intent (not applicable for fragment)
	//-- see ln 722 for interface callback 
	public void mapLocationClick() {																//--call from 568
		
		// TODO Auto-generated method stub
	
		//--setters for information before push to the mapLocation fragment
		/*act.setCallForResult(true);
		act.setRequestCode(MAP_LOCATION);													//--fragment setter		
		act.setResult(false);				*/																//--reset result code
		
		Bundle mapBundle = new Bundle();
		mapBundle.putInt(REQUESTOR, LOCATION_ADD);
		if(null != app.currentCountry())
			mapBundle.putString(COUNTRY, app.currentCountry());

		if (null != locationName.getText().toString())
			mapBundle.putString(FIND_LOCATION, locationName.getText().toString()+","+app.currentCountry());

		if (locationAvailable) {
	    //if (cLatitude !=0 && cLongitude !=0) {
	    	mapBundle.putDouble(LOC_LATITUDE, cLatitude);
	    	mapBundle.putDouble(LOC_LONGITUDE, cLongitude);
	    	mapBundle.putBoolean(HAS_LOCATION, true);
	    } else {
	    	mapBundle.putBoolean(HAS_LOCATION, false);
	    }
	    
	    setTargetFragment(this, MAP_LOCATION);
	    MapLocation mapLocation = new MapLocation();
	    Log.d(TAG,"721 mapLocationClick() before commit()");
	    ((FragMain)getActivity()).callMapLocation(mapBundle, mapLocation);
		
	}	//--END mapLocationClick()

	//--Location Selector for Indoor/Outdoor------------------------------------------------------
	//--Part 1 of 3
	//--setup for coarse if indoor, only use gps if outdoor and fine----------------
	private class IndoorRadioGroupChecked implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			if (rb0.isChecked()) {
	        	indoor=true;																					//--indoor selected (default)
	        	locationProvider=LocationManager.NETWORK_PROVIDER;
	        	SharedPreferences.Editor editor=prefs.edit();
	        	editor.putBoolean("indoor", true).commit();
	        } else {
	        	indoor=false;																					//--outdoor selected
	        	if (mapAccurMode) {
	        		checkGPSAvail();
	        		locationProvider=locationManager.getBestProvider(criteria,true);
		        	SharedPreferences.Editor editor=prefs.edit();
		        	editor.putBoolean("indoor", false).commit();	        		
	        	}
	        }		
			
			if (!stopListen) {
			locationManager.requestLocationUpdates(										//--resume location listening
					locationProvider, 
					LISTEN_TIME, 
					LISTEN_DISTANCE, 
					locationListener); }
		}
	}	
	
	
	//--Part 2 of 3---ask to turn on GPS if not already on---------------------------
	private void checkGPSAvail() {
			//locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    	gpsAvail = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	    	
	    	if (!gpsAvail) {
		    	turnOnGPS= new AlertDialog.Builder(getActivity())
		    	.setIcon(android.R.drawable.ic_menu_mylocation)
		    	.setTitle(R.string.device_gps_not_activated)
		    	.setMessage(R.string.for_outdoor_and_most_accurate_location_detection_please_turn_on_gps_ +
		    			R.string._note_it_may_takes_awhile_for_the_first_location_fix_)
				.setPositiveButton(R.string.settings, new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						enableLocationSettings();
					  }
				})
				.setNegativeButton(R.string.skip, new AlertDialog.OnClickListener() {
					  @Override
					public void onClick(DialogInterface dialog, int whichButton) {
					    turnOnGPS.cancel();
					  }
				}).create();
				turnOnGPS.show();
		    }
	}
	
	//--Part 3 of 3
	//--if gps not On, for outdoor and accuracy_fine, launch setting for user to enable gps----
	private void enableLocationSettings() {
		
		//--launch phone settings, location and security for use to turn on gps-------------
	    Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	    startActivity(settingsIntent);
	    //locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    
	}

	//--OnActivityResult return from Map Location()----------------------------------------------------
	//--Google Map onClick Listener onActivityResult, callback 3.3--
	//--Resultcode -1 is RESULT_OK, 0 is RESULT_CANCELED, return caddress
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode==REQUEST_CHOOSE_ADDRESS) {
			if (resultCode==RESULT_OK) {
			
				address = data.getParcelableExtra(MapLocation.ADDRESS_RESULT);
			
				//--capture location details if address not null--					
				if (null!=address) {
					locationAvailable=true;
					cLatitude=address.getLatitude();
					cLongitude=address.getLongitude();
	
					String checkAddress=((TravelLiteActivity) getActivity()).add2String(address);  //--test for null before adding to useaddress

					if (checkAddress != null) {
						addressAvailable=true;
						useAddress=checkAddress;
						
						//-turn off location update and listener, hide button
						updateAdd=false;																			//--stop location manager update
						locationManager.removeUpdates(locationListener); 					//--stop location listening
				        locUpdate.setVisibility(View.INVISIBLE);

				        //--display the location information for this address
						locationString = String.format(
								"Latitude %f \nLongitude %f ",
								cLatitude,
								cLongitude);
						locationInfo.setText(locationString);
				        
						setAddressToLocation(address);			//--v2
						locationAddress.setText(useAddress.toString());
					} else {
						hLocation.setLatitude(address.getLatitude());
						hLocation.setLongitude(address.getLongitude());
					}
					changesPending=true;		
				}
			} else if (resultCode==RESULT_CANCELED) {
				//--MUST HAVE THIS IF RESULT_CANCELED, ELSE BACK KEY WILL CRASH--
			}
		} else if  (requestCode==SETTINGS_CHANGE) { 
			if (resultCode==RESULT_OK) {
				mapAccurMode=prefs.getBoolean("mapAccurMode", true);
				if (mapAccurMode) {
					criteria.setAccuracy(Criteria.ACCURACY_FINE);
				} else {
					criteria.setAccuracy(Criteria.ACCURACY_COARSE);		
				}
			} else if (resultCode==RESULT_CANCELED) { } //MUST HAVE
		} else{
			super.onActivityResult(requestCode, resultCode, data);
		}
	}		//--END 3.4 onActivityResult 	


	//--callback from MapLocation. test in setupView for requestCode MAP_LOCATION 
	//--and resultCode 1 success, display new address information
	public void displayAddress() {
		// TODO Auto-generated method stub
		if (address != null) {		//--(address !=null)
			Log.d(TAG, "834 display Address");
			//cLatitude 	= address.getLatitude();
			//cLongitude	= address.getLongitude();

			String checkAddress=((TravelLiteActivity) getActivity()).add2String(address);  //--test for null before adding to useaddress
	
			if (checkAddress != null) {
				addressAvailable=true;
				useAddress=checkAddress;
				
				//-turn off location update and listener, hide button
				updateAdd=false;																					//--stop location manager update
				locationManager.removeUpdates(locationListener); 							//--stop location listening
		        locUpdate.setVisibility(View.INVISIBLE);
	
		        //--display the location information for this address
				locationString = String.format(
						"Latitude %f \nLongitude %f ",
						cLatitude,
						cLongitude);
				locationInfo.setText(locationString.toString());
		        
				setAddressToLocation(address);			
				locationAddress.setText(useAddress.toString());
				if (address.getFeatureName() != null) {
					locationName.setText(address.getFeatureName().toString());
				} else {
					locationName.setText("");
				}
			} 
		}
	}		//--END displayAddress


	//--Save New Location------------------------------------------------------------------------------
	//--SAVE Location onClick Listener onActivityResult--	
	public void saveNewLocationClick(View v) {
		
			if (locationName.length()>0)	{
				addLocation();				//--directly below
			} else {
				((TravelLiteActivity) getActivity()).showOkAlertDialog(getActivity().getString(R.string.please_enter_location_name));
			}
		
	} 	//--END 4.4 saveNewLocationClick()
			 
	//--from above - Add New Location to db------------------------------------------------------------------------------
	//--Insert location object called from--
	protected void addLocation() {

		/* 
		 * DATE STRING as ISO8601 strings ("YYYY-MM-DD HH:MM:SS.SSS")
		 * 
		 * Map location info below at onActivityResult method below
		 * dbAddress, dbLatitude, dbLongitude, dbInfo in MapLocation
		 * 
		 * ALERT: may have problem storing double to result long
		 */
	
		if (null == hLocation.getCountry() || hLocation.getCountry().isEmpty()) {
			if (app.currentCountry() != "unknown")
				//Log.d(TAG,"888 fatt tah... get country is "+app.currentCountry());
				hLocation.setCountry(app.currentCountry().toString());
		}
		//Log.d(TAG,"891 sweet... location country is set to "+hLocation.getCountry().toString());
		hLocation.setName(locationName.getText().toString());
		hLocation.setLdate(lDate.getStrDate().toString());
		hLocation.setAddress(locationAddress.getText().toString());
		/*String locationString = String.format(		//v1.0 I think this crash add when no location found
				"Latitude %f \nLongitude %f \nAccuracy +/- %.4fm",
				currentLocation.getLatitude(),
				currentLocation.getLongitude(),
				currentLocation.getAccuracy());*/
		/*if (currentLocation !=null) {
			String accuString = String.format(	"+/- %.4fm", currentLocation.getAccuracy());
			hLocation.setInfo(accuString.toString());
		} else {
		}*/
			
		hLocation.setInfo(" "); 	//IMPORTANT: HOURS OF NULL EXCEPTION on dbInfo
		
		if(locationAvailable) {
			hLocation.setLatitude(cLatitude);
			hLocation.setLongitude(cLongitude);
			editor.putLong("cLatitude", Double.doubleToRawLongBits(cLatitude));
			editor.putLong("cLongitude", Double.doubleToRawLongBits(cLongitude));
			editor.commit();
		}

		long addNew=app.insertHLocation(hLocation);			//--watch out for this 
		app.backupManager.dataChanged();
		hLocation.setId(addNew);
		
		if (addNew>0){
			Intent intent =  new Intent();
			intent.putExtra(LOC_ROWID, addNew);
			getActivity().setResult(Activity.RESULT_OK, intent);
		} else {
			Intent intent = new Intent();
			getActivity().setResult(Activity.RESULT_CANCELED, intent);
		}
		
		getFragmentManager().popBackStackImmediate();				//--Close fragment and return	

	}	//--END 7.4
	
	
	
	//=====SECTION 7==LISTENER METHODS================================

	//=====SECTION 8==HELPER METHODS==================================
	
	//--Find Location Via Location Listener---------------------------------------------------------------
	//--method MyLocationListener(), listens for location change and update
	//--continue findAddress() thread to reverse geocode address.	
	private void findMyLocation() {			//--247 onStart()
		
		Log.d(TAG,"927 findMyLocation");
		String test = "";
		if (address != null) {
			if (address.getMaxAddressLineIndex() > 0)
				test =  address.getAddressLine(0).toString();
		Log.d(TAG, "932 caddress test from mCallback "+test.toString());		
		}

		//--Need to tweak a more elegant messaging on No tagging to holiday
		//--Setup location manager and location listener--
		locationManager=(LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);		
		//gpsAvail=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);		

		//--set criteria--------------------------------------------------------------------------
		criteria=new Criteria();
		if (mapAccurMode) {
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
		} else {
			criteria.setAccuracy(Criteria.ACCURACY_COARSE);		
		}
		
		//Log.d(TAG,"accuracy high "+mapAccurMode+" , indoor "+indoor);
		//--SETUP LOCATION LISTENING CRITERIA------------4 scenarios------------------------
		//--1. accuracy high and outdoor 
		if (mapAccurMode && !indoor) {
			checkGPSAvail();			//-724
			gpsAvail = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (gpsAvail) {
				locationProvider = LocationManager.GPS_PROVIDER;
				Log.d(TAG, "975 scenario 4 high accuracy outdoor with gps ");
			} else {
				locationProvider=locationManager.getBestProvider(criteria,true);
				Log.d(TAG, "978 scenario 4 high accuracy outdoor no gps, best effort ");
			}
		} 
		
		
		//--2. accuracy low and outdoor, has network coverage
		else if (!mapAccurMode && !indoor && hasCoverage) {							//--outdoor, no cell/network coverage						
			locationProvider=locationManager.getBestProvider(criteria,true);
			Log.d(TAG, "958 scenario 2 low accuracy outdoor has coverage ");
		}
		
		//--3. accuracy low and outdoor, no network coverage
		else if (!mapAccurMode && !indoor && !hasCoverage) {
			checkGPSAvail();
			gpsAvail = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);	
			locationProvider=locationManager.getBestProvider(criteria,true);
			Log.d(TAG, "966 scenario 3 low accuracy outdoor no coverage, gps status "+gpsAvail);
		}
		
		//--4. indoor and hascoverage
		else if (indoor && hasCoverage) {				//--indoor and accuracy low use network
			locationProvider = LocationManager.NETWORK_PROVIDER;
			Log.d(TAG, "952 scenario 1 indoor has coverage ");
		} 
		
		//--5. default best effort
		else {
			locationProvider=locationManager.getBestProvider(criteria,true);
			Log.d(TAG, "985 scenario 5 default best effort ");
		}
		
		//--no coverage and indoor
		//--manually enter all information------------
		
		//locationProvider=locationManager.getBestProvider(criteria,true);
		locationListener=new MyLocationListener();	//--see 4.6 & 4.7
		
		//--request updates, currently LISTEN_TIME 30 secs, LISTEN_DISTANCE 200m
		locationManager.requestLocationUpdates(locationProvider, LISTEN_TIME, LISTEN_DISTANCE, locationListener);

		
		//--check for datanetwork connection and prompt user---------------------
		if (!hasCoverage) {
			Toast.makeText(getActivity().getBaseContext(), 
					getString(R.string.note_no_network_available), 
					Toast.LENGTH_SHORT).show();
		} else if (!app.isOnline()) {
			Toast.makeText(getActivity().getBaseContext(), 
					getString(R.string.note_cannot_resolve_address_no_network_available), 
					Toast.LENGTH_SHORT).show();
		}
		
				
		//--display message to begin searching for location-------------
		if (!locationAvailable)  {
			locationInfo.setText(getString(R.string._location_information_not_available));
		}
		
	}	//--END findLocation
	

	//--Set Location and Address field from Address Found---------------------------------------	
	public void setAddressToLocation(Address saddress) {
		
		Address tempAdd=saddress;		
		String addressStr = ((TravelLiteActivity) getActivity()).add2String(address);
		hLocation.setAddress(addressStr.toString());		  //--address string from add2String
		
		if (tempAdd.getCountryName() != null) 
			hLocation.setCountry(tempAdd.getCountryName().toString());
		if (tempAdd.getPostalCode() !=null)	
			hLocation.setPostal(tempAdd.getPostalCode().toString());
		if (tempAdd.getThoroughfare() !=null)
			hLocation.setStreet(tempAdd.getSubThoroughfare()+", "+tempAdd.getThoroughfare());
		if (tempAdd.getLocality() !=null)
			hLocation.setCity(tempAdd.getLocality());
		if (cLatitude !=0 && cLongitude !=0) {
			hLocation.setLatitude(cLatitude);
			hLocation.setLongitude(cLongitude);
		}
		
		//--set default name if location name is empty----------------
		if (locationName.length()==0 && tempAdd.getFeatureName() !=null && tempAdd.getSubThoroughfare() !=null) 
		{	
			if (!tempAdd.getFeatureName().toString().
					equalsIgnoreCase(tempAdd.getSubThoroughfare().toString())) {
				StringBuilder locName=new StringBuilder().append(tempAdd.getFeatureName());
				if (tempAdd.getLocality() !=null)
					locName.append(", ").append(tempAdd.getLocality().toString());
				locationName.setText(locName.toString());
			} else {
				locationName.setHint(R.string.please_enter_location_name );
			}
		}
	}		//--END setAddressToLocation




 	//--Location Listener Inner Class-----------------------------------------------------------------
	//--Called from onResume() locationManager.request --
	class MyLocationListener implements LocationListener {
		boolean checkWhichBetter;
		long currentTime;
		
		//--Upon location information acquired--
		@Override
		public void onLocationChanged(Location location) {

			currentTime=System.currentTimeMillis();
			//Log.d(TAG,"1068 onLocationChanged, which is good");
			
			//--if no current locationavailable--------------
			if (currentLocation==null && location !=null) {
				currentLocation		=location;
				updateStatus();		//--1131
			} else{
			
				//--check which is better before update--------
				//--isBetterLocation method in TravelLiteActivity, current timeDelta set to 2 mins--
				boolean whichBetter=((TravelLiteActivity) getActivity()).isBetterLocation(location, currentLocation);  //--1653
				
				//--use if new location is better------------------
				if (whichBetter) {
					lastLocation=currentLocation;
					currentLocation=location;
					updateStatus(); //--look 5 lines down below				
				}
			}
			
			long timeDelta=currentTime - currentLocation.getTime();
			boolean stillCurrent=timeDelta < TIME_LAPSE;		//location is less than two minutes ago
			
			if (locationAvailable && stillCurrent) {
				criteria.setAccuracy(Criteria.ACCURACY_FINE);
				locationProvider=locationManager.getBestProvider(criteria,true);
				locationManager.requestLocationUpdates(locationProvider, LISTEN_TIME, LISTEN_DISTANCE, locationListener);
			} 
			
		}

		@Override
		public void onProviderDisabled(String provider) { 
			locationManager=(LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);		
			gpsAvail=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		}

		@Override
		public void onProviderEnabled(String provider) { 
			locationManager=(LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);		
			gpsAvail=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) { 
			locationManager=(LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);	
			if (gpsAvail && !indoor && locationAvailable) {
				criteria.setAccuracy(Criteria.ACCURACY_FINE);
				locationProvider=locationManager.getBestProvider(criteria,true);
				locationManager.requestLocationUpdates(locationProvider, LISTEN_TIME, LISTEN_DISTANCE, locationListener);
			} 
			
			if (!gpsAvail && locationAvailable) {
				criteria.setAccuracy(Criteria.ACCURACY_FINE);
				locationProvider=locationManager.getBestProvider(criteria,true);		
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LISTEN_TIME, LISTEN_DISTANCE, locationListener);
			}
			
			if(!locationAvailable) {
				criteria.setAccuracy(Criteria.ACCURACY_COARSE);
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LISTEN_TIME, LISTEN_DISTANCE, locationListener);				
			}

		}
		
		public void updateStatus() {		//--1057
						
			locationAvailable=true;
			if (updateAdd) {			//--once usethis is accepted no more find address
				cLatitude=currentLocation.getLatitude();
				cLongitude=currentLocation.getLongitude();
				cAccuracy=currentLocation.getAccuracy();
				app.setDefaultLat(cLatitude);					//-- write to prefs defLat, used by MapLocation
				app.setDefaultLong(cLongitude);			//-- write to prefs defLong
				//--check data network available for geocoder---------------------
				if (app.isOnline()) { 
					//findAddress();	 	//--4.6 runnable thread to find address
					//---FIND ADDRESS CALL
					try {
						address=new goFindAddress().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, currentLocation).get();
					} catch (InterruptedException e) {						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
		
				//--previously locationNotes
				/*Toast.makeText(getBaseContext(), "Location Information Found: \nLatitude: "+
							cLatitude+"\nLongitude: "+
							cLongitude,Toast.LENGTH_SHORT).show();*/
				locationString = String.format(
						"Latitude %f \nLongitude %f \nAccuracy +/- %.2fm",
						currentLocation.getLatitude(),
						currentLocation.getLongitude(),
						currentLocation.getAccuracy());
				locationInfo.setText(locationString);
				
				if(locationAvailable)
					howAccurate=(int)currentLocation.getAccuracy();
					if (howAccurate <50) {
						criteria.setAccuracy(Criteria.ACCURACY_FINE);
					}
				
				//--display location updates stop/resume button, display message---------
				locUpdate.setVisibility(View.VISIBLE);
				locUpdate.setText(R.string.stop_location_updates);
			}
		}
		
	}	//END LOCATIONLISTENER()

	//--Location Update selection toggle-----------------
	public void locationUpdate() {
		if (!stopListen) {
			stopListen=!stopListen;
			actionText=getString(R.string.update);
			locUpdate.setText(R.string.resume_location_updates);
			updateAdd=false;
			locationManager.removeUpdates(locationListener); 	//--stop location listening
		} else {
			stopListen=!stopListen;
			actionText=getString(R.string.stop);
			locUpdate.setText(R.string.stop_location_updates);
			updateAdd=true;
			locationManager.requestLocationUpdates(					//--resume location listening
					locationProvider, 
					LISTEN_TIME, 
					LISTEN_DISTANCE, 
					locationListener);
		}
	}
	
	
	//=====SECTION 8==HELPER METHODS==================================

	//=====SECTION 9==MENU AND DIALOG METHODS===========================
	//--menu choice-----------------------------------------------------------------------------------------------------
	
	//--part 1 of 4, when menu button is pressed in SECTION 1 onCreateOptionsMenu

	//--part 1.1 of 4, option to remove and add new menu to default
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		
		menu.removeItem(9);						//--remove calculator
		
		/*if (currentLocation != null) {
			geoDist=app.getGeoFence()/1000;
			menu.removeItem(4);
			MenuItem mnu4=menu.add(0, 4, 4, R.string.locations_within_+geoDist+"km");
			{ mnu4.setIcon(android.R.drawable.ic_menu_myplaces); }
		}*/
	}
	
	//--part 2 of 4, when menu item is selected in SECTION 1 onOptionsItemSelected
	//--Return menu choice on menu selected
	
	//--part 3 of 4, called up the menu when part 1 menu button is pressed 
	//--as for context menu that is register with listview, 
	//--onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
	private void CreateMenu(Menu menu) {
		
		/*MenuItem mnu1=menu.add(0, 1 , 1, actionText);
			{ mnu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}*/
		
		MenuItem mnu2=menu.add(0, 2, 2, R.string.add);
			{ mnu2.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}		
		
		/*MenuItem mnu3=menu.add(0, 3, 3, R.string.tips);
		{ mnu3.setIcon(android.R.drawable.ic_menu_info_details); }		*/
		
	}
	
	//--part 4 of 4, execute the menu on item selection in part 2
	private boolean MenuChoice(MenuItem item)
	{
		switch (item.getItemId()) {
		case 1:
			//--stop/resume location updates
			//locationUpdate();
			return true;
			
		case 2:
			//--add location
			if (locationName.length()>0)	{
				addLocation();				//-- 4.4
			} else {
				((TravelLiteActivity) getActivity()).showOkAlertDialog(getActivity().getString(R.string.please_enter_location_name));
			}
			return true;
			
		case 3:
			Intent  lbs=new Intent(getActivity(), SimpleHelp.class);
			lbs.putExtra("helpPage", "locationfaq.html");
			startActivity(lbs);
			return true;
			
		case 4:
			app.setGeoFence(2000);		//5km radius of current location
			Intent locationView=new Intent(getActivity(), LocationView.class);
			Bundle hextras=new Bundle();
			hextras.putBoolean(FILTER_LOCATION, true);
			hextras.putInt(FILTER_TYPE, FILTER_GEO);
			hextras.putLong(HOL_REFID, 0);
			locationView.putExtra("MyLocation", currentLocation);
			locationView.putExtras(hextras);
			startActivity(locationView);
			return true;
		}
		return false;
	}
	
	
	//=====SECTION 9==MENU AND DIALOG METHODS===========================

	//=====SECTION 10==THREAD AND ASYNCTASK METHODS=======================

	//--reverse geocoding to find address from current location--------------------------------
	//--called from 1127 updateStatus, inturn called from location listener onChanged
	private class goFindAddress extends AsyncTask <Location, Void, Address> {

		@Override
		protected Address doInBackground(Location... params) {
			Location foundLocation=params[0];
			double thisLatitude=foundLocation.getLatitude();
			double thisLongitude=foundLocation.getLongitude();
			Address newAddress;
			
			try {
				caddresses=geocoder.getFromLocation(thisLatitude, thisLongitude, 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (caddresses != null && caddresses.size() >0) { 
				newAddress=caddresses.get(0);			
				return newAddress;
			} else {
				return null;
			}
		}

		@Override
		protected void onPostExecute(Address foundAddress) {
			if(foundAddress !=null && updateAdd) {	
				
				//--do not toast when the two locations is close by, within 10m CLOSE_BY
				boolean tooNear=false;
				if (lastLocation != null)
					tooNear=((TravelLiteActivity) getActivity()).locationCloseBy(lastLocation, currentLocation, CLOSE_BY);
				
				//--------------------------------------------------
				useAddress=((TravelLiteActivity) getActivity()).add2String(foundAddress);
				String checkEmpty=useAddress.replaceAll("\\s", "");		//trim all space to check for empty address
				if (useAddress != null && checkEmpty.length() > 0) {
					useThis.setVisibility(View.VISIBLE);		//if only add2String do not return null
					addressAvailable=true;							//set to true only if country and postalcode are not empty
					//useThis.startAnimation (
					//		AnimationUtils.loadAnimation(context, R.anim.cycler));
					
					if (locationAddress.toString().isEmpty()) {
						locationAddress.setText(R.string.new_address_found_+useAddress);
					} else {
						if (!tooNear)
							Toast.makeText(getActivity().getApplicationContext(), getString(R.string.location_address_found_)+useAddress.toString()
							+getString(R.string._please_click_address_found_to_use_this_address), 
							Toast.LENGTH_SHORT).show();
					}
				} else {
					useThis.setVisibility(View.INVISIBLE);	
				}		
			}
		}
	}	//--END goFindAddress()



	//=====SECTION 11==SANDBOX======================================
	
	//--Runnable Thread FindAddress Method----------------------------------------------------
	//--check http://developer.android.com/training/basics/location/geocoding.html 
	//--for Async task with better message handling--------
	//--OBSOLETED Replaced by 1372 goFindAddress AsyncTask method 
	public void findAddress() {
		
		handler=new Handler();
		
		//----------------------------------------------------------------------------------------------------
		final Runnable runOnUIThread=new Runnable() {
				@Override
				public void run() {
					try {
						_showInUI();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
		};
		
			
		Thread thisthread =new Thread()
		{				
				@Override
				public void run() {
					_doInBackgroundPost();
					handler.post(runOnUIThread);		//handle post result in the queue upon completes
				}
		};
		thisthread.setName("addressSearcher");
		thisthread.start();

	
		//--old thread without thread name--
		/*new Thread() {								
				@Override 
				public void run() {					
					_doInBackgroundPost();
					handler.post(runOnUIThread);		//handle post result in the queue upon completes
				}
		}.start();		 */
		
	}
	//----------------------------------------------------------------------------------------------------------
	
	private void _doInBackgroundPost() {
		
			//Attempt to get Location Address from co-ordinates
			//--different from EditLocation as dbLatitude and dbLongitude from db
			//--is not yet available
			try {
				caddresses=geocoder.getFromLocation(cLatitude, cLongitude, 1);				
			} catch (IOException e) {
				//Message.obtain(handler, 1, e.toString()).sendToTarget();
				e.printStackTrace();
			}
			if (caddresses != null && caddresses.size() >0) { 
				address=caddresses.get(0);
				//Message.obtain(handler, 1, useAddress).sendToTarget(); //
				//if (address !=null) {addressAvailable=true;}  //old code
			}		
	}

	private void _showInUI() {
		if(address !=null && updateAdd) {	
					
				//--------------------------------------------------
				useAddress=((TravelLiteActivity) getActivity()).add2String(address);
				String checkEmpty=useAddress.replaceAll("\\s", "");		//trim all space to check for empty address
				if (useAddress != null && checkEmpty.length() > 0) {
					useThis.setVisibility(View.VISIBLE);		//if only add2String do not return null
					addressAvailable=true;							//set to true only if country and postalcode are not empty
					
					if (locationAddress.toString().isEmpty()) {
						locationAddress.setText(R.string.new_address_found_+useAddress);
					} else {
						Toast.makeText(getActivity().getApplicationContext(), R.string.location_address_found_+useAddress.toString()
						+R.string._please_click_address_found_to_use_this_address, 
						Toast.LENGTH_SHORT).show();
					}
				} else {
					useThis.setVisibility(View.INVISIBLE);	
				}
				//--------------------------------------------------				
		}
		
	}//END findLocationAddress()	
	
} //--END MAIN CLASS	






