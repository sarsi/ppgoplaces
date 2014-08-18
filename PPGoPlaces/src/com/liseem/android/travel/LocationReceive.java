package com.liseem.android.travel;

import java.io.File;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import com.liseem.android.travel.items.HLocation;
import com.liseem.android.travel.items.MyDate;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SlidingDrawer;
import android.widget.Spinner;
import android.widget.TextView;

public class LocationReceive extends TravelLiteActivity {
	
	//=====SECTION 1==DECLARATION===================================
	
	private static final String TAG="ReceiveLocation";
	
	protected static final int LOCATIONDATE_DIALOG_ID=0;
	
	private HLocation		hLocation;
	private HLocation		newLocation;
	private File					newFile;
	private String				fileLocation;
	private AlertDialog 		receivedLocation;
	private AlertDialog 		fileNotFound;
	
	private MyDate			myDate;

	private TextView 		locationDate;
	private EditText 			locationAddress;
	private EditText 			locationName;
	private TextView 		locationNotes;
	private Spinner 			holidaySpinner;
	private TextView 		emptyList;
	private ImageButton 	useThis;
	private SlidingDrawer locDrawer;
	private EditText 			locLat;
	private EditText 			locLong;
	private TextView 		locLatText;
	private TextView 		locLongText;
	private TextView 		locInfo;
	private ImageButton 	mapIT;
	private Button 			locHandle;
	
