package com.tragicfruit.twitcast.utils;

/**
 * Created by Jeremy on 12/06/2017.
 */

public class Utils {
    public static String getUrlWithPrefix(String url, boolean ssl) {
        if (!url.startsWith("//")) {
            url = "//" + url;
        }

        if (ssl) {
            return "https:" + url;
        } else {
            return "http:" + url;
        }
    }
}
