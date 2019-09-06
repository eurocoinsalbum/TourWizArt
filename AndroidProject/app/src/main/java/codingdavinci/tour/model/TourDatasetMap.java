package codingdavinci.tour.model;

import java.util.UUID;

public class TourDatasetMap {
    private TourDataset tourDataset;
    private UUID tourDatasetMapUuid ;
    private String title;
    private GpsMapLocation gpsMapLocation;

    public TourDatasetMap(TourDataset tourDataset, UUID tourDatasetMapUuid) {
        this.tourDataset = tourDataset;
        this.tourDatasetMapUuid = tourDatasetMapUuid;
    }

    public TourDataset getTourDataset() {
        return tourDataset;
    }

    public UUID getTourDatasetMapUuid() {
        return tourDatasetMapUuid ;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public GpsMapLocation getGpsMapLocation() {
        return gpsMapLocation;
    }

    public void setGpsMapLocation(GpsMapLocation gpsMapLocation) {
        this.gpsMapLocation = gpsMapLocation;
    }
}
