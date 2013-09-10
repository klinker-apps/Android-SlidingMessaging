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
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.settings.SettingsPagerActivity;
import com.klinker.android.send_message.Transaction;
import wizardpager.wizard.model.AbstractWizardModel;
import wizardpager.wizard.model.MessagePage;
import wizardpager.wizard.model.PageList;
import wizardpager.wizard.model.SingleFixedChoicePage;

public class InitialSetupWizardModel extends AbstractWizardModel {

    public InitialSetupWizardModel(Context context) {
        super(context);
    }

    @Override
    protected PageList onNewRootPageList() {

        boolean isTablet;

        try {
            if ((Transaction.getMyPhoneNumber(mContext) == null || Transaction.getMyPhoneNumber(mContext).equals("")) && SettingsPagerActivity.tabletSize(mContext) > 6.5 && SettingsPagerActivity.xLargeScreen(mContext)) {
                isTablet = true;
            } else {
                isTablet = false;
            }
        } catch (Exception e) {
            isTablet = true;
        }

        return new PageList(
                new MessagePage(this, mContext.getString(R.string.initial_setup))
                        .setMessage(mContext.getString(R.string.initial_setup_message))
                        .setRequired(false),

                new SingleFixedChoicePage(this, mContext.getString(R.string.run_as))
                        .setChoices("Hangouts UI", "Classic UI", "Cards UI", "Cards+ UI")
                        .setRequired(true),

                new SingleFixedChoicePage(this, mContext.getString(R.string.emojis))
                        .setChoices("No Emojis", "Android Style", "iOS Style")
                        .setRequired(true),

                new SingleFixedChoicePage(this, mContext.getString(R.string.enable_slideover))
                        .setChoices("Yes - The most used app, messaging, is always accessible.", "No")
                        .setRequired(true),

                !isTablet ?
                new SingleFixedChoicePage(this, mContext.getString(R.string.need_mms_setup))
                .setChoices("Auto Select","AT&T","AT&T #2","Bell Canada","Fido Canada",
                        "Free Mobile France","Network Norway","Net10","O2",
                        "Rogers","Sprint","Straight Talk AT&T","Tele2","Telus",
                        "T-Mobile US","T-Mobile Polish","Virgin Mobile Canada",
                        "Verizon Wireless","Verizon Wireless #2","Vodafone UK",
                        "Vodafone AU","Not on list")
                    .setRequired(true) :
                new MessagePage(this, mContext.getString(R.string.need_mms_setup))
                .setMessage("Since you are using a tablet, MMS is not available. You need to set up Google Voice in settings before sending any messages!"));
    }
}