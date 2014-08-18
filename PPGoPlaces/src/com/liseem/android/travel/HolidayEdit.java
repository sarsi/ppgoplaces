/* 
 * Fragment: 			EditHoliday.java
 * Description:			Modify Holiday Record
 * 
 * Created: 				May 3, 2012
 * Last updated: 		October 26, 2013
 * 
 * Associated files:
 * Layout View:		edit_holiday.xml
 * Date Dialog:		DateDialogFragment		(interface for callback)
 * 
 * Changes:
 * 	- Tidy codes and documentation
 * - Extract all language strings to strings.xml
 * - Replaced deprecated date dialog with DateDialogFragment
 * 
 * 
 * Outstanding fixes
 * - Replace codes for deprecated date picker dialog
 * 
 */



package com.liseem.android.travel;

import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;
import static com.liseem.android.travel.TravelLiteActivity.*;

import com.liseem.android.travel.DateDialogFragment;
import com.liseem.android.travel.items.Holiday;
import com.liseem.android.travel.items.MyDate;
import java.util.Calendar;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class HolidayEdit extends Fragment 
										  implements DateDialogFragment.DateDialogFragmentListener {
	
	//=====SECTION 1==DECLARATION===================================

	
	private final static String 			TAG="EditHoliday";	
	private static final int 					STARTDATE_DIALOG_ID=0;
	private static final int 					ENDDATE_DIALOG_ID=1;
	
	private PPGoPlacesApplication 	app;
	
	private Holiday 								holiday;

	//--View specific
	private TextView 							startTripDate;
	private TextView 							endTripDate;
	private EditText 							holidayName;
	private EditText 							tripNotes;
	private EditText 							holidayCountry;
	
	//--DB specific-- maybe obsolete later
	private long 									dbRowId;
	
	//--Holiday holiday specific--
	private MyDate 								startDate = new MyDate(); 
	private MyDate 								endDate= new MyDate();
	private Calendar							startTrip;
	private Calendar							endTrip;

	private boolean 							changesPending;
	private AlertDialog 						unsavedChangesDialog;

	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;

		//--inflate layout for fragment 1------------------------------------------------
		View v=inflater.inflate(R.layout.edit_holiday, container, false);
		setHasOptionsMenu(true);
		setRetainInstance(true);
		
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
		
		setUpView();
		setListener();
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

	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 3==SETUP VIEWS====================================

	private void setUpView() {

		//--path to main application--
		app						=(PPGoPlacesApplication)getActivity().getApplication();
		
		//--retrieve bundle information from calling fragment, e.g. rowid
		Bundle bundle 	= this.getArguments();			//--fragment retrieve bundle
		if (bundle != null) {
			dbRowId=bundle.getLong(HOL_ROWID);
			Log.d(TAG, "in bundle retrieve HOL_ROWID");
		}
				
		//--View textview and editview declaration--
		startTripDate		=(TextView)getActivity().findViewById(R.id.startDate);
		endTripDate		=(TextView)getActivity().findViewById(R.id.endDate);
		holidayName		=(EditText)getActivity().findViewById(R.id.tripName);
		tripNotes				=(EditText)getActivity().findViewById(R.id.tripNotes);
		holidayCountry	=(EditText)getActivity().findViewById(R.id.holCountry);
		tripNotes.setMovementMethod(ScrollingMovementMethod.getInstance());

		//--get Holiday instance--
		holiday				=new Holiday();
		if (dbRowId>0) {
			holiday				=app.getHoliday(dbRowId);			
		}
		
		//--views setup--
		holidayName.setText(holiday.getName().toString());
	  	tripNotes.setText(holiday.getNotes().toString());
		holidayCountry.setText(holiday.getCountry().toString());

		/*
		 * change ISO8601 Date String to Calendar date
		 * via str2Calendar from TravelLiteActivity
		 * 
		 * Two steps:
		 * set String date to MyDate instance
		 * get calendar from MyDate instance
		 * 
		 * MyDate instance once set can run date in multiple formats.
		*/
		//--date conversion via MyDate--
		startDate.setStrDate(holiday.getStart_date().toString());
		endDate.setStrDate(holiday.getEnd_date().toString());
		startTrip 		= startDate.getCalDate();
		endTrip 		= endDate.getCalDate();
		
		//--date views setup--
		((TravelLiteActivity) getActivity()).setCalDMY(startTripDate, getString(R.string.start_date_), startDate.getCalDate());
		((TravelLiteActivity) getActivity()).setCalDMY(endTripDate, getString(R.string.end_date_), endDate.getCalDate());
	}
	
	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================
	
	//=====SECTION 4==ADD ADAPTER===================================

	//=====SECTION 5==SET LISTENER====================================

	public void setListener() {
		//--OnClickListener to change start date with datePickerDialog via interface DateDialogFragment
		//startTripDate.setOnClickListener(new changeStartDate());
		startTripDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//Log.d(TAG,"198 landed in startTripDate onClick");
				changeStartDate();				
			}			
		});

		//--OnClickListener to change end date with datePickerDialog via interface DateDialogFragment
		endTripDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//Log.d(TAG,"208 landed in endTripDate onClick");
				changeEndDate();	
			} 			
		});
		
		//==SANDY===============================
		//--try trap back key -------------------------------------------------------
		this.getView().setOnKeyListener (new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					if (changesPending) {
						unsavedChangesDialog = new AlertDialog.Builder(getActivity())
						.setTitle(R.string.unsaved_changes)
						.setMessage(R.string.unsaved_changes_message)
						.setPositiveButton(R.string.abort, new AlertDialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								getActivity().finish();
							}
						})
						.setNegativeButton(R.string.cancel, new AlertDialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								unsavedChangesDialog.cancel();
							}
						})
						.create();
						unsavedChangesDialog.show();
						} else {
							getFragmentManager().popBackStackImmediate();
						}
					}
	           // process normally
				return false;
			}
		});
		//==SANDY===============================

	}	//--END set Listener		
	
	
	//=====SECTION 5==SET LISTENER====================================

	/* 
	 * Change vacation start and end date, call from onClickListener
	 * Execute DateDialogFragment to provide date change
	 * Change result return via callback interface dateDialogFragmentDateSet
	 */
	
	//--call change date dialog fragment to show dialog with preset date-----------------
	public void changeStartDate() {
		
		//--initial date dialog fragment
		DateDialogFragment ddf = DateDialogFragment.newInstance(getActivity(), STARTDATE_DIALOG_ID, startTrip);
		ddf.setDateDialogFragmentListener(this);
		DialogFragment newFragment = ddf;
	    newFragment.show(getFragmentManager(), "DatePicker");
		
	}

	//--call change date dialog fragment to show dialog with preset date-----------------
	public void changeEndDate() {
		
		//--initial date dialog fragment
		DateDialogFragment ddf = DateDialogFragment.newInstance(getActivity(), ENDDATE_DIALOG_ID, endTrip);
		ddf.setDateDialogFragmentListener(this);
		DialogFragment newFragment = ddf;
		newFragment.show(getFragmentManager(), "DatePicker");	
	}
	
	

	//=====SECTION 6==LISTENER METHODS================================
	
	//--equivalent to OnActivityResult I guess
	//--result from DateDialogFragment callback, i.e. calling back this fragment to give 
	//--the new date selected on the datePickerDialog.
	@Override
	public void dateDialogFragmentDateSet(Calendar mdate, int whichDate) {
		
		if (whichDate==STARTDATE_DIALOG_ID) {
			Log.d(TAG, "334 dateDialogFragmentDateSet callback for new startTripDate");
			
			//--error checking, start date must be before end date---------
			if (endTrip.before(mdate)) {
				((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.start_date_cannot_be_after_end_date));
			} else {
				startTrip=mdate;
				startDate.setCalDate(mdate);
				
				//--update display the New end date in text view--
				((TravelLiteActivity) getActivity()).setCalDMY(startTripDate, getString(R.string.start_date_), mdate);			
			}
			
		} else if (whichDate==ENDDATE_DIALOG_ID) {
			Log.d(TAG, "342 dateDialogFragmentDateSet callback for new endTripDate");
			
			
			if (mdate.before(startTrip)) {
				((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.start_date_cannot_be_after_end_date));
			} else {			
				endTrip=mdate;
				endDate.setCalDate(mdate);
			
				//--update display the New end date in text view--
				((TravelLiteActivity) getActivity()).setCalDMY(endTripDate, getString(R.string.end_date_), mdate);			

			}	
		}
	}

	//=====SECTION 6==LISTENER METHODS================================

	//=====SECTION 7==HELPER METHODS==================================
	
	/*
	 * DATA STAGING FOR DB UPDATE
	 * DATE STRING as ISO8601 strings ("YYYY-MM-DD HH:MM:SS.SSS")
	 * 
	 * saveNewHoliday data integrity check routines before commit 
	 * to SQLite "holidaDB" table
	 */
	public void updateHoliday() {
		holiday.setName(holidayName.getText().toString());
		holiday.setCountry(holidayCountry.getText().toString());
		holiday.setStart_date(startDate.getStrDate());
		holiday.setEnd_date(endDate.getStrDate());
		holiday.setNotes(tripNotes.getText().toString());		
		boolean updateDone=app.updateHoliday(holiday);
		
		if (updateDone) {
			Intent intent=new Intent();
			getActivity().setResult(RESULT_OK, intent);			
		}
		
		Toast.makeText(getActivity(), R.string.holiday_information_updated, Toast.LENGTH_SHORT).show();
		
		getFragmentManager().popBackStackImmediate();
	}
	

	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================
	
	//=====SECTION 8==MENU AND DIALOG METHODS===========================
	
	//--remove calculator and setting from action bar-------------------------------------------------------
	@Override
	public void onPrepareOptionsMenu (Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		menu.removeItem(9);
		menu.removeItem(10);
	}
	
	private void CreateMenu(Menu menu) {
		
		MenuItem mnu1=menu.add(0, 1, 1, R.string.update); 
			{ 	//mnu1.setIcon(android.R.drawable.ic_menu_add);
				mnu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}
	
	}
	
	private boolean MenuChoice(MenuItem item)
	{
		switch (item.getItemId()) {
		case 1:
			updateHoliday();
			return true;
		
		default:
			return false;
		}
	}			
	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//=====SECTION 10==SANDBOX======================================
	
	/* 	 
	 * unsaved Data change listener and cancel dialog routine
	 * 
	 */
	/*
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if (changesPending) {
			unsavedChangesDialog = new AlertDialog.Builder(getActivity())
			.setTitle(R.string.unsaved_changes)
			.setMessage(R.string.unsaved_changes_message)
			.setPositiveButton(R.string.abort, new AlertDialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					getActivity().finish();
				}
			})
			.setNegativeButton(R.string.cancel, new AlertDialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					unsavedChangesDialog.cancel();
				}
			})
			.create();
			unsavedChangesDialog.show();
			} else {
				getFragmentManager().popBackStackImmediate();
			}
		}
		return false;
	}
	*/

} //--MAIN CLASS END
