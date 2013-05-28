package com.klinker.android.messaging_sliding.theme;

import java.util.ArrayList;

import com.klinker.android.messaging_donate.R;

import android.content.Context;
import android.graphics.Color;

public class CustomTheme {
	public String name;
	public boolean custom;
	public int titleBarColor;
	public int titleBarTextColor;
	public int messageListBackground;
	public int sendbarBackground;
	public int sentMessageBackground;
	public int receivedMessageBackground;
	public int sentTextColor;
	public int receivedTextColor;
	public int conversationListBackground;
	public int nameTextColor;
	public int summaryTextColor;
	public boolean messageDividerVisibility;
	public int messageDividerColor;
	public int sendButtonColor;
	public boolean darkContactImage;
	public String messageCounterColor;
	public int draftTextColor;
	public int emojiButtonColor;
	public int conversationDividerColor;
    public int unreadConversationColor;
	
	public CustomTheme(String name, Context context)
	{
		if (name.equals("White"))
		{
			this.name = "Light Theme";
			custom = false;
			titleBarColor = getColor(context, R.color.holo_blue);
			titleBarTextColor = getColor(context, R.color.white);
			messageListBackground = getColor(context, R.color.light_silver);
			sendbarBackground = getColor(context, R.color.white);
			sentMessageBackground = getColor(context, R.color.white);
			receivedMessageBackground = getColor(context, R.color.white);
			sentTextColor = getColor(context, R.color.black);
			receivedTextColor = getColor(context, R.color.black);
			conversationListBackground = getColor(context, R.color.light_silver);
			nameTextColor = getColor(context, R.color.black);
			summaryTextColor = getColor(context, R.color.black);
			messageDividerVisibility = true;
			messageDividerColor = getColor(context, R.color.light_divider);
			sendButtonColor = getColor(context, R.color.black);
			darkContactImage = false;
			messageCounterColor = "#C8C8C8";
			draftTextColor = sendButtonColor;
			emojiButtonColor = convertToColorInt("ff8dbc36");
			conversationDividerColor = convertToColorInt("22ffffff");
            unreadConversationColor = receivedMessageBackground;
		} else if (name.equals("Dark"))
		{
			this.name = "Dark Theme";
			custom = false;
			titleBarColor = getColor(context, R.color.holo_blue);
			titleBarTextColor = getColor(context, R.color.white);
			messageListBackground = getColor(context, R.color.dark_silver);
			sendbarBackground = getColor(context, R.color.black);
			sentMessageBackground = getColor(context, R.color.black);
			receivedMessageBackground = getColor(context, R.color.black);
			sentTextColor = getColor(context, R.color.white);
			receivedTextColor = getColor(context, R.color.white);
			conversationListBackground = getColor(context, R.color.dark_silver);
			nameTextColor = getColor(context, R.color.white);
			summaryTextColor = getColor(context, R.color.white);
			messageDividerVisibility = true;
			messageDividerColor = getColor(context, R.color.dark_divider);
			sendButtonColor = getColor(context, R.color.white);
			darkContactImage = false;
			messageCounterColor = "#808080";
			draftTextColor = sendButtonColor;
			emojiButtonColor = convertToColorInt("ff8dbc36");
			conversationDividerColor = convertToColorInt("22ffffff");
            unreadConversationColor = receivedMessageBackground;
		} else if (name.equals("Pitch Black"))
		{
			this.name = "Pitch Black Theme";
			custom = false;
			titleBarColor = getColor(context, R.color.holo_blue);
			titleBarTextColor = getColor(context, R.color.white);
			messageListBackground = getColor(context, R.color.black);
			sendbarBackground = getColor(context, R.color.pitch_black);
			sentMessageBackground = getColor(context, R.color.pitch_black);
			receivedMessageBackground = getColor(context, R.color.pitch_black);
			sentTextColor = getColor(context, R.color.white);
			receivedTextColor = getColor(context, R.color.white);
			conversationListBackground = getColor(context, R.color.pitch_black);
			nameTextColor = getColor(context, R.color.white);
			summaryTextColor = getColor(context, R.color.white);
			messageDividerVisibility = true;
			messageDividerColor = getColor(context, R.color.black);
			sendButtonColor = getColor(context, R.color.white);
			darkContactImage = true;
			messageCounterColor = "#808080";
			draftTextColor = sendButtonColor;
			emojiButtonColor = convertToColorInt("#8DBC36");
			conversationDividerColor = convertToColorInt("22ffffff");
            unreadConversationColor = receivedMessageBackground;
		} else if (name.equals("Dark Blue"))
		{
			this.name = "Dark Blue Theme";
			custom = true;
			titleBarColor = -14851976;
			titleBarTextColor = -3092272;
			messageListBackground = -15980494;
			sendbarBackground = -14783352;
			sentMessageBackground = -14540254;
			receivedMessageBackground = -14540254;
			sentTextColor = -1;
			receivedTextColor = -1;
			conversationListBackground = -16112338;
			nameTextColor = -1;
			summaryTextColor = -1;
			messageDividerVisibility = true;
			messageDividerColor = -13019810;
			sendButtonColor = -5187611;
			darkContactImage = true;
			messageCounterColor = convertToARGB(-5843482);
			draftTextColor = sendButtonColor;
			emojiButtonColor = convertToColorInt("#8DBC36");
			conversationDividerColor = convertToColorInt("22ffffff");
            unreadConversationColor = receivedMessageBackground;
		} else if (name.equals("Burnt Orange"))
		{
			this.name = "Burnt Orange Theme";
			custom = true;
			titleBarColor = -30720;
			titleBarTextColor = -1;
			messageListBackground = -3971072;
			sendbarBackground = -14935012;
			sentMessageBackground = -3487030;
			receivedMessageBackground = -2434342;
			sentTextColor = -14540254;
			receivedTextColor = -14540254;
			conversationListBackground = -4562176;
			nameTextColor = -13158601;
			summaryTextColor = -12763843;
			messageDividerVisibility = true;
			messageDividerColor = -30720;
			sendButtonColor = -1907998;
			darkContactImage = false;
			messageCounterColor = convertToARGB(-10399168);
			draftTextColor = sendButtonColor;
			emojiButtonColor = convertToColorInt("#8DBC36");
			conversationDividerColor = convertToColorInt("22ffffff");
            unreadConversationColor = receivedMessageBackground;
		} else if (name.equals("Light Green"))
		{
			this.name = "Light Green Theme";
			custom = true;
			titleBarColor = -6697984;
			titleBarTextColor = -1;
			messageListBackground = -1513240;
			sendbarBackground = -1;
			sentMessageBackground = -4666500;
			receivedMessageBackground = -4206957;
			sentTextColor = -14540254;
			receivedTextColor = -14540254;
			conversationListBackground = -1513240;
			nameTextColor = -14540254;
			summaryTextColor = -14540254;
			messageDividerVisibility = false;
			messageDividerColor = -1513240;
			sendButtonColor = -14540254;
			darkContactImage = false;
			messageCounterColor = convertToARGB(-3879766);
			draftTextColor = sendButtonColor;
			emojiButtonColor = convertToColorInt("#8DBC36");
			conversationDividerColor = convertToColorInt("22ffffff");
            unreadConversationColor = receivedMessageBackground;
		} else if (name.equals("Holo Purple"))
		{
			this.name = "Holo Purple Theme";
			custom = true;
			titleBarColor = -8697196;
			titleBarTextColor = -1;
			messageListBackground = -12178605;
			sendbarBackground = -16777216;
			sentMessageBackground = -16777216;
			receivedMessageBackground = -16777216;
			sentTextColor = -4349238;
			receivedTextColor = -4547638;
			conversationListBackground = -16777216;
			nameTextColor = -4414519;
			summaryTextColor = -1;
			messageDividerVisibility = true;
			messageDividerColor = -14540254;
			sendButtonColor = -4152115;
			darkContactImage = true;
			messageCounterColor = convertToARGB(-9149061);
			draftTextColor = sendButtonColor;
			emojiButtonColor = convertToColorInt("#8DBC36");
			conversationDividerColor = convertToColorInt("22ffffff");
            unreadConversationColor = receivedMessageBackground;
		} else if (name.equals("Bright Red"))
		{
			this.name = "Bright Red Theme";
			custom = true;
			titleBarColor = -3407872;
			titleBarTextColor = -1;
			messageListBackground = -13027015;
			sendbarBackground = -3407872;
			sentMessageBackground = -2022364;
			receivedMessageBackground = -2481373;
			sentTextColor = -1;
			receivedTextColor = -1;
			conversationListBackground = -12500671;
			nameTextColor = -1;
			summaryTextColor = -1;
			messageDividerVisibility = true;
			messageDividerColor = -3407872;
			sendButtonColor = -15329770;
			darkContactImage = false;
			messageCounterColor = convertToARGB(-3305842);
			draftTextColor = sendButtonColor;
			emojiButtonColor = -13027015;
			conversationDividerColor = convertToColorInt("22ffffff");
            unreadConversationColor = receivedMessageBackground;
		} else if (name.equals("Hangouts"))
        {
            this.name = "Hangouts Theme";
            custom = true;
            titleBarColor = convertToColorInt("cecece");
            titleBarTextColor = convertToColorInt("5c5c5c");
            messageListBackground = convertToColorInt("e5e5e5");
            sendbarBackground = convertToColorInt("ffffff");
            sentMessageBackground = convertToColorInt("ffffff");
            receivedMessageBackground = convertToColorInt("ffffff");
            sentTextColor = convertToColorInt("333333");
            receivedTextColor = convertToColorInt("333333");
            conversationListBackground = convertToColorInt("e5e5e5");
            nameTextColor = convertToColorInt("5c5c5c");
            summaryTextColor = convertToColorInt("5c5c5c");
            messageDividerVisibility = false;
            messageDividerColor = convertToColorInt("e5e5e5");
            sendButtonColor = convertToColorInt("adadad");
            darkContactImage = false;
            messageCounterColor = "FFadadad";
            draftTextColor = convertToColorInt("333333");
            emojiButtonColor = convertToColorInt("ff8dbc36");
            conversationDividerColor = convertToColorInt("adadad");
            unreadConversationColor = receivedMessageBackground;
        }
	}
	
