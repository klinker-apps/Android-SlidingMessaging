package com.klinker.android.messaging_donate.floating_notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.ContactUtil;
import robj.floating.notifications.Extension;

import java.util.HashMap;
import java.util.Map;

public class FNReceiver extends BroadcastReceiver {

    public static Map<Long, String[]> messages = new HashMap<Long, String[]>();

    @Override
    public void onReceive(final Context context, Intent intent) {
        String body = intent.getStringExtra("body");
        String address = intent.getStringExtra("address");

        if (address == null) {
            address = "";
            body = "";
        }
        
        address = address.replace("+", "").replace(" ", "").replace("(", "").replace(")", "");
        long id;

        try {
            id = Long.parseLong(address);
        } catch (Exception e) {
            id = 0;
        }
        
        if (messages.containsKey(id)) {
            String previous = messages.get(id)[1];
            previous += "\n\n" + body;
            messages.remove(id);
            messages.put(id, new String[] {address, previous});
        } else {
            messages.put(id, new String[] {address, body});
        }

        Bitmap image = ContactUtil.getFacebookPhoto(address, context);
        image = Bitmap.createScaledBitmap(image, 200, 200, false);

        Bitmap actionOne = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_menu_msg_compose_holo_dark);
        Bitmap actionTwo = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_menu_call);
        Bitmap actionThree = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_menu_done_holo_dark);

        Extension.addOrUpdate(image, body, id, 0, actionOne, actionTwo, actionThree, false, true, false, context);
    }
}