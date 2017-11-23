package com.learnvideo.esdrawgraph;

import java.nio.FloatBuffer;

/**
 * Created by Chenwei on 2017/11/22.
 */

public class PictureES {
    private FloatBuffer floatBuffer;
    private int verticesPositionSize = 3;

    //纹理坐标
    private float pictureCoords[] = {
            //逆时针顺序排列顶点
            // in counterclockwise order:
            -0.5f,  -1f, 0.0f,   // bottom left
            -0.5f, 1f, 0.0f,   //  top left
            0.5f, 1f, 0.0f,    //top right
            0.5f, -1f, 0f,    //bottom right
    };

    // 片元着色器的脚本
    private  final String fragmentsShader
            = "precision mediump float;" // 声明float类型的精度为中等(精度越高越耗资源)
            + "uniform vec4  uColor;" // uniform的属性uColor
            + "void main(){"
            + "   gl_FragColor = uColor;" // 给此片元的填充色
            + "}";

    // 顶点着色器的脚本
    private  final String verticesShader
            ="attribute vec4 a_position;"
            + "attribute vec2 a_texCoord;"
            + "varying vec2 v_texCoord;"
            + "void main(){"
                +"gl_Position = a_position;"
                +"v_texCoord = a_texCoord;"
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
}
