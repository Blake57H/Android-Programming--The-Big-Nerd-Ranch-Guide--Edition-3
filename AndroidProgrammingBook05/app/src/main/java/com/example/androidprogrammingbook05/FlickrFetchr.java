package com.example.androidprogrammingbook05;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


// test site: http://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=783bed4a14213250ba97950f585674d5&format=json&nojsoncallback=1
public class FlickrFetchr {
    private static final String TAG = "FlickrFetchr";
    private static final String API_KEY = "783bed4a14213250ba97950f585674d5";
    private static final String FETCH_RECENT_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT =
            Uri.parse("https://api.flickr.com/services/rest/?").buildUpon()
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s").build();

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public GalleryItemRaw fetchRecentPhoto(Integer page) {
        String url = buildUrl(FETCH_RECENT_METHOD, null, page);
        return downloadGalleryItems(url);
    }


    public GalleryItemRaw searchPhotos(String query, Integer page) {
        String url = buildUrl(SEARCH_METHOD, query, page);
        return downloadGalleryItems(url);
    }

    private String buildUrl(String method, String query, Integer page) {
        Uri.Builder builder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", method);
        if (method.equals(SEARCH_METHOD)) {
            builder.appendQueryParameter("text", query);
        }
        if (page != null)
            builder.appendQueryParameter("page", Integer.toString(page));
        return builder.build().toString();
    }

    public GalleryItemRaw downloadGalleryItems(String url) {
        GalleryItemRaw items = new GalleryItemRaw();
        try {
            String jsonString = getUrlString(url);
            //parseItems(items, new JSONObject(jsonString)); //parse manually with JsonObject and Json Array
            //parseItems(items, jsonString); // parse using gson
            items = parseItems(jsonString);
            Log.i(TAG, "Received JSON: " + jsonString);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JsonIOException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }

        return items;
    }

    @Deprecated
    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");
        for (int i = 0; i < photoJsonArray.length(); i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setID(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));

            if (!photoJsonObject.has("url_s")) continue;

            item.setUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }
    }

    private GalleryItemRaw parseItems(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, GalleryItemRaw.class);
    }
}



