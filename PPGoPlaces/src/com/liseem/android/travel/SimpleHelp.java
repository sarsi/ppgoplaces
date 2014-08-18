/* 
 * Fragment: 		SimpleHelp.java
 * Description:		Help View
 * 							
 * 
 * Created: 			May 3, 2012
 * Last updated: 	October 20, 2013
 * 
  * Changes:
 * 	- Clean up codes to work in fragment 
 * - Fixed back key crashing program.
 * 
 * 
 */
package com.liseem.android.travel;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class SimpleHelp extends Fragment {
	
	private final static String 		TAG = "SimpleHelp";

	private WebView 						webview;
	private Context 							context = getActivity();
	private PPGoPlacesApplication app;
	private AlertDialog.Builder  		goodHelp;
	private String								isAbout;
	private String								helpPage;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	
		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;
	
		//--inflate layout for fragment 1------------------------------------------------
		View v=inflater.inflate(R.layout.simple_help, container, false);
		setRetainInstance(true);
		return v;			
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.simple_help);		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setupView();
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	

	
	private void setupView() {

		//--get ppgoplaces application-----------------------------
		app									=(PPGoPlacesApplication)getActivity().getApplication();	

		//--get help page name from calling intent--------
		//Intent intent				=getActivity().getIntent();
		Bundle bundle 				= this.getArguments();			//--fragment retrieve bundle
		if (bundle !=null) {
			isAbout						=bundle.getString("helpPage");
			isAbout						=isAbout.substring(0, isAbout.indexOf("."));
			
			helpPage=("file:///android_asset/"+bundle.getString("helpPage"));
			//String helpPage=("file:///android_asset/"+isAbout.toString());
		}
		
		
		
		//--setup view--------------------------------------------------
		//webview=(WebView)findViewById(R.id.webview);
		
		webview=new WebView(getActivity().getBaseContext());
		
		WebSettings settings = webview.getSettings();
		settings.setJavaScriptEnabled(true);
		
		//--trap back key to avoid crashing simplehelp on back key exit call fragment prematurely------------
		webview.setOnKeyListener(new OnKeyListener() {
	        @Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
	            if (keyCode == KeyEvent.KEYCODE_BACK) {
	                return true;
	            }
	            // process normally
	            return false;
	        }
	    });
		//------------------------------------------------------------------------------------
		goodHelp 					= new AlertDialog.Builder(getActivity());
		
		if (isAbout.equalsIgnoreCase("about")) {
			goodHelp.setTitle(R.string.about);
		} else {
			goodHelp.setTitle(R.string.ppgoplaces_help);
			goodHelp.setIcon(R.drawable.ppgp_pt);
		}
		//webview.loadUrl("file:///android_asset/mainmenu.html");
		webview.loadUrl(helpPage.toString());

		goodHelp.setView(webview);
		goodHelp.setCancelable(true);	
		goodHelp.setPositiveButton(R.string.ok, new AlertDialog.OnClickListener(){	  
				     @Override
					public void onClick(DialogInterface dialog, int id){
				    	 //getActivity().finish();
				    	 getFragmentManager().popBackStackImmediate();
				     }
		 });

		 goodHelp.create().show();
	}
	
}		//END MAIN CLASS
