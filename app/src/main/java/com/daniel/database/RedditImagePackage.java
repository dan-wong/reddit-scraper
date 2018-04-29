package com.daniel.database;

public class RedditImagePackage {
    public String id;
    public String url;
    public String title;
    public String author;
    public String score;
    public String commentsUrl;

    public RedditImagePackage(String id, String url, String title, String author, String score, String commentsUrl) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.author = author;
        this.score = score;
        this.commentsUrl = commentsUrl;
    }
}
