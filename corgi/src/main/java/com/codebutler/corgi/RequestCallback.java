package com.codebutler.corgi;

public interface RequestCallback<T> {
    public void onExtraResponse(CachePath cachePath, Response response);
    public void onComplete(Response<T> response);
}
