package com.daniel.async.commentsfromurl;

import java.util.List;

public interface CommentsFromUrlCallback {
    void setCommentsList(List<Comment> comments);

    void error(String message);
}
