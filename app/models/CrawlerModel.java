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

import play.Logger;
import play.Play;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import helper.CDNCrawl;
import helper.WebgatherUtils;

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

	private Node node = null;
	private Gatherconf conf = null;
	private String urlAscii = null;
	private String date = null;
	private String datetime = null;
	private File crawlDir = null;
	private File resultDir = null;

	private File cdxFile = null;
	private File cdxFileNew = null;
	private File cdxFileSave = null;
	private String localpath = null;
	private String host = null;
	private String warcFilename = null;

	private ArrayList<String> domains = null;
	private String msg = null;
	private int exitState = 0;

	private String jobDir = null;
	private String outDir = null;

	/**
	 * Das lokale Verzeichnis, in das die Crawlreports geschrieben werden
	 */
	final static public String crawlreportsDir = Play.application()
			.configuration().getString("toscience-api.webgatherer.crawlreports");

	/**
	 * ein Logger für das Webgathering
	 */
	protected static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

	/**
	 * Setter f+r Node
	 * 
	 * @param myNode ein Node-Objekt für eine Webpage
	 */
	public void setNode(Node myNode) {
		this.node = myNode;
	}

	/**
	 * Getter für Node
	 * 
	 * @return das Node-Objekt der Webpage
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * Getter für Conf
	 * 
	 * @return eine Gatherconf (Crawler-Konfiguration)
	 */
	public Gatherconf getConf() {
		return conf;
	}

	/**
	 * Setter for urlAscii
	 * 
	 * @param url an url in ASCII format
	 */
	public void setUrlAscii(String url) {
		this.urlAscii = url;
	}

	/**
	 * Getter für URL ASCII
	 * 
	 * @return die URL im ASCII-Format
	 */
	public String getUrlAscii() {
		return urlAscii;
	}

	/**
	 * Setter für Datetime
	 * 
	 * @param myDatetime ein Datums-Zeit-String
	 */
	public void setDatetime(String myDatetime) {
		this.datetime = myDatetime;
	}

	/**
	 * Getter für Datetime
	 * 
	 * @return ein Datums-Zeit-String
	 */
	public String getDatetime() {
		return datetime;
	}

	/**
	 * Setter für crawlDir
	 * 
	 * @param file eine Datei, die als crawlDir gesetzt wird.
	 */
	public void setCrawlDir(File file) {
		crawlDir = file;
	}

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
	 * Setter für resultDir
	 * 
	 * @param file eine Datei, die als resultDir gesetzt wird.
	 */
	public void setResultDir(File file) {
		resultDir = file;
	}

	/**
	 * Die Methode, um resultDir auszulesen
	 * 
	 * @return resultDir Das Verzeichnis (absolute Pfadangabe, oberste Ebene), in
	 *         dem die Wayback nach fertigen WARC-Dateien sucht.
	 */
	public File getResultDir() {
		return resultDir;
	}

	/**
	 * Die Methode, um die CDX-Datei zu setzen
	 * 
	 * @param newfile eine Datei, die als CDX-Datei gesetzt wird.
	 */
	public void setCdxFile(File newfile) {
		cdxFile = newfile;
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
	 * Die Methode, um die neue CDX-Datei zu setzen
	 * 
	 * @param newfile eine Datei, die als CDX-Datei gesetzt wird.
	 */
	public void setCdxFileNew(File newfile) {
		cdxFileNew = newfile;
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
	 * Die Methode, um die gespeicherte CDX-Datei zu setzen
	 * 
	 * @param file eine Datei, die als gespeicherte CDX-Datei gesetzt wird.
	 */
	public void setCdxFileSave(File file) {
		cdxFileSave = file;
	}

	/**
	 * Die Methode, um die gespeicherte CDX-Datei auszulesen
	 * 
	 * @return cdxFileSave ist eine CDX-Datei, die der Crawler beim nächsten Crawl
	 *         wieder verwenden wird. Sie liegt im Hauptverzeichnis der
	 *         Site-Crawls.
	 */
	public File getCdxFileSave() {
		return cdxFileSave;
	}

	/**
	 * Setter für localpath
	 * 
	 * @param mylocalpath der lokale Pfad zum Webarchiv
	 */
	public void setLocalpath(String mylocalpath) {
		this.localpath = mylocalpath;
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
	 * Getter für Host
	 * 
	 * @return der Hostname, von dem eingesammelt wird
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Getter für warcFilename
	 * 
	 * @return warcFilename der Name der WARC-Datei
	 */
	public String getWarcFilename() {
		return warcFilename;
	}

	/**
	 * Setter für Domains
	 * 
	 * @param myDomains eine Liste von Domains, die gecrawlt werden sollen
	 */
	public void setDomains(ArrayList<String> myDomains) {
		this.domains = myDomains;
	}

	/**
	 * Getter für Domains
	 * 
	 * @return eine Liste von Domains, die gecrawlt werden sollen
	 */
	public ArrayList<String> getDomains() {
		return this.domains;
	}

	/**
	 * Setter für Message
	 * 
	 * @param myMsg eine Textnachricht
	 */
	public void setMsg(String myMsg) {
		this.msg = myMsg;
	}

	/**
	 * Getter für Message
	 * 
	 * @return eine Textnachricht
	 */
	public String getMsg() {
		return this.msg;
	}

	/**
	 * Setter für ExitState
	 * 
	 * @param myExitState Exit Status für den Hauptcrawl
	 */
	public void setExitState(int myExitState) {
		this.exitState = myExitState;
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
	 * Setter für jobDir
	 * 
	 * @param dir eine Verzeichnisname, der als jobDir gesetzt wird
	 */
	public void setJobDir(String dir) {
		jobDir = dir;
	}

	/**
	 * Getter für jobDir
	 * 
	 * @return das jobDir
	 */
	public String getJobDir() {
		return this.jobDir;
	}

	/**
	 * Setter für outDir
	 * 
	 * @param dir eine Verzeichnisname, der als outDir gesetzt wird
	 */
	public void setOutDir(String dir) {
		outDir = dir;
	}

	/**
	 * Getter für outDir
	 * 
	 * @return das outDir
	 */
	public String getOutDir() {
		return this.outDir;
	}

	/**
	 * Leer-Konstruktor für das Crawler Modell
	 */
	public CrawlerModel() {
		this.date = new SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
		this.datetime =
				date + new SimpleDateFormat("HHmmss").format(new java.util.Date());
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
			if (!resultDir.exists() && conf.getCrawlerSelection()
					.equals(Gatherconf.CrawlerSelection.wpull)) {
				// create output directory
				// der Move vom jobDir zum outDir findet nur bei wpull-Crawls statt.
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
		try {
			CDNCrawl cdnCrawl = new CDNCrawl(this);
			cdnCrawl.start();
		} catch (Exception e) {
			WebgatherLogger.error(e.toString());
			throw new RuntimeException("Crawl not successfully started!", e);
		}

	} // Ende startCrawl()

}
