package com.klinker.android.messaging_sliding.emojis;

import java.util.regex.Pattern;

public class EmojiUtil {

    public static final String emojiString = "\u00a9|\u00ae|[\u203c-\u3299]|[\uD83C\uDC04-\uD83C\uDFf0]|[\uD83D\uDC00-\uD83D\uDEc5]";
    public static Pattern emojiPattern = Pattern.compile(emojiString);

    public static final String smileyString = Pattern.quote(":-)") + "|" +
            Pattern.quote(":-(") + "|" +
            Pattern.quote(";-)") + "|" +
            Pattern.quote(":-P") + "|" +
            Pattern.quote("=-O") + "|" +
            Pattern.quote(":-*") + "|" +
            Pattern.quote(":-O") + "|" +
            Pattern.quote("B-)") + "|" +
            Pattern.quote(":-$") + "|" +
            Pattern.quote(":-!") + "|" +
            Pattern.quote(":-[") + "|" +
            Pattern.quote("O:-)") + "|" +
            Pattern.quote(":-\\") + "|" +
            Pattern.quote(":'-(") + "|" +
            Pattern.quote(":-X") + "|" +
            Pattern.quote(":-D") + "|" +
            Pattern.quote("o_O") + "|" +
            Pattern.quote("<3") + "|" +
            Pattern.quote("X-(") + "|" +
            Pattern.quote(":-/") + "|" +
            Pattern.quote(":-|") + "|" +
            Pattern.quote(":)") + "|" +
            Pattern.quote(":(") + "|" +
            Pattern.quote(";)") + "|" +
            Pattern.quote(":P") + "|" +
            Pattern.quote("=O") + "|" +
            Pattern.quote(":*") + "|" +
            Pattern.quote(":O") + "|" +
            Pattern.quote("B)") + "|" +
            Pattern.quote(":$") + "|" +
            Pattern.quote(":!") + "|" +
            Pattern.quote(":[") + "|" +
            Pattern.quote("O:)") + "|" +
            Pattern.quote(":\\") + "|" +
            Pattern.quote(":'(") + "|" +
            Pattern.quote(":X") + "|" +
            Pattern.quote(":D") + "|" +
            Pattern.quote("X(") + "|" +
            Pattern.quote(":/") + "|" +
            Pattern.quote(":|");
    public static Pattern smileyPattern = Pattern.compile(smileyString);
}
