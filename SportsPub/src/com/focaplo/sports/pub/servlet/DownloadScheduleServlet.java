package com.focaplo.sports.pub.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.focaplo.sports.pub.nba.collector.NBA;

/**
 * 
 * this servlet is scheduled to run at 6:00 AM each day, get that day's games, and save to the "Schedue" record.
 *
 */
public class DownloadScheduleServlet extends HttpServlet{
	Logger log = Logger.getLogger(this.getClass());

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		log.info("download schedule ...");
		NBA espn = new NBA();
		espn.updateSchedule();
		
		
	}

}
