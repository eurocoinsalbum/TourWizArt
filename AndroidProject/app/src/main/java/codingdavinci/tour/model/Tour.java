package codingdavinci.tour.model;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import codingdavinci.tour.activity.CdvActivity;
import codingdavinci.tour.listener.LoadImageListener;
import codingdavinci.tour.manager.DataProvider;

/**
 * This is a tour which was built by the author from the data of a TourDataset
 */
public class Tour implements UuidListItem {
    private UUID tourUuid;
    private TourDataset tourDataset;
    private String title;
    private String description;
    private UUID iconUuid;
    private User author;
    private Status status;
    private Date created;
    private Date lastUpdate;
    private AccessLevel accessLevel;
    private List<DatasetObject> datasetObjects;

    // statistics
    private int statCopied;
    private int statTaken;
    private float avgRating;

    public Tour(UUID tourUuid) {
        this.tourUuid = tourUuid;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean hasIcon() {
        return iconUuid != null;
    }

    public UUID getIconUuid() {
        return iconUuid;
    }

    public void setIconUuid(UUID iconUuid) {
        this.iconUuid = iconUuid;
    }

    public boolean contains(DatasetObject datasetObject) {
        return datasetObjects.contains(datasetObject);
    }

    public enum Status {
        ACTIVE, INACTIVE, DRAFT, DELETED
    }

    public enum AccessLevel {
        PUBLIC, FRIENDS, PRIVATE
    }

    public UUID getTourUuid() {
        return tourUuid;
    }

    public TourDataset getTourDataset() {
        return tourDataset;
    }

    public void setTourDataset(TourDataset tourDataset) {
        this.tourDataset = tourDataset;
    }

    @Override
    public UUID getUuid() {
        return tourUuid;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void getIcon(CdvActivity cdvActivity, LoadImageListener loadImageListener) throws IOException {
        DataProvider.getPreviewImage(cdvActivity, this, ICON_WIDTH, loadImageListener);
    }

    public void getPreview(CdvActivity cdvActivity, LoadImageListener loadImageListener) throws IOException {
        DataProvider.getPreviewImage(cdvActivity, this, 0, loadImageListener);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public boolean isTourObjectsLoaded() {
        return datasetObjects != null;
    }

    /**
     * @return null if the tour has not been fully loaded via DataProvider
     */
    public List<DatasetObject> getDatasetObjects() {
        if (datasetObjects == null) {
            return null;
        }
        return datasetObjects;
    }

    public void setDatasetObjects(List<DatasetObject> datasetObjects) {
        this.datasetObjects = new ArrayList<>(datasetObjects);
        checkAndUpdateTourIcon();
    }

    public void addDatasetObject(DatasetObject datasetObject) {
        datasetObjects.add(datasetObject);
        checkAndUpdateTourIcon();
    }

    public void removeDatasetObject(DatasetObject datasetObject) {
        if (!datasetObjects.remove(datasetObject)) {
            Log.e("cdv", "Remove of datasetObject from tour failed");
        }
        checkAndUpdateTourIcon();
    }

    public void checkAndUpdateTourIcon() {
        DatasetObject iconDatasetObject = tourDataset.getDatasetObject(iconUuid);
        if (contains(iconDatasetObject)) {
            // tour icon still in tour list
            return;
        }
        if (datasetObjects.isEmpty()) {
            // let tour icon untouched as it was the last in the list
            return;
        }
        // set tour icon to first object of tour
        iconUuid = datasetObjects.get(0).getUuid();
    }

    public void moveDatasetObjectOneToFront(DatasetObject datasetObject) {
        int index = datasetObjects.indexOf(datasetObject);
        if (index == -1 || index == 0) {
            // not found or already at top
            return;
        }
        datasetObjects.remove(index);
        datasetObjects.add(index - 1, datasetObject);
    }

    public void moveDatasetObjectOneToEnd(DatasetObject datasetObject) {
        int index = datasetObjects.indexOf(datasetObject);
        if (index == -1 || index == datasetObjects.size() - 1) {
            // not found or already at end
            return;
        }
        datasetObjects.remove(index);
        datasetObjects.add(index + 1, datasetObject);
    }

    public int getStatCopied() {
        return statCopied;
    }

    public void setStatCopied(int statCopied) {
        this.statCopied = statCopied;
    }

    public int getStatTaken() {
        return statTaken;
    }

    public void setStatTaken(int statTaken) {
        this.statTaken = statTaken;
    }

    public float getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(float avgRating) {
        this.avgRating = avgRating;
    }
}
