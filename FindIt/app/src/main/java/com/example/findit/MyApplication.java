package com.example.findit;

import android.app.Application;

public class MyApplication extends Application {

    private MyApplicationCallback callback;

    public void registerCallback(MyApplicationCallback callback) {
        this.callback = callback;
    }

    public void performCallback() {
        if (callback != null) {
            callback.onCallback();
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        performCallback();
    }
}
