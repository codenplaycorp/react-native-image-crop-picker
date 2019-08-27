package com.reactnative.ivpusic.imagepicker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by ipusic on 12/27/16.
 */
class Compression {

    File resize(String originalImagePath, int maxWidth, int maxHeight, int quality) throws IOException {
        File imageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        if(!imageDirectory.exists()) {
            imageDirectory.mkdirs();
        }

        File resizeImageFile = new File(imageDirectory, UUID.randomUUID() + ".jpg");

        ImageUtil.apply(new File(originalImagePath), resizeImageFile, quality, Bitmap.CompressFormat.JPEG);

        return resizeImageFile;
    }

    int getRotationInDegreesForOrientationTag(int orientationTag) {
        switch(orientationTag){
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return -90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            default:
                return 0;
        }
    }

    File compressImage(final ReadableMap options, final String originalImagePath, final BitmapFactory.Options bitmapOptions) throws IOException {
        Double quality = options.hasKey("compressImageQuality") ? options.getDouble("compressImageQuality") : null;
        int targetQuality = quality != null ? (int) (quality * 100) : 80;
        return resize(originalImagePath, 0, 0, targetQuality);
    }

    synchronized void compressVideo(final Activity activity, final ReadableMap options, final String originalVideo, final String compressedVideo, final Promise promise) {
        promise.resolve(originalVideo);
    }
}
