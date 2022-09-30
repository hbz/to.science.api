/*
 * Copyright 2015 hbz NRW (http://www.hbz-nrw.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package helper;

import static archive.fedora.FedoraVocabulary.HAS_PART;
import static archive.fedora.FedoraVocabulary.IS_PART_OF;
import static archive.fedora.Vocabulary.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.core.util.JsonUtil;

import actions.Read;
import archive.fedora.RdfUtils;
import de.hbz.lobid.helper.EtikettMakerInterface;
import de.hbz.lobid.helper.JsonConverter;
import models.implementation.*;
import models.Globals;
import models.Link;
import models.Node;
import util.AdHocUriProvider;
import util.LobidMapperUtils;
import helper.GenericPropertiesLoader;

/**
 * @author jan schnasse
 * @author Ingolf Kuss, hbz
 *
 */

@SuppressWarnings("unchecked")
public class JsonMapper {

	private static final String PREF_LABEL = Globals.profile.getLabelKey();
	private static final String ID2 = Globals.profile.getIdAlias();
	/**
	 * Here are some short names that must be defined in the context document that
	 * is loaded at Globals.context.
	 */
	final static String primaryTopic = "primaryTopic";
	final static String contentType = "contentType";
	final static String accessScheme = "accessScheme";
	final static String publishScheme = "publishScheme";
	final static String transformer = "transformer";
	final static String catalogId = "catalogId";
	final static String createdBy = "createdBy";

	final static String submittedBy = "submittedBy";
	final static String submittedByEmail = "submittedByEmail";
	final static String legacyId = "legacyId";
	final static String importedFrom = "importedFrom";
	final static String name = "name";
	final static String urn = "urn";
	final static String lastModifiedBy = "lastModifiedBy";
	final static String modified = "modified";
	final static String objectTimestamp = "objectTimestamp";
	final static String created = "created";
	final static String describes = "describes";
	final static String isDescribedBy = "isDescribedBy";
	final static String doi = "doi";
	final static String parentPid = "parentPid";
	final static String format = "format";
	final static String size = "size";
	final static String checksumValue = "checksumValue";
	final static String generator = "generator";
	final static String rdftype = "rdftype";
	final static String checksum = "checksum";
	final static String hasData = "hasData";
	final static String fulltext_ocr = "fulltext-ocr";
	final static String title = "title";
	final static String fileLabel = "fileLabel";
	final static String embargoTime = "embargoTime";

	final static String[] typePrios = new String[] {
			"http://purl.org/lobid/lv#ArchivedWebPage",
			"http://purl.org/ontology/bibo/Report",
			"http://purl.org/lobid/lv#Biography", "http://purl.org/library/Game",
			"http://purl.org/lobid/lv#Schoolbook",
			"http://purl.org/ontology/mo/PublishedScore",
			"http://purl.org/lobid/lv#Legislation",
			"http://purl.org/ontology/bibo/ReferenceSource",
			"http://purl.org/lobid/lv#OfficialPublication",
			"http://purl.org/lobid/lv#Bibliography",
			"http://purl.org/lobid/lv#Festschrift",
			"http://purl.org/ontology/bibo/Proceedings",
			"http://hbz-nrw.de/regal#ResearchData",
			"http://purl.org/lobid/lv#EditedVolume",
			"http://purl.org/ontology/bibo/Thesis",
			"http://purl.org/ontology/mo/Record", "http://purl.org/ontology/bibo/Map",
			"http://purl.org/ontology/bibo/AudioVisualDocument",
			"http://purl.org/ontology/bibo/AudioDocument",
			"http://purl.org/ontology/bibo/Image",
			"http://purl.org/ontology/bibo/Article",
			"http://rdvocab.info/termList/RDACarrierType/1018",
			"http://rdvocab.info/termList/RDACarrierType/1010",
			"http://iflastandards.info/ns/isbd/terms/mediatype/T1002",
			"http://purl.org/ontology/bibo/MultiVolumeBook",
			"http://purl.org/ontology/bibo/Journal",
			"http://purl.org/ontology/bibo/Newspaper",
			"http://purl.org/ontology/bibo/Series",
			"http://purl.org/ontology/bibo/Periodical",
			"http://purl.org/ontology/bibo/Collection",
			"http://purl.org/ontology/bibo/Standard",
			"http://purl.org/lobid/lv#Statistics",
			"http://purl.org/ontology/bibo/Book",
			"http://data.archiveshub.ac.uk/def/ArchivalResource",
			"http://purl.org/ontology/bibo/Document",
			"http://purl.org/vocab/frbr/core#Manifestation",
			"http://purl.org/lobid/lv#Miscellaneous",
			"http://purl.org/dc/terms/BibliographicResource",
			"info:regal/zettel/File", "http://hbz-nrw.de/regal#oerAgent" };

	Node node = null;
	String toscience_id = null;
	EtikettMakerInterface profile = Globals.profile;
	JsonConverter jsonConverter = null;

	/**
	 * Ein Konstruktor für diese Klasse
	 */
	public JsonMapper() {
		play.Logger.info("Creating new instance of Class JsonMapper");
		jsonConverter = new JsonConverter(profile);
	}

	/**
	 * Ein Konstruktor für diese Klasse, falls Metadata2-Datenstrom schon
	 * vorhanden sein muss.
	 * 
	 * @param n the node will be mapped to json ld in accordance to the profile
	 */
	public JsonMapper(final Node n) {
		try {
			jsonConverter = new JsonConverter(profile);
			this.node = n;
			if (node == null)
				throw new NullPointerException(
						"JsonMapper can not work on node with value NULL!");
			// if (node.getMetadata1() == null)
			// throw new NullPointerException(
			// node.getPid() + " metadata stream is NULL!");
			if (node.getMetadata(metadata2) == null)
				throw new NullPointerException(
						node.getPid() + " metadata2 stream is NULL!");
		} catch (Exception e) {
			play.Logger.warn("", e.getMessage());
			// play.Logger.debug("", e);
		}

	}

	/**
	 * @return a map without the context document
	 */
	public Map<String, Object> getLdWithoutContext() {
		Map<String, Object> map = getLd();
		map.remove("@context");
		return map;
	}

	/**
	 * @return a map without the context document
	 */
	public Map<String, Object> getLd2WithoutContext() {
		Map<String, Object> map = getLd2();
		map.remove("@context");
		return map;

	}

	/**
	 * @return a map without the context document
	 */
	public Map<String, Object> getLdWithoutContextShortStyle() {
		Map<String, Object> map = getLdShortStyle();
		map.remove("@context");
		return map;
	}

