package com.codebutler.corgi;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class GsonRequest<T> extends Request<T> {
    @Override
    public Response<T> readResponse(InputStream stream) throws IOException {
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(stream);
            return new Gson().<Response<T>>fromJson(isr, getResponseClass());
        } finally {
            if (isr != null) {
                isr.close();
            }
        }
    }
    public abstract Class<? extends GsonResponse<T>> getResponseClass();
}