	private boolean			newLocSaved;



	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_location);
		
		app=(PPGoPlacesApplication)getApplication();
		
		setupView();
	}
	
	@Override
	public void onBackPressed() {		
		super.onBackPressed();
	}

	private void setupView() {
		
		hLocation=new HLocation();
		newLocation=new HLocation();
		
		Intent intent=getIntent();
		String action=intent.getAction();
	
		
		fileLocation=intent.getData().getPath();
		//Log.d(TAG,"landed in setupView(), location name from getData().getPath() "+fileLocation.toString());
		
		if (fileLocation !=null) {
			//File newFile=new File(fileLocation.toString());
			//new createLocationFromFile().execute(fileLocation.toString());
			try {
				newLocation = new retreiveLocationFromFile().execute(fileLocation.toString()).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		//Log.d(TAG,"back from async");
		//--setup view for new location------------------------------------
		locationDate=(TextView)findViewById(R.id.locDate);				
		locationAddress=(EditText)findViewById(R.id.locAdd); 			
		locationName=(EditText)findViewById(R.id.locName);			
		locationNotes=(TextView)findViewById(R.id.locNotes);
		holidaySpinner=(Spinner)findViewById(R.id.holidaySpinner);	
		
		emptyList=(TextView)findViewById(R.id.emptyList);
		holidaySpinner.setEmptyView(emptyList);		//set Spinner to empty textview
		
		//--hide all NON EDIT views-----------------------------------------------
		holidaySpinner.setVisibility(View.INVISIBLE);
		
		useThis=(ImageButton)findViewById(R.id.useThis);
		useThis.setVisibility(View.INVISIBLE);

		//--sliding drawer view setup-------------------------------
		locDrawer=(SlidingDrawer)findViewById(R.id.slidingDrawer1);
		locDrawer.setVisibility(View.INVISIBLE);
		locHandle=(Button)findViewById(R.id.handle);
		locHandle.setVisibility(View.INVISIBLE);
		
		locLat=(EditText)findViewById(R.id.locLat);
		locLong=(EditText)findViewById(R.id.locLong);
		locLatText=(TextView)findViewById(R.id.locLatText);
		locLongText=(TextView)findViewById(R.id.locLongText);
		locInfo=(TextView)findViewById(R.id.locInfo);
	
		mapIT=(ImageButton)findViewById(R.id.mapIT);
		mapIT.setVisibility(View.INVISIBLE);		

		if (newLocation==null) {
			displayErrorAndExit();
		} else {
			displayNewLocation();
		}
	}
	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================

	//--display error message and exit gracefully------------------------------
	protected void displayErrorAndExit() {
		
		fileNotFound= new AlertDialog.Builder(this)
		.setIcon(R.drawable.ppgp_icon)
		.setTitle("Unable To Retrieve File")
		.setMessage("Unable to retrieve file from mail, try download file.")
		.setPositiveButton("OK", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			//fileNotFound.dismiss();
			}
		})
		.create();
		fileNotFound.show();
	}
	
	//--display new location information, called from AsyncTask onPostExecute()-------------------
	protected void displayNewLocation() {
		//Log.d(TAG, "landed in display location");
		//--display today date--------------------------------
		myDate=new MyDate();
		myDate.setCalDate(Calendar.getInstance());

		//--check for null before display--------------------
		if (newLocation.getName() !=null) {
			locationName.setText(newLocation.getName().toString());
			if (newLocation.getAddress() !=null)
				locationAddress.setText(newLocation.getAddress() .toString());
			
			if (newLocation.getLdate() != null) {
				myDate.setStrDate(newLocation.getLdate().toString());
			}
			String todayDate=myDate.getStrDate();		
			locationDate.setText(displayShortDMY(todayDate));

			if (newLocation.getLatitude()>0 && newLocation.getLongitude() >0) {
				StringBuilder locInfo=new StringBuilder();
				locInfo.append("\nLatitude: ").append(newLocation.getLatitude())
				.append("\nLongitude: ").append(newLocation.getLongitude());
				locationNotes.setText(locInfo.toString());

			}
		}
	}
	//=====SECTION 4==ADD ADAPTER===================================
	
	//=====SECTION 5==SET LISTENER====================================
	
	//=====SECTION 6==LISTENER METHODS================================
	//--add new location click-----------------------------------------------------------
	public void updateLocationClick (View v) {
		addNewLocationToDB();
	}
	
	public void addNewLocationToDB() {
		
		if (locationName.getText().toString().equals(""))	{
			showOkAlertDialog("Location Name Is Empty");

		}else {

			//--capture any changes from edit text------------------------
			newLocation.setLdate(myDate.getStrDate().toString());
			newLocation.setName(locationName.getText().toString());
			newLocation.setAddress(locationAddress.getText().toString());
	
			//--reset this until further enhancement---------------------------
			newLocation.setPicture(false);
			newLocation.setHoliday(false);
			newLocation.setRefid(0);
	
			//--add location
			newLocSaved=app.addHLocation(newLocation);
			
			if (newLocSaved)
				new deleteFileTask().execute(fileLocation.toString());
			
			finishUpdateDialog();
		}
	}
	
	//--add new location dialog------------------------------------------
	public void finishUpdateDialog() {
		receivedLocation= new AlertDialog.Builder(this)
		.setIcon(R.drawable.ppgp_pt)
		.setTitle("New Location Added")
		.setMessage("Go to PPGoPlaces main menu ? ")
		.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setResult(RESULT_OK);
				Intent intent = new Intent(LocationReceive.this, PPGoPlaces.class);
				startActivity(intent);
				receivedLocation.dismiss();
			}
		})
		.setNegativeButton("No, return to email", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setResult(RESULT_OK);
				finish();
				receivedLocation.dismiss();	//return to mail
			}
		})
		.create();
		receivedLocation.show();
	}
	
	//--add new location dialog------------------------------------------
	public void quitLocationDialog() {
		receivedLocation= new AlertDialog.Builder(this)
		.setIcon(R.drawable.ppgp_pt)
		.setTitle("Unsaved Location")
		.setMessage("Add location ? ")
		.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				addNewLocationToDB();
				setResult(RESULT_OK);
				receivedLocation.dismiss();
			}
		})
		.setNegativeButton("No, discard", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setResult(RESULT_CANCELED);
				finish();
				receivedLocation.cancel();	//return to mail
			}
		})
		.create();
		receivedLocation.show();
	}
	
	//=====SECTION 6==LISTENER METHODS================================

	//=====SECTION 7==HELPER METHODS==================================

	//==========DATE CHANGE AND DATE PICKER DIALOG=============	 
	//--Change location date routine
	public void changeDateClick(View v) {
		showDialog(LOCATIONDATE_DIALOG_ID);					
	}
	
	@Override
	 public Dialog onCreateDialog(int id) {
		 switch (id) {
			case LOCATIONDATE_DIALOG_ID:
			DatePickerDialog dialog= new DatePickerDialog (this,
						locDateSetListener,
						myDate.getCalDate().get(Calendar.YEAR),
						myDate.getCalDate().get(Calendar.MONTH),
						myDate.getCalDate().get(Calendar.DAY_OF_MONTH));
			return dialog;
		 }
		 return null;
	 }	
	 
	 private DatePickerDialog.OnDateSetListener locDateSetListener =
				new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year,
				int monthOfYear, int dayOfMonth) {
		Calendar d= Calendar.getInstance();
		d.set(year, monthOfYear, dayOfMonth);
		myDate.setCalDate(d);
		String locDate=myDate.getStrDate().toString();

		//--setCalDMY(TextView view, String dTitle, Calendar cdate)
		locationDate.setText(displayShortDMY(locDate));
		//setCalDMY(locationDate, "New Date: ", d);
		}
	 };

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!newLocSaved) {
				quitLocationDialog();		//prompt to save
			} 
		}
		return super.onKeyDown(keyCode, event);	
	}

	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	 
	 
	 
	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================


	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//=====SECTION 10==SANDBOX======================================
	
}		//END MAIN CLASS
