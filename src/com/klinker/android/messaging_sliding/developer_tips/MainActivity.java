package com.klinker.android.messaging_sliding.developer_tips;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import com.fima.cardsui.objects.CardStack;
import com.fima.cardsui.views.CardUI;
import com.klinker.android.messaging_donate.R;


public class MainActivity extends Activity {

    private CardUI mCardView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.developer_tips);

        // init CardView
        mCardView = (CardUI) findViewById(R.id.cardsview);
        mCardView.setSwipeable(false);

        // Create the cards
        MyCard tipOne = new MyCard(getResources().getString(R.string.tip_speed_title), getResources().getString(R.string.tip_speed));
        MyCard tipTwo = new MyCard(getResources().getString(R.string.tip_incoming_mms_title), getResources().getString(R.string.tip_incoming_mms));

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

        // add 3 cards to stack
        mCardView.addCard(tipOne);
        mCardView.addCard(tipTwo);

        // draw cards
        mCardView.refresh();
    }

    //@Override
    //public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_main, menu);
        //return true;
    //}
}