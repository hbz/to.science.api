package helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import models.Node;
import util.AdHocUriProvider;

/**
 * 
 * @author adoud
 *
 */
public class Metadata2Helper {

	/**
	 * This method creates a rdf-LinkedHashMap and enriches it with data from the
	 * new data stream toscienceJson
	 * 
	 * @param toscienceJsonContent: the content from the new data stream toscience
	 * @return An RDF-LinkedHashMap as Metadata2
	 */
	public LinkedHashMap<String, Object> getMetadata2ByToScienceJson(Node n,
			String toscienceJsonContent) {
		try {

			if (toscienceJsonContent == null) {
				play.Logger.debug("toscienceJsonContent is empty");
				return null;
			}

			JsonMapper jsmapper = new JsonMapper();
			jsmapper.node = n;

			play.Logger.debug("Start mapping of toscienceJson to metadata2");

			JSONArray jArr = null;
			JSONObject jObj = null;
			Map<String, Object> rdf = n.getLd2();

			play.Logger.debug("rdf = " + rdf.toString());

			JSONObject toscienceJsonObject = new JSONObject(toscienceJsonContent);

			LinkedHashMap<String, Object> metadata2Map = new LinkedHashMap<>();

			play.Logger.debug(
					"getMetadata2ByToScienceJson() LinkedHashMap has been created");

			if (toscienceJsonObject.has("@context")) {
				jObj = toscienceJsonObject.getJSONObject("@context");
				rdf.put("@context", jObj.toString());
			}

			if (toscienceJsonObject.has("title")) {
				jObj = toscienceJsonObject.getJSONObject("title");
				play.Logger.debug("Found title: " + jObj.toString());
				rdf.put("title", jObj.toString());
			}

			if (toscienceJsonObject.has("language")) {
				jArr = toscienceJsonObject.getJSONArray("language");
				play.Logger.debug("Found language: " + jArr.toString());
				rdf.put("language", jArr.toString());
			}

			rdf.put("accessScheme", "public");
			rdf.put("publishScheme", "public");

			if (toscienceJsonObject.has("funder")) {
				jObj = toscienceJsonObject.getJSONObject("funder");
				play.Logger.debug("Found funder : " + jObj.toString());
				rdf.put("funder", jObj.toString());
			}

			// There is always a ContentType
			jObj = toscienceJsonObject.getJSONArray("contentType");
			rdf.put("contentType", jObj.toString());

			if (toscienceJsonObject.has("isPrimaryTopic")) {
				jObj = toscienceJsonObject.getJSONObject("isPrimaryTopic");
				play.Logger.debug("Found isPrimaryTopic : " + jObj.toString());
				rdf.put("isPrimaryTopic", jObj.toString());
			}

			jsmapper.mapLrmiAgentsToLobid(rdf, toscienceJsonObject, "creator");
			jsmapper.mapLrmiAgentsToLobid(rdf, toscienceJsonObject, "contributor");
			jsmapper.mapLrmiObjectToLobid(rdf, toscienceJsonObject,
					"learningResourceType", "medium");
			jsmapper.mapLrmiObjectToLobid(rdf, toscienceJsonObject, "about",
					"department");

			// Should be mapped?
			if (toscienceJsonObject.has("yearOfCopyright")) {
				jObj = toscienceJsonObject.getJSONObject("yearOfCopyright");
				play.Logger.debug("Found yearOfCopyright : " + jObj.toString());
				rdf.put("yearOfCopyright", jObj.toString());
			}

			// template for Mapping of Array
			if (toscienceJsonObject.has("description")) {
				List<String> descriptions = new ArrayList<>();
				jObj = toscienceJsonObject.get("description");
				if (jObj instanceof java.lang.String) {
					descriptions.add(toscienceJsonObject.getString("description"));
				} else if (jObj instanceof org.json.JSONArray) {
					jArr = toscienceJsonObject.getJSONArray("description");
					for (int i = 0; i < jArr.length(); i++) {
						descriptions.add(jArr.getString(i));
					}
				}
				rdf.put("description", descriptions);
			}

			if (toscienceJsonObject.has("license")) {
				jObj = toscienceJsonObject.getJSONObject("license");
				play.Logger.debug("Found license : " + jObj.toString());
				rdf.put("license", jObj.toString());

			}

			// example for usage of AdHocUriProvider
			if (toscienceJsonObject.has("keywords")) {
				String keyword = null;
				List<Map<String, Object>> subject = new ArrayList<>();
				jArr = toscienceJsonObject.getJSONArray("keywords");
				for (int i = 0; i < jArr.length(); i++) {
					AdHocUriProvider ahu = new AdHocUriProvider();
					Map<String, Object> keywordMap = new LinkedHashMap<>();
					keyword = jArr.getString(i);
					keywordMap.put("prefLabel", keyword);
					keywordMap.put("@id", ahu.getAdhocUri(keyword));
					subject.add(keywordMap);
				}
				rdf.put("subject", subject);
			}

			metadata2Map.put("metadata2", rdf);
			play.Logger.debug("metadata2Map = " + metadata2Map.toString());
			play.Logger.debug("Done mapping of toscienceJson to metadata2.");
			return metadata2Map;
		} catch (Exception e) {
			play.Logger.error("Metadata2 could not be mapped!", e);
			throw new RuntimeException(
					"ToscienceJson could not be mapped to Metadata2", e);
		}

	}
}
