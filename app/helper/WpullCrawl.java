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

import models.Gatherconf;
import models.Gatherconf.QuotaUnitSelection;
import models.Globals;
import play.Logger;
import play.Play;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * a class to implement a wpull crawl
 * 
 * @author Ingolf Kuss
 *
 */
public class WpullCrawl {

	private Gatherconf conf = null;
	private String date = null;
	private String datetime = null;
	private File crawlDir = null;
	private String localpath = null;
	private String warcFilename = null;
	private int exitState = 0;
	private String msg = null;

	final String jobDir =
			Play.application().configuration().getString("regal-api.wpull.jobDir");
	final String crawler =
			Play.application().configuration().getString("regal-api.wpull.crawler");

	private static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

	public File getCrawlDir() {
		return crawlDir;
	}

	public String getLocalpath() {
		return localpath;
	}

	public int getExitState() {
		return exitState;
	}

	/**
	 * a constructor of a wpull crawl
	 * 
	 * @param conf the crawler configuration for the website
	 */
	public WpullCrawl(Gatherconf conf) {
		this.conf = conf;
	}

	/**
	 * creates a new wpull crawler job
	 */
	public void createJob() {
		WebgatherLogger.debug("Create new job " + conf.getName());
		try {
			if (conf.getName() == null) {
				throw new RuntimeException("The configuration has no name !");
			}
			date = new SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
			datetime =
					date + new SimpleDateFormat("HHmmss").format(new java.util.Date());
			crawlDir = new File(jobDir + "/" + conf.getName() + "/" + datetime);
			if (!crawlDir.exists()) {
				// create job directory
				WebgatherLogger.debug("Create job Directory " + jobDir + "/"
						+ conf.getName() + "/" + datetime);
				crawlDir.mkdirs();
			}
		} catch (Exception e) {
			msg = "Cannot create jobDir in " + jobDir + "/" + conf.getName();
			WebgatherLogger.error(msg);
			throw new RuntimeException(msg);
		}
	}

	/**
	 * starts crawling with wpull
	 */
	public void startJob() {
		try {
			String executeCommand = buildExecCommand();
			WebgatherLogger.info("Executing command " + executeCommand);
			WebgatherLogger.info("Logfile = " + crawlDir.toString() + "/crawl.log");
			String[] execArr = executeCommand.split(" ");
			ProcessBuilder pb = new ProcessBuilder(execArr);
			assert crawlDir.isDirectory();
			pb.directory(crawlDir);
			File log = new File(crawlDir.toString() + "/crawl.log");
			log.createNewFile();
			pb.redirectErrorStream(true);
			pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
			Process proc = pb.start();
			assert pb.redirectInput() == ProcessBuilder.Redirect.PIPE;
			assert pb.redirectOutput().file() == log;
			assert proc.getInputStream().read() == -1;
			/*
			 * den Pfad zum WARC unter Globals.heritrixData zu hängen ist eigentlich
			 * Blödsinn, aber ohne localpath wird im Frontend kein Link zu Openwayback
			 * erzeugt (warum nicht ?)
			 */
			localpath = Globals.heritrixData + "/wpull-data" + "/" + conf.getName()
					+ "/" + datetime + "/" + warcFilename + ".warc.gz";
			// exitState = proc.waitFor(); // don't wait
		} catch (Exception e) {
			WebgatherLogger.error(e.toString());
			throw new RuntimeException("wpull crawl not successfully started!", e);
		}
	}

	/**
	 * Builds a shell executable command which starts a wpull crawl
	 * 
	 * the shell executable command as a String For parameters in use see:
	 * http://wpull.readthedocs.io/en/master/options.html if marked as mandatory,
	 * parameter is needed for running smoothly in edoweb context. So only remove
	 * them if reasonable.
	 * 
	 * @return the ExecCommand for wpull
	 */
	private String buildExecCommand() {
		String urlRaw = conf.getUrl().replaceAll("^http://", "")
				.replaceAll("^https://", "").replaceAll("/$", "");
		warcFilename = "WEB-" + urlRaw + "-" + date;
		StringBuilder sb = new StringBuilder();
		sb.append(crawler + " " + conf.getUrl());
		ArrayList<String> domains = conf.getDomains();
		if (domains.size() > 0) {
			sb.append(" --span-hosts");
		}
		sb.append(" --domains=" + urlRaw);
		for (int i = 0; i < domains.size(); i++) {
			sb.append("," + domains.get(i));
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
		if (level != 0) {
			sb.append(" --level=" + Integer.toString(level)); // number of recursions
		}

		int maxByte = conf.getMaxCrawlSize();
		if (maxByte > 0) {
			QuotaUnitSelection qFactor = conf.getQuotaUnitSelection();
			// TODO implement select factor
			sb.append(" --quota=" + Integer.toString(maxByte));
		}

		sb.append(" --link-extractors=javascript,html,css");
		sb.append(" --warc-file=" + warcFilename);
		sb.append(" --user-agent=\"InconspiciousWebBrowser/1.0\" --no-robots");
		sb.append(" --escaped-fragment --strip-session-id");
		sb.append(
				" --no-host-directories --convert-links --page-requisites --no-parent");
		sb.append(" --database=" + warcFilename + ".db");
		sb.append(" --no-check-certificate");
		sb.append(" --no-directories"); // mandatory to prevent runtime errors
		sb.append(" --delete-after"); // mandatory for reducing required disc space
		sb.append(" --convert-links"); // mandatory to rewrite relative urls
		return sb.toString();
	}

}
