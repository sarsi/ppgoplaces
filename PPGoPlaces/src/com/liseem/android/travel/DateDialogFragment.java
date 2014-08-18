/*
 * Fragment:			DateDialogFragment.java
 * Description:			Date picker dialog fragment. Replaced deprecated date picker dialog.
 * 								callback to calling fragment via interface
 * 
 * Date created:		October 26, 2012
 * Last updated:		October 26, 2013
 * 
 * 
 */

package com.liseem.android.travel;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;

public class DateDialogFragment extends DialogFragment {
	
	public static String 									TAG = "DateDialogFragment";
	
	private static final int 								STARTDATE_DIALOG_ID=0;
	private static final int 								ENDDATE_DIALOG_ID=1;
	 
    static Context 											sContext;
    static Calendar 											sDate;
    static DateDialogFragmentListener 			sListener;
    static int 													sDateInfo;
    static int													sWhichDate;
 
 
    public static DateDialogFragment newInstance(Context context, int titleResource, Calendar date){
		DateDialogFragment dialog = new DateDialogFragment();
 
		sContext 				= context;
	    sDate 					= date;
	    sWhichDate			= titleResource;
	    
	    if (titleResource==STARTDATE_DIALOG_ID) {
	    	sDateInfo = R.string.set_start_date;
	    } else if (titleResource==ENDDATE_DIALOG_ID) {
	    	sDateInfo = R.string.set_end_date;	    		
	    }
		Bundle args 		= new Bundle();
		args.putInt("title", titleResource);
		dialog.setArguments(args);
		return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new DatePickerDialog(sContext, dateSetListener, sDate.get(Calendar.YEAR), sDate.get(Calendar.MONTH), sDate.get(Calendar.DAY_OF_MONTH));
    }
    
    //--Part 1 of 2 - fragment interface to  activity callback,
    //--method need to be created in calling program (implements this interface) for this app to execute
    public interface DateDialogFragmentListener{
        public void dateDialogFragmentDateSet(Calendar date, int sWhichDate);		
    }
    
    //--Part 2 of 2 - setter call from calling fragment to set variable listener, i.e. not null
    //--use it at calling fragment in the sequence that calling just before calling this fragment
    public void setDateDialogFragmentListener(DateDialogFragmentListener listener){
        sListener = listener;
    }
    
    //--callback interface from default Android DatePickerDialog--------------
    private DatePickerDialog.OnDateSetListener dateSetListener =
    	    new DatePickerDialog.OnDateSetListener() {
    	 

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				
                //--create new Calendar object for date picking
                //--preset date is date pass in from constructor DateDialogFragment
	   			Calendar newDate = Calendar.getInstance();
    			newDate.set(year, monthOfYear, dayOfMonth);
    			
    			//--call back to the DateDialogFragment listener
    			sListener.dateDialogFragmentDateSet(newDate, sWhichDate);
				
			}
    	};
    
    
}	//END - DateDialogFragment
