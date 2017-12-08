package com.learnvideo.esdrawvideo;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Chenwei on 2017/12/6.
 */

public class DecodeMp4Manager {
    private MediaExtractor mediaExtractor;
    private MediaCodec mediaCodec;
    private String path;
    private final String TAG = DecodeMp4Manager.class.getSimpleName();

    private boolean isFirstStart = true;
    private long startTime;

    private ByteBuffer[] outputBuffers;
    private ByteBuffer[] inputBuffers;

    private DecodeVideoCallback decodeVideoCallback;

    private MediaMetadataRetriever metadata;
    private int videoWidth, videoHeight;
    private int rotation;
    private long duration;
    public DecodeMp4Manager(String path,
                            @Nullable DecodeVideoCallback decodeVideoCallback){
        this.path = path;

        this.decodeVideoCallback = decodeVideoCallback;
    }


    public void prepare(Surface surface){
        if(surface == null){
            throw new IllegalArgumentException("surface == null");
        }

        try {
            mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(path);
            int index = getTractIndex("video/");
            if(index == -1){
                Log.d(TAG, "find track index error");
                return;
            }

            mediaExtractor.selectTrack(index);

            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(index);
            int height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
            if(height != 0){
                videoHeight = height;
            }

            int width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
            if(width != 0){
                videoWidth = width;
            }

            long duration =  mediaFormat.getLong(MediaFormat.KEY_DURATION);
            if(duration != 0) {
                this.duration = duration;
            }

            mediaCodec = MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));
            mediaCodec.configure(mediaFormat, surface,
                    null, 0);
            if(decodeVideoCallback != null){
                decodeVideoCallback.onPrepared();
            }
        }catch (IOException e){
            Log.d(TAG, "IOException: error: " + e.getMessage());
            releaseMediaData();

        }
        Log.d(TAG, "prepare end");
    }



    public void start(){
        mediaCodec.start();
        //获取输入输出缓冲区
        outputBuffers =  mediaCodec.getOutputBuffers();
        inputBuffers = mediaCodec.getInputBuffers();

        new Thread(new DecodeRunnable()).start();
    }

    public void getVideoInfo(){
        metadata = new MediaMetadataRetriever();
        metadata.setDataSource(path);

        String value = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        if (!TextUtils.isEmpty(value)) {
            videoWidth = Integer.parseInt(value);
        }
        value = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        if (!TextUtils.isEmpty(value)) {
            videoHeight = Integer.parseInt(value);
        }
        value = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        if (!TextUtils.isEmpty(value)) {
            rotation = Integer.parseInt(value);
        }

        value = metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        if (!TextUtils.isEmpty(value)) {
            duration = Long.parseLong(value) * 1000;
        }

        metadata.release();
        if(decodeVideoCallback != null){
            decodeVideoCallback.onGotVideoInfo();
        }
    }

    private void doDecode(){
        try {
            while(true) {
                dealWithInputBuffer();
                boolean result = dealWithOutputBuffer();
                if(result){
                    break;
                }
            }
            isFirstStart = true;
            releaseMediaData();

            if(decodeVideoCallback != null){
                decodeVideoCallback.onFinished();
            }
        }catch (IllegalStateException e){
            //在播放时点击back键发生异常
            releaseMediaData();
            isFirstStart = true;
        }

    }

    private void dealWithInputBuffer(){
        boolean isInputEnd = false;
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

    }

    /**
     *
     * @return 是否处理到BUFFER_FLAG_END_OF_STREAM, true:是
     */
    private boolean dealWithOutputBuffer(){
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
            return true;
        }
        return false;
    }


    private int getTractIndex(String mine) {
        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);

            if (mime.startsWith(mine)) {
                Log.d(TAG, "mediaformat: " + mediaFormat.toString());
                return i;
            }
        }
        return -1;
    }

    public synchronized void releaseMediaData(){
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

    public interface DecodeVideoCallback{
        void onGotVideoInfo();
        void onPrepared();
        void onFinished();
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public int getRotation() {
        return rotation;
    }


    class DecodeRunnable implements Runnable{
        @Override
        public void run() {
            doDecode();
        }
    }

}
