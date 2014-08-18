/*
 * Adapter:												TaskListAdapter.java
 * Summary:											Custom adapter for Holiday checklist 
 * 
 * Created: 												May 3, 2012
 * Changed last release: 						September 9, 2012
 * Last updated: 										October 1,3 2013
 * 
 * 
 * Abstract methods:getItem, getItemId, getCount and main action getView, 
 * Constructor taking two args from setListAdapter, context and ArrayList<TaskList>
 *  
 * Changes since last release:
 * 
 * Associated files:
 * Parent file: 											HolidayCheckList.java
 * 
 */

package com.liseem.android.travel.adapter;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import com.liseem.android.travel.PPGoPlacesApplication;
import com.liseem.android.travel.items.AsyncTask;
import com.liseem.android.travel.items.TaskList;
import com.liseem.android.travel.R;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TaskListAdapter extends BaseAdapter {


	private static final String TAG		="TaskListAdapter - CheckList";
		
	private ArrayList<TaskList> 		allTaskList;		//--master list unchange
	private ArrayList<TaskList> 		masterList;		//--master list unchange
	private ArrayList<TaskList> 		checkList;		//--listview adapter feed

	private ArrayList<TaskList> 		notComplete;	//--checklist - hide list holder
	private ArrayList<TaskList> 		unhideList;		//--checklist - unhide list holder

	private CheckedTextView 			checkbox;
	private TextView 							addressText;
	private TaskList 							item;
	private TaskList								lastItem;
	private Context 								context;
	private LayoutInflater 					mInflater;
	private Boolean 							hideComplete;
	private int										dbSize;

	private PPGoPlacesApplication 	app;


	//=====1.0 CONSTRUCTOR==========================================START====

	/*
	 * Important: 
	 * allTaskList contains all tasks and notCompleteTasks all unComplete tasks list throughout
	 * cycle until adapter reload, i.e. they should be readonly except in the constructor. All
	 * program should use instances.
	 */
	public TaskListAdapter(Context context, ArrayList<TaskList> loadCheckList) {
		masterList=new ArrayList<TaskList>();
		checkList=new ArrayList<TaskList>();
		
		this.context=context;
		this.masterList=loadCheckList;
		this.checkList.addAll(masterList);
		//this.allTaskList=loadCheckList;

		//--sandy----------------------
		setAllTaskList(masterList);
		dbSize=loadCheckList.size();			//--redundant
		//Log.d (TAG, "constructor loadCheckList initial size "+dbSize);

		mInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
		app=(PPGoPlacesApplication)context.getApplicationContext();
				
		//--no need since inner class of ShoppingList
		//--can call shoppingList directly, no passing required
	}
	//=====1.0 CONSTRUCTOR=========================================END=======
	

	//=====2.0 ADAPTER ABSTRACT METHODS ===============================START======

	@Override	//system added
	public int getCount() {
		return checkList.size();
	}

	@Override	//system added
	public TaskList getItem(int position) {
		return (null==checkList) ? null : checkList.get(position);
	}

	@Override	//system added
	public long getItemId(int position) {
		return (null==checkList) ? null : checkList.get(position).getId();
	}

	/*
	 * custom_list.xml to inflate, individual
	 * row of item for both name and complete fields.
	 * 
	 * @see android.widget.Adapter
	 * 		#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		
		if(convertView==null) {
			holder=new ViewHolder();
			convertView=mInflater.inflate(R.layout.custom_list, null);
			holder.text=(TextView)convertView.findViewById(R.id.address_text);
			holder.checkbox=(CheckedTextView)convertView.findViewById(android.R.id.text1);
			//holder.checkbox.setTextColor(R.drawable.text_list_item_color); //don't work
			convertView.setTag(holder);
		} else {
			holder=(ViewHolder)convertView.getTag();
		}
			holder.checkbox.setText(checkList.get(position).getName());
			holder.checkbox.setChecked(checkList.get(position).isComplete());
			if(checkList.get(position).hasAddress()) {
				holder.text.setText(checkList.get(position).getAddress());
			}
		return (convertView);		
	}

	//--VIEW HOLDER-- for inflating item view--
	public static class ViewHolder {
		CheckedTextView checkbox;
		TextView text;
		TextView timestamp;
		ImageView icon;
		ProgressBar progress;
		int position;
	}

	//=====3.0 ADAPTER HELPER METHODS ================================START======

	public void setHideComplete(boolean hide) {
		hideComplete=hide;
	}
	
	public boolean onlyComplete() {
		return hideComplete;
	}
	
	public void setAllTaskList(ArrayList<TaskList> taskList) {
		allTaskList = new ArrayList<TaskList>();
		allTaskList=taskList;
	}
	
	public ArrayList<TaskList> getAllTaskList() {
		return allTaskList;
	}
		
	
	
	//--holidaychecklist category spinner selection---------------------------------
	public void spinnerSelection(int position, boolean hide) {
		
			notComplete=new ArrayList<TaskList>();
			unhideList=new ArrayList<TaskList>();	
			
			ArrayList<TaskList> tempHelp = new ArrayList<TaskList>();
			tempHelp = getAllTaskList();
			
			//if (masterList.size() < dbSize) {
			//	masterList=app.getAllTasks();
			//}
			//unhideList= new ArrayList<TaskList>();
			//notComplete= new ArrayList<TaskList>();
			
			setHideComplete(hide);
			switch (position) {
				case 0:		//pre-Holiday
					for (TaskList t : tempHelp) {
						if (t.getCat()==0) {
							unhideList.add(t);
							if (!t.isComplete())
								notComplete.add(t);
						}						
					}
					break;
				case 1:
					for (TaskList t : tempHelp) {
						if (t.getCat()==1) {
							unhideList.add(t);
							if (!t.isComplete())
								notComplete.add(t);
						}
					}
					break;
				case 2:
					for (TaskList t : tempHelp) {
						if (t.getCat()==2) {
							unhideList.add(t);
							if (!t.isComplete())
								notComplete.add(t);
						}
					}
					break;
				case 3:
					for (TaskList t : tempHelp) {
						if (t.getCat()==3) {
							unhideList.add(t);
							if (!t.isComplete())
								notComplete.add(t);
						}
					}
					break;
				case 4:		//show all
					for (TaskList t : tempHelp) {
						if (t.getCat()<4) {
							unhideList.add(t);
							if (!t.isComplete())
								notComplete.add(t);
						}
					}
					break;
				case 5:		//would be 8 in future, packing list
					for (TaskList t : tempHelp) {
						if (t.getCat()>4) {
							unhideList.add(t);
							if (!t.isComplete())
								notComplete.add(t);
						}
					}
					break;
				default:
					break;
			}
			checkList.clear();
			if (hide) {			
				checkList.addAll(notComplete);
			} else {				
				checkList.addAll(unhideList);
			}
			notifyDataSetChanged();		
	}
	
	//--CRUB-------------------------------------------------------------------------------------
	//--called from onClickItem listener -----------------------------------------------
	public void toggleItem(int position) {
		TaskList item = getItem(position);
		item.toggleComplete();
		/*if (onlyComplete()) {		//crash system when delete from checklist, out of bound error
			checkList.remove(item);
		}*/
		notifyDataSetChanged();
	}
	

	//--remove single task item----------------------------------
	public void deleteTask(TaskList task) {
		TaskList t=new TaskList();
		t=task;
		checkList.remove(t);
		//--remove task from master list in setAllTaskList-----
		ArrayList<TaskList> tempHelp = new ArrayList<TaskList>();
		tempHelp = getAllTaskList();
		tempHelp.remove(t);
		setAllTaskList(tempHelp);
		notifyDataSetChanged();
	}
		
	//--remove completed tasks-----------------------------------
	public void removeCompletedTasks() {
		boolean deleteComplete=false;
		try {
			//deleteComplete = new deleteAllCompleted().execute(checkList).get();
			deleteComplete = new deleteAllCompleted().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, checkList).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		if (deleteComplete)
			notifyDataSetChanged();
	}
	
	private class deleteAllCompleted extends AsyncTask <ArrayList<TaskList>, Void, Boolean> {
				
		@Override
		protected Boolean doInBackground(ArrayList<TaskList>... params) {
			ArrayList<TaskList> tempHelp=new ArrayList<TaskList>();
			tempHelp.addAll(params[0]);

			for (TaskList t : checkList) {
				if (t.isComplete()) {
					tempHelp.remove(t);
				}
			}
			
			checkList.clear();
			checkList.addAll(tempHelp);
			
			ArrayList<TaskList> allListHelp=new ArrayList<TaskList>();
			allListHelp=getAllTaskList();
			tempHelp.clear();
			tempHelp.addAll(allListHelp);
			for (TaskList t : allListHelp) {
				if (t.isComplete()) {
					tempHelp.remove(t);
				}
			}
			setAllTaskList(tempHelp);
			return true;
		}
	}
	
	
	//--uncheck all tasks------------------------------------------------
	public void unMarkAllTasks() {
	
		ArrayList<TaskList> unCheckList = new ArrayList<TaskList>();

		//checkList.addAll(currentList);
		 try {
			unCheckList = new unCheckAllTasks().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, checkList).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		 
		 if (!unCheckList.isEmpty()) {
		 checkList.clear();
		 checkList.addAll(unCheckList);
		 notifyDataSetChanged();
		 }
	}
	
	private class unCheckAllTasks extends AsyncTask<ArrayList<TaskList>, Void, ArrayList<TaskList>> {
		
		@Override
		protected ArrayList<TaskList> doInBackground(ArrayList<TaskList>... params) {
			
			ArrayList<TaskList> currentList = new ArrayList<TaskList>();
			currentList.addAll(params[0]);
			
			ArrayList<TaskList> tempHelp = new ArrayList<TaskList>();
			tempHelp = getAllTaskList();		
			for (int i=0 ; i< tempHelp.size(); i++) {
				if (tempHelp.get(i).isComplete()) {
					tempHelp.get(i).setComplete(false);
				}
			}
			setAllTaskList(tempHelp);
			
			for (int i=0 ; i< currentList.size(); i++) {
				if (currentList.get(i).isComplete()) {
					currentList.get(i).setComplete(false);
				}
			}
			return currentList;
		}
	}

	//--add and update may not need if force reload at add adapter from onResume()
	//--add task from add task-----------------------
	public void addTask(TaskList task) {
			checkList.add(task);
			allTaskList.add(task);
			//unhideList.add(task);
			//notComplete.add(task);
			notifyDataSetChanged();
	}
	
	//--update task after edit task-----------------------
	public void updateTask(TaskList task) {
		int lastPosition=app.getLastPosition();
		checkList.set(lastPosition, task);
		
		//--remove old task using last position getter setter, and add new task--
		lastItem=new TaskList();
		lastItem=app.getLastTask();
		
		ArrayList<TaskList> tempHelp = new ArrayList<TaskList>();
		tempHelp = getAllTaskList();
		int position=tempHelp.indexOf(lastItem);
		tempHelp.set(position, task);
		setAllTaskList(tempHelp);
		notifyDataSetChanged();
	}
	
	//--CRUB-------------------------------------------------------------------------------------

	//--reset to user default checklist----------------------------------------
	//--called from another async so should not call another async, i think
	public void resetToUserDefaultList(ArrayList<TaskList> userList) {
			
		//new resetTaskListDB().execute(userList);		//v1.2			//COULD BE ASYNC ISSUE
		
		/*try {
			new resetTaskListDB().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, userList).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}*/
		
		checkList.clear();
		checkList.addAll(userList);
		setAllTaskList(userList);
		boolean isEmpty=app.isTaskDBEmpty();
		if (!isEmpty)
			app.removeAllTasks();
		for (TaskList t: checkList) {
			app.addTask(t);
		}
		notifyDataSetChanged();
	}
	

	//--async to reset all db either factory or user default-------------------------
	public class resetTaskListDB extends AsyncTask<ArrayList, Void, Boolean> {

		@Override
		protected Boolean doInBackground(ArrayList... params) {
			ArrayList<TaskList> newList=new ArrayList<TaskList>();
			newList=params[0];
			
			boolean isEmpty=app.isTaskDBEmpty();
			if (!isEmpty)
				app.removeAllTasks();
			for (TaskList t: newList) {
				app.addTask(t);
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {		
			if (result) {
				checkList.clear();
				allTaskList=app.getAllTasks();
				checkList=allTaskList;
				notifyDataSetChanged();
			}
		}
	}

	
} //--END OF MAIN CLASS


