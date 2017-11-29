package com.learnvideo.androidcodecdemo;

import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DecodeLocalVideoActivity extends AppCompatActivity {
    private TextView tvDecodeVideo;
    private HandlerThread handlerThread;
    private Handler threadHandler;

    private MediaCodec mediaCodec;
    private MediaExtractor mediaExtractor;

    private AssetFileDescriptor testFd;
    private static final String TAG = "decodeVideo";
    private static final int MOVIE_FRAME_RATE = 24;
    private static final int ONE_FRAME_MS = 1000 / MOVIE_FRAME_RATE;

    private TextureView textureView;
    private TextView tvStatus;
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
                decodeVideo();
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
                    mediaCodec.configure(mediaFormat, new Surface(textureView.getSurfaceTexture()), null, 0);

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

        while(true) {
            int inputBufferIndex = -1;

            //获取当前可以使用的输入缓冲区坐标
            try {
                inputBufferIndex = mediaCodec.dequeueInputBuffer(10000);
            }catch (IllegalStateException e){
                //由于在onPause方法中将mediacodec状态改变，
                // 不在Executing状态了，所以该处发生异常，捕获防止闪退
                return;
            }
            boolean isdExtractorDataFinish = false; //当前是否解封装完数据

            //获取解封装的数据并传入codec的输入缓冲流
            if (inputBufferIndex >= 0 && !isdExtractorDataFinish) {
                //当前有可用的缓冲数据
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                int readSampleDataSize = mediaExtractor.readSampleData(inputBuffer, 0);
                if (readSampleDataSize >= 0) {
                    //解封装未完成，将数据传入输入缓冲流
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, readSampleDataSize, mediaExtractor.getSampleTime(), 0);
                    mediaExtractor.advance();
                } else {
                    //当前已完成解封装，传递结束标识符
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    isdExtractorDataFinish = true;
                }
                Log.d(TAG, "inputBufferIndex: " + inputBufferIndex);
            }

            //获取codec输出缓冲流中数据，并显示在界面上


            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = -1;
            try {
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 15000);
            }catch (IllegalStateException e){
                //由于在onPause方法中将mediacodec状态改变，
                // 不在Executing状态了，所以该处发生异常，捕获防止闪退
                return;
            }

            if (outputBufferIndex >= 0) {
                ByteBuffer byteBuffer = outputBuffers[outputBufferIndex];
                mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
            }
            Log.d(TAG, "outputBufferIndex: " + outputBufferIndex);

            //输出缓冲区收到，跳出循环
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                break;
            }

            try {
                //每次执行后停止ONE_FRAME_MSms，防止刷新过快
                Thread.sleep(ONE_FRAME_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        releaseMediaData();

        Log.d(TAG, " end");
    }

    private void releaseMediaData(){
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
