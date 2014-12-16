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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.Util;
import com.klinker.android.messaging_donate.wizardpager.wizard.model.AbstractWizardModel;
import com.klinker.android.messaging_donate.wizardpager.wizard.model.ModelCallbacks;
import com.klinker.android.messaging_donate.wizardpager.wizard.model.Page;
import com.klinker.android.messaging_donate.wizardpager.wizard.ui.PageFragmentCallbacks;
import com.klinker.android.messaging_donate.wizardpager.wizard.ui.ReviewFragment;
import com.klinker.android.messaging_donate.wizardpager.wizard.ui.StepPagerStrip;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ChangeLogMain extends FragmentActivity implements
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wizard_main);

        mWizardModel = new ChangeLogWizardModel(getBaseContext());
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
            @Override
            public void onClick(View view) {
                if (mPager.getCurrentItem() == mCurrentPageSequence.size()) {

                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

                    String version = "";

                    try {
                        version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    SharedPreferences.Editor prefEdit = sharedPrefs.edit();
                    prefEdit.putString("current_version", version);

                    prefEdit.commit();

                    boolean flag = false;

                    if (fromIntent.getStringExtra("com.klinker.android.OPEN") != null) {
                        flag = true;
                    }

                    final Intent intent = new Intent(context, MainActivity.class);
                    intent.setAction(fromIntent.getAction());
                    intent.setData(fromIntent.getData());

                    try {
                        intent.putExtras(fromIntent.getExtras());
                    } catch (Exception e) {

                    }

                    if (flag) {
                        intent.putExtra("com.klinker.android.OPEN", intent.getStringExtra("com.klinker.android.OPEN"));
                    }

                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();

                } else {
                    try {
                        if (mEditingAfterReview) {
                            mPager.setCurrentItem(mPagerAdapter.getCount() - 1);
                        } else {
                            mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                        }
                    } catch (Exception e) {
                        // seems to not like this every once and awhile on some phones, something about notifying data set changed, but we aren't changing anything?
                    }
                }
            }
        });

        mPrevButton.setText(getResources().getString(R.string.skip_log));
        mPrevButton.setBackgroundColor(getResources().getColor(R.color.review_green));
        mPrevButton.setTextColor(getResources().getColor(R.color.white));

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

                String version = "";

                try {
                    version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                SharedPreferences.Editor prefEdit = sharedPrefs.edit();
                prefEdit.putString("current_version", version);

                prefEdit.commit();

                boolean flag = false;

                if (fromIntent.getStringExtra("com.klinker.android.OPEN") != null) {
                    flag = true;
                }

                final Intent intent = new Intent(context, MainActivity.class);
                intent.setAction(fromIntent.getAction());
                intent.setData(fromIntent.getData());

                try {
                    intent.putExtras(fromIntent.getExtras());
                } catch (Exception e) {

                }

                if (flag) {
                    intent.putExtra("com.klinker.android.OPEN", intent.getStringExtra("com.klinker.android.OPEN"));
                }

                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            }
        });

        onPageTreeChanged();
        updateBottomBar();

        Util.checkOverride(context);
    }

    @Override
    public void onPageTreeChanged() {
        mCurrentPageSequence = mWizardModel.getCurrentPageSequence();
        recalculateCutOffPage();
        mStepPagerStrip.setPageCount(mCurrentPageSequence.size() + 1); // + 1 = review step
        mPagerAdapter.notifyDataSetChanged();
        updateBottomBar();
    }

    private void updateBottomBar() {
        int position = mPager.getCurrentItem();
        if (position == mCurrentPageSequence.size()) {
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

        //mPrevButton.setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);
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
        for (int i = mCurrentPageSequence.size() - 1; i >= 0; i--) {
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
        try {
            return mWizardModel.findByKey(key);
        } catch (Exception e) {
            finish();
            return null;
        }
    }

    private boolean recalculateCutOffPage() {
        // Cut off the pager adapter at first required page that isn't completed
        int cutOffPage = mCurrentPageSequence.size() + 1;
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
            int extraPageCount = 1;

            try {
                PackageManager pm = getBaseContext().getPackageManager();
                pm.getPackageInfo("com.klinker.android.messaging_theme", PackageManager.GET_ACTIVITIES);
            } catch (Exception e) {
                extraPageCount++;
            }

            try {
                PackageManager pm = getBaseContext().getPackageManager();
                pm.getPackageInfo("com.klinker.android.emoji_keyboard_trial", PackageManager.GET_ACTIVITIES);
            } catch (Exception e) {
                extraPageCount++;
            }

            try {
                PackageManager pm = getBaseContext().getPackageManager();
                pm.getPackageInfo("com.jb.gosms", PackageManager.GET_ACTIVITIES);
            } catch (Exception e) {
                extraPageCount--;
            }

            return 2 + extraPageCount;
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