	/**
	 * Holt Metadaten im Format lobid --- VERALTET ! DEPRECATED !
	 * 
	 * @return a map representing the rdf data on this object
	 * @deprecated Ld stream is replaced by LD2 stream
	 */
	@Deprecated
	public Map<String, Object> getLd() {
		Collection<Link> ls = node.getRelsExt();
		Map<String, Object> m = getDescriptiveMetadata1();
		Map<String, Object> rdf = m == null ? new HashMap<>() : m;

		changeDcIsPartOfToRegalIsPartOf(rdf);
		rdf.remove("describedby");
		rdf.remove("sameAs");

		rdf.put(ID2, node.getPid());
		rdf.put(primaryTopic, node.getPid());
		for (Link l : ls) {
			if (HAS_PART.equals(l.getPredicate()))
				continue;
			if (REL_HBZ_ID.equals(l.getPredicate()))
				continue;
			if (IS_PART_OF.equals(l.getPredicate()))
				continue;
			addLinkToJsonMap(rdf, l);
		}
		addPartsToJsonMap(rdf);
		rdf.remove("isNodeType");

		rdf.put(contentType, node.getContentType());
		rdf.put(accessScheme, node.getAccessScheme());
		rdf.put(publishScheme, node.getPublishScheme());
		rdf.put(transformer, node.getTransformer().stream().map(t -> t.getId())
				.collect(Collectors.toList()));
		rdf.put(catalogId, node.getCatalogId());
		// rdf.put(embargoTime, node.getEmbargoTime());

		if (node.getFulltext() != null)
			rdf.put(fulltext_ocr, node.getFulltext());

		Map<String, Object> aboutMap = new LinkedHashMap<>();
		aboutMap.put(ID2, node.getAggregationUri() + ".rdf");
		if (node.getCreatedBy() != null)
			aboutMap.put(createdBy, node.getCreatedBy());
		if (node.getSubmittedBy() != null)
			aboutMap.put(submittedBy, node.getSubmittedBy());
		if (node.getSubmittedByEmail() != null)
			aboutMap.put(submittedByEmail, node.getSubmittedByEmail());
		if (node.getLegacyId() != null)
			aboutMap.put(legacyId, node.getLegacyId());
		if (node.getImportedFrom() != null)
			aboutMap.put(importedFrom, node.getImportedFrom());
		if (node.getName() != null)
			aboutMap.put(name, node.getName());
		if (node.getLastModifiedBy() != null)
			aboutMap.put(lastModifiedBy, node.getLastModifiedBy());

		aboutMap.put(modified, node.getLastModified());
		if (node.getObjectTimestamp() != null) {
			aboutMap.put(objectTimestamp, node.getObjectTimestamp());
		} else {
			aboutMap.put(objectTimestamp, node.getLastModified());
		}
		aboutMap.put(created, node.getCreationDate());
		aboutMap.put(describes, node.getAggregationUri());

		rdf.put(isDescribedBy, aboutMap);
		if (node.getDoi() != null) {
			rdf.put(doi, node.getDoi());
		}
		if (node.getUrn() != null) {
			rdf.put(urn, node.getUrn());
		}

		if (node.getParentPid() != null)
			rdf.put(parentPid, node.getParentPid());

		if (node.getMimeType() != null && !node.getMimeType().isEmpty()) {
			Map<String, Object> hasDataMap = new LinkedHashMap<>();
			hasDataMap.put(ID2, node.getDataUri());
			hasDataMap.put(format, node.getMimeType());
			hasDataMap.put(size, node.getFileSize());
			if (node.getFileLabel() != null)
				hasDataMap.put(fileLabel, node.getFileLabel());
			if (node.getChecksum() != null) {
				Map<String, Object> checksumMap = new LinkedHashMap<>();
				checksumMap.put(checksumValue, node.getChecksum());
				checksumMap.put(generator, "http://en.wikipedia.org/wiki/MD5");
				checksumMap.put(rdftype,
						"http://downlode.org/Code/RDF/File_Properties/schema#Checksum");
				hasDataMap.put(checksum, checksumMap);
			}
			rdf.put(hasData, hasDataMap);
		}
		rdf.put("@context", Globals.protocol + Globals.server + "/context.json");
		postprocessing(rdf);
		rdf.remove("note");
		return rdf;
	}

	private static void changeDcIsPartOfToRegalIsPartOf(Map<String, Object> rdf) {
		Object pid = rdf.get("parentPid");
		if (pid != null) {
			rdf.put("externalParent", pid);
			rdf.remove("parentPid");
		}
	}

	private Map<String, Object> getDescriptiveMetadata1() {
		try {
			InputStream stream = new ByteArrayInputStream(
					node.getMetadata("metadata").getBytes(StandardCharsets.UTF_8));
			Map<String, Object> rdf = jsonConverter.convert(node.getPid(), stream,
					RDFFormat.NTRIPLES, profile.getContext().get("@context"));
			return rdf;
		} catch (Exception e) {
			play.Logger
					.warn(node.getPid() + " can not create JSON! " + e.getMessage());
			play.Logger.trace("", e);
		}
		return null;
	}

	/**
	 * Holt Metadaten im Format lobid2 als Java Map
	 * 
	 * @return
	 */
	private Map<String, Object> getDescriptiveMetadata2() {
		try {
			InputStream stream = new ByteArrayInputStream(
					node.getMetadata(metadata2).getBytes(StandardCharsets.UTF_8));
			Map<String, Object> rdf = jsonConverter.convert(node.getPid(), stream,
					RDFFormat.NTRIPLES, profile.getContext().get("@context"));
			return rdf;
		} catch (Exception e) {
			play.Logger.warn(node.getPid()
					+ " has no descriptive Metadata2! Try to return metadata instead.");
			// play.Logger.debug("", e);
		}
		return getDescriptiveMetadata1();
	}

	/**
	 * @return linked data json optimized for displaying large trees. Most of the
	 *         metadata has been left out.
	 */
	public Map<String, Object> getLdShortStyle() {
		Collection<Link> ls = node.getRelsExt();
		Map<String, Object> m = getDescriptiveMetadata1();
		Map<String, Object> rdf = m == null ? new HashMap<>() : m;
		rdf.put(ID2, node.getPid());
		for (Link l : ls) {
			if (getUriFromJsonName(title).equals(l.getPredicate())) {
				addLinkToJsonMap(rdf, l);
				break;
			}
		}
		addPartsToJsonMap(rdf);
		rdf.remove("isNodeType");
		rdf.put(contentType, node.getContentType());
		if (node.getParentPid() != null)
			rdf.put(parentPid, node.getParentPid());
		if (node.getMimeType() != null && !node.getMimeType().isEmpty()) {
			Map<String, Object> hasDataMap = new HashMap<>();
			hasDataMap.put(ID2, node.getDataUri());
			hasDataMap.put(format, node.getMimeType());
			hasDataMap.put(size, node.getFileSize());
			if (node.getChecksum() != null) {
				Map<String, Object> checksumMap = new HashMap<>();
				checksumMap.put(checksumValue, node.getChecksum());
				checksumMap.put(generator, "http://en.wikipedia.org/wiki/MD5");
				checksumMap.put(rdftype,
						"http://downlode.org/Code/RDF/File_Properties/schema#Checksum");
				hasDataMap.put(checksum, checksumMap);
			}
			rdf.put("hasData", hasDataMap);
		}
		postprocessing(rdf);
		return rdf;
	}

	/**
	 * @param rdf
	 */
	private void postprocessing(Map<String, Object> rdf) {
		try {
			addCatalogLink(rdf);
			if ("file".equals(rdf.get("contentType"))) {
				rdf.put(rdftype, Arrays.asList(new String[] { "File" }));
			}

			Collection<Map<String, Object>> t =
					getType(new ObjectMapper().valueToTree(rdf));
			if (t != null && t.size() != 0)
				rdf.put(rdftype, t);

			sortCreatorAndContributors(rdf);
			postProcessSubjectName(rdf);
			postProcess(rdf, "subject");
			postProcess(rdf, "agrovoc");
			postProcess(rdf, "contributor");
			postProcess(rdf, "redaktor");
			postProcess(rdf, "actor");
			postProcess(rdf, "producer");
			postProcess(rdf, "interviewer");
			postProcess(rdf, "collaborator");
			postProcess(rdf, "cartographer");
			postProcess(rdf, "director");
			postProcess(rdf, "cinematographer");
			postProcess(rdf, "photographer");
			postProcess(rdf, "engraver");
			postProcess(rdf, "contributor_");
			postProcess(rdf, "dedicatee");
			postProcess(rdf, "honoree");
			postProcess(rdf, "singer");
			postProcess(rdf, "professionalGroup");
			postProcess(rdf, "editor");
			postProcess(rdf, "publisher");
			postProcess(rdf, "recordingLocation");
			postProcess(rdf, "recordingCoords");
			postProcess(rdf, "collectionOne");
			postProcess(rdf, "medium");
			postProcess(rdf, "predecessor");
			postProcess(rdf, "successor");
			postProcess(rdf, "primaryForm");
			postProcess(rdf, "natureOfContent");
			postProcess(rdf, "institution");
			postProcessContribution(rdf);
			postProcess(rdf, "creator");
			postProcessLinkFields("additionalMaterial", rdf);
			postProcessLinkFields("publisherVersion", rdf);
			postProcessLinkFields("fulltextVersion", rdf);
			createJoinedFunding(rdf);
			applyAffiliation("creator", rdf);
			applyAffiliation("contributor", rdf);
			applyAcademicDegree("creator", rdf);
			applyAcademicDegree("contributor", rdf);

			postProcessWithGenPropLoader("department", "department-de.properties",
					rdf);
			postProcessWithGenPropLoader("funder", "funder-de.properties", rdf);

		} catch (Exception e) {
			play.Logger.debug("", e);
		}
	}

