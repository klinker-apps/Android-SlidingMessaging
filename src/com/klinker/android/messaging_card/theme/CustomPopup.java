package com.klinker.android.messaging_card.theme;

import android.content.Context;
import android.graphics.Color;
import com.klinker.android.messaging_donate.R;

import java.util.ArrayList;

public class CustomPopup {
	public String name;
	public int messageBackground;
	public int sendbarBackground;
    public int dividerColor;
	public int nameTextColor;
    public int numberTextColor;
    public int dateTextColor;
    public int messageTextColor;
    public int draftTextColor;
    public int buttonColor;
    public int emojiButtonColor;
	
	public CustomPopup(String name, Context context)
	{
		if (name.equals("White"))
		{
			this.name = "Light Theme";
            this.messageBackground = context.getResources().getColor(R.color.white);
            this.sendbarBackground = context.getResources().getColor(R.color.white);
            this.dividerColor = context.getResources().getColor(R.color.card_conversation_divider);
            this.nameTextColor = context.getResources().getColor(R.color.card_conversation_name);
            this.numberTextColor = context.getResources().getColor(R.color.card_conversation_summary);
            this.dateTextColor = context.getResources().getColor(R.color.card_message_text_date_2);
            this.messageTextColor = context.getResources().getColor(R.color.card_message_text_body);
            this.draftTextColor = context.getResources().getColor(R.color.card_message_text_body);
            this.buttonColor = context.getResources().getColor(R.color.card_message_text_body);
            this.emojiButtonColor = context.getResources().getColor(R.color.emoji_button);

		} else if (name.equals("Dark"))
		{
			this.name = "Dark Theme";
            this.messageBackground = context.getResources().getColor(R.color.black);
            this.sendbarBackground = context.getResources().getColor(R.color.black);
            this.dividerColor = context.getResources().getColor(R.color.card_dark_conversation_divider);
            this.nameTextColor = context.getResources().getColor(R.color.card_dark_conversation_name);
            this.numberTextColor = context.getResources().getColor(R.color.card_dark_conversation_summary);
            this.dateTextColor = context.getResources().getColor(R.color.card_dark_message_text_date_2);
            this.messageTextColor = context.getResources().getColor(R.color.card_dark_message_text_body);
            this.draftTextColor = context.getResources().getColor(R.color.card_dark_message_text_body);
            this.buttonColor = context.getResources().getColor(R.color.card_dark_message_text_body);
            this.emojiButtonColor = context.getResources().getColor(R.color.emoji_button);
		}
	}
	
	public CustomPopup(String name,
                       int messageBackground,
                       int sendbarBackground,
                       int dividerColor,
                       int nameTextColor,
                       int numberTextColor,
                       int dateTextColor,
                       int messageTextColor,
                       int draftTextColor,
                       int buttonColor,
                       int emojiButtonColor)
	{
		this.name = name;
		this.messageBackground = messageBackground;
        this.sendbarBackground = sendbarBackground;
        this.dividerColor = dividerColor;
        this.nameTextColor = nameTextColor;
        this.numberTextColor = numberTextColor;
        this.dateTextColor = dateTextColor;
        this.messageTextColor = messageTextColor;
        this.draftTextColor = draftTextColor;
        this.buttonColor = buttonColor;
        this.emojiButtonColor = emojiButtonColor;
	}
	
	public String toString()
	{
		return this.name + "\n" +
			   this.messageBackground + "\n" +
               this.sendbarBackground + "\n" +
               this.dividerColor + "\n" +
               this.nameTextColor + "\n" +
               this.numberTextColor + "\n" +
               this.dateTextColor + "\n" +
               this.messageTextColor + "\n" +
               this.draftTextColor + "\n" +
               this.buttonColor + "\n" +
               this.emojiButtonColor;
			   
	}
	
	public int getColor(Context context, int color)
	{
		return context.getResources().getColor(color);
	}
	
	public static CustomPopup themeFromString(String s)
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
		
		return new CustomPopup(data2.get(0),
							   Integer.parseInt(data2.get(1)),
							   Integer.parseInt(data2.get(2)),
							   Integer.parseInt(data2.get(3)),
							   Integer.parseInt(data2.get(4)),
							   Integer.parseInt(data2.get(5)),
							   Integer.parseInt(data2.get(6)),
							   Integer.parseInt(data2.get(7)),
							   Integer.parseInt(data2.get(8)),
							   Integer.parseInt(data2.get(9)),
							   Integer.parseInt(data2.get(10)));
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
