//--in location context menu ------under EVALUATION---------------------

package com.liseem.android.travel;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageGalleryPicker extends Fragment {
	
	//=====SECTION 1==DECLARATION===================================

	private final static String 			TAG="ImageGalleryPicker";
	
	private PPGoPlacesApplication 	app;
	private SharedPreferences 			prefs;
	
	private ImageView 						imageView;
	private Button 								selectPicture;
	

	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;

		//--inflate layout for fragment 1------------------------------------------------
		View v=inflater.inflate(R.layout.image_gallery_picker, container, false);
		setRetainInstance(true);
		
		return v;			
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.image_gallery_picker);
		
		setupView();
		
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	/*@Override
	public void onBackPressed() {
		super.onBackPressed();
	}*/
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 3==SETUP VIEWS====================================

	protected void setupView() {
		
		//--setup path to application---------------------------
		app					=(PPGoPlacesApplication)getActivity().getApplication();
		
		//--setup views--------------------------------------------
		imageView 		= (ImageView)getActivity().findViewById(R.id.ChosenImageView);
		selectPicture 	= (Button) getActivity().findViewById(R.id.ChoosePictureButton);
		selectPicture.setOnClickListener(new selectPictureListener());		
	}

	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================
	
	//=====SECTION 4==ADD ADAPTER===================================

	//=====SECTION 5==SET LISTENER====================================
	
	private class selectPictureListener implements OnClickListener {
	
		@Override
		public void onClick(View v) {
			Intent findPicture = new Intent(Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(findPicture, 0);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode,
			Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			
			
			if (resultCode == Activity.RESULT_OK) {
			Uri imageFileUri = data.getData();
			
			//--check whether picture already existed in db--------------
			String imagePath;
			imagePath=((TravelLiteActivity) getActivity()).getPath(data.getData());
			String havePict=app.queryPictureExists(imagePath);
			
			if (havePict !=null) {
				Toast.makeText(getActivity().getBaseContext(), "picture is already tagged to location"+imagePath, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getActivity().getBaseContext(), "picture not tag to location, will do so..."+imagePath, Toast.LENGTH_LONG).show();
			}
			
			Display currentDisplay = getActivity().getWindowManager().getDefaultDisplay();
			
			int dWidth = currentDisplay.getWidth();
			int dHeight = currentDisplay.getHeight()/2 - 100;
			
			try {
				// Load up the image's dimensions not the image itself
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				
				Bitmap bm = BitmapFactory.decodeStream(getActivity().getContentResolver().
						openInputStream(imageFileUri), null, options);
				
				int heightRatio = (int)Math.ceil(options.outHeight/(float)dHeight);
				int widthRatio = (int)Math.ceil(options.outWidth/(float)dWidth);
				if (heightRatio > 1 && widthRatio > 1)
				{
					if (heightRatio > widthRatio) 
					{
					options.inSampleSize = heightRatio;
					}
					else {
					options.inSampleSize = widthRatio;
					}
				}
				
				options.inJustDecodeBounds = false;
				bm = BitmapFactory.decodeStream(getActivity().getContentResolver().
					openInputStream(imageFileUri), null, options);
				
				imageView.setImageBitmap(bm);
			} catch (FileNotFoundException e) {
			}
		}
	}
	
	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================
	
	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//=====SECTION 10==SANDBOX======================================

				
} 	//END MAIN CLASS
