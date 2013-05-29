/*
 * Copyright (C) 2013 Eric Butler
 *
 * Based on code from Volley
 * Copyright (C) 2011 The Android Open Source Project
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

package com.codebutler.corgi;

import android.os.Process;
import android.util.Log;
import com.jakewharton.disklrucache.DiskLruCache;

import java.util.concurrent.BlockingQueue;

public class RequestDispatcher extends Thread {
    private static final String TAG = "CorgiRequestDispatcher";

    /** The queue of requests to service. */
    private final BlockingQueue<Request> mQueue;

    /** The cache to write to. */
    private final DiskLruCache mCache;

    /** For posting responses and errors. */
    private final BlockingQueue<RequestResponse> mResponseQueue;

    /** Used for telling us to die. */
    private volatile boolean mQuit = false;

    public RequestDispatcher(BlockingQueue<Request> queue, DiskLruCache cache, BlockingQueue<RequestResponse> responseQueue) {
        mQueue = queue;
        mCache = cache;
        mResponseQueue = responseQueue;
    }

    /**
     * Forces this dispatcher to quit immediately.  If any requests are still in
     * the queue, they are not guaranteed to be processed.
     */
    public void quit() {
        mQuit = true;
        interrupt();
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        while (true) {
            try {
                // Take a request from the queue.
                final Request request = mQueue.take();

                Log.d(TAG, "Got request " + request);

                // Perform the request asynchronously. Post response to ResponseDispatcher.
                request.fetch(new RequestCallback() {
                    @Override
                    public void onExtraResponse(String cacheKey, Response response) {
                        try {
                            Log.d(TAG, "onExtraResponse: " + request + " " + cacheKey + " " + response);
                            mResponseQueue.put(new RequestResponse(cacheKey, response));
                        } catch (InterruptedException ignored) {}
                    }

                    @Override
                    public void onComplete(Response response) {
                        try {
                            Log.d(TAG, "onResponse: " + request + " " + response);
                            mResponseQueue.put(new RequestResponse(request, response));
                        } catch (InterruptedException ignored) {}
                    }
                });

            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (mQuit) {
                    return;
                }
            }
        }
    }
}
