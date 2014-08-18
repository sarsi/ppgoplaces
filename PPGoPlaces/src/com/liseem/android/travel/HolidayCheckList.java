/*
 * Fragment: 											HolidayCheckList.java
 * Description:											Holiday To Do and Packing Lists
 * 																To do list for pre to post holiday and a convenient packing list.
 * BoilerPlate:											TEMPLATE FRAGMENT LIFECYCLE AND MENUS
 * 
 * Created: 												May 3, 2012
 * Last release: 										September 9, 2012
 * Last updated: 										October 1, 2013
 * 
 * 
 * Associated files:
 * Intent call from main fragment: 		PPGoPlaces.java
 * Extract db info via dbHelper:	 			TravelLiteDBAdapter.java
 * Custom view adapter: 						ShoppingListItem.java and custom_list.xml for item view
 * Item View: 											custom_list.xml
 * Object File:											taskList.java
 * 
 * Resources files:
 * dropdown.xml:									spinner item view
 * view_task.xml:									main view
 * view_checklist.xml:							main parent view
 * 
 * 
 * Changes since last release:
 * - migrated to fragment
 * - change theme to default theme.Holo
 * - added ActionBar and hide buttons
 * - complete fragment reference boilerplate
 * 
 * 
 * OPTIONS, ACTIONBAR AND CONTEXT MENU
 * - public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)		//--call up options menu
 * - public onPrepareOptionsMenu(Menu menu) 												//--optional remove and add from default
 * - public boolean onOptionsItemSelected(MenuItem item)							//--when an option is selected
 * - private void CreateMenu(Menu menu)															//--create menu for selection
 * - private boolean MenuChoice(MenuItem item)												//--execute selected menu item
 * - registerForContextMenu(listView)																//--attach context menu to a listview
 * - public void onCreateContextMenu(ContextMenu menu, View v,
 *           ContextMenuInfo menuInfo)																	//--create context menu for selection
 * - public boolean onContextItemSelected(MenuItem item)							//--execute selected context item
 * - private void addMenuToActionBar(Menu menu)											//--create actionbar menu for selection
 * - ActionBar actionBar = getActionBar();															//--onCreate() or onStart() call actionbar
 *   - actionBar.setDisplayHomeAsUpEnabled(true);											
 *   - actionBar.setHomeButtonEnabled(false);
 *   - actionBar.setDisplayUseLogoEnabled(false);											//--hide logo
 *   - actionBar.setDisplayShowTitleEnabled(false);											//--hide title on title bar
 *   - actionBar.setDisplayShowHomeEnabled(false);										//--enable home menu
 */


package com.liseem.android.travel;

import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;
import static com.liseem.android.travel.TravelLiteActivity.*;

import com.liseem.android.travel.adapter.TaskListAdapter;
import com.liseem.android.travel.items.TaskList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;



public class HolidayCheckList extends Fragment {

	//=====SECTION 1==DECLARATION===================================

	private static final String 			TAG="HolidayCheckList";
	
	private static final int 					MENU_DIALOG_ID=0;
	private static final int 					SHOWALL=4;						//show all task position on spinner

	protected static final String		USERCHECKLIST="userchecklist.txt";
	private static final int 					READ_BLOCK_SIZE=100;
	private static final int 					IO_BUFFER_SIZE=4*1024;
	
	private OnMenuSelectedListener	mCallback;
	
	private PPGoPlacesApplication 	app;
	private SharedPreferences 			prefs;
	private SharedPreferences			category;
	private SharedPreferences.Editor	editor;
	private SharedPreferences.Editor	catEditor;
	private Context								context;	

	private boolean							isExtStorageAvail;
	private boolean							firstCheckIn;
	
	//--Dialog for loadDefaultCheckList--
	CharSequence[] items 			= { 	"his packing list", "her packing list", "cancel" }; //--cannot use R.String

	int itemsChecked;
	

	//--Data Container --------------------------------
	private ArrayList<String> 			listItems;
	private ArrayList<Integer> 			questFRP;
	private ArrayList<Long> 				filterList;
	private ArrayList<TaskList> 		taskList;
	private ArrayList<TaskList> 		allTaskList;					

