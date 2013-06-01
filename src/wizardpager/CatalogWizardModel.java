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
import android.util.Log;
import com.klinker.android.messaging_donate.R;
import wizardpager.wizard.model.AbstractWizardModel;
import wizardpager.wizard.model.PageList;
import wizardpager.wizard.model.SingleFixedChoicePage;

public class CatalogWizardModel extends AbstractWizardModel {
    public CatalogWizardModel(Context context) {
        super(context);
    }

    @Override
    protected PageList onNewRootPageList() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        boolean needTheme = false;
        boolean needPro = false;
        boolean haveGoSMS = true;

        int extraPageCount = 1;

        String version = "";

        try {
            version = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String changeLog = "Version " + version + ":\n\n" +
                "- Major rework of settings layout\n" +
                "- Rework of change log and initial notes\n" +
                "- Added new 1x1 widget with unread counter\n" +
                "- More options for notification icons\n" +
                "- Layout optimizations\n" +
                "- Fixed color picker force close in theme chooser\n" +
                "- Bug fixes\n\n";

        String themeEditor = "The theme editor now fully supports the Hangouts UI as well, " +
                "better time then ever to get on board and start making Sliding Messaging look exactly how you want!\n\n" +
                mContext.getString(R.string.theme_support);

        String goPro = mContext.getString(R.string.go_pro1) + "\n" +
                mContext.getString(R.string.go_pro2) +
                mContext.getString(R.string.go_pro3) +
                mContext.getString(R.string.go_pro4);

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
			pm.getPackageInfo("com.klinker.android.messaging_donate", PackageManager.GET_ACTIVITIES);
		} catch (Exception e)
		{
            needPro = true;
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
                if (needPro)
                {
                    return new PageList(
                            new SingleFixedChoicePage(this, mContext.getString(R.string.changelog_title))
                                    .setMessage(changeLog)
                                    .setRequired(false),

                            new SingleFixedChoicePage(this, mContext.getString(R.string.pro_dialog))
                                    .setMessage(goPro)
                                    .setButton(true, "market://details?id=com.klinker.android.messaging_donate")
                                    .setRequired(false));
                } else if (needTheme)
                {
                    return new PageList(
                            new SingleFixedChoicePage(this, mContext.getString(R.string.changelog_title))
                                    .setMessage(changeLog)
                                    .setRequired(false),

                            new SingleFixedChoicePage(this, mContext.getString(R.string.theme_support_title))
                                    .setMessage(themeEditor)
                                    .setButton(true, "market://details?id=com.klinker.android.messaging_theme")
                                    .setRequired(false));
                } else
                {
                    return new PageList(
                            new SingleFixedChoicePage(this, mContext.getString(R.string.changelog_title))
                                    .setMessage(changeLog)
                                    .setRequired(false),

                            new SingleFixedChoicePage(this, mContext.getString(R.string.go_sms_title))
                                    .setMessage(goSMS)
                                    .setRequired(false));
                }

            case 2:
                if(needPro && needTheme)
                {
                    return new PageList(
                            new SingleFixedChoicePage(this, mContext.getString(R.string.changelog_title))
                                    .setMessage(changeLog)
                                    .setRequired(false),

                            new SingleFixedChoicePage(this, mContext.getString(R.string.theme_support_title))
                                    .setMessage(themeEditor)
                                    .setButton(true, "market://details?id=com.klinker.android.messaging_theme")
                                    .setRequired(false),

                            new SingleFixedChoicePage(this, mContext.getString(R.string.pro_dialog))
                                    .setMessage(goPro)
                                    .setButton(true, "market://details?id=com.klinker.android.messaging_donate")
                                    .setRequired(false));
                } else if (needPro && haveGoSMS)
                {
                    return new PageList(
                            new SingleFixedChoicePage(this, mContext.getString(R.string.changelog_title))
                                    .setMessage(changeLog)
                                    .setRequired(false),

                            new SingleFixedChoicePage(this, mContext.getString(R.string.pro_dialog))
                                    .setMessage(goPro)
                                    .setButton(true, "market://details?id=com.klinker.android.messaging_donate")
                                    .setRequired(false),

                            new SingleFixedChoicePage(this, mContext.getString(R.string.go_sms_title))
                                    .setMessage(goSMS)
                                    .setRequired(false));
                } else
                {
                    return new PageList(
                            new SingleFixedChoicePage(this, mContext.getString(R.string.changelog_title))
                                    .setMessage(changeLog)
                                    .setRequired(false),

                            new SingleFixedChoicePage(this, mContext.getString(R.string.theme_support_title))
                                    .setMessage(themeEditor)
                                    .setButton(true, "market://details?id=com.klinker.android.messaging_theme")
                                    .setRequired(false),

                            new SingleFixedChoicePage(this, mContext.getString(R.string.go_sms_title))
                                    .setMessage(goSMS)
                                    .setRequired(false));
                }

            case 3:
                return new PageList(
                        new SingleFixedChoicePage(this, mContext.getString(R.string.changelog_title))
                                .setMessage(changeLog)
                                .setRequired(false),

                        new SingleFixedChoicePage(this, mContext.getString(R.string.theme_support_title))
                                .setMessage(themeEditor)
                                .setButton(true, "market://details?id=com.klinker.android.messaging_theme")
                                .setRequired(false),

                        new SingleFixedChoicePage(this, mContext.getString(R.string.pro_dialog))
                                .setMessage(goPro)
                                .setButton(true, "market://details?id=com.klinker.android.messaging_donate")
                                .setRequired(false),

                        new SingleFixedChoicePage(this, mContext.getString(R.string.go_sms_title))
                                .setMessage(goSMS)
                                .setRequired(false));

            default:
                return new PageList(
                        new SingleFixedChoicePage(this, mContext.getString(R.string.changelog_title))
                                .setMessage(changeLog)
                                .setRequired(false));

        }
/*
        return new PageList(
                new SingleFixedChoicePage(this, mContext.getString(R.string.changelog_title))
                        .setMessage(changeLog)
                        .setRequired(false),

                new SingleFixedChoicePage(this, mContext.getString(R.string.theme_support_title))
                        .setMessage(themeEditor)
                        .setButton(true, "market://details?id=com.klinker.android.messaging_theme")
                        .setRequired(false),

                new SingleFixedChoicePage(this, mContext.getString(R.string.pro_dialog))
                        .setMessage(goPro)
                        .setButton(true, "market://details?id=com.klinker.android.messaging_donate")
                        .setRequired(false),

                new SingleFixedChoicePage(this, mContext.getString(R.string.go_sms_title))
                        .setMessage(goSMS)
                        .setRequired(false));
*/
        // Note: The final page is the Notes page, this page can be edited in the ReviewFragment class
        // It wasn't put here because it is automatically called in the main activity and is nessesary to finish the intent.
    }
}
