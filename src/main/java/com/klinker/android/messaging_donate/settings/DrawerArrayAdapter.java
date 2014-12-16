package com.klinker.android.messaging_donate.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.klinker.android.messaging_donate.R;

import java.util.ArrayList;

public class DrawerArrayAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String> text;
    public SharedPreferences sharedPrefs;
    public static int current = 0;

    static class ViewHolder {
        public TextView name;
        public ImageView icon;
    }

    public DrawerArrayAdapter(Activity context, ArrayList<String> text) {
        super(context, R.layout.custom_scheduled);
        this.context = context;
        this.text = text;
        this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public int getCount() {
        return text.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        String settingName = text.get(position);

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.drawer_list_item, null);

            ViewHolder viewHolder = new ViewHolder();

            viewHolder.name = (TextView) rowView.findViewById(R.id.title);
            viewHolder.icon = (ImageView) rowView.findViewById(R.id.icon);

            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.name.setText(settingName);

        if (text.get(position).equals(context.getResources().getString(R.string.theme_settings)))
            holder.icon.setImageResource(R.drawable.drawer_theme);
        else if (text.get(position).equals(context.getResources().getString(R.string.notification_settings)))
            holder.icon.setImageResource(R.drawable.drawer_notifications);
        else if (text.get(position).equals(context.getResources().getString(R.string.popup_settings)))
            holder.icon.setImageResource(R.drawable.drawer_popup);
        else if (text.get(position).equals(context.getResources().getString(R.string.slideover_settings)))
            holder.icon.setImageResource(R.drawable.drawer_slideover);
        else if (text.get(position).equals(context.getResources().getString(R.string.text_settings)))
            holder.icon.setImageResource(R.drawable.drawer_message);
        else if (text.get(position).equals(context.getResources().getString(R.string.conversation_settings)))
            holder.icon.setImageResource(R.drawable.drawer_conversation);
        else if (text.get(position).equals(context.getResources().getString(R.string.mms_settings)))
            holder.icon.setImageResource(R.drawable.drawer_mms);
        else if (text.get(position).equals(context.getResources().getString(R.string.google_voice_settings)))
            holder.icon.setImageResource(R.drawable.drawer_voice);
        else if (text.get(position).equals(context.getResources().getString(R.string.security_settings)))
            holder.icon.setImageResource(R.drawable.drawer_security);
        else if (text.get(position).equals(context.getResources().getString(R.string.advanced_settings)))
            holder.icon.setImageResource(R.drawable.drawer_advanced);
        else if (text.get(position).equals(context.getResources().getString(R.string.quick_templates)))
            holder.icon.setImageResource(R.drawable.drawer_templates);
        else if (text.get(position).equals(context.getResources().getString(R.string.scheduled_sms)))
            holder.icon.setImageResource(R.drawable.drawer_scheduled);
        else if (text.get(position).equals(context.getResources().getString(R.string.mass_sms)))
            holder.icon.setImageResource(R.drawable.card_group_dark);
        else if (text.get(position).equals(context.getResources().getString(R.string.get_help)))
            holder.icon.setImageResource(R.drawable.drawer_help);
        else if (text.get(position).equals(context.getResources().getString(R.string.other_apps)))
            holder.icon.setImageResource(R.drawable.drawer_apps);
        else
            holder.icon.setImageResource(R.drawable.drawer_rating);

        if ((current == position && SettingsPagerActivity.settingsLinksActive && !SettingsPagerActivity.inOtherLinks) ||
                (!SettingsPagerActivity.settingsLinksActive && current == position && SettingsPagerActivity.inOtherLinks)) {
            holder.icon.setColorFilter(context.getResources().getColor(R.color.holo_blue));
            holder.name.setTextColor(context.getResources().getColor(R.color.holo_blue));
        } else {
            holder.icon.setColorFilter(context.getResources().getColor(R.color.light_grey));
            holder.name.setTextColor(context.getResources().getColor(R.color.light_grey));
        }

        return rowView;
    }
}