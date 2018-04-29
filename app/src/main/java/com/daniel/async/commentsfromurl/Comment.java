package com.daniel.async.commentsfromurl;

public class Comment {
    public String author;
    public String score;
    public String body;

    public Comment(String author, String score, String body){
        this.author = author;
        this.score = score;
        this.body = body;
    }

    @Override
    public String toString() {
        return this.author + " [" + this.score + "] " + this.body;
    }
}
