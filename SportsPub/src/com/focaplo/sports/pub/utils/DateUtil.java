package com.focaplo.sports.pub.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateUtil {
	public static String default_timestamp_format="yyyyMMdd HH:mm:ss";
	public static DateTimeFormatter formatter = DateTimeFormat.forPattern(default_timestamp_format).withZone(DateTimeZone.forID("America/New_York"));
	
	public static DateTime parseNewYorkTime(String timestamp){
		return formatter.parseDateTime(timestamp);
	}

	public static Long getStartOfToday() {
		return new DateTime(DateTimeZone.forID("America/New_York")).withTimeAtStartOfDay().getMillis();
	}
	
	public static Long getEndOfToday() {
		return getStartOfToday()+24*60*60*1000;
	}
	
	
	public static DateTime getNow(){
		return new DateTime(DateTimeZone.forID("America/New_York"));
	}
	
	public static String printTime(Long timestamp){
		return new DateTime(timestamp).toString(formatter);
	}
	
}
