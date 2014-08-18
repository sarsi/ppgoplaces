/* 
 * Object: 			MyDate
 * Description:    	Provides date type and format conversion
 * 
 * Last Updated: 	September 13, 2012
 * Last Release:  September 18, 2012 (version 1.1)
 * Last Review: 	September 17, 2013
 * 
 * 
 */

package com.liseem.android.travel.items;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.text.DateFormat;

public class MyDate {
	
	//--For setter and return when instance is called--
	private Calendar calDate;
	private String strDate=new String();				// in ISO8601 strings ("YYYY-MM-DD") format
	private String dMYDate=new String();				// in dd-MMM-YYYY format
	private SimpleDateFormat sdfDate=new SimpleDateFormat();
	private StringBuilder strBuilder=new StringBuilder();;
	private Date dateDate=new Date();
	private long milliDate;
	
	//--Transient variables for methods used--
	private Date tempDate;
	private String tempStr;
	private String tempDMY;
	private StringBuilder tempBuilder;
	private Calendar tempCal;
	private SimpleDateFormat tempSDF;
	
	//=====CLASS GETTERS AND SETTERS========================
	
	//--Return Calendar Date--
	public Calendar getCalDate() {
		return calDate;
	}
	
	public String getStrDate() {
		return strDate;
	}
	
	public String getDMYDate() {
		return dMYDate;
	}
	
	public long getMilliSecsDate() {
		return milliDate;
	}
	
	public String getFileStyle() {
		StringBuilder fs=new StringBuilder();
		fs.append(calDate.get(Calendar.DAY_OF_MONTH))
		.append(calDate.get(Calendar.MONTH))
		.append(calDate.get(Calendar.YEAR));
		return fs.toString();
	}
	
	//--Setter for Calendar Date-----------------
	public void setCalDate(Calendar calDate) {
		this.calDate = calDate;
		strDate=cal2String(calDate);
		dMYDate=displayDMY(strDate);
		milliDate=calDate.getTimeInMillis();
	}
	
	//--Setter for String Date-----------------
	public void setStrDate(String strDate) {
		this.strDate = strDate;
		calDate=str2Calendar(strDate);
		dMYDate=displayDMY(strDate);
		milliDate=calDate.getTimeInMillis();
	}
	
	public void setMilliSecs(long mtime) {
		calDate=Calendar.getInstance();			//need this step to initialize calDate
		calDate.setTimeInMillis(mtime);						
		strDate=cal2String(calDate);
		dMYDate=displayDMY(strDate);		
	}
	
	
	
	//====CLASS CONVERSION METHODS==========================================

	//--Transient variables for methods used--
	/*private Date tempDate;
	private String tempStr;
	private String tempDMY;
	private StringBuilder tempBuilder; 
	private Calendar tempCal;*/
	
	//--Convert Calendar to StringBuilder--
	//--in ISO8601 strings ("YYYY-MM-DD") format--
	public String cal2String(Calendar dbcal) {
		tempBuilder=new StringBuilder()
				.append(dbcal.get(Calendar.YEAR)).append("-")
		        //--NOTE: month is 0 base plus 1 to get correct month
				.append((dbcal.get(Calendar.MONTH))+1).append("-")
		        .append(dbcal.get(Calendar.DAY_OF_MONTH)).append(" ");
		return tempBuilder.toString();
	}
	
	//--Convert ISO8601 strings ("YYYY-MM-DD") format date to Calendar--
	public Calendar str2Calendar(String dbdate) {
		tempCal=Calendar.getInstance(TimeZone.getDefault(),Locale.getDefault());
		tempStr=dbdate;
		tempSDF=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		
		try {
			tempDate=tempSDF.parse(tempStr); //just changed from dbdate to strDate
			tempCal.setTime(tempDate);
		}catch (ParseException e) {
			e.printStackTrace();
		}
		return tempCal;
	}
	
	public Date str2Date(String dbdate) {
		Date thisDate;
		try {
			DateFormat formatter = null;
			thisDate=formatter.parse(dbdate);
			return thisDate;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//--RETURN ISO8601 strings ("YYYY-MM-DD") format date as 
	//-- string dd-MMM-YYYY format
	public String displayDMY(String sdate) {
		tempCal=str2Calendar(sdate);
		tempSDF=new SimpleDateFormat("dd-MMM-yyyy");
		tempStr=tempSDF.format(tempCal.getTime());
		return tempStr;
	}
	

	//====CLASS UTILITY PROGRAMS=============================================

	public Boolean cal2Compare(Calendar early, Calendar late) {
		String strEarly=cal2String(early).toString();	
		String strLate=cal2String(late).toString();
		Boolean result=false;
		Date 	dEarly=null,
				dLate = null;
		try {
			dEarly = new SimpleDateFormat("yyyy-MM-dd").parse(strEarly.toString());
			dLate = new SimpleDateFormat("yyyy-MM-dd").parse(strLate.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if (dLate.after(dEarly)) {
			result=true;
		}
		return result;
	} 
	
	//--return true, if this Calendar date is earlier than the parameter Calendar date--
	public Boolean isEarlier(Calendar late) {
		String strEarly=this.strDate;	
		String strLate=cal2String(late).toString();
		Boolean result=false;
		Date 	dEarly=null,
				dLate = null;
		try {
			dEarly = new SimpleDateFormat("yyyy-MM-dd").parse(strEarly.toString());
			dLate = new SimpleDateFormat("yyyy-MM-dd").parse(strLate.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if (dLate.after(dEarly)) {
			result=true;
		}
		return result;
	} 

	//--return number of days between two calendar date-------------------------------------------
	public int daysBetween(Calendar compDate) {
		  int result = 0;
		  if(compDate.after(this.calDate))			//if compDate is later than date comparing to
		 {
				Calendar c=(Calendar) this.calDate.clone();
				while (c.before(compDate)) {
				    c.add(Calendar.DAY_OF_MONTH, 1);
				    result++;
				  }
		  } else {
			  	Calendar c=(Calendar) compDate.clone();
			  	while (c.before(calDate)) {
				    c.add(Calendar.DAY_OF_MONTH, 1);
				    result++;
				  }
		  }
		  return result;
	}	
	
}		//END MAIN CLASS
