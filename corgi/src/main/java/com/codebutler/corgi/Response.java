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
