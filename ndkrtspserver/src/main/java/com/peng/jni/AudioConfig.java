package com.peng.jni;

public class AudioConfig {
    /*音频源*/
    private final int audioSource;
    /*采样率*/
    private final int sampleRateInHz;
    /*声道*/
    private final int channelConfig;
    //    private final int channelConfigOut;
    /*编码制式和采样大小*/
    private final int audioFormat;

    public AudioConfig(AudioConfigBuilder builder) {
        this.audioSource = builder.audioSource;
        this.sampleRateInHz = builder.sampleRateInHz;
        this.channelConfig = builder.channelConfig;
//        this.channelConfigOut = builder.channelConfigOut;
        this.audioFormat = builder.audioFormat;
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

//    public int getChannelConfigOut() {
//        return channelConfigOut;
//    }

    public int getAudioFormat() {
        return audioFormat;
    }
}
