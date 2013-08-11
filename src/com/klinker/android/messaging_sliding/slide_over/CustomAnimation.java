package com.klinker.android.messaging_sliding.slide_over;

public abstract class CustomAnimation extends Thread {
    static final long FPS = 60;
    public AnimationView view;
    private boolean running = false;

    public CustomAnimation(AnimationView view) {
        super();
        this.view = view;
    }

    public void setRunning(boolean run) {
        running = run;
    }

    @Override
    public void run() {
        long ticksPS = 1000 / FPS;
        long startTime;
        long sleepTime;
        while (running) {
            startTime = System.currentTimeMillis();

            updateView();
            view.postInvalidate();

            sleepTime = ticksPS-(System.currentTimeMillis() - startTime);
            try {
                if (sleepTime > 0)
                    sleep(sleepTime);
            } catch (Exception e) {}
        }
    }

    public abstract void updateView();
}