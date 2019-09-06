package codingdavinci.tour.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.UUID;

import codingdavinci.tour.R;
import codingdavinci.tour.listener.LoadTourListener;
import codingdavinci.tour.listener.SaveTourListener;
import codingdavinci.tour.listener.UuidListItemListener;
import codingdavinci.tour.manager.DataProvider;
import codingdavinci.tour.manager.NoteManager;
import codingdavinci.tour.model.Tour;
import codingdavinci.tour.model.UuidListItem;
import codingdavinci.tour.util.ActivityHelper;
import codingdavinci.tour.util.TourHelper;
import codingdavinci.tour.util.ViewHelper;
import codingdavinci.tour.view.NavigationButton;
import codingdavinci.tour.view.RecyclerViewListAdapter;

public class TourDetailsActivity extends CdvActivity {

    private TextView txtTourTitle;
    private TextView txtTourDescription;
    private TextView txtDatasetObjectTitle;
    private TextView txtDatasetObjectDescription;
    RecyclerView rcyPreviewGallery;
    private Tour tour;
    private RecyclerViewListAdapter rcyPreviewAdapter;

    private NavigationButton btnStartNow;
    private NavigationButton btnCustomize;
    private NavigationButton btnEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_details);
        initUiObjects();
        UUID tourUuid = ActivityHelper.getTourUuid(getIntent());
        Log.i("cdv", "Tour UUID: " + tourUuid.toString());
        DataProvider.loadTour(tourUuid, new LoadTourListener() {
            @Override
            public void tourLoaded(@NonNull Tour tour) {
                // the list will contain exactly one element!
                TourDetailsActivity.this.tour = tour;
                buildScreen();
            }
        });
    }

    @Override
    protected void initUiObjects() {
        buildActionBar();
        txtTourTitle = findViewById(R.id.tour_title);
        txtTourDescription = findViewById(R.id.tour_description);
        txtDatasetObjectTitle = findViewById(R.id.dataset_object_title);
        txtDatasetObjectDescription = findViewById(R.id.dataset_object_description);

        // image list
        rcyPreviewGallery = findViewById(R.id.image_previews);
        rcyPreviewGallery.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL));
        rcyPreviewGallery.setHasFixedSize(true);
        rcyPreviewGallery.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Navigation buttons
        LinearLayout navigationBar = ViewHelper.createNavigationBar(this, R.id.main);
        btnStartNow = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_go, R.string.nav_start_tour_now);
        btnCustomize = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_copy, R.string.nav_customize_tour);
        btnEdit = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_edit_tour, R.string.nav_edit_tour);
        btnEdit.rootView.setVisibility(View.GONE);
    }
    @Override
    protected void buildScreen() {
        txtTourTitle.setText(tour.getTitle());
        txtTourDescription.setText(tour.getDescription());

        rcyPreviewAdapter = new RecyclerViewListAdapter(this, tour.getDatasetObjects(), R.layout.generic_icon_item, new UuidListItemListener() {
            @Override
            public void onClick(int position, UuidListItem uuidListItem) {
                showDatasetObjectDetails(position, uuidListItem);
            }
        });
        rcyPreviewGallery.setAdapter(rcyPreviewAdapter);

        // select first item
        if (!tour.getDatasetObjects().isEmpty()) {
            showDatasetObjectDetails(0, tour.getDatasetObjects().get(0));
        }

        // start tour now
        if (tour.getStatus() == Tour.Status.DRAFT) {
            btnStartNow.textView.setText(R.string.nav_test_tour);
        }
        btnStartNow.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityHelper.startCdvActivity(TourDetailsActivity.this, GoTourActivity.class, ActivityHelper.EXTRA_TOUR_UUID, tour.getTourUuid());
            }
        });

        // customize
        btnCustomize.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Tour clonedTour = TourHelper.createCopyAsCustomized(TourDetailsActivity.this, tour);
                DataProvider.saveTour(clonedTour, true, new SaveTourListener() {
                    @Override
                    public void tourSaved(Tour tour) {
                        ActivityHelper.startCdvActivity(TourDetailsActivity.this, TourCartActivity.class, CreateTourActivity.class, ActivityHelper.EXTRA_TOUR_UUID, clonedTour.getTourUuid());
                    }

                    @Override
                    public void errorOccured() {
                        NoteManager.showError(TourDetailsActivity.this, R.string.internal_error);
                    }
                });
            }
        });

        // Button edit
        if (TourHelper.isUserAllowedToEditTour(tour)) {
            btnEdit.rootView.setVisibility(View.VISIBLE);
            btnEdit.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tour.setStatus(Tour.Status.DRAFT);
                    DataProvider.saveTour(tour, false, new SaveTourListener() {
                        @Override
                        public void tourSaved(Tour tour) {
                            ActivityHelper.startCdvActivity(TourDetailsActivity.this, TourCartActivity.class, ActivityHelper.EXTRA_TOUR_UUID, tour.getTourUuid());
                        }

                        @Override
                        public void errorOccured() {
                            NoteManager.showError(TourDetailsActivity.this, R.string.internal_error);
                        }
                    });
                }
            });
        }
    }

    private void showDatasetObjectDetails(int position, UuidListItem uuidListItem) {
        rcyPreviewAdapter.setSelectedPosition(position);
        txtDatasetObjectTitle.setText(uuidListItem.getTitle());
        txtDatasetObjectDescription.setText(uuidListItem.getDescription());
    }
}
