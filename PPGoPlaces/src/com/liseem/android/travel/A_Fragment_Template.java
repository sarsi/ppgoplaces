/*
 *  Lifecycle 
 *		- add View onCreateView(..
 *		- move setupView() from onCreate() to onStart()
 *		- change lifecycle activity from private to public
 *		- in onCreateView() 
 *				add setHasOptionsMenu(true)
 *				add onCreateOptionsMenu(Menu menu, MenuInflater inflater) …
 *
 * Bundle
 *		replace "Bundle bundle = getIntent().getExtras();" 
 *		with "Bundle bundle = this.getArguments();"
 */


package com.liseem.android.travel;

import static com.liseem.android.travel.PPGoPlacesApplication.*;
import static com.liseem.android.travel.TravelLiteActivity.*;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.MODE_WORLD_READABLE;

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
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class A_Fragment_Template extends Fragment {

	//--BOILER PLATE FOR FRAGMENT-----------------------------------------------------------
	
	//=====SECTION 1==DECLARATION==================================
	
	private static final String 					TAG="A_Fragment_Temple";			//--for debug LogCat
	
	private PPGoPlacesApplication				app;													//--access to main application
	private FragMain									act;													//--access to main activity
	private SharedPreferences 					prefs;												//--shared preferences
	private SharedPreferences.Editor			editor;												//--shared preferences editor

	private OnMenuSelectedListener			mCallback;										//--for callback
	private boolean									onHoliday;										//--sample bundle
	
	private Button										addButton;										//--sample onClickListener
	
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
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		//--inflate layout for fragment------------------------------------------------
		//return inflater.inflate(R.layout.view_checklist, container, false);
		if(container==null) 
			return null;

		//--inflate layout for fragment ------------------------------------------------
		View v=inflater.inflate(R.layout.view_checklist, container, false);
		setHasOptionsMenu(true);								//--use fragment options menu inaddition to activity default
		setRetainInstance(true);								//--retain fragment instance in back stack
		return v;			
	}
			
	@Override
	public void onStart() {
		super.onStart();	

		//--call ActionBar------------------------------------------------------------------
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.show();
		actionBar.setDisplayShowTitleEnabled(false);
	
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
		
		app				=(PPGoPlacesApplication)getActivity().getApplication();
		act				=(FragMain)getActivity();
		prefs			=getActivity().getSharedPreferences (PREFNAME, MODE_PRIVATE);		
		editor			=prefs.edit();
		//((PPGoPlacesApplication)getActivity().getApplication()).setActiveActivity(getActivity());
		
		//--Sample Bundle pass object or variables between calling fragment----------------------
		Bundle bundle 	= this.getArguments();							//--fragment retrieve bundle
		if (bundle!=null) {
			onHoliday		= bundle.getBoolean("onHoliday");
		}		
		
		//--example of button
		addButton			= (Button)getActivity().findViewById(R.id.addButton);
	}

	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================
	
	private void addAdapter() {	
		

	}
	
	//=====SECTION 4==ADD ADAPTER===================================

	//=====SECTION 5==SET LISTENER====================================

	//--Define view listeners and call methods------------------------------------------------
	public void setListener() {
	
		//addButton.setOnClickListener(new callOnClickClass());			//--see in section 6
		
		//--ALTERNATIVE via inner class
		addButton.setOnClickListener (new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub			
			}			
		});
		
		//--trap back key for fragment---------------------------------------------------------
		this.getView().setOnKeyListener (new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
	                return true;
	            }
	            // process normally
				return false;
			}
		});
	}

	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================	
	
	//--Add new checklist task-------------------------------------------------------------------------------------------------------
	private class callOnClickClass implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub			
		}	
	}	


	//=====SECTION 6==LISTENER METHODS=================================

	//=====SECTION 7==HELPER METHODS==================================
	
	public void hideKeyBoard () {
		
 		//--hide soft keyboard and exit
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 
	    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),      
	    InputMethodManager.HIDE_NOT_ALWAYS);
	    
		//--return to previous calling fragment
		getFragmentManager().popBackStackImmediate();

	}
	
	
	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================
	

	//=====8.1 MENU METHODS===========================================END======

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
			super.onPrepareOptionsMenu(menu);

			menu.removeItem(9);			//--removed Calculator
			menu.removeItem(10);		//--removed setting
	}

	private void CreateMenu(Menu menu) {
		
		MenuItem mnu1=menu.add(0, 1, 1, R.string.save_as_user_default); 
			{ mnu1.setIcon(android.R.drawable.ic_menu_agenda);
			mnu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}			//--put on action bar
			
		MenuItem mnu8=menu.add(0, 8, 8, R.string.new_);
			{ 	mnu8.setIcon(android.R.drawable.ic_menu_add); 			
				mnu8.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);} 	//--only if action bar has room				
	}
	
	private boolean MenuChoice(MenuItem item)
	{
		switch (item.getItemId()) {
		case 1:
			// TODO Auto-generated method stub	
			
			//--done and returning to previous fragment stack
			getFragmentManager().popBackStackImmediate();
			return true;
			
		case 8:
			// TODO Auto-generated method stub			
			//--call a fragment without callback interface setup
			((FragMain) getActivity()).callMyPreferences();
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
			//task=adapter.getItem(info.position);
			switch (item.getItemId()) {
				
				case R.id.editItem:
					// TODO Auto-generated method stub			
					return true;
					
				case R.id.deleteItem:
					// TODO Auto-generated method stub								return true;
					
				default:
					return super.onContextItemSelected(item);
			}			
		}

	//=====5.0 MENU ACTION DIALOG================================END======

	//=====6.0 SANDBOX=======================================START=====
	
	
	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================
	
	//--Asynctask task thread sample----------------------------------
	public class readFileToDB extends AsyncTask<String, Integer, String> {
		
		@Override
		protected void onPreExecute() {	
			//--progress bar
			// TODO Auto-generated method stub						
		}

		@Override
		protected String doInBackground(String... params) {

			// TODO Auto-generated method stub		
			return null;

		}

		@Override
		protected void onPostExecute(String result) {
			
			if (result != null) {
				// TODO Auto-generated method stub
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub		
			((TravelLiteActivity) getActivity()).showOkAlertDialog("Good Job !!!");

		}
	}		//--AsyncTask

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS==================END======

	//=====SECTION 10==SANDBOX================================START======
	

	//=====SECTION 10==SANDBOX=================================END======

	//=====SECTION 11==RECYCLE==================================START=====

	
}		//--END MAIN
