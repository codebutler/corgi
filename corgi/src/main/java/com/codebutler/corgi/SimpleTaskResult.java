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
