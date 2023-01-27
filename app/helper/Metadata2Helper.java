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
				String context = toscienceJsonObject.getString("@context");
				play.Logger.debug("Found context: " + context);
				rdf.put("@context", context);
			}

			if (toscienceJsonObject.has("title")) {
				jArr = toscienceJsonObject.getJSONArray("title");
				play.Logger.debug("Found title: " + jArr.toString());
				rdf.put("title", jArr.toString());
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

			// There is always a ContentType?
			if (toscienceJsonObject.has("contentType")) {
				String contentType = toscienceJsonObject.getString("contentType");
				play.Logger.debug("Found contentType : " + contentType);
				rdf.put("contentType", contentType);
			}
			if (toscienceJsonObject.has("isPrimaryTopic")) {
				String isPrimaryTopic = toscienceJsonObject.getString("isPrimaryTopic");
				play.Logger.debug("Found isPrimaryTopic : " + isPrimaryTopic);
				rdf.put("isPrimaryTopic", isPrimaryTopic);
			}

			play.Logger.debug("creator will be mapped");
			if (toscienceJsonObject.has("creator")) {
				jArr = toscienceJsonObject.getJSONArray("creator");
				play.Logger.debug("Found creator : " + jArr.toString());
				rdf.put("creator", jArr.toString());
			}

			play.Logger.debug("contributor will be mapped");
			if (toscienceJsonObject.has("contributor")) {
				jArr = toscienceJsonObject.getJSONArray("contributor");
				play.Logger.debug("Found contributor : " + jArr.toString());
				rdf.put("contributor", jArr.toString());
			}

			play.Logger.debug("medium will be mapped");
			if (toscienceJsonObject.has("medium")) {
				jArr = toscienceJsonObject.getJSONArray("medium");
				play.Logger.debug("Found medium : " + jArr.toString());
				rdf.put("medium", jArr.toString());
			}

			play.Logger.debug("department will be mapped");
			if (toscienceJsonObject.has("department")) {
				jArr = toscienceJsonObject.getJSONArray("department");
				play.Logger.debug("Found department : " + jArr.toString());
				rdf.put("department", jArr.toString());
			}
			// Should be mapped?
			if (toscienceJsonObject.has("yearOfCopyright")) {
				jArr = toscienceJsonObject.getJSONArray("yearOfCopyright");
				play.Logger.debug("Found yearOfCopyright : " + jArr.toString());
				rdf.put("yearOfCopyright", jArr.toString());
			}

			if (toscienceJsonObject.has("license")) {
				jObj = toscienceJsonObject.getJSONObject("license");
				play.Logger.debug("Found license : " + jObj.toString());
				rdf.put("license", jObj.toString());

			}

			// example for usage of AdHocUriProvider
			play.Logger.debug("keywords will be mapped");
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
