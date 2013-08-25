package com.klinker.android.messaging_sliding.developer_tips;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import com.fima.cardsui.views.CardUI;
import com.klinker.android.messaging_donate.R;


public class MainActivity extends Activity {

    private CardUI mCardView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.developer_tips);

        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setCustomView(R.layout.centered_action_bar);

        // init CardView
        mCardView = (CardUI) findViewById(R.id.cardsview);
        mCardView.setSwipeable(false);

        // Create the cards
        MyCard tipOne = new MyCard(getResources().getString(R.string.tip_speed_title), getResources().getString(R.string.tip_speed));
        MyCard tipTwo = new MyCard(getResources().getString(R.string.tip_incoming_mms_title), getResources().getString(R.string.tip_incoming_mms));
        MyCard tipThree = new MyCard(getResources().getString(R.string.tip_slideover_title), getResources().getString(R.string.tip_slideover));
        MyCard tipFour = new MyCard(getResources().getString(R.string.tip_contacts_title), getResources().getString(R.string.tip_contacts));
        MyCard tipFive = new MyCard(getString(R.string.tip_notification_listener_title), getString(R.string.tip_notification_listener));

        // Send them to the google+ post if it is clicked on
        tipOne.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/117432358268488452276/posts/WyfpFR8YNnT")));
            }
        });

        tipTwo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/117432358268488452276/posts/DKHXNkidBXz")));
            }
        });

        tipThree.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/117432358268488452276/posts/PfuWZsNW3PG")));
            }
        });

        tipFour.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/117432358268488452276/posts/LqBQmEJ29qS")));
            }
        });

        tipFive.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                } catch (Exception e)
                {

                }
            }
        });

        // add 3 cards to stack
        mCardView.addCard(tipOne);
        mCardView.addCard(tipTwo);
        mCardView.addCard(tipThree);
        mCardView.addCard(tipFour);
        mCardView.addCard(tipFive);

        // draw cards
        mCardView.refresh();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
    }
}