package com.klinker.android.messaging_sliding;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
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

            rowView.setBackgroundColor(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)));

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) rowView.findViewById(R.id.conversationCount);
            viewHolder.text2 = (TextView) rowView.findViewById(R.id.receivedMessage);
            viewHolder.text3 = (TextView) rowView.findViewById(R.id.receivedDate);

            viewHolder.text.setTextColor(sharedPrefs.getInt("ct_sentTextColor", context.getResources().getColor(R.color.black)));
            viewHolder.text2.setTextColor(sharedPrefs.getInt("ct_sentTextColor", context.getResources().getColor(R.color.black)));
            viewHolder.text3.setTextColor(sharedPrefs.getInt("ct_sentTextColor", context.getResources().getColor(R.color.black)));

            try {
                if (sharedPrefs.getBoolean("custom_font", false)) {
                    viewHolder.text.setTypeface(Typeface.createFromFile(sharedPrefs.getString("custom_font_path", "")));
                    viewHolder.text2.setTypeface(Typeface.createFromFile(sharedPrefs.getString("custom_font_path", "")));
                    viewHolder.text3.setTypeface(Typeface.createFromFile(sharedPrefs.getString("custom_font_path", "")));
                }
            } catch (Exception e) {
                // someone with custom fonts seems to be having an issue in certain places... i don't know why
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
