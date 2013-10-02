package com.klinker.android.messaging_sliding;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Looper;
import android.os.RemoteException;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.ContactUtil;
import com.klinker.android.messaging_donate.utils.IOUtil;
import com.klinker.android.messaging_donate.utils.SendUtil;
import com.klinker.android.messaging_sliding.emojis.EmojiUtil;
import com.klinker.android.messaging_sliding.theme.CustomTheme;

import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

public class MenuArrayAdapter extends ArrayAdapter<String> {
  private final Activity context;
  private final ArrayList<Conversation> conversations;
  private final ViewPager pager;
  private SharedPreferences sharedPrefs;
  private Resources resources;
  
  static class ViewHolder {
	    public TextView text;
	    public TextView text2;
	    public TextView text3;
	    public TextView text4;
	    public QuickContactBadge image;
        public boolean mmsTag;
        public ImageView previewImage;
	  }

  public MenuArrayAdapter(Activity context, ArrayList<Conversation> conversations, ViewPager pager) {
      super(context, R.layout.contact_body);

    this.conversations = conversations;
    this.context = context;
    this.pager = pager;
    this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    this.resources = context.getResources();
  }
  
  @Override
  public int getCount()
  {
      if (MainActivity.limitConversations) {
          if (conversations.size() < 10) {
              return conversations.size();
          } else {
              return 10;
          }
      } else {
          return conversations.size();
      }
  }

  @SuppressLint("SimpleDateFormat")
  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
	  View contactView = convertView;
	  
