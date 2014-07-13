package com.klinker.android.messaging_sliding.scheduled;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.settings.AppSettings;
import com.klinker.android.messaging_donate.utils.ContactUtil;
import com.klinker.android.messaging_sliding.scheduled.scheduled.ScheduledMessage;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SchedulesArrayAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<ScheduledMessage> text;
    private AppSettings settings;

    static class ViewHolder {
        public TextView name;
        public TextView date;
        public TextView message;
        public TextView repetition;
    }

    public SchedulesArrayAdapter(Activity context, ArrayList<ScheduledMessage> text) {
        super(context, R.layout.custom_scheduled);
        this.context = context;
        this.text = text;
        this.settings = AppSettings.init(context);
    }

    @Override
    public int getCount() {
        return text.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        Date sendDate;

        try {
            sendDate = new Date(text.get(position).date);
        } catch (Exception e) {
            sendDate = new Date(0);
        }

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.custom_scheduled, null);

            ViewHolder viewHolder = new ViewHolder();

            viewHolder.name = (TextView) rowView.findViewById(R.id.sms);
            viewHolder.date = (TextView) rowView.findViewById(R.id.date);
            viewHolder.repetition = (TextView) rowView.findViewById(R.id.repetition);
            viewHolder.message = (TextView) rowView.findViewById(R.id.message);

            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();

        String contactName = ContactUtil.loadGroupContacts(text.get(position).address.replaceAll(";", ""), context);
        String dateString;

        if (settings.hourFormat) {
            dateString = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN).format(sendDate);
        } else {
            dateString = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).format(sendDate);
        }

        if (settings.hourFormat) {
            dateString += " " + DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(sendDate);
        } else {
            dateString += " " + DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(sendDate);
        }

        holder.name.setText(contactName);
        holder.date.setText(dateString);
        holder.message.setText(text.get(position).body);

        if (text.get(position).attachment != null && !text.get(position).attachment.equals("")) {
            holder.message.setText(holder.message.getText() + " (with attachment)");
        }

        if (text.get(position).repetition == ScheduledMessage.REPEAT_NEVER)
            holder.repetition.setText(R.string.never);
        else if (text.get(position).repetition == ScheduledMessage.REPEAT_DAILY)
            holder.repetition.setText(R.string.daily);
        else if (text.get(position).repetition == ScheduledMessage.REPEAT_WEEKLY)
            holder.repetition.setText(R.string.weekly);
        else if (text.get(position).repetition == ScheduledMessage.REPEAT_MONTHLY)
            holder.repetition.setText(R.string.monthly);
        else if (text.get(position).repetition == ScheduledMessage.REPEAT_YEARLY)
            holder.repetition.setText(R.string.yearly);

        return rowView;
    }
}
