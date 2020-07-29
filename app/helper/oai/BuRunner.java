/**
 * 
 */
package helper.oai;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;

import org.w3c.dom.Document;

import models.Node;

/**
 * @author aquast
 *
 */
public class BuRunner {

	/**
	 * Constructor to initialize Hashtable
	 */
	public BuRunner(String format, String modsXml) {
		init();
		this.format = format;
		this.modsXml = modsXml;
	}

	private String format;
	private String exitStateStr = null;
	private String stoutStr = null;

	private Hashtable<String, String> bibutils = new Hashtable<>();
	private String modsXml;
	private String params = "";

	private void init() {
		bibutils.put("bib", "/usr/local/bin/xml2bib");
		bibutils.put("end", "/usr/local/bin/xml2end");
		bibutils.put("ris", "/usr/local/bin/xml2ris");
	}

	/**
	 * @return
	 */
	public String getData() {

		// Enumeration<String> bibEnum = bibutils.keys();

		/*
		 * while(bibEnum.hasMoreElements()) { String format = bibEnum.nextElement();
		 * executeBibUtils(doc, format, params); }
		 */

		executeBibUtils(params);

		return stoutStr;
	}

	/**
	 * <p>
	 * <em>Title: </em>
	 * </p>
	 * <p>
	 * Description: Method creates the command line string with all parameters
	 * given. Then executes the shell command
	 * </p>
	 * 
	 * @param paramString
	 * @param fileName
	 */
	public void executeBibUtils(String params) {

		// Complete execute String
		String programPath = bibutils.get(format);
		String defaultParams = new String("");

		String executeString = new String(
				"echo" + modsXml + " | " + programPath + " " + defaultParams);

		play.Logger.info("The execute String: " + executeString);
		try {
			Process proc = java.lang.Runtime.getRuntime().exec(executeString);
			int exitState = proc.waitFor();
			InputStream stout = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(stout);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			StringBuffer lineBuffer = new StringBuffer();
			while ((line = br.readLine()) != null) {
				lineBuffer.append(line + "\n");
			}
			play.Logger.info("STOUT: " + lineBuffer.toString());
			play.Logger.info("Exit State: " + exitState);
			stoutStr = lineBuffer.toString();
			exitStateStr = Integer.toString(exitState);
		} catch (Exception Exc) {
			play.Logger.error(Exc.toString());
		}

	}

	/**
	 * @return String
	 */
	public String getStoutStr() {
		return stoutStr;
	}

	/**
	 * @return String
	 */
	public String getExitStateStr() {
		return exitStateStr;
	}

}
