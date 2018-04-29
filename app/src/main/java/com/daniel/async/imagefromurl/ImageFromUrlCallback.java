package com.daniel.async.imagefromurl;

/**
 * Skeleton that allows classes to show image asynchronously downloaded
 */
import android.graphics.Bitmap;

public interface ImageFromUrlCallback {
    void setImage(Bitmap image);
}
