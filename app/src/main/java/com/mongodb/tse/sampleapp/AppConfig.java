package com.mongodb.tse.sampleapp;

import com.mongodb.stitch.android.StitchClient;
import com.mongodb.stitch.android.services.mongodb.MongoClient;

class AppConfig {
    static private final String appId = "simpatizantes-ohqjw";


    private static String s3ServiceName = "credenciales";
    private static String getS3Bucket = "credenciales-simpatizantes";

    private static String database = "simpatizantes";
    private static String collection = "upload";


    public static final String COGNITO_POOL_ID = "us-east-1:e83984dd-bdb3-4d19-a99b-ea6c54ee7593";
    public static final String COGNITO_POOL_REGION = "us-east-1";
    //public static final String BUCKET_NAME = "credenciales-simpatizantes";
    public static final String BUCKET_REGION = "us-east-1";



    private static StitchClient stitchClient;

    public static String getAppId() {
        return appId;
    }

    public static String getS3ServiceName() {
        return s3ServiceName;
    }

    public static String getS3BucketName() {
        return getS3Bucket;
    }

    public static String getDatabase() {
        return database;
    }

    public static String getCollection() {
        return collection;
    }
}
