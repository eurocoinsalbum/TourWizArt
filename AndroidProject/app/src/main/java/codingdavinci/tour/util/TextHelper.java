package codingdavinci.tour.util;

import java.util.HashMap;
import java.util.Map;

import codingdavinci.tour.listener.LoadTextListener;
import codingdavinci.tour.model.LanguageCode;

public class TextHelper {
    private static Map<Integer, String> texts = new HashMap<>();
    private static LanguageCode loadedLanguage;

    public static String getText(int textId) {
        if (!texts.containsKey(textId)) {
            return "";
        }
        return texts.get(textId);
    }

    public static LanguageCode getLoadedLanguage() {
        return loadedLanguage;
    }

    public static void loadLanguage(final LanguageCode languageCode, final LoadTextListener loadTextListener) {
        HttpHelper.loadLanguage(languageCode, new LoadTextListener() {
            @Override
            public void textsLoaded(boolean success, Map<Integer, String> texts) {
                if (success) {
                    loadedLanguage = languageCode;
                    TextHelper.texts = texts;
                }
                loadTextListener.textsLoaded(success, texts);
            }
        });
    }

    public static void addTexts(Map<Integer,String> additionalTexts) {
        texts.putAll(additionalTexts);
    }
}
