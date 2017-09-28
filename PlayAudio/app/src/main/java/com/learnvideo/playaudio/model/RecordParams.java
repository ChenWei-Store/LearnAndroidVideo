package com.learnvideo.playaudio.model;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;


/**
 * Created by Chenwei on 2017/9/5.
 */

public class RecordParams {
    private int audioSource; //音频物理源， eg：麦克风等 从MediaRecorder.AudioSource类的静态常量指定
    private int sampleRateInHz; //采样率，每秒音频的采样数量,建议不超过48kHz
    private int channelConfig; //声道类型，eg:单声道，双声道，立体声等，通过AudioFormat类的静态常量指定
    private int audioFormat; //采样精度,每次采样需要的比特数,一般用16bit， 通过AudioFormat类的静态常量指定
    private int bufferSizeInBytes; //音频缓冲区大小，不能小于AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
    private RecordParams(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat,
                         int bufferSizeInBytes) {
        this.audioSource = audioSource;
        this.sampleRateInHz = sampleRateInHz;
        this.channelConfig = channelConfig;
        this.audioFormat = audioFormat;
        this.bufferSizeInBytes = bufferSizeInBytes;
    }

    public int getAudioSource() {
        return audioSource;
    }

    public int getSampleRateInHz() {
        return sampleRateInHz;
    }

    public int getChannelConfig() {
        return channelConfig;
    }

    public int getAudioFormat() {
        return audioFormat;
    }

    public int getBufferSizeInBytes() {
        return bufferSizeInBytes;
    }

    public static class Builder {
        private int audioSource;
        private int sampleRateInHz;
        private int channelConfig;
        private int audioFormat;
        private int bufferSizeInBytes;
        public Builder() {
            audioSource = -1;
            sampleRateInHz = 0;
            channelConfig = -1;
            audioFormat = -1;
        }

        public Builder setAudioSource(int audioSource){
            this.audioSource = audioSource;
            return this;
        }

        public Builder setSampleRateInHz(int sampleRateInHz){
            this.sampleRateInHz = sampleRateInHz;
            return this;
        }

        public Builder setChannelConfig(int channelConfig){
            this.channelConfig = channelConfig;
            return this;
        }

        public RecordParams build(){
            if(audioSource == 0){
                audioSource = MediaRecorder.AudioSource.DEFAULT;
            }
            if(sampleRateInHz == 0){
                sampleRateInHz = 45000;
            }

            if(channelConfig == -1){
                channelConfig = AudioFormat.CHANNEL_IN_MONO;
            }

            audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig,
                    audioFormat) * 2;

            return new RecordParams(audioSource, sampleRateInHz, channelConfig, audioFormat,
                    bufferSizeInBytes);
        }
    }
}
