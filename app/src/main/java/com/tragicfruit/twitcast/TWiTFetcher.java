package com.tragicfruit.twitcast;

import android.net.Uri;
import android.util.Log;

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

/**
 * Created by Jeremy on 24/02/2016.
 */
public class TWiTFetcher {
    private static final String TAG = "TWiTFetcher";
    private static final Uri ENDPOINT = Uri.parse("https://twit.tv/api/v1.0");

    private TWiTDatabase mDatabase;

    public TWiTFetcher() {
        mDatabase = TWiTDatabase.get();
    }

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("app-id", SecretConstants.TWIT_API_ID);
        connection.setRequestProperty("app-key", SecretConstants.TWIT_API_KEY);

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

    public List<Show> fetchShows() {
        try {
            Uri uri = ENDPOINT.buildUpon()
                    .appendPath("shows")
                    .appendQueryParameter("shows_active", "1")
                    .build();

            String jsonBody = getUrlString(uri.toString());
            JSONObject jsonObject = new JSONObject(jsonBody);
            return parseShows(jsonObject);

        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
            return null;
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch shows", ioe);
            return null;
        }
    }

    private List<Show> parseShows(JSONObject json) throws JSONException {
        List<Show> showList = new ArrayList<>();

        JSONArray shows = json.getJSONArray("shows");
        for (int i = 0; i < shows.length(); i++) {
            JSONObject show = shows.getJSONObject(i);

            Show newShow = new Show();
            newShow.setId(show.getInt("id"));
            newShow.setTitle(show.getString("label"));
            newShow.setCleanPath(show.getString("cleanPath"));

            JSONObject coverArt = show.getJSONObject("coverArt");
            JSONObject derivatives = coverArt.getJSONObject("derivatives");
            newShow.setCoverArtUrl(derivatives.getString("twit_album_art_600x600"));

            showList.add(newShow);
        }

        return showList;
    }

    public void fetchEpisodes(String url) {
        try {
            if (url == null) {
                 url = ENDPOINT.buildUpon()
                        .appendPath("episodes")
                        .build()
                        .toString();
            }

            String jsonBody = getUrlString(url);
            JSONObject jsonObject = new JSONObject(jsonBody);
            parseEpisodes(jsonObject);

        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch episodes", ioe);
        }
    }

    private void parseEpisodes(JSONObject json) throws JSONException {
        JSONArray episodes = json.getJSONArray("episodes");

        for (int i = 0; i < episodes.length(); i++) {
            JSONObject episodeJson = episodes.getJSONObject(i);

            Episode episode = new Episode();
            episode.setTitle(episodeJson.getString("label"));
            episode.setCleanPath(episodeJson.getString("cleanPath"));

            mDatabase.addEpisode(episode);
        }

        String nextPageUrl = json.getJSONObject("_links").getJSONObject("next").getString("href");
        if (mDatabase.getEpisodeCount() < 50) {
            fetchEpisodes(nextPageUrl);
        } else {
            Log.d(TAG, "Episode count: " + mDatabase.getEpisodeCount());
        }
    }
}
