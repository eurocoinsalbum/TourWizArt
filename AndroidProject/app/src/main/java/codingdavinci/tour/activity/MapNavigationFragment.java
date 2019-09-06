package codingdavinci.tour.activity;


import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;

import codingdavinci.tour.R;
import codingdavinci.tour.listener.LoadImageListener;
import codingdavinci.tour.listener.ScaleListener;
import codingdavinci.tour.manager.DataProvider;
import codingdavinci.tour.model.GpsMapLocation;
import codingdavinci.tour.model.TourDataset;
import codingdavinci.tour.util.LocationHelper;

public class MapNavigationFragment extends Fragment {
    private class MapPoint {
        public ArrayList<Point> pixels;
        public PointF gps;
    }
    private static final String TAG = "MapNavigationFragment";

    private ViewGroup parentOfTargetPositionViews;
    private ImageView currentPositionView;
    private ArrayList<ImageView> targetPositionViews = new ArrayList<>();
    private ImageView mapView;
    private Bitmap bitmap;
    private ScaleGestureDetector scaleGestureDetector;

    private MapPoint currentPosition = new MapPoint();
    private ArrayList<MapPoint> targetPositions;

    private int currentMapTargetWidth = 1000;
    private CdvActivity cdvActivity;
    private int currentMapIndex = -1;
    private TourDataset tourDataset;

    public MapNavigationFragment() {
        // Required empty public constructor
    }

    public void setCdvActivity(CdvActivity cdvActivity) {
        this.cdvActivity = cdvActivity;
    }

