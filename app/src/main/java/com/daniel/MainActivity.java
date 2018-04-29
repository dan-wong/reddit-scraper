package com.daniel;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsEvent;
import com.daniel.async.filewriter.FileWriter;
import com.daniel.async.filewriter.FileWriterAsyncTask;
import com.daniel.async.filewriter.FileWriterCallback;
import com.daniel.database.Database;
import com.daniel.database.DatabaseCallback;
import com.daniel.database.RedditImagePackage;
import com.daniel.fragments.ImageFragment;
import com.daniel.fragments.ImageFragmentInterface;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import daniel.com.redditscraper.R;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity
        implements DatabaseCallback, FileWriterCallback, ImageFragment.OnFragmentInteractionListener {
    private static SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
    private static PinpointManager pinpointManager;

    private EditText subredditEditText;
    private Button getPictureButton;

    private Database database;
    private RedditImagePackage currentRedditImagePackage = null;

    private ImageFragmentInterface imageFragment;
    private String currentSubreddit = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        subredditEditText = findViewById(R.id.subredditEditText);

        database = Database.getInstance();
        database.addListener(this);

        getPictureButton = findViewById(R.id.getPictureBtn);

        getPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subreddit = subredditEditText.getText().toString().trim();
                if (subreddit.isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.empty_subreddit_error, Toast.LENGTH_SHORT).show();
                } else {
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    getPictureButton.setEnabled(false);
                    database.getImage(subreddit);

                    logEvent(subreddit);
                }
            }
        });

        imageFragment = (ImageFragment) getSupportFragmentManager().findFragmentById(R.id.image_fragment);

        AWSMobileClient.getInstance().initialize(this).execute();
        PinpointConfiguration config = new PinpointConfiguration(
                MainActivity.this,
                AWSMobileClient.getInstance().getCredentialsProvider(),
                AWSMobileClient.getInstance().getConfiguration()
        );
        pinpointManager = new PinpointManager(config);
    }

    public void logEvent(String subreddit) {
        if (!currentSubreddit.equals(subreddit)) {
            currentSubreddit = subreddit;
        } else {
            return;
        }

        pinpointManager.getSessionClient().startSession();
        final AnalyticsEvent event =
                pinpointManager.getAnalyticsClient().createEvent("Subreddit")
                        .withAttribute("Subreddit", subreddit)
                        .withAttribute("Time", df.format(Calendar.getInstance().getTime()));
        pinpointManager.getAnalyticsClient().recordEvent(event);
        pinpointManager.getSessionClient().stopSession();
        pinpointManager.getAnalyticsClient().submitEvents();
    }

    @Override
    public void imageReturned(RedditImagePackage redditImagePackage) {
        imageFragment.newImage(redditImagePackage);
        currentRedditImagePackage = redditImagePackage;
    }

    @Override
    public void error(final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                getPictureButton.setEnabled(true);
            }
        });
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void saveImageToDevice() {
        if (FileWriter.isExternalStorageWritable()) {
            final File directory = FileWriter.getPublicAlbumStorageDir(this);
            if (currentRedditImagePackage == null) {
                Toast.makeText(this, R.string.no_current_image_error, Toast.LENGTH_SHORT).show();
            } else { //Try writing to file
                new AlertDialog.Builder(this)
                        .setTitle(R.string.save_image_title)
                        .setMessage(R.string.save_image_prompt)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new FileWriterAsyncTask(directory, currentRedditImagePackage, MainActivity.this).execute();
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

    @Override
    public void imageLoaded() {
        getPictureButton.setEnabled(true);
    }

    @Override
    public void saveImage() {
        MainActivityPermissionsDispatcher.saveImageToDeviceWithPermissionCheck(this);
    }
}
