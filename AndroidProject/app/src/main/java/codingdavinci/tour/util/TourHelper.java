package codingdavinci.tour.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.UUID;

import codingdavinci.tour.R;
import codingdavinci.tour.activity.CdvActivity;
import codingdavinci.tour.activity.MyToursActivity;
import codingdavinci.tour.listener.DeleteTourListener;
import codingdavinci.tour.listener.SaveTourListener;
import codingdavinci.tour.manager.DataProvider;
import codingdavinci.tour.manager.NoteManager;
import codingdavinci.tour.model.DatasetObject;
import codingdavinci.tour.model.Tour;
import codingdavinci.tour.model.TourDataset;
import codingdavinci.tour.model.User;
import codingdavinci.tour.model.UuidListItem;

public class TourHelper {
    public static Tour createCopyAsCustomized(Context context, Tour origTour) {
        Tour tour = createCopy(origTour, true);
        tour.setTourDataset(origTour.getTourDataset());
        tour.setAuthor(DataProvider.getMyUser());
        tour.setTitle(getDefaultTitle(context));
        tour.setDescription("");
        tour.setStatus(Tour.Status.DRAFT);
        tour.setCreated(new Date());
        tour.setLastUpdate(new Date());
        tour.setAccessLevel(Tour.AccessLevel.PUBLIC);
        tour.setStatCopied(0);
        tour.setStatTaken(0);
        tour.setAvgRating(0);

        return tour;
    }

    public static Tour createCopy(Tour origTour, boolean createNewUuid) {
        Tour tour = new Tour(createNewUuid ? UUID.randomUUID() : origTour.getTourUuid());
        updateTour(origTour, tour);
        return tour;
    }

    public static void updateTour(Tour sourceTour, Tour tourToUpdate) {
        tourToUpdate.setTourDataset(sourceTour.getTourDataset());
        tourToUpdate.setTitle(sourceTour.getTitle());
        tourToUpdate.setDescription(sourceTour.getDescription());
        tourToUpdate.setAuthor(sourceTour.getAuthor());
        tourToUpdate.setStatus(sourceTour.getStatus());
        tourToUpdate.setCreated(sourceTour.getCreated());
        tourToUpdate.setLastUpdate(sourceTour.getLastUpdate());
        tourToUpdate.setAccessLevel(sourceTour.getAccessLevel());
        tourToUpdate.setStatCopied(sourceTour.getStatCopied());
        tourToUpdate.setStatTaken(sourceTour.getStatTaken());
        tourToUpdate.setAvgRating(sourceTour.getAvgRating());
        tourToUpdate.setIconUuid(sourceTour.getIconUuid());
        tourToUpdate.setDatasetObjects(new ArrayList<>(sourceTour.getDatasetObjects()));
    }

    public static Tour createNewTour(Context context, TourDataset tourDataset) {
        Tour tour = new Tour(UUID.randomUUID());
        tour.setTourDataset(tourDataset);
        tour.setTitle(getDefaultTitle(context));
        tour.setDescription("");
        tour.setStatus(Tour.Status.DRAFT);
        tour.setAccessLevel(Tour.AccessLevel.PUBLIC);
        tour.setAuthor(DataProvider.getMyUser());
        tour.setCreated(new Date());
        tour.setLastUpdate(new Date());
        tour.setDatasetObjects(new ArrayList<DatasetObject>());
        return tour;
    }

    private static String getDefaultTitle(Context context) {
        return context.getText(R.string.tour_default_title).toString();
    }

    public static Comparator<? super UuidListItem> createSortByTitleComparator() {
        return new Comparator<UuidListItem>() {
            public int compare(@NonNull UuidListItem item1, @NonNull UuidListItem item2) {
                return item1.getTitle().compareToIgnoreCase(item2.getTitle());
            }
        };
    }

    public static View.OnClickListener createSaveDraftListener(final CdvActivity cdvActivity, final Tour tour, final boolean saveTourObjects) {
        cdvActivity.updateModelFromUI();
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataProvider.saveTour(tour, saveTourObjects, new SaveTourListener() {
                    @Override
                    public void tourSaved(Tour tour) {
                        ActivityHelper.startCdvActivity(cdvActivity, MyToursActivity.class, ActivityHelper.EXTRA_TOUR_UUID, tour.getTourUuid());
                    }

                    @Override
                    public void errorOccured() {
                        NoteManager.showError(cdvActivity, R.string.internal_error);
                    }
                });
            }
        };
    }

    public static View.OnClickListener createDeleteListener(final CdvActivity cdvActivity, final Class<? extends CdvActivity> targetClass, final Tour tour) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(cdvActivity)
                        .setTitle(R.string.delete_tour)
                        .setMessage(R.string.confirm_delete_tour)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                DataProvider.deleteTour(tour, new DeleteTourListener() {
                                    @Override
                                    public void tourDeleted(Tour tour) {
                                        ActivityHelper.startCdvActivity(cdvActivity, targetClass, ActivityHelper.EXTRA_TOUR_DATASET_UUID, tour.getTourDataset().getTourDatasetUuid());
                                    }

                                    @Override
                                    public void errorOccured() {
                                        NoteManager.showError(cdvActivity, R.string.internal_error);
                                    }
                                });
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        };
    }

    public static boolean isMyTour(Tour tour) {
        return DataProvider.getMyUser().getUserUuid().equals(tour.getAuthor().getUserUuid());
    }

    public static String getStaedelHackUrl(DatasetObject datasetObject) {
        String urlString = datasetObject.getRealTitle().replace(" ", "-").replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss");
        urlString = urlString.substring(0, Math.min(urlString.length(), 60));
        int pos = urlString.indexOf(',');
        if (pos != -1) {
            urlString = urlString.substring(0, pos);
        }
        return "https://sammlung.staedelmuseum.de/de/werk/" + urlString;
    }

    public static boolean isUserAllowedToEditTour(Tour tour) {
        return isMyTour(tour) || DataProvider.getMyUser().getRole().equals(User.Role.ADMIN);
    }
}
