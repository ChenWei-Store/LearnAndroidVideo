package com.learnvideo.esdrawvideo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Chenwei on 2017/11/22.
 */

public class RenderUtils {
    /**
     * 由于OpenGL ES底层用c/c++写的，而c/c++的字节序列和java的字节顺序(在内存中的存放顺序)不一样，
     * 所以需要转换成c/c++格式的字节序列传递给底层
     * @param vertices
     * @return
     */
    public static FloatBuffer getVertices(float []vertices){
        // 创建顶点坐标数据缓冲
        // vertices.length*4是因为一个float占四个字节
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());             //设置字节顺序
        FloatBuffer vertexBuf = vbb.asFloatBuffer();    //转换为Float型缓冲
        vertexBuf.put(vertices);
        vertexBuf.position(0);
        //向缓冲区中放入顶点坐标数据
        return vertexBuf;
    }

    /**
     * 创建opengles程序，如果执行失败，返回0
     * @param verticesShader
     * @param fragmentsShader
     * @return
     */
    public static int createProgram(String verticesShader, String fragmentsShader){
        //创建顶点着色器
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, verticesShader);
        if(vertexShader == 0){
            //创建顶点着色器失败
            Log.d("Render", "create vertextShader error");
            return 0;
        }

        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentsShader);
        if(fragmentShader == 0){
            //创建片元着色器失败
            Log.d("Render", "create fragmentShader error");
            return 0;
        }

        //创建es程序
        int program = GLES20.glCreateProgram();
        if(program != 0){
            //如果创建成功，绑定顶点着色器，片元着色器并链接程序
            GLES20.glAttachShader(program, vertexShader);
            GLES20.glAttachShader(program, fragmentShader);
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            // 获取program的链接情况
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            // 若链接失败则报错并删除程序
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e("Render", "Could not link program: ");
                Log.e("Render", GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }else {
            Log.d("Render", "create program error");
            //创建失败的话删除程序
            GLES20.glDeleteProgram(program);
            program = 0;
        }

        return program;
    }

    /**
     * 根据type创建一个shader
     * @param type
     */
    private static int loadShader(int type, String source){
        int shader = GLES20.glCreateShader(type);
        if(shader != 0){
            //创建成功
            //绑定shader与shader源码
            GLES20.glShaderSource(shader, source);
            //编译shader
            GLES20.glCompileShader(shader);
            int []compiled = new int[1];
            //编译结果
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if(compiled[0] == 0){
                //编译失败
                GLES20.glDeleteShader(shader);
                shader = 0;
                Log.d("Render", "shader创建失败");
                Log.d("Render", "失败原因： " + GLES20.glGetShaderInfoLog(shader));
            }
        }
        return shader;
    }

    public static int createTexture() {
        int[] textureId = new int[1];
        // 创建Texture对象
        GLES20.glGenTextures(1, textureId, 0);
        if (textureId[0] == 0) {
            //创建成功

            return -1;
        }
        return textureId[0];
    }

    public static boolean isSupportEs2(Context context){
        ActivityManager am =  (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        return am.getDeviceConfigurationInfo().reqGlEsVersion >= 0x2000 ;
    }

    public static String readGLSLFromRaw(Context context, int resId){
        StringBuilder glslSb = new StringBuilder();
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            is = context.getResources().openRawResource(resId);
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            String nextLine;
            while ((nextLine = br.readLine()) != null){
                glslSb.append(nextLine)
                        .append("\n");
            }
        } catch (IOException e) {
            Log.d("Render", "read glsl raw file error");
            Log.d("Render", "error reason: " + e.getMessage());
        }catch (Resources.NotFoundException e){
            Log.d("Render", "read glsl raw file error");
            Log.d("Render", "error reason: " + "can not found glsl raw resource");
        }

        try {
            br.close();
            isr.close();
            is.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return glslSb.toString();
    }
}
