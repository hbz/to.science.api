package helper;

import static archive.fedora.Vocabulary.metadata2;

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

public class ToscienceJsonHelper {

	/**
	 * 
	 * @param rdf
	 * @param lrmiJSONObject
	 * @param agentType
	 */
	public static void mapToscienceAgentsToLobid(Map<String, Object> rdf,
			JSONObject lrmiJSONObject, String agentType) {

		String academicDegreeId = null;
		String affiliationId = null;
		if (lrmiJSONObject.has(agentType)) {

			ArrayList<Map<String, Object>> agents = new ArrayList<>();
			ArrayList<String> agentAcademicDegree = new ArrayList<>();
			ArrayList<String> agentAffiliation = new ArrayList<>();
			ArrayList<String> oerAgent = new ArrayList<>();

			try {
				JSONArray lrmiJSONArray = lrmiJSONObject.getJSONArray(agentType); // z.B.
																																					// creator
				for (int i = 0; i < lrmiJSONArray.length(); i++) {
					JSONObject obj = lrmiJSONArray.getJSONObject(i);
					StringBuffer agentStr = new StringBuffer();
					agentStr.append(agentType + " " + Integer.toString(i + 1) + ": ");

					// do the mapping to lobid2 agent
					HashMap<String, Object> lrmiAgentToLobid =
							mapToscienceAgentToLobid(obj);
					Map<String, Object> agentMap =
							(Map<String, Object>) lrmiAgentToLobid.get("metadata2");

					agents.add(agentMap);

					if (obj.has("academicDegree")) {
						// we need to create academicDegree FlatList required by
						// to.science.forms
						academicDegreeId =
								(String) lrmiAgentToLobid.get("academicDegreeId");
						agentAcademicDegree.add(academicDegreeId.replace(
								"https://d-nb.info/standards/elementset/gnd#academicDegree/",
								"http://hbz-nrw.de/regal#" + agentType + "AcademicDegree/"));
						agentStr.append(academicDegreeId.replace(
								"https://d-nb.info/standards/elementset/gnd#academicDegree/",
								""));
					}
					agentStr.append(" " + obj.getString("name"));
					if (obj.has("affiliation")) {
						// we need to create Affiliation FlatList required by
						// to.science.forms
						affiliationId = (String) lrmiAgentToLobid.get("affiliationId");
						agentAffiliation.add(affiliationId.replace("https://ror.org/",
								"http://hbz-nrw.de/regal#" + agentType + "Affiliation/"));
						GenericPropertiesLoader genPropLoad = new GenericPropertiesLoader();
						Map<String, String> cAffil =
								genPropLoad.loadVocabMap("affiliation-de.properties");
						agentStr.append(" " + cAffil.get(affiliationId));
					}
					oerAgent.add(agentStr.toString());
				} // next agent

				rdf.put(agentType, agents);
				rdf.put(agentType + "AcademicDegree", agentAcademicDegree);
				rdf.put(agentType + "Affiliation", agentAffiliation);

				rdf.put("oerAgent", oerAgent);
			} catch (Exception e) {
				play.Logger.error(e.getMessage());
				throw new RuntimeException(e);
			}
		}

		return;
	}

	/**
	 * 
	 * @param obj
	 * @return
	 */
	public static HashMap<String, Object> mapToscienceAgentToLobid(
			JSONObject obj) {

		HashMap<String, Object> retHash = new HashMap<>();

		Map<String, Object> agentMap = new LinkedHashMap<>();

		try {
			// first do the elementary mappings: id, prefLabel
			if (obj.has("id")) {
				agentMap.put("@id", obj.getString("id"));
				agentMap.put("prefLabel", obj.getString("name"));
				agentMap.put("type", "Person");

			}

			// now do some more stuff: add agent name, affiliation, academicDegree
			if (obj.has("academicDegree")) {
				String academicDegree = obj.getString("academicDegree");
				String academicDegreeId = new String(
						"https://d-nb.info/standards/elementset/gnd#academicDegree/"
								+ academicDegree);

				agentMap.put("academicDegree", academicDegreeId);

				retHash.put("academicDegreeId", academicDegreeId);
			}

			if (obj.has("affiliation")) {
				JSONObject objAffil = obj.getJSONObject("affiliation");
				String affiliationId = new String(objAffil.getString("@id"));
				String affiliationType = new String(objAffil.getString("type"));
				String affiliationPrefLabel =
						new String(objAffil.getString("prefLabel"));

				Map<String, Object> affiliationMap = new LinkedHashMap<>();
				affiliationMap.put("@id", affiliationId);
				affiliationMap.put("type", affiliationType);
				affiliationMap.put("prefLabel", affiliationPrefLabel);
				agentMap.put("affiliation", affiliationMap);

				retHash.put("affiliationId", affiliationId);
			}
		} catch (JSONException e) {
			play.Logger.error(e.getMessage());
			throw new RuntimeException(e);
		}

		retHash.put("metadata2", agentMap);
		// retHash.put(metadataJson, agent);
		return retHash;
	}

}
