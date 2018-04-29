package com.daniel.database;

import com.daniel.async.redditscraper.RedditScraperAsyncTask;
import com.daniel.async.redditscraper.RedditScraperCallback;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Singleton
public class Database implements RedditScraperCallback {
    private static Database instance;

    private DatabaseCallback listener;

    private Map<String, CachedImages> cache;
    private String currentSubredditSearch;

    private Database() {
        cache = new HashMap<>();
    }

    public static Database getInstance() {
        if (instance == null) {
            return new Database();
        } else {
            return instance;
        }
    }

    public void addListener(DatabaseCallback listener) {
        this.listener = listener;
    }

    public void getImage(String subreddit) {
        CachedImages cachedImages = cache.get(subreddit);

        //Either first time we poll for this subreddit, or no redditImagePackages left
        if (cachedImages == null || cachedImages.redditImagePackages.isEmpty()) {
            //Get new redditImagePackages
            currentSubredditSearch = subreddit;

            String lastImageId = "";
            if (cachedImages != null) {
                lastImageId = cachedImages.getLastImageId();
            }

            new RedditScraperAsyncTask(this, subreddit, lastImageId).execute();
        } else {
            listener.imageReturned(cachedImages.redditImagePackages.remove(0));
        }
    }

    private boolean checkIfImage(String url) {
        return url.contains("jpeg") || url.contains("jpg") || url.contains("png");
    }

    @Override
    public void images(List<RedditImagePackage> redditImagePackages) {
        if (redditImagePackages == null) {
            listener.error("Subreddit has no redditImagePackages :(");
            return;
        }

        Iterator<RedditImagePackage> imageIterator = redditImagePackages.iterator();
        while (imageIterator.hasNext()) {
            if (!checkIfImage(imageIterator.next().url)) {
                imageIterator.remove();
            }
        }

        if (redditImagePackages.size() == 0) {
            listener.error("Subreddit has no redditImagePackages :(");
            return;
        }

        cache.put(currentSubredditSearch, new CachedImages(redditImagePackages));
        getImage(currentSubredditSearch);
    }

    @Override
    public void error(String message) {
        listener.error(message);
    }
}
