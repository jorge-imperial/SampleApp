package com.mongodb.tse.sampleapp;

import com.mongodb.stitch.android.StitchClient;
import com.mongodb.stitch.android.services.mongodb.MongoClient;

class AppConfig {
    static private final String appId = "simpatizantes-krlwe";


    private static String s3ServiceName = "";
    private static String getS3Bucket = "";

    private static String database = "simpatizantes";
    private static String collection = "upload";


    public static final String COGNITO_POOL_ID = "";
    public static final String COGNITO_POOL_REGION = "";

    public static final String BUCKET_REGION = "";



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
