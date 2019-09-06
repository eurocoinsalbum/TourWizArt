package codingdavinci.tour.listener;

import android.view.ScaleGestureDetector;
import android.widget.ImageView;

public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;
    private ImageView imageView;

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        scaleFactor *= scaleGestureDetector.getScaleFactor();
        scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 10.0f));
        imageView.setScaleX(scaleFactor);
        imageView.setScaleY(scaleFactor);
        return true;
    }
}
