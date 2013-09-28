package com.klinker.android.messaging_sliding;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Looper;
import android.os.RemoteException;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.emojis.*;
import com.klinker.android.messaging_sliding.theme.CustomTheme;

import java.io.*;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MenuArrayAdapter extends ArrayAdapter<String> {
  private final Activity context;
  private final ArrayList<Conversation> conversations;
  private final ViewPager pager;
  private static final String FILENAME = "newMessages.txt";
  private SharedPreferences sharedPrefs;
  private Pattern pattern;
  
  static class ViewHolder {
	    public TextView text;
	    public TextView text2;
	    public TextView text3;
	    public TextView text4;
	    public QuickContactBadge image;
	  }

  public MenuArrayAdapter(Activity context, ArrayList<Conversation> conversations, ArrayList<String> body, ViewPager pager) {

      super(context, R.layout.contact_body, body);

    this.conversations = conversations;
    this.context = context;
    this.pager = pager;
    this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

    pattern = Pattern.compile(MessageCursorAdapter.patternStr);
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
                  viewHolder.text2.setTextColor(context.getResources().getColor(R.color.holo_blue));
                  viewHolder.text3.setTextColor(context.getResources().getColor(R.color.holo_blue));
                  viewHolder.text4.setTextColor(context.getResources().getColor(R.color.holo_blue));
              } else if (color.equals("white"))
              {
                  viewHolder.text2.setTextColor(context.getResources().getColor(R.color.white));
                  viewHolder.text3.setTextColor(context.getResources().getColor(R.color.white));
                  viewHolder.text4.setTextColor(context.getResources().getColor(R.color.white));
              } else if (color.equals("green"))
              {
                  viewHolder.text2.setTextColor(context.getResources().getColor(R.color.holo_green));
                  viewHolder.text3.setTextColor(context.getResources().getColor(R.color.holo_green));
                  viewHolder.text4.setTextColor(context.getResources().getColor(R.color.holo_green));
              } else if (color.equals("orange"))
              {
                  viewHolder.text2.setTextColor(context.getResources().getColor(R.color.holo_orange));
                  viewHolder.text3.setTextColor(context.getResources().getColor(R.color.holo_orange));
                  viewHolder.text4.setTextColor(context.getResources().getColor(R.color.holo_orange));
              } else if (color.equals("red"))
              {
                  viewHolder.text2.setTextColor(context.getResources().getColor(R.color.holo_red));
                  viewHolder.text3.setTextColor(context.getResources().getColor(R.color.holo_red));
                  viewHolder.text4.setTextColor(context.getResources().getColor(R.color.holo_red));
              } else if (color.equals("purple"))
              {
                  viewHolder.text2.setTextColor(context.getResources().getColor(R.color.holo_purple));
                  viewHolder.text3.setTextColor(context.getResources().getColor(R.color.holo_purple));
                  viewHolder.text4.setTextColor(context.getResources().getColor(R.color.holo_purple));
              } else if (color.equals("black"))
              {
                  viewHolder.text2.setTextColor(context.getResources().getColor(R.color.pitch_black));
                  viewHolder.text3.setTextColor(context.getResources().getColor(R.color.pitch_black));
                  viewHolder.text4.setTextColor(context.getResources().getColor(R.color.pitch_black));
              } else if (color.equals("grey"))
              {
                  viewHolder.text2.setTextColor(context.getResources().getColor(R.color.grey));
                  viewHolder.text3.setTextColor(context.getResources().getColor(R.color.grey));
                  viewHolder.text4.setTextColor(context.getResources().getColor(R.color.grey));
              }  else
              {
                  viewHolder.text2.setTextColor(MainActivity.settings.ctSummaryTextColor);
                  viewHolder.text3.setTextColor(MainActivity.settings.ctSummaryTextColor);
                  viewHolder.text4.setTextColor(MainActivity.settings.ctSummaryTextColor);
              }

              color = sharedPrefs.getString("name_text_color", "default");

              if (color.equals("blue"))
              {
                  viewHolder.text.setTextColor(context.getResources().getColor(R.color.holo_blue));
              } else if (color.equals("white"))
              {
                  viewHolder.text.setTextColor(context.getResources().getColor(R.color.white));
              } else if (color.equals("green"))
              {
                  viewHolder.text.setTextColor(context.getResources().getColor(R.color.holo_green));
              } else if (color.equals("orange"))
              {
                  viewHolder.text.setTextColor(context.getResources().getColor(R.color.holo_orange));
              } else if (color.equals("red"))
              {
                  viewHolder.text.setTextColor(context.getResources().getColor(R.color.holo_red));
              } else if (color.equals("purple"))
              {
                  viewHolder.text.setTextColor(context.getResources().getColor(R.color.holo_purple));
              } else if (color.equals("black"))
              {
                  viewHolder.text.setTextColor(context.getResources().getColor(R.color.pitch_black));
              } else if (color.equals("grey"))
              {
                  viewHolder.text.setTextColor(context.getResources().getColor(R.color.grey));
              }  else
              {
                  viewHolder.text.setTextColor(sharedPrefs.getInt("ct_nameTextColor", context.getResources().getColor(R.color.black)));
              }
          } else
          {
              viewHolder.text.setTextColor(sharedPrefs.getInt("ct_nameTextColor", context.getResources().getColor(R.color.black)));
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
      
      if (MainActivity.settings.ctDarkContactPics)
      {
          holder.image.setImageResource(R.drawable.ic_contact_dark);
      } else {
          holder.image.setImageResource(R.drawable.ic_contact_picture);
      }

	  new Thread(new Runnable() {

		@Override
		public void run() {
            final String number = Conversation.findContactNumber(conversations.get(position).getNumber(), context);
			final Bitmap image = Bitmap.createScaledBitmap(getFacebookPhoto(number), MainActivity.contactWidth, MainActivity.contactWidth, true);

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

                    names = MainActivity.loadGroupContacts(number, context);
                } else
                {
                    String contactName = MainActivity.findContactName(number, context);

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
                    names = MainActivity.loadGroupContacts(number, context);
                } else
                {
                    text = Html.fromHtml(MainActivity.findContactName(number, context));
                }
            }

            final Spanned textF = text;
            final String namesF = names;

		  	context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

				@Override
				public void run() {
                    holder.image.assignContactFromPhone(number, true);

					if (MainActivity.settings.contactPictures2)
					{
                        if (!conversations.get(position).getGroup())
                        {
                            try
                              {
                                holder.image.setImageBitmap(Bitmap.createScaledBitmap(image, MainActivity.contactWidth, MainActivity.contactWidth, true));
                              } catch (Exception e)
                              {
                                if (MainActivity.settings.ctDarkContactPics)
                                {
                                    holder.image.setImageBitmap(Bitmap.createScaledBitmap(drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_contact_dark)), MainActivity.contactWidth, MainActivity.contactWidth, true));
                                } else
                                {
                                    holder.image.setImageBitmap(Bitmap.createScaledBitmap(drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_contact_picture)), MainActivity.contactWidth, MainActivity.contactWidth, true));
                                }
                              }
                        } else
                        {
                            if (MainActivity.settings.ctDarkContactPics)
                            {
                                holder.image.setImageBitmap(Bitmap.createScaledBitmap(drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_contact_dark)), MainActivity.contactWidth, MainActivity.contactWidth, true));
                            } else
                            {
                                holder.image.setImageBitmap(Bitmap.createScaledBitmap(drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_contact_picture)), MainActivity.contactWidth, MainActivity.contactWidth, true));
                            }
                        }
					} else
					{
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
      if (MainActivity.settings.smilies.equals("with"))
      {
          Matcher matcher = pattern.matcher(mBody);

          if (matcher.find())
          {
              if (MainActivity.settings.emojiType)
              {
                  if (MainActivity.settings.smiliesType) {
                      holder.text2.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter2New.getSmiledText(context, mBody)));
                  } else {
                      holder.text2.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter2.getSmiledText(context, mBody)));
                  }
              } else
              {
                  if (MainActivity.settings.smiliesType) {
                      holder.text2.setText(EmojiConverter.getSmiledText(context, EmoticonConverter2New.getSmiledText(context, mBody)));
                  } else {
                      holder.text2.setText(EmojiConverter.getSmiledText(context, EmoticonConverter2.getSmiledText(context, mBody)));
                  }
              }
          } else
          {
              if (MainActivity.settings.smiliesType) {
                  holder.text2.setText(EmoticonConverter2New.getSmiledText(context, mBody));
              } else {
                  holder.text2.setText(EmoticonConverter2.getSmiledText(context, mBody));
              }
          }
      } else if (MainActivity.settings.smilies.equals("without"))
      {
          String patternStr = "[^\\x20-\\x7E]";
          Pattern pattern = Pattern.compile(patternStr);
          Matcher matcher = pattern.matcher(mBody);

          if (matcher.find())
          {
              if (MainActivity.settings.emojiType)
              {
                  if (MainActivity.settings.smiliesType) {
                      holder.text2.setText(EmojiConverter2.getSmiledText(context, EmoticonConverterNew.getSmiledText(context, mBody)));
                  } else {
                      holder.text2.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter.getSmiledText(context, mBody)));
                  }
              } else
              {
                  if (MainActivity.settings.smiliesType) {
                      holder.text2.setText(EmojiConverter.getSmiledText(context, EmoticonConverterNew.getSmiledText(context, mBody)));
                  } else {
                      holder.text2.setText(EmojiConverter.getSmiledText(context, EmoticonConverter.getSmiledText(context, mBody)));
                  }
              }
          } else
          {
              if (MainActivity.settings.smiliesType) {
                  holder.text2.setText(EmoticonConverterNew.getSmiledText(context, mBody));
              } else {
                  holder.text2.setText(EmoticonConverter.getSmiledText(context, mBody));
              }
          }
      } else if (MainActivity.settings.smilies.equals("none"))
      {
          String patternStr = "[^\\x20-\\x7E]";
          Pattern pattern = Pattern.compile(patternStr);
          Matcher matcher = pattern.matcher(mBody);

          if (matcher.find())
          {
              if (MainActivity.settings.emojiType)
              {
                  holder.text2.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter2.getSmiledText(context, mBody)));
              } else
              {
                  holder.text2.setText(EmojiConverter.getSmiledText(context, EmoticonConverter2.getSmiledText(context, mBody)));
              }
          } else
          {
              holder.text2.setText(mBody);
          }
      } else if (MainActivity.settings.smilies.equals("both"))
      {
          String patternStr = "[^\\x20-\\x7E]";
          Pattern pattern = Pattern.compile(patternStr);
          Matcher matcher = pattern.matcher(mBody);

          if (matcher.find())
          {
              if (MainActivity.settings.emojiType)
              {
                  if (MainActivity.settings.smiliesType) {
                      holder.text2.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter3New.getSmiledText(context, mBody)));
                  } else {
                      holder.text2.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter3.getSmiledText(context, mBody)));
                  }
              } else
              {
                  if (MainActivity.settings.smiliesType) {
                      holder.text2.setText(EmojiConverter.getSmiledText(context, EmoticonConverter3New.getSmiledText(context, mBody)));
                  } else {
                      holder.text2.setText(EmojiConverter.getSmiledText(context, EmoticonConverter3.getSmiledText(context, mBody)));
                  }
              }
          } else
          {
              if (MainActivity.settings.smiliesType) {
                  holder.text2.setText(EmoticonConverter3New.getSmiledText(context, mBody));
              } else {
                  holder.text2.setText(EmoticonConverter3.getSmiledText(context, mBody));
              }
          }
      }

	  Date date2 = new Date(0);

	  try
	  {
		  date2 = new Date(conversations.get(position).getDate());
	  } catch (Exception e)
	  {

	  }

	  if (MainActivity.settings.hourFormat)
	  {
		  holder.text3.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2));
	  } else
	  {
		  holder.text3.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2));
	  }

	  holder.text4.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(date2));

      if (MainActivity.deviceType.equals("phablet") || MainActivity.deviceType.equals("tablet"))
      if (MainActivity.deviceType.equals("phablet") || MainActivity.deviceType.equals("tablet"))
      {
          holder.text3.setText("");
          holder.text4.setText("");
          holder.text.setMaxLines(1);
          holder.text2.setMaxLines(1);
      }

	  if (!conversations.get(position).getRead())
	  {
		  if (position != 0)
		  {
			  if (!MainActivity.settings.customBackground)
		        {
				  contactView.setBackgroundColor(MainActivity.settings.ctUnreadConversationColor);
		        }

			  conversations.get(position).setRead();
		  } else
		  {
			  if (!MainActivity.sentMessage)
			  {
				  if (!MainActivity.settings.customBackground)
			        {
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
                if (MainActivity.deviceType.equals("phone") || MainActivity.deviceType.equals("phablet2"))
                {
                    MainActivity.waitToLoad = true;
                }

		    	pager.setCurrentItem(position, false);
                conversations.get(position).setRead();
			    MainActivity.menu.showContent();

		        if (!MainActivity.settings.customBackground)
		        {
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

						ArrayList<String> newMessages = readFromFile(context);

				        for (int j = 0; j < newMessages.size(); j++)
				        {
				        	if (newMessages.get(j).replace("+", "").replace("+1", "").replace("-", "").equals(holder.text.getText().toString().replace("+", "").replace("+1", "").replace("-", "")))
				        	{
				        		newMessages.remove(j);
				        	}
				        }

				        writeToFile(newMessages, context);

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
				builder.setMessage(context.getResources().getString(R.string.delete_messages) + "\n\n" + context.getResources().getString(R.string.conversation) + ": " + MainActivity.findContactName(Conversation.findContactNumber(conversations.get(position).getNumber(), context), context));
				builder.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
			               progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			               progDialog.setMessage(context.getResources().getString(R.string.deleting));
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
				builder.setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
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
  
  	private ArrayList<String> readFromFile(Context context) {
		
      ArrayList<String> ret = new ArrayList<String>();
      
      try {
          InputStream inputStream = context.openFileInput(FILENAME);
          
          if ( inputStream != null ) {
          	InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
          	BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
          	String receiveString = "";
          	
          	while ( (receiveString = bufferedReader.readLine()) != null ) {
          		ret.add(receiveString);
          	}
          	
          	inputStream.close();
          }
      }
      catch (FileNotFoundException e) {
      	
		} catch (IOException e) {
			
		}

      return ret;
	}
  	
  	public Bitmap getFacebookPhoto(String phoneNumber) {
        try
        {
            Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Uri photoUri = null;
            ContentResolver cr = context.getContentResolver();
            Cursor contact = cr.query(phoneUri,
                    new String[] { ContactsContract.Contacts._ID }, null, null, null);

            try
            {
                if (contact.moveToFirst()) {
                    long userId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID));
                    photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);
                    contact.close();
                }
                else {
                    Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);

                    if (MainActivity.settings.ctDarkContactPics)
                    {
                        defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
                    }

                    contact.close();
                    return defaultPhoto;
                }
                if (photoUri != null) {
                    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
                            cr, photoUri);
                    if (input != null) {
                        contact.close();
                        return BitmapFactory.decodeStream(input);
                    }
                } else {
                    Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);

                    if (MainActivity.settings.ctDarkContactPics)
                    {
                        defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
                    }

                    contact.close();
                    return defaultPhoto;
                }
                Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);

                if (MainActivity.settings.ctDarkContactPics)
                {
                    defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
                }

                contact.close();
                return defaultPhoto;
            } catch (Exception e)
            {
                if (MainActivity.settings.ctDarkContactPics)
                {
                    contact.close();
                    return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
                } else
                {
                    contact.close();
                    return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
                }
            }
        } catch (Exception e)
        {
            if (MainActivity.settings.ctDarkContactPics)
            {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
            } else
            {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
            }
        }
	}
  
  public Bitmap drawableToBitmap (Drawable drawable) {
	    if (drawable instanceof BitmapDrawable) {
	        return ((BitmapDrawable)drawable).getBitmap();
	    }

	    try
	    {
		    int width = drawable.getIntrinsicWidth();
		    width = width > 0 ? width : 1;
		    int height = drawable.getIntrinsicHeight();
		    height = height > 0 ? height : 1;
	
		    Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		    Canvas canvas = new Canvas(bitmap); 
		    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		    drawable.draw(canvas);
		    return bitmap;
	    } catch (Exception e)
	    {
	    	return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
	    }
	}
  	
  	private void writeToFile(ArrayList<String> data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(FILENAME, Context.MODE_PRIVATE));
            
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