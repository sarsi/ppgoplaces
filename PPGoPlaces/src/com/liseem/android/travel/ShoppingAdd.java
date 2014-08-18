/* 
 * Fragment: 											ShoppingAll.java
 * Description:											Add Shopping item
 * 																Add new shopping item, tag to category and holiday
 * 
 * Created: 												May 3, 2012
 * Changed last release: 						September 9, 2012
 * Last updated: 										November 18, 2013
 * 
 * 
 * Associated files:
 * Intent call from MainMenu - 				PPGoPlaces.java
 * Extract db info via dbHelper - 			TravelLiteDBAdapter.java
 * View layout - 										add_shopping.xml
 * Spinner layout	 category	-					catdown_item.xml
 * 		and holiday list -								dropdown_item.xml
 * 
  * Changes:
 * 	- Tidy codes and documentation
 * - Extract all language strings to strings.xml
 * 
 * To do:
 * 
 */

package com.liseem.android.travel;

import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;
import static com.liseem.android.travel.TravelLiteActivity.*;

import java.util.ArrayList;

import com.liseem.android.travel.items.Holiday;
import com.liseem.android.travel.items.Shopping;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ToggleButton;

public class ShoppingAdd extends Fragment {

	//=====SECTION 1==DECLARATION===================================
	
	private final static String 			TAG="AddShopping";
	private PPGoPlacesApplication 	app;
	private SharedPreferences 			category;
	private SharedPreferences.Editor catEdit;

	
	//--View Specific--
	private EditText 							itemName;
	private EditText 							itemNotes;
	private EditText 							targetPrice;
	private Spinner 								holidaySpinner;
	private Spinner 								catSpinner;
	private TextView 							homeText;
	private ToggleButton 					toggleHome;
	
	//--Edit Mode Specific
	private long 									dbRowId;
	private boolean 							editItem=false;
	private int 										spinFix;
	private boolean 							holidayMode;
	private boolean							updated;

	//--Data Variables--
	private Shopping 							bbItem;
	private String 								dbName="";
	private String 								dbNotes="";
	private int 										holRefId=0;
	
	//--Lists---
	private ArrayList<Holiday> 			holidayList;
	private ArrayList<String> 			listItems;
	private ArrayList<Integer> 			questFRP;
	
	//--Spinner--
	private int 										selectList;			//store last spinner selection if toggle uncheck restore to bbitem
	private String[] 								shopListCats;	
	private ArrayAdapter<String>		catAdapter;
	private ArrayAdapter<String>		dataAdapter;
	
	//--Action Bar Menu------
	private String									actionText;

	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	
		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;
	
		//--inflate layout for fragment 1------------------------------------------------
		View v=inflater.inflate(R.layout.add_shopping, container, false);
		setHasOptionsMenu(true);								//--use fragment options menu inaddition to activity default
		setRetainInstance(true);								//--retain fragment instance in back stack
		return v;			
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		//--call ActionBar------------------------------------------------------------------
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.show();
		actionBar.setDisplayShowTitleEnabled(false);
	
