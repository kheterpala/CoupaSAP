package com.eikontx.coupaint;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

public class IntUtil {
	
	static Properties props = new java.util.Properties();
	
	static {
		loadProps("app.properties");
	}
	
	public IntUtil() {
	}
	
	private static void loadProps(String appConfigPath) {
		
		InputStream in = IntUtil.class.getClassLoader().getResourceAsStream(appConfigPath);
		
		try {
			props.load(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Date getDate(LocalDate dt) {
		return Date.from(dt.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	
	public static LocalDate getLocalDate(Date dt) {
		return dt.toInstant()
			      .atZone(ZoneId.systemDefault())
			      .toLocalDate();
	}
	
	
	public static Date getPostingDate(Date txDate) {
		LocalDate runDate = LocalDate.now();
		LocalDate postingDate = runDate;
		LocalDate monthEndDate = getFiscalMonthEnd(runDate);		
		LocalDate txDateLocal = getLocalDate(txDate);
		
		if (postingDate.isAfter(monthEndDate) && !txDateLocal.isAfter(monthEndDate)) postingDate = monthEndDate;
		

		return getDate(postingDate);
	}
	
	
	private static LocalDate getFiscalMonthEnd(LocalDate runDateLocal) {
		
		ZoneId timeZone = ZoneId.systemDefault();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

		int month = runDateLocal.get(ChronoField.MONTH_OF_YEAR);
		int year = runDateLocal.get(ChronoField.YEAR);
		
		String curMonthStr = String.format("%02d", month)+"-"+year;
		
		String curMonthRangeStr = getProperty(curMonthStr);
		//System.out.println("Month Str Rnge for:" + curMonthStr + " is:" + curMonthRangeStr);
		
		String[] dtRange = getProperty(curMonthStr).split("-");
		
		LocalDate startDate = LocalDate.parse(dtRange[0], formatter);
		LocalDate endDate = LocalDate.parse(dtRange[1], formatter);
		
		LocalDate monthEndLocal = runDateLocal;
		if (!runDateLocal.isBefore(startDate))
			monthEndLocal = monthEndLocal.plusMonths(1); 
		
		
		month = monthEndLocal.get(ChronoField.MONTH_OF_YEAR);
		year = monthEndLocal.get(ChronoField.YEAR);
		monthEndLocal = LocalDate.of(year, month, 1);
		monthEndLocal = monthEndLocal.plusDays(-1);
				
		//System.out.println("Month Str" + curMonthStr + " Date" + monthEndLocal);
		
		return monthEndLocal;
	}
	
	
	public static String getProperty(String name) {
		String val = null;
	
		val = props.getProperty(name);
		
		return val;
	}
	
	public static Date getDtFromUTC(String zuluTimeStr) {
		Instant instant = Instant.from(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(zuluTimeStr));
		Date dt = Date.from(instant);
		return dt;
	}
	
	
	public static String getSAPDtStr(Date dt) {
		String dtFormat = getProperty("sap_dtformat");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dtFormat);
		
		return formatter.format(getLocalDate(dt));
		
	}
	
	public static List<String> getTokens(String str) {
		String inputDelim = getProperty("coupa_delim");
		List<String> tokens = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(str, inputDelim);
		while (tokenizer.hasMoreElements()) {
			String token = tokenizer.nextToken().trim();
			token = token.replaceAll("^\"|\"$", "");
			tokens.add(token);
		}
		return tokens;
	}
	
	
	public static String escapeSpecialCharacters(String data) {
		String escapedData = data.replaceAll("\\R", " ");
		if (data.contains(",") || data.contains("\"") || data.contains("'")) {
			data = data.replace("\"", "\"\"");
			escapedData = "\"" + data + "\"";
		}
		return escapedData;
	}
	
	public static String splitData(BufferedReader br, String hdrCol1, String hdrCol2,
					int recTypeIdx, String recType) throws IOException {
		
		StringBuilder sb = new StringBuilder();
		
		String line;
		boolean hdrFound = false;
		//Get header & contents
		while ((line = br.readLine()) != null) {
			List<String> tokens = new ArrayList<>();
			tokens = getTokens(line);
			
			if (tokens.get(0).equals("type") && 
					tokens.contains(hdrCol1) && tokens.contains(hdrCol2) &&
					!hdrFound) {
				sb.append(line);
				//System.out.println("Appending line" + line);
				hdrFound= true;
			}
			
			if (hdrFound && tokens.get(recTypeIdx).equals(recType)) {
				sb.append(System.lineSeparator());
				sb.append(line);
			}
			
		}
		
		
		return sb.toString();
		
	}
}
