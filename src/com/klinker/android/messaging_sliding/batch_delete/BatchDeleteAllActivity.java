package com.klinker.android.messaging_sliding.batch_delete;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.database.Cursor;
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
import com.klinker.android.messaging_donate.R;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class BatchDeleteAllActivity extends Activity {
	
	public ArrayList<String> threadIds, inboxNumber, inboxBody, inboxGroup;
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
		
		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		threadIds = b.getStringArrayList("threadIds");
		inboxNumber = b.getStringArrayList("inboxNumber");
        inboxBody = b.getStringArrayList("inboxBody");
        inboxGroup = b.getStringArrayList("group");
		
		ListView contactList = (ListView) findViewById(R.id.messageListView);
        contactList.setDivider(new ColorDrawable(sharedPrefs.getInt("ct_conversationDividerColor", getResources().getColor(R.color.white))));

        if (sharedPrefs.getBoolean("ct_messageDividerVisibility", true)) {
            contactList.setDividerHeight(1);
        } else {
            contactList.setDividerHeight(0);
        }

        // Animation for the list view
        mAdapter = new BatchDeleteAllArrayAdapter(this, inboxBody, inboxNumber, inboxGroup);

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
							
							for (int i = 0; i < positions.size(); i++)
							{
								deleteSMS(context, threadIds.get(positions.get(i)));
							}
							
							ArrayList<String> data = new ArrayList<String>();
							
							String[] projection = new String[]{"_id", "date", "message_count", "recipient_ids", "snippet", "read"};
							Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");
							Cursor query = getContentResolver().query(uri, projection, null, null, "date desc");
							
							if (query.moveToFirst())
							{
								do
								{
									data.add(query.getString(query.getColumnIndex("_id")));
									data.add(query.getString(query.getColumnIndex("message_count")));
									data.add(query.getString(query.getColumnIndex("read")));
									
									data.add(" ");
									
									try
									{
										data.set(data.size() - 1, query.getString(query.getColumnIndex("snippet")).replaceAll("\\\n", " "));
									} catch (Exception e)
									{
									}
									
									data.add(query.getString(query.getColumnIndex("date")));
									
									String[] ids = query.getString(query.getColumnIndex("recipient_ids")).split(" ");
									String numbers = "";
									
									for (int i = 0; i < ids.length; i++)
									{
										try
										{
											if (ids[i] != null && (!ids[i].equals("") || !ids[i].equals(" ")))
											{
												Cursor number = getContentResolver().query(Uri.parse("content://mms-sms/canonical-addresses"), null, "_id=" + ids[i], null, null);
												
												if (number.moveToFirst())
												{
													numbers += number.getString(number.getColumnIndex("address")).replaceAll("-", "").replaceAll("\\)", "").replaceAll("\\(", "").replaceAll(" ", "") + " ";
												} else
												{
													numbers += "0 ";
												}
												
												number.close();
											} else
											{
												
											}
										} catch (Exception e)
										{
											numbers += "0 ";
										}
									}
									
									data.add(numbers.trim());
									
									if (ids.length > 1)
									{
										data.add("yes");
									} else
									{
										data.add("no");
									}
								} while (query.moveToNext());
							}
							
							query.close();
							
							writeToFile3(data, context);

                            ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                @Override
                                public void run() {
                                    progDialog.dismiss();
                                    Intent intent = new Intent(context, com.klinker.android.messaging_donate.MainActivity.class);
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
	}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
    }

    public void deleteSMS(final Context context, final String id) {
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

    public boolean checkLocked(Context context, String id) {
        return context.getContentResolver().query(Uri.parse("content://mms-sms/locked/" + id + "/"), new String[]{"_id"}, null, null, null).moveToFirst();
    }

    public void deleteLocked(Context context, String id) {
        context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + id + "/"), null, null);
        context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/"), "_id=?", new String[] {id});
    }

    public void dontDeleteLocked(Context context, String id) {
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
	
	private static void writeToFile3(ArrayList<String> data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("conversationList.txt", Context.MODE_PRIVATE));
            
            for (int i = 0; i < data.size(); i++)
            {
            	outputStreamWriter.write(data.get(i) + "\n");
            }
            	
            outputStreamWriter.close();
        }
        catch (IOException e) {
            
        } 
		
	}
}
