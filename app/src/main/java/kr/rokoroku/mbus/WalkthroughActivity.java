package kr.rokoroku.mbus;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

/**
 * Created by rok on 2015. 12. 1..
 */
public class WalkthroughActivity extends AppIntro2 {

    // Please DO NOT override onCreate. Use init
    @Override
    public void init(Bundle savedInstanceState) {

        // Add your slide's fragments here
        AppIntroFragment[] slides = {
                AppIntroFragment.newInstance(
                        getString(R.string.walkthrough_search_title),
                        getString(R.string.walkthrough_search_description),
                        R.drawable.walkthrough_1,
                        getResources().getColor(R.color.md_orange_300)),

                AppIntroFragment.newInstance(
                        getString(R.string.walkthrough_route_title),
                        getString(R.string.walkthrough_route_description),
                        R.drawable.walkthrough_2,
                        getResources().getColor(R.color.md_green_200)),

                AppIntroFragment.newInstance(
                        getString(R.string.walkthrough_station_title),
                        getString(R.string.walkthrough_station_description),
                        R.drawable.walkthrough_3,
                        getResources().getColor(R.color.md_indigo_300)),

                AppIntroFragment.newInstance(
                        getString(R.string.walkthrough_favorite_title),
                        getString(R.string.walkthrough_favorite_description),
                        R.drawable.walkthrough_4,
                        getResources().getColor(R.color.md_red_200))
        };

        for (AppIntroFragment slide : slides) {
            addSlide(slide);
        }

        // OPTIONAL METHODS
        askForPermissions(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 2);

        // Turn vibration on and set intensity
        // NOTE: you will probably need to ask VIBRATE permesssion in Manifest
        setVibrate(true);
        setVibrateIntensity(30);
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onDonePressed() {
        // Do something when users tap on Done button.
        if (!isFinishing()) {
            getSharedPreferences(BaseApplication.SHARED_PREFERENCE_KEY, MODE_PRIVATE).edit()
                    .putBoolean(BaseApplication.PREFERENCE_WALKTHROUGH, true)
                    .apply();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }

    @Override
    public void onSlideChanged() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
