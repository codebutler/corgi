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

public class RequestResponse implements Comparable<RequestResponse> {
    private final String mCacheKey;
    private final Request mRequest;
    private final Response mResponse;

    public RequestResponse(Request request, Response response) {
        mRequest  = request;
        mResponse = response;
        mCacheKey = null;
    }

    public RequestResponse(String cacheKey, Response response) {
        mCacheKey = cacheKey;
        mResponse = response;
        mRequest  = null;
    }

    public Request getRequest() {
        return mRequest;
    }

    public Response getResponse() {
        return mResponse;
    }

    public String getCacheKey() {
        return mCacheKey;
    }

    @Override
    public int compareTo(RequestResponse another) {
        return 0;
    }
}
