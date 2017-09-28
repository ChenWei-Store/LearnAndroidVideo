package com.learnvideo.playaudio.model;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

/**
 * Created by Chenwei on 2017/9/12.
 */

public class PlayParams {
    private int streamType;  //音乐播放类型，由AudioManager中常量指定
    private int sampleRateInHz;//采样率，每秒音频的采样数量,建议不超过48kHz
    private int channelConfig; //声道类型，eg:单声道，双声道，立体声等，通过AudioFormat类的静态常量指定. 注意和录音参数中指定的不是一个
    private int audioFormat; ////采样精度,每次采样需要的比特数,一般用16bit， 通过AudioFormat类的静态常量指定
    private int bufferSizeInBytes; //音频缓冲区大小，不能小于AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)

    private PlayParams(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes) {
        this.streamType = streamType;
        this.sampleRateInHz = sampleRateInHz;
        this.channelConfig = channelConfig;
        this.audioFormat = audioFormat;
        this.bufferSizeInBytes = bufferSizeInBytes;
    }

    public int getStreamType() {
        return streamType;
    }

    public void setStreamType(int streamType) {
        this.streamType = streamType;
    }

    public int getSampleRateInHz() {
        return sampleRateInHz;
    }

    public void setSampleRateInHz(int sampleRateInHz) {
        this.sampleRateInHz = sampleRateInHz;
    }

    public int getChannelConfig() {
        return channelConfig;
    }

    public void setChannelConfig(int channelConfig) {
        this.channelConfig = channelConfig;
    }

    public int getAudioFormat() {
        return audioFormat;
    }

    public int getBufferSizeInBytes() {
        return bufferSizeInBytes;
    }

    public void setBufferSizeInBytes(int bufferSizeInBytes) {
        this.bufferSizeInBytes = bufferSizeInBytes;
    }

    public static class Builder {
        private int streamType;  //音乐播放类型，由AudioManager中常量指定
        private int sampleRateInHz;//采样率，每秒音频的采样数量,建议不超过48kHz
        private int channelConfig; //声道类型，eg:单声道，双声道，立体声等，通过AudioFormat类的静态常量指定. 注意和录音参数中指定的不是一个
        private int audioFormat; ////采样精度,每次采样需要的比特数,一般用16bit， 通过AudioFormat类的静态常量指定
        private int bufferSizeInBytes; //音频缓冲区大小，不能小于AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)

        public Builder(){
            streamType = -1;
            channelConfig = -1;
            sampleRateInHz = 0;
            channelConfig = -1;
        }

        public Builder setStreamType(int streamType){
            this.streamType = streamType;
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
        public PlayParams build(){
            if(streamType == -1){
                streamType = AudioManager.USE_DEFAULT_STREAM_TYPE;
            }
            if(sampleRateInHz == 0){
                sampleRateInHz = 45000;
            }

            if(channelConfig == -1){
                channelConfig = AudioFormat.CHANNEL_OUT_MONO;
            }

            audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            bufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig,
                    audioFormat) * 2;
            return new PlayParams(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
        }


    }
}
