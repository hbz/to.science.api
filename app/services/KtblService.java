package services;

import com.typesafe.config.ConfigFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 
 * @author adoud
 *
 */
public class KtblService {

	private final static String etikettUser =
			ConfigFactory.load().getString("regal-api.etikett.user");
	private final static String etikettPwd =
			ConfigFactory.load().getString("regal-api.etikett.pwd");

	/**
	 * This method checks whether a Uri has a label; if not, a new data record
	 * will be created for the Uri in Etikett
	 * 
	 * @param uri
	 * @param label
	 */
	public static void checkAndLoadUri(String uri, String label) {
		try {
			if (getLabelFromEtikett(uri).equals(uri)) {
				createLabelByEtikett(uri, label);
			}
		} catch (Exception e) {
			play.Logger.error(e.toString());
		}
	}

	/**
	 * This method gets the label of a URI if it exists, if not the Uri will be
	 * returned.
	 * 
	 * @param uri
	 * @return
	 */
	public static String getLabelFromEtikett(String uri) {
		try {
			play.Logger.debug("getLabelFromEtikett() has been called");
			if (uri == null || uri.isEmpty())
				return uri;
			if (uri.endsWith("#!"))
				uri = uri.substring(0, uri.length() - 2);
			BufferedReader in = null;
			StringBuffer response = null;
			String auth = etikettUser + ":" + etikettPwd;
			String authHeaderValue =
					"Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
			URL url = new URL(
					"http://localhost:9002/tools/etikett?url=" + uri + "&column=label");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Authorization", authHeaderValue);
			con.setRequestProperty("Accept", "text/plain");
			int responseCode = con.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				response = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
			}
			play.Logger.debug(
					"getLabelFromEtikett(),response.toString()=" + response.toString());
			in.close();
			con.disconnect();
			return response.toString();
		} catch (Exception e) {
			return uri;
		}
	}

	/**
	 * This method creates a new data record at Etikett
	 * 
	 * @param uri
	 * @param label
	 */
	public static void createLabelByEtikett(String uri, String label) {
		try {
			play.Logger.debug("createLabelByEtikett() has been called");
			String endpointUrl = "http://localhost:9002/tools/etikett/update";
			String auth = etikettUser + ":" + etikettPwd;
			String authHeaderValue =
					"Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
			String jsonPayload =
					"{\"uri\": \"" + uri + "\", \"label\": \"" + label + "\"}";
			play.Logger
					.debug("createLabelByEtikett(), uri=" + uri + "label=" + label);
			URL url = new URL(endpointUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Authorization", authHeaderValue);
			con.setRequestProperty("Content-Type", "application/json");
			con.setDoOutput(true);

			try (OutputStream outputStream = con.getOutputStream()) {
				byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
				outputStream.write(input, 0, input.length);
			}
			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				play.Logger.debug("created new Label in Etikett:");
			} else {
				play.Logger.debug("Unable to created new label in Etikett:");
			}
			con.disconnect();
		} catch (Exception e) {
			play.Logger.error(e.toString());

		}
	}
}
