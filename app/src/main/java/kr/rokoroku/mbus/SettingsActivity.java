package kr.rokoroku.mbus;

import android.os.Bundle;
import android.preference.ListPreference;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;

public class SettingsActivity extends AppCompatActivity implements PreferencesFragment.OnFragmentAttachedListener {

    private static final String FRAGMENT_TAG = "PREFERENCE";

    private int mThemeId;
    private PreferencesFragment mFragment;
    protected CoordinatorLayout mCoordinatorLayout;
    protected FrameLayout mContentFrame;
    protected Toolbar mToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.mThemeId = BaseApplication.getInstance().getCurrentTheme();
        this.setTheme(mThemeId);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mContentFrame = (FrameLayout) findViewById(R.id.content_frame);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        if (savedInstanceState == null) {
            // During initial setup, plug in the details fragment.
            mFragment = new PreferencesFragment();
            mFragment.setArguments(getIntent().getExtras());
            mFragment.setOnFragmentAttachedListener(this);
            getFragmentManager().beginTransaction().add(R.id.content_frame, mFragment, FRAGMENT_TAG).commit();
        } else {
            // Recreating the activity, get fragment from FragmentManager
            mFragment = (PreferencesFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
            mFragment.setOnFragmentAttachedListener(this);
            onFragmentAttached(mFragment);
        }

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_activity_settings);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentAttached(PreferencesFragment fragment) {

        mFragment = fragment;
        ListPreference preferenceTheme = (ListPreference) fragment.getPreferenceManager().findPreference("pref_theme");

        if (preferenceTheme != null) {
            preferenceTheme.setOnPreferenceChangeListener((preference, newValue) -> {
                int result = Integer.parseInt((String) newValue);
                BaseApplication.getInstance().setThemeIndex(result);
                if(BaseApplication.getInstance().getCurrentTheme() != mThemeId) {
                    recreate();
                }
                return true;
            });
        }
    }
}
