package com.focaplo.sports.pub.nba.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

public class NbaDao {
	public Logger log = Logger.getLogger(this.getClass().getName());
	
	/**
	 * this method will save the Games and Scores (if there is)
	 * 
	 * @param games
	 */
	public void saveScores(List<Map<String, String>> games){
		for(Map<String, String> game:games){
			long newStatus = this.toInt(game.get("status"),1);
			long vScore = this.toInt(game.get("visitorScore"), 0);
			long hScore = this.toInt(game.get("homeScore"), 0);
			
			Long lastVisitorScore = 0L;
			Long lastHomeScore = 0L;
			String source = game.get("source");
			String gameKeyName = game.get("gameDay")+game.get("visitor")+game.get("home");
			log.debug("game key name " + gameKeyName + " source:"+source+" status:" + newStatus+" scores:" + vScore+ " " + hScore);
			DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
			Key gameKey = KeyFactory.createKey("Game", gameKeyName);
			try{
				log.debug("found the game entity,now check whether we need to update status or scores");
				Entity gameEntity = ds.get(gameKey);
				Long status = (Long)gameEntity.getProperty("status");
				lastVisitorScore = (Long)gameEntity.getProperty("visitorScore"+source);
				lastHomeScore = (Long)gameEntity.getProperty("homeScore"+source);
				status = status==null?1:status;
				lastVisitorScore = lastVisitorScore==null?0:lastVisitorScore;
				lastHomeScore = lastHomeScore==null?0:lastHomeScore;
				log.debug("currenly " + status + " " + lastVisitorScore+ " " + lastHomeScore);
				if(newStatus>status || (vScore+hScore)>(lastVisitorScore+lastHomeScore)){
					log.debug("game status or scores are changed for this game based on data from " + source);
					gameEntity.setProperty("status", newStatus);
					gameEntity.setProperty("visitorScore"+source, vScore);
					gameEntity.setProperty("homeScore"+source, hScore);
					ds.put(gameEntity);
				}else{
					log.debug("no change of this game");
				}
				
			}catch(EntityNotFoundException e){
				log.debug("did not find the game entity with key " + gameKey.toString() + " we will create the game first");
				Entity gameEntity = new Entity("Game", gameKeyName);
				gameEntity.setProperty("gameDate", game.get("gameDate"));
				gameEntity.setProperty("gameDay", game.get("gameDay"));
				gameEntity.setProperty("home", game.get("home"));
				gameEntity.setProperty("visitor", game.get("visitor"));
				gameEntity.setProperty("visitorScore"+source, vScore);
				gameEntity.setProperty("homeScore"+source, hScore);
				gameEntity.setProperty("status", newStatus);
				Key gameEntityKey = ds.put(gameEntity);
				log.debug("new game entity is saved to store, with key " + gameEntityKey.toString());
				
			}
/*			Query q = new Query("Game").setFilter(new FilterPredicate("Game", FilterOperator.EQUAL, gameKey)).setKeysOnly();
			boolean gameEntityExists = ds.prepare(q).asIterable().iterator().hasNext();
			*/
			//now add the scores only if they are valid and changed
			if(lastVisitorScore==null || vScore!=lastVisitorScore || lastHomeScore == null || hScore!=lastHomeScore ){
				log.debug("scores " + vScore+":"+hScore + " are different from last time " + lastVisitorScore+":"+lastHomeScore);
				Entity scores1 = new Entity("Scores", gameKey);
				scores1.setProperty("timestamp", Long.parseLong(game.get("collectionTime")));
				scores1.setProperty("visitorScore", vScore);
				scores1.setProperty("homeScore", hScore);
				scores1.setProperty("source", source);
				Key scoreKey = ds.put(scores1);
				log.debug("the score is saved to Scores with key " + scoreKey.toString() + " source:" + source + " " + vScore+":"+hScore);
				
			}
			
		}
	}
	
