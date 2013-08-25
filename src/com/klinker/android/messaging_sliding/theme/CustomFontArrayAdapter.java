package com.klinker.android.messaging_sliding.theme;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.klinker.android.messaging_donate.R;

import java.util.ArrayList;

public class CustomFontArrayAdapter extends ArrayAdapter<String> {
	  private final Activity context;
	  private final ArrayList<String> name, path;

	  static class ViewHolder {
	    public TextView text;
	    public TextView text2;
	  }

	  public CustomFontArrayAdapter(Activity context, ArrayList<String> name, ArrayList<String> path) {
	    super(context, R.layout.custom_font, name);
	    this.context = context;
	    this.name = name;
	    this.path = path;
	  }

	  @Override
	  public View getView(final int position, View convertView, ViewGroup parent) {
	    View rowView = convertView;
	    if (rowView == null) {
	      LayoutInflater inflater = context.getLayoutInflater();
	      rowView = inflater.inflate(R.layout.custom_font, null);
	      
	      ViewHolder viewHolder = new ViewHolder();
	      viewHolder.text = (TextView) rowView.findViewById(R.id.textView1);
	      viewHolder.text2 = (TextView) rowView.findViewById(R.id.textView2);
	      
	      viewHolder.text.setTextColor(context.getResources().getColor(R.color.white));
	      viewHolder.text2.setTextColor(context.getResources().getColor(R.color.white));
	      rowView.setTag(viewHolder);
	    }

	    ViewHolder holder = (ViewHolder) rowView.getTag();
	    holder.text.setText(name.get(position));
	    holder.text2.setText(path.get(position));
	    
	    if (position != 0)
	    {
	    	try
	    	{
	    		holder.text.setTypeface(Typeface.createFromFile(path.get(position)));
	    	} catch (Exception e)
	    	{
	    		
	    	}
	    }

	    return rowView;
	  }
	} 