	/**
	 * @param key
	 * @param rdf
	 */
	private void postProcessLinkFields(String key, Map<String, Object> rdf) {
		List<Map<String, String>> all = (List<Map<String, String>>) rdf.get(key);
		if (all == null)
			return;
		for (Map<String, String> m : all) {
			m.put(PREF_LABEL, m.get(ID2));
		}

	}

	/**
	 * @param rdf
	 */
	private static void postProcessSubjectName(Map<String, Object> rdf) {
		List<Map<String, Object>> newSubjects = new ArrayList<>();
		Set<String> subjects = (Set<String>) rdf.get("subjectName");
		if (subjects == null || subjects.isEmpty()) {
			return;
		}
		subjects.forEach((subject) -> {
			String id = Globals.protocol + Globals.server + "/adhoc/uri/"
					+ helper.Base64UrlCoder.encode(subject);
			Map<String, Object> subjectMap = new HashMap<>();
			subjectMap.put(PREF_LABEL, subject);
			subjectMap.put(ID2, id);
			newSubjects.add(subjectMap);
		});
		rdf.remove("subjectName");
		List<Map<String, Object>> oldSubjects =
				(List<Map<String, Object>>) rdf.get("subject");
		if (oldSubjects == null) {
			oldSubjects = new ArrayList<>();
		}
		oldSubjects.addAll(newSubjects);
		rdf.put("subject", oldSubjects);
	}

	/**
	 * @param rdf
	 */
	private static void createJoinedFunding(Map<String, Object> rdf) {

		List<Map<String, Object>> fundingId =
				(List<Map<String, Object>>) rdf.get("fundingId");
		if (fundingId == null) {
			fundingId = new ArrayList<>();
		}
		List<String> fundings = (List<String>) rdf.get("funding");
		if (fundings != null) {
			for (String funding : fundings) {
				Map<String, Object> fundingJoinedMap = new LinkedHashMap<>();
				fundingJoinedMap.put(ID2, Globals.protocol + Globals.server
						+ "/adhoc/uri/" + helper.Base64UrlCoder.encode(funding));
				fundingJoinedMap.put(PREF_LABEL, funding);
				fundingId.add(fundingJoinedMap);
			}
			rdf.remove("funding");
		}
		List<String> fundingProgram = (List<String>) rdf.get("fundingProgram");
		/*
		 * in case of casting problems: List<String> fundingProgram = new
		 * ArrayList<String>((java.util.HashSet) rdf.get("fundingProgram"));
		 */
		List<String> projectId = (List<String>) rdf.get("projectId");

		List<Map<String, Object>> joinedFundings = new ArrayList<>();
		if (fundingId.isEmpty())
			return;
		for (int i = 0; i < fundingId.size(); i++) {
			// play.Logger.info(fundingId.get(i));
			Map<String, Object> f = new LinkedHashMap<>();
			Map<String, Object> fundingJoinedMap = new LinkedHashMap<>();
			fundingJoinedMap.put(ID2, fundingId.get(i).get(ID2));
			fundingJoinedMap.put(PREF_LABEL, fundingId.get(i).get(PREF_LABEL));
			f.put("fundingJoined", fundingJoinedMap);
			f.put("fundingProgramJoined", fundingProgram.get(i));
			f.put("projectIdJoined", projectId.get(i));
			joinedFundings.add(f);
		}
		rdf.put("joinedFunding", joinedFundings);
		rdf.put("fundingId", fundingId);
	}

	/**
	 * fetch the affiliation information from flat rdf statement and put them to
	 * the according agents object in rdf
	 * 
	 * @param rdf
	 */
	public void applyAffiliation(String agentType, Map<String, Object> rdf) {

		// set different variable names for creators and contributors

		LinkedHashMap<String, String> affilLabelMap = getPrefLabelMap(
				agentType + "ResearchOrganizationsRegistry-de.properties");

		List<String> agentAffiliation = new ArrayList<>();
		if (rdf.containsKey(agentType + "Affiliation")) {
			try {
				agentAffiliation =
						(ArrayList<String>) rdf.get(agentType + "Affiliation");
			} catch (Exception e) {
				play.Logger.warn("Found no ArrayList, found HashSet");
				// agentAffiliation =
				// castHashSet((HashSet<String>) rdf.get(agentType + "Affiliation"));
			}
			play.Logger.debug("Amount of " + agentType + " " + agentType
					+ "Affiliation" + " in flat list: " + agentAffiliation.size());
		}

		if (rdf.containsKey(agentType)) {
			Object agentsMap = rdf.get(agentType);
			Iterator cit = getLobidObjectIterator(agentsMap);
			int i = 0;
			while (cit.hasNext()) {
				// write the next creatorObject into map
				Map<String, Object> agent = (Map<String, Object>) cit.next();
				Map<String, String> affilFields = new LinkedHashMap<>();
				if (i < agentAffiliation.size()) {
					play.Logger.debug("found affiliation: " + agentAffiliation.get(i)
							+ " on position " + i);
					affilFields.put("@id",
							agentAffiliation.get(i).replace(
									"http://hbz-nrw.de/regal#" + agentType + "Affiliation",
									"https://ror.org"));
					affilFields.put("prefLabel",
							affilLabelMap.get(agentAffiliation.get(i)));
					affilFields.put("type", "Organization");
					agent.put("affiliation", affilFields);
				} else {
					// merde: we have more agents than affiliations.
					// Something went wrong
					// prevent existing affiliations from being overwritten by default
					if (!agent.containsKey("affiliation")) {
						play.Logger.debug("Using default affiliation for " + agentType + " "
								+ agent.get("@id") + " = " + agent.get(PREF_LABEL));
						affilFields.put("@id", "https://ror.org/04tsk2644");
						affilFields.put("prefLabel", "Ruhr-Universität Bochum");
						affilFields.put("type", "Organization");
						agent.put("affiliation", affilFields);
					}
				}
				i++;
			}
		}
	}

	/**
	 * fetch the academic degree information from flat rdf statement and put them
	 * to the according agent object in rdf
	 * 
	 * @param rdf a Map
	 */
	public void applyAcademicDegree(String agentType, Map<String, Object> rdf) {

		ArrayList<String> academicDegree = new ArrayList<>();
		if (rdf.get(agentType + "AcademicDegree") != null) {
			try {
				academicDegree =
						(ArrayList<String>) rdf.get(agentType + "AcademicDegree");
			} catch (Exception e) {
				play.Logger.warn("Found no ArrayList.");
				// academicDegree = castHashSet(
				// (HashSet<String>) rdf.get(agentType + "AcademicDegree"));
			}
			play.Logger.debug("Amount of " + agentType + " " + agentType
					+ "AcademicDegree" + " in flat list: " + academicDegree.size());
		}

		if (rdf.containsKey(agentType))

		{
			Object agentsMap = rdf.get(agentType);
			Iterator cit = getLobidObjectIterator(agentsMap);
			int i = 0;
			while (cit.hasNext()) {
				Map<String, Object> agent = (Map<String, Object>) cit.next();
				if (i < academicDegree.size()) {
					play.Logger.debug("found academicDegree: " + academicDegree.get(i)
							+ " on position " + i);
					agent.put("academicDegree", academicDegree.get(i).replace(
							"http://hbz-nrw.de/regal#" + agentType + "AcademicDegree/", ""));
				} else {
					/*
					 * Es sind nicht genügend akademische Grade in der sequentiellen Liste
					 * in RDF vorhanden. Daher wird für diesen Autor ein Default-Wert
					 * verwendet.
					 */
					if (!agent.containsKey("academicDegree")) {
						play.Logger.debug("Using default academic degree for " + agentType
								+ " " + agent.get(PREF_LABEL));
						agent.put("academicDegree", "unknown");
					}
				}
				i++;
			}
		}
	}

