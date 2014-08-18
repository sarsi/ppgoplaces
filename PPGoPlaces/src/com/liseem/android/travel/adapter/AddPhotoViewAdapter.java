package com.liseem.android.travel.adapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.liseem.android.travel.PPGoPlacesApplication;
import com.liseem.android.travel.R;
import com.liseem.android.travel.TravelLiteActivity;
import com.liseem.android.travel.items.LPicture;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


public class AddPhotoViewAdapter extends PagerAdapter {
	
	private final static String TAG="AddPhotoViewAdapter";

	private PPGoPlacesApplication app;
	private TravelLiteActivity			travelLite;
	
	private Context 							context;
	private ArrayList<File> 			listItems;
	private int 									bmRotate;
	private String 							orientInfo;
	private ExifInterface 					imageExif;
	private Matrix 							matrix;
	private ArrayList<Boolean> 	checkedList;
	private ArrayList<LPicture> 		pictureList;
	private Bitmap 							bm;
	private int 									scaleFactor;
	

	private SharedPreferences prefs;

	public AddPhotoViewAdapter(Context context, ArrayList<File> listItems) {
		this.context=context;
		this.listItems=listItems;
		
		//--setup path to application------------------------------------------------
		app=(PPGoPlacesApplication)context.getApplicationContext();
		travelLite=(TravelLiteActivity)context.getApplicationContext();
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
		return POSITION_NONE;			//very important force notifydatasetchanged to reload as no position is return

	}
	
	//--must have------------remove and recycle view after passing into pager view-----------------
	@Override
	public void destroyItem(View collection, int position, Object view) {
		((ViewPager)collection).removeView((ImageView) view);
		
	}

	//--must have------------this add view to pager view----------------------------------------------------
	@Override
	public Object instantiateItem(View collection, int position) {
		
		LayoutInflater inflater=(LayoutInflater) collection.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View view=inflater.inflate(R.layout.pager_view_item, null);
		((ViewPager) collection).addView(view,0);
		
		
		//==my codes===================================================
		ImageView iv=new ImageView(context);
		
		String fileName=listItems.get(position).getAbsolutePath().toString();
		scaleFactor = travelLite.computeScaleFactor(fileName);			//--compute picture scaleFactor for screen size		
		
		BitmapFactory.Options options=new BitmapFactory.Options();
		options.inSampleSize=scaleFactor; 											//--scale factor
		//options.inSampleSize=6;		//should be bigFactor from shared preferences
		bm=BitmapFactory.decodeFile(listItems.get(position).getAbsolutePath().toString(), options);
		
		//--decoding exif information----------------------------------
			try {
				imageExif = new ExifInterface(listItems.get(position).getAbsolutePath().toString());
				orientInfo=imageExif.getAttribute(ExifInterface.TAG_ORIENTATION);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//--rotate image base on orientation info from exif file-------------------
			matrix=new Matrix();
			if(orientInfo != null)
				bmRotate=Integer.parseInt(orientInfo);
				switch(bmRotate) {
					case 1:			//normal no rotation		
						matrix.postRotate(0);
						bm=Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);	
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
				
				
				iv.setImageBitmap(bm);
				//==my codes===================================================
				
				((ViewPager)collection).addView(iv,0);
		return iv;
	}

	//--must have-----------------------------------------------------------------------------------------------
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view==((ImageView) object);		//when using view as key object which view pager associates page to
	}


	
	//==EXTERNAL HELPERS CLASS================================================
	
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
				tempHelp.setPicPath((listItems.get(i).getAbsolutePath().toString()));
				app.insertPicture(tempHelp);
				Toast.makeText(context, R.string.pictures_tagged, Toast.LENGTH_SHORT).show();
			}
		}
		notifyDataSetChanged();
	}		//END addPictureList 
	
	//--remove all selected pictures tagging from location album------------------
	public void removeAllSelectedItems(ArrayList<LPicture> pictureList) {
		ArrayList<LPicture> tempHelp=new ArrayList<LPicture>();
		tempHelp=pictureList;
		for(int i=0; i<tempHelp.size(); i++) {
			if (pictureList.get(i).isMark()) {
				listItems.remove(i);
				app.deletePicture(pictureList.get(i).getId());
			}
		}
		notifyDataSetChanged();
		Toast.makeText(context, "Selected Pictures Deleted Successfully", Toast.LENGTH_SHORT).show();
	}	
	
	 public void removePicture(int position) {
		 listItems.remove(position);
	 }

}		//END MAIN CLASS
