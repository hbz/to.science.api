package helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.FileNotFoundException;
import org.json.JSONObject;
import play.mvc.Http.MultipartFormData.FilePart;
import org.json.JSONException;
import java.io.IOException;
import models.Globals;
import org.json.JSONArray;

/**
 * 
 * @author adoud
 *
 */
public class KTBLMapperHelper {

	/**
	 * This method gets the content of a FilePart(Json File) and returns it as a
	 * string
	 * 
	 * @param fp
	 * @return the content of the file as a string
	 */
	static public String getStringContentFromJsonFile(FilePart fp) {
		StringBuilder ktblMetadata = null;
		BufferedReader br = null;

		try {
			ktblMetadata = new StringBuilder();
			if (fp != null) {
				File file = (File) fp.getFile();
				br = new BufferedReader(new FileReader(file));
				String line;
				while ((line = br.readLine()) != null) {
					ktblMetadata.append(line);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
		play.Logger.debug("ktblMetadata.toString()=" + ktblMetadata.toString());
		return ktblMetadata.toString();
	}

	/**
	 * The method gets the required KTBL metadata from the json file and returns
	 * it as a string
	 * 
	 * @param allKtblMetadata
	 * @return required KTBL metadata, which must be persisted
	 */
	static public String getToPersistKtblMetadata(String contentJsFile,
			String pid) {

		JSONObject ktbl = new JSONObject();
		JSONObject result = new JSONObject();
		JSONObject infoObject = new JSONObject();
		JSONObject ktblAndTos = null;

		String[] elementsToPut = { "livestock_category", "ventilation_system",
				"livestock_production", "housing_systems", "additional_housing_systems",
				"emi_measurement_techniques", "emissions", "emission_reduction_methods",
				"project_title", "test_design", "info" };

		try {
			String resource_id =
					new String(Globals.protocol + Globals.server + "/resource/" + pid);
			result.put("id", resource_id);

			ktblAndTos = new JSONObject(contentJsFile);

			for (String element : elementsToPut) {
				if (ktblAndTos.has(element)) {
					Object value = ktblAndTos.get(element);
					if (value instanceof JSONArray
							&& (element.contains("additional_housing_systems")
									|| element.contains("emissions")
									|| element.contains("emission_reduction_methods")
									|| element.contains("emi_measurement_techniques"))) {
						JSONArray array = (JSONArray) value;
						ktbl.put(element, array);
					} else {
						ktbl.put(element, value);
						ktbl.put(element, Metadata2Helper.cleanString(value.toString()));
					}
				}
			}
			if (!ktblAndTos.has("info")) {
				infoObject.put("ktbl", ktbl);
				result.put("info", infoObject);
			} else {
				result.put("info", ktblAndTos.get("info"));
			}

		} catch (JSONException e) {
			play.Logger.debug("JSONException," + e);
		}

		return result.toString();
	}

	/**
	 * This method converts a JSONObject into a Map<String, Object>
	 * 
	 * @param jsonObject
	 * @return
	 */

	public static Map<String, Object> getMapFromJSONObject(
			JSONObject jsonObject) {
		Map<String, Object> map = null;
		try {
			map = new LinkedHashMap<>();
			Iterator<String> keys = jsonObject.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				Object value = jsonObject.get(key);
				map.put(key, value);
			}

		} catch (JSONException e) {
			play.Logger.debug("JSONException:getMapFromJSONObject()");
		}

		return map;
	}

	/**
	 * Method checks if a string with a json structure contains a KTBL block or
	 * not
	 * 
	 * @param json
	 * @return true if a KTBL block is present in string, otherwise false
	 */
	public static boolean containsKtblBlock(String json) {
		String[] ktblElements = { "livestock_category", "ventilation_system",
				"livestock_production", "housing_systems", "additional_housing_systems",
				"emi_measurement_techniques", "emissions", "emission_reduction_methods",
				"project_title", "test_design", "info", "relatedDatasets",
				"recordingPeriod" };

		try {
			JSONObject jo = new JSONObject(json);
			for (String element : ktblElements) {
				if (jo.has(element)) {
					return true;
				}
			}
		} catch (JSONException e) {
			play.Logger.debug("JSONException," + e);
		}
		return false;
	}

}