	/**
	 * @param key
	 * @param propertiesFileName
	 * @param rdf
	 */
	private void postProcessWithGenPropLoader(String key,
			String propertiesFileName, Map<String, Object> rdf) {

		List<Map<String, Object>> keyList = new ArrayList<>();

		// Provide resolving for prefLabels from @id via GenericPropertiesLoader
		LinkedHashMap<String, String> genPropMap = new LinkedHashMap<>();
		GenericPropertiesLoader genProp = new GenericPropertiesLoader();
		genPropMap.putAll(genProp.loadVocabMap(propertiesFileName));

		if (rdf.containsKey(key)) {
			Object obj = rdf.get(key);
			Iterator oIt = getLobidObjectIterator(obj);
			while (oIt.hasNext()) {
				Map<String, Object> map = (Map<String, Object>) oIt.next();
				map.put("prefLabel", genPropMap.get(map.get("@id")));
				keyList.add(map);
			}
			rdf.put(key, keyList);

		}

	}

	private void addParts(Map<String, Object> rdf) {
		Read read = new Read();
		List<Map<String, Object>> parts =
				(List<Map<String, Object>>) rdf.get("hasPart");
		List<Map<String, Object>> children = new ArrayList();
		if (parts != null) {
			for (Map<String, Object> part : parts) {
				String id = (String) part.get(ID2);
				Node cn = read.internalReadNode(id);
				if (!"D".equals(cn.getState())) {
					children.add(new JsonMapper(cn).getLd2WithoutContext());
				}
			}
			if (!children.isEmpty()) {
				rdf.put("hasPart", children);
			}
		}
	}

	private void postProcessContribution(Map<String, Object> rdf) {
		try {
			List<Map<String, Object>> creator = new ArrayList<>();
			Collection<Map<String, Object>> contributions =
					(Collection<Map<String, Object>>) rdf.get("contribution");
			if (contributions == null)
				return;
			for (Map<String, Object> contribution : contributions) {
				Map<String, Object> agent =
						((Collection<Map<String, Object>>) contribution.get("agent"))
								.iterator().next();
				if (agent != null) {
					String prefLabel = findLabel(agent);
					agent.put(PREF_LABEL, prefLabel);
					String id = null;
					if (agent.containsKey(ID2)) {
						id = agent.get(ID2).toString();
					}
					if (id == null) {
						id = Globals.protocol + Globals.server + "/adhoc/author/"
								+ prefLabel;
					}
					Map<String, Object> cmap = new HashMap<>();
					cmap.put(PREF_LABEL, prefLabel);
					cmap.put(ID2, id);
					cmap.put("academicDegree", "Privatdozent");
					creator.add(cmap);
				}
			}
			rdf.put("creator", creator);
		} catch (Exception e) {
			play.Logger.debug("Problem processing key contribution.agent", e);
			// play.Logger.debug("", e);
		}
	}

	private static void postProcess(Map<String, Object> m, String field) {
		try {
			Collection<Map<String, Object>> fields =
					(Collection<Map<String, Object>>) m.get(field);
			if (fields != null) {
				play.Logger.trace("Found roles: " + fields);
				for (Map<String, Object> r : fields) {
					String prefLabel = findLabel(r);
					play.Logger.trace("Found label " + prefLabel + " for role " + r);
					r.put(PREF_LABEL, prefLabel);
				}
			}
		} catch (Exception e) {
			play.Logger.debug("Problem processing key " + field, e);
		}
	}

	private static void addCatalogLink(Map<String, Object> rdf) {
		try {
			String hbzId = ((Collection<String>) rdf.get("hbzId")).iterator().next();
			Collection<Map<String, Object>> catalogLink = new ArrayList<>();
			Map<String, Object> cl = new HashMap<>();
			cl.put(ID2, "https://lobid.org/resources/" + hbzId);
			cl.put(PREF_LABEL, hbzId);
			catalogLink.add(cl);
			rdf.put("catalogLink", catalogLink);
		} catch (Exception e) {
			play.Logger.trace("No catalog link available!");
		}
	}

	private void sortCreatorAndContributors(Map<String, Object> rdf) {
		try {
			Collection<Map<String, Object>> cr = getSortedListOfCreators(rdf);
			if (!cr.isEmpty()) {
				rdf.put("creator", cr);
				rdf.remove("creatorName");
				rdf.remove("contributorName");
			}
			Collection<Map<String, Object>> co = getSortedListOfContributors(rdf);
			if (!co.isEmpty()) {
				rdf.put("contributor", co);
			}
		} catch (Exception e) {
			play.Logger.debug("", e);
		}
	}

	private void addLinkToJsonMap(Map<String, Object> rdf, Link l) {
		Map<String, Object> resolvedObject = null;
		String id = l.getObject();
		String value = l.getObjectLabel();
		String jsonName = getJsonName(l.getPredicate());
		if (l.getObjectLabel() != null) {
			resolvedObject = new HashMap<>();
			resolvedObject.put(ID2, id);
			resolvedObject.put(PREF_LABEL, value);
		}
		if (jsonName != null && rdf.containsKey(jsonName)) {
			Collection<Object> list =
					(Collection<Object>) rdf.get(getJsonName(l.getPredicate()));
			if (resolvedObject == null) {
				if (l.isLiteral()) {
					list.add(l.getObject());
				} else {
					resolvedObject = new HashMap<>();
					resolvedObject.put(ID2, id);
					resolvedObject.put(PREF_LABEL, id);
					list.add(resolvedObject);
				}
			} else {
				list.add(resolvedObject);
			}
		} else {
			Collection<Object> list = new ArrayList<>();
			if (resolvedObject == null) {
				if (l.isLiteral()) {
					list.add(l.getObject());
				} else {
					resolvedObject = new HashMap<>();
					resolvedObject.put(ID2, id);
					resolvedObject.put(PREF_LABEL, id);
					list.add(resolvedObject);
				}
			} else {
				list.add(resolvedObject);
			}
			rdf.put(getJsonName(l.getPredicate()), list);
		}
	}

	private void addPartsToJsonMap(Map<String, Object> rdf) {
		for (Link l : node.getPartsSorted()) {
			if (l.getObjectLabel() == null || l.getObjectLabel().isEmpty())
				l.setObjectLabel(l.getObject());
			addLinkToJsonMap(rdf, l);
		}
	}

	Collection<Map<String, Object>> getSortedListOfCreators(
			Map<String, Object> nodeAsMap) {
		Collection<Map<String, Object>> result = new ArrayList<>();
		Collection<String> carray =
				(Collection<String>) nodeAsMap.get("contributorOrder");
		if (carray == null || carray.isEmpty())
			return result;
		for (String cstr : carray) {
			String[] contributorOrdered =
					cstr.contains("|") ? cstr.split("\\|") : new String[] { cstr };
			for (String s : contributorOrdered) {
				Map<String, Object> map = findCreator(nodeAsMap, s.trim());
				if (!map.isEmpty())
					result.add(map);
			}
		}
		return result;
	}

	Collection<Map<String, Object>> getSortedListOfContributors(
			Map<String, Object> nodeAsMap) {
		Collection<Map<String, Object>> result = new ArrayList<>();
		Collection<String> carray =
				(Collection<String>) nodeAsMap.get("contributorOrder");
		if (carray == null || carray.isEmpty())
			return result;
		for (String cstr : carray) {
			String[] contributorOrdered =
					cstr.contains("|") ? cstr.split("\\|") : new String[] { cstr };
			for (String s : contributorOrdered) {
				Map<String, Object> map = findContributor(nodeAsMap, s.trim());
				if (!map.isEmpty())
					result.add(map);
			}
		}
		return result;
	}

