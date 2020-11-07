package ru.nanit.vasyascheduler.services;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import ru.nanit.vasyascheduler.api.storage.Configuration;
import ru.nanit.vasyascheduler.data.Person;
import ru.nanit.vasyascheduler.services.conversion.HtmlToImage;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class PointsManager {

    private HttpPost loginRequest;
    private HttpGet pointsRequest;
    private String dataTemplate;
    private RequestConfig requestConfig;
    private int successCode;

    public PointsManager(Configuration conf){
        loginRequest = new HttpPost(conf.get().getNode("login", "url").getString());

        loginRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
        loginRequest.addHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        loginRequest.addHeader("Accept-Encoding", "gzip, deflate");
        loginRequest.addHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");

        pointsRequest = new HttpGet(conf.get().getNode("points").getString());
        pointsRequest.addHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        pointsRequest.addHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");

        successCode = conf.get().getNode("login", "successCode").getInt();
        dataTemplate = conf.get().getNode("login", "data").getString();
        int connTimeout = conf.get().getNode("connTimeout").getInt() * 1000;

        requestConfig = RequestConfig.custom()
                .setConnectTimeout(connTimeout)
                .setSocketTimeout(connTimeout)
                .setConnectionRequestTimeout(connTimeout)
                .build();
    }

    public BufferedImage getPoints(Person person, String password) throws IOException {
        CookieStore cookies = new BasicCookieStore();
        HttpClient client = HttpClients.custom()
                .setDefaultCookieStore(cookies)
                .build();

        if(login(client, person, password)){
            String html = getPointsHtml(client, cookies);
            if(html != null){
                return new HtmlToImage(html.replace("\n", "")).generate();
            }
        }

        return null;
    }

    private boolean login(HttpClient client, Person person, String password) throws IOException {
        HttpPost post = new HttpPost(loginRequest.getURI());
        post.setConfig(requestConfig);
        post.setHeaders(loginRequest.getAllHeaders());

        Map<String, String> values = new HashMap<>();
        values.put("lastname", URLEncoder.encode(person.getLastName(), "cp1251"));
        values.put("n1", URLEncoder.encode(person.getFirstName(), "cp1251"));
        values.put("n2", URLEncoder.encode(person.getPatronymic(), "cp1251"));
        values.put("password", URLEncoder.encode(password, "cp1251"));

        String data = StrSubstitutor.replace(dataTemplate, values);
        StringEntity entity = new StringEntity(data);

        post.setEntity(entity);

        HttpResponse response = client.execute(post);

        return response.getStatusLine().getStatusCode() == successCode;
    }

    private String getPointsHtml(HttpClient client, CookieStore cookies) throws IOException {
        HttpGet request = new HttpGet(pointsRequest.getURI());
        request.setHeaders(pointsRequest.getAllHeaders());
        request.addHeader("Cookie", cookiesToString(cookies));
        HttpResponse response = client.execute(request);
        return IOUtils.toString(response.getEntity().getContent(), "cp1251");
    }

    private String cookiesToString(CookieStore cookies){
        StringBuilder builder = new StringBuilder();
        for(Cookie c : cookies.getCookies()) {
            builder.append(c.getName());
            builder.append("=");
            builder.append(c.getValue());
            builder.append("; ");
        }
        return builder.toString();
    }
}
