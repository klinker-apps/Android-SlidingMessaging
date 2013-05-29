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
import wizardpager.wizard.model.PageList;
import wizardpager.wizard.model.SingleFixedChoicePage;

public class SandwichWizardModel extends AbstractWizardModel {
    private Context mContext;

    public SandwichWizardModel(Context context) {
        super(context);
        mContext = context;

    }

    @Override
    protected PageList onNewRootPageList() {
        String version = "";

        try {
            version = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        // These are all the availible pages, or you can make a new one and add it, it is really easy how i have set it up.
        // To-do: Go in and make it so for different situations, different pages are created.
            // also have to actually call the intent to bring up this activity
            // Done. then make sure the activity closes to the right place, right now it is set up to just take you to the launcher
                // change this in the mainactivity class
        return new PageList(
                new SingleFixedChoicePage(this, mContext.getString(R.string.changelog_title))
                        .setMessage("Version " + version + ":\n\n" +
                                "- Major rework of settings layout\n" +
                                "- Added new 1x1 widget with unread counter\n" +
                                "- More options for notification icons\n" +
                                "- Layout optimizations\n" +
                                "- Bug fixes\n\n")
                        .setRequired(false),

                new SingleFixedChoicePage(this, mContext.getString(R.string.theme_support_title))
                        .setMessage("The theme editor now fully supports the Hangouts UI as well, " +
                                "better time then ever to get on board and start making Sliding Messaging look exactly how you want!\n\n" +
                                mContext.getString(R.string.theme_support))
                        .setButton(true, "market://details?id=com.klinker.android.messaging_theme")
                        .setRequired(false),

                new SingleFixedChoicePage(this, mContext.getString(R.string.pro_dialog))
                        .setMessage(mContext.getString(R.string.go_pro1) +
                                    mContext.getString(R.string.go_pro2) +
                                    mContext.getString(R.string.go_pro3) +
                                    mContext.getString(R.string.go_pro4))
                        .setButton(true, "market://details?id=com.klinker.android.messaging_donate")
                        .setRequired(false),

                new SingleFixedChoicePage(this, mContext.getString(R.string.go_sms_title))
                        .setMessage(mContext.getString(R.string.go_sms_body))
                        .setRequired(false));
                // Note: The final page is the Notes page, this page can be edited in the ReviewFragment class
                // It wasn't put here because it is automatically called in the main activity and is nessesary to finish the intent.
    }
}
