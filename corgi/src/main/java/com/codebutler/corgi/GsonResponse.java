package com.codebutler.corgi;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public abstract class GsonResponse<T> extends Response<T> {
    protected GsonResponse(T object) {
        super(object);
    }

    protected GsonResponse(Exception error) {
        super(error);
    }

    @Override
    public final void write(OutputStream stream) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(stream);
        new Gson().toJson(this, writer);
        writer.flush();
    }
}
