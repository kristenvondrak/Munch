package com.example.kristenvondrak.dartmunch.Preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.kristenvondrak.dartmunch.R;

/**
 * Created by kristenvondrak on 3/2/16.
 */
public class CircleTriSeekBar extends View {

    /*
        * Constants used to save/restore the instance state.
        */
    private static final int NUM_POINTERS = 3;
    private static final String STATE_PARENT = "parent";
    private static final String STATE_ANGLE = "angle";
    private static final int TEXT_SIZE_DEFAULT_VALUE = 25;
    public static final int COLOR_WHEEL_STROKE_WIDTH_DEF_VALUE = 30;
    public static final float POINTER_RADIUS_DEF_VALUE = 30;
    public static final int MAX_POINT_DEF_VALUE = 100;

    private OnCircleSeekBarChangeListener mOnCircleSeekBarChangeListener;

    // Pointers
    private Paint[] mPointerColors = new Paint[NUM_POINTERS];
    private Paint[] mHaloPointerColors = new Paint[NUM_POINTERS];
    private float mPointerRadius;

    // Color wheel

    private Paint[] mArcColors = new Paint[NUM_POINTERS];
    private int mColorWheelStrokeWidth;
    private float mColorWheelRadius;
    private RectF mColorWheelRectangle = new RectF();


    private int mCurrPointer = -1;
    private float mTranslationOffset;


    // Positions
    private int max = 100;
    private int[] mRadians = new int[NUM_POINTERS];
    private float[] mAngles = new float[NUM_POINTERS];
    private int[] mLastRadians = new int[NUM_POINTERS];
    private float lastX;



    private SweepGradient s;

    private int[] wheel_colors, pointer_colors, pointer_halo_colors, init_positions;

    private boolean block_end = false;

    private boolean block_start = false;


    private float[][] pointerPositions = new float[NUM_POINTERS][2];
    private RectF mColorCenterHaloRectangle = new RectF();
    //private int end_wheel;
    private Rect bounds = new Rect();

    public CircleTriSeekBar(Context context) {
        super(context);
        init(null, 0);
    }

