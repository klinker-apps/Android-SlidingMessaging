package com.klinker.android.messaging_sliding.emoji_pager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_sliding.emoji_pager.adapters.*;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter2;

public class KeyboardFragment extends Fragment {

    private static final String ARG_POSITION = "position";

    private int position;

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

        if (position == 0)
            emojiGrid.setAdapter(new RecentEmojiAdapter(getActivity(), MainActivity.recents));
        else if(position == 1)
            emojiGrid.setAdapter(new PeopleEmojiAdapter(getActivity()));
        else if (position == 2)
            emojiGrid.setAdapter(new ThingsEmojiAdapter(getActivity()));
        else if (position == 3)
            emojiGrid.setAdapter(new NatureEmojiAdapter(getActivity()));
        else if (position == 4)
            emojiGrid.setAdapter(new TransEmojiAdapter(getActivity()));
        else
            emojiGrid.setAdapter(new OtherEmojiAdapter(getActivity()));

        final int tabNum = position;

        emojiGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                if (tabNum == 0) {

                } else if(tabNum == 1)
                    MainActivity.insertEmoji(EmojiAdapter2.mEmojiTexts[position], EmojiAdapter2.sIconIds[position]);
                else if (tabNum == 2)
                    MainActivity.insertEmoji(EmojiAdapter2.mEmojiTexts[position + 153], EmojiAdapter2.sIconIds[position + 153]);
                else if (tabNum == 3)
                    MainActivity.insertEmoji(EmojiAdapter2.mEmojiTexts[position + 315], EmojiAdapter2.sIconIds[position + 315]);
                else if (tabNum == 4)
                    MainActivity.insertEmoji(EmojiAdapter2.mEmojiTexts[position + 493], EmojiAdapter2.sIconIds[position + 493]);
                else if (tabNum == 5)
                    MainActivity.insertEmoji(EmojiAdapter2.mEmojiTexts[position+ 615], EmojiAdapter2.sIconIds[position + 615]);
            }
        });


        return emojiGrid;
    }
}