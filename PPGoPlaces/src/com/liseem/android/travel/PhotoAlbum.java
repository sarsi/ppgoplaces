/*
 * Fragment: 											PhotoAlbum.java
 * Description:											View Photo Album for Holiday and Location
 * 																
 * 
 * Created: 												May 3, 2012
 * Changed last release: 						September 18, 2012
 * Last updated: 										November 28, 2013
 * 
 * 
 * Resources files:
 * layout:													addPhoto.xml
 * 
 * 
 * need to fix:
 * - photo browsing async task
 * 
 */


package com.liseem.android.travel;

import static com.liseem.android.travel.TravelLiteActivity.*;
import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import com.liseem.android.travel.ShoppingList.scaleBitmap;
import com.liseem.android.travel.items.HLocation;
import com.liseem.android.travel.items.LPicture;
import com.liseem.android.travel.items.MyDate;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.ExifInterface;
import android.net.Uri;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.FloatMath;
import android.util.Log;
import android.util.LruCache;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class PhotoAlbum extends Fragment  {
	
	//=====SECTION 1==DECLARATION===================================

	private final static String 			TAG="PhotoAlbum";
	//public final static String 				FB_APPLICATION_ID="333670976716756";
	private static final int 					MENU_DIALOG_ID=0;
	
	//--sandbox facebook----------------
    //final static int AUTHORIZE_ACTIVITY_RESULT_CODE = 0;
    //final static int PICK_EXISTING_PHOTO_RESULT_CODE = 1;
	
	private PPGoPlacesApplication 	app;
	private SharedPreferences 			prefs;
	
	//--db programmatics support------------------------
	private ArrayList<LPicture> 			pictureList;
	private ArrayList<File> 				imageList;
	private ArrayList<Boolean> 		checkedList;
	private ArrayList<Long> 				missingList;
	private ArrayList<HLocation> 		locationList;
	private long 									dbRowId;				//--from intent
	private boolean							holidayAlbum;		//--from intent
	private boolean 							hasPicture;
	private HLocation 							hLocation;
	private File										fileSelected;

	//--view resources--------------------
	private ViewPager 						viewPage;
	private Button 								addPicture;
	private CheckBox 							selectPicture;
	private TextView 							textView;
	private PhotoViewAdapter				adapter;	
	
	//--image adapter and view position--------------------
	private int 										filePosition;			//--current selected image	
	private LPicture 								picture;
	
	//--menu dialog----------------------------------
	private AlertDialog 						removeItems;
	private AlertDialog 						removePicture;
	private AlertDialog 						deletePicture;
	private StringBuilder 						locationInfo;
	private StringBuilder 						fileInfo;	
	
	private int 										scaleFactor;
	private int 										orientate;
	private String 								emailSignature;
	
	//--PhotoViewAdapter---------------------------
	private Context 								context;
	private int 										bmRotate;
	private String 								orientInfo;
	private ExifInterface 						imageExif;
	private Matrix 								matrix;
	private Bitmap 								bm;
	private AlertDialog 						noPicture;
	
	//--sandy memory cache--------------------------
	private LruCache<String, Bitmap> mMemoryCache;
	private String 								imageKey;
	private int										memClass;
	private ActivityManager				activityManager;
	
	
	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	
		setHasOptionsMenu(true);								//--use fragment options menu instead of activity default
		setRetainInstance(true);
		Log.d(TAG, "270 landed in onCreateView");

		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;
	
		//--inflate layout for fragment 1------------------------------------------------
		View v=inflater.inflate(R.layout.add_photo, container, false);

		return v;			
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//getActivity().requestWindowFeature(Window.FEATURE_NO_TITLE); //--must call before view create
	}
	
	
	@Override
	public void onStart() {
		super.onStart();
		//lets remove the title bar, maybe duplicated in xml setup
		//setContentView(R.layout.add_photo);

		setupView();
		setListeners();
		addAdapter();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	//--Create menu--
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
	
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
	    if (getBitmapFromMemCache(key) == null) {
	        mMemoryCache.put(key, bitmap);
	    }
	}

	public Bitmap getBitmapFromMemCache(String key) {
	    return mMemoryCache.get(key);
	}
	
	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 3==SETUP VIEWS====================================

	private void setupView() {
		
		//--setup path to application--------------
		app							=(PPGoPlacesApplication)getActivity().getApplication();
		
		//--retrieve location information from intent-----------		
		//Bundle bundle		=getActivity().getIntent().getExtras();
		Bundle bundle 		= this.getArguments();			//--fragment retrieve bundle
		dbRowId					=bundle.getLong(LOC_ROWID);			//holiday rowId if holidayAlbum is true, else locaiton id
		holidayAlbum			=bundle.getBoolean(HOLIDAY_ALBUM);
			
		//hasPicture				=bundle.getBoolean("hasPicture");
		
		//--sandy----------------------------------------------------
		// Get memory class of this device, exceeding this amount will throw an
	    // OutOfMemory exception.
		activityManager 		= (ActivityManager)getActivity().getSystemService(Context.ACTIVITY_SERVICE);
		memClass 				= activityManager.getMemoryClass();

	    // Use 1/8th of the available memory for this memory cache.
	    final int cacheSize = 1024 * 1024 * memClass / 8;

	    mMemoryCache 		= new LruCache<String, Bitmap>(cacheSize) {
	        @Override
	        protected int sizeOf(String key, Bitmap bitmap) {
	            // The cache size will be measured in bytes rather than number of items.
	           // return bitmap.getByteCount();
	        	int size 			= bitmap.getRowBytes() * bitmap.getHeight();
	        	return size;
	        }
	    };	
		//--sandy-------------------------------------------------------
		
		picture 					= new LPicture();
		hLocation 				= new HLocation();
				
		//--setup main view resources--------------------------------------
		viewPage			=(ViewPager)getActivity().findViewById(R.id.pageView1);
		addPicture		=(Button)getActivity().findViewById(R.id.addButton1);		
		selectPicture	=(CheckBox)getActivity().findViewById(R.id.checkBox1);
		addPicture.setVisibility(View.INVISIBLE);
		
	}
	
	
	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================
		
	private void addAdapter() {
		
		boolean checkSD	=((TravelLiteActivity) getActivity()).checkExternalStorageWriteWrite();
		if (checkSD) {
			loadList();										//--loading pictureList here, big routine. for adapter
			//Log.d(TAG, "picture list size before adding to adapter "+pictureList.size());
			adapter				=new PhotoViewAdapter(getActivity(), pictureList);
			//adapter				=new PhotoViewAdapter(getFragmentManager());		//--?? don't know how
			viewPage.setAdapter(adapter);
			viewPage.setCurrentItem(0);
			
		} else {
			((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.device_storage_is_currently_not_available));
			getFragmentManager().popBackStackImmediate();
		}
	}
	
	
	//--check picture list and picture availability, support setupView()------------------------
	private void loadList() {
		
		pictureList=new ArrayList<LPicture>();
		locationList=new ArrayList<HLocation>();
		
		//--PART 1 LOAD HOLIDAY ALBUM-----------------------------
		if (holidayAlbum) {
			
			locationList=app.getHolidayLocations(dbRowId);
			for (HLocation l : locationList) {
				if (l.hasPicture()) {
					//Log.d(TAG, "has picture is true");
					ArrayList<LPicture> tempHelp = new ArrayList<LPicture>();
					tempHelp= app.getLocationPictures(l.getId());
					if (tempHelp.isEmpty()) {
						l.setPicture(false);
						app.updateHLocation(l);
					} else {
						//Log.d(TAG, "add picture from location to pictureList, tempHelp size "+tempHelp.size());
						pictureList.addAll(tempHelp);
					}
				}
			}
			//Log.d(TAG, "Picture list size is "+pictureList.size());
		} else {				//Not holiday single location album
			
			//--PART 2 LOAD LOCATION ALBUM-----------------------------

			pictureList=app.getLocationPictures(dbRowId);
			if (pictureList.isEmpty()) {
				hLocation = new HLocation();
				hLocation = app.getHLocation(dbRowId);
				hLocation.setPicture(false);
				app.updateHLocation(hLocation);
			}
		}
		
		if (!pictureList.isEmpty()) {
			//--remove if any picture file not exists-----------------------------------
			ArrayList<LPicture> tempList = new ArrayList<LPicture>();
			tempList.addAll(pictureList);
			for (LPicture p : tempList) {
				File test = new File(p.getPicPath());
				if (!test.exists())
					pictureList.remove(p);
			}
		}
		
		//v1.0
		/*Long[] rowIds;
		if (holidayAlbum) {
			rowIds = new Long[] {(long)0, dbRowId};
		} else {
			rowIds = new Long[] {dbRowId, (long)0};
		}*/
		
		
		/*try {
			pictureList = new createImageList().execute(rowIds).get();		//async section 9
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}*/
		
		if (pictureList.isEmpty() || pictureList == null) {
			noPictureDialog();
		} 
		
	}
	

	//=====SECTION 4==ADD ADAPTER===================================

	//=====SECTION 5==SET LISTENER====================================

	private void setListeners() {
		
		//--check box and view selected listeners-----------------------
		viewPage.setOnPageChangeListener (new ImageOnFocusChange());
		selectPicture.setOnCheckedChangeListener(new ImageOnCheckedChange());
		
	}
	
	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================

	
	//--pager view on page change listener, return checkbox status-----------
	private class ImageOnFocusChange implements OnPageChangeListener{
		
		@Override
		public void onPageSelected(int position) {		//position is the current image

			filePosition=position;
			//picture=new LPicture();
			picture=adapter.listItems.get(position);

			selectPicture.setChecked(picture.isMark());
			
			//v1.0
			/*if (pictureList.get(position).isMark()) {			//retrieve the status of picture at position
				selectPicture.setChecked(true);
			} else {
				selectPicture.setChecked(false);
			}*/
						
			//filePosition=viewPage.getCurrentItem();
			//picture=pictureList.get(position);
		}

		@Override
		public void onPageScrollStateChanged(int position) {			}
				
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {			}
		
	}	//END ImageOnFocusChange
	
	
	//--checkbox listener for selecting pictures to tag to location------------------
	private class ImageOnCheckedChange implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			
			adapter.getItem(filePosition).setMark(isChecked);
			picture.setMark(isChecked);
			app.updatePicture(picture);
			
			//v1.0
			/*filePosition=viewPage.getCurrentItem();			//type:int position
			
			if (isChecked) {		//will always return the surface status, true if checked else false
				pictureList.get(filePosition).setMark(true);
				selectPicture.setText(" SELECTED");
				//makeShortToast("Picture selected");
			} else {
				//checkedList.set(filePosition, false);
				pictureList.get(filePosition).setMark(false);
				selectPicture.setText(" ");
			}
			picture=pictureList.get(filePosition);  //after marking checked select status; type LPicture		*/
		}
	}	//END ImageOnCheckedChange
	
	//=====SECTION 6==LISTENER METHODS================================

	//=====SECTION 7==HELPER METHODS==================================
	

	//=====SECTION 7==HELPER METHODS==================================
	
	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	//--remove selected picture tag from location--------------
	public void noPictureDialog() {
		noPicture= new AlertDialog.Builder(getActivity())
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Album is empty")
		.setMessage("No picture found in album")
		.setPositiveButton("OK ", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getFragmentManager().popBackStackImmediate();
			}
		})
		.create();
		noPicture.show();
	}
	
	//--remove selected picture tag from location--------------
	public void removeThisPicture() {
		removePicture= new AlertDialog.Builder(getActivity())
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("UnTag Picture")
		.setMessage("Remove from location album?")
		.setPositiveButton(R.string.yes_remove, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				removeFromAlbum();
				removePicture.dismiss();
			}
		})
		.setNegativeButton(R.string.cancel_action, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				removePicture.cancel();
			}
		})
		.create();
		removePicture.show();
	}

	//--remove from album call from dialog------------------------
	public void removeFromAlbum() {
		
		long locRowId = picture.getRefid();		//location id before delete action.
		
		//--1. remove from adapter------------
		adapter.removePicture(filePosition);		

		//--2. remove from picture db--------
		app.deletePicture(picture.getId());
		
		//--3. check picture db availability of location still------
		boolean locHasPicture = app.queryPictureAvailable(locRowId);

		//--4. if location picture is empty, update location db--------
		if (!locHasPicture) {
			app.updateLocationHasPicture(locRowId, false);
		}
		
		//--5. if picture list of adapter is empty, end activity---
		boolean pictureNone = adapter.listItems.isEmpty();
		if (pictureNone) {
			noPictureDialog();
		}
	}
	
	//--delete selected picture, from context menu support onItemLongClickListener for imageView----
	public void deleteThisPicture() {
	
		//--return file name---------------------------
		String pathName=picture.getPicPath().toString();
		int lastIdx=pathName.lastIndexOf("/");
		String fileName=pathName.substring(lastIdx+1);
		
		deletePicture= new AlertDialog.Builder(getActivity())
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Delete "+fileName.toString()+"?")
		.setMessage("Are you sure to delete?"+
					"\n\nNOTE: \nDeleted picture is not recoverable. ")
		.setPositiveButton("YES, Delete", new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteFromSDcard();
				deletePicture.dismiss();
			}
		})
		.setNegativeButton(R.string.cancel_action, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deletePicture.cancel();
			}
		})
		.create();
		deletePicture.show();
	}
	
	//--!ALERT-------delete from sdcard--------------------!ALERT--------------------------
	public void deleteFromSDcard() {
		
		long locRowId = picture.getRefid();		//location id before delete action.
		
		//--1. remove from adapter------------
		adapter.removePicture(filePosition);		
		Log.d(TAG, "here 2");
		
		//--2. remove from picture db--------		
		File tempFile=new File(picture.getPicPath().toString());
		//app.deletePicture(adapter.getItem(filePosition).getId());
		app.deletePicture(picture.getId());
		
		//--3. physically delete from sdcard----------------------------------
		new deleteFileTask().execute(tempFile);		//removing from imageList via Async Task				
		
		//--4. check picture db availability of location still------
		boolean locHasPicture = app.queryPictureAvailable(locRowId);
		
		//--5. if location picture is empty, update location db--------
		if (!locHasPicture) {
			app.updateLocationHasPicture(locRowId, false);
		}
		
		//--6. if picture list of adapter is empty, end activity---
		boolean pictureNone = adapter.listItems.isEmpty();

		if (pictureNone) {
			noPictureDialog();
		}
	}
	

	//--delete all marked pictures, support menu system and checkbox------------------------------------------------
	public void removeSelectedPictures() {
		
		int count=0;
		for (LPicture lp: pictureList) {
			if (lp.isMark())
				count++;
		} 
		if (count==0) {
			((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.no_picture_selected));
		} else {
			removeItems= new AlertDialog.Builder(getActivity())
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.remove_from_album)
			.setMessage(getString(R.string._remove_tagging_from_location_))
			.setPositiveButton(R.string.yes_remove, new AlertDialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				removeAllSelected();
				removeItems.dismiss();
				}
			})
			.setNegativeButton(R.string.cancel_action, new AlertDialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					removeItems.cancel();
				}
			})
			.create();
			removeItems.show();
		}
	}
	
	private void removeAllSelected() {
		//--PART 1 - LOCATION ALBUM------------------------------	
		if (!holidayAlbum) {
			
			//--1. set location id before delete-------------------
			long locRowId= picture.getRefid();		//location id
			
			//--2. get all picture list from adapter----------------
			ArrayList<LPicture> tempHelp=new ArrayList<LPicture>();
			tempHelp = adapter.listItems;
			
			//--3. delete all marked items from db and adapter----------
			for (LPicture p : tempHelp) {
				if (p.isMark())
					app.deletePicture(p.getId());
			}
			adapter.removeAllSelectedItems();
			
			//--4. check picture db availability of location still------
			boolean locHasPicture = app.queryPictureAvailable(locRowId);
			
			//--5. if location picture is empty, update location db--------
			if (!locHasPicture) {
				app.updateLocationHasPicture(locRowId, false);
			}			
			
			if (adapter.listItems.isEmpty()) 
				noPictureDialog();
			
		} 	//END of PART 1 - LOCATION ALBUM
		
		//--PART 2 HOLIDAY ALBUM-----------------------------------------------
		else {
			
			//--1. Get all picture list from adapter---------------------
			ArrayList<LPicture> tempHelp=new ArrayList<LPicture>();
			ArrayList<LPicture> deleteHelp=new ArrayList<LPicture>();
			ArrayList<Long> markLocList=new ArrayList<Long>();			
			tempHelp.addAll(adapter.getAllPictureList());
			
			//--2. delete all marked pictures-------------------------
			for (LPicture p : tempHelp) {
				if (p.isMark()) {
					app.deletePicture(p.getId());
					int loc = markLocList.indexOf(p.getRefid());  // -1 if not found
					if (loc < 0)
						markLocList.add(p.getRefid());
				}
			}
			adapter.removeAllSelectedItems();
			
			//--3. check all location for empty picture list----
			if (!markLocList.isEmpty()) {
				for (int i=0 ; i < markLocList.size() ; i++) {				
					boolean locHasPicture = app.queryPictureAvailable(markLocList.get(i));				
					//--5. if location picture is empty, update location db--------
					if (!locHasPicture) {
						app.updateLocationHasPicture(markLocList.get(i), false);
					}							
				}
			}
			
			//--4. if picture list is empty no picture dialog and exit---------
			if (adapter.listItems.isEmpty()) {
				noPictureDialog();
			}	
		}	//END of PART 2 - HOLIDAY LOCATIONS ALBUM	
	}
	
	//--get picture details calling from menu dialog---------------
	public void fileInformation() {
		
		//--return file name---------------------------
		String pathName=picture.getPicPath().toString();
		int lastIdx=pathName.lastIndexOf("/");
		String fileName=pathName.substring(lastIdx+1);
		
		File fileSelected=new File(picture.getPicPath());
		
		//--return dmy date string--------------------
		Calendar d= Calendar.getInstance();
		d.setTimeInMillis(fileSelected.lastModified());
		MyDate e=new MyDate();
		e.setCalDate(d);

		//picture=pictureList.get(filePosition);		
		
		fileInfo=new StringBuilder()
		.append("Name: ").append(picture.getName().toString())
		.append(R.string._location_).append(picture.getLocation().toString())
		.append("\nAddress: \n").append(picture.getAddress().toString())
		.append("\n\nDate modified: ").append(e.getDMYDate())
		.append("\nDirectory: ").append(fileSelected.getPath())
		.append("\nFile Size: ").append(fileSelected.length()).append(" bytes");
		
		new AlertDialog.Builder(getActivity())
		.setIcon(android.R.drawable.ic_menu_info_details)
		.setTitle(fileName.toString())
		.setMessage(fileInfo.toString())
		.setPositiveButton("OK", null)
		.show();
	}
	


	private void namePicture() {
		picture=pictureList.get(filePosition);
		AlertDialog.Builder namePicture= new AlertDialog.Builder(getActivity());
		namePicture.setTitle(R.string.name_picture);
		namePicture.setMessage(getString(R.string.enter_a_name_below_));

		// Set an EditText view to get user input 
		final EditText input = new EditText(getActivity());
		namePicture.setView(input);
		namePicture.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
			  Editable value = input.getText();
			  		picture.setName(value.toString());
			  		pictureList.get(filePosition).setName(value.toString());
			  		app.updatePicture(picture);
			  }
		});
		namePicture.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			  @Override
			public void onClick(DialogInterface dialog, int whichButton) {
			    // Canceled.
			  }
		});
		namePicture.show();
	}

	
	//--Share multiple pictures-------------------------------------------------------
	public void sharePicture() {
		
		//--return file name---------------------------
		String pathName=picture.getPicPath().toString();
		int lastIdx=pathName.lastIndexOf("/");
		String fileName=pathName.substring(lastIdx+1);
		//File fileSelected=new File(picture.getPicPath());
		
		//--return dmy date string--------------------
		Calendar d= Calendar.getInstance();
		d.setTimeInMillis(fileSelected.lastModified());
		MyDate e=new MyDate();
		e.setCalDate(d);

		//picture=pictureList.get(filePosition);
		
		fileInfo=new StringBuilder()
		.append("Name: ").append(picture.getName().toString())
		.append(R.string._location_).append(picture.getLocation().toString())
		.append(R.string._address).append(picture.getAddress().toString())
		.append("\n\n\n")
		.append("[").append(emailSignature.toString()).append("]");
		
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
	    shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
	    shareIntent.setType("image/*");
	    shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, picture.getName().toString());
	    shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, fileInfo.toString());
	    
	    // For a file in shared storage.  For data in private storage, use a ContentProvider.
	    //Uri uri = Uri.fromFile(getFileStreamPath(picture.getPicPath().toString()));
	    Uri uri=Uri.fromFile(fileSelected);
	    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
	    startActivity(Intent.createChooser(shareIntent, "Share Picture Via"));
	}
	
	//--Share multiple pictures-------------------------------------------------------
	public void shareMultiplePictures() {
		
		fileInfo=new StringBuilder();		
		ArrayList<Uri> imageUris = new ArrayList<Uri>();
		
		for (LPicture p : pictureList) {
			if (p.isMark()) {
				File file = new File(p.getPicPath().toString());
				Uri uri=Uri.fromFile(file);		// Add your image URIs here
				imageUris.add(uri);
				fileInfo.append(R.string._filename_).append(file.getName())
							.append(R.string._name_).append(p.getName().toString())
							.append(R.string._location_).append(p.getLocation().toString())
							.append(R.string._address).append(p.getAddress().toString())
							.append("\n\n\n")
							.append("[").append(emailSignature.toString()).append("]");
			}
		}
		
		if (imageUris.isEmpty()) {
			((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.no_picture_selected));
		} else {
			Intent shareIntent = new Intent();
			shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
			shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, fileInfo.toString());
			shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
			shareIntent.setType("image/*");
			startActivity(Intent.createChooser(shareIntent, "Share Selected Images Via"));
		}
	}

	//--menu listing action from above context menu choice---------------------------------------
	private void CreateMenu(Menu menu) {
		
		//app.CreateMenu(menu);
		//--replace menu 4 with load default checklist------------------------
		//menu.removeItem(3);
		MenuItem mnu1=menu.add(0,0,0,R.string.help); 
		{	mnu1.setIcon(android.R.drawable.ic_menu_help);}
		
		MenuItem mnu2=menu.add(0,1,1,"Share Selected"); 
		{	mnu2.setIcon(android.R.drawable.ic_menu_share);}
		
		MenuItem mnu3=menu.add(0,2,2,"Remove Selected"); 
		{	mnu3.setIcon(android.R.drawable.ic_menu_delete);}
		
	}
	
	private boolean MenuChoice(MenuItem item)
	{
		switch (item.getItemId()) {
		case 0:
			//--HELP----------------------------------------------------
			Intent  simplehelp=new Intent(getActivity(), SimpleHelp.class);
			simplehelp.putExtra("helpPage", "photoalbum.html");
			startActivity(simplehelp);
			return true;			
		case 1:
			//--SHARE MULTIPLE PICTURES---------------------------
			shareMultiplePictures();
			return true;
		case 2:
			//--REMOVE ALL SELECTED PICTURES------------------
			removeSelectedPictures();
			return true;
		}
		return false;
	} 
	//--menu choice-----------------------------------------------------------------------------------------------------

	//--context menu---------------------------------------------------------------------------------		
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	        ContextMenuInfo menuInfo) {
			menu.setHeaderIcon(android.R.drawable.ic_menu_gallery);
			menu.setHeaderTitle(R.string.picture_menu);
			menu.add(0, 0, 0, R.string.share);
			menu.add(0, 1, 1, R.string.remove);
			//menu.add(0, 2, 2, R.string.delete_file);
			//menu.add(0, 3, 3, R.string.rotate);
			menu.add(0, 4, 4, R.string.details);
			menu.add(0, 5, 5, R.string.rename);			
			//super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item){
		AdapterContextMenuInfo info=(AdapterContextMenuInfo) item.getMenuInfo();
		//--adapter info.position does not work for pager view, need to call pager view directly-------
		pictureList=adapter.listItems;
		picture=pictureList.get(viewPage.getCurrentItem());
		filePosition=viewPage.getCurrentItem();
		switch (item.getItemId()) {
			case 0:
				//--SHARE VIA-----------------------------------------------
				sharePicture();
				return true;
			case 1:
				//--REMOVE TAG---------------------------------------------
				removeThisPicture();
				return true;
			case 2:
				//--PHYSICAL DELETE FILE--------------------------------
				deleteThisPicture();
				return true;
			case 3:
				//--ROTATE PICTURE------------------------------
				rotateThisPicture(viewPage.getCurrentItem());
				return true;
			case 4:
				//--PICTURE DETAILS---------------------------------------
				fileInformation();
				return true;
			case 5:
				//--RENAME PICTURE---------------------------------------
				namePicture();
				return true;
			default:
				return super.onContextItemSelected(item);
		}			
	}


	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	private class checkLocationHasPicture extends AsyncTask<Long, Void, Void> {

		@Override
		protected Void doInBackground(Long... params) {
			long rowId = params[0];
			
			ArrayList<LPicture> tempHelp = new ArrayList<LPicture>();
			tempHelp=adapter.getAllPictureList();
			
			boolean hasPicture=false;
			for (LPicture p : tempHelp){
				if (p.getRefid() == rowId)
					hasPicture=true;
			}
			if (!hasPicture) {
				HLocation thisLocation = new HLocation();
				thisLocation = app.getHLocation(rowId);
				thisLocation.setPicture(false);
				app.updateHLocation(thisLocation);
			}
			return null;
		}
	}
	
	
	private class createImageList extends AsyncTask<Long, Void, ArrayList<LPicture>> {

		@Override
		protected ArrayList<LPicture> doInBackground(Long... params) {
			
			//--retrieve id from params
			long locRowId=params[0];
			long holRowId=params[1];
			
			//--if holiday id exists then holiday album is true
			boolean holidayAlbum=false;
			if (holRowId  > 0) {
				holidayAlbum=true;
			    Log.d(TAG, "in asynctask, holidayAlbum is true"+holidayAlbum);
			}
			//========new db codes===============================
			//pictureList=new ArrayList<LPicture>();
			ArrayList<LPicture> photoList = new ArrayList<LPicture>();
			ArrayList<Long> missingList = new ArrayList<Long>();
			ArrayList<File> imageList = new ArrayList<File>();

			//-------------------------------------------------------------------------
			//hLocation=app.getHLocation(locRowId);	//always crash here ???
			ArrayList<LPicture> tempList;
			ArrayList<LPicture> testList;
			
				//--GET LOCATION ALBUM retrieve photoList from location-----------------------
			if (!holidayAlbum) {
				//--retrieve location pictures
				photoList=app.getLocationPictures(locRowId);		//locRowId location row_id
				testList=new ArrayList<LPicture>();
				testList.addAll(photoList);
				if (!testList.isEmpty()) {
					for (LPicture p : testList) {
						File pictureThis = new File(p.getPicPath().toString());
						if (!pictureThis.exists())
							photoList.remove(p);
					}
				}				
				
				//--OR GET HOLIDAY ALBUM retrieve photoList from all holiday locations-----------------------				
			} else {
				//--retrieve all locations and location pictures for holiday
				ArrayList<HLocation> locationList = new ArrayList<HLocation>();
				locationList=app.loadLocations(holRowId, true);		//holRowId holiday row_id
				//Log.d(TAG, "retrieve holiday location list "+locationList.size());
				//for (HLocation l : locationList) {		//retrieve pictures for each location
				for (int i=0; i<locationList.size(); i++) {
					tempList=new ArrayList<LPicture>();
					testList=new ArrayList<LPicture>();
					
					tempList=app.getLocationPictures(locationList.get(i).getId());
					if (tempList.isEmpty()) {   				//if location picture list is empty
						//Log.d(TAG, "Landed here, tempList is empty");
						locationList.get(i).setPicture(false);
						app.updateHLocation(locationList.get(i));
					} else {											//if location has picture
						testList.addAll(tempList);
						for (LPicture p : tempList) {
							File pictureThis = new File (p.getPicPath().toString());
							if (pictureThis.exists()) {		//if picture still exist at directory
								photoList.add(p);				//add to photoList if picture existed
							} else {
								testList.remove(p);
							}
						}
						if (testList.isEmpty()) {			//if all pictures of location cannot be found
							//Log.d(TAG, "Landed here, tempList is not empty but testList is empty");
							locationList.get(i).setPicture(false);
							app.updateHLocation(locationList.get(i));
						}	
					}
				}
			}
		
			//--IF ALBUM IS EMPTY
			if (photoList.isEmpty()) {
				return null;
			} else {
				return photoList;
			}
		}
	}
	
	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================
	
	//=====SECTION 10==VIEW PAGER CUSTOM ADAPTER==========================
	
	//=============================================
	//   PHOTOVIEWADAPTER - VIEWPAGE ADAPTER
	//=============================================
	
	//--DO NOT break below methods up, page view custom adapter
	//--photoview custom adapter (old extends PagerAdapter) ??FragmentStatePagerAdapter
	private class PhotoViewAdapter extends  PagerAdapter {		//--v1.1 PagerAdapter

	
		private Context 							context;
		private ArrayList<LPicture> 		listItems;
		private ArrayList<LPicture> 		allPictureList;
		private ArrayList<LPicture> 		tempPList;
		private LPicture 							pictureThis;
		private ImageView 					iv;
		private FragmentManager			fm;
		
		//--pinch to zoom---------------------------------------------
		Matrix matrix 								= new Matrix();
		Matrix savedMatrix 					= new Matrix();
		Matrix originalMatrix 					= new Matrix();
		static final int NONE 				= 0;
		static final int DRAG 				= 1;
		static final int ZOOM 				= 2;
		int mode 									= NONE;
		
		PointF start 									= new PointF();
		PointF mid 									= new PointF();
		float oldDist 								= 1f;
		//--pinch to zoom---------------------------------------------
			
 		public PhotoViewAdapter(Context context, ArrayList<LPicture> pictList) {
			this.context	=context;
			this.listItems	=pictList;
 		
			setAllPictureList(pictList);
			
			//--setup path to application------------------------------------------------
			app=(PPGoPlacesApplication)context.getApplicationContext();
			
		}
		
		//--must have 1 of 4---------
		@Override
		public int getCount() {
			return listItems.size();
		}

		//--required this for view pager to keep the current page active------------
		@Override
		public int getItemPosition(Object object) {
			//return getItemPosition(object);
			//return listItems.indexOf(object);
			
			//v1.0 WORKING model temporary remark
			//--IMPORTANT force notifydatasetchanged to reload as no position is return
			return POSITION_NONE;			
		}

		//--get item id--------------------------------------------------
		public long getItemId(int position) {
			return (null==listItems) ? null : listItems.get(position).getId();
		}
		
		//--get item----------------------------------------------------
		public LPicture getItem(int position) {
			return (null==listItems) ? null : listItems.get(position);
		}
		
		//--must have 2 of 4------------remove and recycle view after passing into pager view-----------------
		@Override
		public void destroyItem(View collection, int position, Object view) {
			((ViewPager)collection).removeView((ImageView) view);			
		}

		//--must have 3 of 4------------this add view to pager view----------------------------------------------------
		@Override
		public Object instantiateItem(View collection, int position) {
		
			/*LayoutInflater inflater=(LayoutInflater) collection.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
			View view=inflater.inflate(R.layout.pager_view_item, null);
			ImageView iv = (ImageView)findViewById(R.id.pagerView1);		*/
			
			iv								=new ImageView(context);
			
			//--pinch to zoom-----------
			iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
			//iv.setOnTouchListener(new PinchOnTouchListener());
			((ViewPager) collection).addView(iv,0);		//--add imageView instead of view
			//((ViewPager) collection).addView(view,0);
			registerForContextMenu(iv);

			
			LPicture getPicture 	= new LPicture();
			getPicture 				= listItems.get(position);
			
			Bitmap bitmap		=null;
			
			//--sandy----------------------------
			imageKey 				= getPicture.getFileName();
			//final String imageKey = String.valueOf(getPicture.getResId());
			
			bitmap = getBitmapFromMemCache(imageKey);		//--replace about with scale down asynctask
			if (bitmap != null) {
				iv.setImageBitmap(bitmap);
			} else {
			//--sandy----------------------------

				try {
					bitmap = new bitmapOrientation().execute(getPicture).get();
					//bitmap =  new scaleBitmap(getActivity().getBaseContext()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, getPicture.getFileName()).get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				if (bitmap != null)
					iv.setImageBitmap(bitmap);
			}

			//if (bitmap != null)
			//	iv.setImageBitmap(bitmap);
			return iv;

		} 	//END instantiateItem

		//--must have 4 of 4-----------------------------------------------------------------------------------------------
		@Override
		public boolean isViewFromObject(View view, Object object) {
			//--when using view as key object which view pager associates page to
			return view==((ImageView) object);		
		}
		
		//==EXTERNAL HELPERS CLASS================================================
	
		//--ADAPTER GETTERS AND SETTERS-------------------------------	
		public void setAllPictureList(ArrayList<LPicture> thisList) {
			allPictureList = new ArrayList<LPicture>();
			allPictureList = thisList;
		}
		
		public ArrayList<LPicture> getAllPictureList() {
			return allPictureList;
		}
		
		
		
		//--ADAPTER CRUB-------------------------------------------------------
		//--data helper method and data set changed management---------
 		public void addPictureList (LPicture boilerPlate, ArrayList<Boolean> checkedList) {
			LPicture picture=new LPicture();
			LPicture tempHelp=new LPicture();
			ArrayList<Boolean> selectedList=new ArrayList<Boolean>();
						
			picture=boilerPlate;
			selectedList=checkedList;
			
			for (int i=0; i < selectedList.size(); i++) {
				if (selectedList.get(i)) {
					this.listItems.remove(i);
					tempHelp=new LPicture();
					tempHelp=picture;
					//tempHelp.setPicPath((listItems.get(i).getAbsolutePath().toString()));
					tempHelp.setPicPath((listItems.get(i).getPicPath().toString()));
					app.insertPicture(tempHelp);
					Toast.makeText(context, R.string.pictures_tagged, Toast.LENGTH_SHORT).show();
				}
			}
			notifyDataSetChanged();
		}		//END addPictureList 
				
		 public void removePicture(int position) {
			 listItems.remove(position);
			 destroyItem(viewPage, filePosition, getActivity().findViewById(R.id.pagerView1));  //IMPORTANT
			 notifyDataSetChanged();
			 
			 //--no need since no filter is implemented------------------
				/*tempPList = new ArrayList<LPicture>();
				tempPList = getAllPictureList();
				tempPList.remove(pic);
				setAllPictureList(tempPList);		*/	
		 }
		 

		//--remove all selected pictures tagging from location album------------------
		public void removeAllSelectedItems() {
			ArrayList<LPicture> tempHelp=new ArrayList<LPicture>();
			//Log.d(TAG, "adapter removeAllSelectedItems "+changeList.size());
			tempHelp=getAllPictureList();
		
			tempPList = new ArrayList<LPicture>();
			tempPList.addAll(tempHelp);
		
			for (LPicture p : tempPList) {
				if (p.isMark())
					tempHelp.remove(p);
			}
			setAllPictureList(tempHelp);
			
			tempHelp.clear();
			tempHelp.addAll(listItems);
			for (LPicture p : tempHelp) {
				if (p.isMark())
					listItems.remove(p);
			}
			notifyDataSetChanged();
			
			//v1.0
			/*tempHelp.addAll(listItems);
			for (LPicture p : changeList) {
				if (!p.isMark())
					tempHelp.add(p);
			}
			listItems=tempHelp;
			notifyDataSetChanged();*/
			Toast.makeText(context, "Selected Pictures Deleted Successfully", Toast.LENGTH_SHORT).show();
		}	
		
		//--NOT USE - conflict with context menu-----------------------------------------
 		private class PinchOnTouchListener implements OnTouchListener {

			@Override
			//--pinch to zoom-----------------------------------
			//--Part 1 of 3 - pinch to zoom
			public boolean onTouch(View v, MotionEvent event) {
				ImageView view = (ImageView) v;
				//--make the image scalable as a matrix----------
				view.setScaleType(ImageView.ScaleType.MATRIX);
				originalMatrix.set(matrix);
				float scale;
				
				//--Handle touch events here------------------------
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				
				case MotionEvent.ACTION_DOWN:	//first finger down only
					savedMatrix.set(matrix);
					start.set(event.getX(), event.getY());
					Log.d(TAG, "mode = DRAG");
					mode = DRAG;
					break;
				case MotionEvent.ACTION_UP:		//first finger lifted
				case MotionEvent.ACTION_POINTER_UP:	//second finger lifted
					mode = NONE;
					Log.d(TAG, "mode = NONE");
					break;
				case MotionEvent.ACTION_POINTER_DOWN:  //second finger down
					oldDist = spacing(event);		//calculates the distance between two points user touched
					Log.d(TAG,"oldDist = "+oldDist);
					//--minimal distance between both the fingers
					if (oldDist > 5f) {
						savedMatrix.set(matrix);
						midPoint(mid, event);	//sets the mid-point of the straight line between the two points where user touched
						mode = ZOOM;
						Log.d(TAG, "mode = ZOOM");
					}
					break;
				case MotionEvent.ACTION_MOVE:
					if (mode == DRAG) {	//movement of first finger
						matrix.set(savedMatrix);
						if (view.getLeft() >= -392) {
							matrix.postTranslate(event.getX() - start.x, 
									event.getY() - start.y);
						}
					} else if (mode == ZOOM) { //pinch zooming
						float newDist = spacing(event);
						Log.d(TAG, "newDist = "+newDist);
						if (newDist > 5f) {
							matrix.set(savedMatrix);
							scale = newDist/oldDist; //need to trial and error for limit
							matrix.postScale(scale,  scale, mid.x, mid.y);							
						}						
					}
					break;
				}
				//--perform transformation-----------------------------
				view.setImageMatrix(matrix);
				return true;		//indicate event was handled
			}				
		} 	//END Part 1 of 3 PinchOnTouchListener
		
		//--Part 2 of 3 - pinch to zoom		
		private float spacing(MotionEvent event) {
			float x = event.getX(0) - event.getX(1);
			float y = event.getY(0) - event.getY(1);
			return FloatMath.sqrt(x * x + y * y);
		}
		//--Part 3 of 3 - pinch to zoom		
		private void midPoint(PointF point, MotionEvent event) {
			float x = event.getX(0) + event.getX(1);
			float y = event.getY(0) + event.getY(1);
			point.set(x/2, y/2);
		}
		//--NOT USE - conflict with context menu-----------------------------------------
		
		 
	}		//END 	MAIN custom pager view adapter

	//=============================================
	//   END --- PHOTOVIEWADAPTER - VIEWPAGE ADAPTER
	//=============================================

	
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
			if (deleteStatus) Toast.makeText(getActivity().getApplicationContext(), R.string.file_successfully_deleted, Toast.LENGTH_SHORT).show();
			adapter.notifyDataSetChanged();
		}
	}	
	
	
	//=====Async tasks === PART OF ADAPTER=====
	private class bitmapOrientation extends AsyncTask <LPicture, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(LPicture... params) {
			LPicture pictureThis = new LPicture();
			pictureThis=params[0];
			
			Bitmap bm=null;
			int orient=0;
			int scalePicture=0;
			int bmRotate=0;
			
			//--retreive picture orientation and scaleFactor information, combined store in orient field
			//--left digit orientation info, right digit scale factor
			orient=pictureThis.getOrient();
			
			if (orient > 10) {		//i.e. there is no left orient and right scale found
				String orientText=String.valueOf(orient);
				bmRotate=Integer.parseInt(orientText.substring(0,1));			//first digit
				scalePicture=Integer.parseInt(orientText.substring(1,2));	//second digit scale factor
				orientInfo=orientText.substring(0,1);									//first digit orientation info
			}
			
			//--get scalefactor compare bitmap file size and display - improved Sep 22th 2013 ln1400
			if (scalePicture==0) {
				scalePicture=((TravelLiteActivity) getActivity()).computeScaleFactor(pictureThis.getPicPath().toString());
			}
			Log.d(TAG, "1385 scale factor \"scalePicture\" is "+scalePicture);
			
			//--retrieve bitmap via picture getPicPath()---------------------------------
			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inSampleSize=scalePicture;		//6, should be bigFactor from shared preferences
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
						
			//--update db picture if no previous orient info-------------------------------------
			if (orient<10) {					//png, no exif end up orient is 1
				orient=(bmRotate*10)+scalePicture;	//to get left rotate info, right scale factor
				pictureThis.setOrient(orient);
				app.updatePicture(pictureThis);
				//Log.d(TAG, "update picture with new orient information "+orient);
			}	 
			addBitmapToMemoryCache(pictureThis.getFileName(), bm);			//sandy memcache
			return bm;
		}
	}		//--END bitmapOrientation
	
	
	//=====SECTION 10==VIEW PAGER CUSTOM ADAPTER==========================
	//=====END OF CUSTOM ADAPTER====================================================

	//=====SECTION 11==SANDBOX=============================================
	//--create png file from bitmap, store in cache directory-----------------------
	protected class createAndSendBitMapFile extends AsyncTask <Bitmap, Void, String> {
		
		@Override
		protected String doInBackground(Bitmap... params) {
			//Log.d(TAG, "createBitMapFile async doInBackground landed");
			//Log.d(TAG, "createSerializeLocationFile() doInBackground landed");
			
			//--get bitmap------------------------------------------------------------------
			Bitmap bmp=params[0];
			
			//--create file name for bitmap image------------------------------------
			String fileName=hLocation.getName().toString();
			fileName=fileName.replaceAll("\\s", "");
			int nameLength=fileName.length();
			if (nameLength<9) {
				fileName=fileName.toString()+"ppgoplaces";
			}
			StringBuilder newName=new StringBuilder().append(fileName.substring(0, 8)).append(".jpg");
			fileName=newName.toString();
			
			
			//--create directory and file to write bitmap---------------------------------
			File exportDir=new File(Environment.getExternalStorageDirectory(), PPGP_CACHE );
			if (!exportDir.exists()) {
				exportDir.mkdirs();
			}
			
			File newFile=new File(exportDir, fileName);
			FileOutputStream out;
			try {
				out = new FileOutputStream(newFile);
			
			//--write bitmap to file-------------------------------------------------------------
			//--PNG format, use JPEG for jpg, 0-100 100 is high quality, out is outputstream file
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
				return newFile.getAbsolutePath().toString();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			return null;
			
		}		//END doInBackground

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				//sendFileAttached(result.toString());
			}
			//super.onPostExecute(result);
		}
	
	}	//END createAndSendBitMapFile()	
	
	
	//=====SECTION 11==SANDBOX============================================
	protected void rotateThisPicture(int position) {

		//--retrieve picture and orient info--------------------
		picture = new LPicture();
		picture=adapter.listItems.get(position);
		int orient=picture.getOrient();	
		String scaleStr="";
		int bmRotate=0;
		
		//--retrieve orient and scale information---------------
		if (orient != 0) {
			String orientText=String.valueOf(Math.abs((long)orient));
			bmRotate=Integer.parseInt(orientText.substring(0,1));			//first digit
			scaleStr=orientText.substring(1,2);										//second digit scale factor
			//orientInfo=orientText.substring(0,1);									//first digit orientation info
		}

		//--rotate 90 degree bmRotate----------------------------
		if (scaleStr==null)
			scaleStr=Integer.toString(scaleFactor);
		
		switch (bmRotate) {			//1, 6, 3, 8
		case 1:		//0
			bmRotate=6;		//0 rotate 90,  1 to 6 
			break;
		case 3:		//rotate 180
			bmRotate=8;		//180 rotate to 270, 3 to 8
			break;
		//case 4:		//rotate 270
		//	bmRotate=1;			//mirror vertical flip
		//	break;
		case 6:		//rotate 90
			bmRotate=3;			//90 rotate to 180, 6 to 3
			break;
		//case 7:
		//	bmRotate=6;		   //mirror horizontal flip
		//	break;
		case 8:		//rotate 270
			bmRotate=1;			//270 rotate back to 0, 8 to 1
			break;
		}
		
		//--update new orientation information, and refresh adapter-------------------
		String orientInfo=Integer.toString(bmRotate);
		StringBuilder orientText=new StringBuilder().append(orientInfo).append(scaleStr.toString());
		orient=Integer.parseInt(orientText.toString());
		picture.setOrient(orient);

		app.updatePicture(picture);
		adapter.listItems.get(position).setOrient(orient);

		adapter.notifyDataSetChanged();
		//bitmap= new bitmapOrientation().execute(picture).get();
	}
	
	//=====SECTION 11==SANDBOX============================================

	//--thread to return scale down picture for view picture, context menu 4---------------------
	//--build 156, Sep 22, 2013 - COPIED from ShoppingList, change Object Shopping to String
	public class scaleBitmap extends AsyncTask <String, Void, Bitmap> {
		
		private Context context;

		public scaleBitmap (Context context) {
			this.context = context;
		}
		
		@Override
		protected Bitmap doInBackground(String... params) {
			//Log.d(TAG, "doInBackground AsyncTask, from showPictureDialog");

			ExifInterface 							pictExif;
			Matrix 										pictMatrix;
			Bitmap 									pictBM;
			String 										pictInfo = null;
			int 											pictRotate = 0;
			String										getPicture;
			
			getPicture = params[0];
			String fileName = getPicture.toString();
			
			int scaleFactor;
			
			scaleFactor = ((TravelLiteActivity) getActivity()).computeScaleFactor(fileName);			//--compute picture scale down to fit screen size
			
			BitmapFactory.Options options=new BitmapFactory.Options();
			//Log.d(TAG, "AsyncTask scale factor for bm, prefs "+scaleFactor);			
			options.inSampleSize=scaleFactor;						  //--should be bigFactor from shared preferences
			
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
		
	
	//=====PARKING LOTS FOR OLD CODES=====================================

	//=====PARKING LOTS FOR OLD CODES=====================================
	
}	//END MAIN CLASS
