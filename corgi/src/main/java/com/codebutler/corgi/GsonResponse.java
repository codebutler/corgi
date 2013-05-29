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
