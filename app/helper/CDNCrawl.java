package helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import models.CrawlerModel;
import models.Gatherconf;
import models.Gatherconf.AgentIdSelection;
import play.Logger;
import play.Play;

/**
 * In dieser Klasse wir der CDN-Precrawl für Webcrawl ausgeführt.
 */
public class CDNCrawl extends Thread {

	private CrawlerModel crawlerModel = null;
	private Gatherconf conf = null;
	private File crawlDir = null;
	private File cdxFile = null;
	private File cdxFileNew = null;
	private int CDNGathererExitState = 0;

	final static private String cdn =
			Play.application().configuration().getString("regal-api.cdntools.cdn");

	/**
	 * ein Logger für das Webgathering
	 */
	protected static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

	/**
	 * Der Konstruktor für diese Klasse.
	 * 
	 * @param model a Crawler Model for this Crawl
	 */
	public CDNCrawl(CrawlerModel model) {
		this.crawlerModel = model;
		// das CDX-File für CDN-Crawls
		this.cdxFile = new File(crawlerModel.getJobDir() + "/" + conf.getName()
				+ "/WEB-" + crawlerModel.getHost() + ".cdx");
	}

	/**
	 * This methods starts a CDN-Precrawl and waits for completion.
	 */
	@Override
	public void run() {
		WebgatherLogger.info("Bereite Aufruf des CDN-Gatherer vor. warcFilename="
				+ crawlerModel.getWarcFilename() + ".");
		try {
			// 1. Vorbereiten des CDN-Precrawls
			conf = crawlerModel.getConf();
			crawlDir = crawlerModel.getCrawlDir();

			if (cdxFile.exists()) {
				WebgatherLogger
						.debug("CDX-Datei gefunden: " + cdxFile.getAbsolutePath());
				this.cdxFileNew = new File(crawlerModel.getCrawlDir().getAbsolutePath()
						+ "/" + crawlerModel.getWarcFilename() + ".cdx");
				FileUtils.copyFile(cdxFile, cdxFileNew);
				WebgatherLogger
						.debug("Neue CDX-Datei angelegt: " + cdxFileNew.getAbsolutePath());
			}

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
			String executeCommand = new String(cdn + " " + crawlerModel.getUrlAscii()
					+ " " + crawlerModel.getWarcFilename());
			AgentIdSelection agentId = conf.getAgentIdSelection();
			executeCommand =
					executeCommand.concat(" " + Gatherconf.agentTable.get(agentId));
			executeCommand = executeCommand.concat(" Cookie:");
			if (conf.getCookie() != null && !conf.getCookie().isEmpty()) {
				executeCommand =
						executeCommand.concat(conf.getCookie().replaceAll(" ", "%20"));
			}
			executeCommand = executeCommand.concat(" " + waitParam);
			if (this.cdxFileNew != null) {
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
			try (InputStream inputStream = proc.getInputStream()) {
				assert inputStream.read() == -1;
			}
			CDNGathererExitState = proc.waitFor();
			/**
			 * Exit-Status: 0 = Crawl erfolgreich beendet
			 */
			WebgatherLogger.info("CDN-Crawl für " + conf.getName()
					+ " wurde beendet mit Exit-Status " + CDNGathererExitState);

			// 3. Auslesen der vom cdnparse angelegten Datei hostnames.txt
			// cdnparse ist der 1. Schritt des CDN-Precrawls und ein Python-Programm
			ArrayList<String> domains = conf.getDomains();
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
			crawlerModel.setDomains(domains);

			/**
			 * 4. Nach erfolgreichem CDN-Precrawl wird die neue CDX-Datei für den
			 * Precrawl in das übergeordenete Verzeichnis kopiert und umbenannt. Beim
			 * nächsten CDN-Crawl wird die CDX-Datei von diesem Ort wieder abgeholt
			 * werden.
			 * 
			 * @author Ingolf Kuss
			 * @date 2026-04-27
			 */
			if (cdxFileNew.exists()) {
				/*
				 * File cdxFileSave = new File(crawlDir.getParent() + "/WEB-" +
				 * WebgatherUtils.getDomain(conf.getUrl()) + ".cdx");
				 */
				FileUtils.copyFile(cdxFileNew, cdxFile);
				WebgatherLogger.debug(
						"Aktuelle CDX-Datei abgelegt in: " + cdxFile.getAbsolutePath());
			}

		} catch (Exception e) {
			WebgatherLogger.error(e.toString());
			throw new RuntimeException("cdn crawl not successfully started!", e);
		}
	}

}
