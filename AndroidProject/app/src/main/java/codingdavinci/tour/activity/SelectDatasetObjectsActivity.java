package codingdavinci.tour.activity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.autofill.Dataset;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import codingdavinci.tour.R;
import codingdavinci.tour.filter.DatasetObjectFilter;
import codingdavinci.tour.filter.Filter;
import codingdavinci.tour.fragment.GenericFilterLineFragment;
import codingdavinci.tour.listener.LoadTourDatasetListener;
import codingdavinci.tour.listener.LoadTourListener;
import codingdavinci.tour.listener.SaveTourListener;
import codingdavinci.tour.listener.UuidListItemListener;
import codingdavinci.tour.manager.DataProvider;
import codingdavinci.tour.manager.NoteManager;
import codingdavinci.tour.model.DatasetObject;
import codingdavinci.tour.model.Tour;
import codingdavinci.tour.model.TourDataset;
import codingdavinci.tour.model.UuidListItem;
import codingdavinci.tour.util.ActivityHelper;
import codingdavinci.tour.util.FilterHelper;
import codingdavinci.tour.util.TourHelper;
import codingdavinci.tour.util.ViewHelper;
import codingdavinci.tour.view.NavigationButton;
import codingdavinci.tour.view.RecyclerViewListAdapter;

public class SelectDatasetObjectsActivity extends CdvActivity implements UuidListItemListener {
    private GenericFilterLineFragment filterLineFragment;
    private DatasetObjectFilter customFilter;
    private HiddenFilter hiddenFilter;

    private Tour tour;
    private NavigationButton btnCart;
    private NavigationButton btnDraft;
    private RecyclerView listDatasetObjects;
    private RecyclerViewListAdapter rowAdapter;

    private class SwipeCallback extends ItemTouchHelper.SimpleCallback {
        public SwipeCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            DatasetObject item = (DatasetObject) rowAdapter.getUuidListItem(viewHolder.getAdapterPosition());

            if (direction == ItemTouchHelper.RIGHT) {
                tour.addDatasetObject(item);
                updateBasketContent();
            } else {
                hiddenFilter.add(item);
                filterLineFragment.setHiddenFilteredNumber(hiddenFilter.size());
            }

            rowAdapter.removeItem(viewHolder.getAdapterPosition());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_objects);
        initUiObjects();

