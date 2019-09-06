package codingdavinci.tour.activity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import codingdavinci.tour.R;
import codingdavinci.tour.filter.TourFilter;
import codingdavinci.tour.listener.LoadToursListener;
import codingdavinci.tour.listener.UuidListItemListener;
import codingdavinci.tour.manager.DataProvider;
import codingdavinci.tour.model.Tour;
import codingdavinci.tour.model.UuidListItem;
import codingdavinci.tour.util.ActivityHelper;
import codingdavinci.tour.util.FilterHelper;
import codingdavinci.tour.util.TourHelper;
import codingdavinci.tour.view.RecyclerViewListAdapter;

public class BrowseToursActivity extends CdvActivity implements UuidListItemListener {
    private ImageButton btnFilterTours;
    private RecyclerView listTours;

    private UUID tourDatasetUuid;
    private RecyclerViewListAdapter rowAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rowAdapter = new RecyclerViewListAdapter(BrowseToursActivity.this, new ArrayList(), R.layout.generic_list_item, BrowseToursActivity.this);
        setContentView(R.layout.activity_browse_tours);
        initUiObjects();
        tourDatasetUuid = ActivityHelper.getTourDatasetUuid(getIntent());
        Log.i("cdv", "TourDataSet UUID: " + tourDatasetUuid.toString());
        buildScreen();
    }

    protected void onStart() {
        super.onStart();
        loadTours();
    }

    @Override
    protected void initUiObjects() {
        buildActionBar();
        // Button filter tours
        btnFilterTours = findViewById(R.id.btnFilter);
        btnFilterTours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityHelper.startCdvActivity(BrowseToursActivity.this, FilterToursActivity.class);
            }
        });

        // tours
        listTours = findViewById(R.id.listViewTours);
        listTours.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        listTours.setHasFixedSize(true);
        listTours.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void buildScreen() {
        listTours.setAdapter(rowAdapter);
        loadTours();
    }

    @Override
    public void onClick(int position, UuidListItem uuidListItem) {
        ActivityHelper.startCdvActivity(this, TourDetailsActivity.class, ActivityHelper.EXTRA_TOUR_UUID, uuidListItem.getUuid());
    }

    private void loadTours() {
        DataProvider.getAvailableTours(tourDatasetUuid, new LoadToursListener() {
            @Override
            public void toursLoaded(@NonNull List<Tour> tours) {
                TourFilter filter = new TourFilter(PreferenceManager.getDefaultSharedPreferences(BrowseToursActivity.this));

                List<Tour> filteredTours = FilterHelper.filter(tours, filter);
                Collections.sort(filteredTours, TourHelper.createSortByTitleComparator());

                rowAdapter.setItems(filteredTours);
            }
        });
    }
}
