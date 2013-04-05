package com.codebutler.corgi;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
        throw new NotImplementedException();
    }

    public boolean isValid() {
        long expiresAt = getDate().getTime() + getCachePolicy().getMaxAge();
        return (new Date().getTime() < expiresAt);
    }

    public static class CachePolicy {
        public static final CachePolicy NO_CACHE = new CachePolicy(0);

        public static class Builder {
            private boolean mCacheErrors  = false;
            private long    mMaxAge       = 0;
            private boolean mKeepInMemory = true;

            public Builder maxAge(long maxAge) {
                mMaxAge = maxAge;
                return this;
            }

            public Builder cacheErrors(boolean cacheErrors) {
                mCacheErrors = cacheErrors;
                return this;
            }

            public Builder keepInMemory(boolean keepInMemory) {
                mKeepInMemory = keepInMemory;
                return this;
            }

            public CachePolicy build() {
                return new CachePolicy(mMaxAge, mCacheErrors, mKeepInMemory);
            }
        }

        private final long    mMaxAge;
        private final boolean mCacheErrors;
        private final boolean mKeepInMemory;

        public CachePolicy(long maxAge) {
            this(maxAge, false, true);
        }

        private CachePolicy(long maxAge, boolean cacheErrors, boolean keepInMemory) {
            mMaxAge       = maxAge;
            mCacheErrors  = cacheErrors;
            mKeepInMemory = keepInMemory;
        }

        public long getMaxAge() {
            return mMaxAge;
        }

        public boolean shouldCacheErrors() {
            return mCacheErrors;
        }

        public boolean shouldKeepInMemory() {
            return mKeepInMemory;
        }
    }
}
