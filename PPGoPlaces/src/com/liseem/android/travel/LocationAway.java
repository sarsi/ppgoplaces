package com.liseem.android.travel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.liseem.android.travel.items.HLocation;
import com.liseem.android.travel.items.Holiday;

import android.app.AlertDialog;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class LocationAway extends TravelLiteActivity {

	
	//=====SECTION 1==DECLARATION===================================
	
	private final static String TAG="AwayLocation";
	
	protected static final long CLOSE_BY=20;		//geofence for status update to trigger toast		
	private static final int LISTEN_TIME=60;			//15 secs, shld change to 60,000millisecs
	private static final int LISTEN_DISTANCE=5;		//50 meters shld change to 200m
	private static final int TIME_LAPSE=1000*60*2;		//two minutes
	
	//--system------------------------------------------------------------------------
	private PPGoPlacesApplication	app;
	private SharedPreferences 		prefs;
	private Context							context;
	private SharedPreferences  		countryList;
	
	//--shared preferences--------------------------------------------------------
	private boolean						indoor;					//setting for indoor/outdoor;
	private boolean						mapAccurMode;	//settings for accuracy

	
	//--location listener, provider and manager------------------------------
	private LocationManager 			locationManager;
	private String 							locationProvider;
	private MyLocationListener		locationListener;
	private Location							location;
	private Location							lastLocation;
	private Location							currentLocation;
	private List<Address> 				caddresses;	
	private ArrayList<Address>		addresses;
	private Address							address;				
	private boolean						locationAvail;		//location info available
	private boolean						addressAvail;		//address info available
	private double							cLatitude, cLongitude;
	private float								cAccuracy;
	private boolean						updateAdd;			//false stop address update
	private Geocoder						geocoder;
	private Criteria							criteria;
	private int 									howAccurate;
	private String								useAddress;


	//--programmatic variables--------------------------------------------------
	private Holiday 							holiday;
	private HLocation						hLocation;
	private ArrayList<HLocation>  locationList;
	
	//--network connectivitiy and broadcast receiver-----------------------
	private BroadcastReceiver 		networkStateReceiver;
	private ConnectivityManager 	connectivityManager;
	private boolean 						networkAvail;
	private boolean 						mobileAvail;
	private boolean 						dataConnection;
	private boolean 						hasCoverage;
	private AlertDialog 					turnOnGPS;
	private boolean 						gpsAvail;


	//--telephony services--------------------------------------------------------
	private String 							netOperator;
	private String 							countryCode;
	private String 							isoCode;
	private String 							currentCountry;
	private String								simCountry;




	
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
				LISTEN_TIME, 
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
		outState.putBoolean("locationInfo", locationAvail);
		outState.putBoolean("updateAdd", updateAdd);
		if(locationAvail) {
			outState.putDouble("latitude", cLatitude);
			outState.putDouble("longitude",cLongitude);
		}
	}
	
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		//--Restore vulnerable information upon onResume--
		locationAvail=savedInstanceState.getBoolean("locationInfo");
		updateAdd=savedInstanceState.getBoolean("updateAdd");
		if (locationAvail) {
			cLatitude=savedInstanceState.getDouble("latitude");
			cLongitude=savedInstanceState.getDouble("longitude");
		}
	}	
	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 2==LIFECYCLE METHODS===============================
	private void setupView() {

		//--path to PPGoPlacesApplication------------------------------
		app=(PPGoPlacesApplication)getApplication();	
		
		//--get SharedPreferences object--------------------------------
		prefs=getSharedPreferences(PREFNAME, MODE_PRIVATE);
		countryList=getSharedPreferences(COUNTRY, MODE_PRIVATE);
		indoor=prefs.getBoolean("indoor", true);
		mapAccurMode=prefs.getBoolean("mapAccurMode", false);			//findLocation 2.2
		simCountry=prefs.getString("simCountry", "unknown");
		
		//--initialize instances---------------------------------------------
		locationList=new ArrayList<HLocation>();
		holiday=new Holiday();
		hLocation=new HLocation();
		//hLocation.setPicture(false);			//prevent getAllLocation null exception
		//hLocation.setHoliday(false);			//prevent getAllLocation null exception
		
 		//--detect telco telephony services information---------------------------------
        TelephonyManager tm=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        isoCode=tm.getSimCountryIso().toString();
        countryCode=tm.getNetworkCountryIso().toString();
        netOperator=tm.getNetworkOperatorName().toString();
        
        //--away country discovery----------------------------------------------------------
        if (countryCode != null) 
        		currentCountry=countryList.getString(countryCode, "unknown");
		}
	
	private void setNetworks() {
		//--setup connectivity and connectivity condition----------------------------------
		//--check for wifi and dataconnection-------------------------------------------		
		networkAvail=PPGoPlacesApplication.hasNetwork(this);
		mobileAvail=PPGoPlacesApplication.hasMobile(this);
		dataConnection=app.isOnline();
		hasCoverage=PPGoPlacesApplication.hasCoverage(this);
		
		//--display Google map only if Geocoder is present
		/*if (dataConnection) { 
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
        }*/
        
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
				if (dataConnection && locationAvail && !addressAvail) {
					try {
						address = new goFindAddress().execute(currentLocation).get();
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
						
						//mapIT.setVisibility(View.INVISIBLE);
				} 
			
				/*if (!oldState && dataConnection) {
					mapIT.setVisibility(View.VISIBLE);
				} */
			}
		};
	}
	//=====SECTION 3==SETUP VIEWS====================================
	
	private void setListeners() {
		
		
	}
	//=====SECTION 3==SETUP VIEWS====================================
	
	//=====SECTION 4==ADD ADAPTER===================================
	
	
	private void loadList() {
		hLocation = new HLocation();
		if (currentLocation != null) {
			try {
				hLocation = new findNearByLocation().execute(currentLocation).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		
	}
	//=====SECTION 4==ADD ADAPTER===================================
	
	//=====SECTION 5==SET LISTENER====================================

	
	//=====SECTION 5==SET LISTENER====================================
	
	//=====SECTION 6==LISTENER METHODS================================
	
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
		} else if (!dataConnection) {
			Toast.makeText(getBaseContext(), 
					"No data network available for resolving address", 
					Toast.LENGTH_SHORT).show();
		}
		
				
		//--display message to begin searching for location-------------
		/*if (!locationAvailable)  {
			locationInfo.setText("No location information available");
		}*/
		
	}	//END 3.1 findLocation
	
	//--check and ask to turn on GPS---------------------------------------------
	//--Part 1 of 2
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
	//--Part 2 of 2
	private void enableLocationSettings() {
		
		//--launch phone settings, location and security for use to turn on gps-------------
	    Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	    startActivity(settingsIntent);	    
	}
	
 	//--7.4 Location Listener Inner Class-----------------------------------------------------------------
	//--Called from onResume() locationManager.request --
	private class MyLocationListener implements LocationListener {
		boolean checkWhichBetter;
		

		//--Upon location information acquired--
		@Override
		public void onLocationChanged(Location location) {
			
			//--if no current locationavailable--------------
			if (currentLocation==null && location !=null) {
				currentLocation=location;
				updateStatus();
			} 
			
			//--check which is better before update--------
			//--isBetterLocation method in TravelLiteActivity, current timeDelta set to 2 mins--
			if (currentLocation !=null && location !=null) {
				checkWhichBetter=isBetterLocation(location, currentLocation);
			
				//--use if new location is better------------------
				if (checkWhichBetter) {
					currentLocation=location;
					updateStatus(); //look 5 lines down below				
				}
			}
		}

		@Override
		public void onProviderDisabled(String provider) { }

		@Override
		public void onProviderEnabled(String provider) { }

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) { }
		
		public void updateStatus() {
			
			locationAvail=true;
			if (updateAdd) {		//once update is set to false no more find address
				cLatitude=currentLocation.getLatitude();
				cLongitude=currentLocation.getLongitude();
				cAccuracy=currentLocation.getAccuracy();
				//--check data network available for geocoder---------------------
				if (dataConnection) { 
					try {
					address=new  goFindAddress().execute(currentLocation).get();	 	//4.6 runnable thread to find address
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
			}
			//--previously locationNotes
			/*Toast.makeText(getBaseContext(), "Location Information Found: \nLatitude: "+
						cLatitude+"\nLongitude: "+
						cLongitude,Toast.LENGTH_SHORT).show();*/
			/*locationString = String.format(
					"Latitude %f \nLongitude %f \nAccuracy +/- %.2fm",
					currentLocation.getLatitude(),
					currentLocation.getLongitude(),
					currentLocation.getAccuracy());
			locationInfo.setText(locationString); */
			
			if(locationAvail)
				howAccurate=(int)currentLocation.getAccuracy();
				if (howAccurate <50) {
					criteria.setAccuracy(Criteria.ACCURACY_FINE);
				}
			
			//--display location updates stop/resume button, display message---------
			//locUpdate.setVisibility(View.VISIBLE);
			//locUpdate.setText("Stop Location Updates");
		}
		
	}	//END 7.4 LOCATIONLISTENER()
	
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
		/*if (locationName.length()==0 && tempAdd.getFeatureName() !=null && tempAdd.getSubThoroughfare() !=null) 
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
		}*/
	}		//END setAddressToLocation
	
	//=====SECTION 7==HELPER METHODS==================================
	
	//=====SECTION 8==MENU AND DIALOG METHODS===========================
	
	//=====SECTION 8==MENU AND DIALOG METHODS===========================
	
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
				/*if (useAddress != null && checkEmpty.length() > 0) {
					//useThis.setVisibility(View.VISIBLE);		//if only add2String do not return null
					//addressAvailable=true;							//set to true only if country and postalcode are not empty
					
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
				}		*/
			}
		}
	}	//END goFindAddress()
	
	
	//--find nearby location-----------------------------------------
	private class findNearByLocation extends AsyncTask <Location, Void, HLocation> {

		@Override
		protected HLocation doInBackground(Location... params) {
			ArrayList<HLocation> tempList = new ArrayList<HLocation>();
			HLocation thisLocation = new HLocation();
			Location hasLocation =  params[0];
			tempList=app.getAllLocations();
			
			if (currentCountry != "unknown" && thisLocation !=null) {
				for (HLocation h : tempList) {
					if (h.getLatitude() > 0 && h.getLongitude() > 0) {
						if (locationNearBy (hasLocation, h, CLOSE_BY))
							locationList.add(h);
							thisLocation=h;
					}
				}		
			} 
			if (thisLocation != null) {
				return thisLocation;
			} else {
				return null;
			}
		}
	}
		
	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================
	
	//=====SECTION 10==SANDBOX======================================

	//=====SECTION 10==SANDBOX======================================
	

}	//END MAIN CLASS
