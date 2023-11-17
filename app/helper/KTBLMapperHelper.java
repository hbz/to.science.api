package helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.json.JSONObject;
import play.mvc.Http.MultipartFormData.FilePart;

/**
 * 
 * @author adoud
 *
 */
public class KTBLMapperHelper {

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
	 * 
	 * @param allKtblMetadata
	 * @return
	 */
	static public Object getWantedToPersistKtblMetadata(String contentJsFile) {

		JSONObject allKtblMetadata = new JSONObject(contentJsFile);
		play.Logger.debug("allKtblMetadata=" + allKtblMetadata.toString());

		Object valueOFinfo = allKtblMetadata.get("info");
		play.Logger.debug("valueOFinfo=" + valueOFinfo.toString());

		return valueOFinfo;

	}

}
