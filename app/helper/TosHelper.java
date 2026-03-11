package helper;

import org.json.JSONObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import helper.MyEtikettMaker;
import actions.Modify;
import actions.Read;
import models.Globals;
import models.Link;
import models.Node;
import java.util.stream.Collectors;
import org.json.JSONException;
import play.Play;
import org.eclipse.rdf4j.rio.RDFFormat;
import views.Helper;

/**
 * 
 * @author adoud
 *
 */

public class TosHelper {

	private static final Modify modify = new Modify();

	/**
	 * If a resource is edited via Drupal, the method gets the roles from the old
	 * data stream and saves them in the newly edited toscience data stream
	 * 
	 * @param tosOld
	 * @param tosNew
	 * @return The newly edited data stream(toscience) with the roles for ‘other’,
	 *         ‘creator’ and ‘contributor
	 */

	public static String getRoles(String tosOld, String tosNew) {
		JSONObject joOld;
		JSONObject joNew;

		try {
			joOld = new JSONObject(tosOld);
			joNew = new JSONObject(tosNew);

			String[] keysToCheck = { "other", "creator", "contributor" };

			for (String key : keysToCheck) {
				if (joNew.has(key) && joOld.has(key)) {
					JSONArray newEntries = joNew.getJSONArray(key);
					JSONArray oldEntries = joOld.getJSONArray(key);

					for (int i = 0; i < newEntries.length(); i++) {
						JSONObject newEntry = newEntries.getJSONObject(i);
						String newPrefLabel = newEntry.getString("prefLabel");

						for (int j = 0; j < oldEntries.length(); j++) {
							JSONObject oldEntry = oldEntries.getJSONObject(j);
							String oldPrefLabel = oldEntry.getString("prefLabel");

							if (newPrefLabel.equals(oldPrefLabel)) {
								if (oldEntry.has("role")) {
									JSONArray oldRoles = oldEntry.getJSONArray("role");
									newEntries.getJSONObject(i).put("role", oldRoles);

								}

							}
						}

					}

				}
			}
		} catch (Exception e) {
			play.Logger.debug("Exception in getRoles()" + e);
			return null;
		}

		return joNew.toString();
	}

	/**
	 * This method gets all unresolved PrefLabels and resolves them using the
	 * MyEtikettMaker
	 * 
	 * @param allJsonObjects
	 * @return JSONObject with resolved prefLabels
	 */
	public static JSONObject getPrefLabelsResolved(JSONObject allJsonObjects) {

		Iterator<String> keys = allJsonObjects.keys();

		try {
			while (keys.hasNext()) {
				String key = keys.next();
				Object value = allJsonObjects.get(key);
				play.Logger.debug(" key=" + key + " ,value=" + value.toString()
						+ " ,artOfObject=" + value.getClass().getName());
				if (value.toString().contains("prefLabel")) {
					play.Logger.debug("value contains prefLabel");
					if (value instanceof JSONObject) {
						play.Logger.debug("value instanceof JSONObject");
						JSONObject jsObject = allJsonObjects.getJSONObject(key);
						play.Logger.debug("jsObject=" + jsObject.toString());
						resolvePrefLabel(jsObject);
					} else if (value instanceof JSONArray) {
						play.Logger.debug("value instanceof JSONArray");
						JSONArray jsArray = allJsonObjects.getJSONArray(key);
						play.Logger.debug("jsArray=" + jsArray.toString());
						for (int j = 0; j < jsArray.length(); j++) {
							JSONObject jsObject = jsArray.getJSONObject(j);
							if (jsObject.has("prefLabel")) {
								resolvePrefLabel(jsObject);
							}
						}
					}

				}

			}

		} catch (JSONException e) {
			play.Logger.debug("Exception in getPrefLabelsResolved()" + e);
		}

		return allJsonObjects;

	}

