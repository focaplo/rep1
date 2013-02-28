package com.focaplo.sports.pub.nba.collector;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class NBATest {

	@Test
	public void testDownloadCurrentScores(){
		NBA nba = new NBA();
		List<Map<String, String>> games = nba.downloadCurrentScores();
		for(Map<String, String> game:games){
			System.out.println(game);
		}
	}
}
