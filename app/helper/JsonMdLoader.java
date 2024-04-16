/**
 * 
 */
package helper;

import models.Node;

import static archive.fedora.Vocabulary.metadata1;
import static archive.fedora.Vocabulary.ktbl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.rdf4j.rio.RDFFormat;

/**
 * A class that loads Metadata as json from different sources an provide this
 * metadata in different forms
 */
public class JsonMdLoader {
	private String pid;
	private String jsonAsString;
	private Node node;

	/**
	 * Constructor for getting json metadata from an instance of class Node
	 * 
	 * @param node
	 * @param mdFormat
	 */
	public JsonMdLoader(Node node, String mdFormat) {
		this.node = node;
		jsonAsString = getJsonMd(mdFormat);

	}

	/**
	 * get a String representation from any metadata file saved as json within the
	 * to.science repository
	 * 
	 * @param String fedora's datastream label of the json md file
	 * @return String json representation
	 */
	private String getJsonMd(String mdFormat) {
		String result = null;
		try {
			InputStream stream = new ByteArrayInputStream(
					node.getMetadata(mdFormat).getBytes(StandardCharsets.UTF_8));
			InputStreamReader InStReader = new InputStreamReader(stream, "UTF-8");
			BufferedReader bReader = new BufferedReader(InStReader);
			StringBuilder jsonStringBuilder = new StringBuilder();

			String inputStr;
			while ((inputStr = bReader.readLine()) != null) {
				jsonStringBuilder.append(inputStr);
			}
			result = jsonStringBuilder.toString();
		} catch (Exception e) {
			play.Logger.warn(node.getPid() + " has no " + mdFormat + " Metadata!");
			play.Logger.error("", e);
		}
		return result;
	}

	/**
	 * Get a String representation from a metadata file derived from Node.metadata
	 * 
	 * @param String fedora's datastream label of the json md file
	 * @return String json representation
	 */
	private String getJsonMdfromNode(String mdFormat) {
		return node.getMetadata(mdFormat);
	}

	/**
	 * @return the jsonAsString
	 */
	public String getJsonAsString() {
		return jsonAsString;
	}

	/**
	 * @return the jsonAsString
	 */
	public String getJsonAsString(String mdFormat) {
		return getJsonMd(mdFormat);
	}

	/**
	 * @return the node
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * @param node the node to set
	 */
	public void setNode(Node node) {
		this.node = node;
	}

}
