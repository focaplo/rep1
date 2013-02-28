package com.focaplo.sports.pub.nba.collector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Instant;

import com.focaplo.sports.pub.nba.dao.NbaDao;
import com.focaplo.sports.pub.utils.DateUtil;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public abstract class NbaScoreCollector {
	public Logger log = Logger.getLogger(this.getClass().getName());
	
	public MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
	 //syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));

	public abstract List<Map<String, String>> downloadCurrentScores();
	
	public void updateSchedule(){
		List<Map<String, String>> games = this.downloadCurrentScores();;
		//create the game entities in data store
		Map<String, String> data = this.saveTodaySchedule(games);
		
		//put a download task into queue, based on the first game today
		//Queue queue = QueueFactory.getQueue(nbaScoresDownloadQueue);
		String firstGameStartTimestamp = data.get("firstGameStartTimestamp");
		Long etaMillis = Long.parseLong(firstGameStartTimestamp);
		DateTime dt = new DateTime(etaMillis);
		log.debug("first game starts at " + dt.toString());
		//queue.add(TaskOptions.Builder.withUrl(nbaScoresDownloaderUrl).method(Method.GET).etaMillis(etaMillis));
	}
	

	
	public Map<String, String> saveTodaySchedule(List<Map<String, String>> games){
		log.debug("save today schedule ..." + games.size() + " games found");
		Map<String, String> res = new HashMap<String, String>();
		Long minGameDate = DateUtil.getEndOfToday();//by default end of today
		boolean isAllFinal = true;
		for(Map<String, String> game:games){
			log.debug(game);
			if(!game.containsKey("gameDate")){
				log.error("kind of strange, no game date in the downloaded games?");
				continue;
			}
			if(!"3".equals(game.get("status"))){
				isAllFinal = false;
			}
			Long gameDate = Long.parseLong(game.get("gameDate"));
			
			if(gameDate!=null && gameDate<minGameDate){
				minGameDate = gameDate;
			}
		}
		res.put("firstGameStartTimestamp", Long.toString(minGameDate));
		res.put("isAllFinal", Boolean.toString(isAllFinal));
		//NbaDao dao = new NbaDao();
		//dao.saveTodaySchedule(minGameDate, isAllFinal);
		log.info("put in cache + " + res);
		cache.put("today", res);
		return res;
	}

	
	public void downloadTodayScores(boolean force){
		NbaDao dao = new NbaDao();
		
		//first check the schedule record, 1, -1, 0 (0 means there is not schedule yet)
		Map<String, String> currentSchedule = (Map<String, String>)cache.get("today");
		if(currentSchedule==null){
			log.debug("not found in cache ");
		}
		boolean canProceed=true;
		if(force || currentSchedule==null){
			
		}else{
			if(Instant.now().getMillis()<Long.parseLong(currentSchedule.get("firstGameStartTimestamp"))){
				canProceed=false;
			}else if(Boolean.parseBoolean(currentSchedule.get("isAllFinal"))){
				canProceed=false;
			}
		}
		if(canProceed){
			List<Map<String, String>> games = this.downloadCurrentScores();
			dao.saveScores(games);
			/*boolean isAllFinal = true;
			for(Map<String, String> game:games){
				String status = game.get("status");
				if(!status.equals("3")){
					isAllFinal=false;
					break;
				}
			}
			if(isAllFinal){
				log.debug("all games are final now...");
			}*/
		}else{
			log.debug("looks like either there is no game today or it is before the first game or all games are final already, no need to collect scores");
		}
	}
	
}
