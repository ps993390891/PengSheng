package com.peng.jni;

import android.media.AudioFormat;
import android.media.MediaRecorder;

public class AudioConfigBuilder {
    /*音频源*/
    protected int audioSource = MediaRecorder.AudioSource.MIC;
    /*采样率*/
    protected int sampleRateInHz = 8000;
    /*声道*/
    protected int channelConfig = AudioFormat.CHANNEL_IN_MONO;
//    protected int channelConfigOut = AudioFormat.CHANNEL_OUT_STEREO;
    /*编码制式和采样大小*/
    protected int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    public AudioConfig builder() {
        return new AudioConfig(this);
    }

    public AudioConfigBuilder setAudioSource(int audioSource) {
        this.audioSource = audioSource;
        return this;
    }

    public AudioConfigBuilder setSampleRateInHz(int sampleRateInHz) {
        this.sampleRateInHz = sampleRateInHz;
        return this;
    }

    public AudioConfigBuilder setChannelConfig(int channelConfig) {
        this.channelConfig = channelConfig;
        return this;
    }

    public AudioConfigBuilder setAudioFormat(int audioFormat) {
        this.audioFormat = audioFormat;
        return this;
    }
}
