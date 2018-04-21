package daniel.com.redditscraper.database;

import daniel.com.redditscraper.Image;

public interface DatabaseCallback {
    void imageReturned(Image image);

    void error(String message);
}
