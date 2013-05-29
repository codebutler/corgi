Corgi
=====

A simple async request/response framework for Android.

Goals:

* Decouple async callbacks from the activity/fragment using [Otto](http://square.github.com/otto/).
* Transparent memory caching using [LruCache](http://developer.android.com/reference/android/support/v4/util/LruCache.html) and disk caching using [DiskLruCache](https://github.com/JakeWharton/DiskLruCache).
* Automatic retry, even if app crashes using [Tape](http://square.github.com/tape/) (* not yet implemented).
* Not HTTP specific, but work great with [Retrofit](https://github.com/square/retrofit).

Status
------

Code is kind of a mess, no tests yet, not all features implemented, no samples, lots to do.

Usage
-----

Initialize corgi (in your Application subclass):

```
mCorgi = new Corgi(this, new Corgi.Listener() {
    @Override
    public void onResponse(Response response) {
        // Handle response
    }
});
mCorgi.start();
```

Create a class for each type of request:

```
public class MyRequest extends Request<MyObj> {
    @Override
    public void fetch(final RequestCallback<MyObj> callback) {
        try {
            // do async task, get obj
            callback.onComplete(new Response(obj));
        } catch (Exception ex) {
            callback.onComplete(new Response(ex));
        }
    }
    
    public static class Response extends com.codebutler.corgi.Response<MyObj> {
        protected Response(String id, Post post) {
            super(post);
            mId = id;
        }
        public Response(String id, Exception error) {
            super(error);
            mId = id;
        }
    }
}
```

Tell corgi to go fetch!

```
mCorgi.fetch(new MyRequest());
```

There are also a few helper classes:

* `GsonRequest`/`GsonResponse` - Automatically handles caching of GSON objects.


Usage with Otto
---------------

Corgi is really intended to be used with Otto.

Pass responses from corgi onto otto:

```
return new Corgi(mAppContext, new Corgi.Listener() {
    @Override
    public void onResponse(Response response) {
        bus.post(response);
    }
});
```

Pass any requests from Otto onto Corgi (put this your Application subclass):

```
@Subscribe
public void onRequest(Request request) {
    mCorgi.fetch(request);
}
```

Then in your fragments:

```
@Override
public void onResume() {
    super.onResume();
    mBus.register(this);
    
    mBus.post(new MyRequest());
}

@Subscribe
public void onMyResponse(MyRequest.Response response) {
    if (!response.success()) {
        // Show error
        return;
    }
    MyObj obj = response.getObject();
    // Update UI
}
```

License
=======

    Copyright 2013 Eric Butler

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
