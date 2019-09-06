package codingdavinci.tour.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import codingdavinci.tour.R;
import codingdavinci.tour.listener.LoadTourDatasetListener;
import codingdavinci.tour.listener.UuidListItemListener;
import codingdavinci.tour.manager.DataProvider;
import codingdavinci.tour.model.TourDataset;
import codingdavinci.tour.model.UuidListItem;
import codingdavinci.tour.util.ActivityHelper;
import codingdavinci.tour.util.TourHelper;
import codingdavinci.tour.view.RecyclerViewListAdapter;

public class BrowseDatasetsActivity extends CdvActivity implements UuidListItemListener {
    private RecyclerView listDatasets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_datasets);
        initUiObjects();
        buildScreen();
    }

    @Override
    protected void initUiObjects() {
        buildActionBar();
        // datasets
        listDatasets = findViewById(R.id.listViewDatasets);
        listDatasets.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        listDatasets.setHasFixedSize(true);
        listDatasets.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void buildScreen() {
        DataProvider.getAvailableDatasets(new LoadTourDatasetListener() {
            @Override
            public void tourDatasetsLoaded(@NonNull Collection<TourDataset> tourDatasets) {
                ArrayList<TourDataset> tourDatasetList = new ArrayList<>(tourDatasets.size());
                tourDatasetList.addAll(tourDatasets);
                Collections.sort(tourDatasetList, TourHelper.createSortByTitleComparator());

                RecyclerViewListAdapter rowAdapter = new RecyclerViewListAdapter(BrowseDatasetsActivity.this, tourDatasetList, R.layout.generic_list_item, BrowseDatasetsActivity.this);
                listDatasets.setAdapter(rowAdapter);
            }
        });
    }

    @Override
    public void onClick(int position, UuidListItem uuidListItem) {
        // start activity which was given in intent. On error fall back to MainActivity
        Class<?> followUp;
        try {
            followUp = ActivityHelper.getFollowUp(getIntent());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            followUp = MainActivity.class;
        }
        ActivityHelper.startCdvActivity(this, followUp, null, ActivityHelper.EXTRA_TOUR_DATASET_UUID, uuidListItem.getUuid());
    }
}
