package com.liseem.android.travel.items;

public class Shopping {
	
	protected static final String TAG="Shopping";
	
	private long 		id;
	private long 		refid;
	private int			holId;
	private String		price;
	private String 	name;
	private String 	address;
	private String		notes;
	private String		cat;
	private double	latitude;
	private double	longitude;
	private int			intCat;
	
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
	public Shopping(String shopping) {
		name=shopping;
	}

	public Shopping() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getPrice() {
		return price;
	}
	
	public void setPrice(String price) {
		this.price =price;
	}


	public void setComplete(boolean complete) {
		this.complete=complete;
	}
	
	public boolean getComplete() {
		return complete;
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCat() {
		return cat;
	}

	public void setCat(String cat) {
		this.cat = cat;
	}

	public int getIntCat() {
		if (cat==null) {
			intCat=1;
		} else {
			intCat=Integer.parseInt(cat.toString());
		}
		return intCat;
	}
	
	public void setIntCat(int intCat) {
		this.intCat = intCat;
		if (intCat >0)
			cat = String.valueOf(intCat);
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
	
	public int getHolId () {
		return holId;
	}
	public void setHolId (int holId) {
		this.holId=holId;
	}
	
}		//END MAIN CLASS
