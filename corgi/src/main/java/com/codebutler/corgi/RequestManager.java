package com.codebutler.corgi;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.util.Log;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequestManager {
    private static final String TAG = "RequestManager";
    private static final int MAX_MEMORY = (int) (Runtime.getRuntime().maxMemory() / 1024);

    private final LruCache<String, Response> mCache             = new LruCache<String, Response>(MAX_MEMORY / 8);
    private final List<String>               mPendingRequests   = Collections.synchronizedList(new ArrayList<String>());
    private final Handler                    mMainThreadHandler = new Handler();
    private final File                       mCacheDir;
    private final Bus                        mBus;

    public RequestManager(File cacheDir, Bus bus) {
        mCacheDir = cacheDir;
        mBus      = bus;

        mBus.register(this);
    }

    public <T> Response<T> get(Request<T> request, boolean fetchIfNotFound) {
        String key = request.getCachePath().toString();
        synchronized (mCache) {
            Response<T> response = (Response<T>) mCache.get(key);
            if (response != null) {
                //noinspection unchecked
                if (response.isValid()) {
                    if (!response.getCachePolicy().shouldKeepInMemory()) {
                        mCache.remove(key);
                    }
                    return response;
                } else {
                    mCache.remove(key);
                    // FIXME: Also remove from disk cache, if found...
                }
            }
        }
        if (fetchIfNotFound) {
            fetch(request);
        }
        return null;
    }

    public <T> void fetch(final Request<T> request) {
        fetch(request, request.ignoreCache());
    }

    public <T> void fetch(final Request<T> request, boolean ignoreCache) {
        if (!markRequestStarted(request)) {
            return;
        }

        if (ignoreCache || request.getCachePath() == null) {
            fetchResponse(request);
            return;
        }

        final String key = request.getCachePath().toString();

        synchronized (mCache) {
            Response response = mCache.get(key);
            if (response != null) {
                if (response.isValid()) {
                    if (!response.getCachePolicy().shouldKeepInMemory()) {
                        mCache.remove(key);
                    }
                    Log.d(TAG, "MEM HIT:   " + request + " " + key);
                    postResponse(request, response);
                    return;
                } else {
                    Log.d(TAG, "MEM INVAL  " + request + " " + key);
                    removeCache(request.getCachePath());
                }
            } else {
                Log.d(TAG, "MEM MISS:   " + request + " " + key);
            }
        }

        Log.d(TAG, "DISK CHECK: " + request + " " + request.getCachePath());
        checkDiskCache(request, new ResponseCallback<T>() {
            @Override
            public void onResponse(Response<T> response) {
                if (response != null) {
                    if (response.isValid()) {
                        Log.d(TAG, "DISK HIT:   " + request + " " + request.getCachePath() + " " + response);
                        putMemCache(request.getCachePath(), response);
                        postResponse(request, response);
                    } else {
                        Log.d(TAG, "DISK INVAL: " + request + " " + request.getCachePath() + " " + response);
                        removeCache(request.getCachePath());
                    }
                } else {
                    Log.d(TAG, "DISK MISS:  " + request + " " + request.getCachePath());
                    fetchResponse(request);
                }
            }
        });
    }

    public void removeCache(final CachePath cachePath) {
        Log.d(TAG, "removeCache: " + cachePath);
        synchronized (mCache) {
            for (String key : mCache.snapshot().keySet()) {
                if (key.startsWith(cachePath.toString())) {
                    mCache.remove(key);
                }
            }
        }
        File[] files = mCacheDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith(cachePath.toString())) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
        }
    }

    public void clearCache() {
        synchronized (mCache) {
            mCache.evictAll();
        }
        File[] files = mCacheDir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    @Subscribe
    public void onRequest(Request request) {
      fetch(request);
    }

    private boolean markRequestStarted(Request request) {
        if (request.getCachePath() != null) {
            final String cacheKey = request.getCachePath().toString();
            synchronized (mPendingRequests) {
                if (mPendingRequests.contains(cacheKey)) {
                    return false;
                }
                mPendingRequests.add(cacheKey);
                return true;
            }
        }
        return true;
    }

    private void markRequestComplete(Request request) {
        if (request.getCachePath() != null) {
            synchronized (mPendingRequests) {
                mPendingRequests.remove(request.getCachePath().toString());
            }
        }
    }

    private <T> void checkDiskCache(final Request<T> request, final ResponseCallback<T> callback) {
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                AsyncTask<Void, Void, Response<T>> task = new AsyncTask<Void, Void, Response<T>>() {
                    @Override
                    protected Response<T> doInBackground(Void... voids) {
                        File file = null;
                        try {
                            file = new File(mCacheDir, request.getCachePath().toString());
                            if (file.exists()) {
                                FileInputStream stream = null;
                                try {
                                    stream = new FileInputStream(file);
                                    Response<T> response = request.readResponse(stream);
                                    if (response != null && response.isValid()) {
                                        return response;
                                    } else {
                                        //noinspection ResultOfMethodCallIgnored
                                        file.delete();
                                    }
                                } finally {
                                    if (stream != null) {
                                        try {
                                            stream.close();
                                        } catch (IOException ignored) {}
                                    }
                                }
                            }
                        } catch (Throwable e) {
                            Log.e(TAG, "Error reading disk cache for: " + request + " " + request.getCachePath(), e);
                            if (file != null && file.exists()) {
                                file.delete();
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Response<T> response) {
                        callback.onResponse(response);
                    }
                };
                task.execute();
            }
        });
    }

    private <T> void fetchResponse(final Request<T> request) {
        Log.d(TAG, "FETCH:      " + request + " " + request.getCachePath());
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                request.fetch(new RequestCallback<T>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public void onExtraResponse(CachePath cachePath, Response response) {
                        if (response.success()) {
                            putMemCache(cachePath, response);
                            putDiskCache(cachePath, response);
                        }
                    }

                    @Override
                    public void onComplete(final Response<T> response) {
                        if (response.success()) {
                            putMemCache(request.getCachePath(), response);
                            putDiskCache(request.getCachePath(), response);
                        }
                        postResponse(request, response);
                    }
                });
            }
        });
    }

    private <T> void putMemCache(CachePath cachePath, Response<T> response) {
        if (response.getCachePolicy().getMaxAge() > 0) {
            synchronized (mCache) {
                Log.d(TAG, "MEM PUT:    " + cachePath.toString());
                mCache.put(cachePath.toString(), response);
            }
        }
    }

    public <T> void putDiskCache(final CachePath cachePath, final Response<T> response) {
        if (response.getCachePolicy().getMaxAge() > 0) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            Log.d(TAG, "DISK PUT:   " + cachePath);
                            File file = new File(mCacheDir, cachePath.toString());
                            FileOutputStream stream = null;
                            try {
                                stream = new FileOutputStream(file);
                                response.write(stream);
                                stream.flush();
                            } catch (Throwable ex) {
                                Log.e(TAG, "Error writing cache (" + cachePath + ")", ex);
                                if (file.exists()) {
                                    file.delete();
                                }
                            } finally {
                                if (stream != null) {
                                    try {
                                        stream.close();
                                    } catch (IOException ignored) {
                                    }
                                }
                            }
                            return null;
                        }
                    };
                    task.execute();
                }
            });
        }
    }

    private <T> void postResponse(Request<T> request, Response<T> response) {
        if (!response.success()) {
            Log.e(TAG, String.format("Request failed (%s, %s)", request.getClass().getName(), request.getCachePath()), response.getError());
        }
        mBus.post(response);
        markRequestComplete(request);
    }

    private static interface ResponseCallback<T> {
        public void onResponse(Response<T> response);
    }
}
