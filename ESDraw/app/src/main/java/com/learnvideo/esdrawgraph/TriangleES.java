package com.learnvideo.esdrawgraph;

import java.nio.FloatBuffer;

/**
 * Created by Chenwei on 2017/11/22.
 */

public class TriangleES {

    // 片元着色器的脚本
    private final String fragmentsShader
            = "precision mediump float;" // 声明float类型的精度为中等(精度越高越耗资源)
            + "uniform vec4  uColor;" // uniform的属性uColor
            + "void main(){"
            + "   gl_FragColor = uColor;" // 给此片元的填充色
            + "}";

    private int verticesPositionSize = 3;
    private int verticesStartPosition = 0;
    private int verticesCount = 9;
    // 顶点着色器的脚本
    private final String verticesShader =
            "uniform mat4 uMVPMatrix;"
                    + "attribute vec4 vPosition;" // 顶点位置属性vPosition
                    + "void main(){"
                    + "   gl_Position = vPosition * uMVPMatrix;" // 确定顶点位置
                    + "}";

    private float triangleCoords[] = {
            //逆时针顺序排列顶点
            // in counterclockwise order:
            0.0f, 0.6f, 0.0f,   // top
            -0.5f, -0.2f, 0.0f,   // bottom left
            0.5f, -0.2f, 0.0f    // bottom right
    };

    public static final String UCOLOR = "uColor";
    public static final String UMVPMATRIX = "uMVPMatrix";
    public static final String VPOSITION = "vPosition";

//    private int verticesPositionSize = 2;
//    // 顶点着色器的脚本
//    private  final String verticesShader
//            = "attribute vec2 vPosition;" // 顶点位置属性vPosition
//            + "void main(){"
//            + "   gl_Position = vec4(vPosition,0,1);" // 确定顶点位置
//            + "}";
//
//    private float triangleCoords[] = {
//            //逆时针顺序排列顶点
//            // in counterclockwise order:
//            0.0f,   0.5f,
//            -0.5f, -0.5f,
//            0.5f,  -0.5f,
//    };


    private FloatBuffer floatBuffer;

    public TriangleES() {
        floatBuffer = RenderUtils.getVertices(triangleCoords);
    }

    public String getFragmentsShader() {
        return fragmentsShader;
    }

    public FloatBuffer getFloatBuffer() {
        return floatBuffer;
    }

    public String getVerticesShader() {
        return verticesShader;
    }

    public int getVerticesPositionSize() {
        return verticesPositionSize;
    }

    public int getVerticesStartPosition() {
        return verticesStartPosition;
    }

    public int getVerticesCount() {
        return verticesCount;
    }
}
