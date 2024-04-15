/**
 * 
 */
package helper.ktbl;

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
 * 
 */
public class JsonMdLoader {
	private String pid;
	private String jsonAsString;
	private Node node;

	/**
	 * Constructor
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
			InputStreamReader InStReader = new InputStreamReader(stream);
			BufferedReader bReader = new BufferedReader(InStReader);
			StringBuilder jsonStringBuilder = new StringBuilder();
			result = jsonStringBuilder.toString();
		} catch (Exception e) {
			play.Logger.warn(node.getPid() + " has no " + mdFormat + " Metadata!");
			play.Logger.error("", e);
		}
		return result;
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
