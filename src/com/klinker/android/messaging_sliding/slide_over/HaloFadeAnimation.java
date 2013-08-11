package com.klinker.android.messaging_sliding.slide_over;

public class HaloFadeAnimation extends CustomAnimation {

    private HaloView view;
    private boolean fadeIn;

    public HaloFadeAnimation(HaloView v, boolean fadeIn) {
        super(v);

        this.view = v;
        this.fadeIn = fadeIn;
    }

    @Override
    public void updateView() {

        if (fadeIn) {
            view.haloAlpha -= 2;
            view.haloNewAlpha += 2;
        } else {
            view.haloAlpha += 2;
            view.haloNewAlpha -= 2;
        }

        boolean stop = false;

        if (fadeIn && view.haloAlpha <= 0) {
            view.haloAlpha = 0;
            view.haloNewAlpha = 255;
            stop = true;
        } else if (!fadeIn && view.haloAlpha >= 255) {
            view.haloAlpha = 255;
            view.haloNewAlpha = 0;
            stop = true;
        }

        if (stop) {
            setRunning(false);
            view.animating = false;
        }
    }
}