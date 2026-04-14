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
package models;

import models.Gatherconf;
import models.Gatherconf.AgentIdSelection;
import models.Node;
import play.Logger;
import play.Play;

import java.io.*;
import java.nio.file.Files;

import helper.CrawlLog;
import helper.WebgatherUtils;
import helper.Webgatherer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

/**
 * A class to generally describe a Web-Crawler to be inherited by the
 * implementing Crawler Models (wpull, heritrix, browsertrix, ...)
 * 
 * @author Ingolf Kuss (hbz)
 * @date 2025-09-26
 *
 */
public class CrawlerModel {

	/* Allgemeine Variablen und Konstanten für das Webcrawling */
	@SuppressWarnings("javadoc")
	public enum CrawlControllerState {
		NEW, RUNNING, PAUSED, ABORTED, CRASHED, FINISHED
	}

	protected Node node = null;
	protected Gatherconf conf = null;
	protected String urlAscii = null;
	protected String date = null;
	protected String datetime = null;
	protected File crawlDir = null;
	protected File resultDir = null;
	protected File logAnalysesDir = null;
	protected File cdxFile = null;
	protected File cdxFileNew = null;
	protected String localpath = null;
	protected String host = null;
	protected String warcFilename = null;
	protected int CDNGathererExitState = 0;
	protected ArrayList<String> domains = null;
	protected String msg = null;
	protected int exitState = 0;

	private static String jobDir = null;
	private static String outDir = null;
	final static public String cdn =
			Play.application().configuration().getString("regal-api.cdntools.cdn");
	final static public String crawlreportsDir = Play.application()
			.configuration().getString("toscience-api.webgatherer.crawlreports");

	/**
	 * ein Logger für das Webgathering
	 */
	protected static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

	/**
	 * Die Methode, um das crawlDir auszulesen.
	 * 
	 * @return Das Verzeichnis (absolute Pfadangabe, bei verzweigten
	 *         Verzeichnissen die oberste Ebene), in das der Crawler seine
	 *         Ergebnisdateien (z.B. WARC-Archive, log-Dateien) schreibt.
	 */
	public File getCrawlDir() {
		return crawlDir;
	}

	/**
	 * Die Methode, um resultDir auszulesen
	 * 
	 * @return resultDir Das Verzeichnis (absolute Pfadangabe, oberste Ebene), in
	 *         dem die Wayback nach fertigen WARC-Dateien sucht.
	 */
	public File getResultsDir() {
		return resultDir;
	}

	/**
	 * Die Methode, um die CDX-Datei auszulesen
	 * 
	 * @return cdxFile Eine Datei, die eine Liste bereits eingesammelter URLs für
	 *         diese Website enthält. Nütlich für das inkrementelle Crawling
	 *         (WARC-Deduplikation).
	 */
	public File getCdxFile() {
		return cdxFile;
	}

	/**
	 * Die Methode, um die neue CDX-Datei auszulesen
	 * 
	 * @return cdxFileNew ist eine CDX-Datei, die der Crawler beim nächsten Crawl
	 *         neu schreibt. Als Anfangswert wird die bisherige cdx-Datei, cdxFile
	 *         (="old"), hier hinein kopiert.
	 */
	public File getCdxFileNew() {
		return cdxFileNew;
	}

	/**
	 * Die Methode, um localPath auszulesen
	 * 
	 * @return localPath is ein Parameter, den Fedora benötigt. Es ist eine URL zu
	 *         einer gecrawlten WARC-Datei.
	 */
	public String getLocalpath() {
		return localpath;
	}

	/**
	 * Die Methode, um exitState auszulesen
	 * 
	 * @return exitState ist der Return-Status des Crawlers. Es ist == 0, wenn der
	 *         Crawl erfolgreich war, sonst > 0.
	 */
	public int getExitState() {
		return exitState;
	}

