package com.klinker.android.messaging_card;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class GestureListener extends SimpleOnGestureListener {
	
	public int SWIPE_MIN_DISTANCE;
	public int SWIPE_THRESHOLD_VELOCITY;
	public int SWIPE_START_POINT;
	
	public GestureListener(int dis, int vel, int start)
	{
		super();
		
		SWIPE_MIN_DISTANCE = dis;
		SWIPE_THRESHOLD_VELOCITY = vel;
		SWIPE_START_POINT = start;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	{
		return false;
	}
	
	@Override
	public boolean onDown(MotionEvent e)
	{
		return true;
	}

}
