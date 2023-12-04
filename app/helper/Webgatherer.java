/*
 * Copyright 2014 hbz NRW (http://www.hbz-nrw.de/)
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
package helper;

import static archive.fedora.FedoraVocabulary.HAS_PART;
import play.Logger;
import play.Play;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import models.Gatherconf;
import models.Globals;
import models.Link;
import models.Node;
import actions.Modify;
import actions.Create.WebgathererTooBusyException;
import helper.WpullCrawl.CrawlControllerState;
import actions.Read;

/**
 * @author Jan Schnasse
 *
 */
public class Webgatherer implements Runnable {

	final static String heritrixJobDir =
			Play.application().configuration().getString("regal-api.heritrix.jobDir");
	final static String wpullJobDir =
			Play.application().configuration().getString("regal-api.wpull.jobDir");
	private static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");
	private int precount = 0; // die Anzahl bearbeiteter Webpages
	private int count = 0; // die Anzahl tatsächlich gestarteter Crawls

	@Override
	public void run() {
		// get all webpages

		WebgatherLogger.info("List 50000 resources of type webpage from namespace "
				+ Globals.defaultNamespace + ".");
		play.Logger.info("List 50000 resources of type webpage from namespace "
				+ Globals.defaultNamespace + ".");
		List<Node> webpages =
				new Read().listRepo("webpage", Globals.defaultNamespace, 0, 50000);
		WebgatherLogger.info("Found " + webpages.size() + " webpages.");
		int limit = play.Play.application().configuration()
				.getInt("regal-api.heritrix.crawlsPerNight");

		Node webpagesArray[] = webpages.toArray(new Node[0]);
		WebgatherLogger.debug("Found: " + webpagesArray.length + " webpages.");
		String lastlyCrawledWebpageId =
				helper.WebgatherUtils.readLastlyCrawledWebpageId();
		WebgatherLogger.info("Zuletzt gecrawlte Website: " + lastlyCrawledWebpageId);
		/* fortlaufender Index für Webpages, beginnend bei Null; kann "rollieren" */
		int i = 0;
		int firstLoop = 1;
		while (i < webpagesArray.length) {
			if ((webpagesArray[i].getPid().compareTo(lastlyCrawledWebpageId) <= 0)
					&& (firstLoop == 1)) {
				WebgatherLogger
						.debug(webpagesArray[i].getPid() + " wird übersprungen.");
				i++;
				if (i >= webpagesArray.length) {
					i = 0;
					firstLoop = 0;
				}
				continue;
			}
			precount++;
			bearbWebpage(webpagesArray[i]);
			if (count >= limit)
				break;
			i++;
			if (i >= webpagesArray.length) {
				i = 0; // es geht wieder von vorne los
				firstLoop = 0;
			}
			if (precount >= webpagesArray.length)
				break;
		}
		// er hat webpagesArray.length Webseiten bearbeitet, oder limit Crawls
		// gestartet
		// Ende des Nachtlaufes
		WebgatherLogger
				.info("Ich habe " + precount + " Webpages bearbeitet und dabei " + count
						+ " neue Crawls gestartet.");
		WebgatherLogger.info("ENDE des Webgather-Nachtlaufes.");
	}

