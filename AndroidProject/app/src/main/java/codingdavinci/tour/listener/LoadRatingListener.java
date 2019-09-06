package codingdavinci.tour.listener;

import java.util.Map;
import java.util.UUID;

public interface LoadRatingListener {
    void ratingsLoaded(Map<UUID, Integer> objectRatings);
}
