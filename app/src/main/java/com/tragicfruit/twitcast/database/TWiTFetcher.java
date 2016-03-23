package com.tragicfruit.twitcast.database;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.tragicfruit.twitcast.stream.UpcomingEpisode;
import com.tragicfruit.twitcast.episode.Episode;
import com.tragicfruit.twitcast.R;
import com.tragicfruit.twitcast.show.Show;
import com.tragicfruit.twitcast.episode.StreamQuality;
import com.tragicfruit.twitcast.constants.Constants;
import com.tragicfruit.twitcast.constants.SecretConstants;
import com.tragicfruit.twitcast.database.TWiTLab;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Jeremy on 24/02/2016.
 */
public class TWiTFetcher {
    private static final String TAG = "TWiTFetcher";
    private static final Uri TWIT_API_ENDPOINT = Uri.parse("https://twit.tv/api/v1.0");
    private static final Uri GOOGLE_CALENDAR_ENDPOINT = Uri.parse("https://www.googleapis.com/calendar/v3/calendars");

    private TWiTLab mDatabase;
    private Context mContext;

    public TWiTFetcher(Context context) {
        mContext = context;
        mDatabase = TWiTLab.get(context);
    }

    private byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead;
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

    private String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public File getCoverArt(Show show) throws IOException {
        URL url = new URL(show.getCoverArtUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        Writer writer = null;
        File file;
        try {
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + url);
            }

            File coverArtFolder = new File(mContext.getFilesDir() + "/" + Constants.COVER_ART_FOLDER);
            if (!coverArtFolder.exists()) {
                if (!coverArtFolder.mkdir()) {
                    throw new IOException("Error creating cover art folder");
                }
            }

            file = new File(mContext.getFilesDir() + "/" + Constants.COVER_ART_FOLDER, getImageFileName(Uri.parse(show.getCoverArtUrl())));

            OutputStream out = new FileOutputStream(file);
            writer = new OutputStreamWriter(out);

            int read;
            byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }

            Log.i(TAG, "File saved to: " + file.getAbsolutePath());

            return file;
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private String getImageFileName(Uri uri) {
        String url = uri.toString();

        int startIndex = url.lastIndexOf('/');
        int endIndex = url.lastIndexOf('?');
        return url.substring(startIndex + 1, endIndex);
    }

    private byte[] getApiUrlBytes(String urlSpec) throws IOException {
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

            int bytesRead;
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

    private String getApiUrlString(String urlSpec) throws IOException {
        return new String(getApiUrlBytes(urlSpec));
    }

    public List<UpcomingEpisode> fetchUpcomingEpisodes() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'H:m:ssZZZZZ");
        String timeNow = dateFormat.format(new Date());

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 2); // advance date 2 days
        Date twoDaysTime = c.getTime();

        String timeInTwoDays = dateFormat.format(twoDaysTime);

        Uri uri = GOOGLE_CALENDAR_ENDPOINT.buildUpon()
                .appendPath(Constants.GOOGLE_CALENDAR_ID)
                .appendPath("events")
                .appendQueryParameter("key", SecretConstants.GOOGLE_CALENDAR_API_KEY)
                .appendQueryParameter("singleEvents", "true")
                .appendQueryParameter("orderBy", "startTime")
                .appendQueryParameter("timeMin", timeNow)
                .appendQueryParameter("timeMax", timeInTwoDays)
                .build();