	private static void resolvePrefLabel(JSONObject jsObject) {
		try {
			Object oldPrefLabel = jsObject.opt("prefLabel");
			if (oldPrefLabel == null) {
				return;
			}

			String oldPrefLabelString = oldPrefLabel.toString();
			if (!oldPrefLabelString.contains("http")
					|| oldPrefLabelString.contains("www.openstreetmap.org")) {
				return;
			}

			play.Logger.debug("oldPrefLabel=" + oldPrefLabelString);
			String newPrefLabel =
					MyEtikettMaker.getLabelFromEtikettWs(oldPrefLabelString);
			play.Logger.debug("newPrefLabel=" + newPrefLabel);
			if (newPrefLabel != null && !newPrefLabel.trim().isEmpty()) {
				jsObject.put("prefLabel", newPrefLabel);
			} else {
				jsObject.put("prefLabel", oldPrefLabelString);
			}
		} catch (RuntimeException e) {
			play.Logger
					.debug("Could not resolve prefLabel=" + jsObject.opt("prefLabel"), e);
		} catch (JSONException e) {
			play.Logger
					.debug("Could not update prefLabel=" + jsObject.opt("prefLabel"), e);
		}
	}

	/**
	 * This method deletes the KTBL metadata and keeps only the Toscience metadata
	 * and returns it
	 * 
	 * @param contentJsFile contains Tos and Ktbl metadata
	 * @return a string with Toscience metadata
	 */
	static public String getToPersistTosMd(String contentJsFile, String pid) {
		JSONObject ktblAndTos = null;
		String[] elementsToRemove = { "livestock_category", "ventilation_system",
				"livestock_production", "housing_systems", "additional_housing_systems",
				"emi_measurement_techniques", "emissions", "emission_reduction_methods",
				"project_title", "test_design", "info" };
		try {
			String resource_id =
					new String(Globals.protocol + Globals.server + "/resource/" + pid);
			play.Logger.debug("resource_id= " + resource_id);

			ktblAndTos = new JSONObject(contentJsFile);
			if (resource_id != null) {
				ktblAndTos.put("id", resource_id);
			}

			if (ktblAndTos.has("license")) {
				JSONArray licenseArray = ktblAndTos.getJSONArray("license");

				for (int i = 0; i < licenseArray.length(); i++) {
					JSONObject licenseObject = licenseArray.getJSONObject(i);
					if (!licenseObject.has("prefLabel")) {
						licenseObject.put("prefLabel", licenseObject.opt("@id"));

					}
				}

			}
			for (String element : elementsToRemove) {
				if (ktblAndTos.has(element)) {
					ktblAndTos.remove(element);
				}
			}
		} catch (JSONException e) {
			play.Logger.debug("Exception in getToPersistTosMd()" + e);
		}

		return ktblAndTos.toString();
	}

	static public String getAssociatedDatasets(String tosOld, String tosNew) {
		JSONObject joOld;
		JSONObject joNew;

		try {
			joOld = new JSONObject(tosOld);
			joNew = new JSONObject(tosNew);
			JSONArray oldDatasets = joOld.getJSONArray("associatedDataset");
			JSONArray newDatasets = joNew.getJSONArray("associatedDataset");

			List<String> oldIds = new ArrayList<>();
			for (int i = 0; i < oldDatasets.length(); i++) {
				JSONObject oldDataset = oldDatasets.getJSONObject(i);
				oldIds.add(oldDataset.getString("@id"));
			}

			JSONArray updatedDatasets = new JSONArray();
			for (int i = 0; i < newDatasets.length(); i++) {
				String newId = newDatasets.getString(i);
				boolean found = false;
				for (int j = 0; j < oldIds.size(); j++) {
					if (oldIds.get(j).equals(newId)) {
						updatedDatasets.put(oldDatasets.getJSONObject(j));
						found = true;
						break;
					}
				}
				if (!found) {
					updatedDatasets.put(newId);
				}
			}
			joNew.put("associatedDataset", updatedDatasets);
			return joNew.toString();
		} catch (Exception e) {
			play.Logger.debug("Exception in getAssociatedDatasets()" + e);
			return null;
		}
	}

