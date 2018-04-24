package com.mongodb.tse.sampleapp;

import com.mongodb.stitch.android.StitchClient;

interface StitchClientListener {
    // Method that will be called once in an Activity's
    // lifetime with an initialized StitchClient
    void onReady(StitchClient stitchClient);
}
