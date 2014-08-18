package com.liseem.android.travel.adapter;

import java.util.List;

import com.liseem.android.travel.R;
import com.liseem.android.travel.items.FileOption;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FileListAdapter extends ArrayAdapter<FileOption> {

	private Context context;
	private int id;
	private List<FileOption> items;
	private LayoutInflater mInflater;
	
	public FileListAdapter(Context context, int textViewResourceId, List<FileOption> objects) {
		super(context, textViewResourceId, objects);
		
		this.context=context;
		this.id=textViewResourceId;
		this.items=objects;
		
		//mInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public FileOption getItem (int position) {
		return items.get(position);
	}

	/*@Override
	public long getItemId (int position) {
		return position;
	}*/

	@Override
	public View getView (int position, View convertView, ViewGroup parent) {
		//ViewHolder holder = null;		
		View view = convertView;
		
       if (view == null) {
			 mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view=mInflater.inflate(id, null);
       }
       
		final FileOption option = items.get(position);
        
        if (option != null) {
                TextView t1 = (TextView) view.findViewById(R.id.TextView01);
                TextView t2 = (TextView) view.findViewById(R.id.TextView02);
                
                if(t1!=null)
                    t1.setText(option.getName());
                if(t2!=null)
                    t2.setText(option.getData());       
        }
        return view;
	}


}	//END MAIN CLASS
