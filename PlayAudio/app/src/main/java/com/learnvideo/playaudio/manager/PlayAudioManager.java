package com.learnvideo.playaudio.manager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.learnvideo.playaudio.exception.InitializeFailException;
import com.learnvideo.playaudio.exception.NoBufferDataException;
import com.learnvideo.playaudio.model.AudioParams;


/**
 * Created by Chenwei on 2017/9/4.
 * 关于Android6.0后的音频权限，不在此类中授权
 */

public class PlayAudioManager{
    private static PlayAudioManager playAudioManager;
    private AudioRecord audioRecord;

    private byte[] bytesBuffer;
    private short[] shortBuffer;
    private float[] floatBuffer;

    private int pcmSize;
    private AudioTrack audioTrack;
    private AudioParams audioParams;
    private Context appContext;
    public static final String TAG = PlayAudioManager.class.getSimpleName();
    private PlayAudioManager(Context context){
        appContext = context.getApplicationContext();
    }

    public static PlayAudioManager getInstance(Context context){
        if(playAudioManager == null){
            synchronized (PlayAudioManager.class){
                playAudioManager = new PlayAudioManager(context);
            }
        }
        return playAudioManager;
    }

    public void setAudioConfig(AudioParams audioParams){
        if(audioParams.getBufferSizeInBytes() < AudioRecord.getMinBufferSize(audioParams.getSampleRateInHz(),
                audioParams.getChannelConfig(), audioParams.getAudioFormat())){
            throw new IllegalArgumentException("bufferSizeInBytes 不能小于" +
                    "最小缓冲区大小(AudioRecord.getMinBufferSize)");
        }

        if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M &&
                audioParams.getAudioFormat() == AudioFormat.ENCODING_PCM_FLOAT){
            throw new IllegalArgumentException("ENCODING_PCM_FLOAT 采样精度只能在API 23 及以上使用");
        }

        this.audioParams = audioParams;
    }




    /**
     * 获取音频PCM数据
     * 注意，先授权后在调用该方法，该类不会处理Android6.0及以后的授权问题
     */
    public void startRecordAudio(){
        //验证是否获取权限
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(appContext,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                Log.e(TAG, "请动态获取录音的相关权限");
                Toast.makeText(appContext, "请动态获取录音的相关权限", Toast.LENGTH_LONG).show();
                return;
            }
        }
        audioRecord = new AudioRecord(audioParams.getAudioSource(), audioParams.getSampleRateInHz(),
                audioParams.getChannelConfig(), audioParams.getAudioFormat(), audioParams.getBufferSizeInBytes());

        new RecordThread().start();

    }


    public void stopRecordAudio(){
        Log.e(TAG, "stopRecordAudio");

        if(audioRecord == null){
            return;
        }
        audioRecord.stop();
    }

    public void saveWavFile(String filePath){

    }

    public void release(){
        audioRecord.release();
        audioRecord = null;
        audioTrack.release();
        audioTrack = null;
    }



    class RecordThread extends Thread{
        @Override
        public void run() {
            super.run();

            int resultCode = 0;
            if(audioParams.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT ) {
                shortBuffer = new short[audioParams.getBufferSizeInBytes() / 2];
                audioRecord.startRecording();
                resultCode = audioRecord.read(shortBuffer, 0 , shortBuffer.length);
            }else if(audioParams.getAudioFormat() == AudioFormat.ENCODING_PCM_8BIT){
                bytesBuffer = new byte[audioParams.getBufferSizeInBytes()];
                audioRecord.startRecording();
                resultCode = audioRecord.read(bytesBuffer, 0, bytesBuffer.length);
            }
            else if(audioParams.getAudioFormat() == AudioFormat.ENCODING_PCM_FLOAT){
                floatBuffer = new float[audioParams.getBufferSizeInBytes() / 4];
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    audioRecord.startRecording();
                    resultCode = audioRecord.read(floatBuffer, 0, floatBuffer.length, AudioRecord.READ_BLOCKING);
                }else{
                    Log.e(TAG, "ENCODING_PCM_FLOAT 采样精度只能在API 23 及以上使用");
                }
            }

            Log.e(TAG, "record code:" + resultCode);

            if(resultCode < 0){
                Log.e(TAG, "record error");
                Log.e(TAG, "error code: " + resultCode);
                throw new InitializeFailException("AudioRecord 初始化失败");
            }else{
                Log.e(TAG, "record success");
            }
            pcmSize = resultCode;
            stopRecordAudio();
        }
    }

    public void setBytesBuffer(byte[] bytesBuffer) {
        this.bytesBuffer = bytesBuffer;
    }

    public void setShortBuffer(short[] shortBuffer) {
        this.shortBuffer = shortBuffer;
    }

    public void setFloatBuffer(float[] floatBuffer) {
        this.floatBuffer = floatBuffer;
    }

    /**
     *
     * @param streamType
     * @param mode
     */
    public void startPlay(int streamType, int mode){
        if((shortBuffer == null || shortBuffer.length == 0) && (bytesBuffer == null ||
                bytesBuffer.length == 0) && (floatBuffer == null || floatBuffer.length == 0)){
            throw new NoBufferDataException("要播放的音频流为null");
        }

        audioTrack = new AudioTrack(streamType, audioParams.getSampleRateInHz(), audioParams.getChannelConfig(),
                audioParams.getAudioFormat(), audioParams.getBufferSizeInBytes(), mode);
        audioTrack.play();
        if(audioParams.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT ) {
          audioTrack.write(shortBuffer, 0, shortBuffer.length);
        }else if(audioParams.getAudioFormat() == AudioFormat.ENCODING_PCM_8BIT){
            audioTrack.write(shortBuffer, 0, shortBuffer.length);
        }else if(audioParams.getAudioFormat() == AudioFormat.ENCODING_PCM_FLOAT){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioRecord.startRecording();
                audioTrack.write(floatBuffer, 0, floatBuffer.length, AudioTrack.WRITE_BLOCKING);
            }else{
                Log.e(TAG, "ENCODING_PCM_FLOAT 采样精度只能在API 23 及以上使用");
            }
        }
    }


}
