package com.learnvideo.androidcodecdemo;

import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.nio.ByteBuffer;

public class DecodeLocalVideoActivity extends AppCompatActivity {
    private TextView tvDecodeVideo;
    private HandlerThread handlerThread;
    private Handler threadHandler;

    private MediaCodec mediaCodec;
    private MediaExtractor mediaExtractor;

    private AssetFileDescriptor testFd;
    private static final String TAG = "decodeVideo";

    private TextureView textureView;
    private TextView tvStatus;
    private boolean isFirstStart = true;
    private long startTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode_local_video);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();
        threadHandler = new Handler(handlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    decodeVideo();
                }catch (IllegalStateException e){
                    //在播放时点击back键发生异常
                    isFirstStart = true;
                    releaseMediaData();
                }
            }
        };
        textureView = (TextureView) findViewById(R.id.texture_view);
        tvStatus = (TextView)findViewById(R.id.tv_status);

        tvDecodeVideo = (TextView)findViewById(R.id.tv_decode_video);
        tvDecodeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvStatus.setText("正在播放");
                threadHandler.sendEmptyMessage(0);
            }
        });

        testFd = getResources().openRawResourceFd(R.raw.test);

        textureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick");
                tvStatus.setText("停止播放");
                releaseMediaData();
            }
        });


    }


    private void decodeVideo(){
        Log.d(TAG, "decodeVideo");
        mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(testFd.getFileDescriptor(), testFd.getStartOffset(),
                    testFd.getLength());
            //创建mediacodec
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "mediaExtractor init error");
            if(mediaExtractor != null) {
                mediaExtractor.release();
                mediaExtractor = null;
            }
            if(mediaCodec != null){
                mediaCodec.release();
                mediaCodec = null;
            }
        }

        if(mediaExtractor == null){
            return;
        }

        for(int i = 0; i < mediaExtractor.getTrackCount(); i++){
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if(mime.startsWith("video/")){
                Log.d(TAG, "mediaformat: " + mediaFormat.toString());
                mediaExtractor.selectTrack(i);
                //初始化mediacodec
                try {
                    mediaCodec = MediaCodec.createDecoderByType(mime);
                    mediaCodec.configure(mediaFormat, new Surface(textureView.getSurfaceTexture()),
                            null, 0);

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "mediaCodec create error");
                    if(mediaCodec != null){
                        mediaCodec.release();
                        mediaCodec = null;
                    }
                }
            }
        }

        if(mediaCodec == null){
            return;
        }
        Log.d(TAG, " mediaCodec.start()");
        mediaCodec.start();

        //获取输入输出缓冲区
        ByteBuffer[] outputBuffers =  mediaCodec.getOutputBuffers();
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
        boolean isInputEnd = false;

        while(true) {
            if(!isInputEnd) {
                int inputBufferIndex = -1;
                //获取当前可以使用的输入缓冲区坐标
                inputBufferIndex = mediaCodec.dequeueInputBuffer(10000);

                //获取解封装的数据并传入codec的输入缓冲流
                if (inputBufferIndex >= 0) {
                    //当前有可用的缓冲数据
                    ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                    int readSampleDataSize = mediaExtractor.readSampleData(inputBuffer, 0);
                    if (readSampleDataSize >= 0) {
                        //解封装未完成，将数据传入输入缓冲流
                        mediaCodec.queueInputBuffer(inputBufferIndex, 0, readSampleDataSize, mediaExtractor.getSampleTime(), 0);
                    }
                    boolean result = mediaExtractor.advance();
                    if(!result){
                        //循环确保END_OF_STREAM发送成功
                        while (true) {
                            inputBufferIndex = mediaCodec.dequeueInputBuffer(10000);
                            if(inputBufferIndex < 0){
                                continue;
                            }
                            mediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            isInputEnd = true;
                            break;
                        }
                    }
                    Log.d(TAG, "inputBufferIndex: " + inputBufferIndex);
                }
            }
            //获取codec输出缓冲流中数据，并显示在界面上


            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = -1;
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);

            if (outputBufferIndex >= 0) {
                ByteBuffer byteBuffer = null;
                if ( Build.VERSION.SDK_INT >= 21) {
                    byteBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
                } else {
                    byteBuffer = outputBuffers[outputBufferIndex];
                }
                if(isFirstStart){
                    //开始录制时，记录开始播放时的时间
                    startTime = System.currentTimeMillis();
                    isFirstStart = false;
                }

                long ptsTime = startTime + bufferInfo.presentationTimeUs / 1000;
                long currentTime = System.currentTimeMillis();
                long deltaTime = ptsTime - currentTime;

                if( deltaTime > 0 ) {
                    try {
                        Thread.sleep(deltaTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mediaCodec.releaseOutputBuffer(outputBufferIndex, true);

            }
            Log.d(TAG, "outputBufferIndex: " + outputBufferIndex);

            //输出缓冲区收到，跳出循环
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                break;
            }
        }
        isFirstStart = true;
        releaseMediaData();

        Log.d(TAG, " end");
    }

    private synchronized void releaseMediaData(){
        if(mediaCodec != null){
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }

        if(mediaExtractor != null){
            mediaExtractor.release();
            mediaExtractor = null;
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        releaseMediaData();

        threadHandler.removeCallbacksAndMessages(null);
        threadHandler = null;
        super.onPause();
    }


}
