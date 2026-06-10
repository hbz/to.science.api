package helper;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
			WebgatherLogger.debug("Moving file " + getFilename() + " to directory "
					+ getCrawlerModel().getResultDir().toString());
			Files.move(sourcePath, targetPath);
			WebgatherLogger.debug("File moved successfully.");
		} catch (IOException e) {
			WebgatherLogger.error("WACZ file could not be moved to result directory! "
					+ e.getMessage());
			throw new RuntimeException(e);
		}

		/**
		 * Gibt es schon einen Webschnitt für diesen Crawl ? Dann wird kein weiterer
		 * angelegt. Für TOS-1366.
		 */
		if (webpageVersionExists()) {
			WebgatherLogger.debug(
					"Es gibt schon einen Webschnitt zur Crawl-ID " + getLastCrawlId());
			return;
		}

		/**
		 * Anlage eines Webschnittes
		 */
		postWebpageVersion();
	}

	private boolean webpageVersionExists() {
		BtrixWebclient btrixWebclient = (BtrixWebclient) getCrawlerModel();
		try {
			WebgatherLogger.debug("outDir: " + btrixWebclient.getOutDir());
			WebgatherLogger.debug("toscienceId: " + getToscienceId());
			String outDir = btrixWebclient.getOutDir() + "/" + getToscienceId();
			WebgatherLogger.debug("Durchsuche Output-Verzeichnis " + outDir
					+ " nach Crawl-Verzeichnissen für die crawlId "
					+ this.getLastCrawlId());
			File outFile = new File(outDir);
			// alle Unterverzeichnisse (=Crawl-Verzeichnisse) auflistem
			String entries[] = outFile.list(new FilenameFilter() {
				@Override
				public boolean accept(File d, String name) {
					return d.isDirectory();
				}
			});
			WebgatherLogger.debug(
					"Found a number of " + entries.length + " Crawl-Verzeichnisse.");
			// Crawl-Verzeichnisse absteigend numerisch sortieren (neueste zuerst
			// untersuchen)
			Arrays.sort(entries, new Comparator<String>() {
				@Override
				public int compare(String s1, String s2) {
					return Long.compare(Long.parseLong(s2), Long.parseLong(s1));
				}
			});
			if (entries.length > 0) {
				WebgatherLogger.debug(
						"Habe Crawl-Verzeichnisse absteigend numerisch sortiert; Neuestes ist: "
								+ entries[0]);
			}
			String regExp =
					"^(.*)-" + getLastCrawlId() + "-([0-9]+)-([0-9]+)\\.warc\\.gz$";
			WebgatherLogger.debug("Compiling regExp pattern " + regExp);
			Pattern pattern = Pattern.compile(regExp);
			for (int i = 0; i < entries.length; i++) {
				WebgatherLogger.debug("Found output crawl directory: " + entries[i]);
				/*
				 * Alle Dateien um Unterverzeichnis /archive auflisten - das sind die
				 * warc.gz-Dateien . Dabei nur solche Archivdateien berücksichtigen, die
				 * zur aktuellem (letzen) Crawl-ID gehören.
				 */
				File archiveDir = new File(outDir + "/" + entries[i] + "/archive/");
				String archiveFiles[] = archiveDir.list(new FilenameFilter() {
					@Override
					public boolean accept(File d, String name) {
						WebgatherLogger.debug("Found archive file or dir: " + d.getName());
						if (!d.isFile()) {
							// return false;
						}
						WebgatherLogger.debug("Found archive file: " + name);
						Matcher matcher = pattern.matcher(name);
						if (matcher.find()) {
							WebgatherLogger.debug("Found file " + name
									+ " containing the lastCrawlId " + getLastCrawlId());
							return true;
						}
						WebgatherLogger
								.debug("Archive file " + name + " does not match the pattern.");
						return false;
					}
				});
				if (archiveFiles.length > 0) {
					// Es gibt mindestens eine Archivdatei, die zu dieser Crawl-ID gehört.
					WebgatherLogger.debug("Es gibt schon eine Archivdatei zur Crawl-ID "
							+ getLastCrawlId() + " .");
					return true;
				}
			}
		} catch (Exception e) {
			WebgatherLogger.warn(
					"Failing check whether WebsiteVersion already exsits! Zur Sicherheit wird nun ein Webschnitt angelegt.");
			e.printStackTrace();
		}
		WebgatherLogger.debug("Es wurde keine Archivdatei zur Crawl-ID "
				+ getLastCrawlId() + " gefunden.");
		return false;
	}

}
