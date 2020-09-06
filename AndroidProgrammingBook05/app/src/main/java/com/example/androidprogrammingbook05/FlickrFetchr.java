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

    public GalleryItemRaw fetchItems() {
        GalleryItemRaw items = new GalleryItemRaw();
        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/?")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .build().toString();
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

    public GalleryItemRaw fetchItems(int targetPage) throws JsonIOException{
        GalleryItemRaw items = new GalleryItemRaw();
        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/?")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("page", Integer.toString(targetPage))
                    .appendQueryParameter("extras", "url_s")
                    .build().toString();
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



