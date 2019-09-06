package codingdavinci.tour.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import codingdavinci.tour.activity.CdvActivity;
import codingdavinci.tour.listener.DeleteTourListener;
import codingdavinci.tour.listener.LoadImageListener;
import codingdavinci.tour.listener.LoadPeopleListener;
import codingdavinci.tour.listener.LoadRatingListener;
import codingdavinci.tour.listener.LoadTourDatasetListener;
import codingdavinci.tour.listener.LoadTourListener;
import codingdavinci.tour.listener.LoadToursListener;
import codingdavinci.tour.listener.LoadUuidListItemsListener;
import codingdavinci.tour.listener.PermissionListener;
import codingdavinci.tour.listener.RatingListener;
import codingdavinci.tour.listener.SaveTourListener;
import codingdavinci.tour.listener.LoadUserListener;
import codingdavinci.tour.listener.SaveUserListener;
import codingdavinci.tour.model.DatasetObject;
import codingdavinci.tour.model.People;
import codingdavinci.tour.model.Tour;
import codingdavinci.tour.model.TourDataset;
import codingdavinci.tour.model.User;
import codingdavinci.tour.model.UuidListItem;
import codingdavinci.tour.util.AndroidHelper;
import codingdavinci.tour.util.HttpHelper;
import codingdavinci.tour.util.ImageHelper;
import codingdavinci.tour.util.TextHelper;
import codingdavinci.tour.util.TourHelper;

public class DataProvider {
    private static final String APP_NAME = "GoTour";
    private static final String FILE_PREFIX_PREVIEW = "p";
    private static final String FILE_PREFIX_FULL = "f";
    private static final String FILE_EXT_OBJECT = "jpg";
    private static final String FILE_EXT_MAP = "png";
    private static Map<UUID, TourDataset> cachedTourDatasets;
    private static Map<UUID, Tour> cachedTours = new HashMap<>();
    private static Map<UUID, People> cachedPeople;
    private static Map<UUID, User> cachedUsers;
    private static Map<UUID, Integer> cachedMyRatings;

    private static Map<UUID, Set<UUID>> cachedDatasetToTourMapping = new HashMap<>();
    private static User myUser;
    private static AtomicBoolean permissionRequestSent = new AtomicBoolean(false);

    static {
        //addDummyTours();
    }

    public static void addDummyTours() {
        // add some dummy tours but those should come from the database
        for (int i = 0; i < 3; i++) {
            Tour dummyTour = createDummyTour();
            cachedTours.put(dummyTour.getTourUuid(), dummyTour);
        }
    }

    static public void getAvailableDatasets(@NonNull final LoadTourDatasetListener loadTourDatasetListener) {
        if (cachedTourDatasets == null) {
            loadActiveTourDatasets(new LoadTourDatasetListener() {
                @Override
                public void tourDatasetsLoaded(@NonNull Collection<TourDataset> tourDatasets) {
                    getAvailableDatasets(loadTourDatasetListener);
                }
            });
            return;
        }

        loadTourDatasetListener.tourDatasetsLoaded(cachedTourDatasets.values());
    }

    private static void loadActiveTourDatasets(final LoadTourDatasetListener loadTourDatasetListener) {
        if (cachedTourDatasets != null) {
            loadTourDatasetListener.tourDatasetsLoaded(cachedTourDatasets.values());
            return;
        }

        if (cachedPeople == null) {
            HttpHelper.getPeople(new LoadPeopleListener() {
                @Override
                public void peopleLoaded(List<People> peopleList) {
                    cachedPeople = new HashMap<UUID, People>();
                    for (People people : peopleList) {
                        cachedPeople.put(people.uuid, people);
                    }
                    loadActiveTourDatasets(loadTourDatasetListener);
                }
            });
            return;
        }

        if (cachedUsers == null) {
            HttpHelper.getUser(new LoadUserListener() {
                @Override
                public void usersLoaded(List<User> users) {
                    cachedUsers = new HashMap<UUID, User>();
                    for (User user : users) {
                        cachedUsers.put(user.getUserUuid(), user);
                    }
                    setMyUser(cachedUsers.get(myUser.getUserUuid()));
                    loadActiveTourDatasets(loadTourDatasetListener);
                }
            });
            return;
        }

        if (cachedMyRatings == null) {
            HttpHelper.getRatingsUser(myUser.getUserUuid(), new LoadRatingListener() {
                @Override
                public void ratingsLoaded(Map<UUID, Integer> objectRatings) {
                    cachedMyRatings = objectRatings;
                }
            });
        }

        HttpHelper.getActiveTourDatasets(new LoadTourDatasetListener() {
            @Override
            public void tourDatasetsLoaded(@NonNull final Collection<TourDataset> tourDatasets) {
                cachedTourDatasets = new HashMap<>();
                for (TourDataset tourDataset : tourDatasets) {
                    cachedTourDatasets.put(tourDataset.getTourDatasetUuid(), tourDataset);
                }

                loadTourDatasetListener.tourDatasetsLoaded(tourDatasets);
            }
        });
    }

