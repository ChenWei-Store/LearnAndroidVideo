package com.learnvideo.savenetworkvideo;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    private static String videoUrl = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
    private Handler saveNetworkVideoHandler;
    private TextView tvDownLoad;
    private LoadingDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadingDialog = new LoadingDialog(this);

        tvDownLoad = (TextView)findViewById(R.id.tv_download);

        tvDownLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.show();
                saveNetworkVideoHandler.sendEmptyMessage(0);

            }
        });
        HandlerThread handlerThread = new HandlerThread("SaveNetworkVideo");
        handlerThread.start();
        saveNetworkVideoHandler = new Handler(handlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    //前台通知，AsyncTask提示进度
                    muxingAudioAndVideo();
                    tvDownLoad.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("xxx", "dismiss dialog");
                            loadingDialog.dismiss();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    tvDownLoad.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("xxx", "dismiss dialog");
                            loadingDialog.dismiss();
                        }
                    });
                }
            }
        };

    }

    private void muxingAudioAndVideo() throws IOException {
        Log.d("xxx", "muxing Audio And Video");

        String outputVideoPath = "/mnt/sdcard/test/";
        //api>=18
        File file = new File(outputVideoPath);
        if(!file.exists()){
            file.mkdirs();
        }

        outputVideoPath += "test.mp4";
        MediaMuxer mediaMuxer = new MediaMuxer(outputVideoPath,
                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        //api >= 16
        // 读取视频的MediaExtractor
        MediaExtractor videoExtractor = new MediaExtractor();
        //设置url或者路径后，会获取与该视频相关的参数,生成MediaFormat
        videoExtractor.setDataSource(videoUrl);

        MediaExtractor audioExtractor = new MediaExtractor();
        audioExtractor.setDataSource(videoUrl);

        int videoTrackIndex = -1;
        int audioTrackIndex = -1;
        //由于使用的是同一个源文件，所以在一个循环里查找对应的
        for (int i = 0; i < videoExtractor.getTrackCount(); i++) {
            MediaFormat format = videoExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);

            if (mime.startsWith("video/")) {
                videoTrackIndex = mediaMuxer.addTrack(format);
                videoExtractor.selectTrack(videoTrackIndex);
            }else if(mime.startsWith("audio/")){
                audioTrackIndex = mediaMuxer.addTrack(format);
                audioExtractor.selectTrack(audioTrackIndex);
            }
        }


        mediaMuxer.start();
        // 封装视频track

        if (-1 != videoTrackIndex) {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            info.presentationTimeUs = 0;
            ByteBuffer buffer = ByteBuffer.allocate(500 * 1024);
            while (true) {
                int sampleSize = videoExtractor.readSampleData(buffer, 0);
                if (sampleSize < 0) {
                    break;
                }

                info.offset = 0;
                info.size = sampleSize;
                //是否为同步帧/关键帧
                info.flags = videoExtractor.getSampleFlags();
                //时间戳
                //单位微秒，不是毫秒
                info.presentationTimeUs = videoExtractor.getSampleTime();
                mediaMuxer.writeSampleData(videoTrackIndex, buffer, info);
                videoExtractor.advance();
            }
        }

        // 封装音频track
        if (-1 != audioTrackIndex) {
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            info.presentationTimeUs = 0;
            ByteBuffer buffer = ByteBuffer.allocate(500 * 1024);
            while (true) {
                int sampleSize = audioExtractor.readSampleData(buffer, 0);
                if (sampleSize < 0) {
                    break;
                }

                info.offset = 0;
                info.size = sampleSize;
                info.flags = audioExtractor.getSampleFlags();
//                info.presentationTimeUs += 1000*1000/framerateAudo;
                info.presentationTimeUs = audioExtractor.getSampleTime();

                mediaMuxer.writeSampleData(audioTrackIndex, buffer, info);
                audioExtractor.advance();
            }
        }

        // 释放MediaExtractor
        videoExtractor.release();
        audioExtractor.release();

        // 释放MediaMuxer
        mediaMuxer.stop();
        mediaMuxer.release();
    }
}
