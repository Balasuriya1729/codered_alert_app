package com.example.codered;


import android.app.Application;
import android.os.Looper;
import android.util.Log;

import com.google.api.client.util.DateTime;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

interface ErrorBoundary {
    void uncaughtException(Thread thread, Throwable throwable);
}

public class MyApplication extends Application implements ErrorBoundary {

    @Override
    public void onCreate() {
        super.onCreate();
        setupExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        throwable.printStackTrace();
        Log.d("uncaughtException", throwable.getMessage());
        logOnFirebase(throwable.getMessage());
        try {
            Looper.loop();
        } catch (Throwable t) {
            if (Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
                uncaughtException(Looper.getMainLooper().getThread(), t);
            }
        }
    }

    private void logOnFirebase(String msg) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String id = Store.getUser().getUserId()+ (new DateTime(System.currentTimeMillis()));
        mDatabase.child("Logs").child(id).setValue(msg);
    }

    public void setupExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(this::uncaughtException);
    }
}