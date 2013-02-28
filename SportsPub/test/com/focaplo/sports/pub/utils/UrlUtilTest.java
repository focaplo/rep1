package com.focaplo.sports.pub.utils;

import org.junit.Test;

import com.focaplo.sports.pub.nba.collector.ESPN;

public class UrlUtilTest {

	@Test
	public void testGet(){
		UrlUtil u = new UrlUtil();
		String queryString = "sport=nba&date=20130214&lang=en&offset=-300";
		System.out.println(u.doGet("http://localhost:8888/sportspub", queryString));
		
	}
	
	@Test
	public void testESPN(){
		
		String queryString = "sport=nba&date=20130214&lang=en&offset=-300";
		UrlUtil u = new UrlUtil();
		String response = u.doGet(ESPN.surl, queryString);
		System.out.println(response);
	}
	

}
