package com.klinker.android.messaging_sliding.emojis;

import android.content.Context;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.style.ImageSpan;
import com.klinker.android.messaging_donate.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmoticonConverter3New {
	private static final Factory spannableFactory = Spannable.Factory
	        .getInstance();

	private static final Map<Pattern, Integer> emoticons = new HashMap<Pattern, Integer>();

	static {
        addPattern(emoticons, ":)", R.drawable.ic_action_emo_basic);
        addPattern(emoticons, ":(", R.drawable.ic_action_emo_sad);
        addPattern(emoticons, ";)", R.drawable.ic_action_emo_wink);
        addPattern(emoticons, ":P", R.drawable.ic_action_emo_tongue);
        addPattern(emoticons, "=O", R.drawable.ic_action_emo_wonder);
        addPattern(emoticons, ":*", R.drawable.ic_action_emo_kiss);
        addPattern(emoticons, ":O", R.drawable.ic_action_emo_wonder);
        addPattern(emoticons, "B)", R.drawable.ic_action_emo_cool);
        addPattern(emoticons, ":[", R.drawable.ic_action_emo_err);
        addPattern(emoticons, ">:)", R.drawable.ic_action_emo_evil);
        addPattern(emoticons, ":\\", R.drawable.ic_action_emo_err);
        addPattern(emoticons, ":'(", R.drawable.ic_action_emo_cry);
        addPattern(emoticons, ":X", R.drawable.ic_action_emo_shame);
        addPattern(emoticons, ":D", R.drawable.ic_action_emo_laugh);
        addPattern(emoticons, "X(", R.drawable.ic_action_emo_angry);
        addPattern(emoticons, ":|", R.drawable.ic_action_emo_err);
        addPattern(emoticons, ":-)", R.drawable.ic_action_emo_basic);
        addPattern(emoticons, ":-(", R.drawable.ic_action_emo_sad);
        addPattern(emoticons, ";-)", R.drawable.ic_action_emo_wink);
        addPattern(emoticons, ":-P", R.drawable.ic_action_emo_tongue);
        addPattern(emoticons, "=-O", R.drawable.ic_action_emo_wonder);
        addPattern(emoticons, ":-*", R.drawable.ic_action_emo_kiss);
        addPattern(emoticons, ":-O", R.drawable.ic_action_emo_wonder);
        addPattern(emoticons, "B-)", R.drawable.ic_action_emo_cool);
        addPattern(emoticons, ":-[", R.drawable.ic_action_emo_err);
        addPattern(emoticons, ">:-)", R.drawable.ic_action_emo_evil);
        addPattern(emoticons, ":-\\", R.drawable.ic_action_emo_err);
        addPattern(emoticons, ":'-(", R.drawable.ic_action_emo_cry);
        addPattern(emoticons, ":-X", R.drawable.ic_action_emo_shame);
        addPattern(emoticons, ":-D", R.drawable.ic_action_emo_laugh);
        addPattern(emoticons, "X-(", R.drawable.ic_action_emo_angry);
        addPattern(emoticons, ":-|", R.drawable.ic_action_emo_err);
	}

	private static void addPattern(Map<Pattern, Integer> map, String smile,
	        int resource) {
	    map.put(Pattern.compile(Pattern.quote(smile)), resource);
	}

	public static boolean addSmiles(Context context, Spannable spannable) {
	    boolean hasChanges = false;
	    for (Entry<Pattern, Integer> entry : emoticons.entrySet()) {
	        Matcher matcher = entry.getKey().matcher(spannable);
	        while (matcher.find()) {
	            boolean set = true;
	            for (ImageSpan span : spannable.getSpans(matcher.start(),
	                    matcher.end(), ImageSpan.class))
	                if (spannable.getSpanStart(span) >= matcher.start()
	                        && spannable.getSpanEnd(span) <= matcher.end())
	                    spannable.removeSpan(span);
	                else {
	                    set = false;
	                    break;
	                }
	            if (set) {
	                hasChanges = true;
	                spannable.setSpan(new ImageSpan(context, entry.getValue()),
	                        matcher.start(), matcher.end(),
	                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	            }
	        }
	    }
	    return hasChanges;
	}

	public static Spannable getSmiledText(Context context, CharSequence text) {
	    Spannable spannable = spannableFactory.newSpannable(text);
	    addSmiles(context, spannable);
	    return spannable;
	}
}

