package codingdavinci.tour.view;

import android.graphics.Bitmap;

import java.util.UUID;

public class DashButtonDef {
    public Bitmap bitmap;
    public int imageResourceId;
    public String text;
    public int textResourceId;
    public Class<?> targetActivity;
    public String prefKey;
    public UUID prefUuid;
    public String dataProvider;
    public String dataPackage;

    public DashButtonDef(Bitmap bitmap, int imageResourceId, String dataProvider, String dataPackage, String text, int textResourceId, Class<?> targetActivity, String prefKey, UUID prefUuid) {
        this.bitmap = bitmap;
        this.imageResourceId = imageResourceId;
        this.text = text;
        this.dataProvider = dataProvider;
        this.dataPackage = dataPackage;
        this.textResourceId = textResourceId;
        this.targetActivity = targetActivity;
        this.prefKey = prefKey;
        this.prefUuid = prefUuid;
    }
}
