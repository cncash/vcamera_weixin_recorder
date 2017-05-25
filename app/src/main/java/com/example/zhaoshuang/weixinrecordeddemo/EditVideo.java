package com.example.zhaoshuang.weixinrecordeddemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.yixia.camera.MediaRecorderBase;
import com.yixia.videoeditor.adapter.UtilityAdapter;

import java.util.ArrayList;

/**
 * Created by chenglei on 17/5/25.
 */

public class EditVideo extends Activity {

    private TextureVideoPlayer vv_play;
    private ArrayList<String> paths = new ArrayList<>();
    private int  pathpostion = 0;
    private int progress = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editvideo);
        vv_play = (TextureVideoPlayer)findViewById(R.id.vv_play);
        Intent intent = getIntent();
        paths = intent.getStringArrayListExtra("paths");


        vv_play.setUrl(paths.get(pathpostion));
        vv_play.setVideoMode(TextureVideoPlayer.CENTER_MODE);
        vv_play.setOnVideoPlayingListener(new TextureVideoPlayer.OnVideoPlayingListener() {
            @Override
            public void onVideoSizeChanged(int vWidth, int vHeight) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) vv_play.getLayoutParams();
                params.width = getResources().getDisplayMetrics().widthPixels;
                params.height = (int) ((float) params.width / (float) vWidth * (float) vHeight);
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onPlaying(int duration, int percent) {

            }

            @Override
            public void onPause() {

            }

            @Override
            public void onRestart() {

            }

            @Override
            public void onPlayingFinish() {
                vv_play.stop();
                pathpostion++;
                vv_play.setUrl(paths.get(pathpostion % paths.size()));
                vv_play.play();
            }

            @Override
            public void onTextureDestory() {
                if (vv_play != null) {
                    vv_play.stop();
                }
            }
        });



        new Thread() {
            @Override
            public void run() {

                while (progress != 100) {
                    try {
                        sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progress = UtilityAdapter.FilterParserAction("", UtilityAdapter.PARSERACTION_PROGRESS);
                    Log.i("chenglei", progress + "");
                }
                syntVideo();
            }
        }.start();



    }



    /**
     * 合成视频
     */
    private void syntVideo() {

        //ffmpeg -i "concat:ts0.ts|ts1.ts|ts2.ts|ts3.ts" -c copy -bsf:a aac_adtstoasc out2.mp4
        StringBuilder sb = new StringBuilder("ffmpeg");
        sb.append(" -i");
        String concat = "concat:";
        for (String path : paths) {
            concat += path;
            concat += "|";
        }
        concat = concat.substring(0, concat.length() - 1);
        sb.append(" " + concat);
        sb.append(" -c");
        sb.append(" copy");
        sb.append(" -bsf:a");
        sb.append(" aac_adtstoasc");
        sb.append(" -y");
        sb.append(" " + MyApplication.VIDEO_PATH + "/finish.mp4");
        Log.i("chenglei", sb.toString());
        int i = UtilityAdapter.FFmpegRun("", sb.toString());
        Bundle bundle = new Bundle();
        if (i == 0) {
            Log.i("chenglei", "视频合成成功");
        } else {
            Log.i("chenglei", "视频合成失败");
        }
    }



    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            int type = bundle.getInt("type");
            int intdata = bundle.getInt("intdata");
            switch (type) {
//                Init初始化播放
                case 0:
                    vv_play.setUrl(paths.get(pathpostion));
                    vv_play.play();
                    break;
//                视频编码结果
                case 1:
                    if (intdata == 0) {
                        Toast.makeText(EditVideo.this, "视频编码成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditVideo.this, "视频编码失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }

        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        pathpostion = 0;
        mhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putInt("type", 0);
                bundle.putInt("intdata", 0);
                Message msg = mhandler.obtainMessage();
                msg.setData(bundle);
                msg.sendToTarget();
            }
        }, 50);

    }
}
