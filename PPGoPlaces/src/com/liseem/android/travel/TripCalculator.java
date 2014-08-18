/* 
 * Fragment: 		TripCalculator.java
 * Description:		General, Tip and Exchange Calculator
 * 							Update exchange and tip rate
 * 
 * Created: 			May 3, 2012
 * Last updated: 	October 20, 2013
 * 
  * Changes:
 * 	- Clean up codes to work in fragment 
 * - Moved setup to action bar
 * - Rearrange maths button to more consistent to desktop calculator
 * - More details for tips and total, (split functionality not implemented)
 * 
 * shared preferences
 * fxRate=prefs.getString("fxRate", "1");
 * tipPercent=prefs.getString("tipRate", "0");
 * 
 */

package com.liseem.android.travel;

import static com.liseem.android.travel.TravelLiteActivity.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import com.liseem.android.travel.R.drawable;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TripCalculator extends Fragment {

	//=====SECTION 1==DECLARATION===================================

	private static final String 					TAG	="TripCalculator";

	private PPGoPlacesApplication 			app;
	private SharedPreferences 					prefs;
	private int												ppgarage;
	
	//--calculator specific-------------
	//private WebView myWebView;
	private StringBuilder 								mMathString=new StringBuilder();
	//private ButtonClickListener  					myClickListener;
	private String 										tipPercent;
	private String 										fxRate;
	private String											b4TipNFx;
	private boolean									isTip;
	private StringBuilder								tempSet;						//--for temporary holding first set, split computation
	private String											tipAmt;
	private String											amtPlusTip;
	private String											totalAmt;
	private String											exchangeAmt;
	
	//--rebuild calculator without webView---------
	private TextView 									displayView;
	private StringBuilder 								secondSet=new StringBuilder();
	private StringBuilder 								firstSet=new StringBuilder();
	private boolean 									hasOperand=false;	//--ensure only a single operand a time
	private boolean 									hasDecimal=false;	//--ensure no additional decimal place
	private boolean 									hasFirstSet=false;
	private boolean 									twoNumber=false;		//--only compute when both set has content, continue from previous maths
	private int 												mathOp;
	private int												lastMathOp;
	private boolean 									chrNone	=false;		//--prevent multiple operand entry by user
	private boolean 									decCount=false;		//--ensure decimal count per set of string
	private String 										strOp;
	private boolean 									refreshView=true;		//--turn off updateView to display tip and fx
	private AlertDialog								showTip;
	
	private Button										splitButton;
	private Button 										plusButton;
	private Button 										minusButton;
	private Button 										timesButton;
	private Button 										divideButton;


	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	
		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;
	
		//--inflate layout for fragment 1------------------------------------------------
		View v=inflater.inflate(R.layout.trip_calculator, container, false);
		setHasOptionsMenu(true);
		setRetainInstance(true);
		
		return v;			
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
    @Override
    public void onStart() {
    	super.onStart();
    	
		//--call ActionBar------------------------------------------------------------------
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.show();
		actionBar.setDisplayShowTitleEnabled(false);
    }
    
	@Override
	public void onResume() {
		super.onResume();
		
		setupView();
		setListener();
	}
		
	
	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		//--Save vulnerable information--
		outState.putInt("mathOp", mathOp);
		outState.putString("firstSet", firstSet.toString());
		outState.putString("secondSet", secondSet.toString());
		outState.putString("mMathString", mMathString.toString());
	}
		
	/*@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		//--Restore vulnerable information upon onResume--
		mathOp=savedInstanceState.getInt("mathOp");
		String firstpart=savedInstanceState.getString("firstSet");
		String secondpart=savedInstanceState.getString("secondSet");
		String mathstring=savedInstanceState.getString("mMathString");
		
		firstSet=new StringBuilder().append(firstpart.toString());
		secondSet=new StringBuilder().append(secondpart.toString());
		mMathString=new StringBuilder().append(mathstring.toString());
		
	}*/
	
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
		
		//--setup path to main application----------------------------
		app								=(PPGoPlacesApplication)getActivity().getApplication();
		
		//--retrieve fx and tips rate from shared preferences-----
		//--get SharedPreferences object--------------------------------------
		prefs							=getActivity().getSharedPreferences (PREFNAME, MODE_PRIVATE);
	
		fxRate							=prefs.getString("fxRate", "1");
		tipPercent					=prefs.getString("tipPercent", "0.05");
		amtPlusTip					=prefs.getString("amtPlusTip", "1.05");
		Log.d(TAG, "214 so the tip amount is "+tipPercent.toString());
		Log.d(TAG, "215 so the fxRate is "+fxRate.toString());
		
		//--new rebuild calculator view---------------------------------
		displayView					=(TextView)getActivity().findViewById(R.id.calView);
		splitButton					=(Button)getActivity().findViewById(R.id.splitButton);
		plusButton					=(Button)getActivity().findViewById(R.id.buttonPlus);
		minusButton				=(Button)getActivity().findViewById(R.id.buttonMinus);
		timesButton				=(Button)getActivity().findViewById(R.id.buttonTimes);
		divideButton				=(Button)getActivity().findViewById(R.id.buttonDivide);
		
		splitButton.setVisibility(View.INVISIBLE);			//--future
		
		
		
		displayView.setMovementMethod(ScrollingMovementMethod.getInstance());
	
	}
	
	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================
	
	//=====SECTION 4==ADD ADAPTER===================================

	//=====SECTION 5==SET LISTENER====================================

	protected void setListener() {
		Log.d(TAG,"211 setListener()");
		
		//--set the listener to listen for all the buttons---------------------------
		View.OnClickListener myClickListener =new ButtonClickListener();
		
		int idList[] = { R.id.button0, R.id.button1, R.id.button2,
			    R.id.button3, R.id.button4, R.id.button5, R.id.button6,
			    R.id.button7, R.id.button8, R.id.button9, R.id.buttonLeftParen,
			    R.id.buttonRightParen, R.id.buttonPlus, R.id.buttonPlus,
			    R.id.buttonMinus, R.id.buttonDivide, R.id.buttonTimes,
			    R.id.buttonDecimal, R.id.buttonBackspace, R.id.buttonClear,
			    R.id.tipButton, R.id.buttonEquals, R.id.fxButton };

		for (int id: idList) {
			View v						=getActivity().findViewById(id);
			v.setOnClickListener(myClickListener);
			
			
		}
		
	}
	
	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================
	
	//--calculator input listener and webview updates------------------------------
	private class ButtonClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			 switch (v.getId()) {
			 case R.id.buttonBackspace:
				  if(secondSet.length()>0) {
					   mMathString.deleteCharAt(mMathString.length()-1);
					  secondSet.deleteCharAt(secondSet.length()-1);
					  if (secondSet.length()<1)   twoNumber=false;
				  }
				  break;
			 case R.id.buttonClear:
				 Log.d(TAG,"249 in buttonClear onClick");
				  if(mMathString.length() > 0)
				  mMathString.delete(0, mMathString.length());
				  secondSet.delete(0, secondSet.length());
				  firstSet.delete(0, firstSet.length());
				  hasOperand=false;
				  hasFirstSet=false;
				  twoNumber=false;
				  decCount=false;
				  ppgarage=0;
				  break;
			 case R.id.buttonPlus:								//--first and second set will not take operand
				 Log.d(TAG,"261 in buttonPlus onClick");
				 if (hasFirstSet && !hasOperand){			//--scenario 1: continue from last computed result 
					 mathOp=1;											//--holding in FirstSet not ready for do maths
					 strOp=((Button) v).getText().toString();
					 hasOperand=true;								//--the other two below operand is set in readyformaths()
					 mMathString.append(((Button) v).getText());	
				 } 				 
				 if (secondSet.length() >0) {					//--KEY only execute if secondSet is not empty	
					 if(!hasFirstSet) {								//--scenario 2 - new computation without firstSet
						mathOp=1;
						strOp=((Button) v).getText().toString();
						mMathString.append(((Button) v).getText());	
						readyForMaths();							
					 } else  if (hasFirstSet && hasOperand ) {	//--scenario 3 - ready to do maths with all 3 parts
						readyForMaths();								//--send without and AVOID overwriting operand
						 mathOp=1;
						 strOp=((Button) v).getText().toString();
						mMathString.append(((Button) v).getText());	
					 } 
					 // mMathString.append(((Button) v).getText());			 
					 //readyForMaths(((Button) v).getText().toString());
				 }
				 break;
			 case R.id.buttonMinus:
				 if (hasFirstSet && !hasOperand){		//scenario 1: continue from last computed result 
					 mathOp=2;										//holding in FirstSet not ready for do maths
					 strOp=((Button) v).getText().toString();
					 hasOperand=true;							//the other two below operand is set in readyformaths()
					 mMathString.append(((Button) v).getText());	
				 } 
				 
				 if (secondSet.length() >0) {				//KEY only execute if secondSet is not empty	
					 if(!hasFirstSet) {							//scenario 2 - new computation without firstSet
						mathOp=2;
						strOp=((Button) v).getText().toString();
						mMathString.append(((Button) v).getText());	
						readyForMaths();							
					 } else  if (hasFirstSet && hasOperand ) {	//scenario 3 - ready to do maths with all 3 parts
						readyForMaths();							//send without and AVOID overwriting operand
						 mathOp=2;
						 strOp=((Button) v).getText().toString();
						mMathString.append(((Button) v).getText());	
					 } 
				 }
				 break;
			 case R.id.buttonTimes:
				 if (hasFirstSet && !hasOperand){		//scenario 1: continue from last computed result 
					 mathOp=3;										//holding in FirstSet not ready for do maths
					 strOp=((Button) v).getText().toString();
					 hasOperand=true;							//the other two below operand is set in readyformaths()
					 mMathString.append(((Button) v).getText());	
				 } 
				 
				 if (secondSet.length() >0) {				//KEY only execute if secondSet is not empty	
					 if(!hasFirstSet) {							//scenario 2 - new computation without firstSet
						mathOp=3;
						strOp=((Button) v).getText().toString();
						mMathString.append(((Button) v).getText());	
						readyForMaths();							
					 } else  if (hasFirstSet && hasOperand ) {	//scenario 3 - ready to do maths with all 3 parts
						readyForMaths();							//send without and AVOID overwriting operand
						 mathOp=3;
						 strOp=((Button) v).getText().toString();
						mMathString.append(((Button) v).getText());	
					 } 
				 }
				break;
			 case R.id.buttonDivide:
				 if (hasFirstSet && !hasOperand){		//scenario 1: continue from last computed result 
					 mathOp=4;										//holding in FirstSet not ready for do maths
					 strOp=((Button) v).getText().toString();
					 hasOperand=true;							//the other two below operand is set in readyformaths()
					 mMathString.append(((Button) v).getText());	
				 } 
				 
				 if (secondSet.length() >0) {				//KEY only execute if secondSet is not empty	
					 if(!hasFirstSet) {							//scenario 2 - new computation without firstSet
						mathOp=4;
						strOp=((Button) v).getText().toString();
						mMathString.append(((Button) v).getText());	
						readyForMaths();							
					 } else  if (hasFirstSet && hasOperand ) {	//scenario 3 - ready to do maths with all 3 parts
						readyForMaths();							//send without and AVOID overwriting operand
						 mathOp=4;
						 strOp=((Button) v).getText().toString();
						mMathString.append(((Button) v).getText());	
					 } 
				 }
				 break;
			 case R.id.buttonDecimal:						//ensure no two decimal in each string				 
				 if (decCount==false) {
					 if (secondSet.length()>0) {
						 mMathString.append(((Button) v).getText());
						 secondSet.append(((Button) v).getText());						 
					 } else {
						 mMathString.append("0").append(((Button) v).getText());
						 secondSet.append("0").append(((Button) v).getText());
					 }
					 decCount=true;
				 }
				 break;
			 case R.id.buttonEquals:
				 if (secondSet.length()>0) {
					 if (hasFirstSet && hasOperand ) {
						 if (secondSet.length() > 0) {
							 strOp=((Button) v).getText().toString();
							 mMathString.append(((Button) v).getText());
							 doMaths();
							 hasOperand=false;
						 }
					 } 
				 }				
				 break;
			 case R.id.button0:
				 if (secondSet.length()>0) {
					 mMathString.append(((Button) v).getText());
					 secondSet.append(((Button) v).getText());
				 } 
				 break;			
			 case R.id.tipButton:
				 isTip=true;
				computeTip(); 
				 refreshView=false;		
				 if (ppgarage==234) {
					 Log.d(TAG, "landed in tip, ppgarage");
					 SharedPreferences.Editor editor=prefs.edit();
					 editor.putBoolean("ppgarage", true);
					 editor.commit();
					 ppgarage=0;
				 }
				 break;
			 case R.id.fxButton:
				 isTip=false;
				 computeFx(); 
				 refreshView=false;
				 break;
			 case R.id.splitButton:
				 //--use for split computation
				 //Intent changeFx=new Intent(getActivity(), TipAndFxDialog.class);
				 //startActivity(changeFx);				 
				 break;
			 case R.id.button2:
				 mMathString.append(((Button) v).getText());	
				 secondSet.append(((Button) v).getText());	
				 if (hasFirstSet && secondSet.length()>0) twoNumber=true;
				 ppgarage=ppgarage+100;
				 break;
			 case R.id.button3:
				 mMathString.append(((Button) v).getText());	
				 secondSet.append(((Button) v).getText());	
				 if (hasFirstSet && secondSet.length()>0) twoNumber=true;
				 ppgarage=ppgarage+10;
				 break;
			 case R.id.button4:
				 mMathString.append(((Button) v).getText());	
				 secondSet.append(((Button) v).getText());	
				 if (hasFirstSet && secondSet.length()>0) twoNumber=true;
				 ppgarage=ppgarage+1;
				 break;
			 default: 										//all other numbers except above
				 mMathString.append(((Button) v).getText());	
				 secondSet.append(((Button) v).getText());	
				 if (hasFirstSet && secondSet.length()>0) twoNumber=true;
			 }
			 if (refreshView) {
				 updateWebView();
			 } else {
				 refreshView=true;
			 }
		}
	}
	
	
	//=====SECTION 6==LISTENER METHODS================================

	//=====SECTION 7==HELPER METHODS==================================
	
	
	
	//--routine trigger from  plus, minus. multiply and divide-------------
	//--sequence will be firstSet, follows by operand and finally secondSet
	private void readyForMaths() {
		
			//--routine for firstset----------------but secondset cannot be empty-------
			if (!hasFirstSet && secondSet.length()>0 ) {		//from scenario 2 in buttonPlus case
				firstSet.append(secondSet.toString());
				secondSet.delete(0,secondSet.length());
				decCount=false;
				hasFirstSet=true;
				hasOperand=true;
			} else 						//defensive coding "else" to ensure the two ifs' don't get executed
			
			//--compute only when have both numbers and operand--------------------
			if (hasFirstSet && hasOperand ){		//from scenario 3
				mMathString.append("=");
				lastMathOp=mathOp;
				doMaths();
			} 
			
	}
	
	private void updateWebView() {
		displayView.setText(mMathString.toString());
		
	}

	private void doMaths() {
		String mathResults;
		BigDecimal firstNumber, secondNumber;
		BigDecimal mathCompute=new BigDecimal(0);
		switch (mathOp) {
			case 1:
				firstNumber=new BigDecimal(firstSet.toString());
				secondNumber=new BigDecimal(secondSet.toString());
				mathCompute=firstNumber.add(secondNumber);								
				break;
			case 2:
				firstNumber=new BigDecimal(firstSet.toString());
				secondNumber=new BigDecimal(secondSet.toString());
				mathCompute=firstNumber.subtract(secondNumber);				
				break;
			case 3:
				firstNumber=new BigDecimal(firstSet.toString());
				secondNumber=new BigDecimal(secondSet.toString());
				mathCompute=firstNumber.multiply(secondNumber);		
				break;
			case 4:
				firstNumber=new BigDecimal(firstSet.toString());
				secondNumber=new BigDecimal(secondSet.toString());
					//--avoid big decimal non-terminating expansion error---------
				mathCompute=firstNumber.divide(secondNumber, 2, RoundingMode.HALF_UP);
				break;
			default:
				break;
		}
		//--convert to string from bigdecimal to double to string--------
		double d=mathCompute.doubleValue();
	    DecimalFormat fmt = new DecimalFormat("0.00");  
	    mathResults= fmt.format(d);
	    
	    //--retain history for continuation until clear key----------------
		firstSet.delete(0, firstSet.length());						//retain result for further compute
		secondSet.delete(0, secondSet.length());
		firstSet.append(mathResults.toString());
		//--reset only if equal is pressed else should remind true as there is a operand
		mMathString.append(mathResults.toString());		//--retain history for display
		
		hasFirstSet=true;
		twoNumber=false;													//--accept operand and firstset has data
	    chrNone=false;														//--reset operand for next maths
	    decCount=false;													//--reset decCount, 1 per set as well.
	}
	
	public boolean checkNone(String str, char a) {
		char chr=a;
		 int counter = 0;
		 for( int i=0; i<secondSet.length(); i++ ) {
		     if( secondSet.charAt(i) == chr) {
		         counter++;
		     } 
		 }
		 if (counter==0) {
			 return true;
		 } else {
			 return false;
		 }
	}

	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
			super.onPrepareOptionsMenu(menu);
			
			menu.removeItem(9);
			menu.removeItem(10);
	}
	
	private void CreateMenu(Menu menu) {
		
		MenuItem mnu1=menu.add(0, 1, 1, R.string.setup); 
			{ 	//mnu1.setIcon(android.R.drawable.ic_menu_add);
				mnu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}
			
	}
	
	private boolean MenuChoice(MenuItem item)
	{
		switch (item.getItemId()) {
		case 1:
			//--goto tip and fx dialog---------------
			((FragMain)getActivity()).callTipAndFx();
			return true;
				
		default:
			return super.onContextItemSelected(item);
		}
	}			

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//=====SECTION 10==SANDBOX======================================


	
	//--method to compute and display tip and amount plus tip---------
	public void computeTip() {
		
		String[] haveTip;
		tempSet			=firstSet;
		if (hasFirstSet && !twoNumber) {			//--two if statements necessary to prevent null exception
			tempSet		=firstSet;
			b4TipNFx		=tempSet.toString();
			
			//--get tip amount [0] and amount plus tip [1]-------------------
			haveTip			=((TravelLiteActivity) getActivity()).tipCompute(tempSet.toString(), tipPercent.toString());
			
			//--reset calculator variables------------------------------------------
			mMathString.delete(0, mMathString.length());
			secondSet.delete(0, secondSet.length());
			firstSet.delete(0, firstSet.length());
			hasOperand		=false;
			hasFirstSet		=false;
			twoNumber		=false;
			decCount			=false;
			
			//--display final amount------------------------------------------------
			tipAmt					= haveTip[0];
			totalAmt				= haveTip[1];
			Log.d(TAG,"648 after tipAmt tempSet "+b4TipNFx.toString());
	
			StringBuilder toShow = new StringBuilder ("Amount : \t\t"+b4TipNFx.toString()+
					"\nTips : \t\t\t\t" +tipAmt.toString()+
					"\nTotal : \t\t\t"+totalAmt.toString());
			displayView.setText(mMathString.toString());
			tipShowDialog(toShow.toString());
		
		} else if (!hasFirstSet && secondSet.length()>0) {
			tempSet		=secondSet;
			b4TipNFx		=tempSet.toString();
			
			//--get tip amount [0] and amount plus tip [1]-------------------
			haveTip			=((TravelLiteActivity) getActivity()).tipCompute(tempSet.toString(), tipPercent.toString());
			
			//--reset calculator variables------------------------------------------
			mMathString.delete(0, mMathString.length());
			secondSet.delete(0, secondSet.length());
			firstSet.delete(0, firstSet.length());
			hasOperand		=false;
			hasFirstSet		=false;
			twoNumber		=false;
			decCount			=false;
			
			//--display final amount------------------------------------------------
			tipAmt					= haveTip[0];
			totalAmt				= haveTip[1];
			Log.d(TAG,"648 after tipAmt tempSet "+b4TipNFx.toString());
	
			StringBuilder toShow = new StringBuilder ("Amount : \t\t"+b4TipNFx.toString()+
					"\nTips : \t\t\t\t" +tipAmt.toString()+
					"\nTotal : \t\t\t"+totalAmt.toString());
			displayView.setText(mMathString.toString());
			tipShowDialog(toShow.toString());
		} 

		/*if (tipAmt != null)
			displayView.setText(	"Amount : \t\t"+b4TipNFx.toString()+
												"\nTips : \t\t" +tipAmt.toString()+
												"\nTotal : \t"+totalAmt.toString());*/

		//displayView.setText(message.toString()+checkTip.toString());
		//return haveTip;
	}	//--END computeTip
	
	//--method to convert to local currency---------		
	public void computeFx() {
		
		String looseChange;
		tempSet=firstSet;
		if (hasFirstSet && !twoNumber) {				//--two if statements necessary to prevent null exception
			 tempSet		=firstSet;
			b4TipNFx		=tempSet.toString();
	
			looseChange=((TravelLiteActivity) getActivity()).for2home(tempSet.toString(), fxRate.toString());
			
			//--reset calculator variables------------------------------------------
			mMathString.delete(0, mMathString.length());
			secondSet.delete(0, secondSet.length());
			firstSet.delete(0, firstSet.length());
			hasOperand=false;
			hasFirstSet=false;
			twoNumber=false;
			decCount=false;
			
			//--display final amount------------------------------------------------
			displayView.setText(mMathString.toString());
			StringBuilder toShow = new StringBuilder ("Home Currency : "+looseChange.toString());
			tipShowDialog(toShow.toString());
		} else if (!hasFirstSet && secondSet.length()>0) {
			tempSet=secondSet;
			b4TipNFx		=tempSet.toString();
			
			looseChange=((TravelLiteActivity) getActivity()).for2home(tempSet.toString(), fxRate.toString());
			
			//--reset calculator variables------------------------------------------
			mMathString.delete(0, mMathString.length());
			secondSet.delete(0, secondSet.length());
			firstSet.delete(0, firstSet.length());
			hasOperand=false;
			hasFirstSet=false;
			twoNumber=false;
			decCount=false;
			
			//--display final amount------------------------------------------------
			displayView.setText(mMathString.toString());
			StringBuilder toShow = new StringBuilder ("Home Currency : "+looseChange.toString());
			tipShowDialog(toShow.toString());
		} 
		//displayView.setText("Home Currency : "+looseChange.toString());
	}	//--END computeFX
	
	//--display dialog box for tip and fx results--------------------------
	public void tipShowDialog(String showMsg) {
		showTip= new AlertDialog.Builder(getActivity())
		.setIcon(R.drawable.ppgp_icon)
		.setTitle(R.string.trip_calculator)
		.setMessage(showMsg.toString())
		.setPositiveButton(R.string.ok, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showTip.dismiss();
			}
		})
		.create();
		showTip.show();
	}
	
} 		//END OF MAIN CLASS
