package daniel.com.redditscraper.async.redditscraper;

import java.util.List;

import daniel.com.redditscraper.Image;

public interface RedditScraperCallback {
    void images(List<Image> images);

    void error(String message);
}
