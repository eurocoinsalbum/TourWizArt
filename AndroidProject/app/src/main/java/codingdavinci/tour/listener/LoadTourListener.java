package codingdavinci.tour.listener;

import android.support.annotation.NonNull;

import codingdavinci.tour.model.Tour;


public interface LoadTourListener {
    void tourLoaded(@NonNull Tour tour);
}
