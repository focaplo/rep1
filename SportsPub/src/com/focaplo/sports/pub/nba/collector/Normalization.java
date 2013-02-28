package com.focaplo.sports.pub.nba.collector;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.focaplo.sports.pub.utils.UrlUtil;

public class Normalization {
	public static Logger log = Logger.getLogger(Normalization.class);
	public static String teamsUrl = "http://data.nba.com/data/5s/xml/partners/noseason/league/lpTeams.xml";
	public static List<Map<String, String>> teams = new ArrayList<Map<String, String>>();
	
	public static String getAbbrev(String location, String teamname){
		if(teams.isEmpty()){
			synchronized(Normalization.class) {
				if(teams.isEmpty()){
					load();
				}
			}
		}
		for(Map<String, String> team:teams){
			if(location!=null && location.equalsIgnoreCase((String)team.get("city"))){
				return (String)team.get("abbrev");
			}else if(teamname!=null && teamname.equalsIgnoreCase((String)team.get("name"))){
				return (String)team.get("abbrev");
			}
		}
		throw new RuntimeException("failed to find team with location " + location + " and name " + teamname);
	}
	
	public static void load(){
		try{
			UrlUtil u = new UrlUtil();
			String xmlString = u.doGet(teamsUrl, null);
			log.debug("parsing...");
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new DefaultHandler() {
				
				@Override
				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException {
					if("team".equals(qName)){
						String offcial = attributes.getValue("official");
						if("true".equalsIgnoreCase(offcial)){
							String city = attributes.getValue("city");
							String name = attributes.getValue("name");
							String abbrev = attributes.getValue("abbrev");
							Map<String, String> team = new HashMap<String, String>();
							team.put("city", city);
							team.put("name", name);
							team.put("abbrev", abbrev);
							teams.add(team);
						}
					}
				}
			};
			saxParser.parse(new InputSource(new StringReader(xmlString)), handler);
			
		}catch(Exception e){
			log.error("ERROR", e);
			throw new RuntimeException(e);
		}
	}
}
