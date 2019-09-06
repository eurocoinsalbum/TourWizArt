package codingdavinci.tour.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import codingdavinci.tour.R;
import codingdavinci.tour.listener.LoadStatusListener;
import codingdavinci.tour.listener.LoadTextListener;
import codingdavinci.tour.listener.LoadToursListener;
import codingdavinci.tour.listener.SaveUserListener;
import codingdavinci.tour.manager.DataProvider;
import codingdavinci.tour.model.LanguageCode;
import codingdavinci.tour.model.Tour;
import codingdavinci.tour.model.User;
import codingdavinci.tour.util.ActivityHelper;
import codingdavinci.tour.util.PreferencesHelper;
import codingdavinci.tour.util.TextHelper;
import codingdavinci.tour.util.ViewHelper;
import codingdavinci.tour.view.NavigationButton;

public class MainActivity extends CdvActivity {
    private NavigationButton btnBrowse;
    private NavigationButton btnMyTours;
    private NavigationButton btnCreate;

    private LanguageCode languageCode = LanguageCode.en;
    private AtomicBoolean initalized = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatus(new LoadStatusListener() {
            @Override
            public void statusLoaded() {
                Log.i("cdv", "User loaded");
                setContentView(R.layout.activity_main);
                initUiObjects();

                // check on app start if texts have been loaded. If not load them now before rendering the UI.
                if (TextHelper.getLoadedLanguage() != languageCode) {
                    TextHelper.loadLanguage(languageCode, new LoadTextListener() {
                        @Override
                        public void textsLoaded(boolean success, Map<Integer, String> texts) {
                            languageCode = TextHelper.getLoadedLanguage();
                            initalized.set(true);
                            buildScreen();
                        }
                    });
                    return;
                }
                initalized.set(true);
                buildScreen();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        buildScreen();
    }

    private void initStatus(final LoadStatusListener loadStatusListener) {
        if (DataProvider.getMyUser() != null) {
            loadStatusListener.statusLoaded();
            return;
        }
        final SharedPreferences globalSharedPrefs = getApplicationContext().getSharedPreferences("global", Context.MODE_PRIVATE);

        // myUserUuid
        UUID myUserUuid;
        String myUserUuidString = "";
        // load uuid from Android device database
        if (globalSharedPrefs.contains(PreferencesHelper.PREF_KEY_MY_USER_UUID)) {
            myUserUuidString = globalSharedPrefs.getString(PreferencesHelper.PREF_KEY_MY_USER_UUID, "");
        }
        if (!myUserUuidString.isEmpty()) {
            myUserUuid = UUID.fromString(myUserUuidString);
        } else {
            // first time start - create new UUID for this device
            myUserUuid = UUID.randomUUID();
        }
        DataProvider.saveMyUser(myUserUuid, new SaveUserListener() {
            @Override
            public void userSaved(User.Role role) {
                Log.i("cdv", "User saved");
                SharedPreferences.Editor editor = globalSharedPrefs.edit();
                editor.putString(PreferencesHelper.PREF_KEY_MY_USER_UUID, DataProvider.getMyUserUuidStr());
                editor.apply();
                loadStatusListener.statusLoaded();
            }
        });

        // language
        if (globalSharedPrefs.contains(PreferencesHelper.PREF_KEY_LANGUAGE)) {
            languageCode = LanguageCode.valueOf(globalSharedPrefs.getString(PreferencesHelper.PREF_KEY_LANGUAGE, null));
        } else {
            SharedPreferences.Editor editor = globalSharedPrefs.edit();
            editor.putString(PreferencesHelper.PREF_KEY_LANGUAGE, languageCode.name());
            editor.apply();
        }
    }

    @Override
    protected void initUiObjects() {
        buildActionBar();

        // Navigation buttons
        LinearLayout navigationBar = ViewHelper.createNavigationBar(this, R.id.main);
        btnBrowse = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_browse, R.string.nav_browse_tours);
        btnMyTours = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_favorites, R.string.nav_my_tours);
        btnCreate = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_create_tour, R.string.nav_create_tour);
    }

    @Override
    protected void buildScreen() {
        if (!initalized.get()) {
            Log.i("cdv", "data not initialized yet. Waiting for next call to buildScreen() function");
            return;
        }

        UUID tourDatasetUuid = null;
        if (getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean(PreferencesHelper.PREF_KEY_SHOW_ONLY_STAEDEL, false)) {
            tourDatasetUuid = DataProvider.getStaedelUuid();
        }

        DataProvider.getDashboardTours(tourDatasetUuid, new LoadToursListener() {
            @Override
            public void toursLoaded(@NonNull List<Tour> tours) {
                updateDashboard(tours);
            }
        });

        // browse
        btnBrowse.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean(PreferencesHelper.PREF_KEY_SHOW_ONLY_STAEDEL, false)) {
                    ActivityHelper.startCdvActivity(MainActivity.this, BrowseToursActivity.class, null, ActivityHelper.EXTRA_TOUR_DATASET_UUID, DataProvider.getStaedelUuid());
                } else {
                    ActivityHelper.startCdvActivity(MainActivity.this, BrowseDatasetsActivity.class, BrowseToursActivity.class, null, null);
                }
            }
        });

        // my tours
        btnMyTours.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityHelper.startCdvActivity(MainActivity.this, MyToursActivity.class, null, null, null);
            }
        });

        // create
        btnCreate.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean(PreferencesHelper.PREF_KEY_SHOW_ONLY_STAEDEL, false)) {
                    ActivityHelper.startCdvActivity(MainActivity.this, SelectDatasetObjectsActivity.class, null, ActivityHelper.EXTRA_TOUR_DATASET_UUID, DataProvider.getStaedelUuid());
                } else {
                    ActivityHelper.startCdvActivity(MainActivity.this, BrowseDatasetsActivity.class, SelectDatasetObjectsActivity.class, null, null);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        // drop this event to avoid closing the app
    }

    private void updateDashboard(List<Tour> tours) {
        LinearLayout dashBoardParent = (findViewById(R.id.dash_board_parent));
        dashBoardParent.removeAllViews();
        View dashBoard = ViewHelper.createDashBoard(this, 2, tours);
        dashBoardParent.addView(dashBoard);
    }
}
