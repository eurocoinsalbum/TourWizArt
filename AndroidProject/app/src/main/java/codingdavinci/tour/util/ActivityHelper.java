package codingdavinci.tour.util;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import java.util.UUID;

import codingdavinci.tour.activity.CdvActivity;
import codingdavinci.tour.activity.GoTourActivity;
import codingdavinci.tour.activity.SelectDatasetObjectsActivity;
import codingdavinci.tour.manager.DataProvider;
import codingdavinci.tour.model.DatasetObject;
import codingdavinci.tour.model.TourDataset;

public class ActivityHelper {
    public static final String EXTRA_TOUR_UUID = "tourUuuid";
    public static final String EXTRA_TOUR_DATASET_UUID = "tourDatasetUuid";
    public static final String EXTRA_FOLLOW_UP = "followUp";
    public static final String EXTRA_PREVIOUS = "previous";
    public static final String EXTRA_DATASET_OBJECT_UUID = "datasetObjectUuid";

    public static UUID getTourUuid(Intent intent) {
        return getUuid(intent, EXTRA_TOUR_UUID);
    }

    public static void setTourUuid(Intent intent, UUID tourUuid) {
        setUuid(intent, EXTRA_TOUR_UUID, tourUuid);
    }

    public static UUID getTourDatasetUuid(Intent intent) {
        return getUuid(intent, EXTRA_TOUR_DATASET_UUID);
    }

    public static void setTourDatasetUuid(Intent intent, UUID tourDatasetUuid) {
        setUuid(intent, EXTRA_TOUR_DATASET_UUID, tourDatasetUuid);
    }

    private static UUID getUuid(Intent intent, String extraName) {
        String uuidString = intent.getStringExtra(extraName);
        if (uuidString == null) {
            return null;
        }
        return UUID.fromString(uuidString);
    }

    private static void setUuid(Intent intent, String extraName, UUID uuid) {
        intent.putExtra(extraName, uuid.toString());
    }

    public static void setFollowUp(Intent intent, String canonicalClassName) {
        intent.putExtra(EXTRA_FOLLOW_UP, canonicalClassName);
    }

    public static Class getFollowUp(Intent intent) throws ClassNotFoundException {
        String followUpString = intent.getStringExtra(EXTRA_FOLLOW_UP);
        if (followUpString == null) {
            return null;
        }
        return Class.forName(followUpString);
    }

    public static Class<? extends CdvActivity> getPrevious(Intent intent) throws ClassNotFoundException {
        String previousString = intent.getStringExtra(EXTRA_PREVIOUS);
        if (previousString == null) {
            return null;
        }
        return (Class<? extends CdvActivity>) Class.forName(previousString);
    }

    public static void startCdvActivity(CdvActivity cdvActivity, Class<?> activityToStart) {
        startCdvActivity(cdvActivity, activityToStart, null, null, null);
    }

    public static void startCdvActivity(CdvActivity cdvActivity, Class<?> activityToStart, String extraUuidName, UUID extraUuid) {
        startCdvActivity(cdvActivity, activityToStart, null, extraUuidName, extraUuid);
    }

    public static void startCdvActivity(CdvActivity cdvActivity, Class<?> activityToStart, Class<?> activityToFollowUp, String extraUuidName, UUID extraUuid) {
        if (activityToStart == null) {
            return;
        }
        Intent intent = new Intent(cdvActivity, activityToStart);
        if (extraUuidName != null) {
            ActivityHelper.setUuid(intent, extraUuidName, extraUuid);
        }

        if (activityToFollowUp != null) {
            ActivityHelper.setFollowUp(intent, activityToFollowUp.getCanonicalName());
        }
        intent.putExtra(EXTRA_PREVIOUS, cdvActivity.getClass().getCanonicalName());
        // this removes every called activity from the history list (see also CdvActivity.onOptionsItemSelected()
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        cdvActivity.startActivity(intent);
    }

    public static void createImageClickStaedelHack(final Context context, final TourDataset tourDataset, ImageView imageView, final DatasetObject datasetObject) {
        // click on image - Städel hack
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkShowStaedelWebsiteHack(context, tourDataset, datasetObject);
            }
        });
    }

    public static void checkShowStaedelWebsiteHack(Context context, TourDataset tourDataset, DatasetObject datasetObject) {
        if (tourDataset.getTourDatasetUuid().toString().equals(DataProvider.getStaedelUuid().toString())) {
            String url = TourHelper.getStaedelHackUrl(datasetObject);
            HttpHelper.showWebSite(context, url);
        }
    }
}
