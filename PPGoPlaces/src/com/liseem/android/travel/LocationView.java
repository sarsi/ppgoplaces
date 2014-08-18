/*
 * Fragment: 								ViewLocation.java
 * Description:								View all locations created directly or via holiday, including all locations 
 * 													tagged to holidays.
 * 
 * Created: 									May 3, 2012
 * Changed last release: 			September 6, 2012
 * Last updated: 							October 26, 2013 
 * Clean up:									December 2, 2013
 *  
 * Associated files:
 * view_list.xml							main view
 * ItemListAdapter.java 				custom adapter
 * custom_list.xml						custom adapter view
 * 
 * Changes:
 * 	- migrated to fragment
 * 
 * To do:
 *- need to persist filter onResume
 *- need to fix 334 currentLocation, getIntent() ??
 *- need to fix View Location Map, still in old mapview
 * 
 */

package com.liseem.android.travel;


import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;
import static com.liseem.android.travel.TravelLiteActivity.*;

import android.content.SharedPreferences;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

import android.webkit.WebSettings;
import android.webkit.WebView;


import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.liseem.android.travel.adapter.LocationListAdapter;
import com.liseem.android.travel.items.HLocation;
import com.liseem.android.travel.items.Holiday;
import com.liseem.android.travel.items.LPicture;
import com.liseem.android.travel.items.MyDate;


public class LocationView extends Fragment  {

	//=====SECTION 1==DECLARATION===================================

	private static final String 			TAG="ViewLocation";
	
	private static final int 					MENU_DIALOG_ID=0;
	private final static int 					TAKE_PICTURE=101;
	
	private final static String				FILTER_TYPE="filteredView";
	private final static int					FILTER_NONE=31;
	private final static int					FILTER_HOLIDAY=32;
	private final static int					FILTER_GEO=33;
	
	private OnMenuSelectedListener 	mCallback;
	
	private PPGoPlacesApplication 	app;
	private FragMain							act;
	private SharedPreferences 			prefs;
	private SharedPreferences.Editor	editor;
	
	private boolean							isExtStorageAvail;
	private boolean 							scrollTextMode;


	//private ArrayAdapter adapter;
	private LocationListAdapter 			adapter;
	private AlertDialog 						removeLocation;

	//--List View and Adapter-------------------------------------
	private LPicture 								newPicture;
	private HLocation 							hLocation;
	private Holiday								holiday;
	private Button 								addLocation;
	private ImageButton 						viewGallery;
	private ImageButton 						sayCheese;
	private TextView 							locationText;
	private ListView 							listView;
	private TextView 							emptyList;
	private MyDate 								todayDate;
	private TextView							distView;
	
	
	//--DB Specific-----------------------------------------------------
	private int										filteredView;			//--identify filter state, FILTER_NONE, FILTER_HOLIDAY & FILTER_GEO
	private long 									newRowId;				//--return rowId from add location intent
	private long 									dbrowid;
	private long									holRefId;					//--holiday refid from intent
	private String 								viewSelected;
	private boolean 							jump2Gallery=false;
	private boolean							albumAvail=false;
	private int										listPosition;
	private boolean							hasHoliday;				//--bundle return holiday information
	
	private String 								updates=new String();
	private String 								deviceName;
	private Bitmap 								mapImage;

	//--camera specific-------------------------------------------------
	private boolean 							tookPhoto;
	private Uri 										imageUri;

	//--location services dialog, do not show dialog-------------
	private CheckBox 							dontShow;
	private boolean							skipLocMsg;
	
	//--check network and data availability------------------------
	private boolean 							networkAvail;				//--check for network enabled
	private boolean							mobileAvail;				//--check for mobile enabled
	private boolean							dataConnection;			//--data network available, i.e 3G or WiFi
	private boolean							hasCoverage;				//--no cell or wifi coverage even for location
	
	//--share my location, intent filter--------------------------------
	private File										objectFile;					//--share location object file
	private String									shareFile;						//--object file name
	private HLocation							newLocation;				//--use by AsyncTask
	private HLocation							shareLocation;				//--share location with user approved share info 
	private EditText								searchText;					//--autotext filter
	private String 								fileName;
	private String 								emailSignature; 
	private AlertDialog 						selectLocInfo;
	private String									mapImgStr;					//--use by AsyncTask
	
	//--reference and filter locations------------------------------------
	private AlertDialog 						selectFilter;
	private int										selectChoice;
	private String									refDialogMsg;
	private boolean							refHasCtry;
	private boolean							hasRefLoc;	
	private String 								refCountry;
	private Location								currentLocation;
	private boolean							geoFence;

	
	//--PopUp Dialog Menu------------------------------------------------
 	private CharSequence[] locationItems = {
													"Address", 
													"Street Name",
													"City",
													"Postal Code",
													"Country",
													"Location Date"
													};
 	
 	
	//private boolean[] itemsChecked = new boolean [locationItems.length];
	private boolean[] itemsChecked = new boolean[] {true, true, true, true, true, false};
	
	//--PopUp Dialog ListView Filter-----------------------------------------
	private CharSequence[] filterItems;


	
	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	//--part 1 of 2 - container fragment interface for activity callback-----
	public interface OnMenuSelectedListener {
		//public void OnMenuSelected (String param, boolean bol, long id, int func);		
		public void OnMenuSelected (int func, Bundle bundle);		
	}	

	//--part 2 of 2 - container fragment interface for activity callback-----
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
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
		View v=inflater.inflate(R.layout.view_list, container, false);
		setRetainInstance(true);
		setHasOptionsMenu(true);
		
		return v;			
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		//--call ActionBar------------------------------------------------------------------
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.show();
		actionBar.setDisplayShowTitleEnabled(false);
		
