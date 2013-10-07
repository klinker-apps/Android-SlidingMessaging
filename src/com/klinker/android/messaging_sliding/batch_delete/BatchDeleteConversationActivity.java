package com.klinker.android.messaging_sliding.batch_delete;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.ContactUtil;

import java.util.ArrayList;

public class BatchDeleteConversationActivity extends Activity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    public long threadId;

    public BatchDeleteConversationArrayAdapter adapter;

    public ListView list;

    public final Context context = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.batch_delete);

        BatchDeleteConversationArrayAdapter.itemsToDelete = new ArrayList<Long>();

        if (sharedPrefs.getBoolean("ct_light_action_bar", false)) {
            setTheme(R.style.HangoutsTheme);
        }

        getWindow().setBackgroundDrawable(new ColorDrawable(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver))));

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        threadId = b.getLong("threadId");

        list = (ListView) findViewById(R.id.messageListView);
        list.setDivider(new ColorDrawable(sharedPrefs.getInt("ct_messageDividerColor", getResources().getColor(R.color.light_silver))));

        if (sharedPrefs.getBoolean("ct_messageDividerVisibility", true) && sharedPrefs.getString("run_as", "sliding").equals("sliding")) {
            list.setDividerHeight(1);
        } else {
            list.setDividerHeight(0);
        }

        Button deleteButton = (Button) findViewById(R.id.doneButton);

        final Context context = this;

        deleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final ProgressDialog progDialog = new ProgressDialog(context);
                progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progDialog.setMessage(context.getResources().getString(R.string.deleting));
                progDialog.show();

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        ArrayList<Long> ids = BatchDeleteConversationArrayAdapter.itemsToDelete;

                        for (int i = 0; i < ids.size(); i++) {
                            deleteSMS(context, ids.get(i));
                        }

                        ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                            @Override
                            public void run() {
                                progDialog.dismiss();
                                finish();

                                Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
                                context.sendBroadcast(updateWidget);
                            }

                        });
                    }

                }).start();

            }

        });

        final Button selectAll = (Button) findViewById(R.id.selectAllButton);
        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!BatchDeleteConversationArrayAdapter.checkedAll) {
                    BatchDeleteConversationArrayAdapter.itemsToDelete.clear();
                    BatchDeleteConversationArrayAdapter.checkedAll = true;

                    adapter.notifyDataSetChanged();

                    Uri uri3 = Uri.parse("content://mms-sms/conversations/" + threadId + "/");
                    String[] projection2;

                    String proj = "_id body date type msg_box";

                    projection2 = proj.split(" ");

                    String sortOrder = "normalized_date desc";

                    Cursor selectAllQuery = getContentResolver().query(uri3, projection2, null, null, sortOrder);

                    selectAllQuery.moveToFirst();

                    do {
                        BatchDeleteConversationArrayAdapter.itemsToDelete.add(selectAllQuery.getLong(selectAllQuery.getColumnIndex("_id")));
                    } while (selectAllQuery.moveToNext());

                    selectAllQuery.close();


                    selectAll.setText(getResources().getString(R.string.select_none));
                } else {
                    BatchDeleteConversationArrayAdapter.itemsToDelete.clear();
                    BatchDeleteConversationArrayAdapter.checkedAll = false;

                    adapter.notifyDataSetChanged();

                    selectAll.setText(getResources().getString(R.string.select_all));
                }

            }
        });

        getLoaderManager().restartLoader(1, null, this);
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        Uri uri3 = Uri.parse("content://mms-sms/conversations/" + threadId + "/");
        String[] projection2;

        String proj = "_id body date type msg_box";

        projection2 = proj.split(" ");

        String sortOrder = "normalized_date desc";

        return new android.content.CursorLoader(
                context,
                uri3,
                projection2,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, final Cursor query) {
        adapter = new BatchDeleteConversationArrayAdapter((Activity) context, MainActivity.myContactId, ContactUtil.findContactNumber(threadId + "", context), threadId + "", query);

        list.setAdapter(adapter);
        list.setStackFromBottom(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
    }

    public void deleteSMS(Context context, long id) {
        try {
            context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadId + "/"), "_id=?", new String[]{id + ""});
        } catch (Exception e) {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
        }
    }
}