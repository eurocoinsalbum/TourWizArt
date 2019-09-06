package codingdavinci.tour.filter;

import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

import codingdavinci.tour.model.DatasetObject;
import codingdavinci.tour.model.Tour;

public class DatasetObjectFilter implements Filter<DatasetObject> {
    private String title;
    private Set<String> statusList;
    private SharedPreferences settings;

    public DatasetObjectFilter(SharedPreferences settings) {
        this.settings = settings;

        refresh();
    }

    @Override
    public boolean accept(DatasetObject obj) {
        if (!contains(obj.getRealTitle(), title) && !contains(obj.getArtist().name, title)) {
            return false;
        }

        if (!contains(obj.getStatus(), statusList)) {
            return false;
        }

        return true;
    }

    @Override
    public void clear() {
        SharedPreferences.Editor editor = settings.edit();
        editor.remove("pref_filter_datasetobject_title");
        editor.remove("pref_filter_datasetobject_status");
        editor.commit();

        refresh();
    }

    @Override
    public void refresh() {
        title = settings.getString("pref_filter_datasetobject_title", "");
        statusList = settings.getStringSet("pref_filter_datasetobject_status", null);

        if (statusList == null) {
            statusList = new HashSet<>();
            statusList.add(Tour.Status.ACTIVE.toString());
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
