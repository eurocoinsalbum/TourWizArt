package codingdavinci.tour.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.LinearLayout;

import java.util.List;

import codingdavinci.tour.R;
import codingdavinci.tour.listener.LoadToursListener;
import codingdavinci.tour.manager.DataProvider;
import codingdavinci.tour.model.Tour;
import codingdavinci.tour.util.ActivityHelper;
import codingdavinci.tour.util.PreferencesHelper;
import codingdavinci.tour.util.ViewHelper;
import codingdavinci.tour.view.NavigationButton;

public class MyToursActivity extends CdvActivity {
    private NavigationButton btnDashboard;
    private NavigationButton btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tours);
        initUiObjects();
    }

    @Override
    protected void onStart() {
        super.onStart();
        buildScreen();
    }

    @Override
    protected void initUiObjects() {
        buildActionBar();

        // Navigation buttons
        LinearLayout navigationBar = ViewHelper.createNavigationBar(this, R.id.main);
        btnDashboard = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_dashboard, R.string.nav_dashboard);
        btnCreate = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_create_tour, R.string.nav_create_tour);
    }

    @Override
    protected void buildScreen() {
        DataProvider.getMyTours(new LoadToursListener() {
            @Override
            public void toursLoaded(@NonNull List<Tour> tours) {
                updateDashboard(tours);
            }
        });

        btnDashboard.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityHelper.startCdvActivity(MyToursActivity.this, MainActivity.class);
            }
        });

        // create
        btnCreate.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean(PreferencesHelper.PREF_KEY_SHOW_ONLY_STAEDEL, false)) {
                    ActivityHelper.startCdvActivity(MyToursActivity.this, SelectDatasetObjectsActivity.class, null, ActivityHelper.EXTRA_TOUR_DATASET_UUID, DataProvider.getStaedelUuid());
                } else {
                    ActivityHelper.startCdvActivity(MyToursActivity.this, BrowseDatasetsActivity.class, SelectDatasetObjectsActivity.class, null, null);
                }
            }
        });
    }

    private void updateDashboard(List<Tour> tours) {
        LinearLayout dashBoardParent = (findViewById(R.id.dash_board_parent));
        dashBoardParent.removeAllViews();
        View dashBoard = ViewHelper.createDashBoard(this, 2, tours);
        dashBoardParent.addView(dashBoard);
    }
}
