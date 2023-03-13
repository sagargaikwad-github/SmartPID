package com.eits.smartpid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;

import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.eits.smartpid.adapter.VideoAdapter;


public class VideoPlayActivity extends BaseClass {
    MediaController mediaController;
    VideoView videoView;
    int startPosition;
    int stopPosition = -1;
    Handler handler;
    Runnable runnable;
    Toolbar videoPlayer_Toolbar;
    Uri link;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // hideSystemUI();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);

        videoView = findViewById(R.id.videoPlayer_videoView);
        videoPlayer_Toolbar = findViewById(R.id.videoPlayer_Toolbar);

        configureToolbar();

        Bundle getLink = getIntent().getExtras();
        String Link = getLink.getString("VIDEO_LINK");

         link = Uri.parse(Link);

//        DisplayMetrics metrics = new DisplayMetrics(); getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) videoView.getLayoutParams();
//        params.width =  metrics.widthPixels;
//        params.height = metrics.heightPixels;
//        params.leftMargin = 0;
//        videoView.setLayoutParams(params);


        videoView.setVideoURI(link);


    }

    private void configureToolbar() {
        videoPlayer_Toolbar.setNavigationIcon(R.drawable.ic_back);
        videoPlayer_Toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        videoView.start();
        mediaController = new MediaController(this);
//        handler = new Handler();
//        runnable = new Runnable() {
//            @Override
//            public void run() {
//                videoView.setMediaController(mediaController);
//            }
//        };
//        handler.postDelayed(runnable, 1000);


        videoView.setMediaController(mediaController);

        videoView.requestFocus();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);


        if (stopPosition != -1) {
            videoView.seekTo(stopPosition);
        }

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoView.setVideoURI(null);
                videoView.setVideoURI(link);
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                AlertDialog.Builder build = new AlertDialog.Builder(VideoPlayActivity.this);
                build.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onBackPressed();
                    }
                });
                AlertDialog alt = build.create();
                alt.setMessage("Can't Play this Video");
                alt.show();
                return true;
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPosition = videoView.getCurrentPosition();

//        handler.removeCallbacks(runnable);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

}