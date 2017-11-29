package com.learnvideo.androidcodecdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.tv_decode_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTo(DecodeLocalVideoActivity.class);
            }
        });

        findViewById(R.id.tv_decode_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTo(DecodeLocalAudioActivity.class);
            }
        });
    }

    private void startTo(Class cls){
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }


}
