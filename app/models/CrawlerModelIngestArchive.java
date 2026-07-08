package models;

import java.io.File;

import actions.Create;
import actions.Read;
import play.Logger;

/**
 * Diese Klasse implementiert das Verarbeiten einer Archivdatei für beliebige
 * Crawler. Archivdateien werden in das Ergebnisverzeichnis verschoben. Dort
 * wird eine Wayback-Maschine sie abholen und indexieren. Außerdem wird ein
 * Webschnitt (toscience-Objekt und Fedora-Objekt) für diesen Crawl angelegt.
 * 
 * @author Ingolf Kuss
 * @date 2026-05-11
 */
public class CrawlerModelIngestArchive extends Thread {

	private Gatherconf.CrawlerSelection crawler = null;
	private CrawlerModel crawlerModel = null;
	private String toscienceId = null;
	private String datetime = null;
	private String filename = null;
	private String filenameBase = null;
	private String lastCrawlId = null;

	/**
	 * ein Logger für das Webgathering
	 */
	protected static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

	/**
	 * Setter für crawler
	 * 
	 * @param mycrawler der Name eines Crawlers
	 */
	public void setCrawler(Gatherconf.CrawlerSelection mycrawler) {
		this.crawler = mycrawler;
	}

	/**
	 * Getter für crawler
	 * 
	 * @return der Name eines Crawlers
	 */
	public Gatherconf.CrawlerSelection getCrawler() {
		return crawler;
	}

	/**
	 * Setter für crawlerModel
	 * 
	 * @param mymodel ein CrawlerModel
	 */
	public void setCrawlerModel(CrawlerModel mymodel) {
		this.crawlerModel = mymodel;
	}

	/**
	 * Getter für crawlerModel
	 * 
	 * @return ein CrawlerModel
	 */
	public CrawlerModel getCrawlerModel() {
		return crawlerModel;
	}

	/**
	 * Setter für filename
	 * 
	 * @param myfilename Der Dateiname einer WEB-Archivdatei
	 */
	public void setFilename(String myfilename) {
		this.filename = myfilename;
	}

	/**
	 * Getter für filename
	 * 
	 * @return ein Dateiname für eine Webarchiv-Datei
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Setter für filenameBase
	 * 
	 * @param myfilename Der Basisname einer Webarchivdatei (ohne die Endung
	 *          .warc, .warc.gz oder .wacz)
	 */
	public void setFilenameBase(String myfilename) {
		this.filenameBase = myfilename;
	}

	/**
	 * Getter für filenameBase
	 * 
	 * @return Der Basisname einer Webarchivdatei (ohne die Endung .warc, .warc.gz
	 *         oder .wacz)
	 */
	public String getFilenameBase() {
		return filenameBase;
	}

	/**
	 * Setter für Toscience-ID
	 * 
	 * @param myToscienceId die Toscience-ID für die Webpage
	 */
	public void setToscienceId(String myToscienceId) {
		this.toscienceId = myToscienceId;
	}

	/**
	 * Getter für Toscience-ID
	 * 
	 * @return die Toscience-ID für die Webpage
	 */
	public String getToscienceId() {
		return toscienceId;
	}

	/**
	 * Setter für datetime
	 * 
	 * @param mydatetime ein Datums-Zeit-Stempel für den letzten
	 *          Browsertrix-Crawl. Zeitzone ist die lokale Systemzeit.
	 */
	public void setDatetime(String mydatetime) {
		this.datetime = mydatetime;
	}

	/**
	 * Getter für datetime
	 * 
	 * @return ein Datums-Zeit-Stempel für Crawl. Zeitzone ist die lokale
	 *         Systemzeit.
	 */
	public String getDatetime() {
		return datetime;
	}

	/**
	 * Setter für lastCrawlId
	 * 
	 * @param myCrawlId eine CrawlId für den Webcrawl, z.B. für Browsertrix. Kann
	 *          für andere Crawler null sein.
	 */
	public void setLastCrawlId(String myCrawlId) {
		this.lastCrawlId = myCrawlId;
	}

	/**
	 * Getter für lastCrawlId
	 * 
	 * @return ein eine CrawlId für den Webcrawl, z.B. für Browsertrix. Kann für
	 *         andere Crawler null sein.
	 */
	public String getLastCrawlId() {
		return lastCrawlId;
	}

	/**
	 * Webschnitt anlegen mit Zeitstempel: Angezeigt wird dateTimeLocal im Format
	 * "yyyy-MM-dd HH:mm:ss" . Der Link unter "zum Webschnitt" führt aber auf
	 * dateTimeUtc im Format "yyyyMMddHHmmss". Diese Methode ist unabhängig vom
	 * Crawler-Modell.
	 * 
	 * @author: I. Kuss
	 * @date 2026-05-11
	 */
	public void postWebpageVersion() {

		WebgatherLogger.debug("Beginn erzeuge WebpageVersion für PID "
				+ getToscienceId() + ", Zeitstempel " + getDatetime());
		String versionPid = null;
		Node n = new Read().readNode(getToscienceId());
		new Create().postWebpageVersion(n, versionPid, getLastCrawlId(),
				getCrawler().toString(), getDatetime(),
				new File(getFilename()).getName());
		WebgatherLogger
				.info("WebpageVersion für " + getToscienceId() + "wurde angelegt.");
	}

}
