package com.liseem.android.travel;

import static com.liseem.android.travel.items.TravelLiteDBAdapter.LOC_ROWID;

import com.liseem.android.travel.items.HLocation;
import com.liseem.android.travel.items.LPicture;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.liseem.android.travel.PPGoPlacesApplication;

public class TestCamera extends TravelLiteActivity {


	private static final String TAG="TestCamera";
	private final static int 			TAKE_PICTURE=101;
	
    private Button sayCheese, doneButton;
    private Uri imageUri;
	private boolean tookPhoto;
	private LPicture  picture;
	private HLocation hLocation;
	private long dbRowId;
	private PPGoPlacesApplication app;
	private String imagePath;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_camera);
        
        app=(PPGoPlacesApplication)getApplication();
        
        Log.d(TAG, "landed");
      
		Bundle lextras=getIntent().getExtras();
		if (lextras!=null) 
		{
			dbRowId=lextras.getLong(LOC_ROWID);
		}		
		
		Log.d(TAG, "bundled");
		hLocation=new HLocation(null);
		picture=new LPicture();
		Log.d(TAG, "before db  "+dbRowId);

		
		hLocation=app.getLastLocation();
		Log.d(TAG, "after  db");
		Log.d(TAG, "get location"+hLocation.getName());
		
        sayCheese=(Button)findViewById(R.id.sayCheese);
        doneButton=(Button)findViewById(R.id.doneButton);
        
     
        sayCheese.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(TAG,"ofcourse we reach say cheese");
				imageUri=MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				
				Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, TAKE_PICTURE);				
			}
		});
        
        doneButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				makeShortToast("I am done, bye !!");
				finish();
			}
		});
        
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG,"onActivityResult landed");
		if (requestCode==TAKE_PICTURE && resultCode==Activity.RESULT_OK) {
			Log.d(TAG,"onActivityResult after camera action");
			tookPhoto=true;
			
			if(data.getData()!=null) {
				imagePath=getPath(data.getData());
			} else {
				imagePath=getLastImagePath();			//for samsung phone return data null
				//imagePath=getPath(imageUri);				
			}
			Log.d(TAG,"Now the saving part "+imagePath.toString());
				picture.setPicPath(imagePath.toString());
				picture.setAddress(hLocation.getAddress().toString());
				picture.setLatitude(hLocation.getLatitude());
				picture.setLongitude(hLocation.getLongitude());
				picture.setLocation(hLocation.getName());
				picture.setRefid(hLocation.getId());
				app.insertPicture(picture);
				hLocation.setPicture(true);
				app.updateHLocation(hLocation);
				makeShortToast("Nice shot! new picture added to album");
		}
	}
	
}
