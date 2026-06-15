package helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import models.CrawlerModelIngestArchive;
import play.Play;

/**
 * Eine Klasse (programmiert als Java-Thread), in der eine Wpull-Webarchivdatei
 * in das Ergebnisverzeichnis wpull-data verschoben wird. Dieses kann eine lang
 * andauernde Dateioperation sein, daher ist diese Klasse als Thread
 * porgrammiert. Sie wird also im Hintergrund ausgeführt. Nach Beendigung des
 * Verschiebens wird automatisch ein neuer Webschnitt für diese Archivdatei
 * angelegt. Der Indexer auf dem zugehörigen Wayback-Server wird die Archivdatei
 * (Typ .warc.gz) in Wayback indexieren.
 * 
 * @author Ingolf Kuss, hbz
 * @date 2026-05-11
 */
public class WpullCrawlIngestArchive extends CrawlerModelIngestArchive {

	final static String finishedDir = Play.application().configuration()
			.getString("regal-api.wpull.finishedDir");
	final static String outDir =
			Play.application().configuration().getString("regal-api.wpull.outDir");

	/**
	 * Der Konstruktor für WpullCrawlIngestArchive Thread
	 */
	public WpullCrawlIngestArchive() {
		super();
	}

	/**
	 * This methods starts moving the archive and creating a WebpageVersion
	 */
	@Override
	public void run() {

		/**
		 * WARC-Datei und CDX-Datei werden vom "finished"-Verzeichnis in das
		 * "outDir" verschoben.
		 */
		try {
			String relPath = "/" + getToscienceId() + "/" + getDatetime() + "/"
					+ getFilenameBase();
			Path sourcePath = Paths.get(finishedDir + relPath + ".warc.gz");
			Path targetPath = Paths.get(outDir + relPath + ".warc.gz");
			WebgatherLogger.debug("Moving file " + sourcePath.toString() + " to "
					+ targetPath.toString());
			Files.move(sourcePath, targetPath);
			WebgatherLogger.debug("File moved successfully.");

			sourcePath = Paths.get(finishedDir + relPath + ".cdx");
			targetPath = Paths.get(outDir + relPath + ".cdx");
			WebgatherLogger.debug("Moving file " + sourcePath.toString() + " to "
					+ targetPath.toString());
			Files.move(sourcePath, targetPath);
			play.Logger.debug("File moved successfully.");
		} catch (IOException e) {
			WebgatherLogger.error("WARC and/or CDX file could not be moved to result directory! "
							+ e.getMessage());
			throw new RuntimeException(e);
		}

		/**
		 * Anlage eines Webschnittes
		 */
		postWebpageVersion();

		/**
		 * Abschließend Löschen des crawldir im finished-Verzeichnis.
		 */
		File crawldir =
				new File(finishedDir + "/" + getToscienceId() + "/" + getDatetime());
		if (crawldir.delete()) {
			WebgatherLogger.info("Crawlverzeichnis im finished-Dir, "
					+ crawldir.toString() + ", wurde erfolgreich gelöscht!");
		} else {
			WebgatherLogger
					.warn("Crawlverzeichnis im finished-Dir, " + crawldir.toString()
							+ ", konnte nicht gelöscht werden (nicht leer ?)!");
		}
		return;
	}

}
