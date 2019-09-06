package codingdavinci.tour.listener;

import java.util.List;

import codingdavinci.tour.model.People;

public interface LoadPeopleListener {
    void peopleLoaded(List<People> peopleList);
}
