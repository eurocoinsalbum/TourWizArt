package codingdavinci.tour.util;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;

import codingdavinci.tour.model.GpsMapLocation;
import codingdavinci.tour.model.GpsMapPoint;
import codingdavinci.tour.model.TourDataset;
import codingdavinci.tour.model.TourDatasetMap;

public class LocationHelper {
    public static ArrayList<Point> convertToPoint(String location) {
        String[] locationTokens = location.split(";");
        ArrayList<Point> points = new ArrayList<>(locationTokens.length);
        for (String coordinate : locationTokens) {
            String[] coordinateTokens = coordinate.split(",");
            int mapIndex = Integer.parseInt(coordinateTokens[0]);
            int x = Integer.parseInt(coordinateTokens[1]) * 3;
            int y = (int)(Integer.parseInt(coordinateTokens[2]) * 1.5);
            points.add(mapIndex, new Point(x, y));
        }
        return points;
    }

    public static GpsMapLocation parseGpsMapLocation(String gpsMapLocationString) {
        GpsMapLocation gpsMapLocation = new GpsMapLocation();
        String[] gpsMapPoints = gpsMapLocationString.split(";");
        GpsMapPoint gpsMapPoint1 = parseGpsMapPoint(gpsMapPoints[0]);
        GpsMapPoint gpsMapPoint2 = parseGpsMapPoint(gpsMapPoints[0]);
        gpsMapLocation.setLocGps(new RectF(gpsMapPoint1.pointF.x, gpsMapPoint1.pointF.y, gpsMapPoint2.pointF.x, gpsMapPoint2.pointF.y));
        gpsMapLocation.setLocPixel(new Rect(gpsMapPoint1.point.x, gpsMapPoint1.point.y, gpsMapPoint2.point.x, gpsMapPoint2.point.y));
        return gpsMapLocation;
    }

    private static GpsMapPoint parseGpsMapPoint(String gpsMapPointString) {
        String[] points = gpsMapPointString.split(",");
        GpsMapPoint gpsMapPoint = new GpsMapPoint();
        gpsMapPoint.pointF = new PointF(Float.parseFloat(points[0]), Float.parseFloat(points[1]));
        gpsMapPoint.point = new Point(Integer.parseInt(points[2]), Integer.parseInt(points[3]));
        return gpsMapPoint;
    }

    public static Point convertGpsToPixel(GpsMapLocation gpsMapLocation, PointF gps) {
        Point pixel = new Point();
        RectF locGps = gpsMapLocation.getLocGps();
        Rect locPix = gpsMapLocation.getLocPixel();
        pixel.x = calculatePixel(locGps.left, locGps.right, locPix.left, locPix.right, gps.x);
        pixel.y = calculatePixel(locGps.top, locGps.bottom, locPix.top, locPix.bottom, gps.y);
        return pixel;
    }

    private static Integer calculatePixel(float firstGps, float secondGps, int firstPixel, int secondPixel, float sourceGps) {
        if (sourceGps < firstGps) {
            return null;
        }
        float percentage = (sourceGps - firstGps) / (secondGps - firstGps);
        if (percentage > 1.0) {
            return null;
        }
        return (int)((secondPixel - firstPixel) * percentage);
    }
}
