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

import daniel.com.redditscraper.Image;

public class RedditScraperAsyncTask extends AsyncTask<Void, Void, List<Image>> {
    private static final String URL_FORMAT = "https://www.reddit.com/r/%s/.json?limit=%d&after=%s";
    private static final int LIMIT = 50;

    private RedditScraperCallback callback;
    private String subreddit;
    private String lastImageId;

    public RedditScraperAsyncTask(RedditScraperCallback callback, String subreddit, String lastImageId) {
        this.callback = callback;
        this.subreddit = subreddit;
        this.lastImageId = lastImageId;
    }

    @Override
    protected List<Image> doInBackground(Void... voids) {
        List<Image> images = new ArrayList<>();

        HttpURLConnection urlConnection = null;
        try {
            URL url = formatRequestUrl(subreddit, lastImageId);

            urlConnection = (HttpURLConnection) url.openConnection();

            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                callback.error("Request returned " + urlConnection.getResponseCode());
                return null;
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
                String id = responseArray.getJSONObject(i)
                        .getJSONObject("data")
                        .getString("name");

                String imageUrl = responseArray.getJSONObject(i)
                        .getJSONObject("data")
                        .getString("url");

                String title = responseArray.getJSONObject(i)
                        .getJSONObject("data")
                        .getString("title");

                String author = responseArray.getJSONObject(i)
                        .getJSONObject("data")
                        .getString("author");

                String score = responseArray.getJSONObject(i)
                        .getJSONObject("data")
                        .getString("score");

                String commentUrl = "https://www.reddit.com" +
                        responseArray.getJSONObject(i)
                        .getJSONObject("data")
                        .getString("permalink") +
                        ".json";

                images.add(new Image(id, imageUrl, title, author, score, commentUrl));
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return images;
    }

    @Override
    protected void onPostExecute(List<Image> images) {
        callback.images(images);
    }

    @SuppressLint("DefaultLocale")
    private URL formatRequestUrl(String subreddit, String lastImageId) throws MalformedURLException {
        return new URL(String.format(URL_FORMAT, subreddit, LIMIT, lastImageId));
    }
}
