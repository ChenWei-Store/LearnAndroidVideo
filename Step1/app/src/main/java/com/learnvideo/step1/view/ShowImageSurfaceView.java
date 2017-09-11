package com.learnvideo.step1.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.learnvideo.step1.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Chenwei on 2017/8/29.
 *
 */

public class ShowImageSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    @DrawableRes private int drawableId;
    private Bitmap bmp;
    private int scaleType;
    private static final int DEF = 0;
    private static final int X = 1;
    private static final int Y = 2;
    @IntDef({DEF, X, Y})
    @Retention(RetentionPolicy.SOURCE)
    @interface ScaleType{

    }

    public ShowImageSurfaceView(Context context) {
        this(context, null);
    }

    public ShowImageSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShowImageSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ShowImageSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        //设置初始背景为透明
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        getHolder().addCallback(this);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ShowImageSurfaceView);
        drawableId = ta.getResourceId(R.styleable.ShowImageSurfaceView_drawableId, -1);
        scaleType = ta.getInt(R.styleable.ShowImageSurfaceView_scaleType, 0);
        ta.recycle();
    }

    /**
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.landscape);

        int fixedWidth = -1;
        int fixedHeight = -1;
        if(widthMode == MeasureSpec.AT_MOST){
            fixedWidth = bmp.getWidth();
        }
        if(heightMode == MeasureSpec.AT_MOST){
            fixedHeight = bmp.getHeight();
        }
        //当specMode 为wrap_content时，将bitmap的默认尺寸设置为SurfaceView的默认宽高
        if(fixedWidth != -1 || fixedHeight != -1){
            getHolder().setFixedSize(fixedWidth, fixedHeight);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }




    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(drawableId == -1){
            return;
        }

        int bw = bmp.getWidth();
        int bh = bmp.getHeight();
        Canvas canvas = getHolder().lockCanvas();
        int cw = canvas.getWidth();
        int ch = canvas.getHeight();

        //如果图片本身尺寸和需要显示的尺寸不同，对图片进行处理
        if(bw != cw || bh != ch) {
            float wScale = cw * 1.0f / bw;
            float hScale = ch * 1.0f / bh;
            float scaleResult = 0;
            if (scaleType == DEF) {
                //默认情况下为了尽量保持图片不变形，使用是宽高缩放值中小的那个作为缩放值
                if (wScale < hScale) {
                    scaleResult = wScale;
                } else {
                    scaleResult = hScale;
                }

            }else if(scaleType == X){
                //以x轴的缩放值作为标准
                scaleResult = wScale;
            }else if(scaleType == Y){
                //以y轴缩放值作为标准
                scaleResult = hScale;
            }
            Matrix matrix = new Matrix();
            matrix.postScale(scaleResult, scaleResult);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bw, bh, matrix, true);
        }
        canvas.drawBitmap(bmp, 0, 0, new Paint());
        getHolder().unlockCanvasAndPost(canvas);
        bmp.recycle();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
