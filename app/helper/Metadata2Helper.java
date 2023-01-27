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

			JsonMapper jsmapper = new JsonMapper();
			jsmapper.node = n;

			play.Logger.debug("Start mapping of toscienceJson to metadata2");

			JSONArray jArr = null;
			JSONObject jObj = null;
			Object myObj = null;
			Map<String, Object> rdf = n.getLd2();

			JSONObject toscienceJsonObject = new JSONObject(toscienceJsonContent);
			LinkedHashMap<String, Object> metadata2Map = new LinkedHashMap();

			if (toscienceJsonObject.has("@context")) {
				jArr = toscienceJsonObject.getJSONArray("@context");
				play.Logger.debug("Found context: " + jArr.getString(0));
				rdf.put("@context", jArr.getString(0));
				jObj = jArr.getJSONObject(1);
				String language = jObj.getString("@language");
				play.Logger.debug("Found language: " + language);

				// Mapping of Language with Locale.class as helper
				Map<String, Object> languageMap = new LinkedHashMap<>();
				if (language != null && !language.trim().isEmpty()) {
					if (language.length() == 2) {
						Locale loc = Locale.forLanguageTag(language);
						languageMap.put("@id", "http://id.loc.gov/vocabulary/iso639-2/"
								+ loc.getISO3Language());
					} else if (language.length() == 3) {
						// vermutlich ISO639-2
						languageMap.put("@id",
								"http://id.loc.gov/vocabulary/iso639-2/" + language);
					} else {
						play.Logger.warn(
								"Unbekanntes Vokabluar für Sprachencode! Code=" + language);
					}
				}
				List<Map<String, Object>> languages = new ArrayList<>();
				languages.add(languageMap);
				rdf.put("language", languages);
			}

			rdf.put("accessScheme", "public");
			rdf.put("publishScheme", "public");

			jArr = toscienceJsonObject.getJSONArray("type");
			rdf.put("contentType", jArr.getString(0));

			List<String> names = new ArrayList<>();
			names.add(toscienceJsonObject.getString("name"));
			rdf.put("title", names);

			if (toscienceJsonObject.has("inLanguage")) {
				List<Map<String, Object>> inLangList = new ArrayList<>();
				String inLang = null;
				jArr = toscienceJsonObject.getJSONArray("inLanguage");
				for (int i = 0; i < jArr.length(); i++) {
					Map<String, Object> inLangMap = new LinkedHashMap<>();
					inLang = jArr.getString(i);
					Locale loc = Locale.forLanguageTag(inLang);
					inLangMap.put("@id", "http://id.loc.gov/vocabulary/iso639-2/"
							+ loc.getISO3Language().replace("deu", "ger"));
					String langPrefLabel = inLang;
					if (loc.getDisplayLanguage() != null) {
						langPrefLabel = loc.getDisplayLanguage();
					}
					inLangMap.put("prefLabel", langPrefLabel);
					inLangList.add(inLangMap);
				}
				rdf.put("language", inLangList);
			}

			jsmapper.mapLrmiAgentsToLobid(rdf, toscienceJsonObject, "creator");
			jsmapper.mapLrmiAgentsToLobid(rdf, toscienceJsonObject, "contributor");
			jsmapper.mapLrmiObjectToLobid(rdf, toscienceJsonObject,
					"learningResourceType", "medium");
			jsmapper.mapLrmiObjectToLobid(rdf, toscienceJsonObject, "about",
					"department");

			// template for Mapping of Array
			if (toscienceJsonObject.has("description")) {
				List<String> descriptions = new ArrayList<>();
				myObj = toscienceJsonObject.get("description");
				if (myObj instanceof java.lang.String) {
					descriptions.add(toscienceJsonObject.getString("description"));
				} else if (myObj instanceof org.json.JSONArray) {
					jArr = toscienceJsonObject.getJSONArray("description");
					for (int i = 0; i < jArr.length(); i++) {
						descriptions.add(jArr.getString(i));
					}
				}
				rdf.put("description", descriptions);
			}

			if (toscienceJsonObject.has("license")) {

				List<Map<String, Object>> licenses = new ArrayList<>();
				myObj = toscienceJsonObject.get("license");
				Map<String, Object> licenseMap = null;
				if (myObj instanceof java.lang.String) {

					licenseMap = new LinkedHashMap<>();
					licenseMap.put("@id", toscienceJsonObject.getString("license"));
					licenses.add(licenseMap);
				} else if (myObj instanceof org.json.JSONObject) {
					jObj = toscienceJsonObject.getJSONObject("license");

					licenseMap = new LinkedHashMap<>();
					licenseMap.put("@id", jObj.getString("id"));
					licenses.add(licenseMap);
				} else if (myObj instanceof org.json.JSONArray) {
					jArr = toscienceJsonObject.getJSONArray("license");
					for (int i = 0; i < jArr.length(); i++) {
						jObj = jArr.getJSONObject(i);

						licenseMap = new LinkedHashMap<>();
						licenseMap.put("@id", jObj.getString("id"));
						licenses.add(licenseMap);
					}
				}
				rdf.put("license", licenses);

			}

			if (toscienceJsonObject.has("publisher")) {
				List<Map<String, Object>> institutions = new ArrayList<>();
				jArr = toscienceJsonObject.getJSONArray("publisher");
				for (int i = 0; i < jArr.length(); i++) {
					jObj = jArr.getJSONObject(i);
					Map<String, Object> publisherMap = new LinkedHashMap<>();
					publisherMap.put("prefLabel", jObj.getString("name"));
					publisherMap.put("@id", jObj.getString("id"));
					institutions.add(publisherMap);
				}
				rdf.put("institution", institutions);
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

			// HINT: potential Passepartout for Mapping default JsonObjects
			if (toscienceJsonObject.has("funder")) {

				// Provide resolving for prefLabels from @id via GenericPropertiesLoader
				LinkedHashMap<String, String> genPropMap = new LinkedHashMap<>();
				GenericPropertiesLoader genProp = new GenericPropertiesLoader();
				genPropMap.putAll(genProp.loadVocabMap("funder-de.properties"));

				jObj = toscienceJsonObject.getJSONObject("funder");
				Map<String, Object> funderMap = new LinkedHashMap<>();
				funderMap.put("type", jObj.getString("type"));
				funderMap.put("@id", jObj.getString("url"));
				funderMap.put("prefLabel", genPropMap.get(jObj.getString("url")));
				rdf.put("funder", funderMap);
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
