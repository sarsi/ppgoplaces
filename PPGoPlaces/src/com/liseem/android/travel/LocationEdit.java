/* 
 * Fragment: 			EditLocation.java
 * Description:			Modify Location Record
 * 
 * Created: 				May 3, 2012
 * Last updated: 		November 17, 2013
 * 
 * Associated files:
 * Layout View:		edit_location.xml
 * Date Dialog:		DateDialogFragment		(interface for callback)
 * 
 * Changes:
 * 	- Tidy codes and documentation
 * - Extract all language strings to strings.xml
 * - Replaced deprecated date dialog with DateDialogFragment
 * 
 * 
 * 
 */


package com.liseem.android.travel;


import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;
import static com.liseem.android.travel.TravelLiteActivity.*;


import java.io.IOException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.liseem.android.travel.items.MyDate;
import com.liseem.android.travel.items.HLocation;
import com.liseem.android.travel.items.Holiday;
import com.liseem.android.travel.items.LPicture;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class LocationEdit extends Fragment 
										   implements DateDialogFragment.DateDialogFragmentListener {
	

	//=====SECTION 1==DECLARATION===================================
	
	private final static String 		TAG="EditLocation";
		
	protected static final int 		LOCATIONDATE_DIALOG_ID=0;
	protected static final int 		REQUEST_CHOOSE_ADDRESS=2;
	protected static final int 		TAKE_PICTURE=4;
	protected static final int 		IMAGE_GALLERY=5;
	protected static final int 		EDIT_LOCATION=8;	
	protected static final int 		VIEW_MAP=2;
	protected static final int 		VIEW_IMAGE=3;
	public static final String 			ADDRESS_RESULT ="address";
	
	//--application------------------------------------
	private PPGoPlacesApplication app;
	private FragMain						act;
	private SharedPreferences 		prefs;	

	//--DB Specific--
	private ArrayList<Holiday> 		holidayList;
	private HLocation 						hLocation;
	private Holiday 							holiday;
	private LPicture							oneShot;
	private MyDate 							myDate;
	private ArrayList<String> 		listItems;
	private ArrayList<Long> 			questFRP;
	private boolean 						hasHoliday=false;				//--passing from intents prevent null exceptio no holInfo
	
	//--Views Specific--
	private TextView 						locationDate;
	private TextView 						locationAddress;
	private Spinner 							holidaySpinner;
	private EditText 						locationName;
	private TextView 						locationNotes;
	private View 								sayCheese;
	private ImageButton					useThis;
	private ImageButton					mapIT;
	private SlidingDrawer 				locDrawer;
	private EditText							locLat, locLong;
	private TextView						locLatText, locLongText;
	private TextView						locInfo;
	private TextView 						emptyList;
	private ArrayAdapter<String>	dataAdapter;

	//--Dialog--
	private AlertDialog 					foundAddress;	
	private AlertDialog 					NoMapInfo;
	
	//--Location Specific--
	private Geocoder 						geocoder;
	private List<Address>				caddresses;
	private Address 							address=null;
	private LocationManager 			locationManager; 	
	private LocationListener 			locationListener;
	private Location 						currentLocation;
	private Location 						mLocation;
	private String 							useAddress;					//use for reverse geocoding when address found
	private BroadcastReceiver 		networkStateReceiver;
	
	private Criteria 							criteria;
	private String 							locationProvider;
	private double							cLatitude, cLongitude;
	private boolean						updateAdd=true	;		//stop runonui update after user accept address
	
	private boolean 						locationAvailable=false;
	private boolean 						addressAvailable=false;		//use in reverse geocoding when address found
	private boolean 						pictureAvailable=false;
	private boolean 						gpsAvail;
	private boolean 						networkAvail;	
	private boolean 						dataConnection;				//--check data network availability
	private boolean 						LocNameExist;
	private boolean 						mapAvail=true;
	private boolean 						gpsInfo;		

	//--HLocation Object Specific
	private long 								dbRowId;
	private boolean 						addressAvail=false;		//--use for loading location information
	private boolean 						holidayInfo=false;
	private int 			 						rightPosition=0;				//--IMPORTANT to set spinner default to "HOME, General"

	private Address 							caddress;	
	
	private boolean 						changesPending=false;
	private ArrayList<String> 		locaddress;
	private String 							addString;

	//--HOLIDAY SPECIFIC--
	private boolean						noHoliday;
	private long								holRefId;
	private boolean 						holInfo=false;
	private MyDate							startDate, 
														endDate;
	//--Program Specific--
	Handler 										handler=new Handler();

	//--CAMERA--
	private ArrayList<String> 		pictureList=new ArrayList<String>();
	private SimpleDateFormat 		timeStampFormat= new SimpleDateFormat("yyyyMMddHHmmssSS");
	private Uri 									imageUri;
	private String 							fileName; 	// picture file name
	private String 							imagePath=new String();
	private boolean 						tookPhoto;
	private ImageView 					mImageView;
	private StringBuilder 					imagesDirList=new StringBuilder();	//trap open pictures
	private boolean						updateDone;

	//--IMAGE GALLERY--
	private boolean 						jump2Gallery=false;
	private boolean 						updateList=false;
	private String 							newList;
	private String 							pendingList="";
	private String 							dbInfo;
	private String								oldLocationName;		//compare location name change
	
	//--Fragment callback
	private int									fragmentRequestCode;
	private int									fragmentResultCode;
	private Address							newAddress;


	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	
		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;
	
		//--inflate layout for fragment 1------------------------------------------------
		View v=inflater.inflate(R.layout.edit_location, container, false);
		setHasOptionsMenu(true);								//--use fragment options menu inaddition to activity default
		setRetainInstance(true);								//--retain fragment instance in back stack		
		return v;			
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	} 
	
	@Override
	public void onStart() {
		super.onStart();
		setupView();
		//--cannot call setupView in onResume() as it reload dbInfo again override new
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		setListener();
		
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);        
		getActivity().registerReceiver(networkStateReceiver, filter);
	}
		
	@Override
	public void onPause() {
		getActivity().unregisterReceiver(networkStateReceiver);
		super.onPause();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//--Save vulnerable information--
		outState.putBoolean("locationInfo", locationAvailable);
		outState.putBoolean("takenPicture", tookPhoto);
		if(locationAvailable) {
			outState.putDouble("latitude", cLatitude);
			outState.putDouble("longitude",cLongitude);
		}
		if(tookPhoto) {
			outState.putStringArrayList("imageArray", pictureList);
			outState.putString("imagePath", imagePath);
		}
		outState.putString("lcoationInfo",dbInfo);
	}
	
	
	/*@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		//--Restore vulnerable information upon onResume--
		locationAvailable=savedInstanceState.getBoolean("locationInfo");
		tookPhoto=savedInstanceState.getBoolean("takenPicture");
		if (locationAvailable) {
			cLatitude=savedInstanceState.getDouble("latitude");
			cLongitude=savedInstanceState.getDouble("longitude");
		}
		if(tookPhoto){
			pictureList=savedInstanceState.getStringArrayList("imageArray");		
			imagePath=savedInstanceState.getString("imagePath");
		}
		dbInfo=savedInstanceState.getString("lcoationInfo");
	}*/
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
			
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
	private void setupView() {

		//--path to main application--
		prefs					=getActivity().getSharedPreferences (PREFNAME, MODE_PRIVATE);				
		app						=(PPGoPlacesApplication)getActivity().getApplication();		
		act						=(FragMain)getActivity();
		
		//--check for wifi and dataconnection-------------------------------------------
		networkAvail		=PPGoPlacesApplication.hasNetwork(getActivity());
		dataConnection	=app.isOnline();					//--data network available
		
		//--Initialize instances--
		geocoder					= new Geocoder(getActivity());
		myDate					= new MyDate();			//--location date
		startDate					= new MyDate();			//--holiday start date
		endDate					= new MyDate();			//--holiday end date
		
		//--views declaration--------------------------------------------------------------------
		locationDate			= (TextView)getActivity().findViewById(R.id.locDate);				
		locationAddress  	= (EditText)getActivity().findViewById(R.id.locAdd); 				
		locationName			= (EditText)getActivity().findViewById(R.id.locName);			
		locationNotes			= (TextView)getActivity().findViewById(R.id.locNotes);
		holidaySpinner		= (Spinner)getActivity().findViewById(R.id.holidaySpinner);
		emptyList				= (TextView)getActivity().findViewById(R.id.emptyList);

		//--sliding drawer view setup---------------------------------------------------------
		locDrawer				= (SlidingDrawer)getActivity().findViewById(R.id.slidingDrawer1);
		locLat				    	= (EditText)getActivity().findViewById(R.id.locLat);
		locLong					= (EditText)getActivity().findViewById(R.id.locLong);
		locLatText				= (TextView)getActivity().findViewById(R.id.locLatText);
		locLongText				= (TextView)getActivity().findViewById(R.id.locLongText);
		locInfo						= (TextView)getActivity().findViewById(R.id.locInfo);
	
		useThis					= (ImageButton)getActivity().findViewById(R.id.useThis);
		useThis.setVisibility(View.INVISIBLE);
		mapIT						= (ImageButton)getActivity().findViewById(R.id.mapIT);
		mapIT.setVisibility(View.INVISIBLE);
		
		//--display "home, general" textview inplace of spinner if no holiday---------
		holidaySpinner.setEmptyView(emptyList);		//--set Spinner to empty textview, listItem must be empty

		//--get RowId pass from location view--------------------------------------------
		//--Intent ladd=new Intent();
		fragmentRequestCode	= act.getRequestCode();
		fragmentResultCode		= act.getResultCode();
		
		switch(fragmentRequestCode) {
			case MAP_LOCATION:
				Log.d(TAG,"366 in CASE MAP_LOCATION");
				hLocation					= new HLocation();
				hLocation					= act.getHLocation();
				if (fragmentResultCode==1) {						//--only if result code is successful, i.e. 1
					newAddress						= act.getAddress();
					if (newAddress != null) {
						Log.d(TAG,"373 in newAddress not null");
						addressAvailable= true;
						cLatitude 		= newAddress.getLatitude();
						cLongitude		= newAddress.getLongitude();
						hLocation.setLatitude(cLatitude);
						hLocation.setLongitude(cLongitude);
						setAddressToLocation(newAddress);
					}
				}
				break;
				
			case LOCATION_VIEW:
				Log.d(TAG,"383 in Location View");
				Bundle lextras			= this.getArguments();
				if (lextras!=null) 
				{
					dbRowId				= lextras.getLong(LOC_ROWID);
					holRefId				= lextras.getLong(HOL_REFID);
					hasHoliday			= lextras.getBoolean(HAS_HOLIDAY);
				}	
				hLocation						= new HLocation();
				hLocation						= app.getHLocation(dbRowId);	//--Location Information
				
				//--check for location information available in db--
				if (hLocation.getLatitude() > 0 && hLocation.getLongitude() > 0) {
					locationAvailable	= hLocation.hasLocation();
					cLatitude					= hLocation.getLatitude();					//--for findAddress
					cLongitude				= hLocation.getLongitude();				//--reverse geocoding
				} else{
					locationAvailable	= false;			
				}
				break;
		}
		
		//--load location information-------------------------------------------------------------
		loadLocation();						//--445
			
		//--check whether holiday db is empty------------------------------------------------
		noHoliday 				= app.isHolidayDBEmpty();
		
		//--load data for edit---------must be later than views above-----
		//--but before set view call to hLocation instance----------
		if (!noHoliday) {
			loadList();							//-- 498 must be before view setup for edit
			addItemsOnSpinner();			//-- 438 setup spinner adapter
		}
		
		//--display holiday information if tag to a holiday-----------------
		if (hasHoliday) {
			//locationNotes.setText("Holiday : "+holiday.getName().toString());
			displayHolidayInfo();			//--568 display holiday information method
		} else {
			locationNotes.setText(getString(R.string.no_holiday_tag_to_location));
		}

		//--display address information if available, else if location gps is available 
		//--if address NONE, try to GEOCODER reverse geo for address
		if (addressAvail) {
			locationAddress.setText(hLocation.getAddress().toString());
		} else {
			//--need to check geocoder availability before trying to  find address---------------------
			if (locationAvailable) {
				//--cannot set co-ord to address field, if updated, future will not be able to discover address
				if (app.isOnline()) { 			//--ver1.0 Geocoder.isPresent()
					//findAddress();	 	//--old runnable thread to find address
					//---find address AsyncTask
					try {
						address=new goFindAddress().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, hLocation).get();
					} catch (InterruptedException e) {						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				} else {
					Toast.makeText(getActivity().getBaseContext(), getString(R.string.data_connection_not_available), Toast.LENGTH_SHORT).show();
				}
			}	//--end reverse geocode address
		}

		//--display date-----------------------------------------------------------------------------------------------	
		//--need to ensure locDate is str2Calendar from db date directly
		//--else the Dialog picker will not show
		myDate.setStrDate(hLocation.getLdate().toString());
		String strDate=myDate.getStrDate();
		//--setCalDMY(TextView view, String dTitle, Calendar cdate)
		//setCalDMY(locationDate, "Date: ", myDate.getCalDate());			//--too long for field
		//locationDate.setText(((TravelLiteActivity) getActivity()).displayShortDMY(strDate));
		
		//--display date to text view, using myDate object passing into method
		((TravelLiteActivity)getActivity()).displayTodayDate(locationDate, myDate);					
		
	} //--END setUpView
	
	//--method to load location information
	public void loadLocation() {											//--location location information
		Log.d(TAG,"463 make it into load location");
		//--display location information on view---------------------------------------------------
		oldLocationName= hLocation.getName().toString();		//--comparing for name change in album
		locationName.setText(hLocation.getName().toString());	
			
		//--check for address available in db------------------------
		String checkAddress=null;
		if (hLocation.getAddress() !=null) {
			checkAddress			= hLocation.getAddress().toString();
			checkAddress			= checkAddress.replaceAll("\\s", "");
		}
		if (checkAddress == null || checkAddress.length()==0) {			
			addressAvail			= false;
		} else {
			addressAvail			= true;
		}
	}	//--END loadLocation
	
	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================
	public void addItemsOnSpinner() {				//-- 373 setupView
		
		//--add holiday spinner adapter------------------------------------------------
		dataAdapter=new ArrayAdapter<String>(
			getActivity(), android.R.layout.simple_spinner_item, listItems);
		dataAdapter.setDropDownViewResource(R.layout.dropdown_item); //--custom to remove radio button
		holidaySpinner.setAdapter(dataAdapter);

		if (!noHoliday) 
			rightPosition	=questFRP.indexOf(hLocation.getRefid());
			
		holidaySpinner.setSelection(rightPosition);		//--set spinner to right holiday or home position
		
		//--set on ItemSelectedListner
		holidaySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					
					if (position == 0) {
						hLocation.setRefid(0);
						hLocation.setHoliday(false);
						locationNotes.setText(R.string.no_holiday_tag_to_location);
						hasHoliday=false;
					}	else {
						holiday=holidayList.get(position-1);		//--spinner position to db is -1, 
						hLocation.setRefid(holiday.getId());		//--db index to display on spinner is +1
						hLocation.setHoliday(true);
						startDate.setStrDate(holiday.getStart_date());
						endDate.setStrDate(holiday.getEnd_date());
						hasHoliday=true;
						
						//--update notes view, share method with setupView--
						displayHolidayInfo();		//--568 display holiday information method
					}
				}
				
				@Override
				public void onNothingSelected(AdapterView<?> parent) { } //--do nothing
				
		}); 	//--END setOnItemSelectedListener statement
	} 	//--END holidaySpinner OnItemClickListner()
	
	//--method to setup listitems for spinner adapter
	protected void loadList() {					//-- 372 setupView

		//--initialize instances
		holiday					= new Holiday();
		holidayList				= new ArrayList<Holiday>();
		listItems					= new ArrayList<String>();
		questFRP					= new ArrayList<Long>();
		rightPosition		= 0;
		
		//--get all holiday info and holiday instance pass from intent-----
		//noHoliday			= app.isHolidayDBEmpty();
		//hLocation				= app.getHLocation(dbRowId);			//--Location Information
		//oldLocationName= hLocation.getName().toString();		//--comparing for name change in album

		//--create listItems for spinner, else emptylist  will replay spinner view
		if (!noHoliday) {		//--if holiday db is not empty
			//--retreive DB for edit and spinner information-------------------------
			holidayList		= app.loadHolidays();							//--for spinner
			
			//--setup list for spinner arrayadapter list (move into !noHoliday method if setEmptyList work)
			//--NOTE: listItems must be empty for holidaySpinner.setEmptyView to work
			listItems.add(getString(R.string.home_general));		
			questFRP.add((long)'0');

			for (Holiday h: holidayList) {
				listItems.add(h.getName());
				questFRP.add(h.getId());
			}
		} 	else {					//--update location info if hasholiday is true since holidaydb is empty
			if(hLocation.hasHoliday()) {
				hLocation.setHoliday(false);
				hLocation.setRefid(0);
				app.updateHLocation(hLocation);
			}
		} //--END create listItems
		


	
		//--test for location holiday tag exist, get spinner position info as well-------------------------
		//if(hasHoliday)
		if (hLocation.hasHoliday()) {														//crash here try to get 0 value holiday;
			try {
				holiday				=app.getHoliday(hLocation.getRefid());
				rightPosition		=questFRP.indexOf(hLocation.getRefid());	//critical as listItems is offset by one by "home,general"
				
			} catch (Exception e) {
				//--if holiday record no longer exist revert record to "Home, General"-----
				hLocation.setRefid(0);
				hLocation.setHoliday(false);
				app.updateHLocation(hLocation);
			}
		} 

	}		//--END loadList()
	
	//--method to consistently display holiday information in location notes
	public void displayHolidayInfo() {			//--387 setupView and 478 addItemOnSpinner
		locationNotes.setText(getString(R.string.holiday_information_)+
				getString(R.string._name_)+"\t"+holiday.getName().toString()+
				getString(R.string._from_)+"\t\t"+holiday.getStart_date().toString()+
				getString(R.string._to_)+"\t\t\t"+holiday.getEnd_date().toString());
	}
	
	//=====SECTION 4==ADD ADAPTER===================================
	
	//=====SECTION 5==SET LISTENER====================================	
		
	private void setListener() {
		
		if (dataConnection) 
			mapIT.setVisibility(View.VISIBLE);
		
		useThis.setOnClickListener(new useThisClick());
		locDrawer.setOnDrawerOpenListener(new DrawerOpenListener());
		
		//--call mapLocationClick  launch MapLocation fragment
		mapIT.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mapLocationClick();					//--669
			}			
		});		//--END map location
				
		//--change location date click listener
		locationDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				changeLocationDate();				//--831
			}			
		});		//--END locationDate change
		
		//--listen for data network connection state change--------------------------------------------
		networkStateReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				boolean oldState=dataConnection;
				dataConnection=app.isOnline();
				Log.d(TAG, "601 dataConnection status "+dataConnection);
				if (dataConnection && locationAvailable && !addressAvail) {
					//findAddress();			//--old thread method
					//---AsyncTask method for reverse geocode for address
					try {
						address=new goFindAddress().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, hLocation).get();
					} catch (InterruptedException e) {						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
				
				//--if data network no longer exist---------------------------------
				if (oldState && !dataConnection) {		
						Toast.makeText(getActivity().getBaseContext(), 
								getString(R.string.data_network_is_no_longer_available_), 
								Toast.LENGTH_SHORT).show();
						
						mapIT.setVisibility(View.INVISIBLE);
				} 
			
				if (dataConnection) {		//--Nov 17, 13 (oldState && dataConnection)
					mapIT.setVisibility(View.VISIBLE);
				} 
			}
		};

	}
	
	
	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================

	//--use this address return from reverse geocoding-----------------------------------------------------------
	private class useThisClick implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			setAddressToLocation(address);			//v2
			hLocation.setAddress(useAddress.toString());
			locationAddress.setText(useAddress.toString());
			updateAdd=false;
			addressAvail=true;
		}
	}

	//--open sliding drawer to access map and location info--------------------------------------------
	private class DrawerOpenListener implements OnDrawerOpenListener {

		@Override
		public void onDrawerOpened() {
			
			StringBuilder latText=new StringBuilder().append(hLocation.getLatitude());
			StringBuilder longText=new StringBuilder().append(hLocation.getLongitude());
			locLatText.setText("Latitude");
			locLongText.setText("Longitude");
			locLat.setText(latText.toString());
			locLong.setText(longText.toString());
			//locInfo.setText(hLocation.getInfo().toString());

			//--display Google map only if Geocoder is present
			if (Geocoder.isPresent() && app.isOnline()) { 		//Nov 17, previously only Geocoder.isPresent()
				mapIT.setVisibility(View.VISIBLE);
			}	
		}		
	}
	
	//--alternative using google map to locate location---------------------------------------------------
	//--Google Map onClick Listener--
	public void mapLocationClick()  {					//--578
		
			//--park current location at main activity
			act.setHLocation(hLocation);
		
			//--go to some new activity to display location map
			String searchAdd;
			if (hLocation.getCountry() !=null) {
				searchAdd=hLocation.getName().toString()+","+hLocation.getCountry().toString();
			} else {
				searchAdd=hLocation.getName().toString();
			}
			
			Bundle mapBundle=new Bundle();
			mapBundle.putInt(REQUESTOR, LOCATION_EDIT);
			mapBundle.putString(FIND_LOCATION, searchAdd);
			if (null != hLocation.getCountry())
				mapBundle.putString(COUNTRY, hLocation.getCountry());
			
			if (hLocation.hasLocation()) {
				mapBundle.putDouble(LOC_LATITUDE, hLocation.getLatitude());
				mapBundle.putDouble(LOC_LONGITUDE, hLocation.getLongitude());
				mapBundle.putBoolean(HAS_LOCATION, true);
			} else {
				mapBundle.putBoolean(HAS_LOCATION, false);
			}
			setTargetFragment(this, MAP_LOCATION);
			MapLocation mapLocation = new MapLocation();
			Log.d(TAG,"682 mapLocationClick() before commit()");
			((FragMain)getActivity()).callMapLocation(mapBundle, mapLocation);		
		
	} 		//--END mapLocationClick()
	

	//--Google Map onClick Listener onActivityResult----------------------------------------------
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode==REQUEST_CHOOSE_ADDRESS) { 
			if (resultCode==RESULT_OK) {
					caddress = data.getParcelableExtra(MapLocation.ADDRESS_RESULT);
					
					//=====SANDY==================
					
			        /*currentLocation=data.getParcelableExtra("MyLocation");
			        String locationString = String.format(
							"@ %f, %f +/- %fm",
							currentLocation.getLatitude(),
							currentLocation.getLongitude(),
							currentLocation.getAccuracy());
					Toast.makeText(getBaseContext(), "Location Information Found: \n: "+
								locationString.toString(),Toast.LENGTH_LONG).show();*/
					
					//=====SANDY==================
					
					//--capture location details if address not null--
					if (null!=caddress) {
						
						locationAvailable=true;
						cLatitude=caddress.getLatitude();
						cLongitude=caddress.getLongitude();	
						
						//--unlike add location, edit location uses previously saved location co-ord
						hLocation.setLatitude(caddress.getLatitude());		//set new co-ord to hLocation
						hLocation.setLongitude(caddress.getLongitude());	//set new co-ord to hLocation
						
						String testAdd=((TravelLiteActivity) getActivity()).add2String(caddress);  //test for null before adding to useaddress
							if (testAdd !=null) {
								addressAvailable=true;
								useAddress=testAdd;		//replace useaddress with new address
								updateAdd=false;			//stop find Address thread
								setAddressToLocation(caddress);			//v2
								locationAddress.setText(useAddress.toString());		//display new address				
							}					
							changesPending=true;
							
							//--update new latitude and longitude in drawer view----------------
							StringBuilder latText=new StringBuilder().append(cLatitude);
							StringBuilder longText=new StringBuilder().append(cLongitude);
							locLatText.setText("New Latitude");
							locLongText.setText("New Longitude");
							locLat.setText(latText.toString());
							locLong.setText(longText.toString());
							locDrawer.close();
						} // end of address routine
					} else if (resultCode==RESULT_CANCELED) {
						//--MUST HAVE THIS TO CATCH RESULT_CANCELED, 
						//--ELSE BACK KEY WILL CRASH--
					}
			}  else {
				super.onActivityResult(requestCode, resultCode, data);	
			}			
	}		//--END 3.5 onActivityResult()

	public void updateLocation() {											//--929 option menu
		Boolean validDateEntry=false;	
		if (locationName.getText().toString().equals(""))	{		//--first check - if no location name
			((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.location_name_is_empty));
		} else {												
			if (hasHoliday) {															//--second check - if is tag to holiday
				boolean validDate = ((TravelLiteActivity)getActivity()).dateWithinRange(startDate, myDate, endDate);
				if (!validDate) {
					((TravelLiteActivity)getActivity()).dateOutOfRange();		//--date outside holiday period
				} else {																	//--validDate is true
					updateLocationInDB();		 		 						//--769 date within holiday period
				} 
			} else {				
				updateLocationInDB();											//--769 not tag to holiday	
			}
		}		
	}	//--END updateLocation)
	
	//--update location information
	protected void updateLocationInDB() {							//--762
		/* 
		 * DATA STAGING FOR DB UPDATE
		 * DATE STRING as ISO8601 strings ("YYYY-MM-DD HH:MM:SS.SSS")
		 * 
		 * ALERT: may have problem storing double to result long
		 * 
		 * Do not declare variable e.g. String dbDate here, cause 
		 * error updating db
		 */
		
		//Log.d(TAG, "4.2 updateLocation() from updateLocationClicked from 3.4 and 4.1");

		hLocation.setName(locationName.getText().toString());
		hLocation.setAddress(locationAddress.getText().toString());
		hLocation.setLdate(myDate.getStrDate());

		String newName=locationName.getText().toString();
		if(hLocation.hasPicture()) {
			ArrayList<LPicture> updatePict=new ArrayList<LPicture>();
			updatePict=app.getLocationPictures(hLocation.getId());
		
			if (!updatePict.isEmpty()) {
				for (LPicture p : updatePict) {
					p.setLocation(newName.toString());
					p.setAddress(hLocation.getAddress());
					boolean changeName=app.updatePicture(p);
				}	
			}
		}

		/*if(updateList)					//update from gallery must be before adding camera
		{	hLocation.setInfo(newList);	}*/

		boolean updateDone=app.updateHLocation(hLocation);

		if (updateDone) {
			Intent intent=new Intent();
			getActivity().setResult(RESULT_OK, intent);
		}
		
		app.backupManager.dataChanged();
		getFragmentManager().popBackStackImmediate();
		//------------------------------------------------------------------------------------------
		
	}	//--END updateLocationInDB()	
	
	//=====SECTION 6==LISTENER METHODS================================

	//=====SECTION 7==HELPER METHODS==================================
	//--set location information from address found----------------------------------------------
	public void setAddressToLocation(Address address) {
		
		Address tempAdd=address;		
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
		//--no change to co-ord as there is no location listener coming into edit mode
		
		//--set default name if location name is empty----------------
		if (locationName.length()==0 && tempAdd.getFeatureName() !=null) 
		{	
				StringBuilder locName=new StringBuilder().append(tempAdd.getFeatureName());
				if (tempAdd.getLocality() !=null)
					locName.append(", ").append(tempAdd.getLocality().toString());
				locationName.setText(locName.toString());			
		}		
	}		//--END setAddressToLocation


	//--call change location date dialog fragment------------------------------------------------------------
	public void changeLocationDate() {				//--572 setListener()
		
		//--initial date dialog fragment
		DateDialogFragment ddf = DateDialogFragment.newInstance(getActivity(), LOCATIONDATE_DIALOG_ID, myDate.getCalDate());
		ddf.setDateDialogFragmentListener(this);
		DialogFragment newFragment = ddf;
		newFragment.show(getFragmentManager(), "DatePicker");
	}
	
	//--upon return from date dialog change callback--------------------------------------------------------
	@Override
	public void dateDialogFragmentDateSet(Calendar date, int sWhichDate) {		//--835
		
		if (hasHoliday) {
			MyDate newDate = new MyDate();			
			newDate.setCalDate(date);
			boolean validDate = ((TravelLiteActivity)getActivity()).dateWithinRange(startDate, newDate, endDate);

			if (!validDate) {
				((TravelLiteActivity)getActivity()).dateOutOfRange();		//--outside holiday period
				myDate = startDate;															//--set to holiday start date
			} else {
				myDate.setCalDate(date);													//--within holiday period
			}
		} else {
			myDate.setCalDate(date);														//--not tag to any holiday
		}
		locationDate.setText(((TravelLiteActivity) getActivity()).displayShortDMY(myDate.getStrDate().toString()));		
	}
	

	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
			super.onPrepareOptionsMenu(menu);

			menu.removeItem(9);			//--removed Calculator
			menu.removeItem(10);		//--removed setting
	}
		
	private void CreateMenu(Menu menu) {
		
		MenuItem mnu1=menu.add(0,1,1, R.string.update);
			{ mnu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS); }		
			
	}
	
	private boolean MenuChoice(MenuItem item)
	{
		switch (item.getItemId()) {
		case 1:
			updateLocation();			//--768
			return true;
		}
		return false;
	}
		


	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//--reverse geocoding to find address from current location--------------------------------
	private class goFindAddress extends AsyncTask <HLocation, Void, Address> {

		@Override
		protected Address doInBackground(HLocation... params) {
			HLocation newLocation=new HLocation();
			newLocation=params[0];
			double thisLatitude=newLocation.getLatitude();
			double thisLongitude=newLocation.getLongitude();
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
				//--------------------------------------------------
				useAddress=((TravelLiteActivity) getActivity()).add2String(result);
				String checkEmpty=useAddress.replaceAll("\\s", "");		//trim all space to check for empty address
				if (useAddress != null && checkEmpty.length() > 0) {
					useThis.setVisibility(View.VISIBLE);		//if only add2String do not return null
					addressAvailable=true;							//set to true only if country and postalcode are not empty
					
					if (locationAddress.toString().isEmpty()) {
						locationAddress.setText("New Address Found:\n"+useAddress);
					} else {
						Toast.makeText(getActivity().getBaseContext(), R.string.location_address_found_+useAddress.toString()
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

	//==BACK KEY ROUTINE TO CONFIRM EXIT================================
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//--Replace back key as cancel--
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if(changesPending) 
				{
				foundAddress = new AlertDialog.Builder(getActivity())
				//.setTitle("")
				.setMessage(R.string.unsaved_changes_)
				.setPositiveButton(R.string.update, new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						updateLocation();
						getActivity().finish();
					}
				})
				.setNeutralButton(R.string.quit, new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						getActivity().finish();
					}
				})
				.setNegativeButton(R.string.cancel, new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						foundAddress.cancel();
					}
				})
				.create();
				foundAddress.show();
				}else {
					getActivity().finish();
			} 
		}
		return false; 
	} 	//END 4.5 onKeyDown() 
	
	
	//==RETIRED old background thread to reverse geo to find address==================
	public void findAddress() {
		
		handler=new Handler();
		//Toast.makeText(getBaseContext(), "Trying To Finding Address", Toast.LENGTH_LONG).show();
		
		final Runnable runOnUIThread=new Runnable() {
			@Override
			public void run() {
				try {
					_showInUI();
				} catch (Exception e) {
					e.printStackTrace();
					//Log.d(TAG,"Exception eror from runOnUIThread"+e.getLocalizedMessage());
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
	}
	
	private void _doInBackgroundPost() {
		
			//Attempt to get Location Address from co-ordinates
			try {
				caddresses=geocoder.getFromLocation(cLatitude, 
						cLongitude, 1);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (caddresses != null && caddresses.size() >0) { 
				address=caddresses.get(0);
			}
	}

	private void _showInUI() {
		if(address !=null && updateAdd) {	
			
			//--------------------------------------------------
			useAddress=((TravelLiteActivity) getActivity()).add2String(address);
			String checkEmpty=useAddress.replaceAll("\\s", "");		//trim all space to check for empty address
			if (useAddress != null && checkEmpty.length() > 0) {
				useThis.setVisibility(View.VISIBLE);	//if only add2String do not return null
				addressAvailable=true;						//set to true only if country and postalcode are not empty
				Toast.makeText(getActivity().getBaseContext(), R.string.location_address_found_+useAddress.toString()
						+R.string._please_click_address_found_to_use_this_address, 
						Toast.LENGTH_SHORT).show();
			} else {
				useThis.setVisibility(View.INVISIBLE);	
			}
			//--------------------------------------------------			
		}
	}
	

	
		
} //--END MAIN CLASS		
	