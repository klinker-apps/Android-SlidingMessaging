package com.klinker.android.messaging_sliding;

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
