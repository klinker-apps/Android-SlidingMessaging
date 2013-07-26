package com.klinker.android.messaging_sliding.receivers;

import com.google.android.mms.pdu_alt.DeliveryInd;
import com.google.android.mms.pdu_alt.GenericPdu;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduParser;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class MMSReceivedReceiver extends BroadcastReceiver {
	
	public void onReceive(Context context, Intent intent)
	{
		try
		{
			byte[] pushData = intent.getByteArrayExtra("data");
		    PduParser parser = new PduParser(pushData);
		    GenericPdu pdu = parser.parse();
		    
		    int type = pdu.getMessageType();
		    if(type == PduHeaders.MESSAGE_TYPE_DELIVERY_IND) {
		        String id = new String(((DeliveryInd)pdu).getMessageId());
	
		        int status = ((DeliveryInd)pdu).getStatus();
		        if(status == PduHeaders.STATUS_RETRIEVED) {
		        	ContentValues values = new ContentValues();
				    values.put("status", 0);
				    String where = "_id" + " = '" + id + "'";
				    context.getContentResolver().update(Uri.parse("content://mms"), values, where, null);
				    
				    Toast.makeText(context, "MMS Delivered", Toast.LENGTH_SHORT).show();
		        }
		    }
		} catch (Exception e)
		{
			
		}
	}
}