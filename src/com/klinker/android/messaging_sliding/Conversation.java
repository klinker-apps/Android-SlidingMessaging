package com.klinker.android.messaging_sliding;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class Conversation {

    private long threadId;
    private int count;
    private boolean read;
    private String body;
    private long date;
    private String number;
    private boolean group;

    public Conversation (long threadId, int count, String read, String body,
                         long date, String number) {
        this.threadId = threadId;
        this.count = count;
        this.read = read.equals("1");
        this.body = body;
        this.date = date;
        this.number = number;
        this.group = this.number.split(" ").length > 1;
    }

    public static String findContactNumber(String id, Context context) {
        try {
            String[] ids = id.split(" ");
            String numbers = "";

            for (int i = 0; i < ids.length; i++)
            {
                try
                {
                    if (ids[i] != null && (!ids[i].equals("") || !ids[i].equals(" ")))
                    {
                        Cursor number = context.getContentResolver().query(Uri.parse("content://mms-sms/canonical-addresses"), null, "_id=" + ids[i], null, null);

                        if (number.moveToFirst())
                        {
                            numbers += number.getString(number.getColumnIndex("address")).replace("-", "").replace(")", "").replace("(", "").replace(" ", "") + " ";
                        } else
                        {
                            numbers += ids[i] + " ";
                        }

                        number.close();
                    } else
                    {

                    }
                } catch (Exception e)
                {
                    numbers += "0 ";
                }
            }

            return numbers;
        } catch (Exception e) {
            return id;
        }
    }

    public static String findRecipientId(String number, Context context) {
        try {
            String[] ids = number.split(" ");
            String numbers = "";

            Cursor id = context.getContentResolver().query(Uri.parse("content://mms-sms/canonical-addresses"), new String[] {"_id", "address"}, null, null, null);

            for (int i = 0; i < ids.length; i++)
            {
                try
                {
                    if (ids[i] != null && (!ids[i].equals("") || !ids[i].equals(" ")))
                    {
                        if (id.moveToFirst())
                        {
                            do {
                                if (id.getString(id.getColumnIndex("address")).endsWith(number.replace("+1", "").replace(" ", "").replace("-", "").replace(")", "").replace("(", ""))) {
                                    numbers += id.getString(id.getColumnIndex("_id"));
                                }
                            } while (id.moveToNext());
                        } else
                        {
                            numbers += ids[i] + " ";
                        }

                        id.close();
                    } else
                    {

                    }
                } catch (Exception e)
                {
                    numbers += "0 ";
                }
            }

            return numbers;
        } catch (Exception e) {
            e.printStackTrace();
            return number;
        }
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
}
