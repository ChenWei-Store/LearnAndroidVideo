package com.learnvideo.androidcodecdemo;

import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DecodeLocalAudioActivity extends AppCompatActivity {
    private TextView tvDecodeAudio;
    private AssetFileDescriptor testFd;
    private MediaCodec mediaCodec;
    private MediaExtractor mediaExtractor;
    private AudioTrack audioTrack;

    private static final String TAG = "decode";

    private static final int MOVIE_FRAME_RATE = 24;
    private static final int ONE_FRAME_MS = 1000 / MOVIE_FRAME_RATE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode_local_audio);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tvDecodeAudio = (TextView)findViewById(R.id.tv_decode_audio);

        testFd = getResources().openRawResourceFd(R.raw.test);

        tvDecodeAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new DecodeAudioRunnable()).start();
            }
        });
    }




    class DecodeAudioRunnable implements Runnable{
        @Override
        public void run() {
            Log.d(TAG, "decodeVideo");
            try {
                mediaExtractor = new MediaExtractor();
                try {
                    mediaExtractor.setDataSource(testFd.getFileDescriptor(), testFd.getStartOffset(),
                            testFd.getLength());
                    //创建mediacodec
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "codec/mediaExtractor init error");
                    if (mediaExtractor != null) {
                        mediaExtractor.release();
                        mediaExtractor = null;
                    }
                    if (mediaCodec != null) {
                        mediaCodec.release();
                        mediaCodec = null;
                    }
                }

                if (mediaExtractor == null) {
                    return;
                }

                for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                    MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
                    String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
                    if (mime.startsWith("audio/")) {
                        Log.d(TAG, "mediaformat: " + mediaFormat.toString());
                        mediaExtractor.selectTrack(i);
                        //初始化mediacodec
                        try {
                            mediaCodec = MediaCodec.createDecoderByType(mime);
                            mediaCodec.configure(mediaFormat, null, null, 0);
                            //音频声道数量
                            int audioChannels = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                            //音频采样率
                            int audioSampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                            //缓冲区最大输入
                            int maxInputSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                            //最小buffer
                            final int minBufferSize = AudioTrack.getMinBufferSize(audioSampleRate,
                                    (audioChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO),
                                    AudioFormat.ENCODING_PCM_16BIT);
                            int audioInputBufSize = minBufferSize > 0 ? minBufferSize * 4 : maxInputSize;
                            if (audioInputBufSize > maxInputSize) audioInputBufSize = maxInputSize;
                            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                                    audioSampleRate,
                                    (audioChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO),
                                    AudioFormat.ENCODING_PCM_16BIT,
                                    audioInputBufSize,
                                    AudioTrack.MODE_STREAM
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("xxx", "mediaCodec create error");
                            if (mediaCodec != null) {
                                mediaCodec.release();
                                mediaCodec = null;
                            }
                        }

                    }
                }

                if (mediaCodec == null) {
                    return;
                }
                Log.d(TAG, " mediaCodec.start()");
                mediaCodec.start();
                audioTrack.play();
                //获取输入输出缓冲区
                ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
                ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();

                boolean isdExtractorDataFinish = false; //当前是否解封装完数据
                while (true) {
                    if(!isdExtractorDataFinish){
                        int inputBufferIndex = -1;

                        //获取当前可以使用的输入缓冲区坐标
                        inputBufferIndex = mediaCodec.dequeueInputBuffer(10000);

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
                    }
                    //获取codec输出缓冲流中数据，并显示在界面上


                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    int outputBufferIndex = -1;
                    outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 15000);


                    if (outputBufferIndex >= 0) {
                        ByteBuffer byteBuffer = null;
                        if (Build.VERSION.SDK_INT >= 21) {
                            byteBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
                        } else {
                            byteBuffer = outputBuffers[outputBufferIndex];
                        }
//                    ByteBuffer byteBuffer = outputBuffers[outputBufferIndex];
                        short[] shorts = new short[bufferInfo.size / 2];
                        byteBuffer.position(0);
                        byteBuffer.asShortBuffer().get(shorts);
                        audioTrack.write(shorts, 0, bufferInfo.size / 2);

                        mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                    }
                    Log.d(TAG, "outputBufferIndex: " + outputBufferIndex);

                    //输出缓冲区收到，跳出循环
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break;
                    }
                }

                Log.d(TAG, " end");
            }catch (IllegalStateException e){
            }

            releaseMediaData();

        }
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
        releaseMediaData();
        super.onPause();
    }
}
