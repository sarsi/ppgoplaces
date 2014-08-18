/*
 * MyBackupAgent for Google cloud backup services
 */

package com.liseem.android.travel;

import static com.liseem.android.travel.TravelLiteActivity.*;
import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;

import java.io.IOException;

import com.liseem.android.travel.items.TravelLiteDBAdapter;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;

public class MyBackupAgent extends BackupAgentHelper {
	
	//=====SECTION 1==DECLARATION===================================

	private static final String TAG ="MyBackupAgent";
	
	static final String PREFS_HELPER ="xmlPrefs";	
	static final String PREF_HELPER_KEY ="sharedPrefs";			
	static final String DATABASE_BACKUP_KEY="myholiday";	
	
	// --Object for intrinsic lock
	private PPGoPlacesApplication app;

	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	@Override
	public void onCreate() {
		
		app=(PPGoPlacesApplication)getApplicationContext();
		SharedPreferencesBackupHelper xmlhelper=new SharedPreferencesBackupHelper(this, PREFNAME);		//name of shared Prefs
		addHelper(PREF_HELPER_KEY, xmlhelper);			//key 
		FileBackupHelper dbHelper=new FileBackupHelper(this, DATABASE_NAME);		//name of db
		addHelper(DATABASE_BACKUP_KEY, dbHelper);
	}
	
	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
	          ParcelFileDescriptor newState) throws IOException {
	    // Hold the lock while the FileBackupHelper performs backup
	    synchronized (TravelLiteDBAdapter.sDataLock) {
	        super.onBackup(oldState, data, newState);
	    }
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode,
	        ParcelFileDescriptor newState) throws IOException {
	    // Hold the lock while the FileBackupHelper restores the file
	    synchronized (TravelLiteDBAdapter.sDataLock) {
	        super.onRestore(data, appVersionCode, newState);
	    }
	}
	//=====SECTION 2==LIFECYCLE METHODS===============================

}	//END MAIN CLASS
