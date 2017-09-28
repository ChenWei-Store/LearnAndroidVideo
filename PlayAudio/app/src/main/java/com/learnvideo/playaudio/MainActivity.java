package com.learnvideo.playaudio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.learnvideo.playaudio.manager.PlayAudioManager;
import com.learnvideo.playaudio.model.PlayParams;
import com.learnvideo.playaudio.model.RecordParams;

public class MainActivity extends AppCompatActivity{
    private AudioTimer audioTimer;
    private TextView tvCountDown;
    private final int audioRecordSeconds = 120; //second
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAudio();
        initPlay();
        tvCountDown = (TextView)findViewById(R.id.tv_count_down);
        //启动录音
        findViewById(R.id.tv_record_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayAudioManager.getInstance(MainActivity.this).startRecordAudio();
                startTimer();
            }
        });

        //停止录音
        findViewById(R.id.tv_record_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayAudioManager.getInstance(MainActivity.this).stopRecordAudio();
                stopTimer();
            }
        });

        //播放录音
        findViewById(R.id.tv_play_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayAudioManager.getInstance(MainActivity.this)
                        .startPlay(AudioTrack.MODE_STREAM);
            }
        });

        //停止播放
        findViewById(R.id.tv_play_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayAudioManager.getInstance(MainActivity.this)
                        .stopPlay();
            }
        });
    }

    /**
     * 启动计时器
     */
    private void startTimer(){
        audioTimer = new AudioTimer(audioRecordSeconds * 1000);
        audioTimer.start();
    }

    private void stopTimer(){
        if(audioTimer != null){
            audioTimer.cancel();
            audioTimer = null;
        }
        PlayAudioManager.getInstance(this).stopPlay();
        tvCountDown.setText("finish");
    }

    /**
     * 初始化音频参数
     */
    private void initAudio(){
        RecordParams audioParams = new
                RecordParams.Builder()
                .setAudioSource(MediaRecorder.AudioSource.DEFAULT)
                .setSampleRateInHz(44100)
                .setChannelConfig(AudioFormat.CHANNEL_IN_MONO )
                .build();

        PlayAudioManager.getInstance(this)
                .setAudioConfig(audioParams);
    }

    private void initPlay(){
        PlayParams playParams = new PlayParams.Builder()
                .setChannelConfig(AudioFormat.CHANNEL_OUT_MONO)
                .setStreamType(AudioManager.STREAM_MUSIC)
                .build();
        PlayAudioManager.getInstance(this)
                .setPlayConfig(playParams);

    }


    class AudioTimer extends CountDownTimer{

        public AudioTimer(long millisInFuture) {
            super(millisInFuture, 1000);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            tvCountDown.setText(String.valueOf(millisUntilFinished / 1000));
        }

        @Override
        public void onFinish() {
            PlayAudioManager.getInstance(MainActivity.this).stopRecordAudio();
            tvCountDown.setText("finish");
        }
    }


    @Override
    protected void onDestroy() {
        PlayAudioManager.getInstance(this).release();
        super.onDestroy();
    }


}
