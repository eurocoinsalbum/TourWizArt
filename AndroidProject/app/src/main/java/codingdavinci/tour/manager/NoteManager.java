package codingdavinci.tour.manager;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class NoteManager {
    public static void showError(Context context, int resourceId) {
        Toast.makeText(context, resourceId, Toast.LENGTH_LONG).show();
    }

    public static void showErrors(Context context, List<Integer> resourceIds) {
        String message = "";
        for (int resourceId : resourceIds) {
            if (!message.isEmpty()) {
                message += "\n";
            }
            message += context.getString(resourceId);
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showSnack(View rootLayout, int resourceId) {
        Snackbar.make(rootLayout, resourceId, Snackbar.LENGTH_LONG).show();
    }
}
