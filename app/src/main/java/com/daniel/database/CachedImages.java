package com.daniel.database;

import java.util.List;

public class CachedImages {
    public List<Image> images;
    private String lastImageId;

    public CachedImages(List<Image> images) {
        this.images = images;
        this.lastImageId = images.get(images.size() - 1).id;
    }

    public String getLastImageId() {
        return lastImageId;
    }
}
