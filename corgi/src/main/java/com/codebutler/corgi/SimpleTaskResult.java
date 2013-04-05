package com.codebutler.corgi;

public class SimpleTaskResult<T> {
    private final T mObject;
    private final Exception mException;

    public SimpleTaskResult(T object) {
        mObject    = object;
        mException = null;
    }

    public SimpleTaskResult(Exception exception) {
        if (exception == null) {
            throw new IllegalArgumentException("exception may not be null");
        }

        mException = exception;
        mObject    = null;
    }

    public T getObject() {
        return mObject;
    }

    public Exception getException() {
        return mException;
    }

    public boolean success() {
        return (mException == null);
    }
}
