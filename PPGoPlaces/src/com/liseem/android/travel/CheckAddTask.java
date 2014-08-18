/*
 * Fragment:			CheckAddTask.java
 * Description:			Add and Edit checklist task
 * 
 * Date created:		May 3, 2012
 * Last updated:		October 1, 2013
 * 
 * Resources files:
 * Layout view: 		addTask.xml
 * Parent file: 			HolidayCheckList.java
 * 
 */

package com.liseem.android.travel;

import static com.liseem.android.travel.TravelLiteActivity.*;
import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;

import java.util.ArrayList;
import com.liseem.android.travel.items.TaskList;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class CheckAddTask extends Fragment {

	//=====SECTION 1==DECLARATION===================================

	private final static String 			TAG="AddTask";
	private final static int 					PACKLISTSELECT=4;
	
	private PPGoPlacesApplication 	app;
	
	//--View Specific--
	private Button 								addTask;
	private EditText 							itemName;
	private EditText 							itemNotes;
	private Spinner 								taskSpinner;
	private TextView 							homeText;


	//--Data Containers--
	private TaskList 							task=new TaskList();
	private long 									dbRowId;
	private String[] 								checkListItems;
	private int										selectList;							//--category select

	//private int 										dbCat;									//--Store category into refid, 1,2,3,4,5
	//private String 								dbName="";						//--Task Name
	//private boolean 							dbComplete=false;
	//private String 								dbAddress="";					
	//private double 								dbLatitude=0;
	//private double 								dbLongitude=0;
	//private String 								dbNotes="";
	
	//--Edit Mode-----------
	private String									actionText;							//--new text is Add and edit mode update	
	private boolean 							editItem;								//--True to turn on edit mode
	//private boolean 							updated;
	private int										packItem=8;						//--store packing list item default id, default 9 for new.
	
	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.add_task);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		setHasOptionsMenu(true);								//--use fragment options menu instead of activity default

		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;

		//--inflate layout for fragment 1------------------------------------------------
		View v=inflater.inflate(R.layout.add_task, container, false);
		setRetainInstance(true);
		
		return v;			
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
		setUpView();		//--2.0
		setListener();		//--3.0
	}


	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onPause() {
		super.onPause();
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
		//return true;		
	}


	//--Return menu choice on menu selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		return MenuChoice(item);
	}
	
	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 3==SETUP VIEWS====================================
	
	private void setUpView() {
		
		//--path to main application-----------------------------------------------------------------------
		app						=(PPGoPlacesApplication)getActivity().getApplication();
		
		//--intent get extras, for edit shopping item---------------------------------------------------
		//Bundle bundle	=getActivity().getIntent().getExtras();
		Bundle bundle 	= this.getArguments();			//--fragment retrieve bundle
		if (bundle != null) {
			dbRowId			=bundle.getInt(ITEM_ROWID);
			editItem			=bundle.getBoolean("editmode");
			selectList			=bundle.getInt("selectlist", 1);
			Log.d(TAG, "in bundle getInt, selectList "+selectList);
			if (dbRowId>0) {
				task				=app.getTask(dbRowId);		//cannot retrieve if it is an add statement stupid
				packItem		=task.getCat();			//set original cat id to ensure packing list integrity
			}
		}
				
		//--define view items-------------------------------------------------------------------------------
		itemName			=(EditText)getActivity().findViewById(R.id.itemName);
		itemNotes			=(EditText)getActivity().findViewById(R.id.itemNotes);
		taskSpinner			=(Spinner)getActivity().findViewById(R.id.taskSpinner);
		homeText			=(TextView)getActivity().findViewById(R.id.homeText);
		addTask				=(Button)getActivity().findViewById(R.id.addItem);
		itemName.requestFocus();
		
		//==OBSOLETED BY ACTION BAR=================
		addTask.setText(R.string.add_new_item);
		addTask.setVisibility(View.GONE);
		if (editItem) {					//--Action Bar text "add" or "update"
			actionText = getActivity().getString(R.string.update);
		} else {
			actionText = getActivity().getString(R.string.add);
		}
		
		//--Tag Holiday dropdown list---------------------------------------------------------------------
		addItemsOnSpinner();		//--2.2
		
		//--Setup for Edit Mode-----------------------------------------------------------------------------
		if (editItem) {
				itemName.setText(task.getName().toString());
				if(task.getNotes()!=null)
					{ 	itemNotes.setText(task.getNotes().toString()); }
						addTask.setText(R.string.update_item);
				
				int spinFix=task.getCat();
				if (spinFix > LAST_CAT) {
					taskSpinner.setSelection(4);
				} else {
					taskSpinner.setSelection(spinFix);
				}
		} else {
			taskSpinner.setSelection(selectList);
		}
	}
	
	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================
	
	//--add menu items to spinner static variables--------------------------------------------------------
	public void addItemsOnSpinner() {
		loadCategoryList();
		ArrayAdapter<String> dataAdapter=new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_spinner_item, checkListItems);
		dataAdapter.setDropDownViewResource(R.layout.dropdown_item); //custom to remove radio button
		taskSpinner.setAdapter(dataAdapter);
	} 

	//--Load list------------------------------------------------------------------------------------------------------
	public void loadCategoryList() {
		ArrayList<String> catList = new ArrayList<String>();
		catList=app.loadCheckListCat();
		
		//--String array used by spinner data adapter-------------------
		checkListItems=catList.toArray(new String[catList.size()]);
	}

	//=====SECTION 4==ADD ADAPTER===================================

	//=====SECTION 5==SET LISTENER====================================	
	
	protected void setListener() {
		
		addTask.setOnClickListener(new addTaskButtonListener());
		taskSpinner.setOnItemSelectedListener(new spinnerItemSelectedListener());		
	}

	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================
	
	//--Add/Update checklist task----------------------------------------------------------------------------	
	//--BUTTON OBSOLETED, REPLACED BY ACTION BAR
	public class addTaskButtonListener implements OnClickListener {

		@Override
		public void onClick(View v) {
				saveNewTask();
		}
	}

	//--Checklist category on click listener-----------------------------------------------------------------
	protected class spinnerItemSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			
			Log.d(TAG,"spinnerItemSelectedListener, spinner select "+position);
				if (position==PACKLISTSELECT) 				
				{
					//dbCat=packItem;			//store back 8 or 9 if is a packing list, default 9 for new
					task.setCat(8);
				} else {
					//dbCat=position;				//else save whatever holiday category
					task.setCat(position);
				}
				//task.setCat(dbCat);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {		}		
	}
	
	//--Save  and update task routine------------------------------------------------------------------------------------------------------
	private void saveNewTask() {
		
		/* ------------------------------------------------------------------------------------------------------
		 * DATA STAGING FOR DB UPDATE
		 * DATE STRING as ISO8601 strings ("YYYY-MM-DD HH:MM:SS.SSS")
		 */
		//--Here is my 3 cuprits that wasted a whole day--
		//Log.d(TAG,"saveNewTask, itemName "+itemName.getText().toString());
		
		if (itemName.getText().toString().trim().equals("")) {			
			AlertDialog show = new AlertDialog.Builder(getActivity())
			.setIcon(R.drawable.ppgp_icon)
			.setTitle(R.string.checklist_name)
			.setMessage(R.string.enter_checklist_name)
			.setPositiveButton(R.string.ok, null).show();		
			
		} else {
			
			task.setName(itemName.getText().toString());
			task.setNotes(itemNotes.getText().toString());
			
			//--insert db via application--
			if (editItem) {
				app.updateTask(task);												//--saved edit task
			} else {
				task.setComplete(false);
				dbRowId=app.addTask(task);									//--insert new task
			}
				
			//--return rowid of holiday to viewShopping------------------
			Intent intent=new Intent();
			intent.putExtra(ITEM_ROWID, dbRowId);
			getActivity().setResult(RESULT_OK, intent);
			
			//--hide soft keyboard before exit current screen-----------
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 
					    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),      
					    InputMethodManager.HIDE_NOT_ALWAYS);
					    
			//getActivity().finish();
			getFragmentManager().popBackStackImmediate();		//--cannot use finish(), it will exit application
		}
	}

	//=====3.0 ONCLICK ACTION XML=====================================END=======
	
	//=====4.0 MENU AND MENU HELPERS==================================START======

	private void CreateMenu(Menu menu) {
		
		MenuItem mnu1=menu.add(0, 1, 1, actionText); 
			{ 	//mnu1.setIcon(android.R.drawable.ic_menu_add);
				mnu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}
	
		/*MenuItem mnu2=menu.add(0, 2, 2, R.string.new_);
			{ 	mnu2.setIcon(android.R.drawable.ic_menu_agenda); 
				mnu2.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}
		
		MenuItem mnu3=menu.add(0, 3, 3, R.string.help);
			{ 	mnu3.setIcon(android.R.drawable.ic_menu_help); 
				mnu3.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}*/
			
	}
	
	private boolean MenuChoice(MenuItem item)
	{
		switch (item.getItemId()) {
		case 1:
			saveNewTask();
			return true;
		
		default:
			return false;
		}
	}			


	//=====4.0 MENU AND MENU HELPERS==================================END=======

} 	//--END MAIN CLASS 


