package com.liseem.android.travel;

import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;
import static com.liseem.android.travel.TravelLiteActivity.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.liseem.android.travel.items.HLocation;
import com.liseem.android.travel.items.Holiday;
import com.liseem.android.travel.items.MyDate;

public class MapHolidayLocations extends Fragment {
//replaced extends MapActivity wth Fragment
	
	//=====SECTION 1==DECLARATION===================================

	private static final String 			TAG="MapHolidayLocations";

	//--setupView---------------------------------------
	private PPGoPlacesApplication 	app;
	private SharedPreferences 			prefs;
	private String 								deviceName;
	private long 									holrowid;
	private boolean 							filterLocation;
	private MapView 							map;
	private MapController 					mapControl;	
	private List<Overlay> 					overlays;
	private Context 								context;
	
	//--loadData----------------------------------------
	private ArrayList<HLocation> 		hLocations;
	private ArrayList<HLocation> 		geoLocations;
	private ArrayList<GeoPoint> 		geoHelp;
	private Holiday 								holiday;
	private GeoPoint 							locPoints[];
	private boolean 							geoAvail;
	private boolean 							satelliteMode;
	private TextView 							infoText;
	private TextView 							holText;
	private boolean 							scrollTextMode;

	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	
		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;
	
		//--inflate layout for fragment 1------------------------------------------------
		View v=inflater.inflate(R.layout.map_holiday_locations, container, false);
		setRetainInstance(true);
		