	  if (contactView == null)
	  {
		  LayoutInflater inflater = (LayoutInflater) context
			        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
		  contactView = inflater.inflate(R.layout.contact_body, parent, false);
		  ViewHolder viewHolder = new ViewHolder();
		  viewHolder.text = (TextView) contactView.findViewById(R.id.contactName);
		  viewHolder.text2 = (TextView) contactView.findViewById(R.id.contactBody);
		  viewHolder.text3 = (TextView) contactView.findViewById(R.id.contactDate);
		  viewHolder.text4 = (TextView) contactView.findViewById(R.id.contactDate2);
		  viewHolder.image = (QuickContactBadge) contactView.findViewById(R.id.quickContactBadge3);
          viewHolder.previewImage = (ImageView) contactView.findViewById(R.id.conversationImage);


          if (MainActivity.settings.hideDate)
          {
              viewHolder.text3.setVisibility(View.INVISIBLE);
              viewHolder.text4.setVisibility(View.INVISIBLE);
          }
		  
		  if (MainActivity.settings.customFont)
	      {
	    	  viewHolder.text.setTypeface(Typeface.createFromFile(MainActivity.settings.customFontPath));
	    	  viewHolder.text2.setTypeface(Typeface.createFromFile(MainActivity.settings.customFontPath));
	    	  viewHolder.text3.setTypeface(Typeface.createFromFile(MainActivity.settings.customFontPath));
	    	  viewHolder.text4.setTypeface(Typeface.createFromFile(MainActivity.settings.customFontPath));
	      }

          if (!MainActivity.settings.customTheme)
          {
              String color = sharedPrefs.getString("menu_text_color", "default");

              if (color.equals("blue"))
              {
                  viewHolder.text2.setTextColor(resources.getColor(R.color.holo_blue));
                  viewHolder.text3.setTextColor(resources.getColor(R.color.holo_blue));
                  viewHolder.text4.setTextColor(resources.getColor(R.color.holo_blue));
              } else if (color.equals("white"))
              {
                  viewHolder.text2.setTextColor(resources.getColor(R.color.white));
                  viewHolder.text3.setTextColor(resources.getColor(R.color.white));
                  viewHolder.text4.setTextColor(resources.getColor(R.color.white));
              } else if (color.equals("green"))
              {
                  viewHolder.text2.setTextColor(resources.getColor(R.color.holo_green));
                  viewHolder.text3.setTextColor(resources.getColor(R.color.holo_green));
                  viewHolder.text4.setTextColor(resources.getColor(R.color.holo_green));
              } else if (color.equals("orange"))
              {
                  viewHolder.text2.setTextColor(resources.getColor(R.color.holo_orange));
                  viewHolder.text3.setTextColor(resources.getColor(R.color.holo_orange));
                  viewHolder.text4.setTextColor(resources.getColor(R.color.holo_orange));
              } else if (color.equals("red"))
              {
                  viewHolder.text2.setTextColor(resources.getColor(R.color.holo_red));
                  viewHolder.text3.setTextColor(resources.getColor(R.color.holo_red));
                  viewHolder.text4.setTextColor(resources.getColor(R.color.holo_red));
              } else if (color.equals("purple"))
              {
                  viewHolder.text2.setTextColor(resources.getColor(R.color.holo_purple));
                  viewHolder.text3.setTextColor(resources.getColor(R.color.holo_purple));
                  viewHolder.text4.setTextColor(resources.getColor(R.color.holo_purple));
              } else if (color.equals("black"))
              {
                  viewHolder.text2.setTextColor(resources.getColor(R.color.pitch_black));
                  viewHolder.text3.setTextColor(resources.getColor(R.color.pitch_black));
                  viewHolder.text4.setTextColor(resources.getColor(R.color.pitch_black));
              } else if (color.equals("grey"))
              {
                  viewHolder.text2.setTextColor(resources.getColor(R.color.grey));
                  viewHolder.text3.setTextColor(resources.getColor(R.color.grey));
                  viewHolder.text4.setTextColor(resources.getColor(R.color.grey));
              }  else
              {
                  viewHolder.text2.setTextColor(MainActivity.settings.ctSummaryTextColor);
                  viewHolder.text3.setTextColor(MainActivity.settings.ctSummaryTextColor);
                  viewHolder.text4.setTextColor(MainActivity.settings.ctSummaryTextColor);
              }

              color = sharedPrefs.getString("name_text_color", "default");

              if (color.equals("blue"))
              {
                  viewHolder.text.setTextColor(resources.getColor(R.color.holo_blue));
              } else if (color.equals("white"))
              {
                  viewHolder.text.setTextColor(resources.getColor(R.color.white));
              } else if (color.equals("green"))
              {
                  viewHolder.text.setTextColor(resources.getColor(R.color.holo_green));
              } else if (color.equals("orange"))
              {
                  viewHolder.text.setTextColor(resources.getColor(R.color.holo_orange));
              } else if (color.equals("red"))
              {
                  viewHolder.text.setTextColor(resources.getColor(R.color.holo_red));
              } else if (color.equals("purple"))
              {
                  viewHolder.text.setTextColor(resources.getColor(R.color.holo_purple));
              } else if (color.equals("black"))
              {
                  viewHolder.text.setTextColor(resources.getColor(R.color.pitch_black));
              } else if (color.equals("grey"))
              {
                  viewHolder.text.setTextColor(resources.getColor(R.color.grey));
              }  else
              {
                  viewHolder.text.setTextColor(sharedPrefs.getInt("ct_nameTextColor", resources.getColor(R.color.black)));
              }
          } else
          {
              viewHolder.text.setTextColor(sharedPrefs.getInt("ct_nameTextColor", resources.getColor(R.color.black)));
              viewHolder.text2.setTextColor(MainActivity.settings.ctSummaryTextColor);
              viewHolder.text3.setTextColor(MainActivity.settings.ctSummaryTextColor);
              viewHolder.text4.setTextColor(MainActivity.settings.ctSummaryTextColor);
          }

          viewHolder.text.setTextSize((float)Integer.parseInt(MainActivity.settings.textSize2));
          viewHolder.text2.setTextSize((float)Integer.parseInt(MainActivity.settings.textSize2));
          viewHolder.text3.setTextSize((float)(Integer.parseInt(MainActivity.settings.textSize2) - 2));
          viewHolder.text4.setTextSize((float)(Integer.parseInt(MainActivity.settings.textSize2) - 2));

          if (!MainActivity.settings.contactPictures2)
          {
              viewHolder.image.setVisibility(View.GONE);
              RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) viewHolder.text.getLayoutParams();
              RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) viewHolder.text2.getLayoutParams();
              params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
              params2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
              viewHolder.text.setLayoutParams(params1);
              viewHolder.text2.setLayoutParams(params2);
          }
		  
		  contactView.setTag(viewHolder);
	  }
	  
	  final ViewHolder holder = (ViewHolder) contactView.getTag();
      
      if (MainActivity.settings.ctDarkContactPics) {
          holder.image.setImageResource(R.drawable.default_avatar_dark);
      } else {
          holder.image.setImageResource(R.drawable.default_avatar);
      }

      holder.text.setText("");
      holder.text2.setText("");
      holder.text3.setText("");
      holder.text4.setText("");
      holder.mmsTag = false;
      holder.previewImage.setVisibility(View.GONE);
      holder.text2.setVisibility(View.VISIBLE);
      holder.text4.setVisibility(View.VISIBLE);

	  new Thread(new Runnable() {

		@Override
		public void run() {
            final String number = ContactUtil.findContactNumber(conversations.get(position).getNumber(), context);
			final Bitmap image = ContactUtil.getFacebookPhoto(number, context);

            Spanned text;
            String names = "";

            if (!MainActivity.settings.hideMessageCounter)
            {
                int count = conversations.get(position).getCount();
                if (conversations.get(position).getGroup())
                {
                    if (count > 1)
                    {
                        text = Html.fromHtml("Group MMS   <small><font color=#" + CustomTheme.convertToARGB(MainActivity.settings.ctMessageCounterColor).substring(3) + "><b>" + count + "</b></color></small>");
                    } else
                    {
                        text = Html.fromHtml("Group MMS");
                    }

                    names = ContactUtil.loadGroupContacts(number, context);
                } else
                {
                    String contactName = ContactUtil.findContactName(number, context);

                    if (count > 1)
                    {
                        text = Html.fromHtml(contactName + "   <small><font color=#" + CustomTheme.convertToARGB(MainActivity.settings.ctMessageCounterColor).substring(3) + "><b>" + count + "</b></color></small>");
                    } else
                    {
                        text = Html.fromHtml(contactName);
                    }
                }
            } else
            {
                if (conversations.get(position).getGroup())
                {
                    text = Html.fromHtml("Group MMS");
                    names = ContactUtil.loadGroupContacts(number, context);
                } else
                {
                    text = Html.fromHtml(ContactUtil.findContactName(number, context));
                }
            }

            final Spanned textF = text;
            final String namesF = names;

		  	context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

				@Override
				public void run() {
                    if (!conversations.get(position).getGroup()) {
                        holder.image.assignContactFromPhone(number, true);
                    }

					if (MainActivity.settings.contactPictures2) {
                        holder.image.setImageBitmap(image);
					} else {
						holder.text2.setPadding(10, 0, 0, 15);
					}

                    if (MainActivity.settings.boldNames) {
                        SpannableString spanString = new SpannableString(textF);
                        spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
                        holder.text.setText(spanString);
                    } else {
                        holder.text.setText(textF);
                    }

                    if (conversations.get(position).getGroup())
                    {
                        holder.text2.setText(namesF);
                    }
				}

		    });
		}

	  }).start();

      String mBody = conversations.get(position).getBody();

      if (mBody.startsWith(" ") || mBody == null) {
          holder.mmsTag = true;

          new Thread (new Runnable() {
              @Override
              public void run() {
                  try {
                      Thread.sleep(500);

                      if (holder.mmsTag) {
                          Cursor query = context.getContentResolver().query(Uri.parse("content://mms-sms/conversations/" + conversations.get(position).getThreadId()), new String[] {"_id", "sub"}, null, null, "normalized_date desc limit 1");
                          String sub = "";
                          String image = null;

                          if (query.moveToFirst()) {
                              sub = query.getString(query.getColumnIndex("sub"));

                              if (sub != null) {
                                  sub += "; ";
                              } else {
                                  sub = "";
                              }

                              String selectionPart = "mid=" + query.getString(query.getColumnIndex("_id"));
                              Uri uri = Uri.parse("content://mms/part");
                              Cursor mmsPart = context.getContentResolver().query(uri, new String[] {"_id", "ct", "_data", "text"}, selectionPart, null, null);

                              if (mmsPart.moveToFirst()) {
                                  do {
                                      String partId = mmsPart.getString(mmsPart.getColumnIndex("_id"));
                                      String type = mmsPart.getString(mmsPart.getColumnIndex("ct"));

                                      if ("text/plain".equals(type)) {
                                          String data = mmsPart.getString(mmsPart.getColumnIndex("_data"));
                                          if (data != null) {
                                              sub += MessageCursorAdapter.getMmsText(partId, context);
                                          } else {
                                              sub += mmsPart.getString(mmsPart.getColumnIndex("text"));
                                          }
                                      }

                                      if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||
                                              "image/gif".equals(type) || "image/jpg".equals(type) ||
                                              "image/png".equals(type)) {
                                          if (image == null) {
                                              image = "content://mms/part/" + partId;
                                          } else {
                                              image += " content://mms/part/" + partId;
                                          }
                                      }
                                  } while (mmsPart.moveToNext());

                                  if (image != null) {
                                      image = image.split(" ")[0];
                                  }
                              }

                              mmsPart.close();
                          }

                          query.close();

                          final String subject = sub;

                          Bitmap previewImage;
                          try {
                              previewImage = SendUtil.getImage(context, Uri.parse(image), 600);
                          } catch (Exception e) {
                              previewImage = null;
                          }

                          final Bitmap previewImageFinal = previewImage;

                          context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                              @Override
                              public void run() {
                                  if (!conversations.get(position).getGroup()) {
                                      holder.text2.setText(subject);

                                      if (previewImageFinal != null && MainActivity.settings.conversationListImages) {
                                          holder.text2.setVisibility(View.GONE);
                                          holder.text4.setVisibility(View.GONE);
                                          holder.previewImage.setVisibility(View.VISIBLE);
                                          holder.previewImage.setImageBitmap(previewImageFinal);
                                      }
                                  }
                              }

                          });
                      }
                  } catch (Exception e) {
                      e.printStackTrace();
                  }
              }
          }).start();
      } else {
          MessageCursorAdapter.setMessageText(holder.text2, mBody, context);
      }

	  Date date2 = new Date(0);

	  try {
		  date2 = new Date(conversations.get(position).getDate());
	  } catch (Exception e) { }

	  if (MainActivity.settings.hourFormat) {
		  holder.text3.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2));
	  } else {
		  holder.text3.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2));
	  }

	  holder.text4.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(date2));

      if (MainActivity.deviceType.equals("phablet") || MainActivity.deviceType.equals("tablet")) {
          holder.text3.setText("");
          holder.text4.setText("");
          holder.text.setMaxLines(1);
          holder.text2.setMaxLines(1);
      }

	  if (!conversations.get(position).getRead()) {
		  if (position != 0) {
			  if (!MainActivity.settings.customBackground) {
				  contactView.setBackgroundColor(MainActivity.settings.ctUnreadConversationColor);
		        }

			  conversations.get(position).setRead();
		  } else {
			  if (!MainActivity.sentMessage) {
				  if (!MainActivity.settings.customBackground) {
					  contactView.setBackgroundColor(MainActivity.settings.ctUnreadConversationColor);
			        }

                  conversations.get(position).setRead();
			  }
		  }
	  }

	  final View contactView2 = contactView;

	  contactView.setOnClickListener(new View.OnClickListener()
		{
		    public void onClick(View v) {
                if (MainActivity.deviceType.equals("phone") || MainActivity.deviceType.equals("phablet2")) {
                    MainActivity.waitToLoad = true;
                }

		    	pager.setCurrentItem(position, false);
                conversations.get(position).setRead();

                if (MainActivity.menu != null) {
			        MainActivity.menu.showContent();
                }

		        if (!MainActivity.settings.customBackground) {
		        	contactView2.setBackgroundColor(MainActivity.settings.ctConversationListBackground);
		        }

		        new Thread(new Runnable() {

					@Override
					public void run() {
						try
						{
							ContentValues values = new ContentValues();
			               	values.put("read", true);
			               	context.getContentResolver().update(Uri.parse("content://sms/conversations/"), values, "thread_id=?", new String[] {"" + conversations.get(position).getThreadId()});
			               	context.getContentResolver().update(Uri.parse("content://mms/conversations/"), values, "thread_id=?", new String[] {"" + conversations.get(position).getThreadId()});
						} catch (Exception e)
						{
							e.printStackTrace();
						}

						ArrayList<String> newMessages = IOUtil.readNewMessages(context);

				        for (int j = 0; j < newMessages.size(); j++)
				        {
				        	if (newMessages.get(j).replace("+", "").replace("+1", "").replace("-", "").equals(holder.text.getText().toString().replace("+", "").replace("+1", "").replace("-", "")))
				        	{
				        		newMessages.remove(j);
				        	}
				        }

				        IOUtil.writeNewMessages(newMessages, context);

					}

		        }).start();
		    }
		});

	  contactView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {

				Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		        vibrator.vibrate(25);

                final ProgressDialog progDialog = new ProgressDialog(context);

				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage(resources.getString(R.string.delete_messages) + "\n\n" + resources.getString(R.string.conversation) + ": " + ContactUtil.findContactName(ContactUtil.findContactNumber(conversations.get(position).getNumber(), context), context));
				builder.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
			               progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			               progDialog.setMessage(resources.getString(R.string.deleting));
			               progDialog.show();

			               new Thread(new Runnable(){

								@Override
								public void run() {
                                    Looper.prepare();
                                    deleteSMS(context, "" + conversations.get(position).getThreadId());
								}

			               }).start();
			           }


				public void deleteSMS(final Context context, final String id) {
                    if (checkLocked(context, id)) {
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
                                                        deleteLocked(context, id);

                                                        ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                ((MainActivity)context).mSectionsPagerAdapter.notifyDataSetChanged();
                                                                ((MainActivity)context).refreshViewPager();
                                                                progDialog.dismiss();
                                                            }

                                                        });
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
                                                        dontDeleteLocked(context, id);

                                                        ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                ((MainActivity)context).mSectionsPagerAdapter.notifyDataSetChanged();
                                                                ((MainActivity)context).refreshViewPager();
                                                                progDialog.dismiss();
                                                            }

                                                        });
                                                    }
                                                }).start();
                                            }
                                        })
                                        .create()
                                        .show();
                            }

                        });
                    } else {
                        deleteLocked(context, id);

                        ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                            @Override
                            public void run() {
                                ((MainActivity)context).mSectionsPagerAdapter.notifyDataSetChanged();
                                ((MainActivity)context).refreshViewPager();
                                progDialog.dismiss();
                            }

                        });
                    }
				}

                public boolean checkLocked(Context context, String id) {
                    try {
                        return context.getContentResolver().query(Uri.parse("content://mms-sms/locked/" + id + "/"), new String[]{"_id"}, null, null, null).moveToFirst();
                    } catch (Exception e) {
                        // if failed, then say there are some locked to be careful
                        return false;
                    }
                }

                public void deleteLocked(Context context, String id) {
                    try {
                        context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + id + "/"), null, null);
                        context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/"), "_id=?", new String[] {id});
                    } catch (Exception e) {

                    }
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
                }});
				builder.setNegativeButton(resources.getString(R.string.no), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               dialog.dismiss();
			           }
			       });
				AlertDialog dialog = builder.create();

				dialog.show();
				return false;
			}

		});

	  return contactView;
  }
} 