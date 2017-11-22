package com.learnvideo.esdrawgraph;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    private GLSurfaceView glSurfaceView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        glSurfaceView = (GLSurfaceView)findViewById(R.id.glsurfaceview);
        //设置使用的opengl es版本
        glSurfaceView.setEGLContextClientVersion(2);
        //设置渲染器为绘制三角形的渲染器
        glSurfaceView.setRenderer(new TriangleRender());
        //设置渲染模式
        //RENDERMODE_WHEN_DIRTY:只在surface被创建的时候渲染或者显示调用requestRender,
        // 使用该模式会省电，不需要更新时使用该模式
        //RENDERMODE_CONTINUOUSLY:(会以60fps的速度刷新)，默认是该模式
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