        try {
            String jsonBody = getUrlString(uri.toString());
            JSONObject jsonObject = new JSONObject(jsonBody);
            return parseUpcomingEpisodes(jsonObject);

        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON", e);
            return null;
        } catch (IOException ioe) {
            Log.e(TAG, "Error fetching upcoming episodes", ioe);
            return null;
        }
    }

    private List<UpcomingEpisode> parseUpcomingEpisodes(JSONObject json) throws JSONException {
        List<UpcomingEpisode> upcomingEpisodeList = new ArrayList<>();
        JSONArray items = json.getJSONArray("items");

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);

            UpcomingEpisode episode = new UpcomingEpisode();
            episode.setTitle(item.getString("summary"));

            JSONObject start = item.getJSONObject("start");
            String startDateString = start.getString("dateTime");
            episode.setAiringDate(parseDate(startDateString, "yyyy-MM-dd'T'H:m:ssZZZZZ"));

            Log.d(TAG, "Upcoming show: " +  episode.getTitle() + " - " + episode.getAiringDate());
            upcomingEpisodeList.add(episode);
        }

        return upcomingEpisodeList;
    }

    public List<Show> fetchShows() {
        try {
            Uri uri = TWIT_API_ENDPOINT.buildUpon()
                    .appendPath("shows")
                    .appendQueryParameter("shows_active", "1")
                    .build();

            String jsonBody = getApiUrlString(uri.toString());
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
        // Check if TWiT API limit exceeded
        if (json.has("_status") && json.getInt("_status") == 500) {
            Log.e(TAG, "TWiT API limit exceeded");
            return null;
        }

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
            newShow.setShortCode(show.getString("shortCode"));

            JSONObject coverArt = show.getJSONObject("coverArt");
            JSONObject derivatives = coverArt.getJSONObject("derivatives");
            newShow.setCoverArtUrl(derivatives.getString("twit_album_art_1400x1400"));

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

    public List<Episode> fetchAllEpisodes() throws IOException {
        return fetchEpisodes(null);
    }

    public List<Episode> fetchEpisodes(Show show) throws IOException {
        List<Episode> episodeList;
        if (show == null) {
            episodeList = getEpisodeListFromFeed(Constants.BRICKHOUSE_AUDIO_FEED);
            addVideoFeed(episodeList, Constants.BRICKHOUSE_VIDEO_SMALL_FEED, StreamQuality.VIDEO_SMALL);
            addVideoFeed(episodeList, Constants.BRICKHOUSE_VIDEO_LARGE_FEED, StreamQuality.VIDEO_LARGE);
            addVideoFeed(episodeList, Constants.BRICKHOUSE_VIDEO_HD_FEED, StreamQuality.VIDEO_HD);
        } else {
            episodeList = getEpisodeListFromFeed(show.getAudioFeed());
            addVideoFeed(episodeList, show.getVideoSmallFeed(), StreamQuality.VIDEO_SMALL);
            addVideoFeed(episodeList, show.getVideoLargeFeed(), StreamQuality.VIDEO_LARGE);
            addVideoFeed(episodeList, show.getVideoHdFeed(), StreamQuality.VIDEO_HD);
        }
        return episodeList;
    }

    private void addVideoFeed(List<Episode> episodeList, String feedUrl, StreamQuality feedType) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            String feed = getUrlString(feedUrl);
            Document document = builder.parse(new InputSource(new StringReader(feed)));

            NodeList episodeNodeList = document.getElementsByTagName("item");
            int episodeListStartingIndex = findFirstMatchingIndex(episodeList, (Element) episodeNodeList.item(0));

            // i is index for video feed, j is index for episode list (built from audio feed)
            for (int i = 0, j = episodeListStartingIndex; i < episodeNodeList.getLength() && j < episodeList.size(); i++, j++) {
                Episode episode = episodeList.get(j);

                Element episodeElement = (Element) episodeNodeList.item(i);
                String title = episodeElement.getElementsByTagName("title").item(0).getTextContent();
                if (!title.equals(episode.getTitle())) {
                    i--; // stay on video feed episode and find matching one from audio feed
                    continue;
                }

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

            Log.d(TAG, "Fetched video feed " + feedType.toString());
        } catch (ParserConfigurationException | SAXException e) {
            Log.e(TAG, "Error parsing XML", e);
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
            String feed = getUrlString(feedUrl);
            Document document = builder.parse(new InputSource(new StringReader(feed)));

            NodeList episodeNodeList = document.getElementsByTagName("item");
            for (int i = 0; i < episodeNodeList.getLength(); i++) {
                Episode episode = new Episode();

                Element episodeElement = (Element) episodeNodeList.item(i);
                String title = episodeElement.getElementsByTagName("title").item(0).getTextContent();
                episode.setTitle(title);

                String pubDate = episodeElement.getElementsByTagName("pubDate").item(0).getTextContent();
                episode.setPublicationDate(parseDate(pubDate, "EEE, d MMM yyyy k:m:s ZZZ"));

                String subtitle = episodeElement.getElementsByTagName("itunes:subtitle").item(0).getTextContent();
                episode.setSubtitle(subtitle);

                String showNotes = episodeElement.getElementsByTagName("itunes:summary").item(0).getTextContent();
                episode.setShowNotes(showNotes);

                String duration = episodeElement.getElementsByTagName("itunes:duration").item(0).getTextContent();
                episode.setRunningTime(parseRunningTime(duration));

                String audioLink = episodeElement.getElementsByTagName("link").item(0).getTextContent();
                episode.setAudioUrl(audioLink);

                episodeList.add(episode);
            }

            Log.d(TAG, "Fetched audio feeds");

            return episodeList;

        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch episodes", e);
            return null;
        }
    }

    private Date parseDate(String dateString, String format) {
        try {
//            TimeZone timeZone = TimeZone.getTimeZone("GMT");
//            Calendar calendar = Calendar.getInstance(timeZone);
            GregorianCalendar calendar = new GregorianCalendar();
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);

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
