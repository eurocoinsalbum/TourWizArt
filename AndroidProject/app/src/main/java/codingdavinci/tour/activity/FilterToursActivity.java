package codingdavinci.tour.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.util.UUID;

import codingdavinci.tour.R;
import codingdavinci.tour.manager.DataProvider;
import codingdavinci.tour.model.TourDataset;
import codingdavinci.tour.util.ActivityHelper;


public class FilterToursActivity extends AbstractPreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID tourDatasetUuid = ActivityHelper.getTourDatasetUuid(getIntent());
        TourDataset tourDataset = DataProvider.getTourDataset(tourDatasetUuid);

        //addPreferencesFromResource(FilterHelper.getPreferencesResourceId(tourDataset));
        addPreferencesFromResource(R.xml.filter_tour_preferences);

        PreferenceManager.setDefaultValues(this, R.xml.filter_tour_preferences, false);

        updateScreen();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // propagate all values
        updatePrefSummary(findPreference(key));
    }
}
