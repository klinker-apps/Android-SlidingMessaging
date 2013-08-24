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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import com.klinker.android.messaging_donate.R;
import wizardpager.wizard.model.AbstractWizardModel;
import wizardpager.wizard.model.LinkPage;
import wizardpager.wizard.model.MessagePage;
import wizardpager.wizard.model.PageList;
import wizardpager.wizard.model.SingleFixedChoicePage;

public class ChangeLogWizardModel extends AbstractWizardModel {

    public ChangeLogWizardModel(Context context) {
        super(context);
    }

    @Override
        protected PageList onNewRootPageList() {
        boolean needTheme = false;
        boolean haveGoSMS = true;

        int extraPageCount = 1;

        String version = "";

        try {
            version = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String changeLog = "Version " + version + ":\n\n" +
                "- Beautiful new emoji keyboard for Android emojis. This same keyboard can be used in any app on your phone by downloading our new Emoji Keyboard! http://goo.gl/9DxvSf\n" +
                "- Ability to lock messages\n" +
                "- Batch delete certain messages in single conversation\n" +
                "- Redone share box to match current theme\n" +
                "- UI changes to Hangouts UI and batch delete\n" +
                "- Added tablet support\n" +
                "- Bug fixes\n\n" +
                "- For interested developers: I completely recreated the back-end sending process and published t it as a library to Github. Any developer interested in using it, improving it, or whatever else, check it out! " +
                "There is support for sending SMS and MMS messages all packaged into one easy class to use - this is something Google has neglected for much too long, so with this anyone can easily send " +
                "any type of message from within their application. Think of it as the missing MMS APIs if you want and save yourself the trouble of countless hours digging through their source code trying to figure out what is going on." +
                " <INSERT LINK TO GITHUB PROJECT HERE>";
                
        String themeEditor = "The theme editor now fully supports the Cards UI 2.0, making it a " +
                "better time than ever to get on board and start making Sliding Messaging Pro look exactly how you want!\n\n" +
                mContext.getString(R.string.theme_support);

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


                }

            case 2:


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
/*
new SingleFixedChoicePage(this, mContext.getString(R.string.need_mms_setup))
                                    .setChoices("AT&T","AT&T #2","Bell Canada","Fido Canada",
                                            "Free Mobile France","Network Norway","Net10","O2",
                                            "Rogers","Straight Talk AT&T","Tele2","Telus",
                                            "T-Mobile US","T-Mobile Polish","Virgin Mobile Canada",
                                            "Verizon Wireless","Verizon Wireless #2","Vodafone UK",
                                            "Vodafone AU","Not on list")
                                    .setMessage(mmsSetupMessage)
                                    .setRequired(true));
                                    */
