package com.learnvideo.esdrawgraph;

import java.nio.FloatBuffer;

/**
 * Created by Chenwei on 2017/11/22.
 */

public class PictureES {
    private FloatBuffer floatBuffer;
    private int verticesPositionSize = 2;
    private int textureCoordinatesSize = 2;
    private int vertexCount = 4; //顶点坐标数量
    private int Stride = (verticesPositionSize
            + textureCoordinatesSize) * 4;

    public static final String USAMPLERTEXTURE = "uSamplerTexture";
    public static final String UMVPMATRIX = "uMVPMatrix";
    public static final String APOSITION = "aPosition";
    public static final String ATEXCOORD = "aTexCoord";




    //每行的前两个是屏幕对应的(x, y)坐标，后来两个为纹理(s, t)坐标。
    // 矩形右下角取纹理右上角的颜色
    private static final float[] pictureCoords = {
            -0.5f,  0.5f ,  0 , 0 , // top left
            0.5f,  0.5f  ,  1 , 0 ,// top right
            0.5f, -0.5f  ,  1 , 1 ,// bottom right
            -0.5f, -0.5f  ,  0 , 1};  // bottom left

    // 片元着色器的脚本
    private  final String fragmentsShader
            = "precision mediump float;" // 声明float类型的精度为中等(精度越高越耗资源)
            + "varying vec2 vTexCoord;"
            + "uniform sampler2D uSamplerTexture;"
            + "void main(){"
            + "gl_FragColor = texture2D(uSamplerTexture, vTexCoord);"
            + "}";

    // 顶点着色器的脚本
    private  final String verticesShader
            ="uniform mat4 uMVPMatrix;"
            +"attribute vec4 aPosition;"
            + "attribute vec2 aTexCoord;"
            + "varying vec2 vTexCoord;"
            + "void main(){"
            +"gl_Position = aPosition * uMVPMatrix;"
            +"vTexCoord = aTexCoord;"
            + "}";

    public PictureES(){
        floatBuffer =  RenderUtils.getVertices(pictureCoords);
    }

    public String getFragmentsShader() {
        return fragmentsShader;
    }

    public String getVerticesShader() {
        return verticesShader;
    }

    public FloatBuffer getFloatBuffer() {
        return floatBuffer;
    }

    public int getVerticesPositionSize() {
        return verticesPositionSize;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getStride() {
        return Stride;
    }

    public int getTextureCoordinatesSize() {
        return textureCoordinatesSize;
    }
}
