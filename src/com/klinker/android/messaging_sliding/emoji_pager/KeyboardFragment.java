package com.klinker.android.messaging_sliding.emoji_pager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.klinker.android.messaging_sliding.MainActivity;
import com.klinker.android.messaging_sliding.emoji_pager.android.NatureEmojiAdapter;
import com.klinker.android.messaging_sliding.emoji_pager.android.OtherEmojiAdapter;
import com.klinker.android.messaging_sliding.emoji_pager.android.PeopleEmojiAdapter;
import com.klinker.android.messaging_sliding.emoji_pager.android.ThingsEmojiAdapter;
import com.klinker.android.messaging_sliding.emoji_pager.android.TransEmojiAdapter;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter2;

public class KeyboardFragment extends Fragment {

    private static final String ARG_POSITION = "position";

    private int position;
    private SharedPreferences sharedPrefs;

    public static KeyboardFragment newInstance(int position) {
        KeyboardFragment f = new KeyboardFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        position = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final GridView emojiGrid = new GridView(getActivity());

        emojiGrid.setColumnWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
        emojiGrid.setNumColumns(GridView.AUTO_FIT);

        if(position == 0)
            emojiGrid.setAdapter(new PeopleEmojiAdapter(getActivity()));
        else if (position == 1)
            emojiGrid.setAdapter(new ThingsEmojiAdapter(getActivity()));
        else if (position == 2)
            emojiGrid.setAdapter(new NatureEmojiAdapter(getActivity()));
        else if (position == 3)
            emojiGrid.setAdapter(new TransEmojiAdapter(getActivity()));
        else
            emojiGrid.setAdapter(new OtherEmojiAdapter(getActivity()));

        final int tabNum = position;

        emojiGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                if(tabNum == 0)
                    MainActivity.insertEmoji(EmojiAdapter2.mEmojiTexts[position]);
                else if (tabNum == 1)
                    MainActivity.insertEmoji(EmojiAdapter2.mEmojiTexts[position + 153]);
                else if (tabNum == 2)
                    MainActivity.insertEmoji(EmojiAdapter2.mEmojiTexts[position + 315]);
                else if (tabNum == 3)
                    MainActivity.insertEmoji(EmojiAdapter2.mEmojiTexts[position + 493]);
                else
                    MainActivity.insertEmoji(EmojiAdapter2.mEmojiTexts[position+ 615]);
            }
        });


        return emojiGrid;
    }
}