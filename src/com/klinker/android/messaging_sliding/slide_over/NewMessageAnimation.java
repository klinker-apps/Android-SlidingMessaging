package com.klinker.android.messaging_sliding.slide_over;

import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

public class NewMessageAnimation extends CustomAnimation {

    private float speed;
    private WindowManager manager;

    public NewMessageAnimation(AnimationView v, float speed, WindowManager manager) {
        super(v);

        this.speed = speed;
        this.manager = manager;
    }

    @Override
    public void updateView() {
        view.arcOffset -= speed;

        float nextText;

        if (view.firstText) {
            nextText = (-1 * view.textPaint.measureText(view.name[0]));
        } else {
            nextText = (-1 * view.textPaint.measureText(view.name[1]));
        }

        if (view.firstText && view.arcOffset <= nextText) {
            view.firstText = false;
            view.arcOffset = AnimationView.ORIG_ARC_OFFSET;
        } else if (!view.firstText && view.arcOffset <= nextText) {
            final NewMessageAnimation anim = this;

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    anim.setRunning(false);
                    view.circleText = false;
                    try {
                        manager.removeViewImmediate(view);
                    } catch (Exception e) {

                    }
                }
            });
        }
    }
}
