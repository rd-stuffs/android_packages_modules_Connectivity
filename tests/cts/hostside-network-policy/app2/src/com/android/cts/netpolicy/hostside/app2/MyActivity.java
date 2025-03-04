/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.cts.netpolicy.hostside.app2;

import static com.android.cts.netpolicy.hostside.app2.Common.ACTION_FINISH_ACTIVITY;
import static com.android.cts.netpolicy.hostside.app2.Common.TAG;
import static com.android.cts.netpolicy.hostside.app2.Common.TYPE_COMPONENT_ACTIVTY;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteCallback;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.GuardedBy;

/**
 * Activity used to bring process to foreground.
 */
public class MyActivity extends Activity {

    @GuardedBy("this")
    private BroadcastReceiver finishCommandReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MyActivity.onCreate()");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void finish() {
        synchronized (this) {
            if (finishCommandReceiver != null) {
                unregisterReceiver(finishCommandReceiver);
                finishCommandReceiver = null;
            }
        }
        super.finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "MyActivity.onStart()");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "MyActivity.onNewIntent()");
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MyActivity.onResume(): " + getIntent());
        Common.notifyNetworkStateObserver(this, getIntent(), TYPE_COMPONENT_ACTIVTY);
        synchronized (this) {
            finishCommandReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG, "Finishing MyActivity");
                    MyActivity.this.finish();
                }
            };
            registerReceiver(finishCommandReceiver, new IntentFilter(ACTION_FINISH_ACTIVITY),
                    Context.RECEIVER_EXPORTED);
        }
        final RemoteCallback callback = getIntent().getParcelableExtra(
                Intent.EXTRA_REMOTE_CALLBACK);
        if (callback != null) {
            callback.sendResult(null);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "MyActivity.onDestroy()");
        super.onDestroy();
    }
}
