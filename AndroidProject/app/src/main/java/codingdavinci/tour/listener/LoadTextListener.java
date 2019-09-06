package codingdavinci.tour.listener;

import java.util.Map;

public interface LoadTextListener {
    void textsLoaded(boolean success, Map<Integer, String> texts);
}
