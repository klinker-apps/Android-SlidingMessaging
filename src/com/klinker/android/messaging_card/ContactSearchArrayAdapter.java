package com.klinker.android.messaging_card;

import com.klinker.android.messaging_donate.R;

import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ContactSearchArrayAdapter extends ArrayAdapter<String> {
	  private final Activity context;
	  private final ArrayList<String> names, numbers, types;
	  private SharedPreferences sharedPrefs;

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
	    this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	  }

	  @Override
	  public View getView(final int position, View convertView, ViewGroup parent) {
	    View rowView = convertView;
	    if (rowView == null) {
	      LayoutInflater inflater = context.getLayoutInflater();
	      rowView = inflater.inflate(R.layout.contact_search, null);
	      
	      ViewHolder viewHolder = new ViewHolder();
	      viewHolder.text = (TextView) rowView.findViewById(R.id.conversationCount);
	      viewHolder.text2 = (TextView) rowView.findViewById(R.id.receivedMessage);
	      viewHolder.text3 = (TextView) rowView.findViewById(R.id.receivedDate);
	      
	      if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
	      {
	    	  rowView.setBackgroundResource(R.drawable.card_background);
	    	  viewHolder.text.setTextColor(context.getResources().getColor(R.color.card_conversation_summary));
		      viewHolder.text2.setTextColor(context.getResources().getColor(R.color.card_conversation_summary));
		      viewHolder.text3.setTextColor(context.getResources().getColor(R.color.card_conversation_summary));
	      } else if (sharedPrefs.getString("card_theme", "Light").equals("Dark"))
	      {
	    	  rowView.setBackgroundResource(R.drawable.card_background_dark);
	    	  viewHolder.text.setTextColor(context.getResources().getColor(R.color.card_dark_conversation_summary));
		      viewHolder.text2.setTextColor(context.getResources().getColor(R.color.card_dark_conversation_summary));
		      viewHolder.text3.setTextColor(context.getResources().getColor(R.color.card_dark_conversation_summary));
	      } else if (sharedPrefs.getString("card_theme", "Light").equals("Pitch Black"))
	      {
	    	  rowView.setBackgroundResource(R.drawable.card_background_black);
	    	  viewHolder.text.setTextColor(context.getResources().getColor(R.color.card_black_conversation_summary));
		      viewHolder.text2.setTextColor(context.getResources().getColor(R.color.card_black_conversation_summary));
		      viewHolder.text3.setTextColor(context.getResources().getColor(R.color.card_black_conversation_summary));
	      }
	      
	      if (sharedPrefs.getBoolean("custom_font", false))
	      {
	    	  viewHolder.text.setTypeface(Typeface.createFromFile(sharedPrefs.getString("custom_font_path", "")));
	    	  viewHolder.text2.setTypeface(Typeface.createFromFile(sharedPrefs.getString("custom_font_path", "")));
	    	  viewHolder.text3.setTypeface(Typeface.createFromFile(sharedPrefs.getString("custom_font_path", "")));
	      }
	      
	      rowView.setTag(viewHolder);
	    }

	    ViewHolder holder = (ViewHolder) rowView.getTag();
	    holder.text.setText(names.get(position));
	    holder.text2.setText(numbers.get(position));
	    holder.text3.setText(types.get(position));

	    return rowView;
	  }
	} 

