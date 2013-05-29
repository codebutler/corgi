/*
 * Copyright (C) 2013 Eric Butler
 *
 * Based on code from Volley
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Based on code from Picasso
 * Copyright (C) 2013 Square, Inc.
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

import android.content.Context;
import android.util.Log;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

public class Corgi {
    private static final String TAG = "Corgi";

    /**
     * Staging area for requests that already have a duplicate request in flight.
     *
     * <ul>
     *     <li>containsKey(cacheKey) indicates that there is a request in flight for the given cache
     *          key.</li>
     *     <li>get(cacheKey) returns waiting requests for the given cache key. The in flight request
     *          is <em>not</em> contained in that list. Is null if no requests are staged.</li>
     * </ul>
     */
    private final Map<String, Queue<Request>> mWaitingRequests =
        new HashMap<String, Queue<Request>>();

    /** The memory cache. */
    private final LruCache<String, Response> mMemoryCache;

    /** The disk cache. */
    private final DiskLruCache mDiskCache;

    /** The disk cache dispatcher. */
    private DiskCacheDispatcher mDiskCacheDispatcher;

    /** The request dispatcher. */
    private RequestDispatcher mRequestDispatcher;

    /** The response dispatcher. */
    private ResponseDispatcher mResponseDispatcher;

    /** The disk cache triage queue. */
    private final PriorityBlockingQueue<Request> mDiskCacheQueue =
        new PriorityBlockingQueue<Request>();

    /** The queue of requests that need to be fetched. */
    private final PriorityBlockingQueue<Request> mRequestQueue =
        new PriorityBlockingQueue<Request>();

    /** The queue of responses that need to be cached and delivered. */
    private final PriorityBlockingQueue<RequestResponse> mResponseQueue =
        new PriorityBlockingQueue<RequestResponse>();

    /** Callback for finished responses */
    private final Listener mListener;

    public static interface Listener {
        void onResponse(Response response);
    }

    public Corgi(Context context, Listener listener) {
        mMemoryCache = new LruCache<String, Response>(Utils.calculateMemoryCacheSize(context));
        mDiskCache   = Utils.openDiskLruCache(context);
        mListener    = listener;
    }

    public void start() {
        // Make sure any currently running dispatchers are stopped.
        stop();

        // Create the disk cache dispatcher and start it.
        mDiskCacheDispatcher = new DiskCacheDispatcher(this, mDiskCacheQueue, mRequestQueue, mDiskCache);
        mDiskCacheDispatcher.start();

        mRequestDispatcher = new RequestDispatcher(mRequestQueue, mResponseQueue);
        mRequestDispatcher.start();

        mResponseDispatcher = new ResponseDispatcher(this, mResponseQueue, mMemoryCache, mDiskCache);
        mResponseDispatcher.start();
    }

    public void stop() {
        if (mDiskCacheDispatcher != null) {
            mDiskCacheDispatcher.quit();
        }
        if (mRequestDispatcher != null) {
            mRequestDispatcher.quit();
        }
        if (mResponseDispatcher != null) {
            mResponseDispatcher.quit();
        }
    }

    public <T> void fetch(final Request<T> request) {
        // If the request is uncacheable, skip the cache queue and queue for fetch.
        if (!request.shouldCache() || !request.shouldCheckCache()) {
            mRequestQueue.add(request);
            return;
        }

        // Check memory cache before dispatching to a background thread.
        Response response = checkMemoryCache(request.getCacheKey());
        if (response != null) {
            mListener.onResponse(response);
            return;
        }

        // Insert request into stage if there's already a request with the same cache key in flight.
        synchronized (mWaitingRequests) {
            String cacheKey = request.getCacheKey();
            if (mWaitingRequests.containsKey(cacheKey)) {
                // There is already a request in flight. Queue up.
                Queue<Request> stagedRequests = mWaitingRequests.get(cacheKey);
                if (stagedRequests == null) {
                    stagedRequests = new LinkedList<Request>();
                }
                stagedRequests.add(request);
                mWaitingRequests.put(cacheKey, stagedRequests);
                Log.v(TAG, String.format("Request for cacheKey=%s is in flight, putting on hold.", cacheKey));
            } else {
                // Insert 'null' queue for this cacheKey, indicating there is now a request in
                // flight.
                mWaitingRequests.put(cacheKey, null);
                mDiskCacheQueue.add(request);
            }
        }
    }

    public void removeCache(String cacheKey) {
        try {
            mMemoryCache.remove(cacheKey);
            mDiskCache.remove(cacheKey);
        } catch (IOException e) {
            throw new RuntimeException("Error removing entry from disk cache", e);
        }
    }

    public void clearCache() {
        try {
            mMemoryCache.evictAll();
            mDiskCache.delete();
        } catch (IOException ex) {
            throw new RuntimeException("Error clearing disk cache", ex);
        }
    }

    private Response checkMemoryCache(String cacheKey) {
        synchronized (mMemoryCache) {
            Response response = mMemoryCache.get(cacheKey);
            if (response != null) {
                if (response.isValid()) {
                    if (!response.getCachePolicy().shouldKeepInMemory()) {
                        mMemoryCache.remove(cacheKey);
                    }
                    Log.d(TAG, "MEM HIT:   " + cacheKey);
                    return response;
                } else {
                    Log.d(TAG, "MEM INVAL  " +  cacheKey);
                    mMemoryCache.remove(cacheKey);
                }
            } else {
                Log.d(TAG, "MEM MISS:   " + cacheKey);
            }
        }
        return null;
    }

    void finish(Request request, Response response) {
        String cacheKey = request.getCacheKey();
        if (cacheKey != null) {
            synchronized (mWaitingRequests) {
                Queue<Request> waitingRequests = mWaitingRequests.remove(cacheKey);
                if (waitingRequests != null) {
                    Log.v(TAG, String.format("Releasing %d waiting requests for cacheKey=%s.",
                        waitingRequests.size(), cacheKey));
                    // Process all queued up requests. They won't be considered as in flight, but
                    // that's not a problem as the cache has been primed by 'request'.
                    mDiskCacheQueue.addAll(waitingRequests);
                }
            }
        }

        mListener.onResponse(response);
    }
}
