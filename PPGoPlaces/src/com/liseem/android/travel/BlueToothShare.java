/*
 * 
 * 
 * <uses-permission android:name="android.permission.BLUETOOTH" />
 */


package com.liseem.android.travel;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class BlueToothShare extends TravelLiteActivity {
	
	private static final String TAG="BlueToothShare";
	private static final int REQUEST_ENABLE_BT = 100;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		setupView();
		
		
	}
	
	private void setupView() {
		
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			Log.d(TAG, "No Bluetooth Available");
			Toast.makeText(getBaseContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
		} else {
			Log.d(TAG, "Hey Bluetooth is Available");
				if (!btAdapter.isEnabled()) {		//if not available, request to enable
				    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				}
			
		}
		
	}
	
	
	
	
	

}
