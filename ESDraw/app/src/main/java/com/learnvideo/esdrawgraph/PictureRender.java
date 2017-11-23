package com.learnvideo.esdrawgraph;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Created by Chenwei on 2017/11/22.
 * 绘制图片的渲染器
 */

public class PictureRender implements GLSurfaceView.Renderer{
    private int program;
    private PictureES pictureES;
    private int textureId;
    private Resources resources;
    private int aPositionLocation;
    private int  aTexCoordPosition;
    private int uSamplerTexturePosition;
    private int uMVPMatrixPosition;
    private Bitmap bmp;
    private float []projectMatrix = new float[16];;
    public PictureRender(Resources resources){
        this.resources = resources;
        pictureES = new PictureES();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //值的范围0-1(黑-白)
        //指定刷新颜色缓冲区时所用的颜色,与glClear配合使用
        GLES20.glClearColor(1f, 1f, 1f, 0f);

        program = RenderUtils.createProgram(pictureES.getVerticesShader(), pictureES.getFragmentsShader());
        aPositionLocation = GLES20.glGetAttribLocation(program, PictureES.APOSITION);
        aTexCoordPosition = GLES20.glGetAttribLocation(program, PictureES.ATEXCOORD);
        uSamplerTexturePosition = GLES20.glGetUniformLocation(program,
                PictureES.USAMPLERTEXTURE);
        uMVPMatrixPosition = GLES20.glGetUniformLocation(program, PictureES.UMVPMATRIX);
        //使用原始图像数据
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        bmp = BitmapFactory.decodeResource(resources, R.drawable.test, options);
        textureId = RenderUtils.loadTexture(bmp);
        if(textureId == 0){
            Log.d("Render", "创建纹理失败");
            return;
        }


        //因为传给glsl的参数不会变，所以在onSurfaceCreated设置
        GLES20.glUseProgram(program);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(uSamplerTexturePosition,  0);

        //传递矩形顶点坐标
        pictureES.getFloatBuffer().position(0);
        GLES20.glVertexAttribPointer(aPositionLocation, pictureES.getVerticesPositionSize(),
                GLES20.GL_FLOAT, false, pictureES.getStride(),
                pictureES.getFloatBuffer());
        GLES20.glEnableVertexAttribArray(aPositionLocation);

        //传递纹理顶点坐标
        pictureES.getFloatBuffer().position(2);
        GLES20.glVertexAttribPointer(aTexCoordPosition, pictureES.getTextureCoordinatesSize(),
                GLES20.GL_FLOAT, false, pictureES.getStride(),
                pictureES.getFloatBuffer());
        GLES20.glEnableVertexAttribArray(aTexCoordPosition);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //设置绘图的窗口范围
        GLES20.glViewport(0, 0, width, height);

        //计算投影变换
        final float aspectRatio = width > height ?
                (float) width / (float) height :
                (float) height / (float) width;
        if(width > height){
            Matrix.orthoM(projectMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        }else{
            Matrix.orthoM(projectMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }
    }


    @Override
    public void onDrawFrame(GL10 gl) {
        //初始化背景色，与glClearColor配合使用
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);




        GLES20.glUniformMatrix4fv(uMVPMatrixPosition, 1, false, projectMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, pictureES.getVertexCount());
    }

}
