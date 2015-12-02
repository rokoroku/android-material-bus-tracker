package kr.rokoroku.mbus;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import com.canelmas.let.DeniedPermission;
import com.canelmas.let.Let;
import com.canelmas.let.RuntimePermissionListener;
import com.canelmas.let.RuntimePermissionRequest;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

import java.util.List;

/**
 * Created by rok on 2015. 12. 1..
 */
public class WalkthroughActivity extends AppIntro2 implements RuntimePermissionListener {

    // Please DO NOT override onCreate. Use init
    @Override
    public void init(Bundle savedInstanceState) {

        // Add your slide's fragments here
        AppIntroFragment[] slides = {
                AppIntroFragment.newInstance(
                        "통합 검색",
                        "노선 번호, 정류장 또는 정류장 ID\n어떤 것으로든 검색할 수 있어요.",
                        R.drawable.walkthrough_1,
                        getResources().getColor(R.color.md_orange_300)),

                AppIntroFragment.newInstance(
                        "노선 조회",
                        "이제 내 정류장이 어디인지 헤메지 마세요.\nGPS 기능을 이용하면 현재 위치를 기반으로 가까운 정류장을 알 수 있어요.",
                        R.drawable.walkthrough_2,
                        getResources().getColor(R.color.md_green_200)),

                AppIntroFragment.newInstance(
                        "주변 정류장",
                        "정류장이 상행인지 하행인지 헷갈리시죠?\n주변에 있는 정류장을 지도 상에서 쉽게 찾아 보세요.",
                        R.drawable.walkthrough_3,
                        getResources().getColor(R.color.md_indigo_300)),

                AppIntroFragment.newInstance(
                        "즐겨찾기",
                        "그룹 관리가 가능한 즐겨찾기를 통해 \n나만의 즐겨찾기 목록을 만들어 보세요.",
                        R.drawable.walkthrough_4,
                        getResources().getColor(R.color.md_red_200))
        };

        for (AppIntroFragment slide : slides) {
            addSlide(slide);
        }

        // OPTIONAL METHODS
        askForPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 2);

        // Turn vibration on and set intensity
        // NOTE: you will probably need to ask VIBRATE permesssion in Manifest
        // setVibrate(true);
        // setVibrateIntensity(30);
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
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }

    @Override
    public void onSlideChanged() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Let.handle(this, requestCode, permissions, grantResults);
    }

    @Override
    public void onShowPermissionRationale(List<String> permissions, final RuntimePermissionRequest request) {
        /**
         * show permission rationales in a dialog, wait for user confirmation and retry the permission
         * request by calling request.retry()
         */
    }

    @Override
    public void onPermissionDenied(List<DeniedPermission> deniedPermissionList) {
        /**
         * Do whatever you need to do about denied permissions:
         *   - update UI
         *   - if permission is denied with 'Never Ask Again', prompt a dialog to tell user
         *   to go to the app settings screen in order to grant again the permission denied
         */
    }
}
