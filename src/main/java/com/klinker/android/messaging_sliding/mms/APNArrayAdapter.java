package com.klinker.android.messaging_sliding.mms;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.klinker.android.messaging_donate.R;

import java.util.ArrayList;

public class APNArrayAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String> name, values;

    static class ViewHolder {
        public TextView text;
        public TextView text2;
    }

    public APNArrayAdapter(Activity context, ArrayList<String> name, ArrayList<String> path) {
        super(context, R.layout.custom_font, name);
        this.context = context;
        this.name = name;
        this.values = path;
    }

    @Override
    public int getCount() {
        return name.size() + 2;
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

            viewHolder.text.setTextColor(context.getResources().getColor(R.color.black));
            viewHolder.text2.setTextColor(context.getResources().getColor(R.color.black));
            rowView.setTag(viewHolder);
        }

        final ViewHolder holder = (ViewHolder) rowView.getTag();

        if (position == 0) {
            holder.text.setText(context.getResources().getString(R.string.confirmed_apns));
            holder.text2.setVisibility(View.GONE);
        } else if (position == PresetAPNs.CONFIRMED_APNS + 1) {
            holder.text.setText(context.getResources().getString(R.string.experimental_apns));
            holder.text2.setText(context.getResources().getString(R.string.experimental_apns_summary));
            holder.text2.setVisibility(View.VISIBLE);
        } else {
            int pos = position;

            if (position <= PresetAPNs.CONFIRMED_APNS) {
                pos -= 1;
            } else {
                pos -= 2;
            }

            holder.text.setText(name.get(pos).replace("_", " "));
            holder.text2.setText(values.get(pos));
            holder.text2.setVisibility(View.VISIBLE);

            holder.text.setTextSize(16);
            holder.text2.setTextSize(10);

            rowView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    int pos = position;

                    if (position <= PresetAPNs.CONFIRMED_APNS) {
                        pos -= 1;
                    } else {
                        pos -= 2;
                    }

                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                    String[] apns = values.get(pos).split(", ");

                    SharedPreferences.Editor editor = sharedPrefs.edit();

                    editor.putString("mmsc_url", apns[0]);
                    editor.putString("mms_proxy", apns[1]);
                    editor.putString("mms_port", apns[2]);

                    if (holder.text.getText().equals("Verizon Wireless") || holder.text.getText().equals("Verizon Wireless #2 (Try this if first doesn't work for you)")) {
                        try {
                            String phoneNumber = getMyPhoneNumber().replace("+", "").replace("-", "").replace(")", "").replace("(", "").replace(" ", "");

                            if (phoneNumber.startsWith("+1")) {
                                phoneNumber = phoneNumber.substring(2);
                            } else if (phoneNumber.startsWith("1") && phoneNumber.length() == 11) {
                                phoneNumber = phoneNumber.substring(1);
                            }

                            editor.putString("mmsc_url", apns[0].replace("**********", phoneNumber));
                            editor.putString("mms_proxy", apns[1].replace("null", ""));
                        } catch (Exception e) {
                            // tablet? no phone number then.
                        }
                    }

                    editor.commit();

                    Toast.makeText(context, "APNs Saved", Toast.LENGTH_SHORT).show();
                    context.finish();

                }

            });
        }

        return rowView;
    }

    private String getMyPhoneNumber() {
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getLine1Number();
    }
}