package com.peng.jni;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;

import java.util.List;

public class CameraUtil implements Camera.PreviewCallback {
    private static final String TAG = "CameraUtil";
    /**
     * 视频宽度
     */
    private int videoWidth;
    /**
     * 视频高度
     */
    private int videoHeight;
    private Camera mCamera = null;
    private int zoom;


    public CameraUtil(int videoWidth, int videoHeight) {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
    }

    /**
     * 获取Camera实例
     *
     * @return
     */
    private Camera getCamera(int id) {
        Camera camera = null;
        try {
            camera = Camera.open(id);
        } catch (Exception e) {

        }
        return camera;
    }

    /**
     * 预览相机
     */
    public void startCamera(PreviewFrameListener listener) {
        Log.e(TAG, "startPreview");
        releaseCamera();
        this.listener = listener;
        mCamera = getCamera(0);
        try {
            //设置相机的参数
            setupCamera(mCamera);
            mCamera.setPreviewCallbackWithBuffer(this);
            mCamera.addCallbackBuffer(new byte[videoWidth * videoHeight * 3 / 2]);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "startPreview Exception " + e.getMessage());
        }
    }

    /**
     * 释放相机资源
     */
    public void releaseCamera() {
        if (mCamera != null) {
            listener = null;
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            Log.e(TAG, "releaseCamera");
        }
    }

    /**
     * 设置相机参数
     */
    private void setupCamera(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        parameters.setPreviewFormat(ImageFormat.YV12);//输出的格式
        parameters.setZoom(zoom);
        parameters.setPreviewSize(videoWidth, videoHeight);
        camera.setParameters(parameters);
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mCamera != null) {
            mCamera.addCallbackBuffer(data);
        }
        if (listener != null) {
            listener.onFrame(data);
        }
    }

    private PreviewFrameListener listener;


    public interface PreviewFrameListener {
        void onFrame(byte[] data);
    }
}
