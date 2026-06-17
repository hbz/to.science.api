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
		FIELD_TYPES.put("extent", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("hbzId", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("almaMmsId", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("deprecatedUri", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("otherTitleInformation", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("responsibilityStatement", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("Isbn", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("bibo:doi", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("issn", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("note", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("oclcNumber", StructureType.STRING_ARRAY);
		FIELD_TYPES.put("zdbId", StructureType.STRING_ARRAY);

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
		FIELD_TYPES.put("catalogLink", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("containedIn", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("collectionOne", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("collectionTwo", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("natureOfContent", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("publicationStatus", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("reviewStatus", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("professionalGroup", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("isPrimaryTopicOf", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("publication", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("hasItem", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("inCollection", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("sameAs", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("accessRights", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("bibliographicLevel", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("parallelEdition", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("describedby", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("contribution", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("fulltextOnline", StructureType.SIMPLEOBJECT_ARRAY);
		FIELD_TYPES.put("recordingLocation", StructureType.SIMPLEOBJECT_ARRAY);
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

			normalizeLicensesForPersistence(ktblAndTos);

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

	public static JSONObject getLobidMonographAsJson(String contentJsFile,
			String pid) {
		try {
			JSONObject lobid = new JSONObject(contentJsFile);
			JSONObject mapped = mapLobidMonographToTos(lobid, pid);
			normalizeLicensesForPersistence(mapped);
			return mapped;
		} catch (Exception e) {
			play.Logger.debug("Exception in getLobidMonographAsJson()" + e);
			return null;
		}
	}

	/**
	 * Maps Lobid JSON of a monograph to the toscience JSON format.
	 * 
	 * @param lobid Lobid JSON
	 * @param pid local pid
	 * @return mapped toscience JSON
	 * @throws JSONException if reading JSON fails
	 */
	private static JSONObject mapLobidMonographToTos(JSONObject lobid, String pid)
			throws JSONException {
		JSONObject mapped = new JSONObject();
		mapped.put("@id", pid);
		mapped.put("id", Globals.protocol + Globals.server + "/resource/" + pid);
		mapped.put("contentType", "monograph");

		putStringArray(mapped, "title", getStringValue(lobid.opt("title")));
		copyStringLikeAsArray(mapped, lobid, "extent");
		copyStringLikeAsArray(mapped, lobid, "hbzId");
		copyStringLikeAsArray(mapped, lobid, "almaMmsId");
		copyStringLikeAsArray(mapped, lobid, "deprecatedUri");
		copyStringLikeAsArray(mapped, lobid, "otherTitleInformation");
		copyStringLikeAsArray(mapped, lobid, "responsibilityStatement");
		copyStringLikeAsArray(mapped, lobid, "edition");
		copyStringLikeAsArray(mapped, lobid, "isbn", "Isbn");
		copyStringLikeAsArray(mapped, lobid, "issn");
		copyStringLikeAsArray(mapped, lobid, "note");
		copyStringLikeAsArray(mapped, lobid, "urn");
		copyStringLikeAsArray(mapped, lobid, "abstract", "abstractText");
		copyStringLikeAsArray(mapped, lobid, "zdbId");
		copyStringLikeAsArray(mapped, lobid, "oclcNumber");

		String issued = getIssuedFromLobidMonograph(lobid);
		putStringField(mapped, "issued", issued);

		if (issued != null && !issued.trim().isEmpty()) {
			putStringArray(mapped, "publicationYear", issued);
		}

		putIfNotEmpty(mapped, "language",
				normalizeLobidArray(lobid.optJSONArray("language")));
		putIfNotEmpty(mapped, "medium",
				normalizeLobidArray(lobid.optJSONArray("medium")));
		putIfNotEmpty(mapped, "natureOfContent",
				normalizeLobidArray(lobid.optJSONArray("natureOfContent")));
		putIfNotEmpty(mapped, "license", mapLicensesFromDescribedBy(lobid));
		putIfNotEmpty(mapped, "rdftype",
				mapMonographRdfTypes(lobid.optJSONArray("type")));
		putIfNotEmpty(mapped, "catalogLink", mapCatalogLinks(lobid));
		putIfNotEmpty(mapped, "lv:isPartOf", mapIsPartOf(lobid));
		putIfNotEmpty(mapped, "subject", mapMonographSubjects(lobid));
		putIfNotEmpty(mapped, "publication",
				normalizeLobidArray(lobid.optJSONArray("publication")));
		putIfNotEmpty(mapped, "hasItem",
				normalizeLobidArray(lobid.optJSONArray("hasItem")));
		putIfNotEmpty(mapped, "inCollection",
				normalizeLobidArray(lobid.optJSONArray("inCollection")));
		putIfNotEmpty(mapped, "sameAs",
				normalizeLobidArray(lobid.optJSONArray("sameAs")));
		putIfNotEmpty(mapped, "accessRights",
				normalizeLobidArray(lobid.optJSONArray("accessRights")));
		putIfNotEmpty(mapped, "bibliographicLevel",
				normalizeLobidArray(lobid.optJSONArray("bibliographicLevel")));
		putIfNotEmpty(mapped, "parallelEdition", mapParallelEdition(lobid));
		putIfNotEmpty(mapped, "describedby", mapDescribedBy(lobid));
		putIfNotEmpty(mapped, "contribution",
				normalizeLobidArray(lobid.optJSONArray("contribution")));
		putIfNotEmpty(mapped, "fulltextOnline", mapFulltextOnline(lobid));
		putIfNotEmpty(mapped, "bibo:doi", mapDoiValues(lobid));

		JSONObject contributions =
				mapMonographContributions(lobid.optJSONArray("contribution"));
		JSONArray creators = contributions.optJSONArray("creator");
		JSONArray contributors = contributions.optJSONArray("contributor");
		JSONArray contributorOrder = contributions.optJSONArray("contributorOrder");
		putIfNotEmpty(mapped, "creator", creators);
		putIfNotEmpty(mapped, "contributor", contributors);
		putIfNotEmpty(mapped, "contributorOrder", contributorOrder);

		return mapped;
	}

	/**
	 * Converts Lobid objects to simple objects with @id and prefLabel.
	 * 
	 * @param source source array
	 * @param idKey key for id
	 * @param labelKey key for label
	 * @return normalized array
	 * @throws JSONException if reading JSON fails
	 */
	private static JSONArray mapSimpleObjects(JSONArray source, String idKey,
			String labelKey) throws JSONException {
		JSONArray result = new JSONArray();
		if (source == null) {
			return result;
		}
		for (int i = 0; i < source.length(); i++) {
			if (!(source.get(i) instanceof JSONObject)) {
				continue;
			}
			JSONObject current = source.getJSONObject(i);
			JSONObject simpleObject = createSimpleObject(current.optString(idKey, ""),
					current.optString(labelKey, ""));
			if (simpleObject != null) {
				result.put(simpleObject);
			}
		}
		return result;
	}

	/**
	 * Reads license entries from Lobid describedBy.
	 * 
	 * @param lobid Lobid JSON
	 * @return license array
	 * @throws JSONException if reading JSON fails
	 */
	private static JSONArray mapLicensesFromDescribedBy(JSONObject lobid)
			throws JSONException {
		JSONObject describedBy = lobid.optJSONObject("describedBy");
		if (describedBy == null) {
			return new JSONArray();
		}
		return mapSimpleObjects(describedBy.optJSONArray("license"), "id", "label");
	}

	/**
	 * Reads the Lobid type values and maps them to the rdftype field in the
	 * toscience JSON.
	 * 
	 * @param types Lobid types
	 * @return rdf type array
	 * @throws JSONException if reading JSON fails
	 */
	private static JSONArray mapMonographRdfTypes(JSONArray types)
			throws JSONException {
		JSONArray result = new JSONArray();
		if (types == null) {
			return result;
		}
		for (int i = 0; i < types.length(); i++) {
			String rawType = types.optString(i, "").trim();
			String uri = toLobidTypeUri(rawType);
			if (uri == null || uri.isEmpty()) {
				continue;
			}
			String label = Globals.profile.getEtikett(uri).getLabel();
			if (label == null || label.trim().isEmpty() || label.equals(uri)) {
				label = rawType;
			}
			JSONObject simpleObject = createSimpleObject(uri, label);
			if (simpleObject != null) {
				result.put(simpleObject);
			}
		}
		return result;
	}

	/**
	 * Converts known Lobid type labels to URIs.
	 * 
	 * @param rawType type label or URI
	 * @return URI or null
	 */
	private static String toLobidTypeUri(String rawType) {
		if (rawType == null || rawType.trim().isEmpty()) {
			return null;
		}
		if (rawType.startsWith("http://") || rawType.startsWith("https://")) {
			return rawType;
		}
		switch (rawType) {
		case "BibliographicResource":
			return "http://purl.org/dc/terms/BibliographicResource";
		case "EditedVolume":
			return "http://purl.org/lobid/lv#EditedVolume";
		case "Book":
			return "http://purl.org/ontology/bibo/Book";
		case "Document":
			return "http://purl.org/ontology/bibo/Document";
		case "MultiVolumeBook":
			return "http://purl.org/ontology/bibo/MultiVolumeBook";
		case "Series":
			return "http://purl.org/ontology/bibo/Series";
		case "Periodical":
			return "http://purl.org/ontology/bibo/Periodical";
		case "Proceedings":
			return "http://purl.org/ontology/bibo/Proceedings";
		case "Thesis":
			return "http://purl.org/ontology/bibo/Thesis";
		case "Manifestation":
			return "http://purl.org/vocab/frbr/core#Manifestation";
		default:
			return null;
		}
	}

	/**
	 * Builds the catalogLink value from Lobid ids.
	 * 
	 * @param lobid Lobid JSON
	 * @return catalog link array
	 * @throws JSONException if reading JSON fails
	 */
	private static JSONArray mapCatalogLinks(JSONObject lobid)
			throws JSONException {
		JSONArray result = new JSONArray();
		String hbzId = lobid.optString("hbzId", "").trim();
		if (!hbzId.isEmpty()) {
			JSONObject catalogLink =
					createSimpleObject("https://lobid.org/resources/" + hbzId, hbzId);
			if (catalogLink != null) {
				result.put(catalogLink);
			}
			return result;
		}
		String deprecatedUri = lobid.optString("deprecatedUri", "").trim();
		if (!deprecatedUri.isEmpty()) {
			JSONObject catalogLink = createSimpleObject(deprecatedUri, deprecatedUri);
			if (catalogLink != null) {
				result.put(catalogLink);
			}
		}
		return result;
	}

	/**
	 * Uses the Lobid id as a simple parallelEdition value.
	 * 
	 * @param lobid Lobid JSON
	 * @return parallel edition array
	 * @throws JSONException if reading JSON fails
	 */
	private static JSONArray mapParallelEdition(JSONObject lobid)
			throws JSONException {
		JSONArray result = new JSONArray();
		String id = lobid.optString("id", "").trim();
		if (!id.isEmpty()) {
			JSONObject parallelEdition = new JSONObject();
			parallelEdition.put("@id", id);
			parallelEdition.put("prefLabel", id);
			result.put(parallelEdition);
		}
		return result;
	}

	/**
	 * Normalizes the Lobid describedBy object.
	 * 
	 * @param lobid Lobid JSON
	 * @return normalized describedby array
	 * @throws JSONException if reading JSON fails
	 */
	private static JSONArray mapDescribedBy(JSONObject lobid)
			throws JSONException {
		JSONArray result = new JSONArray();
		JSONObject describedBy = lobid.optJSONObject("describedBy");
		if (describedBy != null) {
			result.put(normalizeLobidObject(describedBy));
		}
		return result;
	}

	/**
	 * Reads parent relations from Lobid isPartOf.
	 * 
	 * @param lobid Lobid JSON
	 * @return containedIn array
	 * @throws JSONException if reading JSON fails
	 */
	private static JSONArray mapIsPartOf(JSONObject lobid) throws JSONException {
		JSONArray result = new JSONArray();
		JSONArray isPartOf = lobid.optJSONArray("isPartOf");
		if (isPartOf == null) {
			return result;
		}
		for (int i = 0; i < isPartOf.length(); i++) {
			JSONObject relation = isPartOf.optJSONObject(i);
			if (relation == null) {
				continue;
			}
			JSONObject mappedRelation = new JSONObject();
			JSONArray types = relation.optJSONArray("type");
			if (types != null && types.length() > 0) {
				JSONArray mappedTypes = new JSONArray();
				for (int j = 0; j < types.length(); j++) {
					String rawType = types.optString(j, "").trim();
					String uri = toLobidTypeUri(rawType);
					if (uri == null || uri.isEmpty()) {
						continue;
					}
					JSONObject mappedType = createSimpleObject(uri, uri);
					if (mappedType != null) {
						mappedTypes.put(mappedType);
					}
				}
				if (mappedTypes.length() > 0) {
					mappedRelation.put("rdftype", mappedTypes);
				}
			}
			JSONArray superordinates = relation.optJSONArray("hasSuperordinate");
			if (superordinates != null) {
				JSONArray mappedSuperordinates = new JSONArray();
				for (int j = 0; j < superordinates.length(); j++) {
					JSONObject current = superordinates.optJSONObject(j);
					if (current == null) {
						continue;
					}
					JSONObject simpleObject = createSimpleObject(
							current.optString("id", ""), current.optString("id", ""));
					if (simpleObject != null) {
						if (current.has("label")) {
							simpleObject.put("label", current.optString("label"));
						}
						mappedSuperordinates.put(simpleObject);
					}
				}
				if (mappedSuperordinates.length() > 0) {
					mappedRelation.put("hasSuperordinate", mappedSuperordinates);
				}
			}
			if (relation.has("numbering")) {
				mappedRelation.put("numbering", relation.optString("numbering"));
			}
			if (mappedRelation.length() > 0) {
				result.put(mappedRelation);
			}
		}
		return result;
	}

	/**
	 * Normalizes monograph subject entries.
	 * 
	 * @param lobid Lobid JSON
	 * @return normalized subject array
	 * @throws JSONException if reading JSON fails
	 */
	private static JSONArray mapMonographSubjects(JSONObject lobid)
			throws JSONException {
		JSONArray subjects = lobid.optJSONArray("subject");
		return normalizeLobidArray(subjects);
	}

	/**
	 * Splits Lobid contributions into creator, contributor and contributorOrder.
	 * 
	 * @param contributions Lobid contribution array
	 * @return object with three arrays
	 * @throws JSONException if reading JSON fails
	 */
	private static JSONObject mapMonographContributions(JSONArray contributions)
			throws JSONException {
		JSONObject result = new JSONObject();
		JSONArray creators = new JSONArray();
		JSONArray contributors = new JSONArray();
		JSONArray contributorOrder = new JSONArray();
		if (contributions == null) {
			result.put("creator", creators);
			result.put("contributor", contributors);
			result.put("contributorOrder", contributorOrder);
			return result;
		}

		for (int i = 0; i < contributions.length(); i++) {
			JSONObject contribution = contributions.optJSONObject(i);
			if (contribution == null) {
				continue;
			}
			JSONObject agent = contribution.optJSONObject("agent");
			JSONObject role = contribution.optJSONObject("role");
			if (agent == null) {
				continue;
			}
			String id = agent.optString("id", "").trim();
			String label = agent.optString("label", "").trim();
			JSONObject simpleObject = createSimpleObject(id, label);
			if (simpleObject == null) {
				continue;
			}
			String resolvedId = simpleObject.optString("@id", "");
			if (!resolvedId.isEmpty()) {
				contributorOrder.put(resolvedId);
			}

			String roleLabel = role != null ? role.optString("label", "") : "";
			if ("Autor/in".equals(roleLabel)) {
				creators.put(simpleObject);
			} else {
				contributors.put(simpleObject);
			}
		}

		result.put("creator", creators);
		result.put("contributor", contributors);
		result.put("contributorOrder", contributorOrder);
		return result;
	}

	/**
	 * Creates a simple object with @id and prefLabel. Missing ids get an ad-hoc
	 * URI.
	 * 
	 * @param id preferred identifier
	 * @param prefLabel preferred label
	 * @return simple object or null
	 * @throws JSONException if writing JSON fails
	 */
	private static JSONObject createSimpleObject(String id, String prefLabel)
			throws JSONException {
		String resolvedLabel = prefLabel != null ? prefLabel.trim() : "";
		String resolvedId = id != null ? id.trim() : "";
		if (resolvedLabel.isEmpty() && resolvedId.isEmpty()) {
			return null;
		}
		if (resolvedId.isEmpty()) {
			resolvedId = buildAdhocUri(resolvedLabel);
		}
		if (resolvedLabel.isEmpty()) {
			resolvedLabel = resolvedId;
		}
		JSONObject result = new JSONObject();
		result.put("@id", resolvedId);
		result.put("prefLabel", resolvedLabel);
		return result;
	}

	/**
	 * Reads the first publication date for the issued field.
	 * 
	 * @param lobid Lobid JSON
	 * @return issued value or null
	 */
	private static String getIssuedFromLobidMonograph(JSONObject lobid) {
		JSONArray publication = lobid.optJSONArray("publication");
		if (publication == null || publication.length() == 0) {
			return null;
		}
		JSONObject firstPublication = publication.optJSONObject(0);
		if (firstPublication == null) {
			return null;
		}
		String startDate = firstPublication.optString("startDate", "").trim();
		if (!startDate.isEmpty()) {
			return startDate;
		}
		String dateStatement =
				firstPublication.optString("dateStatement", "").trim();
		return dateStatement.isEmpty() ? null : dateStatement;
	}

	/**
	 * Writes one string value if it is not empty.
	 * 
	 * @param target target object
	 * @param key target key
	 * @param value value to write
	 * @throws JSONException if writing JSON fails
	 */
	private static void putStringField(JSONObject target, String key,
			String value) throws JSONException {
		if (value != null && !value.trim().isEmpty()) {
			target.put(key, value);
		}
	}

	/**
	 * Copies a Lobid value to a toscience string-array field with the same key.
	 * 
	 * @param target target object
	 * @param source source object
	 * @param key shared source and target key
	 * @throws JSONException if writing JSON fails
	 */
	private static void copyStringLikeAsArray(JSONObject target,
			JSONObject source, String key) throws JSONException {
		copyStringLikeAsArray(target, source, key, key);
	}

	/**
	 * Copies a Lobid value to a toscience string-array field.
	 * 
	 * @param target target object
	 * @param source source object
	 * @param sourceKey source key
	 * @param targetKey target key
	 * @throws JSONException if writing JSON fails
	 */
	private static void copyStringLikeAsArray(JSONObject target,
			JSONObject source, String sourceKey, String targetKey)
			throws JSONException {
		Object value = source.opt(sourceKey);
		if (value == null || value == JSONObject.NULL) {
			return;
		}
		if (value instanceof JSONArray) {
			target.put(targetKey, value);
			return;
		}
		String text = String.valueOf(value).trim();
		if (text.isEmpty()) {
			return;
		}
		putStringArray(target, targetKey, text);
	}

	/**
	 * Puts one string into a JSON array.
	 * 
	 * @param target target object
	 * @param key target key
	 * @param value string value
	 * @throws JSONException if writing JSON fails
	 */
	private static void putStringArray(JSONObject target, String key,
			String value) throws JSONException {
		if (value == null || value.trim().isEmpty()) {
			return;
		}
		JSONArray values = new JSONArray();
		values.put(value);
		target.put(key, values);
	}

	/**
	 * Writes an array only if it is not empty.
	 * 
	 * @param target target object
	 * @param key target key
	 * @param value array to write
	 * @throws JSONException if writing JSON fails
	 */
	private static void putIfNotEmpty(JSONObject target, String key,
			JSONArray value) throws JSONException {
		if (value != null && value.length() > 0) {
			target.put(key, value);
		}
	}

	/**
	 * Returns one string value. If the input is an array, it uses the first
	 * value.
	 * 
	 * @param value string or array input
	 * @return extracted string or null
	 */
	private static String getStringValue(Object value) {
		if (value == null || value == JSONObject.NULL) {
			return null;
		}
		if (value instanceof JSONArray) {
			JSONArray values = (JSONArray) value;
			return values.length() > 0 ? values.optString(0, null) : null;
		}
		return String.valueOf(value);
	}

	/**
	 * Normalizes all values in a Lobid array.
	 * 
	 * @param source source array
	 * @return normalized array
	 * @throws JSONException if reading JSON fails
	 */
	private static JSONArray normalizeLobidArray(JSONArray source)
			throws JSONException {
		JSONArray result = new JSONArray();
		if (source == null) {
			return result;
		}
		for (int i = 0; i < source.length(); i++) {
			result.put(normalizeLobidValue(source.get(i)));
		}
		return result;
	}

	/**
	 * Reads links that should be used as fulltext references.
	 * 
	 * @param lobid Lobid JSON
	 * @return fulltextOnline array
	 * @throws JSONException if reading JSON fails
	 */
	private static JSONArray mapFulltextOnline(JSONObject lobid)
			throws JSONException {
		JSONArray result = new JSONArray();
		JSONArray sameAs = lobid.optJSONArray("sameAs");
		if (sameAs != null) {
			for (int i = 0; i < sameAs.length(); i++) {
				JSONObject current = sameAs.optJSONObject(i);
				if (current == null) {
					continue;
				}
				String id = current.optString("id", "").trim();
				String label = current.optString("label", "").trim();
				JSONObject simpleObject = createSimpleObject(id, label);
				if (simpleObject != null) {
					result.put(simpleObject);
				}
			}
		}
		return result;
	}

	/**
	 * Reads DOI values from Lobid sameAs links.
	 * 
	 * @param lobid Lobid JSON
	 * @return DOI array without the URL prefix
	 */
	private static JSONArray mapDoiValues(JSONObject lobid) {
		JSONArray result = new JSONArray();
		JSONArray sameAs = lobid.optJSONArray("sameAs");
		if (sameAs == null) {
			return result;
		}
		for (int i = 0; i < sameAs.length(); i++) {
			JSONObject current = sameAs.optJSONObject(i);
			if (current == null) {
				continue;
			}
			String id = current.optString("id", "").trim();
			if (id.startsWith("https://doi.org/")) {
				result.put(id.replace("https://doi.org/", ""));
			} else if (id.startsWith("http://doi.org/")) {
				result.put(id.replace("http://doi.org/", ""));
			}
		}
		return result;
	}

	/**
	 * Normalizes a Lobid value. Strings stay unchanged.
	 * 
	 * @param value source value
	 * @return normalized value
	 * @throws JSONException if reading JSON fails
	 */
	private static Object normalizeLobidValue(Object value) throws JSONException {
		if (value == null || value == JSONObject.NULL) {
			return JSONObject.NULL;
		}
		if (value instanceof JSONObject) {
			return normalizeLobidObject((JSONObject) value);
		}
		if (value instanceof JSONArray) {
			return normalizeLobidArray((JSONArray) value);
		}
		return value;
	}

	/**
	 * Normalizes a Lobid object to the internal JSON format.
	 * 
	 * @param source source object
	 * @return normalized object
	 * @throws JSONException if reading JSON fails
	 */
	private static JSONObject normalizeLobidObject(JSONObject source)
			throws JSONException {
		JSONObject result = new JSONObject();
		Iterator<String> keys = source.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			Object value = source.get(key);
			String normalizedKey = normalizeLobidKey(key);
			Object normalizedValue = normalizeLobidValue(value);
			if (shouldWrapSingleObject(normalizedKey)
					&& normalizedValue instanceof JSONObject) {
				JSONArray wrapped = new JSONArray();
				wrapped.put(normalizedValue);
				result.put(normalizedKey, wrapped);
			} else {
				result.put(normalizedKey, normalizedValue);
			}
		}
		if (result.has("@id") && !result.has("prefLabel")) {
			if (result.has("label") && result.opt("label") instanceof String) {
				result.put("prefLabel", result.optString("label"));
			} else {
				result.put("prefLabel", result.optString("@id"));
			}
		}
		if (result.has("type") && !result.has("rdftype")) {
			result.put("rdftype", result.remove("type"));
		}
		return result;
	}

	/**
	 * Rewrites Lobid field names to internal field names.
	 * 
	 * @param key Lobid key
	 * @return normalized key
	 */
	private static String normalizeLobidKey(String key) {
		if ("id".equals(key)) {
			return "@id";
		}
		if ("type".equals(key)) {
			return "rdftype";
		}
		return key;
	}

	/**
	 * Some nested object fields are always stored as arrays.
	 * 
	 * @param key normalized key
	 * @return true if the value should be wrapped into an array
	 */
	private static boolean shouldWrapSingleObject(String key) {
		return "agent".equals(key) || "role".equals(key) || "heldBy".equals(key)
				|| "inCollection".equals(key) || "license".equals(key)
				|| "inDataset".equals(key) || "resultOf".equals(key)
				|| "object".equals(key) || "provider".equals(key)
				|| "sourceOrganization".equals(key) || "modifiedBy".equals(key);
	}

	private static void normalizeLicensesForPersistence(JSONObject metadata)
			throws JSONException {

		boolean isArticle =
				"article".equalsIgnoreCase(metadata.optString("contentType"));

		if (!metadata.has("license")) {
			return;
		}

		JSONArray licenseArray = metadata.getJSONArray("license");
		for (int i = 0; i < licenseArray.length(); i++) {
			JSONObject licenseObject = licenseArray.getJSONObject(i);
			if (!licenseObject.has("prefLabel")) {
				licenseObject.put("prefLabel", licenseObject.opt("@id"));
			} else if (isArticle) {
				normalizeArticleLicensePrefLabel(licenseObject);
			}
		}
	}

	private static void normalizeArticleLicensePrefLabel(JSONObject licenseObject)
			throws JSONException {
		String licenseId = licenseObject.optString("@id", "").trim();
		String prefLabel = licenseObject.optString("prefLabel", "").trim();

		if (!licenseId.isEmpty() && licenseId.startsWith("http")
				&& !prefLabel.isEmpty() && !prefLabel.startsWith("http")
				&& !prefLabel.equals(licenseId)) {
			licenseObject.put("prefLabel", licenseId);
		}
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

	/**
	 * Checks the JSON structure and fixes missing or wrong field formats. It also
	 * adds the local pid as @id if needed.
	 * 
	 * @param allMd metadata JSON
	 * @param n current node
	 * @return validated JSON
	 */
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
			// normalizeArticleLicensePrefLabel(allMd);

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

	private static JSONObject copyJsonObject(JSONObject source)
			throws JSONException {
		return new JSONObject(source.toString());
	}

	private static void persistMissingTosMd(String pid, Node node)
			throws JSONException {
		Map<String, Object> map = null;

		if (Helper.mdStreamExists(pid, "metadata2")) {
			map = RdfHelper.getRdfAsMap(node, RDFFormat.NTRIPLES,
					node.getMetadata("metadata2"));

		} else if (!Helper.mdStreamExists(pid, "metadata2")
				&& Helper.mdStreamExists(pid, "metadata")) {
			map = RdfHelper.getRdfAsMap(node, RDFFormat.NTRIPLES,
					node.getMetadata("metadata"));
		}

		if (map != null) {
			JSONObject allMd = new JSONObject(map);
			allMd = TosHelper.getPrefLabelsResolved(allMd);
			modify.updateMetadata("toscience", node, allMd.toString());
		} else {
			play.Logger
					.debug("No metadata2/metadata data stream found for pid=" + pid);
		}
	}

	private static void normalizeExistingToMd(Node node) throws JSONException {
		JSONObject allMd = new JSONObject(node.getMetadata("toscience"));
		JSONObject original = copyJsonObject(allMd);
		allMd = TosHelper.validateJsonStructure(allMd, node);

		addRdftypeIfMissing(allMd);

		allMd = TosHelper.getPrefLabelsResolved(allMd);

		if (!original.similar(allMd)) {
			modify.updateMetadata("toscience", node, allMd.toString());
			node.getLd2();
		}
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
	public static void persistAndNormalizeTosMd(String pid, Node node) {
		try {
			if (node == null || pid == null || pid.trim().isEmpty()) {
				play.Logger.debug("persistAndNormalizeTosMd(): node=null");
				return;
			}

			// Case 1: toscience does not exist and will be persisted
			if (!Helper.mdStreamExists(pid, "toscience")
					|| node.getMetadata("toscience").length() < 5) {
				persistMissingTosMd(pid, node);

				// Case 2: toscience exists, but may have JSON elements with invalid
				// structures
			} else if (TosHelper.isValidJson(node.getMetadata("toscience"))) {
				normalizeExistingToMd(node);
			}
		} catch (Exception e) {
			play.Logger.debug("Exception in persistAndNormalizeTosMd() " + e);
		}
	}

	/**
	 * This method is an extension of the `persistAndNormalizeToScienceMetadata`
	 * method. It handles child objects if they exist.
	 * 
	 * @param pid
	 * @param node
	 */
	public static void ensureTosMdForRead(String pid, Node node) {
		if (node == null || pid == null || pid.trim().isEmpty()) {
			play.Logger.debug("ensureTosMdForRead(): node is null");
			return;
		}

		persistAndNormalizeTosMd(pid, node);
		persistTosMdForParts(node);
	}

	private static void persistTosMdForParts(Node node) {
		List<Node> parts = new Read().getParts(node);

		if (parts == null || parts.isEmpty()) {
			return;
		}

		for (Node child : parts) {
			if (child != null && child.getPid() != null) {
				persistAndNormalizeTosMd(child.getPid(), child);
			}
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
		ensureTosMdForRead(pid, node);
	}
}
