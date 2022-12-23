/*
 * Copyright 202 hbz NRW (http://www.hbz-nrw.de/)
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import actions.Read;
import de.hbz.lobid.helper.EtikettMakerInterface;
import de.hbz.lobid.helper.JsonConverter;
import models.Globals;
import models.Node;

/**
 * 
 * @author Ingolf Kuss, hbz
 *
 */

public class LRMIMapper {

	private Node node = null;
	private EtikettMakerInterface profile = Globals.profile;
	private JsonConverter jsonConverter = null;
	private static Read read = new Read();

	/**
	 * Ein Konstruktor für diese Klasse
	 */
	public LRMIMapper() {
		play.Logger.trace("Creating new instance of Class LRMIMapper");
		jsonConverter = new JsonConverter(profile);
	}

	/**
	 * Diese Methode bildet gesendete Metadata2 in den bestehenden Datenstrom
	 * LRMI-Data ab. Sie gibt den aktualisierten oder neu erstellten Datenstrom
	 * LRMI-Data zurück.
	 * 
	 * @author I. Kuss, hbz
	 *
	 * @param n der Knoten, an dem die Metadaten aktualisiert oder angehängt
	 *          werden sollen.
	 * @param format Das Format der gesendeten Metadaten
	 * @param content Metadaten im Format lobid2
	 * @return Der aktualisierte oder neu erstellte Datenstrom LRMI-Data
	 */
	public String getLrmiAndLrmifyMetadata(Node n, RDFFormat format,
			String content) {
		this.node = n;
		try {
			/**
			 * - hole den LRMI-Datenstrom (s. GET /lrmiData)
			 */
			// get the current lrmiData as String
			String currentLrmiContent = read.readLrmiData(node);
			// String currentTosContent = read.

			/**
			 * - wandele ihn nach JsonObject (s. JsonMapper.getTosciencefyLrmi)
			 */
			// LRMI-Daten nach JSONObject wandeln
			JSONObject lrmiJsonContent = new JSONObject();
			if (currentLrmiContent != null) {
				lrmiJsonContent = new JSONObject(currentLrmiContent);
			}

			JSONArray arr = null;
			JSONObject obj = null;
			JSONObject subObj = null;
			/**
			 * - wandle die gesendeten Metadata2-Daten nach JSON. Genauso wie hier:
			 * JsonMapper.getDescriptiveMetadata2(), jedoch die Metadata2 nicht aus
			 * dem Node holen.
			 * 
			 */
			play.Logger.debug("lobid2 content:" + content);
			InputStream stream =
					new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
			Map<String, Object> rdf = jsonConverter.convert(node.getPid(), stream,
					format, profile.getContext().get("@context"));
			/**
			 * - gehe die lobid-Daten durch (Mapping lobid => LRMI !) und überschreibe
			 * die entsprechenden Felder in den LRMI-Daten oder füge sie neu ein.
			 * Anmerkung: Löschen kann man mit dieser Methode nicht (das macht auch
			 * hier keinen Sinn, da die LRMI-Daten i.d.R. reicher sein werden als die
			 * gesendeten lobid-Daten, und man will nicht die gesamte Differenz
			 * löschen).
			 */
			/* Rückabbildung lobid2 => LRMI (vgl. JsonMapper.getLd2Lobidify2Lrmi */
			Iterator iterator = null;
			Map<String, Object> map = null;
			Object myObj = null; /* ein Objekt zunächst unbekannten Typs/Klasse */

			/*** Begin Mapping lobid2 => LRMI ***/
			if (rdf.containsKey("language")) {
				iterator = getLobid2Iterator(myObj = rdf.get("language"));
				// 1. Suche Objekt "@language" im JSONArray "@context"
				// leave @context @language unchanged
				/*
				 * if (jcontent.has("@context")) { arr = (JSONArray)
				 * jcontent.get("@context"); obj = null; for (int i = 0; i <
				 * arr.length(); i++) { myObj = arr.get(i); play.Logger .debug("i=" + i
				 * + "; myObj.getClass()=" + myObj.getClass()); if (myObj instanceof
				 * org.json.JSONObject) { obj = (JSONObject) arr.getJSONObject(i); if
				 * (obj.has("@language")) { break; } } } // Falls Objekt nicht gefunden,
				 * hänge ein neues Objekt an das Array an if (obj == null) { obj = new
				 * JSONObject(); arr.put(obj); } } else { arr = new JSONArray(); obj =
				 * new JSONObject(); arr.put(obj); }
				 */

				// 2. Sprache auch auf das LRMI-Feld "inLanguage" abbilden
				JSONArray inLanguageArr = new JSONArray();

				while (iterator.hasNext()) {
					map = (Map<String, Object>) iterator.next();
					// obj.put("@language", map.get("prefLabel"));
					// obj.put("id", map.get("@id"));
					String iso639_1Tag = iso639_1TagExtractor(map.get("@id").toString());
					inLanguageArr.put(iso639_1Tag);
					break;
				}
				while (iterator.hasNext()) {
					map = (Map<String, Object>) iterator.next();
					String iso639_1Tag = iso639_1TagExtractor(map.get("@id").toString());
					inLanguageArr.put(iso639_1Tag);
				}
				// leave language unchanged
				// jcontent.put("@context", arr);
				lrmiJsonContent.put("inLanguage", inLanguageArr);
			}

			if (rdf.containsKey("contentType")) {
				iterator = getLobid2Iterator(rdf.get("contentType"));
				arr = new JSONArray();
				while (iterator.hasNext()) {
					arr.put(iterator.next());
				}
				lrmiJsonContent.put("type", arr);
			}

			if (rdf.containsKey("title")) {
				iterator = getLobid2Iterator(rdf.get("title"));
				// lrmiData only supports one title
				lrmiJsonContent.put("name", iterator.next());
			}

			if (rdf.containsKey("description")) {
				iterator = getLobid2Iterator(rdf.get("description"));
				// lrmiData only supports one description
				lrmiJsonContent.put("description", iterator.next());
			}
			/*
			 * if (rdf.containsKey("medium")) { iterator =
			 * getLobid2Iterator(rdf.get("medium")); // Hole ein Objekt aus LRMI-JSON
			 * oder lege es neu an obj = null; if
			 * (lrmiJsonContent.has("learningResourceType")) { arr = (JSONArray)
			 * lrmiJsonContent.get("learningResourceType"); for (int i = 0; i <
			 * arr.length(); i++) { myObj = arr.get(i); play.Logger .debug("i=" + i +
			 * "; myObj.getClass()=" + myObj.getClass()); if (myObj instanceof
			 * JSONObject) { obj = arr.getJSONObject(i); // nimm nur den ersten
			 * learningResourceType und überschreibe ihn // mit dem, was aus RDF kommt
			 * break; } } // Falls Objekt nicht gefunden, hänge ein neues Objekt an
			 * das Array an if (obj == null) { obj = new JSONObject(); arr.put(obj); }
			 * } else { arr = new JSONArray(); obj = new JSONObject(); arr.put(obj); }
			 * // Jetzt editiere das JSONObject mit den in RDF gefundenen
			 * Informationen while (iterator.hasNext()) { map = (Map<String, Object>)
			 * iterator.next(); if (map.containsKey("prefLabel")) { subObj = new
			 * JSONObject(); subObj.put("de", map.get("prefLabel"));
			 * obj.put("prefLabel", subObj); } obj.put("id", map.get("@id"));
			 * arr.put(obj); } lrmiJsonContent.put("learningResourceType", arr); }
			 * 
			 */
			ArrayList<String> creatorAcadDegree = new ArrayList<>();
			if (rdf.containsKey("creatorAcademicDegree")) {
				iterator = getLobid2Iterator(rdf.get("creatorAcademicDegree"));
				while (iterator.hasNext()) {
					String degreeId = (String) iterator.next();
					creatorAcadDegree.add(degreeId);
				}
				play.Logger.debug("Amount of creator academic degrees in flat List: "
						+ creatorAcadDegree.size());
			}

			ArrayList<String> contributorAcadDegree = new ArrayList<>();
			if (rdf.containsKey("contributorAcademicDegree")) {
				iterator = getLobid2Iterator(rdf.get("contributorAcademicDegree"));
				while (iterator.hasNext()) {
					String degreeId = (String) iterator.next();
					contributorAcadDegree.add(degreeId);
				}
				play.Logger
						.debug("Amount of contributor academic degrees in flat List: "
								+ contributorAcadDegree.size());
			}

			ArrayList<String> creatorAffiliation = new ArrayList<>();
			if (rdf.containsKey("creatorAffiliation")) {
				iterator = getLobid2Iterator(rdf.get("creatorAffiliation"));
				while (iterator.hasNext()) {
					String rorId = (String) iterator.next();
					creatorAffiliation.add(rorId);
				}
				play.Logger.debug("Amount of creator affiliations in flat List: "
						+ creatorAffiliation.size());
			}

			ArrayList<String> contributorAffiliation = new ArrayList<>();
			if (rdf.containsKey("contributorAffiliation")) {
				iterator = getLobid2Iterator(rdf.get("contributorAffiliation"));
				while (iterator.hasNext()) {
					String rorId = (String) iterator.next();
					contributorAffiliation.add(rorId);
				}
				play.Logger.debug("Amount of contributor affiliations in flat List: "
						+ contributorAffiliation.size());
			}
			int attribCounter = 0;
			lrmiJsonContent = mapAgent(rdf, creatorAcadDegree, creatorAffiliation,
					lrmiJsonContent, "creator");
			lrmiJsonContent = mapAgent(rdf, contributorAcadDegree,
					contributorAffiliation, lrmiJsonContent, "contributor");

			if (rdf.containsKey("subject")) {
				arr = new JSONArray();
				iterator = getLobid2Iterator(rdf.get("subject"));
				while (iterator.hasNext()) {
					map = (Map<String, Object>) iterator.next();
					arr.put(map.get("prefLabel"));
				}
				lrmiJsonContent.put("keywords", arr);
			}

			if (rdf.containsKey("license")) {
				JSONObject licenseObj = new JSONObject();
				iterator = getLobid2Iterator(rdf.get("license"));
				while (iterator.hasNext()) {
					map = (Map<String, Object>) iterator.next();
					licenseObj.put("id", map.get("@id"));
				}
				lrmiJsonContent.put("license", licenseObj);
			}

			if (rdf.containsKey("institution")) {
				iterator = getLobid2Iterator(rdf.get("institution"));
				arr = new JSONArray();
				while (iterator.hasNext()) {
					map = (Map<String, Object>) iterator.next();
					obj = new JSONObject();
					obj.put("name", map.get("prefLabel"));
					obj.put("id", map.get("@id"));
					obj.put("type", "Organization");
					arr.put(obj);
				}
				lrmiJsonContent.put("publisher", arr);
			}

			// associate child (content) objects to lmri
			// add child data url as encoding contentUrl to make content accessible
			if (rdf.containsKey("hasPart")) {
				play.Logger.debug("Child Node exists and is found");
				iterator = getLobid2Iterator(rdf.get("hasPart"));
				arr = new JSONArray();
				while (iterator.hasNext()) {
					map = (Map<String, Object>) iterator.next();
					obj = new JSONObject();
					obj.put("type", "MediaObject");
					obj.put("contentUrl", Globals.protocol + Globals.server + "/resource/"
							+ map.get("@id").toString() + "/data");
					if (map.containsKey("size")) {
						play.Logger.debug(
								" map.get(\"size\").toString()" + map.get("size").toString());
						obj.put("size", map.get("size").toString());
					} else {
						play.Logger.debug("map.get(\"size\").toString() NIcht vorhanden");
					}

					if (map.containsKey("encodingFormat")) {
						play.Logger.debug(" map.get(\"encodingFormat\").toString()"
								+ map.get("encodingFormat").toString());
						obj.put("encodingFormat", map.get("encodingFormat").toString());
					} else {
						play.Logger.debug("encodingFormat NIcht vorhanden");
					}
					arr.put(obj);
					play.Logger.debug("Added new encoding-field");
				}
				lrmiJsonContent.put("encoding", arr);
			} else {
				play.Logger.debug("no Child found in lobid2, try to get it from lobid");
				Map<String, Object> l1rdf = node.getLd1();
				if (l1rdf.containsKey("hasPart")) {
					play.Logger.debug("found Child in lobid");
					iterator = getLobid2Iterator(l1rdf.get("hasPart"));
					arr = new JSONArray();
					while (iterator.hasNext()) {
						map = (Map<String, Object>) iterator.next();
						obj = new JSONObject();
						obj.put("type", "MediaObject");
						obj.put("contentUrl", Globals.protocol + Globals.server
								+ "/resource/" + map.get("@id").toString() + "/data");

						if (map.containsKey("size")) {
							play.Logger.debug(
									" map.get(\"size\").toString()" + map.get("size").toString());
							obj.put("size", map.get("size").toString());
						} else {
							play.Logger.debug("map.get(\"size\").toString() NIcht vorhanden");
						}

						if (map.containsKey("encodingFormat")) {
							play.Logger.debug(" map.get(\"encodingFormat\").toString()"
									+ map.get("encodingFormat").toString());
							obj.put("encodingFormat", map.get("encodingFormat").toString());
						} else {
							play.Logger.debug("encodingFormat NIcht vorhanden");
						}
						arr.put(obj);
						play.Logger.debug("Added new encoding-field");
					}

				}
				lrmiJsonContent.put("encoding", arr);
			}

			lrmiJsonContent = lobidFunder2LrmiFunder(rdf, lrmiJsonContent);
			lrmiJsonContent =
					lobidObject2LrmiObject(rdf, lrmiJsonContent, "department");
			lrmiJsonContent = lobidObject2LrmiObject(rdf, lrmiJsonContent, "medium");

			/**
			 * - gib die aktualisierten oder neu angelegten LRMI-Daten zurück (Format
			 * JSON-String)
			 */

			/* zunächst Anreicherung und Update der LRMI-Daten */
			return new JsonMapper().getTosciencefyLrmi(node,
					lrmiJsonContent.toString());

		} catch (Exception e) {
			play.Logger.error("LRMI Content could not be mapped!", e);
			throw new RuntimeException("LRMI.json could not be modified or created !",
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
	public Iterator getLobid2Iterator(Object iObj) {
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
	 * This Extractor converts a three letter ISO639-2 uri into two letter
	 * ISO639-1 tag on the base of java.util.Locale.
	 * 
	 * For Instance, the given Uri http://id.loc.gov/vocabulary/iso639-2/eng will
	 * be converted in en
	 * 
	 * @param iso639_2Uri an ISO639-2 URI as String
	 * @return a two-letter tag representing the ISO639 language
	 */
	@SuppressWarnings("static-method")
	private String iso639_1TagExtractor(String iso639_2Uri) {
		String result = "unknown";
		Locale loc = Locale.forLanguageTag(
				iso639_2Uri.replace("http://id.loc.gov/vocabulary/iso639-2/", "")
						.replace("ger", "deu"));
		Map<String, Locale> localeMap = new HashMap<String, Locale>();
		String[] iso639_1Tags = Locale.getISOLanguages();
		for (int i = 0; i < iso639_1Tags.length; i++) {
			Locale locale = new Locale(iso639_1Tags[i]);
			localeMap.put(locale.getISO3Language(), locale);
		}
		if (localeMap.get(loc.getISO3Language()) != null) {
			result = localeMap.get(loc.getISO3Language()).getLanguage();
		}
		return result;
	}

	/**
	 * Diese Methode bildet einen Autor (z.B. creator oder contributor) von RDF
	 * nach LRMI ab. Für das RDF wird angenommen, dass akademische Grade und
	 * beigeordnete Institutionen (Affiliations) als lineare Liste direkt
	 * unterhalb der Ressource vorliegen, also nicht schon strukturiert unterhalb
	 * der Autoren. Die sequentiellen Listen werden an die entsprechenden Autoren
	 * gemappt.
	 * 
	 * @param attribCounter der Zähler für die direkt unter der Ressource
	 *          sitzenden RDF-Arrays adademicDegree und Affiliation
	 * @param rdf die linked Data der Ressource im Format RDF als Java Map
	 * @param agentType z.B. "creator" oder "contributor"
	 * @return den neuen Zähler für die linearen Listen
	 */
	private JSONObject mapAgent(Map<String, Object> rdf,
			ArrayList<String> acadDegree, ArrayList<String> affiliation,
			JSONObject lrmiJsonContent, String agentType) throws RuntimeException {
		try {
			play.Logger.debug("rdf = " + rdf.toString());
			if (rdf.containsKey(agentType)) {
				play.Logger.debug("add " + agentType + "\'s attributes to lrmi");
				JSONArray arr = new JSONArray();
				Iterator iterator = getLobid2Iterator(rdf.get(agentType));
				int i = 0;
				while (iterator.hasNext()) {
					Map<String, Object> map = (Map<String, Object>) iterator.next();
					JSONObject obj = new JSONObject();
					obj.put("name", map.get("prefLabel"));
					obj.put("id", map.get("@id"));
					// here we set id of agent not the id of affiliation
					obj.put("type", "Person");
					if (map.get("@id").toString().startsWith("https://ror.org")) {
						obj.put("type", "Organization");
					}

					if (i < acadDegree.size()) {
						obj.put("honoricPrefix",
								acadDegree.get(i).replace(
										"http://hbz-nrw.de/regal#" + agentType + "AcademicDegree/",
										""));
					} else {
						/*
						 * Es sind nicht genügend akademische Grade in der sequentiellen
						 * Liste in RDF vorhanden. Daher wird für diesen Autor ein
						 * Default-Wert verwendet.
						 */
						obj.put("honoricPrefix", "Keine Angabe");
					}
					if (i < affiliation.size()) {
						JSONObject affObj = new JSONObject();
						affObj.put("id",
								affiliation.get(i).replace(
										"http://hbz-nrw.de/regal#" + agentType + "Affiliation",
										"https://ror.org"));
						LinkedHashMap<String, String> genPropMap = new LinkedHashMap<>();
						GenericPropertiesLoader genProp = new GenericPropertiesLoader();
						genPropMap.putAll(genProp.loadVocabMap(
								agentType + "ResearchOrganizationsRegistry-de.properties"));
						affObj.put("name", genPropMap.get(affiliation.get(i)));
						affObj.put("type", "Organization");

						// Affiliation element should be omitted, if no specification is
						// made.
						String affilId = affiliation.get(i);
						if (!affilId
								.equals("http://hbz-nrw.de/regal#affiliation/unknown")) {
							obj.put("affiliation", affObj);
						}
					} else {
						/*
						 * Es sind nicht genügend Affiliationen in der sequentiellen Liste
						 * in RDF vorhanden. Daher wird für diesen Autor ein Default-Wert
						 * verwendet.
						 */
						JSONObject affObj = new JSONObject();
						affObj.put("id", "https://ror.org/");
						affObj.put("type", "Organization");
						affObj.put("name", "keine Angabe");
						obj.put("affiliation", affObj);
					}
					// last step to do here: count 1 to i as the internal counter for the
					// ArrayLists
					i++;
					arr.put(obj);
				}
				lrmiJsonContent.put(agentType, arr);
			}
			return lrmiJsonContent;
		} catch (Exception e) {
			play.Logger.error(agentType + " content could not be mapped!", e);
			throw new RuntimeException(agentType + " content could not be mapped!",
					e);
		}

	}

	/**
	 * Method converts a lobid object to json object
	 * 
	 * @param lobidMap
	 * @param lrmiObj Output variable
	 * @param lobidObject what should be checked as input
	 * @return Json-Object of LRMI (@param lrmiObj)
	 */
	public JSONObject lobidObject2LrmiObject(Map<String, Object> lobidMap,
			JSONObject lrmiObj, String lobidObject) {
		Iterator itr = null;
		LinkedHashMap<String, String> genPropMap = new LinkedHashMap<>();
		GenericPropertiesLoader genProp = new GenericPropertiesLoader();
		JSONArray jr = new JSONArray();
		Map<String, Object> map = null;

		try {
			switch (lobidObject) {
			case "department":
				itr = getLobid2Iterator(lobidMap.get("department"));
				genPropMap.putAll(genProp.loadVocabMap("department-de.properties"));
				while (itr.hasNext()) {
					JSONObject aboutObj = new JSONObject();
					JSONObject pLObj1 = new JSONObject();
					JSONObject inSchemeAbout = new JSONObject();
					map = (Map<String, Object>) itr.next();
					aboutObj.put("id", map.get("@id"));
					aboutObj.put("type", "Concept");
					inSchemeAbout.put("id",
							"https://w3id.org/kim/hochschulfaechersystematik/scheme");
					pLObj1.put("de", genPropMap.get(map.get("@id")));
					aboutObj.put("inScheme", inSchemeAbout);
					aboutObj.put("prefLabel", pLObj1);
					jr.put(aboutObj);
				}
				lrmiObj.put("about", jr);
				break;

			case "medium":
				itr = getLobid2Iterator(lobidMap.get("medium"));
				genPropMap.putAll(genProp.loadVocabMap("medium-de.properties"));
				while (itr.hasNext()) {
					JSONObject learningResourceType = new JSONObject();
					JSONObject pLObj2 = new JSONObject();
					JSONObject inSchemeLearningResourceType = new JSONObject();
					map = (Map<String, Object>) itr.next();
					learningResourceType.put("id", map.get("@id"));
					learningResourceType.put("type", "Concept");
					inSchemeLearningResourceType.put("id",
							"https://w3id.org/kim/hcrt/scheme");
					pLObj2.put("de", genPropMap.get(map.get("@id")));
					learningResourceType.put("inScheme", inSchemeLearningResourceType);
					learningResourceType.put("prefLabel", pLObj2);
					jr.put(learningResourceType);
				}
				lrmiObj.put("learningResourceType", jr);
				break;
			default:
				play.Logger.error(
						"unable to modify " + lobidObject + "-Object to to LRMI-Object");
				break;

			}

		} catch (Exception e) {
			play.Logger.error(
					"unable to modify " + lobidObject + "-Object to to LRMI-Object");
		}
		return lrmiObj;

	}

	/**
	 * Maps funder-Object of Lobid into funder-Object of LRMI
	 * 
	 * @param lobidMap Map representation of the lobid metadata
	 * @param lrmiObj JSONObject representation of tha lrmi Metadata
	 * @return lrmiObj with modified funder-Object
	 */
	public JSONObject lobidFunder2LrmiFunder(Map<String, Object> lobidMap,
			JSONObject lrmiObj) {

		try {
			if (lobidMap.containsKey("funder")) {
				Iterator iterator = getLobid2Iterator(lobidMap.get("funder"));
				JSONArray funderArray = new JSONArray();

				// Provide resolving for prefLabels from @id via GenericPropertiesLoader
				LinkedHashMap<String, String> genPropMap = new LinkedHashMap<>();
				GenericPropertiesLoader genProp = new GenericPropertiesLoader();
				genPropMap.putAll(genProp.loadVocabMap("funder-de.properties"));

				Map<String, Object> map = (Map<String, Object>) iterator.next();
				JSONObject funderObj = new JSONObject();
				funderObj.put("url", map.get("@id"));
				funderObj.put("type", "FundingScheme");
				funderObj.put("name", genPropMap.get(map.get("@id")));

				lrmiObj.put("funder", funderObj);
			}
		} catch (JSONException e) {
			play.Logger.error("unable to apply modified funder values to LRMI");
		}

		return lrmiObj;
	}

}
