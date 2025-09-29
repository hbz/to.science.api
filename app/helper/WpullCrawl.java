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

import models.CrawlerModel;
import models.Gatherconf;
import models.Gatherconf.AgentIdSelection;
import models.Gatherconf.RobotsPolicy;
import models.Gatherconf.QuotaUnitSelection;
import models.Globals;
import models.Node;
import play.Play;

import java.io.*;
import java.lang.ProcessBuilder;

import java.util.ArrayList;

import java.util.Hashtable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * a class to implement a wpull crawl
 * 
 * @author Ingolf Kuss
 *
 */
public class WpullCrawl extends CrawlerModel {

	/**
	 * Die Schreibzugriffe von wpull (Downloads) erfolgen in das Verzeichnis
	 * jobDir hinein. jobDir ist das Arbeitsverzeichnis von wpull. jobDir sollte
	 * ein lokales Verzeichnis sein.
	 */
	final static String jobDir =
			Play.application().configuration().getString("regal-api.wpull.jobDir");
	final static String tempJobDir = Play.application().configuration()
			.getString("regal-api.wpull.tempJobDir");
	/**
	 * Im Verzeichnis outDir liegen die fertigen Crawls. Das ist das
	 * Output-Verzeichnis von wpull. Von hier aus werden die Crawls entweder
	 * direkt von Wayback indexiert oder vorher noch weitergehend bearbeitet, z.B.
	 * getestet, ob sie erfolgreich waren.
	 */
	final static String outDir =
			Play.application().configuration().getString("regal-api.wpull.outDir");
	final static String crawler =
			Play.application().configuration().getString("regal-api.wpull.crawler");

