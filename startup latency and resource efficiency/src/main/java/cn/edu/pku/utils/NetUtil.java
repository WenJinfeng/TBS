package cn.edu.pku.utils;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class NetUtil {
    public static String postParams(String url, String data) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String entityStr = null;
        CloseableHttpResponse response = null;
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(500*1000)
                .setConnectionRequestTimeout(500*1000)
                .setSocketTimeout(500*1000).build();
        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            if(data != null) {
                httpPost.setEntity(new StringEntity(data, Charset.forName("UTF-8")));
            }
            httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7.6)");
            httpPost.addHeader("Content-Type", "application/json; charset=utf-8");
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            entityStr = EntityUtils.toString(entity, "UTF-8");
            // System.out.println(Arrays.toString(response.getAllHeaders()));
        } catch (ClientProtocolException e) {
            System.err.println("Http issue");
            e.printStackTrace();
        } catch (ParseException e) {
            System.err.println("parse error");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO exception");
            e.printStackTrace();
        } finally {
            
            if (null != response) {
                try {
                    response.close();
                    httpClient.close();
                } catch (IOException e) {
                    System.err.println("release connection error");
                    e.printStackTrace();
                }
            }
        }

        System.out.println(entityStr);
        return entityStr;
    }
}
