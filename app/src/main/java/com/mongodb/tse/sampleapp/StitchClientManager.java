package com.mongodb.tse.sampleapp;

import android.content.Context;

import com.google.android.gms.tasks.OnSuccessListener;
import com.mongodb.stitch.android.StitchClient;
import com.mongodb.stitch.android.StitchClientFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class StitchClientManager {

    private static StitchClientManager _shared = null;

    private static StitchClient stitchClient;
    private List<StitchClientListener> listeners = new ArrayList<>();

    // Must call this at least once with application context before
    // registering any listener. This can be done at application
    // launch in a subclass of Application, or in each Activity's
    // onCreate() before registering a listener. This can be safely
    // called more than once, but calls beyond the first have no effect.
    public synchronized static void initialize(Context ctx) {
        if (_shared == null) {
            _shared = new StitchClientManager(ctx);
        }
    }

    // Method that should be called in an Activity's onCreate() to register
    // the Activity with the globally managed StitchClient.
    // Will result in NullPointerException if initialize() was never called.
    public synchronized static void registerListener(StitchClientListener listener) {
        _shared.listeners.add(listener);

        if (_shared.stitchClient != null) {
            ListIterator<StitchClientListener> it = _shared.listeners.listIterator();
            while (it.hasNext()) {
                StitchClientListener nextListener = it.next();
                nextListener.onReady(_shared.stitchClient);
                it.remove();
            }
        }
    }

    private StitchClientManager(Context ctx) {
        StitchClientFactory.create(ctx, AppConfig.getAppId()).addOnSuccessListener(new OnSuccessListener<StitchClient>() {
            @Override
            public void onSuccess(StitchClient stitchClient) {
                _shared.stitchClient = stitchClient;
                ListIterator<StitchClientListener> it = _shared.listeners.listIterator();
                while (it.hasNext()) {
                    StitchClientListener nextListener = it.next();
                    nextListener.onReady(_shared.stitchClient);
                    it.remove();
                }
            }
        });
    }

    public static void setClient(StitchClient client) {
        StitchClientManager.stitchClient = client;
    }

    public static StitchClient getStitchClient() {
        return stitchClient;
    }
}
