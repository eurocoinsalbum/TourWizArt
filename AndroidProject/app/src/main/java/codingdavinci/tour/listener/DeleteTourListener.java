package codingdavinci.tour.listener;

import codingdavinci.tour.model.Tour;

public interface DeleteTourListener {
    void tourDeleted(Tour tour);
    void errorOccured();
}
