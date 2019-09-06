package codingdavinci.tour.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.util.UUID;

import codingdavinci.tour.R;
import codingdavinci.tour.manager.DataProvider;
import codingdavinci.tour.model.TourDataset;
import codingdavinci.tour.util.ActivityHelper;


public class FilterDatasetObjectActivity extends AbstractPreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.filter_datasetobject_preferences);

        PreferenceManager.setDefaultValues(this, R.xml.filter_datasetobject_preferences, false);

        updateScreen();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // propagate all values
        updatePrefSummary(findPreference(key));
    }
}
