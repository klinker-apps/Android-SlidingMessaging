package com.klinker.android.messaging_card;

import com.klinker.android.messaging_card.group.GroupActivity;
import com.klinker.android.messaging_donate.R;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.klinker.android.messaging_sliding.emojis.*;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuArrayAdapter extends ArrayAdapter<String> {
  private final Activity context;
  private final ArrayList<String> body;
  private final ArrayList<String> numbers;
  private final ArrayList<String> threadIds;
  private final ArrayList<String> group;
  private final ArrayList<String> count;
  private final ArrayList<String> read;
  private static final String FILENAME = "newMessages.txt";
  private SharedPreferences sharedPrefs;
  private Resources resources;
  
  static class ViewHolder {
	    public TextView name;
	    public TextView messageCount;
	    public TextView number;
	    public TextView summary;
	    public ImageView image;
	    public ImageButton callButton;
	    public View background;
	    public View divider1;
	    public View divider2;
	  }

  public MenuArrayAdapter(Activity context, ArrayList<String> body, ArrayList<String> numbers, ArrayList<String> threadIds, ArrayList<String> group, ArrayList<String> count, ArrayList<String> read) {
    super(context, R.layout.contact_card_2, body);
    this.context = context;
    this.body = body;
    this.numbers = numbers;
    this.threadIds = threadIds;
    this.group = group;
    this.count = count;
    this.read = read;
    this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    this.resources = context.getResources();
  }
  
  @Override
  public int getCount()
  {
	return body.size();
  }

  @SuppressLint("SimpleDateFormat")
  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
	  View contactView = convertView;
	  
	  if (contactView == null)
	  {
		  LayoutInflater inflater = (LayoutInflater) context
			        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
		  if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
		  {
			  contactView = inflater.inflate(R.layout.contact_card_2, parent, false);
		  } else if (sharedPrefs.getString("card_theme", "Light").equals("Dark"))
		  {
			  contactView = inflater.inflate(R.layout.contact_card_2_dark, parent, false);
		  } else if (sharedPrefs.getString("card_theme", "Light").equals("Pitch Black"))
		  {
			  contactView = inflater.inflate(R.layout.contact_card_2_black, parent, false);
		  }
		  
		  ViewHolder viewHolder = new ViewHolder();
		  viewHolder.name = (TextView) contactView.findViewById(R.id.contactName);
		  viewHolder.messageCount = (TextView) contactView.findViewById(R.id.msgCount);
		  viewHolder.number = (TextView) contactView.findViewById(R.id.contactNumber);
		  viewHolder.summary = (TextView) contactView.findViewById(R.id.contactNumberType);
		  viewHolder.image = (ImageView) contactView.findViewById(R.id.contactPicture);
		  viewHolder.callButton = (ImageButton) contactView.findViewById(R.id.callButton);
		  viewHolder.background = (View) contactView.findViewById(R.id.view1);
		  viewHolder.divider1 = (View) contactView.findViewById(R.id.contactLine);
		  viewHolder.divider2 = (View) contactView.findViewById(R.id.contactLine2); 
		  
		  viewHolder.callButton.setVisibility(View.INVISIBLE);
		  viewHolder.divider2.setVisibility(View.GONE);
		  
		  if (sharedPrefs.getBoolean("custom_font", false))
		  {
			  viewHolder.name.setTypeface(MainActivity.font);
			  viewHolder.messageCount.setTypeface(MainActivity.font);
			  viewHolder.number.setTypeface(MainActivity.font);
			  viewHolder.summary.setTypeface(MainActivity.font);
		  }
		  
		  viewHolder.name.setTextSize((float)Integer.parseInt(sharedPrefs.getString("text_size2", 14 + "")));
		  viewHolder.number.setTextSize((float)(Integer.parseInt(sharedPrefs.getString("text_size2", 14 + "")) - 2));
		  viewHolder.summary.setTextSize((float)(Integer.parseInt(sharedPrefs.getString("text_size2", 14 + "")) - 2));
		  
		  viewHolder.name.setText("");
		  viewHolder.number.setText("");
		  viewHolder.summary.setText("");
		  
		  contactView.setTag(viewHolder);
	  }
	  
	  final ViewHolder holder = (ViewHolder) contactView.getTag();

      final String number = MainActivity.findContactNumber(numbers.get(position), context);
	  
	  if (group.get(position).equals("no"))
	  {
		  new Thread(new Runnable() {
	
			@Override
			public void run() {
				final Bitmap image = getFacebookPhoto(number);
				
				context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
	
					@Override
					public void run() {
						holder.image.setImageBitmap(image);
						
						holder.callButton.setVisibility(View.INVISIBLE);
					}
			    	
			    });
			}
			  
		  }).start();
	  } else
	  {
		  if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
		  {	
			  holder.callButton.setImageResource(R.drawable.list_group);
		  } else
		  {
			  holder.callButton.setImageResource(R.drawable.list_group_dark);
		  }
		  
		  holder.callButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(context, GroupActivity.class);
				intent.putExtra("names", MainActivity.loadGroupContacts(number, context));
				intent.putExtra("numbers", number);
				context.startActivity(intent);
				
			}
			  
		  });
	  }
	  
	  Locale sCachedLocale = Locale.getDefault();
	  int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
	  Editable editable = new SpannableStringBuilder(number);
	  PhoneNumberUtils.formatNumber(editable, sFormatType);
		
	  holder.number.setText(editable.toString());
		  
	  if (group.get(position).equals("yes"))
	  {
		  holder.name.setText("Group MMS");
		  
		  if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
		  {
			  holder.image.setImageResource(R.drawable.card_group);
		  } else
		  {
			  holder.image.setImageResource(R.drawable.card_group_dark);
		  }
		  
		  if (!sharedPrefs.getBoolean("display_contact_cards", false))
		  {
			  holder.callButton.setVisibility(View.VISIBLE);
		  }
		  
		  new Thread(new Runnable() {

			@Override
			public void run() {
				final String name = MainActivity.loadGroupContacts(number, context);
				
				context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

					@Override
					public void run() {
						holder.number.setText(name);
					}
			    	
			    });
				
				
				
			}
			  
		  }).start();
	  } else
	  {
		  new Thread(new Runnable() {

				@Override
				public void run() {
					final String name = MainActivity.findContactName(number, context);
					
					context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

						@Override
						public void run() {
							holder.name.setText(name);
							
							if (holder.name.getText().toString().trim().equals(holder.number.getText().toString().trim()))
							{
								  holder.number.setVisibility(View.INVISIBLE);
							} else
							{
								if (Integer.parseInt(sharedPrefs.getString("text_size2", 14 + "")) > 15 || sharedPrefs.getBoolean("hide_contact_number", false))
							    {
								    holder.number.setVisibility(View.GONE);
							    } else
							    {
							    	holder.number.setVisibility(View.VISIBLE);
							    }
							}
						}
				    	
				    });
					
					
					
				}
				  
			  }).start();
	  }
	  
	  if (!sharedPrefs.getBoolean("simple_cards", true))
	  {
		  if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
		  {
			  if (read.get(position).equals("0"))
			  {
				  if (position != 0)
				  {
					  holder.background.setBackgroundResource(R.drawable.rounded_rectangle_unread);
					  holder.divider1.setBackgroundColor(resources.getColor(R.color.card_conversation_summary));
					  holder.divider2.setBackgroundColor(resources.getColor(R.color.card_conversation_summary));
				  } else
				  {
					  if (MainActivity.sentMessage != true)
					  {
						  holder.background.setBackgroundResource(R.drawable.rounded_rectangle_unread);
						  holder.divider1.setBackgroundColor(resources.getColor(R.color.card_conversation_summary));
						  holder.divider2.setBackgroundColor(resources.getColor(R.color.card_conversation_summary));
					  }
				  }
			  } else
			  {
				  holder.background.setBackgroundResource(R.drawable.card_background);
				  holder.divider1.setBackgroundColor(resources.getColor(R.color.card_conversation_divider));
				  holder.divider2.setBackgroundColor(resources.getColor(R.color.card_conversation_divider));
			  }
		  } else if (sharedPrefs.getString("card_theme", "Light").equals("Dark"))
		  {
			  if (read.get(position).equals("0"))
			  {
				  if (position != 0)
				  {
					  holder.background.setBackgroundResource(R.drawable.rounded_rectangle_dark_unread);
					  holder.divider1.setBackgroundColor(resources.getColor(R.color.card_dark_conversation_summary));
					  holder.divider2.setBackgroundColor(resources.getColor(R.color.card_dark_conversation_summary));
				  } else
				  {
					  if (MainActivity.sentMessage != true)
					  {
						  holder.background.setBackgroundResource(R.drawable.rounded_rectangle_dark_unread);
						  holder.divider1.setBackgroundColor(resources.getColor(R.color.card_dark_conversation_summary));
						  holder.divider2.setBackgroundColor(resources.getColor(R.color.card_dark_conversation_summary));
					  }
				  }
			  } else
			  {
				  holder.background.setBackgroundResource(R.drawable.card_background_dark);
				  holder.divider1.setBackgroundColor(resources.getColor(R.color.card_dark_conversation_divider));
				  holder.divider2.setBackgroundColor(resources.getColor(R.color.card_dark_conversation_divider));
			  }
		  } else if (sharedPrefs.getString("card_theme", "Light").equals("Pitch Black"))
		  {
			  if (read.get(position).equals("0"))
			  {
				  if (position != 0)
				  {
					  holder.background.setBackgroundResource(R.drawable.rounded_rectangle_dark_unread);
					  holder.divider1.setBackgroundColor(resources.getColor(R.color.card_black_conversation_summary));
					  holder.divider2.setBackgroundColor(resources.getColor(R.color.card_black_conversation_summary));
				  } else
				  {
					  if (MainActivity.sentMessage != true)
					  {
						  holder.background.setBackgroundResource(R.drawable.rounded_rectangle_dark_unread);
						  holder.divider1.setBackgroundColor(resources.getColor(R.color.card_black_conversation_summary));
						  holder.divider2.setBackgroundColor(resources.getColor(R.color.card_black_conversation_summary));
					  }
				  }
			  } else
			  {
				  holder.background.setBackgroundResource(R.drawable.card_background_black);
				  holder.divider1.setBackgroundColor(resources.getColor(R.color.card_black_conversation_divider));
				  holder.divider2.setBackgroundColor(resources.getColor(R.color.card_black_conversation_divider));
			  }
		  }
	  } else
	  {
		  if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
		  {
			  if (read.get(position).equals("0"))
			  {
				  if (position != 0)
				  {
					  holder.background.setBackgroundColor(context.getResources().getColor(R.color.card_conversation_unread));
					  holder.divider1.setBackgroundColor(resources.getColor(R.color.card_conversation_summary));
					  holder.divider2.setBackgroundColor(resources.getColor(R.color.card_conversation_summary));
				  } else
				  {
					  if (MainActivity.sentMessage != true)
					  {
						  holder.background.setBackgroundColor(context.getResources().getColor(R.color.card_conversation_unread));
						  holder.divider1.setBackgroundColor(resources.getColor(R.color.card_conversation_summary));
						  holder.divider2.setBackgroundColor(resources.getColor(R.color.card_conversation_summary));
					  }
				  }
			  } else
			  {
				  holder.background.setBackgroundColor(context.getResources().getColor(R.color.white));
				  holder.divider1.setBackgroundColor(resources.getColor(R.color.card_conversation_divider));
				  holder.divider2.setBackgroundColor(resources.getColor(R.color.card_conversation_divider));
			  }
		  } else if (sharedPrefs.getString("card_theme", "Light").equals("Dark"))
		  {
			  if (read.get(position).equals("0"))
			  {
				  if (position != 0)
				  {
					  holder.background.setBackgroundColor(context.getResources().getColor(R.color.card_dark_conversation_unread));
					  holder.divider1.setBackgroundColor(resources.getColor(R.color.card_dark_conversation_summary));
					  holder.divider2.setBackgroundColor(resources.getColor(R.color.card_dark_conversation_summary));
				  } else
				  {
					  if (MainActivity.sentMessage != true)
					  {
						  holder.background.setBackgroundColor(context.getResources().getColor(R.color.card_dark_conversation_unread));
						  holder.divider1.setBackgroundColor(resources.getColor(R.color.card_dark_conversation_summary));
						  holder.divider2.setBackgroundColor(resources.getColor(R.color.card_dark_conversation_summary));
					  }
				  }
			  } else
			  {
				  holder.background.setBackgroundColor(context.getResources().getColor(R.color.card_dark_card_background));
				  holder.divider1.setBackgroundColor(resources.getColor(R.color.card_dark_conversation_divider));
				  holder.divider2.setBackgroundColor(resources.getColor(R.color.card_dark_conversation_divider));
			  }
		  } else if (sharedPrefs.getString("card_theme", "Light").equals("Pitch Black"))
		  {
			  if (read.get(position).equals("0"))
			  {
				  if (position != 0)
				  {
					  holder.background.setBackgroundColor(context.getResources().getColor(R.color.card_dark_conversation_unread));
					  holder.divider1.setBackgroundColor(resources.getColor(R.color.card_black_conversation_summary));
					  holder.divider2.setBackgroundColor(resources.getColor(R.color.card_black_conversation_summary));
				  } else
				  {
					  if (MainActivity.sentMessage != true)
					  {
						  holder.background.setBackgroundColor(context.getResources().getColor(R.color.card_dark_conversation_unread));
						  holder.divider1.setBackgroundColor(resources.getColor(R.color.card_black_conversation_summary));
						  holder.divider2.setBackgroundColor(resources.getColor(R.color.card_black_conversation_summary));
					  }
				  }
			  } else
			  {
				  holder.background.setBackgroundColor(context.getResources().getColor(R.color.card_black_card_background));
				  holder.divider1.setBackgroundColor(resources.getColor(R.color.card_black_conversation_divider));
				  holder.divider2.setBackgroundColor(resources.getColor(R.color.card_black_conversation_divider));
			  }
		  }
	  }
	  
	  if (!sharedPrefs.getBoolean("hide_message_counter", false))
	  {
		  holder.messageCount.setText(count.get(position));
	  } else
	  {
		  holder.messageCount.setText("");
	  }
	  
	  if (sharedPrefs.getString("smilies", "with").equals("with"))
	  {
		  String patternStr = "[^\\x20-\\x7E]";
		  Pattern pattern = Pattern.compile(patternStr);
		  Matcher matcher = pattern.matcher(body.get(position));
		  
		  if (matcher.find())
		  {
			  if (sharedPrefs.getBoolean("emoji_type", true))
			  {
                  if (sharedPrefs.getBoolean("smiliesType", true)) {
                      holder.summary.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter2New.getSmiledText(context, body.get(position))));
                  } else {
                      holder.summary.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter2.getSmiledText(context, body.get(position))));
                  }
			  } else
			  {
                  if (sharedPrefs.getBoolean("smiliesType", true)) {
                      holder.summary.setText(EmojiConverter.getSmiledText(context, EmoticonConverter2New.getSmiledText(context, body.get(position))));
                  } else {
                      holder.summary.setText(EmojiConverter.getSmiledText(context, EmoticonConverter2.getSmiledText(context, body.get(position))));
                  }
			  }
	      } else
		  {
              if (sharedPrefs.getBoolean("smiliesType", true)) {
                  holder.summary.setText(EmoticonConverter2New.getSmiledText(context, body.get(position)));
              } else {
                  holder.summary.setText(EmoticonConverter2.getSmiledText(context, body.get(position)));
              }
		  }
	  } else if (sharedPrefs.getString("smilies", "with").equals("without"))
	  {
		  String patternStr = "[^\\x20-\\x7E]";
		  Pattern pattern = Pattern.compile(patternStr);
		  Matcher matcher = pattern.matcher(body.get(position));
		  
		  if (matcher.find())
		  {
			  if (sharedPrefs.getBoolean("emoji_type", true))
			  {
                  if (sharedPrefs.getBoolean("smiliesType", true)) {
                      holder.summary.setText(EmojiConverter2.getSmiledText(context, EmoticonConverterNew.getSmiledText(context, body.get(position))));
                  } else {
                      holder.summary.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter.getSmiledText(context, body.get(position))));
                  }
			  } else
			  {
                  if (sharedPrefs.getBoolean("smiliesType", true)) {
                      holder.summary.setText(EmojiConverter.getSmiledText(context, EmoticonConverterNew.getSmiledText(context, body.get(position))));
                  } else {
                      holder.summary.setText(EmojiConverter.getSmiledText(context, EmoticonConverter.getSmiledText(context, body.get(position))));
                  }
			  }
		  } else
		  {
              if (sharedPrefs.getBoolean("smiliesType", true)) {
                  holder.summary.setText(EmoticonConverterNew.getSmiledText(context, body.get(position)));
              } else {
                  holder.summary.setText(EmoticonConverter.getSmiledText(context, body.get(position)));
              }
		  }
	  } else if (sharedPrefs.getString("smilies", "with").equals("none"))
	  {
		  String patternStr = "[^\\x20-\\x7E]";
		  Pattern pattern = Pattern.compile(patternStr);
		  Matcher matcher = pattern.matcher(body.get(position));
		  
		  if (matcher.find())
		  {
			  if (sharedPrefs.getBoolean("emoji_type", true))
			  {
				  holder.summary.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter2.getSmiledText(context, body.get(position))));
			  } else
			  {
				  holder.summary.setText(EmojiConverter.getSmiledText(context, EmoticonConverter2.getSmiledText(context, body.get(position))));
			  }
		  } else
		  {
			  holder.summary.setText(body.get(position));
		  }
	  } else if (sharedPrefs.getString("smilies", "with").equals("both"))
	  {
		  String patternStr = "[^\\x20-\\x7E]";
		  Pattern pattern = Pattern.compile(patternStr);
		  Matcher matcher = pattern.matcher(body.get(position));
		  
		  if (matcher.find())
		  {
              if (sharedPrefs.getBoolean("emoji_type", true))
              {
                  if (sharedPrefs.getBoolean("smiliesType", true)) {
                      holder.summary.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter3New.getSmiledText(context, body.get(position))));
                  } else {
                      holder.summary.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter3.getSmiledText(context, body.get(position))));
                  }
              } else
              {
                  if (sharedPrefs.getBoolean("smiliesType", true)) {
                      holder.summary.setText(EmojiConverter.getSmiledText(context, EmoticonConverter3New.getSmiledText(context, body.get(position))));
                  } else {
                      holder.summary.setText(EmojiConverter.getSmiledText(context, EmoticonConverter3.getSmiledText(context, body.get(position))));
                  }
              }
	      } else
		  {
              if (sharedPrefs.getBoolean("smiliesType", true)) {
                  holder.summary.setText(EmoticonConverter3New.getSmiledText(context, body.get(position)));
              } else {
                  holder.summary.setText(EmoticonConverter3.getSmiledText(context, body.get(position)));
              }
		  }
	  }
	  
	  contactView.setOnClickListener(new View.OnClickListener() 
		{
		    public void onClick(View v) {
		    	MainActivity.isFastScrolling = true;
				MainActivity.scrollTo = position + 1;
		    	MainActivity.messagePager.setCurrentItem(position + 1, true);
			    read.set(position, "1");
			    
			    if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
			    {
			    	holder.background.setBackgroundResource(R.drawable.card_background);
			    } else if (sharedPrefs.getString("card_theme", "Light").equals("Dark"))
			    {
			    	holder.background.setBackgroundResource(R.drawable.card_background_dark);
			    } else if (sharedPrefs.getString("card_theme", "Light").equals("Pitch Black"))
			    {
			    	holder.background.setBackgroundResource(R.drawable.card_background_black);
			    }
		        
			    new Thread(new Runnable() {

					@Override
					public void run() {
						
						try
						{
							ContentValues values = new ContentValues();
			               	values.put("read", true);
			               	context.getContentResolver().update(Uri.parse("content://sms/conversations/"), values, "thread_id=?", new String[] {threadIds.get(position)});
			               	context.getContentResolver().update(Uri.parse("content://mms/conversations/"), values, "thread_id=?", new String[] {threadIds.get(position)});
						} catch (Exception e)
						{
							e.printStackTrace();
						}
		               	
						ArrayList<String> newMessages = readFromFile(context);
				        
				        for (int j = 0; j < newMessages.size(); j++)
				        {
				        	if (newMessages.get(j).replace("+", "").replace("+1", "").replace("-", "").equals(holder.name.getText().toString().replace("+", "").replace("+1", "").replace("-", "")))
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
		        
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage(resources.getString(R.string.delete_messages) + "\n\n" + resources.getString(R.string.conversation) + ": " + MainActivity.loadGroupContacts(number, context));
				builder.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
			               final ProgressDialog progDialog = new ProgressDialog(context);
			               progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			               progDialog.setMessage(resources.getString(R.string.deleting));
			               progDialog.show();
			               
			               new Thread(new Runnable(){

								@Override
								public void run() {
									deleteSMS(context, threadIds.get(position));
									
									context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

										@Override
										public void run() {
											((MainActivity)context).refreshViewPager(true);
											progDialog.dismiss();
											
											Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
											context.sendBroadcast(updateWidget);
										}
								    	
								    });
								}
			            	   
			               }).start();
			           }
				
				
				public void deleteSMS(Context context, String id) {
				    try {
				        context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/"), "thread_id=?", new String[]{id});
				    } catch (Exception e) {
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
	  
	  contactView.setId(10 + position);

	  return contactView;
  }
  
  public InputStream openDisplayPhoto(long contactId) {
	  Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
	     Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
	     Cursor cursor = context.getContentResolver().query(photoUri,
	          new String[] {Contacts.Photo.PHOTO}, null, null, null);
	     if (cursor == null) {
	         return null;
	     }
	     try {
	         if (cursor.moveToFirst()) {
	             byte[] data = cursor.getBlob(0);
	             if (data != null) {
	                 return new ByteArrayInputStream(data);
	             }
	         }
	     } finally {
	         cursor.close();
	     }
	     return null;
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
	
		    }
		    else {
		    	Bitmap defaultPhoto;
		    	
		    	if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
				{
		    		defaultPhoto = BitmapFactory.decodeResource(resources, R.drawable.card_person);
				} else
				{
					defaultPhoto = BitmapFactory.decodeResource(resources, R.drawable.card_person_dark);
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
		    	Bitmap defaultPhoto;
		    	
		    	if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
				{
		    		defaultPhoto = BitmapFactory.decodeResource(resources, R.drawable.card_person);
				} else
				{
					defaultPhoto = BitmapFactory.decodeResource(resources, R.drawable.card_person_dark);
				}
		    	
		    	contact.close();
		        return defaultPhoto;
		    }
		    
		    Bitmap defaultPhoto;
	    	
	    	if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
			{
	    		defaultPhoto = BitmapFactory.decodeResource(resources, R.drawable.card_person);
			} else
			{
				defaultPhoto = BitmapFactory.decodeResource(resources, R.drawable.card_person_dark);
			}
	    	
	    	contact.close();
		    return defaultPhoto;
	    } catch (Exception e)
	    {
	    	Bitmap defaultPhoto;
	    	
	    	if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
			{
	    		defaultPhoto = BitmapFactory.decodeResource(resources, R.drawable.card_person);
			} else
			{
				defaultPhoto = BitmapFactory.decodeResource(resources, R.drawable.card_person_dark);
			}
	    	
	    	contact.close();
	    	return defaultPhoto;
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
	    	return BitmapFactory.decodeResource(resources, R.drawable.ic_contact_picture);
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