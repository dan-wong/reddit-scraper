package com.daniel.commentsfromurl;

/**
 * Created by theooswanditosw164 on 21/04/18.
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

    @Override
    public String toString() {
        return this.author + " [" + this.score + "] " + this.body;
    }
}
