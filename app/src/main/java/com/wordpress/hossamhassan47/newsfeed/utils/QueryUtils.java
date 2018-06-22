package com.wordpress.hossamhassan47.newsfeed.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.wordpress.hossamhassan47.newsfeed.R;
import com.wordpress.hossamhassan47.newsfeed.model.NewsItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class QueryUtils {
    private static final String LOG_TAG = QueryUtils.class.getName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(Context context, String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, context.getString(R.string.url_building_problem), e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(Context context, URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod(context.getString(R.string.request_method));
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(context, inputStream);
            } else {
                Log.e(LOG_TAG, context.getString(R.string.error_response_code) + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, context.getString(R.string.error_invalid_json_result), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(Context context, InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName(context.getString(R.string.charset_format)));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Query the guardian and return a list of {@link NewsItem} objects.
     */
    public static List<NewsItem> fetchNewsData(Context context, String requestUrl) {
        // Create URL object
        URL url = createUrl(context, requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(context, url);
        } catch (IOException e) {
            Log.e(LOG_TAG, context.getString(R.string.error_invalid_http_request), e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link NewsItem}s
        List<NewsItem> newsItems = extractResultFromJson(context, jsonResponse);

        // Return the list of {@link NewsItem}s
        return newsItems;
    }

    /**
     * Return a list of {@link NewsItem} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<NewsItem> extractResultFromJson(Context context, String newsJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding news to
        List<NewsItem> newsItems = new ArrayList<>();

        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(newsJSON);

            JSONArray newsArray = baseJsonResponse.getJSONObject(context.getString(R.string.json_object_response))
                    .getJSONArray(context.getString(R.string.json_array_results));

            // For each news item in the newsArray, create an {@link NewsItem} object
            for (int i = 0; i < newsArray.length(); i++) {

                NewsItem newsItem = new NewsItem();

                JSONObject currentNewsItem = newsArray.getJSONObject(i);

                // Main fields
                newsItem.setId(currentNewsItem.getString(context.getString(R.string.news_fields_id)));
                newsItem.setSectionName(currentNewsItem.getString(context.getString(R.string.news_fields_section_name)));
                newsItem.setWebPublicationDate(currentNewsItem.getString(context.getString(R.string.news_fields_date)));
                newsItem.setWebTitle(currentNewsItem.getString(context.getString(R.string.news_fields_web_title)));
                newsItem.setWebUrl(currentNewsItem.getString(context.getString(R.string.news_fields_web_url)));

                // Other fields (Thumbnail, Trial Text)
                JSONObject fields = currentNewsItem.getJSONObject(context.getString(R.string.json_object_fields));
                newsItem.setThumbnail(fields.getString(context.getString(R.string.news_fields_thumbnail)));
                newsItem.setTrailText(fields.getString(context.getString(R.string.news_fields_trail_text)));

                // Contributor tag
                JSONArray tags = currentNewsItem.getJSONArray(context.getString(R.string.json_array_tags));
                if (tags.length() > 0) {
                    JSONObject contributor = tags.getJSONObject(0);
                    newsItem.setContributor(contributor.getString(context.getString(R.string.news_fields_first_name)) + " "
                            + contributor.getString(context.getString(R.string.news_fields_last_name)));
                }

                newsItems.add(newsItem);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, context.getString(R.string.error_parse_json_result), e);
        }

        return newsItems;
    }
}
