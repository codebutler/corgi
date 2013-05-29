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

import android.text.TextUtils;

public abstract class CacheKey {
    public static String with(Object... parts) {
        String[] encodedParts = new String[parts.length];
        for (int i = 0; i < parts.length; i++) {
            if (parts[i] == null) {
                throw new IllegalArgumentException("Cache key cannot contain null components.");
            }
            encodedParts[i] = encodeFileName(parts[i].toString());
        }
        return TextUtils.join("__", encodedParts);
    }

    private static String encodeFileName(String name) {
        return name.replaceAll("[^a-z0-9_-]", "_");
    }
}
