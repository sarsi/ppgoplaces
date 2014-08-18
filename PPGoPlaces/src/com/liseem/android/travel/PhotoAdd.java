/*
 * Fragment: 											PhotoAdd.java
 * Description:											Tag new photo to photo album for Holiday and Location
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

import static com.liseem.android.travel.items.TravelLiteDBAdapter.*;
import static com.liseem.android.travel.TravelLiteActivity.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.liseem.android.travel.items.HLocation;
import com.liseem.android.travel.items.LPicture;
import com.liseem.android.travel.items.PhotoOption;

public class PhotoAdd extends Fragment {

	//=====SECTION 1==DECLARATION===================================

	private final static String 			TAG="AddPhoto - PageViewer";
	
	private final static int 					SELECT_DIR=22;
	private PPGoPlacesApplication 	app;
	private SharedPreferences 			prefs;
	
	private PhotoViewAdapter 			adapter;
	//private AddPhotoViewAdapter 		adapter;
	private Context 								context;
	
	//--db programmatics support------------------------
	private ArrayList<PhotoOption> 	photoList;
	private PhotoOption 						photoOpt;
	private ArrayList<File> 				fileListing;
	private ArrayList<File> 				imageList;
	private ArrayList<Boolean> 		checkedList;
	private HLocation 							hLocation=new HLocation();	
	private File 									fileSelected;
	private int 										filePosition=0;
	private long 									dbRowId;
	private LPicture 								picture=new LPicture();
	private boolean 							pendingChange;
	
	//--view resources---------------------------------
	private ViewPager 						viewPage;
	private Button 								addPicture;
	private CheckBox 							selectPicture;
	private TextView 							textView;

	//--retreive file directory data------------
	private File [] 								fileList;
	private File [] 								altList;
	private StringBuilder 						locationInfo;
	private StringBuilder 						fileInfo;
	private String 								path;
	private String 								andropath;				//--100ANDRO pictures dir
	private File 									pictureDir;
	private File 									altDir;						//--path to 100ANDRO
	private boolean 							getOtherDir=false;

	//--for selected images saving to picture db---
	private ArrayList<LPicture> 			pictureList;
	private ArrayList<File> 				currentList;
	private ArrayList<File> 				forCompare;

	//--sandy memcache---------------------------------
	private LruCache<String, Bitmap> mMemoryCache;
	private String									imageKey;
	private int 										memClass;
	private ActivityManager				activityManager;

	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	
		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;
	
		//--inflate layout for fragment 1------------------------------------------------
		View v=inflater.inflate(R.layout.add_photo, container, false);
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
		// lets remove the title bar, maybe duplicated in xml setup
		//getActivity().requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setContentView(R.layout.add_photo);
				
		setupView();
		addAdapter();		//loadList moved to addAdapter
	}
	
	@Override 
	public void onResume() {
		super.onResume();
	}
	
	/*@Override
	public void onBackPressed() {
		super.onBackPressed();
	}*/
	
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
	
	//--LruCache implemenation-------------------------------
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
	    if (getBitmapFromMemCache(key) == null) {
	        mMemoryCache.put(key, bitmap);
	    }
	}
	//--LruCache implemenation-------------------------------
	public Bitmap getBitmapFromMemCache(String key) {
	    return mMemoryCache.get(key);
	}
	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 3==SETUP VIEWS====================================
	
	private void setupView() {
		
		//--setup path to application------------------------------
		app							= (PPGoPlacesApplication)getActivity().getApplication();
		prefs						= getActivity().getSharedPreferences (PREFNAME, MODE_PRIVATE);

		
		Bundle bundle			= this.getArguments();
		dbRowId					= bundle.getLong(LOC_ROWID);
		//hasPicture=locInfo.getBoolean("hasPicture");
		
		//-sandy--------------------------------------------------------
		// Get memory class of this device, exceeding this amount will throw an
	    // OutOfMemory exception.
		 activityManager 	=(ActivityManager)getActivity().getSystemService(Context.ACTIVITY_SERVICE);
		 memClass 				=activityManager.getMemoryClass();
		 //memClass = ((ActivityManager)context.getSystemService(
	     //       Context.ACTIVITY_SERVICE)).getMemoryClass();

	    // Use 1/8th of the available memory for this memory cache.
	    final int cacheSize = 1024 * 1024 * memClass / 8;

	    mMemoryCache 		= new LruCache<String, Bitmap>(cacheSize) {
	        @Override
	        protected int sizeOf(String key, Bitmap bitmap) {
	            // The cache size will be measured in bytes rather than number of items.
	            //return bitmap.getByteCount();
	        	int size = bitmap.getRowBytes() * bitmap.getHeight();
	        	return size;
	        }
	    };
		//--sandy----------------------------------------------------
		
		
		//--setup main view resources--------------------------------------
		viewPage					=(ViewPager)getActivity().findViewById(R.id.pageView1);
		addPicture				=(Button)getActivity().findViewById(R.id.addButton1);		
		selectPicture			=(CheckBox)getActivity().findViewById(R.id.checkBox1);
		addPicture.setText("Add To Location");
		addPicture.setVisibility(View.VISIBLE);
		
		//--check box and view selected listeners---------
		viewPage.setOnPageChangeListener(new ImageOnFocusChange());
		selectPicture.setOnCheckedChangeListener(new ImageOnCheckedChange());
		addPicture.setOnClickListener ((new SaveSelectedImages()));
		
		//--prepare picture boiler plate from selected location
		//--get location object-------------------------------------------------
		hLocation 				= new HLocation();
		hLocation					=app.getHLocation(dbRowId);
		
		photoOpt 				= new PhotoOption();
		photoList 				= new ArrayList<PhotoOption>();
	}
	
	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================
	
	private void addAdapter() {
		
		boolean checkSD=((TravelLiteActivity) getActivity()).checkExternalStorageWriteWrite();
		if (checkSD) {
			defaultPictureList();			//create list for adapter
			//prepareImageList();				//sandy 1.
			//removeDBImage();				//sandy 2.
		} else {
			((TravelLiteActivity) getActivity()).showOkAlertDialog(getString(R.string.device_storage_is_currently_not_available));
			getActivity().finish();			
		}
		
		//Log.d(TAG, "addAdapter, imageList "+imageList.size());
		//adapter=new AddPhotoViewAdapter(this, imageList);
		//adapter =  new PhotoViewAdapter(this, imageList);
		adapter = new PhotoViewAdapter(getActivity(), photoList);
		viewPage.setAdapter(adapter);
		viewPage.setCurrentItem(0);
		
		//TaskStackBuilder backStack
		
	}

	//--load pictures from default DCIM/Camera directory via Asynctask (include /DCIM/100ANDRO/ if exists)------------
	private void defaultPictureList() {
		//Log.d(TAG, "Landed here 1");
		ArrayList<PhotoOption> cameraList = new ArrayList<PhotoOption>();
		ArrayList<PhotoOption> htcList = new ArrayList<PhotoOption>();
		String camPath=Environment.getExternalStorageDirectory().getPath()+"/DCIM/Camera/";
		String androPath=Environment.getExternalStorageDirectory().getPath()+"/DCIM/100ANDRO/";
		try {
			//cameraList = new createImageList().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"/DCIM/Camera/").get();
			cameraList = new createImageList().execute(camPath.toString()).get();

			//htcList = new createImageList().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"/DCIM/100ANDRO/").get();
			File testDir = new File (Environment.getExternalStorageDirectory(), "/DCIM/100ANDRO/");
			if (testDir.exists()) {
				htcList = new createImageList().execute(androPath.toString()).get();
				cameraList.addAll(htcList);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		//Log.d(TAG, "Landed here 3 "+cameraList.size());	
		
		//imageList = new ArrayList<File>();
		//imageList.addAll(cameraList);		
		photoList = new ArrayList<PhotoOption>();
		photoList.addAll(cameraList);
		
		//--setup boolean check list for tracking select and unselect-------------------
		/*checkedList=new ArrayList<Boolean>();
		if (!imageList.isEmpty()) {
			for (File f : imageList) {
				checkedList.add(false);
			}
		}*/		
	}
	

	
	//=====SECTION 4==ADD ADAPTER===================================

	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================

	//--pager view on page change listener, return checkbox status------
	private class ImageOnFocusChange implements OnPageChangeListener{
		
		@Override
		public void onPageSelected(int position) {		//position is the current image
			
			selectPicture.setChecked(photoList.get(position).isMark());
			photoOpt = photoList.get(position);
			filePosition = position;
			
			if (photoList.get(filePosition).isMark()) {
				selectPicture.setText(" SELECTED");
			} else {
				selectPicture.setText(" ");
			}

			//v1.0
			/*if (checkedList.get(position)) {			//retrieve the status of picture at position
				selectPicture.setChecked(true);
			} else {
				selectPicture.setChecked(false);
			}*/
			
			//filePosition=viewPage.getCurrentItem();
			//fileSelected=imageList.get(filePosition);
		}

		@Override
		public void onPageScrollStateChanged(int position) {  }

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {	}
		
	}	//END ImageOnFocusChange
	
	//--checkbox listener for selecting pictures to tag to location------------------------------
	private class ImageOnCheckedChange implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {

			photoList.get(viewPage.getCurrentItem()).setMark(isChecked);
			filePosition=viewPage.getCurrentItem();
			
			if (photoList.get(filePosition).isMark()) {
				selectPicture.setText(" SELECTED");
			} else {
				selectPicture.setText(" ");
			}
			//fileSelected=imageList.get(filePosition);
			
			/*if (isChecked) {		//will always return the surface status, true if checked else false
				checkedList.set(filePosition, true);
				selectPicture.setText(" SELECTED");
				//makeShortToast("Picture selected");
			} else {
				checkedList.set(filePosition, false);
				selectPicture.setText(" ");
			}*/
		}
	}	//END ImageOnCheckedChange
	
	//--save picture button listener, tag selected images to location------------
	private class SaveSelectedImages implements OnClickListener {

		@Override
		public void onClick(View view) {
			
			prepareBoilerPlate();			//prepare picture template with location info
			boolean gotPicture=false;
			
			LPicture tempPict;
			for (PhotoOption p : photoList) {				
				tempPict = new LPicture();
				tempPict = picture;
				if (p.isMark()) {
					tempPict.setPicPath(p.getPath());
					tempPict.setOrient(p.getOrient());
					tempPict.setName(p.getName());
					app.insertPicture(tempPict);
					gotPicture=true;
				}
			}
			if (gotPicture)
				app.updateLocationHasPicture(hLocation.getId(), true);
			getActivity().finish();
		}
	}		//END SaveSelectedImages

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode==SELECT_DIR) {
			if (resultCode==Activity.RESULT_OK) {
				String newPath=data.getStringExtra("directory path");
				
				//ArrayList<File> newFileList = new ArrayList<File>();
				ArrayList<PhotoOption> newFileList = new ArrayList<PhotoOption>();
				if (newPath !=null) {
					try {
						Log.d(TAG, "landed in onActivityResult, new path "+newPath.toString());
						newFileList = new createImageList().execute(newPath.toString()).get();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
				Log.d(TAG, "back from async , new file list size "+newFileList.size());

				if (!newFileList.isEmpty()) {
					//imageList.clear();
					//imageList=newFileList;
					photoList.addAll(newFileList);
					adapter =  new PhotoViewAdapter(getActivity(), photoList);
					viewPage.setAdapter(adapter);
					viewPage.setCurrentItem(0);
					//adapter.notifyDataSetChanged();
				}
			} else if (resultCode==Activity.RESULT_CANCELED) {
				
			}
		}
	}
	//=====SECTION 6==LISTENER METHODS================================

	//=====SECTION 7==HELPER METHODS==================================
	
	//--setup boiler plate with location information for new pictures tagging--------
	private void prepareBoilerPlate() {
		picture=new LPicture();
		if (hLocation.getAddress() !=null)
			picture.setAddress(hLocation.getAddress().toString());
		if (hLocation.getLatitude()>0 && hLocation.getLongitude()>0) {
			picture.setLatitude(hLocation.getLatitude());
			picture.setLongitude(hLocation.getLongitude());
		}
		picture.setLocation(hLocation.getName());
		picture.setRefid(dbRowId);
	}
		
	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	//--menu listing action from above context menu choice---------------------------------------	
	private void CreateMenu(Menu menu) {
		
	MenuItem mnu1=menu.add(0,1,1, R.string.select_directory);
		{ mnu1.setIcon(android.R.drawable.ic_menu_gallery);}

	//MenuItem mnu2=menu.add(0,2,2, "Help");
		//{ mnu2.setIcon(android.R.drawable.ic_menu_help); }
		
	}
		
	private boolean MenuChoice(MenuItem item) {
	
		SharedPreferences.Editor editor = prefs.edit();
		switch (item.getItemId()) {

		case 1:
			Intent chooser=new Intent (getActivity(), FileDirChooser.class);
			startActivityForResult(chooser,SELECT_DIR);
			return true;
		case 2:
			//Intent  simplehelp=new Intent(ViewHoliday.this, SimpleHelp.class);
			//Bundle helpPage=new Bundle();
			//simplehelp.putExtra("helpPage", "viewholiday.html");
			//startActivity(simplehelp);		
			return false;
		}
		return false;
	}
	//--menu choice-----------------------------------------------------------------------------------------------------
	
	
	//=====SECTION 8==MENU AND DIALOG METHODS===========================
	
	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//--create filelist for pictures that are not already in location pictures db-----------------
	private class createImageList extends AsyncTask<String, Void, ArrayList<PhotoOption>> {

		@Override
		protected ArrayList<PhotoOption> doInBackground(String... params) {
			
			//--1. Get a list of all pictures from directory
			//--get picture file list------------------------------------------------------------===------
			//String picturePath=Environment.getExternalStorageDirectory().getPath()+params[0];
			String picturePath=params[0].toString();		//picture path DCIM/camera or ANDRO100
			ArrayList<PhotoOption> newList = new ArrayList<PhotoOption>();
			PhotoOption newItem;
			File pictureDir = new File(picturePath);
			//Log.d(TAG,"async Step 1, pictureDir "+pictureDir.toString());

			//--get file list in DCIM/CAMERA directory---------------------------------------------
			if (!pictureDir.exists()) {							//check directory exists
				return null;
			} else {
				File[] fileList=pictureDir.listFiles();			//IMPORTANT - get files list
				
				for (File f : fileList) {
					newItem = new PhotoOption(f);
					newItem.setMark(false);
					newItem.setId(hLocation.getId());
					newList.add(newItem);
				}
				/*ArrayList <File> listOfFiles = new ArrayList<File>();
				for (File f : fileList) {
					listOfFiles.add(f);								//list of file in directory
				}*/
				//Log.d(TAG,"async Step 1, listOfFiles size "+listOfFiles.size());
				
				//--2. Get list of all pictures already tagged to locations
				//--get list of all pictures tagged to locations----------------------------------------
				ArrayList<LPicture> pictureList = new ArrayList<LPicture>();
				pictureList=app.loadAllPictures();		//--list of picture in db
				
				ArrayList <File> existPicture = new ArrayList<File>();
				//--test physical file still exists in sdcard--------
				for (LPicture p : pictureList ) {
					File locpic= new File (p.getPicPath());
					if (locpic.exists())
						existPicture.add(locpic);			//list of current pictures tagged to location
				}
				//Log.d(TAG,"async Step 2, existPicture size "+existPicture.size());
				
				//--add cache file, to be excluded, path to be deleted list, /mnt/sdcard/DCIM/Camera/cache------------
				File cacheFile=new File(picturePath+"/cache");
				if (cacheFile.exists())
					existPicture.add(0, cacheFile);
				
				//--3. create a temp list for adding file if listOfFiles not in existPicture
				//ArrayList<File> tempListFile = new ArrayList<File>();	
				//tempListFile.addAll(listOfFiles);
				ArrayList<PhotoOption> tempListFile = new ArrayList<PhotoOption>();
				tempListFile.addAll(newList);
				
				//--4. now compare and remove file already existed in db
				int count=existPicture.size();
				//for (File f : listOfFiles) {
				for (PhotoOption f : tempListFile)
					for (File p : existPicture) {
						if (p.getName().equalsIgnoreCase(f.getName()))
							newList.remove(f);
					}
				}
				//Log.d(TAG,  "async Step 3 tempListFile size "+tempListFile.size());
				return newList;
			}
	}
	
	//--correct display orientation of bitmap------------------------------------------------
	//private class bitmapOrientation extends AsyncTask <String, Void, Bitmap> {
	private class bitmapOrientation extends AsyncTask <Integer, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(Integer... params) {
			int position = params[0];
			int scalePicture;
			int bmRotate;
			
			PhotoOption thisPic = new PhotoOption();
			thisPic = photoList.get(position);
			String fileName = photoList.get(position).getPath().toString();
			//String fileName=params[0];
			//int scalePicture=app.getScaleFactor();
			
			//--get scalefactor compare bitmap file size and display - TLA
			//Bitmap testBm = BitmapFactory.decodeFile(fileName);
			if (thisPic.getScale() > 0) {
				scalePicture=thisPic.getScale();
			} else {
				scalePicture=((TravelLiteActivity) getActivity()).computeScaleFactor(fileName);
				photoList.get(position).setScale(scalePicture);
			}
			//Log.d(TAG, "using bitmapScaleFactor from TLA, scalePicture " + scalePicture);
			//testBm.recycle();
			
			
			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inSampleSize=scalePicture;		//6, should be bigFactor from shared preferences
			//bm=BitmapFactory.decodeFile(listItems.get(position).getAbsolutePath().toString(), options);
			Bitmap bm = BitmapFactory.decodeFile(fileName, options);
			
			//--decoding exif information----------------------------------
			if (thisPic.getRotate() > 0) {
				bmRotate=thisPic.getRotate();
			} else {
				String orientInfo=null;
				try {
					//imageExif = new ExifInterface(listItems.get(position).getAbsolutePath().toString());
					ExifInterface imageExif = new ExifInterface(fileName);
					orientInfo=imageExif.getAttribute(ExifInterface.TAG_ORIENTATION);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (orientInfo != null) {
					bmRotate = Integer.parseInt(orientInfo);
					photoList.get(position).setRotate(bmRotate);
				} else {
					bmRotate = 1;
				}
			}	
				//--rotate image base on orientation info from exif file-------------------
				Matrix matrix = new Matrix();
				//if(orientInfo != null) {
				//	int bmRotate = Integer.parseInt(orientInfo);
					switch(bmRotate) {
						case 1:			//normal no rotation		
							matrix.postRotate(0);
							bm=Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);	
							//Log.d(TAG, "in the matrix before orientation case 1");
							break;					
						case 3:			//rotate 180			
							matrix.postRotate(180);
							bm=Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);	
							break;
						case 4:			//flip vertical, i.e. rotate cw 90 degrees			
							matrix.postRotate(270);
							bm=Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);	
							break;
						case 6:			//rotate cw 90 degrees			
							matrix.postRotate(90);
							bm=Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);	
							break;
						case 8:			//rotate cw 270 degrees				
							matrix.postRotate(270);
							bm=Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);	
							break;
						default:
							break;
					}
				//}
				addBitmapToMemoryCache(thisPic.getName(), bm);		//sandy memcache
				return bm;
		}
	} //END bitmapOrientation()
	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================
		
	//=====SECTION 10==PAGEVIEW CUSTOM ADAPTER===========================
	//--note AddPhoto uses File arraylist instead of LPicture arraylist use in PhotoAlbum
	private class PhotoViewAdapter extends PagerAdapter {

		private Context context;
		//private ArrayList<File> listItems;
		ArrayList<PhotoOption> listItems;
		private ArrayList<PhotoOption> allPictureFiles;

		public PhotoViewAdapter(Context context, ArrayList<PhotoOption> pictList) {
			listItems=new ArrayList<PhotoOption>();
			this.context=context;
			this.listItems=pictList;
			
			setAllPictureList(pictList);
			
			
			//--setup path to application------------------------------------------------
			app=(PPGoPlacesApplication)context.getApplicationContext();
		}
		
		//--must have---------
		@Override
		public int getCount() {
			return listItems.size();
		}

		//--required this for view pager to keep the current page active------------
		@Override
		public int getItemPosition(Object object) {
			//return getItemPosition(object);
			//return listItems.indexOf(object);
			//--very important force notifydatasetchanged to reload as no 
			//--position is return
			return POSITION_NONE;			
		}
		
		//--must have------------remove and recycle view after passing into pager view-----------------
		@Override
		public void destroyItem(View collection, int position, Object view) {
			((ViewPager)collection).removeView((ImageView) view);			
		}
		
		@Override
		//--when using view as key object which view pager associates page to
		public boolean isViewFromObject(View view, Object object) {
			return view==((ImageView) object);		
		}
		
		@Override
		public Object instantiateItem(View collection, int position) {
			
			LayoutInflater inflater=(LayoutInflater) collection.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			View view=inflater.inflate(R.layout.pager_view_item, null);
			((ViewPager) collection).addView(view,0);
			
			//==my codes===================================================
			ImageView iv=new ImageView(context);
			
			//String fileName=listItems.get(position).getAbsolutePath().toString();
			//String fileName=listItems.get(position).getPath();
			
			Bitmap bitmap = null;
			//--sandy----------------------------
			imageKey = listItems.get(position).getName();
			//final String imageKey = String.valueOf(getPicture.getResId());

			bitmap = getBitmapFromMemCache(imageKey);
			if (bitmap !=null) {
				iv.setImageBitmap(bitmap);
			} else {
			//--sandy----------------------------	
				try {
					bitmap=new bitmapOrientation().execute(position).get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				iv.setImageBitmap(bitmap);
			}
			//==my codes===================================================
			
			((ViewPager)collection).addView(iv,0);
			return iv;
		}	//END instantiateItem()		

		//--ADAPTER GETTERS AND SETTERS-------------------------------	
		public void setAllPictureList(ArrayList<PhotoOption> thisList) {
			allPictureFiles = new ArrayList<PhotoOption>();
			allPictureFiles= thisList;
		}
		
		public ArrayList<PhotoOption> getAllPictureList() {
			return allPictureFiles;
		}
		
	}	//END PageView Adapter
	

	//=====SECTION 10==PAGEVIEW CUSTOM ADAPTER===========================
	
	//=====SECTION 11==SANDBOX============================================
	//--2. removed file already exists in db and cache files from photoList------------------------
	public void removeDBImage() {
		ArrayList<LPicture> pictureList = new ArrayList<LPicture>();
		pictureList=app.loadAllPictures();		//--list of picture in db
		
		ArrayList <File> existPicture = new ArrayList<File>();
		//--test physical file still exists in sdcard--------
		for (LPicture p : pictureList ) {
			File locpic= new File (p.getPicPath());
			if (locpic.exists())
				existPicture.add(locpic);			//list of current pictures tagged to location
		}
		//Log.d(TAG,"async Step 2, existPicture size "+existPicture.size());
		
		ArrayList<PhotoOption> tempList = new ArrayList<PhotoOption>();
		tempList.addAll(photoList);
		for (PhotoOption p :  tempList) {
			for (File l : existPicture) {
				if (l.getName().equalsIgnoreCase(p.getName())){
					photoList.remove(p);
				}
			}
		}
	}
	
	//--1. prepare PhotoOption arraylist of all image on sdcard, write to disk if not exists or new items found
	public void prepareImageList() {
		
		createFileList();		//get a file list of all exisitng images
		photoList = new ArrayList<PhotoOption>();
		
		boolean hasFotoFile = prefs.getBoolean("hasFotoFile", false);
		if (hasFotoFile) {
			photoList=retreiveFotoList();
		}		
		
		if (!photoList.isEmpty()) {
			
			ArrayList<PhotoOption> tempList = new ArrayList<PhotoOption>();
			ArrayList<File> fileNotFound = new ArrayList<File>();
			fileNotFound.addAll(fileListing);
			
			//--remove records that not exists on sdcard---------------------
			if (!fileListing.isEmpty()) {
				for (File f : fileListing){
					for (PhotoOption p : tempList ) {
						if (p.getName()=="cache") {
							
						} else {
							if (p.getName().equalsIgnoreCase(f.getName())) {
									tempList.add(p);
									fileNotFound.remove(f);
							}	
						}
					}
					if (f.getName()=="cache") {
						fileNotFound.remove(f);
					}
				}
			}
			photoList.clear();
			photoList.addAll(tempList);
			
			//--add new file not found in old list to photoList--------------------
			if (!fileNotFound.isEmpty()) {
				for (File f : fileNotFound) {
					PhotoOption newItem = new PhotoOption(f);
					try {
						newItem = new getOrientInfo().execute(f).get();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					if (newItem !=null)
						photoList.add(newItem);					
				}
				saveFotoList();		//new file found
			}
		} else {
			for (File f : fileListing) {
				PhotoOption newItem = new PhotoOption(f);
				newItem.setMark(false);
				photoList.add(newItem);
			}
			if (!photoList.isEmpty())
				saveFotoList();		//no photolist file exists
		}
		
		
	}
	
	//--2. call from within 1. - create file listing of file in both picture directory--------------------------
	private void createFileList() {
		fileListing = new ArrayList<File>();
		String camPath=Environment.getExternalStorageDirectory().getPath()+"/DCIM/Camera/";
		String androPath=Environment.getExternalStorageDirectory().getPath()+"/DCIM/100ANDRO/";
	
		File pictureDir = new File(camPath);

		//--get file list in DCIM/CAMERA directory---------------------------------------------
		if (pictureDir.exists()) {							//check directory exists
			File[] camList=pictureDir.listFiles();			//IMPORTANT - get files list		
			for (File f : camList) {
				fileListing.add(f);
			}
		}
		
		//--get file from 100ANDRO if exists--------------------------------------------------
		pictureDir=null;
		pictureDir = new File(androPath);
		
		if (pictureDir.exists()) {
			File[] camList=pictureDir.listFiles();
			for (File f : camList) {
				fileListing.add(f);
			}
		}
	}
	
	//--3. call from with 1. for file not found routine get scale and rotate info for file------------------------------------------------
	private class getOrientInfo extends AsyncTask <File, Void, PhotoOption> {

		@Override
		protected PhotoOption doInBackground(File... params) {
			File newFile = params[0];
			String fileName=newFile.getAbsolutePath();
			
			int scalePicture;
			int bmRotate;
			
			PhotoOption thisPic = new PhotoOption(newFile);
			thisPic.setMark(false);
			
			//--get scalefactor compare bitmap file size and display - TLA
			scalePicture=((TravelLiteActivity) getActivity()).computeScaleFactor(fileName);
			thisPic.setScale(scalePicture);
			
			//--get scalefactor compare bitmap file size and display - TLA
			//Bitmap testBm = BitmapFactory.decodeFile(fileName);
			
			String orientInfo=null;
			try {
				//imageExif = new ExifInterface(listItems.get(position).getAbsolutePath().toString());
				ExifInterface imageExif = new ExifInterface(fileName);
				orientInfo=imageExif.getAttribute(ExifInterface.TAG_ORIENTATION);
			} catch (IOException e) {
				e.printStackTrace();
			}
		
			if (orientInfo != null) {
				bmRotate = Integer.parseInt(orientInfo);
				thisPic.setRotate(bmRotate);
			} else {
				thisPic.setRotate(1);
			}
			//addBitmapToMemoryCache(String.valueOf(thisPicture.getId()), thisPic);
			return thisPic;
		}
	}
	
	//--write current list to file------------------------------
	public void saveFotoList() {
		String fotoPath=null;
		try {
			fotoPath = new createFotoListFile().execute(photoList).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		if (fotoPath != null) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(FOTOLIST, fotoPath.toString());
			editor.putBoolean("hasFotoFile", true);
			editor.commit();
		}
	}
	
	//--retrieve file to arraylist-------------------------------
	public ArrayList<PhotoOption> retreiveFotoList() {
		String fotoPath = prefs.getString(FOTOLIST, "");
		ArrayList<PhotoOption> currentList = new ArrayList<PhotoOption>() ;
		try {
			currentList = new retreiveFotoListFile().execute(fotoPath.toString()).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		if (!currentList.isEmpty()) {
			return currentList;
		} else {
			return null;
		}		
	}
	//=====SECTION 11==SANDBOX============================================

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
					Toast.makeText(getActivity().getBaseContext(), R.string.file_saved_successfully_, 
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity().getBaseContext(), R.string.file_create_not_successful_, 
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
				Toast.makeText(getActivity().getBaseContext(), R.string.photo_list_successfully_retrieved_, 
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getActivity().getBaseContext(), R.string.unable_to_retrieve_photo_list_information, 
						Toast.LENGTH_SHORT).show();
			}
		}
		
	}	
	
	//=====PARKING LOTS FOR OLD CODES=====================================
	
	//--To be replaced by asynctasks and defaultPictureList------
	//--load data for adapter-----------------------
	private ArrayList<File> loadData() {
		hLocation=app.getHLocation(dbRowId);
		
		//--prepare picture boiler plate with location information-------
		prepareBoilerPlate();
		
		//--this is no good, return below and is empty
		//-- /mnt/sdcard/Android/data/com.liseem...../files/DCIM 
		//pictureDir=getExternalFilesDir(Environment.DIRECTORY_DCIM);
		
		//--this is good, return /mnt/sdcard/DCIM/Camera---
		path=Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/";
		pictureDir = new File(path);
		
		//--100ANDRO directory for HTC and Sony---------------------------
		andropath=Environment.getExternalStorageDirectory().getPath() + "/DCIM/100ANDRO/";
		altDir=new File(andropath);
		
		//--get file list in DCIM/CAMERA directory---------------------------------------------
		fileList=pictureDir.listFiles();			//IMPORTANT - get files list

		//--get file list in DCIM/100ANDRO directory if directory exists------------------
		//Log.d(TAG, "loadData(), check DCIM/100ANDRO exists ? "+altDir.exists());
		if (altDir.exists()) {			
			altList=altDir.listFiles();
		}
		
		//--convert to file arraylist for adapter, not necessary but don't know why-------
		imageList=new ArrayList<File>();
		checkedList=new ArrayList<Boolean>();
		for (File f : fileList) {
			imageList.add(f);						//--list of pictures NOT in db
			checkedList.add(false);			//--corresponding checklist of imageList
		}
		
		if (altDir.exists() && altList.length>0) {
			for (File f: altList) {
				imageList.add(f);
				checkedList.add(false);
			}
		}

		//--remove pictures from directory list already tagged to location------------------------------
		currentList=new ArrayList<File>();		//create file list for comparison, added /DCIM/Camera/cache
		forCompare=new ArrayList<File>();
		pictureList=new ArrayList<LPicture>();
		
		//--get picture list of location--------------------------------------------
		pictureList=app.loadAllPictures();		//--list of picture in db

		//--add cache file, path to be deleted list, /mnt/sdcard/DCIM/Camera/cache------------
		File cacheFile=new File(path+"/cache");
		currentList.add(cacheFile);
		
		for (LPicture p : pictureList) {
				File tempFile=new File(p.getPicPath().toString());
				currentList.add(tempFile);			//--build a file list of db pictures for compare with imageList
		}

		//--compare currentList to imageList(both camera and 100ANDRO)----------
		forCompare=imageList;
		for (File f : currentList) {
			for (int i=0; i < forCompare.size(); i++) {
				if (forCompare.get(i).getName().equals(f.getName())){
					Log.d(TAG, "file name for forCompare i  "+forCompare.get(i).getName().toString()+" , "+f.getName().toString());
					imageList.remove(i);				//--arrive at final list of file not already in db
					checkedList.remove(i);			//--same corresponding checklist
				}
				
			}
		}
	
		return imageList;
		
	}	//END filterTaggedList
	//=====PARKING LOTS FOR OLD CODES========================================
	
	
}  //END MAIN CLASS
