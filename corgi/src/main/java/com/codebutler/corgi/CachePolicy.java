package com.codebutler.corgi;

public class CachePolicy {
    public static final CachePolicy NO_CACHE = new CachePolicy(0);
    public static final CachePolicy FOREVER  = new CachePolicy(Long.MAX_VALUE);

    public static class Builder {
        private long    mMaxAge       = 0;
        private boolean mKeepInMemory = true;

        public Builder maxAge(long maxAge) {
            mMaxAge = maxAge;
            return this;
        }

        public Builder keepInMemory(boolean keepInMemory) {
            mKeepInMemory = keepInMemory;
            return this;
        }

        public CachePolicy build() {
            return new CachePolicy(mMaxAge, mKeepInMemory);
        }
    }

    private final long    mMaxAge;
    private final boolean mKeepInMemory;

    public CachePolicy(long maxAge) {
        this(maxAge, true);
    }

    CachePolicy(long maxAge, boolean keepInMemory) {
        mMaxAge       = maxAge;
        mKeepInMemory = keepInMemory;
    }

    public long getMaxAge() {
        return mMaxAge;
    }

    public boolean shouldKeepInMemory() {
        return mKeepInMemory;
    }
}