    static public void getAvailableTours(UUID tourDatasetUuid, @NonNull LoadToursListener loadTourListener) {
        // load tours which belong to this dataset
        Set<UUID> tourUuids = cachedDatasetToTourMapping.get(tourDatasetUuid);
        if (tourUuids == null) {
            loadTours(tourDatasetUuid, loadTourListener);
            return;
        }

        List<Tour> tours = new ArrayList<>(tourUuids.size());
        for (UUID tourUuid : tourUuids) {
            Tour tour = cachedTours.get(tourUuid);
            if (tour == null) {
                Log.e("cdv", "Invalid tour mapping: " + tourDatasetUuid + "->" + tourUuid);
                continue;
            }
            tours.add(tour);
        }
        loadTourListener.toursLoaded(tours);
    }

    private static void loadTours(final UUID tourDatasetUuid, final LoadToursListener loadTourListener) {
        HttpHelper.getAvailableTours(tourDatasetUuid, new LoadUuidListItemsListener() {
            @Override
            public void uuidListItemsLoaded(@NonNull List<? extends UuidListItem> tours) {
                Set<UUID> tourUuids = new HashSet<>(tours.size());
                cachedDatasetToTourMapping.put(tourDatasetUuid, tourUuids);
                for (UuidListItem tour : tours) {
                    cachedTours.put(tour.getUuid(), (Tour) tour);
                    tourUuids.add(tour.getUuid());
                }
                // recursive call
                getAvailableTours(tourDatasetUuid, loadTourListener);
            }
        });
    }

    public static void loadTour(final UUID tourUuid, final LoadTourListener loadTourListener) {
        Tour tour = cachedTours.get(tourUuid);
        if (tour == null) {
            HttpHelper.getTour(tourUuid, loadTourListener);
            return;
        }

        // check if tour was already fully loaded
        if (tour.isTourObjectsLoaded()) {
            loadTourListener.tourLoaded(tour);
            return;
        }

        // check if dataset objects for this tour have been loaded already
        if (!tour.getTourDataset().isDatasetObjectsLoaded()) {
            getDatasetObjects(tour.getTourDataset(), new LoadTourDatasetListener() {
                @Override
                public void tourDatasetsLoaded(@NonNull Collection<TourDataset> tourDatasets) {
                    loadTour(tourUuid, loadTourListener);
                }
            });
            return;
        }

        HttpHelper.getTourObjects(tour, new LoadTourListener() {
            @Override
            public void tourLoaded(Tour tour) {
                cachedTours.put(tour.getTourUuid(), tour);
                loadTourListener.tourLoaded(tour);
            }
        });
    }

    private static Tour createDummyTour() {
        Tour tour = new Tour(UUID.randomUUID());
        tour.setTitle("Tour " + tour.getTourUuid().toString().substring(0, 2));
        tour.setAccessLevel(Tour.AccessLevel.PUBLIC);
        tour.setStatus(Tour.Status.ACTIVE);
        return tour;
    }

    public static void setMyUser(User user) {
        myUser = user;
    }

    public static User getMyUser() {
        return myUser;
    }

    public static String getMyUserUuidStr() {
        return myUser.getUserUuid().toString();
    }

    public static TourDataset getTourDataset(UUID uuid) {
        if (!cachedTourDatasets.containsKey(uuid)) {
            throw new IllegalArgumentException("tourDatasetUuid is not known: " + uuid.toString() + "\nAvailable: " + cachedTourDatasets.toString());
        }

        return cachedTourDatasets.get(uuid);
    }

    public static String getAppName() {
        return APP_NAME;
    }

    public static void getPreviewImage(CdvActivity cdvActivity, TourDataset tourDataset, int iconWidth, LoadImageListener loadImageListener) throws IOException {
        getImage(cdvActivity, FILE_PREFIX_PREVIEW, FILE_EXT_OBJECT, tourDataset.getTourDatasetUuid(), iconWidth, HttpHelper.getPreviewImageUri(tourDataset), loadImageListener);
    }

