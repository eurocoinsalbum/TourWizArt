package codingdavinci.tour.filter;

import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

import codingdavinci.tour.model.Tour;

public class TourFilter implements Filter<Tour> {
    private String title;
    private Set<String> accessLevels;
    private SharedPreferences settings;

    public TourFilter(SharedPreferences settings) {
        this.settings = settings;

        refresh();
    }

    public boolean accept(Tour tour) {
        if (!contains(tour.getTitle(), title)) {
            return false;
        }

        if (!contains(tour.getAccessLevel(), accessLevels)) {
            return false;
        }

        return true;
    }

    @Override
    public void clear() {
        SharedPreferences.Editor editor = settings.edit();
        editor.remove("pref_filter_tour_title");
        editor.remove("pref_filter_tour_access_level");
        editor.commit();

        refresh();
    }

    @Override
    public void refresh() {
        title = settings.getString("pref_filter_tour_title", "");
        accessLevels = settings.getStringSet("pref_filter_tour_access_level", null);
        if (accessLevels == null) {
            accessLevels = new HashSet<>();
            accessLevels.add(Tour.AccessLevel.PUBLIC.toString());
        }
    }

    private boolean contains(String str1, String str2) {
        if (str2 == null) {
            return true;
        }

        if (str1 != null) {
            return str1.toUpperCase().contains(str2.toUpperCase());
        }

        return false;
    }

    private boolean contains(Enum<?> e, Set<String> values) {
        if (values == null) {
            return false;
        }

        if (e == null) {
            return true;    // just in case the enum is not set
        }

        return values.contains(e.name());
    }
}
