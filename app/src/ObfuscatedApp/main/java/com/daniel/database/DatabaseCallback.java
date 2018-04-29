package com.daniel.database;

public interface DatabaseCallback {
    void imageReturned(RedditImagePackage redditImagePackage);

    void error(String message);
}
