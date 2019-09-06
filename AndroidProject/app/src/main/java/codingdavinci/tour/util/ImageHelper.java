package codingdavinci.tour.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import codingdavinci.tour.listener.PhotoListener;
import codingdavinci.tour.manager.DataProvider;
import codingdavinci.tour.view.Dimension;

public class ImageHelper {
    private static final int MY_REQUEST_CAMERA = 1;

    public static Bitmap resizeBitmap(Context context, Uri imageUri, int targetW) throws FileNotFoundException {
        // Decode the image file into a Bitmap sized to fill the View
        InputStream input = null;
        try {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = false;
            if (targetW > 0) {
                bmOptions.inSampleSize = getImageDimension(context, imageUri).width / targetW;
            }
            bmOptions.inPurgeable = true;
            input = context.getContentResolver().openInputStream(imageUri);
            return BitmapFactory.decodeStream(input, null, bmOptions);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Dimension getImageDimension(Context context, Uri imageUri) throws FileNotFoundException {
        InputStream input = null;
        try {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            input = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.decodeStream(input, null, bmOptions);
            return new Dimension(bmOptions.outWidth, bmOptions.outHeight);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void showCameraIntent(Activity activity, PhotoListener photoListener) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            try {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File photoFile = createImageFile(activity, true, "cam", timeStamp, "jpg");
                Uri photoUri = getUriForFile(activity, photoFile);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                photoListener.addPath(photoFile);
                activity.startActivityForResult(intent, MY_REQUEST_CAMERA);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File getImageDirectory(Context context, boolean toPublicDir) {
        File storageDir;
        if (toPublicDir) {
            if (AndroidHelper.isExternalStorageWritable()) {
                storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            } else {
                storageDir = getPrivatePicturesDirectory(context);
            }
        } else {
            storageDir = getPrivatePicturesDirectory(context);
        }
        return storageDir;
    }

    public static File createImageFile(Context context, boolean toPublicDir, String prefix, String name, String fileExt) throws IOException {
        String imageFileName = DataProvider.getAppName() + "_" + prefix + "_" + name + "." + fileExt;
        File file = new File(getImageDirectory(context, toPublicDir), imageFileName);
        return file;
    }

    public static File getPrivatePicturesDirectory(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    public static Uri getUriForFile(Context context, File file) {
        return FileProvider.getUriForFile(context, "codingdavinci.tourwizart", file);
    }
}
