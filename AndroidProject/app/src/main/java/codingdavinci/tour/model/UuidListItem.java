package codingdavinci.tour.model;

import java.io.IOException;
import java.util.UUID;

import codingdavinci.tour.activity.CdvActivity;
import codingdavinci.tour.listener.LoadImageListener;

public interface UuidListItem {
    int ICON_WIDTH = 200;

    UUID getUuid();
    String getTitle();
    String getDescription();
    void getIcon(CdvActivity cdvActivity, LoadImageListener loadImageListener) throws IOException;

    boolean hasIcon();
}
