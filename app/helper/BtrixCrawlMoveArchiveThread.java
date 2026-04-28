package helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import actions.Create;
import actions.Read;
import models.Node;
import play.Logger;

/**
 * Eine Klasse (programmiert als Java-Thread), in der eine
 * Browsertrix-Webarchivdatei in das Ergebnisverzeichnis btrix-data verschoben
 * wird. Dieses kann eine lang andauernde Dateioperation sein, daher ist diese
 * Klasse als Thread porgrammiert. Sie wird also im Hintergrund ausgeführt. Nach
 * Beendigung des Verschiebens wird automatisch ein neuer Webschnitt für diese
 * Archivdatei angelegt. Der Indexer auf dem zugehörigen Wayback-Server wird die
 * Archivdatei im Ergebnisverzeichnis auspacken und dessen Einzeldateien (Typ
 * .warc.gz) in Wayback indexieren.
 * 
 * @author I. Kuss, hbz
 * @date 2026-04-28
 */
public class BtrixCrawlMoveArchiveThread extends Thread {

	private BtrixWebclient btrixWebclient = null;
	private String filename = null;
	private File waczFile = null;
	private String toscienceId = null;
	private String lastCrawlId = null;
	private String datetime = null;

	/**
	 * ein Logger für das Webgathering
	 */
	protected static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

	/**
	 * Der Konstruktor für BtrixCrawlMoveArchive Thread
	 * 
	 * @param model eine Instanz von Browsertrix-Webclient
	 */
	public BtrixCrawlMoveArchiveThread(BtrixWebclient model) {
		this.btrixWebclient = model;
	}

	/**
	 * Setter für filename
	 * 
	 * @param myfilename Der volle Name (mit Pfad) einer WACZ-Archivdatei
	 */
	public void setFilename(String myfilename) {
		this.filename = myfilename;
		this.waczFile = new File(filename);
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
	 * Setter für lastCrawlId
	 * 
	 * @param myCrawlId eine Browsertrix-CrawlId für den Webcrawl
	 */
	public void setLastCrawlId(String myCrawlId) {
		this.lastCrawlId = myCrawlId;
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
	 * This methods starts moving the archive and creating a WebpageVersion
	 */
	@Override
	public void run() {

		String waczFilenameResultDir =
				btrixWebclient.getResultDir().toString() + "/" + waczFile.getName();
		try {
			Path sourcePath = Paths.get(filename);
			Path targetPath = Paths.get(waczFilenameResultDir);
			play.Logger.debug("Moving file " + filename + " to directory "
					+ btrixWebclient.getResultDir().toString());
			Files.move(sourcePath, targetPath);
			play.Logger.debug("File moved successfully.");
		} catch (IOException e) {
			play.Logger.error("WACZ file could not be moved to result directory! "
					+ e.getMessage());
			throw new RuntimeException(e);
		}

		/**
		 * Webschnitt anlegen mit Zeitstempel: Angezeigt wird dateTimeLocal im
		 * Format "yyyy-MM-dd HH:mm:ss" . Der Link unter "zum Webschnitt" führt aber
		 * auf dateTimeUtc im Format "yyyyMMddHHmmss".
		 */
		String versionPid = null;
		Node n = new Read().readNode(toscienceId);
		new Create().postWebpageVersion(n, versionPid, lastCrawlId, "btrix",
				datetime, waczFile.getName());
		play.Logger.info("WebpageVersion für " + toscienceId + "wurde angelegt.");

	}

}
