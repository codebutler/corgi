package com.codebutler.corgi;

import com.google.common.base.Joiner;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class CachePath {
    private final String[] mParts;

    public CachePath(Object... parts) {
        mParts = new String[parts.length];
        for (int i = 0; i < parts.length; i++) {
            mParts[i] = encodeFileName(parts[i].toString());
        }
    }

    public String[] getParts() {
        return mParts;
    }

    @Override
    public String toString() {
        return Joiner.on("__").join(mParts);
    }

    private static String encodeFileName(String name) {
        try {
            return URLEncoder.encode(name, "UTF-8").replace("*", "").replace(".", "").replace("?", "");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
