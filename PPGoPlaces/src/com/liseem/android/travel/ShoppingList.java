/* 
 * Fragment: 											ShoppingList.java
 * Description:											Shopping List View
 * 																Shopping list with picture, target price, tag to holiday and mail to friend
 * 
 * Created: 												May 3, 2012
 * Changed last release: 						September 9, 2012
 * Last updated: 										September 22, 2013
 * 
 * 
 * Associated files:
 * Intent call from MainMenu - 				PPGoPlaces.java
 * Extract db info via dbHelper - 			TravelLiteDBAdapter.java
 * Custom view adapter - 						ShoppingListItem.java and custom_list.xml
 * dataAapter spinner drop down list - 	dropdown-list.xml
 * catAdapter spinner drop down list - 	cat_item.xml
 * 
  * Changes:
 * 	- Tidy codes and documentation
 * - Extract all language strings to strings.xml
 * - Fixed bug on picture display for hi-res picture
 * - AsyncTask for bitmap scaling and orientation
 * 
 * Need to fix:
 * - complex refresh of list for selectList and hide/showall
 * 
 */


package com.liseem.android.travel;


import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;
import static com.liseem.android.travel.TravelLiteActivity.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import com.liseem.android.travel.adapter.ItemListAdapter;
import com.liseem.android.travel.items.Holiday;
import com.liseem.android.travel.items.Shopping;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;

public class ShoppingList extends Fragment {

	//=====SECTION 1==DECLARATION===================================

	private static final String 					TAG="ShoppingList";
	private final static int 							TAKE_PICTURE=101;	//--v1.1
	
	private OnMenuSelectedListener 			mCallback;

	
	//--Dialog Menu--
	private CharSequence[] 						items = {
																	EDIT_ITEM, 
																	DELETE_ITEM,
																	DELETE_ITEMS,
																	CANCEL };	
	
	//--application--------------------------------------
	private PPGoPlacesApplication 			app;
	private SharedPreferences 					prefs;
	private SharedPreferences 					category;
	private SharedPreferences.Editor 			editor;
	private SharedPreferences.Editor 			catEditor;
	boolean[] 												itemsChecked = new boolean [items.length];
	
	private ArrayList<String> 					listItems;
	private ArrayList<Integer> 					questFRP;
	private ArrayList<Shopping> 				shoppingList;
	private ArrayList<Holiday> 					holidayList;
	
	private Shopping 									bbItem;
	private String[] 										shopListCats;

	private ItemListAdapter 						adapter;
	private ArrayAdapter<String>				catAdapter;
	private Spinner 										catSpinner;
	private ListView 									listView;				
	private TextView 									emptyList;
	private AlertDialog 								removeItem;
	private EditText 									quickText;
	private Button 										quickButton;
	private TextView 									infoText;
	
	//--Filter parameters pass to adapter for display--
	private ToggleButton								hideComplete;	
	private int	 											selectList=0;
	private boolean 									notComplete;
	private long 											holRefId;					//--filter view by holiday from view holiday
	private boolean									holidayLoc;
	
	private String											viewSelected;
	private String											shopShowAll;			//--text for ShowAll/Hide action bar button
	private boolean									shopHide;				//--boolean for prefs and toggle status
	private Button 										addShopping;
	private String 										smartInput;
	private String 										fxRate;
	
	//--show picture--------------
	private Bitmap 										bm;
	
