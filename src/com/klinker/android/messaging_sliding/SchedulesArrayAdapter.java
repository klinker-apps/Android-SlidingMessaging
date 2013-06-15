package com.klinker.android.messaging_sliding;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.klinker.android.messaging_donate.R;

public class SchedulesArrayAdapter  extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String[]> text;
    public SharedPreferences sharedPrefs;

    static class ViewHolder {
        public TextView name;
        public TextView date;
        public TextView message;
    }

    public SchedulesArrayAdapter(Activity context, ArrayList<String[]> text) {
        super(context, R.layout.custom_scheduled);
        this.context = context;
        this.text = text;
        this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public int getCount()
    {
        return text.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        Date sendDate = new Date(Long.parseLong(text.get(position)[1]));

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.custom_scheduled, null);

            ViewHolder viewHolder = new ViewHolder();

            viewHolder.name = (TextView) rowView.findViewById(R.id.sms);
            viewHolder.name.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,2)));

            viewHolder.date = (TextView) rowView.findViewById(R.id.date);
            viewHolder.date.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0, 2)));
            viewHolder.date.setTextColor(context.getResources().getColor(R.color.messageCounterLight));

            viewHolder.message = (TextView) rowView.findViewById(R.id.message);
            viewHolder.message.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0, 2)));
            viewHolder.message.setTextColor(context.getResources().getColor(R.color.white));

            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();

        String contactName = MainActivity.loadGroupContacts(text.get(position)[0].replaceAll(";", ""), context);
        String dateString;

        if (sharedPrefs.getBoolean("hour_format", false))
        {
            dateString = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN).format(sendDate);
        } else
        {
            dateString = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).format(sendDate);
        }

        if (sharedPrefs.getBoolean("hour_format", false))
        {
            dateString += " " + DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(sendDate);
        } else
        {
            dateString += " " + DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(sendDate);
        }

        holder.name.setText(contactName);
        holder.date.setText(dateString);
        holder.message.setText(text.get(position)[3]);

        return rowView;
    }
}
