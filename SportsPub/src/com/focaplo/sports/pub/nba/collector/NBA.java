package com.focaplo.sports.pub.nba.collector;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.focaplo.sports.pub.utils.DateUtil;
import com.focaplo.sports.pub.utils.UrlUtil;

public class NBA extends NbaScoreCollector{

	public static String mobile_daily_scores = "http://data.nba.com/data/json/mobile/SEASON/scores/YYYYMMDD/daily_scores.json";
	public static String lpbb_current = "http://data.nba.com/data/5s/xml/partners/noseason/league/lpbbCurrent.xml";
	
	
	@Override
	public List<Map<String, String>> downloadCurrentScores() {
		//
		log.debug("downloading games from NBA.com");
		String surl = lpbb_current;
		final List<Map<String, String>> res = new ArrayList<Map<String, String>>();
		//send GET request
		try{
			UrlUtil u = new UrlUtil();
			String xmlString = u.doGet(surl, null);
			log.debug("parsing...");
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			final DateTimeFormatter dtFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(DateTimeZone.forID("America/New_York"));
			final Long collectionTime = DateUtil.getNow().getMillis();
			DefaultHandler handler = new DefaultHandler() {
				
				@Override
				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException {
					if("game".equals(qName)){
						String desc = attributes.getValue("desc");
						String vpoints = attributes.getValue("vPts");
						String hpoints = attributes.getValue("hPts");
						String status = attributes.getValue("status");
						if(desc==null || desc.equals("")){
							throw new RuntimeException("Strang, I am expecting game desc but got empty desc ");
								
						}
						//desc="LAL-DAL, 2013-02-24 13:00:00.0"
						String[] segs = desc.split(",");
						if(segs.length!=2){
							throw new RuntimeException("Strang, I am expecting game desc but the format is not good " + desc);
						}
						String vh = segs[0].trim();
						String date = segs[1].trim();
						String[] vhsegs = vh.split("-");
						if(vhsegs.length!=2){
							log.error("Strange I am expecting visitor-home but got " + vh);
							
						}
						String visitor = vhsegs[0];
						String home = vhsegs[1];
						//parse the date which is already America/New York
						DateTime gameStartDate = dtFormatter.parseDateTime(date);
						//
						Map<String, String> scores = new HashMap<String, String>();
						scores.put("collectionTime", Long.toString(collectionTime));
						scores.put("visitor", visitor);
						if(vpoints!=null && !vpoints.equals("")){
							scores.put("visitorScore", vpoints);
						}
						scores.put("home", home);
						if(hpoints!=null && !hpoints.equals("")){
							scores.put("homeScore", hpoints);
						}
						scores.put("status", status);
						scores.put("gameDate", Long.toString(gameStartDate.getMillis()));
						scores.put("gameDay", gameStartDate.toString("yyyyMMdd"));
						//
						scores.put("source", "NBA");
						res.add(scores);
					}
				}

				
				
			};
			saxParser.parse(new InputSource(new StringReader(xmlString)), handler);
			
		}catch(Exception e){
			log.error("ERROR", e);
			throw new RuntimeException(e);
		}
		return res;
	}
	
	
	
}
