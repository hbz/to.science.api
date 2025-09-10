package helper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import models.Globals;
import models.Node;
import services.KtblService;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * @author adoud
 *
 */
public class Metadata2Helper {

	public static String getQuotedValues(String s) {

		if (s.contains("")) {
			return s.substring(s.toString().indexOf("\"") + 1,
					s.toString().lastIndexOf("\""));
		}
		return s;

	}

	/**
	 * Diese Methode bereinigt den Eingabetext, indem Zeilenumbrueche und
	 * Leerzeichen angepasst werden. Zusaetzlich wird der Inhalt zwischen dem
	 * ersten und dem letzten Anfuehrungszeichen beibehalten
	 * 
	 * @param input
	 * @return
	 */
	public static String cleanString(String input) {

		play.Logger.debug("input-String", input);

		String cleanedString = input.replace("\n", "_n");

		cleanedString = cleanedString.replace("\\", "");

		cleanedString = cleanedString.replace("_n", "\r\n");

		if (cleanedString.startsWith("[")) {
			cleanedString = cleanedString.substring(1);
		}
		if (cleanedString.endsWith("]")) {
			cleanedString = cleanedString.substring(0, cleanedString.length() - 1);
		}
		if (cleanedString.startsWith("\"")) {
			cleanedString = cleanedString.substring(1);
		}
		if (cleanedString.endsWith("\"")) {
			cleanedString = cleanedString.substring(0, cleanedString.length() - 1);
		}
		return cleanedString;
	}

	/**
	 * Method generates a sorted map from the complete JSON and provides it with
	 * data to persist the data stream metadata2
	 * 
	 * @param jo: contains the complete json(to+ktbl)
	 * @param n:current node
	 * @return:a sorted map
	 */
	public static LinkedHashMap<String, Object> getRdfFromTos(JSONObject jo,
			Node n) {

		LinkedHashMap<String, Object> md2Map = new LinkedHashMap<>();
		JSONArray jsArr = null;
		JSONObject jObj = null;

		try {
			AdHocUriProvider ahu = new AdHocUriProvider();
			JsonMapper jsmapper = new JsonMapper();
			Map<String, Object> rdf = n.getLd2();
			jsmapper.node = n;

			if (jo == null || n == null) {
				play.Logger.debug("getRdfFromTos(), JSONObject or node is null ");
				return null;
			}
			if (n.getPid() != null) {
				rdf.put("id", n.getPid());
			}

			if (jo.has("@context")) {
				String context = jo.getString("@context");
				rdf.put("@context", context);
			}

			if (jo.has("language")) {
				List<Map<String, Object>> langList = new ArrayList<>();
				jsArr = jo.getJSONArray("language");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> languageMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					languageMap.put("@id", jObj.getString("@id"));
					languageMap.put("prefLabel", jObj.get("prefLabel"));
					langList.add(languageMap);
				}
				rdf.put("language", langList);
			}

			if (jo.has("title")) {
				Object obj = jo.get("title");
				String title = getQuotedValues(obj.toString());
				rdf.put("title", title);
			}

			if (jo.has("accessScheme")) {
				String accessScheme = jo.getString("accessScheme");
				rdf.put("accessScheme", accessScheme);

			}
			if (jo.has("publishScheme")) {
				String publishScheme = jo.getString("publishScheme");
				rdf.put("publishScheme", publishScheme);
			}

			if (jo.has("contentType")) {
				String contentType = jo.getString("contentType");
				rdf.put("contentType", contentType);
			}

			if (jo.has("license")) {
				List<Map<String, Object>> licenseList = new ArrayList<>();
				jsArr = jo.getJSONArray("license");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> licenseMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					licenseMap.put("@id", jObj.getString("@id"));
					licenseMap.put("prefLabel", jObj.get("prefLabel"));
					licenseList.add(licenseMap);
				}
				rdf.put("license", licenseList);
			}