    public CircleTriSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CircleTriSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.CircleTriSeekBar, defStyle, 0);

        initAttributes(a);

        a.recycle();
        // mAngle = (float) (-Math.PI / 2);

        mArcColors = new Paint[NUM_POINTERS];
        for (int i = 0; i < NUM_POINTERS; i++) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setShader(s);
            paint.setColor(wheel_colors[i]);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(mColorWheelStrokeWidth);
            mArcColors[i] = paint;
        }

        mPointerColors = new Paint[NUM_POINTERS];
        for (int i = 0; i < NUM_POINTERS; i++) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);;
            paint.setStrokeWidth(mPointerRadius);
            paint.setColor(pointer_colors[i]);
            mPointerColors[i] = paint;
        }

        mHaloPointerColors = new Paint[NUM_POINTERS];
        for (int i = 0; i < NUM_POINTERS; i++) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);;
            paint.setColor(pointer_halo_colors[i]);
            paint.setStrokeWidth(mPointerRadius + 10);
            mHaloPointerColors[i] = paint;
        }

        Paint mColorCenterHalo = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorCenterHalo.setColor(Color.CYAN);
        mColorCenterHalo.setAlpha(0xCC);
        // mColorCenterHalo.setStyle(Paint.Style.STROKE);
        // mColorCenterHalo.setStrokeWidth(mColorCenterHaloRectangle.width() /
        // 2);


        mRadians = new int[NUM_POINTERS];
        for (int i = 0; i < NUM_POINTERS; i++) {
            int finishRad = (int) calculateAngleFromText(init_positions[i]) - 90;
            Log.d("***** rad: " , Integer.toString(finishRad));
            mRadians[i] = (finishRad < 360) ? finishRad :  i * 120;
        }

        if (mRadians[1] < mRadians[0])
            mRadians[1] = mRadians[0] + 90;
        if (mRadians[2] < mRadians[1])
            mRadians[2] = mRadians[1] + 90;

        mAngles = new float[NUM_POINTERS];
        for (int i = 0; i < NUM_POINTERS; i++) {
            mAngles[i] = calculateAngleFromRadians(mRadians[i]);
            Log.d("***** angles: " , Float.toString(mAngles[i]));
        }

        invalidate();
    }

    private void initAttributes(TypedArray a) {
        mColorWheelStrokeWidth = a.getInteger(
                R.styleable.CircleTriSeekBar_triwheel_size, COLOR_WHEEL_STROKE_WIDTH_DEF_VALUE);
        mPointerRadius = a.getDimension(
                R.styleable.CircleTriSeekBar_tripointer_size, POINTER_RADIUS_DEF_VALUE);

        max = 100;

        String[] wheel_colors_attr = new String[NUM_POINTERS];
        wheel_colors_attr[0] = a
                .getString(R.styleable.CircleTriSeekBar_wheel_color_1);
        wheel_colors_attr[1] = a
                .getString(R.styleable.CircleTriSeekBar_wheel_color_2);
        wheel_colors_attr[2] = a
                .getString(R.styleable.CircleTriSeekBar_wheel_color_3);

        String[] pointer_colors_attr = new String[NUM_POINTERS];
        pointer_colors_attr[0] = a
                .getString(R.styleable.CircleTriSeekBar_pointer_color_1);
        pointer_colors_attr[1] = a
                .getString(R.styleable.CircleTriSeekBar_pointer_color_2);
        pointer_colors_attr[2] = a
                .getString(R.styleable.CircleTriSeekBar_pointer_color_3);


        String[] pointer_halo_colors_attr = new String[NUM_POINTERS];
        pointer_halo_colors_attr[0] = a
                .getString(R.styleable.CircleTriSeekBar_pointer_halo_color_1);
        pointer_halo_colors_attr[1] = a
                .getString(R.styleable.CircleTriSeekBar_pointer_halo_color_2);
        pointer_halo_colors_attr[2] = a
                .getString(R.styleable.CircleTriSeekBar_pointer_halo_color_3);

        init_positions = new int[NUM_POINTERS];
        init_positions[0] = a.getInteger(R.styleable.CircleTriSeekBar_start_position_1, 0);
        init_positions[1] = a.getInteger(R.styleable.CircleTriSeekBar_start_position_2, NUM_POINTERS);
        init_positions[2] = a.getInteger(R.styleable.CircleTriSeekBar_start_position_3, 66);

        wheel_colors = new int[NUM_POINTERS];
        wheel_colors[0] = Color.parseColor(wheel_colors_attr[0]);
        wheel_colors[1] = Color.parseColor(wheel_colors_attr[1]);
        wheel_colors[2] = Color.parseColor(wheel_colors_attr[2]);

        pointer_colors = new int[NUM_POINTERS];
        pointer_colors[0] = Color.parseColor(pointer_colors_attr[0]);
        pointer_colors[1] = Color.parseColor(pointer_colors_attr[1]);
        pointer_colors[2] = Color.parseColor(pointer_colors_attr[2]);

        pointer_halo_colors = new int[NUM_POINTERS];
        pointer_halo_colors[0] = Color.parseColor(pointer_halo_colors_attr[0]);
        pointer_halo_colors[1] = Color.parseColor(pointer_halo_colors_attr[1]);
        pointer_halo_colors[2] = Color.parseColor(pointer_halo_colors_attr[2]);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // All of our positions are using our internal coordinate system.
        // Instead of translating
        // them we let Canvas do the work for us.

        canvas.translate(mTranslationOffset, mTranslationOffset);

        // Draw the color wheel.
        canvas.drawArc(mColorWheelRectangle, mRadians[0] - 90, mRadians[1] - mRadians[0],
                false, mArcColors[0]);

        canvas.drawArc(mColorWheelRectangle,  mRadians[1] - 90, mRadians[2] - mRadians[1] ,
                false, mArcColors[1]);

        canvas.drawArc(mColorWheelRectangle, mRadians[2] - 90, 360 - mRadians[2] + mRadians[0],
                false, mArcColors[2]);

        // Draw the color wheel.
        //canvas.drawArc(mColorWheelRectangle, 0, 180, false, mArcColors[1]);

        //canvas.drawArc(mColorWheelRectangle, 270, 0, false, mArcColors[0]);

        for (int i = 0; i < NUM_POINTERS; i++) {
            // Draw the pointer's "halo"
            canvas.drawCircle(pointerPositions[i][0], pointerPositions[i][1],
                    mPointerRadius, mHaloPointerColors[i]);

            // Draw the pointer (the currently selected color) slightly smaller on
            // top.
            canvas.drawCircle(pointerPositions[i][0], pointerPositions[i][1],
                    (float) (mPointerRadius / 1.2), mPointerColors[i]);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getDefaultSize(getSuggestedMinimumHeight(),
                heightMeasureSpec);
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int min = Math.min(width, height);
        setMeasuredDimension(min, min);

        mTranslationOffset = min * 0.5f;
        mColorWheelRadius = mTranslationOffset - mPointerRadius;

        mColorWheelRectangle.set(-mColorWheelRadius, -mColorWheelRadius,
                mColorWheelRadius, mColorWheelRadius);

        mColorCenterHaloRectangle.set(-mColorWheelRadius / 2,
                -mColorWheelRadius / 2, mColorWheelRadius / 2,
                mColorWheelRadius / 2);

        updatePointerPositions();

    }


    private double calculateAngleFromText(int position) {
        if (position == 0 || position >= max)
            return (float) 90;

        double f = (double) max / (double) position;

        double f_r = 360 / f;

        return f_r + 90;
    }

    private int calculateRadiansFromAngle(float angle) {
        float unit = (float) (angle / (2 * Math.PI));
        if (unit < 0) {
            unit += 1;
        }
        int radians = (int) ((unit * 360) - ((360 / 4) * NUM_POINTERS));
        if (radians < 0)
            radians += 360;
        return radians;
    }

    private float calculateAngleFromRadians(int radians) {
        return (float) (((radians + 270) * (2 * Math.PI)) / 360);
    }


    private boolean isValid(int i, float newValue) {
        if (i == 0) {
            return newValue < mRadians[1];
        } else if (i == 1) {
            return newValue < mRadians[2];
        } else if (i == 2) {
            return newValue < mRadians[0];
        }
        return false;
    }

    private void updatePointerPositions() {
        for (int i = 0; i < NUM_POINTERS; i++) {
            pointerPositions[i] = calculatePointerPosition(mAngles[i]);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Convert coordinates to our internal coordinate system
        float x = event.getX() - mTranslationOffset;
        float y = event.getY() - mTranslationOffset;



        int nextIndex, prevIndex;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Check whether the user pressed on (or near) the pointer

                mCurrPointer = calculatePointerIndex(x, y);
                mAngles[mCurrPointer] = (float) Math.atan2(y, x);
                mRadians[mCurrPointer] = calculateRadiansFromAngle(mAngles[mCurrPointer]);


                block_end = false;
                block_start = false;

                Log.d("&&&&&&&&", Integer.toString(mCurrPointer));


                nextIndex = mCurrPointer == 2 ? 0 : mCurrPointer + 1;
                if ( mRadians[mCurrPointer] > mRadians[nextIndex]) {
                    mRadians[mCurrPointer] = mRadians[nextIndex];
                    block_end = true;
                }

                if (!block_end) {
                    //setText(String.valueOf(calculateTextFromAngle(arc_finish_radians)));
                    updatePointerPositions();
                    invalidate();
                }
                if (mOnCircleSeekBarChangeListener != null) {
                    mOnCircleSeekBarChangeListener.onStartTrackingTouch(this);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mCurrPointer > -1) {

                    mAngles[mCurrPointer] = (float) Math.atan2(y, x);
                    mRadians[mCurrPointer] = calculateRadiansFromAngle(mAngles[mCurrPointer]);
                    nextIndex = mCurrPointer == 2 ? 0 : mCurrPointer + 1;
                    prevIndex = mCurrPointer == 0 ? 2 : mCurrPointer - 1;

                    if (mLastRadians[mCurrPointer] > mRadians[mCurrPointer]
                            && mRadians[mCurrPointer] < (360 / 6)
                            && x > lastX
                            && mLastRadians[mCurrPointer] > (360 / 6)) {

                        if (!block_end && !block_start)
                            block_end = true;
                        // if (block_start)
                        // block_start = false;
                    } else if (mLastRadians[mCurrPointer] >= mRadians[prevIndex]
                            && mLastRadians[mCurrPointer] <= (360 / 4)
                            && mRadians[mCurrPointer] <= (360 - 1)
                            && mRadians[mCurrPointer] >= ((360 / 4) * 3)
                            && x < lastX) {
                        if (!block_start && !block_end)
                            block_start = true;
                        // if (block_end)
                        // block_end = false;

                    } else if (mRadians[mCurrPointer] >= mRadians[nextIndex] && !block_start
                            && mLastRadians[mCurrPointer] < mRadians[mCurrPointer]) {
                        block_end = true;
                    } else if (mRadians[mCurrPointer] < mRadians[nextIndex] && block_end
                            && mLastRadians[mCurrPointer] > mRadians[nextIndex]) {
                        block_end = false;
                    } else if (mRadians[mCurrPointer] < mRadians[prevIndex]
                            && mLastRadians[mCurrPointer] > mRadians[mCurrPointer]
                            && !block_end) {
                        block_start = true;
                    } else if (block_start && mLastRadians[mCurrPointer] < mRadians[mCurrPointer]
                            && mRadians[mCurrPointer] > mRadians[prevIndex]
                            && mRadians[mCurrPointer] < mRadians[nextIndex]) {
                        block_start = false;
                    }

                    if (block_end) {
                        mRadians[mCurrPointer] = mRadians[nextIndex];
                        //setText(String.valueOf(max));
                        mAngles[mCurrPointer] = calculateAngleFromRadians(mRadians[mCurrPointer]);
                        updatePointerPositions();
                        Log.d("---------- block end", Float.toString(mRadians[mCurrPointer]));
                    } else if (block_start) {
                        mRadians[mCurrPointer] = mRadians[prevIndex];
                        mAngles[mCurrPointer] = calculateAngleFromRadians(mRadians[mCurrPointer]);
                        //setText(String.valueOf(0));
                        Log.d("---------- block start", Float.toString(mRadians[mCurrPointer]));
                        updatePointerPositions();
                    } else {
                        mRadians[mCurrPointer] = calculateRadiansFromAngle(mAngles[mCurrPointer]);
                        mAngles[mCurrPointer] = calculateAngleFromRadians(mRadians[mCurrPointer]);
                        Log.d("---------- none", Float.toString(mRadians[mCurrPointer]));
                        //setText(String.valueOf(calculateTextFromAngle(arc_finish_radians)));
                        updatePointerPositions();
                    }
                    invalidate();
//                    if (mOnCircleSeekBarChangeListener != null)
//                        mOnCircleSeekBarChangeListener.onProgressChanged(this,
//                                Integer.parseInt(text), true);

                    mLastRadians[mCurrPointer] = mRadians[mCurrPointer];


                }
                break;
            case MotionEvent.ACTION_UP:
                mCurrPointer = -1;
                if (mOnCircleSeekBarChangeListener != null) {
                    mOnCircleSeekBarChangeListener.onStopTrackingTouch(this);
                }
                break;
        }
        // Fix scrolling
        if (event.getAction() == MotionEvent.ACTION_MOVE && getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        lastX = x;

        return true;
    }

    /**
     * Calculate the pointer's coordinates on the color wheel using the supplied
     * angle.
     *
     * @param angle
     *            The position of the pointer expressed as angle (in rad).
     *
     * @return The coordinates of the pointer's center in our internal
     *         coordinate system.
     */
    private float[] calculatePointerPosition(float angle) {
        // if (calculateRadiansFromAngle(angle) > end_wheel)
        // angle = calculateAngleFromRadians(end_wheel);
        float x = (float) (mColorWheelRadius * Math.cos(angle));
        float y = (float) (mColorWheelRadius * Math.sin(angle));

        return new float[] { x, y };
    }


    private int calculatePointerIndex(float x, float y) {
        float min = Float.MAX_VALUE;
        int index = 0;

        for (int i = 0; i < NUM_POINTERS; i++) {
            float dist = (float) Math.sqrt(Math.pow(x - pointerPositions[i][0], 2)
                            +  Math.pow(y - pointerPositions[i][1], 2));

            if (dist < min) {
                min = dist;
                index = i;
            }
        }

        return index;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        Bundle state = new Bundle();
        state.putParcelable(STATE_PARENT, superState);
        //state.putFloat(STATE_ANGLE, mAngle);

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;

        Parcelable superState = savedState.getParcelable(STATE_PARENT);
        super.onRestoreInstanceState(superState);

        //mAngle = savedState.getFloat(STATE_ANGLE);
        //arc_finish_radians = calculateRadiansFromAngle(mAngle);
        //setText(String.valueOf(calculateTextFromAngle(arc_finish_radians)));
        //updatePointerPosition();
    }

    public void setInitPosition(int init) {
     //   init_position = init;
      //  setText(String.valueOf(init_position));
      //  mAngle = calculateAngleFromRadians(init_position);
      //  arc_finish_radians = calculateRadiansFromAngle(mAngle);
        updatePointerPositions();
        invalidate();
    }

    public void setOnSeekBarChangeListener(OnCircleSeekBarChangeListener l) {
        mOnCircleSeekBarChangeListener = l;
    }

    public int getMaxValue() {
        return max;
    }

    public interface OnCircleSeekBarChangeListener {

        void onProgressChanged(CircleTriSeekBar seekBar, int progress, boolean fromUser);

        void onStartTrackingTouch(CircleTriSeekBar seekBar);

        void onStopTrackingTouch(CircleTriSeekBar seekBar);

    }

}