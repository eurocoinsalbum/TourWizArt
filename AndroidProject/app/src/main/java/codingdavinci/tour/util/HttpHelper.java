package codingdavinci.tour.util;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import codingdavinci.tour.BuildConfig;
import codingdavinci.tour.listener.DeleteTourListener;
import codingdavinci.tour.listener.LoadPeopleListener;
import codingdavinci.tour.listener.LoadRatingListener;
import codingdavinci.tour.listener.LoadTextListener;
import codingdavinci.tour.listener.LoadTourDatasetListener;
import codingdavinci.tour.listener.LoadTourListener;
import codingdavinci.tour.listener.LoadToursListener;
import codingdavinci.tour.listener.LoadUserListener;
import codingdavinci.tour.listener.LoadUuidListItemsListener;
import codingdavinci.tour.listener.RatingListener;
import codingdavinci.tour.listener.SaveUserListener;
import codingdavinci.tour.manager.DataProvider;
import codingdavinci.tour.model.DatasetObject;
import codingdavinci.tour.model.GpsMapLocation;
import codingdavinci.tour.model.LanguageCode;
import codingdavinci.tour.model.People;
import codingdavinci.tour.model.Tour;
import codingdavinci.tour.model.TourDataset;
import codingdavinci.tour.model.TourDatasetMap;
import codingdavinci.tour.model.User;

public class HttpHelper {

    private static class ImageDownload {
        HttpRequestListener listener;
        DownloadManager downloadManager;

        public ImageDownload(DownloadManager downloadManager, HttpRequestListener listener) {
            this.downloadManager = downloadManager;
            this.listener = listener;
        }
    }

    private static ConcurrentHashMap<Long, ImageDownload> activeDownloads = new ConcurrentHashMap<>();
    public static BroadcastReceiver onDownloadComplete;
    private static AtomicBoolean receiverRegistered = new AtomicBoolean(false);