	/**
	 * Diese Methode bearbeitet eine einzelne Webpage - prüft, ob jetzt neu
	 * eingesammelt werden muss - beginnt ggfs. einen neuen Sammelvorgang
	 * ("Crawl")
	 */
	private void bearbWebpage(Node node) {
		Gatherconf conf = null;
		Node n = node;
		try {
			// Merke toscience-ID der Webpage in einer Datei
			String fileName =
					helper.WebgatherUtils.getFileNameLastlyCrawledWebpageId();
			FileWriter fw = new FileWriter(fileName);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(n.getPid());
			// bw.newLine();
			bw.close();

			WebgatherLogger.info("Precount: " + precount);
			WebgatherLogger.info("PID: " + n.getPid());
			if (n.getState().equals("D")) {
				WebgatherLogger.info("Objekt " + n.getPid() + " wurde gelöscht.");
				return;
			}
			if (n.getConf() == null) {
				WebgatherLogger.info(
						"Webpage " + n.getPid() + " hat noch keine Crawler-Konfigration.");
				return;
			}
			WebgatherLogger
					.info("Config: " + n.getConf() + " is being created in Gatherconf.");
			conf = Gatherconf.create(n.getConf());
			if (!conf.isActive()) {
				WebgatherLogger.info("Site " + n.getPid() + " ist deaktiviert.");
				return;
			}
			WebgatherLogger.info("Test if " + n.getPid() + " is scheduled.");
			// find open jobs
			if (isOutstanding(n, conf)) {
				WebgatherLogger.info(
						"Die Website " + n.getPid() + " soll jetzt eingesammelt werden.");
				if (conf.hasUrlMoved(n)) {
					if (conf.getUrlNew() == null) {
						WebgatherLogger
								.info("De Sick " + n.getPid() + " is unbekannt vertrocke !");
					} else {
						WebgatherLogger.info("De Sick " + n.getPid() + " is umjetrocke noh "
								+ conf.getUrlNew() + " .");
					}
					WebgatherUtils.sendInvalidUrlEmail(n, conf);
				} else {
					WebgatherLogger
							.info("HTTP Response Code = " + conf.getHttpResponseCode());
					WebgatherLogger.info("Create new version for: " + n.getPid() + ".");
					/* new Create().createWebpageVersion(n); */
					new WebgatherUtils().startCrawl(n);
					count++; // count erst hier, so dass fehlgeschlagene Launches nicht
										// mitgezählt werden
					WebgatherLogger.info("Count is now = " + count);
				}
			}

		} catch (WebgathererTooBusyException e) {
			WebgatherLogger.error(
					"Webgathering for " + n.getPid() + " stopped! Heritrix is too busy.");
		} catch (MalformedURLException | URISyntaxException e) {
			setUnknownHost(n, conf);
			WebgatherLogger.error("Fehlgeformte URL bei " + n.getPid() + " !");
		} catch (UnknownHostException e) {
			setUnknownHost(n, conf);
			WebgatherLogger
					.error("Ungültige URL. Neue URL unbekannt für " + n.getPid() + " !");
		} catch (Exception e) {
			WebgatherLogger.error("Couldn't create webpage version for " + n.getPid()
					+ ". Cause: " + e.getLocalizedMessage(), e);
		}

	} // ENDE bearbWebpage()

	private static void setUnknownHost(Node node, Gatherconf conf) {
		if (conf != null && conf.getInvalidUrl() == false) {
			conf.setInvalidUrl(true);
			conf.setUrlNew((String) null);
			String msg = new Modify().updateConf(node, conf.toString());
			WebgatherLogger.info(msg);
			WebgatherLogger
					.info("URL wurde auf ungültig gesetzt. Neue URL unbekannt.");
			WebgatherUtils.sendInvalidUrlEmail(node, conf);
		}
	}

	/**
	 * Diese Methode ermittelt das Datum, an dem zuletzt erfolgreich ein
	 * Webschnitt an die Webpage angehängt wurde.
	 * 
	 * @param n der Knoten für die Webpage
	 * @return Datum+Zeit (Typ Date) des letzten erfolgreich beendeten Webcrwals =
	 *         Änderungsdatum des neuesten Webschnitts
	 * @throws Exception Ausnahme beim Lesen
	 */
	public static Date getLastLaunch(Node n) throws Exception {
		WebgatherLogger
				.debug("BEGIN getLastLaunch for node with pid: " + n.getPid());
		Node lastModifiedChild =
				new Read().getLastModifiedChildOrNull(n, "version");
		if (lastModifiedChild == null)
			return null;
		WebgatherLogger
				.debug("lastModifiedChild has pid: " + lastModifiedChild.getPid());
		WebgatherLogger.debug("lastModifiedChild was last modified on: "
				+ lastModifiedChild.getLastModified().toString());
		return lastModifiedChild.getLastModified();
	}

