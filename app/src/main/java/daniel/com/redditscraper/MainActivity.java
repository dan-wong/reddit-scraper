package daniel.com.redditscraper;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import daniel.com.redditscraper.async.commentsfromurl.Comment;
import daniel.com.redditscraper.async.commentsfromurl.CommentsFromUrlAsyncTask;
import daniel.com.redditscraper.async.commentsfromurl.CommentsFromUrlCallback;
import daniel.com.redditscraper.async.imagefromurl.ImageFromUrlAsyncTask;
import daniel.com.redditscraper.async.imagefromurl.ImageFromUrlCallback;
import daniel.com.redditscraper.async.redditscraper.RedditScraperAsyncTask;
import daniel.com.redditscraper.async.redditscraper.RedditScraperCallback;

public class MainActivity extends AppCompatActivity
        implements ImageFromUrlCallback, RedditScraperCallback, CommentsFromUrlCallback {
    private ImageView imageView;
    private EditText subredditEditText;
    private ProgressBar progressBar;

    private TextView titleTextView;
    private TextView authorTextView;
    private TextView scoreTextView;

    private ListView commentsListView;

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

        commentsListView = findViewById(R.id.commentsListView);
        setListViewHeightBasedOnChildren(commentsListView);

        //Initially hide imageView component
        imageView.setVisibility(View.INVISIBLE);

        findViewById(R.id.getPictureBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subreddit = subredditEditText.getText().toString();
                if (subreddit.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Subreddit cannot be empty!", Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.INVISIBLE);
                    new RedditScraperAsyncTask(MainActivity.this).execute(subreddit);
                }
            }
        });
    }

    @Override
    public void setImage(Bitmap image) {
        progressBar.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(image);
    }

    @Override
    public void images(List<Image> images) {
        List<Image> validImages = new ArrayList<>();

        //Only ever null when url response is not OK which is handled by MainActivity#error
        if (images == null) {
            return;
        } else if (!images.isEmpty()) { //Not (null or empty)
            for (int i = 0; i < images.size(); i++) {
                Image currentImage = images.get(i);

                String url = currentImage.url;
                if (checkIfImage(url)) {
                    validImages.add(currentImage);
                }
            }
        }

        if (!validImages.isEmpty()) {
            int random = new Random().nextInt(validImages.size());

            Image selectedImage = validImages.get(random);
            //Download the image and set it in the imageView
            new ImageFromUrlAsyncTask(this).execute(selectedImage.url);
            new CommentsFromUrlAsyncTask(this, selectedImage.commentsUrl).execute();

            //Set the relevant details in the layout
            titleTextView.setText(selectedImage.title);
            authorTextView.setText(selectedImage.author);
            scoreTextView.setText(selectedImage.score);
            return;
        }

        Toast.makeText(getApplicationContext(), "Subreddit has no images :(", Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.INVISIBLE);

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
    public void error(final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private boolean checkIfImage(String url) {
        return url.contains("jpeg") || url.contains("jpg") || url.contains("png");
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
