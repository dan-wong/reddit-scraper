package daniel.com.redditscraper.imagefromurl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.net.URL;

public class ImageFromUrlAsyncTask extends AsyncTask<String, Void, Bitmap> {
    private ImageFromUrlCallback callback;

    public ImageFromUrlAsyncTask(ImageFromUrlCallback callback) {
        this.callback = callback;
    }

    @Override
    protected Bitmap doInBackground(String[] urls) {
        String imageUrl = urls[0];
        Bitmap image = null;
        try {
            InputStream in = new URL(imageUrl).openStream();
            image = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return image;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        callback.setImage(result);
    }
}
