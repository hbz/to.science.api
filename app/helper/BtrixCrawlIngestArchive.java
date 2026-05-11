package helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import models.CrawlerModelIngestArchive;

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
public class BtrixCrawlIngestArchive extends CrawlerModelIngestArchive {

	/**
	 * Der Konstruktor für BtrixCrawlIngestArchive Thread
	 */
	public BtrixCrawlIngestArchive() {
		super();
	}

	/**
	 * This methods starts moving the archive and creating a WebpageVersion
	 */
	@Override
	public void run() {

		/**
		 * Verschiebe WACZ-Datei ins Result-Dir.
		 */
		String waczFilenameResultDir = getCrawlerModel().getResultDir().toString()
				+ "/" + new File(getFilename()).getName();
		try {
			Path sourcePath = Paths.get(getFilename());
			Path targetPath = Paths.get(waczFilenameResultDir);
			play.Logger.debug("Moving file " + getFilename() + " to directory "
					+ getCrawlerModel().getResultDir().toString());
			Files.move(sourcePath, targetPath);
			play.Logger.debug("File moved successfully.");
		} catch (IOException e) {
			play.Logger.error("WACZ file could not be moved to result directory! "
					+ e.getMessage());
			throw new RuntimeException(e);
		}

		/**
		 * Anlage eines Webschnittes
		 */
		postWebpageVersion();
	}

}
