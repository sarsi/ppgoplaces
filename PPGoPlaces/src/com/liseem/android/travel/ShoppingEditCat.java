/*
 * program:				EditShoppingCat.java
 * summary:			Let user rename shopping list category
 * 
 * date created:		September 14, 2012
 * introduction:		plan next release post v1.0 build 153
 * 
 * Associated files:
 * Layout view: editcheckcat.xml		(shared with EditCheckCat.java)
 * Parent file: ShoppingList.java
 * 
 */

package com.liseem.android.travel;

import static com.liseem.android.travel.TravelLiteActivity.*;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class ShoppingEditCat extends Fragment {
	
	//--application----------------------------------------------
	private PPGoPlacesApplication 		app;
	private SharedPreferences 				category;
	private SharedPreferences.Editor 		catEdit;
	
	//--view layout---------------------------------------------
	private TextView 								category1;
	private TextView 								category2;
	private TextView 								category3;
	private TextView 								category4;
	private TextView 								category5;
	private TextView 								category6;
	private TextView 								category7;
	private TextView 								category8;
	private EditText 								catEdit1;
	private EditText 								catEdit2;
	private EditText 								catEdit3;
	private EditText 								catEdit4;
	private EditText 								catEdit5;
	private EditText 								catEdit6;
	private EditText 								catEdit7;
	private EditText 								catEdit8;
	private Button 									updateButton;

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	
		//--inflate layout for fragment1------------
		//return inflater.inflate(R.layout.view_list, container, false);
		if(container==null) 
			return null;
	
		//--inflate layout for fragment 1------------------------------------------------
		View v=inflater.inflate(R.layout.editcheckcat, container, false);
		setRetainInstance(true);
		
		return v;			
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.editcheckcat);
		
		setupView();
	}
	
	protected void setupView() {
		
		//--application setup------------------------------------------------
		app					=(PPGoPlacesApplication)getActivity().getApplication();
		category			=getActivity().getSharedPreferences (CHECKLIST, MODE_PRIVATE);
		catEdit  			=category.edit();
		
		//--setup layout views----------------------------------------------
		category1		=(TextView)getActivity().findViewById(R.id.catText1);
		category2		=(TextView)getActivity().findViewById(R.id.catText2);
		category3		=(TextView)getActivity().findViewById(R.id.catText3);		
		category4		=(TextView)getActivity().findViewById(R.id.catText4);
		category5		=(TextView)getActivity().findViewById(R.id.catText5);
		category6		=(TextView)getActivity().findViewById(R.id.catText6);
		category7		=(TextView)getActivity().findViewById(R.id.catText7);
		category8		=(TextView)getActivity().findViewById(R.id.catText8);

		catEdit1			=(EditText)getActivity().findViewById(R.id.category1);
		catEdit2			=(EditText)getActivity().findViewById(R.id.category2);
		catEdit3			=(EditText)getActivity().findViewById(R.id.category3);
		catEdit4			=(EditText)getActivity().findViewById(R.id.category4);
		catEdit5			=(EditText)getActivity().findViewById(R.id.category5);
		catEdit6			=(EditText)getActivity().findViewById(R.id.category6);
		catEdit7			=(EditText)getActivity().findViewById(R.id.category7);
		catEdit8			=(EditText)getActivity().findViewById(R.id.category8);

		category6.setVisibility(View.VISIBLE);
		category7.setVisibility(View.VISIBLE);
		category8.setVisibility(View.VISIBLE);
		catEdit6.setVisibility(View.VISIBLE);
		catEdit7.setVisibility(View.VISIBLE);
		catEdit8.setVisibility(View.VISIBLE);
		
		updateButton	=(Button)getActivity().findViewById(R.id.addButton);
		
		//--populate textview---------------------------------------------
		category1.setText("Shopping List - Category 1");
		category2.setText("Shopping List - Category 2");
		category3.setText("Shopping List - Category 3");
		category4.setText("Shopping List - Category 4");
		category5.setText("Shopping List - Category 5");
		category6.setText("Shopping List - Category 6");
		category7.setText("Shopping List - Category 7");
		category8.setText("Shopping List - Category 8");
		
		catEdit1.setText(category.getString("ShopCat1", SHOP_CAT_1));
		catEdit2.setText(category.getString("ShopCat2", SHOP_CAT_2));
		catEdit3.setText(category.getString("ShopCat3", SHOP_CAT_3));
		catEdit4.setText(category.getString("ShopCat4", SHOP_CAT_4));
		catEdit5.setText(category.getString("ShopCat5", SHOP_CAT_5));
		catEdit6.setText(category.getString("ShopCat6", SHOP_CAT_6));
		catEdit7.setText(category.getString("ShopCat7", SHOP_CAT_7));
		catEdit8.setText(category.getString("ShopCat8", SHOP_CAT_8));
		
		updateButton.setOnClickListener(new SaveChangesOnClick());		
		
	}	//END setupView
	
	//--update category name change-------------------------------------
	private class SaveChangesOnClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (!catEdit1.getText().toString().matches("")) 
				catEdit.putString("ShopCat1", catEdit1.getText().toString());
			if (!catEdit2.getText().toString().matches("")) 
				catEdit.putString("ShopCat2", catEdit2.getText().toString());
			if (!catEdit3.getText().toString().matches("")) 
				catEdit.putString("ShopCat3", catEdit3.getText().toString());
			if (!catEdit4.getText().toString().matches("")) 
				catEdit.putString("ShopCat4", catEdit4.getText().toString());
			if (!catEdit5.getText().toString().matches("")) 
				catEdit.putString("ShopCat5", catEdit5.getText().toString());
			if (!catEdit6.getText().toString().matches("")) 
				catEdit.putString("ShopCat6", catEdit6.getText().toString());
			if (!catEdit7.getText().toString().matches("")) 
				catEdit.putString("ShopCat7", catEdit7.getText().toString());
			if (!catEdit8.getText().toString().matches("")) 
				catEdit.putString("ShopCat8", catEdit8.getText().toString());
			catEdit.commit();
			getActivity().finish();
		}		
	}

}	//--END MAIN CLASS