	private static Map<String, Object> findCreator(Map<String, Object> m,
			String authorsId) {
		if (!authorsId.startsWith("http")) {
			Map<String, Object> creatorWithoutId = new HashMap<>();
			creatorWithoutId.put(PREF_LABEL, authorsId);
			creatorWithoutId.put(ID2, Globals.protocol + Globals.server + "/authors/"
					+ RdfUtils.urlEncode(authorsId).replace("+", "%20"));
			return creatorWithoutId;
		}

		Iterator iterator = new LRMIMapper().getLobid2Iterator(m.get("creator"));
		while (iterator.hasNext()) {
			Map<String, Object> creator = (Map<String, Object>) iterator.next();
			if (authorsId.compareTo((String) creator.get("@id")) == 0) {
				return creator;
			}
		}

		return new HashMap<>();
	}

	private static Map<String, Object> findContributor(Map<String, Object> map,
			String authorsId) {
		if (map.get("contributor") != null) {
			Iterator iterator =
					new LRMIMapper().getLobid2Iterator(map.get("contributor"));
			while (iterator.hasNext()) {
				Map<String, Object> contributor = (Map<String, Object>) iterator.next();
				if (authorsId.compareTo((String) contributor.get("@id")) == 0) {
					return contributor;
				}
			}
		}

		return new HashMap<>();
	}

	private static String findLabel(Map<String, Object> map) {

		if (map.containsKey("preferredNameForTheWork"))
			return (String) map.get("preferredNameForTheWork");

		if (map.containsKey("preferredNameForThePerson"))
			return (String) map.get("preferredNameForThePerson");

		if (map.containsKey("preferredNameForTheCorporateBody"))
			return (String) map.get("preferredNameForTheCorporateBody");

		if (map.containsKey("preferredNameForThePlaceOrGeographicName"))
			return (String) map.get("preferredNameForThePlaceOrGeographicName");

		if (map.containsKey("preferredName"))
			return (String) map.get("preferredName");

		if (map.containsKey("preferredNameForTheSubjectHeading"))
			return (String) map.get("preferredNameForTheSubjectHeading");

		if (map.containsKey("alternateName_de")) {
			return (String) map.get("alternateName_de");
		} else if (map.containsKey("alternateName_en")) {
			return (String) map.get("alternateName_en");
		}
		if (map.containsKey("label"))
			return (String) map.get("label");

		if (map.containsKey(PREF_LABEL))
			return (String) map.get(PREF_LABEL);

		return null;
	}

	private String getUriFromJsonName(String n) {
		return profile.getEtikettByName(n).getUri();
	}

	private String getJsonName(String uri) {
		String result = profile.getEtikett(uri).getName();

		if (result == null) {
			play.Logger
					.warn("No json name for " + uri + ". Please fix your labels.json");
			result = uri;
		}

		return result;
	}

	public Map<String, Object> getLd2WithParts() {
		Map<String, Object> rdf = getLd2();
		addParts(rdf);
		return rdf;
	}

	/**
	 * Holt Metadaten im Format lobid2 (falls vorhanden) und reichert sie an mit
	 * Informationen aus dem Node
	 * 
	 * @return
	 */
	public Map<String, Object> getLd2() {
		Collection<Link> ls = node.getRelsExt();
		Map<String, Object> ld2Map = getDescriptiveMetadata2();
		Map<String, Object> ld2Rdf = ld2Map == null ? new HashMap<>() : ld2Map;

		try {
			String jsonString = JsonUtil.mapper().writeValueAsString(ld2Rdf);
			// play.Logger.debug("asRdf: jsonString=" + jsonString);
		} catch (Exception e) {
			play.Logger.error("Fehler beim Logging von jsonString", e);
		}

		changeDcIsPartOfToRegalIsPartOf(ld2Rdf);
		// rdf.remove("describedby");
		// rdf.remove("sameAs");

		ld2Rdf.put(ID2, node.getPid());
		ld2Rdf.put(primaryTopic, node.getPid());
		for (Link l : ls) {
			if (HAS_PART.equals(l.getPredicate()))
				continue;
			if (REL_HBZ_ID.equals(l.getPredicate()))
				continue;
			if (IS_PART_OF.equals(l.getPredicate()))
				continue;
			addLinkToJsonMap(ld2Rdf, l);
		}
		addPartsToJsonMap(ld2Rdf);
		ld2Rdf.remove("isNodeType");

		ld2Rdf.put(contentType, node.getContentType());
		ld2Rdf.put(accessScheme, node.getAccessScheme());
		ld2Rdf.put(publishScheme, node.getPublishScheme());
		ld2Rdf.put(transformer, node.getTransformer().stream().map(t -> t.getId())
				.collect(Collectors.toList()));
		ld2Rdf.put(catalogId, node.getCatalogId());
		// rdf.put(embargoTime, node.getEmbargoTime());

		if (node.getFulltext() != null)
			ld2Rdf.put(fulltext_ocr, node.getFulltext());

		Map<String, Object> aboutMap = new LinkedHashMap<>();
		aboutMap.put(ID2, node.getAggregationUri() + ".rdf");
		if (node.getCreatedBy() != null)
			aboutMap.put(createdBy, node.getCreatedBy());
		if (node.getSubmittedBy() != null)
			aboutMap.put(submittedBy, node.getSubmittedBy());
		if (node.getSubmittedByEmail() != null)
			aboutMap.put(submittedByEmail, node.getSubmittedByEmail());
		if (node.getLegacyId() != null)
			aboutMap.put(legacyId, node.getLegacyId());
		if (node.getImportedFrom() != null)
			aboutMap.put(importedFrom, node.getImportedFrom());
		if (node.getName() != null)
			aboutMap.put(name, node.getName());
		if (node.getLastModifiedBy() != null)
			aboutMap.put(lastModifiedBy, node.getLastModifiedBy());

		aboutMap.put(modified, node.getLastModified());
		if (node.getObjectTimestamp() != null) {
			aboutMap.put(objectTimestamp, node.getObjectTimestamp());
		} else {
			aboutMap.put(objectTimestamp, node.getLastModified());
		}
		try {
			aboutMap.put(created, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
					.format(node.getCreationDate()));
		} catch (Exception e) {
			aboutMap.put(created, node.getCreationDate());
		}
		aboutMap.put(describes, node.getAggregationUri());

		ld2Rdf.put(isDescribedBy, aboutMap);
		if (node.getDoi() != null) {
			ld2Rdf.put(doi, node.getDoi());
		}
		if (node.getUrn() != null) {
			ld2Rdf.put(urn, node.getUrn());
		}

		if (node.getParentPid() != null)
			ld2Rdf.put(parentPid, node.getParentPid());

		if (node.getMimeType() != null && !node.getMimeType().isEmpty()) {
			Map<String, Object> hasDataMap = new LinkedHashMap<>();
			hasDataMap.put(ID2, node.getDataUri());
			hasDataMap.put(format, node.getMimeType());
			hasDataMap.put(size, node.getFileSize());
			if (node.getFileLabel() != null)
				hasDataMap.put(fileLabel, node.getFileLabel());
			if (node.getChecksum() != null) {
				Map<String, Object> checksumMap = new LinkedHashMap<>();
				checksumMap.put(checksumValue, node.getChecksum());
				checksumMap.put(generator, "http://en.wikipedia.org/wiki/MD5");
				checksumMap.put(rdftype,
						"http://downlode.org/Code/RDF/File_Properties/schema#Checksum");
				hasDataMap.put(checksum, checksumMap);
			}
			ld2Rdf.put(hasData, hasDataMap);
		}
		ObjectMapper mapper = new ObjectMapper();

		String issued =
				getPublicationMap(mapper.convertValue(ld2Rdf, JsonNode.class));
		if (issued != null) {
			ld2Rdf.put("issued", issued);
		}
		ld2Rdf.put("@context", Globals.protocol + Globals.server + "/context.json");
		postprocessing(ld2Rdf);
		play.Logger.debug("Exiting JsonMapper.getLd2()");
		return ld2Rdf;
	}