		setupView();		//--2.0
		addAdapter();		//--3.0
		setListener();		//--4.0
	}


	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	

	@Override
	public void onDestroy() {
		super.onDestroy();
		searchText.removeTextChangedListener(listView);
	}
	
	//--Create menu--
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

	private void setupView() {			//Continue from onCreate() prevent reload dbInfo

		//--Path to PPGoPlacesApplication--
		//--need to retry notifySetChanged from non UI thread--
		app							=(PPGoPlacesApplication)getActivity().getApplication();
		act							= (FragMain)getActivity();

		
		//--check external storage writeable------------------------
		isExtStorageAvail	=((TravelLiteActivity) getActivity()).checkExternalStorageWriteWrite();
		
		//--shared preferences-----------------------------------------------------------------     
 		prefs						=getActivity().getSharedPreferences (PREFNAME, MODE_PRIVATE);
 		deviceName			=prefs.getString("deviceName","samsung");
		skipLocMsg				=prefs.getBoolean("skipLocMsg", false);						//skip location msg
		emailSignature		=prefs.getString("emailSignature", DEFAULT_SIGNATURE);
		scrollTextMode		=prefs.getBoolean("scrollTextMode", true);
		
 		//--display tips for add location if skipLocMsg is false-------------------------
		//skipLocMsg=false;			//--ONLY unremark to test addLocationTipsMsg() show
		if (!skipLocMsg)					//--if false, show addLocationTipsMsg()
			{addLocationTipsMsg(); }
		
		//--retrieve bundle from view holiday intent-------------
		Bundle bundle			= this.getArguments();
		if (bundle !=null) {
			hasHoliday			=bundle.getBoolean(FILTER_LOCATION);
			filteredView			=bundle.getInt(FILTER_TYPE);
			holRefId				=(long)bundle.getInt(HOL_REFID);
			currentLocation	=getActivity().getIntent().getParcelableExtra("MyLocation");		//get location via parcelable extra
		}
		
		if (!hasHoliday) {
			filteredView			=FILTER_NONE;				//--FILTER_LOCATION false
		}
		else {
			if (holRefId==0) {
				filteredView		=FILTER_GEO;					//--No holRefId
			} else {
				filteredView		=FILTER_HOLIDAY;			//--filter by holiday
			}
		}
		
		//--setup view resources--------------------------
		//refLocText			=(TextView)findViewById(R.id.refLoc);
		searchText			=(EditText)getActivity().findViewById(R.id.searchText1);
		locationText		=(TextView)getActivity().findViewById(R.id.infoText);
		//addLocation			=(Button)getActivity().findViewById(R.id.addButton);
		viewGallery			=(ImageButton)getActivity().findViewById(R.id.viewButton);
		sayCheese			=(ImageButton)getActivity().findViewById(R.id.sayCheese);
		listView				=(ListView)getActivity().findViewById(R.id.listItem);
		emptyList			=(TextView)getActivity().findViewById(R.id.emptyList);
		listView.setEmptyView(emptyList);
		searchText.setVisibility(View.VISIBLE);
		viewGallery.setVisibility(View.INVISIBLE);
		sayCheese.setVisibility(View.INVISIBLE);
		
		locationText.setMovementMethod(ScrollingMovementMethod.getInstance());
		locationText.setVerticalScrollBarEnabled(true);
		
		if (filteredView==FILTER_GEO) {
			distView			=(TextView)getActivity().findViewById(R.id.distView);
			distView.setVisibility(View.VISIBLE);
			addLocation.setVisibility(View.INVISIBLE);
		}
		
	    //--forcefully close soft keyboard by default----------------------------------
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    imm.hideSoftInputFromWindow(searchText.getWindowToken(),0);


		//--miscellaneous-----------------------
		hLocation				=new HLocation();
		holiday				=new Holiday();
		newPicture			=new LPicture();
		todayDate			=new MyDate();
		
		Calendar thisDate= Calendar.getInstance();
		todayDate.setCalDate(thisDate);
		
		//--check data network connection----------------
		dataConnection	=app.isOnline();
		//Log.d(TAG, "dataConnection "+dataConnection);
		
		//--housekeeping delete ppgp cache file-----------
		new deleteCacheFile().execute(PPGP_CACHE);	//housekeeping routine in TLA
	
	}	//--END 2.0 SetUpView()*/
	
	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================
	
	private void addAdapter() {
		
		//===========3.1 ACTION MENU SELECTION ONITEMCLICK=================
		//adapter=new LocationListAdapter (this, app.loadLocations(holrowid, filterLocation));	
		ArrayList<HLocation> locationList = new ArrayList<HLocation>();
		switch (filteredView) {
		case FILTER_NONE:
			locationList 		= app.getAllLocations();
			break;
		case FILTER_HOLIDAY:
			locationList 		= app.getHolidayLocations(holRefId);
			break;
		case FILTER_GEO:
			try {				
				locationList	=new locationWithinGeoFence().execute(currentLocation).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}		
		}
		
		adapter = new LocationListAdapter (getActivity(), locationList);
		listView.setAdapter(adapter);
		registerForContextMenu(listView);
	}	//--END addAdapter
	
	//=====SECTION 4==ADD ADAPTER===================================

	//=====SECTION 5==SET LISTENER====================================

	private void setListener() {
		
		viewGallery.setOnClickListener(new viewGalleryButtonListener());		//--511
		listView.setOnItemClickListener(new ItemClickedListener());					//--438
		//checkBox.setOnCheckedChangeListener(new listItemCheckedChangeListener());
		sayCheese.setOnClickListener(new sayCheeseClickListener());				//--531
		searchText.addTextChangedListener(new searchTextWatcher());			//--549
		
		if(searchText != null && searchText.length()>0)										//--restore filter onResume
						adapter.getFilter().filter((CharSequence)searchText.getText().toString());
	}	
	
	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================	
				
	public class ItemClickedListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View convertView, int position,
				long id) {
				hLocation=(HLocation) adapter.getItem(position);
				//convertView.setBackgroundColor(Color.TRANSPARENT);
			
			//--show album button if there is picture tag to location-----------
			if (hLocation.hasPicture()) {
				viewGallery.setVisibility(View.VISIBLE);
			} else if (!hLocation.hasPicture()) {
				viewGallery.setVisibility(View.INVISIBLE);
			}
			
			
			//--setup for photo shot-----------------------------------------------------
			sayCheese.setVisibility(View.VISIBLE);				
			newPicture=new LPicture();
			newPicture.setRefid(hLocation.getId());
			newPicture.setLocation(hLocation.getName().toString());
			newPicture.setAddress(hLocation.getAddress().toString());
			newPicture.setDate(todayDate.getDMYDate().toString());
			newPicture.setLatitude(hLocation.getLatitude());
			newPicture.setLongitude(hLocation.getLongitude());
			
			if (null!=convertView) {
				
				StringBuilder tempStr=new StringBuilder();				
				//tempStr.append(hLocation.getName().toString()).append("\n");
				tempStr.append(hLocation.getAddress().toString());
				
				//--cannot resolve doverview reflect errorsous distance after change
				if (filteredView==FILTER_GEO && hLocation.getDistanceHere() >= 0) {
					distView.setText("About "+ hLocation.getDistanceHere()+"m away.");
					//Log.d(TAG, "on item click distanceHere position is at "+position);
				}

				if (tempStr.length()>0) {
					locationText.setText(tempStr.toString());
				} else {
					locationText.setText("No Address Found");
				}
				
				listPosition=position;			//item position int
				dbrowid=hLocation.getId();
			} 
		}
	}
	
	//--add new location---------------------------------------------------------------------
	protected class addButtonListener implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			//Intent intent=new Intent(LocationView.this, LocationAdd.class);
			Bundle lextras=new Bundle();
			if (hasHoliday) {
				//Log.d(TAG,"In addButtonListener, holrowid "+holRefId);
				lextras.putBoolean(FILTER_LOCATION, true);
				lextras.putLong(HOL_REFID,holRefId);
			} else {
				lextras.putBoolean(FILTER_LOCATION, false);
				lextras.putLong(HOL_REFID,0);
			}
			//intent.putExtras(lextras);
			//startActivityForResult(intent, LOCATION_ADD);
			mCallback.OnMenuSelected(5, lextras);
		}
		
	}
	
	//--goto to photo album------------------------------------------------------------------
	protected class viewGalleryButtonListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			
			if (isExtStorageAvail) {
				//Intent myAlbum=new Intent(LocationView.this, PhotoAlbum.class);
				Bundle locAlbum=new Bundle();
				locAlbum.putLong(LOC_ROWID, hLocation.getId());
				locAlbum.putBoolean(HOLIDAY_ALBUM, false);
				mCallback.OnMenuSelected(17, locAlbum);
				//myAlbum.putExtras(local);
				//startActivity(myAlbum);			 
			} else {
				((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.sdcard_is_not_found));
			}
		}		
	}

	//--call default camera to take picture----------------------------------------------
	protected class sayCheeseClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
						
			/*ContentValues values=new ContentValues();
			values.put(MediaStore.Images.Media.TITLE, "test");
			values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by PPGoPlaces");
			imageUri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null);*/
			imageUri=MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

			Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(intent, TAKE_PICTURE);				
		}
		
	}
	
	//--search text------------------------------------------------------------------------------
	protected class searchTextWatcher implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
				adapter.getFilter().filter(s);
		}		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
			case TAKE_PICTURE:
				if (resultCode==Activity.RESULT_OK) {
					
					tookPhoto=true;
					String imagePath;
					if(data.getData()!=null) {
						imagePath=((TravelLiteActivity) getActivity()).getPath(data.getData());
					} else {
						imagePath=((TravelLiteActivity) getActivity()).getLastImagePath();			//for samsung phone return data null
						//imagePath=getPath(imageUri);				
					}
					newPicture=new LPicture();
					newPicture.setRefid(hLocation.getId());
					newPicture.setLocation(hLocation.getName());		
					if (hLocation.getAddress() !=null)
						newPicture.setAddress(hLocation.getAddress().toString());
					if (hLocation.getLatitude() >0 && hLocation.getLongitude()>0) {
						newPicture.setLatitude(hLocation.getLatitude());
						newPicture.setLongitude(hLocation.getLongitude());
					}
					newPicture.setPicPath(imagePath.toString());
					app.insertPicture(newPicture);
					hLocation.setPicture(true);
					app.updateHLocation(hLocation);
					((TravelLiteActivity) getActivity()).makeShortToast(getActivity().getString(
							R.string.nice_shot_new_picture_added_to_album));
				}
				break;
			
			case LOCATION_EDIT:
				if (resultCode==RESULT_OK) {
					HLocation newLocation=new HLocation();
					newLocation=app.getHLocation(hLocation.getId());
					
					//int itemPosition=app.getLastPosition();				
					//oldLocation=app.getLastHLocation();				
					//HLocation newLocation=new HLocation();
					//newLocation=app.getHLocation(oldLocation.getId());

					if (filteredView==FILTER_GEO) {
						long geoDist = app.getGeoFence();
						long distApart=findGeofence(currentLocation, newLocation);
						if (distApart < geoDist) {
							adapter.updateLocation(hLocation, newLocation);
							//Log.d(TAG, "get distance from distanceHere "+distanceHere.get(itemPosition));
						} else {
							//adapter.deleteLocation(hLocation);
							adapter.deleteLocation(listPosition);
						}
					} else {
						//adapter.updateLocation(itemPosition, newLocation);
						adapter.updateLocation(hLocation, newLocation);
					}
				}
				break;
			
			case LOCATION_ADD:
				if (resultCode==RESULT_OK) {
					HLocation newLocation=new HLocation();
					long newLocId=data.getLongExtra(LOC_ROWID, 0);				
					newLocation=app.getHLocation(newLocId);
					adapter.addLocation(newLocation);
					//adapter.notifyDataSetChanged();
				}	
				break;
				
			case TAG_PICTURE:
				if (resultCode==RESULT_OK) {
					String imagePath;
					if(data.getData()!=null) {
						imagePath=((TravelLiteActivity) getActivity()).getPath(data.getData());
					
						newPicture=new LPicture();
						newPicture.setRefid(hLocation.getId());
						newPicture.setLocation(hLocation.getName());		
						if (hLocation.getAddress() !=null)
							newPicture.setAddress(hLocation.getAddress().toString());
						if (hLocation.getLatitude() >0 && hLocation.getLongitude()>0) {
							newPicture.setLatitude(hLocation.getLatitude());
							newPicture.setLongitude(hLocation.getLongitude());
						}
						newPicture.setPicPath(imagePath.toString());
						app.insertPicture(newPicture);
						hLocation.setPicture(true);
						app.updateHLocation(hLocation);
						((TravelLiteActivity) getActivity()).makeShortToast(getActivity().getString(
								R.string.new_picture_tag_to_location_album));
					}
				}
				break;
				
		}		//--END switch

	}			//--END onActivityResult
	
	//=====SECTION 6==LISTENER METHODS================================

	//--location services tip dialog, do not show on user check---------------
	private void addLocationTipsMsg() {	
		
		AlertDialog.Builder listenForLocation= new AlertDialog.Builder(getActivity());
		
		//--create checkbox for don't show this message in again-----------
		LayoutInflater adbInflater = LayoutInflater.from(getActivity());
        View listenNot = adbInflater.inflate(R.layout.checkbox, null);
        dontShow=(CheckBox)listenNot.findViewById(R.id.skipCheck);
        dontShow.setVisibility(View.VISIBLE);
        WebView webview=(WebView)listenNot.findViewById(R.id.webView1);
        WebSettings settings = webview.getSettings();
		settings.setJavaScriptEnabled(true);
		webview.loadUrl("file:///android_asset/tipaddnewlocation.html");
        
        listenForLocation.setView(listenNot);
        listenForLocation.setIcon(android.R.drawable.ic_menu_myplaces);
		listenForLocation.setTitle("Add Location Tips");
		//listenForLocation.setMessage("The program will continue to listen " +
		//		"for new location update."+
		//		"\n\nNOTE: \nTo preserve battery life, \"Stop Location Updates\" when listening for new location and address is no " +
		//		"longer required. ");
		listenForLocation.setPositiveButton("OK", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {				
				//boolean checkBoxResult=false;
				if (dontShow.isChecked()) {
					Toast.makeText(getActivity().getBaseContext(), "This \"ReadMe First\" message will not show again.", Toast.LENGTH_SHORT).show();
					skipLocMsg=true;
					SharedPreferences.Editor editor=prefs.edit();
					editor.putBoolean("skipLocMsg", true);
					editor.commit();
				}
			}
		});
		listenForLocation.create().show();
	}
	
	//=====SECTION 7==HELPER METHODS==================================

	//--receive and add location object------------------------------------------------------
	/*protected void getFromIntent() {
		
		if (shareFile != null)
			new createLocationFromFile().execute(shareFile.toString());		
	}*/
	

	//--select reference location---------------------------not use--------------------------
    /*private void setReferenceLocation() {
    	refLocation=new HLocation();
    	refLocation=hLocation;
    	refLocId=refLocation.getId();
    	
    	//--update shared preference-----------------------
    	SharedPreferences.Editor editor=prefs.edit();
    	editor.putLong("refLocId", refLocation.getId());
    	editor.commit();
    	
    	//--update text view----------------------------------
    	refLocText.setText("Reference Location: "+refLocation.getName());
    }*/

	//--Context menu 4 - create and share location object and show dialog chooser-----------------------------------------------
	//--shareMyLocation Part 1 of 4, showLocationDialog 2 of 5
	public void shareMyLocation() {
				
		//Log.d(TAG,"shareMyLocation Landed 1");
		shareLocation=new HLocation();
		shareLocation=hLocation;		//create object for sharing
		
		//--set non-shareable information-------------
		shareLocation.setPicture(false);		
		shareLocation.setHoliday(false);
		shareLocation.setNotes("");
		shareLocation.setInfo("");
		shareLocation.setId(0);
		shareLocation.setRefid(0);	
		shareLocation.setAddressObj(null);	//too big too share	
		selectLocationInfoDialog();					//Part 2 of 5
	}
	
	//--share location context menu choice---------------------------------
	//--shareMyLocation Part 2 of 4
	protected void selectLocationInfoDialog() {
		
			selectLocInfo=new AlertDialog.Builder(getActivity())
			.setIcon(R.drawable.ppgp_pt)
			.setTitle(shareLocation.getName().toString())
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					sendFileAttached();		//createFileNow();
					selectLocInfo.dismiss();
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					selectLocInfo.cancel();
				}
			})
			//--locationItems=Address,Street Name, City, Postal Code, Country, Object, Location Date
			.setMultiChoiceItems(locationItems, itemsChecked, new DialogInterface.OnMultiChoiceClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					//Toast.makeText(getBaseContext(), locationItems[which] + (isChecked ? 
					//		" selected" : " unselected"),Toast.LENGTH_SHORT).show();
					if (isChecked) {
						switch (which) {
							case 0:
								if (hLocation.getAddress() !=null)
								shareLocation.setAddress(hLocation.getAddress().toString());
								break;
							case 1:
								if (hLocation.getStreet() !=null)
								shareLocation.setStreet(hLocation.getStreet().toString());
								break;
							case 2:
								if (hLocation.getCity() !=null)
								shareLocation.setCity(hLocation.getCity().toString());
								break;
							case 3:
								if (hLocation.getPostal() !=null)
								shareLocation.setPostal(hLocation.getPostal().toString());
								break;
							case 4:
								if (hLocation.getCountry() !=null)
								shareLocation.setCountry(hLocation.getCountry().toString());
								break;
							case 5:
								if (hLocation.getLdate() !=null)
								shareLocation.setLdate(hLocation.getLdate().toString());
								break;
							/*case 6:
								if (hLocation.getAddressObj() != null)
								shareLocation.setAddressObj(hLocation.getAddressObj());
								break;*/
							}
						} else if (!isChecked) {
							switch (which) {
								case 0:
									shareLocation.setAddress("");
									break;
								case 1:
									shareLocation.setStreet("");
									break;
								case 2:
									shareLocation.setCity("");
									break;
								case 3:
									shareLocation.setPostal("");
									break;
								case 4:
									shareLocation.setCountry("");
									break;
								case 5:
									shareLocation.setLdate("");
								/*case 6:
									shareLocation.setAddressObj(null); */
							} 	//switch !isChecked
						}		//else if
					}			//onClick
			})
			.create();
			selectLocInfo.show();

	}


	//--start share intent from return infomation from onPostExecute from async
	//--shareMyLocation Part 4 of 4
	public void  sendFileAttached() {
		
		if (isExtStorageAvail) {
			
			File fileSelected;
			boolean fileCreated;
			//Log.d(TAG,"createFileNow landed");
			try {
				fileSelected=new createLocationFile().execute(shareLocation).get();		//Part 3 of 4 TravelLiteActivity
				
				//--mail body-------------------------------------------------
				StringBuilder fileInfo=new StringBuilder();
			
				//--email body text by user----------------------------------
				fileInfo.append("Note: This attachement can only be retrieved in PPGoPlaces application. " +
						"Click on attachement to add location to PPGoPlaces\'s Locations").append("\n\n");
				
				//--item details------------------------------------------	
				fileInfo.append("\nLocation: ").append(hLocation.getName().toString());
				if (hLocation.getAddress() != null)
					fileInfo.append("\nAddress: ").append(hLocation.getAddress().toString());		
			
				
				//--email signature--------------------------------------------
				fileInfo.append("\n\n")
					.append("[").append(app.getSignature().toString()).append("]");
				
				//--start intent to send location object-------------------------
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			    sendIntent.setType("file/ppgp");
			
			    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Location: "+hLocation.getName().toString());
			    sendIntent.putExtra(Intent.EXTRA_TEXT, fileInfo.toString());   
			    if (fileSelected.exists()) {
			    	Uri uri=Uri.fromFile(fileSelected);
			    	sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
			    }
			    
			    //sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://sdcard/dcim/Camera/filename.jpg"));
			    startActivity(Intent.createChooser(sendIntent, "Send Email.:"));    
			} catch (InterruptedException e) {
				((TravelLiteActivity) getActivity()).showOkAlertDialog("Interrupted Execption: Cannot Complete Request Action");
				e.printStackTrace();
			} catch (ExecutionException e) {
				((TravelLiteActivity) getActivity()).showOkAlertDialog("Executeion Exception: Cannot Complete Request Action");
				e.printStackTrace();
			}
		} else {
			((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.sdcard_is_not_found));
		}
	}
	
	
	//--filter dialog--------------------
	//--Part 1 of 2
	/*public void filterSetup() {
		
		if (refLocation.getCountry() !=null) {
			refCountry=refLocation.getCountry().toString();
			refHasCtry=true;
		} else {
			refHasCtry=false;
		}
		
		if (refLocation.getLatitude() >0 && refLocation.getLongitude() >0) {
			refHasLoc=true;
			
		} else {
			refHasLoc=false;
		}
		
		//StringBuilder menuString=new StringBuilder();
		//menuString.append("Show All Locations");
		if (!refHasCtry && !refHasLoc)  {
			filterItems=new CharSequence[] {"Show All Locations", refLocation.getCountry().toString(), "Within 1km Radius" };
			filterChoiceDialog();			
		} else if (!refHasCtry && refHasLoc) {
			filterItems=new CharSequence[] {"Show All Locations", "Within 1km Radius" };
			filterChoiceDialog();			
		} else if (refHasCtry && !refHasLoc) {
			filterItems=new CharSequence[] {"Show All Locations", refLocation.getCountry().toString()};		
			filterChoiceDialog();			
		} else {
			showOkAlertDialog ("No location and country information available, cannot perform filter action");
		}
	}*/
	
	
	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================	

	//--remove settings menu from filterview, may cause view to go hay wired
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		menu.removeItem(9);			//--removed Calculator
		if (filteredView==FILTER_GEO || filteredView==FILTER_HOLIDAY) {
			menu.removeItem(2);
		}
	}

	
	//--menu listing action from above context menu choice---------------------------------------
	private void CreateMenu(Menu menu) {
		
		MenuItem mnu1=menu.add(0,1,1, R.string.add); 
			{	mnu1.setIcon(android.R.drawable.ic_menu_add);
			mnu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}
		
		MenuItem mnu2=menu.add(0,2,2,"Clear Default"); 
			{	mnu2.setIcon(android.R.drawable.ic_menu_close_clear_cancel);}
			
		MenuItem mnu5=menu.add(0,4,4, "Sort By Date");
			{ mnu5.setIcon(android.R.drawable.ic_menu_sort_by_size); }
		
		MenuItem mnu6=menu.add(0,5,5, "Sort By Name");
			{ mnu6.setIcon(android.R.drawable.ic_menu_sort_by_size); }
	}
	

	private boolean MenuChoice(MenuItem item)
	{
		SharedPreferences.Editor editor=prefs.edit();
		switch (item.getItemId()) {

		case 1:
			//--add new location--------------------------------------
			long noHol			= '0';
			Bundle lextras	=new Bundle();
			if (hasHoliday) {
				//Log.d(TAG,"In addButtonListener, holrowid "+holRefId);
				lextras.putBoolean(FILTER_LOCATION, true);
				lextras.putLong(HOL_REFID,holRefId);
			} else {
				lextras.putBoolean(FILTER_LOCATION, false);
				lextras.putLong(HOL_REFID, noHol);
			}
			mCallback.OnMenuSelected(5, lextras);
			return true;
			
		case 2:
			editor.putLong("defaultLocation", 0);
			editor.commit();
			return true;
			
		case 3:
			//--get help-------------------------------------------------------
			Bundle zextras=new Bundle();
			zextras.putString("helpPage", "viewlocation.html");
			mCallback.OnMenuSelected(18, zextras);				//--Help case 19
			return true;
			
		case 4:
			//--sort listview by date---------------------------------------
			editor.putInt("sortList", 1);
			editor.commit();
			adapter.sortLocationView(1);
			return true;
			
		case 5:
			//--sort listview by name--------------------------------------
			editor.putInt("sortList", 2);
			editor.commit();
			adapter.sortLocationView(2);
			return true;
			
		/*case 6:
			if(refLocId>0)
				filterSetup();
			return true;		*/		
		}
		return false;
	}
	//--menu choice----------------------------------------------------------------------------------
	
	//--context menu---------------------------------------------------------------------------------
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
			menu.setHeaderIcon(android.R.drawable.ic_menu_more);
			menu.setHeaderTitle(R.string.location_menu);
			//MenuInflater inflater = getActivity().getMenuInflater();
			//inflater.inflate(R.menu.context_menu, menu);
			
			
			menu.add(0, 0, 0, R.string.edit);
			menu.add(0, 1, 1, R.string.delete_);
			menu.add(0, 2, 2, R.string.set_as_default);
			menu.add(0, 3, 3, R.string.share_location);
			menu.add(0, 4, 4, R.string.view_map);
			menu.add(0, 5, 5, R.string.view_saved_map);		
			menu.add(0, 6, 6, R.string.add_photo_to_album);

			//menu.add(0, 7, 7, "Save Image To File");
			
			/*if (!holidayLoc)
				menu.add(0, 6, 6, "Set As Reference");*/
			//if (!holidayLoc)
			//	menu.add(0, 7, 7, "View Location Image");
			//menu.addSubMenu(0, 3, 3, "Add Photo To Album");
			super.onCreateContextMenu(menu, v, menuInfo);
	}

	
	@Override
	public boolean onContextItemSelected(MenuItem item){
	
		AdapterContextMenuInfo info=(AdapterContextMenuInfo) item.getMenuInfo();
		//hLocation=(HLocation) adapter.getItem(info.position);
		hLocation=app.getHLocation(adapter.getItemId(info.position));
		listPosition=info.position;
		app.setLastHLocation(hLocation);
		app.setLastPosition(info.position);

		switch (item.getItemId()) {
			case 0:
				//--Edit Location------------------------------------------------------------
				//Log.d(TAG, "app.setLastPosition "+info.position);				
				act.setRequestCode(LOCATION_VIEW);
				Bundle editHol=new Bundle();
				editHol.putLong(LOC_ROWID, hLocation.getId());
				editHol.putLong(HOL_REFID, hLocation.getRefid());				
				editHol.putBoolean(HAS_HOLIDAY, hLocation.hasHoliday());
				mCallback.OnMenuSelected(6, editHol);
				return true;
				
			case 1:
				//--Delete Location--------------------------------------------------------
				deleteThisLocation();		//--1158
				return true;
				
			case 2:
				//--Set Default Location--------------------------------------------------
				SharedPreferences.Editor editor=prefs.edit();
				editor.putLong("defaultLocation", hLocation.getId());
				editor.commit();
				return true;
				
			case 3:
				//--Share Location Object File-------------------------------------------
				shareMyLocation();			//--743
				return true;
				
			case 4:	
				//--View Location Map-----------------------------------------------------
				//dataConnection=app.isOnline();
				if (dataConnection) {
					if (hLocation.getLatitude() !=0 && hLocation.getLongitude() !=0) {
						Bundle bundle=new Bundle();
						bundle.putLong(LOC_ROWID, hLocation.getId());
						mCallback.OnMenuSelected(20, bundle);
						return true;
					} else {
						((TravelLiteActivity) getActivity()).showOkAlertDialog(getActivity().getString(
								R.string.no_location_information_found));
						return false;
					}
				} else {
					((TravelLiteActivity) getActivity()).showOkAlertDialog(getActivity()
							.getString(R.string.no_data_network_connection));
					return false;
				}
				
			case 5:
				//--View Map Bitmap-----------------------------------------------------------
				if (hLocation.getAddressObj() != null) {
					showMapImageDialog() ;		//--1247
					return true;
				} else {
					((TravelLiteActivity) getActivity()).showOkAlertDialog("No saved image available");
					return false;
				}
				
			case 6:
				//--Add picture from Gallery----------------------------------------------------
				Bundle moreInfo=new Bundle();
				moreInfo.putLong(LOC_ROWID, hLocation.getId());
				mCallback.OnMenuSelected(21, moreInfo);
				
				//--50 Hacks ready to use snippets
				//--pick single picture from picture gallery, cannot prevent duplicate select
				/*Intent pickIntent 	= new Intent(Intent.ACTION_GET_CONTENT);
				pickIntent.setType("image/*");
				Intent chooserIntent = Intent.createChooser(pickIntent, "Tag Picture");
				startActivityForResult(chooserIntent,TAG_PICTURE);*/
				return true;
				
			//case 7:
				//--Save image to file in ../download directory--------------------------
				/*if (hLocation.getAddressObj() != null)
					new saveImageToFile().execute(hLocation);		//--sect 9
				return true; */
				
			//case 6:
			//	//--SET AS REFERENCE LOCATION---------------------------------------------
			//	setReferenceLocation();							//--728
			//	return true;
				
			default:
				return super.onContextItemSelected(item);
		}
	} 
	//--context menu----------------------------------------------------------END-------------------


	//--Context menu 2 - delete Location action dialog
	//--permenant delete location 
	public void deleteThisLocation() {
		removeLocation = new AlertDialog.Builder(getActivity())
		.setTitle(R.string.location_action)
		.setMessage(getString(R.string.delete_this_location_)+
				getString(R.string._note_deleted_record_is_not_recoverable_))
		.setPositiveButton(R.string.yes_delete, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//app.deleteLocation(hLocation);
				app.deleteLocation(hLocation);
				adapter.deleteLocation(listPosition);
				//Log.d(TAG, "deleteThisLocation() before Intent()");	
				removeLocation.dismiss();
			}
		})
		.setNegativeButton(R.string.cancel_action, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				removeLocation.cancel();
			}
		})
		.create();
		removeLocation.show();
		
	} //--END deleteThisLocation


	
	//--Part 2 of 2----------not use-----------------------
	//--display listview filter single choice dialog-----------------------------
	/*public void filterChoiceDialog() {
		
		selectFilter=new AlertDialog.Builder(this)
		.setIcon(R.drawable.ppgp_pt)
		.setTitle("Reference to location \""+refLocation.getName().toString()+"\"")
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				switch (selectChoice) {
					case 0:
						adapter.resetLocationFilter();
						
						break;
					case 1:
						if (refHasCtry) {
							//if (refLocation.getCountry() != null)
							adapter.filterByRefCountry(refLocation);
						} else {
						//if (refLocation.getLatitude() > 0 && refLocation.getLongitude() > 0)
							adapter.filterLocationByGeo(refLocation, LOCATION_FILTER_DISTANCE);
						}
						break;
					case 2:
						//f (refLocation.getLatitude() > 0 && refLocation.getLongitude() > 0)
							adapter.filterLocationByGeo(refLocation, LOCATION_FILTER_DISTANCE);
						break;
				}
				selectFilter.dismiss();
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
							selectFilter.cancel();
			}
		})
		.setSingleChoiceItems(filterItems, -1, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {

				//Toast.makeText(getBaseContext(), filterItems[which]+" of location \""
				//		+refLocation.getName().toString()+"\"", Toast.LENGTH_SHORT).show();
				selectChoice=which;
				
			}
		})
		.create();
		selectFilter.show();
	} */
	
	//--Context menu 6 - display saved image---------------------------------
	protected void showMapImageDialog() {
		
		//--retrieve byte[] from db--------------------------------------------------
		byte[] imageByteArray=hLocation.getAddressObj();
		ByteArrayInputStream imageStream=new ByteArrayInputStream(imageByteArray);
		mapImage = BitmapFactory.decodeStream(imageStream);
		
		//--show alert dialog-----------------------------------
		AlertDialog.Builder showPicture = new AlertDialog.Builder(getActivity());
		
		showPicture.setTitle(hLocation.getName().toString());
		showPicture.setIcon(R.drawable.ppgp_pt);
		LayoutInflater showBiz=LayoutInflater.from(getActivity());
		View itemPhoto=showBiz.inflate(R.layout.map_image, null);
		ImageView image = (ImageView) itemPhoto.findViewById(R.id.itemImage);

		image.setImageBitmap(mapImage);

		showPicture.setView(itemPhoto);
		showPicture.setPositiveButton(R.string.ok, new AlertDialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
			
		});
		showPicture.create().show();
		
	}		//--END show map image dialog

	
	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//--Part 1 of 2 - get geo fence list of locations 
	private class locationWithinGeoFence extends AsyncTask<Location, Void, ArrayList<HLocation>> {

		@Override
		protected ArrayList<HLocation> doInBackground(Location... params) {
			Location currentLocation = params[0];
			ArrayList<HLocation>  allLocation = new ArrayList<HLocation>();
			allLocation=app.getAllLocations();
			long distance=app.getGeoFence();
			ArrayList<HLocation>  geoList = new ArrayList<HLocation>();
			for (HLocation l : allLocation) {
				if (l.getLatitude() >0 && l.getLongitude() >0) {
					long locDistance=findGeofence(currentLocation, l);
					//Log.d(TAG, "isWithin Geofence return from part 1 "+isWithin);
					if (locDistance < distance){
						l.setDistanceHere(locDistance);
						geoList.add(l);
					}
				}
			}
			return geoList;
		}
	}	//--END locationWithinGeoFence
	
	//--Part 2 of 2 - return distance between current location and hLocation
	private long findGeofence (Location currentLocation, HLocation hLocation) {
		float[] distanceArray = new float[1];
		Location.distanceBetween(		
				currentLocation.getLatitude(),
				currentLocation.getLongitude(),
				hLocation.getLatitude(),
				hLocation.getLongitude(),
				distanceArray);		//boolean within=(distanceArray[0] < distance);
		long distanceApart=(long)distanceArray[0];
		return distanceApart;
	}	//--END findGeofence
	
	
	

	//--ViewLocation, called in setupView() to delete cache
	//--DELETE CACHE FILE-------------------------------------------------------------------
	//--HOUSEKEEPING async task to delete ppgp cache file-------------------------------------------
	protected class deleteCacheFile extends AsyncTask <String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String dirName=params[0];
			File dir = new File(Environment.getExternalStorageDirectory(), dirName);
			if (dir.exists()) {
				File[] toBeDeleted = dir.listFiles(new FileFilter() {
					@Override
					public boolean accept (File theFile) {
					if (theFile.isFile()) {
						return theFile.getName().endsWith(".ppgp");
					}
						return false;
					}
				});
				
				for (File deletableFile : toBeDeleted) {
					deletableFile.delete();
				}					
			}
			
			if (dir.exists()) {
				File[] toBeDeleted = dir.listFiles(new FileFilter() {
					@Override
					public boolean accept (File theFile) {
					if (theFile.isFile()) {
						return theFile.getName().endsWith(".png");
					}
						return false;
					}
				});
				
				for (File deletableFile : toBeDeleted) {
					deletableFile.delete();
				}					
			}
			return null; 
		}		
	}	//--END deleteCacheFile
	
	
	//--migrate from viewlocation shareMyLocation----
	//--write serialize HLocation object to file------------------------------------------
	public class createLocationFile extends AsyncTask <HLocation, Void, File> {
		boolean fileCreated;
		
		@Override
		protected File doInBackground(HLocation... params) {
			HLocation hLocation=new HLocation();
			String fileName;
			
			hLocation=params[0];									//--retreive location object
			
			//--NEW FILENAME-----------------------------------------
			//--create file name with 4 chars from location and rest from location date--------
			fileName=hLocation.getName().toString();
			fileName=fileName.replaceAll("\\s", "");		//--remove spaces
			int nameLength=fileName.length();
			if (nameLength<9) {
				fileName=fileName.toString()+"ppgoplaces";
			}
			
			StringBuilder newName=new StringBuilder().append(fileName.substring(0, 8)).append(".ppgp");
			fileName=newName.toString();						//--recreate fileName with 8 chars ext ppgp
			
			//--create temp file directory or set path to temp directory----------------------------
			//--"Android/data/com.liseem.android.travel/ppgoplaces/cache" moved to constant
			File exportDir=new File(Environment.getExternalStorageDirectory(), PPGP_CACHE);
			if (!exportDir.exists()) {
				exportDir.mkdirs();
			}
			File exportFile=new File(exportDir, fileName);		//put in /mnt/sdcard/Android/com...travel/cache
			//Log.d(TAG, "export file path "+exportFile.getAbsolutePath());
			
			//----------------------------------------------------------------

			//--CREATE FILE FROM OBJECT-------------------------------	
			try {
				FileOutputStream fout=new FileOutputStream(exportFile, true);
				//FileOutputStream fout=openFileOutput (fileName, MODE_WORLD_READABLE); //only  work on getFilesDir
				ObjectOutputStream oos=new ObjectOutputStream(fout);
				oos.writeObject(hLocation);
				oos.flush();
				oos.close();
				//Log.d(TAG,"Good so far far");
				return exportFile;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}			

		}		//--END doInBackground
		
		@Override
		protected void onPostExecute(File newfile) {
			if (newfile.exists()) {
				Toast.makeText(getActivity().getBaseContext(), R.string.file_saved_successfully_, 
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getActivity().getBaseContext(), R.string.file_create_not_successful_, 
						Toast.LENGTH_SHORT).show();
				
			}
		}
	}		//--END createLocationFile

	
	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//=====SECTION 10==SANDBOX======================================

	//--NOT CURRENTLY USE----create png file from bitmap, store in cache directory--------------
		protected class createBitMapFile extends AsyncTask <Bitmap, Void, String> {
			
			@Override
			protected String doInBackground(Bitmap... params) {
				//Log.d(TAG, "createBitMapFile async doInBackground landed");
				
				//--get bitmap------------------------------------------------------------------
				Bitmap bmp=params[0];
				
				//--create file name for bitmap image------------------------------------
				String fileName=hLocation.getName().toString();
				fileName=fileName.replaceAll("\\s", "");
				int nameLength=fileName.length();
				if (nameLength<9) {
					fileName=fileName.toString()+"ppgoplaces";
				}
				StringBuilder newName=new StringBuilder().append(fileName.substring(0, 8)).append(".png");
				fileName=newName.toString();
				
				
				//--create directory and file to write bitmap---------------------------------
				File exportDir=new File(Environment.getExternalStorageDirectory(), "cache/ppgoplaces");
				if (!exportDir.exists()) {
					exportDir.mkdirs();
				}
				File newFile=new File(exportDir, fileName);
				FileOutputStream out;
				try {
					out = new FileOutputStream(newFile);
				
				//--write bitmap to file-------------------------------------------------------------
				//--PNG format, use JPEG for jpg, 0-100 100 is high quality, out is outputstream file
					bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
					return newFile.getAbsolutePath().toString();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				return null;
				
			}		//--END doInBackground

			@Override
			protected void onPostExecute(String result) {
				if (result != null) {
					mapImgStr=result.toString();
					//Log.d(TAG," in onPostExecute, mapImgStr path "+mapImgStr.toString());
				}
				//super.onPostExecute(result);
			}
		}

	
} 			//--END MAIN CLASS
