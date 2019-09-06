package codingdavinci.tour.model;

import java.io.IOException;
import java.util.UUID;

import codingdavinci.tour.activity.CdvActivity;
import codingdavinci.tour.listener.LoadImageListener;
import codingdavinci.tour.manager.DataProvider;

public class DatasetObject implements UuidListItem {
    public enum Status {
        ACTIVE, INACTIVE
    }

    private UUID datasetObjectUuid;
    private TourDataset tourDataset;
    private String title;
    private String location;
    private People artist;
    private String style;
    private int year;
    private DatasetObject.Status status;

    public DatasetObject(UUID datasetObjectUuid) {
        this.datasetObjectUuid = datasetObjectUuid;
    }

    @Override
    public UUID getUuid() {
        return datasetObjectUuid;
    }

    public UUID getDatasetObjectUuid() {
        return datasetObjectUuid;
    }

    public TourDataset getTourDataset() {
        return tourDataset;
    }

    public void setTourDataset(TourDataset tourDataset) {
        this.tourDataset = tourDataset;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public People getArtist() {
        return artist;
    }

    public void setArtist(People artist) {
        this.artist = artist;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getRealTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return getRealTitle();
    }

    @Override
    public String getTitle() {
        return artist.name;
    }

    @Override
    public void getIcon(CdvActivity cdvActivity, LoadImageListener loadImageListener) throws IOException {
        DataProvider.getPreviewImage(cdvActivity, this, ICON_WIDTH, loadImageListener);
    }

    @Override
    public boolean hasIcon() {
        return true;
    }
}
