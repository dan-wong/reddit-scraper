package com.daniel.async.commentsfromurl;

import java.util.List;

/**
 * Interface which creates structure that allows comment to be requested asynchronously.
 */
public interface CommentsFromUrlCallback {
    void setCommentsList(List<Comment> comments);

    void error(String message);
}
