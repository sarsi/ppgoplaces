package com.liseem.android.travel.items;


import java.io.Serializable;
import android.os.Parcel;
import android.os.Parcelable;



public class HLocation implements Serializable, Parcelable {
	
	protected static final String TAG="HLocation";
	
	private long 				id;
	private long 				refid;	
	private String 			name;
	private String 			locationName;		//same as name
	private String 			ldate;
	private String 			address;
	private String				street;				//ver 1.2
	private String				city;					//ver 1.2
	private String				postal;				//ver 1.2
	private String				country;			//ver 1.2
	
	private String 			notes;
	private double 			latitude;
	private double 			longitude;
	private byte[] 			addressObj;	
	private String 			info;
	private boolean		picture=false;
	private boolean		holiday=false;
	
	//--new beta v0.8-----------------------------
	private long 				distanceHere;

	//=====PARCELABLE IMPLEMENTATION================================
	//--constructor for Parcel-------------------------
	public HLocation(Parcel source) {
		id=source.readLong();
		refid=source.readLong();
		name=source.readString();
		locationName=source.readString();
		ldate=source.readString();
		address=source.readString();
		street=source.readString();
		city=source.readString();
		postal=source.readString();
		country=source.readString();		
		notes=source.readString();
		latitude=source.readDouble();
		longitude=source.readDouble();
		addressObj=source.createByteArray();		//maybe a problem		
		info=source.readString();
		holiday=source.readByte()==1;
		picture=source.readByte()==1;	
		distanceHere=source.readLong();					//programmatic not part of db
	}

	public HLocation() {
		
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeLong(refid);
		dest.writeString(name);
		dest.writeString(locationName);
		dest.writeString(ldate);
		dest.writeString(address);
		dest.writeString(street);
		dest.writeString(city);
		dest.writeString(postal);
		dest.writeString(country);		
		dest.writeString(notes);
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeString(info);
		dest.writeByteArray(addressObj);
		dest.writeByte((byte) (holiday ? 1 : 0)); 		//for boolean
		dest.writeByte((byte) (picture ? 1 : 0)); 		//for boolean, readFromParcel will be picture=in.readByte()==1;		
		dest.writeLong(distanceHere);
	} 
	
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Parcelable.Creator<HLocation> CREATOR = new Parcelable.Creator<HLocation>() {

		@Override
		public HLocation createFromParcel(Parcel source) {
			return new HLocation(source);
		}

		@Override
		public HLocation[] newArray(int size) {
			return new HLocation[size];
		}
	};
	
	//--required during unmarshaling data stored in a Parcel-----------------------
	/*public class MyCreator	implements Parcelable.Creator<HLocation> {

		@Override
		public HLocation createFromParcel(Parcel source) {
			return new HLocation(source);
		}
	
		@Override
		public HLocation[] newArray(int size) {
			return new HLocation[size];
		}
	
	}*/



	//=====PARCELABLE IMPLEMENTATION================================

	//--programmatic variables-----------------------------------------
	public long getDistanceHere() {
		return distanceHere;
	}
	
	public void setDistanceHere(long distance) {
		distanceHere=distance;
	}
	
	//--Support functions for object instance conditions----------
	public boolean hasLocation() {
		//return null != address; //return true is address is not null
		return (latitude != 0 && longitude != 0);
	}
	
	public boolean hasAddress() {
		return null !=address;
	}
	
	//--Getters and Setters for db fields--
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLocationName() {
		return name;
	}
	public void setLocationName(String locationName) {
		this.name = locationName;
	}
	
	public long getRefid() {
		return refid;
	}
	public void setRefid(long dbRefId) {
		this.refid = dbRefId;
	}
	
	public boolean hasPicture() {
		return picture;
	}
	
	public void setPicture(boolean picture) {
		this.picture=picture;
	}
	
	public void setPicture(String strPict) {
		String temp=strPict;
		this.picture=Boolean.parseBoolean(temp);
	}
	
	public boolean getPicture() {
		return picture;
	}
	
	public boolean hasHoliday() {
		return holiday;
	}
	
	public void setHoliday(boolean holiday) {
		this.holiday=holiday;
	}
	
	public void setHoliday(String strHol) {
		String temp=strHol;
		this.holiday=Boolean.parseBoolean(temp);
	}
	
	public boolean getHoliday() {
		return holiday;
	}
	
	public String getLdate() {
		return ldate;
	}
	public void setLdate(String ldate) {
		this.ldate = ldate;
	}
	
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPostal() {
		return postal;
	}

	public void setPostal(String postal) {
		this.postal = postal;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
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
	
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id=id;
		
	}


	public byte[] getAddressObj() {
		return addressObj;
	}

	public void setAddressObj(byte[] obj) {
		this.addressObj=obj;
	}

}


