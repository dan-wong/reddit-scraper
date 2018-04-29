package com.daniel.async.commentsfromurl;

/**
 * Data class containing data for reddit comment
 */
public class Comment {
    public String author;
    public String score;
    public String body;

    public Comment(String author, String score, String body){
        this.author = author;
        this.score = score;
        this.body = body;
    }
}
