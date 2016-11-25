package com.and2long.video_record_without_preview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * 思路:开启一个服务,在服务创建成功时开启一个悬浮窗口,
 * 悬浮窗口中放一个布局界面来预览摄像头画面;
 * 将悬浮窗口大小设置成1px.
 */
public class MainActivity extends AppCompatActivity {



    /**
     * onCreate方法
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //开启服务
        startService(new Intent(this, RecordService.class));
    }

}
