package helper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import models.Globals;
import models.Node;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * @author adoud
 *
 */
public class Metadata2Helper {

	public static String getValueBetweenTwoQuotationMarks(String s) {
		String result = null;
		if (s.contains("")) {
			result = s.substring(s.toString().indexOf("\"") + 1,
					s.toString().lastIndexOf("\""));
		}
		return result;

	}

	public static LinkedHashMap<String, Object> getRdfFromToscience(
			JSONObject tosContent, Node n) {
		// should perhaps be mapped : 1-isPrimaryTopic 2-yearOfCopyright
		try {
			JsonMapper jsmapper = new JsonMapper();
			jsmapper.node = n;

			Map<String, Object> rdf = n.getLd2();
			play.Logger.debug("n.getLd2() = " + rdf.toString());

			JSONArray jsArr = null;
			JSONObject jObj = null;
			LinkedHashMap<String, Object> metadata2Map = new LinkedHashMap<>();

			if (tosContent == null) {
				return null;
			}
			if (n.getPid() != null) {
				rdf.put("id", n.getPid());
			}

			if (tosContent.has("@context")) {
				String context = tosContent.getString("@context");
				rdf.put("@context", context);
			}

			if (tosContent.has("language")) {
				List<Map<String, Object>> langList = new ArrayList<>();
				jsArr = tosContent.getJSONArray("language");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> languageMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					languageMap.put("@id", jObj.getString("@id"));
					languageMap.put("prefLabel", jObj.get("prefLabel"));
					langList.add(languageMap);
				}
				rdf.put("language", langList);
			}

			if (tosContent.has("title")) {
				Object obj = tosContent.get("title");
				String title = getValueBetweenTwoQuotationMarks(obj.toString());
				rdf.put("title", title);
			}

			if (tosContent.has("accessScheme")) {
				String accessScheme = tosContent.getString("accessScheme");
				rdf.put("accessScheme", accessScheme);

			}
			if (tosContent.has("publishScheme")) {
				String publishScheme = tosContent.getString("publishScheme");
				rdf.put("publishScheme", publishScheme);
			}

			if (tosContent.has("contentType")) {
				String contentType = tosContent.getString("contentType");
				rdf.put("contentType", contentType);
			}

			if (tosContent.has("license")) {
				List<Map<String, Object>> licenseList = new ArrayList<>();
				jsArr = tosContent.getJSONArray("license");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> licenseMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					licenseMap.put("@id", jObj.getString("@id"));
					licenseMap.put("prefLabel", jObj.get("prefLabel"));
					licenseList.add(licenseMap);
				}
				rdf.put("license", licenseList);
			}

			if (tosContent.has("description")) {
				Object obj = tosContent.get("description");
				String description = getValueBetweenTwoQuotationMarks(obj.toString());
				rdf.put("description", description);
			}
			if (tosContent.has("subject")) {
				List<Map<String, Object>> subjectList = new ArrayList<>();
				jsArr = tosContent.getJSONArray("subject");
				for (int i = 0; i < jsArr.length(); i++) {
					// AdHocUriProvider ahu = new AdHocUriProvider();
					Map<String, Object> subjectMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					subjectMap.put("@id", jObj.getString("@id"));
					subjectMap.put("prefLabel", jObj.get("prefLabel"));
					subjectList.add(subjectMap);
				}
				rdf.put("subject", subjectList);
			}

