package com.klinker.android.messaging_sliding.slide_over;

import android.app.*;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.IOUtil;
import com.klinker.android.messaging_sliding.quick_reply.QmMarkRead2;
import com.klinker.android.messaging_sliding.receivers.NotificationRepeaterService;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Transaction;

import java.util.ArrayList;
import java.util.List;

public class SlideOverService extends Service {

    public static final String BCAST_CONFIGCHANGED = "android.intent.action.CONFIGURATION_CHANGED";

    public static DisplayMetrics displayMatrix;

    public HaloView haloView;
    public MessageView messageView;
    public ContactView contactView;
    public ArcView arcView;
    public AnimationView animationView;
    public View sendView;

    public WindowManager.LayoutParams haloParams;
    public WindowManager.LayoutParams haloHiddenParams;
    public WindowManager.LayoutParams messageWindowParams;
    public WindowManager.LayoutParams contactParams;
    public WindowManager.LayoutParams arcParams;
    public WindowManager.LayoutParams arcParamsNoBack;
    public WindowManager.LayoutParams animationParams;
    public WindowManager.LayoutParams sendParams;
    public WindowManager.LayoutParams sendParamsFocused;

    private GestureDetector mGestureDetector;

    private EditText sendBox;
    private ImageButton send;
    private ImageButton cancel;

    public Context mContext;

    public Vibrator v;

    public Bitmap halo;

    public WindowManager haloWindow;
    public WindowManager messageWindow;
    public WindowManager arcWindow;
    public WindowManager animationWindow;
    public WindowManager sendWindow;

    public SharedPreferences sharedPrefs;

    public static int SWIPE_MIN_DISTANCE = 0;
    public static double HALO_SLIVER_RATIO = .33;
    public static double PERCENT_DOWN_SCREEN = 0;
    public static float ARC_BREAK_POINT = 0;

    public static final int START_ALPHA = 60;
    public static final int START_ALPHA2 = 120;
    public static final int TOUCHED_ALPHA = 230;

    public static boolean HAPTIC_FEEDBACK = true;

    public int numberNewConv;

    private boolean needDetection = false;
    private boolean vibrateNeeded = true;
    private boolean inDash = false;
    private boolean inFlat = true;
    private boolean initial = true;
    private boolean zoneChange = false;
    private boolean fromDash = true;
    private boolean inButtons = false;
    private boolean topVibrate = true;
    private boolean inClose = false;
    private boolean inMove = false;
    private boolean inClear = false;

    private boolean movingBubble = false;
    private boolean changingSliver = false;
    private boolean actionButtonTouched = false;
    private boolean draggingQuickPeek = false;
    private boolean quickPeekHidden = false;
    private boolean initialOnDown = false;
    private boolean contactPictureTouched = false;

    private boolean enableQuickPeek;

    public int currContact = 0;
    public int originalPos = 0;

    private int lastZone = 0;
    private int currentZone = 0;
    private int zoneWidth;

    private float initX;
    private float initY;

    private double xPortion;
    private double yPortion;

    private double distance = 0;
    private double angle = 0;

    private int windowOffsetY;

    public Handler messageBoxHandler = new Handler();
    public Handler arcViewHandler = new Handler();
    public Handler removeArcHandler = new Handler();
    public Runnable messageBoxRunnable;
    public Runnable arcViewRunnable;
    public Runnable removeArcRunnable;

    private Transaction sendTransaction;

