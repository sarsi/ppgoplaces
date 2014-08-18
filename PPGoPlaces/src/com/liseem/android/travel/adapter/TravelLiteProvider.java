/*
 * need to read BAAD page 250, I think just have to extend 
 * TravelListDBAdapter class with ContentProvider and implement
 * all the abstract methods for URI.
 *  
 *  SO KIV here ...
 */


package com.liseem.android.travel.adapter;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class TravelLiteProvider extends ContentProvider {
	
	
	
	public static final String PROVIDER_NAME=
			"com.liseem.android.travel.adapter.Tables";
	
	public static final Uri CONTENT_URI=
			Uri.parse("content://"+PROVIDER_NAME+"/Tables");
	
	public static final String HOL_ROWID="_id";
	public static final String HOL_REFID="refid";	
	public static final String HOL_HOLIDAY="holiday";
	public static final String HOL_COUNTRY="country";
	public static final String HOL_STARTDATE="start_date";
	public static final String HOL_ENDDATE="end_date";
	public static final String HOL_NOTES="notes";

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public boolean onCreate() {
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
