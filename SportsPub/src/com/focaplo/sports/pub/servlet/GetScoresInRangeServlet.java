package com.focaplo.sports.pub.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.focaplo.sports.pub.nba.dao.NbaDao;
import com.focaplo.sports.pub.utils.DateUtil;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

public class GetScoresInRangeServlet extends HttpServlet{
	Logger log = Logger.getLogger(this.getClass());

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String yyyyMMdd = req.getParameter("date");
		String visitor=req.getParameter("v");
		String home = req.getParameter("h");
		String fromTimestamp = req.getParameter("from"); //yyyyMMdd HH:mm:ss
		String toTimestamp = req.getParameter("to");
		String source = req.getParameter("s");
		if(source==null || source.equals("")){
			source = "ESPN";
		}
		log.debug("request to find scores " + yyyyMMdd+visitor+home+" from:"+fromTimestamp+" to:"+toTimestamp);
		NbaDao dao = new NbaDao();
		String response = null;
		try {
			
			DateTime dfrom = DateUtil.parseNewYorkTime(fromTimestamp);
			DateTime dTo = DateUtil.parseNewYorkTime(toTimestamp);
			JSONObject streams = dao.getScoreStreamsOfGameFromSource(yyyyMMdd, visitor, home, dfrom.getMillis(), dTo.getMillis(), source);
			response = streams.toString();
		} catch (Exception e) {
			log.error("ERROR", e);
			response="{\"status\":\"fail\"}";
		} 
		
		resp.setContentType("application/json");
		PrintWriter pw = resp.getWriter();
		pw.println(response);
		pw.flush();
	}
}
