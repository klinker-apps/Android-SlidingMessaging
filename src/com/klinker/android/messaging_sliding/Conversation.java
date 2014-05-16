package com.klinker.android.messaging_sliding;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Telephony;
import com.android.mms.transaction.MmsMessageSender;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.util_alt.SqliteWrapper;
import com.klinker.android.messaging_donate.utils.MessageUtil;

public class Conversation {

    private long threadId;
    private int count;
    private boolean read;
    private String body;
    private long date;
    private String number;
    private boolean group;

    public Conversation(long threadId, int count, String read, String body,
                        long date, String number) {
        this.threadId = threadId;
        this.count = count;
        this.read = read.equals("1");
        this.body = body;
        this.date = date;
        this.number = number;
        this.group = this.number.split(" ").length > 1;
    }

    @Override
    public String toString() {
        return "threadId: " + threadId + ", count: " + count + " read: " + read + " body: " + body + " date: " + date + " number: " + number + " group: " + group;
    }

    public long getThreadId() {
        return threadId;
    }

    public int getCount() {
        return count;
    }

    public boolean getRead() {
        return read;
    }

    public String getBody() {
        return body;
    }

    public long getDate() {
        return date;
    }

    public String getNumber() {
        return number;
    }

    public boolean getGroup() {
        return group;
    }

    public void setRead() {
        read = true;
    }

    public static final String[] UNREAD_PROJECTION = {
            "_id",
            "read"
    };

    private static final String UNREAD_SELECTION = "(read=0 OR seen=0)";
    private static ContentValues sReadContentValues;

    public void setRead(boolean read, final Context context) {
        this.read = read;
        final Uri threadUri = getUri(getThreadId());

        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... none) {
                // If we have no Uri to mark (as in the case of a conversation that
                // has not yet made its way to disk), there's nothing to do.
                if (threadUri != null) {
                    // Check the read flag first. It's much faster to do a query than
                    // to do an update. Timing this function show it's about 10x faster to
                    // do the query compared to the update, even when there's nothing to
                    // update.
                    boolean needUpdate = true;

                    Cursor c = context.getContentResolver().query(threadUri,
                            UNREAD_PROJECTION, UNREAD_SELECTION, null, null);
                    if (c != null) {
                        try {
                            needUpdate = c.getCount() > 0;
                        } finally {
                            c.close();
                        }
                    }

                    if (needUpdate) {
                        if (sReadContentValues == null) {
                            sReadContentValues = new ContentValues(2);
                            sReadContentValues.put("read", 1);
                            sReadContentValues.put("seen", 1);
                        }

                        sendReadReport(context, getThreadId(), PduHeaders.READ_STATUS_READ);
                        context.getContentResolver().update(threadUri, sReadContentValues,
                                UNREAD_SELECTION, null);
                    }
                }

                return null;
            }
        }.execute();
    }

    private static Uri getUri(long threadId) {
        return ContentUris.withAppendedId(Telephony.Threads.CONTENT_URI, threadId);
    }

    public static void markAllConversationsAsSeen(final Context context) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                blockingMarkAllSmsMessagesAsSeen(context);
                blockingMarkAllMmsMessagesAsSeen(context);
            }
        }, "Conversation.markAllConversationsAsSeen");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    private static final String[] SEEN_PROJECTION = new String[] {
            "seen"
    };

    private static void blockingMarkAllSmsMessagesAsSeen(final Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Uri.parse("content://sms/inbox"),
                SEEN_PROJECTION,
                "seen=0",
                null,
                null);

        int count = 0;

        if (cursor != null) {
            try {
                count = cursor.getCount();
            } finally {
                cursor.close();
            }
        }

        if (count == 0) {
            return;
        }

        ContentValues values = new ContentValues(1);
        values.put("seen", 1);

        resolver.update(Uri.parse("content://sms/inbox"),
                values,
                "seen=0",
                null);
    }

    private static void blockingMarkAllMmsMessagesAsSeen(final Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Uri.parse("content://mms/inbox"),
                SEEN_PROJECTION,
                "seen=0",
                null,
                null);

        int count = 0;

        if (cursor != null) {
            try {
                count = cursor.getCount();
            } finally {
                cursor.close();
            }
        }

        if (count == 0) {
            return;
        }

        ContentValues values = new ContentValues(1);
        values.put("seen", 1);

        resolver.update(Uri.parse("content://mms/inbox"),
                values,
                "seen=0",
                null);

    }

    private void sendReadReport(final Context context,
                                final long threadId,
                                final int status) {
        String selection = Telephony.Mms.MESSAGE_TYPE + " = " + PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF
                + " AND " + Telephony.Mms.READ + " = 0"
                + " AND " + Telephony.Mms.READ_REPORT + " = " + PduHeaders.VALUE_YES;

        if (threadId != -1) {
            selection = selection + " AND " + Telephony.Mms.THREAD_ID + " = " + threadId;
        }

        final Cursor c = SqliteWrapper.query(context, context.getContentResolver(),
                Telephony.Mms.Inbox.CONTENT_URI, new String[]{Telephony.Mms._ID, Telephony.Mms.MESSAGE_ID},
                selection, null, null);

        try {
            if (c == null || c.getCount() == 0) {
                return;
            }

            while (c.moveToNext()) {
                Uri uri = ContentUris.withAppendedId(Telephony.Mms.CONTENT_URI, c.getLong(0));
                MmsMessageSender.sendReadRec(context, MessageCursorAdapter.getFrom(uri, context),
                        c.getString(1), status);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
}
