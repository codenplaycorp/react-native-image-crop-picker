package com.reactnative.ivpusic.imagepicker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.util.Log;

class ImageUtil {

	public static void apply(File input, File output, int quality, Bitmap.CompressFormat format) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bmp = BitmapFactory.decodeFile(input.getPath(), options);

    	int actualHeight = options.outHeight;
		int actualWidth = options.outWidth;
		float maxHeight = 816.0f;
		float maxWidth = 612.0f;
		float imgRatio = actualWidth / actualHeight;
		float maxRatio = maxWidth / maxHeight;

		if (actualHeight > maxHeight || actualWidth > maxWidth) {
			if (imgRatio < maxRatio) {
				imgRatio = maxHeight / actualHeight;
				actualWidth = (int) (imgRatio * actualWidth);
				actualHeight = (int) maxHeight;
			} else if (imgRatio > maxRatio) {
				imgRatio = maxWidth / actualWidth;
				actualHeight = (int) (imgRatio * actualHeight);
				actualWidth = (int) maxWidth;
			} else {
				actualHeight = (int) maxHeight;
				actualWidth = (int) maxWidth;
			}
		}

		options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
		options.inJustDecodeBounds = false;
		options.inDither = false;
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inTempStorage = new byte[16*1024];

		try{
			bmp = BitmapFactory.decodeFile(input.getPath(), options);
		} catch (OutOfMemoryError e) {
			throw new RuntimeException(e);
		}

		Bitmap scaledBitmap = null;
		try{
			scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
		} catch (OutOfMemoryError e) {
			throw new RuntimeException(e);
		}

		float ratioX = actualWidth / (float) options.outWidth;
		float ratioY = actualHeight / (float)options.outHeight;
		float middleX = actualWidth / 2.0f;
		float middleY = actualHeight / 2.0f;

		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

		Canvas canvas = new Canvas(scaledBitmap);
		canvas.setMatrix(scaleMatrix);
		canvas.drawBitmap(bmp, middleX - bmp.getWidth()/2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

		ExifInterface exif;
		try {
			exif = new ExifInterface(input.getPath());
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);

			Matrix matrix = new Matrix();
			if (orientation == 6) {
				matrix.postRotate(90);
			} else if (orientation == 3) {
				matrix.postRotate(180);
			} else if (orientation == 8) {
				matrix.postRotate(270);
			}

			scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try {
			OutputStream os = new BufferedOutputStream(new FileOutputStream(output));
			scaledBitmap.compress(format, quality, os);
			os.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

 	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			inSampleSize *= 2;
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

}