	public CustomTheme(String name,
					   int titleBarColor,
			           int titleBarTextColor,
			           int messageListBackground,
			           int sendbarBackground,
			           int sentMessageBackground,
			           int receivedMessageBackground,
			           int sentTextColor,
			           int receivedTextColor,
			           int conversationListBackground,
			           int nameTextColor,
			           int summaryTextColor,
			           boolean messageDividerVisibility,
			           int messageDividerColor,
			           int sendButtonColor,
			           boolean darkContactImage,
			           int messageCounterColor,
			           int draftTextColor,
			           int emojiButtonColor,
			           int conversationDividerColor,
                       int unreadConversationColor)
	{
		this.name = name;
		this.custom = true;
		this.titleBarColor = titleBarColor;
		this.titleBarTextColor = titleBarTextColor;
		this.messageListBackground = messageListBackground;
		this.sendbarBackground = sendbarBackground;
		this.sentMessageBackground = sentMessageBackground;
		this.receivedMessageBackground = receivedMessageBackground;
		this.sentTextColor = sentTextColor;
		this.receivedTextColor = receivedTextColor;
		this.conversationListBackground = conversationListBackground;
		this.nameTextColor = nameTextColor;
		this.summaryTextColor = summaryTextColor;
		this.messageDividerVisibility = messageDividerVisibility;
		this.messageDividerColor = messageDividerColor;
		this.sendButtonColor = sendButtonColor;
		this.darkContactImage = darkContactImage;
		this.messageCounterColor = convertToARGB(messageCounterColor);
		this.draftTextColor = draftTextColor;
		this.emojiButtonColor = emojiButtonColor;
		this.conversationDividerColor = conversationDividerColor;
        this.unreadConversationColor = unreadConversationColor;
	}
	
