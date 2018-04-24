package com.mongodb.tse.sampleapp;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LoginCompletionListener implements OnCompleteListener {
    public boolean result;
    public boolean triggered = false;

    @Override
    public void onComplete(@NonNull Task task) {
        if (task.isSuccessful()) {
            Log.d("stitch", "task was successful!");
            result = true;
        }
        else {
            Log.e("stitch", "task failed!");
            result = false;
        }
        triggered = true;
    }
}
