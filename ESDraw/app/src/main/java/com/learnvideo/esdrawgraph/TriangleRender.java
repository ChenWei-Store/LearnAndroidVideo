package com.learnvideo.esdrawgraph;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Chenwei on 2017/11/21.
 * 通过OpenGL ES绘制三角形
 */

public class TriangleRender implements GLSurfaceView.Renderer {

    private int program;
    private int vPositionLocation;
    private int uColorLocation;
    private int mvpMatrixPosition;
    private TriangleES triangleES;
    float []projectionMatrix = new float[16];
    float []viewMatrix = new float[16];
    float []mvpMatrix = new float[16];
    public TriangleRender(){
        triangleES = new TriangleES();
    }
    /**
     *surface被创建时调用
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
        vPositionLocation = GLES20.glGetAttribLocation(program, TriangleES.VPOSITION);
        uColorLocation = GLES20.glGetUniformLocation(program, TriangleES.UCOLOR);
        mvpMatrixPosition = GLES20.glGetUniformLocation(program, TriangleES.UMVPMATRIX);

        //因为传给glsl的参数不会变，所以在onSurfaceCreated设置
        // 绑定当前要用的程序
        GLES20.glUseProgram(program);

        //从头开始读数据
        triangleES.getFloatBuffer().position(0);
        // 为画笔指定顶点位置数据
        // (参数:要修改的顶点属性的索引值, 每个顶点属性的坐标数量,
        // 数组中每个组件的数据类型, 指定当被访问时，固定点数据值是否应该被归一化,
        // 指定连续顶点属性之间的偏移量,第一个组件在数组的第一个顶点属性中的偏移量)
        GLES20.glVertexAttribPointer(vPositionLocation, triangleES.getVerticesPositionSize(), GLES20.GL_FLOAT, false, 0,
                triangleES.getFloatBuffer());
        // 设置顶点位置数据的位置
        GLES20.glEnableVertexAttribArray(vPositionLocation);

        // 设置属性uColor所用的颜色，也就是绘制图形的颜色(参数: 索引,R,G,B,A)
        GLES20.glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
    }

    /**
     * surface窗口发生改变
     * @param gl
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //设置绘图的窗口范围
        GLES20.glViewport(0, 0, width, height);

        //计算投影变换
        float ratio = (float) width / height;
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    /**
     * 绘制帧时调用，后台执行
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        //初始化背景色，与glClearColor配合使用
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //获取虚拟的相机视角变换
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //相机视角变换和投影变换
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        //应用投影变换和虚拟的相机视角变换
        GLES20.glUniformMatrix4fv(mvpMatrixPosition, 1, false, mvpMatrix, 0);
        // 绘制图形
        //参数：指定绘制形状，指定起始顶点位置，指定顶点数量
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, triangleES.getVerticesStartPosition(), triangleES.getVerticesCount());
    }
}
