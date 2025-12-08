/*
 * Copyright 2025 hbz NRW (http://www.hbz-nrw.de/)
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

import java.io.File;

import org.apache.commons.io.FileUtils;

import actions.Read;
import models.Gatherconf;
import models.Node;
import play.Logger;

/**
 * Diese Klasse implementiert einen Java-Thread. Die zu einer WebpageVersion
 * (Webschnitt) gehörenden Archivdaten werden von der Festplatte entfernt. Also
 * das komplette zu diesem Webschnitt gehörende Crawl-Vereichnis samt Inhalten.
 * Dieser Thread wird von der Standard DELETE-Methode für Ressourcen vom
 * contentType "version" aufgerufen.
 * 
 * @author Ingolf Kuss, hbz
 * @date 28.11.2025
 *
 */
public class WebpageVersionRemover extends Thread {

	private Node node = null;
	private Gatherconf conf = null;
	private String localpath = null;

	private static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

	/**
	 * ein Konstruktor für diese Klasse
	 */
	public WebpageVersionRemover() {

	}

	/**
	 * Setze Node der Webpage
	 * 
	 * @param node der Node der lokalen Webpage
	 */
	public void setNode(Node node) {
		this.node = node;
	}

	@Override
	public void run() {
		try {
			conf = Gatherconf.create(node.getConf());
			localpath = conf.getLocalDir();
			WebgatherLogger.debug("Lösche Verzeichnis: " + localpath);
			FileUtils.deleteDirectory(new File(localpath));
			WebgatherLogger.info(
					"Verzeichnis wurde samt Inhalt und Unterverzeichnissen gelöscht: "
							+ localpath);
		} catch (Exception e) {
			WebgatherLogger.error(e.toString());
			throw new RuntimeException("WebpageVersion's " + node.getPid()
					+ " webarchive files could not be successfully removed! localpath = "
					+ localpath, e);
		}
	}

}
