/**
 * 
 */
package util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * @author aquast
 *
 */
public class JsonMapperUtils {

	/**
	 * This IteratorBuilder checks if JSONObject is kind of JSONArray or kind of
	 * JSONObject and returns an Iterator either
	 * 
	 * @param iObj a JSONObject of unknown internal structure
	 * @return an Iterator representing the JSONObject
	 */
	public Iterator<Map<String, Object>> getJsonObjectIterator(Object iObj) {
		Iterator<Map<String, Object>> lIterator = null;
		if (iObj instanceof java.util.ArrayList) {
			ArrayList<Map<String, Object>> jList =
					(ArrayList<Map<String, Object>>) iObj;
			lIterator = jList.iterator();
		} else if (iObj instanceof java.util.HashSet) {
			HashSet<Map<String, Object>> jHashSet =
					(HashSet<Map<String, Object>>) iObj;
			lIterator = jHashSet.iterator();
		} else {
			play.Logger.error(
					"Can't generate Iterator - Object provided to Method is kind of: "
							+ iObj.getClass().toString());
		}
		return lIterator;
	}

}
