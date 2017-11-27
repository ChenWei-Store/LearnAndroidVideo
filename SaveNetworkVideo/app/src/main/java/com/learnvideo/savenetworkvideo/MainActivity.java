package com.learnvideo.savenetworkvideo;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
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
    private Handler uiHandler;
    private TextView tvMuxerVideo;
    private TextView tvExtractAudio;
    private TextView tvExtractVideo;
    private LoadingDialog loadingDialog;
    private static final int MUXER_VIDEO = 0;
    private static final int EXTRACT_AUDIO = 1;
    private static final int EXTRACT_VIDEO = 2;
    private static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadingDialog = new LoadingDialog(this);

        tvMuxerVideo = (TextView)findViewById(R.id.tv_muxer_video);
        tvExtractAudio = (TextView)findViewById(R.id.tv_extract_audio);
        tvExtractVideo = (TextView)findViewById(R.id.tv_extract_video);

        tvMuxerVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNetworkVideoHandler.sendEmptyMessage(MUXER_VIDEO);
            }
        });

        tvExtractAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNetworkVideoHandler.sendEmptyMessage(EXTRACT_AUDIO);
            }
        });

        tvExtractVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNetworkVideoHandler.sendEmptyMessage(EXTRACT_VIDEO);

            }
        });

        uiHandler = new Handler();
        HandlerThread handlerThread = new HandlerThread("SaveNetworkVideo");
        handlerThread.start();
        saveNetworkVideoHandler = new Handler(handlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int type = msg.what;
                showDialog();
                if (type == MUXER_VIDEO) {
                    muxingAudioAndVideo();
                }else if(type == EXTRACT_AUDIO){
                    extractAudio();
                }else if(type == EXTRACT_VIDEO){
                    extractVideo();
                }else {
                    throw new IllegalArgumentException("msg.what has a error value");
                }

                dismissDialog();
            }
        };
    }



    private void showDialog(){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                loadingDialog.show();
            }
        });
    }

    private void dismissDialog(){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "dismiss dialog");
                loadingDialog.dismiss();
            }
        });
    }

    private void extractAudio(){
        Log.d(TAG, "extractAudio");
        String outputAudioPath = "/mnt/sdcard/test/";
        File file = new File(outputAudioPath);
        if(!file.exists()){
            file.mkdirs();
        }

        outputAudioPath += "testAudio";
        //api>=18
        MediaMuxer mediaMuxer = null;
        MediaExtractor audioExtractor = null;
        try {
            mediaMuxer = new MediaMuxer(outputAudioPath,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(videoUrl);
        } catch (IOException e) {
            Log.e(TAG, "初始化失败,原因:" + e.getMessage());
            if(mediaMuxer != null){
                mediaMuxer.release();
                mediaMuxer = null;
            }

            if(audioExtractor != null){
                audioExtractor.release();
                audioExtractor = null;
            }
        }

        if(mediaMuxer == null){
            return;
        }

        int trackIndex = -1;
        //由于使用的是同一个源文件，所以在一个循环里查找对应的
        for (int i = 0; i < audioExtractor.getTrackCount(); i++) {
            MediaFormat format = audioExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            Log.d(TAG, "mediaFormat: " + format.toString());
           if(mime.startsWith("audio/")){
               trackIndex = mediaMuxer.addTrack(format);
                audioExtractor.selectTrack(i);
            }
        }

        mediaMuxer.start();
        Log.d(TAG, "start muxer audio");
        // 封装音频track
        if (-1 != trackIndex) {
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

                mediaMuxer.writeSampleData(trackIndex, buffer, info);
                audioExtractor.advance();
            }
        }

        // 释放MediaExtractor
        audioExtractor.release();

        // 释放MediaMuxer
        mediaMuxer.stop();
        mediaMuxer.release();
        Log.d(TAG, "muxer end ");
    }

    private void extractVideo(){
        Log.d(TAG, "extractvideo");

        String outputVideoPath = "/mnt/sdcard/test/";
        File file = new File(outputVideoPath);
        if(!file.exists()){
            file.mkdirs();
        }

        outputVideoPath += "testVideo";
        //api>=18
        MediaMuxer mediaMuxer = null;
        MediaExtractor videoExtractor = null;

        try {
            mediaMuxer = new MediaMuxer(outputVideoPath,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            //api >= 16
            // 读取视频的MediaExtractor
            videoExtractor = new MediaExtractor();
            //设置url或者路径后，会获取与该视频相关的参数,生成MediaFormat
            videoExtractor.setDataSource(videoUrl);
        } catch (IOException e) {
            Log.e(TAG, "初始化失败,原因:" + e.getMessage());
            if(mediaMuxer != null){
                mediaMuxer.release();
                mediaMuxer = null;
            }
            if(videoExtractor != null){
                videoExtractor.release();
                videoExtractor = null;
            }
        }

        if(mediaMuxer == null){
            return;
        }

        int trackIndex = -1;

        //由于使用的是同一个源文件，所以在一个循环里查找对应的
        for (int i = 0; i < videoExtractor.getTrackCount(); i++) {
            MediaFormat format = videoExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            Log.d(TAG, "mediaFormat: " + format.toString());
            if (mime.startsWith("video/")) {
                trackIndex = mediaMuxer.addTrack(format);
                videoExtractor.selectTrack(i);
            }
        }


        mediaMuxer.start();
        // 封装视频track

        Log.d(TAG, "start muxer video");
        if (-1 != trackIndex) {
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
                mediaMuxer.writeSampleData(trackIndex, buffer, info);
                videoExtractor.advance();
            }
        }

        Log.d(TAG, "start muxer audio");

        // 释放MediaExtractor
        videoExtractor.release();

        // 释放MediaMuxer
        mediaMuxer.stop();
        mediaMuxer.release();
        Log.d(TAG, "muxer end ");
    }


    private void muxingAudioAndVideo() {
        Log.d(TAG, "muxing Audio And Video");

        String outputVideoPath = "/mnt/sdcard/test/";
        File file = new File(outputVideoPath);
        if(!file.exists()){
            file.mkdirs();
        }

        outputVideoPath += "test";
        //api>=18
        MediaMuxer mediaMuxer = null;
        MediaExtractor videoExtractor = null;
        MediaExtractor audioExtractor = null;

        try {
            mediaMuxer = new MediaMuxer(outputVideoPath,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            //api >= 16
            // 读取视频的MediaExtractor
            videoExtractor = new MediaExtractor();
            //设置url或者路径后，会获取与该视频相关的参数,生成MediaFormat
            videoExtractor.setDataSource(videoUrl);

            audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(videoUrl);
        } catch (IOException e) {
            Log.e(TAG, "初始化失败,原因:" + e.getMessage());
            if(mediaMuxer != null){
                mediaMuxer.release();
                mediaMuxer = null;
            }
            if(videoExtractor != null){
                videoExtractor.release();
                videoExtractor = null;
            }
            if(audioExtractor != null){
                audioExtractor.release();
                audioExtractor = null;
            }
        }

        if(mediaMuxer == null){
            return;
        }

        int videoTrackIndex = -1;
        int audioTrackIndex = -1;
        //由于使用的是同一个源文件，所以在一个循环里查找对应的
        for (int i = 0; i < videoExtractor.getTrackCount(); i++) {
            MediaFormat format = videoExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            Log.d(TAG, "mediaFormat: " + format.toString());
            if (mime.startsWith("video/")) {
                videoTrackIndex = mediaMuxer.addTrack(format);
                videoExtractor.selectTrack(i);
            }else if(mime.startsWith("audio/")){
                audioTrackIndex = mediaMuxer.addTrack(format);
                audioExtractor.selectTrack(i);
            }
        }


        mediaMuxer.start();
        // 封装视频track

        Log.d(TAG, "start muxer video");
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

        Log.d(TAG, "start muxer audio");
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
        Log.d(TAG, "muxer end ");

    }
}