	private TaskList 					 		task;
	private ArrayList<TaskList> 		userList; 						//--use by async task to readFileToString
	private String									currentList;
	private String[] 								checkListItems;
	private int										taskPosition;				//--position on item click
	private String									helpPage="checklist.html";
	
	//--View ---------------------------------------------
	private TaskListAdapter 				adapter;
	private ListView 							listView;		
	private TextView							emptyList;
	private Button 								addTask;
	private Spinner	 							taskCat;				
	private ToggleButton						hideComplete;		
	
	private AlertDialog 						actionSelectionDialog;
	private AlertDialog 						removeItem;
	private AlertDialog 						deleteItems;
	private AlertDialog 						loadDefaultList;			//--loadDefaultCheckList
	private boolean 							hers;							//--choice for his or her packing list	

	//--Program Containers ----------------------------
	private int 										dbRowId;						//--item rowid pass to intent
	private int										itemCat;						//--item category pass to intent for spinner select

	//--Activity state------------------------------------
	private Handler 								handler;
	private int 										selectList;					//--position of spinner
	private boolean 							notComplete;				//--status of hide or show all
	private String									taskShowAll;				//--toggle text for action bar menu
	private boolean 							onHoliday;					//--pass from calling activitiy need to implement shared pref
	private ProgressDialog					pDialog;
	
	//--Spinner for passing to intent-----------------
	private int 										listPosition;
	

	
	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	//--Part 1 of 2 - container fragment interface for activity callback-----
	public interface OnMenuSelectedListener {
		public void OnMenuSelected (int func, Bundle bundle);		//--calling OnMenuSelected method at activity that implements interface
	}	

