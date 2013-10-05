package com.klinker.android.messaging_donate.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Time;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.*;
import com.klinker.android.messaging_donate.R;

public class MessageUtil {

    public static String getMessageDetails(Context context, Cursor cursor, int size) {
        if (cursor == null) {
            return null;
        }

        boolean isMMS = false;

        try {
            cursor.getString(cursor.getColumnIndex("msg_box"));
            isMMS = true;
        } catch (Exception e) {
        }

        if (isMMS) {
            int type = cursor.getInt(cursor.getColumnIndex("m_type"));
            switch (type) {
                case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
                    return getNotificationIndDetails(context, cursor);
                case PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF:
                case PduHeaders.MESSAGE_TYPE_SEND_REQ:
                    return getMultimediaMessageDetails(context, cursor, size);
                default:
                    return "";
            }
        } else {
            return getTextMessageDetails(context, cursor);
        }
    }

    private static String getNotificationIndDetails(Context context, Cursor cursor) {
        StringBuilder details = new StringBuilder();
        Resources res = context.getResources();

        long id = cursor.getLong(cursor.getColumnIndex("_id"));
        Uri uri = ContentUris.withAppendedId(Telephony.Mms.CONTENT_URI, id);
        NotificationInd nInd;

        try {
            nInd = (NotificationInd) PduPersister.getPduPersister(
                    context).load(uri);
        } catch (Exception e) {
            return context.getResources().getString(R.string.cannot_get_details);
        }

        // Message Type: Mms Notification.
        details.append(res.getString(R.string.message_type_label));
        details.append(res.getString(R.string.multimedia_notification));

        // From: ***
        String from = extractEncStr(context, nInd.getFrom());
        details.append('\n');
        details.append(res.getString(R.string.from_label));
        details.append(!TextUtils.isEmpty(from)? from:
                res.getString(R.string.hidden_sender_address));

        // Date: ***
        details.append('\n');
        details.append(res.getString(
                R.string.expire_on,
                MessageUtil.formatTimeStampString(
                        context, nInd.getExpiry() * 1000L, true)));

        // Subject: ***
        details.append('\n');
        details.append(res.getString(R.string.subject_label));

        EncodedStringValue subject = nInd.getSubject();
        if (subject != null) {
            details.append(subject.getString());
        }

        // Message class: Personal/Advertisement/Infomational/Auto
        details.append('\n');
        details.append(res.getString(R.string.message_class_label));
        details.append(new String(nInd.getMessageClass()));

        // Message size: *** KB
        details.append('\n');
        details.append(res.getString(R.string.message_size_label));
        details.append(String.valueOf((nInd.getMessageSize() + 1023) / 1024));
        details.append(context.getString(R.string.kilobyte));

        return details.toString();
    }

    private static String getMultimediaMessageDetails(
            Context context, Cursor cursor, int size) {
        int type = cursor.getInt(cursor.getColumnIndex("m_type"));
        if (type == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND) {
            return getNotificationIndDetails(context, cursor);
        }

        StringBuilder details = new StringBuilder();
        Resources res = context.getResources();

        long id = cursor.getLong(cursor.getColumnIndex("_id"));
        Uri uri = ContentUris.withAppendedId(Telephony.Mms.CONTENT_URI, id);
        MultimediaMessagePdu msg;

        try {
            msg = (MultimediaMessagePdu) PduPersister.getPduPersister(
                    context).load(uri);
        } catch (MmsException e) {
            return context.getResources().getString(R.string.cannot_get_details);
        }

        // Message Type: Text message.
        details.append(res.getString(R.string.message_type_label));
        details.append(res.getString(R.string.multimedia_message));

        if (msg instanceof RetrieveConf) {
            // From: ***
            String from = extractEncStr(context, ((RetrieveConf) msg).getFrom());
            details.append('\n');
            details.append(res.getString(R.string.from_label));
            details.append(!TextUtils.isEmpty(from)? from:
                    res.getString(R.string.hidden_sender_address));
        }

        // To: ***
        details.append('\n');
        details.append(res.getString(R.string.to_address_label));
        EncodedStringValue[] to = msg.getTo();
        if (to != null) {
            details.append(EncodedStringValue.concat(to));
        }
        else {
        }


        // Bcc: ***
        if (msg instanceof SendReq) {
            EncodedStringValue[] values = ((SendReq) msg).getBcc();
            if ((values != null) && (values.length > 0)) {
                details.append('\n');
                details.append(res.getString(R.string.bcc_label));
                details.append(EncodedStringValue.concat(values));
            }
        }

        // Date: ***
        details.append('\n');
        int msgBox = cursor.getInt(cursor.getColumnIndex("msg_box"));
        if (msgBox == Telephony.Mms.MESSAGE_BOX_DRAFTS) {
            details.append(res.getString(R.string.saved_label));
        } else if (msgBox == Telephony.Mms.MESSAGE_BOX_INBOX) {
            details.append(res.getString(R.string.received_label));
        } else {
            details.append(res.getString(R.string.sent_label));
        }

        details.append(MessageUtil.formatTimeStampString(
                context, msg.getDate() * 1000L, true));

        // Subject: ***
        details.append('\n');
        details.append(res.getString(R.string.subject_label));

        EncodedStringValue subject = msg.getSubject();
        if (subject != null) {
            String subStr = subject.getString();
            // Message size should include size of subject.
            size += subStr.length();
            details.append(subStr);
        }

        // Priority: High/Normal/Low
        details.append('\n');
        details.append(res.getString(R.string.priority_label));
        details.append(getPriorityDescription(context, msg.getPriority()));

        // Message size: *** KB
        details.append('\n');
        details.append(res.getString(R.string.message_size_label));
        details.append((size - 1)/1000 + 1);
        details.append(" KB");

        return details.toString();
    }

