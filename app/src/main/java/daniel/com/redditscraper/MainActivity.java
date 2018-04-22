package daniel.com.redditscraper;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import daniel.com.redditscraper.async.commentsfromurl.Comment;
import daniel.com.redditscraper.async.commentsfromurl.CommentsFromUrlAsyncTask;
import daniel.com.redditscraper.async.commentsfromurl.CommentsFromUrlCallback;
import daniel.com.redditscraper.async.imagefromurl.ImageFromUrlAsyncTask;
import daniel.com.redditscraper.async.imagefromurl.ImageFromUrlCallback;
import daniel.com.redditscraper.database.Database;
import daniel.com.redditscraper.database.DatabaseCallback;

public class MainActivity extends AppCompatActivity
        implements DatabaseCallback, ImageFromUrlCallback, CommentsFromUrlCallback {
    private ImageView imageView;
    private EditText subredditEditText;
    private ProgressBar progressBar;
    private TextView titleTextView;
    private TextView authorTextView;
    private TextView scoreTextView;
    private ListView commentsListView;

    private LinearLayout imageLayout;

    private Database database;

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
                String subreddit = subredditEditText.getText().toString();
                if (subreddit.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Subreddit cannot be empty!", Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    imageLayout.setVisibility(View.VISIBLE);
                    clearTitleTextViews();
                    database.getImage(subreddit);
                }
            }
        });
    }

    private void clearTitleTextViews() {
        titleTextView.setText("");
        authorTextView.setText("");
        scoreTextView.setText("");
    }

    @Override
    public void setImage(Bitmap image) {
        progressBar.setVisibility(View.INVISIBLE);
        imageLayout.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(image);
    }

    @Override
    public void setCommentsList(List<Comment> comments) {
        List<String> stringList = new ArrayList<>();
        for (Comment c: comments){
            stringList.add(c.toString());
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, stringList);
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
}
