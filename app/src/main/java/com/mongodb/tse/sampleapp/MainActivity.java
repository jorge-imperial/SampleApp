package com.mongodb.tse.sampleapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.bson.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import java.io.File;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_IMAGE_RESULT = 101;
    private String mCurrentPhotoPath;
    private File photoFile;
    private static final String TAG = "MainActivity" ;

    String [] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    static final int MY_PERMISSIONS_REQUEST = 100;
    private Bitmap myBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Taking picture", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Crea actividad
                StartIntentForImage(REQUEST_CODE_IMAGE_RESULT, "img_");
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


         checkPermissions();
    }


    private void StartIntentForImage(int requestCodeBack, String imagePrefix) {
        /*
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
        galleryIntent.setType("image/*");
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        */


        try {
            photoFile = createImageFile(imagePrefix);
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.mongodb.tse.sampleapp.fileprovider",
                        photoFile);

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, requestCodeBack);
                }

                startActivityForResult(takePictureIntent, requestCodeBack);
            }
        }
        catch(IOException ex){
            Log.e( TAG, ex.getMessage());
            return;
        }
    }

    private File createImageFile(String prefix) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = prefix + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",  // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();

        Log.d("PHOTO", "Path " + mCurrentPhotoPath);
        return image;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Activity returned.
        if (requestCode == this.REQUEST_CODE_IMAGE_RESULT ) {
            if (data.getData()!=null) {

                try {
                    InputStream stream = getContentResolver().openInputStream(data.getData());
                    myBitmap = BitmapFactory.decodeStream(stream);
                    stream.close();
                    //imageView.setImageBitmap(myBitmap);
                    Log.d("BITMAP", myBitmap.toString());
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
            else {

                // For full images
                // Get the dimensions of the View
                //int targetW = imageView.getWidth();
                //int targetH = imageView.getHeight();

                // Get the dimensions of the bitmap
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                // Determine how much to scale down the image
                //int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false;
                //bmOptions.inSampleSize = scaleFactor;
                bmOptions.inPurgeable = true;

                myBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
                //imageView.setImageBitmap(myBitmap);
            }
            super.onActivityResult(requestCode, resultCode, data);
        }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private int UploadS3() {

        FileInputStream fis;
        try {
            final int size = 16*1024*1024;
            String fname = photoFile.getName();
            fis = openFileInput(fname);

            byte []r = new byte[ size ];

            fis.read(r,0,size);

            fis.close();

            Document doc = new Document();
            doc.append("bucket", AppConfig.getS3BucketName());
            doc.append("key", fname);
            doc.append("contentType", "text/plain");
            doc.append("acl", "public-read");
            doc.append("body", r);

            StitchClientManager.getStitchClient().executeServiceFunction(
                    "put",
                    AppConfig.getS3ServiceName(),
                    doc)
                    .addOnCompleteListener(new OnCompleteListener<Object>() {
                        @Override
                        public void onComplete(@NonNull final Task<Object> task) {
                            if (task.isSuccessful()) {
                                Document d  = (Document) task.getResult();
                                Log.d("STITCH", "Successfully sent:" + d.getString("location"));
                            } else {
                                Log.e("STITCH", "Error sending text :", task.getException());
                            }
                        }
                    });
            return 0;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return 1;

    }


    private void InsertRecord() {
        StitchClientManager.getStitchClient();
    }


    /**
     * Check for multiple permissions.
     * @return
     */
    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }


        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    MY_PERMISSIONS_REQUEST );

            return false;
        }
        return true;
    }




}
