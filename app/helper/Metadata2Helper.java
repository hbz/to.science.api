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

	public static String getValueBetweenTwoQuotationMarks(String s) {
		String result = null;
		if (s.contains("")) {
			result = s.substring(s.toString().indexOf("\"") + 1,
					s.toString().lastIndexOf("\""));
		}
		return result;

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

		// "\" wird aus dem String entfernt
		play.Logger.debug("input-String", input);

		String cleanedString = input.replace("\n", "_n");

		cleanedString = cleanedString.replace("\\", "");

		// "\n\n" wird aus dem String entfernt
		// cleanedString = cleanedString.replace("\n\n", "");

		// "\n" wird aus dem String entfernt
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

	public static LinkedHashMap<String, Object> getRdfFromToscience(
			JSONObject tosAndKtblContent, Node n) {
		try {
			play.Logger.debug(
					"getRdfFromToscience(), tosAndKtblContent= " + tosAndKtblContent);

			AdHocUriProvider ahu = new AdHocUriProvider();
			JsonMapper jsmapper = new JsonMapper();
			jsmapper.node = n;

			Map<String, Object> rdf = n.getLd2();
			play.Logger.debug("n.getLd2() = " + rdf.toString());

			JSONArray jsArr = null;
			JSONObject jObj = null;
			LinkedHashMap<String, Object> metadata2Map = new LinkedHashMap<>();

			if (tosAndKtblContent == null) {
				return null;
			}
			if (n.getPid() != null) {
				rdf.put("id", n.getPid());
			}

			if (tosAndKtblContent.has("@context")) {
				String context = tosAndKtblContent.getString("@context");
				rdf.put("@context", context);
			}

			if (tosAndKtblContent.has("language")) {
				List<Map<String, Object>> langList = new ArrayList<>();
				jsArr = tosAndKtblContent.getJSONArray("language");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> languageMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					languageMap.put("@id", jObj.getString("@id"));
					languageMap.put("prefLabel", jObj.get("prefLabel"));
					langList.add(languageMap);
				}
				rdf.put("language", langList);
			}

			if (tosAndKtblContent.has("title")) {
				Object obj = tosAndKtblContent.get("title");
				String title = getValueBetweenTwoQuotationMarks(obj.toString());
				rdf.put("title", title);
			}

			if (tosAndKtblContent.has("accessScheme")) {
				String accessScheme = tosAndKtblContent.getString("accessScheme");
				rdf.put("accessScheme", accessScheme);

			}
			if (tosAndKtblContent.has("publishScheme")) {
				String publishScheme = tosAndKtblContent.getString("publishScheme");
				rdf.put("publishScheme", publishScheme);
			}

			if (tosAndKtblContent.has("contentType")) {
				String contentType = tosAndKtblContent.getString("contentType");
				rdf.put("contentType", contentType);
			}

			if (tosAndKtblContent.has("license")) {
				List<Map<String, Object>> licenseList = new ArrayList<>();
				jsArr = tosAndKtblContent.getJSONArray("license");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> licenseMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					licenseMap.put("@id", jObj.getString("@id"));
					licenseMap.put("prefLabel", jObj.get("prefLabel"));
					licenseList.add(licenseMap);
				}
				rdf.put("license", licenseList);
			}

			if (tosAndKtblContent.has("description")) {
				Object obj = tosAndKtblContent.get("description");
				String description = getValueBetweenTwoQuotationMarks(obj.toString());

				rdf.put("description", cleanString(description));
			}

			if (tosAndKtblContent.has("usageManual")) {
				Object obj = tosAndKtblContent.get("usageManual");
				String usageManual = cleanString(obj.toString());
				rdf.put("usageManual", usageManual);
			}

			if (tosAndKtblContent.has("subject")) {
				List<Map<String, Object>> subjectList = new ArrayList<>();
				jsArr = tosAndKtblContent.getJSONArray("subject");
				for (int i = 0; i < jsArr.length(); i++) {
					jObj = jsArr.getJSONObject(i);
					String uri = jObj.getString("@id");
					String label = jObj.get("prefLabel").toString();
					play.Logger.debug("uri=" + uri + " ,label=" + label);
					KtblService.checkAndLoadUri(uri, label);
					Map<String, Object> subjectMap = new LinkedHashMap<>();
					subjectMap.put("@id", uri);
					subjectMap.put("prefLabel", label);
					subjectList.add(subjectMap);
				}
				rdf.put("subject", subjectList);
			}

			if (tosAndKtblContent.has("medium")) {
				List<Map<String, Object>> mediumList = new ArrayList<>();
				jsArr = tosAndKtblContent.getJSONArray("medium");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> mediumMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					mediumMap.put("prefLabel", jObj.get("prefLabel").toString());
					mediumMap.put("@id", jObj.get("@id").toString());
					mediumList.add(mediumMap);
				}
				rdf.put("medium", mediumList);
			}
			if (tosAndKtblContent.has("creator")) {
				String uri = null;
				String label = null;
				List<Map<String, Object>> creatorList = new ArrayList<>();
				jsArr = tosAndKtblContent.getJSONArray("creator");
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
			if (tosAndKtblContent.has("contributor")) {
				String uri = null;
				String label = null;
				List<Map<String, Object>> contributorList = new ArrayList<>();
				jsArr = tosAndKtblContent.getJSONArray("contributor");
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
			if (tosAndKtblContent.has("other")) {
				String uri = null;
				String label = null;
				List<Map<String, Object>> otherList = new ArrayList<>();
				jsArr = tosAndKtblContent.getJSONArray("other");
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
			if (tosAndKtblContent.has("dataOrigin")) {
				List<Map<String, Object>> dataOriginList = new ArrayList<>();
				jsArr = tosAndKtblContent.getJSONArray("dataOrigin");
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
			if (tosAndKtblContent.has("fundingId")) {
				List<Map<String, Object>> fundingIdList = new ArrayList<>();
				jsArr = tosAndKtblContent.getJSONArray("fundingId");
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
			if (tosAndKtblContent.has("fundingProgram")) {
				Object obj = tosAndKtblContent.get("fundingProgram");
				String fundingProgram =
						getValueBetweenTwoQuotationMarks(obj.toString());
				rdf.put("fundingProgram", fundingProgram);
			}
			if (tosAndKtblContent.has("institution")) {
				List<Map<String, Object>> institutionList = new ArrayList<>();
				jsArr = tosAndKtblContent.getJSONArray("institution");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> institutionMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					institutionMap.put("prefLabel", jObj.get("prefLabel").toString());
					institutionMap.put("@id", jObj.get("@id").toString());

					institutionList.add(institutionMap);
				}
				play.Logger.debug("institutionList=" + institutionList.toString());
				rdf.put("institution", institutionList);
			}

			if (tosAndKtblContent.has("rdftype")) {
				List<Map<String, Object>> rdftypeList = new ArrayList<>();
				jsArr = tosAndKtblContent.getJSONArray("rdftype");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> rdftypeMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					rdftypeMap.put("prefLabel", jObj.get("prefLabel").toString());
					rdftypeMap.put("@id", "http://hbz-nrw.de/regal#ResearchData");

					rdftypeList.add(rdftypeMap);
				}
				play.Logger.debug("rdftypeList = " + rdftypeList.toString());
				rdf.put("rdftype", rdftypeList);
			}

			if (tosAndKtblContent.has("reference")) {
				Object obj = tosAndKtblContent.get("reference");
				String reference = getValueBetweenTwoQuotationMarks(obj.toString());
				rdf.put("reference", reference);
			}
			if (tosAndKtblContent.has("ddc")) {
				List<Map<String, Object>> ddcList = new ArrayList<>();
				jsArr = tosAndKtblContent.getJSONArray("ddc");
				for (int i = 0; i < jsArr.length(); i++) {
					Map<String, Object> ddcMap = new LinkedHashMap<>();
					jObj = jsArr.getJSONObject(i);
					ddcMap.put("@id", jObj.getString("@id"));
					ddcMap.put("prefLabel", jObj.get("prefLabel"));
					ddcList.add(ddcMap);
				}
				rdf.put("ddc", ddcList);
			}
			if (tosAndKtblContent.has("yearOfCopyright")) {
				Object obj = tosAndKtblContent.get("yearOfCopyright");
				String yearOfCopyright =
						getValueBetweenTwoQuotationMarks(obj.toString());
				rdf.put("yearOfCopyright", yearOfCopyright);
			}
			if (tosAndKtblContent.has("embargoTime")) {
				Object obj = tosAndKtblContent.get("embargoTime");
				String embargoTime = getValueBetweenTwoQuotationMarks(obj.toString());
				rdf.put("embargoTime", embargoTime);
			}

			if (tosAndKtblContent.has("projectId")) {
				Object obj = tosAndKtblContent.get("projectId");
				String projectId = getValueBetweenTwoQuotationMarks(obj.toString());
				rdf.put("projectId", projectId);
			}

			if (tosAndKtblContent.has("issued")) {
				Object issued = tosAndKtblContent.get("issued");
				play.Logger.debug("issued=" + issued.toString());
				rdf.put("issued", issued.toString());
			}

			if (tosAndKtblContent.has("recordingPeriod")) {
				Object obj = tosAndKtblContent.get("recordingPeriod");
				String recordingPeriod =
						getValueBetweenTwoQuotationMarks(obj.toString());
				rdf.put("recordingPeriod", recordingPeriod);
			}

			if (tosAndKtblContent.has("associatedDataset")) {
				JSONArray associatedDatasetArray =
						tosAndKtblContent.getJSONArray("associatedDataset");
				List<String> associatedDataset = new ArrayList<>();
				for (int i = 0; i < associatedDatasetArray.length(); i++) {
					associatedDataset.add(associatedDatasetArray.getString(i));
				}
				rdf.put("associatedDataset", associatedDataset);
			}

			// KTBL-Teil
			if (tosAndKtblContent.has("info")) {
				play.Logger.debug("Found info");
				// project_title
				boolean hasProjectTitle = tosAndKtblContent.getJSONObject("info")
						.getJSONObject("ktbl").has("project_title");
				play.Logger.debug("hasProjectTitle=" + hasProjectTitle);
				if (hasProjectTitle) {
					String project_title = tosAndKtblContent.getJSONObject("info")
							.getJSONObject("ktbl").getString("project_title");
					rdf.put("project_title", project_title);
				}

				// test_design
				boolean hasTestDesign = tosAndKtblContent.getJSONObject("info")
						.getJSONObject("ktbl").has("test_design");
				play.Logger.debug("hasTestDesign=" + hasTestDesign);
				if (hasTestDesign) {
					String test_design = tosAndKtblContent.getJSONObject("info")
							.getJSONObject("ktbl").getString("test_design");
					rdf.put("test_design", test_design);
				}

				// livestock_category
				boolean hasLivestock_category = tosAndKtblContent.getJSONObject("info")
						.getJSONObject("ktbl").has("livestock_category");
				play.Logger.debug("hasLivestock_category=" + hasLivestock_category);
				if (hasLivestock_category) {
					String livestock_category = tosAndKtblContent.getJSONObject("info")
							.getJSONObject("ktbl").getString("livestock_category");
					rdf.put("livestock_category", livestock_category);
				}

				// livestock_production
				boolean hasLivestock_production =
						tosAndKtblContent.getJSONObject("info").getJSONObject("ktbl")
								.has("livestock_production");
				play.Logger.debug("hasLivestock_production=" + hasLivestock_production);
				if (hasLivestock_production) {
					String livestock_production = tosAndKtblContent.getJSONObject("info")
							.getJSONObject("ktbl").getString("livestock_production");
					rdf.put("livestock_production", livestock_production);
				}

				// ventilation_system
				boolean hasVentilation_system = tosAndKtblContent.getJSONObject("info")
						.getJSONObject("ktbl").has("ventilation_system");
				play.Logger.debug("hasVentilation_system=" + hasVentilation_system);
				if (hasLivestock_production) {
					String ventilation_system = tosAndKtblContent.getJSONObject("info")
							.getJSONObject("ktbl").getString("ventilation_system");
					rdf.put("ventilation_system", ventilation_system);
				}

				// housing_systems
				boolean hasHousing_systems = tosAndKtblContent.getJSONObject("info")
						.getJSONObject("ktbl").has("housing_systems");
				play.Logger.debug("hasHousing_systems=" + hasHousing_systems);
				if (hasLivestock_production) {
					String housing_systems = tosAndKtblContent.getJSONObject("info")
							.getJSONObject("ktbl").getString("housing_systems");
					rdf.put("housing_systems", housing_systems);
				}

				// additional_housing_systems
				boolean hasAdditionalHousingSystems =
						tosAndKtblContent.getJSONObject("info").getJSONObject("ktbl")
								.has("additional_housing_systems");
				if (hasAdditionalHousingSystems) {
					JSONArray additionalHousingSystemsArray =
							tosAndKtblContent.getJSONObject("info").getJSONObject("ktbl")
									.getJSONArray("additional_housing_systems");
					List<String> additionalHousingSystems = new ArrayList<>();
					for (int i = 0; i < additionalHousingSystemsArray.length(); i++) {
						additionalHousingSystems
								.add(additionalHousingSystemsArray.getString(i));
					}
					rdf.put("additional_housing_systems", additionalHousingSystems);
				}

				// emi_measurement_techniques
				boolean hasEmi_measurement_techniques =
						tosAndKtblContent.getJSONObject("info").getJSONObject("ktbl")
								.has("emi_measurement_techniques");
				if (hasEmi_measurement_techniques) {
					JSONArray emi_measurement_techniquesArray =
							tosAndKtblContent.getJSONObject("info").getJSONObject("ktbl")
									.getJSONArray("emi_measurement_techniques");
					List<String> emi_measurement_techniques = new ArrayList<>();
					for (int i = 0; i < emi_measurement_techniquesArray.length(); i++) {
						emi_measurement_techniques
								.add(emi_measurement_techniquesArray.getString(i));
					}
					rdf.put("emi_measurement_techniques", emi_measurement_techniques);
				}
				// emissions
				boolean hasEmissions = tosAndKtblContent.getJSONObject("info")
						.getJSONObject("ktbl").has("emissions");
				if (hasEmissions) {
					JSONArray emissionsArray = tosAndKtblContent.getJSONObject("info")
							.getJSONObject("ktbl").getJSONArray("emissions");
					List<String> emissions = new ArrayList<>();
					for (int i = 0; i < emissionsArray.length(); i++) {
						emissions.add(emissionsArray.getString(i));
					}
					rdf.put("emissions", emissions);
				}

				// emission_reduction_methods
				boolean hasEmission_reduction_methods =
						tosAndKtblContent.getJSONObject("info").getJSONObject("ktbl")
								.has("emission_reduction_methods");
				if (hasEmission_reduction_methods) {
					JSONArray ermArray = tosAndKtblContent.getJSONObject("info")
							.getJSONObject("ktbl").getJSONArray("emission_reduction_methods");
					List<String> emission_reduction_methods = new ArrayList<>();
					for (int i = 0; i < ermArray.length(); i++) {
						emission_reduction_methods.add(ermArray.getString(i));
					}
					rdf.put("emission_reduction_methods", emission_reduction_methods);
				}
			}
			metadata2Map.put("metadata2", rdf);
			play.Logger.debug(
					"getRdfFromToscience.metadata2Map()" + metadata2Map.toString());
			return metadata2Map;
		} catch (Exception e) {
			throw new RuntimeException(
					"ToscienceJson could not be mapped to Metadata2", e);
		}

	}

}
