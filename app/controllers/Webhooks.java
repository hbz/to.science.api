/*
 * Copyright 2026 hbz NRW (http://www.hbz-nrw.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package controllers;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;

import actions.Create;
import actions.Read;
import authenticate.BasicAuth;
import helper.BtrixCrawlIngestArchive;
import helper.BtrixWebclient;
import helper.HttpArchiveException;
import helper.WpullCrawlIngestArchive;
import models.Gatherconf;
import models.Message;
import models.Node;
import play.libs.F.Promise;
import play.mvc.Result;

/**
 * In dieser Klasse werden API-Calls (Endpoints) definiert, die von externen
 * Anwendungen aufgerufen werden, sogenannte "Webhooks". Siehe die Definitionen
 * der Endpoints in der "routes"-Datei, to.science.api/conf/routes. API is
 * documented using Swagger. See: https://github.com/wordnik/swagger-ui
 * 
 * Zur eigentlichen Verarbeitung der Calls wird an andere Klassen übergeben.
 * 
 * @author Ingolf Kuss, kuss@hbz-nrw.de
 * @date 2026-04-16
 */
@BasicAuth
@Api(value = "/webhooks", description = "Die Webhooks-Endpoints verarbeiten Anfragen (POSTs) von externen Anwendungen.")
@SuppressWarnings("javadoc")
public class Webhooks extends MyController {

	@ApiOperation(produces = "application/json", nickname = "btrixCrawlFinished", value = "btrixCrawlFinished", notes = "Implementing Browsertrix Webhook \"Crawl Finished\".", response = Message.class, httpMethod = "POST")
	@ApiImplicitParams({
			@ApiImplicitParam(value = "Metadata", required = true, dataType = "string", paramType = "body") })
	/**
	 * Dieser Endpoint verarbeitet eine vom Browertrix bereit gestellte neue
	 * Archivdatei (der Endung WACZ)
	 * 
	 * @author I. Kuss
	 * @date 2026-04-22
	 * @return
	 */
	public static Promise<Result> btrixCrawlFinished() {

		return Promise.promise(() -> {
			JsonNode body = request().body().asJson();
			play.Logger.debug("btrix Crawl Finished sent body: " + body);
			String filename =
					body.findValue("filename").toString().replaceAll("^\"|\"$", "");
			play.Logger.debug("filename found: " + filename);
			/**
			 * Hole cid_stub aus dem Dateinamen. cid_stub = die ersten 12 Zeichen der
			 * Crawler Worfklow Id (cid).
			 */
			File waczFile = new File(filename);
			String regExp = "^([0-9]+)-([0-9a-f]{8})-([0-9a-f]{3})-([0-9]+)\\.wacz$";
			Pattern pattern = Pattern.compile(regExp);
			Matcher matcher = pattern.matcher(waczFile.getName());
			if (!matcher.find()) {
				RuntimeException re =
						new RuntimeException("cid_stub can not be infered from filename "
								+ waczFile.getName() + " !");
				play.Logger.error(re.toString());
				throw re;
			}
			String cid_stub = matcher.group(2) + "-" + matcher.group(3);
			play.Logger.debug("Found cid_stub in filename: " + cid_stub);

			/**
			 * Hole Workflow Config über Get Crawl Configs mit Abfrageparameter
			 * description = cid_stub. Dasselbe macht das Shell-Skript
			 * ks.btrix_get_crawl_configs.sh.
			 */
			BtrixWebclient btrixWebclient = new BtrixWebclient();
			JSONObject crawlConfigs =
					btrixWebclient.getCrawlConfigs("description=" + cid_stub);
			JSONObject crawlConfig =
					(JSONObject) crawlConfigs.getJSONArray("items").get(0);
			play.Logger.debug(
					"Found Crawl Config with name: " + crawlConfig.getString("name"));
			play.Logger.debug("Crawl Config cid = " + crawlConfig.getString("id"));
			String lastCrawlId = crawlConfig.getString("lastCrawlId");
			play.Logger.debug("Last Crawl Id = " + lastCrawlId);
			String toscienceId = (String) crawlConfig.getJSONArray("tags").get(0);
			play.Logger.debug("Crawl Config is for toscience ID: " + toscienceId);
			play.Logger.debug(
					"lastCrawlStartTime: " + crawlConfig.getString("lastCrawlStartTime"));
			// Hole Zeitstempel aus Dateinamen
			DateTimeFormatter formatter =
					DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
			LocalDateTime dateTime =
					LocalDateTime.parse(waczFile.getName().substring(0, 14), formatter);
			ZoneId zoneId = ZoneId.of("UTC");
			ZonedDateTime dateTimeUtc = ZonedDateTime.of(dateTime, zoneId);
			ZonedDateTime dateTimeLocal =
					dateTimeUtc.withZoneSameInstant(ZoneId.systemDefault());
			String datetime = dateTimeLocal.format(formatter);
			play.Logger.debug("datetime: " + datetime);

			btrixWebclient.setResultDir(new File(
					btrixWebclient.getOutDir() + "/" + toscienceId + "/" + datetime));
			if (!btrixWebclient.getResultDir().exists()) {
				// create output directory for this Browsertrix Crawl
				play.Logger.debug("Creating Output Directory "
						+ btrixWebclient.getResultDir().toString());
				btrixWebclient.getResultDir().mkdirs();
			}

			/*
			 * Ab hier wird die Verarbeitung an einen Thread übergeben
			 * (Nebenläufigkeit); lang dauernde Dateioperationen möglich! Nach dem
			 * Verschieben der Archivdatei in den Ergebnisbereich (btrix-data) wird
			 * automatisch ein Webschnitt angelegt.
			 */
			BtrixCrawlIngestArchive moveArchive = new BtrixCrawlIngestArchive();
			moveArchive.setCrawler(Gatherconf.CrawlerSelection.btrix);
			moveArchive.setCrawlerModel(btrixWebclient);
			moveArchive.setToscienceId(toscienceId);
			moveArchive.setFilename(filename);
			moveArchive.setLastCrawlId(lastCrawlId);
			moveArchive.setDatetime(datetime);
			moveArchive.start();

			return ok();
		});
	}

