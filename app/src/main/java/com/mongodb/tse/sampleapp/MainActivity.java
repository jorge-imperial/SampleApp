package com.mongodb.tse.sampleapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.stitch.android.auth.emailpass.EmailPasswordAuthProvider;
import com.mongodb.stitch.android.services.mongodb.MongoClient;
import org.bson.Document;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;



public class MainActivity extends AppCompatActivity  {



    private Bitmap myBitmap;
    private Uri picUri;

    private static final int UPLOAD_FOTOS_RESULT = 108;

    private final static int REQUEST_CODE_BACK_RESULT = 200;
    private final static int REQUEST_CODE_FRONT_RESULT = 201;

    private final static int REQUEST_ALL = 100;


    String mCurrentPhotoPath;
    String pathFrontImg;
    String pathBackImg;

    private String user;
    private String password;

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
                // Crea actividad
                if (UploadImages())
                    InsertRecord();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            this.user = b.getString("user");
            this.password = b.getString("password");
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]
                        {
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,


                        }, REQUEST_ALL);
            }

            return;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_ALL) {

            boolean badPermissions = false;
            for (int i=0; i<grantResults.length; ++i) {

                switch (permissions[i]) {

                    case Manifest.permission.CAMERA:
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                            badPermissions = true;
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                            badPermissions = true;

                }

            }
        }


    }



    public void onClicFront(View view) {
        // Init and start an intent.
        StartIntentForImage(REQUEST_CODE_FRONT_RESULT, "anterior_");
    }

    public void onClickBack(View view) {
        // Init and start an intent.
        StartIntentForImage(REQUEST_CODE_BACK_RESULT, "posterior_");
    }

    private void StartIntentForImage(int requestCodeBack, String imagePrefix) {

        try {
            File photoFile = createImageFile(imagePrefix);
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.mongodb.tse.sampleapp.fileprovider",
                        photoFile);

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, requestCodeBack);
                }
            }
        }
        catch(IOException ex){
            Log.e("Exception" , ex.getMessage());
            return;
        }

    }

    private File createImageFile(String prefix) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = prefix + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();

        Log.d("FOTO", "Path " + mCurrentPhotoPath);
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        ImageView imageView;
        switch (requestCode) {
            case REQUEST_CODE_BACK_RESULT:
                imageView = findViewById(R.id.imageButtonBack);
                pathBackImg = mCurrentPhotoPath;
                break;
            case REQUEST_CODE_FRONT_RESULT:
                imageView = findViewById(R.id.imageButtonFront);
                pathFrontImg = mCurrentPhotoPath;
                break;
            default:
                imageView = null;
        }

        if (resultCode == Activity.RESULT_OK && imageView != null) {
            if(data.getData()!=null)  {

                try {
                    InputStream stream = getContentResolver().openInputStream(data.getData());
                    myBitmap = BitmapFactory.decodeStream(stream);
                    stream.close();
                    imageView.setImageBitmap(myBitmap);
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
                int targetW = imageView.getWidth();
                int targetH = imageView.getHeight();

                // Get the dimensions of the bitmap
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                // Determine how much to scale down the image
                int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;
                bmOptions.inPurgeable = true;

                myBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
                myBitmap.getNinePatchChunk();
                imageView.setImageBitmap(myBitmap);
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
        else if (requestCode == this.UPLOAD_FOTOS_RESULT ) {
            if (resultCode == 2) {
                InsertRecord();
            }
        }
    }


    /**
     * Inserta foto en la base de datos (TEST ONLY)
     * @return
     */
    private boolean UploadImages() {

        File f = new File(pathFrontImg);
        int size = (int) f.length();

        try {
            int maxSize = 1024*1024*16;
            FileInputStream ifs = new FileInputStream(f);
            byte[] buffer = new byte[size];
            int len = ifs.read(buffer, 0, size);
            ifs.close();

            UploadS3(f, buffer);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }



    private int UploadS3(final File photoFile, byte [] buffer) {

        Document doc = new Document();
        doc.append("bucket", AppConfig.getS3BucketName());
        doc.append("key", photoFile.getName() );
        doc.append("contentType", "image/jpeg");
        doc.append("acl", "public-read");
        doc.append("body", new org.bson.types.Binary(buffer));

        StitchClientManager.getStitchClient().executeServiceFunction(
                "put",
                AppConfig.getS3ServiceName(),
                doc).addOnCompleteListener(new OnCompleteListener<Object>() {
            @Override
            public void onComplete(@NonNull final Task<Object> task) {
                if (task.isSuccessful()) {
                    Document d  = (Document) task.getResult();
                    Log.d("STITCH", "Successfully sent:" + d.getString("location"));
                } else {
                    Log.e("STITCH", "Error sending: " + photoFile.getName() + ": " + task.getException());
                }
            }
        });
        return 0;

    }


    private int InsertRecord()  {

        Log.d("MongoDB Atlas", "Insert record");

        final MongoClient mongoClient = new MongoClient(StitchClientManager.getStitchClient(), "mongodb-atlas");
        final MongoClient.Collection coll = mongoClient.getDatabase(AppConfig.getDatabase()).getCollection(AppConfig.getCollection());

        final Date current =  Calendar.getInstance().getTime();


        StitchClientManager.getStitchClient().logInWithProvider(new EmailPasswordAuthProvider(this.user, this.password)).continueWithTask(
                new Continuation<String, Task<Document>>() {
                    @Override
                    public Task<Document> then(@NonNull Task<String> task) throws Exception {
                        final Document insDoc = new Document(
                                "owner_id",
                                task.getResult()
                        );
                        insDoc.put("user", user);
                        insDoc.put("created", current.getTime());
                        insDoc.put("front", pathFrontImg);
                        insDoc.put("back", pathBackImg);
                        return coll.insertOne(insDoc);
                    }
                }
        ).continueWithTask(new Continuation<Document, Task<List<Document>>>() {
            @Override
            public Task<List<Document>> then(@NonNull Task<Document> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return coll.find(new Document("owner_id", StitchClientManager.getStitchClient().getUserId()), 1);
            }
        }).addOnCompleteListener(new OnCompleteListener<List<Document>>() {
            @Override
            public void onComplete(@NonNull Task<List<Document>> task) {
                if (task.isSuccessful()) {
                    Log.d("STITCH", task.getResult().toString());
                    return;
                }
                Log.e("STITCH", task.getException().toString());
            }
        });

        // Clean bitmaps.
        ImageView imageView = findViewById(R.id.imageButtonBack);
        imageView.clearColorFilter();

        imageView = findViewById(R.id.imageButtonFront);
        imageView.clearColorFilter();


        return 0;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("pic_uri", picUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        picUri = savedInstanceState.getParcelable("pic_uri");
    }
}