	/**
	 * provide information about publication dates from JsonNode
	 * 
	 * @param hit JsonNode
	 * @return publication date or date issued
	 */
	public static String getPublicationMap(JsonNode hit) {
		String issued = hit.at("/issued").asText();
		if (issued != null && !issued.isEmpty()) {
			return issued;
		}
		String startDate = hit.at("/publication/0/startDate").asText();
		if (startDate != null && !startDate.isEmpty()) {
			return startDate;
		}
		String publicationYear = hit.at("/publicationYear/0").asText();
		if (publicationYear != null && !publicationYear.isEmpty()) {
			return publicationYear.substring(0, 4);
		}
		return null;
	}

	/**
	 * @param rdf
	 * @return
	 */
	private static Collection<Map<String, Object>> getType(final JsonNode rdf) {
		Collection<Map<String, Object>> result = new ArrayList<>();

		// Special case medium is video - override type
		if (mediumArrayContains(rdf,
				"http://rdaregistry.info/termList/RDAMediaType/1008")
				|| mediumArrayContains(rdf,
						"http://rdvocab.info/termList/RDACarrierType/1050")) {
			String s = "http://rdaregistry.info/termList/RDAMediaType/1008";
			Map<String, Object> tmap = new HashMap<>();
			tmap.put(PREF_LABEL, Globals.profile.getEtikett(s).getLabel());
			tmap.put(ID2, s);
			result.add(tmap);

		}
		JsonNode types = rdf.at("/rdftype");
		types.forEach(t -> {
			String typeId = t.at("/" + ID2).asText();
			if (!"http://purl.org/dc/terms/BibliographicResource".equals(typeId)) {
				Map<String, Object> tmap = new HashMap<>();
				tmap.put(PREF_LABEL, Globals.profile.getEtikett(typeId).getLabel());
				tmap.put(ID2, typeId);
				result.add(tmap);
			}
		});
		return result;
	}

	/**
	 * @param rdf
	 * @param key
	 * @return
	 */
	private static boolean mediumArrayContains(JsonNode rdf, String key) {
		boolean result = false;
		JsonNode mediumArray = rdf.at("/medium");
		for (JsonNode item : mediumArray) {
			if (key.equals(item.at("/" + ID2).asText()))
				result = true;
		}
		return result;
	}

	/**
	 * Diese Methode modifiziert den LRMI-Datenstrom. Der LRMI-Datenstrom muss im
	 * Format JSON-String übergeben werden. Es werden IDs darin ersetzt. Der
	 * LRMI-Datenstrom wird auf lobid gemappeter JSON-String zurück gegeben.
	 * 
	 * @author Ingolf Kuss, hbz
	 * @param n The Node of the resource
	 * @param content Die LRMI-Daten im Format JSON
	 * @date 2021-08-20
	 * 
	 * @return Die Daten im Format LRMI JSON
	 */
	public String getTosciencefyLrmi(Node n, String content) {
		this.node = n;
		play.Logger.debug("Start getTosciencefyLrmi");
		try {
			// LRMI-Daten nach JSONObject wandeln
			JSONObject lrmiJSONObject = new JSONObject(content);
			JSONArray arr = null;
			JSONObject obj = null;

			// toscience-ID generieren
			toscience_id = new String(
					Globals.protocol + Globals.server + "/resource/" + node.getPid());
			play.Logger.debug("toscience ID for this resource is: " + toscience_id);

			// bisherige Top-Level ID
			String top_level_id = null;
			if (lrmiJSONObject.has("id")) {
				top_level_id = lrmiJSONObject.getString("id");
			}
			if (top_level_id == null) {
				play.Logger.debug("Adding new top level id: " + toscience_id);
				lrmiJSONObject.put("id", toscience_id);
			} else if (!toscience_id.equals(top_level_id)) {
				play.Logger.info("Found top level id: " + top_level_id
						+ ". Replacing by toscience id: " + toscience_id);
				lrmiJSONObject.put("id", toscience_id);
			}

			// mainEntityOfPage - ID (mit letztem Änderungsdatum)
			if (lrmiJSONObject.has("mainEntityOfPage")) {
				// ID in "mainEntityOfPage" wird ersetzt
				arr = lrmiJSONObject.getJSONArray("mainEntityOfPage");
				for (int i = 0; i < arr.length(); i++) {
					obj = arr.getJSONObject(i);
					if (obj.has("id")) {
						play.Logger
								.debug("Bestehende mainEntityOfPage-ID " + obj.getString("id")
										+ " wird ersetzt durch " + toscience_id + ".");
					}
					obj.put("id", toscience_id);
					obj.put("dateModified", LocalDate.now());
				}
			} else {
				// Element "mainEntityOfPage" wird neu angelegt
				Collection<Map<String, Object>> mainEntityOfPage = new ArrayList<>();
				Map<String, Object> mainEntityMap = new LinkedHashMap<>();
				mainEntityMap.put("id", toscience_id);
				mainEntityMap.put("dateCreated", LocalDate.now());
				mainEntityOfPage.add(mainEntityMap);
				lrmiJSONObject.put("mainEntityOfPage", mainEntityOfPage);
			}

			// Anlagedatum generieren (falls noch nicht vorhanden)
			if (!lrmiJSONObject.has("dateCreated")) {
				lrmiJSONObject.put("dateCreated", LocalDate.now());
			}

			// geändertes JSONObject als Zeichenkette zurück geben
			play.Logger.debug("Modified LRMI Data to: " + lrmiJSONObject.toString());
			return lrmiJSONObject.toString();

		} catch (JSONException je) {
			play.Logger.error("Content could not be mapped!", je);
			throw new RuntimeException(
					"LRMI.json could not be modified for toscience !", je);
		}
	}

	/**
	 * Diese Methode fügt ein Kindobjekt zu einem LRMI-Datenstrom hinzu. Zurück
	 * gegeben wird der modifizierte LRMI-Datenstrom.
	 * 
	 * @author Ingolf Kuss, hbz
	 * @param parent The Node of the parent resource
	 * @param child The Node of the child resource
	 * @date 2022-03-10
	 * 
	 * @return Die geänderten Daten im Format LRMI JSON
	 */
	public String addLrmiChildToParent(Node parent, Node child) {
		this.node = parent;
		play.Logger.debug("Start addLrmiChildToParent");
		try {
			String lrmiData = node.getMetadata(archive.fedora.Vocabulary.lrmiData);
			// LRMI-Daten nach JSONObject wandeln
			JSONObject lrmiJSONObject = null;
			if (lrmiData == null || lrmiData.isEmpty()) {
				play.Logger.info(
						"LRMI data of parent pid " + node.getPid() + " do not exist, yet.");
				lrmiJSONObject = new JSONObject();
			} else {
				lrmiJSONObject = new JSONObject(lrmiData);
			}

			JSONArray arr = null;
			JSONObject obj = null;

			// Suche nach "encoding"-Array in den vorhandenen LRMI-Daten
			if (lrmiJSONObject.has("encoding")) {
				arr = lrmiJSONObject.getJSONArray("encoding");
			} else {
				arr = new JSONArray();
			}

			/**
			 * Gucke, ob dieses Kind am Elternobjekt schon vorhanden ist. Falls ja,
			 * lösche dieses Kind (um es weiter unten erneut anzuhängen (Patch)).
			 */
			// Hilfs-Array, da man aus JSONArray nichts entfernen kann (seufz):
			ArrayList<JSONObject> hilf = new ArrayList<JSONObject>();
			for (int i = 0; i < arr.length(); i++) {
				obj = arr.getJSONObject(i);
				String contentUrl = obj.getString("contentUrl");
				if (contentUrl.indexOf(child.getPid()) >= 0) {
					continue;
				}
				hilf.add(obj);
			}
			// Neuerzeugung JSON Array aus Hilfs-Array
			arr = new JSONArray(hilf);
			// Hänge nun ein neues Kind an den Parent an.
			obj = new JSONObject();
			obj.put("contentUrl", new String(Globals.protocol + Globals.server
					+ "/resource/" + child.getPid() + "/data"));
			obj.put("type", "MediaObject");
			arr.put(obj);
			lrmiJSONObject.put("encoding", arr);

			// geändertes JSONObject als Zeichenkette zurück geben
			play.Logger.debug("Modified LRMI Data to: " + lrmiJSONObject.toString());
			return lrmiJSONObject.toString();

		} catch (JSONException je) {
			play.Logger.error("Content could not be mapped!", je);
			throw new RuntimeException(
					"LRMI.json could not be modified for toscience !", je);
		}
	}

