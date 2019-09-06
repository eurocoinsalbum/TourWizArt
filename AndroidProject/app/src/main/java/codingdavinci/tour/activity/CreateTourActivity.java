package codingdavinci.tour.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.UUID;

import codingdavinci.tour.R;
import codingdavinci.tour.listener.LoadTourListener;
import codingdavinci.tour.listener.SaveTourListener;
import codingdavinci.tour.manager.DataProvider;
import codingdavinci.tour.manager.NoteManager;
import codingdavinci.tour.model.Tour;
import codingdavinci.tour.util.ActivityHelper;
import codingdavinci.tour.util.TourHelper;
import codingdavinci.tour.util.ViewHelper;
import codingdavinci.tour.view.NavigationButton;

public class CreateTourActivity extends CdvActivity {
    private Tour tour;
    private NavigationButton btnTestGoTour;
    private NavigationButton btnCart;
    private NavigationButton btnDraft;
    private NavigationButton btnDelete;
    private EditText txtTitle;
    private EditText txtDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tour);
        initUiObjects();

        // check if tourUuid was provided (editing) or if a new tour will be created
        UUID tourUuid = ActivityHelper.getTourUuid(getIntent());
        Log.i("cdv", "Tour UUID: " + tourUuid.toString());
        DataProvider.loadTour(tourUuid, new LoadTourListener() {
            @Override
            public void tourLoaded(@NonNull Tour tour) {
                CreateTourActivity.this.tour = TourHelper.createCopy(tour, false);
                buildScreen();
            }
        });
    }

    @Override
    protected void initUiObjects() {
        buildActionBar();
        txtTitle = findViewById(R.id.tour_title);
        txtDescription = findViewById(R.id.tour_description);

        // Navigation buttons
        LinearLayout navigationBar = ViewHelper.createNavigationBar(this, R.id.main);
        btnTestGoTour = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_go, R.string.nav_test_tour);
        btnCart = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_cart_full, R.string.nav_tour_route);
        btnDraft = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_draft, R.string.nav_save_as_draft);
        btnDelete = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_delete, R.string.nav_delete_tour);
    }

    private void testGoTour() {
        updateModelFromUI();

        DataProvider.saveTour(tour, false, new SaveTourListener() {
            @Override
            public void tourSaved(Tour tour) {
                ActivityHelper.startCdvActivity(CreateTourActivity.this, GoTourActivity.class, ActivityHelper.EXTRA_TOUR_UUID, tour.getTourUuid());
            }

            @Override
            public void errorOccured() {
                NoteManager.showError(CreateTourActivity.this, R.string.internal_error);
            }
        });
    }

    private void saveTourAndBack() {
        updateModelFromUI();

        DataProvider.saveTour(tour, false, new SaveTourListener() {
            @Override
            public void tourSaved(Tour tour) {
                ActivityHelper.startCdvActivity(CreateTourActivity.this, TourCartActivity.class, null, ActivityHelper.EXTRA_TOUR_UUID, tour.getTourUuid());
            }

            @Override
            public void errorOccured() {
                NoteManager.showError(CreateTourActivity.this, R.string.internal_error);
            }
        });
    }

    @Override
    public void updateModelFromUI() {
        tour.setTitle(txtTitle.getText().toString().trim());
        tour.setDescription(txtDescription.getText().toString());
    }

    @Override
    protected void buildScreen() {
        txtTitle.setText(tour.getTitle());
        txtDescription.setText(tour.getDescription());

        btnTestGoTour.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testGoTour();
            }
        });

        btnCart.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTourAndBack();
            }
        });
        btnCart.textView.setText(getText(R.string.nav_tour_route) + " (" + tour.getDatasetObjects().size() + ")");

        btnDraft.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateModelFromUI();
                DataProvider.saveTour(tour, false, new SaveTourListener() {
                    @Override
                    public void tourSaved(Tour tour) {
                        ActivityHelper.startCdvActivity(CreateTourActivity.this, MyToursActivity.class, ActivityHelper.EXTRA_TOUR_UUID, tour.getTourUuid());
                    }

                    @Override
                    public void errorOccured() {
                        NoteManager.showError(CreateTourActivity.this, R.string.internal_error);
                    }
                });
            }
        });

        btnDelete.rootView.setOnClickListener(TourHelper.createDeleteListener(this, MainActivity.class, tour));
    }

    @Override
    public void onBackPressed() {
        saveTourAndBack();
    }
}
