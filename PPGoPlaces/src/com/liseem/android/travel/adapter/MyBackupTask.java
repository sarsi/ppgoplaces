/*
 * Fragment: 											MyBackupTask.java
 * Description:											Async task DB and shared preferences backup
 * 
 * Created: 												May 3, 2012
 * Changed last release: 						September 9, 2012
 * Last updated: 										October 19, 2013
 * 
 * 
 * Associated files:
 * Intent call from main fragment: 		MySettings.java (formerly MyPreferences)
 * 
 * Changes:
 * - fixed null error with fragment, need to use "this.app" in place of "mContext"
 * 
 */

package com.liseem.android.travel.adapter;

import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;
import static com.liseem.android.travel.TravelLiteActivity.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.IOException;
import java.nio.channels.FileChannel;

import com.liseem.android.travel.FragMain;
import com.liseem.android.travel.PPGoPlaces.OnMenuSelectedListener;
import com.liseem.android.travel.PPGoPlacesApplication;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class MyBackupTask extends AsyncTask<String,Integer,Integer> {

	//--constants-----------------------------------------
	private static final String 			TAG="MyBackupTask";
	
	public static final int 					BACKUP_SUCCESS = 1;
	public static final int 					RESTORE_SUCCESS = 2;
	public static final int 					BACKUP_ERROR = 3;
	public static final int 					RESTORE_NOFILEERROR = 4;
	public static final String 				COMMAND_BACKUP = "backupDatabase";
	public static final String 				COMMAND_RESTORE = "restoreDatabase";
	
	//private Context 							mContext;
	private CompletionListener 			listener;
	private PPGoPlacesApplication 	app;
	private SharedPreferences 			prefs;
	private boolean 							ppgarage=false;
	private File 									dbBackup;
	private File 									prefsBackup;
	private File 									catBackup;
	private File 									checklistBackup;
	
	private ProgressDialog 					pDialog;
	//private OnMenuSelectedListener	mCallback;

	
	
	//--create interface for activity to "implements MyBackupTask.CompletionListener"------
	//--this is where the unimplemented methods showup on class title
	
	//--Part 1 of 2 - setup interface
	public interface CompletionListener {
		void onBackupComplete();
		void onRestoreComplete();
		void onError(int errorCode);
	}
	
	//--Part 2 of 2 -setter call from calling fragment to set variable listener, i.e. not null 
	public void setCompletionListener(CompletionListener aListener) {
		listener 		= aListener;
	}
	

	
	//--constructor---------------------------------------
	public MyBackupTask(Activity activity) {					//--replace (Context context) 
		super();
		//mContext=context;
		//--setup path to application------------------------------------------------
		app				=(PPGoPlacesApplication)activity.getApplicationContext();		//--replace context with activity
		
		//--get SharedPreferences object--------------------------------------
		prefs			=app.getSharedPreferences (PREFNAME, MODE_PRIVATE);
		ppgarage		=prefs.getBoolean("ppgarage", false);
	}
	
		
	@Override
	protected Integer doInBackground(String... params) {
	
		//--get package name to create package in android data directory---
		//PackageManager pm;
		//String pmName=context.getPackageName();
		
		Log.d(TAG,"92 doInBackground");
		
		//--get path to the database-----------------------------------------------
		//File dbFile = mContext.getDatabasePath(DATABASE_NAME);
		File dbFile = this.app.getDatabasePath(DATABASE_NAME);
		Log.d(TAG, "97 dbFile file name "+dbFile.getName().toString());
		File ppPrefs=new File("/data/data/com.liseem.android.travel/shared_prefs/ppPrefs.xml");
		File catList=new File("/data/data/com.liseem.android.travel/shared_prefs/checklist.xml");
		File userDefault=new File("/data/data/com.liseem.android.travel/files/userchecklist.txt");
		
		//--find directory location for the backup, create if not exists-----
		
		//--old implement save to external
		//File exportDir = new File(Environment.getExternalStorageDirectory(), "PPGoPlacesBackups");
		//String destPath=Environment.getDataDirectory()+"/"+context.getPackageName()+"/PPGoPlacesBackups";
		//File exportDir=new File(Environment.getDataDirectory(), "PPGoPlacesBackups");			
		//File exportDir=new File(mContext.getFilesDir(),"PPGoPlacesBackups");
		//--backup to directory not found, create one----------------------------
		/*if (!exportDir.exists()) {
			exportDir.mkdirs();
		}*/
		//File backup = new File(exportDir, dbFile.getName());
		
		if(!ppgarage) {
			//v1.2
			dbBackup=new File(this.app.getFilesDir(), dbFile.getName());		//--changed mContext to this.app
			
			//v1.3
			prefsBackup=new File(this.app.getApplicationContext().getFilesDir(), ppPrefs.getName());
			catBackup=new File(this.app.getApplicationContext().getFilesDir(), catList.getName());
			checklistBackup=new File(this.app.getApplicationContext().getFilesDir(), userDefault.getName());
			
			//--ALTERNATIVE TESTING--------
			/*File exportDir=new File(Environment.getExternalStorageDirectory(), "Android/data/com.liseem.android.travel/ppgoplaces/rescue");
			if (!exportDir.exists()) {
				exportDir.mkdirs();
			}
			backup = new File(exportDir, dbFile.getName());
			prefsBackup=new File(exportDir, ppPrefs.getName());
			checklist=new File(exportDir, userDefault.getName());*/
		} else {
			//--ppgarage rescue backup or restore file to external directory-------------------------------------
			File exportDir = new File(Environment.getExternalStorageDirectory(), "PPGoPlacesRescue");

			if (!exportDir.exists()) {
				exportDir.mkdirs();
			}
			dbBackup = new File(exportDir, dbFile.getName());
			prefsBackup=new File(exportDir, ppPrefs.getName());
			catBackup= new File(exportDir, catList.getName());
			checklistBackup=new File(exportDir, userDefault.getName());
			SharedPreferences.Editor editor=prefs.edit();
			editor.putBoolean("ppgarage", false);
			editor.commit();
		}
		
		//--check the request operation-------------------------------------------
		String command = params[0];
		if(command.equals(COMMAND_BACKUP)) {
			
			//--attempt backup via file copy
			try {
				dbBackup.createNewFile();				
				fileCopy(dbFile, dbBackup);			//backing up db
				
				//--backup shared preferences---------------
				prefsBackup.createNewFile();
				fileCopy(ppPrefs, prefsBackup);	//backing up sharedPreferences
				
				if (catList.exists()) {
					catBackup.createNewFile();
					fileCopy(catList, catBackup);
				}
				
				if (userDefault.exists()) {
					checklistBackup.createNewFile();
					fileCopy(userDefault, checklistBackup);
				}

				Log.d(TAG, "backup successfully written");
				return BACKUP_SUCCESS;
			} catch (IOException e) {
				return BACKUP_ERROR;
			}
		} else if(command.equals(COMMAND_RESTORE)) {
			
			//--attempt to restor via file copy
			try {
				if(!dbBackup.exists()) {
					return RESTORE_NOFILEERROR;
				}
								
				dbFile.createNewFile();
				fileCopy(dbBackup, dbFile);			//--restoring db backup
				
				prefsBackup.createNewFile();
				fileCopy(prefsBackup, ppPrefs); 	//--restoring sharedPreferences backup
				
				if (catBackup.exists()) {
					catBackup.createNewFile();
					fileCopy(catBackup, catList); 		//--restoring sharedPreferences backup
				}

				if (checklistBackup.exists()) {
					checklistBackup.createNewFile();
					fileCopy(checklistBackup, userDefault);
				}
				return RESTORE_SUCCESS;
			} catch (IOException e) {
				return BACKUP_ERROR;
			}
		} else {
			return BACKUP_ERROR;
		}
	}	//END doInBackground()

	private File getDatabasePath(String string) {
		return null;
	}

	//--update user on status----------------------------------------------------
	@Override
	protected void onPostExecute(Integer result) {
		//pDialog.dismiss();
		switch(result) {
		case BACKUP_SUCCESS:
			if(listener != null) {
				listener.onBackupComplete();
			}
			break;
		case RESTORE_SUCCESS:
			if(listener != null) {
				listener.onRestoreComplete();
			}
			break;
		case RESTORE_NOFILEERROR:
			if(listener != null) {
				listener.onError(RESTORE_NOFILEERROR);
			}
			break;
		default:
			if(listener != null) {
				listener.onError(BACKUP_ERROR);
			}
		}
		
	}		//END onPostExecute
	
	//--file copy helper--------------------------------------------------------------------
	private void fileCopy(File source, File dest) throws IOException {
		FileChannel inChannel = new FileInputStream(source).getChannel();
		FileChannel outChannel = new FileOutputStream(dest).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}		
	}  //END filCopy()	

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		//Log.d(TAG,"now landed in onPreExecute.. before mContext"); //KEEP CRASHING
		/*pDialog=new ProgressDialog(app.getBaseContext());		//CANNOT USE CONTEXT
		pDialog.setMessage("Wait ... performing file action");
		pDialog.setIndeterminate(true);
		pDialog.setCancelable(false);
		//pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		//pDialog.setProgress(0);
		//pDialog.setMax(100);
		pDialog.show();*/
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		//pDialog.setProgress(values[0]);
	}
	
}		//--END MAIN CLASS
