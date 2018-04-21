package daniel.com.redditscraper;

public class Image {
    public String url;
    public String title;
    public String author;
    public String score;
    public String commentsUrl;

    public Image(String url, String title, String author, String score, String commentsUrl) {
        this.url = url;
        this.title = title;
        this.author = author;
        this.score = score;
        this.commentsUrl = commentsUrl;
    }
}
