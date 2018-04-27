package com.daniel.commentsfromurl;

import java.util.List;

public interface CommentsFromUrlCallback {
    void setCommentsList(List<Comment> comments);

    void error(String message);
}
