package com.learnvideo.esdrawgraph;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private GLSurfaceView glSurfaceView;
    private boolean isRender;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        glSurfaceView = (GLSurfaceView)findViewById(R.id.glsurfaceview);

        if(RenderUtils.isSupportEs2(this)) {
            //设置使用的opengl es版本
            glSurfaceView.setEGLContextClientVersion(2);
            //设置渲染器为绘制三角形的渲染器
//            glSurfaceView.setRenderer(new TriangleRender());

            //设置渲染器为绘制图片的渲染器
            glSurfaceView.setRenderer(new PictureRender(getResources()));
            //设置渲染模式
            //RENDERMODE_WHEN_DIRTY:只在surface被创建的时候渲染或者显示调用requestRender,
            // 使用该模式会省电，不需要更新时使用该模式
            //RENDERMODE_CONTINUOUSLY:(会以60fps的速度刷新)，默认是该模式
            glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            isRender = true;
        }else {
            Toast.makeText(this, "该设备不支持OpenGL ES 2.0", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isRender) {
            glSurfaceView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isRender){
            glSurfaceView.onPause();
        }
    }
}
