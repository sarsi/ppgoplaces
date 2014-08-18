/* 
 * Fragment: 											MySettings.java
 * Description:											User Preferences Setup
 * 																Setup gps, users preference and backup
 * 
 * Created: 												May 3, 2012
 * Changed last release: 						September 9, 2012
 * Last updated: 										October 15, 2013
 * 
  * Changes:
 * 	- Clean up codes to work in fragment 
 * 
 * 
 * 
 */

package com.liseem.android.travel;

import static com.liseem.android.travel.TravelLiteActivity.*;

import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import android.widget.TextView;

import com.liseem.android.travel.adapter.MyBackupTask;
//import com.liseem.android.travel.adapter.MyBackupTask.CompletionListener;
//import com.liseem.android.travel.DataBackupTask;
//import com.liseem.android.travel.DataBackupTask.CompleteListener;
import com.liseem.android.travel.items.MyDate;


public class MySettings extends Fragment
										 implements  MyBackupTask.CompletionListener {
	
	//=====SECTION 1==DECLARATION===================================
	
	private static final String 			TAG ="MyPreferences";
	
	private static final int 					REQUEST_MAP_ADDRESS=33;
	protected static final int 			SETTINGS_CHANGE=6;					//--from AddLocation
	//--release id: 446317952058360   debug id: 333670976716756
	public final static String 				FB_APPLICATION_ID="333670976716756";			
	static final Object[] 						sDataLock = new Object[0];			//--google backup manager cloud
	
	private SharedPreferences 			prefs;
	private PPGoPlacesApplication 	app;
	private Editor									editor;
	private BackupManager 				backupManager;								//--google backup manager cloud
	private boolean 							isExtStorageAvail;
	private Context 								context;
	
	//--Find My Location--------------------------------------------
	private Handler 								handler;
	private Geocoder 							geocoder;
	private LocationManager 				locationManager;
	private LocationListener 				locationListener;
	private Location								currentLocation;
	private Location 							bestResult;
	private Address 								address;
	private boolean 							updateAdd=true;							//--stop async post execute update
	private String 								useAddress;										//--return address from async task
	
	private boolean 							locationAvailable;
	private double 								cLatitude;
	private double 								cLongitude;
	private long 									minTime;
	private float 									bestAccuracy;
	private long 									bestTime;
	private List<Address> 					caddresses;
	private boolean 							addressAvailable;	
	
	private AlertDialog 						foundAddress;
	
	//--facebook integration--------------------------
	//private String 								access_token;
	//private long 									fb_expires;

	//--sandy----------------------------
	private static final int 					READ_BLOCK_SIZE=100;


	//--app variables----------------------------
	private MyDate 								today=new MyDate();
	
	//--view setup--------------------------------
	private EditText 							myName;
	private TextView 							infoView;
	private TextView 							lastBackup;
	private View 									getAddress;
	private CheckBox 							mapMode;
	private CheckBox 							scrollMode;
	private CheckBox 							mapAccur;
	private RadioGroup 						radioGroup;
	private RadioButton 						rb0;
	private RadioButton 						rb1;
	private RadioButton 						rb2;

	//--shared preference----------------------
	private boolean 							ppgarage;
	private String 								lastKnownBackup;
	private boolean 							satelliteMode;							//--mapview satellite mode
	private boolean 							scrollTextMode;						//--holiday and location listview scrolling textview on/off
	private boolean 							firstCheckIn;
	private boolean 							mapAccurMode;						//--findMyLocation false=ACCURACY_COARSE, true=ACCURACY_FINE
	private String 								emailSignature;						//--email signature
	
	//--view listeners----------------------------
	private AlertDialog 						restoreDB;
	private Address 								caddress;
	private String 								locationProvider;

	
	//--Dialog for loadDefaultCheckList--
	CharSequence[] items 					= { "his packing list", "her packing list", "cancel" };
	int itemsChecked;
	private AlertDialog 						loadDefaultList;



	
	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	//--setup application path, shared preferences, backup manager, date and call setupView
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Log.d(TAG,"159 onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//Log.d(TAG,"164 onCreateView");
	
		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;
	
		//--inflate layout for fragment 1------------------------------------------------
		View v=inflater.inflate(R.layout.my_prefs, container, false);
		//setRetainInstance(true);
		setHasOptionsMenu(true);
		return v;			
	}

	@Override
	public void onStart() {
		super.onStart();
		setupView();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		//setupView();
	} 
	
	/*@Override
	public void onBackPressed() {
			Intent intent =new Intent();
			setResult(RESULT_OK, intent);
		super.onBackPressed();
	}*/
	
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 3==SETUP VIEWS====================================
	//--setup view resources and get shared preferences
	protected void setupView() {
		
		Log.d(TAG, "208 Landed in setupView");
		//--setup application path--------------------------------------
		app								=(PPGoPlacesApplication)getActivity().getApplication();
		
		//--shared preferences setup-----------------------------------
		prefs							=getActivity().getSharedPreferences (PREFNAME, MODE_PRIVATE);
		editor							= prefs.edit();
		//SharedPreferences.Editor editor=prefs.edit();
		
		//--google cloud backup manager----------------------------
		backupManager			=new BackupManager(getActivity());			//--this never use context, call backupManager.dataChanged() below
		
		today.setCalDate(Calendar.getInstance());
		
		//--views setup-----------------------------------------------------------
		infoView						=(TextView)getActivity().findViewById(R.id.infoView);		
		lastBackup					=(TextView)getActivity().findViewById(R.id.lastBackup);		
		mapMode					=(CheckBox)getActivity().findViewById(R.id.mapCheck);
		mapAccur					=(CheckBox)getActivity().findViewById(R.id.mapAccur);
		scrollMode					=(CheckBox)getActivity().findViewById(R.id.scrollCheck);
		
		radioGroup 					=(RadioGroup) getActivity().findViewById(R.id.radioGroup1);
        rb0 								=(RadioButton) getActivity().findViewById(R.id.radio0);
        rb1 								=(RadioButton) getActivity().findViewById(R.id.radio1);
        rb2 								=(RadioButton) getActivity().findViewById(R.id.radio2);
        
       int currentDist=(int)app.getGeoFence();
        if (currentDist==5000) {
        	rb2.setChecked(true);
        } else if (currentDist==2000) {
        	rb1.setChecked(true);
        } else {
        	rb0.setChecked(true);
        }

		Log.d(TAG, "243 setupView, retreive from shared preferences");
		//--get SharedPreferences object--------------------------------------
		//prefs=getActivity().getSharedPreferences (PREFNAME, MODE_PRIVATE);
		ppgarage						=prefs.getBoolean("ppgarage", false);
		firstCheckIn					=prefs.getBoolean("firstCheckIn", true);
		
		lastKnownBackup		=prefs.getString("lastBackup", "no backup found");
		satelliteMode				=prefs.getBoolean("satelliteMode",false);
		scrollTextMode			=prefs.getBoolean("scrollTextMode",true);
		mapAccurMode			=prefs.getBoolean("mapAccurMode", false);
		emailSignature			=prefs.getString("emailSignature", "sent from \"PPGoPlaces\"");

		//--check external storage writeable------------------------
		isExtStorageAvail		=((TravelLiteActivity) getActivity()).checkExternalStorageWriteWrite();
		
		//--copyrights information at bottom of screen------------------
		infoView.setText("PPGoPlaces version " +app.getVersionName()+
				getString(R.string._copyright_2012_all_rights_reserved));
		
		//--display known last backup date--------------------------------------
		lastBackup.setText(getString(R.string.last_backup_ )+lastKnownBackup.toString());

		//--setup checkbox view and text----------------------------
		if (satelliteMode) {
			mapMode.setText(R.string.map_satellite_view_enable);
		} else {
			mapMode.setText(R.string.map_satellite_view_disable);
		}
		mapMode.setChecked(satelliteMode);		
		mapMode.setOnCheckedChangeListener(new mapModeOnChecked());		
		
		if (mapAccurMode) {
			mapAccur.setText(R.string.find_location_accurracy_high_slow);
		} else {
			mapAccur.setText(R.string.find_location_accuracy_low_fast);
		}
		mapAccur.setChecked(mapAccurMode);
		mapAccur.setOnCheckedChangeListener(new mapAccurOnChecked());
		
		if (scrollTextMode) {
			scrollMode.setText(R.string.list_scrolling_text_enable);
		} else {
			scrollMode.setText(R.string.list_scrolling_text_disable);
		}
		scrollMode.setChecked(scrollTextMode);
		scrollMode.setOnCheckedChangeListener(new scrollModeOnChecked());
		
		//--geo fence selection radio check buttons-----------------------------------------------
		radioGroup.setOnCheckedChangeListener(new android.widget.RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (rb0.isChecked()) {
					app.setGeoFence(1000);
				} else 				
				if (rb1.isChecked()) {
					app.setGeoFence(2000);
				} else {
					app.setGeoFence(5000);
				}				
			}			
		});
		
	}		//END setupView

	//=====SECTION 3==SETUP VIEWS====================================
	
	//=====SECTION 4==ADD ADAPTER===================================

	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================
	
	//--checked mapview satellite mode----------------------------------------------------------
	private class mapModeOnChecked implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			
			if (isChecked) {
				mapMode.setText(R.string.map_satellite_view_enable);
			} else {
				mapMode.setText(R.string.map_satellite_view_disable);
			}
			
			SharedPreferences.Editor editor=prefs.edit();
			editor.putBoolean("satelliteMode", isChecked);
			editor.commit();
			
		}
	}
	
	//--checked find location set accuracy----------------------------------------------------------
	private class mapAccurOnChecked implements OnCheckedChangeListener {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			
			if (isChecked) {
				mapAccur.setText(R.string.find_location_accurracy_high_slow);
			} else {
				mapAccur.setText(R.string.find_location_accuracy_low_fast);
			}
			SharedPreferences.Editor editor=prefs.edit();
			editor.putBoolean("mapAccurMode", isChecked);
			editor.commit();		
			mapAccurMode=isChecked;		//return to AddLocation
		}
	}
	
	//--checked listview scroll text view mode----------------------------------------------------------
	private class scrollModeOnChecked implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			
			if (isChecked) {
				scrollMode.setText(R.string.list_scrolling_text_enable);
			} else {
				scrollMode.setText(R.string.list_scrolling_text_disable);
			}
			SharedPreferences.Editor editor=prefs.edit();
			editor.putBoolean("scrollTextMode", isChecked);
			editor.commit();
			
		}
		
	}
		

	//--change email signature for share and send------------------------------------------------
	public void emailChangeClick() {
		
		AlertDialog.Builder signName= new AlertDialog.Builder(getActivity());
		signName.setTitle(R.string.change_email_signature);
		signName.setMessage(R.string.enter_email_signature_);

		// Set an EditText view to get user input 
		final EditText input = new EditText(getActivity());
		signName.setView(input);
		signName.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
			  Editable value = input.getText();
			  		SharedPreferences.Editor editor=prefs.edit();
					editor.putString("emailSignature", value.toString());
					editor.commit();
			  }
		});
		signName.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			  @Override
			public void onClick(DialogInterface dialog, int whichButton) {
			    // Canceled.
			  }
		});
		signName.show();
		
	}
	
	//--MyBackupTask Abstract Methods and Helpers-----------------------------------------------
	//--restore database from backup------------------------------------------------------------------
	public void onRestoreClick () {
		restoreDB= new AlertDialog.Builder(getActivity())
		.setTitle(getString(R.string.restore_database))
		.setMessage(getString(R.string.data_lost_)+
				getString(R.string._note_all_current_data_will_be_overwritten_))
		.setPositiveButton(R.string.yes_restore, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				restoreBackup();
				restoreDB.dismiss();
			}
		})
		.setNegativeButton(R.string.cancel_action, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				restoreDB.cancel();
			}
		})
		.create();
		restoreDB.show();
	} 
	
	protected void restoreBackup() {
		if( Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ) {
			MyBackupTask restoreTask = new MyBackupTask(getActivity());
			restoreTask.setCompletionListener(this);			//--must set listener in MyBackupTask for callback
			restoreTask.execute(MyBackupTask.COMMAND_RESTORE);
			//getActivity().finish();
			getFragmentManager().popBackStackImmediate();
		}
	} 
	//--restore database from backup------------------------------------------------------------------

	//--dialog backup database from /mnt/sdcard/PPGoPlacesBackups----------------------------------	
	public void onBackupClick() {
		restoreDB= new AlertDialog.Builder(getActivity())
		.setTitle(getString(R.string.backup_database))
		.setMessage(getString(R.string.important_)+
				getString(R.string._note_do_not_backup_if_your_current_information_is_corrupted_))
		.setPositiveButton(R.string.yes_backup, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				backupDB();
				restoreDB.dismiss();
			}
		})
		.setNegativeButton(R.string.cancel_action, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				restoreDB.cancel();
			}
		})
		.create();
		restoreDB.show();
	}
	
	//--start backup db routine-------------------------
	protected void backupDB() {
		if( Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ) {
			MyBackupTask backupTask = new MyBackupTask(getActivity());
			backupTask.setCompletionListener(this);				//--MUST setup listenering call back from MyBackupTask
			//Log.d(TAG,"471 backupDB, setCompletionListener(this)");
			backupTask.execute(MyBackupTask.COMMAND_BACKUP);
			googleBackup();
			//getActivity().finish();
			getFragmentManager().popBackStackImmediate();
		}
	} 
 
	//=====SECTION 6==LISTENER METHODS================================

	//=====SECTION 7==HELPER METHODS==================================
	protected void googleBackup() {
		
		//--execute google. backupManager--------------------------
		backupManager.dataChanged();			//--context.getPackageName() 
	}
	
	//--backup database from /mnt/sdcard/PPGoPlacesBackups----------------------------------	


	//--completion listener  3 interfaces from MyBackupTask.java, onBackupComplete(), onRestoreComplete()
	//--and onError(int errorCode);
	@Override
	public void onBackupComplete() {
		prefs=getActivity().getSharedPreferences (PREFNAME, MODE_PRIVATE);
		SharedPreferences.Editor editor=prefs.edit();
		editor.putString("lastBackup", today.getDMYDate().toString());
		editor.putInt("transB4bup",0);			//reset transaction counter
		editor.commit();
		lastBackup.setText(today.getDMYDate().toString());
		Toast.makeText(getActivity(), getString(R.string.backup_completed), Toast.LENGTH_LONG).show();
		//((TravelLiteActivity) getActivity()).makeLongToast(getString(R.string.backup_completed));
	}

	@Override
	public void onRestoreComplete() {
		Toast.makeText(getActivity(), getString(R.string.restore_successfully), Toast.LENGTH_LONG).show();
		SharedPreferences.Editor editor=prefs.edit();
		editor.putBoolean("firstCheckIn", false);		//if restore successful then do not load new checklist
		editor.commit();
	}

	@Override
	public void onError(int errorCode) {
		if(errorCode == MyBackupTask.RESTORE_NOFILEERROR) {
				Toast.makeText(getActivity(), getString(R.string.no_backup_found_to_restore), Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getActivity(), getString(R.string.error_during_operation_)+errorCode, Toast.LENGTH_LONG).show();
			}
		} 
	//--MyBackupTask Abstract Methods and Helpers-----------------------------------------------
		
	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================
	
	//--menu choice-----------------------------------------------------------------------------------------------------
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
	
	private void CreateMenu(Menu menu) {
		
		MenuItem mnu1=menu.add(0, 1 , 1, R.string.backup_data);
		{ mnu1.setIcon(android.R.drawable.ic_menu_save); }
		
		MenuItem mnu2=menu.add(0, 2, 2, R.string.restore_data);
			{ mnu2.setIcon(android.R.drawable.ic_menu_set_as); }
			
		//MenuItem mnu3=menu.add(0, 3 , 3, R.string.reset_checklist);
		//{ mnu3.setIcon(android.R.drawable.ic_menu_revert); }
		
		MenuItem mnu4=menu.add(0, 4, 4, R.string.email_signature);
		{ mnu4.setIcon(android.R.drawable.ic_menu_send); }		
		
		MenuItem mnu5=menu.add(0, 5 , 5, R.string.about);
		{ mnu5.setIcon(android.R.drawable.ic_input_get); }
		
		MenuItem mnu6=menu.add(0, 6, 6, R.string.help);
		{ mnu6.setIcon(android.R.drawable.ic_menu_help); }		
	}
	
	private boolean MenuChoice(MenuItem item)
	{
		switch (item.getItemId()) {
		case 1:
		if (isExtStorageAvail) {
			onBackupClick();
			return true;
		} else {
			((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.sdcard_is_not_found));
			return false;
		}  //isExtStorageAvail
		case 2:
		if (isExtStorageAvail) {
			onRestoreClick();
			return true;
		} else {
			((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.sdcard_is_not_found));
			return false;
		}  //isExtStorageAvail		
		case 4:
			emailChangeClick();
			return true;
		case 5:
			Intent  aboutppgp=new Intent(getActivity(), SimpleHelp.class);
			aboutppgp.putExtra("helpPage", "about.html");
			startActivity(aboutppgp);
			return true;
		case 6:
			Intent  simplehelp=new Intent(getActivity(), SimpleHelp.class);
			simplehelp.putExtra("helpPage", "settings.html");
			startActivity(simplehelp);
			return true;
		}
		return false;
	}
	//--menu choice-----------------------------------------------------------------------------------------------------

	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//=====SECTION 10==SANDBOX======================================
	

	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //facebook.authorizeCallback(requestCode, resultCode, data);
    }	
	
} //--END MAIN CLASS
