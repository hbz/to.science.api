package helper;

import models.Node;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.LinkedHashMap;
import helper.GenericPropertiesLoader;

public class AmbHelper {

	/**
	 * This method adds the attribute 'name' to the AMBConetent for the element
	 * 'affiliation'.
	 * 
	 * @param ambContent: String
	 * @return new AMBContent with attribute 'name' by the element affiliation
	 */
	public String addNameToAffiliationByAmb(String ambContent) {

		LinkedHashMap<String, String> affilLabelMap =
				JsonMapper.getPrefLabelMap("affiliation-de.properties");

		JSONObject ambJsonContent = new JSONObject(ambContent);

		if (ambJsonContent.has("creator")) {
			JSONArray creatorArray = ambJsonContent.getJSONArray("creator");
			for (int i = 0; i < creatorArray.length(); i++) {
				JSONObject creator = creatorArray.getJSONObject(i);
				if (creator.has("affiliation")) {
					JSONObject affilation = creator.getJSONObject("affiliation");

					if (affilation.has("id")) {
						String id = affilation.getString("id");
						affilation.put("name", affilLabelMap.get(id));

					}
				}
			}

		}

		return ambJsonContent.toString();
	}

	/**
	 * This method adds the attribute 'name' to the AMBConetent for the element
	 * 'funder'.
	 * 
	 * @param ambContent
	 * @return new AMBContent with attribute 'name' by the element funder
	 */
	public String addNameByFunderByAmb(String ambContent) {
		LinkedHashMap<String, String> funderLabelMap = new LinkedHashMap<>();
		GenericPropertiesLoader genProp = new GenericPropertiesLoader();
		funderLabelMap.putAll(genProp.loadVocabMap("funder-de.properties"));

		JSONObject ambJsonContent = new JSONObject(ambContent);

		if (ambJsonContent.has("funder")) {
			JSONObject funder = ambJsonContent.getJSONObject("funder");
			if (funder.has("url")) {
				String url = funder.getString("url");
				funder.put("name", funderLabelMap.get(url));
			}

		}
		return ambJsonContent.toString();
	}
}
