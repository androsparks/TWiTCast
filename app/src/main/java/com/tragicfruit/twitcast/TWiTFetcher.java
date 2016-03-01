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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Jeremy on 24/02/2016.
 */
public class TWiTFetcher {
    private static final String TAG = "TWiTFetcher";
    private static final Uri ENDPOINT = Uri.parse("https://twit.tv/api/v1.0");
    private static final long WEEK_IN_SECONDS = 60 * 60 * 24 * 7;

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

            if (mDatabase.isExcludedShow(newShow)) {
                continue;
            }

            newShow.setTitle(show.getString("label"));
            newShow.setDescription(show.getString("tagLine"));

            JSONObject coverArt = show.getJSONObject("coverArt");
            JSONObject derivatives = coverArt.getJSONObject("derivatives");
            newShow.setCoverArtUrl(derivatives.getString("twit_album_art_600x600"));

//            Log.d(TAG, newShow.getTitle() + ": " + newShow.getId());

            showList.add(newShow);
        }

        return showList;
    }

    public void fetchEpisodes(String url) {
        try {
            if (url == null) {
                Date currentDate = new Date();

                long timeNow = currentDate.getTime() / 1000;
                long oneWeekAgo = timeNow - WEEK_IN_SECONDS;

                url = ENDPOINT.buildUpon()
                        .appendPath("episodes")
                        .appendQueryParameter("filter[airingDate][value]", String.valueOf(oneWeekAgo))
                        .appendQueryParameter("filter[airingDate][operator]", ">=")
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
            episode.setEpisodeNumber(episodeJson.getInt("episodeNumber"));
            episode.setAiringDate(parseDate(episodeJson.getString("airingDate")));
            episode.setShowNotes(episodeJson.getString("showNotes"));

            if (episodeJson.has("video_hd")) {
                episode.setVideoHdUrl(episodeJson.getJSONObject("video_hd").getString("mediaUrl"));
            }

            if (episodeJson.has("video_large")) {
                episode.setVideoLargeUrl(episodeJson.getJSONObject("video_large").getString("mediaUrl"));
            }

            if (episodeJson.has("video_small")) {
                episode.setVideoSmallUrl(episodeJson.getJSONObject("video_small").getString("mediaUrl"));
            }

            if (episodeJson.has("video_audio")) {
                episode.setVideoAudioUrl(episodeJson.getJSONObject("video_audio").getString("mediaUrl"));
                String runningTime = episodeJson.getJSONObject("video_audio").getString("runningTime");
                episode.setRunningTimeInMinutes(parseRunningTime(runningTime));
            }

            JSONObject embedded = episodeJson.getJSONObject("_embedded");
            JSONArray shows = embedded.getJSONArray("shows");
            JSONObject show = shows.getJSONObject(0);
            episode.setShowId(show.getInt("id"));

            mDatabase.addEpisode(episode);
        }

        try {
            String nextPageUrl = json.getJSONObject("_links").getJSONObject("next").getString("href");
            fetchEpisodes(nextPageUrl);
        } catch (JSONException joe) {
            Log.d(TAG, "Episode count: " + mDatabase.getEpisodeCount());
        }
    }

    private Date parseDate(String dateString) {
        try {
            TimeZone timeZone = TimeZone.getTimeZone("GMT");
            Calendar calendar = Calendar.getInstance(timeZone);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

            dateFormat.setCalendar(calendar);
            calendar.setTime(dateFormat.parse(dateString));
            return calendar.getTime();
        } catch (ParseException pe) {
            Log.e(TAG, "Cannot parse episode airing date", pe);
            return null;
        }
    }

    private int parseRunningTime(String runningTime) {
        int hours = Integer.parseInt(runningTime.substring(0, 2));
        int minutes = Integer.parseInt(runningTime.substring(3, 5));
        int seconds = Integer.parseInt(runningTime.substring(6, 8));

        int hoursToMinutes = hours * 60;
        int secondsToMinutes = (int) Math.round((double) seconds / 60);

        return hoursToMinutes + minutes + secondsToMinutes;
    }
}
