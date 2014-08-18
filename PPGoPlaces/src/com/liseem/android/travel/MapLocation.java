/*
 * Fragment: 			MapLocation.java
 * Description:			Google Mapview overlay, search or drag get location information
 * 
 * Created: 				May 3, 2012
 * Last updated: 		November 17, 2013
 * 
 * google map V2 keys
 * debug key: "AIzaSyBVGF_OwWZ5rf8ueLFRly3jlqDtZQnvA1o"
 * release key: "AIzaSyCmsukYhFRrTXFKS-xVzlNiTr53ZzyTIus"
 * 
 * OBSOLETED map V1 keys (do not use)
 * release key: "0riAp5Zuv0TkZdjlxCJFN2pLBGZlgsu5d2okf5g"
 * debug key:"0riAp5Zuv0TmIB7ooEPC6CSbVDCDEgFPy7rE6Zw"
 * 
 * Neuschwanstein Castle
 * Schwangu 87645
 * 47.558341,10.749865
 * 
 * Munich Marienplatz
 * 48.140662,11.576672
 * 
 * Basilica di San Marco, Piazza di San Marco, Florence, Italy
 * 43.778329,11.258798
 * 
 * 32 Dover Rise, Singapore
 * Lat 1.306262, Long 103.783091
 * 
 * Holland Village
 * Lat 1.311054, Long 103.794989
 * 
 * Adelphi
 * Lat 1.295075, Long  103.851158
 * 
 * currently using debug key via test with eclipse, only release key if install 
 * via email to user.
 * 
 * Changes:
 * - migrated to google map v2
 * - removed mapIT and useThis button, replace with search and longclick
 * - pass address to fragment via getActivity().setAddress , requestCode and resultCode
 * - actionBar drop down navigator for map choice of normal, satellite, hybrid and terrain
 * - use on camera change listener to persist zoom setting by user.
 * - remove
 * 
 * To do:
 * - tidy codes and documentation
 * - clean Log.d
 * - centralized final static to TravelLiteActivity
 * 
 * 
 */

package com.liseem.android.travel;

import static android.content.Context.MODE_PRIVATE;
import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;
import static com.liseem.android.travel.TravelLiteActivity.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;

//import android.os.AsyncTask;
import android.os.Bundle;
//import android.os.Handler;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.liseem.android.travel.items.AsyncTask;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SpinnerAdapter;
import android.widget.TextView;


public class MapLocation extends MapFragment   {			//implements ActionBar.OnNavigationListener

	//=====SECTION 1==DECLARATION===================================
	
	private static final String 			TAG ="MapLocation";
	public static final String 				ADDRESS_RESULT ="address";
	
	//--application--------------------------------------------
	private PPGoPlacesApplication 	app;
	private FragMain							act;
	private SharedPreferences 			prefs;
	private SharedPreferences.Editor	editor;
	private ActionBar 							actionBar;

	//--view-----------------------------------------------------
	private EditText 							addressText;
	private SpinnerAdapter 					spinnerAdapter;

	//--fragment callback------------------------------------
	private int										requestorCode;				//--calling fragment code REQUESTOR
	private String									searchAddress;				//--if search address available from requestor
	private boolean							haveCord;						//--for bundle long/lat pass in
	
	//--location------------------------------------------------------------------------
	private List<Address> 					addresses;
	private Address 								address;							//--huntLocation to first find location
	private Address								newAddress;					//--new map on click new location find address
	private String									showAddress;					//--address text from stringbuilder showAddressTap
	
	//--mapview-----------------------------------------------------------------------
	private MapView 							mapView;			
	private GoogleMap						googleMap;
	private LatLng								whereAmI;
	private LatLng								foundLatLng;					//--on click reverse geo
	private LatLng								parking;
	private String									findCountry;
	private MarkerOptions 					markerOptions;
	private Marker								infoWindows;					//--marker windows open by default
	private boolean							infoHide;
	private String									hereWeAre;
	private int										mapChoice;						//--v2 1-normal, 2-Satellite, 3-hybrid and 4-terrain 
	private double 								cLatitude = 1.295075;			//--Adelphi, Singapore
	private double 								cLongitude = 103.851158;
	private float 									zoomSize;
	
	//--background thread for geocoding to prevent exception---------------
	private Context 								mContext;
	//private Handler 							mHandler;
	
