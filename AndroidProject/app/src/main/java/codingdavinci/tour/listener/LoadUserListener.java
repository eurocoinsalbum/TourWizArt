package codingdavinci.tour.listener;

import java.util.List;

import codingdavinci.tour.model.User;

public interface LoadUserListener {
    void usersLoaded(List<User> users);
}
