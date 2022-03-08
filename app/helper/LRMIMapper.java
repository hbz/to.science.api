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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
			 * die entsrpechenden Felder in den LRMI-Daten oder füge sie neu ein.
			 * Anmerkung: Löschen kann man mit dieser Methode nicht (das macht auch
			 * hier keinen Sinn, da die LRMI-Daten i.d.R. reicher sein werden als die
			 * gesendeten lobid-Daten, und man will nicht die gesamte Differenz
			 * löschen).
			 */
			/* Rückabbildung lobid2 => LRMI (vgl. JsonMapper.getLd2Lobidify2Lrmi */
			HashSet<Map<String, Object>> hashSet = null;
			ArrayList<Map<String, Object>> arrayList = null;
			HashSet<String> arrOfString = null;
			ArrayList<String> arrListOfString = null;
			Iterator iterator = null;
			Map<String, Object> map = null;
			Object myObj = null; /* ein Objekt zunächst unbekannten Typs/Klasse */

			/*** Beginn Mapping lobid2 => LRMI ***/
			if (rdf.containsKey("language")) {
				myObj = rdf.get("language");
				if (myObj instanceof java.util.ArrayList) {
					arrayList = (ArrayList<Map<String, Object>>) rdf.get("language");
					iterator = arrayList.iterator();
				} else if (myObj instanceof java.util.HashSet) {
					hashSet = (HashSet<Map<String, Object>>) rdf.get("language");
					iterator = hashSet.iterator();
				}
				// 1. Suche Objekt "@language" im JSONArray "@context"
				if (jcontent.has("@context")) {
					arr = (JSONArray) jcontent.get("@context");
					obj = null;
					for (int i = 0; i < arr.length(); i++) {
						myObj = arr.get(i);
						play.Logger
								.debug("i=" + i + "; myObj.getClass()=" + myObj.getClass());
						if (myObj instanceof org.json.JSONObject) {
							obj = (JSONObject) arr.getJSONObject(i);
							if (obj.has("@language")) {
								break;
							}
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

				// 2. Sprache auch auf das LRMI-Feld "inLanguage" abbilden
				JSONArray inLanguageArr = new JSONArray();

				while (iterator.hasNext()) {
					map = (Map<String, Object>) iterator.next();
					obj.put("@language", map.get("prefLabel"));
					// obj.put("id", map.get("@id"));
					inLanguageArr.put(map.get("prefLabel"));
					break;
				}
				while (iterator.hasNext()) {
					map = (Map<String, Object>) iterator.next();
					inLanguageArr.put(map.get("prefLabel"));
				}
				jcontent.put("@context", arr);
				jcontent.put("inLanguage", inLanguageArr);
			}

			if (rdf.containsKey("contentType")) {
				myObj = rdf.get("contentTyp");
				if (myObj instanceof java.util.ArrayList) {
					arrListOfString = (ArrayList<String>) rdf.get("contentType");
					iterator = arrListOfString.iterator();
				} else if (myObj instanceof java.util.HashSet) {
					arrOfString = (HashSet<String>) rdf.get("contentType");
					iterator = arrOfString.iterator();
				}
				arr = new JSONArray();
				while (iterator.hasNext()) {
					arr.put(iterator.next());
				}
				jcontent.put("type", arr);
			}

			if (rdf.containsKey("title")) {
				myObj = rdf.get("title");
				if (myObj instanceof java.util.ArrayList) {
					arrListOfString = (ArrayList<String>) rdf.get("title");
					iterator = arrListOfString.iterator();
				} else if (myObj instanceof java.util.HashSet) {
					arrOfString = (HashSet<String>) rdf.get("title");
					iterator = arrOfString.iterator();
				}
				jcontent.put("name", iterator.next());
			}

			if (rdf.containsKey("medium")) {
				myObj = rdf.get("medium");
				if (myObj instanceof java.util.ArrayList) {
					arrayList = (ArrayList<Map<String, Object>>) rdf.get("medium");
					iterator = arrayList.iterator();
				} else if (myObj instanceof java.util.HashSet) {
					hashSet = (HashSet<Map<String, Object>>) rdf.get("medium");
					iterator = hashSet.iterator();
				}
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

			if (rdf.containsKey("creator")) {
				myObj = rdf.get("creator");
				if (myObj instanceof java.util.ArrayList) {
					arrayList = (ArrayList<Map<String, Object>>) rdf.get("creator");
					iterator = arrayList.iterator();
				} else if (myObj instanceof java.util.HashSet) {
					hashSet = (HashSet<Map<String, Object>>) rdf.get("creator");
					iterator = hashSet.iterator();
				}
				arr = new JSONArray();
				while (iterator.hasNext()) {
					map = (Map<String, Object>) iterator.next();
					obj = new JSONObject();
					obj.put("name", map.get("prefLabel"));
					obj.put("id", map.get("@id"));
					obj.put("type", "Person"); /* guess */
					arr.put(obj);
				}
				jcontent.put("creator", arr);

			}

			if (rdf.containsKey("contributor")) {
				myObj = rdf.get("contributor");
				if (myObj instanceof java.util.ArrayList) {
					arrayList = (ArrayList<Map<String, Object>>) rdf.get("contributor");
					iterator = arrayList.iterator();
				} else if (myObj instanceof java.util.HashSet) {
					hashSet = (HashSet<Map<String, Object>>) rdf.get("contributor");
					iterator = hashSet.iterator();
				}
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

			if (rdf.containsKey("description")) {
				myObj = rdf.get("description");
				if (myObj instanceof java.util.ArrayList) {
					arrListOfString = (ArrayList<String>) rdf.get("description");
					iterator = arrListOfString.iterator();
				} else if (myObj instanceof java.util.HashSet) {
					arrOfString = (HashSet<String>) rdf.get("description");
					iterator = arrOfString.iterator();
				}
				jcontent.put("description", iterator.next());
			}

			if (rdf.containsKey("license")) {
				myObj = rdf.get("license");
				if (myObj instanceof java.util.ArrayList) {
					arrayList = (ArrayList<Map<String, Object>>) rdf.get("license");
					iterator = arrayList.iterator();
				} else if (myObj instanceof java.util.HashSet) {
					hashSet = (HashSet<Map<String, Object>>) rdf.get("license");
					iterator = hashSet.iterator();
				}
				arr = new JSONArray();
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
				myObj = rdf.get("institution");
				if (myObj instanceof java.util.ArrayList) {
					arrayList = (ArrayList<Map<String, Object>>) rdf.get("institution");
					iterator = arrayList.iterator();
				} else if (myObj instanceof java.util.HashSet) {
					hashSet = (HashSet<Map<String, Object>>) rdf.get("institution");
					iterator = hashSet.iterator();
				}
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
				myObj = rdf.get("hasPart");
				if (myObj instanceof java.util.ArrayList) {
					arrayList = (ArrayList<Map<String, Object>>) rdf.get("hasPart");
					iterator = arrayList.iterator();
				} else if (myObj instanceof java.util.HashSet) {
					hashSet = (HashSet<Map<String, Object>>) rdf.get("hasPart");
					iterator = hashSet.iterator();
				}

				arr = new JSONArray();
				while (iterator.hasNext()) {
					map = (Map<String, Object>) iterator.next();
					obj = new JSONObject();
					obj.put("type", "MediaType");
					obj.put("contentUrl", map.get("@id"));
					obj.put("prefLabel", map.get("prefLabel"));
					arr.put(obj);
					play.Logger.debug("Added new encoding-field");
				}
				jcontent.put("encoding", arr);
			} else {
				play.Logger.debug("no Child found in lobid2, try to get it from lobid");
				Map<String, Object> l1rdf = node.getLd1();
				if (l1rdf.containsKey("hasPart")) {
					play.Logger.debug("found Child in lobid");
					if (myObj instanceof java.util.ArrayList) {
						arrayList = (ArrayList<Map<String, Object>>) l1rdf.get("hasPart");
						iterator = arrayList.iterator();
					} else if (myObj instanceof java.util.HashSet) {
						hashSet = (HashSet<Map<String, Object>>) rdf.get("hasPart");
						iterator = hashSet.iterator();
					}
					arr = new JSONArray();
					while (iterator.hasNext()) {
						map = (Map<String, Object>) iterator.next();
						obj = new JSONObject();
						obj.put("type", "MediaType");
						// obj.put("contentUrl", map.get("@id"));
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

}
