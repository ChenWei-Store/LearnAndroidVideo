package com.learnvideo.esdrawvideo.render;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.learnvideo.esdrawvideo.R;
import com.learnvideo.esdrawvideo.RenderUtils;



/**
 * Created by Chenwei on 2017/11/22.
 * 绘制图片的渲染器
 */

public class PictureRender {
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

    public void onSurfaceCreated() {
        //值的范围0-1(黑-白)
        //指定刷新颜色缓冲区时所用的颜色,与glClear配合使用
//        GLES20.glClearColor(1f, 1f, 1f, 0f);

        program = RenderUtils.createProgram(pictureES.getVerticesShader(), pictureES.getFragmentsShader());
        aPositionLocation = GLES20.glGetAttribLocation(program, PictureES.APOSITION);
        aTexCoordPosition = GLES20.glGetAttribLocation(program, PictureES.ATEXCOORD);
        uSamplerTexturePosition = GLES20.glGetUniformLocation(program,
                PictureES.USAMPLERTEXTURE);
        uMVPMatrixPosition = GLES20.glGetUniformLocation(program, PictureES.UMVPMATRIX);
        //使用原始图像数据
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        bmp = BitmapFactory.decodeResource(resources, R.drawable.watermark, options);
        textureId = RenderUtils.createTexture();
        if(textureId == 0){
            Log.d("Render", "创建纹理失败");
            return;
        }

        if(bmp!=null){
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
            //对画面进行矩阵旋转
        }
    }

    public void onSurfaceChanged(int width, int height) {
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


    public void onDrawFrame() {
        //初始化背景色，与glClearColor配合使用
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

//        GLES20.glViewport(30, 50, bmp.getWidth(), bmp.getHeight());
//        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
//        GLES20.glEnable(GLES20.GL_BLEND);
//        GLES20.glBlendFunc(GLES20.GL_SRC_COLOR, GLES20.GL_DST_ALPHA);


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

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

        GLES20.glUniformMatrix4fv(uMVPMatrixPosition, 1, false, projectMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, pictureES.getVertexCount());
        GLES20.glDisable(GLES20.GL_BLEND);

    }

    public void prepareDraw(int parentWidth, int parentHeight){
        int bw = bmp.getWidth();
        int bh = bmp.getHeight();

        Log.d("xxx", "parentWidth: " + parentWidth);
        Log.d("xxx", "parentHeight: " + parentHeight);
        Log.d("xxx", "bw: " + bw);
        Log.d("xxx", "bh: " + bh);

        int width = parentWidth - bw;
        int height = bh;


        //以左下角作为原点(0, 0)
        GLES20.glViewport(width, height, bw, bh);

        int viewPortWidth = bw;
        int viewPortHeight = bh;
        //计算投影变换
//        final float aspectRatio = viewPortWidth > viewPortHeight?
//                (float) viewPortWidth / viewPortHeight :
//                (float) viewPortHeight / viewPortWidth;
//        if(width > height){
//            Matrix.orthoM(projectMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
//        }else{
//            Matrix.orthoM(projectMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
//        }
    }

}
