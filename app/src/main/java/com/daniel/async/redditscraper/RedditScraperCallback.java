package com.daniel.async.redditscraper;

import java.util.List;

import com.daniel.Image;

public interface RedditScraperCallback {
    void images(List<Image> images);

    void error(String message);
}
