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

import java.io.InputStream;

public abstract class Request<T> implements Comparable<Request<T>> {
    /** Whether or not the cache should be checked before fetching this request. */
    private boolean mShouldCheckCache = true;

    public abstract void fetch(RequestCallback<T> callback);

    public String getCacheKey() {
        return null;
    }

    public Response<T> readResponse(InputStream stream) throws Exception {
        return null;
    }

    /**
     * Set whether or not the cache should be checked before fetching this request.
     */
    public final void setCheckCache(boolean checkCache) {
        mShouldCheckCache = checkCache;
    }

    /**
     * Whether or not the cache should be checked before fetching this request.
     */
    public boolean shouldCheckCache() {
        return mShouldCheckCache;
    }

    /**
     * Returns true if responses to this request should be cached.
     */
    public final boolean shouldCache() {
        return getCacheKey() != null;
    }

    @Override
    public int compareTo(Request<T> other) {
        return 0;
    }

    @Override
    public String toString() {
        if (getCacheKey() != null) {
            return "Request{cacheKey=" + getCacheKey() + "}";
        }
        return super.toString();
    }
}
