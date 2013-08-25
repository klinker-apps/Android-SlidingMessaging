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

public class EmoticonConverter {
	private static final Factory spannableFactory = Spannable.Factory
	        .getInstance();

	private static final Map<Pattern, Integer> emoticons = new HashMap<Pattern, Integer>();

	static {
	    addPattern(emoticons, ":)", R.drawable.emo_im_happy);
	    addPattern(emoticons, ":(", R.drawable.emo_im_sad);
	    addPattern(emoticons, ";)", R.drawable.emo_im_winking);
	    addPattern(emoticons, ":P", R.drawable.emo_im_tongue_sticking_out);
	    addPattern(emoticons, "=O", R.drawable.emo_im_surprised);
	    addPattern(emoticons, ":*", R.drawable.emo_im_kissing);
	    addPattern(emoticons, ":O", R.drawable.emo_im_yelling);
	    addPattern(emoticons, "B)", R.drawable.emo_im_cool);
	    addPattern(emoticons, ":$", R.drawable.emo_im_money_mouth);
	    addPattern(emoticons, ":!", R.drawable.emo_im_foot_in_mouth);
	    addPattern(emoticons, ":[", R.drawable.emo_im_embarrassed);
	    addPattern(emoticons, "O:)", R.drawable.emo_im_angel);
	    addPattern(emoticons, ":\\", R.drawable.emo_im_undecided);
	    addPattern(emoticons, ":'(", R.drawable.emo_im_crying);
	    addPattern(emoticons, ":X", R.drawable.emo_im_lips_are_sealed);
	    addPattern(emoticons, ":D", R.drawable.emo_im_laughing);
	    addPattern(emoticons, "o_O", R.drawable.emo_im_wtf);
	    addPattern(emoticons, "<3", R.drawable.emo_im_heart);
	    addPattern(emoticons, "X(", R.drawable.emo_im_mad);
	    addPattern(emoticons, ":|", R.drawable.emo_im_pokerface);
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
