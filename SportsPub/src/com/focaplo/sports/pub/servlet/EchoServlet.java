package com.focaplo.sports.pub.servlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class EchoServlet extends HttpServlet {
	public Logger log = Logger.getLogger(this.getClass().getName());
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		log.info("I am clicked! from "  + req.getRemoteAddr());
		String queryString = req.getQueryString();
		String pathInfo = req.getPathInfo();
		String rcType = req.getContentType();
		
		Map<String, Object> data = new HashMap<String, Object>();
		Date now = new Date();
		data.put("date", now.toString());
		data.put("timestamp", Long.toString(new Date().getTime()));
		data.put("pathInfo", pathInfo);
		data.put("contextPath", req.getContextPath());
		data.put("requestUri", req.getRequestURI());
		data.put("request", queryString);
		data.put("parameters", req.getParameterMap());
		//request headers
		Enumeration headers = req.getHeaderNames();
		Map<String, String> headerMap = new HashMap<String, String>();
		while(headers.hasMoreElements()){
			String header =(String) headers.nextElement();
			String value = req.getHeader(header);
			headerMap.put(header, value);
		}
		data.put("headers", headerMap);
		//cookies
		Cookie[] cookies = req.getCookies();
		if(cookies!=null){
			List<Map<String, String>> cookieList = new ArrayList<Map<String, String>>();
			
			for(Cookie cookie:cookies){
				Map<String, String> cookieMap = new HashMap<String, String>();
				cookieMap.put("name", cookie.getName());
				cookieMap.put("path", cookie.getPath());
				cookieMap.put("domain", cookie.getDomain());
				cookieMap.put("value", cookie.getValue());
				cookieList.add(cookieMap);
			}
			data.put("cookies", cookieList);
		}
		data.put("result", "You made it");
		StringBuilder sb = new StringBuilder();
		if("application/json".equals(rcType)){
			resp.setContentType("application/json");
			JSONObject jsonObject = new JSONObject(data);
			sb.append(jsonObject.toString());
		}if("application/xml".equals(rcType)){
			sb.append("unsupported request content type");
		}else{
			resp.setContentType("application/json");
			JSONObject jsonObject = new JSONObject(data);
			sb.append(jsonObject.toString());
		}
		
		resp.getWriter().println(sb.toString());
		resp.getWriter().flush();
	}
}
