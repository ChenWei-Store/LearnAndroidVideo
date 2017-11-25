package com.learnvideo.capturevideo;

import android.Manifest;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.TextureView;

public class TextureViewActivity extends AppCompatActivity
        implements TextureView.SurfaceTextureListener, Camera.PreviewCallback{

    private TextureView textureView;
    private PermissionUtils permissionUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textureView = (TextureView)findViewById(R.id.texture_view);

        permissionUtils = new PermissionUtils(this, new PermissionUtils.AfterRequestPermissionCallback() {
            @Override
            public void afterRequestPermissionCallback() {
                if(textureView.isAvailable()){
                    //textureView已绑定listener,直接开启camera
                    CameraService.getInstance().open(TextureViewActivity.this, 1,
                            TextureViewActivity.this, textureView.getSurfaceTexture());
                    CameraService.getInstance().startPreview();
                }else {
                    textureView.setSurfaceTextureListener(TextureViewActivity.this);
                }
            }
        });

        permissionUtils.checkPermission(Manifest.permission.CAMERA, this);
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

    @Override
    protected void onPause() {
        super.onPause();
        CameraService.getInstance().close();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        CameraService.getInstance().open(this, 1, this, textureView.getSurfaceTexture());
        CameraService.getInstance().startPreview();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        CameraService.getInstance().close();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
    }
}
