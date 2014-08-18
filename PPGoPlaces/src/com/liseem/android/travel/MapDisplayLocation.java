/*
 * 
 * google map keys
 * release key: "0riAp5Zuv0TkZdjlxCJFN2pLBGZlgsu5d2okf5g"
 * debug key:"0riAp5Zuv0TmIB7ooEPC6CSbVDCDEgFPy7rE6Zw"
 * 
 * Neuschwanstein Castle
 * Schwangu 87645
 * 47.558341,10.749865
 * 
 * Munich Marienplatz
 * 48.140662,11.576672
 * 
 * Basilica di San Marco, Piazza di San Marco, Florence, Italy
 * 43.778329,11.258798
 * 
 * currently using debug key via test with eclipse, only release key if install 
 * via email to user.
 */

package com.liseem.android.travel;

import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;
import static com.liseem.android.travel.TravelLiteActivity.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import com.google.android.maps.GeoPoint;
//import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapController;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.liseem.android.travel.items.HLocation;
//import com.liseem.android.travel.items.MapOverlay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



public class MapDisplayLocation extends Fragment{
	
	//=====SECTION 1==DECLARATION===================================
	
	private static final String 		TAG ="MapDisplayLocation";
	public static final String 			ADDRESS_RESULT ="address";
	
	//--application--------------------------------------------
	private PPGoPlacesApplication app;
	private SharedPreferences 		prefs;
	private String 							emailSignature;

	//--view-----------------------------------------------------
	private EditText 						addressText;
	private Button 							useLocation, mapLocation;
	private TextView 						caddressText;

	//--location------------------------------------------------------------------------
	private List<Address> 				addresses;
	private List<Address> 				caddresses;
	private Address 							caddress,address;
	private String 							locAdd=" ", addressString;	
	private Geocoder 						gcoder;
	private LocationListener 			locationlistener;
	private Location 						myLocation;
	
	
	//--mapview-----------------------------------------------------------------------
	private MapView 						mapView;														
	private List<Overlay> 				listOverlays;
	//private MapOverlay 					addressOverlay;
	private MyOverlay						addressOverlay;
	private MapController 				mcontrol;
	private MyLocationOverlay 		myLocationOverlay;
	private GeoPoint 						Pointo;
	private GeoPoint 						gp;
	
	//--program fields------------------------------------------------------------------
	private boolean 						viewMap=false;
	private boolean						satelliteMode;
	private long								dbRowId;				//get row id of location from intent
	private HLocation						hLocation;			//retreive location for address display
	private String 							currentLoc;
	private AlertDialog 					exitprogram;
	private String 							whoCalling;
	private double 							longitude, latitude;
	private double 							cLatitude, cLongitude;
	private String								locationAddress;

	
	//--background thread for geocoding to prevent exception---------------
	private Context 							mContext;
	private Handler 							mHandler;
	private GeoPoint 						geoPoint;

	

	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	
		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;
	
		//--inflate layout for fragment 1------------------------------------------------
		View v=inflater.inflate(R.layout.map_location, container, false);
		setRetainInstance(true);
		
