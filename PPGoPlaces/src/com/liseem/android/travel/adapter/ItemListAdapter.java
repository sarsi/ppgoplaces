/*
 * Adapter: 							ItemListAdapter.java
 * Fragment:						ShoppingList.java
 * Description: 					ListView Adapter for ShoppingList.java
 * 
 * Created: 							May 3, 2012
 * Last release:					September 9, 2012
 * Last updated: 					November 12, 2013
 * 
 * Changes:
 * - Tidy codes and group CRUB in single location
 * 
 * Call from ShoppingList 2.1 passing into adapter shoppingList, 4 abstract 
 * classes generated, getItem, getItemId, getCount and main action
 * getView, along with a constructor taking two args from 
 * setListAdapter (2.1), i.e. context and ArrayList<shopping>
 * 
 * Changes:
 * - Tidy codes and documentation
 * 
 */

package com.liseem.android.travel.adapter;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import com.liseem.android.travel.PPGoPlacesApplication;
import com.liseem.android.travel.R;
import com.liseem.android.travel.items.AsyncTask;
import com.liseem.android.travel.items.Shopping;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ItemListAdapter extends BaseAdapter  {


	private static final String 	  		TAG="ItemListAdapter - Shopping";
	
	private ArrayList<Shopping> 		shoppingList=new ArrayList<Shopping>();
	private ArrayList<Shopping> 		allItemList=new ArrayList<Shopping>();
	private ArrayList<Shopping> 		notComplete=new ArrayList<Shopping>();
	private ArrayList<Shopping> 		unhideList=new ArrayList<Shopping>();
	private ArrayList<Shopping> 		filteredList=new ArrayList<Shopping>();		//use for holding last filtered List for holiday view
	
	private CheckedTextView 			checkbox;
	private TextView 							addressText;
	private Shopping 							item;
	private Context 								context;
	private LayoutInflater 					mInflater;
	
	//--hide complete toggle---------------
	private boolean 							hideComplete;
	
	//--holiday filter id------------------------
	private boolean 							showAll=true;			//holiday filter
	private int			 							selectList=0;				//holiday id, if showAll false
	
	private PPGoPlacesApplication 	app;
	
	//--view adapter currently no longer using see ShoppingListItem.java--
	//private ShoppingListItem shoplist;

	
	
	//=====1.0 CONSTRUCTOR==========================================START====

	//--Constructor------------------------------------------------------------------------------- 
	public ItemListAdapter(Context context, ArrayList<Shopping> shopList) {

		this.context			=context;
		this.allItemList		=shoppingList; 					//--allItemList is keep for read only except reload by adapter
		this.shoppingList.addAll(allItemList);

		//this.filteredList		=shopList;
		
		//Log.d(TAG, "shopping list allItemList before setAllItemList "+allItemList.size());
		setAllItemList(shopList);
		
		/*for (Shopping s: allItemList) {
			if(!s.isComplete()) {
				notComplete.add(s);
			}	
		}*/
		
		mInflater			=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		//--setup path to application------------------------------------------------
		app					=(PPGoPlacesApplication)context.getApplicationContext();
		
	}

	
	//=====2.0 ADAPTER ABSTRACT METHODS ===============================START======

	@Override	//system added
	public int getCount() {
		return shoppingList.size();
	}

	@Override	//system added
	public Shopping getItem(int position) {
		return (null==shoppingList) ? null : shoppingList.get(position);
	}

	@Override	//system added
	public long getItemId(int position) { 
		return (null==shoppingList) ? null : shoppingList.get(position).getId();
	}
	
	/*
	 * obsoleted the use of ShoppingListItem, coded directly in the getView
	 * eliminated the complexity of codes troubleshooting.
	 *  
	 * custom_list.xml to inflate, individual
	 * row of item for both name and complete fields.
	 * 
	 * @see android.widget.Adapter
	 * 		#getView(int, android.view.View, android.view.ViewGroup)
	 */

	//--Get View---------------------------------------------------------------------------------------- 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		
		if(convertView==null) {
			holder=new ViewHolder();
			convertView=mInflater.inflate(R.layout.shopping_item_list, null);
			//holder.text=(TextView)convertView.findViewById(R.id.address_text);
			holder.priceText=(TextView)convertView.findViewById(R.id.price_text);
			holder.checkbox=(CheckedTextView)convertView.findViewById(android.R.id.text1);
			convertView.setTag(holder);
		} else {
			holder=(ViewHolder)convertView.getTag();
		}
		holder.checkbox.setText(shoppingList.get(position).getName());
		holder.checkbox.setChecked(shoppingList.get(position).isComplete());

		StringBuilder subText=new StringBuilder();
		if (shoppingList.get(position).getAddress() != null) {
			if (shoppingList.get(position).getAddress().toString().length() > 10)
				subText.append("\tPicture: YES   ");
		}
		
		if (!shoppingList.get(position).getPrice().isEmpty()) {
			subText.append("\tTarget Price : "+shoppingList.get(position).getPrice());
		}
		
		if (subText !=null && subText.length() !=0) {
			holder.priceText.setVisibility(View.VISIBLE);
			holder.priceText.setText(subText.toString());
		} else {
			holder.priceText.setVisibility(View.INVISIBLE);
		}  
		
		return (convertView);
	}

	//--View Holder------------------------------------------------------------------------------------- 	
	public static class ViewHolder {
		CheckedTextView checkbox;
		//TextView text;
		TextView priceText;
		//TextView timestamp;
		//ImageView icon;
		//ProgressBar progress;
		//int position;
	}
	
	
	//=====3.0 ADAPTER HELPER METHODS ================================START======

	public void setHideComplete(boolean hide) {
		hideComplete=hide;
	}
	
	public boolean onlyComplete() {
		return hideComplete;
	}
	
	public void setAllItemList(ArrayList<Shopping> itemList) {
		allItemList = new ArrayList<Shopping>();
		allItemList=itemList;
	}
	
	public ArrayList<Shopping> getAllItemList() {
		return allItemList;
	}
	
	//=========Adapter List create, read, update and delete Methods=========================
	
	//--add holiday-------------------------------------------
	public void addItem(Shopping newItem) {
		shoppingList.add(newItem);
		
		ArrayList<Shopping> tempHelp = new ArrayList<Shopping>();
		tempHelp=getAllItemList();
		tempHelp.add(newItem);
		setAllItemList(tempHelp);
		
		//allItemList.add(newItem);
		//filteredList=shoppingList;
		notifyDataSetChanged();
	}	
	
	//--delete holiday----------------------------------------
	public void deleteItem(Shopping item) {
		shoppingList.remove(item);
		
		ArrayList<Shopping> tempHelp = new ArrayList<Shopping>();
		tempHelp=getAllItemList();
		tempHelp.remove(item);
		setAllItemList(tempHelp);
		
		//allItemList.remove(item);
		//filteredList=shoppingList;
		notifyDataSetChanged();
	}
	
	public void updateItem(Shopping item) {
		int lastPosition = app.getLastPosition();
		shoppingList.set(lastPosition, item);

		//--remove old task using last position getter setter, and add new task--
		Shopping lastItem=new Shopping();
		lastItem=app.getLastItem();
		
		ArrayList<Shopping> tempHelp = new ArrayList<Shopping>();
		tempHelp=getAllItemList();
		int position=tempHelp.indexOf(lastItem);
		tempHelp.set(position, item);
		setAllItemList(tempHelp);
		
		notifyDataSetChanged();
	}
	
	//--toggle checkbox check and unchecked--------------------------
	public void toggleItemSelect(int position) {
		
		Shopping item = new Shopping();
		item = getItem(position);
		item.toggleComplete();
		
		ArrayList<Shopping> tempHelp = new ArrayList<Shopping>();
		tempHelp=getAllItemList();
		int place=tempHelp.indexOf(item);
		tempHelp.get(place).toggleComplete();
		setAllItemList(tempHelp);
		
		//shoppingList.get(position).toggleComplete();
		//allItemList.get(position).toggleComplete();
		
		notifyDataSetChanged();
	}
	
	//--CRUB-------------------------------------------------------------------------------------	
	
	//--remove completed shopping-----------------------
	public void removeCompletedTasks() {
		boolean deleteComplete=false;
		try {
			//deleteComplete = new deleteAllCompleted().execute(checkList).get();
			deleteComplete = new deleteAllCompleted().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, shoppingList).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		if (deleteComplete)
			notifyDataSetChanged();		
		
	}
	
	private class deleteAllCompleted extends AsyncTask <ArrayList<Shopping>, Void, Boolean> {

		@Override
		protected Boolean doInBackground(ArrayList<Shopping>... params) {
			ArrayList<Shopping> tempHelp = new ArrayList<Shopping>();
			tempHelp.addAll(params[0]);
			
			for (Shopping s : shoppingList) {
				if (s.isComplete()) 
					tempHelp.remove(s);
			}
			shoppingList.clear();
			shoppingList.addAll(tempHelp);
			
			ArrayList<Shopping> allListHelp = new ArrayList<Shopping>();
			allListHelp=getAllItemList();
			tempHelp.clear();
			for (Shopping s : allListHelp) {
				if (!s.isComplete()) 
					tempHelp.add(s);
			}
			setAllItemList(tempHelp);
			return true;
		}		
	}
	
	
	//--main filter list view--------------------------------------------
	public void spinnerCatSelection(int position, boolean hide) {
		
		notComplete = new ArrayList<Shopping>();
		unhideList = new ArrayList<Shopping>();
		String category = Integer.toString(position);
		
		ArrayList<Shopping> tempHelp = new ArrayList<Shopping>();
		tempHelp = getAllItemList();
		//Log.d(TAG, "in spinnerCatSelection, tempHelp size "+tempHelp.size());
		
		setHideComplete(hide);
		
		if(position==0) {
			unhideList.addAll(tempHelp);
			for (Shopping s : tempHelp) {
				if (!s.isComplete()) {
					notComplete.add(s);
				}
			}			
		} else {
			for (Shopping s : tempHelp) {
				if (s.getCat()==null) {
					s.setCat("1");
					app.updateItem(s);
				}
				if (s.getCat().equals(category)) {
					unhideList.add(s);
					if (!s.isComplete())
						notComplete.add(s);
				}
			}
		}
		
		
		if (hide) {
			shoppingList.clear();
			shoppingList.addAll(notComplete);
		} else {
			shoppingList.clear();
			shoppingList.addAll(unhideList);
		}
		notifyDataSetChanged();
	}
	
	
	//==OLD CODES======================================
	
		
	
	
	//--get completed list-----------------------------------
	public ArrayList<Long> getCompletedItems() {
		ArrayList<Long> completeList=new ArrayList<Long>();
		for (Shopping s : allItemList) {
			if (s.isComplete())
				completeList.add(s.getId());
		}
		return completeList;
	}
	
	//--return current display list--------------------------
	public ArrayList<Shopping> getShoppingList() {
		return allItemList;
	}
		
	//--update holiday----------------------------------------
	public void updateItem(Shopping newItem, Shopping oldItem) {
		shoppingList.remove(oldItem);
		allItemList.remove(oldItem);
		shoppingList.add(newItem);
		allItemList.add(newItem);
		filteredList=shoppingList;
		notifyDataSetChanged();
	}
		


	//--delete all completed items-------------------------
	public void  deleteCompletedItems() {
		ArrayList<Shopping> tempList = new ArrayList<Shopping>();
		for (Shopping s : allItemList) {
			if (!s.isComplete()) {
				tempList.add(s);					//build not complete list
			} else {
				shoppingList.remove(s);		//remove complete item,  this method to avoid filter refresh issues
			}
		}
		filteredList=shoppingList;
		allItemList=tempList;
	}
	
	//--delete selected shopping item-----------------------------		
	/*public void deleteItem(Shopping item) {
		Shopping bbItem=new Shopping();
		bbItem=item;
		shoppingList.remove(bbItem);
		allItemList.remove(bbItem);
		app.deleteItem(bbItem);
		notifyDataSetChanged();
	}*/

	//=========Adapter sorts and filters Methods=============================	
	
	//--reset shoppinglist to display all-----------------------------------
	public void resetList() {
		shoppingList.clear();
		shoppingList=allItemList;		
		//filteredList=allItemList;
		notifyDataSetChanged();
	}	
	

	/*public void toggleItem(Shopping bb) {
		Shopping bbItem=bb;
		bbItem.toggleComplete();
		app.updateItem(bbItem);
		notifyDataSetChanged();
	}*/
	
	//--hide and unhide complete list---------------------------------------
	//--filter not not complete list---------------------------------
	public void filterItemsNotComplete() {
		
		filteredList=new ArrayList<Shopping>();
		for (Shopping s: allItemList) {
			if (!s.isComplete()) {
					filteredList.add(s);
					notComplete.add(s);
			}
		}
		shoppingList=filteredList;
		notifyDataSetChanged();
	}
	
	public void removeFilterNotComplete() {
		shoppingList=allItemList;
		notifyDataSetChanged();
	}
	
	//--Spinner and Toggle button for filtering items by category and hide completes--
	//--Filter by tasks category 0-3 and 4 show all tasks--
	//--first para showAll refer to showAllTasks regardless of show/hide complete
	public void filterItems(boolean showAll, int selectList, boolean hideComplete) {
		
		allItemList=new ArrayList<Shopping>(app.getShoppings());
		filteredList=new ArrayList<Shopping>();
		notComplete=new ArrayList<Shopping>();
		for (Shopping s: allItemList) {
			if (!s.isComplete()) {
				filteredList.add(s);
				notComplete.add(s);
			}
		}
		
		ArrayList<Shopping> tempHelp=new ArrayList<Shopping>();
		if (hideComplete) {
			if (showAll) {
				shoppingList=notComplete;			//all not completed items
			} else {
				for (Shopping s : notComplete) {
					if (s.getHolId()==selectList)
						tempHelp.add(s);
				}
				shoppingList=tempHelp;				//by holiday only not completed items
			}
		} else {
			if(showAll) {
				shoppingList=allItemList;				//all items 
			} else {
				for (Shopping s: allItemList) {
					if (s.getHolId()==selectList)
						tempHelp.add(s);
				}
				shoppingList=tempHelp;				//by holiday all items
			}
		}
		notifyDataSetChanged(); 
	}
	
	
} //END MAIN CLASS

