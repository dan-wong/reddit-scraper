package com.daniel.async.redditscraper;

import com.daniel.database.RedditImagePackage;

import java.util.List;

public interface RedditScraperCallback {
    void images(List<RedditImagePackage> redditImagePackages);

    void error(String message);
}
