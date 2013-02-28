package com.focaplo.sports.pub.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.focaplo.sports.pub.nba.collector.ESPN;
import com.focaplo.sports.pub.nba.collector.Normalization;

public class WebAppStartUpListener implements ServletContextListener {
	Logger log = Logger.getLogger(this.getClass());
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		log.info("application ended");

	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		log.info("application started");
		//load the teams
		Normalization.load();
		log.info("found " + Normalization.teams.size()+" teams");
		//update today schedule
		ESPN espn = new ESPN();
		espn.updateSchedule();
	}

}
