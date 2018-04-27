package com.daniel;

import android.Manifest;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daniel.async.commentsfromurl.Comment;
import com.daniel.async.commentsfromurl.CommentsFromUrlAsyncTask;
import com.daniel.async.commentsfromurl.CommentsFromUrlCallback;
import com.daniel.async.imagefromurl.ImageFromUrlAsyncTask;
import com.daniel.async.imagefromurl.ImageFromUrlCallback;
import com.daniel.database.Database;
import com.daniel.database.DatabaseCallback;
import com.daniel.filewriter.FileWriter;
import com.daniel.filewriter.FileWriterAsyncTask;
import com.daniel.filewriter.FileWriterCallback;

import java.io.File;
import java.util.List;

import daniel.com.redditscraper.R;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity
        implements DatabaseCallback, ImageFromUrlCallback, CommentsFromUrlCallback, FileWriterCallback {
    private ImageView imageView;
    private EditText subredditEditText;
    private ProgressBar progressBar;
    private TextView titleTextView;
    private TextView authorTextView;
    private TextView scoreTextView;
    private ListView commentsListView;
    private LinearLayout imageLayout;

    private Database database;
    private Image currentImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        subredditEditText = findViewById(R.id.subredditEditText);
        titleTextView = findViewById(R.id.titleTextView);
        authorTextView = findViewById(R.id.authorTextView);
        scoreTextView = findViewById(R.id.scoreTextView);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        imageLayout = findViewById(R.id.imageLayout);
        imageLayout.setVisibility(View.INVISIBLE);

        commentsListView = findViewById(R.id.commentsListView);
        setListViewHeightBasedOnChildren(commentsListView);

        database = Database.getInstance();
        database.addListener(this);

        findViewById(R.id.getPictureBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subreddit = subredditEditText.getText().toString().trim();
                if (subreddit.isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.empty_subreddit_error, Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    imageLayout.setVisibility(View.VISIBLE);
                    clearTitleTextViews();
                    database.getImage(subreddit);
                }
            }
        });

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MainActivityPermissionsDispatcher.saveImageToDeviceWithPermissionCheck(MainActivity.this);
                return true;
            }
        });
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void saveImageToDevice() {
        if (FileWriter.isExternalStorageWritable()) {
            final File directory = FileWriter.getPublicAlbumStorageDir(this);
            if (currentImage == null) {
                Toast.makeText(this, R.string.no_current_image_error, Toast.LENGTH_SHORT).show();
            } else { //Try writing to file
                new AlertDialog.Builder(this)
                        .setTitle(R.string.save_image_title)
                        .setMessage(R.string.save_image_prompt)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new FileWriterAsyncTask(directory, currentImage, MainActivity.this).execute();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        } else {
            Toast.makeText(this, R.string.external_storage_not_mounted, Toast.LENGTH_SHORT).show();
        }
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRationaleForExternalStoragePermission(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.permission_storage_rationale)
                .setPositiveButton(R.string.button_allow, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        request.proceed();
                    }
                })
                .setNegativeButton(R.string.button_deny, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        request.cancel();
                    }
                })
                .show();
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showDeniedExternalStorage() {
        Toast.makeText(this, R.string.permission_storage_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showNeverAskAgainExternalStorage() {
        Toast.makeText(this, R.string.permission_storage_neverask, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setImage(Bitmap image) {
        progressBar.setVisibility(View.INVISIBLE);
        imageLayout.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(image);
        this.currentImage.bitmap = image;
    }

    @Override
    public void setCommentsList(List<Comment> comments) {
        CommentAdapter arrayAdapter = new CommentAdapter(this, comments);
        commentsListView.setAdapter(arrayAdapter);
        setListViewHeightBasedOnChildren(commentsListView);
    }

    @Override
    public void imageReturned(Image image) {
        //Download the image and set it in the imageView
        new ImageFromUrlAsyncTask(this).execute(image.url);
        new CommentsFromUrlAsyncTask(this, image.commentsUrl).execute();

        //Set the relevant details in the layout
        titleTextView.setText(image.title);
        authorTextView.setText(image.author);
        scoreTextView.setText(image.score);

        this.currentImage = image;
    }

    @Override
    public void error(final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void result(Boolean result) {
        if (result) {
            Toast.makeText(this, R.string.write_device_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.write_device_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView
     **** https://stackoverflow.com/questions/18367522/android-list-view-inside-a-scroll-view ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
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
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    private void clearTitleTextViews() {
        titleTextView.setText("");
        authorTextView.setText("");
        scoreTextView.setText("");
    }
}