	/*
	public void saveTodaySchedule(long firstGameStartTimestamp, boolean isAllFinal){
		log.debug("saving today schedule to data store: " + firstGameStartTimestamp + " " + isAllFinal);
		Entity today = new Entity("Schedule", "today");
		today.setProperty("firstGameStartTimestamp", Long.toString(firstGameStartTimestamp));
		today.setProperty("isAllFinal", Boolean.toString(isAllFinal));
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		ds.put(today);
		//update cache
	}
	
	public void updateTodayScheduleAllFinal(){
		Key key  = KeyFactory.createKey("Schedule", "today");
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Entity today;
		try {
			today = ds.get(key);
			today.setProperty("isAllFinal", Boolean.toString(true));
			ds.put(today);
		} catch (EntityNotFoundException e) {
			log.error("Could not find the entity with key " + key.toString());
			
			
		}
	}
	
	public Map<String, String> getCurrentSchedule(){
		Map<String, String> res = new HashMap<String, String>();
		//get today' schedule
		Key key  = KeyFactory.createKey("Schedule", "today");
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		try {
			Entity today = ds.get(key);
			res.put("firstGameStartTimestamp", (String)today.getProperty("firstGameStartTimestamp"));
			res.put("isAllFinal",(String)today.getProperty("isAllFinal"));
			return res;
		}catch (EntityNotFoundException e) {
			log.warn("could not find today schedule entity, this may happen when we start to download scores without saving schedule yet");
			return null;
		}
	}
	
	*/
	public JSONObject getScoreStreamsOfGameFromSource(String yyyyMMdd, String visitor, String home, long fromTimestamp, long toTimestamp, String source) throws JSONException {
		JSONObject res = new JSONObject();
		String gameKeyName = yyyyMMdd+visitor+home;
		log.debug("game key name " + gameKeyName + " from:" + fromTimestamp + " to:" + toTimestamp);
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key gameKey = KeyFactory.createKey("Game", gameKeyName);
		
		JSONArray arr = new JSONArray();
		Filter sourceFilter = new FilterPredicate("source", FilterOperator.EQUAL, source);
		Filter timeMinFilter = new FilterPredicate("timestamp",FilterOperator.GREATER_THAN_OR_EQUAL, fromTimestamp);
		Filter timeMaxFilter = new FilterPredicate("timestamp",FilterOperator.LESS_THAN_OR_EQUAL, toTimestamp);

		Filter timeRangeFilter = CompositeFilterOperator.and(sourceFilter, timeMinFilter, timeMaxFilter);
		Query q = new Query("Scores").setFilter(timeRangeFilter).setAncestor(gameKey).addSort("timestamp", SortDirection.ASCENDING);

		// Use PreparedQuery interface to retrieve results
		PreparedQuery pq = ds.prepare(q);


		for (Entity result : pq.asIterable()) {
			JSONObject scores = new JSONObject();
			log.debug(result.getProperty("found timestamp" + " " + result.getProperty("visitorScore") + " " + result.getProperty("homeScore")));
			scores.put("visitorScore", (Long)result.getProperty("visitorScore"));
			scores.put("homeScore", (Long)result.getProperty("homeScore"));
			scores.put("timestamp", (Long)result.getProperty("timestamp"));
			scores.put("source", (String)result.getProperty("source"));
			arr.put(scores);
		}
		res.put("scores", arr);
		res.put("status", "success");
		return res;
	}
	
	public JSONObject getLastScoreOfGmaeFromSource(String yyyyMMdd, String visitor, String home, String source) throws JSONException{
		JSONObject res = new JSONObject();
		String gameKeyName = yyyyMMdd+visitor+home;
		log.debug("game latest scores if ganme " + gameKeyName );
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key gameKey = KeyFactory.createKey("Game", gameKeyName);
		Query q = new Query("Scores").setFilter(new FilterPredicate("source", FilterOperator.EQUAL, source)).setAncestor(gameKey).addSort("timestamp", SortDirection.DESCENDING);
		PreparedQuery pq = ds.prepare(q);
		List<Entity> scores = pq.asList(FetchOptions.Builder.withLimit(1));
		if(scores.size()>0){
			res.put("visitorScore", (Long)scores.get(0).getProperty("visitorScore"));
			res.put("homeScore", (Long)scores.get(0).getProperty("homeScore"));
			res.put("timestamp", (Long)scores.get(0).getProperty("timestamp"));
			res.put("source", (String)scores.get(0).getProperty("source"));
		}
		res.put("status", "success");
		return res;
	}
	
	public int toInt(String s, int integerOfEmpty){
		if(s==null){
			return integerOfEmpty;
		}
		if(s.equals("")){
			return integerOfEmpty;
		}
		return Integer.parseInt(s);
	}
}
