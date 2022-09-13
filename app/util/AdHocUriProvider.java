/**
 * 
 */
package util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

import models.Globals;
import play.libs.ws.WSResponse;

/**
 * @author aquast
 *
 *         The Class provides a simplified way to create an adHocUri based on a
 *         new to.science.label Object (formely known as "regal etikett") which
 *         is required for the lobid metadata format.
 * 
 *         Class is based on the method code of Enrich.java
 *         enrichLRMIData-method simplified in comparisation to this by reducing
 *         functionality and variables
 */
public class AdHocUriProvider {

	private String labelValue = null;

	private InputStream inStream;

	/**
	 * @param LabelValue the value for which we like to create an AdHocUri
	 * @return a AdHocUri representing a newly created to.science.label
	 */
	public String getAdhocUri(String LabelValue) {

		String adHocUri = null;
		this.labelValue = LabelValue;

		adHocUri = generateNewAdHocUri();

		return adHocUri;
	}

	private String generateNewAdHocUri() {
		String adHocUri = null;

		play.Logger.debug("Start AdHocUri generation for " + labelValue);

		// String encodedValue = labelValue.replace(" ", "+");
		try {
			String encodedValue =
					URLEncoder.encode(labelValue, StandardCharsets.UTF_8.toString())
							.replaceAll("\\+", "%20").replaceAll("%21", "!")
							.replaceAll("%27", "'").replaceAll("%28", "(")
							.replaceAll("%29", ")").replaceAll("%7E", "~");
			WSResponse response = play.libs.ws.WS
					.url(Globals.zettelUrl + "/localAutocomplete" + "?q=" + encodedValue)
					.setFollowRedirects(true).get().get(2000);
			inStream = response.getBodyAsStream();
			String formsResponseBody =
					CharStreams.toString(new InputStreamReader(inStream, Charsets.UTF_8));
			Closeables.closeQuietly(inStream);
			// fetch annoying errors from to.science.forms service
			if (response.getStatus() != 200) {
				play.Logger.error(
						"to.science.forms service request localAutocomplete fails for "
								+ encodedValue + "\nUse URI for setting Label now!");
			} else {
				// Parse out uri value from JSON structure
				JSONArray jFormsResponse = new JSONArray(formsResponseBody);
				JSONObject jFormsObject = jFormsResponse.getJSONObject(0);
				adHocUri = new String(jFormsObject.getString("value"));
				play.Logger.debug("Found adHoc-URI: " + adHocUri);
			}
		} catch (Exception e) {
			play.Logger.error("Content could not be enriched!", e);
			throw new RuntimeException("LRMI.json could not be enriched", e);
		}

		return adHocUri;
	}
}
