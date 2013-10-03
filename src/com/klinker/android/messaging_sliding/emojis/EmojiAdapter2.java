package com.klinker.android.messaging_sliding.emojis;

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

public class EmojiAdapter2 extends BaseAdapter implements StickyGridHeadersBaseAdapter {

    private Context context;

    public EmojiAdapter2(Context context) {
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
    public int getCountForHeader(int header) {
        switch (header) {
            case 0:
                return 153;
            case 1:
                return 162;
            case 2:
                return 178;
            case 3:
                return 122;
            case 4:
                return 240;
            default:
                return 0;
        }
    }

    @Override
    public int getNumHeaders() {
        return 5;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        final View header;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            header = inflater.inflate(R.layout.header, parent, false);
        } else {
            header = convertView;
        }

        TextView text = (TextView) header.findViewById(R.id.text1);

        switch (position) {
            case 0:
                text.setText(context.getResources().getString(R.string.people));
                return header;
            case 1:
                text.setText(context.getResources().getString(R.string.things));
                return header;
            case 2:
                text.setText(context.getResources().getString(R.string.nature));
                return header;
            case 3:
                text.setText(context.getResources().getString(R.string.places));
                return header;
            case 4:
                text.setText(context.getResources().getString(R.string.symbols));
                return header;
            default:
                text.setText("");
                return header;
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ImageView textView;
        if (convertView == null) {
            textView = new ImageView(context);
            int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
            textView.setPadding(scale, (int) (scale * 1.2), scale, (int) (scale * 1.2));
            textView.setAdjustViewBounds(true);
        } else {
            textView = (ImageView) convertView;
        }

        textView.setImageResource(sIconIds[position]);

        return textView;
    }

    public static final String[] mEmojiTexts = {
            // People
            "\u263A", "\uD83D\uDE0A", "\uD83D\uDE00", "\uD83D\uDE01", "\uD83D\uDE02",
            "\uD83D\uDE03", "\uD83D\uDE04", "\uD83D\uDE05", "\uD83D\uDE06", "\uD83D\uDE07",
            "\uD83D\uDE08", "\uD83D\uDE09", "\uD83D\uDE2F", "\uD83D\uDE10", "\uD83D\uDE11",
            "\uD83D\uDE15", "\uD83D\uDE20", "\uD83D\uDE2C", "\uD83D\uDE21", "\uD83D\uDE22",
            "\uD83D\uDE34", "\uD83D\uDE2E", "\uD83D\uDE23", "\uD83D\uDE24", "\uD83D\uDE25",
            "\uD83D\uDE26", "\uD83D\uDE27", "\uD83D\uDE28", "\uD83D\uDE29", "\uD83D\uDE30",
            "\uD83D\uDE1F", "\uD83D\uDE31", "\uD83D\uDE32", "\uD83D\uDE33", "\uD83D\uDE35",
            "\uD83D\uDE36", "\uD83D\uDE37", "\uD83D\uDE1E", "\uD83D\uDE12", "\uD83D\uDE0D",
            "\uD83D\uDE1b", "\uD83D\uDE1c", "\uD83D\uDE1d", "\uD83D\uDE0b", "\uD83D\uDE17",
            "\uD83D\uDE19", "\uD83D\uDE18", "\uD83D\uDE1a", "\uD83D\uDE0e", "\uD83D\uDE2d",
            "\uD83D\uDE0c", "\uD83D\uDE16", "\uD83D\uDE14", "\uD83D\uDE2a", "\uD83D\uDE0f",
            "\uD83D\uDE13", "\uD83D\uDE2b", "\uD83D\uDE4b", "\uD83D\uDE4c", "\uD83D\uDE4d",
            "\uD83D\uDE45", "\uD83D\uDE46", "\uD83D\uDE47", "\uD83D\uDE4e", "\uD83D\uDE4f",
            "\uD83D\uDE3a", "\uD83D\uDE3c", "\uD83D\uDE38", "\uD83D\uDE39", "\uD83D\uDE3b",
            "\uD83D\uDE3d", "\uD83D\uDE3f", "\uD83D\uDE3e", "\uD83D\uDE40", "\uD83D\uDE48",
            "\uD83D\uDE49", "\uD83D\uDE4a", "\uD83D\uDCA9", "\uD83D\uDC76", "\uD83D\uDC66",
            "\uD83D\uDC66", "\uD83D\uDC68", "\uD83D\uDC69", "\uD83D\uDC74", "\uD83D\uDC75",
            "\uD83D\uDC8f", "\uD83D\uDC91", "\uD83D\uDC6a", "\uD83D\uDC6b", "\uD83D\uDC6c",
            "\uD83D\uDC6d", "\uD83D\uDC64", "\uD83D\uDC65", "\uD83D\uDC6e", "\uD83D\uDC77",
            "\uD83D\uDC81", "\uD83D\uDC82", "\uD83D\uDC6f", "\uD83D\uDC70", "\uD83D\uDC78",
            "\uD83C\uDF85", "\uD83D\uDC7c", "\uD83D\uDC71", "\uD83D\uDC72", "\uD83D\uDC73",
            "\uD83D\uDC83", "\uD83D\uDC86", "\uD83D\uDC87", "\uD83D\uDC85", "\uD83D\uDC7b",
            "\uD83D\uDC79", "\uD83D\uDC7a", "\uD83D\uDC7d", "\uD83D\uDC7e", "\uD83D\uDC7f",
            "\uD83D\uDC80", "\uD83D\uDCaa", "\uD83D\uDC40", "\uD83D\uDC42", "\uD83D\uDC43",
            "\uD83D\uDC63", "\uD83D\uDC44", "\uD83D\uDC45", "\uD83D\uDC8b", "\u2764",
            "\uD83D\uDC99", "\uD83D\uDC9a", "\uD83D\uDC9b", "\uD83D\uDC9c", "\uD83D\uDC93",
            "\uD83D\uDC94", "\uD83D\uDC95", "\uD83D\uDC96", "\uD83D\uDC97", "\uD83D\uDC98",
            "\uD83D\uDC9d", "\uD83D\uDC9e", "\uD83D\uDC9f", "\uD83D\uDC4d", "\uD83D\uDC4e",
            "\uD83D\uDC4c", "\u270a", "\u270c", "\u270b", "\uD83D\uDC4a",
            "\u261d", "\uD83D\uDC46", "\uD83D\uDC47", "\uD83D\uDC48", "\uD83D\uDC49",
            "\uD83D\uDC4b", "\uD83D\uDC4f", "\uD83D\uDC50",

            //things
            "\uD83D\uDD30", "\uD83D\uDC84", "\uD83D\uDC5e", "\uD83D\uDC5f", "\uD83D\uDC51",
            "\uD83D\uDC52", "\uD83C\uDFa9", "\uD83C\uDF93", "\uD83D\uDC53", "\u231a",
            "\uD83D\uDC54", "\uD83D\uDC55", "\uD83D\uDC56", "\uD83D\uDC57", "\uD83D\uDC58",
            "\uD83D\uDC59", "\uD83D\uDC60", "\uD83D\uDC61", "\uD83D\uDC62", "\uD83D\uDC5a",
            "\uD83D\uDC5c", "\uD83D\uDCbc", "\uD83C\uDF92", "\uD83D\uDC5d", "\uD83D\uDC5b",
            "\uD83D\uDCb0", "\uD83D\uDCb3", "\uD83D\uDCb2", "\uD83D\uDCb5", "\uD83D\uDCb4",
            "\uD83D\uDCb6", "\uD83D\uDCb7", "\uD83D\uDCb1", "\uD83D\uDCb8", "\uD83D\uDCb9",
            "\uD83D\uDD2b", "\uD83D\uDD2a", "\uD83D\uDCa3", "\uD83D\uDC89", "\uD83D\uDC8a",
            "\uD83D\uDEac", "\uD83D\uDD14", "\uD83D\uDD15", "\uD83D\uDEaa", "\uD83D\uDD2c",
            "\uD83D\uDD2d", "\uD83D\uDD2e", "\uD83D\uDD26", "\uD83D\uDD0b", "\uD83D\uDD0c",
            "\uD83D\uDCdc", "\uD83D\uDCd7", "\uD83D\uDCd8", "\uD83D\uDCd9", "\uD83D\uDCda",
            "\uD83D\uDCd4", "\uD83D\uDCd2", "\uD83D\uDCd1", "\uD83D\uDCd3", "\uD83D\uDCd5",
            "\uD83D\uDCd6", "\uD83D\uDCf0", "\uD83D\uDCdb", "\uD83C\uDF83", "\uD83C\uDF84",
            "\uD83C\uDF80", "\uD83C\uDF81", "\uD83C\uDF82", "\uD83C\uDF88", "\uD83C\uDF86",
            "\uD83C\uDF87", "\uD83C\uDF89", "\uD83C\uDF8a", "\uD83C\uDF8d", "\uD83C\uDF8f",
            "\uD83C\uDF8c", "\uD83C\uDF90", "\uD83C\uDF8b", "\uD83C\uDF8e", "\uD83D\uDCf1",
            "\uD83D\uDCf2", "\uD83D\uDCdf", "\u260e", "\uD83D\uDCde", "\uD83D\uDCe0",
            "\uD83D\uDCe6", "\u2709", "\uD83D\uDCe8", "\uD83D\uDCe9", "\uD83D\uDCea",
            "\uD83D\uDCeb", "\uD83D\uDCed", "\uD83D\uDCec", "\uD83D\uDCee", "\uD83D\uDCe4",
            "\uD83D\uDCe5", "\uD83D\uDCef", "\uD83D\uDCe3", "\uD83D\uDCe2", "\uD83D\uDCe1",
            "\uD83D\uDCac", "\uD83D\uDCad", "\u2712", "\u270f", "\uD83D\uDCdd",
            "\uD83D\uDCcf", "\uD83D\uDCd0", "\uD83D\uDCcd", "\uD83D\uDCcc", "\uD83D\uDCce",
            "\u2702", "\uD83D\uDCba", "\uD83D\uDCbb", "\uD83D\uDCbd", "\uD83D\uDCbe",
            "\uD83D\uDCbf", "\uD83D\uDCc6", "\uD83D\uDCc5", "\uD83D\uDCc7", "\uD83D\uDCcb",
            "\uD83D\uDCc1", "\uD83D\uDCc2", "\uD83D\uDCc3", "\uD83D\uDCc4", "\uD83D\uDCca",
            "\uD83D\uDCc8", "\uD83D\uDCc9", "\u26fa", "\uD83C\uDFa1", "\uD83C\uDFa1",
            "\uD83C\uDFa0", "\uD83C\uDFaa", "\uD83C\uDFa8", "\uD83C\uDFac", "\uD83C\uDFa5",
            "\uD83D\uDCf7", "\uD83D\uDCf9", "\uD83C\uDFa6", "\uD83C\uDFad", "\uD83C\uDFab",
            "\uD83C\uDFae", "\uD83C\uDFb2", "\uD83C\uDFb0", "\uD83C\uDCCF", "\uD83C\uDFb4",
            "\uD83C\uDC04", "\uD83C\uDFaf", "\uD83D\uDCfa", "\uD83D\uDCfb", "\uD83D\uDCc0",
            "\uD83D\uDCfc", "\uD83C\uDFa7", "\uD83C\uDFa4", "\uD83C\uDFb5", "\uD83C\uDFb6",
            "\uD83C\uDFbc", "\uD83C\uDFbb", "\uD83C\uDFb9", "\uD83C\uDFb7", "\uD83C\uDFba",
            "\uD83C\uDFb8", "\u303d",

            // nature
            "\uD83D\uDC15", "\uD83D\uDC36", "\uD83D\uDC29", "\uD83D\uDC08", "\uD83D\uDC31",
            "\uD83D\uDC00", "\uD83D\uDC01", "\uD83D\uDC2d", "\uD83D\uDC39", "\uD83D\uDC22",
            "\uD83D\uDC07", "\uD83D\uDC30", "\uD83D\uDC13", "\uD83D\uDC14", "\uD83D\uDC23",
            "\uD83D\uDC24", "\uD83D\uDC25", "\uD83D\uDC26", "\uD83D\uDC0f", "\uD83D\uDC11",
            "\uD83D\uDC10", "\uD83D\uDC3a", "\uD83D\uDC03", "\uD83D\uDC02", "\uD83D\uDC04",
            "\uD83D\uDC2e", "\uD83D\uDC34", "\uD83D\uDC17", "\uD83D\uDC16", "\uD83D\uDC37",
            "\uD83D\uDC3d", "\uD83D\uDC38", "\uD83D\uDC0d", "\uD83D\uDC3c", "\uD83D\uDC27",
            "\uD83D\uDC18", "\uD83D\uDC28", "\uD83D\uDC12", "\uD83D\uDC35", "\uD83D\uDC06",
            "\uD83D\uDC2f", "\uD83D\uDC3b", "\uD83D\uDC2a", "\uD83D\uDC2b", "\uD83D\uDC0a",
            "\uD83D\uDC33", "\uD83D\uDC0b", "\uD83D\uDC1f", "\uD83D\uDC20", "\uD83D\uDC21",
            "\uD83D\uDC19", "\uD83D\uDC1a", "\uD83D\uDC2c", "\uD83D\uDC0c", "\uD83D\uDC1b",
            "\uD83D\uDC1c", "\uD83D\uDC1d", "\uD83D\uDC1e", "\uD83D\uDC32", "\uD83D\uDC09",
            "\uD83D\uDC3e", "\uD83C\uDF78", "\uD83C\uDF7A", "\uD83C\uDF7b", "\uD83C\uDF77",
            "\uD83C\uDF79", "\uD83C\uDF76", "\u2615", "\uD83C\uDF75", "\uD83C\uDF7c",
            "\uD83C\uDF74", "\uD83C\uDF68", "\uD83C\uDF67", "\uD83C\uDF66", "\uD83C\uDF65",
            "\uD83C\uDF70", "\uD83C\uDF6a", "\uD83C\uDF6b", "\uD83C\uDF6c", "\uD83C\uDF6d",
            "\uD83C\uDF6e", "\uD83C\uDF6f", "\uD83C\uDF73", "\uD83C\uDF54", "\uD83C\uDF5f",
            "\uD83C\uDF5d", "\uD83C\uDF55", "\uD83C\uDF56", "\uD83C\uDF57", "\uD83C\uDF64",
            "\uD83C\uDF63", "\uD83C\uDF71", "\uD83C\uDF5e", "\uD83C\uDF5c", "\uD83C\uDF59",
            "\uD83C\uDF5a", "\uD83C\uDF5b", "\uD83C\uDF72", "\uD83C\uDF65", "\uD83C\uDF62",
            "\uD83C\uDF61", "\uD83C\uDF58", "\uD83C\uDF60", "\uD83C\uDF4c", "\uD83C\uDF4e",
            "\uD83C\uDF4f", "\uD83C\uDF4a", "\uD83C\uDF4b", "\uD83C\uDF44", "\uD83C\uDF45",
            "\uD83C\uDF46", "\uD83C\uDF47", "\uD83C\uDF48", "\uD83C\uDF49", "\uD83C\uDF50",
            "\uD83C\uDF51", "\uD83C\uDF52", "\uD83C\uDF53", "\uD83C\uDF4d", "\uD83C\uDF30",
            "\uD83C\uDF31", "\uD83C\uDF32", "\uD83C\uDF33", "\uD83C\uDF34", "\uD83C\uDF35",
            "\uD83C\uDF37", "\uD83C\uDF38", "\uD83C\uDF39", "\uD83C\uDF40", "\uD83C\uDF41",
            "\uD83C\uDF42", "\uD83C\uDF43", "\uD83C\uDF3a", "\uD83C\uDF3b", "\uD83C\uDF3c",
            "\uD83C\uDF3d", "\uD83C\uDF3e", "\uD83C\uDF3f", "\u2600", "\uD83C\uDF08",
            "\u26c5", "\u2601", "\uD83C\uDF01", "\uD83C\uDF02", "\u2614",
            "\uD83D\uDCA7", "\u26a1", "\uD83C\uDF00", "\u2744", "\u26c4",
            "\uD83C\uDF19", "\uD83C\uDF1e", "\uD83C\uDF1d", "\uD83C\uDF1a", "\uD83C\uDF1b",
            "\uD83C\uDF1c", "\uD83C\uDF11", "\uD83C\uDF12", "\uD83C\uDF13", "\uD83C\uDF14",
            "\uD83C\uDF15", "\uD83C\uDF16", "\uD83C\uDF17", "\uD83C\uDF18", "\uD83C\uDF91",
            "\uD83C\uDF04", "\uD83C\uDF05", "\uD83C\uDF07", "\uD83C\uDF06", "\uD83C\uDF03",
            "\uD83C\uDF0c", "\uD83C\uDF09", "\uD83C\uDF0a", "\uD83C\uDF0b", "\uD83C\uDF0e",
            "\uD83C\uDF0f", "\uD83C\uDF0d", "\uD83C\uDF10",

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
            "\uD83C\uDDE8\uD83C\uDDF3", "\uD83C\uDDF0\uD83C\uDDF7",

            // other
            "\uD83D\uDD1d", "\uD83D\uDD19", "\uD83D\uDD1b", "\uD83D\uDD1c", "\uD83D\uDD1a",
            "\u23f3", "\u231b", "\u23f0", "\u2648", "\u2649",
            "\u264a", "\u264b", "\u264c", "\u264d", "\u264e",
            "\u264f", "\u2650", "\u2651", "\u2652", "\u2653",
            "\u26ce", "\uD83D\uDD31", "\uD83D\uDD2f", "\uD83D\uDEbb", "\uD83D\uDEae",
            "\uD83D\uDEaf", "\uD83D\uDEb0", "\uD83D\uDEb1", "\uD83C\uDD70", "\uD83C\uDD71",
            "\uD83C\uDD8e", "\uD83C\uDD7e", "\uD83D\uDCae", "\uD83D\uDCaf", "\uD83D\uDD20",
            "\uD83D\uDD21", "\uD83D\uDD22", "\uD83D\uDD23", "\uD83D\uDD24", "\u27bf",
            "\uD83D\uDCf6", "\uD83D\uDCf3", "\uD83D\uDCf4", "\uD83D\uDCf5", "\uD83D\uDEb9",
            "\uD83D\uDEba", "\uD83D\uDEbc", "\u267f", "\u267b", "\uD83D\uDEad",
            "\uD83D\uDEa9", "\u26a0", "\uD83C\uDE01", "\uD83D\uDD1e", "\u26d4",
            "\uD83C\uDD92", "\uD83C\uDD97", "\uD83C\uDD95", "\uD83C\uDD98", "\uD83C\uDD99",
            "\uD83C\uDD93", "\uD83C\uDD96", "\uD83C\uDD9a", "\uD83C\uDE32", "\uD83C\uDE33",
            "\uD83C\uDE34", "\uD83C\uDE35", "\uD83C\uDE36", "\uD83C\uDE37", "\uD83C\uDE38",
            "\uD83C\uDE39", "\uD83C\uDE02", "\uD83C\uDE3a", "\uD83C\uDE50", "\uD83C\uDE51",
            "\u3299", "\u00ae", "\u00a9", "\u2122", "\uD83C\uDE1a",
            "\uD83C\uDE2f", "\u3297", "\u2b55", "\u274c", "\u274e",
            "\u2139", "\uD83D\uDEab", "\u2705", "\u2714", "\uD83D\uDD17",
            "\u2734", "\u2733", "\u2795", "\u2796", "\u2716",
            "\u2797", "\uD83D\uDCa0", "\uD83D\uDCa1", "\uD83D\uDCa4", "\uD83D\uDCa2",
            "\uD83D\uDD25", "\uD83D\uDCa5", "\uD83D\uDCa6", "\uD83D\uDCa8", "\uD83D\uDCab",
            "\uD83D\uDD5b", "\uD83D\uDD67", "\uD83D\uDD50", "\uD83D\uDD5c", "\uD83D\uDD51",
            "\uD83D\uDD5d", "\uD83D\uDD52", "\uD83D\uDD5e", "\uD83D\uDD53", "\uD83D\uDD5f",
            "\uD83D\uDD54", "\uD83D\uDD60", "\uD83D\uDD54", "\uD83D\uDD61", "\uD83D\uDD56",
            "\uD83D\uDD62", "\uD83D\uDD57", "\uD83D\uDD63", "\uD83D\uDD58", "\uD83D\uDD64",
            "\uD83D\uDD59", "\uD83D\uDD65", "\uD83D\uDD5a", "\uD83D\uDD66", "\u2195",
            "\u2b06", "\u2197", "\u27a1", "\u2198", "\u2b07",
            "\u2199", "\u2b05", "\u2196", "\u2194", "\u2934",
            "\u2935", "\u23ea", "\u23e9", "\u23eb", "\u23ec",
            "\u25c0", "\u25b6", "\uD83D\uDD3d", "\uD83D\uDD3c", "\u2747",
            "\u2728", "\uD83D\uDD34", "\uD83D\uDD35", "\u26aa", "\u26ab",
            "\uD83D\uDD33", "\uD83D\uDD32", "\u2b50", "\uD83C\uDF1f", "\uD83C\uDF20",
            "\u25ab", "\u25aa", "\u25fd", "\u25fe", "\u25fc",
            "\u25fb", "\u2b1b", "\u2b1c", "\uD83D\uDD39", "\uD83D\uDD38",
            "\uD83D\uDD37", "\uD83D\uDD36", "\uD83D\uDD3a", "\uD83D\uDD3b", "\uD83D\uDD1f",
            "\u20e3", "\u2754", "\u2753", "\u2755", "\u2757",
            "\u203c", "\u2049", "\u3030", "\u27b0", "\u2660",
            "\u2665", "\u2663", "\u2666", "\uD83C\uDD94", "\uD83D\uDD11",
            "\u21a9", "\uD83C\uDD91", "\uD83D\uDD0d", "\uD83D\uDD12", "\uD83D\uDD13",
            "\u21aa", "\uD83D\uDD10", "\u2611", "\uD83D\uDD18", "\uD83D\uDD0e",
            "\uD83D\uDD16", "\uD83D\uDD0f", "\uD83D\uDD03", "\uD83D\uDD00", "\uD83D\uDD01",
            "\uD83D\uDD02", "\uD83D\uDD04", "\uD83D\uDCe7", "\uD83D\uDD05", "\uD83D\uDD06",
            "\uD83D\uDD07", "\uD83D\uDD08", "\uD83D\uDD09", "\uD83D\uDD0a", "\uD83C\uDDe6",
            "\uD83C\uDDe7", "\uD83C\uDDe8", "\uD83C\uDDe9", "\uD83C\uDDea", "\uD83C\uDDeb",
            "\uD83C\uDDec", "\uD83C\uDDed", "\uD83C\uDDee", "\uD83C\uDDef", "\uD83C\uDDf0",
            "\uD83C\uDDf1", "\uD83C\uDDf2", "\uD83C\uDDf3", "\uD83C\uDDf4", "\uD83C\uDDf5",
            "\uD83C\uDDf6", "\uD83C\uDDf7", "\uD83C\uDDf8", "\uD83C\uDDf9", "\uD83C\uDDfa",
            "\uD83C\uDDfb", "\uD83C\uDDfc", "\uD83C\uDDfd", "\uD83C\uDDfe", "\uD83C\uDDff",
    };

    public static final int[] sIconIds = {
            // people
            R.drawable.emoji_u263a, R.drawable.emoji_u1f60a, R.drawable.emoji_u1f600, R.drawable.emoji_u1f601, R.drawable.emoji_u1f602,
            R.drawable.emoji_u1f603, R.drawable.emoji_u1f604, R.drawable.emoji_u1f605, R.drawable.emoji_u1f606, R.drawable.emoji_u1f607,
            R.drawable.emoji_u1f608, R.drawable.emoji_u1f609, R.drawable.emoji_u1f62f, R.drawable.emoji_u1f610, R.drawable.emoji_u1f611,
            R.drawable.emoji_u1f615, R.drawable.emoji_u1f620, R.drawable.emoji_u1f62c, R.drawable.emoji_u1f621, R.drawable.emoji_u1f622,
            R.drawable.emoji_u1f634, R.drawable.emoji_u1f62e, R.drawable.emoji_u1f623, R.drawable.emoji_u1f624, R.drawable.emoji_u1f625,
            R.drawable.emoji_u1f626, R.drawable.emoji_u1f627, R.drawable.emoji_u1f628, R.drawable.emoji_u1f629, R.drawable.emoji_u1f630,
            R.drawable.emoji_u1f61f, R.drawable.emoji_u1f631, R.drawable.emoji_u1f632, R.drawable.emoji_u1f633, R.drawable.emoji_u1f635,
            R.drawable.emoji_u1f636, R.drawable.emoji_u1f637, R.drawable.emoji_u1f61e, R.drawable.emoji_u1f612, R.drawable.emoji_u1f60d,
            R.drawable.emoji_u1f61b, R.drawable.emoji_u1f61c, R.drawable.emoji_u1f61d, R.drawable.emoji_u1f60b, R.drawable.emoji_u1f617,
            R.drawable.emoji_u1f619, R.drawable.emoji_u1f618, R.drawable.emoji_u1f61a, R.drawable.emoji_u1f60e, R.drawable.emoji_u1f62d,
            R.drawable.emoji_u1f60c, R.drawable.emoji_u1f616, R.drawable.emoji_u1f614, R.drawable.emoji_u1f62a, R.drawable.emoji_u1f60f,
            R.drawable.emoji_u1f613, R.drawable.emoji_u1f62b, R.drawable.emoji_u1f64b, R.drawable.emoji_u1f64c, R.drawable.emoji_u1f64d,
            R.drawable.emoji_u1f645, R.drawable.emoji_u1f646, R.drawable.emoji_u1f647, R.drawable.emoji_u1f64e, R.drawable.emoji_u1f64f,
            R.drawable.emoji_u1f63a, R.drawable.emoji_u1f63c, R.drawable.emoji_u1f638, R.drawable.emoji_u1f639, R.drawable.emoji_u1f63b,
            R.drawable.emoji_u1f63d, R.drawable.emoji_u1f63f, R.drawable.emoji_u1f63e, R.drawable.emoji_u1f640, R.drawable.emoji_u1f648,
            R.drawable.emoji_u1f649, R.drawable.emoji_u1f64a, R.drawable.emoji_u1f4a9, R.drawable.emoji_u1f476, R.drawable.emoji_u1f466,
            R.drawable.emoji_u1f467, R.drawable.emoji_u1f468, R.drawable.emoji_u1f469, R.drawable.emoji_u1f474, R.drawable.emoji_u1f475,
            R.drawable.emoji_u1f48f, R.drawable.emoji_u1f491, R.drawable.emoji_u1f46a, R.drawable.emoji_u1f46b, R.drawable.emoji_u1f46c,
            R.drawable.emoji_u1f46d, R.drawable.emoji_u1f464, R.drawable.emoji_u1f465, R.drawable.emoji_u1f46e, R.drawable.emoji_u1f477,
            R.drawable.emoji_u1f481, R.drawable.emoji_u1f482, R.drawable.emoji_u1f46f, R.drawable.emoji_u1f470, R.drawable.emoji_u1f478,
            R.drawable.emoji_u1f385, R.drawable.emoji_u1f47c, R.drawable.emoji_u1f471, R.drawable.emoji_u1f472, R.drawable.emoji_u1f473,
            R.drawable.emoji_u1f483, R.drawable.emoji_u1f486, R.drawable.emoji_u1f487, R.drawable.emoji_u1f485, R.drawable.emoji_u1f47b,
            R.drawable.emoji_u1f479, R.drawable.emoji_u1f47a, R.drawable.emoji_u1f47d, R.drawable.emoji_u1f47e, R.drawable.emoji_u1f47f,
            R.drawable.emoji_u1f480, R.drawable.emoji_u1f4aa, R.drawable.emoji_u1f440, R.drawable.emoji_u1f442, R.drawable.emoji_u1f443,
            R.drawable.emoji_u1f463, R.drawable.emoji_u1f444, R.drawable.emoji_u1f445, R.drawable.emoji_u1f48b, R.drawable.emoji_u2764,
            R.drawable.emoji_u1f499, R.drawable.emoji_u1f49a, R.drawable.emoji_u1f49b, R.drawable.emoji_u1f49c, R.drawable.emoji_u1f493,
            R.drawable.emoji_u1f494, R.drawable.emoji_u1f495, R.drawable.emoji_u1f496, R.drawable.emoji_u1f497, R.drawable.emoji_u1f498,
            R.drawable.emoji_u1f49d, R.drawable.emoji_u1f49e, R.drawable.emoji_u1f49f, R.drawable.emoji_u1f44d, R.drawable.emoji_u1f44e,
            R.drawable.emoji_u1f44c, R.drawable.emoji_u270a, R.drawable.emoji_u270c, R.drawable.emoji_u270b, R.drawable.emoji_u1f44a,
            R.drawable.emoji_u261d, R.drawable.emoji_u1f446, R.drawable.emoji_u1f447, R.drawable.emoji_u1f448, R.drawable.emoji_u1f449,
            R.drawable.emoji_u1f44b, R.drawable.emoji_u1f44f, R.drawable.emoji_u1f450,

            //things
            R.drawable.emoji_u1f530, R.drawable.emoji_u1f484, R.drawable.emoji_u1f45e, R.drawable.emoji_u1f45f, R.drawable.emoji_u1f451,
            R.drawable.emoji_u1f452, R.drawable.emoji_u1f3a9, R.drawable.emoji_u1f393, R.drawable.emoji_u1f453, R.drawable.emoji_u231a,
            R.drawable.emoji_u1f454, R.drawable.emoji_u1f455, R.drawable.emoji_u1f456, R.drawable.emoji_u1f457, R.drawable.emoji_u1f458,
            R.drawable.emoji_u1f459, R.drawable.emoji_u1f460, R.drawable.emoji_u1f461, R.drawable.emoji_u1f462, R.drawable.emoji_u1f45a,
            R.drawable.emoji_u1f45c, R.drawable.emoji_u1f4bc, R.drawable.emoji_u1f392, R.drawable.emoji_u1f45d, R.drawable.emoji_u1f45b,
            R.drawable.emoji_u1f4b0, R.drawable.emoji_u1f4b3, R.drawable.emoji_u1f4b2, R.drawable.emoji_u1f4b5, R.drawable.emoji_u1f4b4,
            R.drawable.emoji_u1f4b6, R.drawable.emoji_u1f4b7, R.drawable.emoji_u1f4b1, R.drawable.emoji_u1f4b8, R.drawable.emoji_u1f4b9,
            R.drawable.emoji_u1f52b, R.drawable.emoji_u1f52a, R.drawable.emoji_u1f4a3, R.drawable.emoji_u1f489, R.drawable.emoji_u1f48a,
            R.drawable.emoji_u1f6ac, R.drawable.emoji_u1f514, R.drawable.emoji_u1f515, R.drawable.emoji_u1f6aa, R.drawable.emoji_u1f52c,
            R.drawable.emoji_u1f52d, R.drawable.emoji_u1f52e, R.drawable.emoji_u1f526, R.drawable.emoji_u1f50b, R.drawable.emoji_u1f50c,
            R.drawable.emoji_u1f4dc, R.drawable.emoji_u1f4d7, R.drawable.emoji_u1f4d8, R.drawable.emoji_u1f4d9, R.drawable.emoji_u1f4da,
            R.drawable.emoji_u1f4d4, R.drawable.emoji_u1f4d2, R.drawable.emoji_u1f4d1, R.drawable.emoji_u1f4d3, R.drawable.emoji_u1f4d5,
            R.drawable.emoji_u1f4d6, R.drawable.emoji_u1f4f0, R.drawable.emoji_u1f4db, R.drawable.emoji_u1f383, R.drawable.emoji_u1f384,
            R.drawable.emoji_u1f380, R.drawable.emoji_u1f381, R.drawable.emoji_u1f382, R.drawable.emoji_u1f388, R.drawable.emoji_u1f386,
            R.drawable.emoji_u1f387, R.drawable.emoji_u1f389, R.drawable.emoji_u1f38a, R.drawable.emoji_u1f38d, R.drawable.emoji_u1f38f,
            R.drawable.emoji_u1f38c, R.drawable.emoji_u1f390, R.drawable.emoji_u1f38b, R.drawable.emoji_u1f38e, R.drawable.emoji_u1f4f1,
            R.drawable.emoji_u1f4f2, R.drawable.emoji_u1f4df, R.drawable.emoji_u260e, R.drawable.emoji_u1f4de, R.drawable.emoji_u1f4e0,
            R.drawable.emoji_u1f4e6, R.drawable.emoji_u2709, R.drawable.emoji_u1f4e8, R.drawable.emoji_u1f4e9, R.drawable.emoji_u1f4ea,
            R.drawable.emoji_u1f4eb, R.drawable.emoji_u1f4ed, R.drawable.emoji_u1f4ec, R.drawable.emoji_u1f4ee, R.drawable.emoji_u1f4e4,
            R.drawable.emoji_u1f4e5, R.drawable.emoji_u1f4ef, R.drawable.emoji_u1f4e3, R.drawable.emoji_u1f4e2, R.drawable.emoji_u1f4e1,
            R.drawable.emoji_u1f4ac, R.drawable.emoji_u1f4ad, R.drawable.emoji_u2712, R.drawable.emoji_u270f, R.drawable.emoji_u1f4dd,
            R.drawable.emoji_u1f4cf, R.drawable.emoji_u1f4d0, R.drawable.emoji_u1f4cd, R.drawable.emoji_u1f4cc, R.drawable.emoji_u1f4ce,
            R.drawable.emoji_u2702, R.drawable.emoji_u1f4ba, R.drawable.emoji_u1f4bb, R.drawable.emoji_u1f4bd, R.drawable.emoji_u1f4be,
            R.drawable.emoji_u1f4bf, R.drawable.emoji_u1f4c6, R.drawable.emoji_u1f4c5, R.drawable.emoji_u1f4c7, R.drawable.emoji_u1f4cb,
            R.drawable.emoji_u1f4c1, R.drawable.emoji_u1f4c2, R.drawable.emoji_u1f4c3, R.drawable.emoji_u1f4c4, R.drawable.emoji_u1f4ca,
            R.drawable.emoji_u1f4c8, R.drawable.emoji_u1f4c9, R.drawable.emoji_u26fa, R.drawable.emoji_u1f3a1, R.drawable.emoji_u1f3a2,
            R.drawable.emoji_u1f3a0, R.drawable.emoji_u1f3aa, R.drawable.emoji_u1f3a8, R.drawable.emoji_u1f3ac, R.drawable.emoji_u1f3a5,
            R.drawable.emoji_u1f4f7, R.drawable.emoji_u1f4f9, R.drawable.emoji_u1f3a6, R.drawable.emoji_u1f3ad, R.drawable.emoji_u1f3ab,
            R.drawable.emoji_u1f3ae, R.drawable.emoji_u1f3b2, R.drawable.emoji_u1f3b0, R.drawable.emoji_u1f0cf, R.drawable.emoji_u1f3b4,
            R.drawable.emoji_u1f004, R.drawable.emoji_u1f3af, R.drawable.emoji_u1f4fa, R.drawable.emoji_u1f4fb, R.drawable.emoji_u1f4c0,
            R.drawable.emoji_u1f4fc, R.drawable.emoji_u1f3a7, R.drawable.emoji_u1f3a4, R.drawable.emoji_u1f3b5, R.drawable.emoji_u1f3b6,
            R.drawable.emoji_u1f3bc, R.drawable.emoji_u1f3bb, R.drawable.emoji_u1f3b9, R.drawable.emoji_u1f3b7, R.drawable.emoji_u1f3ba,
            R.drawable.emoji_u1f3b8, R.drawable.emoji_u303d,

            // nature
            R.drawable.emoji_u1f415, R.drawable.emoji_u1f436, R.drawable.emoji_u1f429, R.drawable.emoji_u1f408, R.drawable.emoji_u1f431,
            R.drawable.emoji_u1f400, R.drawable.emoji_u1f401, R.drawable.emoji_u1f42d, R.drawable.emoji_u1f439, R.drawable.emoji_u1f422,
            R.drawable.emoji_u1f407, R.drawable.emoji_u1f430, R.drawable.emoji_u1f413, R.drawable.emoji_u1f414, R.drawable.emoji_u1f423,
            R.drawable.emoji_u1f424, R.drawable.emoji_u1f425, R.drawable.emoji_u1f426, R.drawable.emoji_u1f40f, R.drawable.emoji_u1f411,
            R.drawable.emoji_u1f410, R.drawable.emoji_u1f43a, R.drawable.emoji_u1f403, R.drawable.emoji_u1f402, R.drawable.emoji_u1f404,
            R.drawable.emoji_u1f42e, R.drawable.emoji_u1f434, R.drawable.emoji_u1f417, R.drawable.emoji_u1f416, R.drawable.emoji_u1f437,
            R.drawable.emoji_u1f43d, R.drawable.emoji_u1f438, R.drawable.emoji_u1f40d, R.drawable.emoji_u1f43c, R.drawable.emoji_u1f427,
            R.drawable.emoji_u1f418, R.drawable.emoji_u1f428, R.drawable.emoji_u1f412, R.drawable.emoji_u1f435, R.drawable.emoji_u1f406,
            R.drawable.emoji_u1f42f, R.drawable.emoji_u1f43b, R.drawable.emoji_u1f42a, R.drawable.emoji_u1f42b, R.drawable.emoji_u1f40a,
            R.drawable.emoji_u1f433, R.drawable.emoji_u1f40b, R.drawable.emoji_u1f41f, R.drawable.emoji_u1f420, R.drawable.emoji_u1f421,
            R.drawable.emoji_u1f419, R.drawable.emoji_u1f41a, R.drawable.emoji_u1f42c, R.drawable.emoji_u1f40c, R.drawable.emoji_u1f41b,
            R.drawable.emoji_u1f41c, R.drawable.emoji_u1f41d, R.drawable.emoji_u1f41e, R.drawable.emoji_u1f432, R.drawable.emoji_u1f409,
            R.drawable.emoji_u1f43e, R.drawable.emoji_u1f378, R.drawable.emoji_u1f37a, R.drawable.emoji_u1f37b, R.drawable.emoji_u1f377,
            R.drawable.emoji_u1f379, R.drawable.emoji_u1f376, R.drawable.emoji_u2615, R.drawable.emoji_u1f375, R.drawable.emoji_u1f37c,
            R.drawable.emoji_u1f374, R.drawable.emoji_u1f368, R.drawable.emoji_u1f367, R.drawable.emoji_u1f366, R.drawable.emoji_u1f369,
            R.drawable.emoji_u1f370, R.drawable.emoji_u1f36a, R.drawable.emoji_u1f36b, R.drawable.emoji_u1f36c, R.drawable.emoji_u1f36d,
            R.drawable.emoji_u1f36e, R.drawable.emoji_u1f36f, R.drawable.emoji_u1f373, R.drawable.emoji_u1f354, R.drawable.emoji_u1f35f,
            R.drawable.emoji_u1f35d, R.drawable.emoji_u1f355, R.drawable.emoji_u1f356, R.drawable.emoji_u1f357, R.drawable.emoji_u1f364,
            R.drawable.emoji_u1f363, R.drawable.emoji_u1f371, R.drawable.emoji_u1f35e, R.drawable.emoji_u1f35c, R.drawable.emoji_u1f359,
            R.drawable.emoji_u1f35a, R.drawable.emoji_u1f35b, R.drawable.emoji_u1f372, R.drawable.emoji_u1f365, R.drawable.emoji_u1f362,
            R.drawable.emoji_u1f361, R.drawable.emoji_u1f358, R.drawable.emoji_u1f360, R.drawable.emoji_u1f34c, R.drawable.emoji_u1f34e,
            R.drawable.emoji_u1f34f, R.drawable.emoji_u1f34a, R.drawable.emoji_u1f34b, R.drawable.emoji_u1f344, R.drawable.emoji_u1f345,
            R.drawable.emoji_u1f346, R.drawable.emoji_u1f347, R.drawable.emoji_u1f348, R.drawable.emoji_u1f349, R.drawable.emoji_u1f350,
            R.drawable.emoji_u1f351, R.drawable.emoji_u1f352, R.drawable.emoji_u1f353, R.drawable.emoji_u1f34d, R.drawable.emoji_u1f330,
            R.drawable.emoji_u1f331, R.drawable.emoji_u1f332, R.drawable.emoji_u1f333, R.drawable.emoji_u1f334, R.drawable.emoji_u1f335,
            R.drawable.emoji_u1f337, R.drawable.emoji_u1f338, R.drawable.emoji_u1f339, R.drawable.emoji_u1f340, R.drawable.emoji_u1f341,
            R.drawable.emoji_u1f342, R.drawable.emoji_u1f343, R.drawable.emoji_u1f33a, R.drawable.emoji_u1f33b, R.drawable.emoji_u1f33c,
            R.drawable.emoji_u1f33d, R.drawable.emoji_u1f33e, R.drawable.emoji_u1f33f, R.drawable.emoji_u2600, R.drawable.emoji_u1f308,
            R.drawable.emoji_u26c5, R.drawable.emoji_u2601, R.drawable.emoji_u1f301, R.drawable.emoji_u1f302, R.drawable.emoji_u2614,
            R.drawable.emoji_u1f4a7, R.drawable.emoji_u26a1, R.drawable.emoji_u1f300, R.drawable.emoji_u2744, R.drawable.emoji_u26c4,
            R.drawable.emoji_u1f319, R.drawable.emoji_u1f31e, R.drawable.emoji_u1f31d, R.drawable.emoji_u1f31a, R.drawable.emoji_u1f31b,
            R.drawable.emoji_u1f31c, R.drawable.emoji_u1f311, R.drawable.emoji_u1f312, R.drawable.emoji_u1f313, R.drawable.emoji_u1f314,
            R.drawable.emoji_u1f315, R.drawable.emoji_u1f316, R.drawable.emoji_u1f317, R.drawable.emoji_u1f318, R.drawable.emoji_u1f391,
            R.drawable.emoji_u1f304, R.drawable.emoji_u1f305, R.drawable.emoji_u1f307, R.drawable.emoji_u1f306, R.drawable.emoji_u1f303,
            R.drawable.emoji_u1f30c, R.drawable.emoji_u1f309, R.drawable.emoji_u1f30a, R.drawable.emoji_u1f30b, R.drawable.emoji_u1f30e,
            R.drawable.emoji_u1f30f, R.drawable.emoji_u1f30d, R.drawable.emoji_u1f310,

            // transportation
            R.drawable.emoji_u1f3e0, R.drawable.emoji_u1f3e1, R.drawable.emoji_u1f3e2, R.drawable.emoji_u1f3e3, R.drawable.emoji_u1f3e4,
            R.drawable.emoji_u1f3e5, R.drawable.emoji_u1f3e6, R.drawable.emoji_u1f3e7, R.drawable.emoji_u1f3e8, R.drawable.emoji_u1f3e9,
            R.drawable.emoji_u1f3ea, R.drawable.emoji_u1f3eb, R.drawable.emoji_u26ea, R.drawable.emoji_u26f2, R.drawable.emoji_u1f3ec,
            R.drawable.emoji_u1f3ef, R.drawable.emoji_u1f3f0, R.drawable.emoji_u1f3ed, R.drawable.emoji_u1f5fb, R.drawable.emoji_u1f5fc,
            R.drawable.emoji_u1f488, R.drawable.emoji_u1f527, R.drawable.emoji_u1f528, R.drawable.emoji_u1f529, R.drawable.emoji_u1f6bf,
            R.drawable.emoji_u1f6c1, R.drawable.emoji_u1f6c0, R.drawable.emoji_u1f6bd, R.drawable.emoji_u1f6be, R.drawable.emoji_u1f3bd,
            R.drawable.emoji_u1f3a3, R.drawable.emoji_u1f3b1, R.drawable.emoji_u1f3b3, R.drawable.emoji_u26be, R.drawable.emoji_u26f3,
            R.drawable.emoji_u1f3be, R.drawable.emoji_u26bd, R.drawable.emoji_u1f3bf, R.drawable.emoji_u1f3c0, R.drawable.emoji_u1f3c1,
            R.drawable.emoji_u1f3c2, R.drawable.emoji_u1f3c3, R.drawable.emoji_u1f3c4, R.drawable.emoji_u1f3c6, R.drawable.emoji_u1f3c7,
            R.drawable.emoji_u1f40e, R.drawable.emoji_u1f3c8, R.drawable.emoji_u1f3c9, R.drawable.emoji_u1f3ca, R.drawable.emoji_u1f682,
            R.drawable.emoji_u1f683, R.drawable.emoji_u1f684, R.drawable.emoji_u1f685, R.drawable.emoji_u1f686, R.drawable.emoji_u1f687,
            R.drawable.emoji_u24c2, R.drawable.emoji_u1f688, R.drawable.emoji_u1f68a, R.drawable.emoji_u1f68b, R.drawable.emoji_u1f68c,
            R.drawable.emoji_u1f68d, R.drawable.emoji_u1f68e, R.drawable.emoji_u1f68f, R.drawable.emoji_u1f690, R.drawable.emoji_u1f691,
            R.drawable.emoji_u1f692, R.drawable.emoji_u1f693, R.drawable.emoji_u1f694, R.drawable.emoji_u1f695, R.drawable.emoji_u1f696,
            R.drawable.emoji_u1f697, R.drawable.emoji_u1f698, R.drawable.emoji_u1f699, R.drawable.emoji_u1f69a, R.drawable.emoji_u1f69b,
            R.drawable.emoji_u1f69c, R.drawable.emoji_u1f69d, R.drawable.emoji_u1f69e, R.drawable.emoji_u1f69f, R.drawable.emoji_u1f6a0,
            R.drawable.emoji_u1f6a1, R.drawable.emoji_u1f6a2, R.drawable.emoji_u1f6a3, R.drawable.emoji_u1f681, R.drawable.emoji_u2708,
            R.drawable.emoji_u1f6c2, R.drawable.emoji_u1f6c3, R.drawable.emoji_u1f6c4, R.drawable.emoji_u1f6c5, R.drawable.emoji_u26f5,
            R.drawable.emoji_u1f6b2, R.drawable.emoji_u1f6b3, R.drawable.emoji_u1f6b4, R.drawable.emoji_u1f6b5, R.drawable.emoji_u1f6b7,
            R.drawable.emoji_u1f6b8, R.drawable.emoji_u1f689, R.drawable.emoji_u1f680, R.drawable.emoji_u1f6a4, R.drawable.emoji_u1f6b6,
            R.drawable.emoji_u26fd, R.drawable.emoji_u1f17f, R.drawable.emoji_u1f6a5, R.drawable.emoji_u1f6a6, R.drawable.emoji_u1f6a7,
            R.drawable.emoji_u1f6a8, R.drawable.emoji_u2668, R.drawable.emoji_u1f48c, R.drawable.emoji_u1f48d, R.drawable.emoji_u1f48e,
            R.drawable.emoji_u1f490, R.drawable.emoji_u1f492, R.drawable.emoji_ufe4e5, R.drawable.emoji_ufe4e6, R.drawable.emoji_ufe4e7,
            R.drawable.emoji_ufe4e8, R.drawable.emoji_ufe4e9, R.drawable.emoji_ufe4ea, R.drawable.emoji_ufe4eb, R.drawable.emoji_ufe4ec,
            R.drawable.emoji_ufe4ed, R.drawable.emoji_ufe4ee,

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
            R.drawable.emoji_u1f1fb, R.drawable.emoji_u1f1fc, R.drawable.emoji_u1f1fd, R.drawable.emoji_u1f1fe, R.drawable.emoji_u1f1ff,
    };

    public Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            int width = drawable.getIntrinsicWidth();
            width = width > 0 ? width : 1;
            int height = drawable.getIntrinsicHeight();
            height = height > 0 ? height : 1;

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);
        }
    }
}