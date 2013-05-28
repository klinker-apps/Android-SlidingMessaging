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
import wizardpager.wizard.model.AbstractWizardModel;
import wizardpager.wizard.model.PageList;
import wizardpager.wizard.model.SingleFixedChoicePage;

public class SandwichWizardModel extends AbstractWizardModel {
    public SandwichWizardModel(Context context) {
        super(context);
    }

    @Override
    protected PageList onNewRootPageList() {
        return new PageList(
                new SingleFixedChoicePage(this, "Changes")
                        .setMessage("Version "+":\n\n" +
                                "- Major rework of settings layout\n" +
                                "- Added new 1x1 widget with unread counter\n" +
                                "- More options for notification icons\n" +
                                "- Layout optimizations\n" +
                                "- Bug fixes\n\n")
                        .setRequired(false),

                new SingleFixedChoicePage(this, "Theme Editor")
                        .setMessage("The theme editor now fully supports the Hangouts UI as well, better time then ever to get on board and start making Sliding Messaging look exactly how you want!\n\n")
                        .setButton(true, "market://details?id=com.klinker.android.messaging_theme")
                        .setRequired(false),

                new SingleFixedChoicePage(this, "Go PRO!")
                        .setMessage("Go pro now for extra features!")
                        .setButton(true, "market://details?id=com.klinker.android.messaging_donate")
                        .setRequired(false),

                new SingleFixedChoicePage(this, "Go SMS")
                        .setMessage("Sliding Messaging doesn't work with Go SMS installed. \n\nPlease remove or disable it before attempting to use my app!")
                        .setRequired(false));
    }
}
