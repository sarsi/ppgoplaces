package com.liseem.android.travel.views;

import android.view.MotionEvent;

public class SimpleMotionEvent extends WrapMotionEvent {
	
	 protected SimpleMotionEvent(MotionEvent event) {
         super(event);
	 }
	
	 @Override
	public float getX(int pointerIndex) {
	         return event.getX(pointerIndex);
	 }
	
	 @Override
	public float getY(int pointerIndex) {
	         return event.getY(pointerIndex);
	 }
	
	 @Override
	public int getPointerCount() {
	         return event.getPointerCount();
	 }
	
	 @Override
	public int getPointerId(int pointerIndex) {
	         return event.getPointerId(pointerIndex);
	 }

}
