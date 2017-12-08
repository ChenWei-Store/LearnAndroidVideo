package com.learnvideo.esdrawvideo.render;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.learnvideo.esdrawvideo.RenderUtils;

import java.nio.FloatBuffer;


/**
 * Created by Chenwei on 2017/12/6.
 *
 */

public class VideoRender implements IRender{
    private final float[] triangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0, 0.f, 0.f,
            1.0f, -1.0f, 0, 1.f, 0.f,
            -1.0f, 1.0f, 0, 0.f, 1.f,
            1.0f, 1.0f, 0, 1.f, 1.f,
    };

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +      // highp here doesn't seem to matter
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    private FloatBuffer triangleVertices;
    private int program;
    private int textureId;
    private SurfaceTexture surfaceTexture;
    private Surface surface;

    private int aPositionLocation;
    private int aTextureCoordLocation;
    private int uMVPMatrixLocation;
    private int uSTMatrixLocation;
    private final String APOSITION_TAG = "aPosition";
    private final String ATEXTURE_COORD_TAG = "aTextureCoord";
    private final String USTMATRIX_TAG = "uSTMatrix";
    private final String UMVPMATRIX_TAG = "uMVPMatrix";

    private final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * 4;

    private float[] mMVPMatrix = new float[16];
    private float[] mSTMatrix = new float[16];

    private static final String TAG = VideoRender.class.getSimpleName();

    public VideoRender() {
        triangleVertices = RenderUtils.getVertices(triangleVerticesData);
        Matrix.setIdentityM(mSTMatrix, 0);
    }

    private boolean getGlSLAttrPosition(int program) {
        aPositionLocation = GLES20.glGetAttribLocation(program, APOSITION_TAG);
        aTextureCoordLocation = GLES20.glGetAttribLocation(program, ATEXTURE_COORD_TAG);
        uMVPMatrixLocation = GLES20.glGetUniformLocation(program, UMVPMATRIX_TAG);
        uSTMatrixLocation = GLES20.glGetUniformLocation(program, USTMATRIX_TAG);
        if (aPositionLocation == -1 || aTextureCoordLocation == -1
                || uMVPMatrixLocation == -1 || uSTMatrixLocation == -1) {

            Log.d(TAG, "get attribute error");
            return false;
        }

        return true;
    }

    public Surface getSurface(){
        return  surface;
    }

    public SurfaceTexture getSurfaceTexture(){
        return surfaceTexture;
    }

    @Override
    public void create() {
        program = RenderUtils.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (program == 0) {
            return;
        }
        if (!getGlSLAttrPosition(program)) {
            return;
        }

        textureId = RenderUtils.createTexture();
        if (textureId == 0) {
            return;
        }

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        surfaceTexture = new SurfaceTexture(textureId);
        surface = new Surface(surfaceTexture);
    }

    @Override
    public void draw() {
        if(surfaceTexture == null){
            return;
        }

        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(mSTMatrix);

        GLES20.glUseProgram(program);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        triangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, triangleVertices);
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        triangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(aTextureCoordLocation, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, triangleVertices);
        GLES20.glEnableVertexAttribArray(aTextureCoordLocation);

        Matrix.setIdentityM(mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(uSTMatrixLocation, 1, false, mSTMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glFinish();
    }

    @Override
    public void prepareDraw(int parentWidth, int parentHeight) {
        GLES20.glViewport(0, 0, parentWidth, parentHeight);

    }
}
