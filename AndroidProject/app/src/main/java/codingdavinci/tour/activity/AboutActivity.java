package codingdavinci.tour.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import codingdavinci.tour.R;

public class AboutActivity extends AppCompatActivity {

    private static final String TAG = "AboutActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Log.d(TAG, "OnCreate: started");
        //ImageView firstImage = (ImageView) findViewById(R.id.aboutImageView);
        //int imageResource = getResources().getIdentifier("@drawable/about.gif", null, this.getPackageName());
        //firstImage.setImageResource(imageResource);
    }
}
