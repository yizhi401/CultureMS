package com.gov.culturems.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import com.gov.culturems.MyApplication;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by peter on 2015/3/30.
 */
public class ImageUtil {

    private static final int maxHeight = 300;
    private static final int maxWidth = 300;

    public static Bitmap decodeStream(Activity activity, Uri uri) throws FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(uri), null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, maxHeight, maxWidth);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(uri), null, options);
    }

    public static Bitmap decodeStream(File file) throws FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(new FileInputStream(file), null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, maxHeight, maxWidth);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeStream(new FileInputStream(file), null, options);
    }

    public static void loadImagePicasso(ImageView imageView, String remoteUrl, int defaultRes) {
        if (TextUtils.isEmpty(remoteUrl)) {
            if (defaultRes == 0) {
                throw new IllegalArgumentException("both remoteUrl and defaultRes are null");
            } else {
                Picasso.with(MyApplication.getInstance().getApplicationContext()).load(defaultRes).into(imageView);
            }
        } else {
            Picasso.with(MyApplication.getInstance().getApplicationContext()).load(remoteUrl).placeholder(defaultRes).into(imageView);
        }

    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
