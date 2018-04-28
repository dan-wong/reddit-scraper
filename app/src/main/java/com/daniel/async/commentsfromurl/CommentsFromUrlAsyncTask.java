package com.daniel.async.commentsfromurl;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CommentsFromUrlAsyncTask extends AsyncTask<Void, Void, List<Comment>> {
    private CommentsFromUrlCallback callback;
    private String url;

    public CommentsFromUrlAsyncTask(CommentsFromUrlCallback callback, String url) {
        this.callback = callback;
        this.url = url;
    }

    @Override
    protected List<Comment> doInBackground(Void... voids) {
        List<Comment> comments = new ArrayList<>();

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(this.url);

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

            JSONArray jsonArray = new JSONArray(jsonString.toString());
            for(int arrayIndex = 1; arrayIndex < jsonArray.length(); arrayIndex++) {
                JSONObject jsonObject = jsonArray.getJSONObject(arrayIndex);
                JSONArray responseArray = jsonObject.getJSONObject("data").getJSONArray("children");
                for (int i = 0; i < responseArray.length(); i++) {
                    String author = responseArray.getJSONObject(i)
                            .getJSONObject("data")
                            .getString("author");

                    String score = responseArray.getJSONObject(i)
                            .getJSONObject("data")
                            .getString("score");

                    String body = responseArray.getJSONObject(i)
                                    .getJSONObject("data")
                                    .getString("body");

                    Comment commentToAdd = new Comment(author, score, body);
                    comments.add(commentToAdd);
                }
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return comments;
    }

    @Override
    protected void onPostExecute(List<Comment> comments) {
        callback.setCommentsList(comments);
    }
}
