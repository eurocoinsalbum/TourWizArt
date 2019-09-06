package codingdavinci.tour.listener;

import android.net.Uri;

import java.io.File;

public interface PhotoListener {
    void addPath(File file);
    void imageTaken(int resultCode);
    void imageLoad();
}
