/* 
 * Database Adapter: 							TravelLiteDBAdapter.java 
 * Description: 									SQLite Database and inner class for SQLite Open Helper
 * 															All query are managed through PPGoPlacesApplication as methods
 * 															which handled db.helper().
 *  
 * Created: 											May 3rd, 2012 (first documented)
 * Last major change:							June 21, 2012		(build 108)	
 * Last updated:									October 1, 2013
 *
 * 
 * There are total of 6 database tables: 
 * - Location - or Place of Interest, with addresses and geo information.
 * - Holiday 	- contains period of holiday and countries.
 * - Pictures - Images reference, contains references to all images taken on location in main media.
 * - Shopping list - shopping list
 * - Check list - includes holiday packing list.
 * - Expenses - expenses tracker NOT IMPLEMENTED
 * 
 * Parking lots
 * - need to get the storing and reading blob work.
 * 
 * Changes since last release:
 *
 * 
 * Bug fixes pending: 
 * 
 * 
 * 
 * NOTES: 
 * 
 */

package com.liseem.android.travel.items;


import static com.liseem.android.travel.TravelLiteActivity.*;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import com.liseem.android.travel.PPGoPlacesApplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//import android.os.AsyncTask;
import com.liseem.android.travel.items.AsyncTask;
import android.util.Log;



public class TravelLiteDBAdapter {
		
	public static final String TAG								="TravelLiteDBAdapter";
	public static final Object[] sDataLock 				= new Object[0];			//--google backup manager cloud


	//--Start of Fields and constants declaration-----------------------------------------
	
	//--CONSTANTS for Holiday Table-------------------------------------------------------
	public static final String HOL_ROWID					="_id";
	public static final String HOL_REFID					="refid";	
	public static final String HOL_HOLIDAY				="holiday";
	public static final String HOL_COUNTRY				="country";
	public static final String HOL_CITY						="city";							//--ver 2
	public static final String HOL_STARTDATE			="start_date";
	public static final String HOL_ENDDATE				="end_date";
	public static final String HOL_NOTES					="hol_notes";

	//--CONSTANTS for Location Table-----------------------------------------------------
	public static final String LOC_ROWID					="_id";
	public static final String LOC_REFID					="refid";							//--same as Holiday rowid
	public static final String LOC_NAME					="name";
	public static final String LOC_DATE						="loc_date";
	public static final String LOC_ADDRESS				="address";
	public static final String LOC_STREET					="streetaddress";			//--ver 2
	public static final String LOC_CITY						="city";							//--ver 2
	public static final String LOC_POSTALCODE		="postalcode";				//--ver 2
	public static final String LOC_COUNTRY				="country";						//--ver 2
	public static final String LOC_NOTES					="notes";
	public static final String LOC_LONGITUDE			="longitude";
	public static final String LOC_LATITUDE				="atitude";
	public static final String LOC_OBJECT					="loc_obj";	
	public static final String LOC_INFO						="info";							//--can be used as category
	public static final String LOC_PICTURE				="picture";
	public static final String LOC_HOLIDAY				="holiday";
	
	//--CONTANTS for Shopping List, Get Milk, Check List and Packing List Table-------
	public static final String ITEM_ROWID 				="id";
	public static final String ITEM_REFID 					="refid";							//--same as holiday  rowid
	public static final String ITEM_NAME 					="name";
	public static final String ITEM_COMPLETE  			="complete";
	public static final String ITEM_PRICE					="price";
	public static final String ITEM_ADDRESS 			="address";	
	public static final String ITEM_LATITUDE 			="latitude";
	public static final String ITEM_LONGITUDE 		="longitude";
	public static final String ITEM_CAT						="category";					//--ver 2
	public static final String ITEM_NOTES 				="notes";
	public static final String ITEM_SEASON				="season";						//--ver 3 packing list
	public static final String ITEM_WEATHER			="weather";					//--ver 3 packing list
	public static final String ITEM_GENDER				="gender";						//--ver 3 packing list
	public static final String ITEM_TRIP						="trip";							//--ver 3 packing list
	public static final String ITEM_SHORTTRIP			="shorttrip";					//--ver 3 packing list
	public static final String ITEM_EVENT					="event";						//--ver 3 check and shopping list
	public static final String ITEM_LOCATION			="location";						//--ver 3 check and shopping list
		
	
	//--CONTANTS for Picture List Table-------------------------------------------------
	public static final String PIC_ROWID 					="id";
	public static final String PIC_REFID 					="refid";							//--same as location id
	public static final String PIC_NAME 					="name";
	public static final String PIC_DATE						="pic_date";
	public static final String PIC_LOCATION  			="location";
	public static final String PIC_ADDRESS 				="address";
	public static final String PIC_PATH						="pic_path";
	public static final String PIC_MARK						="mark";
	public static final String PIC_OBJECT 					="pic_obj";
	public static final String PIC_ORIENT					="pic_orientation";			//--ver 2, first digit orientation, second scalefactor
	public static final String PIC_NOTES 					="notes";							//--orient, 1,3,4,6,8 and scale depends
	
	//--CONTANTS for Expenses Table-- Version 2------------------------------------
	public static final String EX_ROWID 					="id";
	public static final String EX_REFID 						="refid";							//--same as holiday id
	public static final String EX_LOCID						="locId";
	public static final String EX_NAME 						="name";
	public static final String EX_DATE						="ex_date";
	public static final String EX_LOCATION  				="location";
	public static final String EX_FX 							="fxRate";
	public static final String EX_AMNT						="amount";						//--amount in foreign amount
	public static final String EX_LOCAMNT				="local_amnt";
	public static final String EX_NOTES 					="notes";
	public static final String EX_CAT							="category";		
	
	//--CONSTANTS for database and tables name--
	public static final String DATABASE_NAME								="myholiday.db";
	public static final String DATABASE_HOLIDAY_TABLE			="holidayTable";
	public static final String DATABASE_LOCATION_TABLE			="locationTable";
	public static final String DATABASE_SHOPPING_TABLE			="shoppingTable";
	public static final String DATABASE_CHECKLIST_TABLE			="holidayCheckList";
	public static final String DATABASE_PICTURE_TABLE				="pictureTable";
	public static final String DATABASE_EXPENSES_TABLE			="expensesTable";		//--ver2
	public static final String DATABASE_PACKINGLIST_TABLE		="packingList";			//--ver3
	public static final String DATABASE_GETMILK_TABLE			="getMilk";					//--ver3
	
	
	public static final int DATABASE_VERSION=2;			//--need to change to force upgrade to new tables
	
	public static final String TABLE_CREATE_HOLIDAY = "create table "+
			DATABASE_HOLIDAY_TABLE+" ("+
			HOL_ROWID+" integer primary key autoincrement, "+
			HOL_REFID+" integer, "+
			HOL_HOLIDAY+" text, "+
			HOL_COUNTRY+" text, "+
			HOL_CITY+" text, "+
			HOL_STARTDATE+" text, "+
			HOL_ENDDATE+" text, "+
			HOL_NOTES+" text);";
	
	public static final String TABLE_CREATE_LOCATION="create table "+
			DATABASE_LOCATION_TABLE+" ("+
			LOC_ROWID+" integer primary key autoincrement, "+
			LOC_REFID+" integer, "+
			LOC_NAME+" text, "+
			LOC_DATE+" text, "+
			LOC_ADDRESS+" text, "+
			LOC_STREET+" text, "+
			LOC_CITY+" text, "+
			LOC_POSTALCODE+" text, "+
			LOC_COUNTRY+" text, "+
			LOC_NOTES+" text, "+
			LOC_LATITUDE+" double, "+
			LOC_LONGITUDE+" double, "+
			LOC_OBJECT+" blob, "+
			LOC_INFO+" text, "+
			LOC_PICTURE+" text, "+
			LOC_HOLIDAY+" text);";
	
	public static final String TABLE_CREATE_SHOPPING = "create table "+
			DATABASE_SHOPPING_TABLE+" ("+
			ITEM_ROWID+" integer primary key autoincrement, "+
			ITEM_REFID+" integer, "+
			ITEM_NAME+" text, "+
			ITEM_COMPLETE+" text, "+
			ITEM_PRICE+" text, "+
			ITEM_ADDRESS+" text, "+
			ITEM_LATITUDE+" double, "+
			ITEM_LONGITUDE+" double, "+
			ITEM_CAT+" text, "+
			ITEM_NOTES+" text);";
	
	//--ver 3
	public static final String TABLE_CREATE_GETMILK = "create table "+
			DATABASE_GETMILK_TABLE+" ("+
			ITEM_ROWID+" integer primary key autoincrement, "+
			ITEM_REFID+" integer, "+
			ITEM_NAME+" text, "+
			ITEM_COMPLETE+" text, "+
			ITEM_PRICE+" text, "+
			ITEM_ADDRESS+" text, "+
			ITEM_LATITUDE+" double, "+
			ITEM_LONGITUDE+" double, "+
			ITEM_CAT+" text, "+
			ITEM_NOTES+" text);";
	
	public static final String TABLE_CREATE_CHECKLIST = "create table "+
			DATABASE_CHECKLIST_TABLE+" ("+
			ITEM_ROWID+" integer primary key autoincrement, "+
			ITEM_REFID+" integer, "+
			ITEM_NAME+" text, "+
			ITEM_COMPLETE+" text, "+
			ITEM_ADDRESS+" text, "+
			ITEM_LATITUDE+" double, "+
			ITEM_LONGITUDE+" double, "+
			ITEM_CAT+" text, "+
			ITEM_NOTES+" text, "+
			ITEM_EVENT+" text, "+				//--ver3
			ITEM_LOCATION+" integer);";		//--ver3
	
