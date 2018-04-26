package com.daniel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import com.daniel.async.commentsfromurl.Comment;

import daniel.com.redditscraper.R;

/**
 * Created by theooswanditosw164 on 21/04/18.
 */

public class CommentAdapter extends ArrayAdapter<Comment> {

    private List<Comment> comments;
    Context context;

    public CommentAdapter(Context context, List<Comment> data) {
        super(context, R.layout.comment_row, data);
        this.comments = data;
        this.context=context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItem = convertView;

        if (listItem == null){
            listItem = LayoutInflater.from(this.context).inflate(R.layout.comment_row, parent, false);
        }
        Comment comment = comments.get(position);

        TextView commentAuthor = (TextView) listItem.findViewById(R.id.commentAuthor);
        commentAuthor.setText(comment.author);

        TextView commentScore = (TextView) listItem.findViewById(R.id.commentScore);
        commentScore.setText("Score: " + comment.score);

        TextView commentBody = (TextView) listItem.findViewById(R.id.commentBody);
        commentBody.setText(comment.body);

        return listItem;
    }
}
