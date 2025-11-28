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
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import actions.Create;
import actions.Delete;
import models.Gatherconf;
import models.Globals;
import models.Node;
import play.Logger;

/**
 * Diese Klasse implementiert einen Java-Thread. Es wird eine WebpageVersion
 * (Webschnitt) von einem entfernten Server in das lokale System importiert.
 * 
 * @author Ingolf Kuss, hbz
 * @date 11.2025
 *
 */
public class WebpageVersionImporter extends Thread {

	private Node node = null;
	private Gatherconf conf = null;
	private String datetime = null;
	private String localpath = null;
	private String remotepath = null;
	private CopyOption[] copyOptions = new CopyOption[] {
			StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES };
	private String warcFilename = null;
	private String versionPid = null;
	private String quellserverWebschnittPid = null;
	private boolean deleteQuellserverWebschnitt = false;
	private String msg = null;

	static Create create = new Create();
	static Delete delete = new Delete();
	private static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

	/**
	 * ein Konstruktor für diese Klasse
	 */
	public WebpageVersionImporter() {

	}

	/**
	 * Setze Node der Webpage
	 * 
	 * @param node der Node der lokalen Webpage
	 */
	public void setNode(Node node) {
		this.node = node;
	}

	/**
	 * Setze Gatherconf des Webschnittes
	 * 
	 * @param conf die Gatherconf des lokalen Webschnittes
	 */
	public void setConf(Gatherconf conf) {
		this.conf = conf;
	}

	/**
	 * Setze Zeitstempel des Webschnittes
	 * 
	 * @param datetime der Zeitstempel, an dem dieser Webschnitt erstellt wurde.
	 *          Format YYYYMMddHHmmss.
	 */
	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}

	/**
	 * Setze lokalen Pfad
	 * 
	 * @param localpath lokaler Pfad des Datenverzeichnisses
	 */
	public void setLocalpath(String localpath) {
		this.localpath = localpath;
	}

	/**
	 * Hole lokalen Pfad
	 * 
	 * @return lokaler Pfad des Datenverzeichnissses
	 */
	public String getLocalpath() {
		return this.localpath;
	}

	/**
	 * Setze angemounteten Pfad
	 * 
	 * @param remotepath angemountetes Datenverzeichnis, voller Pfad
	 */
	public void setRenotepath(String remotepath) {
		this.remotepath = remotepath;
	}

	/**
	 * Hole angemounteten Pfad
	 * 
	 * @return angemountetes Datenverzeichnis, voller Pfad
	 */
	public String getRemotepath() {
		return this.remotepath;
	}

	/**
	 * Setze die PID des lokalen Webschnittes (oder null)
	 * 
	 * @param versionPid die PID des lokalen Webschnittes (oder null)
	 */
	public void setVersionPid(String versionPid) {
		this.versionPid = versionPid;
	}

	/**
	 * Setze die PID des Webschnittes auf dem Quellserver, von dem importiert wird
	 * 
	 * @param pid die PID des Webschnittes auf dem Quellserver, von dem importiert
	 *          wird
	 */
	public void setQuellserverWebschnittPid(String pid) {
		this.quellserverWebschnittPid = pid;
	}

	/**
	 * Setze Flag, ob nach erfolgreichem Import der Webschnitt auf dem Quellserver
	 * gelöscht werden soll
	 * 
	 * @param flag Flag, ob nach erfolgreichem Import der Webschnitt auf dem
	 *          Quellserver gelöscht werden soll
	 */
	public void setDeleteQuellserverWebschnitt(Boolean flag) {
		this.deleteQuellserverWebschnitt = flag;
	}

	@Override
	public void run() {
		try {
			/*
			 * Kopieren von remotepath in lokales Datenverzeichnis localpath; alle
			 * Inhalte; cp -pr <angmountetes
			 * Verzeichnis>/quellserverWebpagePid/datetime/* localpath
			 */
			WebgatherLogger.info("Kopiere: cp -pr " + remotepath + " " + localpath);
			File localfile = new File(localpath);
			File remotefile = new File(remotepath);
			copyFolder(remotefile.toPath(), localfile.toPath());
			WebgatherLogger.info("Fertig kopiert!");

			/*
			 * die DataUrl beginnt mit http:// und beschreibt eine URL, unter der man
			 * das WARC-Archiv über HTTP herunterladen kann
			 */
			String localDataUrl = Globals.heritrixData + "/"
					+ conf.getCrawlerSelection() + "-data/" + conf.getName() + "/"
					+ this.datetime + "/" + this.warcFilename + ".warc.gz";
			WebgatherLogger.info("localDataUrl = " + localDataUrl);

			/*
			 * Anlegen einer lokalen WebpageVersion (FedoraObjekt); gleicher Aufruf
			 * für alle Crawler
			 */
			create.createWebpageVersion(node, conf, warcFilename, localfile,
					localDataUrl, versionPid);

			if (this.deleteQuellserverWebschnitt == true) {
				/*
				 * Schließlich wird auch noch der original Webschnitt (auf dem
				 * Quellserver) entfernt
				 */
				WebgatherLogger
						.info("der Webschnitt auf dem Quellserver wird gelöscht.");
				msg = delete.deleteRemoteImported(quellserverWebschnittPid);
				WebgatherLogger.info(msg);
			} else {
				WebgatherLogger
						.info("der Webschnitt auf dem Quellserver bleibt erhalten.");
			}

		} catch (Exception e) {
			WebgatherLogger.error(e.toString());
			throw new RuntimeException(
					"WebpageVersion has not been successfully imported!", e);
		}
	}

	/**
	 * Kopiert rekursiv (also mit Inhalten und Unteverzeichissen) ein Verzeichnis
	 * auf ein anderes Vezeichnis.
	 * 
	 * @param src Quellverzeichnus
	 * @param dest Zielverzeichnis
	 * @throws IOException InputOutout-Ausnahmebehandlung
	 */
	private void copyFolder(Path src, Path dest) throws IOException {
		try (Stream<Path> stream = Files.walk(src)) {
			stream.forEach(
					source -> copy(source, dest.resolve(src.relativize(source))));
		}
	}

	/**
	 * Kopiert EINE Datei oder legt ein Verzeichnis (ohne Inhalte) an.
	 * 
	 * @param source Quelldatei oder -verzeichnisname
	 * @param dest Zieldatei oder -verzeichnisname
	 */
	private void copy(Path source, Path dest) {
		try {
			WebgatherLogger
					.debug("Copying " + source.toString() + " to " + dest.toString());
			if (source.getFileName().toString().endsWith(".warc.gz")) {
				this.warcFilename =
						source.getFileName().toString().replaceAll(".warc.gz$", "");
				WebgatherLogger.info("warcFilename = " + this.warcFilename);
			}
			Files.copy(source, dest, copyOptions);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}
