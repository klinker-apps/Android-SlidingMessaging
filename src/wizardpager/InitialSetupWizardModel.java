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

public class InitialSetupWizardModel extends AbstractWizardModel {

    public InitialSetupWizardModel(Context context) {
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

        String changeLog = "Initial Setup";

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
    }
}