	/**
	 * Konstruktor für das Crawler Modell
	 * 
	 * @param node Der Knoten der Website, zu der ein neuer Crawl gestartet werden
	 *          soll.
	 * @param conf the crawler configuration for the website
	 */
	public CrawlerModel(Node node, Gatherconf conf) {
		this.node = node;
		this.conf = conf;
		try {
			WebgatherLogger.debug("URL=" + conf.getUrl());
			this.urlAscii = WebgatherUtils.convertUnicodeURLToAscii(conf.getUrl());
			WebgatherLogger.debug("urlAscii=" + urlAscii);
			this.host = WebgatherUtils.getDomain(urlAscii);
			WebgatherLogger.debug("host=" + host);
			this.date = new SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
			this.datetime =
					date + new SimpleDateFormat("HHmmss").format(new java.util.Date());
			this.crawlDir = new File(jobDir + "/" + conf.getName() + "/" + datetime);
			this.resultDir = new File(outDir + "/" + conf.getName() + "/" + datetime);
			this.cdxFile =
					new File(outDir + "/" + conf.getName() + "/WEB-" + host + ".cdx");
			this.warcFilename = "WEB-" + host + "-" + date;
		} catch (Exception e) {
			WebgatherLogger.error("Ungültige URL :" + conf.getUrl() + " !");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Erzeugt einen neuen Crawler-Job
	 */
	public void createCrawl() {
		WebgatherLogger.debug("Create new job " + conf.getName());
		try {
			if (conf.getName() == null) {
				throw new RuntimeException("The configuration has no name !");
			}
			if (!crawlDir.exists()) {
				// create job directory
				WebgatherLogger.debug("Create job Directory " + jobDir + "/"
						+ conf.getName() + "/" + datetime);
				crawlDir.mkdirs();
			}
			if (!resultDir.exists()) {
				// create output directory
				WebgatherLogger.debug("Create Output Directory " + outDir + "/"
						+ conf.getName() + "/" + datetime);
				resultDir.mkdirs();
			}
			/**
			 * Dieser Codeblock wird für das inkrementelle Crawling benötigt. Es wird
			 * geschaut, ob eine CDX-Datei für diese Webpage existiert. Eine CDX-Datei
			 * enthält eine Liste bereits gesammelter URLs für diese Webpage. Falls
			 * eine CDX-Datei existiert, wird sie in das Arbeitsverzeichnis jobDir
			 * kopiert und entsprechend so umbenannt, dass der neue Crawl sie weiter
			 * schreiben wird.
			 * 
			 * @author Ingolf Kuss
			 * @date 2025-03-12
			 */
			if (cdxFile.exists()) {
				WebgatherLogger
						.debug("CDX-Datei gefunden: " + cdxFile.getAbsolutePath());
				this.cdxFileNew = new File(
						this.crawlDir.getAbsolutePath() + "/" + this.warcFilename + ".cdx");
				FileUtils.copyFile(cdxFile, cdxFileNew);
				WebgatherLogger
						.debug("Neue CDX-Datei angelegt: " + cdxFileNew.getAbsolutePath());
			}
		} catch (Exception e) {
			msg = "Cannot create jobDir in " + jobDir + "/" + conf.getName();
			msg.concat("Cannot create outDir in " + outDir + "/" + conf.getName());
			WebgatherLogger.error(msg);
			throw new RuntimeException(msg);
		}
	}

	/**
	 * Ruft den CDN-Gatherer für diese Website auf.
	 */
	public void startCrawl() {
		WebgatherLogger.info("Bereite Aufruf des CDN-Gatherer vor. warcFilename="
				+ this.warcFilename + ".");
		try {
			// 1. Vorbereiten des CDN-Precrawls
			String waitParam = null;
			int waitSec = conf.getWaitSecBtRequests();
			if (waitSec != 0) {
				// number of second wpull will wait between two requests
				waitParam = "wait=" + Integer.toString(waitSec);
			} else {
				boolean random = conf.isRandomWait();
				if (random == true) {
					// randomize wait times
					waitParam = "random-wait";
				} else {
					// don't wait
					waitParam = "wait=0";
				}
			}
			String executeCommand =
					new String(cdn + " " + this.urlAscii + " " + this.warcFilename);
			AgentIdSelection agentId = conf.getAgentIdSelection();
			executeCommand =
					executeCommand.concat(" " + Gatherconf.agentTable.get(agentId));
			executeCommand = executeCommand.concat(" Cookie:");
			if (conf.getCookie() != null && !conf.getCookie().isEmpty()) {
				executeCommand =
						executeCommand.concat(conf.getCookie().replaceAll(" ", "%20"));
			}
			executeCommand = executeCommand.concat(" " + waitParam);
			if (cdxFileNew != null) {
				executeCommand = executeCommand.concat(" " + cdxFileNew.getName());
			}
			String[] execArr = executeCommand.split(" ");
			executeCommand = executeCommand.replaceAll("%20", " ");
			WebgatherLogger.info("Executing command " + executeCommand);
			WebgatherLogger
					.info("Logfile = " + crawlDir.toString() + "/cdncrawl.log");
			ProcessBuilder pb = new ProcessBuilder(execArr);
			assert crawlDir.isDirectory();
			pb.directory(crawlDir);
			File log = new File(crawlDir.toString() + "/cdncrawl.log");
			log.createNewFile();
			pb.redirectErrorStream(true);
			pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
			// 2. Ausführung des CDN-Precrawls (1. und 2. Schritt)
			Process proc = pb.start();
			assert pb.redirectInput() == ProcessBuilder.Redirect.PIPE;
			assert pb.redirectOutput().file() == log;
			assert proc.getInputStream().read() == -1;
			CDNGathererExitState = proc.waitFor();
			/**
			 * Exit-Status: 0 = Crawl erfolgreich beendet
			 */
			WebgatherLogger.info("CDN-Crawl für " + conf.getName()
					+ " wurde beendet mit Exit-Status " + CDNGathererExitState);

			// 3. Auslesen der vom cdnparse angelegten Datei hostnames.txt
			// cdnparse ist der 1. Schritt des CDN-Precrawls und ein Python-Programm
			domains = conf.getDomains();
			// Add hostnames from cdn precrawl textfile
			List<String> hostnames = new ArrayList<>();
			WebgatherLogger.info("Adding hostnames from file " + crawlDir.toString()
					+ "/hostnames.txt");
			try {
				hostnames = Files.readAllLines(
						new File(crawlDir.toString() + "/hostnames.txt").toPath());
			} catch (IOException e) {
				WebgatherLogger.warn("File hostnames.txt can not be opened!",
						e.toString());
			}
			domains.addAll(hostnames);
		} catch (Exception e) {
			WebgatherLogger.error(e.toString());
			throw new RuntimeException("cdn crawl not successfully started!", e);
		}
	} // Ende startCrawl()

	/**
	 * Suche neuestes Crawler-Logfile. Guckt zuerst in crawlDir
	 * (Arbeitsverzeichnis). Falls dort nichts gefunden, guckt in outDir
	 * (Ergebnisverzeichnis).
	 * 
	 * @param node der Knoten einer Webpage
	 */
	private static File findLatestLogFile(Node node) {
		File logfile = null;
		File latestCrawlDir = Webgatherer.getLatestCrawlDir(jobDir, node.getPid());
		File latestOutDir = Webgatherer.getLatestCrawlDir(outDir, node.getPid());
		if (latestCrawlDir != null) {
			logfile = new File(latestCrawlDir.toString() + "/crawl.log");
		}
		if (logfile == null || !logfile.exists()) {
			if (latestOutDir != null) {
				logfile = new File(latestOutDir.toString() + "/crawl.log");
			}
		}
		return logfile;
	}

	/**
	 * Ermittelt Crawler Exit Status des letzten Crawls. Der Exit-Status ist eine
	 * ganze Zahl. Der Exit-Status ist erst nach Beendigung eines Crawls
	 * verfügbar.
	 * 
	 * @param node der Knoten einer Webpage
	 * @return Crawler Exit Status des letzten wpull-Crawls
	 */
	public static int getCrawlExitStatus(Node node) {
		File logfile = findLatestLogFile(node);
		if (logfile == null || !logfile.exists()) {
			WebgatherLogger.warn(
					"Letztes Crawl-Log für PID " + node.getPid() + " nicht gefunden.");
			return -2;
		}
		CrawlLog crawlLog = new CrawlLog(logfile);
		crawlLog.parse();
		return crawlLog.getExitStatus();
	}

	/**
	 * Ermittelt den aktuellen Status des zuletzt gestarteten Crawls. Mögliche
	 * Werte sind : NEW - RUNNING - PAUSED (nur Heritrix) - ABORTED (beendet vom
	 * Operator) - CRASHED - FINISHED
	 * 
	 * @param node der Knoten einer Webpage
	 * @return Crawler Status des zuletzt gestarteten wpull-Crawls
	 */
	public static CrawlControllerState getCrawlControllerState(Node node) {
		// 1. Kein Crawl-Verzeichnis mit crawl.log vorhanden => Status = NEW
		File logfile = findLatestLogFile(node);
		if (logfile == null || !logfile.exists()) {
			WebgatherLogger.info(
					"Letztes Crawl-Log für PID " + node.getPid() + " nicht gefunden.");
			return CrawlControllerState.NEW;
		}
		// 2. Läuft noch => Status = RUNNING
		if (isCrawlRunning(node)) {
			return CrawlControllerState.RUNNING;
		}
		BufferedReader buf = null;
		String regExp = "^INFO FINISHED.";
		Pattern pattern = Pattern.compile(regExp);
		try {
			buf = new BufferedReader(new FileReader(logfile));
			String line = null;
			while ((line = buf.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					return CrawlControllerState.FINISHED;
				}
			}
		} catch (IOException e) {
			WebgatherLogger.warn(
					"Crawl Controller State cannot be defered from crawlLog "
							+ logfile.getAbsolutePath() + "! Assuming CRASHED.",
					e.toString());
		} finally {
			try {
				if (buf != null) {
					buf.close();
				}
			} catch (IOException e) {
				WebgatherLogger.warn("Read Buffer cannot be closed!");
			}
		}
		return CrawlControllerState.CRASHED;
	}

	private static boolean isCrawlRunning(Node node) {
		// TODO Auto-generated method stub
		return false;
	}

}
