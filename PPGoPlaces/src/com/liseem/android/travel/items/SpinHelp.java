package com.liseem.android.travel.items;

import android.location.Address;
import android.location.Location;

public class SpinHelp {

	
	protected static final String TAG="SpinHelp";
	
	private long 			id;
	private long 			refid;
	private String			name;
	private String			stringCat;
	private int				cat;
	private boolean 	hasCat;
	private Address		address;
	private double		latitude;
	private double		longitude;
	private Location		location;
	
	
	public boolean hasCat() {
		if (stringCat==null) {
			return false;
		} else {
			return true;
		}
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public long getRefid() {
		return refid;
	}
	public void setRefid(long refid) {
		this.refid = refid;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getStringCat() {
		return stringCat;
	}
	public void setStringCat(String stringCat) {
		this.stringCat = stringCat;
		cat = Integer.parseInt(stringCat);
	}
	
	public int getCat() {
		return cat;
	}
	
	public void setCat(int cat) {
		this.cat = cat;
		stringCat = Integer.toString(cat);
	}
	
	public Address getAddress() {
		return address;
	}
	
	public void setAddress(Address address) {
		this.address = address;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	
	
}
