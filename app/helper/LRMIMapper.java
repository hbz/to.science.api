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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
			Iterator iterator = null;
			Map<String, Object> map = null;
			Object myObj = null; /* ein Objekt zunächst unbekannten Typs/Klasse */

			/*** Beginn Mapping lobid2 => LRMI ***/
			if (rdf.containsKey("language")) {
				hashSet = (HashSet<Map<String, Object>>) rdf.get("language");
				iterator = hashSet.iterator();
				// Suche Objekt "@language" im JSONArray "@context"
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

				while (iterator.hasNext()) {
					map = (Map<String, Object>) iterator.next();
					obj.put("@language", map.get("prefLabel"));
					// obj.put("id", map.get("@id"));
					// arr.put(obj);
					break;
				}
				jcontent.put("@context", arr);
			}

			if (rdf.containsKey("contentType")) {
				arrOfString = (HashSet<String>) rdf.get("contentType");
				iterator = arrOfString.iterator();
				arr = new JSONArray();
				while (iterator.hasNext()) {
					arr.put(iterator.next());
				}
				jcontent.put("type", arr);
			}

			if (rdf.containsKey("title")) {
				arrOfString = (HashSet<String>) rdf.get("title");
				iterator = arrOfString.iterator();
				jcontent.put("name", iterator.next());
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
				hashSet = (HashSet<Map<String, Object>>) rdf.get("contributor");
				iterator = hashSet.iterator();
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

			if (rdf.containsKey("abstractText")) {
				arrOfString = (HashSet<String>) rdf.get("abstractText");
				iterator = arrOfString.iterator();
				arr = new JSONArray();
				while (iterator.hasNext()) {
					arr.put(iterator.next());
				}
				jcontent.put("description", arr);
			}

			if (rdf.containsKey("license")) {
				hashSet = (HashSet<Map<String, Object>>) rdf.get("license");
				iterator = hashSet.iterator();
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
				hashSet = (HashSet<Map<String, Object>>) rdf.get("institution");
				iterator = hashSet.iterator();
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
