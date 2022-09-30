package helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Globals;
import models.Node;

public class LobidPostProcessor {

	final String PREF_LABEL = Globals.profile.getLabelKey();
	final String ID2 = Globals.profile.getIdAlias();
	final String rdftype = "rdftype";
	Node node = null;
	JsonMapper jm = new JsonMapper(node);

	/**
	 * @param rdf
	 */
	void postprocessing(Map<String, Object> rdf) {
		try {
			jm.addCatalogLink(rdf);
			if ("file".equals(rdf.get("contentType"))) {
				rdf.put(rdftype, Arrays.asList(new String[] { "File" }));
			}

			Collection<Map<String, Object>> t =
					getType(new ObjectMapper().valueToTree(rdf));
			if (t != null && t.size() != 0)
				rdf.put(rdftype, t);

			jm.sortCreatorAndContributors(rdf);
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
			jm.createJoinedFunding(rdf);
			jm.applyAffiliation("creator", rdf);
			jm.applyAffiliation("contributor", rdf);
			jm.applyAcademicDegree("creator", rdf);
			jm.applyAcademicDegree("contributor", rdf);

			postProcessWithGenPropLoader("department", "department-de.properties",
					rdf);
			postProcessWithGenPropLoader("funder", "funder-de.properties", rdf);

		} catch (Exception e) {
			play.Logger.debug("", e);
		}
	}

	void postProcess(Map<String, Object> m, String field) {
		try {
			Collection<Map<String, Object>> fields =
					(Collection<Map<String, Object>>) m.get(field);
			if (fields != null) {
				play.Logger.trace("Found roles: " + fields);
				for (Map<String, Object> r : fields) {
					String prefLabel = jm.findLabel(r);
					play.Logger.trace("Found label " + prefLabel + " for role " + r);
					r.put(PREF_LABEL, prefLabel);
				}
			}
		} catch (Exception e) {
			play.Logger.debug("Problem processing key " + field, e);
		}
	}

	/**
	 * @param key
	 * @param rdf
	 */
	void postProcessLinkFields(String key, Map<String, Object> rdf) {
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
	void postProcessSubjectName(Map<String, Object> rdf) {
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
	 * @param key
	 * @param propertiesFileName
	 * @param rdf
	 */
	void postProcessWithGenPropLoader(String key, String propertiesFileName,
			Map<String, Object> rdf) {

		List<Map<String, Object>> keyList = new ArrayList<>();

		// Provide resolving for prefLabels from @id via GenericPropertiesLoader
		LinkedHashMap<String, String> genPropMap = new LinkedHashMap<>();
		GenericPropertiesLoader genProp = new GenericPropertiesLoader();
		genPropMap.putAll(genProp.loadVocabMap(propertiesFileName));

		if (rdf.containsKey(key)) {
			Object obj = rdf.get(key);
			Iterator oIt = jm.getLobidObjectIterator(obj);
			while (oIt.hasNext()) {
				Map<String, Object> map = (Map<String, Object>) oIt.next();
				map.put("prefLabel", genPropMap.get(map.get("@id")));
				keyList.add(map);
			}
			rdf.put(key, keyList);

		}

	}

	void postProcessContribution(Map<String, Object> rdf) {
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
					String prefLabel = jm.findLabel(agent);
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

}