    public static void getPreviewImage(CdvActivity cdvActivity, DatasetObject datasetObject, int iconWidth, LoadImageListener loadImageListener) throws IOException {
        getImage(cdvActivity, FILE_PREFIX_PREVIEW, FILE_EXT_OBJECT, datasetObject.getDatasetObjectUuid(), iconWidth, HttpHelper.getPreviewImageUri(datasetObject), loadImageListener);
    }

    public static void getPreviewImage(CdvActivity cdvActivity, Tour tour, int iconWidth, LoadImageListener loadImageListener) throws IOException {
        if (tour.getIconUuid() == null) {
            return;
        }
        getImage(cdvActivity, FILE_PREFIX_PREVIEW, FILE_EXT_OBJECT, tour.getIconUuid(), iconWidth, HttpHelper.getPreviewImageUri(tour), loadImageListener);
    }

    public static void getMap(CdvActivity cdvActivity, TourDataset tourDataset, int mapIndex, int targetWidth, LoadImageListener loadImageListener) throws IOException {
        UUID mapUuid = tourDataset.getTourDatasetMaps().get(mapIndex).getTourDatasetMapUuid();
        Uri mapUri = HttpHelper.getMapImageUri(tourDataset, mapIndex);
        getImage(cdvActivity, FILE_PREFIX_PREVIEW, FILE_EXT_MAP, mapUuid, targetWidth, mapUri, loadImageListener);
    }

    private static void getImage(final CdvActivity cdvActivity, final String filePrefix, String fileExt, UUID imageUuid, final int targetWidth, final Uri uri, final LoadImageListener loadImageListener) throws IOException {
        if (!cdvActivity.hasRequestPermissionListener()) {
            cdvActivity.setRequestPermissionListener(new PermissionListener() {
                @Override
                public void permissionGranted() {
                    cdvActivity.recreate();
                }
            });
        }

        // check if we have permissions for storage. If not request permission and start over
        if (!AndroidHelper.haveStoragePermission(cdvActivity)) {
            if (permissionRequestSent.compareAndSet(false, true)) {
                AndroidHelper.requestStoragePermission(cdvActivity);
            }
            return;
        }

        final File imageFile = ImageHelper.createImageFile(cdvActivity, false, filePrefix, imageUuid.toString(), fileExt);
        if (!imageFile.exists()) {
            // download
            Log.i("cdv", "downloading image:" + imageFile.getAbsolutePath());
            HttpHelper.downloadFile(cdvActivity, uri, imageFile, new HttpHelper.HttpRequestListener() {
                @Override
                public void finished(HttpHelper.Response response) {
                    Log.i("cdv", "response: " + response.success);
                    if (!response.success) {
                        return;
                    }
                    // resize the image to requested size
                    resizeAndSendImage(cdvActivity, imageFile, targetWidth, loadImageListener);
                }
            });
            return;
        }

        // image was alreade downloaded
        Log.i("cdv", "image was already downloaded " + imageFile.getAbsolutePath());
        resizeAndSendImage(cdvActivity, imageFile, targetWidth, loadImageListener);
    }

