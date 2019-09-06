package codingdavinci.tour.listener;

import android.support.annotation.NonNull;

import java.util.Collection;

import codingdavinci.tour.model.TourDataset;

public interface LoadTourDatasetListener {
    void tourDatasetsLoaded(@NonNull Collection<TourDataset> tourDatasets);
}
