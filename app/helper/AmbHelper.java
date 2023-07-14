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
	public String addNameToAffiliationByAmb(String ambContent, String agent) {

		LinkedHashMap<String, String> affilLabelMap =
				JsonMapper.getPrefLabelMap("affiliation-de.properties");
		JSONObject ambJsonContent = new JSONObject(ambContent);
		if (ambJsonContent.getJSONArray(agent) != null
				&& !ambJsonContent.getJSONArray(agent).isEmpty()) {
			JSONArray agentArray = ambJsonContent.getJSONArray(agent);
			for (int i = 0; i < agentArray.length(); i++) {
				JSONObject agentObject = agentArray.getJSONObject(i);
				if (agentObject.has("affiliation")) {
					JSONObject affilation = agentObject.getJSONObject("affiliation");
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

	/**
	 * This method deletes invalid affiliation from an AmbContent
	 * 
	 * @param ambContent
	 * @param agent (creator | contributor)
	 * @return
	 */
	public String removeAffiliation(String ambContent, String agent) {

		JSONObject agentObject = null;
		JSONObject affi = null;
		JSONObject ambJson = new JSONObject(ambContent);
		if (ambJson.getJSONArray(agent) != null
				&& !ambJson.getJSONArray(agent).isEmpty()) {
			JSONArray agentArray = ambJson.getJSONArray(agent);
			for (int i = 0; i < agentArray.length(); i++) {
				agentObject = agentArray.getJSONObject(i);
				if (agentObject.has("affiliation")) {
					affi = agentObject.getJSONObject("affiliation");
					if (affi.has("id")) {
						String id = affi.getString("id");
						if (id.equals("unbekannt")) {
							agentObject.remove("affiliation");

						}
					}

				}

			}
		}
		return ambJson.toString();
	}

	/**
	 * This method adds the Affiliation element to Agent(creator | contributer)
	 * which have no affiliation.
	 * 
	 * @param ambContent
	 * @param agent (creator | contributer)
	 * @return
	 */
	public String addAffiliationToAgent(String ambContent, String agent) {
		JSONObject agentObject = null;
		JSONObject affi = null;

		JSONObject ambJson = new JSONObject(ambContent);
		if (ambJson.getJSONArray(agent) != null
				&& !ambJson.getJSONArray(agent).isEmpty()) {
			JSONArray agentArray = ambJson.getJSONArray(agent);
			for (int i = 0; i < agentArray.length(); i++) {
				agentObject = agentArray.getJSONObject(i);
				if (!agentObject.has("affiliation")) {
					affi = new JSONObject();
					affi.put("id", "unbekannt");
					affi.put("type", "Organization");
					agentObject.put("affiliation", affi);

				}

			}
		}
		return ambJson.toString();

	}

}
