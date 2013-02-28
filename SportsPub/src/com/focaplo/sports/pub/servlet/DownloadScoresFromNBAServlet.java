package com.focaplo.sports.pub.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.focaplo.sports.pub.nba.collector.NBA;

/**
 * 
 * this servlet is scheduled to run every 2 minuts - it will first check the today schedule then download the scores
 *
 */
public class DownloadScoresFromNBAServlet extends HttpServlet{
	Logger log = Logger.getLogger(this.getClass());

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		NBA nba = new NBA();
		nba.downloadTodayScores(false);
		
	}

}
