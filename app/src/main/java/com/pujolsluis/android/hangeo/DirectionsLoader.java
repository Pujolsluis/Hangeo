package com.pujolsluis.android.hangeo;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Created by Luis on 1/9/2017.
 */

public class DirectionsLoader extends AsyncTaskLoader<List<Route>> {

    /** Tag for log messages */
    private static final String LOG_TAG = DirectionsLoader.class.getName();

    /** Query URL */
    private String mUrl;

    /**
     * Constructs a new {@link DirectionsLoader}.
     *
     * @param context of the activity
     * @param url to load data from
     */
    public DirectionsLoader(Context context, String url) {
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
    public List<Route> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of earthquakes.
        List<Route> routeList = DirectionsApiUtils.fetchRouteData(mUrl);
        return routeList;
    }
}
