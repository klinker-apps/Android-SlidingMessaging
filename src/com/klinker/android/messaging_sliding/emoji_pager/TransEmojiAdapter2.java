package com.klinker.android.messaging_sliding.emoji_pager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.klinker.android.messaging_donate.R;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersBaseAdapter;

public class TransEmojiAdapter2 extends BaseAdapter {

    private Context context;

    public TransEmojiAdapter2(Context context)
    {
        this.context = context;
    }

    @Override
    public int getCount() {
        return mEmojiTexts.length;
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ImageView textView;
        if (convertView == null) {
            textView = new ImageView(context);
            int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
            textView.setPadding(scale, (int)(scale*1.2), scale, (int)(scale * 1.2));
            textView.setAdjustViewBounds(true);
        } else {
            textView = (ImageView) convertView;
        }

        textView.setImageResource(sIconIds[position]);

        return textView;
    }

    public static final String[] mEmojiTexts = {
            // transportation
            "\uD83C\uDFE0", "\uD83C\uDFe1", "\uD83C\uDFE2", "\uD83C\uDFE3", "\uD83C\uDFE4",
            "\uD83C\uDFE5", "\uD83C\uDFe6", "\uD83C\uDFE7", "\uD83C\uDFE8", "\uD83C\uDFE9",
            "\uD83C\uDFEa", "\uD83C\uDFeb", "\u26ea", "\u26f2", "\uD83C\uDFEc",
            "\uD83C\uDFEf", "\uD83C\uDFf0", "\uD83C\uDFEd", "\uD83D\uDDFB", "\uD83D\uDDFc",
            "\uD83D\uDC88", "\uD83D\uDD27", "\uD83D\uDD28", "\uD83D\uDD29", "\uD83D\uDEBF",
            "\uD83D\uDEc1", "\uD83D\uDEc0", "\uD83D\uDEbd", "\uD83D\uDEBe", "\uD83C\uDFbd",
            "\uD83C\uDFa3", "\uD83C\uDFb1", "\uD83C\uDFb3", "\u26be", "\u26f3",
            "\uD83C\uDFbe", "\u26bd", "\uD83C\uDFbf", "\uD83C\uDFc0", "\uD83C\uDFc1",
            "\uD83C\uDFc2", "\uD83C\uDFc3", "\uD83C\uDFc4", "\uD83C\uDFc6", "\uD83C\uDFc7",
            "\uD83D\uDC0E", "\uD83C\uDFc8", "\uD83C\uDFc9", "\uD83C\uDFca", "\uD83D\uDE82",
            "\uD83D\uDE83", "\uD83D\uDE84", "\uD83D\uDE85", "\uD83D\uDE86", "\uD83D\uDE87",
            "\u24c2", "\uD83D\uDE88", "\uD83D\uDE8a", "\uD83D\uDE8b", "\uD83D\uDE8c",
            "\uD83D\uDE8d", "\uD83D\uDE8e", "\uD83D\uDE8f", "\uD83D\uDE90", "\uD83D\uDE91",
            "\uD83D\uDE92", "\uD83D\uDE93", "\uD83D\uDE94", "\uD83D\uDE95", "\uD83D\uDE96",
            "\uD83D\uDE97", "\uD83D\uDE98", "\uD83D\uDE99", "\uD83D\uDE9a", "\uD83D\uDE9b",
            "\uD83D\uDE9c", "\uD83D\uDE9d", "\uD83D\uDE9e", "\uD83D\uDE9f", "\uD83D\uDEa0",
            "\uD83D\uDEa1", "\uD83D\uDEa2", "\uD83D\uDEa3", "\uD83D\uDE81", "\u2708",
            "\uD83D\uDEc2", "\uD83D\uDEc3", "\uD83D\uDEc4", "\uD83D\uDEc5", "\u26f5",
            "\uD83D\uDEb2", "\uD83D\uDEb3", "\uD83D\uDEb4", "\uD83D\uDEb5", "\uD83D\uDEb7",
            "\uD83D\uDEb8", "\uD83D\uDE89", "\uD83D\uDE80", "\uD83D\uDEa4", "\uD83D\uDEb6",
            "\u26fd", "\uD83C\uDD7F", "\uD83D\uDEa5", "\uD83D\uDEa6", "\uD83D\uDEa7",
            "\uD83D\uDEa8", "\u2668", "\uD83D\uDC8C", "\uD83D\uDC8d", "\uD83D\uDC8e",
            "\uD83D\uDC90", "\uD83D\uDC92", "\uD83C\uDDEF\uD83C\uDDF5", "\uD83C\uDDFA\uD83C\uDDF8", "\uD83C\uDDEB\uD83C\uDDF7",
            "\uD83C\uDDE9\uD83C\uDDEA", "\uD83C\uDDEE\uD83C\uDDF9", "\uD83C\uDDEC\uD83C\uDDE7", "\uD83C\uDDEA\uD83C\uDDF8", "\uD83C\uDDF7\uD83C\uDDFA",
            "\uD83C\uDDE8\uD83C\uDDF3", "\uD83C\uDDF0\uD83C\uDDF7"
    };

