/* 
 * Fragment: 						ViewHoliday.java
 * Description:						Holiday List View
 * 											Photo album, map view of all locations and location lists.
 * 
 * Created: 							May 3, 2012
 * Last release:					September 9, 2012
 * Last updated: 					October 24, 2013
 * 
 * Associated files:
 * view_list.xml					main view
 * HolidayListAdapter.java	custom adapter
 * list_item.xml					custom adapter view
 * 
  * Changes since last release:
 * 	- Tidy codes and documentation
 * - Extract all language strings to strings.xml
 * - Migrated to fragment
 * 
 * To do:
 * 
 */

package com.liseem.android.travel;


import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;
import static com.liseem.android.travel.TravelLiteActivity.*;

import com.liseem.android.travel.items.HLocation;
import com.liseem.android.travel.items.LPicture;
import com.liseem.android.travel.items.MyDate;
import com.liseem.android.travel.adapter.HolidayListAdapter;
import com.liseem.android.travel.items.Holiday;

import java.util.ArrayList;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class HolidayView extends Fragment {

	//=====SECTION 1==DECLARATION===================================

	private static final String 			TAG="ViewHoliday";
	
	private static final int 					MENU_DIALOG_ID=0;
	private static final String 			TABLESIZE="table";
	
	private final static String				FILTER_TYPE="filteredView";
	private final static int					FILTER_HOLIDAY=32;

	//--menu selection callback listener to PPGoPlaces----
	private OnMenuSelectedListener mCallback;

	//--application and global---------------------
	private PPGoPlacesApplication 	app;
	private SharedPreferences 			prefs;
	

	//--List View and Adapter--
	private HolidayListAdapter 			adapter;					//--custom adapter
	private TextView 							holidayText;
	private Button 								addHoliday;
	private ListView 							listView;
	private TextView 							emptyList;
	private ImageButton 						viewGallery;
	private ImageButton 						sayCheese;
	//private EditText 							searchText;
	private Holiday								oldHoliday;
	private Holiday 								holiday;
	private AlertDialog 						removeholiday;
	
	//--Data fields and variables--
	private ArrayList<HLocation> 		locationList;
	private ArrayList<Holiday> 			holidayList;
	private boolean 							hasHolidayAlbum;
	private String									viewSelected;
	private boolean							geoAvail;
	private boolean							hasLocation;

	private boolean 							newAPI=false;
	private boolean							dataConnection;			//--check for data network connection

	


	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	//--part 1 of 2 - container fragment interface for activity callback-----
	public interface OnMenuSelectedListener {
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
		setHasOptionsMenu(true);
		setRetainInstance(true);
		
		return v;			
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		//--call ActionBar------------------------------------------------------------------
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.show();
		actionBar.setDisplayShowTitleEnabled(false);		
	}
	
	@Override
	public void onResume() {
		super.onResume();

		setupView();								
		addAdapter(); 								
		setListener();								
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
	

	//=====SECTION 2==LIFECYCLE METHODS===========================END==

	//=====SECTION 3==SETUP VIEWS====================================
	
	private void setupView() {

		//--path to main application-----------------------------------------
		app								=(PPGoPlacesApplication)getActivity().getApplication();
		
		//--get SharedPreferences object--------------------------------
		prefs							=getActivity().getSharedPreferences(PREFNAME, MODE_PRIVATE);

		holiday						=new Holiday();
				
		//--setup resources views---------------------------------------------
		holidayText					= (TextView)getActivity().findViewById(R.id.infoText);
		viewGallery					= (ImageButton)getActivity().findViewById(R.id.viewButton);
		listView						=	(ListView)getActivity().findViewById(R.id.listItem);
		emptyList					= (TextView)getActivity().findViewById(R.id.emptyList);
		sayCheese					= (ImageButton)getActivity().findViewById(R.id.sayCheese);
		viewGallery.setVisibility	(View.INVISIBLE);
		listView.setEmptyView	(emptyList);
		sayCheese.setVisibility	(View.INVISIBLE);
		
		//searchText=(EditText)findViewById(R.id.searchText1);
		//searchText.setVisibility(View.INVISIBLE);
				
		
		//--check data network connection--------------------------------
		dataConnection=app.isOnline();
		
	}  	//--END setupView()

	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================

 	public void addAdapter() {
		
		/* Custom adapter that retrieve data and feed to list view
		 * list view inflate data received from adapter to android.R.id.list under hotlist.xml
		 * 
		 * below statement replaced the previous two adapter statements.
		 * setListAdapter (new ArrayAdapter<String> (this, android.list_item, listItems ));
		 * adapter and list view is for inflating row, not main view which is a function of 
		 * setContentView in onCreate() life cycle.
		 */
		
		//--initialize adapter and identify how the custom adapter get data via constructor----
		adapter						=new HolidayListAdapter (getActivity(), app.loadHolidays());
		
		//--set adapter that feed data to list view 
		//-- this instance of listview who own the resource android.id.list-----
		listView.setAdapter(adapter);
		
		//--register context menu to list view
		registerForContextMenu(listView);
		
	}		//--END addAdapter()


	//=====SECTION 4==ADD ADAPTER===================================

	//=====SECTION 5==SET LISTENER====================================
  	/*
	 * Important step as list view display is sorted by descending order, i.e. last first
	 * using table record size from loadHolList() less current list position, adding 1 to 
	 * offset 0 position taken by "Add New Holiday" button
	 */

	private void setListener() {			//--from onResume()
		
		//--This method send the onclick to a private class
		listView.setOnItemClickListener(new ItemClickedListener());
		viewGallery.setOnClickListener(new viewGalleryButtonListener());
		
	}		//--END setListener(0
	
	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================
	
	//--call from listView.setOnItemClickListener
	public class ItemClickedListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> listView, View convertView, int position,
				long id) {
					
					hasHolidayAlbum=false;
					holiday=(Holiday)adapter.getItem(position);			//--get holiday object from arraylist at position
				
					//--display holiday details in text view-------------------------
					if (null!=convertView) {	
					MyDate start=new MyDate();
					MyDate end=new MyDate();
					start.setStrDate(holiday.getStart_date().toString());
					end.setStrDate(holiday.getEnd_date().toString());
					
					StringBuilder tempStr=new StringBuilder();				//--build holiday details string for display
					tempStr
					.append(holiday.getHoliday().toString())
					.append("\nFrom: ").append(start.getDMYDate().toString())
					.append("\nTo: ").append(end.getDMYDate().toString());
					
					holidayText.setText(tempStr.toString());					//--display string in text field above list view
					
					//--check whether any location album available---------------------------
					ArrayList<LPicture> tempList=new ArrayList<LPicture>();
					ArrayList<HLocation> locationList=new ArrayList<HLocation>();
					locationList=app.loadLocations(holiday.getId(), true);			//--get all locations
					if (!locationList.isEmpty()) {
						for (HLocation h: locationList) {
							if (h.hasPicture()) {
								hasHolidayAlbum=true;							//--check album available for each location(s) 
								break;
							}
						}
					} 
										
					if (hasHolidayAlbum) {
						viewGallery.setVisibility(View.VISIBLE);
					} else {
						viewGallery.setVisibility(View.INVISIBLE);
					}
					//--end check album routine
			}
		}
	}

	//--call from addHoliday.setOnClickListener 
	public class addButtonListener implements OnClickListener {
	
		@Override
		public void onClick(View v) {
			
			//--goto add holiday---------------
			Bundle extras=new Bundle();
			mCallback.OnMenuSelected(2, extras);				//--About case 2	
		}
	}
	
	//--call from viewGallery.setOnClickListener
	//--routine to check if album is available found at least in 1 location, or there is location tagged to holiday
	private class viewGalleryButtonListener implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			
			//--goto photo album---------------
			//mCallback.OnMenuSelected("", true, holiday.getId(), 17);				//--PhotoAlbum case 2	
			Bundle extras=new Bundle();
			extras.putBoolean(HOLIDAY_ALBUM, true);			//--holidayAlbum is true
			extras.putLong(LOC_ROWID, holiday.getId());		//--holiday id
			mCallback.OnMenuSelected(17, extras);				//--PhotoAlbum case 2	
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode==HOLIDAY_ADD) {
			if (resultCode==Activity.RESULT_OK) {
				Holiday newHoliday=new Holiday();
				long newHolId=data.getLongExtra(HOL_ROWID, 0);
				newHoliday=app.getHoliday(newHolId);
				adapter.addHoliday(newHoliday);
			}
		} else if (requestCode==HOLIDAY_EDIT) {
			if (requestCode==Activity.RESULT_OK) {
				Holiday newHoliday=new Holiday();
				newHoliday=app.getHoliday(holiday.getId());			//where oldHoliday??
				adapter.updateHoliday(newHoliday, holiday);
			}
		}
	}
	//=====SECTION 6==LISTENER METHODS================================

	//=====SECTION 7==HELPER METHODS==================================

	//--obsoleted
	//--check any location tag to holiday, and location has valid latitude and longitude
	private void checkLocationInfo() {
		ArrayList<HLocation> hLocations=new ArrayList<HLocation>();
		hLocations=app.loadLocations(holiday.getId(), true);
		if (hLocations.isEmpty()) {
			hasLocation=false;
		}else{
			hasLocation=true;
		}
		
		if (hasLocation) {
			for (HLocation h: hLocations) {
				if (h.getLatitude() !=0 && h.getLongitude() !=0) {
					geoAvail=true;
				}
			}
		}
	}
		
	//--check any location tag to holiday, and location has valid latitude and longitude
	private void checkHomeLocationInfo() {
		ArrayList<HLocation> hLocations=new ArrayList<HLocation>();
		hLocations=app.getHomeLocations();
		if (hLocations.isEmpty()) {
			hasLocation=false;
		}else{
			hasLocation=true;
		}
		
		if (hasLocation) {
			for (HLocation h: hLocations) {
				if (h.getLatitude() !=0 && h.getLongitude() !=0) {
					geoAvail=true;
				}
			}
		}
	}
	//=====SECTION 7==HELPER METHODS=============================END==

	//=====SECTION 8==MENU AND DIALOG METHODS===========================
	
	//--remove calculator from action bar-------------------------------------------------------
	@Override
	public void onPrepareOptionsMenu (Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		menu.removeItem(9);
	}

	//--menu listing action from above context menu choice-------------------------------	
	private void CreateMenu(Menu menu) {
		
	/*MenuItem mnu1=menu.add(0,1,1, R.string.settings);
		{ mnu1.setIcon(android.R.drawable.ic_menu_preferences);}

	MenuItem mnu2=menu.add(0,2,2, R.string.help);
		{ mnu2.setIcon(android.R.drawable.ic_menu_help); }*/
		
	MenuItem mnu3=menu.add(0,3,3, R.string.sort_by_start_date);
		{ mnu3.setIcon(android.R.drawable.ic_menu_sort_by_size); }
		
	MenuItem mnu4=menu.add(0,4,4, R.string.sort_by_name);
		{ mnu4.setIcon(android.R.drawable.ic_menu_sort_by_size); }
		
	MenuItem mnu8=menu.add(0, 8, 8, R.string.new_);
		{ 	mnu8.setIcon(android.R.drawable.ic_menu_add); 			
			mnu8.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}		
			
	}
		
	private boolean MenuChoice(MenuItem item)
	{
		SharedPreferences.Editor editor = prefs.edit();
		switch (item.getItemId()) {
		case 1:		//--not use, use activity default
			//--go to settings menu-----------------------------------------------------------------
			Bundle extras = new Bundle();
			mCallback.OnMenuSelected(13, extras);				//--Settings case 13		
			return true;
		case 2:		//--not use, jumping to help page not working
			//--get help----------------------------------------------------------------------------------
			//mCallback.OnMenuSelected("mainmenu.html", false, 0, 18);				//--Help case 19
			Bundle zextras=new Bundle();
			zextras.putString("helpPage", "viewholiday.html");
			mCallback.OnMenuSelected(18, zextras);				//--Help case 19
			return true;
			
		case 3:
			//--sort list view by date-----------------------------------------------------------------
			editor.putInt("sortHolidayList", 1);
			editor.commit();
			adapter.sortHolidayView(1);
			return true;
			
		case 4:
			//--sort listview by name------------------------------------------------------------------
			editor.putInt("sortHolidayList", 2);
			editor.commit();
			adapter.sortHolidayView(2);
			return true;
			
		case 8:
			//--Action Bar - add New vacation------------------------------------------------------
			Bundle vextras=new Bundle();
			mCallback.OnMenuSelected(2, vextras);				//--Add Location case 2 	
			return true;
			
		}
		return false;
	}
	//--menu choice-----------------------------------------------------------------------------------------------------
	
	//--context menu---------------------------------------------------------------------------------
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
			menu.setHeaderIcon(android.R.drawable.ic_menu_more);
			menu.setHeaderTitle(R.string.holiday_menu);
			menu.add(0,  0, 0, R.string.edit_);
			menu.add(0,  1, 1, R.string.delete_);
			menu.add(0,  2, 2, R.string.add_new_location);
			menu.add(0,  3, 3, R.string.view_locations);
			menu.add(0,  4, 4, R.string.view_locations_map);
			menu.add(0,  5, 5, R.string.shopping_list_for_holiday);
			//menu.add(0,  6, 6, R.string.view_home_locations);
			super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item){
		AdapterContextMenuInfo info=(AdapterContextMenuInfo) item.getMenuInfo();
		holiday=(Holiday) adapter.getItem(info.position);

		switch (item.getItemId()) {
			//case R.id.editItem:
			case 0:
				//--Edit Holiday--------------------------------------------
				Bundle extras=new Bundle();
				extras.putLong(HOL_ROWID, holiday.getId());
				mCallback.OnMenuSelected(3, extras);				//--Holiday Edit case 3	
				return true;
				
			//case R.id.deleteItem:
			case 1:
				//--Delete Holiday------------------------------------------------------
				deleteThisHoliday();
				return true;
				
			case 2:
				//--Add new location--------------------------------------------
				((FragMain)getActivity()).setRequestCode(HOLIDAY_VIEW);
				Bundle lextras=new Bundle();
				lextras.putBoolean(FILTER_LOCATION, true);
				lextras.putLong(HOL_REFID, holiday.getId());
				mCallback.OnMenuSelected(5, lextras);				//--Location Add case 5	
				return true;
				
			case 3:
				//--View holiday locations---------------------------------------------
				checkLocationInfo();
				//hasLocation=app.hasHolidayLocations(holiday.getId(), true);
				if(hasLocation){
					//mCallback.OnMenuSelected("", true, holiday.getId(), 5);				//--Location Add case 5	
					Bundle hextras=new Bundle();
					hextras.putBoolean(FILTER_LOCATION, true);
					hextras.putInt(FILTER_TYPE, FILTER_HOLIDAY);
					hextras.putLong(HOL_REFID, holiday.getId());
					mCallback.OnMenuSelected(5, hextras);				//--Location Add case 5	

			    	/*Intent listLoc=new Intent(HolidayView.this, LocationView.class);
					Bundle hextras=new Bundle();
					hextras.putBoolean(FILTER_LOCATION, true);
					hextras.putInt(FILTER_TYPE, FILTER_HOLIDAY);
					hextras.putLong(HOL_REFID, holiday.getId());
					listLoc.putExtras(hextras);
					startActivity(listLoc);*/
					return true;					
				} else {
					((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.No_location_info));
					return false;
				}
				
			case 4:
				//--View holiday location maps---------------------------------------------
				if (app.isOnline()) {
					checkLocationInfo();		//5.4 valid location information available
					if (geoAvail && hasLocation) {
						//mCallback.OnMenuSelected("", true, holiday.getId(), 19);				//--Map Locations Add case 19							
						Bundle mextras=new Bundle();
						mextras.putBoolean(FILTER_LOCATION, true);
						mextras.putLong(HOL_REFID, holiday.getId());
						mCallback.OnMenuSelected(19, mextras);				//--Map Locations Add case 19							
						return true;
					} else {
						((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.No_location_info));
						return false;
					}
				} else {
					((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.No_network_for_map_show));
					return false;
				}
			case 5:
				//--VIEW SHOPPING LIST---------------------------------------------
				//mCallback.OnMenuSelected("", true, holiday.getId(), 11);				//--Holiday Shopping List case 11											
				Bundle sextras=new Bundle();
				sextras.putBoolean(FILTER_LOCATION, true);
				sextras.putLong(HOL_REFID, holiday.getId());
				mCallback.OnMenuSelected(11, sextras);				//--Holiday Shopping List case 11											
				return true;
				
			case 6:
				//--View home locations---------------------------------------------
				checkHomeLocationInfo();				
				if (geoAvail && hasLocation) {
					//mCallback.OnMenuSelected("", true, holiday.getId(), 4);				//--Locations View case 4												
					Bundle aextras=new Bundle();
					aextras.putBoolean(FILTER_LOCATION, true);
					aextras.putInt(FILTER_TYPE, FILTER_HOLIDAY);
					aextras.putLong(HOL_REFID, 0);
					mCallback.OnMenuSelected(4, aextras);				//--Locations View case 4												
					return true;
				} else {
					((TravelLiteActivity)getActivity()).showOkAlertDialog(getString(R.string.No_location_info));
					return false;
				}
				
			default:
				return super.onContextItemSelected(item);
		}
	}
	//--context menu----------------------------------------------------------END-------------------			

	
	//--delete Holiday alert dialog
	//--call from 5.1, case 1: delete holiday--
	public void deleteThisHoliday() {
		removeholiday= new AlertDialog.Builder(getActivity())
		.setTitle(R.string.holiday_action)
		.setMessage(getString(R.string.delete_this_holiday)+
				getString(R.string.note_deleted_record_is_not_recoverable))
		.setPositiveButton(R.string.yes_delete, new AlertDialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				app.deleteHoliday(holiday);
				adapter.deleteHoliday(holiday);
				removeholiday.dismiss();
			}
		})
		.setNegativeButton(R.string.cancel_action, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				removeholiday.cancel();
			}
		})
		.create();
		removeholiday.show();
	}

	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//=====SECTION 10==SANDBOX======================================


	
} 	//END MAIN CLASS
