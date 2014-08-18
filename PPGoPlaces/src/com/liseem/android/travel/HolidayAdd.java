/* 
 * Fragment: 			AddHoliday.java
 * Description:			Insert New Holiday Record
 * 
 * Created: 				May 3, 2012
 * Last updated: 		October 26, 2013
 * 
 * Associated files:
 * Layout View:		add_holiday.xml
 * Date Dialog:		DateDialogFragment		(interface for callback)
 * 
 * Changes:
 * 	- Tidy codes and documentation
 * - Extract all language strings to strings.xml
 * - Add DateDialogFragment replaced deprecated date dialog
 * 
 * 
 * Outstanding fixes
 * - Replace codes for deprecated date picker dialog
 * 
 * 
 * 
 */


package com.liseem.android.travel;

import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;
import com.liseem.android.travel.items.Holiday;
import com.liseem.android.travel.items.MyDate;
import com.liseem.android.travel.DateDialogFragment;

import java.text.ParseException;
import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;



public class HolidayAdd extends Fragment 
											implements DateDialogFragment.DateDialogFragmentListener {

	//=====SECTION 1==DECLARATION===================================

	private final static String 			TAG="AddHoliday";
	private static final int 					STARTDATE_DIALOG_ID=0;
	private static final int 					ENDDATE_DIALOG_ID=1;
	private static final int 					DEFAULT_END_DAYS=14;

	private PPGoPlacesApplication 	app;
	
	private Holiday 								holiday;	

	private View 									addButton;	
	private TextView 							startTripDate;
	private TextView 							endTripDate;
	private EditText 							holidayName;
	private EditText 				 			tripNotes;
	private EditText 							holidayCountry;
	private MyDate								startDate;
	private MyDate								endDate;
	private Calendar 							startTrip; 
	private Calendar							endTrip;
	private String									actionText;
	private Context								context;
	


	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;

		//--inflate layout for fragment 1------------------------------------------------
		View v=inflater.inflate(R.layout.add_holiday, container, false);
		setHasOptionsMenu(true);
		//setRetainInstance(true);
		
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
		
		//--Path to PPGoPlacesApplication--
		app						=(PPGoPlacesApplication)getActivity().getApplication();
		holiday				=new Holiday();
		
		startTripDate		=(TextView)getActivity().findViewById(R.id.startDate);
		endTripDate		=(TextView)getActivity().findViewById(R.id.endDate);
		holidayName		=(EditText)getActivity().findViewById(R.id.tripName);
		tripNotes				=(EditText)getActivity().findViewById(R.id.tripNotes);
		holidayCountry	=(EditText)getActivity().findViewById(R.id.holCountry);
				
		tripNotes.setMovementMethod(ScrollingMovementMethod.getInstance());
		
		//--get holiday name pass from smartInput from PPGoPlaces----------
		actionText = getActivity().getString(R.string.add);
		Bundle hextras	=getActivity().getIntent().getExtras();
		if (hextras != null) {
			String tempName=hextras.getString(HOL_HOLIDAY);
			if (tempName !=null) {
				holidayName.setText(tempName.toString());
				actionText = getString(R.string.update);
			}
		}
		
		holidayName.requestFocus();

		/* 
		 * change ISO8601 Date String to Calendar date
		 * via str2Calendar from TravelLiteActivity
		*/
		
		//--setup default start date and forecast end date--
		startDate 		= new MyDate();
		endDate			= new MyDate();
        startTrip			= Calendar.getInstance();
        endTrip			=Calendar.getInstance();
        endTrip.add(Calendar.DAY_OF_MONTH,DEFAULT_END_DAYS);  //--Default end date offset 14 days from start date
		startDate.setCalDate(startTrip);
		endDate.setCalDate(endTrip);
        
		//--setup calendar text view in DD-MMM-YYYY-- tryout
        ((TravelLiteActivity) getActivity()).setCalDMY(startTripDate, getString(R.string.start_date_), startTrip);
        ((TravelLiteActivity) getActivity()).setCalDMY(endTripDate, getString(R.string.end_date_), endTrip);
		

		/*
		 * Set up text view for holiday name, country and notes from db
		 * for updating
		 */
		
		//--Monitor text change--
        tripNotes.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {				
			}

			//--Abstract Methods Not Use Here--
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) { }
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) { }     	
        });
		
		//--holiday name text field listener for add button---------------------------
		holidayName.addTextChangedListener(new TextWatcher() {

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
			 }			
		});
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
				//Log.d(TAG,"253 landed in startTripDate onClick");
				changeStartDate();				
			}			
		});

		//--OnClickListener to change end date with datePickerDialog via interface DateDialogFragment
		endTripDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//Log.d(TAG,"263 landed in endTripDate onClick");
				changeEndDate();	
			} 			
		});
	}	//--END set Listener
	
	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================
			
	/* 
	 * Change vacation start and end date, call from onClickListener
	 * Execute DateDialogFragment to provide date change
	 * Change result return via callback interface dateDialogFragmentDateSet
	 */
	
	//--call change date dialog fragment to show dialog with preset date-----------------
	public void changeStartDate() {
		
		//--call date dialog fragment for date change dialog
		DateDialogFragment ddf = DateDialogFragment.newInstance(getActivity(), STARTDATE_DIALOG_ID, startTrip);
		ddf.setDateDialogFragmentListener(this);
		DialogFragment newFragment = ddf;
	    newFragment.show(getFragmentManager(), "DatePicker");
		
	}

	//--call change date dialog fragment to show dialog with preset date-----------------
	public void changeEndDate() {
		
		//--call date dialog fragment for date change dialog
		DateDialogFragment ddf = DateDialogFragment.newInstance(getActivity(), ENDDATE_DIALOG_ID, endTrip);
		ddf.setDateDialogFragmentListener(this);
		DialogFragment newFragment = ddf;
		newFragment.show(getFragmentManager(), "DatePicker");	
	}

	
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

	//--INSERT HOLIDAY VIA PASSING AS HOLIDAY OBJECT--
	protected void addHoliday() {
				
		holiday.setCountry(holidayCountry.getText().toString());
		holiday.setStart_date(startDate.getStrDate());
		holiday.setEnd_date(endDate.getStrDate());
		holiday.setHoliday(holidayName.getText().toString());
		holiday.setNotes(tripNotes.getText().toString());	
		//holiday.setStart_date(((TravelLiteActivity) getActivity()).cal2String(startTrip).toString());
		//holiday.setEnd_date(((TravelLiteActivity) getActivity()).cal2String(endTrip).toString());
		
		long addNew=app.addHoliday(holiday);
		
		if (addNew>0) {
			Intent intent =new Intent();
			intent.putExtra(HOL_ROWID, addNew);
			getActivity().setResult(Activity.RESULT_OK, intent);
		} else {
			Intent intent=new Intent();
			getActivity().setResult(Activity.RESULT_CANCELED, intent);
		}
		
		Toast.makeText(getActivity().getBaseContext(),getString(R.string.new_holiday_added), Toast.LENGTH_SHORT).show();

		//--hide soft keyboard before exit current screen-----------
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 
				    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),      
				    InputMethodManager.HIDE_NOT_ALWAYS);
		//--return to calling fragment
		getFragmentManager().popBackStackImmediate();
	} 
	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================
	
	//--remove calculator  and setting from action bar-------------------------------------------------------
	@Override
	public void onPrepareOptionsMenu (Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		menu.removeItem(9);
		menu.removeItem(10);
	}
	
	private void CreateMenu(Menu menu) {
		
		MenuItem mnu1=menu.add(0, 1, 1, R.string.add); 
			{ 	//mnu1.setIcon(android.R.drawable.ic_menu_add);
				mnu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}
	
	}
	
	private boolean MenuChoice(MenuItem item)
	{
		switch (item.getItemId()) {
		case 1:
			addHoliday();
			return true;
		
		default:
			return false;
		}
	}			
	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//=====SECTION 10==SANDBOX======================================

	
} //--END MAIN
	