	/**
	 * Diese Methode macht 2 Mappings: 1. Holt Metadaten im Format lobid2 (falls
	 * vorhanden) und mappt Felder aus LRMI-Daten darauf. LRMI => metadata2(rdf).
	 * 2. Mappt LRMI-Daten => toscience.json. Im Gegensatz zu den in rdf
	 * verwendeten HashSets, bleibt die Sortierreihenfolge bei den im JSONObject
	 * verwendeten JSONArrays erhalten.
	 * 
	 * @author Ingolf Kuss, hbz
	 * @param n The Node of the resource
	 * @param content Die LRMI-Daten im Format JSON String
	 * @date 2021-07-14, 2022-09-29
	 * 
	 * @return eine Hash-Map mit folgenden Ingalten: key="metadata2" : value= RDF
	 *         metadaten als Map<String, Object>; key="toscience.json" : value=
	 *         lobid2 Metadaten als toscience Objekt MetadataJson (implementiert
	 *         letztendes JSONObject)
	 */
	public HashMap<String, Object> getLd2Lobidify2Lrmi(Node n, String content) {
		/* Mapping von LRMI.json nach lobid2.json = json2 */
		this.node = n;
		try {
			HashMap<String, Object> retHash = new HashMap();
			/*
			 * Neues JSON-Objekt anlegen für rdf-Daten. Achtung: Die Map erhält die
			 * Sortierung der Felder nicht !
			 */
			Map<String, Object> rdf = node.getLd2();
			/*
			 * Neues JSON-Objekt anlegen für Metadaten im Format toscience.json.
			 * lobid2 Daten im Format JSON.
			 */
			MetadataJson metadataJson = new MetadataJson();

			// LRMIDaten nach JSONObject wandeln
			JSONObject lrmiJSONObject = new JSONObject(content);
			play.Logger.debug("Start mapping of lrmi to lobid2");
			JSONArray arr = null;
			JSONObject obj = null;
			Object myObj = null; /* Objekt von zunächst unbekanntem Typ/Klasse */
			String prefLabel = null;

			if (lrmiJSONObject.has("@context")) {
				arr = lrmiJSONObject.getJSONArray("@context");
				play.Logger.debug("Found context: " + arr.getString(0));
				/* das geht nicht; @context wird in lobid2 automatisch erstellt */
				/* kann man nicht mappen; soll auch nicht gemappt werden! */
				rdf.put("@context", arr.getString(0));
				obj = arr.getJSONObject(1);
				String language = obj.getString("@language");
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
				// languageMap.put("label", "Deutsch");
				// languageMap.put("prefLabel", "Deutsch");
				List<Map<String, Object>> languages = new ArrayList<>();
				languages.add(languageMap);
				rdf.put("language", languages);
			}

			rdf.put(accessScheme, "public");
			rdf.put(publishScheme, "public");

			arr = lrmiJSONObject.getJSONArray("type");
			rdf.put("contentType", arr.getString(0));

			List<String> names = new ArrayList<>();
			names.add(lrmiJSONObject.getString(name));
			rdf.put("title", names);

			if (lrmiJSONObject.has("inLanguage")) {
				List<Map<String, Object>> inLangList = new ArrayList<>();
				String inLang = null;
				arr = lrmiJSONObject.getJSONArray("inLanguage");
				for (int i = 0; i < arr.length(); i++) {
					Map<String, Object> inLangMap = new LinkedHashMap<>();
					inLang = arr.getString(i);
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

			rdf = mapLrmiAgentsToLobid(rdf, lrmiJSONObject, "creator");
			rdf = mapLrmiAgentsToLobid(rdf, lrmiJSONObject, "contributor");
			rdf = mapLrmiObjectToLobid(rdf, lrmiJSONObject, "learningResourceType",
					"medium");
			rdf = mapLrmiObjectToLobid(rdf, lrmiJSONObject, "about", "department");

			// template for Mapping of Array
			if (lrmiJSONObject.has("description")) {
				List<String> descriptions = new ArrayList<>();
				myObj = lrmiJSONObject.get("description");
				if (myObj instanceof java.lang.String) {
					descriptions.add(lrmiJSONObject.getString("description"));
				} else if (myObj instanceof org.json.JSONArray) {
					arr = lrmiJSONObject.getJSONArray("description");
					for (int i = 0; i < arr.length(); i++) {
						descriptions.add(arr.getString(i));
					}
				}
				rdf.put("description", descriptions);
			}

			if (lrmiJSONObject.has("license")) {
				License ambLicense = new License();
				List<Map<String, Object>> licenses = new ArrayList<>();
				myObj = lrmiJSONObject.get("license");
				Map<String, Object> licenseMap = null;
				if (myObj instanceof java.lang.String) {
					ambLicense.setById(lrmiJSONObject.getString("license"));
					licenseMap = new LinkedHashMap<>();
					licenseMap.put("@id", lrmiJSONObject.getString("license"));
					licenses.add(licenseMap);
				} else if (myObj instanceof org.json.JSONObject) {
					obj = lrmiJSONObject.getJSONObject("license");
					ambLicense.setById(obj.getString("id"));
					licenseMap = new LinkedHashMap<>();
					licenseMap.put("@id", obj.getString("id"));
					licenses.add(licenseMap);
				} else if (myObj instanceof org.json.JSONArray) {
					arr = lrmiJSONObject.getJSONArray("license");
					for (int i = 0; i < arr.length(); i++) {
						obj = arr.getJSONObject(i);
						ambLicense.setById(obj.getString("id"));
						licenseMap = new LinkedHashMap<>();
						licenseMap.put("@id", obj.getString("id"));
						licenses.add(licenseMap);
					}
				}
				rdf.put("license", licenses);
				metadataJson.put("license", ambLicense.getAmbJSONObject());
			}

			if (lrmiJSONObject.has("publisher")) {
				List<Map<String, Object>> institutions = new ArrayList<>();
				arr = lrmiJSONObject.getJSONArray("publisher");
				for (int i = 0; i < arr.length(); i++) {
					obj = arr.getJSONObject(i);
					Map<String, Object> publisherMap = new LinkedHashMap<>();
					publisherMap.put("prefLabel", obj.getString(name));
					publisherMap.put("@id", obj.getString("id"));
					institutions.add(publisherMap);
				}
				rdf.put("institution", institutions);
			}

			// example for usage of AdHocUriProvider
			if (lrmiJSONObject.has("keywords")) {
				String keyword = null;
				List<Map<String, Object>> subject = new ArrayList<>();
				arr = lrmiJSONObject.getJSONArray("keywords");
				for (int i = 0; i < arr.length(); i++) {
					AdHocUriProvider ahu = new AdHocUriProvider();
					Map<String, Object> keywordMap = new LinkedHashMap<>();
					keyword = arr.getString(i);
					keywordMap.put("prefLabel", keyword);
					keywordMap.put("@id", ahu.getAdhocUri(keyword));
					subject.add(keywordMap);
				}
				rdf.put("subject", subject);
			}

			// HINT: potential Passepartout for Mapping default JsonObjects
			if (lrmiJSONObject.has("funder")) {

				// Provide resolving for prefLabels from @id via GenericPropertiesLoader
				LinkedHashMap<String, String> genPropMap = new LinkedHashMap<>();
				GenericPropertiesLoader genProp = new GenericPropertiesLoader();
				genPropMap.putAll(genProp.loadVocabMap("funder-de.properties"));

				obj = lrmiJSONObject.getJSONObject("funder");
				Map<String, Object> funderMap = new LinkedHashMap<>();
				funderMap.put("type", obj.getString("type"));
				funderMap.put("@id", obj.getString("url"));
				funderMap.put("prefLabel", genPropMap.get(obj.getString("url")));
				rdf.put("funder", funderMap);
			}

			// postprocessing(rdf);

			play.Logger.debug("Done mapping LRMI data to lobid2.");
			retHash.put(metadata2, rdf);
			retHash.put(archive.fedora.Vocabulary.metadataJson, metadataJson);
			return retHash;
		} catch (Exception e) {
			play.Logger.error("Content could not be mapped!", e);
			throw new RuntimeException("LRMI.json could not be mapped to lobid2.json",
					e);
		}

	}

	/**
	 * This IteratorBuilder checks if JSONObject is in Array (JSONArray) or Object
	 * (JSONObject) structure and returns an iterator either
	 * 
	 * @param iObj a JSONObject of unknown internal structure
	 * @return an Iterator representing the JSONObject
	 */
	public Iterator getLobidObjectIterator(Object iObj) {
		Iterator lIterator = null;
		if (iObj instanceof java.util.ArrayList) {
			ArrayList<Map<String, Object>> jList =
					(ArrayList<Map<String, Object>>) iObj;
			lIterator = jList.iterator();
		} else if (iObj instanceof java.util.HashSet) {
			HashSet<Map<String, Object>> jHashSet =
					(HashSet<Map<String, Object>>) iObj;
			lIterator = jHashSet.iterator();
		}
		return lIterator;
	}

	/**
	 * @param properties
	 * @return
	 */
	private static LinkedHashMap<String, String> getPrefLabelMap(
			String properties) {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		GenericPropertiesLoader GenProp = new GenericPropertiesLoader();
		map.putAll(GenProp.loadVocabMap(properties));
		return map;
	}

	/**
	 * Cast a HashSet into ArrayList.
	 * 
	 * @param set
	 * @return
	 */
	public ArrayList<String> castHashSet(HashSet<String> set) {
		ArrayList<String> list = new ArrayList<>();
		Iterator<String> sIt = set.iterator();
		while (sIt.hasNext()) {
			list.add(sIt.next());
		}
		return list;

	}

	/**
	 * Add an List to existing rdfArray, or create rdfArray if not already exists
	 * 
	 * @param rdf
	 * @param key name of the Array in the rdf
	 * @param valueList List with Values to be added
	 * @return
	 */
	public Map<String, Object> addToRdfArray(Map<String, Object> rdf, String key,
			ArrayList<String> valueList) {
		if (rdf.containsKey(key)) {
			ArrayList<String> rdfArray = (ArrayList<String>) rdf.get(key);
			for (int i = 0; i < valueList.size(); i++) {
				rdfArray.add(valueList.get(i));
				play.Logger.debug("Added to list " + key + " value " + valueList.get(i)
						+ " on position " + i);
			}
			rdfArray.addAll(valueList);
		} else {
			rdf.put(key, valueList);
		}
		return rdf;
	}

	/**
	 * Try to create a method for mapping objects with String-Output from lrmi to
	 * lobid (Kayhan)
	 * 
	 * @param Rdf add object in rdf=Rdf
	 * @param lrmiJSONObject lrmi
	 * @param lrmiObject input String
	 * @param lobidObject output String
	 * @return rdf
	 */
	public Map<String, Object> mapLrmiObjectToLobid(Map<String, Object> Rdf,
			JSONObject lrmiJSONObject, String lrmiObject, String lobidObject) {

		Map<String, Object> rdf = Rdf;
		JSONObject obj = null;
		JSONArray arr = null;
		String prefLabel = null;

		try {

			if (lrmiJSONObject.has(lrmiObject)) {
				List<Map<String, Object>> list = new ArrayList<>();
				arr = lrmiJSONObject.getJSONArray(lrmiObject);

				// Provide resolving for prefLabels from id via GenericPropertiesLoader
				LinkedHashMap<String, String> genPropMap = new LinkedHashMap<>();
				GenericPropertiesLoader genProp = new GenericPropertiesLoader();
				genPropMap.putAll(genProp.loadVocabMap(lobidObject + "-de.properties"));

				for (int i = 0; i < arr.length(); i++) {
					obj = arr.getJSONObject(i);
					Map<String, Object> map = new LinkedHashMap<>();
					// verify id
					if (obj.has("id")) {
						map.put("@id", obj.getString("id"));
						map.put("prefLabel", genPropMap.get(obj.get("id")));
					} else {
						// Dieser Fall sollte nicht vorkommen
						play.Logger.warn("Achtung! " + lrmiObject + "(" + lobidObject
								+ ") hat keine ID !");
					}

					list.add(map);
				}
				rdf.put(lobidObject, list);
			}

		} catch (Exception e) {
			play.Logger.error(e.getMessage());
		}

		return rdf;
	}

	/**
	 * Map the creators, contributors etc from lrmi to lobid
	 * 
	 * @param Rdf
	 * @param lrmiJSONObject
	 * @param agentType
	 * @return
	 */
	public Map<String, Object> mapLrmiAgentsToLobid(Map<String, Object> Rdf,
			JSONObject lrmiJSONObject, String agentType) {

		Map<String, Object> rdf = Rdf;
		String academicDegreeId = null;
		String affiliationId = null;
		String affiliationType = null;
		if (lrmiJSONObject.has(agentType)) {

			ArrayList<Map<String, Object>> agents = new ArrayList<>();
			ArrayList<String> agentAcademicDegree = new ArrayList<>();
			ArrayList<String> agentAffiliation = new ArrayList<>();
			ArrayList<String> agent = new ArrayList<>();

			try {
				JSONArray lrmiJSONArray = lrmiJSONObject.getJSONArray(agentType);
				for (int i = 0; i < lrmiJSONArray.length(); i++) {
					JSONObject obj = lrmiJSONArray.getJSONObject(i);
					StringBuffer agentStr = new StringBuffer();
					Map<String, Object> agentMap = new LinkedHashMap<>();
					agentMap.put("prefLabel", obj.getString(name));
					if (obj.has("id")) {
						agentMap.put("@id", obj.getString("id"));
					}
					agentStr.append(agentType + " " + Integer.toString(i + 1) + ": ");
					if (obj.has("honoricPrefix")) {
						String honoricPrefix = obj.getString("honoricPrefix");
						academicDegreeId = new String(
								"https://d-nb.info/standards/elementset/gnd#academicDegree/"
										+ honoricPrefix);
						agentMap.put("academicDegree", academicDegreeId);

						// we need to create academicDegree FlatList required by
						// to.science.forms
						agentAcademicDegree.add(academicDegreeId.replace(
								"https://d-nb.info/standards/elementset/gnd#academicDegree/",
								"http://hbz-nrw.de/regal#" + agentType + "AcademicDegree/"));
						agentStr.append(academicDegreeId.replace(
								"https://d-nb.info/standards/elementset/gnd#academicDegree/",
								""));
						agentStr.append(" " + obj.getString(name));
					}
					if (obj.has("affiliation")) {
						JSONObject obj2 = obj.getJSONObject("affiliation");
						affiliationId = new String(obj2.getString("id"));
						affiliationType = new String(obj2.getString("type"));

						Map<String, Object> affiliationMap = new LinkedHashMap<>();
						affiliationMap.put("@id", affiliationId);
						affiliationMap.put("type", affiliationType);
						agentMap.put("affiliation", affiliationMap);

						// we also need to create Affiliation FlatList required by
						// to.science.forms
						agentAffiliation.add(affiliationId.replace("https://ror.org/",
								"http://hbz-nrw.de/regal#" + agentType + "Affiliation/"));
						GenericPropertiesLoader genPropLoad = new GenericPropertiesLoader();
						Map<String, String> cAffil =
								genPropLoad.loadVocabMap("affiliation-de.properties");
						agentStr.append(" " + cAffil.get(affiliationId));
					}
					agents.add(agentMap);
					agent.add(agentStr.toString());
				}
				rdf.put(agentType, agents);
				rdf.put(agentType + "AcademicDegree", agentAcademicDegree);
				rdf.put(agentType + "Affiliation", agentAffiliation);
				rdf = addToRdfArray(rdf, "oerAgent", agent);
			} catch (Exception e) {
				play.Logger.error(e.getMessage());
			}
		}

		return rdf;
	}

}
