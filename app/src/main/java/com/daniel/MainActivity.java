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

import com.daniel.database.Database;
import com.daniel.database.DatabaseCallback;
import com.daniel.database.Image;
import com.daniel.filewriter.FileWriter;
import com.daniel.filewriter.FileWriterAsyncTask;
import com.daniel.filewriter.FileWriterCallback;
import com.daniel.fragments.ImageFragment;
import com.daniel.fragments.ImageFragmentInterface;

import java.io.File;

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
    private EditText subredditEditText;
    private Button getPictureButton;

    private Database database;
    private Image currentImage = null;

    private ImageFragmentInterface imageFragment;

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
                }
            }
        });

        imageFragment = (ImageFragment) getSupportFragmentManager().findFragmentById(R.id.image_fragment);

        Toast.makeText(this, Crypto.generateKey(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void imageReturned(Image image) {
        imageFragment.newImage(image);
        currentImage = image;
    }

    @Override
    public void error(final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
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
