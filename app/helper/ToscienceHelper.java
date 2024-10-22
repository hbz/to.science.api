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
	 * If a resource is edited via Drupal, the method gets the roles from the old
	 * data stream and saves them in the newly edited toscience data stream
	 * 
	 * @param tosOld
	 * @param tosNew
	 * @return The newly edited data stream(toscience) with the roles for ‘other’,
	 *         ‘creator’ and ‘contributor
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
			e.printStackTrace();
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
			for (String element : elementsToRemove) {
				if (ktblAndTos.has(element)) {
					ktblAndTos.remove(element);
				}
			}
		} catch (JSONException e) {
			play.Logger.debug("JSONException," + e);
		}

		return ktblAndTos.toString();
	}

}
