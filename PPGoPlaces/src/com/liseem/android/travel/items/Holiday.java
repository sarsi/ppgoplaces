package com.liseem.android.travel.items;

import java.io.Serializable;

public class Holiday implements Serializable {
	
	protected static final String TAG="Holiday";
	
	private long 			id;
	private long 			refid;
	private int 				holId;				//--holId and refid is the same the later is long, former int, INSERT and UPDATE use holId
	private String 		holiday;		//--get and set name are the same as holiday
	private String 		country;
	private String 		city;
	private String 		start_date;
	private String 		end_date;
	private String 		notes;
	private boolean 	photoAlbum;

	
	//--programmatic setters and getters-------------------
	
	public boolean hasAlbum() {
		return photoAlbum;
	}
	
	public void setAlbum(boolean album) {
		photoAlbum=album;
	}
	
	//--db setters and getters-----------------------------------
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

	public String getHoliday() {
		return holiday;
	}

	public void setHoliday(String holiday) {
		this.holiday = holiday;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getStart_date() {
		return start_date;
	}

	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}

	public String getEnd_date() {
		return end_date;
	}

	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getName() {
		return holiday;
	}

	public void setName(String name) {
		this.holiday = name;
	}
	
	public int geHolId() {
		return holId;
	}
	
	public void setHolId(int holId) {
		this.holId = holId;
	}
	
	
}	