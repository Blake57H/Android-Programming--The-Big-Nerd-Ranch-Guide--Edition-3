package com.example.androidprogrammingbook05;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends VisibleFragment {
    private static final String TAG = "PhotoGalleryFragment";
    private static final String KEY_SUBTITLE = "subtitle";

    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private int mCurrentPage, mPages, mPhotoPerPage, mTotalPhotos;
    private int mRecyclerRow = 1, mLastVisibleItem, mFirstVisibleItem;
    private static boolean mLoadSwitch = false;
    private GridLayoutManager mGridLayoutManager;
    private ThumbnailDownloader<Integer> mThumbnailDownloader;
    private List<Integer> mBindDone = new ArrayList<>();
    private String mSubtitle;
    private ProgressDialog mProgressDialog;

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
        setHasOptionsMenu(true);
        updateItems();

        //Intent i = PollService.newIntent(getActivity());
        //getActivity().startService(i);
        //PollService.setServiceAlarm(getActivity(), true);

        Handler responseHandler = new Handler();

        ActivityManager am = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        int cacheSize = am.getMemoryClass() * 1024 * 1024 / 8;

        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler, cacheSize);
        mThumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<Integer>() {
            @Override
            public void onThumbnailDownloaded(Integer target, Bitmap thumbnail) {
                mPhotoRecyclerView.getAdapter().notifyItemChanged(target);

            }
        });
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
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
                //mPhotoRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        mGridLayoutManager = new GridLayoutManager(getActivity(), 3);
        mPhotoRecyclerView.setLayoutManager(mGridLayoutManager);
        setupOnScrollListener();
        setupAdapter();

        updateSubtitle(QueryPreferences.getStoredQuery(getActivity()));

        return v;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);
        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "Query text submit: " + query);
                QueryPreferences.setStoredQuery(getActivity(), query);
                updateItems();
                resetRecyclerViewItem();

                searchView.onActionViewCollapsed();
                getActivity().invalidateOptionsMenu();
                updateSubtitle(getString(R.string.search_subtitle, query));

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "Query text changed: " + newText);
                return false;
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (Build.VERSION_CODES.LOLLIPOP > Build.VERSION.SDK_INT) {
            if (PollService.isServiceAlarmOn(getActivity())) {
                toggleItem.setTitle(R.string.stop_polling);
            }
        } else if (!PollService_JobScheduler.isSchedulerExist(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                resetRecyclerViewItem();
                updateItems();

                getActivity().invalidateOptionsMenu();
                updateSubtitle(null);

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getRootView().getWindowToken(), 0);

                return true;
            case R.id.menu_item_toggle_polling:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                    PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                } else {
                    PollService_JobScheduler.setJobScheduler(getActivity(), PollService_JobScheduler.isSchedulerExist(getActivity()));
                }
                getActivity().invalidateOptionsMenu();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle(String queryText) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        mSubtitle = queryText;
        activity.getSupportActionBar().setSubtitle(queryText);
    }


    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        Log.d(TAG, "query: " + query);
        beginFetcherTask(null, query);
    }

    private void beginFetcherTask(@Nullable Integer targetPage, String queryText) {
        if (!mLoadSwitch) {
            mLoadSwitch = true;
            showProgressDialog(queryText);
            if (targetPage == null) {
                new FetchItemTask(queryText).execute();
            } else
                new FetchItemTask(queryText).execute(targetPage);
            mLoadSwitch = false;
        } else {
            Log.i(TAG, "Content load is already running");
        }

    }

    private void showProgressDialog(String searchText) {
        mProgressDialog = new ProgressDialog(getActivity());
        if (searchText == null) {
            mProgressDialog.setTitle(getString(R.string.progress_dialog_title, getString(R.string.progress_dialog_title_recent_photo)));
        } else {
            mProgressDialog.setTitle(getString(R.string.progress_dialog_title, searchText));
        }
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private boolean searchBindListItem(int value) {
        for (int item : mBindDone
        ) {
            if (item == value) return true;
        }
        return false;
    }

    private void removeBindListItem(int value) {
        for (int counter = 0; counter < mBindDone.size(); counter++) {
            if (mBindDone.get(counter) == value) {
                mBindDone.remove(counter);
                return;
            }
        }
    }

    private void resetRecyclerViewItem() {
        mThumbnailDownloader.clearQueue();
        mCurrentPage = 0;
        mPhotoRecyclerView.clearOnScrollListeners();
        mItems.clear();
        mBindDone.clear();
        setupAdapter(true);
    }

    private void setupOnScrollListener() {
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int lastVisibleItem = mGridLayoutManager.findLastVisibleItemPosition();
                int firstVisibleItem = mGridLayoutManager.findFirstVisibleItemPosition();

                Log.d(TAG, "Showing item " + firstVisibleItem + " to " + lastVisibleItem);
                int begin = mFirstVisibleItem <= firstVisibleItem ? mLastVisibleItem : Math.max(firstVisibleItem - 10, 0);
                int end = mLastVisibleItem <= lastVisibleItem ? Math.min(lastVisibleItem + 10, mItems.size() - 1) : mFirstVisibleItem;
                for (int position = begin; position <= end; position++) {
                    if (searchBindListItem(position)) {
                        Log.i(TAG, "Item in cache, not requesting to bind");
                        continue;
                    }
                    String url = mItems.get(position).getUrl();
                    mThumbnailDownloader.queueThumbnail(position, url);
                    //mThumbnailDownloader.queueThumbnail((PhotoHolder) recyclerView.findViewHolderForLayoutPosition(position),url);

                }

                if (end == mItems.size() - 1 && mItems.size() > 0) {
                    if (mCurrentPage < mPages)
                        beginFetcherTask(mCurrentPage + 1, QueryPreferences.getStoredQuery(getActivity()));
                }
            }
        });
    }

    private void setupAdapter() {
        mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
    }

    private void setupAdapter(boolean noNewAdapter) {
        if (noNewAdapter)
            mPhotoRecyclerView.getAdapter().notifyDataSetChanged();
    }

    private class FetchItemTask extends AsyncTask<Integer, Void, GalleryItemRaw> {
        private String mQuery;

        public FetchItemTask(String query) {
            mQuery = query;
        }

        @Override
        protected GalleryItemRaw doInBackground(Integer... integers) {
            if (mQuery == null) {
                if (integers.length == 0)
                    return new FlickrFetchr().fetchRecentPhoto(null);
                else
                    return new FlickrFetchr().fetchRecentPhoto(integers[0]);
            } else {
                if (integers.length == 0)
                    return new FlickrFetchr().searchPhotos(mQuery, null);
                else
                    return new FlickrFetchr().searchPhotos(mQuery, integers[0]);

            }

//            return new FlickrFetchr().fetchItems();
        }

        @Override
        protected void onPostExecute(GalleryItemRaw items) throws NullPointerException {
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
            mProgressDialog.dismiss();


            setupAdapter(true);
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //private TextView mTitleTextView;
        private ImageView mItemImageView;
        private GalleryItem mGalleryItem;

        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            mItemImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
            itemView.setOnClickListener(this);
        }

        /*
                public void bindGalleryItem(GalleryItem item) {
                    mTitleTextView.setText(item.toString());
                }
        */
        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }

        public void bindGalleryItem(GalleryItem galleryItem){
            mGalleryItem = galleryItem;
        }

        @Override
        public void onClick(View view) {
            //Intent i = new Intent(Intent.ACTION_VIEW, mGalleryItem.getPhotoPageUri());
            Intent i = PhotoPageActivity.newIntent(getActivity(), mGalleryItem.getPhotoPageUri());
            startActivity(i);
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
            View view = inflater.inflate(R.layout.list_item_gallery, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            //holder.bindGalleryItem(galleryItem);
            holder.bindGalleryItem(galleryItem);

            Bitmap bitmap = mThumbnailDownloader.queueThumbnail(mItems.get(position).getUrl());
            if (bitmap == null) {
                removeBindListItem(position);
                Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
                holder.bindDrawable(placeholder);
                mThumbnailDownloader.queueThumbnail(position, galleryItem.getUrl());
            } else {
                holder.bindDrawable(new BitmapDrawable(getResources(), bitmap));
            }
            mBindDone.add(position);
            //mThumbnailDownloader.queueThumbnail(holder, galleryItem.getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }
}
