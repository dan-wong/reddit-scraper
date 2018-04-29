package com.daniel.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daniel.async.commentsfromurl.Comment;
import com.daniel.async.commentsfromurl.CommentAdapter;
import com.daniel.async.commentsfromurl.CommentsFromUrlAsyncTask;
import com.daniel.async.commentsfromurl.CommentsFromUrlCallback;
import com.daniel.async.imagefromurl.ImageFromUrlAsyncTask;
import com.daniel.async.imagefromurl.ImageFromUrlCallback;
import com.daniel.database.RedditImagePackage;

import java.util.List;

import daniel.com.redditscraper.R;

public class ImageFragment extends Fragment
        implements ImageFragmentInterface, CommentsFromUrlCallback, ImageFromUrlCallback {
    private OnFragmentInteractionListener mListener;

    private TextView titleTextView, authorTextView, scoreTextView;
    private ListView commentsListView;
    private ImageView imageView;
    private LinearLayout imageLayout;
    private ProgressBar progressBar;

    public ImageFragment() {
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView
     **** https://stackoverflow.com/questions/18367522/android-list-view-inside-a-scroll-view ****/
    private static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * listAdapter.getCount()) - 150;
        listView.setLayoutParams(params);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        titleTextView = view.findViewById(R.id.titleTextView);
        authorTextView = view.findViewById(R.id.authorTextView);
        scoreTextView = view.findViewById(R.id.scoreTextView);
        commentsListView = view.findViewById(R.id.commentsListView);
        imageView = view.findViewById(R.id.imageView);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        imageLayout = view.findViewById(R.id.imageLayout);
        imageLayout.setVisibility(View.INVISIBLE);

        setListViewHeightBasedOnChildren(commentsListView);

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.saveImage();
                return true;
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void newImage(RedditImagePackage redditImagePackage) {
        if (imageLayout.getVisibility() == View.INVISIBLE) {
            imageLayout.setVisibility(View.VISIBLE);
        }

        titleTextView.setText(redditImagePackage.title);
        authorTextView.setText(redditImagePackage.author);
        scoreTextView.setText(redditImagePackage.score);

        new CommentsFromUrlAsyncTask(ImageFragment.this, redditImagePackage.commentsUrl).execute();
        new ImageFromUrlAsyncTask(ImageFragment.this).execute(redditImagePackage.url);
    }

    @Override
    public void setCommentsList(List<Comment> comments) {
        CommentAdapter arrayAdapter = new CommentAdapter(getContext(), comments);
        commentsListView.setAdapter(arrayAdapter);
        setListViewHeightBasedOnChildren(commentsListView);
    }

    @Override
    public void error(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setImage(Bitmap image) {
        progressBar.setVisibility(View.INVISIBLE);
        mListener.imageLoaded();
        imageView.setImageBitmap(image);
    }

    public interface OnFragmentInteractionListener {
        void imageLoaded();

        void saveImage();
    }
}