		return v;			
	}
	
   @Override
    public void onCreate(Bundle data) {
        super.onCreate(data);
       // setContentView(R.layout.map_holiday_locations);

        setupView();
    }
   
	/*@Override
	public void onBackPressed() {
		super.onBackPressed();
	}*/
	
   
	/*@Override
	public boolean isRouteDisplayed() {
		return false;
	}*/

	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 3==SETUP VIEWS====================================
	
	private void setupView() {
		
		//--Path to PPGoPlacesApplication--
		//--need to retry notifySetChanged from non UI thread--
		app							=(PPGoPlacesApplication)getActivity().getApplication();
		
		//--shared preferences-------------------------------------------------
 		prefs						=getActivity().getSharedPreferences (PREFNAME, MODE_PRIVATE);
 		deviceName			=prefs.getString("deviceName","samsung");
 		satelliteMode			=prefs.getBoolean("satelliteMode", false);
 		scrollTextMode		=prefs.getBoolean("scrollTextMode", true);

		//--setup view-----------------------------------------------------------
		holText					=(TextView)getActivity().findViewById(R.id.holText);
		infoText					=(TextView)getActivity().findViewById(R.id.infoText);
		holText.setSelected(scrollTextMode);	
		
		//--retrieve bundle from view holiday intent---------------------
		Bundle hextras		=getActivity().getIntent().getExtras();
		if (hextras !=null) {
			holrowid				=hextras.getLong(HOL_REFID);
			filterLocation		=hextras.getBoolean(FILTER_LOCATION, true);
		}
		
		loadData();		//setup locPoints array of all holiday locations
		
		//--ppgp drawable marker---------------------------------------------
       Drawable marker 	= getResources().getDrawable(R.drawable.ppgp_pt);		//red_marker
        
       //--Initialise constructor of inner class with marker--------------
        LocsItemizedOverlay locs = new LocsItemizedOverlay(marker, context); 
        
        //--add itemized locs to map overlay arraylist----------------------
        map 						= (MapView)getActivity().findViewById(R.id.map_view);
        map.setStreetView(true);
        map.setSatellite(satelliteMode);
       
        List<Overlay> overlays = map.getOverlays();
        overlays.add(locs);

        map.setBuiltInZoomControls(true);
         mapControl = map.getController();
      
        mapControl.setCenter(locs.getCenter());
        //mapControl.setCenter(locPoints[0]);
        mapControl.zoomToSpan(locs.getLatSpanE6(), locs.getLonSpanE6());   
        Toast.makeText(getActivity().getBaseContext(), R.string.please_be_patient_the_map_will_take_a_while_to_load, Toast.LENGTH_SHORT).show();
		
	}		//END setupView

	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================

	private void loadData() {
		hLocations=app.loadLocations(holrowid, filterLocation);
		holiday=app.getHoliday(holrowid);
		
		MyDate start=new MyDate();
		MyDate end=new MyDate();
		start.setStrDate(holiday.getStart_date().toString());
		end.setStrDate(holiday.getEnd_date().toString());
		
		StringBuilder tempStr=new StringBuilder();
		tempStr
		.append(holiday.getHoliday().toString())
		.append(" From ").append(start.getDMYDate().toString())
		.append(" To ").append(end.getDMYDate().toString());
		
		holText.setText(tempStr.toString());		
		
		//--check if all location for gps location information
		for (HLocation h: hLocations) {
			if (h.getLatitude() !=0 && h.getLongitude() !=0) {
				geoAvail=true;
			}
		}
		
		//--get all available location informatons
		geoHelp=new ArrayList<GeoPoint>();
		geoLocations=new ArrayList<HLocation>();
		if (geoAvail) {
			for (HLocation h: hLocations) {
				if (h.getLatitude() !=0 && h.getLongitude() !=0) {
					GeoPoint gP=new GeoPoint((int)(h.getLatitude()*1E6),(int)(h.getLongitude()*1E6));
					geoLocations.add(h);
					geoHelp.add(gP);
				}			
			}
		}
	
		//--array of location points loc points use in ItemisedOverlay
		locPoints=new GeoPoint[geoHelp.size()];
		geoHelp.toArray(locPoints);				
	}
	
	//=====SECTION 4==ADD ADAPTER===================================

	//=====SECTION 5==SET LISTENER====================================
	
	//=====SECTION 6==LISTENER METHODS================================

	//=====SECTION 7==HELPER METHODS==================================
	
    private class LocsItemizedOverlay extends ItemizedOverlay<OverlayItem> {

    	Context context;
    	String locationName;
    	OverlayItem item;
    	
    	//--constructor----------------------------------------------------------------
        public LocsItemizedOverlay(Drawable defaultMarker, Context mContext) {
            super(defaultMarker);
            this.context=mContext;
            
            //--change the direction of the shadow so the bottom of the marker is the part "touching"
            boundCenterBottom(defaultMarker);

            //--static data, so we call this right away
            populate();
        }

        @Override
        public GeoPoint getCenter() {
            Integer averageLat = 0;
            Integer averageLon = 0;
            for (GeoPoint point : locPoints) {
                averageLat += point.
                		getLatitudeE6();
                averageLon += point.getLongitudeE6();
            }
            averageLat /= locPoints.length;
            averageLon /= locPoints.length;
            return new GeoPoint(averageLat, averageLon);
        }


        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            super.draw(canvas, mapView, false);
        }

       
        @Override
        protected OverlayItem createItem(int i) {
            // the "title" and "snippet" fields aren't used anywhere just yet... so
            // we've ignored them here
            item = new OverlayItem(locPoints[i], null, null);            
            return item;
        }

        @Override
        public int size() {
            return locPoints.length;
        }
        
		@Override
		protected boolean onTap(int i) {
				
			infoText.setText(geoLocations.get(i).getName()+"\n"
					+geoLocations.get(i).getAddress());
			
			//Toast.makeText(getApplicationContext(), geoLocations.get(i).getName()+"\n"
			//		+geoLocations.get(i).getAddress(), Toast.LENGTH_LONG).show();
			return super.onTap(i);
		}
        
        
        @Override
		public boolean onTap(GeoPoint p, MapView mapView) {
        	return super.onTap(p, mapView);
		}

		@Override
		public boolean onTouchEvent(MotionEvent e, MapView mapView) {
			return super.onTouchEvent(e, mapView);
		}
    }		//END of LocsItemizedOverlay
    
	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//=====SECTION 10==SANDBOX======================================
    
    private void takeScreenShot() {
    	View view 				= getActivity().findViewById(R.id.map_view);
    	View v 					= view.getRootView();
        v.setDrawingCacheEnabled(true);
        Bitmap b 					= v.getDrawingCache();    
        String tempString	=holiday.getName().toString();
       tempString				=tempString.substring(0, 7);
        
        File exportDir = new File(Environment.getExternalStorageDirectory(), "ScreenCapture");
        if (!exportDir.exists()) {
        	exportDir.mkdirs();
        }   
        
        File file = new File(exportDir, tempString+".jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), b, "Screen", "screen");
        }catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    

   
    
}			//END OF MAIN CLASS
