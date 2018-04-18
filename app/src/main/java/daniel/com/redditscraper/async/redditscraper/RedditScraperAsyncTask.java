package daniel.com.redditscraper.async.redditscraper;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RedditScraperAsyncTask extends AsyncTask<String, Void, List<String>> {
    private static final String TAG = "RedditScraperAsyncTask";
    private static final String URL_FORMAT = "https://www.reddit.com/r/%s/.json?limit=%d";
    private static final int LIMIT = 10;

    private RedditScraperCallback callback;

    public RedditScraperAsyncTask(RedditScraperCallback callback) {
        this.callback = callback;
    }

    @Override
    protected List<String> doInBackground(String[] strings) {
        List<String> imageUrls = new ArrayList<>();

        HttpURLConnection urlConnection = null;
        try {
            URL url = formatRequestUrl(strings[0]);

            urlConnection = (HttpURLConnection) url.openConnection();

            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Request Failed. HTTP Error Code: " + urlConnection.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder jsonString = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
            br.close();

            JSONObject jsonObject = new JSONObject(jsonString.toString());
            JSONArray responseArray = jsonObject.getJSONObject("data").getJSONArray("children");
            for (int i = 0; i < responseArray.length(); i++) {
                String imageUrl = responseArray.getJSONObject(i)
                        .getJSONObject("data")
                        .getString("url");
                imageUrls.add(imageUrl);
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return imageUrls;
    }

    @Override
    protected void onPostExecute(List<String> imageUrls) {
        callback.imageUrls(imageUrls);
    }

    @SuppressLint("DefaultLocale")
    private URL formatRequestUrl(String subreddit) throws MalformedURLException {
        return new URL(String.format(URL_FORMAT, subreddit, LIMIT));
    }
}
