/*
 * Copyright 2012 Roman Nurik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wizardpager;

import android.content.Context;
import android.content.pm.PackageManager;
import com.klinker.android.messaging_donate.R;
import wizardpager.wizard.model.AbstractWizardModel;
import wizardpager.wizard.model.LinkPage;
import wizardpager.wizard.model.MessagePage;
import wizardpager.wizard.model.PageList;

public class ChangeLogWizardModel extends AbstractWizardModel {

    public ChangeLogWizardModel(Context context) {
        super(context);
    }

    @Override
    protected PageList onNewRootPageList() {
        boolean needTheme = false;
        boolean needEmoji = false;
        boolean haveGoSMS = true;

        int extraPageCount = 1;

        String version = "";

        try {
            version = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String changeLog = "Version " + version + ":\n\n" +
                "- 3 different Google Voice message syncing options\n" +
                "\t1) Refreshing on specified interval\n" +
                "\t2) Refreshing when Google Voice app shows a notification\n" +
                "\t3) Manual refresh from menu or settings\n" +
                "- MMS fixes including a possible fix for sending with WiFi enabled which is still experimental. Feel free to disable the WiFi MMS fix now though if you want to try!\n" +
                "- Google Voice sending settings are now saved on a per-conversation basis so that you don't have to remember to switch back and forth between SMS and Voice\n" +
                "- New settings menu navigation\n" +
                "- More delivery report options\n" +
                "- Holo Light theme applied throughout the app to better match Google's current styles\n" +
                "- Option to disable the mark as read by swiping away a notification on Android 4.3\n" +
                "- Tablet specific UI changes and fixes\n" +
                "- Option to enable a button that will take you to the full app in SlideOver\n" +
                "- Option to launch SlideOver popup from the DashClock extension\n" +
                "- Fix for keyboard showing up over the text input box in SlideOver\n" +
                "- Bug fixes\n\n\n" +
                "To My Testers:\n" +
                "I need everyone to try sending MMS on WiFi without the \"WiFi Fix\" in MMS settings enabled. Report finding to the Google+ community please. It works well on my phone, but MMS has always" +
                "worked well for me so I'd just like to be sure everything is all good before I take that option out completely.";
                
        String themeEditor = "The theme editor now fully supports the Cards UI 2.0, making it a " +
                "better time than ever to get on board and start making Sliding Messaging Pro look exactly how you want!\n\n" +
                mContext.getString(R.string.theme_support);

        String emojiKeyboard = mContext.getString(R.string.need_emoji_summary);

        String goSMS = mContext.getString(R.string.go_sms_body);

        try
        {
            PackageManager pm = mContext.getPackageManager();
            pm.getPackageInfo("com.klinker.android.messaging_theme", PackageManager.GET_ACTIVITIES);
        } catch (Exception e)
        {
            needTheme = true;
            extraPageCount++;
        }

        try
        {
            PackageManager pm = mContext.getPackageManager();
            pm.getPackageInfo("com.klinker.android.emoji_keyboard_trial", PackageManager.GET_ACTIVITIES);
        } catch (Exception e)
        {
            needEmoji = true;
            extraPageCount++;
        }

        try
        {
            PackageManager pm = mContext.getPackageManager();
     		pm.getPackageInfo("com.jb.gosms", PackageManager.GET_ACTIVITIES);
        } catch (Exception e)
        {
            haveGoSMS = false;
            extraPageCount--;
        }


        switch(extraPageCount)
        {
            case 1:
                if (needTheme)
                {
                    return new PageList(
                            new MessagePage(this, mContext.getString(R.string.changelog_title))
                                    .setMessage(changeLog)
                                    .setRequired(false),

                            new LinkPage(this, mContext.getString(R.string.theme_support_title))
                                    .setExtra(themeEditor, "market://details?id=com.klinker.android.messaging_theme")
                                    .setRequired(false));

                } else if (haveGoSMS)
                {
                    return new PageList(
                            new MessagePage(this, mContext.getString(R.string.changelog_title))
                                    .setMessage(changeLog)
                                    .setRequired(false),

                            new MessagePage(this, mContext.getString(R.string.go_sms_title))
                                    .setMessage(goSMS)
                                    .setRequired(false));


                } else if (needEmoji)
                {
                    return new PageList(
                            new MessagePage(this, mContext.getString(R.string.changelog_title))
                                    .setMessage(changeLog)
                                    .setRequired(false),

                            new LinkPage(this, mContext.getString(R.string.need_emoji_title))
                                    .setExtra(emojiKeyboard, "market://details?id=com.klinker.android.emoji_keyboard_trial")
                                    .setRequired(false));
                }

            case 2:

                if(needTheme && haveGoSMS)
                {
                    return new PageList(
                            new MessagePage(this, mContext.getString(R.string.changelog_title))
                                    .setMessage(changeLog)
                                    .setRequired(false),

                            new LinkPage(this, mContext.getString(R.string.theme_support_title))
                                    .setExtra(themeEditor, "market://details?id=com.klinker.android.messaging_theme")
                                    .setRequired(false),

                            new MessagePage(this, mContext.getString(R.string.go_sms_title))
                                    .setMessage(goSMS)
                                    .setRequired(false));
                } else if (needTheme && needEmoji)
                {
                    return new PageList(
                            new MessagePage(this, mContext.getString(R.string.changelog_title))
                                    .setMessage(changeLog)
                                    .setRequired(false),

                            new LinkPage(this, mContext.getString(R.string.theme_support_title))
                                    .setExtra(themeEditor, "market://details?id=com.klinker.android.messaging_theme")
                                    .setRequired(false),

                            new LinkPage(this, mContext.getString(R.string.need_emoji_title))
                                    .setExtra(emojiKeyboard, "market://details?id=com.klinker.android.emoji_keyboard_trial")
                                    .setRequired(false));
                } else if (needEmoji && haveGoSMS)
                {
                    return new PageList(
                            new MessagePage(this, mContext.getString(R.string.changelog_title))
                                    .setMessage(changeLog)
                                    .setRequired(false),

                            new LinkPage(this, mContext.getString(R.string.need_emoji_title))
                                    .setExtra(emojiKeyboard, "market://details?id=com.klinker.android.emoji_keyboard_trial")
                                    .setRequired(false),

                            new MessagePage(this, mContext.getString(R.string.go_sms_title))
                                    .setMessage(goSMS)
                                    .setRequired(false));
                }

            case 3:

                return new PageList(
                        new MessagePage(this, mContext.getString(R.string.changelog_title))
                                .setMessage(changeLog)
                                .setRequired(false),

                        new LinkPage(this, mContext.getString(R.string.theme_support_title))
                                .setExtra(themeEditor, "market://details?id=com.klinker.android.messaging_theme")
                                .setRequired(false),

                        new LinkPage(this, mContext.getString(R.string.need_emoji_title))
                                .setExtra(emojiKeyboard, "market://details?id=com.klinker.android.emoji_keyboard_trial")
                                .setRequired(false),

                        new MessagePage(this, mContext.getString(R.string.go_sms_title))
                                .setMessage(goSMS)
                                .setRequired(false));


            default:
                return new PageList(
                        new MessagePage(this, mContext.getString(R.string.changelog_title))
                                .setMessage(changeLog)
                                .setRequired(false));

        }
        // Note: The final page is the Notes page, this page can be edited in the ReviewFragment class
        // It wasn't put here because it is automatically called in the main activity and is nessesary to finish the intent.
    }
}
