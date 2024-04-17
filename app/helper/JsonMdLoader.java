/**
 * 
 */
package helper;

import models.Node;
import models.Globals;

import play.mvc.Http.Response;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * A class that loads Metadata as json from different sources an provide this
 * metadata in different forms
 */
public class JsonMdLoader {
	private String pid;
	private String jsonAsString = "";
	private InputStream contentStream;
	private Node node;

	/**
	 * Constructor for getting json metadata from an instance of class Node. A
	 * minor set of MDFormats is applicable for this only
	 * 
	 * @param node
	 * @param mdFormat
	 */
	public JsonMdLoader(Node node, String mdFormat) {
		this.node = node;
		contentStream = getMdContent(node, mdFormat);
		jsonAsString = getJsonMd(contentStream);

	}

	/**
	 * Constructor for getting json metadata from an instance of class Node. A
	 * minor set of MDFormats is applicable for this only
	 * 
	 * @param objPid the pid of the to.science Object
	 * @param mdFormat the MD format
	 */
	public JsonMdLoader(String objPid, String mdFormat) {
		contentStream = getMdContent(objPid, mdFormat);
		jsonAsString = getJsonMd(contentStream);

	}

	/**
	 * get a String representation from any metadata file saved as json within the
	 * to.science repository
	 * 
	 * @param String fedora's datastream label of the json md file
	 * @return String json representation
	 */
	private String getJsonMd(InputStream mdContentStream) {
		String result = null;
		try {
			InputStreamReader InStReader =
					new InputStreamReader(mdContentStream, "UTF-8");
			BufferedReader bReader = new BufferedReader(InStReader);
			StringBuilder jsonStringBuilder = new StringBuilder();

			String inputStr;
			while ((inputStr = bReader.readLine()) != null) {
				jsonStringBuilder.append(inputStr);
			}
			result = jsonStringBuilder.toString();
		} catch (Exception e) {
			play.Logger.warn("InputStream generation for " + node.getPid()
					+ " MD-Content failed!");
		}
		return result;
	}

	private InputStream getMdContent(Node node, String mdFormat) {
		try {
			InputStream stream = new ByteArrayInputStream(
					node.getMetadata(mdFormat).getBytes(StandardCharsets.UTF_8));
			return stream;
		} catch (Exception e) {
			play.Logger
					.warn(node.getPid() + "does not return " + mdFormat + " Metadata!");
		}
		return null;
	}

	/**
	 * Method calls to.science API to get MD content from MD datastream
	 * 
	 * @param objPid
	 * @param mdString
	 * @return
	 */
	private InputStream getMdContent(String objPid, String mdFormat) {
		HttpURLConnection connection = null;
		URL url = null;
		try {
			url = new URL(Globals.fedoraIntern + "/objects/" + objPid
					+ "/datastreams/" + mdFormat + "/content");
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();
			return connection.getInputStream();
		} catch (Exception e) {
			play.Logger.error("Connection to " + url.toString() + " failed");
		}
		return null;
	}

	/**
	 * @return the jsonAsString
	 */
	public String getJsonAsString() {
		return jsonAsString;
	}

}
