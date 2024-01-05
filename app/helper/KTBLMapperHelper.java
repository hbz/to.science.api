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
	static public String getToPersistKtblMetadata(String contentJsFile) {

		JSONObject wantedKtblMetadata = null;
		JSONObject allKtblMetadata = null;

		allKtblMetadata = new JSONObject(contentJsFile);
		wantedKtblMetadata = new JSONObject();

		if (allKtblMetadata.has("recordingPeriod")) {
			wantedKtblMetadata.put("recordingPeriod",
					allKtblMetadata.getJSONArray("recordingPeriod"));
		}
		if (allKtblMetadata.has("relatedDatasets")) {
			wantedKtblMetadata.put("relatedDatasets",
					allKtblMetadata.getJSONArray("relatedDatasets"));
		}
		if (allKtblMetadata.has("info")) {
			wantedKtblMetadata.put("info", allKtblMetadata.get("info"));
		}

		return wantedKtblMetadata.toString();
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

}
