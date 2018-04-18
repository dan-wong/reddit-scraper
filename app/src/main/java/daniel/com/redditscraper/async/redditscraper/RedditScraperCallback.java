package daniel.com.redditscraper.async.redditscraper;

import java.util.List;

public interface RedditScraperCallback {
    void imageUrls(List<String> imageUrls);

    void updateProgress(int progress);
}