	public String toString()
	{
		return this.name + "\n" +
			   this.titleBarColor + "\n" +
			   this.titleBarTextColor + "\n" +
			   this.messageListBackground + "\n" +
			   this.sendbarBackground + "\n" +
			   this.sentMessageBackground + "\n" +
			   this.receivedMessageBackground + "\n" +
			   this.sentTextColor + "\n" +
			   this.receivedTextColor + "\n" +
			   this.conversationListBackground + "\n" +
			   this.nameTextColor + "\n" +
			   this.summaryTextColor + "\n" +
			   this.messageDividerVisibility + "\n" +
			   this.messageDividerColor + "\n" +
			   this.sendButtonColor + "\n" +
			   this.darkContactImage + "\n" +
			   this.messageCounterColor + "\n" +
			   this.draftTextColor + "\n" +
			   this.emojiButtonColor + "\n" +
			   this.conversationDividerColor + "\n" +
               this.unreadConversationColor;
			   
	}
	
	public int getColor(Context context, int color)
	{
		return context.getResources().getColor(color);
	}
	
	public static CustomTheme themeFromString(String s)
	{
		String[] data = s.split("\n");
		ArrayList<String> data2 = new ArrayList<String>();
		
		for (int i = 0; i < data.length; i++)
		{
			if (!data[i].equals(" "))
			{
				data2.add(data[i].trim());
			}
		}
		
		int draftTextColor = 0;
		
		try
		{
			draftTextColor = Integer.parseInt(data2.get(17));
		} catch (Exception e)
		{
			draftTextColor = Integer.parseInt(data2.get(14));
		}
		
		int emojiButtonColor = 0;
		
		try
		{
			emojiButtonColor = Integer.parseInt(data2.get(18));
		} catch (Exception e)
		{
			emojiButtonColor = convertToColorInt("#8DBC36");
		}
		
		int conversationDividerColor = 0;
		
		try
		{
			conversationDividerColor = Integer.parseInt(data2.get(19));
		} catch (Exception e)
		{
			conversationDividerColor = convertToColorInt("#A1A1A1");
		}

        int unreadConversationColor = 0;

        try
        {
            unreadConversationColor = Integer.parseInt(data2.get(20));
        } catch (Exception e)
        {
            unreadConversationColor = Integer.parseInt(data2.get(6));
        }
		
		return new CustomTheme(data2.get(0),
							   Integer.parseInt(data2.get(1)),
							   Integer.parseInt(data2.get(2)),
							   Integer.parseInt(data2.get(3)),
							   Integer.parseInt(data2.get(4)),
							   Integer.parseInt(data2.get(5)),
							   Integer.parseInt(data2.get(6)),
							   Integer.parseInt(data2.get(7)),
							   Integer.parseInt(data2.get(8)),
							   Integer.parseInt(data2.get(9)),
							   Integer.parseInt(data2.get(10)),
							   Integer.parseInt(data2.get(11)),
							   Boolean.parseBoolean(data2.get(12)),
							   Integer.parseInt(data2.get(13)),
							   Integer.parseInt(data2.get(14)),
							   Boolean.parseBoolean(data2.get(15)),
					           Integer.parseInt(data2.get(16)),
					           draftTextColor,
					           emojiButtonColor,
					           conversationDividerColor,
                               unreadConversationColor);
	}
	
	public static String convertToARGB(int color) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + alpha + red + green + blue;
    }
	
	public static int convertToColorInt(String argb) throws NumberFormatException {

    	if (argb.startsWith("#")) {
    		argb = argb.replace("#", "");
    	}

        int alpha = -1, red = -1, green = -1, blue = -1;

        if (argb.length() == 8) {
            alpha = Integer.parseInt(argb.substring(0, 2), 16);
            red = Integer.parseInt(argb.substring(2, 4), 16);
            green = Integer.parseInt(argb.substring(4, 6), 16);
            blue = Integer.parseInt(argb.substring(6, 8), 16);
        }
        else if (argb.length() == 6) {
            alpha = 255;
            red = Integer.parseInt(argb.substring(0, 2), 16);
            green = Integer.parseInt(argb.substring(2, 4), 16);
            blue = Integer.parseInt(argb.substring(4, 6), 16);
        }

        return Color.argb(alpha, red, green, blue);
    }
}
