/*
 * Activity: AddLocation.java
 * Description:
 * MAIN program add location action activity capturing locations, gpx info, picture
 * and tagging to holiday.
 * 
 * Last updated: September 6, 2012
 * 
 * Changes:
 * - Tidy up old codes and added setResult return to viewlocation
 * - Changes to mylocation listener algorithms to on status changed and providers changed
 * to enhance location listener.
 *    
 * 
 * 
 */


package com.liseem.android.travel;

import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;

import java.io.IOException;
import java.io.Serializable;
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

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.AdapterView.OnItemSelectedListener;

public class MoveAround extends TravelLiteActivity implements Serializable {
	
	//=====SECTION 1==DECLARATION===================================
	
	private final static String TAG="MoveAround";
	
	protected static final int LOCATIONDATE_DIALOG_ID=0;
	protected static final int REQUEST_CHOOSE_ADDRESS=2;
	protected static final int REQUEST_CHOOSE_MYLOCATION=3;	
	protected static final int TAKE_PICTURE=4;
	protected static final int IMAGE_GALLERY=5;
	protected static final int SETTINGS_CHANGE=6;		
	
	protected final static int	 ADD_LOCATION=103;			//from PPGoPlaces
	public static final String ADDRESS_RESULT ="address";
	
	//--check whether location is current or within accuracy
 	protected static final long CLOSE_BY=20;				//geofence for status update to trigger toast	
 	protected static final long TIME_LAPSE=300000;	//5 mins 5x60000 
	
	//--listen for location
	private static final int LISTEN_FAST=3000;			//15 secs, shld change to 60,000millisecs
	private static final int LISTEN_TIME=10000;			//15 secs, shld change to 60,000millisecs
	private static final int LISTEN_DISTANCE=20;		//50 meters shld change to 200m
	
	private PPGoPlacesApplication 	app;
	private SharedPreferences 			prefs;
	private Context 								context;
	private volatile Thread 				threaker;
	
	//--VIEW SPECIFIC--
	private TextView 							locationDate;
	private TextView 							locationAddress;
	private EditText 							locationName;
	private TextView 							locationInfo;
	private TextView 							locationText;
	private RadioGroup 						radioGroup;
	private RadioButton 						rb0;
	private RadioButton 						rb1;
	private AlertDialog.Builder 			noLocation;	
	private AlertDialog 						foundAddress;	
	private Spinner 								holidaySpinner;
	private ImageButton 						mapIT;							//search via maplocation button
	private ImageButton 						useThis;						//use address found
	private Button								locUpdate;
	private TextView 							emptyList;


	//--DB SPECIFIC--
	private ArrayList<Holiday> 			holidayList;
	private ArrayList<LPicture> 			imagesList;
	private ArrayList<String> 			listItems;	
	private Holiday								holiday;
	private boolean							noHolidayList;
	private HLocation							hLocation;
	private LPicture								oneShot;
	private MyDate								lDate, startDate, endDate;
	private Calendar 							locDate;
	private boolean 							changesPending=false;
	private String 								whoCalling;
	private ArrayList<Long> 				questFRP;	
	private long 									dbRefId;

	//--LOCATION SPECIFIC--GEOCODER
	private List<Address> 					caddresses;	
	private Address 								address; 
	private Address 								caddress;	
	private Geocoder 							geocoder;
	private Handler 								handler;		
	private String 								useAddress;				//string to hold runOnUIThread return
	private Location 							currentLocation;		//new current location
	private Location 							lastLocation;			//previous location
	private boolean							hasHoliday=false;
	private boolean							indoor=true;						
	private AlertDialog						turnOnGPS;
	
	//--LOCATION SPECIFIC--LOCATION MANAGER
	private LocationManager 				locationManager; 	
	private MyLocationListener			locationListener;
	//private LocationListener 				locationListener;		//Sept 1 need to investigate why not the former
	private String 								locationProvider;
	private Criteria 								criteria;
	private double								cLatitude=0;
	private double								cLongitude=0;
	private double								cAccuracy=0;
	private boolean 							locationAvailable=false;
	private boolean 							addressAvailable=false;	
	
