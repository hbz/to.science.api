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
import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static archive.fedora.Vocabulary.*;
import actions.Modify;
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
public class BtrixWebclient extends CrawlerModel {

	/* Browsertrix spezifische Variablen */
	private CloseableHttpClient httpClient = null;
	private HttpEntityEnclosingRequestBase entityEnclosingRequest = null;
	private HttpRequestBase request = null;
	private CloseableHttpResponse response = null;
	private ObjectMapper objectMapper = new ObjectMapper();
	private String bearerToken = null;
	private String scopeType = null;
	private String btrixWorkflowId = null;
	private String started = null;

	/*
	 * Authorisierung für Browsertrix
	 */
	final static String btrix_api_url =
			Play.application().configuration().getString("regal-api.btrix.apiUrl");
	final static String btrix_admin_username = Play.application().configuration()
			.getString("regal-api.btrix.adminUsername");
	final static String btrix_admin_password = Play.application().configuration()
			.getString("regal-api.btrix.adminPassword");
	final static String btrix_org_name =
			Play.application().configuration().getString("regal-api.btrix.orgName");
	final static String btrix_orgid =
			Play.application().configuration().getString("regal-api.btrix.orgId");

	/**
	 * (Leerer) Konstruktor für den Browsertrix Webclient
	 * 
	 * Dieser Konstruktor wird benötigt, um Aufrufe an Browsertrix zu ermöglichen,
	 * die noch nicht auf ein toscience-Objekt bezogen sind.
	 */
	public BtrixWebclient() {
		super();
		/**
		 * Das Arbeitsverzeichnis von Browsertrix-Crawls für den CDN-Precrawl ist
		 * jobDir. jobDir sollte ein lokales Verzeichnis sein.
		 */
		this.setJobDir(
				Play.application().configuration().getString("regal-api.btrix.jobDir"));
		/**
		 * Im Verzeichnis outDir liegen die fertigen Crawls. Von hier aus werden die
		 * Crawls direkt von Wayback indexiert.
		 */
		this.setOutDir(
				Play.application().configuration().getString("regal-api.btrix.outDir"));
		try {
			getBearerToken();
		} catch (Exception e) {
			WebgatherLogger.error("Browsertrix-Workflow für PID " + node.getPid()
					+ " URL " + conf.getUrl() + " kann nicht angelegt werden !");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Konstruktor zu Browsertrix Crawler Workflow
	 * 
	 * @param node der Knoten der Website, zu der ein neuer Crawl gestartet werden
	 *          soll.
	 * @param conf the crawler configuration for the website
	 */
	public BtrixWebclient(Node node, Gatherconf conf) {
		super(node, conf);
		/**
		 * Das Arbeitsverzeichnis von Browsertrix-Crawls für den CDN-Precrawl ist
		 * jobDir. jobDir sollte ein lokales Verzeichnis sein.
		 */
		this.setJobDir(
				Play.application().configuration().getString("regal-api.btrix.jobDir"));
		/**
		 * Im Verzeichnis outDir liegen die fertigen Crawls. Von hier aus werden die
		 * Crawls direkt von Wayback indexiert.
		 */
		this.setOutDir(
				Play.application().configuration().getString("regal-api.btrix.outDir"));
		this.setCrawlDir(
				new File(this.getJobDir() + "/" + conf.getName() + "/" + datetime));
		this.setResultDir(
				new File(this.getOutDir() + "/" + conf.getName() + "/" + datetime));
		this.setCdxFile(new File(
				this.getOutDir() + "/" + conf.getName() + "/WEB-" + host + ".cdx"));
		try {
			getBearerToken();
			if (conf.getBtrixWorkflowId() != null) {
				this.btrixWorkflowId = conf.getBtrixWorkflowId();
				WebgatherLogger.debug("btrixWorkflowId: " + btrixWorkflowId);
			}
			/*
			 * Wenn es noch keine Worfkflow ID in der conf gibt, wird jetzt eine
			 * angelegt. Ansonsten wird ein Update ("Patch") gemacht.
			 */
			updateCrawlerConfig();
		} catch (Exception e) {
			WebgatherLogger.error("Browsertrix-Workflow für PID " + node.getPid()
					+ " URL " + conf.getUrl() + " kann nicht angelegt werden !");
			throw new RuntimeException(e);
		}
	}

	private void getBearerToken() {
		try {
			httpClient = HttpClients.createDefault();
			entityEnclosingRequest = new HttpPost(btrix_api_url + "/auth/jwt/login");
			WebgatherLogger.debug("btrix_api_url " + btrix_api_url);
			WebgatherLogger.debug("btrix_admin_username " + btrix_admin_username);
			// WebgatherLogger.debug("btrix_admin_password " + btrix_admin_password);
			entityEnclosingRequest.addHeader("Content-Type",
					"application/x-www-form-urlencoded");
			entityEnclosingRequest
					.setEntity(new StringEntity("username=" + btrix_admin_username
							+ "&password=" + btrix_admin_password + "&grant_type=password"));
			entityEnclosingRequest.addHeader("Accept", "application/json");
			response = httpClient.execute(entityEnclosingRequest);
			if (response.getStatusLine().getStatusCode() == 200) {
				String tokenResponseJson = EntityUtils.toString(response.getEntity());
				JsonNode tokenJsonNode = objectMapper.readTree(tokenResponseJson);
				this.bearerToken = tokenJsonNode.get("access_token").asText();
				WebgatherLogger.debug("Got bearer Token " + this.bearerToken);
			} else {
				throw new RuntimeException("Status-Code von /auth/jwt/login: "
						+ response.getStatusLine().getStatusCode());
			}
		} catch (Exception e) {
			msg = "Bearer-Token für Browsertrix-Workflow für PID " + node.getPid()
					+ " kann nicht geholt werden!";
			WebgatherLogger.error(msg, e.toString());
			throw new RuntimeException(e);
		} finally {
			try {
				httpClient.close();
				response.close();
			} catch (Exception e) {
				WebgatherLogger.warn("httpClient kann nicht geschlossen werden.",
						e.toString());
			}
		}
	}

	/**
	 * Diese Methode führt einen GET-Request auf den Browsertrix-Endpoint Get
	 * Crawl Configs durch.
	 * 
	 * API-Doc: https://docs.browsertrix.com/api/#tag/crawlconfigs/operation/
	 * get_crawl_configs_api_orgs__oid__crawlconfigs_get
	 * 
	 * @param queryString ein queryString für die Anfrage
	 * @return a JSON Object with the found Crawl Configs
	 */
	public JSONObject getCrawlConfigs(String queryString) {
		try {
			httpClient = HttpClientBuilder.create().build();
			request = new HttpGet(btrix_api_url + "/orgs/" + btrix_orgid
					+ "/crawlconfigs?" + queryString);
			WebgatherLogger.debug("request = " + request.toString());
			request.addHeader("Authorization", "Bearer " + this.bearerToken);
			request.addHeader("Accept", "application/json");
			response = httpClient.execute(request);
			String responseJson = getResponseJson();
			WebgatherLogger.debug("received response: " + responseJson);
			// JSON ausparsen
			JSONObject responseJsonObject = new JSONObject(responseJson);
			int total = responseJsonObject.getInt("total");
			WebgatherLogger.debug("Found a number of " + total + " item(s).");
			return responseJsonObject;

		} catch (Exception e) {
			msg = "Could not get Crawl Configs for queryString " + queryString;
			WebgatherLogger.error(msg, e.getMessage());
			throw new RuntimeException(e);
		} finally {
			try {
				httpClient.close();
				response.close();
			} catch (Exception e) {
				WebgatherLogger.warn("httpClient kann nicht geschlossen werden.",
						e.toString());
			}
		}
	}

	private void updateCrawlerConfig() {
		try {
			httpClient = HttpClientBuilder.create().build();
			if (this.btrixWorkflowId == null) {
				entityEnclosingRequest = new HttpPost(
						btrix_api_url + "/orgs/" + btrix_orgid + "/crawlconfigs/");
			} else {
				entityEnclosingRequest = new HttpPatch(btrix_api_url + "/orgs/"
						+ btrix_orgid + "/crawlconfigs/" + btrixWorkflowId);
			}
			WebgatherLogger.debug("btrix_api_url " + btrix_api_url);
			WebgatherLogger.debug("btrix_orgid " + btrix_orgid);
			WebgatherLogger.debug("request = " + entityEnclosingRequest.toString());
			entityEnclosingRequest.addHeader("Authorization",
					"Bearer " + this.bearerToken);
			entityEnclosingRequest.addHeader("Content-Type", "application/json");
			String jsonBody = createJsonBody();
			WebgatherLogger.debug("jsonBody=" + jsonBody);
			entityEnclosingRequest.setEntity(new StringEntity(jsonBody, "UTF-8"));
			entityEnclosingRequest.addHeader("Accept", "application/json");
			response = httpClient.execute(entityEnclosingRequest);
			String responseJson = getResponseJson();
			WebgatherLogger.debug("received response: " + responseJson);
			// JSON ausparsen
			JSONObject responseJsonObject = new JSONObject(responseJson);
			if (this.btrixWorkflowId == null) {
				this.btrixWorkflowId = responseJsonObject.getString("id");
				WebgatherLogger
						.debug("Crawler Workflow angelegt mit btrix_workflow_id: "
								+ btrixWorkflowId);
				/**
				 * hier werden die ersten 12 Stellen der Crawler Workflow ID (cid) als
				 * "description" hinterlegt. Dies geschieht für den späteren Abgleich
				 * mit den crawl_ids. Diese enthalten nur die ersten 12 Ziffern der
				 * Workflow IDs.
				 */
				Thread.sleep(10000);
				entityEnclosingRequest = new HttpPatch(btrix_api_url + "/orgs/"
						+ btrix_orgid + "/crawlconfigs/" + btrixWorkflowId);
				entityEnclosingRequest.addHeader("Authorization",
						"Bearer " + this.bearerToken);
				entityEnclosingRequest.addHeader("Content-Type", "application/json");
				JSONObject data = new JSONObject(jsonBody);
				data.put("description", btrixWorkflowId.substring(0, 12));
				entityEnclosingRequest
						.setEntity(new StringEntity(data.toString(), "UTF-8"));
				entityEnclosingRequest.addHeader("Accept", "application/json");
				response = httpClient.execute(entityEnclosingRequest);
				responseJson = getResponseJson();
				WebgatherLogger.debug("received response from update wit description "
						+ btrixWorkflowId.substring(0, 12) + ": " + responseJson);
				/* Übernahme der WorkflowId in die toscience Crawler Conf */
				conf.setBtrixWorkflowId(btrixWorkflowId);
				msg = new Modify().updateConf(node, conf.toString());
				WebgatherLogger.info(msg);
			} else {
				WebgatherLogger.debug("Crawler Workflow mit btrix_workflow_id "
						+ btrixWorkflowId + " wurde aktualisiert.");
			}
		} catch (Exception e) {
			msg = "Browsertrix Crawler Config für PID " + node.getPid()
					+ " kann nicht gesendet werden!";
			WebgatherLogger.error(msg, e.getMessage());
			throw new RuntimeException(e);
		} finally {
			try {
				httpClient.close();
				response.close();
			} catch (Exception e) {
				WebgatherLogger.warn("httpClient kann nicht geschlossen werden.",
						e.toString());
			}
		}
	}

	private String createJsonBody() {
		JSONObject data = new JSONObject();
		try {
			// Name oder Titel der Site
			data.put("name", conf.getName());
			String md = node.getMetadata(toscience);
			if (md != null) {
				JSONObject jo = new JSONObject(md);
				if (jo.has("title")) {
					// Hole Titel aus den toscience-Metadaten
					data.put("name", jo.getJSONArray("title").get(0).toString());
				}
			}
			data.put("inactive", !conf.isActive());
			// data.put("description", conf.getNotices());
			JSONArray tags = new JSONArray();
			tags.put(conf.getName());
			data.put("tags", tags);
			// maximale Crawlgröße in Byte
			data.put("maxCrawlSize", conf.getMaxCrawlSize());
			if (conf.getMaxCrawlSize() > 0) {
				switch (conf.getQuotaUnitSelection()) {
				case KB:
					data.put("maxCrawlSize", conf.getMaxCrawlSize() * 1000);
					break;
				case MB:
					data.put("maxCrawlSize", conf.getMaxCrawlSize() * 1000000);
					break;
				case GB:
					data.put("maxCrawlSize", conf.getMaxCrawlSize() * 1000000000);
					break;
				default:
					// standardmäßig wird Kilobyte angenommen
					data.put("maxCrawlSize", conf.getMaxCrawlSize() * 1000);
					break;
				}
			}
			// Und jetzt eine Config aufbauen:
			JSONObject config = new JSONObject();
			switch (conf.getCrawlSubdomains()) {
			case hostnames:
				this.scopeType = "host";
				break;
			case domains:
				this.scopeType = "domain";
				break;
			default:
				// standardmäßig wird die Domain ohne Subdomains eingesammelt
				this.scopeType = "host";
				break;
			}
			JSONArray seeds = new JSONArray();
			JSONObject seed =
					createSeed(this.urlAscii, this.scopeType, conf.getDeepness());
			seeds.put(seed);
			/*
			 * zu inkludierende (zusätzliche) Domains. Diese erhalten jeweils ein
			 * eigenes "Seed" und zusätzlich einen Eintrag im Array "include".
			 */
			JSONArray include = new JSONArray();
			for (String domain : conf.getDomains()) {
				include.put(domain);
				seeds.put(createSeed(domain, this.scopeType, conf.getDeepness()));
			}
			config.put("seeds", seeds);
			config.put("scopeType", this.scopeType);
			config.put("include", include);
			/*
			 * Exclusions = auszuschließende Bereiche
			 */
			JSONArray exclude = new JSONArray();
			for (String urlExcluded : conf.getUrlsExcluded()) {
				exclude.put(urlExcluded);
			}
			config.put("exclude", exclude);
			config.put("depth", conf.getDeepness());
			config.put("extraHops", 1);
			config.put("lang", "de");
			config.put("blockAds", true);
			// Limits amount of time to wait for a page to load; in Sekunden
			// nimm default Wert
			// config.put("pageLoadTimeout", 120);
			// Delay Before Next Page; in Sekunden
			config.put("pageExtraDelay", conf.getWaitSecBtRequests());
			config.put("useSitemap", true);
			config.put("userAgent",
					Gatherconf.agentTable.get(conf.getAgentIdSelection()));
			data.put("config", config);
		} catch (JSONException e) {
			msg = "Crawlerconf JSON (JsonBody) für PID " + node.getPid()
					+ " kann nicht gebaut werden!";
			WebgatherLogger.error(msg, e.getMessage());
		}
		return data.toString();
	} // ENDE createJsonBody()

	private JSONObject createSeed(String url, String seedScopeType, int depth) {
		JSONObject seed = new JSONObject();
		try {
			seed.put("url", url);
			seed.put("scopeType", seedScopeType);
			seed.put("depth", depth);
			/* one hop out -- the crawler will visit pages one link away. */
			seed.put("extraHops", 1);
		} catch (JSONException e) {
			msg = "Seed with url " + url + " could not be created!";
			WebgatherLogger.warn(msg, e.getMessage());
		}
		return seed;
	}

	/**
	 * Erzeugt einen neuen Browsertrix-Crawler-Job
	 */
	@Override
	public void createCrawl() {
		super.createCrawl();
	}

	/**
	 * Ruft den CDN-Gatherer für diese Website auf, anschließend Browsertrix für
	 * den Hauptcrawl
	 */
	@Override
	public void startCrawl() {
		// Dies führt den CDN-Precrawl aus.
		super.startCrawl();

		/*
		 * Das hier muss in einem Thread passieren; wie bei WpullCawl.startCrawl()
		 * ==> Wirklich ?? NEIN
		 */
		try {
			/**
			 * Für Browsertrix-Crawls wird die cdx-Datei des CDN-Precrawls hier ein
			 * Verzeichnis höher kopiert, um als Vorlage für den nächsten CDN-Precrawl
			 * dienen zu können. Für wpull-Crawls ist das nicht notwendig, da die
			 * CDX-Datei in die des Hauptcrawls integriert ist. Diese wird am Ende des
			 * Hauptcrawls ein Verzeichnis höher kopiert (siehe "cdxFileSave" in
			 * Create.createWebpageVersion). Evtl. kann man auch für Browsertrix
			 * diesen Punkt durch eine Aktion ersetzen, die am Ende des Hauptcrawls
			 * geschehen wird.
			 */
			this.setCdxFileNew(new File(
					this.getResultDir().getAbsolutePath() + "/" + warcFilename + ".cdx"));
			if (this.getCdxFileNew().exists()) {
				this.setCdxFileSave(new File(Play.application().configuration()
						.getString("regal-api.btrix.outDir") + "/" + conf.getName()
						+ "/WEB-" + WebgatherUtils.getDomain(conf.getUrl()) + ".cdx"));
				FileUtils.copyFile(this.getCdxFileNew(), this.getCdxFileSave());
				WebgatherLogger.debug("Aktuelle CDX-Datei abgelegt in: "
						+ this.getCdxFileSave().getAbsolutePath());
			}

		} catch (Exception e) {
			WebgatherLogger.warn(e.toString());
			WebgatherLogger.warn("CDX file could not be copied to main directory! "
					+ this.getCdxFileSave().getAbsolutePath());
		}

		try {

			/**
			 * Rufe Hauptcrawl in Browsertrix auf
			 */

			// ToDo: berücksichtige cdxFile (von evtl. vorhergehenden Crawls ==> siehe
			// den Kommentar oben.
			// ToDo: berücksichtige domains (aus hostnames.txt, vom CDN-Precrawl
			// ermittelt) ==> OK, das geschieht in der übergeordneten Klasse.
			// ToDo: berücksichtige CDN-Precrawl (.warc-Datei davon) ==> OK, Wayback
			// indexiert diesen als separate Datei.

			try {
				httpClient = HttpClientBuilder.create().build();
				request = new HttpPost(btrix_api_url + "/orgs/" + btrix_orgid
						+ "/crawlconfigs/" + this.btrixWorkflowId + "/run");
				request.addHeader("Authorization", "Bearer " + this.bearerToken);
				request.addHeader("Content-Type", "application/json");
				WebgatherLogger.debug("request=" + request.toString());
				request.addHeader("Accept", "application/json");
				response = httpClient.execute(request);
				String responseJson = getResponseJson();
				WebgatherLogger.debug("received response: " + responseJson);
				// JSON ausparsen
				JSONObject responseJsonObject = new JSONObject(responseJson);
				this.started = responseJsonObject.getString("started");
				WebgatherLogger.debug("Crawl zu Workflow " + this.btrixWorkflowId
						+ " gestartet: " + started);
			} catch (Exception e) {
				msg = "Browsertrix Crawl für Workflow " + btrixWorkflowId + ", PID "
						+ node.getPid() + " kann nicht gestartet werden!";
				WebgatherLogger.error(msg, e.getMessage());
				throw new RuntimeException(e);
			} finally {
				try {
					httpClient.close();
					response.close();
				} catch (Exception e) {
					WebgatherLogger.warn("httpClient kann nicht geschlossen werden.",
							e.toString());
				}
			}

		} catch (Exception e) {
			WebgatherLogger.error(e.toString());
			throw new RuntimeException("Browsertrix crawl not successfully started!",
					e);
		}
	}

	private String getResponseJson() {
		try {
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				String responseJson = EntityUtils.toString(response.getEntity());
				return responseJson;
			}
			String errorBody = EntityUtils.toString(response.getEntity());
			throw new RuntimeException("Status-Code von " + request + " : "
					+ statusCode + ". Fehler-Body: " + errorBody);
		} catch (Exception e) {
			WebgatherLogger.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}

}
