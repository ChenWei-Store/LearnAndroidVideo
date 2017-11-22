package com.learnvideo.esdrawgraph;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Chenwei on 2017/11/21.
 * 通过OpenGL ES绘制三角形
 */

public class TriangleRender implements GLSurfaceView.Renderer {
    private static final String TAG = TriangleRender.class.getSimpleName();

    private int program;
    private int vPosition;
    private int uColor;
    private TriangleES triangleES;
    float []projectionMatrix = new float[16];
    float []viewMatrix = new float[16];
    float []mvpMatrix = new float[16];
    public TriangleRender(){
        triangleES = new TriangleES();
    }
    /**
     *
     * @param gl
     * @param config
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //值的范围0-1(黑-白)
        //指定刷新颜色缓冲区时所用的颜色,与glClear配合使用
        GLES20.glClearColor(1f, 1f, 1f, 0f);

        program = RenderUtils.createProgram(triangleES.getVerticesShader(), triangleES.getFragmentsShader());
        // 获取着色器中的属性引用id(传入的字符串就是我们着色器脚本中的属性名)
        vPosition = GLES20.glGetAttribLocation(program, "vPosition");
        uColor = GLES20.glGetUniformLocation(program, "uColor");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //设置绘图的窗口范围
        GLES20.glViewport(0, 0, width, height);

        //计算投影变换
        float ratio = (float) width / height;
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //初始化背景色，与glClearColor配合使用
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //获取虚拟的相机视角变换
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        //相机视角变换和投影变换
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);


        // 绑定当前要用的程序
        GLES20.glUseProgram(program);
        // 为画笔指定顶点位置数据
        // (参数:要修改的顶点属性的索引值, 每个顶点属性的坐标数量,
        // 数组中每个组件的数据类型, 指定当被访问时，固定点数据值是否应该被归一化,
        // 指定连续顶点属性之间的偏移量,第一个组件在数组的第一个顶点属性中的偏移量)
        GLES20.glVertexAttribPointer(vPosition, triangleES.getVerticesPositionSize(), GLES20.GL_FLOAT, false, 0,
                triangleES.getFloatBuffer());
        // 允许顶点位置数据数组
        GLES20.glEnableVertexAttribArray(vPosition);

        // 设置属性uColor所用的颜色，也就是绘制图形的颜色(参数: 索引,R,G,B,A)
        GLES20.glUniform4f(uColor, 1.0f, 0.0f, 0.0f, 1.0f);

        //应用投影变换和虚拟的相机视角变换
        int mVPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mVPMatrixHandle, 1, false, mvpMatrix, 0);

        // 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 3);
    }
}
