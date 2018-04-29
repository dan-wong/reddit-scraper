package com.daniel.database;

import java.util.List;

public class CachedImages {
    public List<RedditImagePackage> redditImagePackages;
    private String lastImageId;

    public CachedImages(List<RedditImagePackage> redditImagePackages) {
        this.redditImagePackages = redditImagePackages;
        this.lastImageId = redditImagePackages.get(redditImagePackages.size() - 1).id;
    }

    public String getLastImageId() {
        return lastImageId;
    }
}