	//--picture taking------------
	private Dialog 										replacePicture;
	private String 										imagePath;					//--new picture path
	private boolean 									pictureExist;				//--check picture already existed for item
	private String 										emailSignature;
	private StringBuilder 								emailBody;
	

	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.view_shopping);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		setHasOptionsMenu(true);								//--use fragment options menu instead of activity default
		//setRetainInstance(true);

		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;

		//--inflate layout for fragment 1------------------------------------------------
		View v=inflater.inflate(R.layout.view_shopping, container, false);
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

		setupView();		//--2.0
		addAdapter();		//--3.0
		setListener();		//--4.0		
				
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		smartInput=quickText.toString();
		if(smartInput !=null)
			outState.putString("smartInput", smartInput);
		if(infoText != null)
			outState.putString("infoText", infoText.toString());
	}

	/*@Override
	public void onBackPressed() {
		super.onBackPressed();
	}*/
	
	/*@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		smartInput=savedInstanceState.getString("smartInput");
		infoText.setText(savedInstanceState.getString("infoText"));
		if (smartInput !=null) {
			quickText.setText(smartInput.toString());
			quickButton.setClickable(true);
		}
	}*/
	
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
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
		//return true;		
	}

	//--Return menu choice on menu selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		return MenuChoice(item);
	}
	
	//--part 1 of 2 - container fragment interface for activity callback-----
	public interface OnMenuSelectedListener {
		//public void OnMenuSelected (String param, boolean bol, long id, int func);		
		public void OnMenuSelected (int func, Bundle bundle);		
	}	

	//--part 2 of 2 - container fragment interface for activity callback-----
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mCallback=(OnMenuSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException (activity.toString()
					+"must implement OnMenuSelectedListener");
		}
	}

	
		
	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 3==SETUP VIEWS====================================
	
	private void setupView() {
		
		//--setup path to main application--
		app						=(PPGoPlacesApplication)getActivity().getApplication();
		bbItem					=new Shopping();
		
		//--retrieve shared preferences-----------------------------
		prefs					=	getActivity().getSharedPreferences (PREFNAME, MODE_PRIVATE);
		category				=	getActivity().getSharedPreferences (CHECKLIST, MODE_PRIVATE);
		editor					= prefs.edit();
		catEditor  			= 	category.edit();
 		
		fxRate					=	prefs.getString("fxRate","1");			//--retreive fx rate
		shopHide				=	prefs.getBoolean("shopHide", false);	//--last ShowAll status
		notComplete		= shopHide;										//--notComplete adapter
		if (shopHide) {
			shopShowAll	=	getString(R.string.show_all);			//--hide complete, button display show all
		} else {
			shopShowAll	=	getString(R.string.hide);
		}
		emailSignature	=	app.getSignature();							//--get email signature from app
		
		//--check for shopping list category exist-----------------
		boolean checkCat=category.contains("ShopCat1");
		if (!checkCat) {
			createCategory();
		}

		//--retrieve bundle from view holiday intent-------------
		Bundle hextras = this.getArguments();							//--fragment retrieve bundle
		//Bundle hextras=getActivity().getIntent().getExtras(); //--old activity bundle
		if (hextras !=null) {
			holRefId			=hextras.getLong(HOL_REFID);
			holidayLoc		=hextras.getBoolean(FILTER_LOCATION);
			//currentLocation=getIntent().getParcelableExtra("MyLocation");					//--get location via parcelable extra
		}
		
	
		//--setup views--
		addShopping		=(Button)getActivity().findViewById(R.id.addButton);
		listView				=(ListView)getActivity().findViewById(R.id.listItem);
		emptyList			=(TextView)getActivity().findViewById(R.id.emptyList);
		//shopCat				=(Spinner)getActivity().findViewById(R.id.shopCat);				//--v1.0 switch shopping to category view
		catSpinner			=(Spinner)getActivity().findViewById(R.id.shopCat);
		
		hideComplete		=(ToggleButton)getActivity().findViewById(R.id.showNotcomplete);
		infoText				=(TextView)getActivity().findViewById(R.id.infoText);
		quickText			=(EditText)getActivity().findViewById(R.id.quickText);			//--fx edit box
		quickButton			=(Button)getActivity().findViewById(R.id.quickButton);			//--quick convert
		
		listView.setEmptyView(emptyList);
		infoText.setText("");
		quickButton.setText(R.string.convert);
		quickButton.setClickable(false);
		
		//--BUTTON OBSOLETED BY ACTION BAR--------------
		addShopping.setVisibility(View.GONE);
		hideComplete.setVisibility(View.GONE);
		
		
		//--add text watcher for quick button---------------------------------
		quickText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				
				int lastCount=quickText.length();			
				if (s.length()>0 || lastCount >0) 
					quickButton.setClickable(true);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start,
					int count, int after) {
				
				int lastCount=quickText.length();			
				if (count>0 || lastCount >0) 
					quickButton.setClickable(true);
					}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
					
					int lastCount=quickText.length();
					if (count < 1 || lastCount <1)
						quickButton.setClickable(false);
			}
			
		});
	
	    //--forcefully close soft keyboard by default----------------------------------
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	    		//imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
	    imm.hideSoftInputFromWindow(quickText.getWindowToken(),0);
	    
		//--force soft keyboard close by default---------------------------------------
		//getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
	}	//END 2.1 SetupView
	
	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================

	public void addAdapter() {
		/*
		 * ListView, setAapter to inner custom adapter class ItemListAdapter
		 * passing to it shoppingList (ArrayList<Shopping> shoppingList> 
		 * contains object instances of getAllShopping, query from 
		 * TravelLiteDBAdapter.
		 */
		shoppingList 		= new ArrayList<Shopping>();
		
		if (holidayLoc) {
			shoppingList	=app.getByRefId(holRefId);
		} else {
			shoppingList	=app.getAllItems();
			//Log.d(TAG, " in not holidayLoc, shoppingList size "+shoppingList.size());
		}
		
		adapter=new ItemListAdapter(getActivity(), shoppingList);
		listView.setAdapter(adapter);
		registerForContextMenu(listView);				//--tag context menu to list view
		
		//loadList();													//--get ListItems for spinner only
		loadCategoryList();										//--retrieve category list for shopping list from shared preference check list
		
		//--add category spinner adapter-----------------------------------------------
		catAdapter=new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_spinner_item, shopListCats);
		catAdapter.setDropDownViewResource(R.layout.catdown_item); //custom to remove radio button
		catSpinner.setAdapter(catAdapter);
		catSpinner.setSelection(0);
	
		//--setup list of holiday on spinner -- 
		/*dataAdapter=new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, listItems);
		dataAdapter.setDropDownViewResource(R.layout.dropdown_item); //custom to remove radio button
		shopCat.setAdapter(dataAdapter);
		
		shopCat.setSelection(0);		//default display all for now.*/
		
	}	//END addAdapter()
	
	
	//--retrieve spinner list for shopping list for category spinner---------------------------------------
	//--call from addAdapter()
	public void loadCategoryList() {

		ArrayList<String> catList = new ArrayList<String>();
		ArrayList<String> getList = new ArrayList<String>();
		
		//--add first category to show all shopping list
		catList.add(getActivity().getString(R.string.show_all));
		
		//--load all shopping category from application helpers
		getList=app.loadShopListCat();
		
		//--add each category to categrory list
		for(String s : getList) {
			catList.add(s);
		}
		
		//--String array used by spinner data adapter-------
		shopListCats=catList.toArray(new String[catList.size()]);
	}	//--END loadCategoryList() {
	
	/*
	 * call from 2.1 setListAdapter return getAllShopping in 
	 * ArrayList<Shopping> shoppingList for use by adapter to inflate
	 * view. 
	 * 
	 * Improve codes shortest it can be via passing of Shopping ArrayList
	 * object without handing db query.
	 */
	//v1.0
	public void loadList() {
	
		holidayList		=	new ArrayList<Holiday>();				
		questFRP			=	new ArrayList<Integer>();
	 	listItems			=	new ArrayList<String>();
		shoppingList	=	new ArrayList<Shopping>();
		
		holidayList		=app.loadHolidays();		


		ArrayList<Integer> tempFRP	=	new ArrayList<Integer>();
	 	ArrayList<String>tempList		=	new ArrayList<String>();

		shoppingList=adapter.getShoppingList();		//--get current shopping list for listview
		
		tempList.add(getString(R.string.show_all_items));
		tempFRP.add(0);

		//--add Home, General into spinner listItems---------
		for (Shopping s : shoppingList) {
			if (s.getHolId()==0) {
				tempList.add(getActivity().getString(R.string.home_general));
				tempFRP.add(0);
				break;
			}
		}
		
	 	//--only display holidays with shopping items---------
	 	for (Holiday h: holidayList) {
	 		boolean duplicate=false;
	 		for (Shopping s : shoppingList) {
	 			if (h.getId()==s.getHolId() && !duplicate) {	 
	 				tempList.add(h.getHoliday());
	 				tempFRP.add(h.geHolId());
			 		duplicate=true;
	 			}
	 		}
	 	}
	 	
		listItems.clear();
		listItems=tempList;
		questFRP.clear();		
		questFRP=tempFRP;
	 	
	} //end of loadList()
	

	//=====SECTION 4==ADD ADAPTER===================================

	//=====SECTION 5==SET LISTENER====================================
	//listening to the check item select and unselect action

	public void setListener() {
		
		//--shopping item checkbox listener-------------------------------------------
		listView.setOnItemClickListener(new checkBoxCheckedListener());
			    
		//--add new shopping item--
		//addShopping.setOnClickListener( new addButtonListener());	
		
		//--toggle button listener for hide completed items-----------------------
		hideComplete.setOnCheckedChangeListener(new hideCompletedCheckedListener());
		
		//v1.0
		//--on item selected spinner FILTER by shopping category -------------
		//shopCat.setOnItemSelectedListener(new spinnerSelectionListener());

		//--on item selected spinner FILTER by shopping category -------------
		catSpinner.setOnItemSelectedListener(new categorySelectionListener());
		
		//--quick button on click listener for fx convert----------------------------
		quickButton.setOnClickListener(new quickOnClickListener());
		
	}	//END setListener
	
	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================
	
	//--quick conversion button----------------------------
	private class quickOnClickListener implements OnClickListener {
		
		@Override
		public void onClick (View v) {
			smartInput=quickText.getText().toString();
			String checkFx=((TravelLiteActivity) getActivity()).for2home(smartInput, fxRate);
			infoText.setText(getString(R.string.foreign_)+smartInput+" | "+ getString(R.string.home_)+checkFx);
		}
	}
	
	//--category spinner selection---------------------------------------------------------------------------------
	private class categorySelectionListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position,
				long id) {
			if (position==0) {} else {}
			selectList=position;			

			adapter.spinnerCatSelection(position, notComplete);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) { }
	}
	
	
	//--holiday spinner selection---------------------------------------------------------------------------------
	//--DISABLE, need to review user logic------------------------
	private class spinnerSelectionListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position,
				long id) {
		
				selectList=questFRP.get(position);
				if (position==0) {} else {}
				
				//v1.0
				//adapter.filterItems(showAll, selectList, notComplete);
				//Log.d(TAG,"adapter.filterItems showAll, selectList, notComplete "+showAll+", "+selectList+", "+notComplete);
				
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {		}
		
	}
	
	//--shopping item checkbox checked toggle listener----------------------------------------
	private class checkBoxCheckedListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			bbItem=adapter.getItem(position);
			bbItem.toggleComplete();
			app.updateItem(bbItem);
			adapter.toggleItemSelect(position);	
			//adapter.toggleItem(bbItem);
		}
		
	}
	
	//--REPLACED BY ActionBar
	//--toggle button to FILTER hide all completed tasks-----------------------------------------
	//-- to prevent list out of sync, two parameters isChecked and selectList
	//--need to be sent to the adapter--
	private class hideCompletedCheckedListener implements OnCheckedChangeListener	 {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
						
			editor.putBoolean("shopHide", isChecked);
			editor.commit();
			
			notComplete=isChecked;
			adapter.spinnerCatSelection(selectList, notComplete);
			//adapter.filterItems(showAll, selectList, isChecked);
		}		
	}

	//--REPLACED BY ActionBar
	/*private class addButtonListener implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			//Intent intent=new Intent(ShoppingList.this, ShoppingAdd.class);
			Bundle extras=new Bundle();
			extras.putLong("rowid", 0);
			extras.putBoolean("editmode", false);
			mCallback.OnMenuSelected(12, extras);
			//intent.putExtras(extras);
			//startActivityForResult(intent, SHOPPING_ADD);
		}
	} 		*/

	//--onActivityResult RESULT from taking picture--------------------------------------------------
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	
		//--TAKE PICTURE-------------------------------------------------------------------------  
		if (requestCode==TAKE_PICTURE && resultCode==RESULT_OK) {
			
			//--delete previous picture from phone directory---------------------
			if (pictureExist) {
				pictureExist=false;
				deleteFromSDcard(bbItem.getAddress().toString());
			}

			//String imagePath;
			if(data.getData()!=null) {
				imagePath=((TravelLiteActivity) getActivity()).getPath(data.getData());
			} else {
				imagePath=((TravelLiteActivity) getActivity()).getLastImagePath();			//for samsung phone return data null
				//imagePath=getPath(imageUri);				
			}
			
			bbItem.setAddress(imagePath.toString());
			app.updateItem(bbItem);
			adapter.notifyDataSetChanged();
			((TravelLiteActivity) getActivity()).makeShortToast(getString(R.string.nice_shot_new_picture_added_to_item));
		} else if (requestCode==SHOPPING_ADD) {
			if (resultCode==RESULT_OK) {
				/*Shopping newItem=new Shopping();
				long newItemId=data.getLongExtra(ITEM_ROWID, 0);
				newItem=app.getItem(newItemId);
				adapter.addItem(newItem);
				//buildListItems();
				loadList();
				dataAdapter.notifyDataSetChanged();*/
			}else if (resultCode==RESULT_CANCELED) {
				
			}
		}else if (requestCode==SHOPPING_EDIT) {
			if (resultCode==RESULT_OK) {
				/*bbItem=new Shopping();
				bbItem=app.getItem(oldItem.getId());
				adapter.updateItem(bbItem, oldItem);
				loadList();
				dataAdapter.notifyDataSetChanged();*/
			} else if (resultCode==RESULT_CANCELED) {
				
			}
		}
	}	//--END onActivityResult
	
	//=====SECTION 6==LISTENER METHODS================================

	//=====SECTION 7==HELPER METHODS==================================
	
	//--create sharedPreferences for shopping list category-----
	protected void createCategory() {
		catEditor.putInt("Default", 1);
		catEditor.putString("ShopCat1", SHOP_CAT_1);
		catEditor.putString("ShopCat2", SHOP_CAT_2);
		catEditor.putString("ShopCat3", SHOP_CAT_3);
		catEditor.putString("ShopCat4", SHOP_CAT_4);
		catEditor.putString("ShopCat5", SHOP_CAT_5);
		catEditor.putString("ShopCat6", SHOP_CAT_6);
		catEditor.putString("ShopCat7", SHOP_CAT_7);
		catEditor.putString("ShopCat8", SHOP_CAT_8);
		catEditor.commit();
	}
	
	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	//--menu listing action from above context menu choice---------------------------------------
	//--remark out as there is no default activity menu
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
			menu.removeItem(10);
			menu.removeItem(11);
	}
	
	private void CreateMenu(Menu menu) {
		
		//app.CreateMenu(menu);		
		//--replace menu 2 with trip calculator------------------------
		//menu.removeItem(2);
		
		MenuItem mnu1=menu.add(0, 1 , 1, R.string.setup_fx_rate);
			{ mnu1.setIcon(android.R.drawable.ic_menu_agenda); }
		
		MenuItem mnu2=menu.add(0, 2, 2,R.string.trip_calculator); 
			{	mnu2.setIcon(android.R.drawable.ic_dialog_dialer);}
		
		MenuItem mnu3=menu.add(0, 3, 3, R.string.rename_category); 
			{	mnu3.setIcon(android.R.drawable.ic_menu_edit);}
			
		MenuItem mnu4=menu.add(0, 4, 4, R.string.settings);
			{ mnu4.setIcon(android.R.drawable.ic_menu_preferences); }
	
		MenuItem mnu5=menu.add(0, 5, 5, R.string.delete_all_completed);
			{ mnu5.setIcon(android.R.drawable.ic_menu_delete); }
			
		//MenuItem mnu6=menu.add(0, 6, 6, R.string.help);
			//{ mnu6.setIcon(android.R.drawable.ic_menu_help); }
		
		MenuItem mnu7=menu.add(0, 7, 7, shopShowAll);
			{ 	//mnu7.setIcon(android.R.drawable.ic_menu_more);
				//mnu7.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
				mnu7.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}		
			
		MenuItem mnu8=menu.add(0, 8, 8, R.string.new_);
			{ 	mnu8.setIcon(android.R.drawable.ic_menu_add); 			//ic_input_add
				//mnu8.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
				mnu8.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}		
		
		MenuItem mnu9=menu.add(0, 9, 9, R.string.help);
			{ 	mnu9.setIcon(android.R.drawable.ic_menu_help); 			//ic_menu_help
				//mnu9.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
				mnu9.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);}		

					
	}
	
	private boolean MenuChoice(MenuItem item)
	{
		switch (item.getItemId()) {

		/*case 0:
			Intent myLocation=new Intent (ShoppingList.this, ViewLocation.class);
			startActivity(myLocation);
			return true;
		case 1:
			Intent myHoliday=new Intent (ShoppingList.this, ViewHoliday.class);
			startActivity(myHoliday);
			return true; */
			
		case 1:
			//--goto tip and fx dialog---------------
			Bundle kextras = new Bundle();
			mCallback.OnMenuSelected(15, kextras);				//--tip and fx case 15		

			//Intent convFx=new Intent (ShoppingList.this, TipAndFxDialog.class);
			//startActivity(convFx);	
			return true;
			
		case 2:
			//--goto trip calculator---------
			Bundle qextras = new Bundle();
			mCallback.OnMenuSelected(14, qextras);				//--Trip calculator case 14
			
			//Intent calThis=new Intent (ShoppingList.this, TripCalculator.class);
			//startActivity(calThis);		
			return true;
			
		case 3:
			//--goto Shopping Edit-----------
			Bundle sextras = new Bundle();
			mCallback.OnMenuSelected(22, sextras);
			//Intent changeName = new Intent(this, ShoppingEditCat.class);
			//startActivity(changeName);
			return true; 
			
		case 4:
			//--goto Settings---------------------
			((FragMain) getActivity()).callMyPreferences();
			//mCallback.OnMenuSelected(13, extras);				//--Settings case 13		

			//Intent settings=new Intent (ShoppingList.this, MyPreferences.class);
			//startActivity(settings);			
			return true;
			
		case 5:
			removeCompletedItems();
			return true;
			
		/*case 6:
			//--goto Help----------------------------
			Bundle oextras  = new Bundle();
			oextras.putString("helpPage", "shopping.html");
			mCallback.OnMenuSelected(18, oextras);				//--Help case 19
			
			//Intent  simplehelp=new Intent(ShoppingList.this, SimpleHelp.class);
			//	new Bundle();
			//simplehelp.putExtra("helpPage", "shopping.html");
			//startActivity(simplehelp);
			return true;*/
		
		case 7:
			if (shopHide) {
				item.setTitle(R.string.hide);
			} else {
				item.setTitle(R.string.show_all);
			}
			editor.putBoolean("shopHide", !shopHide);			//--update prefs with showall choice
			editor.commit();
			notComplete=!notComplete;
			adapter.spinnerCatSelection(selectList, notComplete);
			return true;
		
		case 8:
			//--add new shop item---------------------------------------
			Bundle extras=new Bundle();
			extras.putLong("rowid", 0);
			extras.putBoolean("editmode", false);
			mCallback.OnMenuSelected(12, extras);			
			return true;
		
		case 9:
			//--goto Help----------------------------------------------------
			Bundle yextras  = new Bundle();
			yextras.putString("helpPage", "shopping.html");
			mCallback.OnMenuSelected(18, yextras);				//--Help case 19
			return true;
			
		}
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
			menu.setHeaderIcon(android.R.drawable.ic_menu_more);
			menu.setHeaderTitle(R.string.my_shopping_list);
			MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.context_menu, menu);
			menu.add(0, 3, 3, R.string.take_picture);
			menu.add(0, 4, 4, R.string.view_picture);
			menu.add(0, 5, 5, R.string.send_item);
			super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item){
		AdapterContextMenuInfo info=(AdapterContextMenuInfo) item.getMenuInfo();
		bbItem=adapter.getItem(info.position);
		switch (item.getItemId()) {
			case R.id.editItem:
				//--Edit Shopping Item------------------------------------------
				//oldItem=new Shopping();		v1.0
				//oldItem=bbItem;					v1.0
				app.setLastItem(bbItem);
				app.setLastPosition(selectList);
				//Intent intent=new Intent(ShoppingList.this, ShoppingAdd.class);				
				Bundle extras=new Bundle();
				extras.putLong("rowid", bbItem.getId());
				extras.putBoolean("editmode", true);
				mCallback.OnMenuSelected(12, extras);
				//intent.putExtras(extras);
				//startActivityForResult(intent, SHOPPING_EDIT);
				return true;
			case R.id.deleteItem:
				//--DELETE ITEM---------------------------------------------
				deleteThisItem();
				return true;
			case 3:
				//--TAKE PICTURE---------------------------------------------
				if (bbItem.getAddress() !=null) {
					File tempFile=new File(bbItem.getAddress());
					if (tempFile.exists()) { //--data exist and file exist-----------------------
						pictureExist=true;
						showReplaceDialog();
					} else { //--if data exist but picture got deleted from phone-----------
						pictureExist=false;
						Intent sayCheese=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						startActivityForResult(sayCheese, TAKE_PICTURE);	
					}
				} else {
					Intent sayCheese=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					startActivityForResult(sayCheese, TAKE_PICTURE);	
				}
				return true;
			case 4:
				//--VIEW PICTURE---------------------------------------------
				if (bbItem.getAddress() !=null) {
					File pictureFile=new File(bbItem.getAddress().toString());
					if (pictureFile.exists()) {
						showPictureDialog();
					}
				}
				return true;
			case 5:
				//--SEND PICTURE---------------------------------------------
				shareShoppingItem();
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	//--pop up menu case 2 - delete item
	public void deleteThisItem() {
		removeItem= new AlertDialog.Builder(getActivity())
		.setTitle(viewSelected)
		.setMessage("Delete this Item?"+
				"\n\nNOTE: \nDeleted record is not recoverable. ")
		.setPositiveButton("YES, Delete", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				removeThisItem();
				removeItem.dismiss();
			}
		})
		.setNegativeButton("Cancel Action", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				removeItem.cancel();
			}
		})
		.create();
		removeItem.show();
	} 
	
	private void removeThisItem() {
		app.deleteItem(bbItem);
		adapter.deleteItem(bbItem);
		//shoppingList.remove(contextItemPosition);		//rebuild listItems for spinner
		//loadList();															//--holiday spinner obsoleted
		//dataAdapter.notifyDataSetChanged();				//--holiday spinner obsoleted
		//buildListItems();
		//dataAdapter.notifyDataSetChanged();				//refresh spinner list
	}

	//-- menu case 5 - delete all completed items !!!	
	public void removeCompletedItems() {
		removeItem= new AlertDialog.Builder(getActivity())
		.setTitle(viewSelected)
		.setMessage("Delete All Completed Items?"+
				"\n\nNOTE: \nDeleted records are not recoverable. ")
		.setPositiveButton("YES, Delete", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				removeAllCompletedItems();
				removeItem.dismiss();
			}
		})
		.setNegativeButton("Cancel Action", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				removeItem.cancel();
			}
		})
		.create();
		removeItem.show();
	}
	
	private void removeAllCompletedItems() {
		ArrayList<Long> completeList=new ArrayList<Long>();
		completeList=adapter.getCompletedItems();
		if (!completeList.isEmpty())
			app.removeItems(completeList);
		
		adapter.deleteCompletedItems();
		
		//ArrayList<Shopping> tempHelp=new ArrayList<Shopping>();
		//tempHelp=adapter.deleteAllCompletedItems();
		//shoppingList=tempHelp;
		//loadList();
		//dataAdapter.notifyDataSetChanged();
		//buildListItems();											//rebuild listItems from shoppingList
	}
			
	//--replace existing picture user choice---------------------------------
	public void showReplaceDialog() {
		replacePicture= new AlertDialog.Builder(getActivity())
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Replace Picture")
		.setMessage("Current Picture Will Be Deleted?")
		.setPositiveButton("YES, Replace", new AlertDialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, TAKE_PICTURE);					
				//bbItem.setAddress(imagePath.toString());
				//deleteFromSDcard();
				replacePicture.dismiss();
			}
		})
		.setNegativeButton("Cancel Action", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				replacePicture.cancel();
			}
		})
		.create();
		replacePicture.show();
	}
	
	//--!ALERT-------delete from sdcard--------------------!ALERT--------------------------
	public void deleteFromSDcard(String deletePath) {
		File tempFile=new File(deletePath.toString());
		//app.deletePicture(pictureList.get(filePosition).getId());
		//pictureList.remove(filePosition);		
		if (tempFile.exists())
			new deleteFileTask().execute(tempFile);		//removing from imageList via Async Task		
	}
	
	//--VIEW PICTURE FOR SHOPPING------------------------------------------------------------
	//--display picture dialog-------------------------------------------------------
	public void showPictureDialog() {
		//--AsyncTask to get scaled bitmap with corrected orientation
		try {
			Log.d(TAG, "in showPictureDialog before AsyncTask");
			bm=new scaleBitmap(getActivity().getBaseContext()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, bbItem).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		Log.d(TAG, "cameback from showPictureDialog before AsyncTask");
		
		//--gather information for target price and item notes---------------
		StringBuilder itemNotes=new StringBuilder();		
		/*if (bbItem.getPrice()!=null && bbItem.getPrice() !="0") {
			itemNotes.append("Target Price: ").append(bbItem.getPrice().toString());
		}*/
		
		if (bbItem.getNotes() != null && bbItem.getNotes() !="") {
			itemNotes.append("\nNotes: ").append(bbItem.getNotes().toString());
		}
		
		//--show alert dialog-----------------------------------
		AlertDialog.Builder showPicture = new AlertDialog.Builder(getActivity());
		
		showPicture.setTitle(bbItem.getName().toString());
		showPicture.setIcon(R.drawable.ppgp_pt);
		LayoutInflater showBiz=LayoutInflater.from(getActivity());
		View itemPhoto=showBiz.inflate(R.layout.item_image, null);
		TextView text = (TextView) itemPhoto.findViewById(R.id.itemInfo);
		ImageView image = (ImageView) itemPhoto.findViewById(R.id.itemImage);
		text.setMovementMethod(ScrollingMovementMethod.getInstance());
		
		if (itemNotes.length()>0) {
			text.setText(itemNotes.toString()); 
		} else {
			text.setVisibility(View.INVISIBLE);
		}
		image.setImageBitmap(bm);
		showPicture.setView(itemPhoto);
		showPicture.setPositiveButton("OK", new AlertDialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
			
		});
		showPicture.create().show();
	}
	

	
	//--share shopping item to fellow PPGoPlaces------------------------------------------
	public void shareShoppingItem() {
		emailBody=new StringBuilder();
		AlertDialog.Builder signName= new AlertDialog.Builder(getActivity());
		signName.setIcon(android.R.drawable.ic_menu_edit);
		signName.setTitle("Add Message");
		signName.setMessage("Enter Message below: ");

		// Set an EditText view to get user input 
		final EditText input = new EditText(getActivity());
		input.setLines(10);
		input.setMovementMethod(ScrollingMovementMethod.getInstance());
		//input.setTypeface(Typeface.SERIF);
		input.setGravity(1);
		input.setBackgroundResource(R.drawable.edittext_brown_border);
		input.setPadding(5, 5, 5, 5);
		input.setVerticalScrollBarEnabled(true);
		input.setGravity(Gravity.LEFT);
		input.setImeOptions(EditorInfo.IME_ACTION_SEND);
		signName.setView(input);
		
		signName.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
					Editable value = input.getText();
			  		emailBody.append(value.toString());
			  		goSendMail();
			  }
		});
		signName.show();		
	}
	
	public void goSendMail() {
		
		StringBuilder fileInfo=new StringBuilder();
		
		//--email body text by user----------------------------------
		fileInfo.append(emailBody.toString()).append("\n\n");
		
		//--item details------------------------------------------		
		fileInfo.append("Name: ").append(bbItem.getName().toString());		
		if (bbItem.getPrice() !=null)
			fileInfo.append("\nTarget Price: ").append(bbItem.getPrice().toString());
		if(bbItem.getNotes() !=null)
			fileInfo.append("\nNotes: ").append(bbItem.getNotes().toString());
		
		//--email signature--------------------------------------------
		fileInfo.append("\n\n")
			.append("[").append(emailSignature.toString()).append("]");
		



		//--setup intent to send mail----------------------
		//String mailId="";
		//Intent shareIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", mailId, null));
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
	    shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
	    shareIntent.setType("image/*");
	    //shareIntent.setType("message/rfc822");
	    shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "[Shopping Item] "+bbItem.getName().toString());
	    shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, fileInfo.toString());
	    
	    //--For a file in shared storage.  For data in private storage, use a ContentProvider.
	    //Uri uri = Uri.fromFile(getFileStreamPath(picture.getPicPath().toString()));

	    
	    //--check file name for null---------------------------			
		if (bbItem.getAddress() !=null) {
			bbItem.getAddress().toString();
		    File fileSelected=new File(bbItem.getAddress().toString());
		    if (fileSelected.exists()) {
		    	Uri uri=Uri.fromFile(fileSelected);
		    	shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
		    }
		}	
			 	
	   //--start intent to send mail-------------------------------
	    startActivity(Intent.createChooser(shareIntent, "Send Email.."));	
	}

	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//--async task to delete file-------------------------------------------
	private class deleteFileTask extends AsyncTask <File, Void, Boolean> {

		@Override
		protected Boolean doInBackground(File... params) {
			
				for (File f: params) {
					return f.delete();
				}
				return null;			
		}
		
		@Override
		protected void onPostExecute(Boolean deleteStatus) {
			if (deleteStatus) Toast.makeText(getActivity().getApplicationContext(), R.string.previous_picture_deleted, Toast.LENGTH_SHORT).show();
		}
	}

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//=====SECTION 10==SANDBOX======================================

	//--thread to return scale down picture for view picture, context menu 4---------------------
	//--build 156, Sep 22, 2013
	public class scaleBitmap extends AsyncTask <Shopping, Void, Bitmap> {
		
		private Context context;

		public scaleBitmap (Context context) {
			this.context = context;
		}
		
		@Override
		protected Bitmap doInBackground(Shopping... params) {
			//Log.d(TAG, "doInBackground AsyncTask, from showPictureDialog");

			ExifInterface 							pictExif;
			Matrix 										pictMatrix;
			Bitmap 									pictBM;
			String 										pictInfo = null;
			int 											pictRotate = 0;
			Shopping									shopItem;
			
			shopItem = params[0];
			String fileName = shopItem.getAddress().toString();
			
			int scaleFactor;
			
			scaleFactor = ((TravelLiteActivity) getActivity()).computeScaleFactor(fileName);			//--compute picture scale down to fit screen size
			
			BitmapFactory.Options options=new BitmapFactory.Options();
			//Log.d(TAG, "AsyncTask scale factor for bm, prefs "+scaleFactor);			
			options.inSampleSize=scaleFactor;						  															//--should be bigFactor from shared preferences
			
			pictBM=BitmapFactory.decodeFile(fileName.toString(), options);
			
				//--decoding exif information----------------------------------
				try {
					pictExif = new ExifInterface(fileName);
					pictInfo=pictExif.getAttribute(ExifInterface.TAG_ORIENTATION);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//--rotate image base on orientation info from exif file-------------------
				pictMatrix=new Matrix();
				
				if(pictInfo != null)
					pictRotate=Integer.parseInt(pictInfo);
					//Log.d(TAG, "inside orientinfo, pictRotate = "+pictRotate);
					switch(pictRotate) {
						case 1:			//normal no rotation		
							pictMatrix.postRotate(0);
							pictBM=Bitmap.createBitmap(pictBM, 0, 0, pictBM.getWidth(), pictBM.getHeight(), pictMatrix, true);	
							break;					
						case 3:			//rotate 180			
							pictMatrix.postRotate(180);
							pictBM=Bitmap.createBitmap(pictBM, 0, 0, pictBM.getWidth(), pictBM.getHeight(), pictMatrix, true);	
							break;
						case 4:			//flip vertical, i.e. rotate cw 90 degrees			
							pictMatrix.postRotate(270);
							pictBM=Bitmap.createBitmap(pictBM, 0, 0, pictBM.getWidth(), pictBM.getHeight(), pictMatrix, true);	
							break;
						case 6:			//rotate cw 90 degrees			
							pictMatrix.postRotate(90);
							pictBM=Bitmap.createBitmap(pictBM, 0, 0, pictBM.getWidth(), pictBM.getHeight(), pictMatrix, true);	
							break;
						case 8:			//rotate cw 270 degrees				
							pictMatrix.postRotate(270);
							pictBM=Bitmap.createBitmap(pictBM, 0, 0, pictBM.getWidth(), pictBM.getHeight(), pictMatrix, true);	
							break;
						default:
							break;
					}
									
				if (pictBM != null) {
					return pictBM;
				} else {
					return null;
				}				
		}		//--End doInBackground

	}		//--END AsyncTask scaleBitmap
	
			
}			//--END MAIN CLASS
	
	

