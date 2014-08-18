/*
 * Need to ensure deployment key in map_location prior to export
 * signed apk
 * debug key="0riAp5Zuv0TmIB7ooEPC6CSbVDCDEgFPy7rE6Zw"
 * deployment key = "0riAp5Zuv0TkZdjlxCJFN2pLBGZlgsu5d2okf5g"
 */

package com.liseem.android.travel.items;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Address;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.liseem.android.travel.PPGoPlacesApplication;


public class MapOverlay extends Overlay {

	private static final String TAG ="MapOverlay";
	
	private static final int CONTAINER_RADIUS		=10;
	private static final int CONTAINER_SHADOW_OFFSET =1;
	
	private Address address;
	private GeoPoint geopoint;
	private boolean positionChange;

	private PPGoPlacesApplication app;
	
	public MapOverlay(Address address) {
		super();
		assert(null != address);
		this.setAddress(address);
		Double convertedLongitude = address.getLongitude() * 1E6;
		Double convertedLatitude = address.getLatitude() * 1E6;
		
		setGeopoint(new GeoPoint(
				convertedLatitude.intValue(),
				convertedLongitude.intValue()));
	}
	
	public MapOverlay(double Latitude, double Longitude) {
		Double convertedLongitude = Longitude * 1E6;
		Double convertedLatitude = Latitude * 1E6;
		
		setGeopoint(new GeoPoint(
				convertedLatitude.intValue(),
				convertedLongitude.intValue()));
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
			
		//--translate the GeoPoint to screen pixels--
		Point screenPts=new Point();
		Projection projection=mapView.getProjection();
		//mapView.getProjection().toPixels(geopoint, screenPts);
		projection.toPixels(getGeoPoint(),screenPts);
		
		//--getting the marker--
		Paint containerPaint=new Paint();
		containerPaint.setAntiAlias(true);
		
		
		int containerX=screenPts.x;
		int containerY=screenPts.y;
		
		//Bitmap bmp=BitmapFactory.decodeResource(app.getApplicationContext().getResources(), R.drawable.ppgp_pt);
		//canvas.drawBitmap(bmp, containerX, containerY-28, null);
		
		if (shadow) {
			containerX += CONTAINER_SHADOW_OFFSET;
			containerY += CONTAINER_SHADOW_OFFSET;
			containerPaint.setARGB(80,00, 00, 00);
			//--add the marker--
			canvas.drawCircle(containerX, containerY, CONTAINER_RADIUS, containerPaint);
		} else {
			containerPaint.setColor(Color.RED);	
			canvas.drawCircle(containerX, containerY, CONTAINER_RADIUS, containerPaint);
		} 
	}

	public GeoPoint getGeoPoint() {
			return geopoint;
	}

	public void setGeopoint(GeoPoint geoPoint) {
		this.geopoint=geoPoint;
	}

	public void setAddress(Address address2) {
		this.address=address2;		
	}

	public Address getAddress() {
		return address;
	}
	
	//==sandy================
	@Override
	public boolean onTap(GeoPoint geoPoint, MapView mapView) {
		this.geopoint=geoPoint;
		mapView.getController().animateTo(geoPoint);
		positionChange=true;
		return super.onTap(geoPoint, mapView);
	}
	
	public boolean geoChange() {
		return positionChange;
	}
	
	public void resetGeoChange() {
		positionChange=!positionChange;
	}
	//==sandy================


}	//END MAIN CLASS