			if (tosContent.has("medium")) {
				List<Map<String, Object>> mediumList = new ArrayList<>();
				jsArr = tosContent.getJSONArray("medium");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> mediumMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					mediumMap.put("prefLabel", jObj.get("prefLabel").toString());
					mediumMap.put("@id", jObj.get("@id").toString());
					mediumList.add(mediumMap);
				}
				rdf.put("medium", mediumList);
			}
			if (tosContent.has("creator")) {
				List<Map<String, Object>> creatorList = new ArrayList<>();
				jsArr = tosContent.getJSONArray("creator");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> creatorMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					creatorMap.put("prefLabel", jObj.get("prefLabel").toString());
					creatorMap.put("@id", jObj.get("@id").toString());
					creatorList.add(creatorMap);
				}
				rdf.put("creator", creatorList);
			}
			if (tosContent.has("contributor")) {
				List<Map<String, Object>> contributorList = new ArrayList<>();
				jsArr = tosContent.getJSONArray("contributor");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> contributorrMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					contributorrMap.put("prefLabel", jObj.get("prefLabel").toString());
					contributorrMap.put("@id", jObj.get("@id").toString());
					contributorList.add(contributorrMap);
				}

				rdf.put("contributor", contributorList);
			}
			if (tosContent.has("other")) {
				List<Map<String, Object>> otherList = new ArrayList<>();
				jsArr = tosContent.getJSONArray("other");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> otherMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					otherMap.put("prefLabel", jObj.get("prefLabel").toString());
					otherMap.put("@id", jObj.get("@id").toString());
					otherList.add(otherMap);
				}
				rdf.put("other", otherList);
			}
			if (tosContent.has("dataOrigin")) {
				List<Map<String, Object>> dataOriginList = new ArrayList<>();
				jsArr = tosContent.getJSONArray("dataOrigin");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> dataOriginMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					String dataOriginPrefLabel = jObj.get("prefLabel").toString();
					dataOriginMap.put("prefLabel", dataOriginPrefLabel);
					dataOriginMap.put("@id",
							"http://hbz-nrw.de/regal#" + dataOriginPrefLabel);
					dataOriginList.add(dataOriginMap);

				}
				play.Logger.debug("dataOriginList=" + dataOriginList.toString());
				rdf.put("dataOrigin", dataOriginList);
			}
			if (tosContent.has("fundingId")) {
				Object obj = tosContent.get("fundingId");
				String fundingId = getValueBetweenTwoQuotationMarks(obj.toString());
				rdf.put("fundingId", fundingId);
			}
			if (tosContent.has("fundingProgram")) {
				Object obj = tosContent.get("fundingProgram");
				String fundingProgram =
						getValueBetweenTwoQuotationMarks(obj.toString());
				rdf.put("fundingProgram", fundingProgram);
			}
			if (tosContent.has("institution")) {
				List<Map<String, Object>> institutionList = new ArrayList<>();
				jsArr = tosContent.getJSONArray("institution");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> institutionMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					institutionMap.put("prefLabel", jObj.get("prefLabel").toString());
					institutionMap.put("@id", jObj.get("@id").toString());

					institutionList.add(institutionMap);
				}
				rdf.put("institution", institutionList);
			}

			if (tosContent.has("rdftype")) {
				List<Map<String, Object>> rdftypeList = new ArrayList<>();
				jsArr = tosContent.getJSONArray("rdftype");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> rdftypeMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					rdftypeMap.put("prefLabel", jObj.get("prefLabel").toString());
					rdftypeMap.put("@id", "http://hbz-nrw.de/regal#ResearchData");

					rdftypeList.add(rdftypeMap);
				}
				rdf.put("rdftype", rdftypeList);
			}

			if (tosContent.has("reference")) {
				Object obj = tosContent.get("reference");
				String reference = getValueBetweenTwoQuotationMarks(obj.toString());
				rdf.put("reference", reference);
			}
			if (tosContent.has("ddc")) {
				List<Map<String, Object>> ddcList = new ArrayList<>();
				jsArr = tosContent.getJSONArray("ddc");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> ddcMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					ddcMap.put("@id", jObj.getString("@id"));
					ddcMap.put("prefLabel", jObj.get("prefLabel"));
					ddcList.add(ddcMap);
				}
				rdf.put("ddc", ddcList);
			}
			if (tosContent.has("yearOfCopyright")) {
				Object obj = tosContent.get("yearOfCopyright");
				String yearOfCopyright =
						getValueBetweenTwoQuotationMarks(obj.toString());
				rdf.put("yearOfCopyright", yearOfCopyright);
			}
			if (tosContent.has("embargoTime")) {
				Object obj = tosContent.get("embargoTime");
				String embargoTime = getValueBetweenTwoQuotationMarks(obj.toString());
				rdf.put("embargoTime", embargoTime);
			}

			if (tosContent.has("projectId")) {
				Object obj = tosContent.get("projectId");
				String projectId = getValueBetweenTwoQuotationMarks(obj.toString());
				rdf.put("projectId", projectId);
			}

			if (tosContent.has("issued")) {
				Object obj = tosContent.get("issued");
				String issued = getValueBetweenTwoQuotationMarks(obj.toString());
				play.Logger.debug("issued=" + issued);
				rdf.put("issued", issued);
			}

			// rdf.put("@context", Globals.protocol + Globals.server +
			// "/context.json");
			metadata2Map.put("metadata2", rdf);
			return metadata2Map;
		} catch (Exception e) {
			throw new RuntimeException(
					"ToscienceJson could not be mapped to Metadata2", e);
		}

	}

}
