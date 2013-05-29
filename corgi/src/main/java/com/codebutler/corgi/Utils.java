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

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.StatFs;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;

public class Utils {
    private static final int MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final int MAX_MEM_CACHE_SIZE = 20 * 1024 * 1024; // 20MB

    static int calculateDiskCacheSize(File dir) {
      StatFs statFs = new StatFs(dir.getAbsolutePath());
      int available = statFs.getBlockCount() * statFs.getBlockSize();
      // Target 2% of the total space.
      int size = available / 50;
      // Bound inside min/max size for disk cache.
      return Math.max(Math.min(size, MAX_DISK_CACHE_SIZE), MIN_DISK_CACHE_SIZE);
    }

    static int calculateMemoryCacheSize(Context context) {
      ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
      boolean largeHeap = (context.getApplicationInfo().flags & FLAG_LARGE_HEAP) != 0;
      int memoryClass = am.getMemoryClass();
      if (largeHeap && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        memoryClass = ActivityManagerHoneycomb.getLargeMemoryClass(am);
      }
      // Target 15% of the available RAM.
      int size = 1024 * 1024 * memoryClass / 7;
      // Bound to max size for mem cache.
      return Math.min(size, MAX_MEM_CACHE_SIZE);
    }

    public static DiskLruCache openDiskLruCache(Context context) {
        try {
            File cacheDir = new File(context.getCacheDir(), "corgi");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            return DiskLruCache.open(cacheDir, getAppVersion(context), 1, calculateDiskCacheSize(cacheDir));
        } catch (IOException ex) {
            throw new RuntimeException("Failed to open DiskLruCache", ex);
        }
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to get app version", ex);
        }
    }

    private static class ActivityManagerHoneycomb {
      static int getLargeMemoryClass(ActivityManager activityManager) {
        return activityManager.getLargeMemoryClass();
      }
    }
}
