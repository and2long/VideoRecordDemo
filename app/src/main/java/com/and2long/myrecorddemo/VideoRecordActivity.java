package com.and2long.myrecorddemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OutputFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;


public class VideoRecordActivity extends Activity implements OnClickListener, SurfaceHolder.Callback {
    private static final String TAG = "VideoRecordActivity";
    private ImageButton start;
    private SurfaceView mSurfaceView;
    private SurfaceHolder surfaceHolder;
    private boolean isRecording = false;
    private MediaRecorder mediarecorder;
    private Camera camera;
    private Parameters params;
    private File file;
    private int BitRate = 5;
    private int displayOrientation = 90;
    private long time = 0 - 1;
    private TextView time_tv;
    private LinearLayout ll_time;
    private Handler handler;
    private String vid_name;
    private Button next;
    private float density;
    private ViewGroup.LayoutParams layoutParams;
    private Button back;
    private ProgressDialog progressDialog;
    String saveVideoPath;
    String currentVideoFilePath;
    private List<String> videoList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置无标题栏，状态栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_videorecord);
        ButterKnife.bind(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //删除缓存.
        Utils.delFiles(new File(getSDPath(VideoRecordActivity.this)));

        initView();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Waiting...");
        videoList = new ArrayList<>();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放.
        if (mediarecorder != null) {
            mediarecorder.reset();
            mediarecorder.release();
            mediarecorder = null;
        }
    }

    private void initView() {
        start = (ImageButton) this.findViewById(R.id.start_record);
        start.setOnClickListener(this);
        back = (Button) findViewById(R.id.bt_back);
        back.setOnClickListener(this);

        layoutParams = start.getLayoutParams();
        density = getResources().getDisplayMetrics().density;

        time_tv = (TextView) findViewById(R.id.time);
        ll_time = (LinearLayout) findViewById(R.id.ll_time);
        handler = new Handler();

        mSurfaceView = (SurfaceView) this.findViewById(R.id.surfaceview);

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);

        next = (Button) findViewById(R.id.bt_next);
        next.setOnClickListener(this);

        surfaceHolder = mSurfaceView.getHolder();// 取得holder
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setKeepScreenOn(true);
        mSurfaceView.setFocusable(true);
        surfaceHolder.addCallback(this); // holder加入回调接口


    }

    private Runnable timeRun = new Runnable() {
        @Override
        public void run() {
            time++;
            ll_time.setVisibility(View.VISIBLE);
            time_tv.setText(timeFormat((int) time));
            handler.postDelayed(timeRun, 1000);

        }
    };


    /**
     * 时间格式化
     *
     * @param timeMs
     * @return
     */
    public String timeFormat(int timeMs) {
        int totalSeconds = timeMs;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.getDefault());
        if (hours > 0) {
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }


    /**
     * 开始录制
     */
    protected void start() {
        vid_name = Utils.getStringDate() + ".mp4";
        currentVideoFilePath = getSDPath(VideoRecordActivity.this) + vid_name;
        videoList.add(currentVideoFilePath);
        /*file = new File(currentVideoFilePath);
        if (file.exists()) {
            // 如果文件存在，删除它，演示代码保证设备上只有一个录音文件            file.delete();
        }*/
        camera.stopPreview();
        camera.unlock();

        if (mediarecorder == null) {
            mediarecorder = new MediaRecorder();// 创建mediarecorder对象
            mediarecorder.setCamera(camera);
            // 设置录制视频源为Camera(相机)
            mediarecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
            mediarecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
//        // 录像旋转90度
            mediarecorder.setOrientationHint(displayOrientation);
            // mediaRecorder.setVideoSource(VideoSource.CAMERA);
            // 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
            mediarecorder.setOutputFormat(OutputFormat.MPEG_4);
            // // 设置录制的视频编码h263 h264
            mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            // 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
            mediarecorder.setVideoSize(1280, 720);

            mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            // 设置高质量录制,改变编码速率
            mediarecorder.setVideoEncodingBitRate(BitRate * 1024 * 512);

            // 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
            mediarecorder.setVideoFrameRate(30);

            mediarecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
            // 设置视频文件输出的路径
            mediarecorder.setOnErrorListener(new OnErrorListener() {

                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    // 发生错误，停止录制
                    mediarecorder.stop();
                    mediarecorder.reset();
                    mediarecorder.release();
                    mediarecorder = null;
                    isRecording = false;
                }
            });
        }
        mediarecorder.setOutputFile(currentVideoFilePath);
        try {
            // 准备、开始
            mediarecorder.prepare();
            mediarecorder.start();//开始刻录
            isRecording = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        layoutParams.width = (int) (30 * density);
        layoutParams.height = (int) (30 * density);
        start.setLayoutParams(layoutParams);

        handler.post(timeRun);

    }

    protected void stop() {
        if (isRecording) {
            // 如果正在录制，停止并释放资源
            try {
                mediarecorder.setOnErrorListener(null);
                mediarecorder.setPreviewDisplay(null);
                mediarecorder.stop();
                mediarecorder.reset();
                mediarecorder.release();

                mediarecorder = null;
                isRecording = false;
                handler.removeCallbacks(timeRun);

                layoutParams.width = (int) (42 * density);
                layoutParams.height = (int) (42 * density);
                start.setLayoutParams(layoutParams);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 点击事件处理
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //录制与暂停.
            case R.id.start_record:
                //设置500ms内只响应一次点击事件.
                start.setClickable(false);
                if (!isRecording) {
                    //开始
                    start();
                } else {
                    //暂停
                    stop();
                    camera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            if (success == true)
                                VideoRecordActivity.this.camera.cancelAutoFocus();
                        }
                    });
                    //如果录制事件大于等于1秒,显示下一步按钮.
                    if (time >= 1) {
                        next.setVisibility(View.VISIBLE);
                    }
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        start.setClickable(true);
                    }
                }, 500);
                break;
            //进入下一界面.
            case R.id.bt_next:
                progressDialog.show();
                if (isRecording) {
                    stop();
                }
                mergeVideos();

                break;
            //返回上一界面.
            case R.id.bt_back:
                finish();
                break;
            default:
                break;
        }
    }

    private void next() {
        Intent intent = new Intent(VideoRecordActivity.this, VideoPrevireActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("videoPath", saveVideoPath);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    /**
     * 合并视频.
     */
    private void mergeVideos() {

        saveVideoPath = getSDPath(VideoRecordActivity.this) + Utils.getStringDate() + ".mp4";

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String[] str =  videoList.toArray(new String[videoList.size()]);
                    for (int i = 0; i < str.length; i++) {
                        System.out.println(str[i]);
                    }
                    Utils.appendVideo(VideoRecordActivity.this, saveVideoPath, str);
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    next();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * 获取缓存文件路径
     * @param context
     * @return
     */
    public static String getSDPath(Context context) {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        } else if (!sdCardExist) {

            Toast.makeText(context, "No SDCard", Toast.LENGTH_SHORT).show();

        }
        File eis = new File(sdDir.toString() + "/MyRecordCache/");
        try {
            if (!eis.exists()) {
                eis.mkdir();
            }
        } catch (Exception e) {

        }
        return sdDir.toString() + "/MyRecordCache/";

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder
        Log.d(TAG, "surfaceCreated");

        if (null == camera) {
            camera = Camera.open();
            try {
                camera.setPreviewDisplay(surfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera();
            camera.startPreview();

        }

    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");

        camera();
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            params.set("orientation", "portrait");
            camera.setDisplayOrientation(90);
        } else {
            params.set("orientation", "landscape");
            camera.setDisplayOrientation(0);
        }
    }

    private void camera() {
        try {
            camera.stopPreview();
            params = camera.getParameters();
            params.setPreviewSize(1280, 720);
            params.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            camera.setParameters(params);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int ort = getResources().getConfiguration().orientation;
        if (ort == Configuration.ORIENTATION_PORTRAIT) {
            displayOrientation = 90;
//            Toast.makeText(this, "竖屏", Toast.LENGTH_SHORT).show();
        } else if (ort == Configuration.ORIENTATION_LANDSCAPE) {
            displayOrientation = 0;
//            Toast.makeText(this, "横屏", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
// surfaceDestroyed的时候同时对象设置为null

        if (mediarecorder != null) {
            mediarecorder.setOnErrorListener(null);
            mediarecorder.setPreviewDisplay(null);
            mediarecorder.stop();
            mediarecorder.reset();
            mediarecorder.release();
            mediarecorder = null;
            //camera.lock();
        }
        camera.stopPreview();
        camera.release();
        camera = null;
//        finish();
    }

}
