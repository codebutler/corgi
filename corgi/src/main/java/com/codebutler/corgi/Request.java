package com.codebutler.corgi;

import java.io.InputStream;

public abstract class Request<T> {
    public abstract void fetch(RequestCallback<T> callback);

    public CachePath getCachePath() {
       return null;
    }

    public Response<T> readResponse(InputStream stream) throws Exception {
        return null;
    }
}
