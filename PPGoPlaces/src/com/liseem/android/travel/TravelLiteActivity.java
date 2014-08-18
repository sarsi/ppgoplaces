/*
 * last version: v1.0 build 153
 * last updated:
 * 
 * changes:
 * 
 * 
 * PPGoPlaces main helper class, contains common activity shared
 * methods for date rendering, date textview update, etc.
 * 
 * 
 * Resources registers:
 * dropdown.xml spinner item view used by all spinners
 * 
 * 
 */
package com.liseem.android.travel;

import static com.liseem.android.travel.TravelLiteActivity.TIME_LAPSE;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import com.liseem.android.travel.items.HLocation;
import com.liseem.android.travel.items.Holiday;
import com.liseem.android.travel.items.LPicture;
import com.liseem.android.travel.items.PhotoOption;
import com.liseem.android.travel.items.MyDate;
import com.liseem.android.travel.items.TaskList;
import com.liseem.android.travel.items.TravelLiteDBAdapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class TravelLiteActivity extends Activity implements Serializable {
	
	//=====SECTION 1==DECLARATION===================================

	private static final String TAG ="TravelLitActivity";
	
	//--shared preferences----------------------------------------
	public static final String 	PREFNAME					="ppPrefs";			//--shared preferences name 
	public static final String 	COUNTRY					="country";			//--shared preferences country code table
	public static final String	CHECKLIST					="checklist";		//--shared preferences check list category		
	public static final String 	PPGP_CACHE				="Android/data/com.liseem.android.travel/ppgoplaces/cache";
	public static final String 	FOTOLIST						="fotolist";
	public static final String 	DEFAULT_SIGNATURE	="sent from \"PPGoPlaces\"";  //--cannot use strings.xml

	//==SHARED INTENT BUNDLE EXTRA CONSTANTS===================
	public static final int		LOCATION_VIEW			=8;
	public static final int 		LOCATION_EDIT			=9;	
	public static final int 		LOCATION_ADD			=10;		
	public static final int		MAP_LOCATION			=11;								//--Bundle from MapLocation for LocationAdd AddCode
	public static final int		TAG_PICTURE				=12;								//--Picture Gallery Tag Photo, intentChooser
	
	public static final int		HOLIDAY_VIEW 			=21;								//--Bundle from HolidayView for LocationAdd AddCode	
	public static final int 		HOLIDAY_EDIT				=22;	
	public static final int 		HOLIDAY_ADD				=23;	
	
	public static final int 		SHOPPING_EDIT			=31;	
	public static final int 		SHOPPING_ADD			=32;	
	public static final int 		TASK_EDIT					=36;	
	public static final int 		TASK_ADD					=37;	

	public static final String	REQUESTOR				="requesterCode";		//--use for mapLocation to identify requester
	public static final String	ADD_CODE					= "addCode";				//--Add Code for LocationAdd bundle
	public static final String	NEW_ADDRESS			=	"newAddress"; 		//--New address from Map Location	
	public static final String 	HAS_HOLIDAY				="hasHolidayInfo";	
	public static final String 	FILTER_LOCATION		="filter_locations";	
	public static final String 	CANCEL						="Cancel Action";
	public static final String 	HOMEMODE					="Home, General";
	public static final String	HAS_LOCATION			="locationAvailable";	//--bundle mapLocation
	public static final String	FIND_LOCATION			="locationAddress";	//--bundle to search address
	public static final String	HOLIDAY_ALBUM			="holidayAlbum";		//--bundle holiday album true or false 
	

	private static final int 		READ_BLOCK_SIZE		=100;							//--file input buffer
	private static final int 		IO_BUFFER_SIZE			=4*1024;						//--file copy action 
	
	//--use by isBetterLocation
	protected static final int TIME_LAPSE				=120000;						//--two minutes 1000*60*2 milliseconds
	
	public static final String 	MAIN_MENU_1				="View Holidays";
	public static final String 	MAIN_MENU_2				="View Locations";
	public static final String 	MAIN_MENU_3				="View CheckList";
	public static final String 	MAIN_MENU_4				="View Shopping List";
	public static final String 	MAIN_MENU_5				="View Pictures";
	public static final String 	MAIN_MENU_6				="Future expansion";
	
	public static final int 		LAST_CAT					=5;						//--use by TaskListAdapter

	public static final String TASK_MENU_1				="Pre Holiday";
	public static final String TASK_MENU_2				="At The Last Minute";	
	public static final String TASK_MENU_3				="While On Holiday";
	public static final String TASK_MENU_4				="Post Holiday";
	public static final String TASK_MENU_5				="Show All Tasks";
	public static final String TASK_MENU_8				="Packing List";

	public static final String SHOP_CAT_1					="Food and Grocery";
	public static final String SHOP_CAT_2					="Apparel and Accessories";	
	public static final String SHOP_CAT_3					="Gifts and Souvenir";
	public static final String SHOP_CAT_4					="Health and Beauty";
	public static final String SHOP_CAT_5					="Sporting Goods";
	public static final String SHOP_CAT_6					="Electronics, Books and Media";
	public static final String SHOP_CAT_7					="Home and Garden";
	public static final String SHOP_CAT_8					="Toys";
		
	public static final String ADD_LOCATION			="Add New Location";
	public static final String EDIT_LOCATION			="Edit Location";
	public static final String DELETE_LOCATION		="Delete Location";
	
	public static final String ADD_HOLIDAY				="Add New Holiday";
	public static final String EDIT_HOLIDAY				="View/Edit Holiday";
	public static final String DELETE_HOLIDAY			="Delete Holiday";
	
	public static final String ADD_Task						="Add New Task";
	public static final String EDIT_TASK					="Edit Task";
	public static final String DELETE_TASK				="Delete Task";
	public static final String DELETE_TASKS				="Delete Completed Tasks";
	
	public static final String ADD_ITEM						="Add New Item";
	public static final String EDIT_ITEM						="Edit Item";
	public static final String DELETE_ITEM				="Delete Item";
	public static final String DELETE_ITEMS				="Delete Completed Items";
	
	//==SHARED MENU====================================
	private static final int MENU_ITEM_1 					= 0;
	private static final int MENU_ITEM_2 					= 1;
	private static final int MENU_ITEM_3 					= 2;
	private static final int MENU_ITEM_4 					= 3;
	
	private static Criteria coarse;
	private static Criteria fine;
	private Calendar calDate;
	private Date dateDate;
	private String strDate; //date String in ISO8601 YYY-MM-DD format
	private SimpleDateFormat sdfDate;
	private StringBuilder strBuilder;
	private String dbCountry, ctry;
	private AlertDialog dateOutOfRangeDialog;
	private Dialog loadDefaultList;
	private int holRefid;
	private ArrayList<Holiday> holidayList;
	private ArrayList<String> listItems;
	private ArrayList<Integer> questFRP;
	private TravelLiteDBAdapter dbHelper;
	private Location currentLocation;
	private LocationManager lm;
	private String providerFine;
	private String providerCoarse;
	protected String deviceName;
	
	//--shared preferences-------------------------
	private SharedPreferences prefs;
	public PPGoPlacesApplication app;
	private Context context;
	

	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================
	
	public TravelLiteActivity() {
		super();
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return null;
	}
	
	/*============================================================================
	 * Routine return full path of application not from calling activity
	 *============================================================================
	 */
	public PPGoPlacesApplication getApplicationPath() {
		return (PPGoPlacesApplication)getApplication();
	}
	// Common loadlist sequence shared by multiple activity

	
	@Override
	protected void onResume() {
		super.onResume();
		
		//--redundant can be deleted as it is already in setFirstRun-----------------
		deviceName=android.os.Build.MANUFACTURER;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	
	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 3==MATHS AND LOGIC HELPERS===========================

	//--trip calculator compute helper for calculating fx or tip amount to local-----------
	//--one unit of foreign currency to one unit of home currency
	
	public String for2home (String strAmount, String strRate) {
		    String aString = strAmount;
		    String mString = strRate;
		    Log.d(TAG, "254 for2home strAmount: "+aString.toString()+"  strRate: "+mString.toString());
		    BigDecimal holAmount=new BigDecimal (strAmount);
		    BigDecimal haveRate=new BigDecimal (strRate);
		    
		    BigDecimal homeAmount=holAmount.multiply(haveRate);
		    
		    double d=homeAmount.doubleValue();
		    DecimalFormat fmt = new DecimalFormat("0.00");  
		    String homeStrAmount= fmt.format(d);  
		   
		   //String homeStrAmount = homeAmount.toString();
		    return homeStrAmount;
	}
	
	//--return string array with tip amont [0] and amount plus tip [1]----
	public String[] tipCompute (String strAmount, String tipPercent ) {
		
		String[] finalAmount		= new String[2];
	    Log.d(TAG, "275 add2String");
	    BigDecimal holAmount	= new BigDecimal (strAmount);
	    BigDecimal haveRate		= new BigDecimal (tipPercent);
	    
	    BigDecimal tipAmount	= holAmount.multiply(haveRate);
	    BigDecimal amtPlusTip	= holAmount.add(tipAmount);
	    
	    double d							= tipAmount.doubleValue();
	    double t							= amtPlusTip.doubleValue();
	    DecimalFormat fmt 		= new DecimalFormat("0.00");  
	    finalAmount[0]				= fmt.format(d);  
	    finalAmount[1]				= fmt.format(t);
		
		return finalAmount;
		
	}

	//--rate for home to 1 unit of foreign, i.e. to get $0.2 to 1rmb
	public String divideByRate (String strAmount, String strRate) {
	    String aString = strAmount;
	    String mString = strRate;
	    BigDecimal holAmount=new BigDecimal (strAmount);
	    BigDecimal haveRate=new BigDecimal (strRate);
	    BigDecimal homeAmount=holAmount.divide(haveRate);
	    
	    double d=homeAmount.doubleValue();
	    DecimalFormat fmt = new DecimalFormat("0.00");  
	    String homeStrAmount= fmt.format(d);  
	    
	   //String homeStrAmount = homeAmount.toString();
	    return homeStrAmount;
	}
	
	//--test for whether string input is of numeric value for computing---------------------
	public boolean testForNumeric (String strAmount) {
			//BigDecimal haveAmount=new BigDecimal(strAmount);
			//return haveAmount.stripTrailingZeros().scale() <=0;		
			try {
				Integer.parseInt(strAmount);
			} catch (NumberFormatException e) {
				return false;
			}
			return true;
	}
	
	//--return numeric value from string input------------------------------------------
	public int getNumber (String strAmount) {
		
		try {
			return Integer.parseInt(strAmount);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
		
	//=====SECTION 3==MATHS AND LOGIC HELPERS===========================

	//=====SECTION 4==DATE CONVERSION HELPERS===========================
	/*
	 * ============================================================================
	 * DATE CONVERSION from STRING TO CALENDAR, CALENDAR TO ISO8601 STRING 
	 * 
	 * Tough Nuts (Die here) -- only below follow works. 
	 * first set proper format for simpleDateFormat, from "simpleDateFormat" parse
	 * to "DATE" format and finally to "Calendar" via setTime method of Calendar
	 * STRING need to be saved as ISO8601 strings ("YYYY-MM-DD HH:MM:SS.SSS")
	 * ============================================================================
	 */
	public Calendar str2Calendar(String dbdate) {
		calDate=Calendar.getInstance(TimeZone.getDefault(),Locale.getDefault());
		strDate=dbdate;
		sdfDate=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		
		try {
			dateDate=sdfDate.parse(strDate); //just changed from dbdate to strDate
			calDate.setTime(dateDate);
		}catch (ParseException e) {
			e.printStackTrace();
		}
		return calDate;
	}
	

	//--Convert Calendar to StringBuilder--
	public StringBuilder cal2String(Calendar dbcal) {
		strBuilder=new StringBuilder()
				.append(dbcal.get(Calendar.YEAR)).append("-")
		        // month is 0 base plus 1 to get correct month
				.append((dbcal.get(Calendar.MONTH))+1).append("-")
		        .append(dbcal.get(Calendar.DAY_OF_MONTH)).append(" ");
		return strBuilder;
	}
	
	//--format from String to DD-MMM-YYYY String display
	//i.e. 02-27-2012 to 27-Feb-2012--
	//EXTREMELY COMPLEX CONVERSION, parse here and there
	public String displayDMY(String sdate) {
		/*try {
			dateDate = new SimpleDateFormat("yyyy-MM-dd").parse(sdate);
		} catch (ParseException e) {
			Log.d(TAG, "displayDMY() died at SimpleDateFormat conversion ");
			e.printStackTrace();
		}
		calDate=Calendar.getInstance();
		calDate.setTime(dateDate);*/
		calDate=str2Calendar(sdate);
		sdfDate=new SimpleDateFormat("dd-MMM-yyyy");
		strDate=sdfDate.format(calDate.getTime());
		return strDate;
	}
	
	//--need a short one for add location-----------------------
	public String displayShortDMY(String sdate) {
		/*try {
			dateDate = new SimpleDateFormat("yyyy-MM-dd").parse(sdate);
		} catch (ParseException e) {
			Log.d(TAG, "displayDMY() died at SimpleDateFormat conversion ");
			e.printStackTrace();
		}
		calDate=Calendar.getInstance();
		calDate.setTime(dateDate);*/
		calDate=str2Calendar(sdate);
		sdfDate=new SimpleDateFormat("dd/MM/yy");
		strDate=sdfDate.format(calDate.getTime());
		return strDate;
	}
	
	
	
	//--if early is later than late cal then return false, else true
	//--first date suppose to be earlier or equal than the second date--
	//--if true return true lah, else false lah--
	protected Boolean cal2Compare(Calendar start, Calendar end) {
		String strEarly=cal2String(start).toString();	
		String strLate=cal2String(end).toString();
		Boolean 	result=false;
		Date 		dEarly=null,
						dLate = null;
		try {
			dEarly = new SimpleDateFormat("yyyy-MM-dd").parse(strEarly.toString());
			dLate = new SimpleDateFormat("yyyy-MM-dd").parse(strLate.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if (dLate.after(dEarly) || (dLate.equals(dEarly))) {
			result=true;
		}
		return result;
	}
	
	//-- Alert Dialog box for out of range date--
	protected void dateOutOfRange() {
		dateOutOfRangeDialog=new AlertDialog.Builder(this)
		.setTitle(R.string.date_mismatch)
		.setMessage(R.string.date_must_be_within_holiday_period)
		.setPositiveButton(R.string.OK,new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				dateOutOfRangeDialog.cancel();				
			}			
		})
		.create();
		dateOutOfRangeDialog.show();
	}
	
	
	//-- Return TRUE if testDate is after holStart AND before holEnd
	//-- use LocationEdit 838 (change date) and 733 (update location)

	protected boolean dateWithinRange(MyDate start, MyDate within, MyDate end) {	
		Calendar startDate=start.getCalDate();
		Calendar testDate=within.getCalDate();
		Calendar endDate=end.getCalDate();
		
		//--testDate must be within holStart and holEnd
		Boolean compareStart=cal2Compare(startDate,testDate);  //early first
		Boolean compareEnd=cal2Compare(testDate,endDate);		

		if (compareStart && compareEnd) {
			return true;
		} else {
			return false;
		}

	}

	//--simple routine to compute number days between two calendar dates-------------------------
	public static int daysBetween(Calendar start, Calendar end) {
		  int result = 0;
		  Calendar c = (Calendar) start.clone();
		  while (c.before(end)) {
		    c.add(Calendar.DAY_OF_MONTH, 1);
		    result++;
		  }
		  return result;
	}
	
	
	/*
	 * ============================================================================
	 * SETUP DATE TEXTVIEW 
	 * 
	 * Take calendar into string buffer and display on Date TextView
	 * 
	 * ============================================================================
	 */ 

	protected void setCalView(TextView view, String dTitle, Calendar cDate) {
		strBuilder=new StringBuilder().append(dTitle+"\n")
		        // month is 0 base plus 1 to get correct month
		        .append(cDate.get(Calendar.DAY_OF_MONTH)).append("/")
		        .append((cDate.get(Calendar.MONTH))+1).append("/")
		        .append(cDate.get(Calendar.YEAR)).append("");   
		view.setText(strBuilder);
	}
	
	/*public void setDMY(TextView view, String dTitle, String sdate) {
		
		calDate=str2Calendar(sdate);
		sdfDate=new SimpleDateFormat("dd-MMM-yyyy");
		strDate=sdfDate.format(calDate.getTime());
		view.setText(dTitle+"\n"+strDate);
	} */
	
	protected void setCalDMY(TextView view, String dTitle, Calendar cdate) {
	
		//--from string to dd-MMM-yyyy format--
		//calDate=str2Calendar(strBuilder.toString());
		sdfDate=new SimpleDateFormat("dd-MMM-yyyy");
		strDate=sdfDate.format(cdate.getTime());
		
		//--display on date text view--
		view.setText(dTitle+"\n"+strDate);		
	}
	
	//--Display today date--------------------------------
	//--use by PPGoPlaces, LocationAdd and LocationEdit
	public void displayTodayDate(TextView view, MyDate date) {
		//Calendar toDate=Calendar.getInstance();
		MyDate myDate=new MyDate();		//initialize lDate
		myDate=date;
		StringBuilder strBuilder=new StringBuilder().append(myDate.getDMYDate());
		view.setText(strBuilder.toString());
	}
	
	//--Display today date--------------------------------
	public String getTodayDate() {
		Calendar todayDate=Calendar.getInstance();
		MyDate myDate=new MyDate();		//initialize lDate
		myDate.setCalDate(todayDate);
		StringBuilder strBuilder=new StringBuilder().append(myDate.getDMYDate());
		return strBuilder.toString();
	}
	
	
	//--Get today date in String for setters------------
	public String setTodayDate() {
		Calendar todayDate=Calendar.getInstance();
		MyDate myDate=new MyDate();		//initialize lDate
		myDate.setCalDate(todayDate);
		return myDate.getStrDate();
	}

	//--retrieve current holiday base on today date--------------
	//--Part 1 of 2
	public Holiday awayOnHoliday() {
		
		MyDate todayDate=new MyDate();
		Calendar calDate=Calendar.getInstance();
		todayDate.setCalDate(calDate);
		String thisDate=todayDate.getDMYDate();
		Holiday currentHoliday=new Holiday();
		try {
			currentHoliday=new getThisHoliday().execute(thisDate).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
		
		if (currentHoliday !=null) {
			return currentHoliday;
		} else {
			return null;
		}		
	}
	//--Part 2 of 2
	private class getThisHoliday extends AsyncTask<String, Void, Holiday> {

		@Override
		protected Holiday doInBackground(String... params) {
			String today=params[0];
			ArrayList<Holiday> holidayList=new ArrayList<Holiday>();
			ArrayList<Holiday> tempHelp=new ArrayList<Holiday>();
			holidayList=app.loadHolidays();
			if (!holidayList.isEmpty()) {
				for (Holiday h : holidayList) {
					int afterStart=today.compareTo(h.getStart_date().toString());
					if (afterStart >= 0) {
						int beforeEnd=today.compareTo(h.getEnd_date().toString());
						if (beforeEnd <= 0) {
							return h;
						}
					}
				}
				return null;
			} else {
				return null;
			}
		}
	}
	
	
	//=====SECTION 4==DATE CONVERSION HELPERS===========================

	
	//=====SECTION 5==STRING AND ARRAY HELPERS===========================
	
	//--Convert Address object to String, and return null if all field is empty--
	//--use to return address to address string for location add/edit address field---------
	//--IF MODIFY THIS NEED TO DO THE SAME FOR ONE IN 4.0 PPGoPlacesApplication TOO-----
	protected String add2String(Address tempAdd) {
		boolean gotCountry=false;
		boolean gotPostal=false;
		StringBuilder useAddress=new StringBuilder();
		if (tempAdd.getFeatureName() !=null && tempAdd.getSubThoroughfare() !=null)	{		//e.g. Villa Rosa
			if (!tempAdd.getFeatureName().toString().
					equalsIgnoreCase(tempAdd.getSubThoroughfare().toString())) {
				useAddress.append(tempAdd.getFeatureName().toString()).append("\n");	
			}
		}

		if (tempAdd.getSubThoroughfare() !=null)	//e.g. 127
			useAddress.append(tempAdd.getSubThoroughfare().toString()).append(" ");		
		if (tempAdd.getThoroughfare() !=null)			//e.g. Via Cristoforo Colombo
			useAddress.append(tempAdd.getThoroughfare().toString()).append("\n");		
		if (tempAdd.getLocality() !=null)					//e.g. Positano
			useAddress.append(tempAdd.getLocality().toString()).append("\n");
		if (tempAdd.getCountryName() != null) {
			useAddress.append(tempAdd.getCountryName().toString()).append(" ");
			gotCountry=true; }
		if (tempAdd.getPostalCode() !=null)	{			//e.g. 84017
			useAddress.append(tempAdd.getPostalCode());	
			gotPostal=true; }
		//if (gotCountry && gotPostal) {
		if(useAddress !=null) {
			return useAddress.toString();
		} else {
			return null;
		}
	}

	
	//--Convert String array to ArrayList--
	protected static ArrayList<String> arrayListFromString(String string) {
		ArrayList<String> itemList=new ArrayList<String>();
        String[] strings = string.replace("[", "").replace("]", "").split(", ");
        //int result[] = new int[strings.length];
        for (int i = 0; i < strings.length; i++) {
                itemList.add(strings[i].toString());
        }
        return itemList;
	}
	
	protected static ArrayList<String> simpleStr2ArrayList(String string, String delimiter) {
		ArrayList<String> itemList=new ArrayList<String>();
        String[] strings = string.split(delimiter);
        
        int test=string.indexOf(delimiter, 1);
        
        if (test<0) {
        	itemList.add(string.trim());
        } else {
        
	        //int result[] = new int[strings.length];
	        for (int i = 0; i < strings.length; i++) {
	                itemList.add(strings[i].toString());
	        }
        }
        return itemList;
	}
	
	
	//-- Convert arrayList to String with delimiter---------
	protected static String array2String(ArrayList<String> arrayStr, String delimiter) {
		StringBuilder arToStr=new StringBuilder();

			for (int i=0; i<arrayStr.size(); i++) {
				arToStr.append(arrayStr.get(i).toString());
				if(i<(arrayStr.size()-1)) {
					arToStr.append(delimiter);
				}
		}
		return arToStr.toString();		
	}
	
	//--Convert string with delimiter to integer Array--
	//-- return a ArrayList<Integer>  e.g. "," delimiter
	protected static ArrayList<Integer> str2IntArray(String string, String delimiter) {
		ArrayList<Integer> numList=new ArrayList<Integer>();
        String[] strings = string.split(delimiter);
        
        int test=string.indexOf(delimiter, 1);
        
        for (String s: strings) {
        	int n=Integer.parseInt(s);
        	numList.add(n);
        }
        return numList;
	}	
	
	protected static String intArray2String(ArrayList<Integer> arrayStr, String delimiter) {
		StringBuilder arToStr=new StringBuilder();

		for (int i=0; i<arrayStr.size(); i++) {
			arToStr.append(arrayStr.get(i).toString());
			if(i<(arrayStr.size()-1)) {
				arToStr.append(delimiter);
			}
		}
		return arToStr.toString();		
	}
		
	//=====SECTION 5==STRING AND ARRAY HELPERS===========================

	//=====SECTION 6==TOAST AND ALERT DIALOG HELPERS=======================
	//--quick alert dialog with short text--------------------------------
	public void showOkAlertDialog(CharSequence message)
	{
		new AlertDialog.Builder(this)
		.setIcon(R.drawable.ppgp_pt)
		.setTitle("PPGoPlaces")
		.setMessage(message)
		.setPositiveButton("OK", null)
		.show();
	}
	
	//--show toast message--------------------------------------------
	public void makeShortToast(CharSequence message)
	{
		Toast.makeText(
		this,
		message,
		Toast.LENGTH_SHORT).show();
	}
	
	//--show toast message--------------------------------------------
	public void makeLongToast(CharSequence message)
	{
		Toast.makeText(
				this,
				message,
				Toast.LENGTH_LONG).show();
	}
	
	//--Log.d message-------------------------------------------------
	public void makeLog(String message)
	{
		Log.d(
				TAG,
				message);
	}
	
	//--update transaction before backup-------------------------
	public void updateTransB4bup() {
		prefs=getSharedPreferences (PREFNAME, MODE_PRIVATE);
		int thisTrans=prefs.getInt("transB4bup",0);

		thisTrans=thisTrans+1;
		SharedPreferences.Editor editor=prefs.edit();
		editor.putInt("transB4bup", thisTrans);
		editor.commit();
	}
	
	//=====SECTION 6==TOAST AND ALERT DIALOG HELPERS=======================

	//=====SECTION 7==FILE, DB AND OBJECT HELPERS===========================
	
	//=====4.0 FILE WRITE AND READ TO ARRAYLIST====v1.2 replaced by read and write async in holidaycheclist.java========================START=====
	/*
	 * This is first of THREE PARTS:
	 * Part 1 - This method write the tasks list to String file with comma delimited no quotes.
	 * Part 2 - Write method output a text file to data/data/com.liseem...travel/files.
	 * Part 3 - Read in text file with delimited back to String with comma delimited. 
	 *            - conversion to ArrayList<TaskList> and replace db is done in HolidayCheckList.java
	 * Part 4 - Copy text file from res/raw to create first user defined checklist template
	 * 			  - method CopyFileStream() for copy inputStream to outputStream for file copy.
	 * 
	 * Specific methods build to return a String delimited by "," for writing to external text file.
	 * from BAAD example 213
	 */
	
	//--PART I-----------------------output db to String with delimiter-----------------------------------------------
	protected String taskListDB2String() {
		
		PPGoPlacesApplication app=getApplicationPath();
		
		//--retrieve all checklist tasks from db----------------------------------------
		ArrayList<TaskList> readTasks= new ArrayList<TaskList>();
		readTasks=app.loadAllTasks();	
		
		StringBuilder strBuilder;
		strBuilder= new StringBuilder();
		int tt=readTasks.size();
		
		//--pass all tasks to String builder and delimited with comma----------
		for (int i=0; i<tt; i++) {
			strBuilder.append(readTasks.get(i).getCat()).append(",")
						.append(readTasks.get(i).getName().toString());
				if (i < (tt-1)) {
					strBuilder.append(",");
				}
		}
		String returnString= strBuilder.toString();
		return returnString;
	} 
	

	//--PART 2------------------write String to text file-------------------------------------------------------------
	//-- method take two parameters, first the string that contain output from part I , i.e. all the 
	//-- tasks in delimited, the second para is the first name to write as
	//
	protected void write2TextFile (String delimitedString, String outputFileName) {
		
		String outString=delimitedString;
		String fileName=outputFileName;
		
		//--get external backup directory, similar to db backup per MyBackupTask.java--------
		/*File exportDir = new File(Environment.getExternalStorageDirectory(), "PPGoPlacesBackups");
				Log.d(TAG, "file directory external directory "+exportDir.getAbsolutePath().toString());
				
		//--backup to directory not found, create one----------------------------
		if (!exportDir.exists()) {
			exportDir.mkdirs();
		}
				
		File file = new File(exportDir, fileName);*/
		
		try
		{
			//FileOutputStream fout=new FileOutputStream(file);
			FileOutputStream fout=openFileOutput (fileName, MODE_WORLD_READABLE);

			OutputStreamWriter osw=new OutputStreamWriter(fout);
			osw.write(outString);
			osw.flush();
			osw.close();
			Toast.makeText(getBaseContext(), "File saved successfully!", Toast.LENGTH_SHORT).show();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//--PART 3------------------read text file with delimited into ArrayList----------------------------------------
	//--method need the file name of the text file with comma delimited, if no
	//
	//--read file back to string, must provide the same file name as writeStringToFile

 	protected String read2DelimitedStr (String inputFileName) {
	
		String delimitedString="";
		String fileName=inputFileName;
		
		FileInputStream fIn;
		try {
			fIn = openFileInput(fileName);
			InputStreamReader isr=new InputStreamReader (fIn);
			
			char[] inputBuffer=new char[READ_BLOCK_SIZE];
			
			int charRead;
			while ((charRead=isr.read(inputBuffer))>0)
			{
				//---convert the chars to a String------
				String readString=String.copyValueOf(inputBuffer,0, charRead);
				delimitedString += readString;
				
				inputBuffer=new char[READ_BLOCK_SIZE];
			}
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return delimitedString;
	}		//END read2DelimitedStr
	
	//--PART 4------------------copy text file from res/raw to user default---------------------------------
	//--method copy either his or her checklist from res/raw to getFilesDir() once
	//--during HolidayCheckList first time access, i.e. prefs getBoolean firstCheckIn=true 
	//
	//--create first user default checklist from res/raw to template, first run--------
	protected void createFirstUserDefault(boolean hers, String outputFileName) {
		
		String delimitedString="";
		String fileName=outputFileName;
		
		//--open rawherchecklist.txt file for inputstream for copying to first default packing list--
		InputStream is;
		if (hers) {
			is=this.getResources().openRawResource(R.raw.herchecklist);
		} else {
			is=this.getResources().openRawResource(R.raw.hischecklist);
		}
		
		//--setup output file for default checklist to /data/data/com.liseem..travel/files directory----
		//String destPath=context.getFilesDir().toString()+"/"+fileName;
		//File outF = new File (context.getFilesDir(), fileName);
		//CopyFile(is, new FileOutputStream(destPath));
		
		byte[] b=new byte[IO_BUFFER_SIZE];
		
		//--setup output stream to accept byte stream from input stream, 
		//--and saved first default userchecklist.txt file
		OutputStream out = null;
		int read;
		try {
			FileOutputStream fout=openFileOutput (fileName, MODE_WORLD_READABLE);
			out = new BufferedOutputStream(fout);
			CopyFileStream(is, out);		//see method directly below
			
			Toast.makeText(getBaseContext(), "File saved successfully!", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//--copy routine from file via input stream to new file via output stream-------------------
	public void CopyFileStream(InputStream is, OutputStream os) throws IOException {
		
		//--copy 1024 bytes at a time------------------------------
		byte[] buffer=new byte[1024];
		int length;
		while ((length=is.read(buffer)) > 0) {
			os.write(buffer, 0, length);
		}
		is.close();
		os.close();
	}
	
	
	//--NOT USE CAN BE DELETED--------------------------------
	//--2 in 1 text file to string delimted and then to arraylist---
	protected  ArrayList<String> readFile2Array (String inputFileName) {
		String inputName=inputFileName;
		ArrayList<String> itemList=new ArrayList<String>();
		String str="";
		
		try
		{
			FileInputStream fin=openFileInput (inputName);
			InputStreamReader isr=new InputStreamReader(fin);
			
			char[] inputBuffer=new char[READ_BLOCK_SIZE];
	
			int charRead;
			while ((charRead=isr.read(inputBuffer))>0)
			{
				//---convert the chars to a String------
				String readString=String.copyValueOf(inputBuffer,0, charRead);
				str += readString;
				
				inputBuffer=new char[READ_BLOCK_SIZE];
			}


		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		//--here String str is split into string array
        String[] strings = str.split(",");
        
        int locDel=str.indexOf(",", 1);
        
        if (locDel<0) {
        		itemList.add(str.trim());		//only 1 item no delimiter
        } else {
	        for (int i = 0; i < strings.length; i++) {
	            itemList.add(strings[i].toString());
	        }
        }
		return itemList;
	}
		
	//--migrate from viewlocation shareMyLocation----
	//--write serialize HLocation object to file------------------------------------------
	public class createLocationFile extends AsyncTask <HLocation, Void, File> {
		boolean fileCreated;
		
		@Override
		protected File doInBackground(HLocation... params) {
			HLocation hLocation=new HLocation();
			String fileName;
			
			hLocation=params[0];									//retreive location object
			
			//--NEW FILENAME-----------------------------------------
			//--create file name with 4 chars from location and rest from location date--------
			fileName=hLocation.getName().toString();
			fileName=fileName.replaceAll("\\s", "");		//remove spaces
			int nameLength=fileName.length();
			if (nameLength<9) {
				fileName=fileName.toString()+"ppgoplaces";
			}
			
			StringBuilder newName=new StringBuilder().append(fileName.substring(0, 8)).append(".ppgp");
			fileName=newName.toString();						//recreate fileName with 8 chars ext ppgp
			
			//--create temp file directory or set path to temp directory----------------------------
			//--"Android/data/com.liseem.android.travel/ppgoplaces/cache" moved to constant
			File exportDir=new File(Environment.getExternalStorageDirectory(), PPGP_CACHE);
			if (!exportDir.exists()) {
				exportDir.mkdirs();
			}
			File exportFile=new File(exportDir, fileName);		//put in /mnt/sdcard/Android/com...travel/cache
			//Log.d(TAG, "export file path "+exportFile.getAbsolutePath());
			
			//----------------------------------------------------------------

			//--CREATE FILE FROM OBJE-------------------------------	
			try {
				FileOutputStream fout=new FileOutputStream(exportFile, true);
				//FileOutputStream fout=openFileOutput (fileName, MODE_WORLD_READABLE); //only  work on getFilesDir
				ObjectOutputStream oos=new ObjectOutputStream(fout);
				oos.writeObject(hLocation);
				oos.flush();
				oos.close();
				//Log.d(TAG,"Good so far far");
				return exportFile;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}			

		}		//END doInBackground
		
		@Override
		protected void onPostExecute(File newfile) {
			if (newfile.exists()) {
				Toast.makeText(getBaseContext(), "File saved successfully!", 
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getBaseContext(), "File create not successful!", 
						Toast.LENGTH_SHORT).show();
				
			}
		}
	}		//--END createLocationFile

	//--ReceiveLocation, retreive intent filter file back to location object
	//--FILE TO OBJECT extract object from file and add to location--------------------------------------------
	protected class retreiveLocationFromFile extends AsyncTask <String, Void, HLocation> {

		@Override
		protected HLocation doInBackground(String... params) {
			HLocation hLocation=new HLocation();
			String importFile=params[0];
			
			try {
				FileInputStream fIn=new FileInputStream(importFile);
				//FileInputStream fIn = openFileInput(filename);			//only work with getFilesDir
				//InputStreamReader isr=new InputStreamReader (fIn);
				ObjectInputStream ois=new ObjectInputStream(fIn);
				hLocation=(HLocation) ois.readObject();
				ois.close();
				return hLocation;
			} catch (Exception e){
				e.printStackTrace();
				return null;			
			}
		}
		
	@Override
	protected void onPostExecute(HLocation newLocation) {
			
			if (newLocation != null) {
				if (newLocation != null)
					//app.addHLocation(newLocation);		//culprit double adding to db
				Toast.makeText(getBaseContext(), "New location successfully retrieved!", 
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getBaseContext(), "Unable to retrieve Location Information", 
						Toast.LENGTH_SHORT).show();
			}
		}
		
	}	
	
	//--ViewLocation, called in setupView() to delete cache
	//--DELETE CACHE FILE-------------------------------------------------------------------
	//--HOUSEKEEPING async task to delete ppgp cache file-------------------------------------------
	protected class deleteCacheFile extends AsyncTask <String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String dirName=params[0];
			File dir = new File(Environment.getExternalStorageDirectory(), dirName);
			if (dir.exists()) {
				File[] toBeDeleted = dir.listFiles(new FileFilter() {
					@Override
					public boolean accept (File theFile) {
					if (theFile.isFile()) {
						return theFile.getName().endsWith(".ppgp");
					}
						return false;
					}
				});
				
				for (File deletableFile : toBeDeleted) {
					deletableFile.delete();
				}					
			}
			
			if (dir.exists()) {
				File[] toBeDeleted = dir.listFiles(new FileFilter() {
					@Override
					public boolean accept (File theFile) {
					if (theFile.isFile()) {
						return theFile.getName().endsWith(".png");
					}
						return false;
					}
				});
				
				for (File deletableFile : toBeDeleted) {
					deletableFile.delete();
				}					
			}
			return null; 
		}		
	}

	//--DELETE FILE-----------------------------------------------------------------------------
	//--async task to delete file-------------------------------------------
	protected class deleteFileTask extends AsyncTask <String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			
				String fileName=params[0];
				boolean deleteState=false;
				File f = new File(fileName.toString());
				if (f.exists())
					deleteState=f.delete();
				return deleteState;			
		}
	}

	//--create a cache file for add photo list ----------------------------------------------
	//--write serialize photoList object to file------------------------------------------
	public class createFotoListFile extends AsyncTask <ArrayList<PhotoOption>, Void, String> {
		boolean fileCreated;
		
		@Override
		protected String doInBackground(ArrayList<PhotoOption>... params) {
			ArrayList<PhotoOption> photoList = new ArrayList<PhotoOption>();
			String fileName;
			
			photoList=params[0];									//retreive location object
			
			//--NEW FILENAME-----------------------------------------
			//--create file name with 4 chars from location and rest from location date--------
			fileName=FOTOLIST;
			
			//--create temp file directory or set path to temp directory----------------------------
			//--"Android/data/com.liseem.android.travel/ppgoplaces/cache" moved to constant
			File exportDir=new File(Environment.getExternalStorageDirectory(), PPGP_CACHE);
			if (!exportDir.exists()) {
				exportDir.mkdirs();
			}
			File exportFile=new File(exportDir, fileName);		//put in /mnt/sdcard/Android/com...travel/cache
			//Log.d(TAG, "export file path "+exportFile.getAbsolutePath());
			
			//----------------------------------------------------------------

			//--CREATE FILE FROM OBJE-------------------------------	
			try {
				FileOutputStream fout=new FileOutputStream(exportFile, true);
				//FileOutputStream fout=openFileOutput (fileName, MODE_WORLD_READABLE); //only  work on getFilesDir
				ObjectOutputStream oos=new ObjectOutputStream(fout);
				oos.writeObject(photoList);
				oos.flush();
				oos.close();
				//Log.d(TAG,"Good so far far");
				return exportFile.getAbsolutePath();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}			

		}		//END doInBackground
		
		@Override
		protected void onPostExecute(String results) {
			File newfile = new File(results.toString());
			if (newfile.exists()) {
				Toast.makeText(getBaseContext(), "File saved successfully!", 
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getBaseContext(), "File create not successful!", 
						Toast.LENGTH_SHORT).show();
				
			}
		}
	}		//END createFotoListFile
	
	
	//--Receive fotolist file back to ArrayList<PhotoOption>
	//--FILE TO OBJECT extract object from file and add to location--------------------------------------------
	protected class retreiveFotoListFile extends AsyncTask <String, Void, ArrayList<PhotoOption>> {

		@Override
		protected ArrayList<PhotoOption> doInBackground(String... params) {
			ArrayList<PhotoOption> photoList = new ArrayList<PhotoOption>();
			String importFile=params[0];
			
			try {
				FileInputStream fIn=new FileInputStream(importFile);
				//FileInputStream fIn = openFileInput(filename);			//only work with getFilesDir
				//InputStreamReader isr=new InputStreamReader (fIn);
				ObjectInputStream ois=new ObjectInputStream(fIn);
				photoList=(ArrayList<PhotoOption>) ois.readObject();
				ois.close();
				return photoList;
			} catch (Exception e){
				e.printStackTrace();
				return null;			
			}
		}
		
	@Override
	protected void onPostExecute(ArrayList<PhotoOption> newList) {
			
			if (newList != null && !newList.isEmpty()) {
					//app.addHLocation(newLocation);		//culprit double adding to db
				Toast.makeText(getBaseContext(), "Photo list successfully retrieved!", 
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getBaseContext(), "Unable to retrieve photo list Information", 
						Toast.LENGTH_SHORT).show();
			}
		}
		
	}	
	
	//=====SECTION 7==FILE, DB AND OBJECT HELPERS===========================
	
	//=====SECTION 8==DEVICE HARDWARE AND STORAGE HELPERS===================	
	
	//--Need to check whether external storage is available to perform activity---------
	public boolean checkExternalStorageWriteWrite() {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();		//key to return all the external storage state

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		    return true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		    return false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		    return false;
		}
	}
	//==CHECK WHETHER DEVICE HAS GPS======================
	public boolean deviceHasGPS() {
		String className="com.google.android.maps.MapView";
		try {
			Class investigateClass=Class.forName(className);
			return true;
		}catch (ClassNotFoundException e) {
			String a=e.getMessage();
			return false;
		} catch (Exception e) {
			Log.d(TAG, "1.1 Class investigateClass(): Exception e ");
		} 
		return true;
	}
	
	
	//==CHECK FOR INTENT SERVICE OR FEATURE AVAILABLE ON PHONE===================

	//--check availability of intent object in device, i.e. able to disable in shared 
	//--preferences if not available. from Android Developers tutorial
	
	public static boolean isIntentAvailable(Context context, String action) {
	    final PackageManager packageManager = context.getPackageManager();
	    final Intent intent = new Intent(action);
	    List<ResolveInfo> list =
	            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
	    return list.size() > 0;
	}
	
	//--check or detect camera hardware
	public boolean checkCameraHardware (Context context) {
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}
	
	//--check or detect gps hardware
	public boolean checkGPSHardware (Context context) {
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
			return true;
		} else {
			return false;
		}
	}
	

	
	
	//=====SECTION 8==DEVICE HARDWARE AND STORAGE HELPERS===================	
	
	//=====SECTION 9==IMAGES, URI AND SCREEN HELPERS=========================	
	
	//--Get real path to image file from Uri--
		/*protected String getPathFromURI(Uri contentUri) {
	        String[] proj = { MediaStore.Images.Media.DATA };
	        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
	        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	        cursor.moveToFirst();
	        return cursor.getString(column_index);
	    }*/
		
		//--Get real Path from fake path, the above is exactly the same--
		//--need to delete one--
		public String getPath(Uri uri) {
			  String[] projection = { MediaColumns.DATA };
			  Cursor cursor = managedQuery(uri, projection, null, null, null);
			  int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
			  cursor.moveToFirst();
			  return cursor.getString(column_index);
		}
		
		@TargetApi(5)
		public String getExifInfo(String path) {
			ExifInterface exif;
			try {
				exif = new ExifInterface(path.toString());
				String info=exif.getAttribute(ExifInterface.TAG_ORIENTATION);
				return info;

			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
	//--
	//--from android developer site, calculate image size from bitmap option not actual bitmap
	//--int reqWidth and reqHeight is screen size
	
 	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    double inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	        if (width > height) {
	            inSampleSize = Math.round((float)height / (float)reqHeight);
	        } else {
	            inSampleSize = Math.round((float)width / (float)reqWidth);
	        }
	    }
	    inSampleSize=inSampleSize*1.5;
	    return (int) inSampleSize;
	}
	
	
	//--getting display screen size---------------------------------------------
	private Display getDisplay() {
	    return ((WindowManager) getBaseContext().getSystemService(
	            Context.WINDOW_SERVICE)).getDefaultDisplay();
	}

	public int getScreenOrientabliction() {					
		return getDisplay().getOrientation();
	}
	
	public int getScreenHeight() {
	    return getDisplay().getHeight();
	}

	public int getScreenWidth() {
	    return getDisplay().getWidth();
	}
	
	//--BITMAP AND DISPLAY------------------------------------------------------------------------------
	
	//--Codes revised Sep 22th, 2013
	//--Call by ShoppingList
	//--BITMAP Picture ScaleFactor - method to compute scaling factor
	//--use by photo information and add photo async to compute scalefactor---------

	public int computeScaleFactor(String fileName) {
		float deco=(float) 0.9;			//--decoration overhead such as textView, etc.
		int inSampleSize;					//--sample size
		
		//--STEP 1 - get phone screen size
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int dispW = size.x;
		int dispH = size.y;
		Log.d(TAG,"1412 display size point wide "+dispW+" and height "+dispH);
		
		 //--STEP 2 - get Bitmap shorter size--------------		
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;			//--option only no bitmap downloaded
		//Bitmap bmp = BitmapFactory.decodeFile(fileName.toString(), opts);		
		BitmapFactory.decodeFile(fileName.toString(), opts);		
		
		int picW=opts.outWidth;
		int picH=opts.outHeight;
		int shortW;
		Log.d(TAG,"Bitmap picture size of S2 wide "+picW+" and height "+picH);
		//bmp.recycle();										//--cannot be use with inJustDecodeBounds=true
		
		//--get bitmap shorter size
		if (picW > picH) {
			shortW=picH;
		} else {
			shortW=picW;
		}
		
       //--STEP 3 - get Display shorter size----------------------------------
       int dispS=0;												//--get the shorter side of target display
             
       if (dispW > dispH) {
    	   dispS=dispH;
       } else {
    	   dispS=dispW;
       }
       
       //--STEP 4 - compute factor with DECO compensation-------------
       inSampleSize = Math.round(shortW /(dispS*deco));
       Log.d(TAG, "computed scale factor "+inSampleSize);
       if (inSampleSize > 1) {
    	   return inSampleSize;							//--e.g. 4 will return inSampleSize as 1/4 of width or 1/16 of pixels
       } else {
    	   return 1;
       }       
       //return (int) inSampleSize;
       
	} 	//--END bitmapScaleFactor

	
	//--firstrun for computing scale factor camera size to screen size

	public int computeCameraScaleFactor() {
		float deco=(float) 0.9;
		int inSampleSize;
		
		//--STEP 1 - get phone screen size
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int dispW = size.x;
		int dispH = size.y;
		
      //-- get Display shorter size----------------------------------
       int dispS=0;		//--shorter side of target display
             
       if (dispW > dispH) {
    	   dispS=dispH;
       } else {
    	   dispS=dispW;
       }
		
		//--STEP 2 - get Camera largest resolution sizes--------------
		Camera camera = Camera.open();
        Camera.Parameters cameraParameters = camera.getParameters();
        List<Camera.Size> listSupportedPictureSizes = cameraParameters.getSupportedPictureSizes();
       int pictS=0, photoS=0;
       int targetS; 		//--shorter side of target photo
        
       for (Camera.Size c : listSupportedPictureSizes) {
    	   if (c.width > c.height) {
	    	   if (c.height>pictS) {
	    		   pictS=c.height;
	    		   photoS=c.height;
	    	   }
    	   } else {
	    	   if(c.width>pictS) {
	    		   pictS=c.width;
	    		   photoS=c.width;
	    	   }
    	   }   
       }
       
       //--STEP 3 - compute factor with DECO compensation-------------
       inSampleSize = Math.round(photoS /(dispS*deco));
       		Log.d(TAG, " bitmap computed scale factor "+inSampleSize);
       if (inSampleSize > 1) {
    	   return inSampleSize;
       } else {
    	   return 1;
       }       
       //return (int) inSampleSize;
       
	} 	//--END computeCameraScaleFactor
	
	
	//--to get the reference to File object from imageUri --
 	 public static File covertImageUriToFile (Uri imageUri, Activity activity) {

	 Cursor mcursor=null;
		 try {
			 String[] proj={MediaColumns.DATA, BaseColumns._ID,
					 	MediaStore.Images.ImageColumns.ORIENTATION};
			 mcursor=activity.managedQuery(imageUri, 
					 proj, 	//columns to return
					 null, 	//where which rows
					 null, 	//where selection args
					 null);	//order
			 int file_ColumnIndex=mcursor.getColumnIndexOrThrow(MediaColumns.DATA);
			 int orientation_ColumnIndex= mcursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
			 if (mcursor.moveToFirst()) {
				 String orientation=mcursor.getString(orientation_ColumnIndex);
				 return new File(mcursor.getString(file_ColumnIndex));
		 	}
			 return null;
		 } finally {
			 if (mcursor != null) {
				 mcursor.close();
			 }
		 }
	 } //END convertImageUriToFile()
 	 

 	 //--use on samsung phone to get picture path because it return null even picture taken ok
 	//--gets the last image id from the media store
 	protected String getLastImagePath(){			//int for return id, change to String for return path
 	    final String[] imageColumns = { BaseColumns._ID, MediaColumns.DATA };
 	    final String imageOrderBy = BaseColumns._ID+" DESC";
 	    Cursor imageCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, null, null, imageOrderBy);
 	    if(imageCursor.moveToFirst()){
 	        int id = imageCursor.getInt(imageCursor.getColumnIndex(BaseColumns._ID));
 	        String fullPath = imageCursor.getString(imageCursor.getColumnIndex(MediaColumns.DATA));
 	        imageCursor.close();
 	        //return id;
 	        return fullPath;
 	    }else{
 	        return null;
 	    }
 	}
 	 
 	//--with the last image id, below is how to remove the image---------------------
 	protected void removeImage(int id) {
        ContentResolver cr = getContentResolver();
         cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, BaseColumns._ID + "=?", new String[]{ Long.toString(id) } );
 	}
 
 	//--rotate picture for display base on exif orientation information------------------
 	//--ASYNC CALL FROM PAGEVIEWER NOT SUCCESSFUL
	protected class orientateBitmap extends AsyncTask <Long, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(Long... params) {
			LPicture pictureThis = new LPicture();
			pictureThis=app.getPicture(params[0]);
			
			Bitmap bm=null;
			int orient=0;
			int scalePicture=0;
			int bmRotate=0;
			String orientInfo=null;
			Matrix matrix;
			ExifInterface imageExif;
			
			//--retreive picture orientation and scaleFactor information, combined store in orient field
			//--left digit orientation info, right digit scale factor
			orient=pictureThis.getOrient();

			if (orient > 10) {		//i.e. there is no left orient and right scale found
				String orientText=String.valueOf(orient);
				bmRotate=Integer.parseInt(orientText.substring(0,1));			//first digit
				scalePicture=Integer.parseInt(orientText.substring(1,2));	//second digit scale factor
				orientInfo=orientText.substring(0,1);									//first digit orientation info
			}
			
			//--get scalefactor compare bitmap file size and display - TLA------
			if (scalePicture==0) {
				scalePicture=computeScaleFactor(pictureThis.getPicPath().toString());
				//Log.d(TAG, "is it here in scalePicture 0 "+scalePicture);
			}
						
			//--retrieve bitmap via picture getPicPath()---------------------------------			
			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inSampleSize=scalePicture;		//6, should be bigFactor from shared preferences
			//bm=BitmapFactory.decodeFile(listItems.get(position).getAbsolutePath().toString(), options);
			bm=BitmapFactory.decodeFile(pictureThis.getPicPath().toString(), options);
			
			//--rotate image base on orientation info from exif file-------------------
			matrix=new Matrix();
			
			if (bmRotate==0) {
				try {
					//--find orientation information if not found in picture orient info
					//Log.d(TAG, "in bmRotate = 0");
					imageExif = new ExifInterface(pictureThis.getPicPath().toString());
					orientInfo=imageExif.getAttribute(ExifInterface.TAG_ORIENTATION);
				} catch (IOException e) {
					e.printStackTrace();
				}
				bmRotate=Integer.parseInt(orientInfo);
				if (bmRotate==0) 
					bmRotate=1;
			}
		
			switch(bmRotate) {
				case 1:			//normal no rotation		
					matrix.postRotate(0);
					bm=Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);	
					//Log.d(TAG, "in the matrix before orientation, 1 - normal no rotation ");
					break;					
				case 3:			//rotate 180			
					matrix.postRotate(180);
					bm=Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);	
					//Log.d(TAG, "in the matrix before orientation, 3 - rotate 180 ");
					break;
				case 4:			//flip vertical, i.e. rotate cw 90 degrees			
					matrix.postRotate(270);
					bm=Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);	
					//Log.d(TAG, "in the matrix before orientation, should not be in here 4 -  flip vertical ");
					break;
				case 6:			//rotate cw 90 degrees			
					matrix.postRotate(90);
					bm=Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);	
					//Log.d(TAG, "in the matrix before orientation, 6 - rotate 90 ");
					break;
				case 8:			//rotate cw 270 degrees				
					matrix.postRotate(270);
					bm=Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);	
					//Log.d(TAG, "in the matrix before orientation, 8 - rotate 270 ");
					break;
				default:
					break;
			}
			return bm;
		}
	}		//END bitmapOrientation
 	
	//=====SECTION 9==IMAGES, URI AND SCREEN HELPERS=========================	

 	//=====SECTION 10==LOCATION, GPS AND GEO FENCE HELPERS=====================	
 	
	//==GPS, LOCATION METHODS============================	
	//--called from LocationAdd, 1080 MyLocationListener, LocationAway and MoveAround - Nov 18, 2013
	//--Test whether new location is better than current 
 	//--http://developer.android.com/guide/topics/location/strategies.html
	//--isBetterLocation() Part 1 of 2
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {		//--1080
		
		boolean isSignificantlyNewer			=false;						
		boolean isSignificantlyOlder				=false;		
		boolean isNewer								=false;		
		
		boolean isLessAccurate						=false; 
		boolean isMoreAccurate					=false;
		boolean isSignificantLessAccurate	=false; 
		
		if (currentBestLocation==null) {				//--this is a new location, i.e. no currentLocation			
			return true;											//--a new location is always better than no location
		}
		
		//--check the new location fix is new or older
		long timeDelta=location.getTime() - currentBestLocation.getTime();		//--new versus old location
		isSignificantlyNewer=timeDelta > TIME_LAPSE;										//--time lapse new by more than 2 mins ago
		isSignificantlyOlder=timeDelta < -TIME_LAPSE;		
		isNewer=timeDelta > 10000;																		//--true if timeDelta > 5 secs			
		//Log.d(TAG,"1665 timeDelta "+timeDelta+", TimeLapse "+TIME_LAPSE);
		//Log.d(TAG,"1666 isSignificantlyNewer "+isSignificantlyNewer);
		//Log.d(TAG,"1667 isSignificantlyOlder "+isSignificantlyOlder);
		//Log.d(TAG, "1668 isNewer "+isNewer);
		
		//--if more than two minutes use new location
		if (isSignificantlyNewer) {
			return true;											//--use any update if current is more than 2 mins old		
		} else if (isSignificantlyOlder) {
			return false;											//--if location is more than two mins older than currentLocation
		}
		
		//--if new location fix is more or less accurate
		//int accuracyDelta=(int) (location.getAccuracy()-currentBestLocation.getAccuracy());
		int accuracyDelta=(int) (location.getAccuracy()-currentBestLocation.getAccuracy());								//--positive if new location if accuracy is more, negative if better

		isLessAccurate=(accuracyDelta	> 0 && accuracyDelta < 5) ;				//--new location accuracy is worst of current, 5M delta
		isMoreAccurate=accuracyDelta < -5;													//--new location is more accurate, 5M delta
		isSignificantLessAccurate=accuracyDelta > 50; 									//--more than 100 meters
		//Log.d(TAG, "1686 accuracyDelta "+accuracyDelta);
		//Log.d(TAG, "1687 isLessAccurate "+isLessAccurate);
		//Log.d(TAG, "1686 isMoreAccurate "+isMoreAccurate);
		//Log.d(TAG, "1689 isSignificantLessAccurate "+isSignificantLessAccurate);
		
		//--check if old and new location are from same provider
		boolean fromSameProvider=isSameProvider(location.getProvider(), currentBestLocation.getProvider());
		
		if (isSignificantLessAccurate) return false;										//--don't bother if new is too inaccurate
		
		//--use new location if following accuracy and new timing
		//--1. if new location more accurate by 5m
		if (isMoreAccurate) {																			
			//Log.d(TAG, "1704 isMoreAccuracy");
			return true;		
			
		//--2. if 5 secs newer but within 5m tolerance
		} else if (isNewer && !isLessAccurate) {	
			//Log.d(TAG, "1707 isNewer and NOT isLessAccuracy");
			return true;		//both time newer and NOT less accuracy
			
		//--3. if 5 secs newer, NOT significant less accurate, however from same provider
		} else if (isNewer && !isSignificantLessAccurate && fromSameProvider) {
			//Log.d(TAG, "1711 isNewer and NOT isSignificantLessAccuracy, and from SAME provider");
			return true;
		}
		
		//--all other condition return false
		return false;
	}
	
	//--isBetterLocation() Part 2 of 2
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1==null) {
			return provider2==null;
		}
		return provider1.equals(provider2);	//compare two string return true if same
	}
 		
	//--GEO FENCE of location within "distance" of reference or default location---------------
	//--Part 1 of 2 -- current location is the reference location
	protected boolean locationWithinGeoFence (HLocation currentLocation, HLocation hLocation, long distance) {
		float[] distanceArray = new float[1];
		Location.distanceBetween(		
				currentLocation.getLatitude(),
				currentLocation.getLongitude(),
				hLocation.getLatitude(),
				hLocation.getLongitude(),
				distanceArray);
		
		boolean within=(distanceArray[0] < distance);
		return within; 		//true if distance array is less than distance
	}
	
	protected boolean locationNearBy (Location currentLocation, HLocation hLocation, long distance) {
		float[] distanceArray = new float[1];
		Location.distanceBetween(		
				currentLocation.getLatitude(),			//location object
				currentLocation.getLongitude(),
				hLocation.getLatitude(),						//HLocation object
				hLocation.getLongitude(),
				distanceArray);
		
		boolean within=(distanceArray[0] < distance);
		return within; 		//true if distance array is less than distance
	}
	

	//--Part 2 of 2
	public ArrayList<HLocation> filterLocationByGeo(HLocation refLocation, ArrayList<HLocation> allLocationList, long distance) {
		ArrayList<HLocation> tempHelp = new ArrayList<HLocation>();
		for (HLocation l : allLocationList) {

			if (l.getLatitude() >0 && l.getLongitude() >0) {

				boolean isWithin=locationWithinGeoFence(refLocation, l, distance);
				//Log.d(TAG, "isWithin Geofence return from part 1 "+isWithin);
				if (isWithin){
					tempHelp.add(l);
				}
			}
		}
		return tempHelp;
	}	

	//--add location goFindAddress asynctask, not to toast if distance is too close----------------
	protected boolean locationCloseBy (Location lastLocation, Location newLocation, long distance) {
		float[] distanceArray = new float[1];
		Location.distanceBetween(		
				lastLocation.getLatitude(),
				lastLocation.getLongitude(),
				newLocation.getLatitude(),
				newLocation.getLongitude(),
				distanceArray);
		
		boolean within=(distanceArray[0] < distance);
		return within; 		//true if distance array is less than distance
	}
	
 	//=====SECTION 10==LOCATION, GPS AND GEO FENCE HELPERS=====================	
	 	
	//=====SECTION 11==SANDBOX========================================	
	
	//TRYING OUT SHARE MENU===========
	
		/*		From calling activity
		 * 
		 * 		@Override
		 *		public boolean onCreateOptionsMenu(Menu menu) {
		 *			// Add any custom menu items you'd like here.
		 *			return super.onCreateOptionsMenu(menu);
    	 *		}
		 */

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// Handle clicks here.
			switch (item.getItemId()) {
			case MENU_ITEM_1: break;
			case MENU_ITEM_2: break;
			case MENU_ITEM_3: break;
			}
			return super.onOptionsItemSelected(item);
		}

	 
	
	//=====LOCATION COMPUTATION========================================================
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
		return within; 		//true if distance array is less than distance
	}

			
		
		
	/*
	 * ============================================================================
	 * SANDBOX
	 * 
	 * Routine Serialization and deserialization
	 * 
	 * ============================================================================
	 */ 	
	//==========2.3 SERIALIZATION AND DESERIALIZATION OF OBJECT==================
	//--Serialization method from object to byte for sqlite blob--
	public static byte[] serializableObject(Object obj) {
		ByteArrayOutputStream out= new ByteArrayOutputStream();
		
		try {
			ObjectOutputStream oos=new ObjectOutputStream(out);
			oos.writeObject(obj);

			oos.close();
			
			//Get the bytes of the serialized object
			byte[] buff=out.toByteArray();

			return buff;
		} catch (IOException e) {
			return null;
		}
	}
	
	public static Object deSerializableBlob (byte[] blob) {
		InputStream bais=new ByteArrayInputStream(blob);
		
		try {
			ObjectInputStream ois=new ObjectInputStream(bais);
			try {
				Object obj=ois.readObject();
				ois.close();
				return obj;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}
			
		} catch (IOException e) {
			return null;
		}		
	}
	

	
	
	//--read HLocation file back to object------------------------------------
	public HLocation createSerializeLocation(String filename) {
		HLocation hLocation=new HLocation();
		try {
			FileInputStream fIn = openFileInput(filename);
			//InputStreamReader isr=new InputStreamReader (fIn);
			ObjectInputStream ois=new ObjectInputStream(fIn);
			hLocation=(HLocation) ois.readObject();
			ois.close();
			return hLocation;
		} catch (Exception e){
			e.printStackTrace();
			return null;			
		}
	}
	
	
	//===========================================================================
	
	
	/*
	 * ============================================================================
	 * SANDBOX
	 * 
	 * Routine call by test button 
	 * 
	 * ============================================================================
	 */ 	
	
	public void long2cal (long testdate, String anotherdate) {
		//SimpleDateFormat sdf1=new SimpleDateFormat("yyyy-MM-dd");
		//Date resultdate=new Date(testdate);
		//sdf1.format(resultdate);
		Calendar tdate;
				tdate=str2Calendar(anotherdate);
				long headdate;
				headdate=tdate.getTime().getTime();
		
		Toast.makeText(getBaseContext(), "testdate1 : "+testdate+"\n"+"startdate : "+headdate, Toast.LENGTH_LONG).show();
	}
	
	final Calendar c = Calendar.getInstance();

    int maxYear = c.get(Calendar.YEAR) - 20; // this year ( 2011 ) - 20 = 1991
    int maxMonth = c.get(Calendar.MONTH);
    int maxDay = c.get(Calendar.DAY_OF_MONTH);

    int minYear = 1960;
    int minMonth = 0; // january
    int minDay = 25;

	//=====SECTION 11==SANDBOX========================================	
	

    
	//===========================================================================
    /*
     * 	ERROR MESSAGE WAREHOUSE
     */ 
    //===========================================================================
	    
	public void endDateB4Start() {
		AlertDialog show = new AlertDialog.Builder(this).setTitle("@id/entry_error")
				.setMessage("\"Start Date\" Is Later \"End Date\"")
				.setPositiveButton("OK", null).show();
	}
	
	public void dateNotInHoliday() {
		AlertDialog show = new AlertDialog.Builder(this).setTitle("@id/entry_error")
				.setMessage("\"Date\" Is Not Within Holiday Period")
				.setPositiveButton("OK", null).show();
	}
	
	public void emptyEntry(String fieldname) {
		AlertDialog show = new AlertDialog.Builder(this)
		.setTitle("@id/entry_error")
		.setMessage(fieldname.toString()+" Entry Is Empty")
		.setPositiveButton("OK", null).show();
	}



	
} //END MAIN CLASS
