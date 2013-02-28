package com.focaplo.sports.pub.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class ScoreDataDaoTest {

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
	public void testSaveGameData() throws ParseException{
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key gameKey = KeyFactory.createKey("Game", "20130214MIAOKC");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		Entity game = new Entity(gameKey);
		game.setProperty("gameDate", "20130214 21:00:00");
		game.setProperty("visitor", "MIA");
		game.setProperty("home", "OKC");
		
		ds.put(game);
		
		Entity scores1 = new Entity("Scores", gameKey);
		String timestamp = "20130214 21:10:00";
		scores1.setProperty("timestamp", sdf.parse(timestamp).getTime());
		scores1.setProperty("visitorScore", 20);
		scores1.setProperty("homeScore", 28);
		
		Entity scores2 = new Entity("Scores", gameKey);
		String timestamp2 = "20130214 21:20:00";
		scores2.setProperty("timestamp", sdf.parse(timestamp2).getTime());
		scores2.setProperty("visitorScore", 30);
		scores2.setProperty("homeScore", 38);
		
		
		Entity scores3 = new Entity("Scores", gameKey);
		String timestamp3 = "20130214 21:30:00";
		scores3.setProperty("timestamp", sdf.parse(timestamp3).getTime());
		scores3.setProperty("visitorScore", 40);
		scores3.setProperty("homeScore", 48);
		
		
		List<Entity> scores = Arrays.asList(scores1, scores2, scores3);
		ds.put(scores);
		
		//now query
		String from = "20130214 21:15:00";
		String to = "20130214 21:25:00";
		
		Filter timeMinFilter = new FilterPredicate("timestamp",FilterOperator.GREATER_THAN_OR_EQUAL, sdf.parse(from).getTime());
		Filter timeMaxFilter = new FilterPredicate("timestamp",FilterOperator.LESS_THAN_OR_EQUAL, sdf.parse(to).getTime());

		Filter timeRangeFilter = CompositeFilterOperator.and(timeMinFilter, timeMaxFilter);
		Query q = new Query("Scores").setFilter(timeRangeFilter).setAncestor(gameKey);

		// Use PreparedQuery interface to retrieve results
		PreparedQuery pq = ds.prepare(q);


		for (Entity result : pq.asIterable()) {
		
		  Long visitorScore = (Long) result.getProperty("visitorScore");
		  Long homeScore = (Long) result.getProperty("homeScore");
		  System.out.println(visitorScore + " " + homeScore);
		}
	}
}
