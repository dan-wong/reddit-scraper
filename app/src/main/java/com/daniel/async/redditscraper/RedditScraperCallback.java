package com.daniel.async.redditscraper;

import com.daniel.database.Image;

import java.util.List;

public interface RedditScraperCallback {
    void images(List<Image> images);

    void error(String message);
}