    @Override
    public void onCreate() {
        super.onCreate();

        // gets the display
        Display d = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final int height = d.getHeight();
        final int width = d.getWidth();

        displayMatrix = getResources().getDisplayMetrics();

        halo = BitmapFactory.decodeResource(getResources(),
                R.drawable.halo_bg);

        initialSetup(halo, height, width);

        haloView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                mGestureDetector.onTouchEvent(event);

                if ((event.getX() > haloView.getX() && event.getX() < haloView.getX() + halo.getWidth() && event.getY() > haloView.getY() && event.getY() < haloView.getY() + halo.getHeight()) || needDetection) {
                    final int type = event.getActionMasked();

                    if (numberNewConv == 0) { // no messages to display
                        switch (type) {
                            case MotionEvent.ACTION_DOWN:

                                onDown(event);
                                return true;

                            case MotionEvent.ACTION_MOVE:

                                if (changingSliver) {
                                    changeSliverWidth(halo, event, width);
                                } else if (movingBubble) {
                                    movingHalo(halo, event);
                                } else {
                                    noMessagesMove(event, height, width);
                                }

                                return true;

                            case MotionEvent.ACTION_UP:

                                if (changingSliver) {
                                    setSliver(halo, event, height, width);
                                    changingSliver = false;
                                } else if (movingBubble) {
                                    setHalo(halo, event, height, width);
                                    movingBubble = false;
                                } else {
                                    noMessagesUp();
                                }

                                removeArcHandler.removeCallbacks(removeArcRunnable);

                                return true;
                        }
                    } else // if they have a new message to display
                    {
                        zoneWidth = (width - SWIPE_MIN_DISTANCE) / (numberNewConv);

                        switch (type) {
                            case MotionEvent.ACTION_DOWN:

                                onDown(event);
                                return true;

                            case MotionEvent.ACTION_MOVE:

                                if (changingSliver) {
                                    changeSliverWidth(halo, event, width);
                                } else if (movingBubble) {
                                    movingHalo(halo, event);
                                } else {
                                    messagesMove(event, height, width, zoneWidth);
                                }

                                return true;

                            case MotionEvent.ACTION_UP:
                                if (changingSliver) {
                                    setSliver(halo, event, height, width);
                                    changingSliver = false;
                                } else if (movingBubble) {
                                    setHalo(halo, event, height, width);
                                    movingBubble = false;
                                } else {
                                    messagesUp();
                                }

                                removeArcHandler.removeCallbacks(removeArcRunnable);

                                return true;
                        }
                    }

                }

                return false;
            }
        });

        messageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                try { sendWindow.updateViewLayout(sendView, sendParams); } catch (Exception e) { }
                sendBox.clearFocus();
                //TODO shutdown keyboard
                messageViewTouched(motionEvent, height, width);
                /*sendWindow.updateViewLayout(sendView, sendParams);
                EditText edit = (EditText) sendView.findViewById(R.id.message_entry_slideover);
                edit.clearFocus();*/

                return false;
            }
        });

        sendBox.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                sendWindow.updateViewLayout(sendView, sendParamsFocused);
                sendBox.requestFocus();
                //TODO shutdown keyboard
                //messageViewTouched(motionEvent, height, width);
                /*sendWindow.updateViewLayout(sendView, sendParams);
                EditText edit = (EditText) sendView.findViewById(R.id.message_entry_slideover);
                edit.clearFocus();*/

                return false;
            }
        });

        contactView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                try { sendWindow.updateViewLayout(sendView, sendParams); } catch (Exception e) { }
                sendBox.clearFocus();

                contactViewTouched(event, height, width);
                /*sendWindow.updateViewLayout(sendView, sendParamsFocused);
                EditText edit = (EditText) sendView.findViewById(R.id.message_entry_slideover);
                edit.requestFocus();*/

                return false;
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sendBox.getText().toString().equals("")) {
                    Message mMessage = new Message(sendBox.getText().toString(), ContactView.numbers[ContactView.currentContact]);
                    sendTransaction.sendNewMessage(mMessage, Long.parseLong(ContactView.threadIds[ContactView.currentContact]));

                    sendBox.setText("");
                    sendBox.clearFocus();
                    try { sendWindow.updateViewLayout(sendView, sendParams); } catch (Exception e) { }

                    ContactView.currentContact = 0;
                    currContact = 0;
                    ContactView.refreshArrays();
                    contactView.invalidate();
                    messageView.invalidate();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendBox.setText("");
                sendBox.clearFocus();
                try { sendWindow.updateViewLayout(sendView, sendParams); } catch (Exception e) { }
            }
        });

        haloWindow.addView(haloView, haloParams);

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.klinker.android.messaging.STOP_HALO");
        registerReceiver(stopSlideover, filter);

        filter = new IntentFilter();
        filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.registerReceiver(mBroadcastReceiver, filter);

        filter = new IntentFilter();
        filter.addAction("com.klinker.android.messaging.UPDATE_HALO");
        this.registerReceiver(newMessageReceived, filter);

        filter = new IntentFilter();
        filter.addAction("com.klinker.android.messaging.CLEAR_MESSAGES");
        this.registerReceiver(clearMessages, filter);

        filter = new IntentFilter();
        filter.addAction(BCAST_CONFIGCHANGED);
        this.registerReceiver(orientationChange, filter);
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {

            if (enableQuickPeek) {
                haloView.playSoundEffect(SoundEffectConstants.CLICK);

                currContact = 0;
                ContactView.currentContact = 0;

                if (HAPTIC_FEEDBACK) {
                    v.vibrate(10);
                }

                // will launch the floating message box feature
                try {

                    messageWindow.removeView(contactView);

                    try {
                        messageWindow.removeView(messageView);
                        sendWindow.removeView(sendView);
                    } catch (Exception x) {
                        // message view is gone already
                    }

                    messageBoxHandler.removeCallbacks(messageBoxRunnable);

                    if (sharedPrefs.getBoolean("slideover_only_unread", false))
                        startService(new Intent(getBaseContext(), QmMarkRead2.class));

                    ContactView.refreshArrays();

                    contactView.invalidate();
                    messageView.invalidate();
                } catch (Exception e) {

                    ContactView.refreshArrays();

                    contactView.invalidate();
                    messageView.invalidate();

                    messageWindow.addView(contactView, contactParams);
                    messageWindow.addView(messageView, messageWindowParams);
                    sendWindow.addView(sendView, sendParams);
                }

                quickPeekHidden = false;

                arcView.newConversations.clear();

                haloView.haloNewAlpha = 0;
                haloView.haloAlpha = 255;
                haloView.invalidate();

                try {
                    haloWindow.removeViewImmediate(haloView);
                    haloWindow.addView(haloView, haloParams);
                } catch (Exception e) {

                }

                numberNewConv = 0;
            }

            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {

            if (!sharedPrefs.getBoolean("slideover_disable_sliver_drag", false)) {
                haloView.playSoundEffect(SoundEffectConstants.CLICK);

                if (HAPTIC_FEEDBACK) {
                    v.vibrate(10);
                }

                // change sliver width
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            arcWindow.removeViewImmediate(arcView);
                        } catch (Exception e) {

                        }
                    }
                }, 220);

                changingSliver = true;
            }

            return true;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            // Move slideover bubble
            if (!sharedPrefs.getBoolean("slideover_disable_drag", false)) {
                try {
                    arcWindow.removeViewImmediate(arcView);
                } catch (Exception e) {

                }

                if (!changingSliver) {

                    haloView.playSoundEffect(SoundEffectConstants.CLICK);
                    if (HAPTIC_FEEDBACK) {
                        v.vibrate(10);
                    }

                    movingBubble = true;
                }
            }
        }
    }

    public void initialSetup(Bitmap halo, int height, int width) {
        mContext = this;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        enableQuickPeek = sharedPrefs.getBoolean("enable_quick_peek", true);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        int breakAngle = sharedPrefs.getInt("slideover_break_point", 33);
        float breakAng = 0;
        breakAng += breakAngle;

        HALO_SLIVER_RATIO = sharedPrefs.getInt("slideover_sliver", 33) / 100.0;
        PERCENT_DOWN_SCREEN = sharedPrefs.getFloat("slideover_downscreen", 0);
        HAPTIC_FEEDBACK = sharedPrefs.getBoolean("slideover_haptic_feedback", true);
        ARC_BREAK_POINT = breakAng * .9f;

        setParams(halo, height, width);

        if (width > height)
            SWIPE_MIN_DISTANCE = (int) (height * (sharedPrefs.getInt("slideover_activation", 33) / 100.0));
        else
            SWIPE_MIN_DISTANCE = (int) (width * (sharedPrefs.getInt("slideover_activation", 33) / 100.0));

        haloView = new HaloView(this);
        arcView = new ArcView(this, halo, SWIPE_MIN_DISTANCE, ARC_BREAK_POINT, HALO_SLIVER_RATIO);
        animationView = new AnimationView(this, halo);
        messageView = new MessageView(this);
        contactView = new ContactView(this);
        sendView = View.inflate(this, R.layout.send_bar, null);

        numberNewConv = arcView.newConversations.size();

        mGestureDetector = new GestureDetector(mContext, new GestureListener());

        messageBoxRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    messageWindow.removeView(contactView);
                    messageWindow.removeView(messageView);
                    sendWindow.removeView(sendView);
                } catch (Exception e) {

                }
            }
        };

        arcViewRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    arcWindow.addView(arcView, arcParamsNoBack);
                } catch (Exception e) {

                }
            }
        };

        removeArcRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    arcWindow.removeViewImmediate(arcView);
                } catch (Exception e) {

                }
            }
        };

        windowOffsetY = 50;

        sendBox = (EditText) sendView.findViewById(R.id.message_entry_slideover);
        sendBox.clearFocus();

        send = (ImageButton) sendView.findViewById(R.id.send);
        cancel = (ImageButton) sendView.findViewById(R.id.cancel);

        sendTransaction = new Transaction (mContext, MainActivity.sendSettings);
    }

    public void setParams(Bitmap halo, int height, int width) {
        messageWindowParams = new WindowManager.LayoutParams(
                width - 100,  // 50 pixels on each side
                toDP(160) + toDP(56),        // 160 dp tall + 53 for the reply box
                50,         // 50 pixel width on the side
                50 + toDP(63),         // 50 plus the height of the contactParams down the screen (plus 3 dp margin)
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        messageWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
        messageWindowParams.windowAnimations = android.R.style.Animation_InputMethod;

        contactParams = new WindowManager.LayoutParams(
                width - 100,  // 50 pixels on each side
                toDP(60),     // 60 dp tall
                50,         // 50 pixel width on the side
                50,         // 50 pixels down the screen
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        contactParams.gravity = Gravity.TOP | Gravity.LEFT;
        contactParams.windowAnimations = android.R.style.Animation_Toast;

        sendParams = new WindowManager.LayoutParams(
                width - 100,  // 50 pixels on each side
                toDP(50),        // 160 dp tall
                50,         // 50 pixel width on the side
                50 + toDP(63) + toDP(163),         // 50 plus the height of the contactParams down the screen (plus 3 dp margin)
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    |WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        sendParams.gravity = Gravity.TOP | Gravity.LEFT;
        sendParams.windowAnimations = android.R.style.Animation_Toast;

        sendParamsFocused = new WindowManager.LayoutParams(
                width - 100,  // 50 pixels on each side
                toDP(50),        // 160 dp tall
                50,         // 50 pixel width on the side
                50 + toDP(63) + toDP(163),         // 50 plus the height of the contactParams down the screen (plus 3 dp margin)
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        sendParamsFocused.gravity = Gravity.TOP | Gravity.LEFT;
        sendParamsFocused.windowAnimations = android.R.style.Animation_Toast;

        haloParams = new WindowManager.LayoutParams(
                halo.getWidth(),
                halo.getHeight(),
                sharedPrefs.getString("slideover_side", "left").equals("left") ? (int) (-1 * (1 - HALO_SLIVER_RATIO) * halo.getWidth()) : (int) (width - (halo.getWidth() * (HALO_SLIVER_RATIO))),
                (int) sharedPrefs.getFloat("slideover_downscreen", 0),
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        haloParams.gravity = Gravity.TOP | Gravity.LEFT;
        haloParams.windowAnimations = android.R.style.Animation_Toast;

        /*haloHiddenParams = new WindowManager.LayoutParams(
                halo.getWidth(),
                halo.getHeight(),
                sharedPrefs.getString("slideover_side", "left").equals("left") ? -1 * halo.getWidth() : width + halo.getWidth(),
                (int) sharedPrefs.getFloat("slideover_downscreen", 0),
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        |WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        haloHiddenParams.gravity = Gravity.TOP | Gravity.LEFT;*/

        arcParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                PixelFormat.TRANSLUCENT);
        arcParams.dimAmount = .7f;

        arcParamsNoBack = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        animationParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);

        messageWindow = (WindowManager) getSystemService(WINDOW_SERVICE);
        arcWindow = (WindowManager) getSystemService(WINDOW_SERVICE);
        animationWindow = (WindowManager) getSystemService(WINDOW_SERVICE);
        haloWindow = (WindowManager) getSystemService(WINDOW_SERVICE);
        sendWindow = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    public void onDown(MotionEvent event) {
        initX = event.getX();
        initY = event.getY();

        arcView.newMessagePaint.setAlpha(START_ALPHA2);

        needDetection = true;
    }

    public void contactViewTouched(MotionEvent event, int height, int width) {
        float currentX = event.getRawX();
        float currentY = event.getRawY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (currentY > windowOffsetY && currentY < windowOffsetY + toDP(60) && currentX > 50 && currentX < width - 50) {// if it is in the y zone and the x zone
                    currentX -= 50; // to match the start of the window

                    if (currentX < toDP(60) && !ContactView.ignore[0]) { // contact 1 touched
                        contactZone(0);
                    } else if (currentX > toDP(63) && currentX < toDP(123) && !ContactView.ignore[1]) { // contact 2 touched
                        contactZone(1);
                    } else if (currentX > toDP(126) && currentX < toDP(186) && !ContactView.ignore[2]) { // contact 3 touched
                        contactZone(2);
                    } else if (currentX > toDP(189) && currentX < toDP(249) && !ContactView.ignore[3]) { // contact 4 touched
                        contactZone(3);
                    } else if (currentX > toDP(252) && currentX < toDP(312) && !ContactView.ignore[4]) { // contact 5 touched
                        contactZone(4);
                    } else if (currentX > width - 50 - toDP(60) && currentX < width - 50) {

                        try { messageWindow.removeView(messageView);
                            sendWindow.removeView(sendView);
                            currContact = ContactView.currentContact;
                            ContactView.currentContact = 5;
                        } catch (Exception e) {
                        }

                        quickPeekHidden = true;
                        actionButtonTouched = true;
                        contactPictureTouched = false;

                        originalPos = (int) currentY;
                    }

                    contactView.invalidate();
                    messageView.invalidate();
                } else if (currentY > windowOffsetY + toDP(63) + toDP(163) && currentY < windowOffsetY + toDP(63) + toDP(163) + toDP(50) && currentX > 50 && currentX < width - 50) {// if it is in the y zone and the x zone
                    sendWindow.updateViewLayout(sendView, sendParamsFocused);
                    sendBox.requestFocus();
                }

                break;

            case MotionEvent.ACTION_MOVE:

                currentX -= 50;

                if (actionButtonTouched) {
                    if ((currentY > originalPos + toDP(7) || currentY < originalPos - toDP(7))) {
                        draggingQuickPeek = true;

                        windowOffsetY = (int) (currentY - toDP(30));
                        contactParams.y = windowOffsetY;
                        messageWindow.updateViewLayout(contactView, contactParams);
                    }

                } else if (currentX < toDP(60) && !ContactView.ignore[0]) { // contact 1 touched
                    contactZoneNoVibrate(0);
                } else if (currentX > toDP(63) && currentX < toDP(123) && !ContactView.ignore[1]) { // contact 2 touched
                    contactZoneNoVibrate(1);
                } else if (currentX > toDP(126) && currentX < toDP(186) && !ContactView.ignore[2]) { // contact 3 touched
                    contactZoneNoVibrate(2);
                } else if (currentX > toDP(189) && currentX < toDP(249) && !ContactView.ignore[3]) { // contact 4 touched
                    contactZoneNoVibrate(3);
                } else if (currentX > toDP(252) && currentX < toDP(312) && !ContactView.ignore[4]) { // contact 5 touched
                    contactZoneNoVibrate(4);
                }

                contactView.invalidate();
                messageView.invalidate();

                break;

            case MotionEvent.ACTION_UP:

                if (contactPictureTouched) { // contact picture was touched
                    try { messageWindow.addView(messageView, messageWindowParams);
                        sendWindow.addView(sendView, sendParams); } catch (Exception e) { }

                } else if (actionButtonTouched && draggingQuickPeek) { // finished actually dragging the window around

                    windowOffsetY = (int) (currentY - toDP(30));
                    currContact = 5;
                    ContactView.currentContact = 5;

                    messageWindowParams.y = toDP(63) + windowOffsetY;
                    sendParams.y = toDP(63) + toDP(163) + windowOffsetY;
                    sendParamsFocused.y = toDP(63) + toDP(163) + windowOffsetY;

                    messageView.invalidate();

                    try { messageWindow.updateViewLayout(messageView, messageWindowParams);
                            sendWindow.updateViewLayout(sendView, sendParams);} catch (Exception e) { }

                    actionButtonTouched = false;
                    draggingQuickPeek = false;
                    quickPeekHidden = true;
                } else if (actionButtonTouched && quickPeekHidden) { // keep quick peek hidden unless contact pic touched
                    actionButtonTouched = false;
                    draggingQuickPeek = false;
                    quickPeekHidden = true;

                    currContact = 5;
                    ContactView.currentContact = 5;
                    contactView.invalidate();

                    messageWindowParams.y = toDP(63) + windowOffsetY;

                    sendParams.y = toDP(63) + toDP(163) + windowOffsetY;
                    sendParamsFocused.y = toDP(63) + toDP(163) + windowOffsetY;

                    try { messageWindow.removeView(messageView);
                        sendWindow.removeView(sendView); } catch (Exception e) { }


                } else if (actionButtonTouched && !quickPeekHidden) { // hides quick peek when you touch the action button
                    ContactView.currentContact = 5;
                    contactView.invalidate();
                    messageView.invalidate();

                    messageWindowParams.y = toDP(63) + windowOffsetY;
                    sendParams.y = toDP(63) + toDP(163) + windowOffsetY;
                    sendParamsFocused.y = toDP(63) + toDP(163) + windowOffsetY;
                    messageWindow.removeView(messageView);
                    sendWindow.removeView(sendView);

                    draggingQuickPeek = false;
                    quickPeekHidden = true;
                    actionButtonTouched = false;
                }

                break;
        }
    }

    public void messageViewTouched(MotionEvent motionEvent, int height, int width) {
        float currentX = motionEvent.getRawX();
        float currentY = motionEvent.getRawY();

        if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (currentX > 50 && currentX < width - 50 && currentY > windowOffsetY + toDP(63) && currentY < windowOffsetY + toDP(63) + toDP(160)) {
                messageView.playSoundEffect(SoundEffectConstants.CLICK);

                if (HAPTIC_FEEDBACK) {
                    v.vibrate(10);
                }

                try {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ContactView.currentContact = 0;
                            contactView.invalidate();
                        }
                    }, 200);

                    closeNotifications();

                    Intent intent = finishFlat();
                    intent.putExtra("openToPage", ContactView.currentContact);
                    startActivity(intent);

                    messageWindow.removeView(contactView);
                    messageWindow.removeView(messageView);
                    sendWindow.removeView(sendView);
                } catch (Exception e) {
                    // already open and intent is null
                }
            }
        }
    }

    public void messagesMove(MotionEvent event, int height, int width, int zoneWidth) {

        initalMoveSetup(event);

        if (distance > toDP(10) && !animationView.isActivated()) {
            try {
                arcWindow.addView(arcView, arcParams);
            } catch (Exception e) {
            }
        } else {
            try {
                arcWindow.removeView(arcView);
            } catch (Exception e) {
            }
        }

        float rawY = event.getRawY();
        float rawX = event.getRawX();

        if ((rawY < 120 && PERCENT_DOWN_SCREEN > height / 2) || (rawY > height - 120 && PERCENT_DOWN_SCREEN < height / 2)) // in Button Area
        {
            inButtons = true;

            if (HAPTIC_FEEDBACK && topVibrate) {
                v.vibrate(25);
                vibrateNeeded = true;
                topVibrate = false;
            }

            if (rawX < width / 3) // Clear Zone
            {
                inClear = true;
                if (HAPTIC_FEEDBACK && (inMove || inClose)) {
                    v.vibrate(25);
                    inMove = false;
                    inClose = false;
                }
                arcView.clearPaint.setAlpha(TOUCHED_ALPHA);
                arcView.closePaint.setAlpha(START_ALPHA2);
                arcView.movePaint.setAlpha(START_ALPHA2);
                arcView.newMessagePaint.setAlpha(START_ALPHA2);
                arcView.conversationsPaint.setAlpha(START_ALPHA);

            } else if (rawX > width * .33 && rawX < width * .67) // Close Zone
            {
                inClose();

                arcView.clearPaint.setAlpha(START_ALPHA2);

            } else // Move Zone
            {
                inMove();

                arcView.clearPaint.setAlpha(START_ALPHA2);
            }

            arcView.invalidate();

            updateArcView();


        } else if ((!(initY > event.getY()) && angle > ARC_BREAK_POINT)) // in dash area
        {
            checkInButtons();

            resetZoneAlphas();
            lastZone = 0;
            currentZone = 0;

            fromDash = true;

            inDash();

        } else // in flat area
        {
            currentZone = getCurrentZone(distance, zoneWidth, SWIPE_MIN_DISTANCE, numberNewConv);

            if (lastZone != currentZone) {
                zoneChange = true;
                lastZone = currentZone;
            } else {
                zoneChange = false;
            }

            checkInButtons();

            inFlat();

            if (distance < SWIPE_MIN_DISTANCE)
                fromDash = true;

            if (zoneChange) {
                resetZoneAlphas();

                if (!fromDash) {
                    if (HAPTIC_FEEDBACK) {
                        v.vibrate(25);
                    }
                } else {
                    fromDash = false;
                }


                if (currentZone != 0)
                    arcView.textPaint[currentZone - 1].setAlpha(TOUCHED_ALPHA);

                zoneChange = false;
                arcView.invalidate();

                updateArcView();
            }

        }
    }

    public void noMessagesMove(MotionEvent event, int height, int width) {
        initalMoveSetup(event);

        if (distance > toDP(10)) {
            try {
                arcWindow.addView(arcView, arcParams);
            } catch (Exception e) {
            }
        } else {
            try {
                arcWindow.removeView(arcView);
            } catch (Exception e) {
            }
        }

        float rawY = event.getRawY();
        float rawX = event.getRawX();

        if ((rawY < 120 && PERCENT_DOWN_SCREEN > height / 2) || (rawY > height - 120 && PERCENT_DOWN_SCREEN < height / 2)) // in Button Area
        {
            inButtons = true;

            if (HAPTIC_FEEDBACK && topVibrate) {
                v.vibrate(25);
                vibrateNeeded = true;
                topVibrate = false;
            }

            if (rawX < width / 2) // Close Zone
            {
                inClose();

            } else // Move Zone
            {
                inMove();
            }

            arcView.invalidate();

            updateArcView();

        } else if ((!(initY > event.getY()) && angle > ARC_BREAK_POINT)) // in dash area
        {
            checkInButtons();
            inDash();

        } else // in flat area
        {
            checkInButtons();
            inFlat();
        }
    }

    public void messagesUp() {
        if (distance > SWIPE_MIN_DISTANCE) {
            arcView.newConversations.clear();

            haloView.haloNewAlpha = 0;
            haloView.haloAlpha = 255;
            haloView.invalidate();
            try {
                haloWindow.removeView(haloView);
                haloWindow.addView(haloView, haloParams);
            } catch (Exception e) {

            }

            numberNewConv = 0;
        }

        haloWindow.updateViewLayout(haloView, haloParams);

        // now will fire a different intent depending on what view you are in
        if (inClear) // clear button clicked
        {
            arcView.newConversations.clear();

            haloView.haloNewAlpha = 0;
            haloView.haloAlpha = 255;
            haloView.invalidate();
            haloWindow.updateViewLayout(haloView, haloParams);

            numberNewConv = 0;

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(1);
            mNotificationManager.cancel(2);

        } else if (inButtons) {
            finishButtons();
        } else if (distance > SWIPE_MIN_DISTANCE && inFlat) {
            try {
                Intent intent = finishFlat();

                if (currentZone == 0)
                    intent.putExtra("openToPage", 0);
                else
                    intent.putExtra("openToPage", currentZone - 1);

                startActivity(intent);
            } catch (Exception e) {
                // already open and intent is null
            }
        } else if (distance > SWIPE_MIN_DISTANCE && inDash) {
            finishDash();
        }

        arcView.newMessagePaint.setAlpha(START_ALPHA2);
        resetZoneAlphas();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    arcWindow.removeViewImmediate(arcView);
                } catch (Exception e) {

                }
            }
        }, 175);

        needDetection = true;
    }

    public void noMessagesUp() {
        if (distance > SWIPE_MIN_DISTANCE) {
            arcView.newConversations.clear();

            haloView.haloNewAlpha = 0;
            haloView.haloAlpha = 255;
            haloView.invalidate();
            haloWindow.removeView(haloView);
            haloWindow.addView(haloView, haloParams);

            numberNewConv = 0;
        } else {
            haloWindow.updateViewLayout(haloView, haloParams);
        }

        // now will fire a different intent depending on what view you are in
        if (inButtons) {
            finishButtons();
        } else if (distance > SWIPE_MIN_DISTANCE && inFlat) {
            try {
                Intent intent = finishFlat();
                startActivity(intent);
            } catch (Exception e) {
                // already open and intent is null
            }
        } else if (distance > SWIPE_MIN_DISTANCE && inDash) {
            finishDash();
        }

        arcView.newMessagePaint.setAlpha(START_ALPHA2);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    arcWindow.removeViewImmediate(arcView);
                } catch (Exception e) {

                }
            }
        }, 175);

        needDetection = true;
    }

    public void initalMoveSetup(MotionEvent event) {
        xPortion = initX - event.getX();
        yPortion = initY - event.getY();

        distance = Math.sqrt(Math.pow(xPortion, 2) + Math.pow(yPortion, 2));
        angle = Math.toDegrees(Math.atan(yPortion / xPortion));

        if (!sharedPrefs.getString("slideover_side", "left").equals("left"))
            angle *= -1;
    }

    public void inClose() {
        inClose = true;

        if (HAPTIC_FEEDBACK && (inMove || inClear)) {
            v.vibrate(25);
            inMove = false;
            inClear = false;
        }

        arcView.closePaint.setAlpha(TOUCHED_ALPHA);
        arcView.movePaint.setAlpha(START_ALPHA2);
        arcView.clearPaint.setAlpha(START_ALPHA2);
        arcView.newMessagePaint.setAlpha(START_ALPHA2);
        arcView.conversationsPaint.setAlpha(START_ALPHA);
    }

    public void inMove() {
        inMove = true;

        if (HAPTIC_FEEDBACK && (inClose || inClear)) {
            v.vibrate(25);
            inClose = false;
            inClear = false;
        }

        arcView.closePaint.setAlpha(START_ALPHA2);
        arcView.movePaint.setAlpha(TOUCHED_ALPHA);
        arcView.newMessagePaint.setAlpha(START_ALPHA2);
        arcView.conversationsPaint.setAlpha(START_ALPHA);
    }

    public void inDash() {
        if (inFlat && distance > SWIPE_MIN_DISTANCE) {
            inFlat = false;
            inDash = true;

            arcView.conversationsPaint.setAlpha(START_ALPHA2 + 20);
            arcView.newMessagePaint.setAlpha(START_ALPHA2);
            arcView.invalidate();

            updateArcView();

            if (!initial) {
                if (HAPTIC_FEEDBACK) {
                    v.vibrate(25);
                }
            } else {
                initial = false;
            }
        }

        if (distance > SWIPE_MIN_DISTANCE && vibrateNeeded) {
            arcView.conversationsPaint.setAlpha(START_ALPHA2 + 20);
            arcView.newMessagePaint.setAlpha(START_ALPHA2);
            arcView.invalidate();

            updateArcView();

            if (HAPTIC_FEEDBACK) {
                v.vibrate(25);
            }

            vibrateNeeded = false;
        }

        if (distance < SWIPE_MIN_DISTANCE) {
            arcView.conversationsPaint.setAlpha(START_ALPHA);
            arcView.newMessagePaint.setAlpha(START_ALPHA2);
            arcView.invalidate();
            try {
                arcWindow.updateViewLayout(arcView, arcParamsNoBack);
            } catch (Exception e) {

            }
            vibrateNeeded = true;
        }
    }

    public void inFlat() {
        if (inDash && distance > SWIPE_MIN_DISTANCE) {
            inDash = false;
            inFlat = true;

            arcView.newMessagePaint.setAlpha(TOUCHED_ALPHA);
            arcView.conversationsPaint.setAlpha(START_ALPHA);
            arcView.invalidate();

            updateArcView();

            if (!initial) {
                if (HAPTIC_FEEDBACK) {
                    v.vibrate(25);
                }
            } else {
                initial = false;
            }
        }
        if (distance > SWIPE_MIN_DISTANCE && vibrateNeeded) {
            arcView.newMessagePaint.setAlpha(TOUCHED_ALPHA);
            arcView.conversationsPaint.setAlpha(START_ALPHA);
            arcView.invalidate();

            updateArcView();

            if (HAPTIC_FEEDBACK) {
                v.vibrate(25);
            }

            vibrateNeeded = false;
        }

        if (distance < SWIPE_MIN_DISTANCE) {
            resetZoneAlphas();
            lastZone = 0;
            currentZone = 0;

            arcView.newMessagePaint.setAlpha(START_ALPHA2);
            arcView.conversationsPaint.setAlpha(START_ALPHA);
            arcView.invalidate();
            try {
                arcWindow.updateViewLayout(arcView, arcParamsNoBack);
            } catch (Exception e) {

            }
            vibrateNeeded = true;
        }
    }

    public void finishButtons() {
        if (inMove) // move button was clicked
        {
            Intent intent = new Intent(getBaseContext(), com.klinker.android.messaging_sliding.slide_over.SlideOverSettings.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } else // close button was clicked
        {
            sharedPrefs.edit().putBoolean("slideover_enabled", false).commit();

            Intent service = new Intent();
            service.setAction("com.klinker.android.messaging.STOP_HALO");
            sendBroadcast(service);
        }
    }

    public Intent finishFlat() {
        closeNotifications();

        if (isRunning(getApplication())) {
            Intent intent = new Intent();
            intent.setAction("com.klinker.android.messaging_donate.KILL_FOR_HALO");
            sendBroadcast(intent);
            return null;
        } else {
            Intent intent = new Intent(getBaseContext(), com.klinker.android.messaging_sliding.MainActivityPopup.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("fromHalo", true);
            return intent;
        }

    }

    public void finishDash() {
        closeNotifications();

        if (sharedPrefs.getString("slideover_secondary_action", "conversations").equals("markRead")) {
            startService(new Intent(getBaseContext(), QmMarkRead2.class));
        } else {
            if (isRunning(getApplication())) {
                Intent intent = new Intent();
                intent.setAction("com.klinker.android.messaging_donate.KILL_FOR_HALO");
                sendBroadcast(intent);
            } else {
                Intent intent = new Intent(getBaseContext(), com.klinker.android.messaging_sliding.MainActivityPopup.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("fromHalo", true);
                intent.putExtra("secAction", true);
                intent.putExtra("secondaryType", sharedPrefs.getString("slideover_secondary_action", "conversations"));
                startActivity(intent);
            }
        }
    }

    public void contactZone(int number) {

        contactView.playSoundEffect(SoundEffectConstants.CLICK);
        if (HAPTIC_FEEDBACK) {
            ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(10);
        }

        ContactView.currentContact = number;
        currContact = number;

        contactPictureTouched = true;
        quickPeekHidden = false;
        actionButtonTouched = false;
    }

    public void contactZoneNoVibrate(int number) {
        ContactView.currentContact = number;
        currContact = number;

        contactPictureTouched = true;
        quickPeekHidden = false;
        actionButtonTouched = false;
    }

    public void changeSliverWidth(Bitmap halo, MotionEvent event, int width) {
        int sliver;

        if (sharedPrefs.getString("slideover_side", "left").equals("left")) {
            sliver = (int) ((event.getRawX() * 100) / width);
        } else {
            sliver = (int) ((1 - (event.getRawX() / width)) * 100);
        }

        HALO_SLIVER_RATIO = sliver / 100.0;

        haloParams.x = sharedPrefs.getString("slideover_side", "left").equals("left") ? (int) (-1 * (1 - HALO_SLIVER_RATIO) * halo.getWidth()) : (int) (width - (halo.getWidth() * (HALO_SLIVER_RATIO)));

        haloWindow.updateViewLayout(haloView, haloParams);
    }

    public void setSliver(Bitmap halo, MotionEvent event, int height, int width) {
        if (sharedPrefs.getString("slideover_side", "left").equals("left"))
            sharedPrefs.edit().putInt("slideover_sliver", (int) ((event.getRawX() * 100) / width)).commit();
        else
            sharedPrefs.edit().putInt("slideover_sliver", (int) ((1 - (event.getRawX() / width)) * 100)).commit();

        HALO_SLIVER_RATIO = sharedPrefs.getInt("slideover_sliver", 33) / 100.0;

        haloParams.x = sharedPrefs.getString("slideover_side", "left").equals("left") ? (int) (-1 * (1 - HALO_SLIVER_RATIO) * halo.getWidth()) : (int) (width - (halo.getWidth() * (HALO_SLIVER_RATIO)));
        haloParams.y = (int) sharedPrefs.getFloat("slideover_downscreen", 0);

        haloWindow.updateViewLayout(haloView, haloParams);
    }

    public void setHalo(Bitmap halo, MotionEvent event, int height, int width) {

        int currX = (int) event.getRawX();
        float currY = event.getRawY() - halo.getWidth() / 2;

        if (currX < width / 2) {
            sharedPrefs.edit().putString("slideover_side", "left").commit();
        } else {
            sharedPrefs.edit().putString("slideover_side", "right").commit();
        }

        sharedPrefs.edit().putFloat("slideover_downscreen", currY).commit();

        PERCENT_DOWN_SCREEN = currY;

        arcView.invalidate();

        haloParams.x = sharedPrefs.getString("slideover_side", "left").equals("left") ? (int) (-1 * (1 - HALO_SLIVER_RATIO) * halo.getWidth()) : (int) (width - (halo.getWidth() * (HALO_SLIVER_RATIO)));
        haloParams.y = (int) currY;

        haloWindow.removeView(haloView);
        haloWindow.addView(haloView, haloParams);
    }

    public void movingHalo(Bitmap halo, MotionEvent event) {
        haloParams.x = (int) event.getRawX() - halo.getWidth() / 2;
        haloParams.y = (int) event.getRawY() - halo.getHeight() / 2;

        haloWindow.updateViewLayout(haloView, haloParams);
    }

    public void updateArcView() {
        try {
            arcWindow.updateViewLayout(arcView, arcParams);
        } catch (Exception e) {

        }
    }

    public boolean isRunning(Context ctx) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
                return true;
        }

        return false;
    }

    public int toDP(int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, displayMatrix);
    }

    public void checkInButtons() {
        if (inButtons) {
            arcView.closePaint.setAlpha(START_ALPHA);
            arcView.movePaint.setAlpha(START_ALPHA);
            arcView.clearPaint.setAlpha(START_ALPHA);

            inButtons = false;

            inClose = false;
            inMove = false;
            inClear = false;

            topVibrate = true;
        }
    }

    public int getCurrentZone(double distance, int zoneWidth, int arcLength, int maxZones) {
        int extraDist = (int) distance - arcLength;
        int currZone = 0;

        while (extraDist > 0 && currZone < maxZones) {
            currZone++;
            extraDist -= zoneWidth;
        }

        return currZone;
    }

    public void resetZoneAlphas() {
        try {
            for (int i = 0; i < numberNewConv; i++) {
                arcView.textPaint[i].setAlpha(START_ALPHA2);
            }

            arcView.invalidate();
            updateArcView();
        } catch (Exception e) {

        }
    }

    public void closeNotifications() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(1);
                mNotificationManager.cancel(2);
                mNotificationManager.cancel(4);
                IOUtil.writeNotifications(new ArrayList<String>(), mContext);

                Intent intent = new Intent("com.klinker.android.messaging.CLEARED_NOTIFICATION");
                mContext.sendBroadcast(intent);

                Intent stopRepeating = new Intent(mContext, NotificationRepeaterService.class);
                PendingIntent pStopRepeating = PendingIntent.getService(mContext, 0, stopRepeating, 0);
                AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarm.cancel(pStopRepeating);
            }
        }, 500);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(stopSlideover);
            unregisterReceiver(newMessageReceived);
            unregisterReceiver(mBroadcastReceiver);
            unregisterReceiver(clearMessages);
            unregisterReceiver(orientationChange);
        } catch (Exception e) {

        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    public BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent myIntent) {
            stopService(new Intent(context, SlideOverService.class));
            haloView.invalidate();
            try {
                haloWindow.removeViewImmediate(haloView);
            } catch (Exception e) {
            }
            unregisterReceiver(this);
            startService(new Intent(context, SlideOverService.class));
        }
    };

    public BroadcastReceiver stopSlideover = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            haloView.invalidate();

            try { haloWindow.removeViewImmediate(haloView); } catch (Exception e) { }
            try { messageWindow.removeViewImmediate(messageView); } catch (Exception e) { }
            try { messageWindow.removeViewImmediate(contactView); } catch (Exception e) { }
            try { sendWindow.removeViewImmediate(sendView); } catch (Exception e) { }

            stopSelf();
            unregisterReceiver(this);
        }
    };

    public BroadcastReceiver newMessageReceived = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            ContactView.refreshArrays();

            //new Handler().postDelayed(new Runnable() {
            //@Override
            //public void run() {
            ContactView.currentContact = 0;
            contactView.invalidate();
            messageView.invalidate();
            //    }
            //}, 200);

            String name = intent.getStringExtra("name");
            String message = intent.getStringExtra("message");

            int index;
            boolean exists = false;
            boolean same = false;

            // check for mms messages, group texts really messed things up with the opening to conversations. So now it will remove the old mms notification and throw the new one to the first spot.
            // Good for group mms. But more than one conversation at once won't work.
            // They will also eat the normal mms messages notifications
            // Don't see any way around this though, and i don't think many people will run into this problem, if they do, they won't be able to replicate  it hehe :)

            for (index = 0; index < numberNewConv; index++) {
                if (message.equals(arcView.newConversations.get(index)[1])) {
                    same = true;
                    break;
                }
            }

            if (same) {
                arcView.newConversations.add(new String[]{name, message});
                arcView.newConversations.remove(index);
            } else {
                for (index = 0; index < numberNewConv; index++) {
                    if (name.equals(arcView.newConversations.get(index)[0])) {
                        exists = true;
                        break;
                    }
                }
            }

            if (!exists && !same)
                arcView.newConversations.add(new String[]{name, message});
            else if (!same) {
                String oldMessage = arcView.newConversations.get(index)[1];
                arcView.newConversations.add(new String[]{name, oldMessage + " | " + message});
                arcView.newConversations.remove(index);
            }

            numberNewConv = arcView.newConversations.size();

            arcView.updateTextPaint();
            arcView.invalidate();

            // set the icon to the red, recieved, icon
            if (!haloView.animating) {
                haloView.haloNewAlpha = 0;
                haloView.haloAlpha = 255;
                haloView.animating = true;

                HaloFadeAnimation animation = new HaloFadeAnimation(haloView, true);
                animation.setRunning(true);
                animation.start();
            }

            animationView = new AnimationView(getApplicationContext(), halo);

            if (!animationView.circleText) {
                if (!sharedPrefs.getBoolean("popup_reply", false) || (sharedPrefs.getBoolean("popup_reply", true) && sharedPrefs.getBoolean("slideover_popup_lockscreen_only", false))) {
                    // start the animation
                    animationView.circleText = true;
                    animationView.firstText = true;
                    animationView.arcOffset = AnimationView.ORIG_ARC_OFFSET;
                    animationView.name = new String[]{name, message.length() > 50 ? message.substring(0, 50) + "..." : message};
                    animationView.circleLength = 0;
                    animationView.circleStart = animationView.originalCircleStart;
                    animationWindow.addView(animationView, animationParams);

                    NewMessageAnimation animation = new NewMessageAnimation(animationView, ((float) (3 * (sharedPrefs.getInt("slideover_animation_speed", 33) / 100.0) + 1)) / 2, haloWindow);
                    animation.setRunning(true);
                    animation.start();
                }
            }

            if (!sharedPrefs.getBoolean("popup_reply", false)) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        haloView.invalidate();
                        try {
                            haloWindow.removeView(haloView);
                            haloWindow.addView(haloView, haloParams);
                        } catch (Exception e) {

                        }
                    }
                }, 1500);
            }
        }
    };

    public BroadcastReceiver clearMessages = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // FIXME sometimes this doesnt work... swiping away a notification or having popup automatically doesn't clear the color
            // turning on popup after lockscreen only and testing with message, popup will show but then the halo will never go back to white
            // until you manually bring up slideover from halo
            arcView.newConversations.clear();

            haloView.haloNewAlpha = 0;
            haloView.haloAlpha = 255;
            haloView.invalidate();

            try {
                haloWindow.removeViewImmediate(haloView);
                haloWindow.addView(haloView, haloParams);
            } catch (Exception e) {

            }

            numberNewConv = 0;
        }
    };

    public BroadcastReceiver orientationChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent myIntent) {
            // remove the message view and contact view so they don't cause problems
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        messageWindow.removeView(messageView);
                        messageWindow.removeView(contactView);
                        sendWindow.removeView(sendView);
                    } catch (Exception e) {

                    }
                }
            }, 300);
        }
    };

    public static void restartHalo(final Context context) {
        Intent service = new Intent();
        service.setAction("com.klinker.android.messaging.STOP_HALO");
        context.sendBroadcast(service);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("slideover_enabled", false)) {
                    Intent service = new Intent(context, com.klinker.android.messaging_sliding.slide_over.SlideOverService.class);
                    context.startService(service);
                }
            }
        }, 500);
    }
}
