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

package com.klinker.android.messaging_donate.wizardpager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.droidprism.APN;
import com.droidprism.Carrier;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.wizardpager.wizard.model.AbstractWizardModel;
import com.klinker.android.messaging_donate.wizardpager.wizard.model.ModelCallbacks;
import com.klinker.android.messaging_donate.wizardpager.wizard.model.Page;
import com.klinker.android.messaging_donate.wizardpager.wizard.ui.PageFragmentCallbacks;
import com.klinker.android.messaging_donate.wizardpager.wizard.ui.ReviewFragment;
import com.klinker.android.messaging_donate.wizardpager.wizard.ui.StepPagerStrip;

import java.util.List;

public class InitialSetupMain extends FragmentActivity implements
        PageFragmentCallbacks,
        ReviewFragment.Callbacks,
        ModelCallbacks {
    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;

    private boolean mEditingAfterReview;

    private AbstractWizardModel mWizardModel;

    private boolean mConsumePageSelectedEvent;

    private Button mNextButton;
    private Button mPrevButton;

    private List<Page> mCurrentPageSequence;
    private StepPagerStrip mStepPagerStrip;

    private Intent fromIntent;
    private String version;

    private Activity mContext;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wizard_main);

        mContext = this;

        mWizardModel = new InitialSetupWizardModel(getBaseContext());
        fromIntent = getIntent();
        version = fromIntent.getStringExtra("version");
        mWizardModel.version = version;

        if (savedInstanceState != null) {
            mWizardModel.load(savedInstanceState.getBundle("model"));
        }

        mWizardModel.registerListener(this);

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mStepPagerStrip = (StepPagerStrip) findViewById(R.id.strip);
        mStepPagerStrip.setOnPageSelectedListener(new StepPagerStrip.OnPageSelectedListener() {
            @Override
            public void onPageStripSelected(int position) {
                position = Math.min(mPagerAdapter.getCount() - 1, position);
                if (mPager.getCurrentItem() != position) {
                    mPager.setCurrentItem(position);
                }
            }
        });

        mNextButton = (Button) findViewById(R.id.next_button);
        mPrevButton = (Button) findViewById(R.id.prev_button);

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mStepPagerStrip.setCurrentPage(position);

                if (mConsumePageSelectedEvent) {
                    mConsumePageSelectedEvent = false;
                    return;
                }

                mEditingAfterReview = false;
                updateBottomBar();
            }
        });

        final Context context = this;

        mNextButton.setOnClickListener(new View.OnClickListener() {

            private SharedPreferences sharedPrefs;
            private SharedPreferences.Editor editor;
            @Override
            public void onClick(View view) {

                if (mPager.getCurrentItem() == mCurrentPageSequence.size() - 1) {

                    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                    editor = sharedPrefs.edit();

                    String version = "1.0";
                    SharedPreferences.Editor prefEdit = sharedPrefs.edit();
                    prefEdit.putString("current_version", version);
                    prefEdit.commit();

                    // Sets the carrier for MMS
                    String carrier;

                    try {
                        carrier = mWizardModel.findByKey(context.getString(R.string.need_mms_setup)).getData().getString(Page.SIMPLE_DATA_KEY);
                    } catch (Exception e)
                    {
                        carrier = "";
                    }

                    if (null != carrier) {
                        setAPN(carrier);
                    }

                    // Sets the display layout
                    String display;

                    try {
                        display = mWizardModel.findByKey(context.getString(R.string.run_as)).getData().getString(Page.SIMPLE_DATA_KEY);
                    } catch (Exception e)
                    {
                        display = "";
                    }

                    setDisplay(display);

                    // Sets up the emojis
                    String emojis;

                    try {
                        emojis = mWizardModel.findByKey(context.getString(R.string.emojis)).getData().getString(Page.SIMPLE_DATA_KEY);
                    } catch (Exception e)
                    {
                        emojis = "";
                    }

                    setEmojis(emojis);

                    // Enables SlideOver
                    String slideOver;

                    try {
                        slideOver = mWizardModel.findByKey(context.getString(R.string.enable_slideover)).getData().getString(Page.SIMPLE_DATA_KEY);
                    } catch (Exception e)
                    {
                        slideOver = "";
                    }

                    setSlideOver(slideOver);

                    // Launch the regular changelog intent
                    boolean flag = false;

                    if (fromIntent.getStringExtra("com.klinker.android.OPEN") != null)
                    {
                        flag = true;
                    }

                    final Intent intent = new Intent(context, ChangeLogMain.class);
                    intent.setAction(fromIntent.getAction());
                    intent.setData(fromIntent.getData());
                    intent.putExtra("initial_run", true);

                    try
                    {
                        intent.putExtras(fromIntent.getExtras());
                    } catch (Exception e)
                    {

                    }

                    if (flag)
                    {
                        intent.putExtra("com.klinker.android.OPEN", intent.getStringExtra("com.klinker.android.OPEN"));
                    }

                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();

                } else {
                    if (mEditingAfterReview) {
                        mPager.setCurrentItem(mPagerAdapter.getCount() - 1);
                    } else {
                        mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                    }
                }
            }

            public void setDisplay(String display)
            {
                if (display.equals("Hangouts UI"))
                    editor.putString("run_as", "hangout");
                else if (display.equals("Classic UI"))
                    editor.putString("run_as", "sliding");
                else if (display.equals("Cards UI"))
                    editor.putString("run_as", "card2");
                else
                    editor.putString("run_as", "card+");

                editor.commit();
            }

            public void setEmojis(String emojis)
            {
                if (emojis.equals("No Emojis"))
                    editor.putBoolean("emoji", false);
                else if (emojis.equals("Android Style"))
                {
                    editor.putBoolean("emoji", true);
                    editor.putBoolean("emoji_type", true);
                } else
                {
                    editor.putBoolean("emoji", true);
                    editor.putBoolean("emoji_type", false);
                }

                editor.commit();
            }

            public void setSlideOver(String slideOver)
            {
                if (slideOver.equals("Yes"))
                    editor.putBoolean("slideover_enabled", true);
                else
                    editor.putBoolean("slideover_enabled", false);

                editor.commit();

                Intent service = new Intent();
                service.setAction("com.klinker.android.messaging.STOP_HALO");
                sendBroadcast(service);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (sharedPrefs.getBoolean("slideover_enabled", false)) {
                            Intent service = new Intent(getApplicationContext(), com.klinker.android.messaging_sliding.slide_over.SlideOverService.class);
                            startService(service);
                        }
                    }
                }, 500);
            }

            public void setAPN(String carrier)
            {
                if(carrier.equals("Not on list"))
                {
                    Context context = getApplicationContext();
                    CharSequence text = "Configure APN's manually at AppSettings->MMS AppSettings";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else if (!carrier.equals(""))
                {
                    if (carrier.equals("Auto Select")) {
                        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        final String networkOperator = manager.getNetworkOperator();

                        if (networkOperator != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        int mcc = Integer.parseInt(networkOperator.substring(0, 3));
                                        String s = networkOperator.substring(3);
                                        int mnc = Integer.parseInt(s.replaceFirst("^0{1,2}", ""));
                                        Carrier c = Carrier.getCarrier(mcc, mnc);
                                        APN a = c.getAPN();

                                        sharedPrefs.edit().putString("mmsc_url", a.mmsc).putString("mms_proxy", a.proxy).putString("mms_port", a.port + "").commit();
                                    } catch (Exception e) {
                                        mContext.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    Toast.makeText(context, "Error auto selecting APNs", Toast.LENGTH_LONG).show();
                                                } catch (Exception e) {
                                                    // context no longer active probably...
                                                }
                                            }
                                        });
                                    }
                                }
                            }).start();
                        } else {
                            Toast.makeText(context, "Error auto selecting APNs.", Toast.LENGTH_SHORT).show();
                        }
                    } else if (carrier.equals("AT&T"))
                    {
                        editor.putString("mmsc_url","http://mmsc.cingular.com");
                        editor.putString("mms_proxy","wireless.cingular.com");
                        editor.putString("mms_port","80");

                    } else if (carrier.equals("AT&T #2"))
                    {
                        editor.putString("mmsc_url","http://mmsc.mobile.att.net");
                        editor.putString("mms_proxy","proxy.mobile.att.net");
                        editor.putString("mms_port","80");
                    } else if (carrier.equals("Bell Canada"))
                    {
                        editor.putString("mmsc_url","http://mms.bell.ca/mms/wapenc");
                        editor.putString("mms_proxy","web.wireless.bell.ca");
                        editor.putString("mms_port","80");
                    } else if (carrier.equals("Free Mobile France"))
                    {
                        editor.putString("mmsc_url","http://212.27.40.225");
                        editor.putString("mms_proxy","212.27.40.225");
                        editor.putString("mms_port","80");
                    } else if (carrier.equals("Network Norway"))
                    {
                        editor.putString("mmsc_url","http://mms.nwn.no");
                        editor.putString("mms_proxy","188.149.250.10");
                        editor.putString("mms_port","80");
                    } else if (carrier.equals("Net10"))
                    {
                        editor.putString("mmsc_url","http://mms-tf.net");
                        editor.putString("mms_proxy","mms3.tracfone.com");
                        editor.putString("mms_port","80");
                    } else if (carrier.equals("O2"))
                    {
                        editor.putString("mmsc_url","http://mmsc.mms.02.co.uk:8002");
                        editor.putString("mms_proxy","193.113.200.195");
                        editor.putString("mms_port","8080");
                    } else if (carrier.equals("Rogers"))
                    {
                        editor.putString("mmsc_url","http://mms.gprs.rogers.com");
                        editor.putString("mms_proxy","10.128.1.69");
                        editor.putString("mms_port","8080");
                    } else if (carrier.equals("Sprint"))
                    {
                        editor.putString("mmsc_url","http://mms.sprintpcs.com");
                        editor.putString("mms_proxy","68.28.31.7");
                        editor.putString("mms_port","80");
                    } else if (carrier.equals("Straight Talk AT&T"))
                    {
                        editor.putString("mmsc_url","http://mmsc.cingular.com");
                        editor.putString("mms_proxy","66.209.11.33");
                        editor.putString("mms_port","80");
                    } else if (carrier.equals("Tele2"))
                    {
                        editor.putString("mmsc_url","http://mmsc.tele2.se");
                        editor.putString("mms_proxy","130.244.202.30");
                        editor.putString("mms_port","8080");
                    } else if (carrier.equals("Telus"))
                    {
                        editor.putString("mmsc_url","http://aliasredirect.net/proxy/mmsc");
                        editor.putString("mms_proxy","74.49.0.18");
                        editor.putString("mms_port","80");
                    } else if (carrier.equals("T-Mobile US"))
                    {
                        editor.putString("mmsc_url","http://mms.msg.eng.t-mobile.com/mms/wapenc");
                        editor.putString("mms_proxy","216.155.165.50");
                        editor.putString("mms_port","8080");
                    } else if (carrier.equals("T-Mobile Polish"))
                    {
                        editor.putString("mmsc_url","http://mms/servlets/mms");
                        editor.putString("mms_proxy","213.158.194.226");
                        editor.putString("mms_port","8080");
                    } else if (carrier.equals("Virgin Mobile Canada"))
                    {
                        editor.putString("mmsc_url","http://mms.bell.ca/mms/wapenc");
                        editor.putString("mms_proxy","web.wireless.bell.ca");
                        editor.putString("mms_port","80");
                    } else if (carrier.equals("Verizon Wireless"))
                    {
                        try {
                            String phoneNumber = getMyPhoneNumber().replace("+", "").replace("-","").replace(")","").replace("(", "").replace(" ", "");

                            if (phoneNumber.startsWith("+1"))
                            {
                                phoneNumber = phoneNumber.substring(2);
                            } else if (phoneNumber.startsWith("1") && phoneNumber.length() == 11)
                            {
                                phoneNumber = phoneNumber.substring(1);
                            }

                            editor.putString("mmsc_url","http://mms.vtext.com/servlets/mms?X-VZW-MDN=" + phoneNumber);
                            editor.putString("mms_proxy","null");
                            editor.putString("mms_port","8080");
                        } catch (Exception e) {
                            // mostlikely a tablet user without a phone number...
                        }
                    } else if (carrier.equals("Verizon Wireless #2"))
                    {
                        try {
                            String phoneNumber = getMyPhoneNumber().replace("+", "").replace("-","").replace(")","").replace("(", "").replace(" ", "");

                            if (phoneNumber.startsWith("+1"))
                            {
                                phoneNumber = phoneNumber.substring(2);
                            } else if (phoneNumber.startsWith("1") && phoneNumber.length() == 11)
                            {
                                phoneNumber = phoneNumber.substring(1);
                            }

                            editor.putString("mmsc_url","http://mms.vtext.com/servlets/mms?X-VZW-MDN=" + phoneNumber);
                            editor.putString("mms_proxy","null");
                            editor.putString("mms_port","80");
                        } catch (Exception e) {
                            // mostlikely a tablet user without a phone number...
                        }
                    } else if (carrier.equals("Vodafone AU"))
                    {
                        editor.putString("mmsc_url","http://pxt.vodafone.net.au/pxtsend");
                        editor.putString("mms_proxy","10.202.2.60");
                        editor.putString("mms_port","8080");
                    } else if (carrier.equals("Vodafone UK"))
                    {
                        editor.putString("mmsc_url","http://mms.vodafone.co.uk/servlets/mms");
                        editor.putString("mms_proxy","212.183.137.012");
                        editor.putString("mms_port","8799");
                    } else // fido canada... skipped it earlier haha
                    {
                        editor.putString("mmsc_url","http://mms.fido.ca");
                        editor.putString("mms_proxy","205.151.11.13");
                        editor.putString("mms_port","80");
                    }

                    editor.commit();
                }
            }

            private String getMyPhoneNumber(){
                TelephonyManager mTelephonyMgr;
                mTelephonyMgr = (TelephonyManager)
                        context.getSystemService(Context.TELEPHONY_SERVICE);
                return mTelephonyMgr.getLine1Number();
            }
        });

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
            }
        });

        onPageTreeChanged();
        updateBottomBar();
    }

    @Override
    public void onPageTreeChanged() {
        mCurrentPageSequence = mWizardModel.getCurrentPageSequence();
        recalculateCutOffPage();
        mStepPagerStrip.setPageCount(mCurrentPageSequence.size()); // + 1 = review step
        mPagerAdapter.notifyDataSetChanged();
        updateBottomBar();
    }

    private void updateBottomBar() {
        int position = mPager.getCurrentItem();
        if (position == mCurrentPageSequence.size() - 1) {
            mNextButton.setText(R.string.finish);
            mNextButton.setBackgroundResource(R.drawable.finish_background);
            mNextButton.setTextAppearance(this, R.style.TextAppearanceFinish);
        } else {
            mNextButton.setText(mEditingAfterReview
                    ? R.string.review
                    : R.string.next);
            mNextButton.setBackgroundResource(R.drawable.selectable_item_background);
            TypedValue v = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, v, true);
            mNextButton.setTextAppearance(this, v.resourceId);
            mNextButton.setEnabled(position != mPagerAdapter.getCutOffPage());
        }

        mPrevButton.setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWizardModel.unregisterListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("model", mWizardModel.save());
    }

    @Override
    public AbstractWizardModel onGetModel() {
        return mWizardModel;
    }

    @Override
    public void onEditScreenAfterReview(String key) {
        for (int i = mCurrentPageSequence.size(); i >= 0; i--) {
            if (mCurrentPageSequence.get(i).getKey().equals(key)) {
                mConsumePageSelectedEvent = true;
                mEditingAfterReview = true;
                mPager.setCurrentItem(i);
                updateBottomBar();
                break;
            }
        }
    }

    @Override
    public void onPageDataChanged(Page page) {
        if (page.isRequired()) {
            if (recalculateCutOffPage()) {
                mPagerAdapter.notifyDataSetChanged();
                updateBottomBar();
            }
        }
    }

    @Override
    public Page onGetPage(String key) {
        return mWizardModel.findByKey(key);
    }

    private boolean recalculateCutOffPage() {
        // Cut off the pager adapter at first required page that isn't completed
        int cutOffPage = mCurrentPageSequence.size();
        for (int i = 0; i < mCurrentPageSequence.size(); i++) {
            Page page = mCurrentPageSequence.get(i);
            if (page.isRequired() && !page.isCompleted()) {
                cutOffPage = i;
                break;
            }
        }

        if (mPagerAdapter.getCutOffPage() != cutOffPage) {
            mPagerAdapter.setCutOffPage(cutOffPage);
            return true;
        }

        return false;
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {
        private int mCutOffPage;
        private Fragment mPrimaryItem;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i >= mCurrentPageSequence.size()) {
                return new ReviewFragment();
            }

            return mCurrentPageSequence.get(i).createFragment();
        }

        @Override
        public int getItemPosition(Object object) {
            // TODO: be smarter about this
            if (object == mPrimaryItem) {
                // Re-use the current fragment (its position never changes)
                return POSITION_UNCHANGED;
            }

            return POSITION_NONE;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mPrimaryItem = (Fragment) object;
        }

        @Override
        public int getCount() {

            return 5;
        }

        public void setCutOffPage(int cutOffPage) {
            if (cutOffPage < 0) {
                cutOffPage = Integer.MAX_VALUE;
            }
            mCutOffPage = cutOffPage;
        }

        public int getCutOffPage() {
            return mCutOffPage;
        }
    }
}