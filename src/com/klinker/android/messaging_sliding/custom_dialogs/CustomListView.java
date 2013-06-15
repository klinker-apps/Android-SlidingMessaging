package com.klinker.android.messaging_sliding.custom_dialogs;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class CustomListView extends ListView {
	public CustomListView (Context context) {
	    super(context);
	}

	public CustomListView (Context context, AttributeSet attrs) {
	    super(context, attrs);
	}

	public CustomListView (Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	}

	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
	    super.onSizeChanged(xNew, yNew, xOld, yOld);

	    setSelection(getCount());

	}
	
	
}
