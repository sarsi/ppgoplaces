package com.liseem.android.travel;

import static com.liseem.android.travel.TravelLiteActivity.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import com.liseem.android.travel.TravelLiteActivity;
import com.liseem.android.travel.items.HLocation;
import com.liseem.android.travel.items.LPicture;
import com.liseem.android.travel.items.MyDate;

import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;

public class CameraActionClick extends Fragment {

	//=====SECTION 1==DECLARATION===================================

	private final static String 			TAG="CameraActionClick";
	private final static int 					TAKE_PICTURE=101;

	private PPGoPlacesApplication 	app;
	private SharedPreferences 			prefs;
	private File 									storageDir;
	private Uri 										imageUri;

	private HLocation 							hLocation;
	private LPicture								oneShot;
	private long 									locRowId;
	private long 									dbRowId;
	private boolean 							tookPhoto=false;
	private ArrayList<String> 			pictureList;
	private String 								imagePath;

	private String 								fileName;
	private Calendar 							pDate;
	private MyDate  							myDate;
	private String									sDate;
	
	//--image capture----------
	private SimpleDateFormat 			timeStampFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
	private Button 								shutterButton;
	
	//--image processing-----
	private ExifInterface 						imageExif;
	
	//--image display--
	private ImageView 						imageView;
	private int 										targetW, targetH;
	private int 										photoW, photoH;
	private int 										scaleFactor;
	private Bitmap bm;

	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	
		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;
	
		//--inflate layout for fragment 1------------------------------------------------
		View v=inflater.inflate(R.layout.image_album, container, false);
		setRetainInstance(true);
		
