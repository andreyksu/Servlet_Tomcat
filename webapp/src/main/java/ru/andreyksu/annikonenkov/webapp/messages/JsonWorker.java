package ru.andreyksu.annikonenkov.webapp.messages;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonWorker {

	public String parseJson(String rowJson) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject object = (JSONObject) parser.parse(rowJson);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

}
