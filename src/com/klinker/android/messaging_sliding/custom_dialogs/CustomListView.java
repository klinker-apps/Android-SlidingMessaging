package com.klinker.android.messaging_sliding.custom_dialogs;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class CustomListView extends ListView {
    private OnSizeChangedListener mOnSizeChangedListener;

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
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mOnSizeChangedListener != null) {
            mOnSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
        }
    }

    /**
     * Set the listener which will be triggered when the size of
     * the view is changed.
     */
    public void setOnSizeChangedListener(OnSizeChangedListener l) {
        mOnSizeChangedListener = l;
    }

    public interface OnSizeChangedListener {
        void onSizeChanged(int width, int height, int oldWidth, int oldHeight);
    }
	
	
}