	//--check gps availability
	private boolean 							gpsAvail;						//check for gps enabled
	
	//--check network and data availability
	private boolean 							networkAvail;				//check for network enabled
	private boolean							mobileAvail;				//check for mobile enabled
	private boolean							dataConnection;			//data network available, i.e 3G or WiFi
	private boolean							hasCoverage;				//no cell or wifi coverage even for location
	
	private int										howAccurate;				//set criteria to fine if less than 50m-updateStatus
	private boolean 							LocNameExist;
	private boolean 							mapAvail=true;
	private boolean 							gpsInfo;	
	private boolean							updateAdd=true	;		//stop runonui update after user accept address			
	private boolean							stopListen;					//stop/resume location listener
	private boolean 							mapAccurMode;			//set Accuracy option, true FINE and false COARSE
	private boolean							listenMessage;		
	private CheckBox							dontShow;					//checkbox in dialog, do not show in again
	private boolean							skipLocMsg;					//shared pref not show listenForLocation msg again
	private String									locationString;

	//--NETWORK CONNECTIVITY
	private BroadcastReceiver 			networkStateReceiver;
	private ConnectivityManager 		connectivityManager;
	
	
	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_location);

		//getLastNonConfigurationInstance();
		//onRetainNonConfigurationInstance();
		setupView();						//2.1
		setNetworks();					//2.2
		findMyLocation(context);	//3.1
		

	}	//END onCreate()
	
	@Override
	protected void onResume() {
		super.onResume();

		locationManager.requestLocationUpdates(					//resume location listening
				locationProvider, 
				LISTEN_FAST, 
				LISTEN_DISTANCE, 
				locationListener);

		setListeners();				//2.3		
		
		//--listen to network change broadcast receiver----------
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);        
		registerReceiver(networkStateReceiver, filter);
		
	} //END onResume()

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	@Override
	protected void onPause() {
		locationManager.removeUpdates(locationListener); 		//stop location listening
		unregisterReceiver(networkStateReceiver);						//stop listening to broadcast
		super.onPause();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		//--Save vulnerable information--
		outState.putBoolean("locationInfo", locationAvailable);
		outState.putBoolean("updateAdd", updateAdd);
		if(locationAvailable) {
			outState.putDouble("latitude", cLatitude);
			outState.putDouble("longitude",cLongitude);
		}
	}
	
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		//--Restore vulnerable information upon onResume--
		locationAvailable=savedInstanceState.getBoolean("locationInfo");
		updateAdd=savedInstanceState.getBoolean("updateAdd");
		if (locationAvailable) {
			cLatitude=savedInstanceState.getDouble("latitude");
			cLongitude=savedInstanceState.getDouble("longitude");
		}
	}


	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 3==SETUP VIEWS====================================

	private void setupView() {		//onCreate()
		
		//--path to PPGoPlacesApplication------------------------------
		app=(PPGoPlacesApplication)getApplication();	
		
		//--get SharedPreferences object--------------------------------
		prefs=getSharedPreferences(PREFNAME, MODE_PRIVATE);
		indoor=prefs.getBoolean("indoor", true);
		mapAccurMode=prefs.getBoolean("mapAccurMode", false);			//findLocation 2.2
		
			
		//--initialize instances---------------------------------------------
		holidayList=new ArrayList<Holiday>();
		listItems=new ArrayList<String>();
		holiday=new Holiday();
		hLocation=new HLocation();
		imagesList=new ArrayList<LPicture>();
		hLocation.setPicture(false);			//prevent getAllLocation null exception
		hLocation.setHoliday(false);			//prevent getAllLocation null exception

		questFRP=new ArrayList<Long>();			//KIV
		
		//--Initialize Variables and Object--------------------------------
		//this.context=context;
		geocoder=new Geocoder(this);		//for reverse geocoder
		handler=new Handler();						//for location listener background thread
		caddresses=new ArrayList<Address>();
		address=new Address(null);
		caddress=new Address(null);
				
		//--setup views--------------------------------------------------------
		locationDate=(TextView)findViewById(R.id.locDate);
		locationAddress=(EditText)findViewById(R.id.locAdd);
		locationName=(EditText)findViewById(R.id.locName);
		//locationNotes=(TextView)findViewById(R.id.locNotes);
		locationInfo=(TextView)findViewById(R.id.locInfo);
		holidaySpinner=(Spinner)findViewById(R.id.holidaySpinner);
		emptyList=(TextView)findViewById(R.id.emptyList);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
        rb0 = (RadioButton) findViewById(R.id.radio0);
        rb1 = (RadioButton) findViewById(R.id.radio1);
        //--see setNetworks for default radio button setting
        locUpdate=(Button)findViewById(R.id.locUpdate);
        locUpdate.setVisibility(View.INVISIBLE);

		mapIT=(ImageButton)findViewById(R.id.mapIT);
		mapIT.setVisibility(View.INVISIBLE);
		useThis=(ImageButton)findViewById(R.id.useThis);
		useThis.setVisibility(View.INVISIBLE);
		
		holidaySpinner.setEmptyView(emptyList);		//set Spinner to empty textview
		
		//--Holiday RowId pass from ViewHoliday or PPGoPlaces add location--
		Bundle lextras=getIntent().getExtras();
		if (lextras != null) 
		{
			//String dbLocName;
			hasHoliday=lextras.getBoolean(FILTER_LOCATION);
			dbRefId=lextras.getLong(HOL_REFID);
	
			//dbLocName=lextras.getString(LOC_NAME);			//v1.0 obsoleted old menu
			if (hasHoliday)			//v1.0		(dbRefid>0)
				hLocation.setRefid(dbRefId);		//v1.0 .set(lextras.getLong(HOL_REFID));
			/*if (dbLocName != null) {				//v1.0 obsoleted old menu codes
				hLocation.setName(dbLocName);
				locationName.setText(dbLocName.toString());} */
		} else {
			hLocation.setRefid(0);			//hasHoliday false
		}
		//--set focus on locationName field--
		locationName.requestFocus();
		

		//--loadList, if there is holiday--
		loadList();							//4.2
		//v1.0 addItemsOnSpinner();			//see 4.4 move to end of loadList()
		
		lDate=new MyDate();
		lDate.setCalDate(Calendar.getInstance());
		String todayDate=lDate.getStrDate();
		
		locationDate.setText(displayShortDMY(todayDate));
		//displayTodayDate(locationDate);			//helper program from travelliteactivity, too long for textview name
		
	} 	//END 2.1 SetupView

	//==========2.2 Setup Network and Broadcast Receiver=====================
	private void setNetworks() {
		//--setup connectivity and connectivity condition----------------------------------
		//--check for wifi and dataconnection-------------------------------------------		
		networkAvail=PPGoPlacesApplication.hasNetwork(this);
		mobileAvail=PPGoPlacesApplication.hasMobile(this);
		dataConnection=app.isOnline();
		hasCoverage=PPGoPlacesApplication.hasCoverage(this);
		//Log.d(TAG, "setupView(), is network available "+networkAvail+", is mobile available "+mobileAvail);
		//Log.d(TAG, "is dataConnection available "+dataConnection+", coverage "+hasCoverage);
		//Log.d(TAG, "indoor "+indoor);
		
		//--display Google map only if Geocoder is present
		if (app.isOnline()) { 
			mapIT.setVisibility(View.VISIBLE);
		}		
		
		//--set default to indoor if hasCoverage is true, for quicker discovery-----
        if (hasCoverage) {
        	rb0.setChecked(true);
        	indoor=true;
        } else if (indoor) {
        	rb0.setChecked(true);
        } else {
        	rb1.setChecked(true);
        }
        
        
		
		//--setup network broadcast listener--------------
		networkStateReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				boolean oldState=dataConnection;
				
				networkAvail=PPGoPlacesApplication.hasNetwork(context);
				mobileAvail=PPGoPlacesApplication.hasMobile(context);
				dataConnection=app.isOnline();				
				hasCoverage=PPGoPlacesApplication.hasCoverage(context);
				
				//--geocode for address if it is not already found---------------
				//--too many folks finding address!!!
				if (dataConnection && locationAvailable && !addressAvailable) {
					//---FIND ADDRESS CALL
					try {
						address=new goFindAddress().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, currentLocation).get();
					} catch (InterruptedException e) {	
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
				
				//--if data network no longer exist---------------------------------
				if (oldState && !dataConnection) {
						Toast.makeText(getBaseContext(), 
								"Data network is no longer available...", 
								Toast.LENGTH_SHORT).show();
						
						mapIT.setVisibility(View.INVISIBLE);
				} 
			
				if (!oldState && dataConnection) {
						mapIT.setVisibility(View.VISIBLE);
				} 
			}
		};
		
	}
	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================

	//--4.1 loadlist-- Query db and Load Holiday Information----------------------------------------------------
	//--for creating Holiday spinner list---------------------
	public void loadList() {
		holidayList.clear();
		listItems.clear();
		questFRP.clear();
		
		//--get all holiday info and holiday instance pass from intent-----
		noHolidayList=app.isHolidayDBEmpty();
		
		if (!noHolidayList) {			//v1.1 check for empty holiday table
			holidayList=app.loadHolidays();	//return AllHolidays ArrayList
			
			if (hasHoliday) {			//retreive holiday info, v1.0 hLocation.getRefid()>0, 
				//long tempId=hLocation.getRefid();
				holiday=app.getHoliday(hLocation.getRefid());
			}
	
			//--add "Home, General" to spinner----------------------------------------
			listItems.add(getString(R.string.home_general));
			questFRP.add((long)0);
		
		 	//--Need to do it this way to add "Home, General" to Spinner--
		 	//if(!holidayList.isEmpty()) {		//v1.0 redundant now since I do check above
		 		for (Holiday h: holidayList) {
		 			questFRP.add(h.getId());
		 			listItems.add(h.getHoliday());
		 		//}
		 	}
		 	addItemsOnSpinner();		//if Holiday Table is not empty
		} else {
			//--empty holiday table, default to home, general-----
			hLocation.setRefid(0);
		}
	}	 //END 4.3 loadList()
		
	//--4.2 Add Holiday Items To Spinner-----------------------------------------------------------------
		//--Add holiday list to drop down menu for tagging to location--	 
	public void addItemsOnSpinner() {
			final ArrayAdapter<String> dataAdapter=new ArrayAdapter<String>(
					this, android.R.layout.simple_spinner_item, listItems);		
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
				if (position > 0) {
					holiday =app.getHoliday(hLocation.getRefid()); 
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
			
		}); 	//END setOnItemSelectedListener statement
	} 	//END 4.3 addItemsOnSpinner()

	//=====SECTION 4==ADD ADAPTER===================================
	
	//=====SECTION 5==SET LISTENER====================================
	
	//-- To be implemented to store file directory here and filename will go into new db tables
	//-- along with address or geo info	
	private void setListeners() {
		mapIT.setOnClickListener(new mapLocationClick());
		useThis.setOnClickListener(new useThisOnClick());
		radioGroup.setOnCheckedChangeListener(new IndoorRadioGroupChecked());
		locUpdate.setOnClickListener(new continueListenForLocaiton());


	}	//END 2.3 setListeners
	
	
	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================

	//--Use Address Found-------------------------------------------------------------------------------
	//--Part 1 of 2 - use address found from reverse geo coding, async task below-------
	private class useThisOnClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			setAddressToLocation(address);								//v2, findAddress return address
			locationAddress.setText(useAddress.toString());
			locationInfo.setText("No new update.");
		}
	}
	
	//--Part 2 of 2 - show button for Stop/Resume location listener--------------------------
	private class continueListenForLocaiton implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (!stopListen) {
				stopListen=!stopListen;
				locUpdate.setText("Resume Location Updates");
				updateAdd=false;
				locationManager.removeUpdates(locationListener); //stop location listening
			} else {
				stopListen=!stopListen;
				locUpdate.setText("Stop Location Updates");
				updateAdd=true;
				locationManager.requestLocationUpdates(					//resume location listening
						locationProvider, 
						LISTEN_TIME, 
						LISTEN_DISTANCE, 
						locationListener);
			}
		}
	}
	
	
	//--alternative address and location search via google map-------------------------------
	//--Google Map onClick Listener--
	//--onActivityResult at 3.4 for Intent--
	private class mapLocationClick implements OnClickListener {	
		
		@Override
		public void onClick(View v) {
			Intent intent=new Intent(MoveAround.this, MapLocation.class);
    	    Bundle addextras=new Bundle();
    	    addextras.putString("locAddress", locationName.getText().toString());
    	    if (cLatitude !=0 && cLongitude !=0) {
    	    	addextras.putDouble(LOC_LATITUDE, cLatitude);
    	    	addextras.putDouble(LOC_LONGITUDE, cLongitude);
    	    }
    	    intent.putExtras(addextras);
    	    startActivityForResult(intent, REQUEST_CHOOSE_ADDRESS);
		}
	}	//END 3.3 mapLocationClick()

	//--Location Selector for Indoor/Outdoor------------------------------------------------------
	//--Part 1 of 3
	//--setup for coarse if indoor, only use gps if outdoor and fine----------------
	private class IndoorRadioGroupChecked implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			if (rb0.isChecked()) {
	        	indoor=true;			//indoor selected (default)
	        	locationProvider=LocationManager.NETWORK_PROVIDER;
	        	SharedPreferences.Editor editor=prefs.edit();
	        	editor.putBoolean("indoor", true).commit();
	        } else {
	        	indoor=false;			//outdoor selected
	        	if (mapAccurMode) {
	        		checkGPSAvail();
	        		locationProvider=locationManager.getBestProvider(criteria,true);
		        	SharedPreferences.Editor editor=prefs.edit();
		        	editor.putBoolean("indoor", false).commit();	        		
	        	}
	        }		
			
			if (!stopListen) {
			locationManager.requestLocationUpdates(					//resume location listening
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
		    	turnOnGPS= new AlertDialog.Builder(this)
		    	.setIcon(android.R.drawable.ic_menu_mylocation)
		    	.setTitle("Device GPS Not Activated")
		    	.setMessage("For \"Outdoor\" and most accurate location detection, please turn ON GPS. " +
		    			"\n\nNote: It may takes awhile for the first location fix.")
				.setPositiveButton("Settings", new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						enableLocationSettings();
					  }
				})
				.setNegativeButton("Skip", new AlertDialog.OnClickListener() {
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode==REQUEST_CHOOSE_ADDRESS) {
			if (resultCode==RESULT_OK) {
			
				caddress = data.getParcelableExtra(MapLocation.ADDRESS_RESULT);
			
				//--capture location details if address not null--					
				if (null!=caddress) {
					locationAvailable=true;
					cLatitude=caddress.getLatitude();
					cLongitude=caddress.getLongitude();
	
					String checkAddress=add2String(caddress);  //test for null before adding to useaddress

					if (checkAddress != null) {
						addressAvailable=true;
						useAddress=checkAddress;
						
						//-turn off location update and listener, hide button
						updateAdd=false;			//stop location manager update
						locationManager.removeUpdates(locationListener); 		//stop location listening
				        locUpdate.setVisibility(View.INVISIBLE);

				        //--display the location information for this address
						locationString = String.format(
								"Latitude %f \nLongitude %f ",
								cLatitude,
								cLongitude);
						locationInfo.setText(locationString);
				        
						setAddressToLocation(caddress);			//v2
						locationAddress.setText(useAddress.toString());
					} else {
						hLocation.setLatitude(caddress.getLatitude());
						hLocation.setLongitude(caddress.getLongitude());
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
	}		//END 3.4 onActivityResult 	

	//--Save New Location------------------------------------------------------------------------------
	//--SAVE Location onClick Listener onActivityResult--	
	public void saveNewLocationClick(View v) {
		
			if (locationName.length()>0)	{
				addLocation();				//4.4
			} else {
				showOkAlertDialog("Please Enter Location Name");
			}
		
	} 	//END 4.4 saveNewLocationClick()
			 
	//--4.5 Add New Location to db------------------------------------------------------------------------------
	//--Insert location object called from 3.5--
	protected void addLocation() {

		/* 
		 * DATE STRING as ISO8601 strings ("YYYY-MM-DD HH:MM:SS.SSS")
		 * 
		 * Map location info below at onActivityResult method below
		 * dbAddress, dbLatitude, dbLongitude, dbInfo in MapLocation
		 * 
		 * ALERT: may have problem storing double to result long
		 */
	
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
		}

		long addNew=app.insertHLocation(hLocation);			//watch out for this 
		app.backupManager.dataChanged();
		hLocation.setId(addNew);
		
		if (addNew>0) {
			Intent intent =new Intent();
			intent.putExtra(LOC_ROWID, addNew);
			setResult(RESULT_OK, intent);
		} else {
			Intent intent=new Intent();
			setResult(RESULT_CANCELED, intent);
		}
		finish();				//Close activity and return	

	}	//END 7.4
	
	
	
	//=====SECTION 6==LISTENER METHODS================================

	//=====SECTION 7==HELPER METHODS==================================
	
	//--7.1 Find Location Via Location Listener---------------------------------------------------------------
	//--method 4.4 MyLocationListener(), listens for location change and update
	//--continue 4.5 to findAddress() thread to reverse geocode address.	
	private void findMyLocation(Context context) {

		//--Need to tweak a more elegant messaging on No tagging to holiday
		//--Setup location manager and location listener--
		locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);		
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
		//--1. accuracy high and outdoor--------
		if (mapAccurMode && !indoor) {
			checkGPSAvail();
			gpsAvail = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (gpsAvail) {
				locationProvider = LocationManager.GPS_PROVIDER;
			} else {
				locationProvider=locationManager.getBestProvider(criteria,true);
			}
		} 
		
		//--2. accuracy low and outdoor, has network coverage
		else if (!mapAccurMode && !indoor && hasCoverage) {							//outdoor, no cell/network coverage						
			locationProvider=locationManager.getBestProvider(criteria,true);
		}
		
		//--3. accuracy low and outdoor, no network coverage
		else if (!mapAccurMode && !indoor && !hasCoverage) {
			checkGPSAvail();
			gpsAvail = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);	
			locationProvider=locationManager.getBestProvider(criteria,true);
		}
		
		//--4. indoor and hascoverage
		else if (indoor && hasCoverage) {				//indoor and accuracy low use network
			locationProvider = LocationManager.NETWORK_PROVIDER;
		} else {
			locationProvider=locationManager.getBestProvider(criteria,true);
		}
		
		//--no coverage and indoor
		//--manually enter all information------------
		
		//locationProvider=locationManager.getBestProvider(criteria,true);
		locationListener=new MyLocationListener();	//see 4.6 & 4.7
		
		//--request updates, currently LISTEN_TIME 30 secs, LISTEN_DISTANCE 200m
		locationManager.requestLocationUpdates(locationProvider, LISTEN_TIME, LISTEN_DISTANCE, locationListener);

		
		//--check for datanetwork connection and prompt user---------------------
		if (!hasCoverage) {
			Toast.makeText(getBaseContext(), 
					"No network coverage", 
					Toast.LENGTH_SHORT).show();
		} else if (!app.isOnline()) {
			Toast.makeText(getBaseContext(), 
					"No data network available for resolving address", 
					Toast.LENGTH_SHORT).show();
		}
		
				
		//--display message to begin searching for location-------------
		if (!locationAvailable)  {
			locationInfo.setText("No location information available");
		}
		
	}	//END 3.1 findLocation
	

	//--7.2 Set Location and Address field from Address Found---------------------------------------	
	public void setAddressToLocation(Address address) {
		
		Address tempAdd=address;		
		hLocation.setAddress(useAddress.toString());		  //useAddress from add2String
		
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
				locationName.setHint("Please Enter Location Name" );
			}
		}
	}		//END setAddressToLocation

	//--7.3 Date Change and Date Picker Dialog-------------------------------------------------------------
	//--Change location date routine
	public void changeDateClick(View v) {
		showDialog(LOCATIONDATE_DIALOG_ID);
	}
	
	@Override
	public Dialog onCreateDialog(int id) {
		 switch (id) {
			case LOCATIONDATE_DIALOG_ID:
				return new DatePickerDialog (this,
						locDateSetListener,
						lDate.getCalDate().get(Calendar.YEAR),
						lDate.getCalDate().get(Calendar.MONTH),
						lDate.getCalDate().get(Calendar.DAY_OF_MONTH));
		 }
		 return null;
	 }	
	 
	 private DatePickerDialog.OnDateSetListener locDateSetListener =
				new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year,
				int monthOfYear, int dayOfMonth) {
		Calendar d= Calendar.getInstance();
		d.set(year, monthOfYear, dayOfMonth);
				
		//--setCalDMY(TextView view, String dTitle, Calendar cdate)
		//--Display at view locationDate in dd-MMM-YYYY format--
		lDate.setCalDate(d);
		String thisDate=lDate.getStrDate();
		
		//setCalDMY(locationDate, "New Date: ", locDate);
		locationDate.setText(displayShortDMY(thisDate));
		}
	 }; //END Statement DatePickerDialog.OnDateSetListener


 	//--7.4 Location Listener Inner Class-----------------------------------------------------------------
	//--Called from onResume() locationManager.request --
	private class MyLocationListener implements LocationListener {
		boolean checkWhichBetter;
		long currentTime;
		
		//--Upon location information acquired--
		@Override
		public void onLocationChanged(Location location) {

			currentTime=System.currentTimeMillis();
			
			//--if no current locationavailable--------------
			if (currentLocation==null && location !=null) {
				currentLocation=location;
				updateStatus();
			} else {
			
				//--check which is better before update--------
				//--isBetterLocation method in TravelLiteActivity, current timeDelta set to 2 mins--
				checkWhichBetter=isBetterLocation(location, currentLocation);
				
				//--use if new location is better------------------
				if (checkWhichBetter) {
					lastLocation=currentLocation;
					currentLocation=location;
					updateStatus(); //look 5 lines down below				
				}
			}
			
			long timeDelta=currentTime - currentLocation.getTime();
			boolean stillCurrent=timeDelta < TIME_LAPSE;		//location is less than 5 minutes ago
			
			if (locationAvailable && stillCurrent) {
				criteria.setAccuracy(Criteria.ACCURACY_FINE);
				locationProvider=locationManager.getBestProvider(criteria,true);
				locationManager.requestLocationUpdates(locationProvider, LISTEN_TIME, LISTEN_DISTANCE, locationListener);
			} 
		}

		@Override
		public void onProviderDisabled(String provider) { 
			locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);		
			gpsAvail=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		}

		@Override
		public void onProviderEnabled(String provider) { 
			locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);		
			gpsAvail=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) { 
			locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);	
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
		
		public void updateStatus() {
						
			locationAvailable=true;
			if (updateAdd) {			//once usethis is accepted no more find address
				cLatitude=currentLocation.getLatitude();
				cLongitude=currentLocation.getLongitude();
				cAccuracy=currentLocation.getAccuracy();
				//--check data network available for geocoder---------------------
				if (app.isOnline()) { 
					//findAddress();	 	//4.6 runnable thread to find address
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
				locUpdate.setText("Stop Location Updates");
			}
		}
		
	}	//END 7.4 LOCATIONLISTENER()

	
	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================
	//--menu choice-----------------------------------------------------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		CreateMenu(menu);
		return true;
	}

	//--Return menu choice on menu selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		return MenuChoice(item); 
	}
	
	private void CreateMenu(Menu menu) {
		
		MenuItem mnu1=menu.add(0, 0 , 0, "Settings");
			{ mnu1.setIcon(android.R.drawable.ic_menu_preferences); }
		
		MenuItem mnu2=menu.add(0,  1, 1, "Help");
			{ mnu2.setIcon(android.R.drawable.ic_menu_help); }		
		
		MenuItem mnu3=menu.add(0,  2, 2, "Tips");
		{ mnu3.setIcon(android.R.drawable.ic_menu_myplaces); }		
		
	}
	
	private boolean MenuChoice(MenuItem item)
	{
		switch (item.getItemId()) {
		case 0:
			Intent myPrefs=new Intent (MoveAround.this, MySettings.class);
			startActivityForResult(myPrefs, SETTINGS_CHANGE);
			return true;
		case 1:
			Intent  simplehelp=new Intent(MoveAround.this, SimpleHelp.class);
			simplehelp.putExtra("helpPage", "addlocation.html");
			startActivity(simplehelp);
			return true;
		case 2:
			Intent  lbs=new Intent(MoveAround.this, SimpleHelp.class);
			lbs.putExtra("helpPage", "locationfaq.html");
			startActivity(lbs);
			return true;
		}
		return false;
	}
	
	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//--9.1 Runnable Thread FindAddress Method----------------------------------------------------
	//--check http://developer.android.com/training/basics/location/geocoding.html 
	//--for Async task with better message handling--------
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
				useAddress=add2String(address);
				String checkEmpty=useAddress.replaceAll("\\s", "");		//trim all space to check for empty address
				if (useAddress != null && checkEmpty.length() > 0) {
					useThis.setVisibility(View.VISIBLE);		//if only add2String do not return null
					addressAvailable=true;							//set to true only if country and postalcode are not empty
					
					if (locationAddress.toString().isEmpty()) {
						locationAddress.setText("New Address Found:\n"+useAddress);
					} else {
						Toast.makeText(getApplicationContext(), "Location Address found: \n"+useAddress.toString()
						+"\n\nPlease click \"Address Found\" to use this address", 
						Toast.LENGTH_SHORT).show();
					}
				} else {
					useThis.setVisibility(View.INVISIBLE);	
				}
				//--------------------------------------------------				
		}
		
	}//END findLocationAddress()	

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================
	//--reverse geocoding to find address from current location--------------------------------
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
		protected void onPostExecute(Address result) {
			if(result !=null && updateAdd) {	
				
				//--do not toast when the two locations is close by, within 10m CLOSE_BY
				boolean tooNear=false;
				if (lastLocation != null)
					tooNear=locationCloseBy(lastLocation, currentLocation, CLOSE_BY);
				
				//--------------------------------------------------
				useAddress=add2String(result);
				String checkEmpty=useAddress.replaceAll("\\s", "");		//trim all space to check for empty address
				if (useAddress != null && checkEmpty.length() > 0) {
					useThis.setVisibility(View.VISIBLE);		//if only add2String do not return null
					addressAvailable=true;							//set to true only if country and postalcode are not empty
					
					if (locationAddress.toString().isEmpty()) {
						locationAddress.setText("New Address Found:\n"+useAddress);
					} else {
						if (!tooNear)
							Toast.makeText(getApplicationContext(), "Location Address found: \n"+useAddress.toString()
							+"\n\nPlease click \"Address Found\" to use this address", 
							Toast.LENGTH_SHORT).show();
					}
				} else {
					useThis.setVisibility(View.INVISIBLE);	
				}		
			}
		}
	}	//END goFindAddress()
	
	//=====SECTION 10==SANDBOX======================================
	
	
} // MAIN CLASS END	






