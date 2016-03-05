package com.tragicfruit.twitcast;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Jeremy on 24/02/2016.
 */
public class TWiTFetcher {
    private static final String TAG = "TWiTFetcher";
    private static final Uri ENDPOINT = Uri.parse("https://twit.tv/api/v1.0");

    private TWiTDatabase mDatabase;
    private Context mContext;

    public TWiTFetcher(Context context) {
        mContext = context;
        mDatabase = TWiTDatabase.get();
    }

    public String readRssFeed(String urlSpec) throws IOException {
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
            return new String(out.toByteArray());
        } finally {
            connection.disconnect();
        }
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
            newShow.setCoverArtSmallUrl(derivatives.getString("twit_album_art_300x300"));
            newShow.setCoverArtUrl(derivatives.getString("twit_album_art_600x600"));
            newShow.setCoverArtLargeUrl(derivatives.getString("twit_album_art_1400x1400"));

            JSONArray hdVideoArray = show.getJSONArray("hdVideoSubscriptionOptions");
            newShow.setVideoHdFeed(getRSSFeedFromJSONArray(hdVideoArray));

            JSONArray sdVideoLarge = show.getJSONArray("sdVideoLargeSubscriptionOptions");
            newShow.setVideoLargeFeed(getRSSFeedFromJSONArray(sdVideoLarge));

            JSONArray sdVideoSmall = show.getJSONArray("sdVideoSmallSubscriptionOptions");
            newShow.setVideoSmallFeed(getRSSFeedFromJSONArray(sdVideoSmall));

            JSONArray audio = show.getJSONArray("audioSubscriptionOptions");
            newShow.setAudioFeed(getRSSFeedFromJSONArray(audio));