	//--ver 3
	public static final String TABLE_CREATE_PACKINGLIST = "create table "+				
			DATABASE_PACKINGLIST_TABLE+" ("+
			ITEM_ROWID+" integer primary key autoincrement, "+
			ITEM_REFID+" integer, "+
			ITEM_NAME+" text, "+
			ITEM_COMPLETE+" text, "+
			ITEM_ADDRESS+" text, "+
			ITEM_LATITUDE+" double, "+
			ITEM_LONGITUDE+" double, "+
			ITEM_CAT+" text, "+
			ITEM_NOTES+" text, "+							
			ITEM_SEASON+" text, "+					//--new from checklist
			ITEM_GENDER+" text, "+					//--new from checklist
			ITEM_TRIP+" text, "+							//--new from checklist
			ITEM_SHORTTRIP+" text);";				//--new from checklist


	
	public static final String TABLE_CREATE_PICTURE = "create table "+
			DATABASE_PICTURE_TABLE+" ("+
			PIC_ROWID+" integer primary key autoincrement, "+
			PIC_REFID+" integer, "+
			PIC_NAME+" text, "+
			PIC_DATE+" text, "+
			PIC_LOCATION+" text, "+
			PIC_ADDRESS+" text, "+
			PIC_PATH+" text, "+
			PIC_MARK+" text, "+
			PIC_OBJECT+" blob, "+
			PIC_ORIENT+" integer, "+
			PIC_NOTES+" text);";
	
	public static final String TABLE_CREATE_EXPENSES = "create table "+
			DATABASE_EXPENSES_TABLE+" ("+
			EX_ROWID+" integer primary key autoincrement, "+
			EX_REFID+" integer, "+
			EX_LOCID+" integer, "+
			EX_NAME+" text, "+
			EX_DATE+" text, "+
			EX_LOCATION+" text, "+
			EX_FX+" integer, "+
			EX_AMNT+" integer, "+
			EX_LOCAMNT+" integer, "+
			EX_NOTES+" text, "+
			EX_CAT+" text);";
			

	
	private Context 							context;
	private SharedPreferences 		prefs;
	
	public SQLiteOpenHelper 			dbHelper;
	public SQLiteDatabase 				db;
	public ContentValues 				values;
	
	protected HLocation 				hlocation;
	protected Holiday 					holiday;
	protected LPicture 					lpic;
	protected Shopping 					shopping;
	protected TaskList 					task;
	protected boolean 					dbEmpty;
	//protected MyPref 						myprefs;

	public ArrayList<Shopping> 		currentShoppingList;
	public ArrayList<TaskList> 		currentCheckList;
	public ArrayList<Holiday> 		currentHolidayList;
	public ArrayList<HLocation> 	currentLocationList;
	public ArrayList<LPicture> 		currentPictureList;

	public PPGoPlacesApplication 	app;
	//--End of Fields and Constants declaration--
	
	//--TavelLiteDBAdapter Constructor--
	public TravelLiteDBAdapter(Context context) {
		this.context							=context;
		dbHelper									=new TravelLiteDBHelper(context);
		app											=(PPGoPlacesApplication)context.getApplicationContext();
		prefs										=app.getSharedPreferences (PREFNAME, MODE_PRIVATE);
	}
	
	
	/*==================================================================
	 * START OF DBHELPER INNER CLASS
	 * =================================================================
	 */
	public class TravelLiteDBHelper extends SQLiteOpenHelper {
		
		
		//--Class Constructors----------------------------------------------------------------------
		public TravelLiteDBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
	
		//--onCreate()--first time create new tables-------------------------------------------
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TABLE_CREATE_HOLIDAY);
			db.execSQL(TABLE_CREATE_LOCATION);	
			db.execSQL(TABLE_CREATE_SHOPPING);
			db.execSQL(TABLE_CREATE_CHECKLIST);
			db.execSQL(TABLE_CREATE_PICTURE);
			db.execSQL(TABLE_CREATE_EXPENSES);
			db.execSQL(TABLE_CREATE_PACKINGLIST);					//--ver 3
			db.execSQL(TABLE_CREATE_GETMILK);							//--ver 3
			

