package com.focaplo.sports.pub.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.log4j.Logger;


public class UrlUtil {
	Logger log = Logger.getLogger(this.getClass());
	
	public String doGet(String surl, String queryString){
		System.out.println(surl + " " + queryString);
        try {
        	
            URL url = queryString==null?new URL(surl):new URL(surl+"?"+queryString);
            log.debug(url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(4000);
            connection.setReadTimeout(2000);
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.addRequestProperty("User-Agent", "iPhone OS/6.1; ScoreCenter; 3.1.1 build 385");
            connection.addRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Cookie", "viAppId=174A150F-5A54-4E7B-B53C-2A10FB46624C");
            connection.addRequestProperty("Device", "iphone");
            connection.addRequestProperty("Carrier", "at&t");
            connection.addRequestProperty("Application-Version", "3.1.1");
            connection.addRequestProperty("Device-OS", "ios");
            connection.addRequestProperty("Device-Version", "N94AP");
            connection.addRequestProperty("Appbundle-Version", "385");
            connection.addRequestProperty("Application-Display-Name", "ScoreCenter");
            connection.addRequestProperty("AlertsToken", "ec69d9816c71446684f22a24690979d0479e6edaeda2a37cfc569dd2648f12a3");
            connection.addRequestProperty("Emanning","174A150F-5A54-4E7B-B53C-2A10FB46624C");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // OK
            	BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String strLine;
				StringBuilder sb = new StringBuilder();
				while ((strLine = br.readLine()) != null){
					sb.append("\n" + strLine);
				}
				return sb.toString();
            } else {
                // Server returned HTTP error code.
            	throw new RuntimeException("ERROR CODE:"+connection.getResponseCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	public String doPost(String surl, String queryString) throws UnsupportedEncodingException{
		String message = URLEncoder.encode(queryString, "UTF-8");

        try {
            URL url = new URL(surl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(message);
            writer.close();
    
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // OK
            	BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String strLine;
				StringBuilder sb = new StringBuilder();
				while ((strLine = br.readLine()) != null){
					sb.append("\n" + strLine);
				}
				return sb.toString();
            } else {
                // Server returned HTTP error code.
            	throw new RuntimeException("ERROR CODE:"+connection.getResponseCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
}
