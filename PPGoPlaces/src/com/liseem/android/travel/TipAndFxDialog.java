/* 
 * Fragment: 		TipAndFxDialog.java
 * Description:		Tip and Exchange Setup View
 * 							Update exchange and tip rate
 * 
 * Created: 			May 3, 2012
 * Last updated: 	October 19, 2013
 * 
  * Changes:
 * 	- Clean up codes to work in fragment 
 * - Fixed strings.xml with getString
 * - Replace update button with action bar item
 * - Added amount plus tip in shared preferences
 * 
 * shared preferences
 * fxRate=prefs.getString("fxRate", "1");
 *	tipPercent=prefs.getString("tipRate", "0");
 * 
 */


package com.liseem.android.travel;

import static com.liseem.android.travel.TravelLiteActivity.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class TipAndFxDialog extends Fragment {

	//=====SECTION 1==DECLARATION===================================

	private static final String 				TAG="TipAndFxDialog";
	
	private PPGoPlacesApplication 		app;
	private SharedPreferences.Editor		editor;

	//--view specific-------------
	private Button 									addButton;
	private Button									checkRate;
	private EditText 								fxConv;
	private EditText 								tipInfo;
	private SharedPreferences 				prefs;
	private RadioGroup 							radioGroup;
	private RadioButton 							rb2;
	private RadioButton 							rb1;


	//--java code variables----------
	private String 									fxRate;
	private String 									tipPercent;
	private TextView 								tipsHint;
	private TextView 								fxHint;
	private boolean 								homeOne;

	private EditText 								forConv;
	private TextView 								rateText;
	private TextView 								currText;

	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	
		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;
	
		//--inflate layout for fragment 1------------------------------------------------
		View v=inflater.inflate(R.layout.fx_setup, container, false);
		setHasOptionsMenu(true);	
		setRetainInstance(true);
		
		return v;			
	}
	
    @Override
    public void onCreate(Bundle icicle) {
    	super.onCreate(icicle);
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
		//--call ActionBar------------------------------------------------------------------
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.show();
		actionBar.setDisplayShowTitleEnabled(false);

    	setupView();
    }
    
	//--Create Options menu---------------------------------
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		CreateMenu(menu);
	}


	//--Return menu choice on menu selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		return MenuChoice(item);
	}
	

	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 3==SETUP VIEWS====================================
	
	protected void setupView() {
               
			app					=(PPGoPlacesApplication)getActivity().getApplication();
        

	        //--setup views-------------------------------------------------------
	        //launch = (Button)getActivity().findViewById(R.id.saveButton);		//save on back key
	        fxHint				=(TextView)getActivity().findViewById(R.id.textView2);
	        tipsHint			=(TextView)getActivity().findViewById(R.id.textView3);
	        rateText			=(TextView)getActivity().findViewById(R.id.textView5);
	        currText			=(TextView)getActivity().findViewById(R.id.textView4);
	        fxConv 			=(EditText) getActivity().findViewById(R.id.fxText);
	        tipInfo				=(EditText) getActivity().findViewById(R.id.tipText);
	        radioGroup 		=(RadioGroup)getActivity().findViewById(R.id.radioGroup1);
	        rb1 					=(RadioButton) getActivity().findViewById(R.id.radio0);
	        rb2 					=(RadioButton) getActivity().findViewById(R.id.radio1);
	        rb2.setChecked(true);
	        currText.setText(R.string.to_home_);
	        
	
			//--retrieve fx and tips rate from shared preferences------------
			//--get SharedPreferences object--------------------------------------
			prefs=getActivity().getSharedPreferences (PREFNAME, MODE_PRIVATE);
		
			fxRate=prefs.getString(getString(R.string.fxrate), "1");
			tipPercent=prefs.getString(getString(R.string.tippercent), "0");
			//Log.d(TAG, "127 where is the problem ");
			
			//--load previous rate or default
			//fxConv.setText(fxRate.toString());
			String convertBack=tipToDisplay();
			tipInfo.setText(convertBack.toString());
	        //Log.d(TAG,"133 tipInfo.setText ");
			
			//--setup text view------------------------------------------------------
			fxHint.setText(R.string.enter_exchange_rate_below_);
	        tipsHint.setText(R.string.enter_customary_tips_below_e_g_12_for_12_);
	        
	        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
	        {
		        @Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
		        
			        if (rb1.isChecked()) {
			        	homeOne=true;
			        	currText.setText("to Foreign ");
			        } else {
			        	homeOne=false;
			        	currText.setText(R.string.to_home_);
			        }
		        }
	        });
	              
    }
	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================
	
	//=====SECTION 5==SET LISTENER====================================

	//--Action bar menu item - check rate entered---------------------------------------
	public void verifyRate() {
		String getFx=fxConv.getText().toString();
		String convertAmt;
		fxHint.setText("");
		if (getFx.isEmpty()) {
			((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.please_enter_a_exchange_rate));
		} else {
			convertAmt=convertRate();
			Log.d(TAG, "182 checkRateClick, return convertAmt from convertRate() "+convertAmt.toString());

		    if (homeOne) {
		    	fxHint.setText(getString(R.string._1_of_foreign_)+convertAmt.toString()+getString(R.string._home_currency));
		    } else {
		    	fxHint.setText(getString(R.string._1_home_)+convertAmt.toString()+getString(R.string._of_foreign_currency));	    	
		    }		
		}			
	} 		//--END verifyRate
	
	//--Action bar menu item - update fx and tip rate-----------------------------------
	public void updateRate() {

    	//--write to SharedPreferences object--------------------------------------
		prefs					=getActivity().getSharedPreferences (PREFNAME, MODE_PRIVATE);
		editor					=prefs.edit();

		fxRate="1";
		if (fxConv.getText()==null) 
    	{
    		fxRate="1"; 
    	} else if (homeOne){
    		fxRate=convertRate();
    	} else if (!homeOne) {
    		fxRate=fxConv.getText().toString();
    	}
		editor.putString("fxRate", fxRate.toString());
		editor.commit();

		//--editor for tip and amt plus tip are write to prefs in method below
    	convertTip();
   			
 		//--hide soft keyboard and exit
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 
	    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),      
	    InputMethodManager.HIDE_NOT_ALWAYS);
	    
		//--return to previous calling fragment
		getFragmentManager().popBackStackImmediate();
	}			//--END updateRate

	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
			super.onPrepareOptionsMenu(menu);
			
			menu.removeItem(9);
			menu.removeItem(10);
			menu.removeItem(11);
	}
	
	private void CreateMenu(Menu menu) {
		
		MenuItem mnu1=menu.add(0, 1, 1, R.string.verify); 
			{ 	//mnu1.setIcon(android.R.drawable.ic_menu_add);
				mnu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}
	
		MenuItem mnu2=menu.add(0, 2, 2, R.string.save);
			{ 	//mnu2.setIcon(android.R.drawable.ic_menu_agenda); 
				mnu2.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}
		
		/*MenuItem mnu3=menu.add(0, 3, 3, R.string.help);
			{ 	mnu3.setIcon(android.R.drawable.ic_menu_help); 
				mnu3.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}*/
			
	}
	
	private boolean MenuChoice(MenuItem item)
	{
		switch (item.getItemId()) {
		case 1:
			verifyRate();			
			return true;
		
		case 2:
			updateRate();
			return true;
		
		default:
			return super.onContextItemSelected(item);
		}
	}			

	//=====SECTION 7==HELPER METHODS==================================
			
	//--convert if user enter $1 home to x foreign currency
	//-- need to store rate in local amount equivalent to $1 foreign for maths--
	public String convertRate() {
		String tempRate=fxConv.getText().toString();
		String unitAmnt="1";
		BigDecimal hasRate=new BigDecimal(tempRate);

		BigDecimal convCurr=new BigDecimal(unitAmnt);

		BigDecimal getConv=convCurr.divide(hasRate, 4, RoundingMode.HALF_UP);		
		
		double d=getConv.doubleValue();		
	    Log.d(TAG,"245 convertRate, getConv d double from bigdecimal "+d);

	    DecimalFormat fmt = new DecimalFormat("0.0000");			//--("###,###,##0.00##");  
	    String convertedAmtStr= fmt.format(d);  		
	    Log.d(TAG,"249 convertRate, convert amt to string "+convertedAmtStr.toString());
	    
	    return convertedAmtStr;	    
	} //end convertRate
	
	//--convert tip entry to decimal for maths----------
	public void convertTip() {
		String haveTip=tipInfo.getText().toString();
		String amtPlusTip="1"+tipInfo.getText().toString();
		Log.d(TAG,"318 convertTip method, total with tip "+amtPlusTip);
		String toDec="100";
		BigDecimal tipBig=new BigDecimal(haveTip);
		BigDecimal amtBig=new BigDecimal(amtPlusTip);
		
		BigDecimal conPer=new BigDecimal(toDec);
		
		BigDecimal tipDecimal=tipBig.divide(conPer);
		BigDecimal amtDecimal=amtBig.divide(conPer);
				
		double t = tipDecimal.doubleValue();
		double a = amtDecimal.doubleValue();
		
		DecimalFormat fmt = new DecimalFormat("0.00");  
		
	    String convertedTip= fmt.format(t);  	
	    String convertedAmt= fmt.format(a);  	
	    
		editor.putString("amtPlusTip", convertedAmt.toString());
		editor.putString("tipPercent", convertedTip.toString());
		editor.commit();
	    
	    //return convertedTip;
	}

	//--convert back from shared preferences for presenting to user---------
	public String tipToDisplay() {
		Log.d(TAG,"240 tipToDisplay method ");
		String haveTip=tipPercent.toString();
		String toDec="100";
		BigDecimal tipBig=new BigDecimal(haveTip);
		BigDecimal conPer=new BigDecimal(toDec);
		BigDecimal tipDecimal=tipBig.multiply(conPer);
		
		double t = tipDecimal.doubleValue();
		DecimalFormat fmt = new DecimalFormat("0");  
	    String convertedTip= fmt.format(t);  	
	    return convertedTip;		
	}
	
	//=====SECTION 7==HELPER METHODS==================================

}		//END OF MAIN CLASS
		
