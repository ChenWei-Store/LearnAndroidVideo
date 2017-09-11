package com.learnvideo.step1.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Chenwei on 2017/8/31.
 */

public class TestView extends View {
    public TestView(Context context) {
        this(context, null);
    }

    public TestView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TestView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(600, 600);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int cx = canvas.getWidth() / 2;
        int cy = canvas.getHeight() / 2;
        int radius = 100;

        Paint paint= new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(ContextCompat.getColor(getContext(), android.R.color.black));
        paint.setStrokeWidth(4);
        canvas.drawCircle(cx, cy, radius, paint);

        Paint paint2= new Paint();
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeWidth(20);
        paint2.setColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
        canvas.drawCircle(cx, cy, radius + 4, paint2);
//
//        Paint paint3= new Paint();
//        paint3.setStyle(Paint.Style.STROKE);
//        paint3.setStrokeWidth(40);
//        paint3.setColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));
//        canvas.drawCircle(cx, cy, radius + 40 + 4, paint3);
    }

}
