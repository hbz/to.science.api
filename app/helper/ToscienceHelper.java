package helper;

import org.json.JSONObject;
import java.util.List;
import java.util.Iterator;
import org.json.JSONArray;
import helper.MyEtikettMaker;
import java.util.List;
import java.util.stream.Collectors;
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
		while (keys.hasNext()) {
			String key = keys.next();
			Object value = allJsonObjects.get(key);
			if (value.toString().contains("prefLabel")) {
				if (value instanceof JSONObject) {
					jsObject = allJsonObjects.getJSONObject(key);
					oldPrefLabel = jsObject.get("prefLabel");
					if (oldPrefLabel.toString().contains("http")) {
						String newPrefLabel =
								MyEtikettMaker.getLabelFromEtikettWs(oldPrefLabel.toString());
						play.Logger.debug("neuPrefLabel=" + newPrefLabel);
						jsObject.put("prefLabel", newPrefLabel);
					}
				} else if (value instanceof JSONArray) {
					JSONArray jsArray = allJsonObjects.getJSONArray(key);
					for (int j = 0; j < jsArray.length(); j++) {
						jsObject = jsArray.getJSONObject(j);
						oldPrefLabel = jsObject.get("prefLabel");
						if (oldPrefLabel.toString().contains("http")) {
							String newPrefLabel =
									MyEtikettMaker.getLabelFromEtikettWs(oldPrefLabel.toString());
							play.Logger.debug("neuPrefLabel=" + newPrefLabel);
							jsObject.put("prefLabel", newPrefLabel);
						}
					}
				}

			}

		}
		return allJsonObjects;

	}

}
