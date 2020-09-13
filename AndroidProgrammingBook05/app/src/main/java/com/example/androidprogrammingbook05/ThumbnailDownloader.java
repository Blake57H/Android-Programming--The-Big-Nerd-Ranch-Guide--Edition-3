package com.example.androidprogrammingbook05;

import android.app.Activity;
import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "Thumbnailownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private static final int KEY_IMAGE_ID = 1;

    private boolean mHasQuit = false;
    private Handler mRequestHandler, mResponseHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    private LruCache<String, Bitmap> mLruCache;

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> thumbnailDownloadListener) {
        mThumbnailDownloadListener = thumbnailDownloadListener;
    }

    public ThumbnailDownloader(Handler responseHandler, int cacheSize) {
        super(TAG);
        mResponseHandler = responseHandler;
        mLruCache = new LruCache<String, Bitmap>(cacheSize);
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a URL: " + url);

        if (url == null) {
            mRequestMap.remove(target);
        } else {
            Bitmap bitmap = mLruCache.get(url);
            if (bitmap == null) {
                mRequestMap.put(target, url);
                mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
            } else {
                handleRequest(target, bitmap);
            }

        }
    }

    public void queueThumbnail(T target, String url, boolean cacheOnlySwitch){
        if(!cacheOnlySwitch) queueThumbnail(target, url);
        else{
            if(mLruCache.get(url) == null){

            }
        }
    }

    public Bitmap queueThumbnail(String url){
        if(url == null) return null;
        return mLruCache.get(url);
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    private void handleRequest(final T target) {
        try {
            final String url = mRequestMap.get(target);

            if (url == null) {
                return;
            }

            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created");

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target) != url || mHasQuit)
                        return;
                    mRequestMap.remove(target);
                    mLruCache.put(url, bitmap);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }

    private void handleRequest(final T target, final Bitmap cachedBitmap) {
        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
                mThumbnailDownloadListener.onThumbnailDownloaded(target, cachedBitmap);
            }
        });
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }
}