    static {
        onDownloadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // check if download was registered by this class
                Long downloadId = new Long(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
                Log.i("cdv", "received download: " + downloadId);
                if (!activeDownloads.containsKey(downloadId)) {
                    Log.i("cdv", "not my download: " + downloadId);
                    return;
                }

                // create response for listener
                ImageDownload imageDownload = activeDownloads.get(downloadId);
                Response response = new Response();
                response.success = false;

                // get result of download
                Cursor cursor = imageDownload.downloadManager.query(new DownloadManager.Query().setFilterById(downloadId));
                if (cursor.moveToFirst()) {
                    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        response.success = true;
                    } else {
                        int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
                        response.message = "Download not correct, status [" + status + "] reason [" + reason + "]";
                    }
                }
                Log.i("cdv", "callback download: " + downloadId);
                activeDownloads.remove(downloadId);
                imageDownload.listener.finished(response);
            }
        };
    }

    public static final String HTTP_SERVER = BuildConfig.SERVER_URL;
    private static final String HTTP_DIRECTORY_NAME_IMAGES = "images/";
    private static final String HTTP_DIRECTORY_NAME_PREVIEWS = "previews/";
    private static final String HTTP_NAME_DATASET_PREVIEW = "preview.jpg";
    private static final String HTTP_DIRECTORY_NAME_MAPS = "maps/";

    public static final String GET_ACTIVE_TOUR_DATASETS_SCRIPT_URI = HTTP_SERVER + "getActiveTourDatasets.php";
    public static final String GET_TOUR_DATASET_OBJECTS_SCRIPT_URI = HTTP_SERVER + "getDatasetObjects.php";
    public static final String GET_AVAILABLE_TOURS_SCRIPT_URI = HTTP_SERVER + "getAvailableTours.php";
    public static final String GET_DASHBOARD_TOURS_SCRIPT_URI = HTTP_SERVER + "getDashboardTours.php";
    public static final String GET_MY_TOURS_SCRIPT_URI = HTTP_SERVER + "getMyTours.php";
    public static final String GET_TOUR_OBJECTS_SCRIPT_URI = HTTP_SERVER + "getTourObjects.php";
    public static final String GET_TOUR_SCRIPT_URI = HTTP_SERVER + "getTour.php";
    public static final String GET_TEXTS_SCRIPT_URI = HTTP_SERVER + "getTexts.php";
    public static final String GET_PEOPLE_SCRIPT_URI = HTTP_SERVER + "getPeople.php";
    public static final String GET_USERS_SCRIPT_URI = HTTP_SERVER + "getUsers.php";
    public static final String GET_RATINGS_USER_SCRIPT_URI = HTTP_SERVER + "getRatingsUser.php";
    public static final String SAVE_RATING_SCRIPT_URI = HTTP_SERVER + "saveRating.php";
    public static final String SAVE_USER_SCRIPT_URI = HTTP_SERVER + "saveUser.php";

    public static final String SAVE_TOUR_SCRIPT_URI = HTTP_SERVER + "saveTour.php";
    public static final String DELETE_TOUR_SCRIPT_URI = HTTP_SERVER + "deleteTour.php";

    // Text
    private static final String JSON_TEXT_ID = "textId";
    private static final String JSON_TEXT = "text";
    // Users
    private static final String JSON_USERS = "users";
    private static final String JSON_USER_UUID = "userUuid";
    private static final String JSON_USER_NAME = "name";
    private static final String JSON_USER_ROLE = "role";
    // Ratings
    private static final String JSON_RATINGS = "ratings";
    private static final String JSON_RATING = "rating";
    private static final String JSON_RATING_ITEM_UUID = "itemUuid";

    // People
    private static final String JSON_PEOPLE_LIST = "peopleList";
    private static final String JSON_PEOPLE_UUID = "uuid";
    private static final String JSON_PEOPLE_NAME = "name";
    // TourDataset
    private static final String JSON_TOUR_DATASET_UUID = "tourDatasetUuid";
    private static final String JSON_TOUR_DATASET_LOCATION_TYPE = "tourLocationType";
    private static final String JSON_TOUR_DATASET_OWNER_UUID = "ownerUuid";
    private static final String JSON_TOUR_DATASET_MAPS = "maps";
    // DatasetMap
    private static final String JSON_DATASET_MAP_UUID = "datasetMapUuid";
    private static final String JSON_DATASET_MAP_TITLE_TEXT_ID = "titleTextId";
    private static final String JSON_DATASET_MAP_LOCATION = "location";
    private static final String JSON_DATASET_MAP_INDEX = "mapIndex";
    // DatasetObject
    private static final String JSON_DATASET_OBJECT_UUID = "datasetObjectUuid";
    private static final String JSON_DATASET_OBJECT_UUIDS = "datasetObjectUuids";
    private static final String JSON_DATASET_OBJECTS = "datasetObjects";
    private static final String JSON_DATASET_OBJECT_TITLE_TEXT_ID = "titleTextId";
    private static final String JSON_DATASET_OBJECT_LOCATION = "location";
    private static final String JSON_DATASET_OBJECT_ARTIST_UUID = "artistUuid";
    private static final String JSON_DATASET_OBJECT_STYLE_TEXT_ID = "styleTextId";
    private static final String JSON_DATASET_OBJECT_YEAR = "year";

    private static final String JSON_DATASET_OBJECT_DESCRIPTION_TEXT_ID = "descriptionTextId";
    // Tour
    private static final String JSON_TOUR_UUID = "tourUuid";
    private static final String JSON_TOUR_TITLE_TEXT_ID = "titleTextId";
    private static final String JSON_TOUR_DESCRIPTION_TEXT_ID = "descriptionTextId";
    private static final String JSON_TOUR_STATUS = "status";
    private static final String JSON_TOUR_AUTHOR_USER_UUID = "authorUserUuid";
    private static final String JSON_TOUR_ACCESS_LEVEL = "accessLevel";
    private static final String JSON_TOUR_ICON_UUID = "iconUuid";
    private static final String JSON_TOUR_OBJECTS = "tourObjects";
    private static final String JSON_TOUR_TITLE = "title";
    private static final String JSON_TOUR_DESCRIPTION = "description";
    private static final String JSON_TOUR = "tour";

    public static void showWebSite(Context context, String url) {
        Intent webIntent = new Intent(Intent.ACTION_VIEW);
        webIntent.setData(Uri.parse(url));
        context.startActivity(webIntent);
    }

    /**
     * Load tour from scratch
     */
    public static void getTour(UUID tourUuid, LoadTourListener loadTourListener) {
        // TODO implement if needed (should not be needed)
        throw new IllegalArgumentException("Not implemented");
        //new HttpRequestAsync(createURL(GET_TOUR_SCRIPT_URI, null, tourUuid, null, null, null, listener), null, listener).execute();
    }

    public static void getUser(final LoadUserListener loadUserListener) {
        HttpRequestListener listener = new HttpRequestListener() {
            @Override
            public void finished(Response response) {
                List<User> users = new ArrayList<>();
                if (response.success) {
                    try {
                        JSONArray jsonUser = response.output.getJSONArray(JSON_USERS);
                        for (int i = 0 ; i < jsonUser.length(); i++) {
                            JSONObject jsonUserObject = jsonUser.getJSONObject(i);
                            UUID uuid = UUID.fromString(jsonUserObject.getString(JSON_USER_UUID));
                            User user = new User(uuid);
                            user.setName(jsonUserObject.getString(JSON_USER_NAME));
                            user.setRole(User.Role.valueOf(jsonUserObject.getString(JSON_USER_ROLE)));
                            users.add(user);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                loadUserListener.usersLoaded(users);
            }
        };
        new HttpRequestAsync(createURL(GET_USERS_SCRIPT_URI, null, null, null, null, null, listener), null, listener).execute();
    }

    public static void getPeople(final LoadPeopleListener loadPeopleListener) {
        HttpRequestListener listener = new HttpRequestListener() {
            @Override
            public void finished(Response response) {
                List<People> peopleList = new ArrayList<>();
                if (response.success) {
                    try {
                        JSONArray jsonPeopleList = response.output.getJSONArray(JSON_PEOPLE_LIST);
                        for (int i = 0 ; i < jsonPeopleList.length(); i++) {
                            JSONObject jsonPeopleObject = jsonPeopleList.getJSONObject(i);
                            UUID uuid = UUID.fromString(jsonPeopleObject.getString(JSON_PEOPLE_UUID));
                            String name = jsonPeopleObject.getString(JSON_PEOPLE_NAME);
                            People people = new People(uuid, name);
                            peopleList.add(people);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                loadPeopleListener.peopleLoaded(peopleList);
            }
        };
        new HttpRequestAsync(createURL(GET_PEOPLE_SCRIPT_URI, null, null, null, null, null, listener), null, listener).execute();
    }

    public static void getTourObjects(final Tour tour, final LoadTourListener loadTourListener) {
        if (!tour.getTourDataset().isDatasetObjectsLoaded()) {
            throw new IllegalArgumentException("dataset objects of tourDataset have not been loaded");
        }
        HttpRequestListener listener = new HttpRequestListener() {
            @Override
            public void finished(Response response) {
                if (response.success) {
                    try {
                        List<DatasetObject> datasetObjects = new ArrayList<>();
                        JSONArray jsonTourObjects = response.output.getJSONArray(JSON_TOUR_OBJECTS);
                        for (int i = 0 ; i < jsonTourObjects.length(); i++) {
                            JSONObject jsonTourObject = jsonTourObjects.getJSONObject(i);
                            UUID datasetObjectUuid = UUID.fromString(jsonTourObject.getString(JSON_DATASET_OBJECT_UUID));
                            DatasetObject datasetObject = tour.getTourDataset().getDatasetObject(datasetObjectUuid);
                            datasetObjects.add(datasetObject);
                        }
                        tour.setDatasetObjects(datasetObjects);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                loadTourListener.tourLoaded(tour);
            }
        };
        new HttpRequestAsync(createURL(GET_TOUR_OBJECTS_SCRIPT_URI, null, tour.getTourUuid(), null, null, null, listener), null, listener).execute();
    }

    public static void getActiveTourDatasets(final LoadTourDatasetListener loadTourDatasetListener) {
        HttpRequestListener listener = new HttpRequestListener() {
            @Override
            public void finished(Response response) {
                List<TourDataset> tourDatasets = new ArrayList<>();
                if (response.success) {
                    try {
                        tourDatasets.addAll(createTourDatasetsFromJSON(response.output.getJSONArray("tourDatasets")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                loadTourDatasetListener.tourDatasetsLoaded(tourDatasets);
            }
        };
        new HttpRequestAsync(createURL(GET_ACTIVE_TOUR_DATASETS_SCRIPT_URI, null, null, null, null, null, listener), null, listener).execute();
    }

    private static Collection<TourDataset> createTourDatasetsFromJSON(JSONArray jsonTourDatasets) throws JSONException {
        List<TourDataset> tourDatasets = new ArrayList<>(jsonTourDatasets.length());
        for (int i = 0 ; i < jsonTourDatasets.length(); i++) {
            JSONObject jsonTourDataset = jsonTourDatasets.getJSONObject(i);
            TourDataset tourDataset = new TourDataset(UUID.fromString(jsonTourDataset.getString(JSON_TOUR_DATASET_UUID)));
            tourDataset.setTitle(TextHelper.getText(jsonTourDataset.getInt(JSON_TOUR_TITLE_TEXT_ID)));
            tourDataset.setDescription(TextHelper.getText(jsonTourDataset.getInt(JSON_TOUR_DESCRIPTION_TEXT_ID)));
            tourDataset.setOwner(DataProvider.getUser(UUID.fromString(jsonTourDataset.getString(JSON_TOUR_DATASET_OWNER_UUID))));
            tourDataset.setLocationType(TourDataset.LocationType.valueOf(jsonTourDataset.getString(JSON_TOUR_DATASET_LOCATION_TYPE)));

            // maps
            JSONArray jsonMaps = jsonTourDataset.getJSONArray(JSON_TOUR_DATASET_MAPS);
            List<TourDatasetMap> tourDatasetMaps = new ArrayList<>(jsonMaps.length());
            for (int j = 0; j < jsonMaps.length(); j++) {
                JSONObject jsonMap = jsonMaps.getJSONObject(j);
                TourDatasetMap tourDatasetMap = new TourDatasetMap(tourDataset, UUID.fromString(jsonMap.getString(JSON_DATASET_MAP_UUID)));
                tourDatasetMap.setTitle(TextHelper.getText(jsonMap.getInt(JSON_DATASET_MAP_TITLE_TEXT_ID)));
                if (!jsonMap.isNull(JSON_DATASET_MAP_LOCATION)) {
                    tourDatasetMap.setGpsMapLocation(LocationHelper.parseGpsMapLocation(jsonMap.getString(JSON_DATASET_MAP_LOCATION)));
                }
                tourDatasetMaps.add(tourDatasetMap);
            }
            tourDataset.setTourDatasetMaps(tourDatasetMaps);

            tourDatasets.add(tourDataset);
        }
        return tourDatasets;
    }

    public static void getDatasetObjects(final TourDataset tourDataset, final LoadTourDatasetListener loadTourDatasetListener) {
        HttpRequestListener listener = new HttpRequestListener() {
            @Override
            public void finished(Response response) {
                if (response.success) {
                    try {
                        Collection<DatasetObject> datasetObjects = new ArrayList<>();
                        JSONArray jsonDatasetObjects = response.output.getJSONArray(JSON_DATASET_OBJECTS);
                        for (int i = 0 ; i < jsonDatasetObjects.length(); i++) {
                            JSONObject jsonDatasetObject = jsonDatasetObjects.getJSONObject(i);
                            DatasetObject datasetObject = new DatasetObject(UUID.fromString(jsonDatasetObject.getString(JSON_DATASET_OBJECT_UUID)));
                            datasetObject.setTourDataset(tourDataset);
                            datasetObject.setTitle(TextHelper.getText(jsonDatasetObject.getInt(JSON_DATASET_OBJECT_TITLE_TEXT_ID)));
                            datasetObject.setLocation(jsonDatasetObject.getString(JSON_DATASET_OBJECT_LOCATION));
                            datasetObject.setArtist(DataProvider.getPeople(UUID.fromString(jsonDatasetObject.getString(JSON_DATASET_OBJECT_ARTIST_UUID))));
                            datasetObject.setStyle(TextHelper.getText(jsonDatasetObject.getInt(JSON_DATASET_OBJECT_STYLE_TEXT_ID)));
                            datasetObject.setYear(jsonDatasetObject.getInt(JSON_DATASET_OBJECT_YEAR));
                            datasetObjects.add(datasetObject);
                        }
                        tourDataset.setDatasetObjects(datasetObjects);
                    } catch (JSONException e) {
                        Log.e("cdv", "Error", e);
                        return;
                    }
                }
                List<TourDataset> tourDatasets = new ArrayList<>(1);
                tourDatasets.add(tourDataset);
                loadTourDatasetListener.tourDatasetsLoaded(tourDatasets);
            }
        };
        new HttpRequestAsync(createURL(GET_TOUR_DATASET_OBJECTS_SCRIPT_URI, tourDataset.getTourDatasetUuid(), null, null, null, null, listener), null, listener).execute();
    }

    public static void getAvailableTours(UUID tourDatasetUuid, final LoadUuidListItemsListener loadUuidListItemsListener) {
        HttpRequestListener listener = new HttpRequestListener() {
            @Override
            public void finished(Response response) {
                List<Tour> tours = new ArrayList<>();
                if (response.success) {
                    try {
                        tours.addAll(createToursFromJSON(response.output.getJSONArray("tours")));
                    } catch (JSONException e) {
                        Log.e("cdv", "Cannot convert JSON to tour", e);
                    }
                }
                loadUuidListItemsListener.uuidListItemsLoaded(tours);
            }
        };
        new HttpRequestAsync(createURL(GET_AVAILABLE_TOURS_SCRIPT_URI, tourDatasetUuid, null, null, null, null, listener), null, listener).execute();
    }

    private static Collection<Tour> createToursFromJSON(JSONArray jsonTours) {
        List<Tour> tours = new ArrayList<>(jsonTours.length());
        for (int i = 0 ; i < jsonTours.length(); i++) {
            JSONObject jsonTour = null;
            try {
                jsonTour = jsonTours.getJSONObject(i);
                Tour tour = new Tour(UUID.fromString(jsonTour.getString(JSON_TOUR_UUID)));
                tour.setTourDataset(DataProvider.getTourDataset(UUID.fromString(jsonTour.getString(JSON_TOUR_DATASET_UUID))));
                tour.setTitle(TextHelper.getText(jsonTour.getInt(JSON_TOUR_TITLE_TEXT_ID)));
                tour.setDescription(TextHelper.getText(jsonTour.getInt(JSON_TOUR_DESCRIPTION_TEXT_ID)));
                tour.setStatus(Tour.Status.valueOf(jsonTour.getString(JSON_TOUR_STATUS)));
                tour.setAuthor(DataProvider.getUser(UUID.fromString(jsonTour.getString(JSON_TOUR_AUTHOR_USER_UUID))));
                tour.setAccessLevel(Tour.AccessLevel.valueOf(jsonTour.getString(JSON_TOUR_ACCESS_LEVEL)));
                String tourIconUuidString = jsonTour.getString(JSON_TOUR_ICON_UUID);
                if (tourIconUuidString != null && !tourIconUuidString.isEmpty()) {
                    tour.setIconUuid(UUID.fromString(tourIconUuidString));
                }
                tours.add(tour);
            } catch (JSONException e) {
                Log.e("cdv", "JSON tour error", e);
            }
        }
        return tours;
    }

    public static void getMyTours(final LoadToursListener loadToursListener) {
        HttpRequestListener listener = new HttpRequestListener() {
            @Override
            public void finished(Response response) {
                List<Tour> tours = new ArrayList<>();
                if (response.success) {
                    try {
                        tours.addAll(createToursFromJSON(response.output.getJSONArray("tours")));
                    } catch (JSONException e) {
                        Log.e("cdv", "Cannot convert JSON to tour", e);
                    }
                }
                loadToursListener.toursLoaded(tours);
            }
        };
        new HttpRequestAsync(createURL(GET_MY_TOURS_SCRIPT_URI, null, null, null, null, null, listener), null, listener).execute();
    }

    public static void getDashboardTours(UUID tourDatasetUuid, final LoadToursListener loadToursListener) {
        HttpRequestListener listener = new HttpRequestListener() {
            @Override
            public void finished(Response response) {
                List<Tour> tours = new ArrayList<>();
                if (response.success) {
                    try {
                        tours.addAll(createToursFromJSON(response.output.getJSONArray("tours")));
                    } catch (JSONException e) {
                        Log.e("cdv", "Cannot convert JSON to tour", e);
                    }
                }
                loadToursListener.toursLoaded(tours);
            }
        };
        new HttpRequestAsync(createURL(GET_DASHBOARD_TOURS_SCRIPT_URI, tourDatasetUuid, null, null, null, null, listener), null, listener).execute();
    }

    public static Map<Integer, String> createTextsFromJson(JSONArray jsonTexts) throws JSONException {
        Map<Integer, String> texts = new HashMap<>();
        for (int i = 0 ; i < jsonTexts.length(); i++) {
            JSONObject jsonText = jsonTexts.getJSONObject(i);
            texts.put(jsonText.getInt(JSON_TEXT_ID), jsonText.getString(JSON_TEXT));
        }
        return texts;
    }

    public static Map<UUID, Integer> createRatingsFromJson(JSONArray jsonRatings) throws JSONException {
        Map<UUID, Integer> ratings = new HashMap<>();
        for (int i = 0 ; i < jsonRatings.length(); i++) {
            JSONObject jsonRating = jsonRatings.getJSONObject(i);
            UUID itemUuid = UUID.fromString(jsonRating.getString(JSON_RATING_ITEM_UUID));
            Integer ratingValue = jsonRating.getInt(JSON_RATING);
            ratings.put(itemUuid, ratingValue);
        }
        return ratings;
    }

    public static void loadLanguage(LanguageCode languageCode, final LoadTextListener loadTextListener) {
        HttpRequestListener listener = new HttpRequestListener() {
            @Override
            public void finished(Response response) {
                Map<Integer, String> texts = new HashMap<>();
                if (response.success) {
                    try {
                        JSONArray jsonTexts = response.output.getJSONArray("texts");
                        texts.putAll(createTextsFromJson(jsonTexts));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        response.success = false;
                        response.message = e.getLocalizedMessage();
                    }
                }
                loadTextListener.textsLoaded(response.success, texts);
            }
        };
        new HttpRequestAsync(createURL(GET_TEXTS_SCRIPT_URI, null, null, null, null, "languageCode=" + languageCode, listener), null, listener).execute();
    }

    public static void saveTour(Tour tour, boolean saveTourObjects, HttpRequestListener listener) {
        try {
            JSONObject postJSONObject = new JSONObject();
            postJSONObject.put(JSON_TOUR_UUID, tour.getTourUuid().toString());
            postJSONObject.put(JSON_TOUR_DATASET_UUID, tour.getTourDataset().getTourDatasetUuid().toString());
            postJSONObject.put(JSON_TOUR_TITLE, tour.getTitle());
            postJSONObject.put(JSON_TOUR_DESCRIPTION, tour.getDescription());
            postJSONObject.put(JSON_TOUR_AUTHOR_USER_UUID, tour.getAuthor().getUserUuid().toString());
            postJSONObject.put(JSON_TOUR_STATUS, tour.getStatus().toString());
            postJSONObject.put(JSON_TOUR_ACCESS_LEVEL, tour.getAccessLevel().toString());
            postJSONObject.put(JSON_TOUR_ICON_UUID, tour.getIconUuid() != null ? tour.getIconUuid().toString() : "");
            if (saveTourObjects) {
                JSONArray jsonTourObjects = new JSONArray();
                for (DatasetObject datasetObject : tour.getDatasetObjects()) {
                    JSONObject jsonTourObject = new JSONObject();
                    jsonTourObject.put(JSON_DATASET_OBJECT_UUID, datasetObject.getDatasetObjectUuid());
                    jsonTourObjects.put(jsonTourObject);
                }
                postJSONObject.put(JSON_DATASET_OBJECT_UUIDS, jsonTourObjects);
            }
            new HttpRequestAsync(createURL(SAVE_TOUR_SCRIPT_URI, null, tour.getTourUuid(), null, null, "languageCode=" + TextHelper.getLoadedLanguage(), listener), postJSONObject, listener).execute();
        } catch (JSONException e) {
            e.printStackTrace();
            sendErrorResponse(e.getLocalizedMessage(), listener);
        }
    }

    public static void deleteTour(final Tour tour, final DeleteTourListener deleteTourListener) {
        HttpRequestListener listener = new HttpRequestListener() {
            @Override
            public void finished(Response response) {
                if (!response.success) {
                    deleteTourListener.errorOccured();
                    return;
                }
                deleteTourListener.tourDeleted(tour);
            }
        };
        new HttpRequestAsync(createURL(DELETE_TOUR_SCRIPT_URI, null, tour.getTourUuid(), null, null, null, listener), null, listener).execute();
    }

    public static void saveObjectRating(UUID userUuid, UUID itemUuid, int rating, final RatingListener ratingListener) {
        HttpRequestListener listener = new HttpRequestListener() {
            @Override
            public void finished(Response response) {
                if (!response.success) {
                    // drop error
                    return;
                }
                ratingListener.rated();
            }
        };
        String params = "userUuid=" + userUuid.toString() + "&itemUuid=" + itemUuid + "&rating=" + String.valueOf(rating);
        new HttpRequestAsync(createURL(SAVE_RATING_SCRIPT_URI, null, null, null, null, params, listener), null, listener).execute();
    }

    public static void saveUser(String name, final SaveUserListener saveUserListener) {
        HttpRequestListener listener = new HttpRequestListener() {
            @Override
            public void finished(Response response) {
                if (!response.success) {
                    // drop error
                    return;
                }
                User.Role role;
                try {
                    role = User.Role.valueOf(response.output.getString(JSON_USER_ROLE));
                } catch (Exception e) {
                    Log.i("cdv", "error getting user role", e);
                    role = User.Role.USER;
                }
                saveUserListener.userSaved(role);
            }
        };
        String params = "userName=" + name;
        new HttpRequestAsync(createURL(SAVE_USER_SCRIPT_URI, null, null, null, null, params, listener), null, listener).execute();
    }


    public static void getRatingsUser(UUID userUuid, final LoadRatingListener loadRatingListener) {
        HttpRequestListener listener = new HttpRequestListener() {
            @Override
            public void finished(Response response) {
                Map<UUID, Integer> ratings = new HashMap<>();
                if (response.success) {
                    try {
                        JSONArray jsonRatings = response.output.getJSONArray(JSON_RATINGS);
                        ratings = createRatingsFromJson(jsonRatings);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        response.success = false;
                        response.message = e.getLocalizedMessage();
                    }
                }
                loadRatingListener.ratingsLoaded(ratings);
            }
        };
        String params = "userUuid=" + userUuid.toString();
        new HttpRequestAsync(createURL(GET_RATINGS_USER_SCRIPT_URI, null, null, null, null, params, listener), null, listener).execute();
    }

    private static void sendErrorResponse(String logText, HttpRequestListener listener) {
        Log.e("cdv", logText);
        if (listener == null) {
            return;
        }
        HttpHelper.Response response = new HttpHelper.Response();
        response.success = false;
        response.output = new JSONObject();
        listener.finished(response);
    }

    public static URL createURL(String serverUri, UUID tourDatasetUuid, UUID tourUuid, UUID datasetObjectUuid, UUID tourObjectUuid, String params, final HttpRequestListener listener) {

        try {
            return new URL(serverUri + "?"
                    + "userUuid=" + DataProvider.getMyUserUuidStr()
                    + (tourDatasetUuid != null ? "&tourDatasetUuid=" + tourDatasetUuid : "")
                    + (tourUuid != null ? "&tourUuid=" + tourUuid : "")
                    + (datasetObjectUuid != null ? "&datasetObjectUuid=" + datasetObjectUuid : "")
                    + (tourObjectUuid != null ? "&tourObjectUuid=" + tourObjectUuid : "")
                    + (params != null ? "&" + params : "")
            );
        } catch (Exception e) {
            final Response response = new Response();
            response.message = e.getMessage();
            Log.e("cdv", "Exception: " + response.httpCode + " - " + response.message + " '" + response.output + "'", e);
            new Runnable() {
                @Override
                public void run() {
                    listener.finished(response);
                }
            }.run();
        }
        return null;
    }

    private static String urlEncode(String text) {
        try {
            return URLEncoder.encode(text, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return text;
        }
    }

    public static Uri getPreviewImageUri(TourDataset tourDataset) {
        return Uri.parse(HTTP_SERVER + HTTP_DIRECTORY_NAME_IMAGES + tourDataset.getTourDatasetUuid().toString() + "/" + HTTP_NAME_DATASET_PREVIEW);
    }

    public static Uri getPreviewImageUri(DatasetObject datasetObject) {
        return Uri.parse(HTTP_SERVER + HTTP_DIRECTORY_NAME_IMAGES + datasetObject.getTourDataset().getTourDatasetUuid().toString() + "/" + HTTP_DIRECTORY_NAME_PREVIEWS + datasetObject.getDatasetObjectUuid().toString() + ".jpg");
    }

    public static Uri getPreviewImageUri(Tour tour) {
        return Uri.parse(HTTP_SERVER + HTTP_DIRECTORY_NAME_IMAGES + tour.getTourDataset().getTourDatasetUuid().toString() + "/" + HTTP_DIRECTORY_NAME_PREVIEWS + tour.getIconUuid().toString() + ".jpg");
    }

    public static Uri getMapImageUri(TourDataset tourDataset, int mapIndex) {
        UUID mapUuid = tourDataset.getTourDatasetMaps().get(mapIndex).getTourDatasetMapUuid();
        return Uri.parse(HTTP_SERVER + HTTP_DIRECTORY_NAME_IMAGES + tourDataset.getTourDatasetUuid().toString() + "/" + HTTP_DIRECTORY_NAME_MAPS + mapUuid + ".jpg");
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void downloadFile(Context context, Uri uri, File file, final HttpRequestListener listener) {
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("Download: " + uri.getPath());
        request.setDescription("Destination: " + file.getAbsolutePath());
        request.setVisibleInDownloadsUi(false);
        request.setDestinationUri(Uri.fromFile(file));
        //request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        if (receiverRegistered.compareAndSet(false, true)) {
            context.getApplicationContext().registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            Log.i("cdv", "Registered download receiver context: " + context.toString());
        } else {
            Log.i("cdv", "Download receiver context already registered: " + context.toString());
        }

        // enqueue download
        DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        Long downloadId = new Long(downloadManager.enqueue(request));
        Log.i("cdv", "download: " + uri.toString());
        Log.i("cdv", "download id: " + downloadId);
        activeDownloads.put(downloadId, new ImageDownload(downloadManager, new HttpRequestListener() {
            @Override
            public void finished(Response response) {
                if (!response.success) {
                    Log.e("cdv", "Error download: " + response.httpCode + " - " + response.message);
                }
                listener.finished(response);
            }
        }));
    }

    private static String getMimeType(String fileName) {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(fileName));
    }

    public interface HttpRequestListener {
        void finished(Response response);
    }

    public static class Response {
        public boolean success = false;
        public int httpCode = -1;
        public String message = "";
        public JSONObject output = null;
    }

    private static class HttpRequestAsync extends AsyncTask<Void, Void, Response> {
        private final JSONObject postJSONObject;
        URL url;
        private HttpRequestListener listener;

        public HttpRequestAsync(URL url, JSONObject postJSONObject, HttpRequestListener listener) {
            this.url = url;
            this.postJSONObject = postJSONObject;
            this.listener = listener;
        }

        @Override
        protected Response doInBackground(Void... unused) {
            Response response = new Response();
            BufferedReader reader = null;
            String output = "";
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection)url.openConnection();
                Log.i("cdv", url.toString());
                if (postJSONObject != null) {
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(postJSONObject.toString());
                    Log.d("cdv", postJSONObject.toString());
                    writer.flush();
                    writer.close();
                    os.close();
                }
                response.httpCode = conn.getResponseCode();
                response.message = conn.getResponseMessage();
                if (response.httpCode != 200) {
                    reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                } else {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                }
                String line;
                while ((line = reader.readLine()) != null) {
                    output += line + "\n";
                }
                Log.i("cdv", "Request response: " + response.httpCode + " - " + response.message + " '" + output + "'");
                if (output.length() > 100) {
                    Log.i("cdv", "end of response is: '" + output.substring(output.length() - 50) + "'");
                }
                // sometimes the last char is missing...why? :-(
                if (output.charAt(output.length() - 1) != '}') {
                    output += "}";
                }
                response.output = new JSONObject(output);

                if (response.httpCode == 200) {
                    response.success = true;
                }
            } catch (Throwable e) {
                Log.e("cdv", "Exception: " + response.httpCode + " - " + response.message + " '" + e.getLocalizedMessage() + "'", e);
            } finally {
                try {
                    reader.close();
                    if (conn != null) {
                        conn.disconnect();
                    }
                } catch (Exception e) {
                }
            }

            if (!response.success) {
                //NoteManager.showError(context, );
            }
            return(response);
        }

        @Override
        protected void onPostExecute(Response response) {
            if (listener != null) {
                listener.finished(response);
            }
        }
    }

}
