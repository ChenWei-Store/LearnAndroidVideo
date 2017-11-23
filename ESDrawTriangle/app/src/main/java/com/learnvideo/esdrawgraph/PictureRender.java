package com.learnvideo.esdrawgraph;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Chenwei on 2017/11/22.
 */

public class PictureRender implements GLSurfaceView.Renderer{
    private int program;
    private PictureES pictureES;
    private int[] textureParams;
    private Context context;
    public PictureRender(Context context){
        this.context = context;
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //值的范围0-1(黑-白)
        //指定刷新颜色缓冲区时所用的颜色,与glClear配合使用
        GLES20.glClearColor(1f, 1f, 1f, 0f);

        //启用2D纹理
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        //激活纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        program = RenderUtils.createProgram(pictureES.getVerticesShader(), pictureES.getFragmentsShader());
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.test);
        textureParams = RenderUtils.loadTexture(bmp);
        if(textureParams == null || textureParams.length == 0 || textureParams[0] == 0){
            Log.d("Render", "创建纹理失败");
        }else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureParams[0]);
        }


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //设置绘图的窗口范围
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //初始化背景色，与glClearColor配合使用
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureParams[0]);

    }
}
