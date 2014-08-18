/*
 * Adapter:							Location List Adapter
 * Fragment:						LocationView.java
 * Description:						Custom List Adapter for Location List View
 * 											Provides location data feed to listview and helper methods.
 * 
 * Layout item:					list_item.xml
 * 
 * Created: 							May 3, 2012
 * Last release:					September 9, 2012
 * Last updated: 					November 20, 2013
 * 
 * Changes:
 * - tidy codes and documentation
 * - improves to filterable, add constructor to get locationList and synchronized
 * 
 */

package com.liseem.android.travel.adapter;

import static com.liseem.android.travel.TravelLiteActivity.*;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import com.liseem.android.travel.PPGoPlacesApplication;
import com.liseem.android.travel.R;
import com.liseem.android.travel.items.HLocation;
import com.liseem.android.travel.items.AsyncTask;
import com.liseem.android.travel.items.Holiday;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LocationListAdapter extends BaseAdapter implements Filterable {
	
	private static final String 			TAG="LocationListAdapter";
	
	private Context 								context;
	private ArrayList<HLocation> 		locationList = new ArrayList<HLocation>();
	private ArrayList<HLocation> 		allLocationList;
	private ArrayList<HLocation> 		filteredList =new ArrayList<HLocation>();
	private ArrayList<String> 			listItems=new ArrayList<String>();
	private ArrayList<Long> 				questFRP=new ArrayList<Long>();
	private LayoutInflater 					mInflater;

	private PPGoPlacesApplication 	app;
	private SharedPreferences 			prefs;
	private boolean 							scrollTextMode;
	private int										sortList;
	private locationFilter 					filter;
	private boolean 							holidayFilter;
	private long 									holRefId=0;

	
	//--Constructor------------------------------------------------------------------------------- 	
	public LocationListAdapter(Context context, ArrayList<HLocation> allLocation) {
		
		this.context 			= context;
		setAllLocationList(allLocation);							//--master list
		locationList.addAll(allLocation);							//--list for feeding to adapter		

		//this.allLocationList	=allLocation;						//--master unmaniplated list		
		//this.locationList.addAll(allLocationList);			//--listview feed
		//allLocationList 		= new ArrayList<HLocation>();
		//locationList 				= new ArrayList<HLocation>();

		mInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		//--setup path to application------------------------------------------------
		app							= (PPGoPlacesApplication)context.getApplicationContext();
		
		//--shared preferences-----------------------------------------------------------------
		prefs						= app.getSharedPreferences (PREFNAME, MODE_PRIVATE);
		scrollTextMode		= prefs.getBoolean("scrollTextMode", true);
		sortList					= prefs.getInt("sortList", 1);
		sortLocationView(sortList);
	}
	
	//=========Next 4 Base Adatper Methods=========

	@Override
	public int getCount() {
		return (locationList !=null) ? locationList.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return (locationList==null) ? null : locationList.get(position);
	}

	//--strange getItemid return 1 less from getItem id??
	@Override
	public long getItemId(int position) {
		return (locationList==null) ? null : locationList.get(position).getId();
	}

	//--Get View---------------------------------------------------------------------------------------- 	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
			
		ViewHolder holder;
		
		if (convertView==null) {									//--if no view, create view
			holder=new ViewHolder();
			convertView=mInflater.inflate(R.layout.list_item, null);
			holder.text=(TextView)convertView.findViewById(R.id.itemName);
			holder.text.setSelected(scrollTextMode);		//--for scrolling text view marquee to work
			convertView.setTag(holder);
		} else {
			holder=(ViewHolder)convertView.getTag();	//--else recycle view
		}
		holder.text.setText(locationList.get(position).getName());		
		
		return (convertView);
	}

	//--View Holder------------------------------------------------------------------------------------- 
	public static class ViewHolder {
		TextView text;
		//TextView timestamp;
		//ImageView icon;
		//ProgressBar progress;
		//int position;
	}
	
	//=========END Main Custom Adatper Methods==============================

	
	//=====HELPER METHODS============================================
	
	//=========Adapter List setter, getter, add, change and delete Methods===============
	
	public void setAllLocationList(ArrayList<HLocation> locList) {
		allLocationList = new ArrayList<HLocation>();
		allLocationList = locList;
	}
	
	public ArrayList<HLocation> getAllLocationList() {
		return allLocationList;
	}
	
	//--CRUB-----------------------------------------------------------------------------
	//--update location--------------------
	//public void updateLocation(int position, HLocation newLocation) {		
	public void updateLocation(HLocation oldLocation, HLocation newLocation) {		
		
		//--update master list, remove old holiday insert new holiday-------------------
		ArrayList<HLocation> tempHelp = new ArrayList<HLocation>();
		//HLocation tempLocation = new HLocation();
				
		//--update master list------------------
		tempHelp = getAllLocationList();
		//tempLocation = app.getLastHLocation();
		tempHelp.remove(oldLocation);
		tempHelp.add(newLocation);
		setAllLocationList(tempHelp);

		//--update adapter list-----------------
		//locationList.set(position, newLocation);
		locationList.remove(oldLocation);
		locationList.add(newLocation);
		notifyDataSetChanged();
	}
	
	//--add location-----------------------
	public void addLocation(HLocation newLocation) {		
		
		ArrayList<HLocation> tempHelp = new ArrayList<HLocation>();		
		tempHelp=getAllLocationList();
		tempHelp.add(newLocation);
		setAllLocationList(tempHelp);
		
		locationList.add(newLocation);
		notifyDataSetChanged();
	}
	
	//--delete location--------------------
	public void deleteLocation(int position) {
	//public void deleteLocation(HLocation oldLocation) {		//--don't know why object list will not refresh
		
		ArrayList<HLocation> tempHelp = new ArrayList<HLocation>();		
		tempHelp=getAllLocationList();
		tempHelp.remove(getItem(position));
		//tempHelp.remove(oldLocation);
		setAllLocationList(tempHelp);
		
		locationList.remove(getItem(position));
		//locationList.remove(oldLocation);
		notifyDataSetChanged();
	}

	
	//--CRUB---END--------------------------------------------------------------------------
	
	//--SORT AND FILTERS-----------------------------------------------------------------
	
	//=========Adapter sorts and filters Methods=============================	
	
	//--TEXT FILTER VIEW------------Text watcher filter location--------------------------------------------------------
	//--3 abstract methods from implements Filterable
	//--Part 1 of 3 - text filter
	@Override
	public Filter getFilter() {
		if (filter==null)
			filter=new locationFilter(locationList);
		return filter;
	}
	//--Part 2 of 3 - text filter
	private class locationFilter extends Filter {
		
		private ArrayList<HLocation> allLocationList;
		 
		public locationFilter (ArrayList<HLocation> thisList) {		//--constructor pass from getFilter locationList
			allLocationList = new ArrayList<HLocation>();
			synchronized (this) {
				allLocationList.addAll(thisList);										//--thisList is "locationList"
			}
		}

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			
			//constraint=constraint.toString().toLowerCase();
			String findText = constraint.toString().toLowerCase();
			FilterResults result=new FilterResults();
			
			
			//if (constraint != null && constraint.toString().length() > 0) {
			if (findText != null && findText.toString().length() > 0) {
				
					ArrayList<HLocation> tempHelp = new ArrayList<HLocation>();
					
					for (HLocation l : allLocationList) {
						
						if(l.getName().toString().toLowerCase().contains(findText.toString())) //constraint.toString()
							tempHelp.add(l);
					}
					result.values=tempHelp;
					result.count=tempHelp.size();
				
			}  else {
				
				synchronized (this) {
					result.values=allLocationList;
					result.count=allLocationList.size();
				}
			}
			return result;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			locationList = (ArrayList<HLocation>)results.values;
			notifyDataSetChanged();
		}		
	}	
	
	//--remove filter-------------------------------------------------------------------------
	//--Part 3 of 3 - text filter
	public void resetLocationFilter() {
		locationList.clear();
		locationList=getAllLocationList();
		notifyDataSetChanged();
	}
	
	//--GEO FENCING-----------------------------------------------------------------------------------------------
	//--get location with 5KM of reference or default location-----------------------------
	//--Part 1 of 2 -- current location is the reference location
	private boolean locationWithinGeofence (HLocation currentLocation, HLocation hLocation, long distance) {
		float[] distanceArray = new float[1];
		Location.distanceBetween(		
				currentLocation.getLatitude(),
				currentLocation.getLongitude(),
				hLocation.getLatitude(),
				hLocation.getLongitude(),
				distanceArray);
		
		boolean within=(distanceArray[0] < distance);
		return within; 		//--true if distance array is less than distance
	}
	//--Part 2 of 2
	public void filterLocationByGeo(HLocation refLocation, long distance) {
		ArrayList<HLocation> tempHelp = new ArrayList<HLocation>();
		allLocationList = new ArrayList<HLocation>();
		allLocationList = getAllLocationList();
		for (HLocation l : allLocationList) {

			if (l.getLatitude() >0 && l.getLongitude() >0) {

				boolean isWithin=locationWithinGeofence(refLocation, l, distance);
				//Log.d(TAG, "isWithin Geofence return from part 1 "+isWithin);
				if (isWithin){
					tempHelp.add(l);
				}
			}
		}
		locationList.clear();
		locationList=tempHelp;
		notifyDataSetChanged();
	}
	
	//--COLLECTION SORT BY NAME AND DATE---------------------------------------------------
	//--sort by method call from viewlocation---------------------------------------------
	//--Part 1 of 2 - sortBy (1 - Date, 2 - Name, 3 - Country)
	public void sortLocationView(int sortBy) {
		ArrayList<HLocation> testList = new ArrayList<HLocation>();

		try {
			testList=new sortLocations().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,sortBy).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		if (!testList.isEmpty()) {
			locationList.clear();
			locationList.addAll(testList);
			notifyDataSetChanged();
		}			
	}

	//--Sort asynctask method-------------------------------------------------------------
	//--Part 2 of 2 - sortBy
	private class sortLocations extends AsyncTask<Integer, Void, ArrayList<HLocation>> {

		@Override
		protected ArrayList<HLocation> doInBackground(
				Integer... params) {
			int select=params[0];
			ArrayList<HLocation> tempHelp = new ArrayList<HLocation>();
			tempHelp.addAll(locationList);

			switch (select) {
				case 1:
					//--BY DATE---------------------------------------------------------------
					Collections.sort(tempHelp, new Comparator<HLocation>() {
						@Override
						public int compare(HLocation lhs, HLocation rhs) {
							int result=rhs.getLdate().toString().compareTo(lhs.getLdate().toString());
							if (result>0) {
								return 1;
							} else  if (result <0) {
								return -1;
							} else {
								return 0;
							}
						}
					});
					break;
				case 2:
					//--BY NAME---------------------------------------------------------------
					Collections.sort(tempHelp, new Comparator<HLocation>() {
						@Override
						public int compare(HLocation lhs, HLocation rhs) {
							int result=lhs.getName().toString().compareTo(rhs.getName().toString());
							if (result>0) {
								return 1;
							} else  if (result <0) {
								return -1;
							} else {
								return 0;
							}
						}
					});
					break;
				case 3:
					//--BY COUNTRY-------------------------------------------------------------
					Collections.sort(tempHelp, new Comparator<HLocation>() {
						@Override
						public int compare(HLocation lhs, HLocation rhs) {
							if (lhs.getCountry() != null && rhs.getCountry() !=null) {
								int result=lhs.getCountry().toString().compareTo(rhs.getCountry().toString());
								if (result>0) {
									return 1;
								} else  if (result <0) {
									return -1;
								} else {
									return 0;
								}
							} else {
								return 0;
							}
						}
					});
					break;
			}
			return tempHelp;
		}
	}
	
	//--SORT AND FILTERS--------END-----------------------------------------------------
	


	//=====SECTION 11==SANDBOX========================================	

	//=======NOT USE============================
	
	//--FILTER BY COUNTRY----------------------------------------------------------------------
	
	//--filtered by reference location country---------------------------------------------
	public void filterByRefCountry(HLocation location) {
		ArrayList<HLocation> tempHelp = new ArrayList<HLocation>();
		String refCountry=location.getCountry().toString();
		allLocationList = new ArrayList<HLocation>();
		allLocationList = getAllLocationList();		
		for (HLocation f :  allLocationList) {
			if (f.getCountry() !=null && f.getCountry().equals(refCountry)) {
				tempHelp.add(f);
			}
		}
		locationList=tempHelp;
		notifyDataSetChanged();
	}
	

	
	//--get a country list---------------NOT USE------------------------------
	public ArrayList<String> getCountryList() {
		ArrayList<String> listItems= new ArrayList<String>();
		ArrayList<String> helperList= new ArrayList<String>();
		
		for (HLocation l : locationList) {
			if (l.getCountry() != null || l.getCountry().toString().length()!=0) {
				helperList.add(l.getCountry().toString());
			}
		}
		
		//--interesting codes using hashset----------------------
		HashSet hs = new HashSet();
		hs.addAll(helperList);
		listItems.addAll(hs);
		
		return listItems;
	}

	//--filtered by holiday---------------------------------------------------------
	public void filterByHoliday(long holrowid) {
		ArrayList<HLocation> tempHelp = new ArrayList<HLocation>();
		allLocationList = new ArrayList<HLocation>();
		allLocationList = getAllLocationList();

		for (HLocation h : allLocationList) {
			if (h.getRefid()==holrowid) {
				tempHelp.add(h);
			}
		}
		locationList=tempHelp;
		notifyDataSetChanged();
	}

	//--setters and getters----------------------------------------------------------
	public void setHolidayFilter(boolean filter) {
		holidayFilter=filter;
	}

	public boolean getHolidayFilter () {
		return holidayFilter;
	}
	
	public void setHolRefId(long id) {
		holRefId=id;
	}
	
	public long getHolRefId () {
		return holRefId;
	}
	
	
	public ArrayList<HLocation> sortDate(ArrayList<HLocation> locList) {
		ArrayList<HLocation> tempHelp = new ArrayList<HLocation>();
		tempHelp=locList;
		
		tempHelp=locationList;
		Collections.sort(tempHelp, new Comparator<HLocation>() {

			@Override
			public int compare(HLocation lhs, HLocation rhs) {
				int result=rhs.getLdate().toString().compareTo(lhs.getLdate().toString());
				if (result>0) {
					return 1;
				} else  if (result <0) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		return tempHelp;	
	}
	
	public ArrayList<HLocation> sortName(ArrayList<HLocation> locList) {
		ArrayList<HLocation> tempHelp = new ArrayList<HLocation>();
		tempHelp=locList;
		
		tempHelp=locationList;
		Collections.sort(tempHelp, new Comparator<HLocation>() {

			@Override
			public int compare(HLocation lhs, HLocation rhs) {
				int result=lhs.getName().toString().compareTo(rhs.getName().toString());
				if (result>0) {
					return 1;
				} else  if (result <0) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		
		return tempHelp;	
	}

	public ArrayList<HLocation> sortCountry(ArrayList<HLocation> locList) {
		ArrayList<HLocation> tempHelp = new ArrayList<HLocation>();
		tempHelp=locList;
		
		tempHelp=locationList;
		Collections.sort(tempHelp, new Comparator<HLocation>() {

			@Override
			public int compare(HLocation lhs, HLocation rhs) {
				if (lhs.getCountry() != null && rhs.getCountry() !=null) {
					int result=lhs.getCountry().toString().compareTo(rhs.getCountry().toString());
					if (result>0) {
						return 1;
					} else  if (result <0) {
						return -1;
					} else {
						return 0;
					}
				} else {
					return 0;
				}
			}
		});
		
		return tempHelp;	
	}

}		//--END MAIN CLASS