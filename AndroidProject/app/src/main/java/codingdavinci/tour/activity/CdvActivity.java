package codingdavinci.tour.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import codingdavinci.tour.R;
import codingdavinci.tour.listener.PermissionListener;
import codingdavinci.tour.manager.DataProvider;
import codingdavinci.tour.util.ActivityHelper;
import codingdavinci.tour.util.HttpHelper;

public abstract class CdvActivity extends AppCompatActivity {
    private static final String BLOOMBERG_PHILANTHROPY_URL = "http://www.bloomberg.org";
    private static final String ICONS8_URL = "https://icons8.de";
    private static final String TOURWIZART_STATISTICS = "http://cdv.homepage-master.de/statistics.php";
    private static final String DATA_PRIVACY_URL = HttpHelper.HTTP_SERVER + "dataPrivacy.html";
    private static final String IMPRESSUM_URL = HttpHelper.HTTP_SERVER + "impressum.html";

    private PermissionListener permissionListener;
    private Class<CdvActivity> previousActivityClass;
    protected abstract void initUiObjects();
    protected abstract void buildScreen();

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //xml inflation for option menu
        getMenuInflater().inflate(R.menu.main, menu);

        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            //noinspection RestrictedApi
            m.setOptionalIconsVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // use tmp object to avoid overlapping permission requests during callback of permissionGranted
        PermissionListener listener = permissionListener;
        permissionListener = null;
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            listener.permissionGranted();
        }
    }

    public void setRequestPermissionListener(PermissionListener permissionListener) {
        Log.i("cdv", "set permission listener: " + permissionListener.hashCode());
        this.permissionListener = permissionListener;
    }

    public Class<CdvActivity> getPreviousActivityClass() {
        return previousActivityClass;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // Handle presses on the action bar items
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                ActivityHelper.startCdvActivity(this, MainActivity.class);
                break;

            case R.id.settings:
                startActivity(new Intent(this, AppPreferenceActivity.class));
                break;

            case R.id.bloomberg_philantropy:
                HttpHelper.showWebSite(this, BLOOMBERG_PHILANTHROPY_URL);
                break;

            case R.id.icons8:
                HttpHelper.showWebSite(this, ICONS8_URL);
                break;

            case R.id.statistics:
                HttpHelper.showWebSite(this, TOURWIZART_STATISTICS + "?userUuid=" + DataProvider.getMyUserUuidStr());
                break;

            case R.id.impressum:
                HttpHelper.showWebSite(this, IMPRESSUM_URL);
                break;

            case R.id.data_privacy:
                HttpHelper.showWebSite(this, DATA_PRIVACY_URL);
                break;

            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                break;

/*
            case R.id.Map:
                startActivity(new Intent(this, StolpersteineMapActivity.class));
                break;
            case R.id.use_fragment:
                startActivity(new Intent(this, TestMapNavigationFragmentActivity.class));
                break;
*/
            default:
                return (super.onOptionsItemSelected(menuItem));
        }

        return true;
    }

    protected void buildActionBar() {
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon8_ic_launcher_tourwzart_round);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean hasRequestPermissionListener() {
        return permissionListener != null;
    }

    public void updateModelFromUI() {
    }
}