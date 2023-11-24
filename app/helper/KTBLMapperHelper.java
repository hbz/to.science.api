package helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;
import play.mvc.Http.MultipartFormData.FilePart;

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
		StringBuilder ktblMetadata = new StringBuilder();

		if (fp != null) {
			play.Logger.debug("FilePart !=NULL");
			File file = (File) fp.getFile();
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				String line;
				while ((line = br.readLine()) != null) {
					ktblMetadata.append(line);
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

		JSONObject allKtblMetadata = new JSONObject(contentJsFile);
		JSONObject wantedKtblMetadata = new JSONObject();

		wantedKtblMetadata.put("recordingPeriod",
				allKtblMetadata.getJSONArray("recordingPeriod"));
		wantedKtblMetadata.put("relatedDatasets",
				allKtblMetadata.getJSONArray("relatedDatasets"));
		wantedKtblMetadata.put("info", allKtblMetadata.get("info"));

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
		Map<String, Object> map = new LinkedHashMap<>();

		Iterator<String> keys = jsonObject.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			Object value = jsonObject.get(key);
			map.put(key, value);
		}

		return map;
	}

}
