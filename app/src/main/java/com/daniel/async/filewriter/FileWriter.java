package com.daniel.async.filewriter;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import daniel.com.redditscraper.R;

public class FileWriter {
    private static final String LOG_TAG = "FileWriter";

    //https://developer.android.com/training/data-storage/files#java
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    //https://developer.android.com/training/data-storage/files#java
    public static File getPublicAlbumStorageDir(Context context) {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name_folder));
        if (!file.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }
}
