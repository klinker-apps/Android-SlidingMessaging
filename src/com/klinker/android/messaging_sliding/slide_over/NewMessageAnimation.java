package com.klinker.android.messaging_sliding.slide_over;

public class NewMessageAnimation extends CustomAnimation {

    private float speed;

    public NewMessageAnimation(AnimationView v, float speed) {
        super(v);

        this.speed = speed;
    }

    @Override
    public void updateView() {
        view.arcOffset -= speed;

        float nextText = (-1 * view.textPaint.measureText(view.name[0]));

        if (view.firstText && view.arcOffset <= nextText) {
            view.firstText = false;
            view.arcOffset = AnimationView.ORIG_ARC_OFFSET;
        }
    }
}
