package com.daniel.fragments;

import android.content.Context;
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

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.daniel.GlideApp;
import com.daniel.commentsfromurl.Comment;
import com.daniel.commentsfromurl.CommentAdapter;
import com.daniel.commentsfromurl.CommentsFromUrlAsyncTask;
import com.daniel.commentsfromurl.CommentsFromUrlCallback;
import com.daniel.database.Image;

import java.util.List;

import daniel.com.redditscraper.R;

public class ImageFragment extends Fragment
        implements ImageFragmentInterface, CommentsFromUrlCallback {
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
        params.height = totalHeight + (listView.getDividerHeight() * listAdapter.getCount()) + 150;
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
    public void newImage(Image image) {
        if (imageLayout.getVisibility() == View.INVISIBLE) {
            imageLayout.setVisibility(View.VISIBLE);
        }

        titleTextView.setText(image.title);
        authorTextView.setText(image.author);
        scoreTextView.setText(image.score);

        new CommentsFromUrlAsyncTask(ImageFragment.this, image.commentsUrl).execute();

        GlideApp.with(this)
                .load(image.url)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getContext(), R.string.failed_load_image, Toast.LENGTH_SHORT).show();
                        mListener.imageLoaded();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.INVISIBLE);
                        mListener.imageLoaded();
                        return false;
                    }
                })
                .into(imageView);
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

    public interface OnFragmentInteractionListener {
        void imageLoaded();

        void saveImage();
    }
}
