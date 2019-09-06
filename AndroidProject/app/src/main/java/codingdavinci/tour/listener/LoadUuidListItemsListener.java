package codingdavinci.tour.listener;

import android.support.annotation.NonNull;

import java.util.List;

import codingdavinci.tour.model.UuidListItem;

public interface LoadUuidListItemsListener {
    void uuidListItemsLoaded(@NonNull List<? extends UuidListItem> uuidListItems);
}
