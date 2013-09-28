package com.klinker.android.messaging_sliding.views;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.*;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.klinker.android.messaging_donate.utils.ContactUtil;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.MessageCursorAdapter;
import com.klinker.android.messaging_sliding.receivers.CacheService;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

import java.util.ArrayList;
import java.util.Calendar;

public class ConversationFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, PullToRefreshAttacher.OnRefreshListener {

    private int position;
    private String myId;
    private View view;
    private SharedPreferences sharedPrefs;
    private Context context;
    private Cursor messageQuery;
    private CustomListView listView;
    private Resources resources;

    public ProgressBar spinner;
    private PullToRefreshAttacher mPullToRefreshAttacher;

    public ConversationFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);

        Bundle args = this.getArguments();
        this.position = args.getInt("position");
        this.myId = args.getString("myId");

        this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        resources = context.getResources();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        try {
            if (!MainActivity.settings.cacheConversations || !CacheService.cached || !MainActivity.notChanged || !(position < MainActivity.settings.numOfCachedConversations)) {
                messageQuery.close();
            }
        } catch (Exception e) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.message_frame, container, false);

        if (MainActivity.settings.runAs.equals("card+")) {
            final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, resources
                    .getDisplayMetrics());
            final int marginTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, resources
                    .getDisplayMetrics());

            CustomListView list = (CustomListView) view.findViewById(R.id.fontListView);

            list.setBackgroundResource(R.drawable.background_card);
            list.setBackgroundColor(MainActivity.settings.ctSentMessageBackground);
            list.setOverScrollMode(View.OVER_SCROLL_NEVER);

            view.findViewById(R.id.messageBackground).setPadding(margin, marginTop, margin, marginTop);
        }

        mPullToRefreshAttacher = ((MainActivity) getActivity()).getPullToRefreshAttacher();

        return refreshMessages();
    }

    @SuppressWarnings("deprecation")
    public View refreshMessages() {
        final ContentResolver contentResolver = context.getContentResolver();

        final TextView groupList = (TextView) view.findViewById(R.id.groupList);

        if (MainActivity.conversations.get(position).getGroup()) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    final String name = ContactUtil.loadGroupContacts(ContactUtil.findContactNumber(MainActivity.conversations.get(position).getNumber(), context), context);

                    ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                        @Override
                        public void run() {
                            groupList.setText(name);
                        }
                    });

                }

            }).start();

            groupList.setTextColor(MainActivity.settings.titleBarTextColor);

            if (!MainActivity.settings.customTheme) {
                String titleColor = sharedPrefs.getString("title_color", "blue");

                if (titleColor.equals("blue")) {
                    groupList.setBackgroundColor(resources.getColor(R.color.holo_blue));
                } else if (titleColor.equals("orange")) {
                    groupList.setBackgroundColor(resources.getColor(R.color.holo_orange));
                } else if (titleColor.equals("red")) {
                    groupList.setBackgroundColor(resources.getColor(R.color.holo_red));
                } else if (titleColor.equals("green")) {
                    groupList.setBackgroundColor(resources.getColor(R.color.holo_green));
                } else if (titleColor.equals("purple")) {
                    groupList.setBackgroundColor(resources.getColor(R.color.holo_purple));
                } else if (titleColor.equals("grey")) {
                    groupList.setBackgroundColor(resources.getColor(R.color.grey));
                } else if (titleColor.equals("black")) {
                    groupList.setBackgroundColor(resources.getColor(R.color.pitch_black));
                } else if (titleColor.equals("darkgrey")) {
                    groupList.setBackgroundColor(resources.getColor(R.color.darkgrey));
                }
            } else {
                groupList.setBackgroundColor(MainActivity.settings.titleBarColor);
            }
        } else {
            groupList.setHeight(0);
        }

        listView = (CustomListView) view.findViewById(R.id.fontListView);

        spinner = (ProgressBar) view.findViewById(R.id.emptyView);

        if (MainActivity.waitToLoad) {
            spinner.setVisibility(View.VISIBLE);
        } else {
            spinner.setVisibility(View.GONE);
        }

        if (MainActivity.settings.cacheConversations && CacheService.cached && MainActivity.notChanged && position < MainActivity.settings.numOfCachedConversations) {
            MainActivity.threadedLoad = false;
        }

        MainActivity.numToLoad = 20;

        if (MainActivity.threadedLoad) {
            if (MainActivity.waitToLoad) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {

                        }

                        MainActivity.waitToLoad = false;

                        Uri uri3 = Uri.parse("content://mms-sms/conversations/" + MainActivity.conversations.get(position).getThreadId() + "/");
                        String[] projection2;
                        String proj = "_id body date type read msg_box locked sub";

                        if (MainActivity.settings.showOriginalTimestamp) {
                            proj += " date_sent";
                        }

                        if (MainActivity.settings.deliveryReports) {
                            proj += " status";
                        }

                        projection2 = proj.split(" ");

                        String sortOrder = "normalized_date desc";

                        if (MainActivity.settings.limitMessages) {
                            sortOrder += " limit " + MainActivity.numToLoad;
                        }

                        messageQuery = contentResolver.query(uri3, projection2, null, null, sortOrder);

                        ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                            @Override
                            public void run() {

                                MessageCursorAdapter adapter = new MessageCursorAdapter((Activity) context, myId, ContactUtil.findContactNumber(MainActivity.conversations.get(position).getNumber(), context), MainActivity.conversations.get(position).getThreadId(), messageQuery, position);

                                listView.setAdapter(adapter);

                                listView.setStackFromBottom(true);
                                spinner.setVisibility(View.GONE);

                                listView.setOnSizeChangedListener(new CustomListView.OnSizeChangedListener() {
                                    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
                                        smoothScrollToEnd(false, height - oldHeight);
                                    }
                                });
                            }

                        });

                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {

                        }

                        ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    listView.setSelection(messageQuery.getCount() - 1);
                                } catch (Exception e) {

                                }
                            }

                        });

                    }

                }).start();
            } else {
                getLoaderManager().restartLoader(position, null, this);
            }
        } else {
            getLoaderManager().destroyLoader(position);

            Uri uri3 = Uri.parse("content://mms-sms/conversations/" + MainActivity.conversations.get(position).getThreadId() + "/");
            String[] projection2;
            String proj = "_id body date type read msg_box locked sub";

            if (MainActivity.settings.showOriginalTimestamp) {
                proj += " date_sent";
            }

            if (MainActivity.settings.deliveryReports || MainActivity.settings.voiceAccount != null) {
                proj += " status";
            }

            projection2 = proj.split(" ");

            String sortOrder = "normalized_date desc";

            if (MainActivity.settings.limitMessages) {
                sortOrder += " limit " + MainActivity.numToLoad;
            }

            if (!MainActivity.settings.cacheConversations || !CacheService.cached || !MainActivity.notChanged || !(position < MainActivity.settings.numOfCachedConversations)) {
                messageQuery = contentResolver.query(uri3, projection2, null, null, sortOrder);
            } else {
                messageQuery = CacheService.conversations.get(position);
            }

            MessageCursorAdapter adapter = new MessageCursorAdapter((Activity) context, myId, ContactUtil.findContactNumber(MainActivity.conversations.get(position).getNumber(), context), MainActivity.conversations.get(position).getThreadId(), messageQuery, position);

            listView.setAdapter(adapter);
            listView.setStackFromBottom(true);
            spinner.setVisibility(View.GONE);

            listView.setOnSizeChangedListener(new CustomListView.OnSizeChangedListener() {
                public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
                    smoothScrollToEnd(false, height - oldHeight);
                }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    MainActivity.threadedLoad = true;
                }
            }, 100L);
        }

        listView.setDivider(new ColorDrawable(MainActivity.settings.messageDividerColor));

        if (MainActivity.settings.messageDividerVisible && MainActivity.settings.runAs.equals("sliding")) {
            listView.setDividerHeight(1);
        } else if (MainActivity.settings.runAs.equals("card+") && MainActivity.settings.messageDividerVisible) {
            listView.setDivider(resources.getDrawable(R.drawable.card_plus_divider));
        } else {
            listView.setDividerHeight(0);
        }

        final PullToRefreshAttacher.OnRefreshListener refreshListener = this;

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if (position != MainActivity.pullToRefreshPosition) {
                    mPullToRefreshAttacher.setRefreshableView(listView, refreshListener);
                    MainActivity.pullToRefreshPosition = position;
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {

            }
        });

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        Uri uri3 = Uri.parse("content://mms-sms/conversations/" + MainActivity.conversations.get(position).getThreadId() + "/");
        String[] projection2;
        String proj = "_id body date type read msg_box locked sub";

        if (MainActivity.settings.showOriginalTimestamp) {
            proj += " date_sent";
        }

        if (MainActivity.settings.deliveryReports || MainActivity.settings.voiceAccount != null) {
            proj += " status";
        }

        projection2 = proj.split(" ");

        String sortOrder = "normalized_date desc";

        if (MainActivity.settings.limitMessages) {
            sortOrder += " limit " + MainActivity.numToLoad;
        }

        return new CursorLoader(
                context,
                uri3,
                projection2,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor query) {
        MessageCursorAdapter adapter = new MessageCursorAdapter((Activity) context, myId, ContactUtil.findContactNumber(MainActivity.conversations.get(position).getNumber(), context), MainActivity.conversations.get(position).getThreadId(), query, position);

        listView.setAdapter(adapter);
        listView.setStackFromBottom(true);
        spinner.setVisibility(View.GONE);

        listView.setOnSizeChangedListener(new CustomListView.OnSizeChangedListener() {
            public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
                smoothScrollToEnd(false, height - oldHeight);
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    listView.setSelection(query.getCount() - 1);
                } catch (Exception e) {

                }
            }
        }, 500);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onRefreshStarted(View view) {

        MainActivity.numToLoad += 20;

        new AsyncTask<Void, Void, Void>() {

            private Cursor query;

            @Override
            protected Void doInBackground(Void... params) {
                long startTime = Calendar.getInstance().getTimeInMillis();

                Uri uri3 = Uri.parse("content://mms-sms/conversations/" + MainActivity.conversations.get(position).getThreadId() + "/");
                String[] projection2;
                String proj = "_id body date type read msg_box locked sub";

                if (MainActivity.settings.showOriginalTimestamp) {
                    proj += " date_sent";
                }

                if (MainActivity.settings.deliveryReports || MainActivity.settings.voiceAccount != null) {
                    proj += " status";
                }

                projection2 = proj.split(" ");

                String sortOrder = "normalized_date desc";

                if (MainActivity.settings.limitMessages) {
                    sortOrder += " limit " + MainActivity.numToLoad;
                }

                query = context.getContentResolver().query(uri3, projection2, null, null, sortOrder);

                // time for a cool animation ;)
                if (Calendar.getInstance().getTimeInMillis() - startTime < 500) {
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {

                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                MessageCursorAdapter adapter = new MessageCursorAdapter((Activity) context, myId, ContactUtil.findContactNumber(MainActivity.conversations.get(position).getNumber(), context), MainActivity.conversations.get(position).getThreadId(), query, position);

                listView.setAdapter(adapter);
                listView.setStackFromBottom(true);
                spinner.setVisibility(View.GONE);

                listView.setOnSizeChangedListener(new CustomListView.OnSizeChangedListener() {
                    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
                        smoothScrollToEnd(false, height - oldHeight);
                    }
                });

                listView.setSelection(adapter.getCount() - MainActivity.numToLoad + 20);

                // Notify PullToRefreshAttacher that the refresh has finished
                mPullToRefreshAttacher.setRefreshComplete();
            }
        }.execute();
    }

    private int mLastSmoothScrollPosition;

    private void smoothScrollToEnd(boolean force, int listSizeChange) {
        int lastItemVisible = listView.getLastVisiblePosition();
        int lastItemInList = listView.getAdapter().getCount() - 1;
        if (lastItemVisible < 0 || lastItemInList < 0) {
            return;
        }

        View lastChildVisible =
                listView.getChildAt(lastItemVisible - listView.getFirstVisiblePosition());
        int lastVisibleItemBottom = 0;
        int lastVisibleItemHeight = 0;
        if (lastChildVisible != null) {
            lastVisibleItemBottom = lastChildVisible.getBottom();
            lastVisibleItemHeight = lastChildVisible.getHeight();
        }

        int listHeight = listView.getHeight();
        boolean lastItemTooTall = lastVisibleItemHeight > listHeight;
        boolean willScroll = force ||
                ((listSizeChange != 0 || lastItemInList != mLastSmoothScrollPosition) &&
                        lastVisibleItemBottom + listSizeChange <=
                                listHeight - listView.getPaddingBottom());
        if (willScroll || (lastItemTooTall && lastItemInList == lastItemVisible)) {
            if (Math.abs(listSizeChange) > 200) {
                if (lastItemTooTall) {
                    listView.setSelectionFromTop(lastItemInList,
                            listHeight - lastVisibleItemHeight);
                } else {
                    listView.setSelection(lastItemInList);
                }
            } else if (lastItemInList - lastItemVisible > 20) {
                listView.setSelection(lastItemInList);
            } else {
                if (lastItemTooTall) {
                    listView.setSelectionFromTop(lastItemInList,
                            listHeight - lastVisibleItemHeight);
                } else {
                    listView.smoothScrollToPosition(lastItemInList);
                }
                mLastSmoothScrollPosition = lastItemInList;
            }
        }
    }
}