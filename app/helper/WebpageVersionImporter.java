/**
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

	private String localpath = null;
	private String remotepath = null;
	private CopyOption[] copyOptions = new CopyOption[] {
			StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES };

	private static final Logger.ALogger WebgatherLogger =
			Logger.of("webgatherer");

	/**
	 * ein Konstruktor für diese Klasse
	 */
	public WebpageVersionImporter() {

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
			// copy(remotefile.toPath(), localfile.toPath());
			copyFolder(remotefile.toPath(), localfile.toPath());
			WebgatherLogger.info("Fertig kopiert!");

		} catch (Exception e) {
			WebgatherLogger.error(e.toString());
			throw new RuntimeException(
					"WebpageVersion has not been successfully imported!", e);
		}
	}

	/**
	 * Kopiert rekursiv (also mit Inhalten und Unteverzeichissen) ein Verzeichnis
	 * auf ein anderes Vezeichnis. Überschreibt, falls schon exsitiert.
	 * 
	 * @param src Quellverzeichnus
	 * @param dest Zielverzeichnis
	 * @throws IOException InputOutout-Ausnahmebehandlung
	 */
	public void copyFolder(Path src, Path dest) throws IOException {
		try (Stream<Path> stream = Files.walk(src)) {
			stream.forEach(
					source -> copy(source, dest.resolve(src.relativize(source))));
		}
	}

	private void copy(Path source, Path dest) {
		try {
			Files.copy(source, dest, copyOptions);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}
