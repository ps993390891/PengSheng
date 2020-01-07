package com.peng.jni;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingDeque;

public class H264Encoder {

    private static final String TAG = "H264Encoder";
    private MediaCodec mMediaCodec;
    private MediaCodec.BufferInfo bufferInfo;
    private final String VCODEC = "video/avc";
    private final int FRAME = 1;
    private LinkedBlockingDeque<byte[]> queue;
    private int bitrate = 1024 * 500;//码率
    private boolean isStop = true;
    private int videoWidth;
    private int videoHeight;
    private int bufferSize;
    private byte[] yuv420 = null;
    private byte[] ppsSpsData = null;
    //编码等待超时时间 微秒
    private static final int TIMEOUT_USEC = 10000;
    //帧率
    private int videoFrameRate = 15;
    private boolean isEnStop = true;
    private RtspServerJni jni;

    public void startMediaCode(int videoWidth, int videoHeight, RtspServerJni jni) {
        this.jni = jni;
        ELog.i(TAG, "startMediaCode videoWidth: " + videoWidth + " videoHeight: " + videoHeight);
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        queue = new LinkedBlockingDeque<>(50);

        bufferSize = videoWidth * videoHeight * 3 / 2;
        try {
            mMediaCodec = MediaCodec.createEncoderByType(VCODEC);
            bufferInfo = new MediaCodec.BufferInfo();
            MediaFormat format = MediaFormat.createVideoFormat(VCODEC, videoWidth, videoHeight);
            //码率模式
            //BITRATE_MODE_CQ: 表示完全不控制码率，尽最大可能保证图像质量
            //BITRATE_MODE_CBR: 表示编码器会尽量把输出码率控制为设定值
            //BITRATE_MODE_VBR: 表示编码器会根据图像内容的复杂度（实际上是帧间变化量的大小）来动态调整输出码率，图像复杂则码率高，图像简单则码率低；
//            format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
            //描述平均位速率（以位/秒为单位）的键。 关联的值是一个整数
            format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
            //描述视频格式的帧速率（以帧/秒为单位）的键。
            format.setInteger(MediaFormat.KEY_FRAME_RATE, videoFrameRate);
            //色彩格式
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
            //关键帧间隔时间，单位是秒
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, FRAME);
            mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaCodec.start();
            startEnCode();
        } catch (Exception e) {
            e.printStackTrace();
            ELog.i(TAG, "startMediaCode IOException");
        }

    }

    public void setVideoFrameRate(int videoFrameRate) {
        this.videoFrameRate = videoFrameRate;
    }

    public void offVieoEncoder(byte[] yv12) {
        if(queue == null){
            ELog.e(TAG, "queue null");
            return;
        }
        if (!queue.offer(yv12)) {
            ELog.i(TAG, "queue is full!");
            queue.clear();
            queue.offer(yv12);
        }
    }

    /**
     * 开始编码
     */
    private void startEnCode() {
        isStop = false;
        isEnStop = false;
//        createfile();
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                ELog.e(TAG,"startEnCode");
                while (!isStop) {
                    try {
                        yuv420 = new byte[bufferSize];
                        YV12toI420(queue.take(), yuv420, videoWidth, videoHeight);
                        ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
                        //<0表示一直等待，不丢帧。>0表示等待时间，微秒级. 0表示立即返回
                        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
                        if (inputBufferIndex >= 0) {
                            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                            inputBuffer.clear();
                            inputBuffer.put(yuv420);
                            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, yuv420.length, System.currentTimeMillis(), 0);
                        } else {
                            ELog.e(TAG, "dequeueInputBuffer fail. inputBufferIndex:" + inputBufferIndex);
                        }
                        drainLoop();
                    } catch (Exception e) {
                        e.printStackTrace();
                        ELog.i(TAG, "EnCode Exception");
                    }
                }
                isEnStop = true;
            }
        });
    }

    /**
     * 取出编码后的数据
     */
    private void drainLoop() {
        try {
            ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
            int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
            if (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);
                if (ppsSpsData != null) {
                    //最终的h264数据
                    byte[] output;
                    if (outData[4] == 0x65) {
                        output = new byte[outData.length + ppsSpsData.length];
                    } else {
                        output = new byte[outData.length];
                    }
                    System.arraycopy(outData, 0, output, 0, outData.length);

                    //如果不添加pps和sps，直接保存成h264文件是无法播放的。
                    if (output[4] == 0x65) {     // key frame 编码器生成关键帧时只有 00 00 00 01 65 没有pps sps， 要加上
                        System.arraycopy(output, 0, yuv420, 0, outData.length);
                        System.arraycopy(ppsSpsData, 0, output, 0, ppsSpsData.length);
                        System.arraycopy(yuv420, 0, output, ppsSpsData.length, outData.length);
                    }
                    try {
//                        outputStream.write(output, 0, output.length);
                        jni.sendVideo(output, output.length);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ELog.e(TAG,"---->>sendVideo Exception");
                    }
                } else {  //保存pps sps 只有开始时 第一个帧里有， 保存起来后面用
                    ByteBuffer spsPpsBuffer = ByteBuffer.wrap(outData);
                    if (spsPpsBuffer.getInt() == 0x00000001) {
                        //长度为84
                        ppsSpsData = new byte[outData.length];
                        System.arraycopy(outData, 0, ppsSpsData, 0, outData.length);
                    } else {
                        ELog.i(TAG, "spsPpsBuffer.getInt() != 0x00000001");
                        return;
                    }
                }
                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            } else {
                ELog.e(TAG, "dequeueOutputBuffer fail. outputBufferIndex:" + outputBufferIndex);
            }


        } catch (Exception t) {
            t.printStackTrace();
            ELog.i(TAG, "drainLoop Exception");
        }

    }


    private BufferedOutputStream outputStream;

    private void createfile() {
        File file = new File("mnt/sdcard/test.h264");
        if (file.exists()) {
            file.delete();
        }
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止视频编码
     */
    public void stopVideoEncoder() {
        try {
            if (queue != null) {
                queue.clear();
            }
            isStop = true;
            while (isEnStop) {
                Thread.sleep(10);
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (mMediaCodec != null) {
                mMediaCodec.stop();
                mMediaCodec.release();
                mMediaCodec = null;
            }
            ELog.i(TAG, "stopVideoEncoder");
        } catch (Exception e) {
            mMediaCodec = null;
            ELog.i(TAG, "stopVideoEncoder Exception");
            e.printStackTrace();
        }
    }

    private void YV12toI420(byte[] yv12bytes, byte[] i420bytes, int width,
                            int height) {
        int total = width * height;
        System.arraycopy(yv12bytes, 0, i420bytes, 0, total);
        System.arraycopy(yv12bytes, total + (total >> 2),
                i420bytes, total, (total >> 2));
        System.arraycopy(yv12bytes, total, i420bytes, total
                + (total >> 2), (total >> 2));
    }
}