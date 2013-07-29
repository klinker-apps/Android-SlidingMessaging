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

public class CatalogWizardModel extends AbstractWizardModel {

    public CatalogWizardModel(Context context) {
        super(context);
    }

    @Override
        protected PageList onNewRootPageList() {
        boolean needTheme = false;
        boolean haveGoSMS = true;
        boolean needMMS = false;

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        int extraPageCount = 1;

        String version = "";

        try {
            version = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String changeLog = "Version " + version + ":\n\n" +
                "- Added Cards UI 2.0, based on the original cards UI, but correcting old speed issues, adding full theme-ability, and bringing it more in line with other layouts\n" +
                "- Option to pin conversation list to left side of screen at all times\n" +
                "- Fixes to sending animations sometimes not showing\n" +
                "- Minor UI Changes\n" +
                "- Bug fixes\n\n";
                
        String themeEditor = "The theme editor now fully supports the Hangouts UI and popup, " +
                "better time than ever to get on board and start making Sliding Messaging look exactly how you want!\n\n" +
                mContext.getString(R.string.theme_support);

        String notesPage = mContext.getResources().getString(R.string.changelog_disclaimers)
                        + "\n\n" +
                        mContext.getResources().getString(R.string.override_stock_disclaimer)
                        + "\n\n" +
                        mContext.getResources().getString(R.string.twitter_link);

        /*
        String goPro = mContext.getString(R.string.go_pro1) + "\n" +
                mContext.getString(R.string.go_pro2) +
                mContext.getString(R.string.go_pro3) +
                mContext.getString(R.string.go_pro4);
                */

        String goSMS = mContext.getString(R.string.go_sms_body);

        String mmsSetupMessage = mContext.getString(R.string.mms_setup_message);

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

        if (sharedPrefs.getString("mmsc_url", "").equals(""))
        {
            needMMS = true;
            extraPageCount++;
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


                } else if (needMMS)
                {
                    return new PageList(
                            new MessagePage(this, mContext.getString(R.string.changelog_title))
                                    .setMessage(changeLog)
                                    .setRequired(false),

                            new SingleFixedChoicePage(this, mContext.getString(R.string.need_mms_setup))
                                    .setChoices("AT&T","AT&T #2","Bell Canada","Fido Canada",
                                            "Free Mobile France","Network Norway","Net10","O2",
                                            "Rogers","Straight Talk AT&T","Tele2","Telus",
                                            "T-Mobile US","T-Mobile Polish","Virgin Mobile Canada",
                                            "Verizon Wireless","Verizon Wireless #2","Vodafone UK",
                                            "Vodafone AU","Not on list")
                                    .setMessage(mmsSetupMessage)
                                    .setRequired(true));
                }

            case 2:

                if (needTheme && haveGoSMS)
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
                } else if (needTheme && needMMS)
                {
                    return new PageList(
                            new MessagePage(this, mContext.getString(R.string.changelog_title))
                                    .setMessage(changeLog)
                                    .setRequired(false),

                            new LinkPage(this, mContext.getString(R.string.theme_support_title))
                                    .setExtra(themeEditor, "market://details?id=com.klinker.android.messaging_theme")
                                    .setRequired(false),

                            new SingleFixedChoicePage(this, mContext.getString(R.string.need_mms_setup))
                                    .setChoices("AT&T","AT&T #2","Bell Canada","Fido Canada",
                                            "Free Mobile France","Network Norway","Net10","O2",
                                            "Rogers","Straight Talk AT&T","Tele2","Telus",
                                            "T-Mobile US","T-Mobile Polish","Virgin Mobile Canada",
                                            "Verizon Wireless","Verizon Wireless #2","Vodafone UK",
                                            "Vodafone AU","Not on list")
                                    .setMessage(mmsSetupMessage)
                                    .setRequired(true));

                } else if (needMMS && haveGoSMS)
                {
                    return new PageList(
                            new MessagePage(this, mContext.getString(R.string.changelog_title))
                                    .setMessage(changeLog)
                                    .setRequired(false),

                            new MessagePage(this, mContext.getString(R.string.go_sms_title))
                                    .setMessage(goSMS)
                                    .setRequired(false),

                            new SingleFixedChoicePage(this, mContext.getString(R.string.need_mms_setup))
                                    .setChoices("AT&T","AT&T #2","Bell Canada","Fido Canada",
                                            "Free Mobile France","Network Norway","Net10","O2",
                                            "Rogers","Straight Talk AT&T","Tele2","Telus",
                                            "T-Mobile US","T-Mobile Polish","Virgin Mobile Canada",
                                            "Verizon Wireless","Verizon Wireless #2","Vodafone UK",
                                            "Vodafone AU","Not on list")
                                    .setMessage(mmsSetupMessage)
                                    .setRequired(true));
                }

            case 3:

                return new PageList(

                        new MessagePage(this, mContext.getString(R.string.changelog_title))
                                .setMessage(changeLog)
                                .setRequired(false),

                        new MessagePage(this, mContext.getString(R.string.go_sms_title))
                                .setMessage(goSMS)
                                .setRequired(false),

                        new LinkPage(this, mContext.getString(R.string.theme_support_title))
                                .setExtra(themeEditor, "market://details?id=com.klinker.android.messaging_theme")
                                .setRequired(false),

                        new SingleFixedChoicePage(this, mContext.getString(R.string.need_mms_setup))
                                .setChoices("AT&T","AT&T #2","Bell Canada","Fido Canada",
                                        "Free Mobile France","Network Norway","Net10","O2",
                                        "Rogers","Straight Talk AT&T","Tele2","Telus",
                                        "T-Mobile US","T-Mobile Polish","Virgin Mobile Canada",
                                        "Verizon Wireless","Verizon Wireless #2","Vodafone UK",
                                        "Vodafone AU","Not on list")
                                .setMessage(mmsSetupMessage)
                                .setRequired(true));

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
