package com.uncc.mobileappdev.inclass05;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by Stephen on 2/12/2018.
 */

public class RequestParams {

    private HashMap<String, String> params;
    private StringBuilder sb;

    public RequestParams() {
        this.params = new HashMap<>();
        sb = new StringBuilder();
    }

    public RequestParams addParameter(String key, String value){
        try {
            params.put(key, URLEncoder.encode(value, "UTF8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getEncodedParams(){
        for(String key : params.keySet()){
            if(sb.length() > 0){
                sb.append("&");
            }
            sb.append(key + "=" + params.get(key));
        }
        return sb.toString();
    }

    public String getEncodedURL(String url){
        return url + "?" + getEncodedParams();
    }

    public void encodePostParams(HttpURLConnection connection) throws IOException {
        connection.setDoOutput(true);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
        outputStreamWriter.write(getEncodedParams());
        outputStreamWriter.flush();
    }
}