    private static String getTextMessageDetails(Context context, Cursor cursor) {

        StringBuilder details = new StringBuilder();
        Resources res = context.getResources();

        // Message Type: Text message.
        details.append(res.getString(R.string.message_type_label));
        details.append(res.getString(R.string.text_message));

        // Address: ***
        details.append('\n');
        int smsType = cursor.getInt(cursor.getColumnIndex("type"));
        if (Telephony.Sms.isOutgoingFolder(smsType)) {
            details.append(res.getString(R.string.to_address_label));
        } else {
            details.append(res.getString(R.string.from_label));
        }
        details.append(cursor.getString(cursor.getColumnIndex("address")));

        // Sent: ***
        if (smsType == Telephony.Sms.MESSAGE_TYPE_INBOX) {
            long date_sent = cursor.getLong(cursor.getColumnIndex("date_sent"));
            if (date_sent > 0) {
                details.append('\n');
                details.append(res.getString(R.string.sent_label));
                details.append(MessageUtil.formatTimeStampString(context, date_sent, true));
            }
        }

        // Received: ***
        details.append('\n');
        if (smsType == Telephony.Sms.MESSAGE_TYPE_DRAFT) {
            details.append(res.getString(R.string.saved_label));
        } else if (smsType == Telephony.Sms.MESSAGE_TYPE_INBOX) {
            details.append(res.getString(R.string.received_label));
        } else {
            details.append(res.getString(R.string.sent_label));
        }

        long date = cursor.getLong(cursor.getColumnIndex("date"));
        details.append(MessageUtil.formatTimeStampString(context, date, true));

        // Delivered: ***
        if (smsType == Telephony.Sms.MESSAGE_TYPE_SENT) {
            // For sent messages with delivery reports, we stick the delivery time in the
            // date_sent column (see MessageStatusReceiver).
            long dateDelivered = cursor.getLong(cursor.getColumnIndex("date_sent"));
            if (dateDelivered > 0) {
                details.append('\n');
                details.append(res.getString(R.string.delivered_label));
                details.append(MessageUtil.formatTimeStampString(context, dateDelivered, true));
            }
        }

        // Error code: ***
        int errorCode = cursor.getInt(cursor.getColumnIndex("error_code"));
        if (errorCode != 0) {
            details.append('\n')
                    .append(res.getString(R.string.error_code_label))
                    .append(errorCode);
        }

        return details.toString();
    }

    public static String formatTimeStampString(Context context, long when) {
        return formatTimeStampString(context, when, false);
    }

    public static String formatTimeStampString(Context context, long when, boolean fullFormat) {
        Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.setToNow();

        // Basic settings for formatDateTime() we want for all cases.
        int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT |
                DateUtils.FORMAT_ABBREV_ALL |
                DateUtils.FORMAT_CAP_AMPM;

        // If the message is from a different year, show the date and year.
        if (then.year != now.year) {
            format_flags |= DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
        } else if (then.yearDay != now.yearDay) {
            // If it is from a different day than today, show only the date.
            format_flags |= DateUtils.FORMAT_SHOW_DATE;
        } else {
            // Otherwise, if the message is from today, show the time.
            format_flags |= DateUtils.FORMAT_SHOW_TIME;
        }

        // If the caller has asked for full details, make sure to show the date
        // and time no matter what we've determined above (but still make showing
        // the year only happen if it is a different year from today).
        if (fullFormat) {
            format_flags |= (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
        }

        return DateUtils.formatDateTime(context, when, format_flags);
    }

    private static String extractEncStr(Context context, EncodedStringValue value) {
        if (value != null) {
            return value.getString();
        } else {
            return "";
        }
    }

    static private String getPriorityDescription(Context context, int PriorityValue) {
        Resources res = context.getResources();
        switch(PriorityValue) {
            case PduHeaders.PRIORITY_HIGH:
                return res.getString(R.string.priority_high);
            case PduHeaders.PRIORITY_LOW:
                return res.getString(R.string.priority_low);
            case PduHeaders.PRIORITY_NORMAL:
            default:
                return res.getString(R.string.priority_normal);
        }
    }
}
