package com.codebutler.corgi;

import android.os.AsyncTask;

public abstract class SimpleAsyncTask<Result> extends AsyncTask<Void, Void, SimpleTaskResult<Result>> {
    @Override
    protected final SimpleTaskResult<Result> doInBackground(Void... unused) {
        try {
            return new SimpleTaskResult<Result>(doInBackground());
        } catch (Exception e) {
            return new SimpleTaskResult<Result>(e);
        }
    }

    protected abstract Result doInBackground() throws Exception;
}
