/* 
 * Adapter:							HolidayListAdapter
 * Fragment: 						HolidayView.java
 * Description:						Custom List Adapter for Holiday List View
 * 											Provides holiday data feed to list view, helper methods for sort, add and delete
 * 
 * Layout item:					list_item.xml
 * 
 * Created: 							May 3, 2012
 * Last release:					September 9, 2012
 * Last updated: 					November 19, 2013
 * 
  * Changes:
 * 	- Tidy codes and documentation
 * - Bug fix to persist sort preference upon onResume()
 * - Remark out redundant codes
 * 
 */


package com.liseem.android.travel.adapter;

import static android.content.Context.MODE_PRIVATE;
import static com.liseem.android.travel.TravelLiteActivity.PREFNAME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;

import com.liseem.android.travel.PPGoPlacesApplication;
import com.liseem.android.travel.R;
import com.liseem.android.travel.items.AsyncTask;
import com.liseem.android.travel.items.Holiday;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HolidayListAdapter extends BaseAdapter {
	
	private static final String 			TAG="HolidayListAdapter";
	
	private LayoutInflater 					mInflater;
	private ArrayList<Holiday> 			holidayList		= new ArrayList<Holiday>();
	private ArrayList<Holiday> 			allHolidayList	= new ArrayList<Holiday>();
	private ArrayList<String> 			listItems;
	private Context 								context;

	private PPGoPlacesApplication 	app;
	private SharedPreferences 			prefs;
	private boolean 							scrollTextMode;
	private int										sortHolidayList;

	//=====1.0 CONSTRUCTOR==========================================START====
	
	//--Constructor------------------------------------------------------------------------------- 
	public HolidayListAdapter (Context context, ArrayList<Holiday> listOfTrips) {
		
		this.context		= context;
		setAllHolidayList(listOfTrips);													//--master list
		holidayList.addAll(listOfTrips);												//--list for feeding to adapter
		
		//allHolidayList		= listOfTrips;												//--master list
		//holidayList.addAll(allHolidayList);											//--list for feeding to adapter

		
		mInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		//--setup path to application------------------------------------------------
		app						= (PPGoPlacesApplication)context.getApplicationContext();

		//--shared preferences-----------------------------------------------------------------
		prefs					= app.getSharedPreferences (PREFNAME, MODE_PRIVATE);
		scrollTextMode	= prefs.getBoolean("scrollTextMode", true);
		sortHolidayList	= prefs.getInt("sortHolidayList", 0);			//--retrieve user sort preference
		
		if (sortHolidayList > 0) {														//--sort list if preference available						
			sortHolidayView(sortHolidayList);
		} 
	}

	//=====2.0 ADAPTER ABSTRACT METHODS ===============================START======

	@Override
	public int getCount() {
		return (holidayList != null) ? holidayList.size() : 0;
	}

	@Override
	public Object getItem(int position) {		
		return (null==holidayList) ? null : holidayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return (null==holidayList) ? null : holidayList.get(position).getId();
	}
	
	//--Get View---------------------------------------------------------------------------------------- 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView==null) {													//--if no view, create view
			holder				= new ViewHolder();
			convertView	= mInflater.inflate(R.layout.list_item, null);
			holder.text		= (TextView)convertView.findViewById(R.id.itemName);
			holder.text.setSelected(scrollTextMode);						//--for scrolling text view marquee to work
			convertView.setTag(holder);
		} else {
			holder				= (ViewHolder)convertView.getTag();	//--else recycle view
		}
		holder.text.setText(holidayList.get(position).getHoliday());
		return (convertView);
	}
	
	//--View Holder------------------------------------------------------------------------------------- 
	//--View Holder, add appropriate name for new view type, e.g. CheckBoxText checktext--
	public static class ViewHolder {
		TextView text;
		//TextView timestamp;
		//ImageView icon;
		//ProgressBar progress;
		//int position;
	}	
	
	//=====3.0 ADAPTER HELPER METHODS ================================START======

	//=========Adapter List setter, getter, add, change and delete Methods======================


	//--setters for master list with new arraylist allHolidays
	public void setAllHolidayList (ArrayList<Holiday> newHolidayList) {
		allHolidayList 			= new ArrayList<Holiday>();				//--reset master list
		allHolidayList 			= newHolidayList;									//--set master to new holiday list
	}
	
	//--getter to trieve master list
	public ArrayList<Holiday> getAllHolidayList() {
		return allHolidayList;
	}
	
	//--CRUB-----------------------------------------------------------------------------
	//--update holiday----------------------------------------
	public void updateHoliday(Holiday newHoliday, Holiday oldHoliday) {
		
		//--update master list, remove old holiday insert new holiday-------------------
		ArrayList<Holiday> tempHelp = new ArrayList<Holiday>();
		tempHelp = getAllHolidayList();
		tempHelp.remove(oldHoliday);
		tempHelp.add(newHoliday);
		setAllHolidayList(tempHelp);														//--setter update master list
		
		//--update adapter list-------------------
		holidayList.remove(oldHoliday);
		holidayList.add(newHoliday);
		notifyDataSetChanged();
	}
	
	//--add holiday-------------------------------------------
	public void addHoliday(Holiday newHoliday) {
		
		ArrayList<Holiday> tempHelp = new ArrayList<Holiday>();
		tempHelp = getAllHolidayList();
		tempHelp.add(newHoliday);
		setAllHolidayList(tempHelp);														//--setter update master list

		holidayList.add(newHoliday);
		notifyDataSetChanged();
	}
	
	//--delete holiday----------------------------------------
	public void deleteHoliday(Holiday oldHoliday) {
		
		ArrayList<Holiday> tempHelp = new ArrayList<Holiday>();
		tempHelp = getAllHolidayList();
		tempHelp.remove(oldHoliday);
		setAllHolidayList(tempHelp);														//--setter update master list

		holidayList.remove(oldHoliday);
		notifyDataSetChanged();
	}
	
	
	//=========Adapter sorts and filters Methods=============================	
	
	//--reset filter or remove sort--------------------------------------
	public void resetHolidayFilter() {
		
		ArrayList<Holiday> tempHelp = new ArrayList<Holiday>();
		tempHelp = getAllHolidayList();

		holidayList.clear();
		holidayList.addAll(tempHelp); 
		notifyDataSetChanged();
	}
	
	//--sort by method call from viewHoliday-------------
	//--Part 1 of 2 - sort holiday list (1 - by Start Date, 2 - by Name)
	public void sortHolidayView(int sortBy) {
		ArrayList<Holiday> testList = new ArrayList<Holiday>();
		
		try {
			testList = new sortHolidays().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, sortBy).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		if (!testList.isEmpty()) {
			holidayList=testList;
			notifyDataSetChanged();
		}
	}
	
	//--Sort asynctask method-------------------------------------------------------------
	//--Part 2 of 2 - background process to sort list, integer identify 1=by Date, 2=by Name  
	private class sortHolidays extends AsyncTask<Integer, Void, ArrayList<Holiday>> {

		@Override
		protected ArrayList<Holiday> doInBackground(
				Integer... params) {
			int select=params[0];
			ArrayList<Holiday> tempHelp = new ArrayList<Holiday>();
			tempHelp=holidayList;

			switch (select) {
				case 1:
					//--sort by start date---------------------------------------------------------------
					Collections.sort(tempHelp, new Comparator<Holiday>() {
						@Override
						public int compare(Holiday lhs, Holiday rhs) {
							int result=rhs.getStart_date().toString().compareTo(lhs.getStart_date().toString());
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
					//--sort by name---------------------------------------------------------------
					Collections.sort(tempHelp, new Comparator<Holiday>() {
						@Override
						public int compare(Holiday lhs, Holiday rhs) {
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
				case 3:			//--option not use---------------------------------------------------
					//--sort by country-------------------------------------------------------------
					Collections.sort(tempHelp, new Comparator<Holiday>() {
						@Override
						public int compare(Holiday lhs, Holiday rhs) {
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
			return tempHelp;									//--return sorted holiday list
		}
	}

}		//--END MAIN CLASS
