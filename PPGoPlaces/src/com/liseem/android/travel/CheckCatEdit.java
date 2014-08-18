/*
 * Fragment:			EditCheckCat.java
 * Description:			Let user rename checklist category instead of hardcoded ones
 * 
 * Date created:		September 14, 2012
 * introduction:		Release v1.1 (September 18, 2012)
 * 
 * Last Updated:		October 13, 2013
 * 
 * Associated files:
 * Layout view: 		editcheckcat.xml
 * Parent file: 			HolidayCheckList.java
 * 
 */

package com.liseem.android.travel;

import static com.liseem.android.travel.TravelLiteActivity.*;

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
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class CheckCatEdit extends Fragment {
	
	//=====SECTION 1==DECLARATION===================================

	private static final String 				TAG="CheckCatEdit";
	
	//--application----------------------------------------------
	private PPGoPlacesApplication 		app;
	private SharedPreferences 				category;
	private SharedPreferences.Editor 		catEdit;
	
	//--view layout---------------------------------------------
	private TextView 								preText;
	private TextView 								lmText;
	private TextView 								ohText;
	private TextView 								postText;
	private TextView 								plText;
	private EditText 								preEdit;
	private EditText 								lmEdit;
	private EditText 								ohEdit;
	private EditText 								postEdit;
	private EditText 								plEdit;
	private Button 									updateButton;

	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.editcheckcat);
	}
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		setHasOptionsMenu(true);								//--use fragment options menu instead of activity default

		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;

		//--inflate layout for fragment 1------------------------------------------------
		View v=inflater.inflate(R.layout.editcheckcat, container, false);
		setRetainInstance(true);
		
		return v;			
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

	}

	//--Create Options menu---------------------------------
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		CreateMenu(menu);
		//return true;		
	}


	//--Return menu choice on menu selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		return MenuChoice(item);
	}
	
	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 3==SETUP VIEWS====================================

	public void setupView() {
		
		Log.d(TAG, "landed in setupView");
		//--application setup------------------------------------------------
		app			=(PPGoPlacesApplication)getActivity().getApplication();
		category	=getActivity().getSharedPreferences (CHECKLIST, MODE_PRIVATE);
		catEdit  	= category.edit();
		
		//--setup layout views----------------------------------------------
		preText	=(TextView)getActivity().findViewById(R.id.catText1);
		lmText		=(TextView)getActivity().findViewById(R.id.catText2);
		ohText		=(TextView)getActivity().findViewById(R.id.catText3);		
		postText	=(TextView)getActivity().findViewById(R.id.catText4);
		plText		=(TextView)getActivity().findViewById(R.id.catText5);

		preEdit		=(EditText)getActivity().findViewById(R.id.category1);
		lmEdit		=(EditText)getActivity().findViewById(R.id.category2);
		ohEdit		=(EditText)getActivity().findViewById(R.id.category3);
		postEdit	=(EditText)getActivity().findViewById(R.id.category4);
		plEdit		=(EditText)getActivity().findViewById(R.id.category5);

		//==OBSOLETED BY ACTION BAR=================
		updateButton=(Button)getActivity().findViewById(R.id.addButton);
		updateButton.setVisibility(View.GONE);
		
		//--populate textview---------------------------------------------
		preText.setText("Check List - Category 1");
		lmText.setText("Check List - Category 2");
		ohText.setText("Check List - Category 3");
		postText.setText("Check List - Category 4");
		plText.setText("Packing List - Category");
		
		preEdit.setText(category.getString("TaskCat1", TASK_MENU_1));
		lmEdit.setText(category.getString("TaskCat2", TASK_MENU_2));
		ohEdit.setText(category.getString("TaskCat3", TASK_MENU_3));
		postEdit.setText(category.getString("TaskCat4", TASK_MENU_4));
		plEdit.setText(category.getString("TaskCat8", TASK_MENU_8));
		
		updateButton.setOnClickListener(new SaveChangesOnClick());		
		
	}	//END setupView

	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==VIEW LISTENER===================================
	
	//--update category name change-------------------------------------
	//--OBSOLETE BUTTON
	private class SaveChangesOnClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (!preEdit.getText().toString().matches("")) 
				catEdit.putString("TaskCat1", preEdit.getText().toString());
			if (!lmEdit.getText().toString().matches("")) 
				catEdit.putString("TaskCat2", lmEdit.getText().toString());
			if (!ohEdit.getText().toString().matches("")) 
				catEdit.putString("TaskCat3", ohEdit.getText().toString());
			if (!postEdit.getText().toString().matches("")) 
				catEdit.putString("TaskCat4", postEdit.getText().toString());
			if (!plEdit.getText().toString().matches("")) 
				catEdit.putString("TaskCat8", plEdit.getText().toString());
			catEdit.commit();
			
			//--hide soft keyboard and exit
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 
		    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),      
		    InputMethodManager.HIDE_NOT_ALWAYS);
			//getActivity().finish();
			getFragmentManager().popBackStackImmediate();
		}		
	}

	//=====SECTION 4==VIEW LISTENER===================================

	//=====SECTION 5==MENU AND MENU METHODS============================

	private void CreateMenu(Menu menu) {
		
		MenuItem mnu1=menu.add(0, 1, 1, R.string.save); 
			{ 	//mnu1.setIcon(android.R.drawable.ic_menu_add);
				mnu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}
	
		/*MenuItem mnu2=menu.add(0, 2, 2, R.string.new_);
			{ 	mnu2.setIcon(android.R.drawable.ic_menu_agenda); 
				mnu2.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}
		
		MenuItem mnu3=menu.add(0, 3, 3, R.string.help);
			{ 	mnu3.setIcon(android.R.drawable.ic_menu_help); 
				mnu3.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}*/
			
	}
	
	private boolean MenuChoice(MenuItem item)
	{
		switch (item.getItemId()) {
		case 1:
			saveCategory();
			return true;
		
		default:
			return super.onContextItemSelected(item);
		}
	}			

	public void saveCategory() {
		if (!preEdit.getText().toString().matches("")) 
			catEdit.putString("TaskCat1", preEdit.getText().toString());
		if (!lmEdit.getText().toString().matches("")) 
			catEdit.putString("TaskCat2", lmEdit.getText().toString());
		if (!ohEdit.getText().toString().matches("")) 
			catEdit.putString("TaskCat3", ohEdit.getText().toString());
		if (!postEdit.getText().toString().matches("")) 
			catEdit.putString("TaskCat4", postEdit.getText().toString());
		if (!plEdit.getText().toString().matches("")) 
			catEdit.putString("TaskCat8", plEdit.getText().toString());
		catEdit.commit();
		
		//--hide soft keyboard and exit
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 
	    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),      
	    InputMethodManager.HIDE_NOT_ALWAYS);
		//getActivity().finish();
		getFragmentManager().popBackStackImmediate();
	}
	//=====SECTION 5==MENU AND MENU METHODS============================
	
	

}	//END MAIN CLASS
