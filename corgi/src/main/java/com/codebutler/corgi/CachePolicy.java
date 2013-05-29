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
