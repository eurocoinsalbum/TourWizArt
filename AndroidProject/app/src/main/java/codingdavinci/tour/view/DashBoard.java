package codingdavinci.tour.view;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.UUID;

import codingdavinci.tour.R;
import codingdavinci.tour.activity.CdvActivity;
import codingdavinci.tour.manager.NoteManager;
import codingdavinci.tour.util.ActivityHelper;

public class DashBoard {
    private static final int DASH_BUTTON_SIZE = 500;

    private final CdvActivity cdvActivity;
    private int colsPerRow;
    private int numberOfDashButtonsAdded = 0;
    private LinearLayout dashBoardView;
    private LinearLayout currentDashRow;

    public DashBoard(CdvActivity cdvActivity, int colsPerRow) {
        this.cdvActivity = cdvActivity;
        this.colsPerRow = colsPerRow;
        // layout
        dashBoardView = new LinearLayout(cdvActivity);
        dashBoardView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        dashBoardView.setOrientation(LinearLayout.VERTICAL);
    }

    public void addDashButton(final CdvActivity cdvActivity, DashButtonDef dashButtonDef) {
        if (numberOfDashButtonsAdded % colsPerRow == 0) {
            Log.i("cdv", "Adding dashboard row");
            currentDashRow = new LinearLayout(cdvActivity);
            dashBoardView.addView(currentDashRow);
            currentDashRow.setOrientation(LinearLayout.HORIZONTAL);
            currentDashRow.setGravity(Gravity.CENTER_HORIZONTAL);
            currentDashRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        View dashButtonView = cdvActivity.getLayoutInflater().inflate(R.layout.dashbutton, currentDashRow, false);
        ImageButton imageButton = dashButtonView.findViewById(R.id.image);

        // image
        if (dashButtonDef.bitmap != null) {
            imageButton.setImageBitmap(dashButtonDef.bitmap);
        } else {
            imageButton.setImageResource(dashButtonDef.imageResourceId);
        }
        imageButton.setColorFilter(0x99FFFFFF, PorterDuff.Mode.SRC_OVER);
        imageButton.setBackgroundColor(Color.TRANSPARENT);

        // text
        TextView textView = dashButtonView.findViewById(R.id.text);
        if (dashButtonDef.text != null) {
            textView.setText(dashButtonDef.text);
        } else {
            textView.setText(dashButtonDef.textResourceId);
        }

        ((TextView)dashButtonView.findViewById(R.id.data_provider)).setText(dashButtonDef.dataProvider);
        ((TextView)dashButtonView.findViewById(R.id.data_package)).setText(dashButtonDef.dataPackage);

        // some temporary final variable so that the dashButtonDeb object can be released immediately (because of Bitmap)
        final Class<?> targetActivity = dashButtonDef.targetActivity;
        final String prefKey = dashButtonDef.prefKey;
        final UUID prefUuid = dashButtonDef.prefUuid;
        // click callback
        dashButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (targetActivity == null) {
                    NoteManager.showError(cdvActivity, R.string.internal_error);
                    return;
                }
                ActivityHelper.startCdvActivity(cdvActivity, targetActivity, prefKey, prefUuid);
            }
        });

        Log.i("cdv", "Adding dash button " + textView.getText().toString());
        currentDashRow.addView(dashButtonView);
        numberOfDashButtonsAdded++;
    }

    public LinearLayout getView() {
        return dashBoardView;
    }
}
