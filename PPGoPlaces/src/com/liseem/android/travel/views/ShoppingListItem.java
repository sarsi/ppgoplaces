package com.liseem.android.travel.views;

import android.widget.LinearLayout;
import com.liseem.android.travel.R;
import com.liseem.android.travel.items.Shopping;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;


public class ShoppingListItem extends LinearLayout {

	private Shopping item;
	private CheckedTextView checkbox;
	private TextView addressText;
	private TextView priceText;

	public ShoppingListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		//ListView shoppingList = (ListView)findViewById(R.id.text1);
		checkbox=(CheckedTextView)findViewById(android.R.id.text1);
		addressText=(TextView)findViewById(R.id.address_text);
		priceText=(TextView)findViewById(R.id.price_text);
		
	}

	//setItem is key, to inflate getName into setText
	public void setItem(Shopping item) {
		this.item = item;
		checkbox.setText(item.getName());
		checkbox.setChecked(item.isComplete());
		if(item.getPrice()!=null) {
			priceText.setVisibility(View.VISIBLE);
			priceText.setText(item.getPrice());
		} else {
			priceText.setVisibility(View.GONE);
		}
		
		if(item.hasAddress()) {
			addressText.setText(item.getAddress());
			addressText.setVisibility(View.VISIBLE);
		} else {
			addressText.setVisibility(View.GONE);
		}

	}
	
	public Shopping getItem() {
		return item;
	}
	
}
	
