package daniel.com.redditscraper;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.ExecutionException;

import daniel.com.redditscraper.async.checkimagevalid.CheckImageValidAsyncTask;
import daniel.com.redditscraper.async.imagefromurl.ImageFromUrlAsyncTask;
import daniel.com.redditscraper.async.imagefromurl.ImageFromUrlCallback;
import daniel.com.redditscraper.async.redditscraper.RedditScraperAsyncTask;
import daniel.com.redditscraper.async.redditscraper.RedditScraperCallback;

public class MainActivity extends AppCompatActivity implements ImageFromUrlCallback, RedditScraperCallback {
    private ImageView imageView;
    private EditText subredditEditText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        subredditEditText = findViewById(R.id.subredditEditText);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setScaleY(3f);

        //Initially hide imageView component
        imageView.setVisibility(View.INVISIBLE);

        findViewById(R.id.getPictureBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subreddit = subredditEditText.getText().toString();
                if (subreddit.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Subreddit cannot be empty!", Toast.LENGTH_SHORT).show();
                } else {
                    new RedditScraperAsyncTask(MainActivity.this).execute(subreddit);
                    progressBar.setProgress(0);
                }
            }
        });
    }

    @Override
    public void setImage(Bitmap image) {
        if (imageView.getVisibility() == View.INVISIBLE) imageView.setVisibility(View.VISIBLE);
        progressBar.setProgress(100);
        imageView.setImageBitmap(image);
    }

    @Override
    public void imageUrls(List<String> imageUrls) {
        if (!(imageUrls == null || imageUrls.isEmpty())) { //Not (null or empty)
            for (String url : imageUrls) {
                try {
                    if (new CheckImageValidAsyncTask().execute(url).get()) {
                        new ImageFromUrlAsyncTask(this)
                                .execute(url);
                        return;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }

        Toast.makeText(getApplicationContext(), "Subreddit has no images :(", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateProgress(int progress) {
        progressBar.setProgress(progress * 9);
    }
}
