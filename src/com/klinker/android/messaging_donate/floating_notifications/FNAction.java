package com.klinker.android.messaging_donate.floating_notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.MainActivity;
import robj.floating.notifications.Extension;

public class FNAction extends BroadcastReceiver {

	@Override 
        public void onReceive(final Context context, Intent intent) {
		final long id = intent.getLongExtra(Extension.ID, -1);
		int action = intent.getIntExtra(Extension.ACTION, -1);

		switch (action) {

			case 0:
				// start main activity popup
				Intent popup = new Intent(context, com.klinker.android.messaging_sliding.MainActivityPopup.class);
				context.startActivity(popup);
				break;

			case 1:
				// create new reply overlay
				String editTextHint = context.getResources().getString(R.string.reply_to) + " " + MainActivity.findContactName(MainActivity.findContactNumber(id + "", context), context);
				String previousText = intent.getStringExtra(Extension.MSG);
				Bitmap image = MainActivity.getFacebookPhoto(MainActivity.findContactNumber(id + "", context), context);
                Extension.onClickListener imageOnClick = new Extension.onClickListener() {
					@Override
					public void onClick() {
						Intent intent = new Intent(context, com.klinker.android.messaging_sliding.MainActivity.class);
						intent.putExtra("com.klinker.android.OPEN_THREAD", MainActivity.findContactNumber(id + "", context));
						context.startActivity(intent);
					}
				};

				Extension.onClickListener sendOnClick = new Extension.onClickListener() {
					@Override
					public void onClick(String str) {
						// TODO send message here
					}
				};

                Extension.onClickListener extraOnClick = new Extension.onClickListener() {
					@Override
					public void onClick(final String str) {
						// TODO figure out how to get the string from editText in extension
						// Open emoji dialog here
					}
				};

				Bitmap extraButton = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_emoji_dark);

				Extension.replyOverlay(editTextHint, previousText, image, imageOnClick, sendOnClick, extraOnClick, true, extraButton, context, false);
				
				break;

			case 2:
				// start call intent
				String address = MainActivity.findContactNumber(id + "", context);
				Intent callIntent = new Intent(Intent.ACTION_CALL);
                                callIntent.setData(Uri.parse("tel:" + address));
                                callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(callIntent);
				break;

			case 3:
				// start mark read service
				context.startService(new Intent(context, com.klinker.android.messaging_sliding.quick_reply.QmMarkRead2.class));
				break;

		}
	}
}