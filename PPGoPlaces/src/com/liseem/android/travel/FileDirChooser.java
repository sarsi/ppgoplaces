package com.liseem.android.travel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.liseem.android.travel.adapter.FileListAdapter;
import com.liseem.android.travel.items.FileOption;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class FileDirChooser extends Fragment {

	//=====SECTION 1==DECLARATION===================================
	
	private final static String 			TAG="FileDirChooser";
	
	private final static String 			FOLDER="Folder";
	private final static String 			PARENT_DIRECTORY="Parent Directory";

	//--system-----------------------------------------------
	private PPGoPlacesApplication		app;
	private SharedPreferences 			prefs;
	private SharedPreferences 			directoryList;
	private Context 								context;		
	
	//--views-------------------------------------------------
	private TextView 							infoText;
	private TextView 							emptyList;
	private ListView 							listView;
	//private Button								addDir;
	private FileListAdapter					adapter;
	
	//--programmatics-------------------------------------
	private File 									currentDir;
	private List<FileOption> 				dir ;
	private List<FileOption> 				files;
	
	//=====SECTION 1==DECLARATION===================================

	//=====SECTION 2==LIFECYCLE METHODS===============================

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;

		//--inflate layout for fragment 1------------------------------------------------
		View v=inflater.inflate(R.layout.file_list, container, false);
		setRetainInstance(true);
		
		return v;			
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.file_list);
		
		setupView();
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		
		addAdapter();
		setListeners();
	}


	@Override
	public void onPause() {
		super.onPause();
	}

	
	//=====SECTION 2==LIFECYCLE METHODS===============================

	//=====SECTION 3==SETUP VIEWS====================================


	protected void setupView() {
		
		//--system----------------------------------------------------------------------
		app						=(PPGoPlacesApplication)getActivity().getApplication();

		//--views-------------------------------------------------------------------------
		infoText				=(TextView)getActivity().findViewById(R.id.infoText);
		emptyList			=(TextView)getActivity().findViewById(R.id.emptyList);
		listView				=(ListView)getActivity().findViewById(R.id.listItem);
		//addDir				=(Button)getActivity().findViewById(R.id.addButton);
		listView.setEmptyView(emptyList);
		
		//--root dir setup----------------------------------------------------------------
		//currentDir=new File(Environment.getExternalStorageDirectory(),"");
		currentDir=new File("sdcard");
		populate(currentDir);		
	}
	
	//--adding all dirs and files to arraylist before passing to adapter-------
	private void populate(File file) {
		
		File[] dirs = file.listFiles();
		getActivity().setTitle(R.string.current_directory_+file.getName());
        
        if(!file.getName().equalsIgnoreCase("sdcard")) {
        	String temp=file.getParent().toString();
        	//Log.d(TAG, " what is the parent directory "+temp.toString());
        }

        dir = new ArrayList<FileOption>();
        files = new ArrayList<FileOption>();
        
        try{
        	String findDot=".";
            for(File f : dirs) {
            String firstChar=f.getName().toString().substring(0,1);
            
	            if (!firstChar.equalsIgnoreCase(findDot)) {
	               if(f.isDirectory()) {
	            	   //--constructor for FileOption - file/dir name, type of data, complete path------------
	                   dir.add(new FileOption(f.getName(),FOLDER,f.getAbsolutePath()));
	               } else {
	                   files.add(new FileOption(f.getName(),"File Size: "+f.length(),f.getAbsolutePath()));
	               }
	            }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        
        //--sort and finally add files to end of list of dirs---------------------
        Collections.sort(dir);
        Collections.sort(files);
        dir.addAll(files);
        
        //--add return to parent .. except sdcard-----------------------------
        if(!file.getName().equalsIgnoreCase("sdcard"))
            dir.add(0, new FileOption("..", PARENT_DIRECTORY ,file.getParent()));
     
        
        //--finally pass arraylist dir to adapter to feed into listview--------
		//adapter=new FileListAdapter(FileDirChooser.this, R.layout.file_item, dir);	//--before fragment
		adapter=new FileListAdapter(getActivity(), R.layout.file_item, dir);
		listView.setAdapter(adapter);
		registerForContextMenu(listView);
	}
	
	//=====SECTION 3==SETUP VIEWS====================================

	//=====SECTION 4==ADD ADAPTER===================================

	private void addAdapter() {
		
		//listItem.setAdapter(adapter);
		//registerForContextMenu(listItem);
	}
	
	//=====SECTION 4==ADD ADAPTER===================================

	//=====SECTION 5==SET LISTENER====================================

	private void setListeners() {
		
		listView.setOnItemClickListener(new FileItemClickListener());
	}
	
	
	//=====SECTION 5==SET LISTENER====================================

	//=====SECTION 6==LISTENER METHODS================================

	private class FileItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> listView, View convertView, int position,
				long id) {
			//super.onListItemClick(listView, convertView, position, id);
			FileOption fileSelected = adapter.getItem(position);
			String testStr=fileSelected.getPath().toString();
			//Log.d(TAG, "file or directory selected "+testStr.toString());
			if(fileSelected.getData().equalsIgnoreCase(FOLDER) || 
					fileSelected.getData().equalsIgnoreCase(PARENT_DIRECTORY)){
					currentDir = new File(fileSelected.getPath());
					//Toast.makeText(context, "hey it works !!!", Toast.LENGTH_SHORT).show();
					populate(currentDir);
			}
			
			File testFile=new File(fileSelected.getPath().toString());
			
			//Toast.makeText(getBaseContext(), "item click is "+fileSelected.getName(), Toast.LENGTH_SHORT).show();
			//Log.d(TAG, "in FileItemClickListener, string temp "+testFile.isFile());
		}
	}
	//=====SECTION 6==LISTENER METHODS================================

	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 7==HELPER METHODS==================================

	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	//--menu listing action from above context menu choice---------------------------------------	
	private void CreateMenu(Menu menu) {
		
	MenuItem mnu1=menu.add(0,1,1, "Settings");
		{ mnu1.setIcon(android.R.drawable.ic_menu_preferences);}

	MenuItem mnu2=menu.add(0,2,2, "Help");
		{ mnu2.setIcon(android.R.drawable.ic_menu_help); }
		
	MenuItem mnu3=menu.add(0,3,3, "Sort By Start Date");
		{ mnu3.setIcon(android.R.drawable.ic_menu_sort_by_size); }
		
	MenuItem mnu4=menu.add(0,4,4, "Sort By Name");
		{ mnu4.setIcon(android.R.drawable.ic_menu_sort_by_size); }
			
	}
	
	@TargetApi(11)
	private void addMenuToActionBar(Menu menu) {
	
			MenuItem mnu1=menu.add(0,1,1, "Settings");
				{ mnu1.setIcon(android.R.drawable.ic_menu_preferences); 
					mnu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);}
		
			MenuItem mnu2=menu.add(0,2,2, "Help");
				{ mnu2.setIcon(android.R.drawable.ic_menu_help); 
				mnu2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);}
				
			MenuItem mnu3=menu.add(0,3,3, "Sort By Start Date");
				{ mnu3.setIcon(android.R.drawable.ic_menu_sort_by_size); 
				mnu3.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);}
				
			MenuItem mnu4=menu.add(0,4,4, "Help");
				{ mnu4.setIcon(android.R.drawable.ic_menu_sort_by_size); 
				mnu4.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);}			
	}

	
	private boolean MenuChoice(MenuItem item)
	{
		/*SharedPreferences.Editor editor = prefs.edit();
		switch (item.getItemId()) {

		case 1:
			Intent settings=new Intent (ViewHoliday.this, MyPreferences.class);
			startActivity(settings);
			return true;
		case 2:
			Intent  simplehelp=new Intent(ViewHoliday.this, SimpleHelp.class);
			Bundle helpPage=new Bundle();
			simplehelp.putExtra("helpPage", "viewholiday.html");
			startActivity(simplehelp);		
			return true;
		case 3:
			//--sort listview by date----------------
			editor.putInt("sortHolidayList", 1);
			editor.commit();
			adapter.sortHolidayView(1);
			return true;
		case 4:
			//--sort listview by name---------------
			editor.putInt("sortHolidayList", 2);
			editor.commit();
			adapter.sortHolidayView(2);
			return true;
		} */
		return false;
	}
	//--menu choice-----------------------------------------------------------------------------------------------------
	
	//--context menu---------------------------------------------------------------------------------
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
			menu.setHeaderIcon(android.R.drawable.ic_menu_gallery);
			menu.setHeaderTitle("Select Photo Directory");
			//MenuInflater inflater = getMenuInflater();
			//inflater.inflate(R.menu.context_menu, menu);
			menu.add(0,  0, 0, "Select Directory");
			//menu.add(0,  1, 1, "Delete ");
			//menu.add(0,  2, 2, "Add New Location");
			//menu.add(0,  3, 3, "View Locations");
			//menu.add(0,  4, 4, "View Locations Map");
			//menu.add(0,  5, 5, "View HOME Locations");
			super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item){
		AdapterContextMenuInfo info=(AdapterContextMenuInfo) item.getMenuInfo();
		FileOption fileSelected = adapter.getItem(info.position);
		String filePath=fileSelected.getPath().toString();

		switch (item.getItemId()) {
			case 0:
				//--RETURN SELECTED PATH--------------------------------------------		
				File testDir = new File(filePath.toString());
				if (testDir.isDirectory() && filePath != null) {
					Intent intent = new Intent();
					intent.putExtra("directory path", filePath.toString());
					getActivity().setResult(Activity.RESULT_OK, intent);
					getActivity().finish();
					return true;
				} else {
					((TravelLiteActivity) getActivity()).showOkAlertDialog("Not a directory");
					getActivity().finish();
					return false;
				} 
			default:
				return super.onContextItemSelected(item);
		}
	}
	//--context menu----------------------------------------------------------END-------------------			
	//=====SECTION 8==MENU AND DIALOG METHODS===========================

	//=====SECTION 9==THREAD AND ASYNCTASK METHODS=======================

	//=====SECTION 10==SANDBOX======================================

	
}	//END MAIN CLASS



