package com.peng.jni;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;

public class AACEncoder {

    private static final String TAG = "AACEncoder";
    private MediaCodec mAudioEncoder;
    private MediaCodec.BufferInfo mAudioEncodeBufferInfo;
    private ByteBuffer[] encodeInputBuffers;
    private ByteBuffer[] encodeOutputBuffers;
    private RtspServerJni jni;

    /**
     * 初始化编码器
     */
    public void initAudioEncoder(RtspServerJni jni) {
        this.jni = jni;
        try {
            mAudioEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            MediaFormat format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 8000, 1);
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            format.setInteger(MediaFormat.KEY_BIT_RATE, 10000);//比特率
//            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 5 * 1024);
            mAudioEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mAudioEncoder == null) {
            Log.e(TAG, "create mediaEncode failed");
            return;
        }

        mAudioEncoder.start(); // 启动MediaCodec,等待传入数据
        encodeInputBuffers = mAudioEncoder.getInputBuffers(); //上面介绍的输入和输出Buffer队列
        encodeOutputBuffers = mAudioEncoder.getOutputBuffers();
        mAudioEncodeBufferInfo = new MediaCodec.BufferInfo();
    }

    public void encodeData(byte[] data) {
        //dequeueInputBuffer（time）需要传入一个时间值，-1表示一直等待，0表示不等待有可能会丢帧，其他表示等待多少毫秒
        int inputIndex = mAudioEncoder.dequeueInputBuffer(1000 * 20);//获取输入缓存的index
        if (inputIndex >= 0) {
            ByteBuffer inputByteBuf = encodeInputBuffers[inputIndex];
            inputByteBuf.clear();
            inputByteBuf.put(data);//添加数据
            inputByteBuf.limit(data.length);//限制ByteBuffer的访问长度
            mAudioEncoder.queueInputBuffer(inputIndex, 0, data.length, 0, 0);//把输入缓存塞回去给MediaCodec
        }

        int outputIndex = mAudioEncoder.dequeueOutputBuffer(mAudioEncodeBufferInfo, 1000 * 20);//获取输出缓存的index
        while (outputIndex >= 0) {
            //获取缓存信息的长度
            int byteBufSize = mAudioEncodeBufferInfo.size;
            //添加ADTS头部后的长度
//            int bytePacketSize = byteBufSize + 7;
            //拿到输出Buffer
            ByteBuffer outPutBuf = encodeOutputBuffers[outputIndex];
            outPutBuf.position(mAudioEncodeBufferInfo.offset);
            outPutBuf.limit(mAudioEncodeBufferInfo.offset + mAudioEncodeBufferInfo.size);

//            byte[]  targetByte = new byte[bytePacketSize];
            byte[] targetByte = new byte[byteBufSize];
            //添加ADTS头部
//            addADTStoPacket(targetByte, bytePacketSize);
            /*
            get（byte[] dst,int offset,int length）:ByteBuffer从position位置开始读，读取length个byte，并写入dst下
            标从offset到offset + length的区域
             */
//            outPutBuf.get(targetByte,7,byteBufSize);
            outPutBuf.get(targetByte, 0, byteBufSize);

            outPutBuf.position(mAudioEncodeBufferInfo.offset);

            jni.sendAudio(targetByte, targetByte.length);

            //释放
            mAudioEncoder.releaseOutputBuffer(outputIndex, false);
            outputIndex = mAudioEncoder.dequeueOutputBuffer(mAudioEncodeBufferInfo, 0);
        }
    }

    /**
     * 添加ADTS头部
     *
     * @param packet    ADTS header 的 byte[]，长度为7
     * @param packetLen 该帧的长度，包括header的长度
     */
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = 4; //4: 41000Hz  11: 8000 Hz
        int chanCfg = 1; // 1 Channel

        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }
}
