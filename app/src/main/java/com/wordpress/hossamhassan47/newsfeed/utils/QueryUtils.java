package com.wordpress.hossamhassan47.newsfeed.utils;

import android.text.TextUtils;
import android.util.Log;

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
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
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
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the news JSON results.", e);
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
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
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
    public static List<NewsItem> fetchNewsData(String requestUrl) {

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link NewsItem}s
        List<NewsItem> newsItems = extractResultFromJson(jsonResponse);

        // Return the list of {@link NewsItem}s
        return newsItems;
    }

    /**
     * Return a list of {@link NewsItem} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<NewsItem> extractResultFromJson(String newsJSON) {
        Log.v(LOG_TAG, newsJSON);

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding news to
        List<NewsItem> newsItems = new ArrayList<>();

        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(newsJSON);

            JSONArray newsArray = baseJsonResponse.getJSONObject("response").getJSONArray("results");

            // For each news item in the newsArray, create an {@link NewsItem} object
            for (int i = 0; i < newsArray.length(); i++) {

                NewsItem newsItem = new NewsItem();

                JSONObject currentNewsItem = newsArray.getJSONObject(i);

                // Main fields
                newsItem.setId(currentNewsItem.getString("id"));
                newsItem.setType(currentNewsItem.getString("type"));
                newsItem.setSectionId(currentNewsItem.getString("sectionId"));
                newsItem.setSectionName(currentNewsItem.getString("sectionName"));
                newsItem.setWebPublicationDate(currentNewsItem.getString("webPublicationDate"));
                newsItem.setWebTitle(currentNewsItem.getString("webTitle"));
                newsItem.setWebUrl(currentNewsItem.getString("webUrl"));
                newsItem.setApiUrl(currentNewsItem.getString("apiUrl"));

                // Other fields (Thumbnail, Trial Text)
                JSONObject fields = currentNewsItem.getJSONObject("fields");
                newsItem.setThumbnail(fields.getString("thumbnail"));
                newsItem.setTrailText(fields.getString("trailText"));

                // Contributor tag
                JSONArray tags = currentNewsItem.getJSONArray("tags");
                if (tags.length() > 0) {
                    JSONObject contributor= tags.getJSONObject(0);
                    newsItem.setContributor(contributor.getString("firstName") + " " +contributor.getString("lastName"));
                }

                newsItems.add(newsItem);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the news JSON results", e);
        }

        return newsItems;
    }
}
