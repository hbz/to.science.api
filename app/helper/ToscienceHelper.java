package helper;

import org.json.JSONObject;
import java.util.List;
import java.util.Iterator;
import org.json.JSONArray;
import helper.MyEtikettMaker;
import models.Globals;

import java.util.stream.Collectors;
import org.json.JSONException;
import play.Play;

/**
 * 
 * @author adoud
 *
 */

public class ToscienceHelper {

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
		try {
			String resource_id =
					new String(Globals.protocol + Globals.server + "/resource/" + pid);
			play.Logger.debug("resource_id= " + resource_id);

			ktblAndTos = new JSONObject(contentJsFile);
			if (resource_id != null) {
				ktblAndTos.put("id", resource_id);
			}
			ktblAndTos.remove("recordingPeriod");
			ktblAndTos.remove("relatedDatasets");
			ktblAndTos.remove("info");
		} catch (JSONException e) {
			play.Logger.debug("JSONException:getToPersistTosMetadata()");
		}

		return ktblAndTos.toString();
	}

}
