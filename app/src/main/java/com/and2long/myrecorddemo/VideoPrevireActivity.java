package com.and2long.myrecorddemo;

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VideoPrevireActivity extends AppCompatActivity {

    @Bind(R.id.videoView)
    VideoView videoView;
    @Bind(R.id.ib_play)
    ImageView ivPlay;
    private String videoPath;
    private int position;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videopreview);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle("视频预览");
        videoPath = getIntent().getExtras().getString("videoPath");
        videoView.setVideoPath(videoPath);
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.seekTo(0);
                videoView.start();
            }
        });
        ivPlay.setVisibility(View.INVISIBLE);
    }


    /**
     * 点击事件
     *
     * @param view
     */
    @OnClick( {R.id.rl_play})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_play:
                if (videoView.isPlaying()) {
                    System.out.println();
                    videoView.pause();
                    position = videoView.getCurrentPosition();
                    ivPlay.setVisibility(View.VISIBLE);
                } else {
                    videoView.seekTo(position);
                    videoView.start();
                    ivPlay.setVisibility(View.INVISIBLE);
                }
        }
    }

}
