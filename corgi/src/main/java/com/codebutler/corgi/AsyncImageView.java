package com.codebutler.corgi;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class AsyncImageView extends ImageView {
    private Callback               mListener;
    private Drawable               mDefaultDrawable;
    private BitmapRequest          mRequest;
    private BitmapRequest.Response mResponse;
    private boolean                mLoaded;

    private Bus mBus;

    public AsyncImageView(Context context) {
        this(context, null);
    }

    public AsyncImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AsyncImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mDefaultDrawable = getDefaultDrawable();
        setImageDrawable(mDefaultDrawable);
    }

    public void setBus(Bus bus) {
        mBus = bus;
    }

    public void setListener(Callback callback) {
        mListener = callback;
    }

    public boolean isLoaded() {
        return mLoaded;
    }

    public void loadRequest(BitmapRequest request, boolean drawGradient) {
        if (mRequest == null || (!mRequest.getUrl().equals(request.getUrl()))) {
            setImageDrawable(mDefaultDrawable);
            mLoaded = false;
            mRequest = request;
            mBus.post(request);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mBus.register(this);
        if (mRequest != null) {
            mBus.post(mRequest);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBus.unregister(this);
    }

    @Subscribe
    public void onBitmapResponse(BitmapRequest.Response response) {
        if (mRequest == null || (!response.getUrl().equals(mRequest.getUrl()))) {
            return;
        }
        loadResponse(response);
    }

    protected Drawable getDefaultDrawable() {
        return new ColorDrawable(Color.GRAY);
    }

    private void loadResponse(BitmapRequest.Response response) {
        if (mResponse != null && mResponse.getUrl().equals(response.getUrl())) {
            return;
        }

        mResponse = response;

        if (response.success()) {
            setImageDrawable(new BitmapDrawable(getContext().getResources(), response.getObject()));
        } else {
            setImageDrawable(mDefaultDrawable);
        }

        mLoaded = true;
        if (mListener != null) {
            mListener.onImageLoaded();
        }
    }

    public static interface Callback {
        public void onImageLoaded();
    }
}
