package com.eits.smartpid;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;


public class VideoPlayActivity extends BaseClass {
    MediaController mediaController;
    VideoView videoView;
    int startPosition;
    int stopPosition = -1;
    Handler handler;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        videoView=findViewById(R.id.videoPlayer_videoView);

        Bundle getLink=getIntent().getExtras();
        String Link=getLink.getString("VIDEO_LINK");

        Uri link= Uri.parse(Link);

        videoView.setVideoURI(link);
        videoView.start();

    }
    @Override
    protected void onResume() {
        super.onResume();

        mediaController = new MediaController(this);
        handler= new Handler();
        runnable=new Runnable() {
            @Override
            public void run() {
                videoView.setMediaController(mediaController);
            }
        };
        handler.postDelayed(runnable,1000);


        if (stopPosition != -1) {
            videoView.seekTo(stopPosition);
        }

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
    protected void onPause() {
        super.onPause();
        stopPosition = videoView.getCurrentPosition();

        handler.removeCallbacks(runnable);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();


    }

}