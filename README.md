Corgi
=====

A simple async request/response framework for Android built on [Otto](http://square.github.com/otto/).

Goals:

* Decouple async callbacks from the activity/fragment.
* Transparent memory/disk caching (using [DiskLruCache](https://github.com/JakeWharton/DiskLruCache).
* Automatic retry, even if app crashes (using [Tape](http://square.github.com/tape/)).
* Not HTTP specific, but works great with [Retrofit](https://github.com/square/retrofit).

Status
------

Code is kind of a mess, no tests yet, not all features implemented, no samples, lots to do.

Usage
-----

Create a class for each type of request:

```java
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


In your fragment:

```java
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

There are also a few helper classes:

* `GsonRequest`

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