            showList.add(newShow);
        }

        return showList;
    }

    private String getRSSFeedFromJSONArray(JSONArray subscriptionOptions) {
        for (int i = subscriptionOptions.length() - 1; i >= 0; i--) {
            try {
                JSONObject option = subscriptionOptions.getJSONObject(i);
                JSONObject feedProvider = option.getJSONObject("feedProvider");
                String label = feedProvider.getString("label");

                if (label.equals("RSS")) {
                    return option.getString("url");
                }
            } catch (JSONException je) {
                // Continue to next iteration
            }
        }

        return null;
    }

    public List<Episode> fetchAllEpisodes() {
        return fetchEpisodes(null);
    }

    public List<Episode> fetchEpisodes(Show show) {
        List<Episode> episodeList;
        if (show == null) {
            episodeList = getEpisodeListFromFeed(Constants.BRICKHOUSE_AUDIO_FEED);
            addVideoFeed(episodeList, Constants.BRICKHOUSE_VIDEO_SMALL_FEED, Feed.VIDEO_SMALL);
            addVideoFeed(episodeList, Constants.BRICKHOUSE_VIDEO_LARGE_FEED, Feed.VIDEO_LARGE);
            addVideoFeed(episodeList, Constants.BRICKHOUSE_VIDEO_HD_FEED, Feed.VIDEO_HD);
        } else {
            episodeList = getEpisodeListFromFeed(show.getAudioFeed());
            addVideoFeed(episodeList, show.getVideoSmallFeed(), Feed.VIDEO_SMALL);
            addVideoFeed(episodeList, show.getVideoLargeFeed(), Feed.VIDEO_LARGE);
            addVideoFeed(episodeList, show.getVideoHdFeed(), Feed.VIDEO_HD);
        }

        Log.d(TAG, "Fetched video feeds");

        return episodeList;
    }

    private void addVideoFeed(List<Episode> episodeList, String feedUrl, Feed feedType) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            String feed = readRssFeed(feedUrl);
            Document document = builder.parse(new InputSource(new StringReader(feed)));

            NodeList episodeNodeList = document.getElementsByTagName("item");
            int episodeListStartingIndex = findFirstMatchingIndex(episodeList, (Element) episodeNodeList.item(0));

            // i is index for video feed, j is index for episode list (built from audio feed)
            for (int i = 0, j = episodeListStartingIndex; j < episodeList.size(); i++, j++) {
                Episode episode = episodeList.get(j);

                Element episodeElement = (Element) episodeNodeList.item(i);
                String link = episodeElement.getElementsByTagName("link").item(0).getTextContent();

                switch (feedType) {
                    case VIDEO_SMALL:
                        episode.setVideoSmallUrl(link);
                        break;
                    case VIDEO_LARGE:
                        episode.setVideoLargeUrl(link);
                        break;
                    case VIDEO_HD:
                        episode.setVideoHdUrl(link);
                        break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Cannot add video feed: " + feedUrl, e);
        }
    }

    private int findFirstMatchingIndex(List<Episode> episodeList, Element firstXmlItem) {
        String firstXmlItemTitle = firstXmlItem.getElementsByTagName("title").item(0).getTextContent();

        for (int i = 0; i < episodeList.size(); i++) {
            String episodeTitle = episodeList.get(i).getTitle();
            if (firstXmlItemTitle.equals(episodeTitle)) {
                return i;
            }
        }

        return -1;
    }

    private List<Episode> getEpisodeListFromFeed(String feedUrl) {
        try {
            List<Episode> episodeList = new ArrayList<>();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            String feed = readRssFeed(feedUrl);
            Document document = builder.parse(new InputSource(new StringReader(feed)));

            NodeList episodeNodeList = document.getElementsByTagName("item");
            Log.d(TAG, "Raw episode count: " + episodeNodeList.getLength());
            for (int i = 0; i < episodeNodeList.getLength(); i++) {
                Episode episode = new Episode();

                Element episodeElement = (Element) episodeNodeList.item(i);
                String title = episodeElement.getElementsByTagName("title").item(0).getTextContent();
                episode.setTitle(title);

                String pubDate = episodeElement.getElementsByTagName("pubDate").item(0).getTextContent();
                episode.setPublicationDate(parseDate(pubDate));

                String subtitle = episodeElement.getElementsByTagName("itunes:subtitle").item(0).getTextContent();
                episode.setSubtitle(subtitle);

                String showNotes = episodeElement.getElementsByTagName("itunes:summary").item(0).getTextContent();
                episode.setShowNotes(showNotes);

                String duration = episodeElement.getElementsByTagName("itunes:duration").item(0).getTextContent();
                episode.setRunningTime(parseRunningTime(duration));

                String audioLink = episodeElement.getElementsByTagName("link").item(0).getTextContent();
                episode.setVideoAudioUrl(audioLink);

                episodeList.add(episode);
            }

            Log.d(TAG, "Fetched audio feeds");

            return episodeList;

        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch episodes", e);
            return null;
        }
    }

    private Date parseDate(String dateString) {
        try {
//            TimeZone timeZone = TimeZone.getTimeZone("GMT");
//            Calendar calendar = Calendar.getInstance(timeZone);
            GregorianCalendar calendar = new GregorianCalendar();
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy k:m:s ZZZ");

            dateFormat.setCalendar(calendar);
            calendar.setTime(dateFormat.parse(dateString));

            return calendar.getTime();
        } catch (ParseException pe) {
            Log.e(TAG, "Cannot parse episode airing date", pe);
            return null;
        }
    }

    private String parseRunningTime(String runningTime) {
        int hours = Integer.parseInt(runningTime.substring(0, 1));
        int minutes = Integer.parseInt(runningTime.substring(2, 4));
        int seconds = Integer.parseInt(runningTime.substring(5, 7));

//        int hoursToMinutes = hours * 60;
        int secondsToMinutes = (int) Math.round((double) seconds / 60);
        minutes += secondsToMinutes;

        if (hours > 0) {
            return mContext.getString(R.string.episode_running_time_long, hours, minutes);
        } else {
            return mContext.getString(R.string.episode_running_time_short, minutes);
        }
    }
}
