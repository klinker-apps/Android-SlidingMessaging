/*
 * Copyright 2013 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.messaging_sliding.backup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.ViewSwitcher;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.IOUtil;
import com.klinker.android.messaging_donate.utils.Util;
import com.klinker.android.messaging_sliding.views.HoloTextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Calendar;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class DropboxActivity extends Activity {

    private static final String APP_KEY = "1za95hzw3jszlxa";
    private static final String APP_SECRET = "qhctnkhmjxx1tn1";
    private static final Session.AccessType ACCESS_TYPE = Session.AccessType.APP_FOLDER;

    private static final String FILE_NAME = "/sms_backup.txt";
    private static final long MIN_WAIT_TIME = 1500;
    public static final String[] BACKUP_PROJECTION = new String[]{
            "date",
            "date_sent",
            "type",
            "read",
            "status",
            "error_code",
            "address",
            "body",
            "locked"
    };

    private DropboxAPI<AndroidAuthSession> mDBApi;

    private Context mContext;
    private TextSwitcher title;
    private TextSwitcher summary;
    private TextSwitcher progressText;
    private SmoothProgressBar progressBar;
    private Button backupButton;
    private Button restoreButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dropbox_activity);

        mContext = this;

        title = (TextSwitcher) findViewById(R.id.title);
        summary = (TextSwitcher) findViewById(R.id.info);
        progressText = (TextSwitcher) findViewById(R.id.progress_text);
        progressBar = (SmoothProgressBar) findViewById(R.id.progress_bar);
        backupButton = (Button) findViewById(R.id.backup_button);
        restoreButton = (Button) findViewById(R.id.restore_button);

        progressBar.setSmoothProgressDrawableColor(getResources().getColor(R.color.dropbox_blue));

        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

        title.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                HoloTextView myText = new HoloTextView(mContext);
                myText.setTextSize(30);
                return myText;
            }
        });

        title.setInAnimation(in);
        title.setOutAnimation(out);

        summary.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                HoloTextView myText = new HoloTextView(mContext);
                myText.setTextSize(20);
                return myText;
            }
        });

        summary.setInAnimation(in);
        summary.setOutAnimation(out);

        progressText.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                HoloTextView myText = new HoloTextView(mContext);
                myText.setTextSize(17);
                return myText;
            }
        });

        progressText.setInAnimation(in);
        progressText.setOutAnimation(out);

        title.setText(getString(R.string.dropbox_sync));
        summary.setText(getString(R.string.dropbox_summary));
        progressText.setVisibility(View.GONE);

        progressBar.setProgress(100);

        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getString("db_token_key", "").equals("")) {
            mDBApi.getSession().startAuthentication(this);
        } else {
            AccessTokenPair token = new AccessTokenPair(
                    prefs.getString("db_token_key", ""),
                    prefs.getString("db_token_secret", "")
            );
            mDBApi.getSession().setAccessTokenPair(token);
        }

        backupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(DropboxActivity.this)
                        .setTitle(getString(R.string.dropbox_sync))
                        .setMessage(getString(R.string.dropbox_overwrite_backup))
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                progressText.setVisibility(View.VISIBLE);
                                backupButton.setEnabled(false);
                                restoreButton.setEnabled(false);
                                progressBar.setIndeterminate(true);
                                summary.setText("");
                                progressText.setText(getString(R.string.dropbox_downloading_previous_backup));

                                new WriteAsyncTask().execute("overwrite");
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                progressText.setVisibility(View.VISIBLE);
                                backupButton.setEnabled(false);
                                restoreButton.setEnabled(false);
                                progressBar.setIndeterminate(true);
                                summary.setText("");
                                progressText.setText(getString(R.string.dropbox_downloading_previous_backup));

                                new WriteAsyncTask().execute("append");
                            }
                        })
                        .show();
            }
        });

        restoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressText.setVisibility(View.VISIBLE);
                backupButton.setEnabled(false);
                restoreButton.setEnabled(false);
                progressBar.setIndeterminate(true);
                summary.setText("");
                progressText.setText(getString(R.string.dropbox_downloading_previous_backup));

                new ReadAsyncTask().execute();
            }
        });

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                mDBApi.getSession().finishAuthentication();
                AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putString("db_token_key", tokens.key)
                        .putString("db_token_secret", tokens.secret)
                        .commit();
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }

        if (!Util.isDefaultSmsApp(mContext)) {
            Util.setDefaultSmsApp(mContext);
        }
    }

    private void processBackup(WriteAsyncTask task, File file, boolean overwrite) {
        findViewById(android.R.id.content).post(new Runnable() {
            @Override
            public void run() {
                progressText.setText(getString(R.string.dropbox_finding_messages));
            }
        });

        long start = Calendar.getInstance().getTimeInMillis();
        Cursor query = getContentResolver().query(Uri.parse("content://sms/"), BACKUP_PROJECTION, null, null, null);
        long finish = Calendar.getInstance().getTimeInMillis();

        if (finish - start < MIN_WAIT_TIME) {
            try {
                Thread.sleep(MIN_WAIT_TIME - (finish - start));
            } catch (Exception e) {
            }
        }

        findViewById(android.R.id.content).post(new Runnable() {
            @Override
            public void run() {
                progressBar.setIndeterminate(false);
                progressBar.setProgress(0);
                progressText.setText(getString(R.string.dropbox_backing_up));
            }
        });

        IOUtil.writeSmsCursor(file, query, task, overwrite);
        query.close();

        findViewById(android.R.id.content).post(new Runnable() {
            @Override
            public void run() {
                progressText.setText(getString(R.string.dropbox_writing));
                progressBar.setIndeterminate(true);
            }
        });

        try {
            start = Calendar.getInstance().getTimeInMillis();
            FileInputStream inputStream = new FileInputStream(file);
            DropboxAPI.Entry response = mDBApi.putFileOverwrite(FILE_NAME, inputStream,
                    file.length(), null);
            file.delete();
            finish = Calendar.getInstance().getTimeInMillis();

            if (finish - start < MIN_WAIT_TIME) {
                try {
                    Thread.sleep(MIN_WAIT_TIME - (finish - start));
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processRestore(ReadAsyncTask task, File file) {
        findViewById(android.R.id.content).post(new Runnable() {
            @Override
            public void run() {
                progressBar.setIndeterminate(false);
                progressBar.setProgress(0);
                progressText.setText(getString(R.string.dropbox_processing_backup));
            }
        });

        IOUtil.readSmsFile(file, task, this);
    }

    public class WriteAsyncTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            for (String param : params) {
                File file = null;

                try {
                    // try to load in the file we want to use for the initial backup, if it doesn't exist then
                    // no big deal
                    file = new File(Environment.getExternalStorageDirectory() + "/EvolveSMS" + FILE_NAME);
                    FileOutputStream outputStream = new FileOutputStream(file);
                    DropboxAPI.DropboxFileInfo info = mDBApi.getFile(FILE_NAME, null, outputStream, null);
                    Log.i("dropbox_evolve", "The file's rev is: " + info.getMetadata().rev);
                } catch (DropboxServerException e) {
                    e.printStackTrace();
                } catch (DropboxException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                processBackup(this, file, param.equals("overwrite"));
            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setProgress(100);
            progressBar.setIndeterminate(false);
            progressText.setText(getString(R.string.setup_finished));
            title.setText(getString(R.string.finished_setup));
            summary.setText(getString(R.string.dropbox_finished));
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        public void onProgressUpdate(Integer... values) {
            for (Integer value : values) {
                progressBar.setProgress(value.intValue());
            }
        }
    }

    public class ReadAsyncTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            File file = null;

            try {
                file = new File(Environment.getExternalStorageDirectory() + "/EvolveSMS" + FILE_NAME);
                FileOutputStream outputStream = new FileOutputStream(file);
                DropboxAPI.DropboxFileInfo info = mDBApi.getFile(FILE_NAME, null, outputStream, null);
                Log.i("dropbox_evolve", "The file's rev is: " + info.getMetadata().rev);
            } catch (DropboxServerException e) {
                // this is where to notify user if dropbox file does not exist and quit out.
                return "no_backup";
            } catch (DropboxException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            processRestore(this, file);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setProgress(100);
            progressBar.setIndeterminate(false);
            progressText.setText(getString(R.string.setup_finished));
            title.setText(getString(R.string.finished_setup));

            if (!result.equals("no_backup")) {
                summary.setText(getString(R.string.dropbox_finished));
            } else {
                summary.setText(getString(R.string.dropbox_no_backup_found));
                backupButton.setEnabled(true);
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        public void onProgressUpdate(Integer... values) {
            for (Integer value : values) {
                progressBar.setProgress(value.intValue());
            }
        }
    }
}
