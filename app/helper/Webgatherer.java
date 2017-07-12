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

import play.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.text.ParseException;

import com.ibm.icu.util.Calendar;

import models.Gatherconf;
import models.Globals;
import models.Link;
import models.Node;
import actions.Create;
import actions.Create.WebgathererTooBusyException;
import actions.Read;

/**
 * @author Jan Schnasse
 *
 */
public class Webgatherer implements Runnable {

	private static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

	@Override
	public void run() {
		// get all webpages

		WebgatherLogger.info("List 50000 resources of type webpage from namespace "
				+ Globals.namespaces[0] + ".");
		play.Logger.info("List 50000 resources of type webpage from namespace "
				+ Globals.namespaces[0] + ".");
		List<Node> webpages =
				new Read().listRepo("webpage", Globals.namespaces[0], 0, 50000);
		WebgatherLogger.info("Found " + webpages.size() + " webpages.");
		int count = 0;
		int precount = 0;
		int limit = play.Play.application().configuration()
				.getInt("regal-api.heritrix.crawlsPerNight");
		// get all configs
		for (Node n : webpages) {
			try {
				precount++;
				WebgatherLogger.info("Precount: " + precount);
				WebgatherLogger.info("PID: " + n.getPid());
				WebgatherLogger.info(
						"Config: " + n.getConf() + " is being created in Gatherconf.");
				Gatherconf conf = Gatherconf.create(n.getConf());
				if (!conf.isActive()) {
					WebgatherLogger.info("Site " + n.getPid() + " ist deaktiviert.");
					continue;
				}
				WebgatherLogger.info("Test if " + n.getPid() + " is scheduled.");
				// find open jobs
				if (isOutstanding(n, conf)) {
					WebgatherLogger.info("Create new version for: " + n.getPid() + ".");
					new Create().createWebpageVersion(n);
					count++; // count erst hier, so dass fehlgeschlagene Launches nicht
										// mitgezählt werden
				}

			} catch (WebgathererTooBusyException e) {
				WebgatherLogger.error("Webgatherer stopped! Heritrix is too busy.");
			} catch (Exception e) {
				WebgatherLogger
						.error("Couldn't create webpage version for " + n.getPid(), e);
			}
			if (count >= limit)
				break;
		}
	}

	/**
	 * @param n a webpage
	 * @param conf user definde config
	 * @return nextLaunch
	 * @throws Exception can be IOException or Json related Exceptions
	 */
	public static Date nextLaunch(Node n) throws Exception {
		Date lastHarvest = new Read().getLastModifiedChild(n).getLastModified();
		Gatherconf conf = Gatherconf.create(n.getConf());
		Calendar cal = Calendar.getInstance();
		cal.setTime(lastHarvest);
		Date nextTimeHarvest = getSchedule(cal, conf);
		return nextTimeHarvest;
	}

	private boolean isOutstanding(Node n, Gatherconf conf) {
		if (new Date().before(conf.getStartDate()))
			return false;
		// if a crawl job is still running, never return with true !!
		List<Link> parts = n.getRelatives(archive.fedora.FedoraVocabulary.HAS_PART);
		if (parts == null || parts.isEmpty()) {
			return true;
		}
		try {
			File latest = Globals.heritrix.getLatestCrawlDir(n.getPid());
			if (latest == null) {
				return true;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdf_hr = new SimpleDateFormat("yyyy-MM-dd");
			Date latestDate = sdf.parse(latest.getName().substring(0, 8));
			Calendar latestCalendar = Calendar.getInstance();
			latestCalendar.setTime(latestDate);
			if (conf.getInterval().equals(models.Gatherconf.Interval.once)) {
				WebgatherLogger.info(n.getPid()
						+ " will be gathered only once. It has already been gathered on "
						+ sdf_hr.format(latestDate));
				return false;
			}
			WebgatherLogger.info(n.getPid() + " has been last gathered on "
					+ sdf_hr.format(latestDate));
			WebgatherLogger
					.info(n.getPid() + " shall be launched " + conf.getInterval());
			Date nextDateHarvest = getSchedule(latestCalendar, conf);
			WebgatherLogger.info(n.getPid() + " should be next gathered on "
					+ sdf_hr.format(nextDateHarvest));
			Date today = new Date();
			if (sdf.format(nextDateHarvest).compareTo(sdf.format(today)) > 0) {
				WebgatherLogger.info(
						n.getPid() + " " + n.getConf() + " will be launched next time at "
								+ new SimpleDateFormat("yyyy-MM-dd").format(nextDateHarvest));
				return false;
			}
			WebgatherLogger.info(n.getPid() + " will be launched now!");
			return true;
		} catch (ParseException e) {
			WebgatherLogger.error("Cannot parse date string.", e);
			return false;
		}
	}

	private static Date getSchedule(Calendar cal, Gatherconf conf) {
		switch (conf.getInterval()) {
		case daily:
			cal.add(Calendar.DATE, 1);
			break;
		case weekly:
			cal.add(Calendar.DATE, 7);
			break;
		case monthly:
			cal.add(Calendar.MONTH, 1);
			break;
		case quarterly:
			cal.add(Calendar.MONTH, 3);
			break;
		case halfYearly:
			cal.add(Calendar.MONTH, 6);
			break;
		case annually:
			cal.add(Calendar.YEAR, 1);
			break;
		case once:
			break;
		}
		return cal.getTime();
	}
}
