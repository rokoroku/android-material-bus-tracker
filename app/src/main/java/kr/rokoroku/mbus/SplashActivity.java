package kr.rokoroku.mbus;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import co.naughtyspirit.showcaseview.ShowcaseView;
import io.codetail.animation.SupportAnimator;
import kr.rokoroku.mbus.core.FavoriteFacade;
import kr.rokoroku.mbus.data.model.Favorite;
import kr.rokoroku.mbus.data.model.FavoriteGroup;
import kr.rokoroku.mbus.data.model.FavoriteItem;
import kr.rokoroku.mbus.data.model.Route;
import kr.rokoroku.mbus.data.model.RouteStation;
import kr.rokoroku.mbus.data.model.Station;
import kr.rokoroku.mbus.data.model.StationRoute;
import kr.rokoroku.mbus.util.RevealUtils;
import kr.rokoroku.mbus.util.ViewUtils;

public class SplashActivity extends AppCompatActivity {

    private View mLogoLayout;
    private View mDummyView;
    private AsyncTask loadFavoriteTask;
    private boolean isFinishing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(BaseApplication.getInstance().getCurrentTheme());
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        mLogoLayout = findViewById(R.id.logo_layout);
        mDummyView = findViewById(R.id.dummy_view);
        ViewUtils.runOnUiThread(() -> {
            if (!isFinishing) startMainActivity();
        }, 1600);
        findViewById(android.R.id.content).setOnClickListener(v -> {
            if (!isFinishing && loadFavoriteTask.getStatus() == AsyncTask.Status.FINISHED) {
                startMainActivity();
            }
        });

        executeLoadFavoriteTask();
    }

    private void executeLoadFavoriteTask() {
        loadFavoriteTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void[] params) {
                Favorite currentFavorite = FavoriteFacade.getInstance().getCurrentFavorite();
                for (FavoriteGroup favoriteGroup : currentFavorite.getFavoriteGroups()) {
                    for (int i = 0; i < favoriteGroup.size(); i++) {
                        FavoriteItem favoriteItem = favoriteGroup.get(i);
                        FavoriteItem.Type type = favoriteItem.getType();
                        switch (type) {
                            case ROUTE:
                                favoriteItem.getData(Route.class);
                                favoriteItem.getData(RouteStation.class);
                                break;
                            case STATION:
                                favoriteItem.getData(Station.class);
                                favoriteItem.getData(StationRoute.class);
                                break;
                        }
                    }
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        ImageView logoImageView = (ImageView) findViewById(R.id.logo_image);
        ((AnimationDrawable) logoImageView.getDrawable()).start();
    }

    public synchronized void startMainActivity() {
        isFinishing = true;
        RevealUtils.revealView(mDummyView, RevealUtils.Position.CENTER, 500, new SupportAnimator.SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd() {
                if (!isFinishing()) {
                    boolean shouldShowWalkthrough = !getSharedPreferences(BaseApplication.SHARED_PREFERENCE_KEY, MODE_PRIVATE).contains(BaseApplication.PREFERENCE_WALKTHROUGH);
                    Intent intent = new Intent(SplashActivity.this, shouldShowWalkthrough ? WalkthroughActivity.class : MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
            }
        });
    }
}
