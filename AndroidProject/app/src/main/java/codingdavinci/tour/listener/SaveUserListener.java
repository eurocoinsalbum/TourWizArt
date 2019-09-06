package codingdavinci.tour.listener;

import codingdavinci.tour.model.User;

public interface SaveUserListener {
    void userSaved(User.Role role);
}
