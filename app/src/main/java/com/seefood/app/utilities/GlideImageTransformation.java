package com.seefood.app.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ExifInterface;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;

import java.security.MessageDigest;

public class GlideImageTransformation extends BitmapTransformation {

    private int mOrientation;

    public GlideImageTransformation(Context context, int orientation) {
        super();
        mOrientation = orientation;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        int exifOrientationDegrees = getExifOrientationDegrees(mOrientation);
        return TransformationUtils.rotateImageExif(pool, toTransform, exifOrientationDegrees);
    }

    private int getExifOrientationDegrees(int orientation) {
        int exifInt;
        switch (orientation) {
            case 90:
                exifInt = ExifInterface.ORIENTATION_ROTATE_90;
                break;
            // other cases
            default:
                exifInt = ExifInterface.ORIENTATION_NORMAL;
                break;
        }
        return exifInt;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
    }
}
