package com.mongodb.tse.sampleapp;

import com.mongodb.stitch.android.StitchClient;

class AppConfig {
    static private final String appId = "simpatizantes-ohqjw";
    private static StitchClient stitchClient;
    private static String s3ServiceName = "credenciales";
    private static String getS3Bucket = "credenciales-simpatizantes";


    public static String getAppId() {
        return appId;
    }

    public static String getS3ServiceName() {
        return s3ServiceName;
    }

    public static String getS3BucketName() {
        return getS3Bucket;
    }
}
