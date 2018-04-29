package com.daniel.async.redditscraper;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.daniel.database.RedditImagePackage;

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

public class RedditScraperAsyncTask extends AsyncTask<Void, Void, List<RedditImagePackage>> {
    private static final String ENDPOINT_URL_FORMAT = "https://www.reddit.com/r/%s/.json?limit=%d&after=%s";
    private static final String ENDPOINT_COMMENT_URL_FORMAT = "https://www.reddit.com%s.json";
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
    protected List<RedditImagePackage> doInBackground(Void... voids) {
        List<RedditImagePackage> redditImagePackages = new ArrayList<>();

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
                JSONObject currentJSONObject = responseArray.getJSONObject(i);

                String id = getId(currentJSONObject);
                String imageUrl = getImageUrl(currentJSONObject);
                String title = getTitle(currentJSONObject);
                String author = getAuthor(currentJSONObject);
                String score = getScore(currentJSONObject);

                String commentUrl;
                commentUrl = String.format(ENDPOINT_COMMENT_URL_FORMAT, responseArray.getJSONObject(i).getJSONObject("data").getString("permalink"));

                redditImagePackages.add(new RedditImagePackage(id, imageUrl, title, author, score, commentUrl));
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return redditImagePackages;
    }

    @Override
    protected void onPostExecute(List<RedditImagePackage> redditImagePackages) {
        callback.images(redditImagePackages);
    }

    @SuppressLint("DefaultLocale")
    private URL formatRequestUrl(String subreddit, String lastImageId) throws MalformedURLException {
        return new URL(String.format(ENDPOINT_URL_FORMAT, subreddit, LIMIT, lastImageId));
    }

    private String getId(JSONObject jsonObject) throws JSONException {
        return jsonObject.getJSONObject("data").getString("name");
    }

    private String getImageUrl(JSONObject jsonObject) throws JSONException {
        return jsonObject.getJSONObject("data").getString("url");
    }

    private String getTitle(JSONObject jsonObject) throws JSONException {
        return jsonObject.getJSONObject("data").getString("title");
    }

    private String getAuthor(JSONObject jsonObject) throws JSONException {
        return jsonObject.getJSONObject("data").getString("author");
    }

    private String getScore(JSONObject jsonObject) throws JSONException {
        return jsonObject.getJSONObject("data").getString("score");
    }
}