	/**
	 * Dieser Endpoint verarbeitet eine von wpull bereit gestellte neu Archivdatei
	 * (der Endung .warc.gz)
	 * 
	 * @author I. Kuss
	 * @date 2026-05-11
	 * @return
	 */
	public static Promise<Result> wpullCrawlFinished() {

		return Promise.promise(() -> {

			/**
			 * Entgegennahme der POST-Parameter
			 */
			JsonNode body = request().body().asJson();
			play.Logger.debug("wpull Crawl Finished sent body: " + body);
			String pid = body.findValue("pid").toString().replaceAll("^\"|\"$", "");
			play.Logger.debug("webpage pid: " + pid);
			String crawldir =
					body.findValue("crawldir").toString().replaceAll("^\"|\"$", "");
			play.Logger.debug("crawldir: " + crawldir);
			String warcFilenameBase = body.findValue("warcFilenameBase").toString()
					.replaceAll("^\"|\"$", "");
			play.Logger.debug("warcFilenameBase: " + warcFilenameBase);

			/*
			 * Ab hier wird die Verarbeitung an einen Thread übergeben
			 * (Nebenläufigkeit); lang dauernde Dateioperationen möglich! Nach dem
			 * Verschieben der Archivdatei in den Ergebnisbereich (wpull-data) wird
			 * automatisch ein Webschnitt angelegt. Anschließend wird das crawldir im
			 * "finished"-Verzeichnis gelöscht.
			 */
			WpullCrawlIngestArchive moveArchive = new WpullCrawlIngestArchive();
			moveArchive.setCrawler(Gatherconf.CrawlerSelection.wpull);
			moveArchive.setToscienceId(pid);
			moveArchive.setFilename(warcFilenameBase + ".warc.gz");
			moveArchive.setFilenameBase(warcFilenameBase);
			moveArchive.setDatetime(crawldir);
			moveArchive.start();

			return ok();
		});
	}

	/**
	 * Dieser Endpoint legt für eine vom LAV (Landesarchiv NRW) bereitgestellte
	 * Archivdatei einen Webschnitt an (Endung .warc.gz). Es kann auch eine
	 * Archivdatei stellvertretend für mehrere Archivdateien stehen, für die aber
	 * nur gemeinsam ein Webschnitt angelegt wird (für alle Archivdateien in einem
	 * vom LAV gelieferten Verzeichnis).
	 * 
	 * @author I. Kuss
	 * @date 2026-07-07
	 * @return
	 */
	public static Promise<Result> externalCrawlIngest() {

		return Promise.promise(() -> {

			/**
			 * Entgegennahme der POST-Parameter
			 */
			JsonNode body = request().body().asJson();
			play.Logger.debug("LAV Crawl sent body: " + body);
			String pid = body.findValue("pid").toString().replaceAll("^\"|\"$", "");
			play.Logger.debug("webpage pid: " + pid);
			String collection =
					body.findValue("collection").toString().replaceAll("^\"|\"$", "");
			play.Logger.debug("collection: " + collection);
			String crawldir =
					body.findValue("crawldir").toString().replaceAll("^\"|\"$", "");
			play.Logger.debug("crawldir: " + crawldir);
			String warcFilenameBase = body.findValue("warcFilenameBase").toString()
					.replaceAll("^\"|\"$", "");
			play.Logger.debug("warcFilenameBase: " + warcFilenameBase);

			play.Logger.debug("Beginn erzeuge WebpageVersion für PID " + pid
					+ ",Collection: " + collection + ", Zeitstempel " + crawldir);
			String versionPid = null;
			Node result = null;
			try {
				Node n = new Read().readNode(pid);
				String lastCrawlId = "";
				result = new Create().postWebpageVersion(n, versionPid, lastCrawlId,
						collection, crawldir,
						new File(warcFilenameBase + ".warc.gz").getName());
				play.Logger.info("WebpageVersion für " + pid + " wurde angelegt.");
			} catch (Exception e) {
				play.Logger.error(
						"WebpageVersion für " + pid + " konnte nicht angelegt werden!");
				play.Logger.error(e.getMessage(), e);
				// throw new HttpArchiveException(500, e);
			}

			return getJsonResult(result);
		});
	}

}
