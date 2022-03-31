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
import java.util.Locale;
import java.util.Map;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.json.JSONArray;
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

	Node node = null;
	EtikettMakerInterface profile = Globals.profile;
	JsonConverter jsonConverter = null;
	static Read read = new Read();

	/**
	 * Ein Konstruktor für diese Klasse
	 */
	public LRMIMapper() {
		play.Logger.info("Creating new instance of Class LRMIMapper");
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
			String oldContent = read.readLrmiData(node);
			/**
			 * - wandele ihn nach JsonObject (s. JsonMapper.getTosciencefyLrmi)
			 */
			// LRMI-Daten nach JSONObject wandeln
			JSONObject jcontent = null;
			if (oldContent == null) {
				jcontent = new JSONObject();
			} else {
				jcontent = new JSONObject(oldContent);
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
				jcontent.put("inLanguage", inLanguageArr);
			}

			if (rdf.containsKey("contentType")) {
				iterator = getLobid2Iterator(rdf.get("contentTyp"));
				arr = new JSONArray();
				while (iterator.hasNext()) {
					arr.put(iterator.next());
				}
				jcontent.put("type", arr);
			}

			if (rdf.containsKey("title")) {
				iterator = getLobid2Iterator(rdf.get("title"));
				// lrmiData only supports one title
				jcontent.put("name", iterator.next());
			}

			if (rdf.containsKey("medium")) {
				iterator = getLobid2Iterator(rdf.get("medium"));
				// Hole ein Objekt aus LRMI-JSON oder lege es neu an
				obj = null;
				if (jcontent.has("learningResourceType")) {
					arr = (JSONArray) jcontent.get("learningResourceType");
					for (int i = 0; i < arr.length(); i++) {
						myObj = arr.get(i);
						play.Logger
								.debug("i=" + i + "; myObj.getClass()=" + myObj.getClass());
						if (myObj instanceof org.json.JSONObject) {
							obj = arr.getJSONObject(i);
							// nimm nur den ersten learningResourceType und überschreibe ihn
							// mit dem, was aus RDF kommt
							break;
						}
					}
					// Falls Objekt nicht gefunden, hänge ein neues Objekt an das Array an
					if (obj == null) {
						obj = new JSONObject();
						arr.put(obj);
					}
				} else {
					arr = new JSONArray();
					obj = new JSONObject();
					arr.put(obj);
				}
				// Jetzt editiere das JSONObject mit den in RDF gefundenen Informationen
				while (iterator.hasNext()) {
					map = (Map<String, Object>) iterator.next();
					if (map.containsKey("prefLabel")) {
						subObj = new JSONObject();
						subObj.put("de", map.get("prefLabel"));
						obj.put("prefLabel", subObj);
					}
					obj.put("id", map.get("@id"));
					break; // nimm nur den ersten Medientypen
				}
				jcontent.put("learningResourceType", arr);
			}

			if (node.getLd2().containsKey("creator")) {
				arr = new JSONArray();
				iterator = getLobid2Iterator(node.getLd2().get("creator"));
				while (iterator.hasNext()) {
					map = (Map<String, Object>) iterator.next();
					obj = new JSONObject();
					obj.put("name", map.get("prefLabel"));
					obj.put("id", map.get("@id"));
					obj.put("type", map.get("type"));
					obj.put("honoricPrefix", map.get("academicTitle"));
					if (map.containsKey("affiliation")) {
						Iterator aIterator = getLobid2Iterator(map.get("affilitation"));
						while (aIterator.hasNext()) {
							Map aMap = (Map<String, Object>) aIterator.next();
							JSONObject aObj = new JSONObject();
							aObj.put("name", aMap.get("prefLabel"));
							aObj.put("id", aMap.get("@id"));
							aObj.put("type", "Organization");
							obj.put("affiliation", aObj);
							break; // es gibt nur eine Affiliation pro Autor (?)
						}
					}
					arr.put(obj);
				}
				jcontent.put("creator", arr);
			}

			if (rdf.containsKey("academicTitle")) {
				iterator = getLobid2Iterator(rdf.get("academicTitle"));
				while (iterator.hasNext()) {
					map = (Map<String, Object>) iterator.next();
					play.Logger.debug("found affiliation" + map.toString());
				}

			}

			if (rdf.containsKey("creator")) {
				iterator = getLobid2Iterator(rdf.get("creator"));
				arr = new JSONArray();
				while (iterator.hasNext()) {
					map = (Map<String, Object>) iterator.next();
					obj = new JSONObject();
					obj.put("name", map.get("prefLabel"));
					obj.put("id", map.get("@id"));
					obj.put("type", "Person"); /* guess */
					Iterator mIterator = null;
					if (map.containsKey("affiliation")) {
						play.Logger.debug("key affiliation found in rdf");
						mIterator = getLobid2Iterator(map.get("affiliation"));
					}
					if (mIterator != null) {
						while (mIterator.hasNext()) {
							Map aMap = (Map<String, Object>) mIterator.next();
							JSONObject mObj = new JSONObject();
							mObj.put("name", aMap.get("prefLabel"));
							mObj.put("id", aMap.get("@id"));
							mObj.put("type", "Organization"); /* guess */
							obj.put("affiliation", mObj);
							break; // es gibt nur eine Affiliation pro Creator (?)
						}
					} else {
						play.Logger.warn("found no affiliation associated with creator");
						JSONObject dummyObj = new JSONObject();
						dummyObj.put("name", "Example Affiliation");
						dummyObj.put("id", "https://example.org");
						dummyObj.put("type", "Organization"); /* guess */
						obj.put("affiliation", dummyObj);
					}
					arr.put(obj);
				}
				jcontent.put("creator", arr);

			}

			if (rdf.containsKey("contributor")) {
				iterator = getLobid2Iterator(rdf.get("contributor"));
				arr = new JSONArray();
				while (iterator.hasNext()) {
					map = (Map<String, Object>) iterator.next();
					obj = new JSONObject();
					obj.put("name", map.get("prefLabel"));
					obj.put("id", map.get("@id"));
					obj.put("type", "Person"); /* guess; can't match if id is absent */
					arr.put(obj);
				}
				jcontent.put("contributor", arr);
			}

			if (rdf.containsKey("subject")) {
				iterator = getLobid2Iterator(rdf.get("subject"));
				while (iterator.hasNext()) {
					map = (Map<String, Object>) iterator.next();
					jcontent.put("keywords", map.get("prefLabel"));
				}
			}

			if (rdf.containsKey("license")) {
				arr = new JSONArray();
				iterator = getLobid2Iterator(rdf.get("license"));
				while (iterator.hasNext()) {
					map = (Map<String, Object>) iterator.next();
					obj = new JSONObject();
					// obj.put("name", map.get("prefLabel"));
					obj.put("id", map.get("@id"));
					arr.put(obj);
				}
				jcontent.put("license", arr);
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
				jcontent.put("publisher", arr);
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
					arr.put(obj);
					play.Logger.debug("Added new encoding-field");
				}
				jcontent.put("encoding", arr);
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
					}
					arr.put(obj);
					play.Logger.debug("Added new encoding-field");
				}
				jcontent.put("encoding", arr);
			}

			/**
			 * - gib die aktualisierten oder neu angelegten LRMI-Daten zurück (Format
			 * JSON-String)
			 */

			/* zunächst Anreicherung und Update der LRMI-Daten */
			return new JsonMapper().getTosciencefyLrmi(node, jcontent.toString());

		} catch (Exception e) {
			play.Logger.error("LRMI Content could not be mapped!", e);
			throw new RuntimeException("LRMI.json could not be modified or created !",
					e);
		}

	}

	/**
	 * Check if JSONObject has Array or Object structure and returns an iterator
	 * either
	 * 
	 * @param iObj a list type object (ArrayList or HashSet), which has an
	 *          iterator
	 * @return an Iterator for the list type object
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
	 * convert a three letter ISO639-2 uri into two letter ISO639-1 tag on the
	 * base of java.util.Locale example: given Uri
	 * "http://id.loc.gov/vocabulary/iso639-2/eng" will be converted in "en"
	 * 
	 * @param iso639_2Uri
	 * @return
	 */

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

}
