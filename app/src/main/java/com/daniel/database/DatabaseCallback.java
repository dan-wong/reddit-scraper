package com.daniel.database;

import com.daniel.Image;

public interface DatabaseCallback {
    void imageReturned(Image image);

    void error(String message);
}
