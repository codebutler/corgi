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

import android.content.Context;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.IOException;

public class DiskCache {
    /** Application context. */
    private final Context mContext;

    /** Wrapped DiskLruCache. */
    private DiskLruCache mDiskLruCache;

    public DiskCache(Context context) {
        mContext = context;
        mDiskLruCache = Utils.openDiskLruCache(context);
    }

    public DiskLruCache.Snapshot get(String key) throws IOException {
        return mDiskLruCache.get(key);
    }

    public DiskLruCache.Editor edit(String key) throws IOException {
        return mDiskLruCache.edit(key);
    }

    public boolean remove(String key) throws IOException {
        // FIXME: Remove all entries that begin with 'key'
        return mDiskLruCache.remove(key);
    }

    public void clear() throws IOException {
        // FIXME: Extend DiskLruCache to support clearing without closing.
        // Then this entire file can go away!
        mDiskLruCache.delete();
        mDiskLruCache = Utils.openDiskLruCache(mContext);
    }
}
