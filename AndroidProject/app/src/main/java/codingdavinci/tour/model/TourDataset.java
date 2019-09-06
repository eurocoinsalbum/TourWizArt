package codingdavinci.tour.model;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import codingdavinci.tour.activity.CdvActivity;
import codingdavinci.tour.listener.LoadImageListener;
import codingdavinci.tour.manager.DataProvider;

/**
 * This class represents the underlying data (all images of a museum, ...)
 * which users can use to build a tour
 */
public class TourDataset implements UuidListItem {
    private UUID tourDatasetUuid;
    private String title;
    private String description;
    private TourType tourType;
    private User owner;
    private Status status = Status.ACTIVE;
    private LocationType locationType;
    private Map<UUID, DatasetObject> datasetObjects;
    private List<TourDatasetMap> tourDatasetMaps;

    // statistics
    private int statTaken;
    private int statCreatedTours;
    private int statAvailableTours;

    public enum Status {
        ACTIVE, INACTIVE, DRAFT, DELETED
    }

    public enum TourType {
        MUSEUM, CITY
    }

    public enum LocationType {
        ADDRESS, PIXEL, GPS, GPSMAP
    }

    public TourDataset(UUID tourDatasetUuid) {
        this.tourDatasetUuid = tourDatasetUuid;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRealTitle() {
        return title;
    }

    @Override
    public UUID getUuid() {
        return tourDatasetUuid;
    }

    @Override
    public String getTitle() {
        return owner.getName();
    }

    @Override
    public String getDescription() {
        return title;
    }

    @Override
    public void getIcon(CdvActivity cdvActivity, LoadImageListener loadImageListener) throws IOException {
        DataProvider.getPreviewImage(cdvActivity, this, ICON_WIDTH, loadImageListener);
    }

    @Override
    public boolean hasIcon() {
        return true;
    }

    public UUID getTourDatasetUuid() {
        return tourDatasetUuid;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TourType getTourType() {
        return tourType;
    }

    public void setTourType(TourType tourType) {
        this.tourType = tourType;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    public void setDatasetObjects(Collection<DatasetObject> datasetObjects) {
        this.datasetObjects = new HashMap<>();
        for (DatasetObject datasetObject : datasetObjects) {
            this.datasetObjects.put(datasetObject.getDatasetObjectUuid(), datasetObject);
        }
    }

    public boolean isDatasetObjectsLoaded() {
        return datasetObjects != null;
    }

    public DatasetObject getDatasetObject(UUID datasetObjectUuid) {
        if (datasetObjects == null) {
            return null;
        }
        return datasetObjects.get(datasetObjectUuid);
    }

    /**
     * @return null if the tour has not been fully loaded via DataProvider
     */
    public Collection<DatasetObject> getDatasetObjects() {
        if (datasetObjects == null) {
            return null;
        }
        return datasetObjects.values();
    }

    public void setTourDatasetMaps(List<TourDatasetMap> tourDatasetMaps) {
        this.tourDatasetMaps = tourDatasetMaps;
    }

    public List<TourDatasetMap> getTourDatasetMaps() {
        return tourDatasetMaps;
    }

}
