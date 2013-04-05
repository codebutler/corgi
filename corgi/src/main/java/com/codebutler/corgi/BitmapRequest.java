package com.codebutler.corgi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Date;

public class BitmapRequest extends Request<Bitmap> {
    private final HttpClient mHttpClient;
    private final String     mUrl;
    private final CachePath  mCachePath;

    public BitmapRequest(HttpClient httpClient, String url) {
        mHttpClient = httpClient;
        mUrl        = url;
        mCachePath  = new CachePath("bitmap", mUrl);
    }

    public BitmapRequest(HttpClient httpClient, String url, CachePath cachePath) {
        mHttpClient = httpClient;
        mUrl        = url;
        mCachePath  = cachePath;
    }

    @Override
    public void fetch(final RequestCallback<Bitmap> callback) {
        new BitmapDownloaderTask(mHttpClient, (String) mUrl) {
            @Override
            protected void onPostExecute(TaskResult<Bitmap> result) {
                if (result.success()) {
                    callback.onComplete(new Response(mUrl, result.getObject()));
                } else {
                    callback.onComplete(new Response(mUrl, result.getException()));
                }
            }
        }.execute();
    }

    @Override
    public CachePath getCachePath() {
        return mCachePath;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    public Response readResponse(InputStream stream) throws Exception {
        ObjectInputStream objectStream = new ObjectInputStream(stream);
        Date   date         = (Date)   objectStream.readObject();
        byte[] buffer       = (byte[]) objectStream.readObject();
        String errorMessage = (String) objectStream.readObject();

        Bitmap    bitmap = (buffer       != null) ? BitmapFactory.decodeByteArray(buffer, 0, buffer.length) : null;
        Exception error  = (errorMessage != null) ? new Exception(errorMessage)                             : null;
        return new Response(mUrl, date, bitmap, error);
    }

    public static class Response extends com.codebutler.corgi.Response<Bitmap> {
        private final String mUrl;

        private Response(String url, Bitmap bitmap) {
            super(bitmap);
            mUrl = url;
        }

        private Response(String url, Exception error) {
            super(error);
            mUrl = url;
        }

        private Response(String url, Date date, Bitmap bitmap, Exception error) {
            super(date, bitmap, error);
            mUrl = url;
        }

        public String getUrl() {
            return mUrl;
        }

        @Override
        public void write(OutputStream stream) throws Exception {
            ObjectOutputStream objectStream = null;
            try {
                objectStream = new ObjectOutputStream(stream);
                objectStream.writeObject(getDate());

                // Bitmap is not serializable...
                Bitmap bitmap = getObject();
                if (bitmap != null) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, buffer);
                    objectStream.writeObject(buffer.toByteArray());
                } else {
                    objectStream.writeObject(null);
                }

                Exception error = getError();
                if (error != null) {
                    objectStream.writeObject(error.toString());
                } else {
                    objectStream.writeObject(null);
                }
            } finally {
                if (objectStream != null) {
                    objectStream.flush();
                    objectStream.close();
                }
            }
        }

        @Override
        public CachePolicy getCachePolicy() {
            return new CachePolicy.Builder()
                .maxAge(1800000)
                .cacheErrors(true)
                .build();
        }
    }

    private static abstract class BitmapDownloaderTask extends AsyncTask<Void, Void, TaskResult<Bitmap>> {
        private final HttpClient mHttpClient;
        private final String     mUrl;

        private BitmapDownloaderTask(HttpClient httpClient, String url) {
            mHttpClient = httpClient;
            mUrl        = url;
        }

        @Override
        protected TaskResult<Bitmap> doInBackground(Void... unused) {
            try {
                HttpGet getRequest = new HttpGet(mUrl);
                HttpResponse response = mHttpClient.execute(getRequest);

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    throw new Exception("Error " + statusCode);
                }

                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    throw new Exception("No response entity");
                }

                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap == null) {
                        throw new Exception("Bitmap was empty");
                    }
                    return new TaskResult<Bitmap>(bitmap);
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            } catch (Exception ex) {
                return new TaskResult<Bitmap>(ex);
            }
        }
    }

    public static class TaskResult<T> {
        private final T         mObject;
        private final Exception mException;

        public TaskResult(T object) {
            if (object == null) {
                throw new IllegalArgumentException("object is required");
            }

            mObject = object;
            mException = null;
        }

        public TaskResult(Exception exception) {
            if (exception == null) {
                throw new IllegalArgumentException("exception is required");
            }

            mException = exception;
            mObject = null;
        }

        public boolean success() {
            return (mException == null);
        }

        public T getObject() {
            return mObject;
        }

        public Exception getException() {
            return mException;
        }
    }
}