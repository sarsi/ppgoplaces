package com.liseem.android.travel.items;

public class TaskList {
	
	protected static final String TAG="CheckList";
	
	private int	 		id;
	private int 			refid;
	private int			cat;		//formerly int cat same as refid, reassign to v2 new String cat field
	private String 	name,
								address,
								notes;
	private double	latitude,
								longitude;
	
	private boolean complete;

	
	//--Support functions for object instance conditions--
	public void toggleComplete() {
		//return null != address; //return true is address is not null
		complete = !complete;
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public boolean hasAddress() {
		return null !=address;
	}
	

	 //--Getters and Setters for db object fields
	public TaskList(String shopping) {
		name=shopping;
	}

	public TaskList() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public void setComplete(boolean complete) {
		this.complete=complete;
	}
	
	public boolean getComplete() {
		return complete;
	}

	public int getCat() {
		return cat;
	}
	
	public void setCat(int cat) {
		//this.refid=cat;
		this.cat=cat;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public long getRefid() {
		return refid;
	}
	
	public void setRefid(int refid) {
		//this.cat=refid;
		this.refid = refid;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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
	
}		//END MAIN CLASS