			//--For version change example Version 1 to Version 2
			//--implement dropandCreate(db);
		}
	
		//--AVOID-- dropandcreate destructive method for version upgrade-------------
		/*
		 * db.execSQL("drop table if exists "+DATABASE_HOLIDAY_TABLE+";");
		 * db.execSQL("drop table if exists "+DATABASE_LOCATION_TABLE+";");
		 */
		
		//--onUpgrade method, a non destructive approach attempting to retain data--
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
						
			//--moving to version 3
			//--for version 1 to version 2, when moving to version 3 just replace all below with new
			//--there is none version 1 out there anymore....
			if (newVersion > oldVersion) {
				db.execSQL("alter table "+DATABASE_CHECKLIST_TABLE+" add column "+ITEM_EVENT+" text");
				db.execSQL("alter table "+DATABASE_CHECKLIST_TABLE+" add column "+ITEM_LOCATION+" integer");
				db.execSQL(TABLE_CREATE_PACKINGLIST);
				db.execSQL(TABLE_CREATE_GETMILK);
			}
			//--old version 1 to version 2
			/*
			 * 			if (newVersion > oldVersion) {
				db.execSQL("alter table "+DATABASE_HOLIDAY_TABLE+" add column "+HOL_CITY+" text");
				db.execSQL("alter table "+DATABASE_LOCATION_TABLE+" add column "+LOC_STREET+" text");
				db.execSQL("alter table "+DATABASE_LOCATION_TABLE+" add column "+LOC_CITY+" text");
				db.execSQL("alter table "+DATABASE_LOCATION_TABLE+" add column "+LOC_POSTALCODE+" text");
				db.execSQL("alter table "+DATABASE_LOCATION_TABLE+" add column "+LOC_COUNTRY+" text");
				db.execSQL("alter table "+DATABASE_SHOPPING_TABLE+" add column "+ITEM_CAT+" text");
				db.execSQL("alter table "+DATABASE_CHECKLIST_TABLE+" add column "+ITEM_CAT+" integer");
				db.execSQL("alter table "+DATABASE_PICTURE_TABLE+" add column "+PIC_ORIENT+" integer");
				db.execSQL(TABLE_CREATE_EXPENSES);
			}
			 */
		}
	}
	/*==================================================================
	 * END OF DBHelper
	 * =================================================================
	 */
	
	/* ================================================================================
	 * START OF SQLITE DATABASE methods
	 * ================================================================================
	 */	
	public TravelLiteDBAdapter open() throws SQLException
	{
		db=dbHelper.getWritableDatabase();
		return this;
	}
	
	public TravelLiteDBAdapter open_readonly() throws SQLException
	{
		db=dbHelper.getReadableDatabase();
		return this;
	}
	
	public void close()
	{
		db.close();
	}
	

	/* ================================================================================
	 * END OF DATABASE methods
	 * ================================================================================
	 */	
		
	
	/* ================================================================================
	 * START OF LOCATION TABLE CRUD methods
	 * ================================================================================
	 */
	
	//=====CREATE NEW RECORD==================LOCATION===================

	//--CREATE --Add New Location------------------------------------------------------------------
	public long insertLocation(HLocation location) {
		long dbrowid=0;
		try {
			dbrowid=new addNewLocation().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, location).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return dbrowid;
	}
	
	public boolean addLocation(HLocation location) {
		long dbrowid=0;
		try {
			dbrowid=new addNewLocation().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, location).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		if (dbrowid>0) {
			return true;
		} else {
			return false;			
		}
	}
	
	private class addNewLocation extends AsyncTask<HLocation, Void, Long> {

		@Override
		protected Long doInBackground(HLocation... params) {
			HLocation location=new HLocation();
			location=params[0];
			ContentValues values=new ContentValues();
			values.put(LOC_REFID, location.getRefid());
			values.put(LOC_NAME, location.getName());
			values.put(LOC_DATE, location.getLdate());
			values.put(LOC_ADDRESS,location.getAddress());
			values.put(LOC_STREET,location.getStreet());
			values.put(LOC_CITY,location.getCity());
			values.put(LOC_POSTALCODE,location.getPostal());
			values.put(LOC_COUNTRY,location.getCountry());		
			values.put(LOC_NOTES,location.getNotes());
			values.put(LOC_LATITUDE,location.getLatitude());
			values.put(LOC_LONGITUDE,location.getLongitude());
			values.put(LOC_OBJECT, location.getAddressObj());
			values.put(LOC_INFO,location.getInfo());		
			boolean boolPic=location.getPicture();
			values.put(LOC_PICTURE, Boolean.toString(boolPic));
			boolean boolHol=location.getHoliday();
			values.put(LOC_HOLIDAY, Boolean.toString(boolHol));
			return db.insert(DATABASE_LOCATION_TABLE, null, values);	
		}		
	}
	

	
	//=====READ RECORD=======================LOCATION===================
	
	//--READ--Retrieves Single Location Record---------------------------------------------------
	public HLocation getLocation(long dbRowId) {
		HLocation hLocation=new HLocation();
		try {
			hLocation=new retrieveLocation().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,dbRowId).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return hLocation;
	}
	
	private class retrieveLocation extends AsyncTask<Long, Void, HLocation> {

		@Override
		protected HLocation doInBackground(Long... params) {
			long dbRowId=params[0];
			hlocation=new HLocation();
			Cursor cursor=db.query(true, DATABASE_LOCATION_TABLE, new String[] {
					LOC_ROWID,
					LOC_REFID,
					LOC_NAME,
					LOC_DATE,
					LOC_ADDRESS,
					LOC_STREET,
					LOC_CITY,
					LOC_POSTALCODE,
					LOC_COUNTRY,
					LOC_NOTES,
					LOC_LATITUDE,
					LOC_LONGITUDE,
					LOC_OBJECT,
					LOC_INFO,
					LOC_PICTURE,
					LOC_HOLIDAY},
					LOC_ROWID+"="+dbRowId, 
					null, null, null, null, null);			
			cursor.moveToFirst();
			if (cursor !=null) {
				hlocation.setId(cursor.getLong(0));		
				long holrefid=cursor.getLong(1);
				hlocation.setRefid(holrefid);
				hlocation.setName(cursor.getString(2));
				hlocation.setLdate(cursor.getString(3));
				hlocation.setAddress(cursor.getString(4));
				hlocation.setStreet(cursor.getString(5));
				hlocation.setCity(cursor.getString(6));
				hlocation.setPostal(cursor.getString(7));
				hlocation.setCountry(cursor.getString(8));			
				hlocation.setNotes(cursor.getString(9));
				hlocation.setLatitude(cursor.getDouble(10));
				hlocation.setLongitude(cursor.getDouble(11));
				hlocation.setAddressObj(cursor.getBlob(12));  	
				hlocation.setInfo(cursor.getString(13));
		    	hlocation.setPicture(cursor.getString(14));
		    	hlocation.setHoliday(cursor.getString(15));	
	 		} cursor.close();
			return hlocation;
		}		
	}
	

	
	
	//--Get All Locations--retrieves all the locations records--------------------------------------------------
	public ArrayList<HLocation> getAllLocations() {
		try {
			currentLocationList=new ArrayList<HLocation>();
			currentLocationList=new retrieveAllLocation().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return currentLocationList;	
	}
	
	//--need to exclude blob from retrieve all, db too big
	private class retrieveAllLocation extends AsyncTask<Void, Void, ArrayList<HLocation>>{
		
		@Override
		protected ArrayList<HLocation> doInBackground(Void... params) {
			currentLocationList=new ArrayList<HLocation>();			
			Cursor cursor=db.query(DATABASE_LOCATION_TABLE, new String[] {
					LOC_ROWID,
					LOC_REFID,
					LOC_NAME,
					LOC_DATE,
					LOC_ADDRESS,
					LOC_STREET,
					LOC_CITY,
					LOC_POSTALCODE,
					LOC_COUNTRY,
					LOC_NOTES,
					LOC_LATITUDE,
					LOC_LONGITUDE,
					LOC_INFO,
					LOC_PICTURE,
					LOC_HOLIDAY},
					null, null, null, null, LOC_DATE+" DESC");
			cursor.moveToFirst();			//LOC_OBJECT (sits before LOC_INFO) excluded
			if (! cursor.isAfterLast()) {
				do {
					HLocation loc=new HLocation();  //must be inside do while else all item in arraylist is the same
					loc.setId(cursor.getInt(0));
					loc.setRefid(cursor.getInt(1));
					loc.setName(cursor.getString(2));
					loc.setLdate(cursor.getString(3));
					loc.setAddress(cursor.getString(4));
					loc.setStreet(cursor.getString(5));
					loc.setCity(cursor.getString(6));
					loc.setPostal(cursor.getString(7));
					loc.setCountry(cursor.getString(8));
					loc.setNotes(cursor.getString(9));
					loc.setLatitude(cursor.getDouble(10));
					loc.setLongitude(cursor.getDouble(11));
					//loc.setAddressObj(cursor.getBlob(12));	//shift all cursor 1 up
					loc.setInfo(cursor.getString(12));				//13
					loc.setPicture(cursor.getString(13));			//14
					loc.setHoliday(cursor.getString(14));			//15
					currentLocationList.add(loc);
				} while (cursor.moveToNext());
			}
			cursor.close();
			return currentLocationList;
		}

	}		//END retrieveAllLocation
	

	//--Get Last Location------------------------------------------------------------------------------------------------
	public HLocation getLastLocation() {
		HLocation hLocation=new HLocation();
		try {
			hLocation=new retreiveLastLocation().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return hLocation;		
	}
	
	private class retreiveLastLocation extends AsyncTask<Void, Void, HLocation> {

		@Override
		protected HLocation doInBackground(Void... params) {
			hlocation=new HLocation();			
			Cursor cursor=db.query(DATABASE_LOCATION_TABLE, new String[] {
					LOC_ROWID,
					LOC_REFID,
					LOC_NAME,
					LOC_DATE,
					LOC_ADDRESS,
					LOC_STREET,
					LOC_CITY,
					LOC_POSTALCODE,
					LOC_COUNTRY,
					LOC_NOTES,
					LOC_LATITUDE,
					LOC_LONGITUDE,				
					LOC_INFO,
					LOC_PICTURE,
					LOC_HOLIDAY},
					null, null, null, null, null);
			cursor.moveToLast();				//LOC_OBJECT, before LOC_INFO
			if (cursor !=null) {
				hlocation.setId(cursor.getLong(0));		
				long holrefid=cursor.getLong(1);
				hlocation.setRefid(holrefid);
				hlocation.setName(cursor.getString(2));
				hlocation.setLdate(cursor.getString(3));
				hlocation.setAddress(cursor.getString(4));
				hlocation.setStreet(cursor.getString(5));
				hlocation.setCity(cursor.getString(6));
				hlocation.setPostal(cursor.getString(7));
				hlocation.setCountry(cursor.getString(8));
				hlocation.setNotes(cursor.getString(9));
				hlocation.setLatitude(cursor.getDouble(10));
				hlocation.setLongitude(cursor.getDouble(11));
				//hlocation.setAddressObj(cursor.getBlob(12));		//everyone below shift 1 up
				hlocation.setInfo(cursor.getString(12));
		    	hlocation.setPicture(cursor.getString(13));
		    	hlocation.setHoliday(cursor.getString(14));	
	 		} 
			cursor.close();		
			return hlocation;
		}		
	}
	
	

	
	//--Test Empty DB----------------------------------------------------------------------------------------------------
	public boolean isLocationDBEmpty() {
		boolean dbEmpty=false;
		try {
			dbEmpty=new checkDBEmpty().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return dbEmpty;
	}
	
	private class checkDBEmpty extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			Cursor cursor=db.query(DATABASE_LOCATION_TABLE, new String[] {
					LOC_ROWID,
					LOC_REFID,
					LOC_NAME,
					LOC_DATE,
					LOC_ADDRESS,
					LOC_STREET,
					LOC_CITY,
					LOC_POSTALCODE,
					LOC_COUNTRY,
					LOC_NOTES,
					LOC_LATITUDE,
					LOC_LONGITUDE,
					LOC_INFO,
					LOC_PICTURE,
					LOC_HOLIDAY},
					null, null, null, null, null);
			//cursor.moveToFirst();			//v1.01  LOC_OBJECT (sits before LOC_INFO) excluded
			if(cursor !=null && cursor.getCount()>0) {
				cursor.close();
				return false;
			} else {
				cursor.close();
				return true;
			}
		}

	}
	
	//--Get Holiday Locations---------------------------------------------------------------------------------------------
	public ArrayList<HLocation> getHolidayLocations(long holrowid) {
		try {
			currentLocationList=new ArrayList<HLocation>();
			currentLocationList=new retrieveHolidayLocations().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, holrowid).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return currentLocationList;
	}
	
	//--Get Holiday Locations---------------------------------------------------------------------------------------------
	public boolean hasHolidayLocations(long holrowid) {
		try {
			currentLocationList=new ArrayList<HLocation>();
			currentLocationList=new retrieveHolidayLocations().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, holrowid).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		if (currentLocationList.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}
	
	private class retrieveHolidayLocations extends AsyncTask<Long, Void, ArrayList<HLocation>> {

		@Override
		protected ArrayList<HLocation> doInBackground(Long... params) {
			long holrowid=params[0];
			ArrayList<HLocation> locationList=new ArrayList<HLocation>();

			Cursor cursor=db.query(true,DATABASE_LOCATION_TABLE, new String[] {
					LOC_ROWID,
					LOC_REFID,
					LOC_NAME,
					LOC_DATE,
					LOC_ADDRESS,
					LOC_STREET,
					LOC_CITY,
					LOC_POSTALCODE,
					LOC_COUNTRY,
					LOC_NOTES,
					LOC_LATITUDE,
					LOC_LONGITUDE,
					LOC_INFO,
					LOC_PICTURE,
					LOC_HOLIDAY},
					LOC_REFID+"="+holrowid, null, null, null, null, null);
			cursor.moveToFirst();								//LOC_OBJECT (sits before LOC_INFO) excluded
			if (! cursor.isAfterLast()) {
				do {
					HLocation loc=new HLocation();  //must be inside do while else all item in arraylist is the same
					loc.setId(cursor.getInt(0));
					loc.setRefid(cursor.getInt(1));
					loc.setName(cursor.getString(2));
					loc.setLdate(cursor.getString(3));
					loc.setAddress(cursor.getString(4));
					loc.setStreet(cursor.getString(5));
					loc.setCity(cursor.getString(6));
					loc.setPostal(cursor.getString(7));
					loc.setCountry(cursor.getString(8));
					loc.setNotes(cursor.getString(9));
					loc.setLatitude(cursor.getDouble(10));
					loc.setLongitude(cursor.getDouble(11));
					//loc.setAddressObj(cursor.getBlob(12));		//cursor below shift up by 1
					loc.setInfo(cursor.getString(12));
					loc.setPicture(cursor.getString(13));
					loc.setHoliday(cursor.getString(14));				
					locationList.add(loc);
				} while (cursor.moveToNext());
			}
			cursor.close();
			return locationList;
		}		
	}
	

		
	
	//=====UPDATE RECORD======================LOCATION==================

	//--UPDATE Location --
	public boolean updateLocation (HLocation hlocation) {
		boolean updated=false;
		try {
			updated = new updateLocationChange().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, hlocation).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return updated;
	}
	
	private class updateLocationChange extends AsyncTask<HLocation, Void, Boolean> {

		@Override
		protected Boolean doInBackground(HLocation... params) {
			hlocation=new HLocation();
			hlocation=params[0];
			long rowId=hlocation.getId();
			ContentValues values=new ContentValues();
			values.put(LOC_REFID, hlocation.getRefid());
			values.put(LOC_NAME, hlocation.getName());
			values.put(LOC_DATE, hlocation.getLdate());
			values.put(LOC_ADDRESS,hlocation.getAddress());
			values.put(LOC_STREET,hlocation.getStreet());
			values.put(LOC_CITY,hlocation.getCity());
			values.put(LOC_POSTALCODE,hlocation.getPostal());
			values.put(LOC_COUNTRY,hlocation.getCountry());		
			values.put(LOC_NOTES,hlocation.getNotes());
			values.put(LOC_LATITUDE,hlocation.getLatitude());
			values.put(LOC_LONGITUDE,hlocation.getLongitude());
			values.put(LOC_OBJECT, hlocation.getAddressObj());
			values.put(LOC_INFO,hlocation.getInfo());	
			boolean boolPic=hlocation.getPicture();
			values.put(LOC_PICTURE, Boolean.toString(boolPic));
			boolean boolHol=hlocation.getHoliday();
			values.put(LOC_HOLIDAY, Boolean.toString(boolHol));		
			return db.update(DATABASE_LOCATION_TABLE, values, LOC_ROWID+"="+rowId, null)>0;	
		}
	}
	
	//--update location hasPicture status-----------------------------------------------------
	public boolean updateLocationHasPicture (long rowId, boolean hasPicture) {		
			ContentValues values=new ContentValues();
			boolean boolPic=hasPicture;
			values.put(LOC_PICTURE, Boolean.toString(boolPic));
			/*values.put(LOC_ROWID, rowId);
			
			boolean updateLoc=false;			
			try {
				updateLoc = new updateLocationPictureState().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, values).get();
				//updateLoc = new updateLocationPictureState().execute(values).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			return updateLoc;*/
			return db.update(DATABASE_LOCATION_TABLE, values, LOC_ROWID+"="+rowId, null)>0;		
	}	
	private class updateLocationPictureState extends AsyncTask<ContentValues, Void, Boolean> {

		@Override
		protected Boolean doInBackground(ContentValues... params) {
			ContentValues newValues=new ContentValues();
			newValues=params[0];
			long dbRowId=values.getAsLong(LOC_ROWID);
			newValues.remove(LOC_ROWID);
			return db.update(DATABASE_LOCATION_TABLE, newValues, LOC_ROWID+"="+dbRowId, null)>0;	
		}		
	}
	
	//--update location hasHoliday status-----------------------------------------------------
	public boolean updateLocationHasHoliday (long rowId, boolean hasHoliday) {		
		ContentValues values=new ContentValues();
		boolean boolHol=hasHoliday;
		values.put(LOC_HOLIDAY, Boolean.toString(boolHol));
		/*values.put(LOC_ROWID, rowId);
		
		boolean updateLoc=false;			
		try {
			updateLoc = new updateLocationHolidayState().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, values).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return updateLoc;*/
		
		return db.update(DATABASE_LOCATION_TABLE, values, LOC_ROWID+"="+rowId, null)>0;		
	}
	private class updateLocationHolidayState extends AsyncTask<ContentValues, Void, Boolean> {

		@Override
		protected Boolean doInBackground(ContentValues... params) {
			ContentValues newValues=new ContentValues();
			newValues=params[0];
			long dbRowId=values.getAsLong(LOC_ROWID);
			newValues.remove(LOC_ROWID);
			return db.update(DATABASE_LOCATION_TABLE, newValues, LOC_ROWID+"="+dbRowId, null)>0;	
		}		
	}
	

	
	//=====DELETE RECORD======================LOCATION===================
	//--delete a location record--
	public boolean deleteLocation(long rowid) {
		boolean deleted=false;
		try {
			deleted=new removeLocation().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,rowid).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return deleted;
	}
	
	private class removeLocation extends AsyncTask<Long, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Long... params) {
			long rowId=params[0];
			return db.delete(DATABASE_LOCATION_TABLE, LOC_ROWID+"="+rowId, null)>0;
		}
		
	}
	
	/* ================================================================================
	 * End of LOCATION TABLE methods
	 * ================================================================================
	 */
	

	/* ================================================================================
	 * START OF HOLIDAY TABLE CRUD methods
	 * ================================================================================
	 */

	//=====CREATE RECORD======================HOLIDAY===================
	
	public long insertHoliday(Holiday holiday) {
		long dbrowid=0;
		try {
			dbrowid=new addHoliday().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, holiday).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return dbrowid;
	}
	
	private  class addHoliday extends AsyncTask<Holiday, Void, Long> {

		@Override
		protected Long doInBackground(Holiday... params) {
			holiday=new Holiday();
			holiday=params[0];
			ContentValues values=new ContentValues();
			values.put(HOL_REFID, holiday.getRefid());
			values.put(HOL_HOLIDAY, holiday.getHoliday());
			values.put(HOL_COUNTRY,holiday.getCountry());
			values.put(HOL_CITY,holiday.getCity());
			values.put(HOL_STARTDATE,holiday.getStart_date());
			values.put(HOL_ENDDATE,holiday.getEnd_date());
			values.put(HOL_NOTES, holiday.getNotes());
			return db.insert(DATABASE_HOLIDAY_TABLE, null, values);		
		}		
	}
	
	
	//=====READ RECORD======================HOLIDAY===================
	//db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
	//--retrieves a particular holiday record--
	//db.query(boolean, table, columns, selection, 
	//			selectionArgs, groupBy, having, orderBy, limit)
	public Holiday getHoliday(long rowid) {
		Holiday thisHoliday=new Holiday();
		try {
			thisHoliday=new retreiveHoliday().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,rowid).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return thisHoliday;
	}
	
	private class retreiveHoliday extends AsyncTask<Long, Void, Holiday> {

		@Override
		protected Holiday doInBackground(Long... params) {
			Log.d(TAG, "landed in doInBackground retreiveHoliday called from getHoliday");
			long rowid=params[0];
			holiday=new Holiday();
			Cursor cursor=
					db.query(true,DATABASE_HOLIDAY_TABLE, new String[] {
					HOL_ROWID,
					HOL_REFID,
					HOL_HOLIDAY,
					HOL_COUNTRY,
					HOL_CITY,
					HOL_STARTDATE,
					HOL_ENDDATE,
					HOL_NOTES
					},
					HOL_ROWID+"="+rowid, 
					null, null, null, null,null);
			cursor.moveToFirst();
			if (cursor !=null) {
				holiday.setId(cursor.getLong(0));
				holiday.setRefid(cursor.getLong(1));
				holiday.setHoliday(cursor.getString(2));
				holiday.setCountry(cursor.getString(3));
				holiday.setCity(cursor.getString(4));			
				holiday.setStart_date(cursor.getString(5));
				holiday.setEnd_date(cursor.getString(6));
				holiday.setNotes(cursor.getString(7));
			}
			cursor.close();
			return holiday;
		}		
	}
	
	
	//--retrieves all the holidays records-
	// db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
	public ArrayList<Holiday> getAllHolidays() {
		
		currentHolidayList=new ArrayList<Holiday>();
		try {
			currentHolidayList=new retreiveAllHolidays().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return currentHolidayList;
	}
	
	private class retreiveAllHolidays extends AsyncTask<Void, Void, ArrayList<Holiday>> {

		@Override
		protected ArrayList<Holiday> doInBackground(Void... params) {
			ArrayList<Holiday>holidayList= new ArrayList<Holiday>();

			Cursor cursor=db.query(DATABASE_HOLIDAY_TABLE, new String[] {
					HOL_ROWID,
					HOL_REFID,
					HOL_HOLIDAY,
					HOL_COUNTRY,
					HOL_CITY,
					HOL_STARTDATE,
					HOL_ENDDATE,
					HOL_NOTES
			//},  null, null, null, null, null);
			},  null, null, null, null, HOL_ROWID+" desc");
			cursor.moveToFirst();
			if (!cursor.isAfterLast()) {
				do {
					Holiday holiday = new Holiday();
					holiday.setId(cursor.getLong(0));
					holiday.setHolId(cursor.getInt(0));
					holiday.setRefid(cursor.getLong(1));
					holiday.setHoliday(cursor.getString(2));
					holiday.setCountry(cursor.getString(3));
					holiday.setCity(cursor.getString(4));
					holiday.setStart_date(cursor.getString(5));
					holiday.setEnd_date(cursor.getString(6));
					holiday.setNotes(cursor.getString(7));
					holidayList.add(holiday);			
				}while (cursor.moveToNext()); 
			}
			cursor.close();
			return holidayList;
		}		
	}
	

	
	//--test for empty table--------------------------------------------------------------
	public boolean isHolidayDBEmpty() {
		boolean checkEmpty=false;
		try {
			checkEmpty=new checkHolidayDBEmpty().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return checkEmpty;
	}
	
	private class checkHolidayDBEmpty extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean isEmpty;
			Cursor cursor=db.query(DATABASE_HOLIDAY_TABLE, new String[] {
					HOL_ROWID,
					HOL_REFID,
					HOL_HOLIDAY,
					HOL_COUNTRY,
					HOL_CITY,
					HOL_STARTDATE,
					HOL_ENDDATE,
					HOL_NOTES
			},  null, null, null, null, null);
			if (cursor !=null && cursor.getCount()>0 ) {
				isEmpty=false;
			} else {
				isEmpty=true;
			}
			cursor.close();
			return isEmpty;
		}		
	}
	

	//=====UPDATE RECORD======================HOLIDAY===================
	//-- update existing Holiday
	//--added HOL_REFID, holiday.getRefid(), somehow missing June 22
	public boolean updateHoliday(Holiday holiday) {
		boolean updated=false;
		try {
			updated=new updateHolidayChange().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,holiday).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return updated;		
	}
	
	private class updateHolidayChange extends AsyncTask<Holiday, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Holiday... params) {
			holiday=new Holiday();
			holiday=params[0];
			long rowId=holiday.getId();
			ContentValues values=new ContentValues();
			values.put(HOL_REFID, holiday.getRefid());
			values.put(HOL_HOLIDAY, holiday.getHoliday());
			values.put(HOL_COUNTRY,holiday.getCountry());
			values.put(HOL_CITY,holiday.getCity());
			values.put(HOL_STARTDATE,holiday.getStart_date());
			values.put(HOL_ENDDATE,holiday.getEnd_date());
			values.put(HOL_NOTES, holiday.getNotes());
			return db.update(DATABASE_HOLIDAY_TABLE, values, HOL_ROWID+"="+rowId, null)>0;		
		}
	}
	

	//=====DELETE RECORD======================HOLIDAY===================	
	//--delete a holiday record--
	// db.delete(table, whereClause, whereArgs)
	
	public boolean deleteHoliday(long rowId) {
		boolean removed=false;
		try {
			removed=new removeHoliday().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,rowId).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return removed;
	}
	
	private class removeHoliday extends AsyncTask<Long, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Long... params) {
			long rowId=params[0];
			return db.delete(DATABASE_HOLIDAY_TABLE, HOL_ROWID+"="+rowId, null)>0;
		}
		
	}
	
	
	/* ================================================================================
	 * End of HOLIDAY TABLE methods
	 * ================================================================================
	 */

	/* ================================================================================
	 * START OF SHOPPING TABLE methods
	 * ================================================================================
	 */
	
	//=====CREATE NEW RECORD==================SHOPPING===================
	public long insertItem(Shopping bbb) {
		long rowid=0;
		try {
			rowid=new addItem().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,bbb).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return rowid;
	}
	
	private class addItem extends AsyncTask<Shopping, Void, Long> {

		@Override
		protected Long doInBackground(Shopping... params) {
			Shopping bbb=new Shopping();
			bbb=params[0];
			ContentValues values=new ContentValues();
			values.put(ITEM_REFID, bbb.getHolId());
			values.put(ITEM_NAME, bbb.getName());
			boolean booVal=bbb.getComplete();
			values.put(ITEM_COMPLETE,Boolean.toString(booVal));
			values.put(ITEM_PRICE,bbb.getPrice());
			values.put(ITEM_ADDRESS,bbb.getAddress());
			values.put(ITEM_LATITUDE,bbb.getLatitude());
			values.put(ITEM_LONGITUDE,bbb.getLongitude());
			values.put(ITEM_CAT, bbb.getCat());		
			values.put(ITEM_NOTES, bbb.getNotes());
			return db.insert(DATABASE_SHOPPING_TABLE, null, values);	
		}		
	}
	

	//=====UPDATE RECORD======================SHOPPING===================
	
	//--update item via shopping instance--
	public boolean updateItem(Shopping bbb) {
		boolean updated=false;
		try {
			updated=new updateItemChange().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,bbb).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return updated;
	}
	
	private class updateItemChange extends AsyncTask<Shopping, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Shopping... params) {
			Shopping bbb=new Shopping();
			bbb=params[0];
			ContentValues values=new ContentValues();
			values.put(ITEM_ROWID, bbb.getId());
			values.put(ITEM_REFID, bbb.getHolId());
			values.put(ITEM_NAME, bbb.getName());
			boolean booVal=bbb.getComplete();
			values.put(ITEM_COMPLETE,Boolean.toString(booVal));
			values.put(ITEM_PRICE, bbb.getPrice());
			values.put(ITEM_ADDRESS, bbb.getAddress());
			values.put(ITEM_LATITUDE, bbb.getLatitude());
			values.put(ITEM_LONGITUDE, bbb.getLongitude());
			values.put(ITEM_CAT, bbb.getCat());		
			values.put(ITEM_NOTES, bbb.getNotes());
			long id=bbb.getId();
			return db.update(DATABASE_SHOPPING_TABLE, values, ITEM_ROWID+"="+id, null)>0;			
		}
		
	}
	

	//=====READ SINGLE RECORD==================SHOPPING===================
	public Shopping getItem(long rowId) {
		shopping=new Shopping();
		try {
			shopping = new retreiveItem().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,rowId).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return shopping;
		
	}
	
	private class retreiveItem extends AsyncTask<Long, Void, Shopping> {

		@Override
		protected Shopping doInBackground(Long... params) {
			long rowId=params[0];
			Shopping si=new Shopping();
			Cursor cursor=db.query(true, DATABASE_SHOPPING_TABLE, new String[] {
					ITEM_ROWID,
					ITEM_REFID,
					ITEM_NAME,
					ITEM_COMPLETE,
					ITEM_PRICE,
					ITEM_ADDRESS,
					ITEM_LATITUDE,
					ITEM_LONGITUDE,
					ITEM_CAT,
					ITEM_NOTES},
					ITEM_ROWID+"="+rowId, 
					null, null, null, null, null);
			cursor.moveToFirst();
			if (cursor !=null) {
				si.setId(cursor.getLong(0));
				si.setRefid(cursor.getLong(1));
				si.setHolId(cursor.getInt(1));
				si.setName(cursor.getString(2));
				String boolValue=cursor.getString(3);
				si.setComplete(Boolean.parseBoolean(boolValue));
				si.setPrice(cursor.getString(4));
				si.setAddress(cursor.getString(5));
				si.setLatitude(cursor.getDouble(6));
				si.setLongitude(cursor.getDouble(7));
				si.setCat(cursor.getString(8));
				si.setNotes(cursor.getString(9)); }
			cursor.close();
			return si;
		}
	}
	
	
	//=====READ ALL BY HOLIDAY REFID ==================SHOPPING===================
	public ArrayList<Shopping> getByRefId (long refId) {
		currentShoppingList=new ArrayList<Shopping>();
		try {
			currentShoppingList = new retreiveByHoliday().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,refId).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return currentShoppingList;		
	}
	
	private class retreiveByHoliday extends AsyncTask<Long, Void, ArrayList<Shopping>> {

		@Override
		protected ArrayList<Shopping> doInBackground(Long... params) {
			long refId=params[0];
			ArrayList<Shopping> shopList = new ArrayList<Shopping>();
			Shopping si;
			Cursor cursor=db.query(true, DATABASE_SHOPPING_TABLE, new String[] {
					ITEM_ROWID,
					ITEM_REFID,
					ITEM_NAME,
					ITEM_COMPLETE,
					ITEM_PRICE,
					ITEM_ADDRESS,
					ITEM_LATITUDE,
					ITEM_LONGITUDE,
					ITEM_CAT,
					ITEM_NOTES},
					ITEM_REFID+"="+refId,  
					null, null, null, null, null);
			cursor.moveToFirst();
			if (! cursor.isAfterLast()) {
				do {
					si=new Shopping();
					si.setId(cursor.getInt(0));
					si.setRefid(cursor.getLong(1));
					si.setHolId(cursor.getInt(1));
					si.setName(cursor.getString(2));
					String boolValue=cursor.getString(3);
					si.setComplete(Boolean.parseBoolean(boolValue));
					si.setPrice(cursor.getString(4));
					si.setAddress(cursor.getString(5));
					si.setLatitude(cursor.getDouble(6));
					si.setLongitude(cursor.getDouble(7));
					si.setCat(cursor.getString(8)); 
					si.setNotes(cursor.getString(9)); 
					shopList.add(si);		
				} while (cursor.moveToNext());
			}
				cursor.close();
				return shopList;		
		}		
	}
	

	//=====READ ALL RECORDS===================SHOPPING===================

	public ArrayList<Shopping> getAllItems() {
		ArrayList<Shopping> shoppingList=new ArrayList<Shopping>();		
		try {
			shoppingList=new retreiveAllItems().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return shoppingList;		
	}
	
	private class retreiveAllItems extends AsyncTask<Void, Void, ArrayList<Shopping>> {

		@Override
		protected ArrayList<Shopping> doInBackground(Void... params) {
			currentShoppingList= new ArrayList<Shopping>();
			Cursor cursor=db.query(DATABASE_SHOPPING_TABLE, new String[] {
					ITEM_ROWID,
					ITEM_REFID,
					ITEM_NAME,
					ITEM_COMPLETE,
					ITEM_PRICE,
					ITEM_ADDRESS,
					ITEM_LATITUDE,
					ITEM_LONGITUDE,
					ITEM_CAT,
					ITEM_NOTES,
			},  null, null, null, null, null);
			cursor.moveToFirst();
			if (! cursor.isAfterLast()) {
				do {
					Shopping si=new Shopping();  //must be inside do while else all item in arraylist is the same
					int id=cursor.getInt(0);
					//long refid=cursor.getLong(1);
					int holid=cursor.getInt(1);
					String name=cursor.getString(2);
					String boolValue=cursor.getString(3);
					boolean complete=Boolean.parseBoolean(boolValue);
					String price=cursor.getString(4);
					String address=cursor.getString(5);
					double latitude=cursor.getDouble(6);
					double longitude=cursor.getDouble(7);
					String cat=cursor.getString(8);
					String notes=cursor.getString(9);
					si.setId(id);
					//si.setRefid(refid);
					si.setHolId(holid);
					si.setName(name);		
					si.setComplete(complete);
					si.setPrice(price);
					si.setAddress(address);
					si.setLatitude(latitude);
					si.setLongitude(longitude);
					si.setCat(cat);
					si.setNotes(notes);
					currentShoppingList.add(si);		
				} while (cursor.moveToNext());
			}
			cursor.close();
			return currentShoppingList;
		}		
	}

	//=====DELETE RECORD======================SHOPPING===================
	public boolean deleteItem(long rowId) {
		boolean deleted=false;
		try {
			deleted = new removeItem().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,rowId).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return deleted;
	}
	
	private class removeItem extends AsyncTask<Long, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Long... params) {
			long rowId=params[0];
			return db.delete(DATABASE_SHOPPING_TABLE, ITEM_ROWID+"="+rowId, null)>0;
		}		
	}
	

	//--DELETE BATCH selected items from ArrayList<Long>--
	public void removeItems(ArrayList<Long> selection) {
		for (long id : selection) {
			deleteItem(id);
		}
	}
	
	//--DELETE BATCH selected items from ArrayList<Shopping>--
	public void deleteSelectedItems(ArrayList<Shopping> bbList) {
		for (Shopping bb : bbList) {
			deleteItem(bb.getId());
		}
	}
	
	/* ================================================================================
	 * END OF SHOPPING CRUD METHODS
	 * ================================================================================
	 */	
	/* ================================================================================
	 * START OF CHECKLIST TABLE CRUD methods
	 * ================================================================================
	 */
	
	//=====CREATE NEW RECORD==================CHECKLIST===================
	public long insertTask(TaskList bbb) {
		long added=0;
		try {
			added = new addTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,bbb).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return added;
	}
	
	private class addTask extends AsyncTask<TaskList, Void, Long> {

		@Override
		protected Long doInBackground(TaskList... params) {
			TaskList bbb = new TaskList();
			bbb=params[0];
			ContentValues values=new ContentValues();
			values.put(ITEM_REFID, bbb.getRefid());
			values.put(ITEM_NAME, bbb.getName());
			boolean booVal=bbb.getComplete();
			values.put(ITEM_COMPLETE,Boolean.toString(booVal));
			values.put(ITEM_ADDRESS,bbb.getAddress());
			values.put(ITEM_LATITUDE,bbb.getLatitude());
			values.put(ITEM_LONGITUDE,bbb.getLongitude());
			values.put(ITEM_CAT,bbb.getCat());
			values.put(ITEM_NOTES, bbb.getNotes());
			return db.insert(DATABASE_CHECKLIST_TABLE, null, values);		
		}		
	}
	

	//=====CREATE LIST OF NEW RECORDS============CHECKLIST===================
	public ArrayList<Integer> insertTaskList(ArrayList<TaskList> newTaskList) {
			ArrayList<Integer> insertedList=new ArrayList<Integer>();
			try {
				insertedList = new addTaskList().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,newTaskList).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			return insertedList;
	}
	
	private class addTaskList extends AsyncTask<ArrayList<TaskList>, Void, ArrayList<Integer>> {

		@Override
		protected ArrayList<Integer> doInBackground(
				ArrayList<TaskList>... params) {
			ArrayList<Integer> newList=new ArrayList<Integer>();
			ArrayList<TaskList> checkList=new ArrayList<TaskList>();
			checkList=params[0];
			for (TaskList task : checkList) {
				int added=(int)insertTask(task);
				newList.add(added);
			}
			return newList;
		}		
	}
	
	
	//=====UPDATE RECORD=====================CHECKLIST===================
	
	//--update item via shopping instance--
	public boolean updateTask(TaskList task) {
		boolean updated=false;
		try {
			updated =  new updateTaskChange().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,task).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return updated;		
	}
	
	private class updateTaskChange extends AsyncTask<TaskList, Void, Boolean> {

		@Override
		protected Boolean doInBackground(TaskList... params) {
			task=new TaskList();
			task=params[0];
			ContentValues values=new ContentValues();
			values.put(ITEM_ROWID, task.getId());
			values.put(ITEM_REFID, task.getRefid());
			values.put(ITEM_NAME, task.getName());
			boolean booVal=task.getComplete();
			values.put(ITEM_COMPLETE,Boolean.toString(booVal));
			values.put(ITEM_ADDRESS, task.getAddress());
			values.put(ITEM_LATITUDE, task.getLatitude());
			values.put(ITEM_LONGITUDE, task.getLongitude());
			values.put(ITEM_CAT,  task.getCat());
			values.put(ITEM_NOTES, task.getNotes());
			long id=task.getId();
			return db.update(DATABASE_CHECKLIST_TABLE, values, ITEM_ROWID+"="+id, null)>0;		
		}		
	}
	
	
	//=====READ SINGLE RECORD=================CHECKLIST===================
	public TaskList getTask(long rowId)  {
		task=new TaskList();
		try {
			task = new retreiveTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,rowId).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return task;
	}
	
	private class retreiveTask extends AsyncTask<Long, Void, TaskList> {

		@Override
		protected TaskList doInBackground(Long... params) {
			long rowId=params[0];
			TaskList si=new TaskList();
			Cursor cursor=db.query(true, DATABASE_CHECKLIST_TABLE, new String[] {
					ITEM_ROWID,
					ITEM_REFID,
					ITEM_NAME,
					ITEM_COMPLETE,
					ITEM_ADDRESS,
					ITEM_LATITUDE,
					ITEM_LONGITUDE,
					ITEM_CAT,
					ITEM_NOTES},
					ITEM_ROWID+"="+rowId, 
					null, null, null, null, null);
			cursor.moveToFirst();
			if (cursor !=null) {
				si.setId(cursor.getInt(0));
				si.setRefid(cursor.getInt(1));
				si.setName(cursor.getString(2));
				String boolValue=cursor.getString(3);
				si.setComplete(Boolean.parseBoolean(boolValue));
				si.setAddress(cursor.getString(4));
				si.setLatitude(cursor.getDouble(5));
				si.setLongitude(cursor.getDouble(6));
				si.setCat(cursor.getInt(7));
				si.setNotes(cursor.getString(8)); }
			cursor.close();
			return si;
		}
	}

	
	
	//--check whether db is empty-----------------------------
	public boolean isTaskDBEmpty() {
		boolean isEmpty=false;
		try {
			isEmpty = new checkTaskDBEmpty().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return isEmpty;
	}
	
	private class checkTaskDBEmpty extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			
			Cursor cursor=db.query(DATABASE_CHECKLIST_TABLE, new String[] {
					ITEM_ROWID,
					ITEM_REFID,
					ITEM_NAME,
					ITEM_COMPLETE,
					ITEM_ADDRESS,
					ITEM_LATITUDE,
					ITEM_LONGITUDE,
					ITEM_CAT,
					ITEM_NOTES},
					null, null, null, null, null);
			cursor.moveToFirst();
			if(cursor !=null && cursor.getCount()>0) {
				cursor.close();
				return false;
			} else {
				cursor.close();
				return true;
			}
		}
	}

	
	
	//=====READ SELECTED CATEGORY REFIDS=========CHECKLIST===================

	public ArrayList<TaskList> getSelectedTasks(long refId) {
		ArrayList<TaskList> currentTaskList=new ArrayList<TaskList>();
		try {
			currentTaskList = new retreiveSelectedTasks().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,refId).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return currentTaskList;
	}
	
	private class retreiveSelectedTasks extends AsyncTask<Long, Void, ArrayList<TaskList>> {

		@Override
		protected ArrayList<TaskList> doInBackground(Long... params) {
			long refId=params[0];
			currentCheckList=new ArrayList<TaskList>();
			
			Cursor cursor=db.query(true, DATABASE_CHECKLIST_TABLE, new String[] {
					ITEM_ROWID,
					ITEM_REFID,
					ITEM_NAME,
					ITEM_COMPLETE,
					ITEM_ADDRESS,
					ITEM_LATITUDE,
					ITEM_LONGITUDE,
					ITEM_CAT,
					ITEM_NOTES},
					ITEM_REFID+"="+refId, 
					null, null, null, null, null);
			cursor.moveToFirst();
			if (!cursor.isAfterLast()) {
				do {
					TaskList si=new TaskList();
					si.setId(cursor.getInt(0));
					si.setRefid(cursor.getInt(1));
					si.setName(cursor.getString(2));
					String boolValue=cursor.getString(3);
					si.setComplete(Boolean.parseBoolean(boolValue));
					si.setAddress(cursor.getString(4));
					si.setLatitude(cursor.getDouble(5));
					si.setLongitude(cursor.getDouble(6));
					si.setCat(cursor.getInt(7));
					si.setNotes(cursor.getString(8)); 
					currentCheckList.add(si);
				}while (cursor.moveToNext()); 
			}
			cursor.close();
			return currentCheckList;
		}		
	}

	
	//=====READ ALL RECORDS==================CHECKLIST===================
	public ArrayList<TaskList> getAllTasks() {
		ArrayList<TaskList> allTasks=new ArrayList<TaskList>();
		try {
			allTasks=new retreiveAllTasks().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return allTasks;				
	}
	
	private class retreiveAllTasks extends AsyncTask<Void, Void, ArrayList<TaskList>> {

		@Override
		protected ArrayList<TaskList> doInBackground(Void... params) {
			currentCheckList= new ArrayList<TaskList>();
			Cursor cursor=db.query(DATABASE_CHECKLIST_TABLE, new String[] {
					ITEM_ROWID,
					ITEM_REFID,
					ITEM_NAME,
					ITEM_COMPLETE,
					ITEM_ADDRESS,
					ITEM_LATITUDE,
					ITEM_LONGITUDE,
					ITEM_CAT,
					ITEM_NOTES,
			},  null, null, null, null, null);
			cursor.moveToFirst();
			if (! cursor.isAfterLast()) {
				do {
					TaskList si=new TaskList();  //must be inside do while else all item in arraylist is the same
					int id=cursor.getInt(0);
					int refid=cursor.getInt(1);
					String name=cursor.getString(2);
					String boolValue=cursor.getString(3);
					boolean complete=Boolean.parseBoolean(boolValue);
					String address=cursor.getString(4);
					double latitude=cursor.getDouble(5);
					double longitude=cursor.getDouble(6);
					int cat=cursor.getInt(7);
					String notes=cursor.getString(8);
					si.setId(id);
					si.setRefid(refid);
					si.setName(name);		
					si.setComplete(complete);
					si.setAddress(address);
					si.setLatitude(latitude);
					si.setLongitude(longitude);
					si.setCat(cat);
					si.setNotes(notes);
					currentCheckList.add(si);		
				} while (cursor.moveToNext());
			}
			cursor.close();
			return currentCheckList;
		}		
	}

	
	//=====DELETE TASK=====================CHECKLIST===================
	public boolean deleteTask(long rowId) {
		boolean deleted=false;
		try {
			deleted=new removeTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,rowId).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return deleted;		
	}
	
	private class removeTask extends AsyncTask<Long, Void, Boolean> {		

		@Override
		protected Boolean doInBackground(Long... params) {
			long rowId=params[0];
			return db.delete(DATABASE_CHECKLIST_TABLE, ITEM_ROWID+"="+rowId, null)>0;
		}		
	}

	//=====DELETE ALL COMPLETED TASK===========CHECKLIST===================
	public boolean removeCompletedTasks() {
		ArrayList<TaskList> allTheTasks = new ArrayList<TaskList>();
		try {
			allTheTasks = new retreiveAllTasks().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR).get();
			for (TaskList t : allTheTasks) {
				if (t.isComplete()) {
					new removeTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,(long)t.getId()).get();
				}
			}
			return true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}	
		return false;
	}
	
	
	//=====DELETE SELECTED TASKS=====================CHECKLIST===================

	public void removeSelectedTasks(ArrayList<Integer> selection) {
		for (int i : selection) {
			boolean deleted=false;
			try {
				deleted=new removeTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,(long)i).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
	
	//--use with care, remove all tasks--------------------------------
	
	public boolean removeAllTasks() {
		boolean deleted=false;
		try {
			deleted=new deleteAllTasks().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return deleted;
	}
	
	private class deleteAllTasks extends AsyncTask <Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			return db.delete(DATABASE_CHECKLIST_TABLE, null, null)>0;
		}
	}
	
	/* ================================================================================
	 * END OF CHECKLIST CRUD METHODS
	 * ================================================================================
	 */		

	/* ================================================================================
	 * START OF PICTURE TABLE CRUD methods
	 * ================================================================================
	 */
			
	//=====CREATE RECORD======================PICTURES===================
			
	//--INSERT new Picture Information into database--
	public long insertPicture(LPicture lpic) {
		long newpic=0;
		try {
			newpic = new addPicture().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,lpic).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return newpic;		
	}
	
	private class addPicture extends AsyncTask<LPicture, Void, Long> {
		
		@Override
		protected Long doInBackground(LPicture... params) {
			LPicture lpic=new LPicture();
			lpic=params[0];
			ContentValues values=new ContentValues();
			values.put(PIC_REFID, lpic.getRefid());
			values.put(PIC_NAME, lpic.getName());
			values.put(PIC_LOCATION, lpic.getLocation());
			values.put(PIC_DATE, lpic.getDate());
			values.put(PIC_ADDRESS, lpic.getAddress());
			values.put(PIC_PATH, lpic.getPicPath());
			boolean isItMark=lpic.getMark();
			values.put(PIC_MARK, Boolean.toString(isItMark));
			values.put(PIC_OBJECT,lpic.getObject());
			values.put(PIC_ORIENT, lpic.getOrient());	
			values.put(PIC_NOTES, lpic.getNotes());
			return db.insert(DATABASE_PICTURE_TABLE, null, values);		
		}
	}
		
	//--BATCH INSERT new Picture Information into database--
	public void addPictureList(ArrayList<LPicture> pictureList) {
		for (LPicture p : pictureList ) {
			new addPicture().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,p);
		}
	}
	
	public boolean insertPictures(ArrayList<LPicture> listOfPictures) {
		for (LPicture lpic : listOfPictures) {
			long addPic=0;
			try {
				addPic=new addPicture().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,lpic).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	public boolean OLDinsertPictures(ArrayList<LPicture> listOfPictures) {
		for (LPicture lpic : listOfPictures) {
			ContentValues values=new ContentValues();
			values.put(PIC_REFID, lpic.getRefid());
			values.put(PIC_NAME, lpic.getName());
			values.put(PIC_LOCATION, lpic.getLocation());
			values.put(PIC_DATE, lpic.getDate());
			values.put(PIC_ADDRESS, lpic.getAddress());
			values.put(PIC_PATH, lpic.getPicPath());
			boolean isItMark=lpic.getMark();
			values.put(PIC_MARK, Boolean.toString(isItMark));
			values.put(PIC_OBJECT,lpic.getObject());
			values.put(PIC_ORIENT, lpic.getOrient());	
			values.put(PIC_NOTES, lpic.getNotes());
			db.insert(DATABASE_PICTURE_TABLE, null, values);	
		}
		return true;
	}

//=====READ RECORD========================PICTURES===================

	//--RETRIEVE a picture Information from database by row id--		
	public LPicture getPicture(long rowid)  {
		LPicture picture = new LPicture();
		try {
			picture=new retreivePicture().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,rowid).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return picture;
	}
			
	private class retreivePicture extends AsyncTask <Long, Void, LPicture> {

		@Override
		protected LPicture doInBackground(Long... params) {
			long rowid=params[0];
			lpic=new LPicture();
			Cursor cursor=
					db.query(true,DATABASE_PICTURE_TABLE, new String[] {
					PIC_REFID,
					PIC_NAME,
					PIC_DATE,
					PIC_LOCATION,
					PIC_ADDRESS,
					PIC_PATH,
					PIC_MARK,
					PIC_OBJECT,
					PIC_ORIENT,
					PIC_NOTES
					},
					PIC_ROWID+"="+rowid, 
					null, null, null, null,null);
			cursor.moveToFirst();

			if (cursor !=null) {
				lpic.setId(cursor.getLong(0));
				lpic.setRefid(cursor.getLong(1));
				lpic.setName(cursor.getString(2));
				lpic.setDate(cursor.getString(3));
				lpic.setLocation(cursor.getString(4));
				lpic.setAddress(cursor.getString(5));
				lpic.setPicPath(cursor.getString(6));
				String boolValue=cursor.getString(7);
				boolean mark=Boolean.parseBoolean(boolValue);
				lpic.setMark(mark);
				lpic.setObject(cursor.getBlob(8));
				lpic.setOrient(cursor.getInt(9));
				lpic.setNotes(cursor.getString(10));
			}
			cursor.close();
			return lpic;
		}
		
	}


	//--RETRIEVE ALL Pictures Information from database for a LOCATION--		
	public ArrayList<LPicture> getAllPictures() {
		ArrayList<LPicture> pictureList = new ArrayList<LPicture>();
		try {
			pictureList=new retreiveAllPictures().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return pictureList;
	}
	
	private class retreiveAllPictures extends AsyncTask<Void, Void, ArrayList<LPicture>> {

		@Override
		protected ArrayList<LPicture> doInBackground(Void... params) {
			ArrayList<LPicture> selectedPictures = new ArrayList<LPicture>();		
			Cursor cursor=
					db.query(DATABASE_PICTURE_TABLE, new String[] {
							PIC_ROWID,
							PIC_REFID,
							PIC_NAME,
							PIC_DATE,
							PIC_LOCATION,
							PIC_ADDRESS,
							PIC_PATH,
							PIC_MARK,
							PIC_OBJECT,
							PIC_ORIENT,
							PIC_NOTES
					}, null, null, null, null, PIC_ROWID+" desc");
			cursor.moveToFirst();
			if (!cursor.isAfterLast()) {
				do {
						LPicture lpic=new LPicture();
						lpic.setId(cursor.getLong(0));
						lpic.setRefid(cursor.getLong(1));
						lpic.setName(cursor.getString(2));
						lpic.setDate(cursor.getString(3));
						lpic.setLocation(cursor.getString(4));
						lpic.setAddress(cursor.getString(5));
						lpic.setPicPath(cursor.getString(6));
						String boolValue=cursor.getString(7);
						boolean mark=Boolean.parseBoolean(boolValue);
						lpic.setMark(mark);
						lpic.setObject(cursor.getBlob(8));
						lpic.setOrient(cursor.getInt(9));
						lpic.setNotes(cursor.getString(10));
						selectedPictures.add(lpic);
					}while (cursor.moveToNext()); 
			}
			cursor.close();
			return selectedPictures;
		}			
	}
	
	//--QUERY LOCATION ID exists-----------------------------------------------
	public boolean queryPictureAvailable(long locId) {
		boolean isAvailable=false;
		//Log.d(TAG, "landed in query, rowid ");
		//Log.d(TAG, "landed in query, rowid "+locId);
		try {
			isAvailable=new checkPictureAvailable().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,locId).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return isAvailable;
	}
	
	private class checkPictureAvailable extends AsyncTask<Long, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Long... params) {
			long locId = params[0];
			/*Cursor cursor=
					db.query(DATABASE_PICTURE_TABLE, null, PIC_REFID+" = "+locId, null, null,  null,  null);*/
			String where=PIC_REFID+"="+locId;

			Cursor cursor= db.query(DATABASE_PICTURE_TABLE, null, where, null, null, null, null);

			//Log.d(TAG, "landed in async check picture available, b4 cursor get countr ");
			//Log.d(TAG, "landed in async check picture available, cursor get countr "+cursor.getCount());
		
			if(cursor == null || cursor.getCount() <1 ) {			//&& cursor.getCount()>0
				cursor.close();
				return false;
			} else {
				cursor.close();
				return true;
			}
		}
	}
	
	//--QUERY PICTURE EXISTS
	public String queryPictureExists(String picpath) {
		String pathToPic="";
		try {
			pathToPic=new checkPictureExisted().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,picpath.toString()).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return pathToPic.toString();
	}
	
	private class checkPictureExisted extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String picpath=params[0].toString();
			String[] columns={
					PIC_ROWID,
					PIC_REFID,
					PIC_NAME,
					PIC_DATE,
					PIC_LOCATION,
					PIC_ADDRESS,
					PIC_PATH,
					PIC_MARK,
					PIC_OBJECT,
					PIC_ORIENT,
					PIC_NOTES};
			String[] parms={picpath.toString()};
			Cursor cursor=
					db.query(DATABASE_PICTURE_TABLE, columns, PIC_PATH+" = "+picpath, parms, null, null, null);
			
			if(cursor != null) {
				cursor.moveToFirst();
				String locName=cursor.getString(4);
				cursor.close();
				return locName.toString();
			} else {
				cursor.close();
				return null;
			}
		}		
	}
	

	
	//--BATCH RETRIEVE Pictures Information from database for a LOCATION--		
	public ArrayList<LPicture> getLocationPictures(Long refId) {
		ArrayList<LPicture> pictureList = new ArrayList<LPicture>();
		try {
			pictureList=new retreiveLocationPicture().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, refId).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return pictureList;
	}
	
	private class retreiveLocationPicture extends AsyncTask <Long, Void, ArrayList<LPicture>>{
		
		@Override
		protected ArrayList<LPicture> doInBackground(Long... params) {
			long refId=params[0];
			ArrayList<LPicture> selectedPictures = new ArrayList<LPicture>();		
			Cursor cursor=
				db.query(true, DATABASE_PICTURE_TABLE, new String[] {
						PIC_ROWID,
						PIC_REFID,
						PIC_NAME,
						PIC_DATE,
						PIC_LOCATION,
						PIC_ADDRESS,
						PIC_PATH,
						PIC_MARK,
						PIC_OBJECT,
						PIC_ORIENT,
						PIC_NOTES
				}, PIC_REFID+"="+refId, null, null, null, null, null);
			cursor.moveToFirst();
			if (!cursor.isAfterLast()) {
				do {
					LPicture lpic=new LPicture();
					lpic.setId(cursor.getLong(0));
					lpic.setRefid(cursor.getLong(1));
					lpic.setName(cursor.getString(2));
					lpic.setDate(cursor.getString(3));
					lpic.setLocation(cursor.getString(4));
					lpic.setAddress(cursor.getString(5));
					lpic.setPicPath(cursor.getString(6));
					String boolValue=cursor.getString(7);
					boolean mark=Boolean.parseBoolean(boolValue);
					lpic.setMark(mark);
					lpic.setObject(cursor.getBlob(8));
					lpic.setOrient(cursor.getInt(9));
					lpic.setNotes(cursor.getString(10));
					selectedPictures.add(lpic);
				}while (cursor.moveToNext()); 
			}
			cursor.close();
			return selectedPictures;
		}
	}			
	
	//--NOT USE--------------------------------------------------
	public Cursor checkHasPicture(long dbRowId) {
		String where=PIC_REFID+"="+dbRowId;
		return db.query(DATABASE_PICTURE_TABLE, null, where, null, null, null, null);
	}
	
	
	//=====UPDATE RECORD======================PICTURES==================
	
	//--UPDATE Picture Information in database--
	public boolean updatePicture(LPicture lpic) {	
		boolean updated=false;
		try {
			updated=new updatePictureChange().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,lpic).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return updated;
	}
	
	private class updatePictureChange extends AsyncTask<LPicture, Void, Boolean>{
		
		@Override
		protected Boolean doInBackground(LPicture... params) {
			LPicture lpic = new LPicture();
			lpic = params[0];
			long rowid=lpic.getId();
			ContentValues values=new ContentValues();
			values.put(PIC_REFID, lpic.getRefid());
			values.put(PIC_NAME, lpic.getName());
			values.put(PIC_DATE, lpic.getDate());
			values.put(PIC_LOCATION, lpic.getLocation());
			values.put(PIC_ADDRESS, lpic.getAddress());
			values.put(PIC_PATH, lpic.getPicPath());
			boolean Marked=lpic.getMark();
			values.put(PIC_MARK, Boolean.toString(Marked));
			values.put(PIC_OBJECT,lpic.getObject());			//may cause problem
			values.put(PIC_ORIENT, lpic.getOrient());
			values.put(PIC_NOTES, lpic.getNotes());
			return db.update(DATABASE_PICTURE_TABLE, values, PIC_ROWID+"="+rowid, null) >0;
		}		
	}	
	
	//--BATCH UPDATE List of pictures Information in database base on ArrayList--
	public void updatePictures(ArrayList<LPicture> pictureList) {
		//for (int i=0; i< pictureList.size(); i++) {
		for (LPicture p : pictureList) {
			try {
				boolean updated = new updatePictureChange().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,p).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			//updatePicture(pictureList.get(i));
		}
	}
	
	//=====DELETE RECORD======================PICTURES==================

	//--DELETE Picture Information from database--		
	public boolean deletePicture(long rowId) {
		boolean deleted=false;
		try {
			deleted=new removePicture().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,rowId).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return deleted;
		
	}
	
	private class removePicture extends AsyncTask <Long, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Long... params) {
			long rowId=params[0];
			return db.delete(DATABASE_PICTURE_TABLE, ITEM_ROWID+"="+rowId, null)>0;
		}
	}

	//--BATCH DELETE Picture Information from database by LOCATION--		
	public void deletePictures(ArrayList<LPicture> pictureList)
	{
		for (LPicture p : pictureList) {
			try {
				long rowId=p.getId();
				boolean deleted=new removePicture().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,rowId).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			//db.delete(DATABASE_PICTURE_TABLE, ITEM_ROWID+"="+p.getId(), null);
		}
	}
	
	//--I think this is redundant because an ArrayList from program can be any base--
	//--BATCH DELETE of pictures Information from database by list of ROWID--		
	public void deletePictureList(ArrayList<Long> selection) {
		for (int i=0; i<selection.size(); i++) {
			long rowid=selection.get(i);
			try {
				boolean deleted=new removePicture().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,rowid).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			//long id=selection.get(i);
			//db.delete(DATABASE_PICTURE_TABLE, ITEM_ROWID+"="+id, null);
		}
	}		
	
	/* ================================================================================
	 * END OF PICTURES CRUD METHODS
	 * ================================================================================
	 */	
				
}		//END MAIN CLASS


