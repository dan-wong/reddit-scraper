package com.daniel.async.filewriter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.daniel.database.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class FileWriterAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private File directory;
    private Image image;
    private FileWriterCallback callback;

    public FileWriterAsyncTask(File directory, Image image, FileWriterCallback callback) {
        this.directory = directory;
        this.image = image;
        this.callback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if (directory.isDirectory()) {
            FileOutputStream outputStream;
            try {
                Bitmap bitmap;
                try {
                    InputStream in = new URL(image.url).openStream();
                    bitmap = BitmapFactory.decodeStream(in);
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                outputStream = new FileOutputStream(new File(directory, image.title + ".png"));
                outputStream.write(byteArray);
                outputStream.close();

                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        callback.result(result);
    }
}