	//--Part 2 of 2 - container fragment interface, listen for activity callback-----
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.view_checklist);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		setHasOptionsMenu(true);								//--use fragment options menu instead of activity default
		//setRetainInstance(true);
		
		//--inflate layout for fragment------------------------------------------------
		//return inflater.inflate(R.layout.view_checklist, container, false);
		if(container==null) 
			return null;

		//--inflate layout for fragment ------------------------------------------------
		View v=inflater.inflate(R.layout.view_checklist, container, false);
		return v;			
	}
			
	@Override
	public void onStart() {
		super.onStart();	
		
		//--call ActionBar------------------------------------------------------------------
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.show();
		actionBar.setDisplayShowTitleEnabled(false);
		
		app				=(PPGoPlacesApplication)getActivity().getApplication();
		prefs			=getActivity().getSharedPreferences (PREFNAME, MODE_PRIVATE);		
		category		=getActivity().getSharedPreferences (CHECKLIST, MODE_PRIVATE);
		editor			=prefs.edit();
		
		//SharedPreferences.Editor editor=prefs.edit();		
		//selectList	=prefs.getInt("selectList", 4);
		task				=new TaskList();
		firstCheckIn	=prefs.getBoolean("firstCheckIn", true);
		//notComplete=prefs.getBoolean("hideComplete", false);
		
		//--if older versionCode before 11 create new sharedpreferences for category
		if (!firstCheckIn) {			
			boolean checkCat=category.contains("TaskCat8");
			if (!checkCat) {
				createCategory();		
			}
		}
		
		//--check external storage writeable-----------------------------------------------------
		isExtStorageAvail=((TravelLiteActivity) getActivity()).checkExternalStorageWriteWrite();		
		
		//--check if it first access to checklist, if true load new checklist-----------------
		if (firstCheckIn) {
			//SharedPreferences.Editor editor=prefs.edit();
			editor.putBoolean("firstCheckIn", false);
			editor.commit();
			
			//--create initial category in shared preferences------------------------------------
			createCategory();	
			
			//--setup default checklist and packing list 
			setupDefaultPackingList();
		}
	
		setupView();		//--2.0
		addAdapter();		//--3.0			//--leave it here to reload adapter upon return from edit or add
		setListener();		//--4.0		
	}

	@Override
	public void onResume() {
		super.onResume();
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
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//--Save vulnerable information--
		outState.putInt("selectList", selectList);										//--category spinner position
		outState.putBoolean("notComplete", notComplete);					//--show all choice
	}
	
	//--Create Options menu---------------------------------
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		//onPrepareOptionsMenu(menu);
		CreateMenu(menu);
		//return true;		
	}

	//--Remove default Activity menu------------------------------
	//--remark out as there is no default activity menu
	/*@Override
	public void onPrepareOptionsMenu(Menu menu) {
			menu.removeItem(1);
			menu.removeItem(2);
			menu.removeItem(3);
			menu.removeItem(4);
			menu.removeItem(5);
	}*/
	
	//--Return menu choice on menu selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		return MenuChoice(item);
	}
	


	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 3==SETUP VIEWS====================================
	
	private void setupView() {
		
		//--Get intent from holiday screen for display on Holiday tasks--------------------------------------
		Bundle bundle 	= this.getArguments();							//--fragment retrieve bundle
		//Bundle bundle	= getActivity().getIntent().getExtras();	//--old activity bundle
		if (bundle!=null) {
			onHoliday		= bundle.getBoolean("onHoliday");
		}		

	
		
		//--SetUpViews----------------------------------------------------------------------------------------------------
		taskCat			= (Spinner)getActivity().findViewById(R.id.taskCat);
		addTask			= (Button)getActivity().findViewById(R.id.addButton);
		listView			= (ListView)getActivity().findViewById(R.id.listItem);
		emptyList		= (TextView)getActivity().findViewById(R.id.emptyList);
		hideComplete	= (ToggleButton)getActivity().findViewById(R.id.showNotcomplete);
		listView.setEmptyView(emptyList);
		
		//--BUTTON OBSOLETED BY ACTION BAR-------------------------------------------------------------------
		addTask.setVisibility(View.GONE);
		hideComplete.setVisibility(View.GONE);		
		}

	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================
	
	private void addAdapter() {	
		
		//--setUp Adapter and Listview--------------------------------------------------------------------------------------------
		adapter				= new TaskListAdapter(getActivity(), app.loadAllTasks());
		listView.setAdapter(adapter);
		registerForContextMenu(listView);									//--must register for context menu to work
		
		//--Setup Spinner for task category--------------------------------------------------------------------------------------- 
		loadCheckListCat();									//--load checkListItems from category shared preferences
		ArrayAdapter<String> dataAdapter=new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_spinner_item, checkListItems);
		dataAdapter.setDropDownViewResource(R.layout.dropdown_item); //custom to remove radio button
		taskCat.setAdapter(dataAdapter);
		
		//--retrieve user last choice for category and show all choices----------------------------------------------------
		selectList				= category.getInt("Default", 4);			//--retreive choice from sharedPreferences
		notComplete		= category.getBoolean(getString(R.string.hide), false);	 //--retreive choice from sharedPreferences
		taskCat.setSelection(selectList);
		hideComplete.setChecked(notComplete);
		if (notComplete) {															//--display initial action bar user setting
			taskShowAll 		= getString(R.string.show_all);
		} else {
			taskShowAll 		= getString(R.string.hide);
		}
		
		//--call adapter method that build list for adapter base on show all and category choice------------------
		adapter.spinnerSelection(selectList, notComplete);		
	}
	
	//--load checklist category for category spinner--------------------------------------------------------------------------
	private void loadCheckListCat() {
		
		//--load checklistitems from sharedPreferences----------------
		ArrayList<String> catList = new ArrayList<String>();
		catList=app.loadCheckListChoice();
		
		//--String array used by spinner data adapter-------------------
		checkListItems=catList.toArray(new String[catList.size()]);
	}
	
	//=====SECTION 4==ADD ADAPTER===================================

	//=====SECTION 5==SET LISTENER====================================

	//--Define view listeners and call methods----------------------------------------------------------------------------------
	public void setListener() {
	
		listView.setOnItemClickListener(new CheckedOnItemClickListener());
		addTask.setOnClickListener( new addTaskButtonListener());
		taskCat.setOnItemSelectedListener(new onCategorySelected());
		hideComplete.setOnCheckedChangeListener(new hideCompletedTasksListener());
	}

	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================	
	
	//--Add new checklist task-------------------------------------------------------------------------------------------------------
	private class addTaskButtonListener implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			//Intent intent=new Intent(HolidayCheckList.this, CheckAddTask.class);
			Bundle extras=new Bundle();
			extras.putInt("rowid", 0);
			extras.putBoolean("editmode", false);
			mCallback.OnMenuSelected(10, extras);
			//intent.putExtras(extras);
			//startActivityForResult(intent, TASK_ADD);
		}
	}	
	
	//--Tasks category dropdown selection menu-----------------------------------------------------------------------------
	private class onCategorySelected implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {

			selectList=position;			//--position 0-3 holiday, 4 show all, 8/9 packing list 
			//Log.d(TAG, "landed in onItemSelect, position "+position);
			
			notComplete=category.getBoolean("Hide", notComplete);
			SharedPreferences.Editor catEdit=category.edit();			
			catEdit.putInt("Default", position);
			catEdit.commit();
			
			adapter.spinnerSelection(position, notComplete);
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {		}
		
	}

	//****UNUSED LISTENER METHOD REPLACE BY SHOW ALL IN ACTION BAR------TO BE REMOVED****
	//--FILTER toggle button to hide all completed tasks
	//-- to prevent list out of sync, two parameters isChecked and selectList
	//--need to be sent to the adapter--
	//OnCheckedChangeListener hideCompletedTasksListener=new OnCheckedChangeListener() {
	private class hideCompletedTasksListener implements OnCheckedChangeListener {
	
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			
			notComplete=isChecked;
			selectList=category.getInt("Default", selectList);
			SharedPreferences.Editor catEdit=category.edit();			
			catEdit.putBoolean("Hide", isChecked);
			catEdit.commit();
			
			adapter.spinnerSelection(selectList, notComplete);
		}
	}

	//--Check and uncheck task action--------------------------------------------------------------------------------------------
	private class CheckedOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view , int position,
				long id) {
			taskPosition	=position;									//--saved item position
			adapter.toggleItem(position);								//--first toggle item
			task					=adapter.getItem(position);		//--then get item with new state
			if (task != null)
				app.updateTask(task);									//--finally update db
		}
	}
	

	
	//=====SECTION 6==LISTENER METHODS=================================

	//=====SECTION 7==HELPER METHODS==================================
	
	//--create sharedPreferences for checklist category-----
	protected void createCategory() {
		//category=getSharedPreferences (CHECKLIST, MODE_PRIVATE);
		SharedPreferences.Editor catEdit = category.edit();
		catEdit.putInt("Default", 1);
		catEdit.putBoolean(getActivity().getString(R.string.hide), false);
		catEdit.putString("TaskCat1", TASK_MENU_1);
		catEdit.putString("TaskCat2", TASK_MENU_2);
		catEdit.putString("TaskCat3", TASK_MENU_3);
		catEdit.putString("TaskCat4", TASK_MENU_4);
		catEdit.putString("TaskCat8", TASK_MENU_8);
		catEdit.commit();
	}
	
	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================
	
	//--Context Menu -- Continue from delete single selected item----------------------------------------		
	//--delete Item alert dialog
	//--call from 5.1, case 1: delete Item--
	public void deleteThisTask() {
		removeItem= new AlertDialog.Builder(getActivity())
		.setTitle(R.string.delete_task_action)
		.setMessage(getString(R.string.delete_this_item_)+
				getString(R.string._note_deleted_record_is_not_recoverable_))
		.setPositiveButton(R.string.yes_delete, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//app.deleteTask(task.getId());
				app.deleteTask(task);
				adapter.deleteTask(task);
				removeItem.dismiss();
			}
		})
		.setNegativeButton(R.string.cancel_action, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				removeItem.cancel();
			}
		})
		.create();
		removeItem.show();
	}
 	
	//--Optons Menu---Delete all completed tasks---------------------------------------------------------------------------------------------------------
	public void deleteCompletedTasks() {
		deleteItems= new AlertDialog.Builder(getActivity())
		.setTitle(R.string.delete_completed_tasks_action)
		.setMessage(getString(R.string.delete_selected_task_s_)+
				getString(R.string._note_deleted_record_is_not_recoverable_))
		.setPositiveButton(R.string.yes_delete, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			removeSelectedTasks();
			}
		})
		.setNegativeButton(R.string.cancel_action, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteItems.cancel();
			}
		})
		.create();
		deleteItems.show();
	}
	
	//--Options Menu---Continue from deleteSectedItems()------------------------------------------------
	protected void removeSelectedTasks() {
		boolean removeCompleted=app.removeCompletedTasks();
		if (removeCompleted)
			adapter.removeCompletedTasks();
		//ArrayList<Integer> selection=adapter.removeItemSelected();
		//app.removeSelectedTasks(selection);
	}

	//--Options Menu---uncheck all tasks in checklist------------------------------------------------------
	public void unMarkAllDialog() {
		deleteItems= new AlertDialog.Builder(getActivity())
		.setIcon(android.R.drawable.ic_menu_agenda)
		.setTitle(R.string.uncheck_all_tasks)
		.setMessage(getString(R.string.uncheck_all_checked_tasks_))
		.setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				unSelectAllTasks();
			}
		})
		.setNegativeButton(R.string.cancel_action, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteItems.cancel();
			}
		})
		.create();
		deleteItems.show();
	}
	
	private void unSelectAllTasks() {
		ArrayList<TaskList> unCheckTask = new ArrayList<TaskList>();
		unCheckTask = app.getAllTasks();
		for (TaskList t : unCheckTask) {
			if (t.isComplete()) {
				t.setComplete(false);
				app.updateTask(t);
			}
		}
		adapter.unMarkAllTasks();
	}
	
	
	//--Options Menu---Reset to user default checklist------------------------------------------------------
	public void resetUserDefaultList() {
		deleteItems= new AlertDialog.Builder(getActivity())
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(R.string.reset_checklist_to_user_default)
		.setMessage(getString(R.string.current_tasks_will_be_overwritten_)+
				getString(R.string._proceed_to_reset_checklist_))
		.setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//restoreDefaultList();
				new readFileToDB().execute("default");
			}
		})
		.setNegativeButton(R.string.cancel_action, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteItems.cancel();
			}
		})
		.create();
		deleteItems.show();
	}
		
	//--Options Menu---Reset to user default checklist------------------------------------------------------
	public void createUserDefaultDialog() {
		deleteItems= new AlertDialog.Builder(getActivity())
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(R.string.save_user_default_checklist)
		.setMessage(getString(R.string.any_previously_save_file_will_be_overwritten_)+
				getString(R.string._proceed_to_save_default_checklist_))
				.setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//createFirstUserDefault(USERCHECKLIST);
						new writeUserDefaultFile().execute(USERCHECKLIST);
					}
				})
				.setNegativeButton(R.string.cancel_action, new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteItems.cancel();
					}
				})
				.create();
		deleteItems.show();
	}
	

	//--Options Menu---setup first run packing list--------------------------------------
	public void setupDefaultPackingList() {
		//--Dialog for loadDefaultCheckList--
		CharSequence[] itemSelect = { getString(R.string.his_packing_list), getString(R.string.her_packing_list)};
		int itemsChecked;
		loadDefaultList= new AlertDialog.Builder(getActivity())
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(R.string.holiday_packing_list_choice)
		.setSingleChoiceItems(itemSelect,-1, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case 0:
						new readFileToDB().execute("his");				//--v1.2 use raw/hischecklist instead
						//new readFileToDB().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "his").get();	
						//loadCheckList(false);
						//--createFirstUserDefault(false, USERCHECKLIST);
						break;
					case 1:
						new readFileToDB().execute("hers"); 			//--v1.2 use raw/herschecklist instead
						//loadCheckList(true);
						//createFirstUserDefault(true, USERCHECKLIST);
						break;
				} 
				loadDefaultList.dismiss();
			}
		})
		.create();
		loadDefaultList.show();
	}

	//=====6.0 MENU METHODS===========================================END======

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
			super.onPrepareOptionsMenu(menu);
			//getActivity().invalidateOptionsMenu();			//--crash

			menu.removeItem(9);
			
			/*menu.removeItem(1);		
			MenuItem mnu1=menu.add(0, 1, 1, R.string.save_as_user_default); 
			{ mnu1.setIcon(android.R.drawable.ic_menu_agenda);}

			menu.removeItem(2);
			MenuItem mnu2=menu.add(0, 2, 2, R.string.reset_to_user_default);
			{ mnu2.setIcon(android.R.drawable.ic_menu_set_as); }*/
			

			//MenuItem mnu6=menu.add(0, 6, 6, "Locations Within "+geoDist+"km");
			//{ mnu6.setIcon(android.R.drawable.ic_menu_myplaces); }	
		//return true;
	}

	private void CreateMenu(Menu menu) {
		
		MenuItem mnu1=menu.add(0, 1, 1, R.string.save_as_user_default); 
			{ mnu1.setIcon(android.R.drawable.ic_menu_agenda);}
	
		MenuItem mnu2=menu.add(0, 2, 2, R.string.reset_to_user_default);
			{ mnu2.setIcon(android.R.drawable.ic_menu_set_as); }
		
		MenuItem mnu3=menu.add(0, 3, 3, R.string.delete_all_completed);
			{ mnu3.setIcon(android.R.drawable.ic_menu_delete); }
		
		MenuItem mnu4=menu.add(0, 4, 4, R.string.uncheck_all_tasks);
			{ mnu4.setIcon(android.R.drawable.ic_notification_clear_all); }		
			
		MenuItem mnu5=menu.add(0, 5, 5, R.string.rename_category);
			{ mnu5.setIcon(android.R.drawable.ic_menu_edit); }		
			
		MenuItem mnu6=menu.add(0, 6, 6, R.string.reset_to_factory_default);
			{ mnu6.setIcon(android.R.drawable.ic_dialog_alert); }		
		
		MenuItem mnu7=menu.add(0, 7, 7, taskShowAll);
			{ 	//mnu7.setIcon(android.R.drawable.ic_menu_more);
				if (notComplete) {
					mnu7.setIcon(android.R.drawable.checkbox_off_background);
				} else {
					mnu7.setIcon(android.R.drawable.checkbox_on_background);
				}
				//mnu7.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
				mnu7.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}	
			
		MenuItem mnu8=menu.add(0, 8, 8, R.string.new_);
			{ 	mnu8.setIcon(android.R.drawable.ic_menu_add); 			//ic_input_add
				//mnu8.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
				mnu8.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}		
		

			
	}
	
	private boolean MenuChoice(MenuItem item)
	{
		switch (item.getItemId()) {
		case 1:
			if (isExtStorageAvail) {
				createUserDefaultDialog();
				return true;
			} else {
				((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.sdcard_is_not_found));
				return false;
			} //--isExtStorageAvail
			
		case 2:
			File file = getActivity().getBaseContext().getFileStreamPath(USERCHECKLIST);  //this is use to check getFilesDir
			if (isExtStorageAvail) {
				if (file.exists()) {
					resetUserDefaultList();
					return true;
				} else {
					((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.no_user_default_found));
					return false;
				}
			} else {
				((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.sdcard_is_not_found));
				return false;
			} //--isExtStorageAvail			
			
		case 3:
			deleteCompletedTasks();
			return true;
			
		case 4:
			unMarkAllDialog();
			return true;
			
		case 5:
			//--rename checklist category
			//Bundle aextras = new Bundle();
			((FragMain)getActivity()).callCheckCatEdit();
			//mCallback.OnMenuSelected(23, aextras);
			//Intent changeName=new Intent(this, CheckCatEdit.class);
			//startActivity(changeName);
			return true;
			
		case 6:
			if(isExtStorageAvail) {
				setupDefaultPackingList();
				return true;
			} else {
				((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.sdcard_is_not_found));
				return false;
			}  //isExtStorageAvail
			
		case 7:
			//--Show All or Display Only Uncompleted Tasks----------------------------------
			if (notComplete) {
				item.setTitle(R.string.hide);
				item.setIcon(android.R.drawable.checkbox_on_background);
			} else {
				item.setTitle(R.string.show_all);
				item.setIcon(android.R.drawable.checkbox_off_background);
			}
			selectList=category.getInt("Default", selectList);
			SharedPreferences.Editor catEdit=category.edit();			
			catEdit.putBoolean(getString(R.string.hide), !notComplete);
			catEdit.commit();			
			notComplete = !notComplete;
			adapter.spinnerSelection(selectList, notComplete);
			return true;
		
		case 8:
			//--Add New task------------------------------------------------------------------------
			//Log.d(TAG, "case 8 add new, selectList = "+selectList);
			Bundle extras=new Bundle();
			extras.putInt("rowid", 0);
			extras.putBoolean("editmode", false);
			if (selectList == 4) {						//--show all for category 
				extras.putInt("selectlist", 0);		//--default pre-holiday
			} else if (selectList == 5 ){
				extras.putInt("selectlist", 4);		//--push to packing list in add task, no 5
			} else {
				extras.putInt("selectlist", selectList);
			}
			mCallback.OnMenuSelected(10, extras);
			return true;
			
		case 9:
			//--Help-------------------------------------------------------------------------------------
			//Intent  simplehelp=new Intent(HolidayCheckList.this, SimpleHelp.class);
			Bundle fragHelp=new Bundle();
			fragHelp.putString("helpPage", helpPage.toString());
			mCallback.OnMenuSelected(18, fragHelp);
			//startActivity(simplehelp);
			return true;
			
		}
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
			//menu.setHeaderIcon(android.R.drawable.ic_menu_myplaces);
			menu.setHeaderIcon(R.drawable.ppgp_icon);
			MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.context_menu, menu);
			menu.setHeaderTitle(R.string.item_action);
			super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item){
			AdapterContextMenuInfo info=(AdapterContextMenuInfo) item.getMenuInfo();
			task=adapter.getItem(info.position);
			switch (item.getItemId()) {
				
				case R.id.editItem:
					//--Edit Task----------------------------------------------------------------------------
					app.setLastPosition(taskPosition);
					app.setLastTask(task);				
					//Intent intent=new Intent(HolidayCheckList.this, CheckAddTask.class);
					Bundle extras=new Bundle();
					extras.putInt(ITEM_ROWID, task.getId());
					extras.putBoolean("editmode", true);
					mCallback.OnMenuSelected(10, extras);
					//intent.putExtras(extras);
					//startActivity(intent);
					//startActivityForResult(intent, TASK_EDIT);
					return true;
					
				case R.id.deleteItem:
					//--Delete Task-----------------------------------------------------------------------
					deleteThisTask();
					return true;
					
				default:
					return super.onContextItemSelected(item);
			}			
		}

	//=====5.0 MENU ACTION DIALOG================================END======

	//=====6.0 SANDBOX=======================================START=====
	
	
	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================
	
	//--Asynctask to read raw text file to create checklist database------------------------------------
	public class readFileToDB extends AsyncTask<String, Integer, String> {
		
		@Override
		protected void onPreExecute() {													//--progress bar
			//super.onPreExecute();
			//pDialog=new ProgressDialog(HolidayCheckList.this);			//--CANNOT USE CONTEXT
			pDialog=new ProgressDialog(getActivity());			//--CANNOT USE CONTEXT, even getApplicationContext()

			pDialog.setMessage(getString(R.string.wait_loading_checklist));
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pDialog.setProgress(0);
			pDialog.setMax(100);

			pDialog.show();
			
		}

		@Override
		protected String doInBackground(String... params) {

			String whoCheck=params[0];
			String delimitedString="";
			String fileName;
			int pCycle;

			//--sample syntax--------------------------------------------
			//InputStream raw = context.getAssets().open("filename.ext");
			//Reader is = new BufferedReader(new InputStreamReader(raw, "UTF8"));
			
			//FileInputStream fIn;	//original
			InputStream raw = null;			
			try {

				//fIn = openFileInput(fileName);		//original
				if (whoCheck=="default"){
					raw=getActivity().openFileInput(USERCHECKLIST);
				} else if (whoCheck=="hers") {
					raw=getResources().openRawResource(R.raw.herchecklist);
				} else {
					raw=getResources().openRawResource(R.raw.hischecklist);
				}
				InputStreamReader isr=new InputStreamReader (raw);
				
				//--for use in progress bar dialog-- too fast for progress bar so don't use
				int totalSize=raw.available();						//totalSize, 871 for hers
				totalSize=totalSize/READ_BLOCK_SIZE;		//i.e. 8.7 while cycle
				int byteSize=100/totalSize;							//12 per increment each cycle
				
				char[] inputBuffer=new char[READ_BLOCK_SIZE];				
				pCycle=0;
				int charRead;
				while ((charRead=isr.read(inputBuffer))>0)
				{
					//---convert the chars to a String------
					String readString=String.copyValueOf(inputBuffer,0, charRead);
					delimitedString += readString;
					pCycle=pCycle+byteSize;		
					publishProgress((pCycle));	
					inputBuffer=new char[READ_BLOCK_SIZE];			
				}
			} catch (FileNotFoundException e) {			
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			return delimitedString;
		}

		@Override
		protected void onPostExecute(String result) {
			
			if (result != null) {
				String[] arrayStr = result.split(",");
				ArrayList<TaskList> userList=new ArrayList<TaskList>();
				
					for (int i=0; i<arrayStr.length; i++) {
						int idx=i;
						String catStr=arrayStr[i].toString();
						catStr.trim();
						int intCat=Integer.parseInt(catStr);
						
						TaskList task=new TaskList();
						task.setCat(intCat);
						i++;
						task.setName(arrayStr[i].toString());
						userList.add(task);
					}
					
					adapter.resetToUserDefaultList(userList);
					//adapter.notifyDataSetChanged();
					//Toast.makeText(getBaseContext(), "Checklist successfully reset to user default", Toast.LENGTH_SHORT).show();	
				} else {
					((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.no_user_default_created));
				}

			pDialog.dismiss();
			//super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			pDialog.setProgress(values[0]);
			//super.onProgressUpdate(values);			
		}
	}		//--END readFileToDB()

	//--Asynctask to write user custom checklist from database--------------------------------------------------
	public class writeUserDefaultFile extends AsyncTask<String, Void, Boolean> {
		
		@Override
		protected Boolean doInBackground(String... params) {
			
			//--retrieve all checklist tasks from db------------------------------------------------------
			ArrayList<TaskList> readTasks= new ArrayList<TaskList>();
			String fileName=params[0];
			readTasks=app.loadAllTasks();	
			
			StringBuilder strBuilder;
			strBuilder= new StringBuilder();
			int tt=readTasks.size();
			
			//--pass all tasks to String builder and delimited with comma----------
			for (int i=0; i<tt; i++) {
				strBuilder.append(readTasks.get(i).getCat()).append(",")
							.append(readTasks.get(i).getName().toString());
					if (i < (tt-1)) {
						strBuilder.append(",");
					}
			}
			String userCurrentList= strBuilder.toString();
			
			try
			{
				//FileOutputStream fout=new FileOutputStream(file);
				FileOutputStream fout=getActivity().openFileOutput (fileName, MODE_WORLD_READABLE);

				OutputStreamWriter osw=new OutputStreamWriter(fout);
				osw.write(userCurrentList);
				osw.flush();
				osw.close();
				return true;
			}
			catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				Toast.makeText(getActivity().getBaseContext(), "File saved successfully!", Toast.LENGTH_SHORT).show();
			} else {
				((TravelLiteActivity) getActivity()).showOkAlertDialog("User Default file creation failed, please again retry later");
			}
		}
		
	} //--END writeUserDefaultFile

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS==================END======

	//=====SECTION 10==SANDBOX================================START======
	

	//=====SECTION 10==SANDBOX=================================END======

	//=====SECTION 11==RECYCLE==================================START=====

	//****NEED TO VERIFY OBSOLETE CODES BEFORE DELETE------------------------------TO BE REMOVED****
	//--OnAcitivity call back from Intent-----------------------------------------------------------	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		/*if(requestCode==TASK_EDIT) {
			if (resultCode==RESULT_OK) {
			long rowid=data.getLongExtra(ITEM_ROWID, 0);
			//int position=app.getLastPosition();
			task = new TaskList();
			//task=app.getTask(adapter.getItemId(position));
			if (rowid>0)
				task=app.getTask(rowid);
			if (task !=null)
				adapter.updateTask(task);
			} else if (resultCode==RESULT_CANCELED) {
				
			}
		} else if (requestCode==TASK_ADD) {
			if (resultCode==RESULT_OK) { */
				/*long rowid=data.getLongExtra(ITEM_ROWID, 0);
				if (rowid !=0) {
					task=new TaskList();
					task=app.getTask(rowid);
				if (task !=null)
					adapter.addTask(task);
				}*/
			/*	notComplete=category.getBoolean("Hide", notComplete);
				selectList=category.getInt("Default", selectList);
				adapter.spinnerSelection(selectList, notComplete);
			}else if (resultCode==RESULT_CANCELED) {
				
			}
		}*/
	}
	
}	//--END MAIN CLASS
	
	

