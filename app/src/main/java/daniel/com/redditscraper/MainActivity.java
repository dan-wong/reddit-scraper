package daniel.com.redditscraper;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    private Button getPictureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        subredditEditText = findViewById(R.id.subredditEditText);
        getPictureButton = findViewById(R.id.getPictureBtn);

        //Initially hide imageView component
        imageView.setVisibility(View.INVISIBLE);

        getPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subreddit = subredditEditText.getText().toString();
                if (subreddit.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Subreddit cannot be empty!", Toast.LENGTH_SHORT).show();
                } else {
                    new RedditScraperAsyncTask(MainActivity.this).execute(subreddit);
                }
            }
        });
    }

    @Override
    public void setImage(Bitmap image) {
        imageView.setImageBitmap(image);
    }

    @Override
    public void imageUrls(List<String> imageUrls) {
        if (!(imageUrls == null || imageUrls.isEmpty())) { //Not (null or empty)
            for (String url : imageUrls) {
                try {
                    if (new CheckImageValidAsyncTask().execute(url).get()) {
                        imageView.setVisibility(View.VISIBLE);
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
}