	/**
	 * @param n a webpage
	 * @return nextLaunch
	 * @throws Exception can be IOException or Json related Exceptions
	 */
	public static Date nextLaunch(Node n) throws Exception {
		Date lastHarvest =
				new Read().getLastModifiedChild(n, (Node) null).getLastModified();
		Gatherconf conf = Gatherconf.create(n.getConf());
		if (lastHarvest == null) {
			return conf.getStartDate();
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(lastHarvest);
		Date nextTimeHarvest = getSchedule(cal, conf);
		return nextTimeHarvest;
	}

	private static boolean isOutstanding(Node n, Gatherconf conf) {
		WebgatherLogger.debug("BEGIN isOutstanding for pid: " + n.getPid());
		if (new Date().before(conf.getStartDate()))
			return false;
		// Falls ein Crawl noch läuft, gib nie `true` zurück !!
		CrawlControllerState ccs = WpullCrawl.getCrawlControllerState(n);
		if (ccs.equals(CrawlControllerState.RUNNING)) {
			return false;
		}
		WebgatherLogger
				.debug("Nicht vor Beginndatum und Crawl läuft auch noch nicht.");
		List<Link> parts = n.getRelatives(archive.fedora.FedoraVocabulary.HAS_PART);
		if (parts == null || parts.isEmpty()) {
			WebgatherLogger.debug(
					"Website hat noch keine \"Teile\" und soll jetzt gesammelt werden.");
			return true;
		}
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdf_hr = new SimpleDateFormat("yyyy-MM-dd");
			Date latestDate = getLastLaunch(n);
			if (latestDate == null) {
				return true;
			}
			WebgatherLogger
					.debug("Datum des letzten Einsammelns: " + latestDate.toString());
			Calendar latestCalendar = Calendar.getInstance();
			WebgatherLogger.debug("Fetched Calendar instance");
			latestCalendar.setTime(latestDate);
			WebgatherLogger.debug("Set Date and time in calendar instance");
			if (conf.getInterval().equals(models.Gatherconf.Interval.once)) {
				WebgatherLogger.info(n.getPid()
						+ " will be gathered only once. It has already been gathered on "
						+ sdf_hr.format(latestDate));
				return false;
			}
			WebgatherLogger.info(n.getPid() + " has been last gathered on "
					+ sdf_hr.format(latestDate));
			WebgatherLogger
					.info(n.getPid() + " shall be launched " + conf.getInterval());
			Date nextDateHarvest = getSchedule(latestCalendar, conf);
			WebgatherLogger.info(n.getPid() + " should be next gathered on "
					+ sdf_hr.format(nextDateHarvest));
			Date today = new Date();
			if (sdf.format(nextDateHarvest).compareTo(sdf.format(today)) > 0) {
				WebgatherLogger.info(
						n.getPid() + " " + n.getConf() + " will be launched next time at "
								+ new SimpleDateFormat("yyyy-MM-dd").format(nextDateHarvest));
				return false;
			}
			WebgatherLogger.info(n.getPid() + " will be launched now!");
			return true;
		} catch (ParseException e) {
			WebgatherLogger.error("Cannot parse date string.", e);
			return false;
		} catch (Exception e) {
			WebgatherLogger.error("Kann letztes Crawl-Datum nicht bestimmen.", e);
			return false;
		}
	}

	private static Date getSchedule(Calendar cal, Gatherconf conf) {
		switch (conf.getInterval()) {
		case daily:
			cal.add(Calendar.DATE, 1);
			break;
		case weekly:
			cal.add(Calendar.DATE, 7);
			break;
		case monthly:
			cal.add(Calendar.MONTH, 1);
			break;
		case quarterly:
			cal.add(Calendar.MONTH, 3);
			break;
		case halfYearly:
			cal.add(Calendar.MONTH, 6);
			break;
		case annually:
			cal.add(Calendar.YEAR, 1);
			break;
		case once:
			break;
		}
		return cal.getTime();
	}

	/**
	 * @param name the jobs name e.g. the pid
	 * @param jobDir Arbeitsverzeichnus für einen spezifischen Crawler
	 * @return the servers directory where to store the data
	 * 
	 *         Im Unterschied zu getCurrentCrawlDir wird nicht der Pfad mit dem
	 *         aktuellen Datum zurückgegeben, sondern der Pfad mit dem LETZTEN
	 *         (=neuesten) Datum - oder NULL, falls noch gar nicht gecrawlt wurde.
	 */
	public static File getLatestCrawlDir(String jobDir, String name) {
		File dir = new File(jobDir + "/" + name);
		WebgatherLogger.debug("jobDir/name=" + dir.toString());
		// gibt es das Verzeichnis überhaupt ?
		if (!dir.exists() || !dir.isDirectory()) {
			WebgatherLogger
					.info("Zu " + name + " wurden noch keine Crawls angestoßen.");
			return null;
		}
		File[] files = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File d) {
				return (d.isDirectory() && d.getName().matches("^[0-9]+"));
			}
		});
		if (files == null || files.length <= 0) {
			WebgatherLogger
					.info("Zu " + name + " wurden noch keine Crawls angestoßen.");
			return null;
		}
		WebgatherLogger
				.debug("Found crawl directories: " + java.util.Arrays.toString(files));
		Arrays.sort(files, Collections.reverseOrder());
		File latest = files[0];
		return latest;
	}

	/*
	 * @return die Anzahl bisher begonnener Sammelvorgänge (Summe über alle
	 * möglichen Crawler) = die Anzahl angelegter Versionen
	 */
	public static int getLaunchCount(Node node) {
		int launchCount = 0;
		if (!node.getContentType().equals("webpage")) {
			WebgatherLogger.warn("Knoten " + node.getName()
					+ " ist nicht vom Inhaltstyp \"webpage\" ! Anzahl begonnener Sammelvorgänge kann nicht ermittelt werden!");
			return -1;
		}
		List<Link> children = node.getRelatives(HAS_PART);
		launchCount = children.size(); // hopefully all children are versions - how
																		// to verify that ?
		WebgatherLogger.debug("Launch Count = " + launchCount);
		return launchCount;
	}

}
