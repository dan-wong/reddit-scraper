package daniel.com.redditscraper;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import daniel.com.redditscraper.imagefromurl.ImageFromUrlAsyncTask;
import daniel.com.redditscraper.imagefromurl.ImageFromUrlCallback;

public class MainActivity extends AppCompatActivity implements ImageFromUrlCallback {
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);

        new ImageFromUrlAsyncTask(this)
                .execute("https://i.imgur.com/OMd45uE.jpg");
    }

    @Override
    public void setImage(Bitmap image) {
        imageView.setImageBitmap(image);
    }
}
