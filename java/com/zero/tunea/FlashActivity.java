package com.zero.tunea;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zero.tunea.service.GetSongs;

import java.util.Timer;
import java.util.TimerTask;

public class FlashActivity extends Activity {

    public static Handler handler;

    ImageView iv;
    TextView tv;
    LinearLayout ll;

    int timeOut = 3000;
    int animeTime = timeOut - 500;

    @SuppressLint({"HandlerLeak", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                int command = msg.what;
                if(command == 1){
                    startActivity(new Intent(FlashActivity.this, MainActivity.class));
                    finish();
                }
            }
        };
        super.onCreate(savedInstanceState);
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Intent getSongs = new Intent(this, GetSongs.class);
            startService(getSongs);
        }
        setContentView(R.layout.activity_flash);
        iv = findViewById(R.id.imageViewFlash);
        tv = findViewById(R.id.textViewFlash);
        ll = findViewById(R.id.textHide);
        ObjectAnimator oaImage = new ObjectAnimator(),
                        oaImage2 = new ObjectAnimator(),
                        oaText = new ObjectAnimator();
        oaImage.setTarget(iv);
        float ivY = iv.getY();
        oaImage.setPropertyName("translationY");
        oaImage.setFloatValues(-600, ivY);
        oaImage.setDuration(animeTime);
        oaImage.start();
        oaImage2.setTarget(iv);
        oaImage2.setPropertyName("alpha");
        oaImage2.setFloatValues(0, 1);
        oaImage2.setDuration(animeTime);
        oaImage2.start();
        oaText.setTarget(tv);
        oaText.setPropertyName("translationX");
        oaText.setFloatValues(-200, tv.getX());
        oaText.setDuration(animeTime-200);
        oaText.start();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.obtainMessage(1).sendToTarget();
            }
        }, timeOut);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}