	//--for use on action bar drop down navigator, R.string or getString cannot work
	//--implements listener, string array, and 3 parts below
	//private String[] mapType 				= new String[] {"normal", "satellite", "hybrid", "terrain"};
	


	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		super.onCreateView(inflater, container, savedInstanceState);		//--without this fragment will crash with null error at onResume()
		//setRetainInstance(true);
		setHasOptionsMenu(true);																	//--use fragment options menu inaddition to activity default
		
		//--inflate layout for fragment1------------
		//--return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) {
			return null; }
		
		
		//--inflate layout for fragment 1------------------------------------------------
		View view					=inflater.inflate(R.layout.map_location, container, false);		
		mapView 					= (MapView) view.findViewById(R.id.map_view);
		mapView.requestTransparentRegion(mapView);
		mapView.onCreate(savedInstanceState);
		mapView.onResume();																		//--needed to get the map to display immediately
			
		//googleMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map_view)).getMap();
		//googleMap = mapView.getMap();
		
		try {
			if (googleMap == null) {
				MapsInitializer.initialize(this.getActivity());
				googleMap 		= mapView.getMap();
			} else {
				googleMap		= mapView.getMap();
			}
 		} catch (GooglePlayServicesNotAvailableException e) {
     		e.printStackTrace();
 		}

		//googleMap.getUiSettings().setMyLocationButtonEnabled(true);		//--set My Location Button
		//googleMap 		= mapView.getMap();
		Log.d(TAG, "232 onCreateView, container not null, leaving onCreateView");
		
		return view;			
	}
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//mapView.onCreate(savedInstanceState);
		Log.d(TAG, "228 arrived at onCreate()");
	}

	@Override
	public void onStart() {
		super.onStart();
		
		Log.d(TAG, "249 onStart() before setupView");
		setupView();		
		//addAdapter();		//--call in setupView just for spinner adapter before action bar
		setListener();		
		
	}
	
	@Override
	public void onResume() {
		super.onResume();	
		//mapView.onResume();
		//Log.d(TAG, "238 after onResume");

	}	
	
	@Override
	public void onPause() {
		super.onPause();	
		mapView.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mapView.onLowMemory();
	}
	
	public void onFinish() {
		googleMap.stopAnimation();
	}
	
	public void onCancel() {
		googleMap.stopAnimation();
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
		
		//--path to main application-----------------------------------------
		app								= (PPGoPlacesApplication)getActivity().getApplication();
		act								= (FragMain)getActivity();
		prefs							=getActivity().getSharedPreferences (PREFNAME, MODE_PRIVATE);	
		editor							= prefs.edit();
		
		//addAdapter();																	//--initial adapter before action bar
		
		//--call ActionBar------------------------------------------------------------------
		actionBar 					= getActivity().getActionBar();
		actionBar.show();
		actionBar.setDisplayShowTitleEnabled(false);
		
		//--Part 1 of 3 ActionBar drop-down navigator
		//actionBar.setNavigationMode(1);				//--actionBar.setNavigationMode(NAVIGATION_MODE_LIST);		
		//actionBar.setListNavigationCallbacks(spinnerAdapter, this);
		
		//--setup last cLatitude and cLongitude before bundle-----------------------
		cLatitude						= Double.longBitsToDouble(prefs.getLong("cLatitude",Double.doubleToLongBits(1.295075)));
		cLongitude					= Double.longBitsToDouble(prefs.getLong("cLongitude",Double.doubleToLongBits(103.851158)));

		//--retrieve bundle information from call fragment---------------------------	
		Bundle bundle 			= this.getArguments();									//--fragment retrieve bundle
		if (bundle!=null) {
			haveCord					= bundle.getBoolean(HAS_LOCATION, false);
			requestorCode			= bundle.getInt(REQUESTOR);
			findCountry				= bundle.getString(COUNTRY);
			
			if (!haveCord) {
				hereWeAre			= getString(R.string.last_known);					//--Adelphi
			} else {
				double tLat			= cLatitude;
				double tLng		= cLongitude;
				
				hereWeAre			= getString(R.string.you_are_here);
				searchAddress		= bundle.getString(FIND_LOCATION);
				cLatitude				= bundle.getDouble(LOC_LATITUDE, tLat);
				cLongitude			= bundle.getDouble(LOC_LONGITUDE, tLng);
			}
		}
		
		act.setResult(0);				//--default as no result, until address obtain successfully, for return to LocationAdd
			
		//setTargetFragment(getFragmentManager().getFragment(bundle, whoCalling),0);
		
		//--get SharedPreferences object----------------------------------
		mapChoice					= prefs.getInt("mapChoice", 5);							//--map type choice
		zoomSize						= prefs.getFloat("zoomSize", (float)13.0);		//--get last zoom size
		
		//--choice of map display
		switch (mapChoice) {
			case 5:
				googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				break;
			case 6:
				googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
				break;
			case 7:
				googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				break;
			case 8:
				googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
				break;
			default:
				googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				break;
		}
	
		//==========2.1 XML VIEW REFERENCE==================================
		addressText				= (EditText)getActivity().findViewById(R.id.locate_address);
		addressText.setMovementMethod(ScrollingMovementMethod.getInstance());
		
		parking						= new LatLng	(cLatitude, cLongitude);			//--last known or just Adelphi	
		whereAmI					= new LatLng (cLatitude, cLongitude);
				
		/*if (googleMap == null) 
			googleMap = mapView.getMap();*/
		
		if (haveCord) {
			huntAddress(whereAmI);					//--463
		} else {
			huntAddress(parking);
		}
				
		//Log.d(TAG,"368 setupView before googleMap.moveCamera");
		googleMap.setMyLocationEnabled(true);				//--ALWAYS CRASHED MAP
		
	
	}	//END setupView()
	
	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================
	private void addAdapter() {
		
		//--Part 2 of 3 ActionBar drop-down navigator
		//--setup action bar drop down navigation
		//spinnerAdapter = new ArrayAdapter(getActivity(),
		//          android.R.layout.simple_spinner_dropdown_item, mapType);		
	
	}
	
	//=====SECTION 4==ADD ADAPTER===================================

	//=====SECTION 5==SET LISTENER====================================

	public void setListener() {
		
		//======SANDY=====================================
		//--addressText search for address
		addressText.setOnEditorActionListener(new EditText.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				// TODO Auto-generated method stub
					huntLocation();
					return true;
				}
				return false;
			}
			
		});
		
		//=======SANDY======================================
		
		//--hide and unhide marker info
		googleMap.setOnMarkerClickListener( new GoogleMap.OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				
				infoHide = !infoHide;
				if (infoHide) {
					marker.hideInfoWindow();
				} else {
					marker.showInfoWindow();
				}
				return false;
			} 			
		});
				  
		//--set long click at marker to save location
		googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
			
			@Override
			public void onMapLongClick(LatLng arg0) {
				useLocation();
			}
		});
		
		//--set location marker at new map click and get address via reverseGeo 
		googleMap.setOnMapClickListener (new GoogleMap.OnMapClickListener() {
			
			@Override
			public void onMapClick(LatLng newLatLng) {
				
	            //--Getting the Latitude and Longitude of the touched location
                 //--Adding Marker on the touched location with address
                huntAddress(newLatLng);					//--463
			}
		});
		
		//--trace for zoom size change from on camera change listener
		googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
			
			@Override
			public void onCameraChange(CameraPosition arg0) {
				zoomSize = arg0.zoom;
				//Log.d(TAG,"468 on camera change listener "+zoomSize);				
			}
		});
		
		
	}	//END--setListener

	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================

	//=====SANDY====================================
	
	//--lookup address from LatLng call AsyncTask for reverse geo
	//--call from onMapClick 443, setupView 347
	public void huntAddress(LatLng useLatLng) {
		
		LatLng latLng				= useLatLng;
		try {
			newAddress = new ReverseGeocodingTask(getActivity()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, latLng).get();
			if (newAddress != null) {
				address				= newAddress;		//--set address with new address if newAddress not empty
				cLatitude 			= newAddress.getLatitude();
				cLongitude			= newAddress.getLongitude();
				foundLatLng		= latLng;					//--set as new foundLatLng only if valid address available
				
				showAddress		= getAddressString(newAddress);		//--574 get short address string
				
				//--do it only if address is available
				googleMap.clear();
				markerOptions	= new MarkerOptions().position(foundLatLng).title(showAddress);
				infoWindows 		= googleMap.addMarker(markerOptions);
				infoWindows.showInfoWindow();
				infoHide				= false;
				googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(foundLatLng, zoomSize));
			} 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//--look up LatLng and adddress from address string from addressText edit text field
	public void huntLocation() {
		if(Geocoder.isPresent()) {
			
	 		//--hide soft keyboard and exit
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 
		    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),      
		    InputMethodManager.HIDE_NOT_ALWAYS);

			//--get address input on addressText to addressString--
			String findAddress 	= addressText.getText().toString();
			Geocoder gcoder		= new Geocoder(getActivity());
			
			try {
				//--geocoder try to locate address from user inputs--
				address = new ReverseGeocodingAddressString(getActivity()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, findAddress.toString()).get();
				
				//--if found addresses more than zero get address and geo points--
				if (address != null) {
					cLatitude 		= address.getLatitude();
					cLongitude		= address.getLongitude();
					foundLatLng	= new LatLng (cLatitude, cLongitude);
					showAddress 	= getAddressString(address);
					
					googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(foundLatLng, zoomSize));
					googleMap.clear();
					infoWindows 	= googleMap.addMarker(new MarkerOptions()
													.position(foundLatLng)
													.title(showAddress));
				} 
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} //END try
		}
	}
	
	//--onMapLongClickListener--
	public void useLocation() {
		if (address != null ) {				//--|| sListener != null
			
			//--save zoom size to preferences
			editor.putFloat("zoomSize", zoomSize);
			editor.commit();
			
			Log.d(TAG, "663 useLocation.onLongClick");
			//--setters for information before pop back to the locationAdd fragment
			act.setAddress(address);									//--set address
			act.setRequestCode(MAP_LOCATION);				//--set resultRequestCode of callback fragment
			act.setResultCode(1);											//--set resultCode 1 success, default 0 failed
			getFragmentManager().popBackStackImmediate();	//--return to previous fragment
		}
	}

	//=====SECTION 6==LISTENER METHODS================================

	//=====SECTION 7==HELPER METHODS==================================
	
	public String getAddressString(Address thisAddress) {
		
		Address useAddress = thisAddress;
		
        /*displayText = String.format("%s, %s, %s",
            useAddress.getMaxAddressLineIndex() > 0 ? useAddress.getAddressLine(0) : "",
            useAddress.getCountryName(),
            useAddress.getPostalCode());*/
            
        //useLocation.setVisibility(View.VISIBLE);
		StringBuilder newText=new StringBuilder();		
		//if (address.getThoroughfare() != null)
		//	newText.append(address.getThoroughfare());
		//if (address.getFeatureName() != null) 
		//	newText.append(address.getFeatureName());	
		if (address.getMaxAddressLineIndex() > 0)
			newText.append(address.getAddressLine(0)).append(" ");
		if (address.getCountryName() != null)
			newText.append(address.getCountryName()).append(" ");
		if (address.getPostalCode() != null)
			newText.append(address.getPostalCode()).append(" ");
		//if (address.getLocality() != null)
		//	newText.append(", Locality: ").append(address.getLocality());
		
	    Log.d(TAG, "548 address string "+newText.toString());

	    return newText.toString();

	    
        //--Setting the title for the marker.
        //--This will be displayed on taping the marker
        //markerOptions.title(displayText);

        //--Placing a marker on the touched position
        //googleMap.addMarker(markerOptions);

	}	//--END showAddressTap
	

	
	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
			super.onPrepareOptionsMenu(menu);

			menu.removeItem(9);			//--removed Calculator
			menu.removeItem(10);		//--removed setting
			menu.removeItem(11);		//--removed help
	}

	private void CreateMenu(Menu menu) {
		
		SubMenu subMenu = menu.addSubMenu(R.string.map_view);
        subMenu.add(0,5,5,R.string.normal);
        subMenu.add(0,6,6,R.string.satellite);
        subMenu.add(0,7,7,R.string.hybrid);
        subMenu.add(0,8,8,R.string.terrain);
        
		//MenuItem mnu2=menu.add(0, 2, 2, R.string.map); 
		/*SubMenu mnu2=menu.addSubMenu(0, 2, 2, R.string.map); 
		{ mnu2.setIcon(android.R.drawable.ic_menu_agenda);
		((MenuItem) mnu2).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}	*/ 		//--put on action bar
				
	}
	
	private boolean MenuChoice(MenuItem item)
	{
		//--update shared preferences mapChoice for setupView
		editor.putInt("mapChoice", item.getItemId());
		editor.commit();
		Log.d(TAG, "634 MenuChoice item selected "+item.getItemId());
		
		//--"Normal", "Satellite", "Hybrid", "Terrain"
		//--choice of map display		
		switch (item.getItemId()) {
		case 1:
			// TODO Auto-generated method stub	
			break;
		case 5:
			googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			break;
		case 6:
			googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			break;
		case 7:
			googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			break;
		case 8:
			googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			break;
		default:
			googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			break;	
			//--done and returning to previous fragment stack
			//getFragmentManager().popBackStackImmediate();
		}
		return false;
	}
	
	//--Part 2 of 3 ActionBar drop-down navigator
	//--action bar drop down navigation menu
	/*@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		
		//--update shared preferences mapChoice for setupView
		editor.putInt("mapChoice", itemPosition);
		editor.commit();
		
		//--"Normal", "Satellite", "Hybrid", "Terrain"
		//--choice of map display		
		switch (itemPosition) {
			case 0:
				googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				break;
			case 1:
				googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
				break;
			case 2:
				googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				break;
			case 3:
				googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
				break;
			default:
				googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				break;
		}

		return false;
	}*/

	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//--Asyn task reverse geo coding from famous address string----------
	private class ReverseGeocodingAddressString extends AsyncTask<String, Void, Address> {
	    
	    public ReverseGeocodingAddressString(Context context) {
	        super();
	        mContext = context;
	    }

	    @Override
	    protected Address doInBackground(String... params) {
	        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

	        String addressString=params[0];
	        try {
	            // Call the synchronous getFromLocation() method by passing in the lat/long values.
 	        	addresses = geocoder.getFromLocationName(addressString, 1);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        
		        //-------------------------
				//--if found addresses more than zero get address and geo points--
				if (addresses.size() > 0 && addresses !=null) {
					Address newAddress = addresses.get(0);
					if (newAddress !=null) {
						return (newAddress);
					} else {
						return null;
					}
				} else {
					return (null);
				}
	    	}		
	    
		}	//--END ReverseGeocodingAddressString


	//--reverse geocoding from LatLng
	private class ReverseGeocodingTask extends AsyncTask<LatLng, Void, Address>{
        Context mContext;
		
 
        public ReverseGeocodingTask(Context context){
            super();
            mContext = context;
        }
 
        // Finding address using reverse geocoding
        @Override
        protected Address doInBackground(LatLng... params) {
            Geocoder geocoder = new Geocoder(mContext);
            double latitude = params[0].latitude;
            double longitude = params[0].longitude;
 
            List<Address> addresses = null;
            String addressText="";
 
            try {
                addresses = geocoder.getFromLocation(latitude, longitude,1);
            } catch (IOException e) {
                e.printStackTrace();
            }
 

            if (addresses != null && addresses.size() > 0) {
            	Address address = addresses.get(0);
            	return address;
            } else {
            	return null;
            }
        }
 
        /*@Override
        protected void onPostExecute(String addressText) {
            // Setting the title for the marker.
            // This will be displayed on taping the marker
            markerOptions.title(addressText);
 
            // Placing a marker on the touched position
            googleMap.addMarker(markerOptions); 
        }*/
        
    }		//END--ReverseGeoTasking



 
	//=====SECTION 10==SANDBOX======================================
	
	//=====SECTION 10==SANDBOX======================================	
	
	//==OLD CODES==========================
		    /* if(addresses != null && addresses.size() > 0 ){
			    Address address = addresses.get(0);
			
			    addressText = String.format("%s, %s, %s",
			    address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
			    address.getCountryName(),
			    address.getPostalCode()); 
			    
				/*useLocation.setVisibility(View.VISIBLE);
				StringBuilder addinfo=new StringBuilder()
				.append("Througfare: ")
				.append(address.getThoroughfare()).append(", FeatureName \n")
				.append(address.getFeatureName()).append(", PostalCode ")
				.append(address.getPostalCode()).append(", MaxAddressLine \n")
				.append(address.getMaxAddressLineIndex())
				.append(", Locality: ")
				.append(address.getLocality());
			} */
			//return addressText;

	
}	//--END MAIN CLASS
	