			if (jo.has("description")) {
				Object obj = jo.get("description");
				String description = getQuotedValues(obj.toString());
				rdf.put("description", cleanString(description));
			}

			if (jo.has("usageManual")) {
				Object obj = jo.get("usageManual");
				String usageManual = cleanString(obj.toString());
				rdf.put("usageManual", usageManual);
			}

			if (jo.has("subject")) {
				List<Map<String, Object>> subjectList = new ArrayList<>();
				jsArr = jo.getJSONArray("subject");
				for (int i = 0; i < jsArr.length(); i++) {
					jObj = jsArr.getJSONObject(i);
					String uri = jObj.getString("@id");
					String label = jObj.get("prefLabel").toString();
					KtblService.checkAndLoadUri(uri, label);
					Map<String, Object> subjectMap = new LinkedHashMap<>();
					subjectMap.put("@id", uri);
					subjectMap.put("prefLabel", label);
					subjectList.add(subjectMap);
				}
				rdf.put("subject", subjectList);
			}

			if (jo.has("medium")) {
				List<Map<String, Object>> mediumList = new ArrayList<>();
				jsArr = jo.getJSONArray("medium");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> mediumMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					mediumMap.put("prefLabel", jObj.get("prefLabel").toString());
					mediumMap.put("@id", jObj.get("@id").toString());
					mediumList.add(mediumMap);
				}
				rdf.put("medium", mediumList);
			}
			if (jo.has("creator")) {
				String uri = null;
				String label = null;
				List<Map<String, Object>> creatorList = new ArrayList<>();
				jsArr = jo.getJSONArray("creator");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> creatorMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					uri = jObj.get("@id").toString();
					label = jObj.get("prefLabel").toString();
					if (uri.length() == 0) {
						uri = ahu.getAdhocUri(label);
					}
					creatorMap.put("prefLabel", label);
					creatorMap.put("@id", uri);
					creatorList.add(creatorMap);
				}
				rdf.put("creator", creatorList);
			}
			if (jo.has("contributor")) {
				String uri = null;
				String label = null;
				List<Map<String, Object>> contributorList = new ArrayList<>();
				jsArr = jo.getJSONArray("contributor");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> contributorrMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					uri = jObj.get("@id").toString();
					label = jObj.get("prefLabel").toString();
					if (uri.length() == 0) {
						uri = ahu.getAdhocUri(label);
					}
					contributorrMap.put("prefLabel", label);
					contributorrMap.put("@id", uri);
					contributorList.add(contributorrMap);
				}
				rdf.put("contributor", contributorList);
			}
			if (jo.has("other")) {
				String uri = null;
				String label = null;
				List<Map<String, Object>> otherList = new ArrayList<>();
				jsArr = jo.getJSONArray("other");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> otherMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					uri = jObj.get("@id").toString();
					label = jObj.get("prefLabel").toString();
					if (uri.length() == 0) {
						uri = ahu.getAdhocUri(label);
					}
					otherMap.put("prefLabel", label);
					otherMap.put("@id", uri);
					otherList.add(otherMap);
				}
				rdf.put("other", otherList);
			}
			if (jo.has("dataOrigin")) {
				List<Map<String, Object>> dataOriginList = new ArrayList<>();
				jsArr = jo.getJSONArray("dataOrigin");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> dataOriginMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					String dataOriginPrefLabel = jObj.get("prefLabel").toString();
					dataOriginMap.put("prefLabel", dataOriginPrefLabel);
					dataOriginMap.put("@id",
							"http://hbz-nrw.de/regal#" + dataOriginPrefLabel);
					dataOriginList.add(dataOriginMap);
				}
				rdf.put("dataOrigin", dataOriginList);
			}
			if (jo.has("fundingId")) {
				List<Map<String, Object>> fundingIdList = new ArrayList<>();
				jsArr = jo.getJSONArray("fundingId");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> languageMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					String uri = jObj.getString("@id");
					String label = jObj.get("prefLabel").toString();
					languageMap.put("@id", uri);
					languageMap.put("prefLabel", label);
					KtblService.checkAndLoadUri(uri, label);
					fundingIdList.add(languageMap);
				}
				rdf.put("fundingId", fundingIdList);
			}
			if (jo.has("fundingProgram")) {
				Object obj = jo.get("fundingProgram");
				String fundingProgram = getQuotedValues(obj.toString());
				rdf.put("fundingProgram", fundingProgram);
			}
			if (jo.has("institution")) {
				List<Map<String, Object>> institutionList = new ArrayList<>();
				jsArr = jo.getJSONArray("institution");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> institutionMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					institutionMap.put("prefLabel", jObj.get("prefLabel").toString());
					institutionMap.put("@id", jObj.get("@id").toString());
					institutionList.add(institutionMap);
				}
				rdf.put("institution", institutionList);
			}

			if (jo.has("rdftype")) {
				List<Map<String, Object>> rdftypeList = new ArrayList<>();
				jsArr = jo.getJSONArray("rdftype");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> rdftypeMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					rdftypeMap.put("prefLabel", jObj.get("prefLabel").toString());
					rdftypeMap.put("@id", "http://hbz-nrw.de/regal#ResearchData");
					rdftypeList.add(rdftypeMap);
				}
				rdf.put("rdftype", rdftypeList);
			}

			if (jo.has("reference")) {
				Object obj = jo.get("reference");
				String reference = getQuotedValues(obj.toString());
				rdf.put("reference", reference);
			}
			if (jo.has("ddc")) {
				List<Map<String, Object>> ddcList = new ArrayList<>();
				jsArr = jo.getJSONArray("ddc");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> ddcMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					ddcMap.put("@id", jObj.getString("@id"));
					ddcMap.put("prefLabel", jObj.get("prefLabel"));
					ddcList.add(ddcMap);
				}
				rdf.put("ddc", ddcList);
			}
			if (jo.has("yearOfCopyright")) {
				Object obj = jo.get("yearOfCopyright");
				String yearOfCopyright = getQuotedValues(obj.toString());
				rdf.put("yearOfCopyright", yearOfCopyright);
			}
			if (jo.has("embargoTime")) {
				Object obj = jo.get("embargoTime");
				String embargoTime = getQuotedValues(obj.toString());
				rdf.put("embargoTime", embargoTime);
			}

			if (jo.has("projectId")) {
				Object obj = jo.get("projectId");
				String projectId = getQuotedValues(obj.toString());
				rdf.put("projectId", projectId);
			}

			if (jo.has("issued")) {
				Object issued = jo.get("issued");
				rdf.put("issued", issued.toString());
			}

			if (jo.has("recordingPeriod")) {
				Object obj = jo.get("recordingPeriod");
				String recordingPeriod = getQuotedValues(obj.toString());
				rdf.put("recordingPeriod", recordingPeriod);
			}

			if (jo.has("associatedDataset")) {
				JSONArray associatedDatasetArray = jo.getJSONArray("associatedDataset");
				List<String> associatedDataset = new ArrayList<>();
				for (int i = 0; i < associatedDatasetArray.length(); i++) {
					associatedDataset.add(associatedDatasetArray.getString(i));
				}
				rdf.put("associatedDataset", associatedDataset);
			}

			// KTBL
			if (jo.has("info")) {
				play.Logger.debug("Found info");
				boolean hasProjectTitle =
						jo.getJSONObject("info").getJSONObject("ktbl").has("project_title");
				if (hasProjectTitle) {
					String project_title = jo.getJSONObject("info").getJSONObject("ktbl")
							.getString("project_title");
					rdf.put("project_title", project_title);
				}

				boolean hasTestDesign =
						jo.getJSONObject("info").getJSONObject("ktbl").has("test_design");
				if (hasTestDesign) {
					String test_design = jo.getJSONObject("info").getJSONObject("ktbl")
							.getString("test_design");
					rdf.put("test_design", test_design);
				}

				boolean hasLivestock_category = jo.getJSONObject("info")
						.getJSONObject("ktbl").has("livestock_category");
				if (hasLivestock_category) {
					String livestock_category = jo.getJSONObject("info")
							.getJSONObject("ktbl").getString("livestock_category");
					rdf.put("livestock_category", livestock_category);
				}

				boolean hasLivestock_production = jo.getJSONObject("info")
						.getJSONObject("ktbl").has("livestock_production");
				if (hasLivestock_production) {
					String livestock_production = jo.getJSONObject("info")
							.getJSONObject("ktbl").getString("livestock_production");
					rdf.put("livestock_production", livestock_production);
				}

				boolean hasVentilation_system = jo.getJSONObject("info")
						.getJSONObject("ktbl").has("ventilation_system");
				if (hasLivestock_production) {
					String ventilation_system = jo.getJSONObject("info")
							.getJSONObject("ktbl").getString("ventilation_system");
					rdf.put("ventilation_system", ventilation_system);
				}

				boolean hasHousing_systems = jo.getJSONObject("info")
						.getJSONObject("ktbl").has("housing_systems");
				if (hasLivestock_production) {
					String housing_systems = jo.getJSONObject("info")
							.getJSONObject("ktbl").getString("housing_systems");
					rdf.put("housing_systems", housing_systems);
				}

				boolean hasAdditionalHousingSystems = jo.getJSONObject("info")
						.getJSONObject("ktbl").has("additional_housing_systems");
				if (hasAdditionalHousingSystems) {
					JSONArray additionalHousingSystemsArray = jo.getJSONObject("info")
							.getJSONObject("ktbl").getJSONArray("additional_housing_systems");
					List<String> additionalHousingSystems = new ArrayList<>();
					for (int i = 0; i < additionalHousingSystemsArray.length(); i++) {
						additionalHousingSystems
								.add(additionalHousingSystemsArray.getString(i));
					}
					rdf.put("additional_housing_systems", additionalHousingSystems);
				}

				boolean hasEmi_measurement_techniques = jo.getJSONObject("info")
						.getJSONObject("ktbl").has("emi_measurement_techniques");
				if (hasEmi_measurement_techniques) {
					JSONArray emi_measurement_techniquesArray = jo.getJSONObject("info")
							.getJSONObject("ktbl").getJSONArray("emi_measurement_techniques");
					List<String> emi_measurement_techniques = new ArrayList<>();
					for (int i = 0; i < emi_measurement_techniquesArray.length(); i++) {
						emi_measurement_techniques
								.add(emi_measurement_techniquesArray.getString(i));
					}
					rdf.put("emi_measurement_techniques", emi_measurement_techniques);
				}
				boolean hasEmissions =
						jo.getJSONObject("info").getJSONObject("ktbl").has("emissions");
				if (hasEmissions) {
					JSONArray emissionsArray = jo.getJSONObject("info")
							.getJSONObject("ktbl").getJSONArray("emissions");
					List<String> emissions = new ArrayList<>();
					for (int i = 0; i < emissionsArray.length(); i++) {
						emissions.add(emissionsArray.getString(i));
					}
					rdf.put("emissions", emissions);
				}

				boolean hasEmission_reduction_methods = jo.getJSONObject("info")
						.getJSONObject("ktbl").has("emission_reduction_methods");
				if (hasEmission_reduction_methods) {
					JSONArray ermArray = jo.getJSONObject("info").getJSONObject("ktbl")
							.getJSONArray("emission_reduction_methods");
					List<String> emission_reduction_methods = new ArrayList<>();
					for (int i = 0; i < ermArray.length(); i++) {
						emission_reduction_methods.add(ermArray.getString(i));
					}
					rdf.put("emission_reduction_methods", emission_reduction_methods);
				}
			}
			md2Map.put("metadata2", rdf);
			play.Logger.debug("md2Map" + md2Map.toString());

		} catch (Exception e) {
			play.Logger.debug("Exception in getRdfFromTos()" + e);
		}
		return md2Map;
	}

}
