package com.wordpress.hossamhassan47.newsfeed.activities;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wordpress.hossamhassan47.newsfeed.R;
import com.wordpress.hossamhassan47.newsfeed.adapters.NewsAdapter;
import com.wordpress.hossamhassan47.newsfeed.loaders.NewsLoader;
import com.wordpress.hossamhassan47.newsfeed.model.NewsItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Activity that used to display news stories
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<List<NewsItem>> {

    // Current Section Name
    private String newsSection;
    private String orderBy;
    private String orderDate;

    // Loader ID
    private static final int NEWS_LOADER_ID = 1;

    // News Adapter
    private NewsAdapter mAdapter;

    // Empty Text View
    private TextView mEmptyStateTextView;

    // Progress Indicator
    private ProgressBar mLoadSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Navigation Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        // Load Preferences
        loadPreferenceValues();

        // Selected default section
        navigationView.getMenu().getItem(getDefaultSectionIndex()).setChecked(true);

        // Find a reference to the {@link ListView} in the layout
        ListView newsListView = (ListView) findViewById(R.id.list_view_news);

        // Create a new adapter that takes an empty list of earthquakes as input
        mAdapter = new NewsAdapter(this, new ArrayList<NewsItem>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        newsListView.setAdapter(mAdapter);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected news story.
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current news story that was clicked on
                NewsItem currentNewsItem = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri newsItemUri = Uri.parse(currentNewsItem.getWebUrl());

                // Create a new intent to view the news story URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsItemUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });

        // Set Empty view message
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        newsListView.setEmptyView(mEmptyStateTextView);

        // Get a reference to the Loading Indicator
        mLoadSpinner = findViewById(R.id.loading_indicator);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            getLoaderManager().initLoader(NEWS_LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    private void loadPreferenceValues(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        newsSection = sharedPrefs.getString(
                getString(R.string.key_default_section),
                getString(R.string.default_value_section));

        orderBy = sharedPrefs.getString(
                getString(R.string.key_order_by),
                getString(R.string.default_value_order_by));

        orderDate = sharedPrefs.getString(
                getString(R.string.key_order_date),
                getString(R.string.default_value_order_date));
    }

    private int getDefaultSectionIndex(){
        int index;
        switch (newsSection){
            case "world":
                index = 0;
                setTitle(R.string.section_world_news);
            break;
            case "politics":
                index = 1;
                setTitle(R.string.section_politics);
                break;
            case "commentisfree":
                index = 2;
                setTitle(R.string.section_opinions);
                break;
            case "lifeandstyle":
                index = 3;
                setTitle(R.string.section_life_and_style);
                break;
            case "football":
                index = 4;
                setTitle(R.string.section_football);
                break;
            case "tv-and-radio":
                index = 5;
                setTitle(R.string.section_tv_radio);
            default:
                index = 0;
                setTitle(R.string.section_world_news);
                break;
        }

        return index;
    }

    @Override
    public Loader<List<NewsItem>> onCreateLoader(int id, Bundle args) {

        // parse breaks apart the URI string that's passed into its parameter
        Uri baseUri = Uri.parse(getString(R.string.url_base));

        // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // Append query parameter and its value. For example, the `format=geojson`
        uriBuilder.appendQueryParameter(getString(R.string.url_filter_section), newsSection);
        uriBuilder.appendQueryParameter(getString(R.string.url_filter_order_by), orderBy);
        uriBuilder.appendQueryParameter(getString(R.string.url_filter_use_date), orderDate);

        Log.v("URL", uriBuilder.toString());
        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<NewsItem>> loader, List<NewsItem> data) {
        // Hide loading indicator
        mLoadSpinner.setVisibility(View.GONE);

        // Set empty state text to display "No news found."
        mEmptyStateTextView.setText(R.string.no_news);

        // Clear the adapter of previous news data
        mAdapter.clear();

        // If there is a valid list of {@link NewsItem}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (data != null && !data.isEmpty()) {
            mAdapter.addAll(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<NewsItem>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // Set Current section name & Title based on selected item
        if (id == R.id.nav_lifestyle) {
            newsSection = getString(R.string.section_life_and_style_value);
            setTitle(R.string.section_life_and_style);

        } else if (id == R.id.nav_football) {
            newsSection = getString(R.string.section_football_value);
            setTitle(R.string.section_football);

        } else if (id == R.id.nav_politics) {
            newsSection = getString(R.string.section_politics_value);
            setTitle(R.string.section_politics);

        } else if (id == R.id.nav_opinions) {
            newsSection = getString(R.string.section_opinions_value);
            setTitle(R.string.section_opinions);

        } else if (id == R.id.nav_world_news) {
            newsSection = getString(R.string.section_world_news_value);
            setTitle(R.string.section_world_news);

        } else if (id == R.id.nav_tv_radio) {
            newsSection = getString(R.string.section_tv_radio_value);
            setTitle(R.string.section_tv_radio);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        // Restart loader to display selected section news stories
        mLoadSpinner.setVisibility(View.VISIBLE);
        getLoaderManager().restartLoader(NEWS_LOADER_ID, null, this);

        return true;
    }

}
