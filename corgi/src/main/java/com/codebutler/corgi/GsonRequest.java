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
