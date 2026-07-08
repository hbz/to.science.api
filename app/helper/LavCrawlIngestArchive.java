package helper;

import models.CrawlerModelIngestArchive;
import play.Play;

/**
 * Eine Klasse (programmiert als Java-Thread), in der für eine vom LAV
 * (Landesarchiv NRW) gelieferte Webarchivdatei ein Webschnitt angelegt wird.
 * 
 * @author Ingolf Kuss, hbz
 * @date 2026-07-08
 */
public class LavCrawlIngestArchive extends CrawlerModelIngestArchive {

	final static String outDir =
			Play.application().configuration().getString("regal-api.lav.outDir");

	/**
	 * Der Konstruktor für LavCrawlIngestArchive Thread
	 */
	public LavCrawlIngestArchive() {
		super();
	}

	/**
	 * Diese Methode legt einen Webschnitt für das LAV-Webarchiv an.
	 */
	@Override
	public void run() {

		/**
		 * Anlage eines Webschnittes
		 */
		play.Logger.debug("Creating a WebpageVersion for pid " + getToscienceId());
		postWebpageVersion();

		return;
	}

}
