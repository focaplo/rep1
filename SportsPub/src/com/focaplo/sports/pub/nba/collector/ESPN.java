package com.focaplo.sports.pub.nba.collector;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.focaplo.sports.pub.utils.UrlUtil;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONObject;




public class ESPN extends NbaScoreCollector{
	public Logger log = Logger.getLogger(this.getClass().getName());
	public static String surl = "http://scores.espn.go.com/allsports/scorecenter/v2/events";
	//public static String nbaScoresDownloadQueue = "nbaScoresDownloader";
	//public static String nbaScoresDownloaderUrl = "/sys/nba/downloadScores";
	//public static Long nbaScoreDwonloadIntervalInMilli=2*60*1000L;//download scores every 2 minutes
	
	

	
	public List<Map<String, String>> downloadCurrentScores(){
		log.debug("downloading games from ESPN");
		List<Map<String, String>> res = new ArrayList<Map<String, String>>();
		//send GET request
		try{
			UrlUtil u = new UrlUtil();
			String jsonString = u.doGet(surl, "sport=nba&lang=en&offset=-300");
			log.debug(jsonString);
			JSONObject espn = new JSONObject(jsonString);
			if(espn.has("contents")){
				JSONArray root = espn.getJSONArray("contents");
				for(int i=0; i<root.length();i++){
					JSONObject obj = root.getJSONObject(i);
					if(obj.has("contents")){
						JSONArray games = obj.getJSONArray("contents");
						for(int j=0; j<games.length(); j++){
							JSONObject game = games.getJSONObject(j);
							//String status = game.getString("status");
							String statusType = game.getString("statusType");
							String gameDateGMT = game.getString("gameDate");
							//log.debug("game " + gameDateGMT + " status:" + statusType);
							if(game.has("competitors")){
								JSONArray teams = game.getJSONArray("competitors");
								if(teams.length()==2){
									JSONObject visitor = teams.getJSONObject(0);
									String visitorShortName = Normalization.getAbbrev(visitor.getString("location"), visitor.getString("name"));
									String visitorScore = visitor.has("score")?visitor.getString("score"):null;
									//log.debug("visitor:" + visitorShortName + " score:" + visitorScore);
									JSONObject home = teams.getJSONObject(1);
									String homeShortName = Normalization.getAbbrev(home.getString("location"), home.getString("name"));
									String homeScore = home.has("score")?home.getString("score"):null;
									//log.debug("home:" + homeShortName + " score:" + homeScore);
									Map<String, String> scores = new HashMap<String, String>();
									scores.put("collectionTime", Long.toString(new Date().getTime()));
									scores.put("visitor", visitorShortName);
									if(visitorScore!=null){
										scores.put("visitorScore", visitorScore);
									}
									scores.put("home", homeShortName);
									if(homeScore!=null){
										scores.put("homeScore", homeScore);
									}
									scores.put("status", statusType);
									//2013-02-15T01:00:00
									DateTime dt = new DateTime(gameDateGMT);
									scores.put("gameDate", Long.toString(dt.getMillis()));
									scores.put("gameDay", dt.withZone(DateTimeZone.forID("America/New_York")).toString("yyyyMMdd"));
									//
									scores.put("source", "ESPN");
									res.add(scores);
								}
							}
						}
					}
					
				}
			}
		}catch(Exception e){
			log.error("ERROR", e);
			throw new RuntimeException(e);
		}
		//parse the JSON response
		return res;
	}
}
