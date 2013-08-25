package com.klinker.android.messaging_sliding.blacklist;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.klinker.android.messaging_donate.R;

import java.util.ArrayList;

public class ContactSearchArrayAdapter extends ArrayAdapter<String> {
	  private final Activity context;
	  private final ArrayList<String> names, numbers, types;

	  static class ViewHolder {
	    public TextView text;
	    public TextView text2;
	    public TextView text3;
	  }

	  public ContactSearchArrayAdapter(Activity context, ArrayList<String> names, ArrayList<String> numbers, ArrayList<String> types) {
	    super(context, R.layout.contact_search, names);
	    this.context = context;
	    this.names = names;
	    this.numbers = numbers;
	    this.types = types;
	  }

	  @Override
	  public View getView(final int position, View convertView, ViewGroup parent) {
	    View rowView = convertView;
	    if (rowView == null) {
	      LayoutInflater inflater = context.getLayoutInflater();
	      rowView = inflater.inflate(R.layout.contact_search, null);
	      
	      rowView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
	      
	      ViewHolder viewHolder = new ViewHolder();
	      viewHolder.text = (TextView) rowView.findViewById(R.id.conversationCount);
	      viewHolder.text2 = (TextView) rowView.findViewById(R.id.receivedMessage);
	      viewHolder.text3 = (TextView) rowView.findViewById(R.id.receivedDate);
	      
	      viewHolder.text.setTextColor(context.getResources().getColor(R.color.white));
	      viewHolder.text2.setTextColor(context.getResources().getColor(R.color.white));
	      viewHolder.text3.setTextColor(context.getResources().getColor(R.color.white));
	      
	      rowView.setTag(viewHolder);
	    }

	    ViewHolder holder = (ViewHolder) rowView.getTag();
	    holder.text.setText(names.get(position));
	    holder.text2.setText(numbers.get(position));
	    holder.text3.setText(types.get(position));

	    return rowView;
	  }
	} 
