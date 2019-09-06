package codingdavinci.tour.util;

import android.preference.MultiSelectListPreference;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PreferencesHelper {
    public static final String PREF_KEY_LANGUAGE = "pref_language";
    public static final String PREF_KEY_GENDER = "pref_gender";
    public static final String PREF_KEY_AGE = "pref_age";
    public static final String PREF_KEY_SHOW_ONLY_STAEDEL = "pref_show_only_staedel";
    public static final String PREF_KEY_MY_USER_UUID = "pref_my_user_uuid";

    private PreferencesHelper() {
    }

    public static String getSummary(MultiSelectListPreference preference) {
        // human readable
        CharSequence[] entries = preference.getEntries();

        // values to be stored
        CharSequence[] entryValues = preference.getEntryValues();

        Map<String, String> map = new HashMap<>();
        if (entries.length > 0 && entryValues.length > 0 && entries.length == entryValues.length) {

            for (int i=0; i<entries.length; i++) {
                map.put(entryValues[i].toString(),  entries[i].toString());
            }
        }

        // current selected values
        Set<String> values = preference.getValues();

        StringBuilder builder = new StringBuilder();

        for (String value : values) {
            String v = map.get(value);
            if (v != null) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }

                builder.append(v);
            }
        }

        return builder.toString();
    }
}