	private static Map<String, Object> jsonObjectToMap(JSONObject jsonObject) {
		Map<String, Object> map = new TreeMap<>();
		try {
			Iterator<String> keys = jsonObject.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				Object value = jsonObject.get(key);
				if (value instanceof JSONObject) {
					map.put(key, jsonObjectToMap((JSONObject) value));
				} else if (value instanceof JSONArray) {
					map.put(key, jsonArrayToList((JSONArray) value));
				} else {
					map.put(key, value);
				}
			}
		} catch (Exception e) {
			play.Logger.debug("Exception in jsonObjectToMap(): " + e);
		}
		return map;
	}

	public static Map<String, Object> jsonToMap(String tosDs) {
		Map<String, Object> map = null;
		play.Logger.debug("EjsonToMap(), tosDs= " + tosDs);
		JSONObject jo = null;
		try {

			jo = new JSONObject(tosDs);

		} catch (Exception e) {
			play.Logger.debug("Exception in jsonToMap()" + e);

		}
		// map = removeQuotes(map);
		map = transformMap(jo);

		return map;
	}

	private static List<Object> jsonArrayToList(JSONArray jsonArray) {
		List<Object> list = new ArrayList<>();
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				Object value = jsonArray.get(i);
				if (value instanceof JSONObject) {
					list.add(jsonObjectToMap((JSONObject) value));
				} else {
					list.add(value);
				}
			}
		} catch (Exception e) {
			play.Logger.debug("Exception in jsonArrayToList(): " + e);
		}

		return list;
	}

	public static void addHasPartElementByNewFile(Node node) {

		String result = null;
		try {
			if (node.getParentPid() != null) {
				Node parentNode = new Read().readNode(node.getParentPid());
				String tosDs = parentNode.getMetadata("toscience");
				JSONObject jo = new JSONObject(tosDs);
				JSONArray hasPartArray = jo.optJSONArray("hasPart");

				if (hasPartArray == null) {
					hasPartArray = new JSONArray();
				}

				boolean alreadyExists = false;
				for (int i = 0; i < hasPartArray.length(); i++) {
					JSONObject existingObject = hasPartArray.getJSONObject(i);
					if (existingObject.getString("@id").equals(node.getPid())) {
						alreadyExists = true;
						break;
					}
				}

				if (!alreadyExists) {
					JSONObject hasPartObject = new JSONObject();
					hasPartObject.put("prefLabel", node.getFileLabel());
					hasPartObject.put("@id", node.getPid());
					hasPartArray.put(hasPartObject);
					jo.put("hasPart", hasPartArray);
					result = jo.toString();
					new Modify().updateMetadata("toscience", parentNode, result);
				}
			}
		} catch (Exception e) {
			play.Logger.debug("JSONException," + e);
		}
	}

	public static Map<String, String> extractFRLIds(List<Link> links) {
		Map<String, String> idMap = new HashMap<>();
		for (Link link : links) {
			if (link.getPredicate() != null
					&& link.getPredicate().contains("#hasPart")) {
				idMap.put(link.getObject(), link.getPredicateLabel());
			}
		}
		return idMap;
	}

	public static void addHasPartElementByOldFile(Map<String, String> map) {
		Node childNode = null;
		for (String pid : map.keySet()) {
			childNode = new Read().readNode(pid);
			addHasPartElementByNewFile(childNode);
		}
	}

	public static Map<String, Object> removeQuotes(Map<String, Object> data) {
		Map<String, Object> cleanedData = new LinkedHashMap<>();
		for (String key : data.keySet()) {
			play.Logger.debug("key" + key);
			String cleanedKey = key.replace("\"", "");
			Object value = data.get(key);
			value = value.toString().replace("\"", "");
			if (value.toString().contains("prefLabel:")
					|| value.toString().contains("@id:")) {
				value = value.toString().replace("prefLabel:", "prefLabel=");
				value = value.toString().replace("@id:", "@id=");
			}
			cleanedData.put(cleanedKey, value);
		}
		return cleanedData;
	}

	private static String addSpaceAfterClosingBrace(String input) {
		return input.replaceAll("\\},", "}, ");
	}

	public static String restructureDataRecords(String input) {
		String regex = "\\{prefLabel=(.*?),@id=(.*?)\\}";
		String replacement = "{@id=$2, prefLabel=$1}";

		String s = replaceRecords(input, regex, replacement);
		return addSpaceAfterClosingBrace(s);
	}

	private static String replaceRecords(String input, String regex,
			String replacement) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.find()) {
			return input;
		}
		return matcher.replaceAll(replacement);
	}

	public static String cleanBrackets(String cleanedString) {
		if (cleanedString.startsWith("[")) {
			cleanedString = cleanedString.substring(1);
		}
		if (cleanedString.endsWith("]")) {
			cleanedString = cleanedString.substring(0, cleanedString.length() - 1);
		}
		return cleanedString;
	}

	public static Map<String, Object> transformMap(JSONObject jo) {
		if (jo == null) {
			return new LinkedHashMap<>();
		}

		play.Logger.debug("transformMap(), jo=" + jo.toString());

		Map<String, Object> newMap = new LinkedHashMap<>();
		JSONArray jsonArray = null;
		String key = null;
		Object value = null;

		try {
			Iterator<String> keys = jo.keys();
			while (keys.hasNext()) {

				key = keys.next();
				value = jo.opt(key);

				if (key == null || value == null) {
					continue;
				}

				// String strValue = value.toString();
				// strValue = cleanBrackets(strValue);

				if ("id".equals(key)) {
					continue;

					// (ArrayList, Strings)
				} else if ("fundingProgram".equals(key) || "projectId".equals(key)
						|| "contributorOrder".equals(key)) {
					List<String> arrayList = new ArrayList<>();
					jsonArray = jo.getJSONArray(key);
					for (int i = 0; i < jsonArray.length(); i++) {
						arrayList.add(jsonArray.get(i).toString());
					}
					newMap.put(key, arrayList);

					// (ArraList,TreeMap)
				} else if ("fundingId".equals(key) || "creator".equals(key)
						|| "contributor".equals(key) || "publisherVersion".equals(key)
						|| "additionalMaterial".equals(key) || "contribution".equals(key)
						|| "joinedFunding".equals(key) || "professionalGroup".equals(key)) {
					List<Map<String, Object>> arrayList = new ArrayList<>();
					jsonArray = jo.getJSONArray(key);
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						Map<String, Object> keyMap = new TreeMap<>();
						keyMap = jsonObjectToMap(jsonObject);
						arrayList.add(keyMap);
					}
					newMap.put(key, arrayList);

					// (ArraList,TreeMap)
				} else if ("fulltextVersion".equals(key)
						|| "internalReference".equals(key)) {

					List<Map<String, Object>> arrayList = new ArrayList<>();
					jsonArray = jo.getJSONArray(key);
					for (int i = 0; i < jsonArray.length(); i++) {
						Object o = jsonArray.opt(i);
						Map<String, Object> keyMap = new TreeMap<>();
						keyMap.put("@id", o);
						keyMap.put("prefLabel", o);
						arrayList.add(keyMap);
					}
					newMap.put(key, arrayList);

				} else if ("@id".equals(key)) {
					newMap.put(key, cleanBrackets(value.toString()));

					// (HashSet,TreeMap)
				} else if ("language".equals(key) || "dataOrigin".equals(key)
						|| "ddc".equals(key) || "license".equals(key)
						|| "medium".equals(key) || "isPrimaryTopicOf".equals(key)
						|| "rdftype".equals(key) || "isLike".equals(key)
						|| "other".equals(key) || "institution".equals(key)
						|| "subject".equals(key) || "collectionOne".equals(key)
						|| "editor".equals(key) || "containedIn".equals(key)
						|| "publicationStatus".equals(key) || "reviewStatus".equals(key)
						|| "collectionTwo".equals(key) || "parallelEdition".equals(key)
						|| "recordingLocation".equals(key)
						|| "recordingCoordinates".equals(key)
						|| "bibliographicLevel".equals(key) || "describedby".equals(key)
						|| "fulltextOnline".equals(key) || "hasItem".equals(key)
						|| "inCollection".equals(key) || "lv:isPartOf".equals(key)
						|| "parallelEdition".equals(key) || "publication".equals(key)
						|| "sameAs".equals(key) || "spatial".equals(key)
						|| "relation".equals(key) || "natureOfContent".equals(key)
						|| "successor".equals(key) || "predecessor".equals(key)
						|| "exampleOfWork".equals(key) || "tableOfContents".equals(key)
						|| "containsExampleOfWork".equals(key)
						|| "associatedDataset".equals(key)) {
					jsonArray = jo.getJSONArray(key);
					Set<Map<String, Object>> mySet = new HashSet<>();

					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						Map<String, Object> keyMap = new TreeMap<>();
						keyMap = jsonObjectToMap(jsonObject);
						mySet.add(keyMap);
					}
					newMap.put(key, mySet);

				} else if ("publishScheme".equals(key) || "accessScheme".equals(key)
						|| "catalogId".equals(key) || "primaryTopic".equals(key)
						|| "issued".equals(key) || "contentType".equals(key)
						|| "@context".equals(key)) {

					Set<String> mySet = new HashSet<>();
					mySet.add(jo.get(key).toString());
					newMap.put(key, mySet);

					// joinedFunding
				} else if ("joinedFunding".equals(key)) {
					play.Logger.debug("jo.get(joinedFunding)" + jo.get("joinedFunding"));
				} else {
					// z.B description | usageManual | urn (HashSet, Strings)
					Set<String> mySet = new HashSet<>();
					jsonArray = jo.getJSONArray(key);
					for (int i = 0; i < jsonArray.length(); i++) {
						mySet.add(jsonArray.get(i).toString());
					}
					newMap.put(key, mySet);
				}
			}
			// Key prefLabel is saved as a HashSet
			newMap.put("prefLabel", new HashSet<>(Arrays.asList(jo.get("@id"))));

		} catch (Exception e) {
			play.Logger.debug("Exception in transformMap(), Key=" + key + ", value="
					+ value + "," + e);
		}

		return newMap;
	}

	public static void displayDataStructuresAndTypes(Map<String, Object> map) {

		if (map != null) {
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				// Datenstruktur ermitteln
				String dataStructure = value.getClass().getSimpleName();
				play.Logger.debug("Key: " + key + ", Datenstruktur: " + dataStructure);

				// Typ der Elemente ermitteln
				if (value instanceof HashSet) {
					HashSet<?> set = (HashSet<?>) value;
					play.Logger.debug("  Elementtyp im HashSet:");
					for (Object element : set) {
						play.Logger.debug("    " + element.getClass().getSimpleName());
					}
				} else if (value instanceof ArrayList) {
					ArrayList<?> list = (ArrayList<?>) value;
					play.Logger.debug("  Elementtyp in ArrayList:");
					for (Object element : list) {
						play.Logger.debug("    " + element.getClass().getSimpleName());
					}
				} else {
					play.Logger.debug("  Datentyp: " + value.getClass().getSimpleName());
				}
			}
		}
	}

	public static void logObjectInfo(Map<String, Object> m) {

		if (m != null) {
			for (String key : m.keySet()) {
				Object obj = m.get(key);
				if (obj != null) {
					String typeName;
					if (obj.getClass().isArray()) {
						typeName = obj.getClass().getComponentType().getName();
					} else {
						typeName = obj.getClass().getName();
					}
					play.Logger
							.debug("key=" + key + ", Value=" + obj + ", Type=" + typeName);
				} else {
					play.Logger.debug("key=" + key + ", Value=null, Type=null");
				}
			}
		}
	}

	public static String updateConent(String content) {
		JSONObject jo = null;
		try {
			jo = new JSONObject(content);
			jo.remove("itemID");
			jo.remove("accessScheme");
			jo.remove("publishScheme");
			jo.remove("isMemberOf");
			// jo.remove("joinedFunding");

			if (!jo.has("rdftype")) {
				String contentType = jo.optString("contentType");
				String prefLabel = null;
				String id = null;

				if ("monograph".equals(contentType)) {
					prefLabel = "Monografie";
					id = "http://purl.org/ontology/bibo/Book";

				} else if ("researchData".equals(contentType)) {
					prefLabel = "Forschungsdaten";
					id = "http://hbz-nrw.de/regal#ResearchData";

				} else {
					play.Logger.debug("updateContent(), rdftype not set for contentType="
							+ contentType);
				}

				if (prefLabel != null && id != null) {
					JSONObject rdfTypeObject =
							new JSONObject().put("prefLabel", prefLabel).put("@id", id);
					JSONArray rdfTypeArray = new JSONArray().put(rdfTypeObject);
					jo.put("rdftype", rdfTypeArray);
					play.Logger.debug("updateContent(), rdftype has been added");
				}
			}

			if (jo.has("contributerOrder")) {
				JSONArray contOrder = jo.getJSONArray("contributerOrder");
				jo.remove("contributerOrder");
				jo.put("contributorOrder", contOrder);

			}
			// If title is a string, it will be converted into an array of strings.
			if (jo.has("title")) {
				if (jo.get("title") != null
						&& jo.get("title").getClass() == String.class) {
					play.Logger.debug("title wird ueberschrieben");
					jo.put("title", new String[] { (String) jo.get("title") });
				}
			}

		} catch (Exception e) {
			play.Logger.debug("Exception in updateConent" + e);
		}
		if (jo == null) {
			return null;
		}
		return jo.toString();
	}

	/**
	 * Method checks if a string has a JSON structure or not
	 * 
	 * @param jsonString
	 * @return
	 */
	public static boolean isValidJson(String jString) {
		if (jString == null || jString.length() < 10 || jString.isEmpty()) {
			return false;
		}
		try {
			new JSONObject(jString);
			return true;
		} catch (JSONException e) {
		}
		try {
			new JSONArray(jString);
			return true;
		} catch (JSONException ex) {

		}

		return false;
	}

	public static JSONObject validateJsonStructure(JSONObject allMd) {

		try {

			if (allMd.has("issued")) {
				String issued = allMd.get("issued").toString();
				allMd.put("issued", Metadata2Helper.getQuotedValues(issued));
			}

			if (allMd.has("license")) {
				JSONArray licenseArray = allMd.getJSONArray("license");
				for (int i = 0; i < licenseArray.length(); i++) {
					JSONObject licenseObject = licenseArray.getJSONObject(i);
					if (!licenseObject.has("prefLabel")) {
						licenseObject.put("prefLabel", licenseObject.opt("@id"));
					}
				}
			}

			// wird ergaenzt um weitere Elemente
		} catch (JSONException e) {
			play.Logger.debug("Exception in validateJsonStructure()" + e);
		}
		return allMd;
	}

	public static void persistAndNormalizeToscienceMetadata(String pid, Node node) {
		JSONObject allMd = null;
		Map<String, Object> map = null;
		try {

			// Case 1: toscience does not exist and will be persisted
			if (!Helper.mdStreamExists(pid, "toscience")
					|| node.getMetadata("toscience").length() < 5) {

				if (Helper.mdStreamExists(pid, "metadata2")) {
					map = RdfHelper.getRdfAsMap(node, RDFFormat.NTRIPLES,
							node.getMetadata("metadata2"));

				} else if (!Helper.mdStreamExists(pid, "metadata2")
						&& Helper.mdStreamExists(pid, "metadata")) {
					map = RdfHelper.getRdfAsMap(node, RDFFormat.NTRIPLES,
							node.getMetadata("metadata"));
				}

				if (map != null) {
					allMd = new JSONObject(map);
					allMd = TosHelper.getPrefLabelsResolved(allMd);
					modify.updateMetadata("toscience", node, allMd.toString());
				} else {
					play.Logger
							.debug("No metadata2/metadata stream found for pid=" + pid);
				}

				// Case 2: toscience exists, but may have JSON elements with invalid
				// structures
			} else if (TosHelper.isValidJson(node.getMetadata("toscience"))) {

				allMd = new JSONObject(node.getMetadata("toscience"));
				JSONObject original = new JSONObject(allMd.toString());
				allMd = TosHelper.validateJsonStructure(allMd);

				if (!original.toString().equals(allMd.toString())) {
					// 1: update toscience
					modify.updateMetadata("toscience", node, allMd.toString());
					// 2: update metadata2?
					// 3: update ktbl?

					// update json2
					node.getLd2();
				}

			}

		} catch (Exception e) {
			play.Logger
					.debug("Exception in persistAndNormalizeToscienceMetadata() " + e);
		}
	}
}
