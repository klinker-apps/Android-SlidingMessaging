package com.klinker.android.messaging_sliding.batch_delete;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.Conversation;

import java.util.ArrayList;

public class BatchDeleteAllActivity extends Activity {
	
	//public ArrayList<String> threadIds, inboxNumber, inboxBody, inboxGroup;
    public ArrayList<Conversation> conversations;
    public BatchDeleteAllArrayAdapter mAdapter;
    public final Context context = this;

    public Boolean deleteLocked = null;
    public boolean showingDialog = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.batch_delete);

		BatchDeleteAllArrayAdapter.itemsToDelete = new ArrayList<Integer>();

        if (sharedPrefs.getBoolean("ct_light_action_bar", false))
        {
            setTheme(R.style.HangoutsTheme);
        }

        getWindow().setBackgroundDrawable(new ColorDrawable(sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver))));

		conversations = MainActivity.conversations;
		
		ListView contactList = (ListView) findViewById(R.id.messageListView);
        contactList.setDivider(new ColorDrawable(sharedPrefs.getInt("ct_conversationDividerColor", getResources().getColor(R.color.white))));

        if (sharedPrefs.getBoolean("ct_messageDividerVisibility", true)) {
            contactList.setDividerHeight(1);
        } else {
            contactList.setDividerHeight(0);
        }

        // Animation for the list view
        mAdapter = new BatchDeleteAllArrayAdapter(this, conversations);

        SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(mAdapter);
        swingBottomInAnimationAdapter.setListView(contactList);
        contactList.setAdapter(swingBottomInAnimationAdapter);

		Button deleteButton = (Button) findViewById(R.id.doneButton);
		
		final Context context = this;
		
		deleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				final ProgressDialog progDialog = new ProgressDialog(context);
	               progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	               progDialog.setMessage(context.getResources().getString(R.string.deleting));
	               progDialog.show();
	               
	               new Thread(new Runnable(){

						@Override
						public void run() {
							ArrayList<Integer> positions = BatchDeleteAllArrayAdapter.itemsToDelete;
							
							for (int i = 0; i < positions.size(); i++) {
								deleteSMS(context, conversations.get(i).getThreadId());
							}

                            ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                @Override
                                public void run() {
                                    progDialog.dismiss();
                                    Intent intent = new Intent(context, MainActivity.class);
                                    context.startActivity(intent);
                                    finish();
                                    overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);

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

                if (!BatchDeleteAllArrayAdapter.checkedAll) {
                    BatchDeleteAllArrayAdapter.itemsToDelete.clear();
                    BatchDeleteAllArrayAdapter.checkedAll = true;

                    mAdapter.notifyDataSetChanged();

                    for (int i = 0; i < conversations.size(); i++)
                        BatchDeleteAllArrayAdapter.itemsToDelete.add(i);

                    selectAll.setText(getResources().getString(R.string.select_none));

                } else {
                    BatchDeleteAllArrayAdapter.itemsToDelete.clear();
                    BatchDeleteAllArrayAdapter.checkedAll = false;

                    mAdapter.notifyDataSetChanged();

                    selectAll.setText(getResources().getString(R.string.select_all));
                }

            }
        });
	}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
    }

    public void deleteSMS(final Context context, final long id) {
        if (checkLocked(context, id)) {
            while (showingDialog) {
                try {
                    Thread.sleep(250);
                } catch (Exception e) {

                }
            }

            if (deleteLocked == null) {
                showingDialog = true;
                ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                    @Override
                    public void run() {
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.locked_messages)
                                .setMessage(R.string.locked_messages_summary)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                deleteLocked = true;
                                                showingDialog = false;
                                                deleteLocked(context, id);
                                            }
                                        }).start();
                                    }
                                })
                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                deleteLocked = false;
                                                showingDialog = false;
                                                dontDeleteLocked(context, id);
                                            }
                                        }).start();
                                    }
                                })
                                .create()
                                .show();
                    }

                });
            } else {
                if (deleteLocked) {
                    deleteLocked(context, id);
                } else {
                    dontDeleteLocked(context, id);
                }
            }
        } else {
            deleteLocked(context, id);
        }
    }

    public boolean checkLocked(Context context, long id) {
        try {
            return context.getContentResolver().query(Uri.parse("content://mms-sms/locked/" + id + "/"), new String[]{"_id"}, null, null, null).moveToFirst();
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteLocked(Context context, long id) {
        try {
            context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + id + "/"), null, null);
            context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/"), "_id=?", new String[] {id + ""});
        } catch (Exception e) {

        }
    }

    public void dontDeleteLocked(Context context, long id) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newDelete(Uri.parse("content://mms-sms/conversations/" + id + "/"))
                .withSelection("locked=?", new String[]{"0"})
                .build());
        try {
            context.getContentResolver().applyBatch("mms-sms", ops);
        } catch (RemoteException e) {
        } catch (OperationApplicationException e) {
        }
    }
}
