package com.focaplo.sports.pub.nba.collector;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.focaplo.sports.pub.nba.dao.NbaDao;
import com.focaplo.sports.pub.utils.DateUtil;
import com.focaplo.sports.pub.utils.UrlUtil;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class ESPNTest {

	@Test
	public void testGetToday(){
		ESPN e = new ESPN();
		e.downloadTodayScores(true);
	}
	@Test
	public void testGetTodaySchedule(){
		ESPN e = new ESPN();
		e.updateSchedule();
	}
	
	@Test
	public void testGetGames() throws UnsupportedEncodingException{
		ESPN e = new ESPN();
		System.out.println(e.downloadCurrentScores());
	}
	
	@Test
	public void testParseDate() throws ParseException{
		String timestamp = "2013-02-15T01:00:00Z";
		DateTime dt = new DateTime(timestamp);
		System.out.println("date:" + dt);
		Date myDate = new Date();
		myDate.setTime(dt.getMillis());
		System.out.println(myDate);
	}
	
	private final LocalServiceTestHelper helper =
	        new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	
	@Before
	public void setUp() {
	        helper.setUp();
	}

	@After
	public void tearDown() {
	        helper.tearDown();
	}
	
	@Test
	public void testSave() throws UnsupportedEncodingException{
		ESPN e = new ESPN();
		List<Map<String, String>> games = e.downloadCurrentScores();
		System.out.println(games);
		NbaDao dao = new NbaDao();
		dao.saveScores(games);
		//now query the scores
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		String gameKeyName = "20130214MIAOKC";
		Key gameKey = KeyFactory.createKey("Game", gameKeyName);
		Query q = new Query("Scores").setAncestor(gameKey);

		// Use PreparedQuery interface to retrieve results
		PreparedQuery pq = ds.prepare(q);


		for (Entity result : pq.asIterable()) {
		
		  String visitorScore = (String) result.getProperty("visitorScore");
		  String homeScore = (String) result.getProperty("homeScore");
		  System.out.println(result.getProperty("timestamp")+ " " + visitorScore + " " + homeScore);
		}
	}
	
	@Test
	public void testQueryWithRange() throws UnsupportedEncodingException, JSONException{
		String surl = "http://localhost:8888/pub/nba/scores";
		String queryString = "s=ESPN&date=20130224&v=GSW&h=MIN&from="+URLEncoder.encode("20130224 16:00:00", "UTF-8") + "&to=" + URLEncoder.encode("20130224 17:00:00", "UTF-8");
		UrlUtil u = new UrlUtil();
		String s = u.doGet(surl, queryString);
		System.out.println(s);
		JSONObject scores = new JSONObject(s);
		JSONArray arr = scores.getJSONArray("scores");
		for(int i=0; i<arr.length();i++){
			JSONObject score = arr.getJSONObject(i);
			System.out.println("" + DateUtil.printTime(score.getLong("timestamp")) + " " + score.getString("visitorScore") + " " + score.getString("homeScore"));
		}
	}
	
	@Test
	public void testJodaTime(){
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd HH:mm:ss");
		{
			DateTime dt2  =formatter.parseDateTime("20130219 19:30:00");
			System.out.println(dt2 + " " + dt2.getMillis());
		}
		{
			DateTime dt2  =formatter.withZone(DateTimeZone.forID("America/Los_Angeles")).parseDateTime("20130217 21:30:00");
			System.out.println(dt2 + " " + dt2.getMillis());
		}
		DateTime dfrom = formatter.withZone(DateTimeZone.forID("America/New_York")).parseDateTime("20130217 21:30:00");
		System.out.println(dfrom);
		long milis = dfrom.getMillis();
		System.out.println(milis);
		Date date = new Date();
		System.out.println(date);
		date.setTime(milis);
		System.out.println(date);
	}
	
	
	@Test
	public void testGetLatestScoreOfGame() throws JSONException{
		String surl = "http://sportspubstatic.appspot.com/pub/nba/latest";
		String queryString = "date=20130224&v=GS&h=MIN&s=ESPN";
		UrlUtil u = new UrlUtil();
		String s = u.doGet(surl, queryString);
		System.out.println(s);
		JSONObject score = new JSONObject(s);
	
		System.out.println("" + DateUtil.printTime(score.getLong("timestamp")) + " " + score.getString("visitorScore") + " " + score.getString("homeScore"));
		
	}
}
