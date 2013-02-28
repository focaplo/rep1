package com.focaplo.sports.pub.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.focaplo.sports.pub.nba.dao.NbaDao;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

public class GetLastScoreServlet  extends HttpServlet{
	Logger log = Logger.getLogger(this.getClass());

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String yyyyMMdd = req.getParameter("date");
		String visitor=req.getParameter("v");
		String home = req.getParameter("h");
		String source = req.getParameter("s");
		if(source == null || source.equals("")){
			source = "ESPN";
		}
		NbaDao dao = new NbaDao();
		String response = null;
		try {
			JSONObject streams = dao.getLastScoreOfGmaeFromSource(yyyyMMdd, visitor, home, source);
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
