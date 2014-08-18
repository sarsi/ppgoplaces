package com.liseem.android.travel.adapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import com.liseem.android.travel.PPGoPlacesApplication;
import com.liseem.android.travel.R;
import com.liseem.android.travel.TravelLiteActivity;
import com.liseem.android.travel.items.LPicture;

import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.ExifInterface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.FloatMath;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;

//=================================================
// SANDY LOT TO FIX, CROSS CHECK INNER CLASS AT PHOTOALBUM
//=================================================
public class ViewPhotoAdapter extends PagerAdapter {
	
	private final static String 		TAG="ViewPhotoAdapter";

	private PPGoPlacesApplication app;
	private TravelLiteActivity			travelLite;
	
	private Context 							context;
	private ArrayList<LPicture> 		listItems;
	private ArrayList<LPicture> 		allPictureList;
	private ArrayList<LPicture> 		tempPList;
	private LPicture 							pictureThis;
	private ImageView 					iv;
	private FragmentManager			fm;
	
	//--sandy memory cache--------------------------
	private LruCache<String, Bitmap> mMemoryCache;
	private String 								imageKey;
	private int										memClass;
	
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
	

	private SharedPreferences prefs;

	public ViewPhotoAdapter(Context context, ArrayList<LPicture> listItems) {
		this.context	= context;
		this.listItems	= listItems;
		
		setAllPictureList(listItems);
		
		//--setup path to application------------------------------------------------
		app					=(PPGoPlacesApplication)context.getApplicationContext();
		travelLite			=(TravelLiteActivity)context.getApplicationContext();
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
	
	//--must have------------remove and recycle view after passing into pager view-----------------
	@Override
	public void destroyItem(View collection, int position, Object view) {
		((ViewPager)collection).removeView((ImageView) view);			
	}

	//--must have------------this add view to pager view----------------------------------------------------
	@Override
	public Object instantiateItem(View collection, int position) {
	
		/*LayoutInflater inflater=(LayoutInflater) collection.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
		View view=inflater.inflate(R.layout.pager_view_item, null);
		ImageView iv = (ImageView)findViewById(R.id.pagerView1);		*/
		
		iv=new ImageView(context);
		
		//--pinch to zoom-----------
		iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
		//iv.setOnTouchListener(new PinchOnTouchListener());
		((ViewPager) collection).addView(iv,0);		//add imageView instead of view
		//((ViewPager) collection).addView(view,0);
		//registerForContextMenu(iv);

		
		LPicture getPicture = new LPicture();
		getPicture = listItems.get(position);
		
		Bitmap bitmap=null;
		//--sandy----------------------------
		imageKey = getPicture.getFileName();
		//final String imageKey = String.valueOf(getPicture.getResId());
		
		bitmap = getBitmapFromMemCache(imageKey);
		if (bitmap != null) {
			iv.setImageBitmap(bitmap);
		} else {
		//--sandy----------------------------

			/*try {
				bitmap = new bitmapOrientation().execute(getPicture).get();
				//bitmap =  new scaleBitmap(getActivity().getBaseContext()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, getPicture.getFileName()).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}*/
			if (bitmap != null)
				iv.setImageBitmap(bitmap);
		}

		//if (bitmap != null)
		//	iv.setImageBitmap(bitmap);
		return iv;

	} 	//END instantiateItem

	//--must have-----------------------------------------------------------------------------------------------
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view==((ImageView) object);		//when using view as key object which view pager associates page to
	}
	
	//==EXTERNAL HELPERS CLASS================================================

	//--using cache memory
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
	    if (getBitmapFromMemCache(key) == null) {
	        mMemoryCache.put(key, bitmap);
	    }
	}
	
	public Bitmap getBitmapFromMemCache(String key) {
	    return mMemoryCache.get(key);
	}
	
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
		 //destroyItem(viewPage, filePosition, app.findViewById(R.id.pagerView1));  //IMPORTANT
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
	
	 
}		//--END MAIN custom pager view adapter
