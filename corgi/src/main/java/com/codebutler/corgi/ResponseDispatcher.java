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

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.OutputStream;
import java.util.concurrent.PriorityBlockingQueue;

public class ResponseDispatcher extends Thread {
    /** For posting responses. */
    private final Corgi mCorgi;

    /** Used for telling us to die. */
    private volatile boolean mQuit = false;

    private final PriorityBlockingQueue<RequestResponse> mResponseQueue;
    private final LruCache<String, Response> mMemoryCache;
    private final DiskLruCache mDiskCache;

    public ResponseDispatcher(Corgi corgi, PriorityBlockingQueue<RequestResponse> responseQueue, LruCache<String, Response> memoryCache, DiskLruCache diskCache) {
        mCorgi = corgi;
        mResponseQueue = responseQueue;
        mMemoryCache = memoryCache;
        mDiskCache = diskCache;
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
                RequestResponse info = mResponseQueue.take();

                String cacheKey = info.getCacheKey() != null ? info.getCacheKey() : info.getRequest().getCacheKey(); // FIXME
                Response response = info.getResponse();
                if (cacheKey != null && response.success()) {
                    mMemoryCache.put(cacheKey, response);

                    DiskLruCache.Editor editor = mDiskCache.edit(cacheKey);
                    OutputStream outputStream = editor.newOutputStream(0);
                    response.write(outputStream);
                    outputStream.flush();
                    outputStream.close();
                    editor.commit();
                }

                if (info.getRequest() != null) {
                    mCorgi.finish(info.getRequest(), response);
                }

            } catch (InterruptedException ignored) {
                if (mQuit) {
                    return;
                }

            } catch (Exception ex) {
                throw new RuntimeException(ex); // FIXME
            }
        }
    }
}
