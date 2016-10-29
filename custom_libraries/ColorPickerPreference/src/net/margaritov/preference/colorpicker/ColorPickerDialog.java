/*
 * Copyright (C) 2010 Daniel Nilsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.margaritov.preference.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ColorPickerDialog 
	extends 
		Dialog 
	implements
		ColorPickerView.OnColorChangedListener,
		View.OnClickListener {

	private ColorPickerView mColorPicker;

	private ColorPickerPanelView mOldColor;
	private ColorPickerPanelView mNewColor;
	
	private Button mBtn1, mBtn2, mBtn3, mBtn4, mBtn5;
	
	private EditText hexValue;

	private OnColorChangedListener mListener;
	
	private boolean changeText;

	public interface OnColorChangedListener {
		public void onColorChanged(int color);
	}
	
	public ColorPickerDialog(Context context, int initialColor) {
		super(context);

		init(initialColor);
	}

	private void init(int color) {
		// To fight color banding.
		getWindow().setFormat(PixelFormat.RGBA_8888);

		setUp(color);

	}

	private void setUp(int color) {
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View layout = inflater.inflate(R.layout.dialog_color_picker, null);

		setContentView(layout);

		setTitle(R.string.dialog_color_picker);
		
		mColorPicker = (ColorPickerView) layout.findViewById(R.id.color_picker_view);
		mOldColor = (ColorPickerPanelView) layout.findViewById(R.id.old_color_panel);
		mNewColor = (ColorPickerPanelView) layout.findViewById(R.id.new_color_panel);
		
		mBtn1 = (Button) layout.findViewById(R.id.button1);
		mBtn2 = (Button) layout.findViewById(R.id.button2);
		mBtn3 = (Button) layout.findViewById(R.id.button3);
		mBtn4 = (Button) layout.findViewById(R.id.button4);
		mBtn5 = (Button) layout.findViewById(R.id.button5);
		
		hexValue = (EditText) layout.findViewById(R.id.editText1); 
		
		((LinearLayout) mOldColor.getParent()).setPadding(
			Math.round(mColorPicker.getDrawingOffset()), 
			0, 
			Math.round(mColorPicker.getDrawingOffset()), 
			0
		);	
		
		mOldColor.setOnClickListener(this);
		mNewColor.setOnClickListener(this);
		mColorPicker.setOnColorChangedListener(this);
		mOldColor.setColor(color);
		mColorPicker.setColor(color, true);
		
		final Context context = getContext();
		
		mBtn1.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mColorPicker.setColor(context.getResources().getColor(R.color.holo_blue), true);
				
			}
			
		});
		
		mBtn2.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mColorPicker.setColor(context.getResources().getColor(R.color.holo_green), true);
				
			}
			
		});
		
		mBtn3.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mColorPicker.setColor(context.getResources().getColor(R.color.holo_orange), true);
				
			}
			
		});
		
		mBtn4.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mColorPicker.setColor(context.getResources().getColor(R.color.holo_purple), true);
				
			}
			
		});
		
		mBtn5.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mColorPicker.setColor(context.getResources().getColor(R.color.holo_red), true);
				
			}
			
		});
		
		hexValue.setText(ColorPickerPreference.convertToARGB(mOldColor.getColor()).substring(1));
		
		hexValue.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				
				
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				
				if ((arg0.length() == 6 || arg0.length() == 8) && changeText)
				{
					try
					{
						mColorPicker.setColor(ColorPickerPreference.convertToColorInt(arg0.toString()), false);
						mNewColor.setColor(ColorPickerPreference.convertToColorInt(arg0.toString()));
					} catch (Exception e)
					{
						
					}
				}
				
				changeText = true;
				
			}
			
		});

	}

	@Override
	public void onColorChanged(int color) {

		mNewColor.setColor(color);
		changeText = false;
		hexValue.setText(ColorPickerPreference.convertToARGB2(color));
		
		/*
		if (mListener != null) {
			mListener.onColorChanged(color);
		}
		*/

	}

	public void setAlphaSliderVisible(boolean visible) {
		mColorPicker.setAlphaSliderVisible(visible);
	}
	
	/**
	 * Set a OnColorChangedListener to get notified when the color
	 * selected by the user has changed.
	 * @param listener
	 */
	public void setOnColorChangedListener(OnColorChangedListener listener){
		mListener = listener;
	}

	public int getColor() {
		return mColorPicker.getColor();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.new_color_panel) {
			if (mListener != null) {
				mListener.onColorChanged(mNewColor.getColor());
			}
		}
		dismiss();
	}
	
	@Override
	public Bundle onSaveInstanceState() {
		Bundle state = super.onSaveInstanceState();
		state.putInt("old_color", mOldColor.getColor());
		state.putInt("new_color", mNewColor.getColor());
		return state;
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mOldColor.setColor(savedInstanceState.getInt("old_color"));
		mColorPicker.setColor(savedInstanceState.getInt("new_color"), true);
	}
}