    public void setTourDataset(TourDataset tourDataset) {
        this.tourDataset = tourDataset;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_navigation, container, false);
    }

    public void setNumberOfTargets(int numberOfTargets) {
        targetPositions = new ArrayList<>(numberOfTargets);
        buildScreen();
    }

    private synchronized void buildScreen() {
        Log.i("cdv", "buildScreen");
        // map
        if (mapView != null && bitmap != null) {
            mapView.setImageBitmap(bitmap);
            Log.i("cdv", "set image bitmap (" + bitmap.getWidth() + "x" + bitmap.getHeight());
        }

        // target positions: check if UI and data objects have been initialized already
        if (targetPositions != null && parentOfTargetPositionViews != null) {
            Log.i("cdv", "prepare targets");
            parentOfTargetPositionViews.removeAllViews();
            targetPositionViews = new ArrayList<>(targetPositions.size());

            // add the new targetPositionViews
            for (int i = 0; i < targetPositions.size(); i++) {
                Log.i("cdv", "add target " + i);
                ImageView imageView = (ImageView) getLayoutInflater().inflate(R.layout.map_marker, parentOfTargetPositionViews, true);
                imageView.setImageResource(R.drawable.map_flag);
                targetPositionViews.set(i, imageView);
                parentOfTargetPositionViews.addView(imageView, i);
                updatePositionView(imageView, targetPositions.get(i));
            }
        }
    }

    public void setCurrentPixelPositions(ArrayList<Point> pixelPositions) {
        Log.i("cdv", "set current position on mapIndex 0 to " + pixelPositions.get(0).toString());
        currentPosition.pixels = pixelPositions;
        updateCurrentPositionView();
    }

    public void setTargetPixelPosition(int targetIndex, ArrayList<Point> pixelPositions) {
        Log.i("cdv", "set target " + targetIndex + " position on mapIndex 0 to " + pixelPositions.get(0).toString());
        targetPositions.get(targetIndex).pixels = pixelPositions;
        updatePositionView(targetPositionViews.get(targetIndex), targetPositions.get(targetIndex));
    }

    public void setCurrentGpsPosition(PointF gpsPosition) {
        currentPosition.gps = gpsPosition;
        updateCurrentPositionView();
    }

    public void setTargetGpsPosition(int targetIndex, PointF gpsPosition) {
        targetPositions.get(targetIndex).gps = gpsPosition;
        updatePositionView(targetPositionViews.get(targetIndex), targetPositions.get(targetIndex));
    }

    private void updateCurrentPositionView() {
        updatePositionView(currentPositionView, currentPosition);
    }

    private boolean hasCurrentPosition() {
        return currentPosition.pixels != null || currentPosition.gps != null;
    }

    private void updatePositionView(ImageView positionView, MapPoint mapPoint) {
        if (mapPoint.pixels == null && mapPoint.gps == null) {
            positionView.setVisibility(View.GONE);
            Log.i("cdv", "target invisible");
            return;
        }
        Log.i("cdv", "target visible");
        positionView.setVisibility(View.VISIBLE);

        if (isGpsMap()) {
            Log.i("cdv", "map is GPS");
            if (mapPoint.gps == null) {
                Log.i("cdv", "GPS position unknown");
                return;
            }
            GpsMapLocation gpsMapLocation = tourDataset.getTourDatasetMaps().get(currentMapIndex).getGpsMapLocation();
            mapPoint.pixels.set(currentMapIndex, LocationHelper.convertGpsToPixel(gpsMapLocation, mapPoint.gps));
            Log.i("cdv", "GPS position: " + mapPoint.pixels.get(currentMapIndex).toString());
        }

        if (mapPoint.pixels == null) {
            return;
        }

        PointF pointF = zoomPixel(mapPoint.pixels.get(currentMapIndex));
        positionView.setX(pointF.x);
        positionView.setY(pointF.y);
        positionView.setVisibility(View.VISIBLE);
    }

    private PointF zoomPixel(Point pixel) {
        // TODO check current zoom level
        return new PointF(pixel.x, pixel.y);
    }

    private boolean isGpsMap() {
        return tourDataset.getLocationType().equals(TourDataset.LocationType.GPSMAP);
    }

    public void switchToMapIndex(final int mapIndex) throws IOException {
        Log.i("cdv", "Switching to map index: " + mapIndex);
        currentMapIndex = mapIndex;
        DataProvider.getMap(cdvActivity, tourDataset, mapIndex, currentMapTargetWidth, new LoadImageListener() {
            @Override
            public void imageLoaded(Bitmap bitmap) {
                MapNavigationFragment.this.bitmap = bitmap;
                buildScreen();
            }
        });
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);

        Log.d(TAG, "onViewCreated: started");
        parentOfTargetPositionViews = getActivity().findViewById(R.id.parent_target_positions);
        currentPositionView = getView().findViewById(R.id.current_position);
        currentPositionView.setImageResource(R.drawable.map_position);

        int bitmapWidth = 330;
        int bitmapHeight = 218;

        int screenWidth = view.getWidth();
        int screenHeight= view.getHeight();

        mapView = view.findViewById(R.id.imageMap);
        scaleGestureDetector = new ScaleGestureDetector(cdvActivity, new ScaleListener());

        // set maximum scroll amount (based on center of image)
        int maxX = (int)((bitmapWidth / 2) - (screenWidth / 2));
        int maxY = (int)((bitmapHeight / 2) - (screenHeight / 2));
        // set scroll limits
        final int maxLeft = (maxX * -1);
        final int maxRight = maxX;
        final int maxTop = (maxY * -1);
        final int maxBottom = maxY;

        // set touchlistener
        mapView.setOnTouchListener(new View.OnTouchListener()
        {
            float downX, downY;
            int totalX, totalY;
            int scrollByX, scrollByY;
            public boolean onTouch(View view, MotionEvent event)
            {
                float currentX, currentY;
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX();
                        downY = event.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        currentX = event.getX();
                        currentY = event.getY();
                        scrollByX = (int)(downX - currentX);
                        scrollByY = (int)(downY - currentY);

                        // scrolling to left side of image (pic moving to the right)
                        if (currentX > downX)
                        {
                            if (totalX == maxLeft)
                            {
                                scrollByX = 0;
                            }
                            if (totalX > maxLeft)
                            {
                                totalX = totalX + scrollByX;
                            }
                            if (totalX < maxLeft)
                            {
                                scrollByX = maxLeft - (totalX - scrollByX);
                                totalX = maxLeft;
                            }
                        }

                        // scrolling to right side of image (pic moving to the left)
                        if (currentX < downX)
                        {
                            if (totalX == maxRight)
                            {
                                scrollByX = 0;
                            }
                            if (totalX < maxRight)
                            {
                                totalX = totalX + scrollByX;
                            }
                            if (totalX > maxRight)
                            {
                                scrollByX = maxRight - (totalX - scrollByX);
                                totalX = maxRight;
                            }
                        }

                        // scrolling to top of image (pic moving to the bottom)
                        if (currentY > downY)
                        {
                            if (totalY == maxTop)
                            {
                                scrollByY = 0;
                            }
                            if (totalY > maxTop)
                            {
                                totalY = totalY + scrollByY;
                            }
                            if (totalY < maxTop)
                            {
                                scrollByY = maxTop - (totalY - scrollByY);
                                totalY = maxTop;
                            }
                        }

                        // scrolling to bottom of image (pic moving to the top)
                        if (currentY < downY)
                        {
                            if (totalY == maxBottom)
                            {
                                scrollByY = 0;
                            }
                            if (totalY < maxBottom)
                            {
                                totalY = totalY + scrollByY;
                            }
                            if (totalY > maxBottom)
                            {
                                scrollByY = maxBottom - (totalY - scrollByY);
                                totalY = maxBottom;
                            }
                        }

                        mapView.scrollBy(scrollByX, scrollByY);
                        downX = currentX;
                        downY = currentY;
                        break;
                }
                return true;
            }
        });
        //scaleImage(mapImageView);
    }

    @Override
    public void onStart() {
        super.onStart();
        buildScreen();
    }

    private void rotate(float degree, View view, ImageView imageView)
    {
        final RotateAnimation rotateAnim = new RotateAnimation(0.0f, degree,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);

        rotateAnim.setDuration(0);
        rotateAnim.setFillAfter(true);
        //ImageView location = (ImageView) view.findViewById(R.id.imageViewLocation);
        imageView.startAnimation(rotateAnim);
    }
}
