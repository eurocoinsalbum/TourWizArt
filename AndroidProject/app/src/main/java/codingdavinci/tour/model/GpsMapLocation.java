package codingdavinci.tour.model;

import android.graphics.Rect;
import android.graphics.RectF;

public class GpsMapLocation {
    private Rect locPixel;
    private RectF locGps;

    public Rect getLocPixel() {
        return locPixel;
    }

    public void setLocPixel(Rect locPixel) {
        this.locPixel = locPixel;
    }

    public RectF getLocGps() {
        return locGps;
    }

    public void setLocGps(RectF locGps) {
        this.locGps = locGps;
    }
}
