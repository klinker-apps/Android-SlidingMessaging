package com.klinker.android.messaging_sliding.notifications;

public class IndividualSetting {
    public String ringtone;
    public int color;
    public String vibratePattern;
    public String name;

    public IndividualSetting(String name, int c, String vib, String ring) {
        this.ringtone = ring;
        this.color = c;
        this.vibratePattern = vib;
        this.name = name;
    }
}
