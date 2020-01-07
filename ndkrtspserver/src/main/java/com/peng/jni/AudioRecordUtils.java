package com.peng.jni;

import android.media.AudioRecord;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class AudioRecordUtils {

    private static final String TAG = "AudioRecordUtils";
    private AudioRecord mAudioRecord;
    /*采集数据需要的缓冲区的大小*/
    private int bufferSizeInBytes;
    /*录制标识*/
    private boolean isRecording = false;

    private FileOutputStream fileOutputStream;
//    private AACEncoder aacEncoder;

    /**
     * 初始化录音
     */
    private void initAudioRecord() {
        if (mAudioRecord != null) {
            return;
        }
        AudioConfig audioConfig = new AudioConfigBuilder().builder();
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(audioConfig.getSampleRateInHz(),
                audioConfig.getChannelConfig(), audioConfig.getAudioFormat());
        ELog.e(TAG, "bufferSizeInBytes:" + bufferSizeInBytes);
        mAudioRecord = new AudioRecord(audioConfig.getAudioSource(), audioConfig.getSampleRateInHz(), audioConfig.getChannelConfig(),
                audioConfig.getAudioFormat(), bufferSizeInBytes);
//        aacEncoder = new AACEncoder();
    }

    /*初始化文件*/
    private void initFile() {
        try {
            fileOutputStream = new FileOutputStream(new File("/sdcard/test.g711"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始录音
     */
    public void startRecord(final RtspServerJni jni) {
        if(isRecording){
            return;
        }
        initAudioRecord();
//        aacEncoder.initAudioEncoder(jni);
        isRecording = true;
//        initFile();
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                ELog.e(TAG, "startRecord");
                short[] iputPcm = new short[bufferSizeInBytes];
                byte[] out = new byte[bufferSizeInBytes];
//                byte[] bufPcm = new byte[bufferSizeInBytes];
                if(!isRecording){
                    return;
                }
                mAudioRecord.startRecording();
                ELog.e(TAG, "startRecording:"+isRecording );
                try {
                    while (isRecording) {
                        //从MIC保存数据到缓冲区
                        int end = mAudioRecord.read(iputPcm, 0, bufferSizeInBytes);
//                        int end = mAudioRecord.read(bufPcm, 0, bufferSizeInBytes);
//                        aacEncoder.encodeData(bufPcm);
                        G711Code.G711aEncoder(iputPcm, out, end);
                        jni.sendAudio(out, end);
//                        ELog.e(TAG,"---->>sendAudio");
//                        fileOutputStream.write(out, 0, end);
//                        fileOutputStream.flush();

                    }
                    ELog.e(TAG, "audio stop");
//                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    ELog.e(TAG, "audio record Exception");
                }
            }
        });
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        ELog.e(TAG, "stopRecord()");
        isRecording = false;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mAudioRecord != null) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
        ELog.e(TAG, "release");
    }
}
