package com.liseem.android.travel.items;

import java.io.File;

public class LPicture {

	protected static final String TAG="LPicture";
	
	private long 		Id=0;
	private long 		refId=0;	
	private String 		name="";
	private String 		locationName="";
	private String 		picDate="";
	private String 		address="";
	private double 	latitude;
	private double 	longitude;
	private String		picpath="";
	private byte[] 		picObject;
	private String 		notes="";
	private int			orient;
	private int			scalePicture;
	private int			bmRotate;
	private boolean 	mark=false;
	private String		fileName;
	
	//--Support functions for object instance conditions--
	public boolean hasAddress() {
		//return null != address; //return true is address is not null
		return (address != null);
	}
	
	public boolean hasGeo() {
		//return null != address; //return true is address is not null
		return (latitude != 0 && longitude != 0);
	}
	
	public boolean isMark() {
		return mark;
	}
	
	public void toggleMark() {
		mark=!mark;
	}
	
	public String getFileName() {
		return new File(picpath).getName();
		//int idx = picpath.replaceAll("\\", "/").lastIndexOf("/");
		//return idx >= 0 ? picpath.substring(idx + 1) : picpath;
	}
	
	public int getScalePicture() {
		if (orient > 10) {
			String orientText=String.valueOf(orient);
			scalePicture=Integer.parseInt(orientText.substring(1,2));	//second digit scale factor		
			return scalePicture;
		} else {
			return 0;
		}
	}
	
	public int getRotate() {
		if (orient > 10) {
			String orientText=String.valueOf(orient);		
			bmRotate=Integer.parseInt(orientText.substring(0,1));			//first digit	
			return bmRotate;
		} else {
			return 0;
		}
	}
	
	//--Getters and Setters for db fields--
	
	public long getId() {
		return Id;
	}

	public void setId(long id) {
		this.Id=id;
	}
	
	public long getRefid() {
		return refId;
	}
	public void setRefid(long dbRefId) {
		this.refId = dbRefId;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLocation() {
		return locationName;
	}
	public void setLocation(String locationName) {
		this.locationName = locationName;
	}
	
	public String getDate() {
		return picDate;
	}
	public void setDate(String picdate) {
		this.picDate = picdate;
	}
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
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
	
	public byte[] getObject() {
		return picObject;
	}
	
	public void setObject(byte[] picobject) {
		this.picObject =picobject;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes=notes;
	}

	public int getOrient() {
		return orient;
	}

	public void setOrient(int orient) {
		this.orient = orient;
	}

	public boolean getMark() {
		return mark;
	}	
	
	public void setMark(boolean mark) {
		this.mark=mark;	
	}
	
	public String getPicPath() {
		return picpath;
	}

	public void setPicPath(String pathinfo) {
		this.picpath=pathinfo;
	}	
	
	public String getThumbPath() {			//does this even work?
		int idx=picpath.indexOf(",")+1;
		String thumbNail=picpath.substring(idx, picpath.length());
		return thumbNail;
	}
	
	public String getBitmapPath() {	//does this even work?
		int idx=picpath.indexOf(",");
		String imagePath=picpath.substring(1, idx);
		return imagePath;
	}
}