		return v;			
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.image_album);
		

		setUpView();
		loadInfo();
	}

	/*@Override
	public void onBackPressed() {
		super.onBackPressed();
	}*/
	
	
	/*@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		//--Restore vulnerable information upon onResume--
		tookPhoto=savedInstanceState.getBoolean("takenPicture");
		if(tookPhoto){
			pictureList=savedInstanceState.getStringArrayList("imageArray");		
			imagePath=savedInstanceState.getString("imagePath");
		}		
		super.onRestoreInstanceState(savedInstanceState);
	}*/

	@Override
	public void onSaveInstanceState(Bundle outState) {
		//--Save vulnerable information--
		outState.putBoolean("takenPicture", tookPhoto);
		if(tookPhoto){
			outState.putStringArrayList("imageArray", pictureList);
			outState.putString("imagePath", imagePath);
		}		
		super.onSaveInstanceState(outState);
	}
	
	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 3==SETUP VIEWS====================================
	
	protected void setUpView() {
		
		//--path to main application--------------------------------------------------------
		app						=(PPGoPlacesApplication)getActivity().getApplication();
		prefs					=getActivity().getSharedPreferences (PREFNAME, MODE_PRIVATE);			
				
		hLocation				=new HLocation();
		
		Bundle locInfo=getActivity().getIntent().getExtras();
		if(locInfo != null) {
			dbRowId=locInfo.getLong(LOC_ROWID);
			hLocation=app.getHLocation(dbRowId);
		}
			
		
		//--local key features needed for activity-------camera-----------------------
		//boolean isCamera=isIntentAvailable(this, MediaStore.ACTION_IMAGE_CAPTURE);
	
		//--keep pending in shared preferences for orphan management---------
		/*SharedPreferences.Editor editor=prefs.edit();
		if (isCamera)
			{ editor.putBoolean("camera", true); 
		    editor.commit();}
			
		    Log.d(TAG,"update Shared Preferences");*/

	    //--setup views-------------------------------------------------------------------------
		
		    shutterButton		=(Button)getActivity().findViewById(R.id.capture);
		    shutterButton.setOnClickListener(listener);
		    
		    imageView			=(ImageView)getActivity().findViewById(R.id.image);
		    
		//--get environment setup-------------------------------------------------------------	    
		storageDir=new File(
		Environment.getExternalStorageDirectory(),"test.jpg");
		//------------------------------------------------------------------------------------------
	}
	
	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================

	protected void loadInfo() {
		
		oneShot=new LPicture();
	
		//TRY GO STRAIGHT TO TAKE PICTURE
		Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, TAKE_PICTURE);				
		
	}		//END runIntent()
		
	//=====SECTION 4==ADD ADAPTER===================================

	//=====SECTION 5==SET LISTENER====================================
		

	View.OnClickListener listener=new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
					Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					startActivityForResult(intent, TAKE_PICTURE);										
		}
	};
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode==Activity.RESULT_OK) {
			tookPhoto=true;
			
			if(data!=null) {
				imagePath=((TravelLiteActivity) getActivity()).getPath(data.getData());
				oneShot.setPicPath(imagePath.toString());
			}
			
			savePictures();		
			getActivity().finish();
		}
	}
	
	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================
	


	//=====SECTION 6==LISTENER METHODS================================

	//=====SECTION 7==HELPER METHODS==================================	
		
   private void savePictures() {
	   		hLocation.setPicture(true);
	   		app.updateHLocation(hLocation);
	   
			//--db stagging------------------------------------
			oneShot=app.pictureSetLocation(hLocation);								//set location ref id
			
			//imagePath=getPath(data.getData());
			oneShot.setPicPath(imagePath.toString());					//set picture path
			
			locationProcessing();														//pre saving to exif
			
			//--db insert-----------------------------------------
			long savedImages=app.insertPicture(oneShot);

			getActivity().finish();
			//------------------------------------------------------------------------------------------			
	} 
   
   @TargetApi(5)
	private void locationProcessing() {
		   
		   if (hLocation.getLatitude() !=0 && hLocation.getLongitude() !=0) {
			   Double dLat=hLocation.getLatitude();
			   Double dLong= hLocation.getLongitude();
			   String sLat=dLat.toString();
			   String sLong=dLong.toString();
		   
				try {
					imageExif = new ExifInterface(oneShot.getPicPath().toString());
					//orientInfo=exif.getAttribute(ExifInterface.TAG_ORIENTATION);
					
					float[] gpsInfo=new float[] {0, 0};
					boolean hasGPSInfo=imageExif.getLatLong(gpsInfo);
					
					//Log.d(TAG, "3.4 onActivityResult, TAKE_PICTURE: exif orientation :"+orientInfo.toString());
			
				} catch (IOException e) {
					e.printStackTrace();
				}
				boolean hasThumbnail=imageExif.hasThumbnail();
				Log.d(TAG, "pre save picture, location processing checking for thumbnail, ??save blob "+hasThumbnail);
		   }
	   }
   
	public void oldSetPicture() {
		
		//--Get display size-------------------------------------------------
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		Point size = new Point();
		targetW=display.getWidth();
		targetH=display.getHeight();
		//int width = size.x;
		//int height = size.y;
		
		//--Get dimensions of the view--
		//targetW=imageView.getWidth();
		//targetH=imageView.getHeight();
		
		//--Get dimensions fo the bitmap--
		BitmapFactory.Options bmOptions=new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds=true;
		bmOptions.inSampleSize=5;		//--too big will crash OutOfMemory
		/*photoW=bmOptions.outWidth;
		photoH=bmOptions.outHeight;
		
		//--Determine how much to scale down the image--
		scaleFactor=Math.min(photoW/targetW, photoH/targetH);
		
		//--Decode the image file into a Bitmap sized to fill view--
		bmOptions.inJustDecodeBounds=false;
		bmOptions.inSampleSize=5;			//scaleFactor don't work due to orientation
		bmOptions.inPurgeable=true;*/
		bm=BitmapFactory.decodeFile(oneShot.getPicPath(), bmOptions);

		//--set ImageView-------------------------------------------------
		imageView.setImageBitmap(bm);
	}
	  
	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//=====SECTION 10==SANDBOX======================================
	
	//--moving files to destinated directory from uri before deleting it from default
	
	//-- from onactivityResult, Uri u=intent.getData(); writeFiles(u);
	
	private void WriteFile(Uri newPicture) {
		
		String TempFilePath;
	    String TempPath;
	    ArrayList<String> TempFilePaths=new ArrayList<String>();
	    

	    //--find my picture directory or directory/location--------
	    //File directory = new File("/sdcard/" + getString(R.string.app_name)+ "/" +hLocation.getId());
	    File directory = new File("/sdcard/PPGoPlaces/"+hLocation.getId());

	    if (!directory.exists()) {
	        directory.mkdirs();
	    }

	    String TempPictureFile;
	    try {
	        TempPictureFile = newPicture.getLastPathSegment() + ".jpg";
	        TempFilePath = directory.getPath() + "/" + TempPictureFile;
	        FileOutputStream myOutStream = new FileOutputStream(TempFilePath);
	        InputStream myInStream = getActivity().getContentResolver().openInputStream(newPicture);
	        FileIO myFileIO = new FileIO();
	        myFileIO.copy(myInStream, myOutStream);
	        //now delete the file after copying
	        getActivity().getContentResolver().delete(newPicture, null, null);
	        TempFilePaths.add(TempFilePath);
	    } catch (Exception e) {
	        //Toast.makeText(getApplicationContext(), getString(R.string.ErrorOpeningFileOutput), Toast.LENGTH_SHORT).show();
	    }
		
	}

	//--can implement in separate file I think, but probably good enough as inner class==
	//--
	public class FileIO {
		private static final int BUFFER_SIZE = 1024 * 2;

		public FileIO() {
		    // Utility class.
		}

		public int copy(InputStream input, OutputStream output) throws Exception, IOException {
		    byte[] buffer = new byte[BUFFER_SIZE];

		    BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
		    BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
		    int count = 0, n = 0;
			    try {
			        while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
			            out.write(buffer, 0, n);
			            count += n;
			        }
			        out.flush();
			    } finally {
			        try {
			            out.close();
			        } catch (IOException e) {
			            Log.e(e.getMessage(), null);
			        }
			        try {
			            in.close();
			        } catch (IOException e) {
			            Log.e(e.getMessage(), null);
			        }
			    }
			    return count;
		}
		}
	
}  //END OF MAIN CLASS
