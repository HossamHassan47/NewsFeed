package com.wordpress.hossamhassan47.newsfeed.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.wordpress.hossamhassan47.newsfeed.model.NewsItem;
import com.wordpress.hossamhassan47.newsfeed.utils.QueryUtils;

import java.util.List;

public class NewsLoader extends AsyncTaskLoader<List<NewsItem>> {

    /**
     * Query URL
     */
    private String mUrl;

    /**
     * Constructs a new {@link NewsLoader}.
     *
     * @param context of the activity
     * @param url     to load data from
     */
    public NewsLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<NewsItem> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of news items.
        return QueryUtils.fetchNewsData(getContext(), mUrl);
    }
}
