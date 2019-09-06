package codingdavinci.tour.listener;

import android.support.annotation.NonNull;

import java.util.List;

import codingdavinci.tour.model.Tour;

public interface LoadToursListener {
    void toursLoaded(@NonNull List<Tour> tours);
}
