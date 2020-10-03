package com.hridayapp.mapapplication.Util;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.hridayapp.mapapplication.MapsActivity;
import com.hridayapp.mapapplication.R;

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_SCREEN = 3000;
    Animation topanimation,bottomanimation;
    ImageView image;
    TextView logo;
    TextView power;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
      setContentView(R.layout.activity_splash);

        //Animations
        topanimation = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomanimation = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);

        //Hooks
        image = findViewById(R.id.imageView);
        logo = findViewById(R.id.textView);
        power = findViewById(R.id.power);

        image.setAnimation(topanimation);
        logo.setAnimation(bottomanimation);
        power.setAnimation(bottomanimation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this,MapsActivity.class);
                startActivity(intent);
                finish();
            }
        },SPLASH_SCREEN);

    }

}