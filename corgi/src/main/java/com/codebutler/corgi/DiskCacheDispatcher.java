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

import android.util.Log;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class DiskCacheDispatcher extends Thread {
    private static final String TAG = "CorgiCacheDispatcher";

    /** The queue of request coming in for triage. */
    private final BlockingQueue<Request> mCacheQueue;

    /** The queue of request going out to the network. */
    private final BlockingQueue<Request> mRequestQueue;

    /** The disk cache to read from. */
    private final DiskCache mCache;

    /** For posting responses. */
    private final Corgi mCorgi;

    /** Used for telling us to die. */
    private volatile boolean mQuit = false;

    /**
     * Creates a new cache triage dispatcher thread.  You must call {@link #start()}
     * in order to begin processing.
     *
     * @param cacheQueue Queue of incoming requests for triage
     * @param requestQueue Queue to post requests that require network to
     * @param cache Cache to use for resolution
     */
    public DiskCacheDispatcher(Corgi corgi, BlockingQueue<Request> cacheQueue, BlockingQueue<Request> requestQueue, DiskCache cache) {
        mCorgi = corgi;
        mCacheQueue = cacheQueue;
        mRequestQueue = requestQueue;
        mCache = cache;
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
        while (true) {
            try {
                Request request = mCacheQueue.take();

                Log.d(TAG, "Got request: " + request);

                // Attempt to retrieve this item from cache.
                Response response = readFromCache(request);
                if (response == null) {
                    Log.d(TAG, "Found in cache: " + request + " " + response);
                    mRequestQueue.put(request);
                    continue;
                }

                // If it is completely expired, just send it to the network.
                if (!response.isValid()) {
                    Log.d(TAG, "Response is invalid: " + request);
                    mRequestQueue.put(request);
                    continue;
                }

                Log.d(TAG, "Cache hit! " + request);

                // We have a cache hit.
                mCorgi.finish(request, response);

            } catch (InterruptedException ignored) {
                // We may have been interrupted because it was time to quit.
                if (mQuit) {
                    return;
                }
            }
        }
    }

    private Response readFromCache(Request request) {
        String cacheKey = request.getCacheKey();
        try {
            DiskLruCache.Snapshot snapshot = mCache.get(cacheKey);
            if (snapshot == null) {
                return null;
            }
            return request.readResponse(snapshot.getInputStream(0));
        } catch (Exception ex) {
            try {
                mCache.remove(cacheKey);
            } catch (IOException ignored) {}
        }
        return null;
    }
}