	/**
	 * Konstruktor zu WpullCrawl
	 * 
	 * @param node der Knoten der Website, zu der ein neuer Crawl gestartet werden
	 *          soll.
	 * @param conf the crawler configuration for the website
	 */
	public WpullCrawl(Node node, Gatherconf conf) {
		super(node, conf);
		try {
			/*
			 * Die URI localpath wird von Fedora benötigt, um ein Objekt anlegen zu
			 * können. Ohne "localpath" wird im Frontend kein Link zur Wayback
			 * erzeugt.
			 */
			this.localpath = Globals.heritrixData + "/wpull-data" + "/"
					+ conf.getName() + "/" + datetime + "/" + warcFilename + ".warc.gz";
		} catch (Exception e) {
			WebgatherLogger.error("Ungültige URL :" + conf.getUrl() + " !");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Erzeugt einen neuen Wpull-Crawler-Job
	 */
	public void createJob() {
		super.runCrawl();
	}

	/**
	 * Ruft den CDN-Gatherer für diese Website auf, anschließend wpull für den
	 * Hauptcrawl
	 */
	@Override
	public void startJob() {
		super.startJob();
		try {
			// Bereite Kommando für den Hauptcrawl vor
			String executeCommand = buildExecCommand();
			ProcessBuilder pb = new ProcessBuilder();
			assert crawlDir.isDirectory();
			pb.directory(crawlDir);
			File log = new File(crawlDir.toString() + "/cdncrawl.log");
			log.createNewFile();
			pb.redirectErrorStream(true);
			pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
			WpullThread wpullThread = new WpullThread(pb, 1);
			wpullThread.setNode(node);
			wpullThread.setConf(conf);
			wpullThread.setCrawlDir(crawlDir);
			wpullThread.setOutDir(resultDir);
			wpullThread.setWarcFilename(warcFilename);
			wpullThread.setHost(host);
			wpullThread.setLocalPath(localpath);
			wpullThread.setExecuteCommand(executeCommand);
			wpullThread.setLogFileCDN(log);
			wpullThread.start();
			exitState = wpullThread.getExitState();

		} catch (Exception e) {
			WebgatherLogger.error(e.toString());
			throw new RuntimeException("wpull crawl not successfully started!", e);
		}
	}

	/**
	 * Builds a shell executable command which starts a wpull crawl
	 * 
	 * For wpull parameters in use see:
	 * http://wpull.readthedocs.io/en/master/options.html If marked as mandatory,
	 * parameter is needed for running smoothly in edoweb context. So only remove
	 * them if reasonable.
	 * 
	 * @return the ExecCommand for wpull
	 */
	private String buildExecCommand() {
		StringBuilder sb = new StringBuilder();
		sb.append(crawler + " " + urlAscii);

		if (conf.getCookie() != null && !conf.getCookie().isEmpty()) {
			sb.append(
					" --header=Cookie:%20" + conf.getCookie().replaceAll(" ", "%20"));
		}

		sb.append(" --recursive");
		ArrayList<String> urlsExcluded = conf.getUrlsExcluded();
		if (urlsExcluded.size() > 0) {
			sb.append(" --reject-regex=.*" + urlsExcluded.get(0));
			for (int i = 1; i < urlsExcluded.size(); i++) {
				sb.append("|" + urlsExcluded.get(i));
			}
			sb.append(".*");
		}

		int level = conf.getDeepness();
		if (level > 0) {
			sb.append(" --level=" + Integer.toString(level)); // number of recursions
		}

		long maxByte = conf.getMaxCrawlSize();
		if (maxByte > 0) {
			QuotaUnitSelection qFactor = conf.getQuotaUnitSelection();
			Hashtable<QuotaUnitSelection, Integer> sizeFactor = new Hashtable<>();
			sizeFactor.put(QuotaUnitSelection.KB, 1024);
			sizeFactor.put(QuotaUnitSelection.MB, 1048576);
			sizeFactor.put(QuotaUnitSelection.GB, 1073741824);

			long size = maxByte * sizeFactor.get(qFactor).longValue();
			sb.append(" --quota=" + Long.toString(size));
		}

		int waitSec = conf.getWaitSecBtRequests();
		if (waitSec != 0) {
			sb.append(" --wait=" + Integer.toString(waitSec)); // number of second
																													// wpull waits between
																													// requests
		} else {
			boolean random = conf.isRandomWait();
			if (random == true) {
				sb.append(" --random-wait"); // randomize wait times
			}
		}

		int tries = conf.getTries();
		if (tries != 0) {
			sb.append(" --tries=" + Integer.toString(tries)); // number of requests
																												// wpull performs on
																												// transient errors
		}

		int waitRetry = conf.getWaitRetry();
		if (waitRetry != 0) {
			sb.append(" --waitretry=" + Integer.toString(waitRetry)); // wait between
																																// re-tries
		}

		// select agent-string for http-request
		AgentIdSelection agentId = conf.getAgentIdSelection();
		sb.append(" --user-agent=" + Gatherconf.agentTable.get(agentId));

		sb.append(" --link-extractors=javascript,html,css");
		sb.append(" --warc-file=" + warcFilename);
		if (conf.getRobotsPolicy().equals(RobotsPolicy.classic)
				|| conf.getRobotsPolicy().equals(RobotsPolicy.ignore)) {
			sb.append(" --no-robots");
		}
		/* Benutze Internet-Protokoll Version 4 */
		sb.append(" -4");
		// sb.append(" --http-proxy=externer-web-proxy.hbz-nrw.de:3128");
		// kommt "Misconfigured redirect"
		sb.append(" --escaped-fragment --strip-session-id");
		sb.append(" --no-host-directories --page-requisites");
		sb.append(" --database=" + warcFilename + ".db");
		sb.append(" --no-check-certificate");
		sb.append(" --no-directories"); // mandatory to prevent runtime errors
		sb.append(" --delete-after"); // mandatory for reducing required disc space
		sb.append(" --convert-links"); // mandatory to rewrite relative urls
		/**
		 * ohne diesen Parameter wird www.facebook.com, www.youtoube.com uvm.
		 * eingesammelt (aktiviert 12.05.2020)
		 */
		sb.append(" --no-strong-redirects");
		/**
		 * um CDN-Crawls und Haupt-Crawl im gleichen Archiv zu bündeln
		 */
		sb.append(" --warc-append");
		// auskommentiert 27.08.2020 für EDOZWO-1026
		// sb.append(" --warc-tempdir=" + tempJobDir)
		sb.append(" --warc-move=" + resultDir);
		sb.append(" --warc-cdx");
		if (this.cdxFileNew != null && this.cdxFileNew.exists()) {
			sb.append(" --warc-dedup=" + warcFilename + ".cdx");
		}
		play.Logger.debug("Built Crawl command: " + sb.toString());
		return sb.toString();
	}

	/**
	 * Prüfung, ob ein Crawl zu einer gegebenen URL aktuell läuft
	 * 
	 * @param node der Knoten zu der Webpage mit der URL
	 * @return boolean Crawl läuft
	 */
	public static boolean isWpullCrawlRunning(Node node) {
		BufferedReader buf = null;
		String cmd = "ps -eaf";
		String regExp1 =
				Play.application().configuration().getString("regal-api.wpull.crawler");
		Pattern pattern1 = Pattern.compile(regExp1);
		Matcher matcher1 = null;
		try {
			String urlAscii = WebgatherUtils
					.convertUnicodeURLToAscii(Gatherconf.create(node.getConf()).getUrl());
			String regExp2 = urlAscii;
			// Maskiere Sonderzeichen des Regulären Ausdrucks mit Pattern.quote
			Pattern pattern2 = Pattern.compile(Pattern.quote(regExp2));
			Matcher matcher2 = null;
			WebgatherLogger.debug("Setze Systemkommando ab: " + cmd);
			WebgatherLogger.debug("Suche nach wpull-Aufrufen mit url " + regExp2);
			String line;
			Process proc = Runtime.getRuntime().exec(cmd);
			buf = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = buf.readLine()) != null) {
				// WebgatherLogger.debug("found line: " + line);
				matcher1 = pattern1.matcher(line);
				if (matcher1.find()) {
					// WebgatherLogger.debug("wpull3 found in line");
					matcher2 = pattern2.matcher(line);
					if (matcher2.find()) {
						WebgatherLogger
								.debug("Found wpull Crawl process for this url=" + line);
						return true;
					}
				}
			}
		} catch (Exception e) {
			WebgatherLogger.warn("Fehler beim Aufruf des Systenkommandos: " + cmd,
					e.toString());
			throw new RuntimeException(
					"Crawl Job Zustand kann nicht bestimmt werden !", e);
		} finally {
			try {
				if (buf != null) {
					buf.close();
				}
			} catch (IOException e) {
				WebgatherLogger.warn("Read Buffer cannot be closed!");
			}
		}
		return false;
	}

}
