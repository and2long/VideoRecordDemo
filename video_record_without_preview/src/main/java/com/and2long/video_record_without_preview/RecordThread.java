package com.and2long.video_record_without_preview;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by and2long on 2016/11/25.
 */

public class RecordThread extends Thread {
    private static final String TAG = "RecordThread";
    private MediaRecorder mediarecorder;
    private long recordTime;
    private Camera mCamera;
    private SurfaceHolder surfaceHolder;

    public RecordThread(long recordTime, SurfaceHolder surfaceHolder) {
        this.recordTime = recordTime;
        this.surfaceHolder = surfaceHolder;
    }

    @Override
    public void run() {

        /**
         * 开始录像
         */
        startRecord();

        /**
         * 启动定时器，到规定时间recordTime后执行停止录像任务
         */
        Timer timer = new Timer();

        timer.schedule(new TimerThread(), recordTime);
    }


    /**
     * 获取摄像头实例对象
     *
     * @return
     */
    public Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(0);
        } catch (Exception e) {
            // 打开摄像头错误
            Log.i(TAG, "打开摄像头错误");
        }
        return c;
    }

    /**
     * 开始录像
     */
    public void startRecord() {
        mediarecorder = new MediaRecorder();// 创建mediarecorder对象
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);
        // 解锁camera
        mCamera.unlock();
        mediarecorder.setCamera(mCamera);
        mediarecorder.setOrientationHint(90);
        // 设置录制视频源为Camera(相机)
        mediarecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediarecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        // 设置录制文件质量，格式，分辨率之类，这个全部包括了
        mediarecorder.setProfile(CamcorderProfile
                .get(CamcorderProfile.QUALITY_480P));
        mediarecorder.setPreviewDisplay(surfaceHolder.getSurface());
        // 设置视频文件输出的路径
        mediarecorder.setOutputFile("/sdcard/000001.mp4");
        try {
            // 准备录制
            mediarecorder.prepare();
            // 开始录制
            mediarecorder.start();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        if (mediarecorder != null) {
            // 停止录制
            mediarecorder.stop();
            // 释放资源
            mediarecorder.release();
            mediarecorder = null;

            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
        }
    }

    /**
     * 定时器
     *
     * @author bcaiw
     */
    class TimerThread extends TimerTask {

        /**
         * 停止录像
         */
        @Override
        public void run() {
            stopRecord();
            this.cancel();
        }
    }

}
