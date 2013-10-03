package com.klinker.android.messaging_sliding.developer_tips;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.fima.cardsui.objects.Card;
import com.klinker.android.messaging_donate.R;

public class MyCard extends Card {

    private String message;

    public MyCard(String title, String message) {
        super(title);
        this.message = message;
    }

    @Override
    public View getCardContent(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dev_tip_card, null);

        ((TextView) view.findViewById(R.id.title)).setText(title);
        ((TextView) view.findViewById(R.id.description)).setText(message);

        return view;
    }

}