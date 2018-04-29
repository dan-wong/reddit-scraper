package com.daniel.async.redditscraper;

import com.daniel.database.RedditImagePackage;

import java.util.List;

/**
 * Skeleton allowing class to obtain list of reddit post packages and show error message if applicable
 */
public interface RedditScraperCallback {
    void images(List<RedditImagePackage> redditImagePackages);

    void error(String message);
}
