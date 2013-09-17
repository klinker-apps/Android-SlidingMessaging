package com.klinker.android.messaging_sliding.views;

import android.app.Activity;
import android.widget.ProgressBar;

public class ProgressAnimator extends Thread {

    private int maxProgress;
    private int currentProgress;
    private Activity context;
    private ProgressBar mmsProgress;
    public boolean alreadyRunning;

    @Override
    public void run() {
        currentProgress = 0;
        maxProgress = 10;
        alreadyRunning = true;

        while (currentProgress < 100) {
            if (currentProgress > maxProgress) {
                try {
                    Thread.sleep(100);
                    continue;
                } catch (Exception e) {

                }
            } else {
                currentProgress += 2;

                context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                    @Override
                    public void run() {
                        mmsProgress.setProgress(currentProgress);
                    }

                });

                try {
                    Thread.sleep(30);
                } catch (Exception e) {

                }
            }
        }

        alreadyRunning = false;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    public void setMmsProgress(ProgressBar progress) {
        this.mmsProgress = progress;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }
}
