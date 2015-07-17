package kr.rokoroku.mbus;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import io.codetail.animation.SupportAnimator;
import kr.rokoroku.mbus.util.RevealUtils;

public class SplashActivity extends AppCompatActivity {

    Handler mHandler;
    View mLogoLayout;
    View mDummyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(BaseApplication.getInstance().getThemeId());
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        mLogoLayout = findViewById(R.id.logo_layout);
        mDummyView = findViewById(R.id.dummy_view);
        mHandler = new Handler();
        mHandler.postDelayed(() -> RevealUtils.revealView(mDummyView, RevealUtils.Position.CENTER, 400, new SupportAnimator.SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd() {
                if (!isFinishing()) {
                    startMainActivity();
                }
            }
        }), 1500);
        mLogoLayout.setOnClickListener(v -> {
            if (!isFinishing()) {
                startMainActivity();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        ImageView logoImageView = (ImageView) findViewById(R.id.logo_image);
        ((AnimationDrawable)logoImageView.getDrawable()).start();
    }

    public void startMainActivity() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
