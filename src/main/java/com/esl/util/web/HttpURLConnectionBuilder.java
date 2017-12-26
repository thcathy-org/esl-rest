package com.esl.util.web;

import org.apache.commons.lang3.StringUtils;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpURLConnectionBuilder {
	String uri;
	String encodedParams;
	String referer;
	
	public HttpURLConnection createConnection() {
		HttpURLConnection connection = null;
		try {
			URL url = new URL (uri);

			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestProperty ("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; Trident/5.0)");
			
			if (StringUtils.isNotBlank(referer)) connection.setRequestProperty("Referer", referer);
			
			if (encodedParams != null) {
				connection.setRequestMethod("POST");				
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setRequestProperty("Content-Length", "" + Integer.toString(encodedParams.getBytes().length));
				connection.setUseCaches(false);
			    connection.setDoInput(true);
			    connection.setDoOutput(true);
				
				// Set request params
				DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
				wr.writeBytes(encodedParams);
				wr.flush();
				wr.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return connection;
	}
	
	public HttpURLConnectionBuilder setURL(String uri) {
		this.uri = uri;
		return this;
	}
	
	public HttpURLConnectionBuilder setEncodedParams(String params) {
		this.encodedParams = params;
		return this;
	}
	
	public HttpURLConnectionBuilder referer(String referer) {
		this.referer = referer;
		return this;
	}	
}
