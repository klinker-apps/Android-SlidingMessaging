package com.klinker.android.messaging_sliding.quick_reply;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.SendUtil;
import com.klinker.android.messaging_donate.settings.AppSettings;
import com.klinker.android.messaging_sliding.ContactSearchArrayAdapter2;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter2;
import com.klinker.android.messaging_sliding.emojis.EmojiConverter;
import com.klinker.android.messaging_sliding.emojis.EmojiConverter2;
import com.klinker.android.messaging_sliding.receivers.CacheService;
import com.klinker.android.send_message.Transaction;
import com.klinker.android.send_message.Utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendMessage extends Activity {
	
	public ArrayList<String> contactNames, contactNumbers, contactTypes;
	public boolean firstContactSearch = true;
	public String inputText;

    public String runAs;
    public int ctConversationListBackground;
    public int ctSendButtonColor;
    public int ctSendBarBackground;
    public int emojiButtonColor;
    public int draftTextColor;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setFinishOnTouchOutside(false);
		
		Intent intent = getIntent();
		String action = intent.getAction();
		inputText = "";
		
		if (Intent.ACTION_SEND.equals(action))
		{
			try
			{
				inputText = intent.getCharSequenceExtra(Intent.EXTRA_TEXT).toString();
			} catch (Exception e)
			{
				
			}
		}
		
		final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        runAs = sharedPrefs.getString("run_as", "sliding");
        ctConversationListBackground = sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver));
        ctSendButtonColor = sharedPrefs.getInt("ct_sendButtonColor", getResources().getColor(R.color.black));
        ctSendBarBackground = sharedPrefs.getInt("ct_sendbarBackground", getResources().getColor(R.color.white));
        emojiButtonColor = sharedPrefs.getInt("ct_emojiButtonColor", getResources().getColor(R.color.emoji_button));
        draftTextColor = sharedPrefs.getInt("ct_draftTextColor", ctSendButtonColor);
		
		LayoutInflater inflater2 = (LayoutInflater) this
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View newMessageView = inflater2.inflate(R.layout.send_to, (ViewGroup) this.getWindow().getDecorView(), false);
		
		if (sharedPrefs.getBoolean("dark_theme_quick_reply", true))
		{
			if (sharedPrefs.getBoolean("pitch_black_theme", false))
			{
				newMessageView.setBackgroundColor(getResources().getColor(R.color.black));
			} else
			{
				newMessageView.setBackgroundColor(getResources().getColor(R.color.dark_silver));
			}
		} else
		{
			newMessageView.setBackgroundColor(getResources().getColor(R.color.light_silver));
		}
		
		final TextView mTextView = (TextView) newMessageView.findViewById(R.id.charsRemaining2);
		final EditText mEditText = (EditText) newMessageView.findViewById(R.id.messageEntry2);
		
		mEditText.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	        }

	        public void onTextChanged(CharSequence s, int start, int before, int count) {
	        	int length = Integer.parseInt(String.valueOf(s.length()));
	        	
	        	if (!sharedPrefs.getString("signature", "").equals(""))
	        	{
	        		length += ("\n" + sharedPrefs.getString("signature", "")).length();
	        	}
	        	
	        	String patternStr = "[^" + Utils.GSM_CHARACTERS_REGEX + "]";
				Pattern pattern = Pattern.compile(patternStr);
				Matcher matcher = pattern.matcher(s);
				
				int size = 160;
				
				if (matcher.find() && !sharedPrefs.getBoolean("strip_unicode", false))
				{
					size = 70;
				}
	        	
	        	int pages = 1;
	        	
	        	while (length > size)
	        	{
	        		length-=size;
	        		pages++;
	        	}
	        	
	            mTextView.setText(pages + "/" + (size - length));
	        }

	        public void afterTextChanged(Editable s) {
	        }
		});
		
		mEditText.setText(inputText);
		
		if (!sharedPrefs.getBoolean("keyboard_type", true))
		{
			mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			mEditText.setImeOptions(EditorInfo.IME_ACTION_NONE);
		}
		
		final Context context = this;
		final EditText contact = (EditText) newMessageView.findViewById(R.id.contactEntry);

		contact.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	        	if (firstContactSearch)
	        	{
	        		try
	        		{
	        			contactNames = new ArrayList<String>();
	        			contactNumbers = new ArrayList<String>();
	        			contactTypes = new ArrayList<String>();
	        			
	        			Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
	        			String[] projection    = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
	        			                ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.LABEL};
	        	
	        			Cursor people = getContentResolver().query(uri, projection, null, null, null);
	        	
	        			int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
	        			int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
	        	
	        			people.moveToFirst();
	        			do {
	        				int type = people.getInt(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
	        				String customLabel = people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));
	        				
	        				if (sharedPrefs.getBoolean("mobile_only", false))
	        				{
	        					if (type == 2)
	        					{
	        						contactNames.add(people.getString(indexName));
	    	        				contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
	        						contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel).toString());
	        					}
	        				} else
	        				{
	        					contactNames.add(people.getString(indexName));
		        				contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
	        					contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel).toString());
	        				}
	        			} while (people.moveToNext());
	        			people.close();
	        		} catch (IllegalArgumentException e)
	        		{
	        			
	        		}
	        		
	        		firstContactSearch = false;
	        	}
	        }

	        @SuppressLint("DefaultLocale")
			public void onTextChanged(CharSequence s, int start, int before, int count) {
	        	ArrayList<String> searchedNames = new ArrayList<String>();
	        	ArrayList<String> searchedNumbers = new ArrayList<String>();
	        	ArrayList<String> searchedTypes = new ArrayList<String>();
	        	
	        	String text = contact.getText().toString();
	        	
	        	String[] text2 = text.split("; ");
	        	
	        	text = text2[text2.length-1].trim();
	        	
	        	try
	        	{
	        		if (text.substring(0,1).equals("+"))
	        		{
	        			text = "\\" + text;
	        		}
	        	} catch (Exception e)
	        	{
	        		
	        	}
	        	
	  		    Pattern pattern;

                try
                {
                    pattern = Pattern.compile(text.toLowerCase());
                } catch (Exception e)
                {
                    pattern = Pattern.compile(text.toLowerCase().replace("(", "").replace(")", "").replace("?", "").replace("[", "").replace("{", "").replace("}", "").replace("\\", ""));
                }
	        	
			    for (int i = 0; i < contactNames.size(); i++)
			    {
			    	try
			    	{
			    		Long.parseLong(text);
			    		
				        if (text.length() <= contactNumbers.get(i).length())
				        {
				        	Matcher matcher = pattern.matcher(contactNumbers.get(i));
					        if(matcher.find())
					        {
					        	searchedNames.add(contactNames.get(i));
					        	searchedNumbers.add(contactNumbers.get(i));
					        	searchedTypes.add(contactTypes.get(i));
					        }
				        }
			    	} catch (Exception e)
			    	{
			    		if (text.length() <= contactNames.get(i).length())
				        {
			    			Matcher matcher = pattern.matcher(contactNames.get(i).toLowerCase());
					        if(matcher.find())
					        {
					        	searchedNames.add(contactNames.get(i));
					        	searchedNumbers.add(contactNumbers.get(i));
					        	searchedTypes.add(contactTypes.get(i));
					        }
				        }
			    	}
			    }
	        	
		        ListView searchView = (ListView) newMessageView.findViewById(R.id.contactSearch);
		        ContactSearchArrayAdapter2 adapter;
		        
		        if (sharedPrefs.getBoolean("dark_theme_quick_reply", true))
		        {
		        	searchView.setBackgroundColor(getResources().getColor(R.color.dark_silver));
		        } else
		        {
		        	searchView.setBackgroundColor(getResources().getColor(R.color.light_silver));
		        }
		        
		        if (text.length() != 0)
		        {
	        		adapter = new ContactSearchArrayAdapter2((Activity)context, searchedNames, searchedNumbers, searchedTypes);
		        } else
		        {
	        		adapter = new ContactSearchArrayAdapter2((Activity)context, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
		        }
	        	
	        	searchView.setAdapter(adapter);
	        	
	        	searchView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						TextView view2 = (TextView) arg1.findViewById(R.id.receivedMessage);
						
						String[] t1 = contact.getText().toString().split("; ");
						String string = "";
						
						for (int i = 0; i < t1.length - 1; i++)
						{
							string += t1[i] + "; ";
						}
						
						contact.setText(string + view2.getText() + "; ");
						contact.setSelection(contact.getText().toString().length());
						
					}
	        		
	        	});
	        }

	        public void afterTextChanged(Editable s) {
	        }
		});
		
		ImageButton sendButton = (ImageButton) newMessageView.findViewById(R.id.sendButton);
		sendButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (contact.getText().toString().equals(""))
				{
					Toast.makeText(context, "ERROR: No valid recipients", Toast.LENGTH_SHORT).show();
				} else if (mEditText.getText().toString().equals(""))
				{
					Toast.makeText(context, "ERROR: Nothing to send", Toast.LENGTH_SHORT).show();
				} else
				{
					final String[] contacts = contact.getText().toString().split("; ");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SendUtil.sendMessage(context, contacts, mEditText.getText().toString());
                        }
                    }).start();

                    if (sharedPrefs.getBoolean("cache_conversations", false)) {
                        Intent cacheService = new Intent(context, CacheService.class);
                        context.startService(cacheService);
                    }

                    if (sharedPrefs.getBoolean("voice_enabled", false)) {
                        registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                unregisterReceiver(this);
                                finish();
                            }
                        }, new IntentFilter(Transaction.REFRESH));
                    } else {
                        finish();
                    }
				}
			}
			
		});
		
		ImageButton emojiButton = (ImageButton) newMessageView.findViewById(R.id.display_emoji);
		
		if (!sharedPrefs.getBoolean("emoji", false))
		{
			emojiButton.setVisibility(View.GONE);
			LayoutParams params = (RelativeLayout.LayoutParams)mEditText.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			mEditText.setLayoutParams(params);
		} else
		{
			emojiButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("Insert Emojis");
					LayoutInflater inflater = ((Activity) context).getLayoutInflater();
					View frame = inflater.inflate(R.layout.emoji_frame, null);
					
					final EditText editText = (EditText) frame.findViewById(R.id.emoji_text);
                    ImageButton peopleButton = (ImageButton) frame.findViewById(R.id.peopleButton);
                    ImageButton objectsButton = (ImageButton) frame.findViewById(R.id.objectsButton);
                    ImageButton natureButton = (ImageButton) frame.findViewById(R.id.natureButton);
                    ImageButton placesButton = (ImageButton) frame.findViewById(R.id.placesButton);
                    ImageButton symbolsButton = (ImageButton) frame.findViewById(R.id.symbolsButton);
					
					final GridView emojiGrid = (GridView) frame.findViewById(R.id.emojiGrid);
					Button okButton = (Button) frame.findViewById(R.id.emoji_ok);
					
					if (sharedPrefs.getBoolean("emoji_type", true))
					{
						emojiGrid.setAdapter(new EmojiAdapter2(context));
						emojiGrid.setOnItemClickListener(new OnItemClickListener() {
						
								public void onItemClick(AdapterView<?> parent, View v, int position, long id)
								{
									editText.setText(EmojiConverter2.getSmiledText(context, editText.getText().toString() + EmojiAdapter2.mEmojiTexts[position]));
									editText.setSelection(editText.getText().length());
								}
						});

                        peopleButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(0);
                            }
                        });

                        objectsButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + (2 * 7));
                            }
                        });

                        natureButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + (3 * 7));
                            }
                        });

                        placesButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + 178 + (5 * 7));
                            }
                        });

                        symbolsButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + 178 + 122 + (7 * 7));
                            }
                        });
					} else
					{
						emojiGrid.setAdapter(new EmojiAdapter(context));
						emojiGrid.setOnItemClickListener(new OnItemClickListener() {
						
								public void onItemClick(AdapterView<?> parent, View v, int position, long id)
								{
									editText.setText(EmojiConverter.getSmiledText(context, editText.getText().toString() + EmojiAdapter.mEmojiTexts[position]));
									editText.setSelection(editText.getText().length());
								}
						});

                        peopleButton.setMaxHeight(0);
                        objectsButton.setMaxHeight(0);
                        natureButton.setMaxHeight(0);
                        placesButton.setMaxHeight(0);
                        symbolsButton.setMaxHeight(0);

                        LinearLayout buttons = (LinearLayout) frame.findViewById(R.id.linearLayout);
                        buttons.setMinimumHeight(0);
                        buttons.setVisibility(View.GONE);
					}
					
					builder.setView(frame);
					final AlertDialog dialog = builder.create();
					dialog.show();
					
					okButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (sharedPrefs.getBoolean("emoji_type", true))
							{
								mEditText.setText(EmojiConverter2.getSmiledText(context, mEditText.getText().toString() + editText.getText().toString()));
								mEditText.setSelection(mEditText.getText().length());
							} else
							{
								mEditText.setText(EmojiConverter.getSmiledText(context, mEditText.getText().toString() + editText.getText().toString()));
								mEditText.setSelection(mEditText.getText().length());
							}
							
							dialog.dismiss();
						}
						
					});
				}
				
			});
		}

        final AppSettings settings = AppSettings.init(this);
        final ImageButton voiceButton = (ImageButton) newMessageView.findViewById(R.id.voiceButton);

        if (settings.voiceAccount != null) {
            if (settings.voiceEnabled) {
                voiceButton.setImageResource(R.drawable.voice_enabled);
            } else {
                voiceButton.setImageResource(R.drawable.voice_disabled);
            }

            voiceButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (settings.voiceEnabled) {
                        settings.voiceEnabled = false;
                        sharedPrefs.edit().putBoolean("voice_enabled", false).commit();
                        voiceButton.setImageResource(R.drawable.voice_disabled);
                    } else {
                        settings.voiceEnabled = true;
                        sharedPrefs.edit().putBoolean("voice_enabled", true).commit();
                        voiceButton.setImageResource(R.drawable.voice_enabled);
                    }
                }
            });
        } else {
            voiceButton.setVisibility(View.GONE);
        }
		
		ListView searchView = (ListView) newMessageView.findViewById(R.id.contactSearch);

        try {
            mEditText.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,2)));
        } catch (Exception e) {
            mEditText.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,1)));
        }
		
		View v1 = newMessageView.findViewById(R.id.view1);
		View v2 = newMessageView.findViewById(R.id.sentBackground);

        mTextView.setTextColor(draftTextColor);
        v1.setBackgroundColor(ctSendBarBackground);
        v2.setBackgroundColor(ctSendBarBackground);
        sendButton.setBackgroundResource(R.drawable.pitch_black_send_button);
        sendButton.setImageResource(R.drawable.ic_action_send_white);
        sendButton.setColorFilter(ctSendButtonColor);
        searchView.setBackgroundColor(ctConversationListBackground);
        emojiButton.setBackgroundResource(R.drawable.pitch_black_send_button);
        emojiButton.setColorFilter(emojiButtonColor);
        voiceButton.setBackgroundResource(R.drawable.pitch_black_send_button);
        voiceButton.setColorFilter(emojiButtonColor);
        mEditText.setTextColor(draftTextColor);
        contact.setTextColor(draftTextColor);

        if (runAs.equals("hangout") || runAs.equals("card2") || runAs.equals("speed"))
        {
            emojiButton.setImageResource(R.drawable.ic_emoji_dark);
        }
		
		if (sharedPrefs.getBoolean("custom_font", false))
		{
			mTextView.setTypeface(Typeface.createFromFile(sharedPrefs.getString("custom_font_path", "")));
			mEditText.setTypeface(Typeface.createFromFile(sharedPrefs.getString("custom_font_path", "")));
			contact.setTypeface(Typeface.createFromFile(sharedPrefs.getString("custom_font_path", "")));
		}
		
		setContentView(newMessageView);
	}
}
