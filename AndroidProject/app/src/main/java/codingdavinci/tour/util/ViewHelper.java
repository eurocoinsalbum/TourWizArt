package codingdavinci.tour.util;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.List;

import codingdavinci.tour.R;
import codingdavinci.tour.activity.CdvActivity;
import codingdavinci.tour.activity.TourDetailsActivity;
import codingdavinci.tour.listener.LoadImageListener;
import codingdavinci.tour.model.Tour;
import codingdavinci.tour.view.DashBoard;
import codingdavinci.tour.view.DashButtonDef;
import codingdavinci.tour.view.NavigationButton;

public class ViewHelper {

    public static LinearLayout createDashBoard(final CdvActivity cdvActivity, int colsPerRow, List<Tour> tours) {
        final DashBoard dashBoard = new DashBoard(cdvActivity, colsPerRow);
        for (final Tour tour : tours) {
            Log.i("cdv", "Trying to add tour: " + tour.getTitle() + " to dashboard");
            if (tour.getIconUuid() == null) {
                dashBoard.addDashButton(cdvActivity, new DashButtonDef(null, R.drawable.icons8_draft, tour.getTourDataset().getOwner().getName(), tour.getTourDataset().getRealTitle(), tour.getTitle(), 0, TourDetailsActivity.class, ActivityHelper.EXTRA_TOUR_UUID, tour.getTourUuid()));
                continue;
            }
            try {
                tour.getPreview(cdvActivity, new LoadImageListener() {
                    @Override
                    public void imageLoaded(Bitmap bitmap) {
                        dashBoard.addDashButton(cdvActivity, new DashButtonDef(bitmap, 0, tour.getTourDataset().getOwner().getName(), tour.getTourDataset().getRealTitle(), tour.getTitle(), 0, TourDetailsActivity.class, ActivityHelper.EXTRA_TOUR_UUID, tour.getTourUuid()));
                    }
                });
            } catch (IOException e) {
                Log.e("cdv", "Cannot create dash tour button", e);
            }
        }

        return dashBoard.getView();
    }

    public static LinearLayout createNavigationBar(CdvActivity cdvActivity, int parentLayoutId) {
        LinearLayout navigationParent = cdvActivity.findViewById(parentLayoutId);
        View navigationView = cdvActivity.getLayoutInflater().inflate(R.layout.navigation_bar, navigationParent, true);
        return navigationView.findViewById(R.id.navigation_bar);
    }

    public static NavigationButton addNavigationButton(CdvActivity cdvActivity, ViewGroup navigationBar, int imageResourceId, int textResourceId) {
        // root
        NavigationButton navigationButton = new NavigationButton();
        navigationButton.rootView = cdvActivity.getLayoutInflater().inflate(R.layout.navigation_button, navigationBar, false);
        navigationBar.addView(navigationButton.rootView);
        // image
        navigationButton.imageButton = navigationButton.rootView.findViewById(R.id.button);
        navigationButton.imageButton.setImageResource(imageResourceId);
        // text
        navigationButton.textView = navigationButton.rootView.findViewById(R.id.title);
        navigationButton.textView.setText(cdvActivity.getText(textResourceId));

        Log.i("cdv", "Navigation button added: " + cdvActivity.getText(textResourceId));
        return navigationButton;
    }
}
