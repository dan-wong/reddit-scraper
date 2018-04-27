package com.daniel.database;

public interface DatabaseCallback {
    void imageReturned(Image image);

    void error(String message);
}
