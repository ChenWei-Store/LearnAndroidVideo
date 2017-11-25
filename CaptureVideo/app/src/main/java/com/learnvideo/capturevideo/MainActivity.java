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
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.security.Permission;

public class MainActivity extends AppCompatActivity {
    private PermissionUtils permissionUtils;
    private TextView tvSurface;
    private TextView tvTexture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        permissionUtils = new PermissionUtils(this, new PermissionUtils.AfterRequestPermissionCallback() {
            @Override
            public void afterRequestPermissionCallback() {
            }
        });

        permissionUtils.checkPermission(Manifest.permission.CAMERA, this);

        tvTexture = (TextView)findViewById(R.id.tv_texture_view);
        tvSurface = (TextView)findViewById(R.id.tv_surface_view);

        tvTexture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTo(TextureViewActivity.class);
            }
        });

        tvSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTo(SurfaceViewActivity.class);
            }
        });
    }

    private void startTo(Class targetCls){
        Intent intent = new Intent(this, targetCls);
        startActivity(intent);
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


}
