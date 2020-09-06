package com.example.androidprogrammingbook05;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";
    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private int mCurrentPage, mPages, mPhotoPerPage, mTotalPhotos;
    private int mRecyclerRow = 1;
    private static boolean mLoadSwitch = false;
    private GridLayoutManager mGridLayoutManager;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance() {

        Bundle args = new Bundle();

        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        beginFetcherTask();

        mThumbnailDownloader = new ThumbnailDownloader<>();
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mPhotoRecyclerView = v.findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                DisplayMetrics dm = new DisplayMetrics();

                getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
                float widthDP = dm.widthPixels / dm.density;
                mRecyclerRow = Math.round(widthDP / 150);
                if (mRecyclerRow == 0) mRecyclerRow = 1;
                //mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), mRecyclerRow));
                mGridLayoutManager.setSpanCount(mRecyclerRow);
                Log.d(TAG, "Span Count: " + mRecyclerRow);
            }
        });
        mGridLayoutManager = new GridLayoutManager(getActivity(), 3);
        mPhotoRecyclerView.setLayoutManager(mGridLayoutManager);
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (mLoadSwitch) {
                    Log.i(TAG, "Content load is already running");
                    return;
                }
                if (!recyclerView.canScrollVertically(1) && mItems.size() > 0) {
                    if (mCurrentPage == mPages) return;
                    if (mItems.size() == mTotalPhotos) return;
                    beginFetcherTask(mCurrentPage + 1);
                }
            }
        });

        setupAdapter();
        return v;
    }


    private void beginFetcherTask() {
        beginFetcherTask(null);
    }

    private void beginFetcherTask(@Nullable Integer targetPage) {
        if (!mLoadSwitch) {
            mLoadSwitch = true;
            if (targetPage == null)
                new FetchItemTask().execute();
            else
                new FetchItemTask().execute(targetPage);
            mLoadSwitch = false;
        } else {
            Log.i(TAG, "Content load is already running");
        }

    }

    private void setupAdapter() {
        mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
    }

    private void setupAdapter(boolean noNewAdapter) {
        if (!noNewAdapter)
            mPhotoRecyclerView.getAdapter().notifyDataSetChanged();
    }

    private class FetchItemTask extends AsyncTask<Integer, Void, GalleryItemRaw> {

        @Override
        protected GalleryItemRaw doInBackground(Integer... integers) {
            if (mItems.size() == 0)
                return new FlickrFetchr().fetchItems();
            else {
                return new FlickrFetchr().fetchItems(integers[0]);
            }
//            return new FlickrFetchr().fetchItems();
        }

        @Override
        protected void onPostExecute(GalleryItemRaw items) {
            if (items == null) {
                Log.e(TAG, "onPostExecute.items is null");
                return;
            }
            if (mCurrentPage >= items.mPhotos.mPage) {
                Log.i(TAG, "Page " + mCurrentPage + " has already added. RecyclerViiew won't update.");
                return;
            }
            mItems.addAll(items.mPhotos.mItems);
            mCurrentPage = items.mPhotos.mPage;
            mPages = items.mPhotos.mPages;
            mPhotoPerPage = items.mPhotos.mPerPage;
            mTotalPhotos = items.mPhotos.mTotal;
            setupAdapter(false);
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        //private TextView mTitleTextView;
        private ImageView mItemImageView;

        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            mItemImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
        }

        /*
                public void bindGalleryItem(GalleryItem item) {
                    mTitleTextView.setText(item.toString());
                }
        */
        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //TextView textView = new TextView(getActivity());
            //return new PhotoHolder(textView);
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view= inflater.inflate(R.layout.list_item_gallery, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            //holder.bindGalleryItem(galleryItem);
            Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
            holder.bindDrawable(placeholder);
            mThumbnailDownloader.queueThumbnail(holder, galleryItem.getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }
}
