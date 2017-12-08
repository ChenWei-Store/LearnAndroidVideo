package com.learnvideo.esdrawvideo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private VideoPreviewView videoPreviewView;
    private String path = "/mnt/sdcard/test/test";
    private DecodeMp4Manager decodeMp4Manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!RenderUtils.isSupportEs2(this)){
            Toast.makeText(this, "手机设备不支持Android版本", Toast.LENGTH_SHORT).show();
            return;
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        videoPreviewView = (VideoPreviewView) findViewById(R.id.videoPeviewView);
        decodeMp4Manager = new DecodeMp4Manager(path, new DecodeMp4Manager.DecodeVideoCallback() {
            @Override
            public void onGotVideoInfo() {
                int height = decodeMp4Manager.getVideoHeight();
                int width = decodeMp4Manager.getVideoWidth();

                videoPreviewView.setVideoSize(width, height);
            }

            @Override
            public void onPrepared() {
                decodeMp4Manager.start();
            }

            @Override
            public void onFinished() {

            }
        });

        findViewById(R.id.tv_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decodeMp4Manager.prepare(videoPreviewView.getSurface());
            }
        });
        decodeMp4Manager.getVideoInfo();
//        videoPreviewView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                decodeMp4Manager.prepare(videoPreviewView.getSurface());
//                decodeMp4Manager.start();
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoPreviewView.onResume();
    }



    @Override
    protected void onPause() {
        super.onPause();
        videoPreviewView.onPause();
        if(decodeMp4Manager != null) {
            decodeMp4Manager.releaseMediaData();
        }
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
