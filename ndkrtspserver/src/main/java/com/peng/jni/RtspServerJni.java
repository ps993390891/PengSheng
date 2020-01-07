package com.peng.jni;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;



public class RtspServerJni implements OnClientNumberListener, CameraUtil.PreviewFrameListener {
    private static final String TAG = "RtspServerJni";
    private static final int TWO_TWO_TWO =  222;

    static {
        System.loadLibrary("rtsp_server_jni");
    }


    public static final int AAC = 0;//采样8000 单通道1 无adts头false
    public static final int G711a = 1;//采样8000

    //h264
    public native boolean startRtspServer(int prot, int type, OnClientNumberListener listener);

    public native void sendVideo(byte[] data, int size);

    public native void sendAudio(byte[] data, int size);


    //私有化构造函数
    private RtspServerJni() {
//        EventBus.getDefault().register(this);
    }

    private static RtspServerJni instance = null;

    public static RtspServerJni getInstance() {
        if (instance == null) {
            synchronized (RtspServerJni.class) {
                if (instance == null) {
                    instance = new RtspServerJni();
                }
            }
        }
        return instance;
    }

    private CameraUtil cameraUtil;
    private H264Encoder h264Encoder;
    private AudioRecordUtils audioRecordUtils;
    private boolean isCode = false;
    private int clientNumber = 0;
    private boolean isStartServer = false;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case TWO_TWO_TWO:
                    stopCode();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 开启rtsp服务
     */
    public void start() {
        if (isStartServer) {
            return;
        }
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                isStartServer = true;
                h264Encoder = new H264Encoder();
                audioRecordUtils = new AudioRecordUtils();
                cameraUtil = new CameraUtil(640, 480);
                startRtspServer(9654, RtspServerJni.G711a, RtspServerJni.this);
            }
        });

    }

    /**
     * 开始编码
     */
    private void startCode() {
        if (isCode) {
            return;
        }
        ELog.e(TAG, "---->>>startCode");
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                ELog.e(TAG, "---->>>startCode...");
                isCode = true;
                h264Encoder.startMediaCode(640, 480, RtspServerJni.this);
                audioRecordUtils.startRecord(RtspServerJni.this);
                cameraUtil.startCamera(RtspServerJni.this);
            }
        });
    }

    /**
     * 结束编码
     */
    private void stopCode() {
        ELog.e(TAG, "---->>>stopCode");
        releaseCamera();
        audioRecordUtils.stopRecord();
        h264Encoder.stopVideoEncoder();
        isCode = false;
    }

    public void offVieo(byte[] data) {
        if (isCode) {
            h264Encoder.offVieoEncoder(data);
        }
    }

    public void releaseCamera() {
        cameraUtil.releaseCamera();
    }

    @Override
    public void onClientNumber(int number) {
        ELog.e(TAG, "onClientNumber------------------------->>" + number);
        clientNumber = number;
        if (number > 0) {
            handler.removeMessages(TWO_TWO_TWO);
            startCode();
        } else {
            handler.sendEmptyMessageDelayed(TWO_TWO_TWO,10*1000);
        }
    }

    @Override
    public void onFrame(byte[] data) {
        offVieo(data);
    }

//    @Subscribe(threadMode = ThreadMode.BACKGROUND)
//    public void onMessageEvent(CameraCloseEvent event) {
//        if (clientNumber > 0) {
//            cameraUtil.startCamera(RtspServerJni.this);
//        }
//    }
}

