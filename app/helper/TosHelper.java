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
import archive.fedora.RdfUtils;
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

	private enum StructureType {
		STRING, STRING_ARRAY, SIMPLEOBJECT_ARRAY
	}

	private static final Map<String, StructureType> FIELD_TYPES =
			new LinkedHashMap<>();

	static {
		FIELD_TYPES.put("@id", StructureType.STRING);
		FIELD_TYPES.put("id", StructureType.STRING);
		FIELD_TYPES.put("catalogId", StructureType.STRING);
		FIELD_TYPES.put("issued", StructureType.STRING);
		FIELD_TYPES.put("contentType", StructureType.STRING);
		FIELD_TYPES.put("primaryTopic", StructureType.STRING);

		FIELD_TYPES.put("title", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("prefLabel", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("alternative", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("description", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("usageManual", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("edition", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("associatedPublication", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("reference", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("creatorName", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("contributorName", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("contributorOrder", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("fundingProgram", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("embargoTime", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("nextVersion", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("previousVersion", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("urn", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("subjectName", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("yearOfCopyright", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("projectId", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("recordingPeriod", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("fulltextVersion", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("additionalNotes", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("internalReference", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("abstractText", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("publicationYear", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("bibliographicCitation", StructureType.STRING_ARRAY);

		FIELD_TYPES.put("fundingId", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("isLike", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("subject", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("language", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("medium", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("rdftype", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("institution", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("dataOrigin", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("associatedDataset", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("additionalMaterial", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("license", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("ddc", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("publisherVersion", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("containedIn", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("collectionOne", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("collectionTwo", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("publicationStatus", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("reviewStatus", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("professionalGroup", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("isPrimaryTopicOf", StructureType.SIMPLEOBJECT_ARRAY);
	}

	/**
	 * If a resource is edited via Drupal, the method gets the roles from the old
	 * data stream and saves them in the newly edited toscience data stream
	 * 
	 * @param tosOld
	 * @param tosNew
	 * @return The newly edited data stream(toscience) with the roles for other,
	 *         creator and contributor
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

			addRdftypeIfMissing(jo);

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

	public static void addRdftypeIfMissing(JSONObject jo) {
		if (jo == null || jo.has("rdftype")) {
			return;
		}

		try {
			String contentType = jo.optString("contentType");
			String prefLabel = null;
			String id = null;

			if ("monograph".equalsIgnoreCase(contentType)) {
				prefLabel = "Monografie";
				id = "http://purl.org/ontology/bibo/Book";

			} else if ("researchData".equalsIgnoreCase(contentType)) {
				prefLabel = "Forschungsdaten";
				id = "http://hbz-nrw.de/regal#ResearchData";

			} else {
				play.Logger.debug(
						"updateContent(), rdftype not set for contentType=" + contentType);
			}

			if (prefLabel != null && id != null) {
				JSONObject rdfTypeObject =
						new JSONObject().put("prefLabel", prefLabel).put("@id", id);
				JSONArray rdfTypeArray = new JSONArray().put(rdfTypeObject);
				jo.put("rdftype", rdfTypeArray);
				play.Logger.debug("updateContent(), rdftype has been added");
			}
		} catch (Exception e) {
			play.Logger.debug("Exception in addRdftypeIfMissing " + e);
		}
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

	public static JSONObject validateJsonStructure(JSONObject allMd, Node n) {

		try {

			if (n != null && n.getPid() != null
					&& (!allMd.has("@id") || allMd.opt("@id") == null
							|| allMd.optString("@id").trim().isEmpty())) {
				allMd.put("@id", n.getPid());
			}

			for (Map.Entry<String, StructureType> field : FIELD_TYPES.entrySet()) {
				validateFieldByStructureType(allMd, field.getKey(), field.getValue());
			}
			normalizeAssociatedDatasets(allMd, n);

		} catch (JSONException e) {
			play.Logger.debug("Exception in validateJsonStructure()" + e);
		}
		return allMd;
	}

	private static void validateFieldByStructureType(JSONObject metadata,
			String key, StructureType structureType) throws JSONException {
		switch (structureType) {
		case STRING:
			validateStringField(metadata, key);
			return;
		case STRING_ARRAY:
			normalizeStringArrayField(metadata, key);
			return;
		case SIMPLEOBJECT_ARRAY:
			normalizeSimpleObjectArrayField(metadata, key);
			return;
		default:
			return;
		}
	}

	private static void validateStringField(JSONObject metadata, String key)
			throws JSONException {
		if (!metadata.has(key) || metadata.isNull(key)) {
			return;
		}

		Object value = metadata.get(key);

		if (value instanceof String) {
			String stringValue = (String) value;
			if (stringValue.trim().isEmpty()) {
				return;
			}
			if (isStringInArrayFormat(stringValue)) {
				metadata.put(key, Metadata2Helper.getQuotedValues(stringValue));
			}
			return;
		}

		metadata.put(key, Metadata2Helper.getQuotedValues(String.valueOf(value)));
	}

	private static boolean isStringInArrayFormat(String value) {
		String trimmed = value.trim();
		return trimmed.startsWith("[\"") && trimmed.endsWith("\"]");
	}

	private static void normalizeStringArrayField(JSONObject metadata, String key)
			throws JSONException {

		if (!metadata.has(key) || metadata.isNull(key)) {
			return;
		}

		Object value = metadata.get(key);

		if (value instanceof JSONArray) {
			return;
		}

		JSONArray ja = new JSONArray();

		String stringValue = String.valueOf(value);

		if (isStringInArrayFormat(stringValue)) {
			ja.put(Metadata2Helper.getQuotedValues(stringValue));
		} else {
			ja.put(stringValue);
		}

		metadata.put(key, ja);
	}

	private static void normalizeSimpleObjectArrayField(JSONObject metadata,
			String key) throws JSONException {

		if (!metadata.has(key) || metadata.isNull(key)) {
			return;
		}

		Object value = metadata.get(key);
		JSONArray normalized = new JSONArray();

		if (value instanceof JSONObject) {
			normalized.put(normalizeSimpleObjectEntry(value));

		} else if (value instanceof JSONArray) {
			JSONArray input = (JSONArray) value;
			for (int i = 0; i < input.length(); i++) {
				Object entry = input.get(i);
				JSONObject normalizedEntry = normalizeSimpleObjectEntry(entry);
				if (normalizedEntry != null) {
					normalized.put(normalizedEntry);
				}
			}
		} else {
			normalized.put(normalizeSimpleObjectEntry(value));
		}
		metadata.put(key, normalized);
	}

	private static JSONObject normalizeSimpleObjectEntry(Object entry)
			throws JSONException {
		if (entry instanceof JSONObject) {
			return completeSimpleObject((JSONObject) entry);
		}

		if (entry == null || entry == JSONObject.NULL) {
			return null;
		}
		// String
		JSONObject wrapped = new JSONObject();
		String prefLabel = String.valueOf(entry);
		wrapped.put("@id", buildAdhocUri(prefLabel));
		wrapped.put("prefLabel", prefLabel);
		return wrapped;
	}

	private static JSONObject completeSimpleObject(JSONObject object)
			throws JSONException {
		String prefLabel = object.optString("prefLabel", "").trim();
		String id = object.optString("@id", "").trim();

		if (prefLabel.isEmpty() && !id.isEmpty()) {
			object.put("prefLabel", object.opt("@id"));
		}
		if (id.isEmpty() && !prefLabel.isEmpty()) {
			object.put("@id", buildAdhocUri(prefLabel));
		}
		return object;
	}

	private static void normalizeAssociatedDatasets(JSONObject metadata,
			Node node) throws JSONException {
		if (!metadata.has("associatedDataset") && metadata.has("relatedDatasets")
				&& !metadata.isNull("relatedDatasets")) {
			metadata.put("associatedDataset", metadata.get("relatedDatasets"));
		}

		if (metadata.has("associatedDataset") && metadata.has("relatedDatasets")) {
			metadata.remove("relatedDatasets");
		}

		if (!metadata.has("associatedDataset")
				|| metadata.isNull("associatedDataset")) {
			return;
		}

		JSONArray datasets = metadata.optJSONArray("associatedDataset");
		if (datasets == null) {
			return;
		}

		for (int i = 0; i < datasets.length(); i++) {
			Object entry = datasets.get(i);
			if (!(entry instanceof JSONObject)) {
				continue;
			}
			JSONObject dataset = (JSONObject) entry;
			String id = dataset.optString("@id", "").trim();
			if (id.isEmpty() || !id.startsWith("http")) {
				String prefLabel = dataset.optString("prefLabel", "").trim();
				if (!prefLabel.isEmpty()) {
					dataset.put("@id", buildAdhocUri(prefLabel));
				} else {
					String pid = node != null ? node.getPid() : "resource";
					dataset.put("@id", buildAdhocUri(pid + "/associatedDataset/" + i));
				}
			}
		}
	}

	/**
	 * The method generates a dummy URI if it is missing from a SimpleObject, so
	 * that Elasticsearch does not throw parsing exceptions.
	 * 
	 * @param prefLabel
	 * @return
	 */
	private static String buildAdhocUri(String prefLabel) {
		return Globals.protocol + Globals.server + "/adhoc/"
				+ RdfUtils.urlEncode(prefLabel).replace("+", "%20");
	}

	/**
	 * Method Persists the Toscience data stream if it is not already present. If
	 * the data stream already exists, the method checks the structure of the
	 * individual elements. If necessary, the required modifications to the
	 * structure will be made.
	 * 
	 * @param pid
	 * @param node
	 */
	public static void persistAndNormalizeToscienceMetadata(String pid,
			Node node) {
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
				allMd = TosHelper.validateJsonStructure(allMd, node);

				addRdftypeIfMissing(allMd);

				allMd = TosHelper.getPrefLabelsResolved(allMd);

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

	/**
	 * This method is an extension of the `persistAndNormalizeToScienceMetadata`
	 * method. It handles child objects if they exist.
	 * 
	 * @param pid
	 * @param node
	 */
	public static void persistAndNormalizeToscienceMetadataWithParts(String pid,
			Node node) {
		persistAndNormalizeToscienceMetadata(pid, node);
		for (Node child : new Read().getParts(node)) {
			persistAndNormalizeToscienceMetadata(child.getPid(), child);
		}
	}
}
