/*
 * Copyright (C) 2013 Eric Butler
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

import java.io.OutputStream;
import java.util.Date;

public abstract class Response<T> {
    private final T mObject;
    private final Exception mError;
    private final Date mDate;

    protected Response(T object) {
        if (object == null) {
            throw new IllegalArgumentException("object is required");
        }

        mDate   = new Date();
        mObject = object;
        mError  = null;
    }

    protected Response(Exception error) {
        if (error == null) {
            throw new IllegalArgumentException("error is required");
        }

        mDate   = new Date();
        mError   = error;
        mObject  = null;
    }

    protected Response(Date date, T object, Exception error) {
        if (object == null && error == null) {
            throw new IllegalArgumentException("object or error required");
        }

        mDate   = date;
        mObject = object;
        mError  = error;
    }

    public boolean success() {
        return (mError == null);
    }

    public T getObject() {
        return mObject;
    }

    public Exception getError() {
        return mError;
    }

    public Date getDate() {
        return mDate;
    }

    public CachePolicy getCachePolicy() {
        return CachePolicy.NO_CACHE;
    }

    public void write(OutputStream stream) throws Exception {
        throw new IllegalStateException("not implemented");
    }

    public boolean isValid() {
        CachePolicy cachePolicy = getCachePolicy();
        if (cachePolicy == CachePolicy.FOREVER) {
            return true;
        }
        long expiresAt = getDate().getTime() + cachePolicy.getMaxAge();
        return (new Date().getTime() < expiresAt);
    }

}
