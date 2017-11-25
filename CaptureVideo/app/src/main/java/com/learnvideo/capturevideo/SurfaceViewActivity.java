package com.learnvideo.capturevideo;

import android.Manifest;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;

public class SurfaceViewActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        Camera.PreviewCallback{
    private SurfaceView surfaceView;
    private PermissionUtils permissionUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        surfaceView = (SurfaceView)findViewById(R.id.surface_view);
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        permissionUtils = new PermissionUtils(this, new PermissionUtils.AfterRequestPermissionCallback() {
            @Override
            public void afterRequestPermissionCallback() {
                startCamera();
            }
        });
        permissionUtils.checkPermission(Manifest.permission.CAMERA, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CameraService.getInstance().close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionUtils.requestPermissionResult(this, Manifest.permission.CAMERA,
                requestCode, grantResults, "相机");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        permissionUtils.onGoSettingResult(requestCode, Manifest.permission.CAMERA);
    }

    private void startCamera(){
        //Surface建立后在启动camera
//        surfaceView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                CameraService.getInstance().init(getApplicationContext(), new Camera.PreviewCallback() {
//                            @Override
//                            public void onPreviewFrame(byte[] data, Camera camera) {
//
//                            }
//                        }, 1);
//            }
//        }, 500);


        surfaceView.getHolder().addCallback(this);
        //为开启预览时通过隐藏surfaceView来显示白色背景
        surfaceView.setVisibility(View.VISIBLE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        CameraService.getInstance().open(this, 1, this, surfaceView.getHolder());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        CameraService.getInstance().startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CameraService.getInstance().close();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }
}
