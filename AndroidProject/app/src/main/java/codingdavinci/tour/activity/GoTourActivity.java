package codingdavinci.tour.activity;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import codingdavinci.tour.R;
import codingdavinci.tour.listener.LoadImageListener;
import codingdavinci.tour.listener.LoadTourListener;
import codingdavinci.tour.listener.SaveTourListener;
import codingdavinci.tour.manager.DataProvider;
import codingdavinci.tour.manager.NoteManager;
import codingdavinci.tour.model.DatasetObject;
import codingdavinci.tour.model.GpsMapLocation;
import codingdavinci.tour.model.Tour;
import codingdavinci.tour.util.ActivityHelper;
import codingdavinci.tour.util.LocationHelper;
import codingdavinci.tour.util.TourHelper;
import codingdavinci.tour.util.ViewHelper;
import codingdavinci.tour.view.NavigationButton;

public class GoTourActivity extends CdvActivity {

    private LinearLayout navigationBar;
    private NavigationButton btnPublish;
    private NavigationButton btnCart;
    private NavigationButton btnDraft;
    private NavigationButton btnDelete;

    private ImageButton btnPrevious;
    private ImageButton btnNext;
    private ImageView imgCurrentObject;
    private TextView txtDescripion;
    private TextView txtTitle;
    private Tour tour;
    private int currentPositionIndex = 0;
    private DatasetObject currentObject;
    private MapNavigationFragment mapNavigationFragment;
    private RatingBar objectRatingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_tour);
        initUiObjects();
    }

    @Override
    protected void onStart() {
        super.onStart();
        UUID tourUuid = ActivityHelper.getTourUuid(getIntent());
        Log.i("cdv", "GoTour UUID: " + tourUuid.toString());
        DataProvider.loadTour(tourUuid, new LoadTourListener() {
            @Override
            public void tourLoaded(@NonNull Tour tour) {
                // the list will contain exactly one element!
                GoTourActivity.this.tour = tour;
                buildScreen();
            }
        });
    }

    @Override
    protected void initUiObjects() {
        buildActionBar();
        // previous button
        btnPrevious = findViewById(R.id.btnPrevious);
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPositionIndex == 0) {
                    return;
                }
                currentPositionIndex--;
                updateObjectViews();
            }
        });

        // next button
        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPositionIndex == tour.getDatasetObjects().size() - 1) {
                    return;
                }
                currentPositionIndex++;
                updateObjectViews();
            }
        });

        imgCurrentObject = findViewById(R.id.imageCurrentObject);
        txtTitle = findViewById(R.id.txtTitle);
        txtDescripion = findViewById(R.id.txtDescription);
        objectRatingBar = findViewById(R.id.itemRatingBar);
        mapNavigationFragment = (MapNavigationFragment)getSupportFragmentManager().findFragmentById(R.id.mapNavigation);

        // Navigation buttons for draft mode (will not be visible in 'non draft'-mode)
        navigationBar = ViewHelper.createNavigationBar(this, R.id.main);
        btnPublish = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_publish, R.string.nav_publish_tour);
        btnCart = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_cart_full, R.string.nav_tour_route);
        btnDraft = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_draft, R.string.nav_save_as_draft);
        btnDelete = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_delete, R.string.nav_delete_tour);
    }

    @Override
    protected void buildScreen() {
        // show/hide navigation bar and related elements
        boolean tourIsActive = tour.getStatus().equals(Tour.Status.ACTIVE);
        navigationBar.setVisibility(tourIsActive ? View.GONE : View.VISIBLE);
        objectRatingBar.setVisibility(tourIsActive ? View.VISIBLE : View.GONE);

        buildMap();

        // button publish
        btnPublish.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publish();
            }
        });

        // button cart
        btnCart.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityHelper.startCdvActivity(GoTourActivity.this, TourCartActivity.class, null, ActivityHelper.EXTRA_TOUR_UUID, tour.getTourUuid());
            }
        });
        btnCart.textView.setText(getText(R.string.nav_tour_route) + " (" + tour.getDatasetObjects().size() + ")");

        btnDraft.rootView.setOnClickListener(TourHelper.createSaveDraftListener(this, tour, false));
        btnDelete.rootView.setOnClickListener(TourHelper.createDeleteListener(this, MainActivity.class, tour));

        // rating
        objectRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                DataProvider.setObjectRating(currentObject, new Float(rating).intValue());
            }
        });

        updateObjectViews();
    }

    private void updateObjectViews() {
        currentObject = tour.getDatasetObjects().get(currentPositionIndex);
        Integer rating = DataProvider.getUserRating(currentObject.getDatasetObjectUuid());
        if (rating != null) {
            objectRatingBar.setRating(rating);
        } else {
            objectRatingBar.setRating(0);
        }

        loadCurrentObjectImage();
        updatePosition();
        updateNextTarget();

        txtTitle.setText(currentObject.getTitle());
        txtDescripion.setText(currentObject.getDescription());
        btnPrevious.setVisibility(currentPositionIndex == 0 ? View.INVISIBLE : View.VISIBLE);
        btnNext.setVisibility(currentPositionIndex == tour.getDatasetObjects().size() - 1 ? View.INVISIBLE : View.VISIBLE);
    }

    private void buildMap() {
        mapNavigationFragment.setCdvActivity(this);
        mapNavigationFragment.setTourDataset(tour.getTourDataset());
        if (!tour.getTourDataset().getTourDatasetMaps().isEmpty()) {
            try {
                mapNavigationFragment.switchToMapIndex(0);
            } catch (IOException e) {
                NoteManager.showError(this, R.string.internal_error);
            }
        }
    }

    private void updatePosition() {
        switch (tour.getTourDataset().getLocationType()) {
            case PIXEL: {
                ArrayList<Point> points = LocationHelper.convertToPoint(currentObject.getLocation());
                mapNavigationFragment.setCurrentPixelPositions(points);
                break;
            }
        }
    }

    private void updateNextTarget() {
        if (currentPositionIndex < tour.getDatasetObjects().size() - 1) {
            mapNavigationFragment.setNumberOfTargets(1);
            String targetLocation = tour.getDatasetObjects().get(currentPositionIndex + 1).getLocation();
            switch (tour.getTourDataset().getLocationType()) {
                case PIXEL:
                case GPSMAP: {
                    ArrayList<Point> points = LocationHelper.convertToPoint(targetLocation);
                    mapNavigationFragment.setTargetPixelPosition(0, points);
                    break;
                }
            }
        } else {
            mapNavigationFragment.setNumberOfTargets(0);
        }
    }

    private void loadCurrentObjectImage() {
        final int positionIndexToLoad = currentPositionIndex;
        try {
            DataProvider.getPreviewImage(this, currentObject, DatasetObject.ICON_WIDTH, new LoadImageListener() {
                @Override
                public void imageLoaded(Bitmap bitmap) {
                    // check if position is still the expected one or if the user continued
                    if (positionIndexToLoad != currentPositionIndex) {
                        return;
                    }
                    imgCurrentObject.setImageBitmap(bitmap);
                }
            });
        } catch (IOException e) {
            imgCurrentObject.setImageResource(R.drawable.icons8_draft);
        }
    }

    private void publish() {
        if (!checkTourForPublishing()) {
            return;
        }
        // activate tour
        tour.setStatus(Tour.Status.ACTIVE);

        DataProvider.saveTour(tour, false, new SaveTourListener() {
            @Override
            public void tourSaved(Tour tour) {
                ActivityHelper.startCdvActivity(GoTourActivity.this, MainActivity.class, ActivityHelper.EXTRA_TOUR_UUID, tour.getTourUuid());
            }

            @Override
            public void errorOccured() {
                NoteManager.showError(GoTourActivity.this, R.string.internal_error);
            }
        });
    }

    private boolean checkTourForPublishing() {
        List<Integer> errorResourceIds = new ArrayList<>();
        // title not empty
        if (tour.getTitle().isEmpty()) {
            errorResourceIds.add(R.string.enter_tour_title);
        }
        // title not default value
        if (tour.getTitle().equalsIgnoreCase(getText(R.string.tour_default_title).toString())) {
            errorResourceIds.add(R.string.enter_tour_title);
        }
        // description not empty
        if (tour.getDescription().isEmpty()) {
            errorResourceIds.add(R.string.enter_tour_description);
        }
        // tour not empty
        if (tour.getDatasetObjects().isEmpty()) {
            errorResourceIds.add(R.string.enter_tour_objects);
        }

        if (!errorResourceIds.isEmpty()) {
            NoteManager.showErrors(this, errorResourceIds);
            return false;
        }
        return true;
    }
}
