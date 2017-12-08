package com.learnvideo.esdrawvideo;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;

import com.learnvideo.esdrawvideo.render.PictureRender;
import com.learnvideo.esdrawvideo.render.VideoRender;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Created by Chenwei on 2017/12/6.
 */

public class VideoPreviewView extends GLSurfaceView
        implements SurfaceTexture.OnFrameAvailableListener, GLSurfaceView.Renderer {
    private VideoRender videoRender;
    private PictureRender pictureRender;
    private int videoWidth;
    private int videoHeight;

    private int surfaceWidth;
    private int surfaceHeight;
    private boolean isStartPlay;
    public VideoPreviewView(Context context) {
        super(context);
        init();
    }

    public VideoPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d("xxx", "onMeasure");
        if(videoHeight > 0 && videoWidth > 0){
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            if(width < height){
                height = width * videoHeight / videoWidth;
            }else {
                width = height * videoWidth / videoHeight;
            }
            setMeasuredDimension(width, height);
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    public void init(){
        videoRender = new VideoRender();
        pictureRender = new PictureRender(getResources());

        setEGLContextClientVersion(2);

        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
    }

    public Surface getSurface(){
        return videoRender.getSurface();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 0f);

        videoRender.create();
        videoRender.getSurfaceTexture().setOnFrameAvailableListener(this);
        pictureRender.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        surfaceWidth = width;
        surfaceHeight = height;
        //设置绘图的窗口范围
        GLES20.glViewport(0, 0, width, height);
        pictureRender.onSurfaceChanged(width, height);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        if(videoRender.getSurface() != null ) {
            videoRender.prepareDraw(surfaceWidth, surfaceHeight);
            videoRender.draw();
            pictureRender.prepareDraw(surfaceWidth, surfaceHeight);
            pictureRender.onDrawFrame();
        }
    }

    public void setVideoSize(int width, int height){
        this.videoWidth =  width;
        this.videoHeight = height;
        requestLayout();
    }
}
