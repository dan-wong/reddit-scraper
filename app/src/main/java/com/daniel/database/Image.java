package com.daniel.database;

import android.graphics.Bitmap;

public class Image {
    public String id;
    public String url;
    public String title;
    public String author;
    public String score;
    public String commentsUrl;

    public Bitmap bitmap = null;

    public Image(String id, String url, String title, String author, String score, String commentsUrl) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.author = author;
        this.score = score;
        this.commentsUrl = commentsUrl;
    }
}
