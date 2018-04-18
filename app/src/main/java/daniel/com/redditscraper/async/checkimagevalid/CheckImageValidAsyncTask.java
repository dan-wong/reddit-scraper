package daniel.com.redditscraper.async.checkimagevalid;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class CheckImageValidAsyncTask extends AsyncTask<String, Void, Boolean> {
    @Override
    protected Boolean doInBackground(String[] strings) {
        URLConnection connection;
        try {
            connection = new URL(strings[0]).openConnection();
            String contentType = connection.getHeaderField("Content-Type");
            return contentType.startsWith("image/");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
