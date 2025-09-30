/*
* Copyright 2025 hbz NRW(http://www.hbz-nrw.de/)
*
* Licensed under the Apache License,Version 2.0(the"License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,software
* distributed under the License is distributed on an"AS IS"BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package helper;

import java.io.Closeable;

import org.apache.commons.httpclient.HttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.CrawlerModel;
import models.Gatherconf;

import models.Node;
import play.Play;

/**
 * a class to implement a wpull crawl
 * 
 * @author Ingolf Kuss
 *
 */
public class BrowsertrixWorkflow extends CrawlerModel {

	/* Browsertrix spezifische Variablen */
	private String bearerToken = null;

	/*
	 * Authorisierung für Browsertrix
	 */
	final static String btrix_api_url = Play.application().configuration()
			.getString("regal-api.browsertrix.apiUrl");
	final static String btrix_admin_username = Play.application().configuration()
			.getString("regal-api.bowsertrix.adminUsername");
	final static String btrix_admin_password = Play.application().configuration()
			.getString("regal-api.browsertrix.adminPassword");
	final static String btrix_org_name = Play.application().configuration()
			.getString("regal-api.browsertrix.orgName");
	final static String btrix_orgid = Play.application().configuration()
			.getString("regal-api.browsertrix.orgId");
	/**
	 * Im Verzeichnis outDir liegen die fertigen Crawls. Von hier aus werden die
	 * Crawls direkt von Wayback indexiert.
	 */
	final static String outDir = Play.application().configuration()
			.getString("regal-api.browsertrix.outDir");

	/**
	 * Konstruktor zu Browsertrix Crawler Workflow
	 * 
	 * @param node der Knoten der Website, zu der ein neuer Crawl gestartet werden
	 *          soll.
	 * @param conf the crawler configuration for the website
	 */
	public BrowsertrixWorkflow(Node node, Gatherconf conf) {
		super(node, conf);

		try {
			/*
			 * Wenn es noch keine Worfkflow ID in der conf gibt, wird jetzt eine
			 * angelegt.
			 */
			if (conf.getBtrixWorkflowId() == null) {
				getBearerToken();
				// create Crawler Config
			}
		} catch (Exception e) {
			WebgatherLogger.error("Ungültige URL :" + conf.getUrl() + " !");
			throw new RuntimeException(e);
		}
	}

	private void getBearerToken() {
		CloseableHttpClient httpClient = null;
		HttpResponse tokenResponse = null;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			httpClient = HttpClients.createDefault();
			HttpPost tokenRequest = new HttpPost(btrix_api_url + "/auth/jwt/login");
			tokenRequest.addHeader("Content-Type",
					"application/x-www-form-urlencoded");
			tokenRequest.setEntity(new StringEntity("username=" + btrix_admin_username
					+ "&password=" + btrix_admin_password + "&grant_type=password"));
			tokenRequest.addHeader("Accept", "application/json");
			tokenResponse = httpClient.execute(tokenRequest);
			if (tokenResponse.getStatusLine().getStatusCode() == 200) {
				String tokenResponseJson =
						EntityUtils.toString(tokenResponse.getEntity());
				JsonNode tokenJsonNode = objectMapper.readTree(tokenResponseJson);
				this.bearerToken = tokenJsonNode.get("access_token").asText();
				WebgatherLogger.debug("Got bearer Token " + this.bearerToken);
			} else {
				throw new RuntimeException("Status-Code von /auth/jwt/login: "
						+ tokenResponse.getStatusLine().getStatusCode());
			}
		} catch (Exception e) {
			msg = "Bearer-Token für Browsertrix-Workflow für PID" + node.getPid()
					+ " kann nicht geholt werden!";
			WebgatherLogger.error(msg, e.toString());
			throw new RuntimeException(e);
		} finally {
			try {
				httpClient.close();
				((Closeable) tokenResponse).close();
			} catch (Exception e) {
				WebgatherLogger.warn("httpClient kann nicht geschlossen werden.",
						e.toString());
			}
		}
	}

	/**
	 * Ruft den CDN-Gatherer für diese Website auf, anschließend Browsertrix für
	 * den Hauptcrawl
	 */
	@Override
	public void startJob() {
		super.startJob();

		try {

			// Bereite Kommando für den Hauptcrawl vor

		} catch (Exception e) {
			WebgatherLogger.error(e.toString());
			throw new RuntimeException("Browsertrix crawl not successfully started!",
					e);
		}
	}

}
