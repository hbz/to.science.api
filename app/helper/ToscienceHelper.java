package helper;

import org.json.JSONObject;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import org.json.JSONArray;
import helper.MyEtikettMaker;
import models.Globals;
import java.util.stream.Collectors;
import org.json.JSONException;
import play.Play;
import models.Node;
import models.Link;
import java.util.Map;
import actions.Read;
import actions.Modify;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TreeMap;

/**
 * 
 * @author adoud
 *
 */

public class ToscienceHelper {

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
			e.printStackTrace();
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

		Object oldPrefLabel = null;
		JSONObject jsObject = null;

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
						jsObject = allJsonObjects.getJSONObject(key);
						play.Logger.debug("jsObject=" + jsObject.toString());
						oldPrefLabel = jsObject.get("prefLabel");
						if (oldPrefLabel.toString().contains("http")
								&& !oldPrefLabel.toString().contains("www.openstreetmap.org")) {
							play.Logger.debug("oldPrefLabel=" + oldPrefLabel.toString());
							String newPrefLabel =
									MyEtikettMaker.getLabelFromEtikettWs(oldPrefLabel.toString());
							play.Logger.debug("newPrefLabel=" + newPrefLabel);
							jsObject.put("prefLabel", newPrefLabel);
						}
					} else if (value instanceof JSONArray) {
						play.Logger.debug("value instanceof JSONArray");
						JSONArray jsArray = allJsonObjects.getJSONArray(key);
						play.Logger.debug("jsArray=" + jsArray.toString());
						for (int j = 0; j < jsArray.length(); j++) {
							jsObject = jsArray.getJSONObject(j);
							oldPrefLabel = jsObject.get("prefLabel");
							if (oldPrefLabel.toString().contains("http") && !oldPrefLabel
									.toString().contains("www.openstreetmap.org")) {
								play.Logger.debug("oldPrefLabel=" + oldPrefLabel.toString());
								String newPrefLabel = MyEtikettMaker
										.getLabelFromEtikettWs(oldPrefLabel.toString());
								play.Logger.debug("newPrefLabel=" + newPrefLabel);
								jsObject.put("prefLabel", newPrefLabel);
							}
						}
					}

				}

			}

		} catch (JSONException e) {
			// Behandlung der JSONException
			e.printStackTrace();
		}

		return allJsonObjects;

	}

	/**
	 * This method deletes the KTBL metadata and keeps only the Toscience metadata
	 * and returns it
	 * 
	 * @param contentJsFile contains Tos and Ktbl metadata
	 * @return a string with Toscience metadata
	 */
	static public String getToPersistTosMetadata(String contentJsFile,
			String pid) {
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
			for (String element : elementsToRemove) {
				if (ktblAndTos.has(element)) {
					ktblAndTos.remove(element);
				}
			}
		} catch (JSONException e) {
			play.Logger.debug("JSONException," + e);
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
			e.printStackTrace();
			return null;
		}
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

	public static Map<String, Object> jsonToMap(String tosDs) {
		Map<String, Object> map = null;
		String key = null;
		Object value = null;
		JSONObject jo = null;
		try {
			map = new LinkedHashMap<>();
			jo = new JSONObject(tosDs);
			Iterator<String> keys = jo.keys();
			while (keys.hasNext()) {
				key = keys.next();
				value = jo.get(key);
				play.Logger.debug("Key=" + key + " ,value=" + value);
				map.put(key, value);

			}
		} catch (Exception e) {
			play.Logger.debug("Exception by jsonToMap(), Key=" + key + " , value="
					+ value + "," + e);
		}

		map = removeQuotes(map);
		map = transformMap(map, jo);

		return map;
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

	public static Map<String, Object> transformMap(Map<String, Object> inputMap,
			JSONObject jo) {
		Map<String, Object> newMap = new LinkedHashMap<>();
		JSONArray jsonArray = null;
		String key = null;
		Object value = null;
		try {
			for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
				key = entry.getKey();
				value = entry.getValue();
				String strValue = (String) value;
				strValue = cleanBrackets(strValue);
				strValue = restructureDataRecords(strValue);

				if ("id".equals(key)) {
					continue;
					// (ArrayList, Strings)
				} else if ("fundingProgram".equals(key) || "projectId".equals(key)
						|| "contributorOrder".equals(key)) {
					ArrayList<String> arrayList = new ArrayList<>();
					jsonArray = jo.getJSONArray(key);
					for (int i = 0; i < jsonArray.length(); i++) {
						arrayList.add(jsonArray.get(i).toString());
					}
					newMap.put(key, arrayList);
					// (ArraList,TreeMap)
				} else if ("fundingId".equals(key) || "creator".equals(key)
						|| "contributor".equals(key) || "publisherVersion".equals(key)
						|| "fulltextVersion".equals(key)
						|| "additionalMaterial".equals(key)) {
					ArrayList<Map<String, Object>> arrayList = new ArrayList<>();
					jsonArray = jo.getJSONArray(key);
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						Map<String, Object> keyMap = new TreeMap<>();
						keyMap.put("@id", jsonObject.getString("@id"));
						keyMap.put("prefLabel", jsonObject.getString("prefLabel"));
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
						|| "collectionTwo".equals(key) || "internalReference".equals(key)
						|| "parallelEdition".equals(key)) {

					jsonArray = jo.getJSONArray(key);
					// List<Map<String, Object>> keyList = new ArrayList<>();
					HashSet<Map<String, Object>> mySet = new HashSet<>();

					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						Map<String, Object> keyMap = new TreeMap<>();
						keyMap.put("@id", jsonObject.getString("@id"));
						keyMap.put("prefLabel", jsonObject.getString("prefLabel"));
						mySet.add(keyMap);

					}
					newMap.put(key, mySet);

				} else if ("publishScheme".equals(key) || "accessScheme".equals(key)
						|| "catalogId".equals(key)) {
					play.Logger.debug("jo.get(key).toString()=" + jo.get(key).toString());
					HashSet<String> mySet = new HashSet<>();
					mySet.add(jo.get(key).toString());
					newMap.put(key, mySet);
				} else {
					// z.B description | usageManual | urn(HashSet, Strings)
					HashSet<String> mySet = new HashSet<>();
					jsonArray = jo.getJSONArray(key);
					for (int i = 0; i < jsonArray.length(); i++) {
						mySet.add(jsonArray.get(i).toString());
					}

					// mySet.add(strValue);
					newMap.put(key, mySet);
				}
			}
			// Key prefLabel is saved as a HashSet
			newMap.put("prefLabel", new HashSet<>(Arrays.asList(jo.get("@id"))));

		} catch (Exception e) {
			play.Logger.debug("Exception by transformMap(), Key=" + key + " , value="
					+ value + "," + e);
		}

		return newMap;
	}

	public static void displayDataStructuresAndTypes(Map<String, Object> map) {
		play.Logger.debug("***********************************");
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

	public static void logObjectInfo(String key, Object obj) {
		play.Logger.debug("***********************************");
		if (obj != null) {
			String typeName;
			if (obj.getClass().isArray()) {
				typeName = obj.getClass().getComponentType().getName();
			} else {
				typeName = obj.getClass().getName();
			}
			play.Logger.debug("key=" + key + ", Value=" + obj + ", Type=" + typeName);
		} else {
			play.Logger.debug("key=" + key + ", Value=null, Type=null");
		}
	}
}
