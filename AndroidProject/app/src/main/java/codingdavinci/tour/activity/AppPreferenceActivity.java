package codingdavinci.tour.activity;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import codingdavinci.tour.R;
import codingdavinci.tour.util.PreferencesHelper;

/**
 * It's mandatory to put the extra intent "prefsName" before calling this activity
 */
public class AppPreferenceActivity extends AbstractPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("global");

        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        updateScreen();
    }

    @TargetApi(value = 11)
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // language changed
        if (key.equals(PreferencesHelper.PREF_KEY_LANGUAGE)) {
            // reload activity
            if (Build.VERSION.SDK_INT < 11) {
                finish();
                return;
            }
            this.recreate();
        }

        // propagate all values
        updatePrefSummary(findPreference(key));
    }
}