		return v;			
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.map_location);

		setupView();
	}

	@Override
	public void onResume() {
		super.onResume();
		myLocationOverlay.enableMyLocation();
	}	
	
	/*@Override
	public void onBackPressed() {
		super.onBackPressed();
	}*/
	
	@Override
	public void onPause() {
		myLocationOverlay.disableMyLocation();
		super.onPause();		
	}

	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	

	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 3==SETUP VIEWS====================================

	private void setupView() {
		
		//--path to main application-----------------------------------------
		app							=(PPGoPlacesApplication)getActivity().getApplication();
		
		//--get SharedPreferences object--------------------------------
		prefs						=getActivity().getSharedPreferences(PREFNAME, MODE_PRIVATE);
		satelliteMode			=prefs.getBoolean("satelliteMode", false);
		emailSignature		=prefs.getString("emailSignature", DEFAULT_SIGNATURE);
		
		//--get extras pass from Intent--	
		Bundle bundle			=getActivity().getIntent().getExtras();
		if (bundle != null) 
		{
			dbRowId				=bundle.getLong(LOC_ROWID);
			hLocation				=new HLocation();
			hLocation				=app.getHLocation(dbRowId);
		}	else {
			getActivity().finish();
		}
		
		//--turn off view that not use in this activity------------------------------
		addressText		=(EditText)getActivity().findViewById(R.id.locate_address);
		addressText.setVisibility(View.GONE);
		mapLocation		=(Button)getActivity().findViewById(R.id.map_location_button);
		//mapLocation.setVisibility(View.GONE);
		mapLocation.setText(R.string.share_this_view);
		useLocation			=(Button)getActivity().findViewById(R.id.use_location_button);
		//useLocation.setVisibility(View.GONE);
		useLocation.setText(R.string.save_location_image);

		//--sett up my Current Location on mapview-----------------------------
		mapView				=(MapView)getActivity().findViewById(R.id.map_view);
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(satelliteMode);
		//--problem with streetview lot of square tile--
		mapView.setStreetView(true);
		//--to display traffic--


		myLocationOverlay=new MyLocationOverlay(getActivity(),mapView);
		myLocation			=myLocationOverlay.getLastFix();
		mapView.getOverlays().add(myLocationOverlay); //this is my location 
		
		cLatitude				=hLocation.getLatitude();
		cLongitude			=hLocation.getLongitude();
		//=====SANDY=======add edit location marker on mapview=====
		if (hLocation.getLatitude() != 0 && hLocation.getLongitude() !=0) {
			addressOverlay=new MyOverlay(hLocation.getLatitude(), hLocation.getLongitude());
			mapView.getOverlays().add(addressOverlay);
		}
		
		mapView.invalidate();
		
		mcontrol=mapView.getController();
		mcontrol.setZoom(2);
		if (cLatitude >0 && cLongitude >0) {
			animateToLocation();
		} 
		
		//--display location address------------------------------
		StringBuilder locationAddress=new StringBuilder();
		locationAddress
			.append("Name: ").append(hLocation.getName()).append("\n")
			.append(hLocation.getAddress().toString()).append("\n")
			.append("Latitude: ").append(hLocation.getLatitude())
			.append("Longitude: ").append(hLocation.getLongitude());
				
	}	//END setupView()
	
	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================
	
	//=====SECTION 4==ADD ADAPTER===================================

	//=====SECTION 5==SET LISTENER====================================

	//--BUTTON--save location view image to hlocation blob--------------------
	public void useLocationClick(View v) {
		//new createBitMapFile().execute(getMapImage());
		new storeImageToDB().execute(getMapImage());
		getActivity().finish();
	}
	
	//--BUTTON--share this view with friend----------------------------------------
	public void mapLocationClick(View v) {
		new createAndSendBitMapFile().execute(getMapImage());
		getActivity().finish();
	}
	
	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================

	//=====SECTION 6==LISTENER METHODS================================

	//=====SECTION 7==HELPER METHODS==================================

	//--Display location from calling intent-- 
	//longitude and latitude information from onCreate getIntent
	public void animateToLocation() {

		Pointo=new GeoPoint(
				(int) (cLatitude * 1E6),
				(int) (cLongitude * 1E6));
		//Log.d(TAG, "animateToLocation() Pointo ");

		mcontrol.animateTo(Pointo);
		mcontrol.setZoom(15);	
	}

	protected boolean isRouteDisplayed() {
		return false;
	}
	
	//--create a bitmap from view cache of map view------------------------------------
	private Bitmap getMapImage() {
		//Log.d(TAG, "getMapImage() landed");
		
		//--need to put this in button to save mapview
		//--disable caching, destroy the cache, and force a rebuild
		mapView.setWillNotCacheDrawing(false);
		mapView.destroyDrawingCache();				//destroy cache
		mapView.buildDrawingCache();					//force a redraw
		
		//--copy the drawing cache before the system recycles it
		Bitmap cacheMap=Bitmap.createBitmap(mapView.getDrawingCache()); //quickly get cache
		
		//--I think this draws bitmap back to mapView---------
		Bitmap bmap = Bitmap.createBitmap(mapView.getWidth(), mapView.getHeight(),
                Bitmap.Config.ARGB_8888);
		Canvas offscreencanvas = new Canvas(bmap);
		offscreencanvas.drawBitmap(cacheMap, 0, 0, null);
		//Log.d(TAG, "getMapImage() before return cacheMap");
		
		return cacheMap;
		
		//===another codes==============
		//-- Position map for output 
        /*MapController mc = mapView.getController();  
        mc.setCenter(SOME_POINT);  
        mc.setZoom(16);  
  
        //-- Capture drawing cache as bitmap 
        mapView.setDrawingCacheEnabled(true);  
        Bitmap bmp = Bitmap.createBitmap(mapView.getDrawingCache());  
        mapView.setDrawingCacheEnabled(false);  
  
        return bmp;  */
        //====from anther=====================
	}
		

	//--store bitmap from getMapImage to hLocation blob----------------------------------------------
	protected class storeImageToDB extends AsyncTask <Bitmap, Void, Void> {

		@Override
		protected Void doInBackground(Bitmap... params) {
			Bitmap bmp=params[0];
			//Log.d(TAG,"storeImageToDB, doinBackground landed");
			
			//------------------------
			ByteArrayOutputStream baos=new ByteArrayOutputStream();		
			bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
			byte[] imageBlob=baos.toByteArray();
			
			//Log.d(TAG,"storeImageToDB, after creating byte[] imageBlob");
			hLocation.setAddressObj(imageBlob);
			app.updateHLocation(hLocation);
			
			//Log.d(TAG,"storeImageToDB, done updating hLocation ");
			//encodedImage = Base64.encodeToString(imageBlob, Base64.NO_WRAP);
			return null;
		}
		
	}
	
	//--send jpg file created from createBitMapFile---------------------------------------
	protected void  sendFileAttached(String shareFile) {
		
		//--create attachment-------------------------------------
		File fileSelected=new File(shareFile.toString());	
		
		//--mail body-------------------------------------------------
		StringBuilder fileInfo=new StringBuilder();
		
		//--item details------------------------------------------	
		fileInfo.append("Location: ").append(hLocation.getName().toString());
		if (hLocation.getAddress() != null)
			fileInfo.append("\nAddress: ").append(hLocation.getAddress().toString());		
	
		
		//--email signature--------------------------------------------
		fileInfo.append("\n\n")
			.append("[").append(emailSignature.toString()).append("]");
		
		//--start intent to send location object-------------------------
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
	    sendIntent.setType("file/ppgp");
	
	    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Location: "+hLocation.getName().toString());
	    sendIntent.putExtra(Intent.EXTRA_TEXT, fileInfo.toString());   
	    if (fileSelected.exists()) {
	    	Uri uri=Uri.fromFile(fileSelected);
	    	sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
	    }
    
	    //sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://sdcard/dcim/Camera/filename.jpg"));
	    startActivity(Intent.createChooser(sendIntent, "Send Email.:"));    
	}
	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================
	
	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//--create png file from bitmap, store in cache directory-----------------------
	protected class createAndSendBitMapFile extends AsyncTask <Bitmap, Void, String> {
		
		@Override
		protected String doInBackground(Bitmap... params) {
			//Log.d(TAG, "createBitMapFile async doInBackground landed");
			//Log.d(TAG, "createSerializeLocationFile() doInBackground landed");
			
			//--get bitmap------------------------------------------------------------------
			Bitmap bmp=params[0];
			
			//--create file name for bitmap image------------------------------------
			String fileName=hLocation.getName().toString();
			fileName=fileName.replaceAll("\\s", "");
			int nameLength=fileName.length();
			if (nameLength<9) {
				fileName=fileName.toString()+"ppgoplaces";
			}
			StringBuilder newName=new StringBuilder().append(fileName.substring(0, 8)).append(".jpg");
			fileName=newName.toString();
			
			
			//--create directory and file to write bitmap---------------------------------
			File exportDir=new File(Environment.getExternalStorageDirectory(), PPGP_CACHE );
			if (!exportDir.exists()) {
				exportDir.mkdirs();
			}
			
			File newFile=new File(exportDir, fileName);
			FileOutputStream out;
			try {
				out = new FileOutputStream(newFile);
			
			//--write bitmap to file-------------------------------------------------------------
			//--PNG format, use JPEG for jpg, 0-100 100 is high quality, out is outputstream file
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
				return newFile.getAbsolutePath().toString();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			return null;
			
		}		//END doInBackground

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				sendFileAttached(result.toString());
			}
			//super.onPostExecute(result);
		}
	
	}	//END createAndSendBitMapFile()
	
	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//=====SECTION 10==SANDBOX======================================	
	private class MyOverlay extends com.google.android.maps.Overlay {
		 
		public MyOverlay(double Latitude, double Longitude) {
			Double convertedLongitude = Longitude * 1E6;
			Double convertedLatitude = Latitude * 1E6;
			
			setGeopoint(new GeoPoint(
					convertedLatitude.intValue(),
					convertedLongitude.intValue()));			
		}
		
	    @Override
	    public void draw(Canvas canvas, MapView mapView, boolean shadow) {                              
	        super.draw(canvas, mapView, shadow);
	 
	        if (!shadow) {                                                                              
	            Point point = new Point();
	            mapView.getProjection().toPixels(getGeoPoint(), point);                                      
	 
	            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ppgp_pt);   
	            int x = point.x - bmp.getWidth() / 2;                                                   
	            int y = point.y - bmp.getHeight();                                                      
	 
	            canvas.drawBitmap(bmp, x, y, null);                                                    
	        }
	 
	    }
	 
	}
	
	public void setGeopoint(GeoPoint geoPoint) {
		this.geoPoint=geoPoint;		
	}
	
	public GeoPoint getGeoPoint() {
		return geoPoint;
	}


}  	//END MAIN CLASS
	
