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
	 * This method get metadata in lobid2 format (if exists) and map it with data
	 * from the new data stream toscienceJson
	 * 
	 * @param toscienceJsonContent: the content from the new data stream toscience
	 * @return An RDF-LinkedHashMap as Metadata2
	 */
	public static LinkedHashMap<String, Object> getLd2Lobidify2ToscienceJson(
			Node n, JSONObject toscienceJsonContent) {
		// should perhaps be mapped : 1-isPrimaryTopic 2-yearOfCopyright
		try {

			JsonMapper jsmapper = new JsonMapper();
			jsmapper.node = n;
			JSONArray jsArr = null;
			JSONObject jObj = null;
			LinkedHashMap<String, Object> metadata2Map = new LinkedHashMap<>();

			if (toscienceJsonContent == null) {
				play.Logger.debug("toscienceJsonContent is empty");
				return null;
			}
			play.Logger.debug("Start mapping of toscienceJson to metadata2");

			Map<String, Object> rdf = n.getLd2();

			play.Logger.debug("rdf = " + rdf.toString());

			play.Logger.debug(
					"getMetadata2ByToScienceJson() LinkedHashMap has been created");

			if (toscienceJsonContent.has("@context")) {
				String context = toscienceJsonContent.getString("@context");
				play.Logger.debug("Found context: " + context);
				rdf.put("@context", context);
			}

			if (toscienceJsonContent.has("language")) {
				List<Map<String, Object>> langList = new ArrayList<>();
				jsArr = toscienceJsonContent.getJSONArray("language");
				play.Logger.debug("Found context: " + jsArr.toString());

				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> languageMap = new LinkedHashMap<>();
					JSONObject jo = jsArr.getJSONObject(i);
					languageMap.put("prefLabel", jo.getString("prefLabel"));
					languageMap.put("@id", jo.getString("@id"));
					langList.add(languageMap);

				}
				rdf.put("language", langList);
			}

			if (toscienceJsonContent.has("title")) {
				jsArr = toscienceJsonContent.getJSONArray("title");
				String title = getValueBetweenTwoQuotationMarks(jsArr.toString());
				rdf.put("title", title);
				play.Logger.debug("Found title: " + title);
			}

			rdf.put("accessScheme", "public");
			rdf.put("publishScheme", "public");

			if (toscienceJsonContent.has("contentType")) {
				String contentType = toscienceJsonContent.getString("contentType");
				play.Logger.debug("Found contentType : " + contentType);
				rdf.put("contentType", contentType);
			}

			if (toscienceJsonContent.has("funder")) {
				Map<String, Object> funderMap = new LinkedHashMap<>();
				jObj = toscienceJsonContent.getJSONObject("funder");
				funderMap.put("@id", jObj.getString("@id"));
				funderMap.put("prefLabel", jObj.get("prefLabel"));
				play.Logger.debug("Found funder : " + jObj.toString());
				rdf.put("funder", funderMap);
			}

			if (toscienceJsonContent.has("license")) {
				Map<String, Object> licenseMap = new LinkedHashMap<>();
				jObj = toscienceJsonContent.getJSONObject("license");
				play.Logger.debug("Found license : " + jObj.getString("@id"));
				licenseMap.put("@id", jObj.getString("@id"));
				licenseMap.put("prefLabel", jObj.get("prefLabel"));
				rdf.put("license", licenseMap);
			}

			if (toscienceJsonContent.has("description")) {
				Object obj = toscienceJsonContent.get("description");
				String description = getValueBetweenTwoQuotationMarks(obj.toString());
				play.Logger.debug("Found description : " + obj.toString());
				rdf.put("description", description);
			}

			if (toscienceJsonContent.has("subject")) {
				jsArr = toscienceJsonContent.getJSONArray("subject");
				List<Map<String, Object>> keyWordList = new ArrayList<>();
				for (int i = 0; i < jsArr.length(); i++) {
					AdHocUriProvider ahu = new AdHocUriProvider();
					Map<String, Object> keywordMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					play.Logger.debug("Found keyword: " + jObj.toString());
					keywordMap.put("prefLabel", jObj.get("prefLabel").toString());
					keywordMap.put("@id",
							ahu.getAdhocUri(jObj.get("prefLabel").toString()));
					keyWordList.add(keywordMap);
				}
				rdf.put("subject", keyWordList);
			}

			if (toscienceJsonContent.has("medium")) {
				jsArr = toscienceJsonContent.getJSONArray("medium");
				List<Map<String, Object>> mediumList = new ArrayList<>();
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> mediumMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					play.Logger.debug("Found medium : " + jObj.toString());
					mediumMap.put("prefLabel", jObj.get("prefLabel").toString());
					mediumMap.put("@id", jObj.get("@id").toString());
					mediumList.add(mediumMap);
				}
				rdf.put("medium", mediumList);
			}

			if (toscienceJsonContent.has("department")) {
				jsArr = toscienceJsonContent.getJSONArray("department");
				List<Map<String, Object>> departmentList = new ArrayList<>();
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> departmentMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					play.Logger.debug("Found department : " + jObj.toString());
					departmentMap.put("prefLabel", jObj.get("prefLabel").toString());
					departmentMap.put("@id", jObj.get("@id").toString());
					departmentList.add(departmentMap);
				}
				rdf.put("department", departmentList);
			}

			if (toscienceJsonContent.has("creator")) {
				mapToscienceAgentsToLobid(rdf, toscienceJsonContent, "creator");
				play.Logger.debug("Metadata2Helper(),after Mapping of creator. rdf = "
						+ rdf.toString());
			}
			if (toscienceJsonContent.has("contributor")) {
				mapToscienceAgentsToLobid(rdf, toscienceJsonContent, "contributor");
				play.Logger
						.debug("Metadata2Helper(),after Mapping of contributor. rdf = "
								+ rdf.toString());
			}

			// rdf.put("language", "ger");
			play.Logger.debug("rdf = " + rdf.toString());

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

	/**
	 * This method maps creator and contributors from tosciencejson to ld2
	 * 
	 * @param rdf
	 * @param toscienceJSONObject
	 * @param agentType: creator | contributor
	 */
	public static void mapToscienceAgentsToLobid(Map<String, Object> rdf,
			JSONObject toscienceJSONObject, String agentType) {
		String academicDegree = null;
		JSONArray jsArr = null;
		String prefLabelOrganisation = null;
		String affiliationId = null;
		String affiliationType = null;

		if (toscienceJSONObject.has(agentType)) {
			ArrayList<Map<String, Object>> agents = new ArrayList<>();
			ArrayList<String> agentAcademicDegree = new ArrayList<>();
			ArrayList<String> agentAffiliation = new ArrayList<>();
			ArrayList<String> oerAgent = new ArrayList<>();

			try {
				jsArr = toscienceJSONObject.getJSONArray(agentType);
				for (int i = 0; i < jsArr.length(); i++) {

					Map<String, Object> agentMap = new LinkedHashMap<>();
					JSONObject agentObject = jsArr.getJSONObject(i);

					StringBuffer agentStr = new StringBuffer();

					if (agentObject.has("academicDegree")) {
						academicDegree = agentObject.getString("academicDegree");
						agentMap.put("academicDegree", academicDegree);
						agentAcademicDegree.add(academicDegree);

					}

					if (agentObject.has("affiliation")) {
						Map<String, Object> affiliationMap = new LinkedHashMap<>();
						JSONObject affi = agentObject.getJSONObject("affiliation");

						prefLabelOrganisation = affi.getString("prefLabel");
						affiliationId = affi.getString("@id");
						affiliationType = affi.getString("type");

						agentAffiliation.add(prefLabelOrganisation);

						affiliationMap.put("@id", affiliationId);
						affiliationMap.put("type", affiliationType);
						affiliationMap.put("prefLabel", prefLabelOrganisation);
						agentMap.put("affiliation", affiliationMap);

					}
					agents.add(agentMap);
					agentStr.append(agentType + " " + Integer.toString(i + 1) + ": ");
					agentStr.append(academicDegree);
					agentStr.append(prefLabelOrganisation);
					oerAgent.add(agentStr.toString());
				}

				rdf.put(agentType, agents);
				rdf.put("oerAgent", oerAgent);
				rdf.put(agentType + "Affiliation", agentAffiliation);
				rdf.put(agentType + "AcademicDegree", agentAcademicDegree);

			} catch (Exception e) {
				play.Logger.error(e.getMessage());
				throw new RuntimeException(e);
			}
		}

	}

	/**
	 * This method gets the string value between two quotation marks("").
	 * 
	 * @param s: For example. s= ["FunderTestByNameUploadFormular"]
	 * @return: For example FunderTestByNameUploadFormular
	 */
	public static String getValueBetweenTwoQuotationMarks(String s) {
		String result = null;
		if (s.contains("")) {
			result = s.substring(s.toString().indexOf("\"") + 1,
					s.toString().lastIndexOf("\""));
		}
		return result;

	}

}
