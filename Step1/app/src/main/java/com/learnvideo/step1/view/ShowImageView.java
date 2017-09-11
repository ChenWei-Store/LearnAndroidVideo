package com.learnvideo.step1.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.View;

import com.learnvideo.step1.R;

/**
 * Created by Chenwei on 2017/8/29.
 */

public class ShowImageView extends View {
    @DrawableRes
    private int drawableId;
    private Bitmap bmp;

    public ShowImageView(Context context) {
        this(context, null);
    }

    public ShowImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShowImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ShowImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ShowImageView);
        drawableId = ta.getResourceId(R.styleable.ShowImageView_drawableRes, -1);
        ta.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        bmp = BitmapFactory.decodeResource(getResources(), drawableId);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = 0;
        int height = 0;
        if(widthMode == MeasureSpec.EXACTLY){
            width = MeasureSpec.getSize(widthMeasureSpec);
        }else{
            width = bmp.getWidth();
        }

        if(heightMode == MeasureSpec.EXACTLY){
            height = MeasureSpec.getSize(heightMeasureSpec);
        }else{
            height = bmp.getHeight();
        }
        setMeasuredDimension(width, height);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int bw = bmp.getWidth();
        int bh = bmp.getHeight();
        int cw = canvas.getWidth();
        int ch = canvas.getHeight();

        //如果图片本身尺寸和需要显示的尺寸不同，对图片进行处理
        if(bw != cw || bh != ch) {
            float wScale = cw * 1.0f / bw;
            float hScale = ch * 1.0f / bh;
            float scaleResult = 0;
            //默认情况下为了尽量保持图片不变形，使用是宽高缩放值中小的那个作为缩放值
            if (wScale < hScale) {
                scaleResult = wScale;
            } else {
                scaleResult = hScale;
            }


            Matrix matrix = new Matrix();
            matrix.postScale(scaleResult, scaleResult);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bw, bh, matrix, true);
        }
        canvas.drawBitmap(bmp, 0, 0, new Paint());
        bmp.recycle();
    }
}