		setupView();		//--2.0
	}
	
	@Override
	public void onResume() {
		super.onResume();	
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
	private void setupView() {
		
		//--setup application----------------------
		app								= (PPGoPlacesApplication)getActivity().getApplication();
		category						= getActivity().getSharedPreferences (CHECKLIST, MODE_PRIVATE);
		catEdit  						= category.edit();
		
		//--initialize item instance
		bbItem 						= new Shopping();

		
		//--intent get extras, for edit shopping item--
		Bundle bundle				= this.getArguments();							//--fragment retrieve bundle
		if (bundle != null) 
		{
			dbRowId					=bundle.getLong("rowid");
			editItem					=bundle.getBoolean("editmode");
			if (dbRowId > 0)
				bbItem=app.getItem(dbRowId);		//cannot retrieve if it is an add statement stupid
		}
		
		//--declare view layout information		
		itemName					= (EditText)getActivity().findViewById(R.id.itemName);
		itemNotes					= (EditText)getActivity().findViewById(R.id.itemNotes);
		targetPrice					= (EditText)getActivity().findViewById(R.id.targetPrice);
		holidaySpinner			= (Spinner)getActivity().findViewById(R.id.holidaySpinner);
		catSpinner					= (Spinner)getActivity().findViewById(R.id.catSpinner);
		homeText					= (TextView)getActivity().findViewById(R.id.homeText);
		toggleHome				= (ToggleButton)getActivity().findViewById(R.id.toggleHome);
		itemName.requestFocus();
		
		if (!editItem) {
			actionText="Add";
			toggleHome.setChecked(false);
		} else {
			actionText="Update";
		}
		
		//--Tag Holiday dropdown list for spinner
		loadList();						//--load holiday for spinner in addAdapter
		loadCategoryList();			//--load shopping category spinner in addAdapter()
		addItemsOnSpinner();
		
		//--Setup for Edit Mode--
		if (editItem) {
				itemName.setText(bbItem.getName().toString());
				itemNotes.setText(bbItem.getNotes().toString());
				targetPrice.setText(bbItem.getPrice().toString());
				holRefId=bbItem.getHolId();

				//--setup holiday spinner selection------------
				if (bbItem.getHolId()==0){
					holidaySpinner.setVisibility(View.INVISIBLE);
					toggleHome.setChecked(true);
					holidayMode=false;
					homeText.setVisibility(View.VISIBLE);
					homeText.setText(R.string.home_general);
				} else {
					spinFix=questFRP.indexOf(bbItem.getHolId());
					holidaySpinner.setSelection(spinFix);
					holidayMode=true;
				}
				//--setup shop category spinner-----------------
				if (bbItem.getCat() == null) {
					bbItem.setCat("1");					
				} else {
					String catStr=bbItem.getCat().toString();
					if (catStr != null && catStr.length()>0) {
						int catSpin=Integer.parseInt(catStr);
						if (catSpin > 0)
							catSpinner.setSelection(catSpin-1);
					}
				}
			}
	}
	
	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================
	public void addItemsOnSpinner() {
		
		//--add holiday spinner adapter-----------------------------------------------
		dataAdapter=new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_spinner_item, listItems);
		dataAdapter.setDropDownViewResource(R.layout.dropdown_item); //--custom to remove radio button
		holidaySpinner.setAdapter(dataAdapter);
		
		//--Set on ItemSelectedListner
		holidaySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				holidayMode=true;
				bbItem.setHolId(questFRP.get(position));
				selectList=bbItem.getHolId();		//??
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		//--check if holiday list is empty---------------------------
		if (listItems.isEmpty()) {
	        holidaySpinner.setVisibility(View.INVISIBLE);
	        homeText.setVisibility(View.VISIBLE);
	        homeText.setText(R.string.home_general);
	        holidayMode=false;
	        bbItem.setHolId(0);
	        toggleHome.setChecked(true);
		}

		//--add category spinner adapter-----------------------------------------------
		catAdapter=new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_spinner_item, shopListCats);
		catAdapter.setDropDownViewResource(R.layout.catdown_item); //custom to remove radio button
		catSpinner.setAdapter(catAdapter);

		//Set on ItemSelectedListner
		catSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {

				String catStr=Integer.toString(position+1); 		//0 reserve for showall, so cat need to plus 1
				bbItem.setCat(catStr);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

	} 	//END addAdapter()

	
	
	//--create string array list for category spinner---------------------------------------
	public void loadCategoryList() {
		ArrayList<String> catList = new ArrayList<String>();
		catList=app.loadShopListCat();
		
		//--String array used by spinner data adapter-------
		shopListCats=catList.toArray(new String[catList.size()]);
	}
	
	//--load holiday list for holiday spinner--------------------------------------------------
	public void loadList() {

		//--MUST ALWAYS REMEMBER TO INITIALIZE VARIABLES BEFORE ACCEPTING
		//--DATA !!!!!!!!!!!!!!!!
		holidayList		=new ArrayList<Holiday>();
		listItems			=new ArrayList<String>();
		questFRP			=new ArrayList<Integer>();
	 	holidayList.clear();
		listItems.clear();
		questFRP.clear();
		
		holidayList=app.loadHolidays();
		
		
		if (!holidayList.isEmpty()) {
		 	for (Holiday h: holidayList) {
		 		listItems.add(h.getName());
		 		questFRP.add(h.geHolId());
		 	}
		}
		
		
		if (editItem) {
			if (holRefId > 0) {
				try {
			 		Holiday tempHol = new Holiday();
			 		tempHol=app.getHoliday(bbItem.getRefid());	 	
				} catch (Exception e) {
					bbItem.setHolId(0);
					app.updateItem(bbItem);
				} 
			}
		}
	}
			
	//=====SECTION 4==ADD ADAPTER===================================

	//=====SECTION 5==SET LISTENER====================================
	
	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================	
	public void setListener() {
		
		toggleHome.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				//--Perform action on clicks
			    if (isChecked) {
			        holidaySpinner.setVisibility(View.INVISIBLE);
			        homeText.setVisibility(View.VISIBLE);
			        //homeText.setText("\t\t"+getString(R.string.home_general));
			        holidayMode=false;
			        bbItem.setHolId(0);
			        //Toast.makeText(getBaseContext(), "selectList "+bbItem.getHolId(), Toast.LENGTH_SHORT).show();
			    } else {
			        holidaySpinner.setVisibility(View.VISIBLE);
			        homeText.setVisibility(View.INVISIBLE);
			        bbItem.setHolId(selectList);
			        holidayMode=true;
			        //Toast.makeText(getBaseContext(), "selectList "+bbItem.getHolId(), Toast.LENGTH_SHORT).show();
			    }
			}			
		});		
	}


	//=====SECTION 6==LISTENER METHODS================================

	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
			super.onPrepareOptionsMenu(menu);

			menu.removeItem(9);			//--removed Calculator
			menu.removeItem(10);		//--removed setting
	}

	private void CreateMenu(Menu menu) {
		
		MenuItem mnu1=menu.add(0, 1, 1, actionText); 
			{ //mnu1.setIcon(android.R.drawable.ic_menu_agenda);
			mnu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}			//--put on action bar
			
	}
	
	private boolean MenuChoice(MenuItem item)
	{
		switch (item.getItemId()) {
		case 1:
			addNewItem();
			return true;		
		}
		return false;
	}
	
	//--save new shopping item
	public void addNewItem() {
		
		/* 
		 * DATA STAGING FOR DB UPDATE
		 * DATE STRING as ISO8601 strings ("YYYY-MM-DD HH:MM:SS.SSS")
		 */
		//--Here is my 3 cuprits that wasted a whole day--
			bbItem.setName(itemName.getText().toString());
			bbItem.setNotes(itemNotes.getText().toString());
			bbItem.setPrice(targetPrice.getText().toString());
			
		if (bbItem.getName().length()<1) {
			AlertDialog show = new AlertDialog.Builder(getActivity())
			.setTitle("Empty Item Name")
			.setMessage("Please Kindly Enter Item.")
			.setPositiveButton("OK", null).show();		
			
		} else {
			if (editItem) {
				updated=app.updateItem(bbItem);
				if (updated) {
					Intent intent =new Intent();
					getActivity().setResult(RESULT_OK, intent);
				} else {
					Intent intent=new Intent();
					getActivity().setResult(RESULT_CANCELED, intent);
				}
			} else {
				long addNew=app.addItem(bbItem);
				if (addNew>0) {
					Intent intent =new Intent();
					intent.putExtra(ITEM_ROWID, addNew);
					getActivity().setResult(RESULT_OK, intent);
				} else {
					Intent intent=new Intent();
					getActivity().setResult(RESULT_CANCELED, intent);
				}				
				((TravelLiteActivity) getActivity()).updateTransB4bup();
			}
			//getActivity().finish();
			//--return to previous calling fragment
			getFragmentManager().popBackStackImmediate();
		}			
	}

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//=====SECTION 10==SANDBOX======================================

	
} 	// END MAIN CLASS