    private static final int[] sIconIds = {
            // other
            R.drawable.emoji_u1f51d, R.drawable.emoji_u1f519, R.drawable.emoji_u1f51b, R.drawable.emoji_u1f51c, R.drawable.emoji_u1f51a,
            R.drawable.emoji_u23f3, R.drawable.emoji_u231b, R.drawable.emoji_u23f0, R.drawable.emoji_u2648, R.drawable.emoji_u2649,
            R.drawable.emoji_u264a, R.drawable.emoji_u264b, R.drawable.emoji_u264c, R.drawable.emoji_u264d, R.drawable.emoji_u264e,
            R.drawable.emoji_u264f, R.drawable.emoji_u2650, R.drawable.emoji_u2651, R.drawable.emoji_u2652, R.drawable.emoji_u2653,
            R.drawable.emoji_u26ce, R.drawable.emoji_u1f531, R.drawable.emoji_u1f52f, R.drawable.emoji_u1f6bb, R.drawable.emoji_u1f6ae,
            R.drawable.emoji_u1f6af, R.drawable.emoji_u1f6b0, R.drawable.emoji_u1f6b1, R.drawable.emoji_u1f170, R.drawable.emoji_u1f171,
            R.drawable.emoji_u1f18e, R.drawable.emoji_u1f17e, R.drawable.emoji_u1f4ae, R.drawable.emoji_u1f4af, R.drawable.emoji_u1f520,
            R.drawable.emoji_u1f521, R.drawable.emoji_u1f522, R.drawable.emoji_u1f523, R.drawable.emoji_u1f524, R.drawable.emoji_u27bf,
            R.drawable.emoji_u1f4f6, R.drawable.emoji_u1f4f3, R.drawable.emoji_u1f4f4, R.drawable.emoji_u1f4f5, R.drawable.emoji_u1f6b9,
            R.drawable.emoji_u1f6ba, R.drawable.emoji_u1f6bc, R.drawable.emoji_u267f, R.drawable.emoji_u267b, R.drawable.emoji_u1f6ad,
            R.drawable.emoji_u1f6a9, R.drawable.emoji_u26a0, R.drawable.emoji_u1f201, R.drawable.emoji_u1f51e, R.drawable.emoji_u26d4,
            R.drawable.emoji_u1f192, R.drawable.emoji_u1f197, R.drawable.emoji_u1f195, R.drawable.emoji_u1f198, R.drawable.emoji_u1f199,
            R.drawable.emoji_u1f193, R.drawable.emoji_u1f196, R.drawable.emoji_u1f19a, R.drawable.emoji_u1f232, R.drawable.emoji_u1f233,
            R.drawable.emoji_u1f234, R.drawable.emoji_u1f235, R.drawable.emoji_u1f236, R.drawable.emoji_u1f237, R.drawable.emoji_u1f238,
            R.drawable.emoji_u1f239, R.drawable.emoji_u1f202, R.drawable.emoji_u1f23a, R.drawable.emoji_u1f250, R.drawable.emoji_u1f251,
            R.drawable.emoji_u3299, R.drawable.emoji_u00ae, R.drawable.emoji_u00a9, R.drawable.emoji_u2122, R.drawable.emoji_u1f21a,
            R.drawable.emoji_u1f22f, R.drawable.emoji_u3297, R.drawable.emoji_u2b55, R.drawable.emoji_u274c, R.drawable.emoji_u274e,
            R.drawable.emoji_u2139, R.drawable.emoji_u1f6ab, R.drawable.emoji_u2705, R.drawable.emoji_u2714, R.drawable.emoji_u1f517,
            R.drawable.emoji_u2734, R.drawable.emoji_u2733, R.drawable.emoji_u2795, R.drawable.emoji_u2796, R.drawable.emoji_u2716,
            R.drawable.emoji_u2797, R.drawable.emoji_u1f4a0, R.drawable.emoji_u1f4a1, R.drawable.emoji_u1f4a4, R.drawable.emoji_u1f4a2,
            R.drawable.emoji_u1f525, R.drawable.emoji_u1f4a5, R.drawable.emoji_u1f4a6, R.drawable.emoji_u1f4a8, R.drawable.emoji_u1f4ab,
            R.drawable.emoji_u1f55b, R.drawable.emoji_u1f567, R.drawable.emoji_u1f550, R.drawable.emoji_u1f55c, R.drawable.emoji_u1f551,
            R.drawable.emoji_u1f55d, R.drawable.emoji_u1f552, R.drawable.emoji_u1f55e, R.drawable.emoji_u1f553, R.drawable.emoji_u1f55f,
            R.drawable.emoji_u1f554, R.drawable.emoji_u1f560, R.drawable.emoji_u1f555, R.drawable.emoji_u1f561, R.drawable.emoji_u1f556,
            R.drawable.emoji_u1f562, R.drawable.emoji_u1f557, R.drawable.emoji_u1f563, R.drawable.emoji_u1f558, R.drawable.emoji_u1f564,
            R.drawable.emoji_u1f559, R.drawable.emoji_u1f565, R.drawable.emoji_u1f55a, R.drawable.emoji_u1f566, R.drawable.emoji_u2195,
            R.drawable.emoji_u2b06, R.drawable.emoji_u2197, R.drawable.emoji_u27a1, R.drawable.emoji_u2198, R.drawable.emoji_u2b07,
            R.drawable.emoji_u2199, R.drawable.emoji_u2b05, R.drawable.emoji_u2196, R.drawable.emoji_u2194, R.drawable.emoji_u2934,
            R.drawable.emoji_u2935, R.drawable.emoji_u23ea, R.drawable.emoji_u23e9, R.drawable.emoji_u23eb, R.drawable.emoji_u23ec,
            R.drawable.emoji_u25c0, R.drawable.emoji_u25b6, R.drawable.emoji_u1f53d, R.drawable.emoji_u1f53c, R.drawable.emoji_u2747,
            R.drawable.emoji_u2728, R.drawable.emoji_u1f534, R.drawable.emoji_u1f535, R.drawable.emoji_u26aa, R.drawable.emoji_u26ab,
            R.drawable.emoji_u1f533, R.drawable.emoji_u1f532, R.drawable.emoji_u2b50, R.drawable.emoji_u1f31f, R.drawable.emoji_u1f320,
            R.drawable.emoji_u25ab, R.drawable.emoji_u25aa, R.drawable.emoji_u25fd, R.drawable.emoji_u25fe, R.drawable.emoji_u25fc,
            R.drawable.emoji_u25fb, R.drawable.emoji_u2b1b, R.drawable.emoji_u2b1c, R.drawable.emoji_u1f539, R.drawable.emoji_u1f538,
            R.drawable.emoji_u1f537, R.drawable.emoji_u1f536, R.drawable.emoji_u1f53a, R.drawable.emoji_u1f53b, R.drawable.emoji_u1f51f,
            R.drawable.emoji_u20e3, R.drawable.emoji_u2754, R.drawable.emoji_u2753, R.drawable.emoji_u2755, R.drawable.emoji_u2757,
            R.drawable.emoji_u203c, R.drawable.emoji_u2049, R.drawable.emoji_u3030, R.drawable.emoji_u27b0, R.drawable.emoji_u2660,
            R.drawable.emoji_u2665, R.drawable.emoji_u2663, R.drawable.emoji_u2666, R.drawable.emoji_u1f194, R.drawable.emoji_u1f511,
            R.drawable.emoji_u21a9, R.drawable.emoji_u1f191, R.drawable.emoji_u1f50d, R.drawable.emoji_u1f512, R.drawable.emoji_u1f513,
            R.drawable.emoji_u21aa, R.drawable.emoji_u1f510, R.drawable.emoji_u2611, R.drawable.emoji_u1f518, R.drawable.emoji_u1f50e,
            R.drawable.emoji_u1f516, R.drawable.emoji_u1f50f, R.drawable.emoji_u1f503, R.drawable.emoji_u1f500, R.drawable.emoji_u1f501,
            R.drawable.emoji_u1f502, R.drawable.emoji_u1f504, R.drawable.emoji_u1f4e7, R.drawable.emoji_u1f505, R.drawable.emoji_u1f506,
            R.drawable.emoji_u1f507, R.drawable.emoji_u1f508, R.drawable.emoji_u1f509, R.drawable.emoji_u1f50a, R.drawable.emoji_u1f1e6,
            R.drawable.emoji_u1f1e7, R.drawable.emoji_u1f1e8, R.drawable.emoji_u1f1e9, R.drawable.emoji_u1f1ea, R.drawable.emoji_u1f1eb,
            R.drawable.emoji_u1f1ec, R.drawable.emoji_u1f1ed, R.drawable.emoji_u1f1ee, R.drawable.emoji_u1f1ef, R.drawable.emoji_u1f1f0,
            R.drawable.emoji_u1f1f1, R.drawable.emoji_u1f1f2, R.drawable.emoji_u1f1f3, R.drawable.emoji_u1f1f4, R.drawable.emoji_u1f1f5,
            R.drawable.emoji_u1f1f6, R.drawable.emoji_u1f1f7, R.drawable.emoji_u1f1f8, R.drawable.emoji_u1f1f9, R.drawable.emoji_u1f1fa,
            R.drawable.emoji_u1f1fb, R.drawable.emoji_u1f1fc, R.drawable.emoji_u1f1fd, R.drawable.emoji_u1f1fe, R.drawable.emoji_u1f1ff
    };

    public Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        try
        {
            int width = drawable.getIntrinsicWidth();
            width = width > 0 ? width : 1;
            int height = drawable.getIntrinsicHeight();
            height = height > 0 ? height : 1;

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e)
        {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
        }
    }
}