    private static void resizeAndSendImage(Context context, File imageFile, int width, LoadImageListener loadImageListener) {
        try {
            Bitmap bitmap = ImageHelper.resizeBitmap(context, Uri.fromFile(imageFile), width);
            loadImageListener.imageLoaded(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void getDatasetObjects(TourDataset tourDataset, final LoadTourDatasetListener loadTourDatasetListener) {
        final List<TourDataset> loadedTourDatasets = new ArrayList<>(1);
        loadedTourDatasets.add(tourDataset);

        // check if tour dataset was already fully loaded
        if (tourDataset.isDatasetObjectsLoaded()) {
            loadTourDatasetListener.tourDatasetsLoaded(loadedTourDatasets);
            return;
        }

        // load tourDataset from server
        HttpHelper.getDatasetObjects(tourDataset, loadTourDatasetListener);
    }

    public static void saveTour(final Tour tour, boolean saveTourObjects, final SaveTourListener saveTourListener) {
        HttpHelper.saveTour(tour, saveTourObjects, new HttpHelper.HttpRequestListener() {
            @Override
            public void finished(HttpHelper.Response response) {
                if (!response.success) {
                    saveTourListener.errorOccured();
                    return;
                }

                // insert/update tour in cache
                Tour updatedTour = cachedTours.get(tour.getTourUuid());
                if (updatedTour == null) {
                    cachedTours.put(tour.getTourUuid(), tour);
                    updatedTour = tour;
                } else {
                    TourHelper.updateTour(tour, updatedTour);
                }

                // assign tour to it's tourDataset
                UUID tourDatasetUuid = updatedTour.getTourDataset().getTourDatasetUuid();
                Set<UUID> tourUuids = cachedDatasetToTourMapping.get(tourDatasetUuid);
                if (tourUuids != null) {
                    tourUuids.add(updatedTour.getTourUuid());
                } else {
                    // we don't need to add it now as datasets have not been loaded until now at all
                }

                // update new textIds if received
                try {
                    JSONArray jsonTexts = response.output.getJSONArray("texts");
                    TextHelper.addTexts(HttpHelper.createTextsFromJson(jsonTexts));
                } catch (JSONException e) {
                    e.printStackTrace();
                    response.success = false;
                    response.message = e.getLocalizedMessage();
                }
                saveTourListener.tourSaved(updatedTour);
            }
        });
    }

    public static void deleteTour(final Tour tour, final DeleteTourListener deleteTourListener) {
        HttpHelper.deleteTour(tour, new DeleteTourListener() {
            @Override
            public void tourDeleted(Tour tour) {
                // remove from caches
                cachedTours.remove(tour.getTourDataset());

                Set<UUID> datasetToTourMappings = cachedDatasetToTourMapping.get(tour.getTourDataset().getTourDatasetUuid());
                if (datasetToTourMappings != null) {
                    datasetToTourMappings.remove(tour.getTourUuid());
                }

                // call listener
                deleteTourListener.tourDeleted(tour);
            }

            @Override
            public void errorOccured() {
                deleteTourListener.errorOccured();
            }
        });
    }

    public static void getMyTours(final LoadToursListener loadToursListener) {
        HttpHelper.getMyTours(new LoadToursListener() {
            @Override
            public void toursLoaded(@NonNull List<Tour> tours) {
                // fill-up caches
                if (cachedTours == null) {
                    cachedTours = new HashMap<>();
                }
                for (Tour tour : tours) {
                    cachedTours.put(tour.getTourUuid(), tour);
                }
                loadToursListener.toursLoaded(tours);
            }
        });
        return;
    }

    public static void getDashboardTours(final UUID tourDatasetUuid, final LoadToursListener loadToursListener) {
        if (cachedTourDatasets == null) {
            loadActiveTourDatasets(new LoadTourDatasetListener() {
                @Override
                public void tourDatasetsLoaded(@NonNull Collection<TourDataset> tourDatasets) {
                    getDashboardTours(tourDatasetUuid, loadToursListener);
                }
            });
            return;
        }

        HttpHelper.getDashboardTours(tourDatasetUuid, new LoadToursListener() {
            @Override
            public void toursLoaded(@NonNull List<Tour> tours) {
                // fill-up caches
                if (cachedTours == null) {
                    cachedTours = new HashMap<>();
                }
                for (Tour tour : tours) {
                    cachedTours.put(tour.getTourUuid(), tour);
                }
                loadToursListener.toursLoaded(tours);
            }
        });
        return;
    }

    public static User getUser(UUID uuid) {
        return cachedUsers.get(uuid);
    }

    public static UUID getStaedelUuid() {
        if (cachedTourDatasets != null) {
            for (TourDataset tourDataset : cachedTourDatasets.values()) {
                if (tourDataset.getRealTitle().equals("Staedel") || tourDataset.getRealTitle().equals("Städel")) {
                    return tourDataset.getUuid();
                }
            }
        }
        return UUID.fromString("37d81284-6ee2-4294-8a06-61fdf907a053");
    }

    public static People getPeople(UUID peopleUuid) {
        People people = cachedPeople.get(peopleUuid);
        if (people == null) {
            throw new IllegalArgumentException("Unknown peopleUuid: " + peopleUuid);
        }
        return people;
    }

    public static Integer getUserRating(UUID itemUuid) {
        return cachedMyRatings.get(itemUuid);
    }

    public static void setObjectRating(final DatasetObject datasetObject, final int rating) {
        HttpHelper.saveObjectRating(myUser.getUserUuid(), datasetObject.getDatasetObjectUuid(), rating, new RatingListener() {
            @Override
            public void rated() {
                cachedMyRatings.put(datasetObject.getDatasetObjectUuid(), rating);
            }
        });
    }

    public static void saveMyUser(final UUID myUserUuid, final SaveUserListener saveUserListener) {
        // add temporarely dummy user
        setMyUser(new User(myUserUuid));
        HttpHelper.saveUser("standard", new SaveUserListener() {
            @Override
            public void userSaved(User.Role role) {
                setMyUser(new User(myUserUuid));
                saveUserListener.userSaved(role);
            }
        });
    }
}
