package codingdavinci.tour.listener;

import codingdavinci.tour.model.Tour;

public interface SaveTourListener {
    void tourSaved(Tour tour);
    void errorOccured();
}
