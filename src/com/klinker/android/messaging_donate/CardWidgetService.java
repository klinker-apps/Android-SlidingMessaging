package com.klinker.android.messaging_donate;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class CardWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new CardViewsFactory(this.getApplicationContext(), intent);
    }
}

class CardViewsFactory implements RemoteViewsService.RemoteViewsFactory {

	private int mCount;
    private List<WidgetItem> mWidgetItems = new ArrayList<WidgetItem>();
	private Context mContext;
    public CardViewsFactory(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public void onCreate() {
    	mWidgetItems = new ArrayList<WidgetItem>();
    	String[] projection = new String[]{"_id", "date", "message_count", "recipient_ids", "snippet", "read"};
		Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");
		Cursor query = mContext.getContentResolver().query(uri, projection, null, null, "date desc");
		
		if (query.moveToFirst())
		{
			do
			{
				mWidgetItems.add(new WidgetItem(query.getString(query.getColumnIndex("recipient_ids")),
						                        query.getString(query.getColumnIndex("message_count")),
						                        query.getString(query.getColumnIndex("recipient_ids")),
						                        query.getString(query.getColumnIndex("snippet")),
						                        query.getString(query.getColumnIndex("read")),
						                        mContext));
			} while (query.moveToNext());
		}
		
		mCount = mWidgetItems.size();
    }
    
	@Override
	public int getCount() {
		
		return mCount;
	}

	@Override
	public RemoteViews getViewAt(int arg0) {
		RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_card);
		rv.setTextViewText(R.id.contactName, mWidgetItems.get(arg0).name);
		rv.setTextViewText(R.id.contactNumber, mWidgetItems.get(arg0).number);
		rv.setTextViewText(R.id.contactNumberType, mWidgetItems.get(arg0).preview);
		rv.setTextViewText(R.id.msgCount, mWidgetItems.get(arg0).count);
		rv.setTextViewText(R.id.unreadText, mWidgetItems.get(arg0).read);
		rv.setImageViewBitmap(R.id.contactPicture, getFacebookPhoto(mWidgetItems.get(arg0).number, mContext));
		
		Bundle extras = new Bundle();
        extras.putString("CONVERSATION_TO_OPEN", mWidgetItems.get(arg0).number);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_card_background, fillInIntent);
		
		return rv;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public RemoteViews getLoadingView() {
		return null;
	}

	@Override
	public int getViewTypeCount() {
		return 4;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public void onDataSetChanged() {
		mWidgetItems = new ArrayList<WidgetItem>();
    	String[] projection = new String[]{"_id", "date", "message_count", "recipient_ids", "snippet", "read"};
		Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");
		Cursor query = mContext.getContentResolver().query(uri, projection, null, null, "date desc");
		
		if (query.moveToFirst())
		{
			do
			{
				mWidgetItems.add(new WidgetItem(query.getString(query.getColumnIndex("recipient_ids")),
						                        query.getString(query.getColumnIndex("message_count")),
						                        query.getString(query.getColumnIndex("recipient_ids")),
						                        query.getString(query.getColumnIndex("snippet")),
						                        query.getString(query.getColumnIndex("read")),
						                        mContext));
			} while (query.moveToNext());
		}
		
		mCount = mWidgetItems.size();
	}

	@Override
	public void onDestroy() {
		
	}
	
	public static Bitmap getFacebookPhoto(String phoneNumber, Context context) {
		  try
		  {
		    Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		    Uri photoUri = null;
		    ContentResolver cr = context.getContentResolver();
		    Cursor contact = cr.query(phoneUri,
		            new String[] { ContactsContract.Contacts._ID }, null, null, null);

		    try
		    {
			    if (contact.moveToFirst()) {
			        long userId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID));
			        photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);
			        contact.close();
			    }
			    else {
			        Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.card_person);
			        
			        contact.close();
			        return defaultPhoto;
			    }
			    if (photoUri != null) {
			        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
			                cr, photoUri);
			        if (input != null) {
			        	contact.close();
			            return BitmapFactory.decodeStream(input);
			        }
			    } else {
			        Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.card_person);
			        
			        contact.close();
			        return defaultPhoto;
			    }
			    Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.card_person);
		        
		        contact.close();
			    return defaultPhoto;
		    } catch (Exception e)
		    {
		        	contact.close();
		        	return BitmapFactory.decodeResource(context.getResources(), R.drawable.card_person);
		    }
		  } catch (Exception e)
		  {
		        	return BitmapFactory.decodeResource(context.getResources(), R.drawable.card_person);
		  }
		}

}