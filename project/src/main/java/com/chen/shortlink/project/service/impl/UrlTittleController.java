package com.chen.shortlink.project.service.impl;

import com.chen.shortlink.project.service.UrlTitleService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * URL 标题接口实现层
 */
@Service
public class UrlTittleController implements UrlTitleService {
    @Override
    public String getTitleByUrl(String url) {
        URL targetUrl = null;
        try {
            targetUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Document document = Jsoup.connect(url).get();
                return document.title();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "Error while fetching title.";
    }
}