        // check if tourUuid was provided (editing) or if a new tour should be created
        UUID tourUuid = ActivityHelper.getTourUuid(getIntent());
        if (tourUuid == null) {
            TourDataset tourDataset = DataProvider.getTourDataset(ActivityHelper.getTourDatasetUuid(getIntent()));
            SelectDatasetObjectsActivity.this.tour = TourHelper.createNewTour(this, tourDataset);
            Log.i("cdv", "New Tour UUID: " + tour.getTourUuid().toString());
            buildScreen();
        } else {
            Log.i("cdv", "Tour UUID: " + tourUuid.toString());
            DataProvider.loadTour(tourUuid, new LoadTourListener() {
                @Override
                public void tourLoaded(@NonNull Tour tour) {
                    SelectDatasetObjectsActivity.this.tour = TourHelper.createCopy(tour, false);
                    buildScreen();
                }
            });
        }
    }

    protected void onStart() {
        super.onStart();
        buildScreen();
    }

    @Override
    protected void initUiObjects() {
        buildActionBar();

        hiddenFilter = new HiddenFilter();
        customFilter = new DatasetObjectFilter(PreferenceManager.getDefaultSharedPreferences(SelectDatasetObjectsActivity.this));

        filterLineFragment = (GenericFilterLineFragment)
                getSupportFragmentManager().findFragmentById(R.id.filterline_fragment);

        // Button filter
        filterLineFragment.setFilterButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityHelper.startCdvActivity(SelectDatasetObjectsActivity.this, FilterDatasetObjectActivity.class);
            }
        });

        // Button remove custom filter
        filterLineFragment.setCustomButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customFilter.clear();   // clears the preferences used by this filter
                buildScreen();
            }
        });

        // Button remove hidden filter
        filterLineFragment.setHiddenButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hiddenFilter.clear();
                buildScreen();
            }
        });

        // Navigation buttons
        LinearLayout navigationBar = ViewHelper.createNavigationBar(this, R.id.main);
        btnCart = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_cart_full, R.string.nav_tour_route);
        btnDraft = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_draft, R.string.nav_save_as_draft);

        listDatasetObjects = findViewById(R.id.listViewDatasetObjects);
        listDatasetObjects.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        listDatasetObjects.setHasFixedSize(true);
        listDatasetObjects.setLayoutManager(new LinearLayoutManager(this));

        new ItemTouchHelper(new SwipeCallback()).attachToRecyclerView(listDatasetObjects);
    }

    @Override
    protected void buildScreen() {
        btnCart.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTourAndContinue();
            }
        });

        btnDraft.rootView.setOnClickListener(TourHelper.createSaveDraftListener(this, tour, true));

        DataProvider.getDatasetObjects(tour.getTourDataset(), new LoadTourDatasetListener() {
            @Override
            public void tourDatasetsLoaded(@NonNull Collection<TourDataset> tourDatasets) {
                if (tourDatasets.size() == 0) {
                    Log.e("cdv", "Empty tourDatasets");
                    return;
                }

                Collection<DatasetObject> origDatasetObjects = tourDatasets.iterator().next().getDatasetObjects();
                // add all objects to the available objects which have not been already added to the tour (in case of editing tour)
                List<DatasetObject> datasetObjects = new ArrayList<>(origDatasetObjects.size());
                for (DatasetObject datasetObject : origDatasetObjects) {
                    if (!tour.contains(datasetObject)) {
                        datasetObjects.add(datasetObject);
                    }
                }

                customFilter.refresh();

                List<DatasetObject> filteredDatasetObject = FilterHelper.filter(datasetObjects, customFilter);
                filterLineFragment.setCustomFilteredNumber(datasetObjects.size() - filteredDatasetObject.size());
                filterLineFragment.setHiddenFilteredNumber(hiddenFilter.size());

                filteredDatasetObject = FilterHelper.filter(filteredDatasetObject, hiddenFilter);

                Collections.sort(filteredDatasetObject, TourHelper.createSortByTitleComparator());

                rowAdapter = new RecyclerViewListAdapter(SelectDatasetObjectsActivity.this, filteredDatasetObject, R.layout.generic_list_item, SelectDatasetObjectsActivity.this);
                listDatasetObjects.setAdapter(rowAdapter);
            }
        });

        updateBasketContent();
    }

    private void updateBasketContent() {
        if (tour.getDatasetObjects().isEmpty()) {
            btnCart.imageButton.setImageResource(R.drawable.icons8_cart_empty);
        } else {
            btnCart.imageButton.setImageResource(R.drawable.icons8_cart_full_yellow);
        }
        btnCart.textView.setText(getText(R.string.nav_tour_route) + "(" + tour.getDatasetObjects().size() + ")");
    }

    private void saveTourAndContinue() {
        if (tour.getDatasetObjects().isEmpty()) {
            NoteManager.showError(this, R.string.enter_tour_objects);
            return;
        }

        DataProvider.saveTour(tour, true, new SaveTourListener() {
            @Override
            public void tourSaved(Tour tour) {
                ActivityHelper.startCdvActivity(SelectDatasetObjectsActivity.this, TourCartActivity.class, null, ActivityHelper.EXTRA_TOUR_UUID, tour.getTourUuid());
            }

            @Override
            public void errorOccured() {
                NoteManager.showError(SelectDatasetObjectsActivity.this, R.string.internal_error);
            }
        });
    }

    @Override
    public void onClick(int position, UuidListItem uuidListItem) {
        ActivityHelper.checkShowStaedelWebsiteHack(this, tour.getTourDataset(), ((DatasetObject)uuidListItem));
    }

    @Override
    public void onBackPressed() {
        // TODO ask user to save or delete the tour
    }

    private class HiddenFilter implements Filter<DatasetObject> {
        Map<UUID, DatasetObject> hiddenMap = new HashMap<>();

        @Override
        public boolean accept(DatasetObject datasetObject) {
            return !hiddenMap.containsKey(datasetObject.getUuid());
        }

        public void add(DatasetObject datasetObject) {
            hiddenMap.put(datasetObject.getUuid(), datasetObject);
        }

        public void clear() {
            hiddenMap.clear();
        }

        @Override
        public void refresh() {
        }

        public int size() {
            return hiddenMap.size();
        }
    }
}
