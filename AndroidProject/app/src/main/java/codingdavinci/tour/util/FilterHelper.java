package codingdavinci.tour.util;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import codingdavinci.tour.R;
import codingdavinci.tour.filter.Filter;
import codingdavinci.tour.filter.TourFilter;
import codingdavinci.tour.model.TourDataset;

public class FilterHelper {

    private FilterHelper() {
    }

    public static int getPreferencesResourceId(TourDataset tourDataset) {
        switch (tourDataset.getTourType()) {
            case MUSEUM:
                return R.xml.filter_tour_preferences;

            case CITY:
                return R.xml.filter_tour_preferences;

            default:
                return -1;
        }
    }

    public static TourFilter createTourFilter(SharedPreferences settings) {
        return new TourFilter(settings);
    }

    public static <T> List<T> filter(List<T> list, Filter<T> filter) {
        List<T> filteredList = new ArrayList<>();

        for(T t : list) {
            if (filter.accept(t)) {
                filteredList.add(t);
            }
        }

        return filteredList;
    }
}
