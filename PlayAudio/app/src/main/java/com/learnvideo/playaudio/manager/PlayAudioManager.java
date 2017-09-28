package com.learnvideo.playaudio.manager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.learnvideo.playaudio.Utils;
import com.learnvideo.playaudio.exception.InitializeFailException;
import com.learnvideo.playaudio.model.PlayParams;
import com.learnvideo.playaudio.model.RecordParams;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Chenwei on 2017/9/4.
 * 关于Android6.0后的音频权限，不在此类中授权
 */

public class PlayAudioManager{
    private static PlayAudioManager playAudioManager;
    private AudioRecord audioRecord;

    private short[] shortBuffer;

    private AudioTrack audioTrack;
    private RecordParams audioParams;
    private PlayParams playParams;
    private Context appContext;
    private String pcmFileName;
    private RecordThread recordThread;
    private PlayThread playThread;
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

    public void setAudioConfig(RecordParams audioParams){
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

    public void setPlayConfig(PlayParams playParams){
        this.playParams = playParams;
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
        recordThread = new RecordThread();
        recordThread.setRecord(true);
        recordThread.start();
    }

    /**
     * 停止录音
     */
    public void stopRecordAudio(){
        Log.e(TAG, "stopRecordAudio");
        if(recordThread != null) {
            recordThread.setRecord(false);
            recordThread = null;
        }
        if(audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }

    }

    public void saveWavFile(String filePath){

    }

    public void release(){
        stopRecordAudio();
        stopPlay();
    }



    /**
     *
     * @param mode 仅支持Android 8.0以下版本，Android 8.0中该构造方法已被废弃
     */
    public void startPlay(int mode){
        audioTrack = new AudioTrack(playParams.getStreamType(), playParams.getSampleRateInHz(), playParams.getChannelConfig(),
                playParams.getAudioFormat(), playParams.getBufferSizeInBytes(), mode);
        playThread = new PlayThread();
        playThread.setIsPlay(true);
        playThread.start();
    }

    public void stopPlay(){
        Log.d(TAG, "stopPlay");
        if(playThread != null) {
            playThread.setIsPlay(false);
            playThread = null;
        }
        if(audioTrack != null) {
            audioTrack.flush();
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }

    }

    class RecordThread extends Thread {
        private boolean isRecord;

        @Override
        public void run() {
            shortBuffer = new short[audioParams.getBufferSizeInBytes() / 2];
            audioRecord.startRecording();

            writePcmToFile();
        }

        public void setRecord(boolean isRecord){
            this.isRecord = isRecord;
        }

        /**
         * 读取pcm数据并保存在文件中
         */
        private void writePcmToFile() {
            pcmFileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            FileOutputStream fos = null;
            int resultCode = 0;
            try {
                fos = appContext.openFileOutput(pcmFileName, Context.MODE_PRIVATE);
                while (isRecord) {
                    resultCode = audioRecord.read(shortBuffer, 0, shortBuffer.length);
                    byte[] bytes = Utils.short2Byte(shortBuffer);
                    fos.write(bytes);
                    Log.d(TAG, "resultCode: " + resultCode);
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "创建pcm文件失败");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "pcm文件写入失败");
                e.printStackTrace();
            }finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    class PlayThread extends Thread{
        private boolean isPlay;
        @Override
        public void run() {
            super.run();
            FileInputStream fis = null;
            byte []bytes = null;
            try {
                fis = appContext.openFileInput(pcmFileName);
                bytes = new byte[fis.available()];
                fis.read(bytes);
                short []data = Utils.byte2Short(bytes);
                int temp = playParams.getBufferSizeInBytes() / 2;
                int offsetInShorts = 0;  //每次播放pcm数据的偏移量
                int sizeInShorts = temp; //把该数据作为一个单元
                audioTrack.play();
                while (isPlay) {
                    if(audioTrack != null) {
                        audioTrack.write(data, offsetInShorts, sizeInShorts);
                    }
                    offsetInShorts += sizeInShorts;
                    if(offsetInShorts >= data.length){
                        stopPlay();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                try {
                    fis.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setIsPlay(boolean isPlay){
            this.isPlay = isPlay;
        }

    }


}
