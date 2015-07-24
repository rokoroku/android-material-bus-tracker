package kr.rokoroku.mbus;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by rok on 2015. 7. 23..
 */
public class PreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public final int DEFAULT_PREFERENCE_RESOURCE = 0;

    /**
     * XML Resource id to set a preference screen.
     */
    private int resourceId;

    private OnFragmentAttachedListener onFragmentAttachedListener;

    public PreferencesFragment() {
        this.resourceId = DEFAULT_PREFERENCE_RESOURCE;
    }

    @Override
    public void setArguments(Bundle args) {
        if (args != null) this.resourceId = args.getInt("resourceId", DEFAULT_PREFERENCE_RESOURCE);
        super.setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set default preference key
        getPreferenceManager().setSharedPreferencesName(BaseApplication.SHARED_PREFERENCE_KEY);

        // Change setting resource when user is female
        if (resourceId == DEFAULT_PREFERENCE_RESOURCE) {
            addPreferencesFromResource(R.xml.preference);

        } else {
            // Load the preferences from an XML resource
            addPreferencesFromResource(resourceId);
        }

        // Call onFragmentAttachedListener
        if (onFragmentAttachedListener != null) onFragmentAttachedListener.onFragmentAttached(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = super.onCreateView(inflater, container, savedInstanceState);
        // if (contentView != null) {
            // Remove default layout padding
            // ListView listView = (ListView) contentView.findViewById(android.R.id.list);
            // listView.setDivider(new ColorDrawable(ThemeUtils.getResourceColor(inflater.getContext(), android.R.color.transparent)));
            // listView.setDividerHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
            // listView.setPadding(0, 0, 0, 0);
        // }
        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreferenceValue(findPreference(key));
    }

    private void updatePreferenceValue(Preference preference) {
//        if (preference instanceof PreferenceValueInterface) {
//            PreferenceValueInterface preferenceInterface = (PreferenceValueInterface) preference;
//            preferenceInterface.updateValue();
//        }
    }

    public void setOnFragmentAttachedListener(OnFragmentAttachedListener onFragmentAttachedListener) {
        this.onFragmentAttachedListener = onFragmentAttachedListener;
    }

    public interface OnFragmentAttachedListener {
        void onFragmentAttached(PreferencesFragment fragment);
    }
}
