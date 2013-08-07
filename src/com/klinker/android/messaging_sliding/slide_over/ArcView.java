package com.klinker.android.messaging_sliding.slide_over;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.*;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by luke on 8/2/13.
 */
public class ArcView extends ViewGroup {
    public Context mContext;

    public static float TEXT_SIZE;
    public static float TEXT_GAP;

    public Bitmap halo;

    public Paint newMessagePaint;
    public Paint conversationsPaint;
    public Paint textPaint;
    public float radius;
    public float breakAngle;

    public SharedPreferences sharedPrefs;

    public int height;
    public int width;

    public int sliverAdjustment = 0;
    public double sliverPercent;

    public ArrayList<String[]> newConversations;

    public ArcView(Context context, Bitmap halo, float radius, float breakAngle, double sliverPercent) {
        super(context);

        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        TEXT_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, context.getResources().getDisplayMetrics());
        TEXT_GAP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 2, context.getResources().getDisplayMetrics());

        Display d = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        height = d.getHeight();
        width = d.getWidth();

        newMessagePaint = new Paint();
        newMessagePaint.setAntiAlias(true);
        newMessagePaint.setColor(Color.WHITE);
        newMessagePaint.setAlpha(SlideOverService.START_ALPHA2);
        newMessagePaint.setStyle(Paint.Style.STROKE);
        newMessagePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics()));

        conversationsPaint = new Paint(newMessagePaint);
        conversationsPaint.setAlpha(SlideOverService.START_ALPHA);
        float dashLength = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
        conversationsPaint.setPathEffect(new DashPathEffect(new float[] {dashLength, dashLength*2}, 0));
        
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setAlpha(SlideOverService.START_ALPHA2);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

        this.halo = halo;
        this.radius = radius;
        this.breakAngle = breakAngle;
        this.sliverPercent = sliverPercent;

        newConversations = new ArrayList<String[]>();

        // manually fill the newConversations arraylist for now
        newConversations.add(new String[] {"Jacob Klinker", "SlideOver is working well!"});
        newConversations.add(new String[] {"Brett Deters", "Want to go to Jethros? I think it would be an awesome night for that!"});
        newConversations.add(new String[] {"Matt Swiontek", "Your apartment is great!"});

    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /*if (sharedPrefs.getString("slideover_side", "left").equals("left")) {

            if (sliverPercent > .5) // move the arc right
            {
                sliverPercent -= .5;
                sliverAdjustment = (int)((halo.getWidth()/2) * sliverPercent);
            } else // move the arc left
            {
                sliverAdjustment = (int)(-1 * ((halo.getWidth()/2) * sliverPercent));
            }

        } else
        {

        }


        CharSequence text = "" + sliverAdjustment;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(mContext, text, duration);
        toast.show();*/

        int[] point = getPosition();

        // Draws the arcs that you can interact with
        if (sharedPrefs.getString("slideover_side", "left").equals("left")) {
            // todo: adjust for sliver size on the left and right of the oval here
            RectF oval = new RectF(-1 * radius, point[1] + (halo.getHeight() / 2) -  radius, radius, point[1] + (halo.getHeight() / 2) + radius);

            Path newMessagePath = new Path();
            newMessagePath.addArc(oval, breakAngle, -180);

            Path conversationsPath = new Path();
            conversationsPath.addArc(oval, breakAngle, 180);

            canvas.drawPath(newMessagePath, newMessagePaint);
            canvas.drawPath(conversationsPath, conversationsPaint);
        } else
        {
            RectF oval = new RectF(width - radius, point[1] + (halo.getHeight() / 2) -  radius, width + radius, point[1] + (halo.getHeight() / 2) + radius);

            Path newMessagePath = new Path();
            newMessagePath.addArc(oval, breakAngle - 45, -180);

            Path conversationsPath = new Path();
            conversationsPath.addArc(oval, breakAngle - 45, 180);

            canvas.drawPath(newMessagePath, newMessagePaint);
            canvas.drawPath(conversationsPath, conversationsPaint);
        }

        float conversationsRadius = radius + TEXT_SIZE + TEXT_GAP;

        // Draws the new conversations from the arraylist newConversations
        for (int i = 0; i < newConversations.size(); i++)
        {
            if (sharedPrefs.getString("slideover_side", "left").equals("left")) {
                // todo: adjust for sliver size on the left and right of the oval here
                RectF oval = new RectF(-1 * conversationsRadius, point[1] + (halo.getHeight() / 2) -  conversationsRadius, conversationsRadius, point[1] + (halo.getHeight() / 2) + conversationsRadius);

                Path textPath = new Path();
                textPath.addArc(oval, -88, 90 + breakAngle);

                canvas.drawTextOnPath(newConversations.get(i)[0] + " - " + newConversations.get(i)[1], textPath, 0f, 0f, textPaint);
            } else
            {
                RectF oval = new RectF(width - conversationsRadius, point[1] + (halo.getHeight() / 2) -  conversationsRadius, width + conversationsRadius, point[1] + (halo.getHeight() / 2) + conversationsRadius);

                Path textPath = new Path();
                textPath.addArc(oval, -180 - breakAngle + 5, breakAngle + 90);

                canvas.drawTextOnPath(newConversations.get(i)[0] + " - " + newConversations.get(i)[1], textPath, 0f, 0f, textPaint);
            }

            conversationsRadius += TEXT_SIZE + TEXT_GAP;
        }

        /*

        Original Circle Drawing

        if (sharedPrefs.getString("slideover_side", "left").equals("left")) {
            canvas.drawCircle(point[0] + (halo.getHeight()/2), point[1] + (halo.getHeight() / 2), radius, newMessagePaint);
        } else {
            canvas.drawCircle(point[0] + (halo.getHeight()/2), point[1] + (halo.getHeight() / 2), radius, newMessagePaint);
        }

        */
    }


    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public int[] getPosition()
    {
        int[] returnArray = {0, 0};

        if (sharedPrefs.getString("slideover_side", "left").equals("left")) {
            returnArray[0] = (int)(-1 * halo.getWidth() * (1 - SlideOverService.HALO_SLIVER_RATIO));
        } else {
            returnArray[0] = (int)(width - (halo.getWidth() * SlideOverService.HALO_SLIVER_RATIO));
        }

        returnArray[1] = (int)(height * SlideOverService.PERCENT_DOWN_SCREEN);

        return returnArray;
    }
}
