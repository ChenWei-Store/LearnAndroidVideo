package com.learnvideo.playaudio.model;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;


/**
 * Created by Chenwei on 2017/9/5.
 */

public class AudioParams {
    private int audioSource; //音频物理源， eg：麦克风等 从MediaRecorder.AudioSource类的静态常量指定
    private int sampleRateInHz; //采样率，每秒音频的采样数量,建议不超过48kHz
    private int channelConfig; //声道类型，eg:单声道，双声道，立体声等，通过AudioFormat类的静态常量指定
    private int audioFormat; //采样精度,每次采样需要的比特数,一般用16bit， 通过AudioFormat类的静态常量指定
    private int bufferSizeInBytes; //音频缓冲区大小，不能小于AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
    private int seconds;
    private AudioParams(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat,
                        int bufferSizeInBytes, int seconds) {
        this.audioSource = audioSource;
        this.sampleRateInHz = sampleRateInHz;
        this.channelConfig = channelConfig;
        this.audioFormat = audioFormat;
        this.bufferSizeInBytes = bufferSizeInBytes;
        this.seconds = seconds;
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
        private int seconds;
        public Builder() {
            audioSource = -1;
            sampleRateInHz = 0;
            channelConfig = -1;
            audioFormat = -1;
            bufferSizeInBytes = 0;
        }

        /**
         * 这个方法最好最后在调用
         * @param seconds
         * @return
         */
        public Builder setSeconds(int seconds) {
            this.seconds = seconds;
            //每秒音频的采样数量 * 声道数量 * 每次采样需要的字节数 *  音频播放时间(s)
            bufferSizeInBytes = sampleRateInHz * getChannelCount(channelConfig)
                    * getPCMPerSample(audioFormat) * seconds;
            return this;
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

        public Builder setAudioFormat(int audioFormat){
            this.audioFormat = audioFormat;
            return this;
        }

        public AudioParams build(){
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
            return new AudioParams(audioSource, sampleRateInHz, channelConfig, audioFormat,
                    bufferSizeInBytes, seconds);
        }

        /**
         * 根据具体的声道配置获取声道数量
         *
         * @param channelConfig
         * @return
         */
        private int getChannelCount(int channelConfig) {
            int channelCount = 0;
            switch (channelConfig) {
                case AudioFormat.CHANNEL_IN_DEFAULT: // AudioFormat.CHANNEL_CONFIGURATION_DEFAULT
                case AudioFormat.CHANNEL_IN_MONO:
                case AudioFormat.CHANNEL_CONFIGURATION_MONO:
                    channelCount = 1;
                    break;
                case AudioFormat.CHANNEL_IN_STEREO:
                case AudioFormat.CHANNEL_CONFIGURATION_STEREO:
                case (AudioFormat.CHANNEL_IN_FRONT | AudioFormat.CHANNEL_IN_BACK):
                    channelCount = 2;
                    break;
                case AudioFormat.CHANNEL_INVALID:
                default:
                    throw new IllegalArgumentException("channelConfig param is error, can not find channel count");
            }
            return channelCount;
        }

        /**
         * 根据具体的音频配置获取每次采样需要的字节数
         *
         * @param audioFormat
         * @return
         */
        private int getPCMPerSample(int audioFormat) {
            int byteCount = 0;
            switch (audioFormat) {
                case AudioFormat.ENCODING_PCM_8BIT:
                    byteCount = 1;
                    break;
                case AudioFormat.ENCODING_PCM_16BIT:
                    byteCount = 2;
                    break;
                case AudioFormat.ENCODING_PCM_FLOAT:
                    byteCount = 4;
                    break;
                default:
                    throw new IllegalArgumentException("audioFormat param is error, can not find channel PCM per sample");
            }
            return byteCount;
        }
    }


}
