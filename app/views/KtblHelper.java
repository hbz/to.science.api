package views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import helper.JsonMdLoader;
import models.Node;
import play.Logger;
import models.Globals;
import actions.Modify;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;

public class KtblHelper {

	/**
	 * @param node
	 * @param key
	 * @return
	 */
	public static List<String> getAssociatedDataset(Node node, String key) {
		String mdStream = getTosJson(node);
		List<String> valueList = new ArrayList<>();
		if (mdStream != null) {
			try {
				JsonNode jnAll = new ObjectMapper().readTree(mdStream);
				JsonNode jn = jnAll.findValue(key);
				Iterator<JsonNode> jIt = jn.elements();
				while (jIt.hasNext()) {
					JsonNode nextNode = jIt.next();
					valueList.add(nextNode.asText().replace("_", " ").replace("\"", ""));

				}
				return valueList;
			} catch (IOException e) {
				play.Logger.warn(e.getMessage());
			}
		}
		return null;
	}

	/**
	 * Get complete toscience.json as String
	 * 
	 * @param node
	 * @return
	 */
	public static String getTosJson(Node node) {
		JsonMdLoader tos = new JsonMdLoader(node, "toscience");
		return tos.getJsonAsString();
	}

	/**
	 * Get complete toscience.json as String
	 * 
	 * @param node
	 * @return
	 */
	public static String getKtblJson(Node node) {
		JsonMdLoader ktbl = new JsonMdLoader(node.getPid(), "ktbl");
		return ktbl.getJsonAsString();
	}

	/**
	 * Get content of Json creator-Array from toscience.json Method to provide
	 * field "creator" (persons) in correct sequence and with roles from json
	 * instead of N-triples
	 * 
	 * @param node
	 * @return List
	 */
	public static List<String> getCreators(Node node) {
		String mdStream = getTosJson(node);
		List<String> creatorsList = new ArrayList<>();
		JsonNode jNode = null;
		try {
			JsonNode jn = new ObjectMapper().readTree(mdStream);
			jNode = jn.findValue("creator");

			List<JsonNode> cardNode = jNode.findParents("prefLabel");
			for (int i = 0; i < cardNode.size(); i++) {
				String card = cardNode.get(i).findValues("prefLabel").toString() + "; "
				// + cardNode.get(i).findValues("@id").toString() + "; " +
						+ cardNode.get(i).findValues("role").toString();
				card = card.replace("[", "").replace("]", "").replace("\"", "")
						.replace("_", " ").replace(",", ", ");
				creatorsList.add(card);
			}
			return creatorsList;
		} catch (Exception e) {
			play.Logger.warn(e.getMessage());
		}
		return null;
	}

	/**
	 * Get content of Json contributor-Array from toscience.json Method to provide
	 * field "contributor" (persons) in correct sequence and with roles from json
	 * instead of N-triples
	 * 
	 * @param node
	 * @return List
	 */
	public static List<String> getContributors(Node node) {
		String mdStream = getTosJson(node);
		List<String> contributorsList = null;
		JsonNode jNode = null;
		JsonNode jn = null;
		try {
			jn = new ObjectMapper().readTree(mdStream);
			jNode = jn.findValue("contributor");
		} catch (IOException e) {
			play.Logger.warn(e.getMessage());
		}

		if (jNode != null && !jNode.isNull()) {
			contributorsList = new ArrayList<>();
			List<JsonNode> cardNode = jNode.findParents("prefLabel");
			for (int i = 0; i < cardNode.size(); i++) {
				String card = cardNode.get(i).findValues("prefLabel").toString() + "; "
				// + cardNode.get(i).findValues("@id").toString() + "; "
						+ cardNode.get(i).findValues("role").toString();
				contributorsList.add(card.replace("[", "").replace("]", "")
						.replace("\"", "").replace("_", " ").replace(",", ", "));
			}
		}
		return contributorsList;
	}

	/**
	 * Get content of Json other-Array from toscience.json Method to provide field
	 * "others" (persons) in correct sequence and with roles from json instead of
	 * N-triples
	 * 
	 * @param node
	 * @return List
	 */
	public static List<String> getOthers(Node node) {
		String mdStream = getTosJson(node);
		List<String> othersList = new ArrayList<>();
		JsonNode jNode = null;
		try {
			JsonNode jn = new ObjectMapper().readTree(mdStream);
			jNode = jn.findValue("other");

			List<JsonNode> cardNode = jNode.findParents("prefLabel");
			for (int i = 0; i < cardNode.size(); i++) {
				String card = cardNode.get(i).findValues("prefLabel").toString() + "; "
				// + cardNode.get(i).findValues("@id").toString() + "; "
						+ cardNode.get(i).findValues("role").toString();
				othersList.add(card.replace("[", "").replace("]", "").replace("\"", "")
						.replace("_", " ").replace(",", ", "));
			}
			return othersList;
		} catch (Exception e) {
			play.Logger.warn(e.getMessage());
		}
		return null;
	}

	/**
	 * Get content of Json other-Array from toscience.json
	 * 
	 * @param node
	 * @param key
	 * @return List
	 */
	public static String getToscienceLiteralValue(Node node, String key) {
		String literalValue = null;
		String mdStream = getTosJson(node);
		if (mdStream != null) {
			try {
				JsonNode jn = new ObjectMapper().readTree(mdStream);
				literalValue = jn.findValue(key).toString().replace("_", " ")
						.replace("[", "").replace("]", "").replace("\"", "");
			} catch (Exception e) {
				play.Logger.warn(e.getMessage());
			}
		}
		return literalValue;
	}

	/**
	 * @param node
	 * @param key
	 * @return
	 */
	public static List<String> getToscienceArrayValues(Node node, String key) {
		String mdStream = getTosJson(node);
		List<String> valueList = null;
		if (mdStream != null) {
			try {
				JsonNode jnAll = new ObjectMapper().readTree(mdStream);
				JsonNode jn = jnAll.findValue(key);
				if (jn != null && !jn.isNull()) {
					valueList = new ArrayList<>();

					Iterator<JsonNode> jIt = jn.elements();
					while (jIt.hasNext()) {
						JsonNode nextNode = jIt.next();
						valueList.add(nextNode.asText().replace("\\u2019", "'"));
					}
				}

			} catch (IOException e) {
				play.Logger.warn(e.getMessage());
			}
		}
		return valueList;
	}

	/**
	 * Get content of an Array of objects with prefLabel from toscience.json
	 * 
	 * @param node
	 * @param key
	 * @return List
	 */
	public static List<Hashtable<String, String>> getToscienceObjectArray(
			Node node, String key) {
		List<Hashtable<String, String>> valueList = new ArrayList<>();
		String mdStream = getTosJson(node);
		JsonNode jNode = null;
		try {
			JsonNode jn = new ObjectMapper().readTree(mdStream);
			jNode = jn.findValue(key);

			List<JsonNode> cardNode = jNode.findParents("prefLabel");
			for (int i = 0; i < cardNode.size(); i++) {
				Hashtable<String, String> valueHash = new Hashtable<>();
				valueHash.put("prefLabel", cardNode.get(i).findValues("prefLabel")
						.toString().replace("[", "").replace("]", "").replace("\"", ""));
				valueHash.put("id", cardNode.get(i).findValues("@id").toString()
						.replace("[", "").replace("]", "").replace("\"", ""));
				if (cardNode.get(i).has("role")) {
					valueHash.put("roles",
							cardNode.get(i).findValues("role").toString().replace("[", "")
									.replace("]", "").replace("\"", "").replace(",", ", ")
									.replace("_", " "));
				}
				valueList.add(valueHash);
			}
			return valueList;
		} catch (Exception e) {
			play.Logger.warn(e.getMessage());
		}
		return null;
	}

	/**
	 * Tests if Metadata Stream is available from Fedora subsystem
	 * 
	 * @param pid
	 * @param mdFormat
	 * @return
	 */
	public static boolean mdStreamExists(String pid, String mdFormat) {
		JsonMdLoader jMd = new JsonMdLoader(pid, mdFormat);
		return jMd.datastreamExists();
	}

	/**
	 * Get content of Json other-Array from toscience.json
	 * 
	 * @param node
	 * @return List
	 */
	public static String getLivestockCategory(Node node) {
		String livestockCat = null;
		String mdStream = getKtblJson(node);
		if (mdStream != null) {
			try {
				JsonNode jn = new ObjectMapper().readTree(mdStream);
				livestockCat = jn.findValue("livestock_category").toString()
						.replace("_", " ").replace("\"", "");
			} catch (Exception e) {
				play.Logger.warn(e.getMessage());
			}
		}
		return livestockCat;
	}

	/**
	 * Get content of Json other-Array from toscience.json
	 * 
	 * @param node
	 * @return List
	 */
	public static String getHousingSystems(Node node) {
		String value = null;
		String mdStream = getKtblJson(node);
		if (mdStream != null) {
			try {
				JsonNode jn = new ObjectMapper().readTree(mdStream);
				value = jn.findValue("housing_systems").toString().replace("_", " ")
						.replace("\"", "");
			} catch (Exception e) {
				play.Logger.warn(e.getMessage());
			}
		}
		return value;
	}

	/**
	 * Get content of Json other-Array from toscience.json
	 * 
	 * @param node
	 * @return List
	 */
	public static String getTestDesign(Node node) {
		String value = null;
		String mdStream = getKtblJson(node);
		if (mdStream != null) {
			try {
				JsonNode jn = new ObjectMapper().readTree(mdStream);
				value = jn.findValue("test_design").toString().replace("_", " ")
						.replace("\"", "");
			} catch (Exception e) {
				play.Logger.warn(e.getMessage());
			}
		}
		return value;
	}

	/**
	 * Get content of Json other-Array from toscience.json
	 * 
	 * @param node Object
	 * @return List
	 */
	public static String getLivestockProduction(Node node) {
		String value = null;
		String mdStream = getKtblJson(node);
		if (mdStream != null) {
			try {
				JsonNode jn = new ObjectMapper().readTree(mdStream);
				value = jn.findValue("livestock_production").toString()
						.replace("_", " ").replace("\"", "");
			} catch (Exception e) {
				play.Logger.warn(e.getMessage());
			}
		}
		return value;
	}

	/**
	 * Get content of Json other-Array from toscience.json
	 * 
	 * @param node
	 * @return List
	 */
	public static String getVentilationSystem(Node node) {
		String value = null;
		String mdStream = getKtblJson(node);
		if (mdStream != null) {
			try {
				JsonNode jn = new ObjectMapper().readTree(mdStream);
				value = jn.findValue("ventilation_system").toString().replace("_", " ")
						.replace("\"", "");
			} catch (Exception e) {
				play.Logger.warn(e.getMessage());
			}
		}
		return value;
	}

	/**
	 * Get content of Json other-Array from toscience.json
	 * 
	 * @param node
	 * @return List
	 */
	public static String getProjectTitle(Node node) {
		String value = null;
		String mdStream = getKtblJson(node);
		if (mdStream != null) {
			try {
				JsonNode jn = new ObjectMapper().readTree(mdStream);
				value = jn.findValue("project_title").toString().replace("_", " ")
						.replace("\"", "").replace("emim", "EmiM")
						.replace("emidat", "EmiDaT");
			} catch (IOException e) {
				play.Logger.warn(e.getMessage());
			}
		}
		return value;
	}

	/**
	 * @param node
	 * @param key
	 * @return
	 */
	public static List<String> getKtblArrayValues(Node node, String key) {
		String mdStream = getKtblJson(node);
		List<String> valueList = new ArrayList<>();
		if (mdStream != null) {
			try {
				JsonNode jnAll = new ObjectMapper().readTree(mdStream);
				JsonNode jn = jnAll.findValue(key);
				Iterator<JsonNode> jIt = jn.elements();
				while (jIt.hasNext()) {
					JsonNode nextNode = jIt.next();
					valueList.add(nextNode.asText().replace("_", " ").replace("\"", ""));

				}
				return valueList;
			} catch (Exception e) {
				play.Logger.warn(e.getMessage());
			}
		}
		return null;
	}

	/**
	 * @param node
	 * @param key
	 * @return
	 */
	public static List<String> getKtblEmissions(Node node, String key) {
		String mdStream = getKtblJson(node);
		List<String> valueList = new ArrayList<>();
		if (mdStream != null) {
			try {
				JsonNode jnAll = new ObjectMapper().readTree(mdStream);
				JsonNode jn = jnAll.findValue(key);
				Iterator<JsonNode> jIt = jn.elements();
				while (jIt.hasNext()) {
					JsonNode nextNode = jIt.next();
					valueList.add(nextNode.asText().toUpperCase().replace("_", " ")
							.replace("\"", "").replace("3", "₃").replace("2", "₂")
							.replace("4", "₄").replace("DOUR", "dour"));

				}
				return valueList;
			} catch (IOException e) {
				play.Logger.warn(e.getMessage());
			}
		}
		return null;
	}

	/**
	 * @param mdStream Object
	 * @return String
	 */
	public static String trimText(Object mdStream) {

		return mdStream.toString().trim().replace("&quot;", "\"");
	}

	/**
	 * Take a string and UperCase the very first Letter
	 * 
	 * @param value String
	 * @return String
	 */
	// TODO: fix implementation
	public static String capitalizeFirst(String value) {

		return value.substring(0, 0).toUpperCase() + value.substring(1);
	}

	public static String getRecordingPeriod(Node node) {
		String output = null;
		String mdStream = getKtblJson(node);
		if (mdStream != null) {
			try {
				JsonNode jn = new ObjectMapper().readTree(mdStream);
				output = jn.findValue("recordingPeriod").toString().replace("_", " ")
						.replace("\"", "");

			} catch (Exception e) {
				play.Logger.warn(e.getMessage());
			}
		}
		return output;
	}

	public static String getLabel(Node node) {
		String label = null;
		try {
			JSONObject jo = new JSONObject(node.getMetadata("toscience"));
			if (!jo.has("@id") || jo.get("@id").toString().isEmpty()) {
				String id = Globals.urnbase + node.getPid();
				jo.put("@id", id);
				node.setMetadata("toscience", jo.toString());
				new Modify().updateMetadata("toscience", node, jo.toString());
			}
			label = jo.get("@id").toString();

		} catch (Exception e) {
			play.Logger.debug("Exception in getLabel(): " + e);
		}

		return label;
	}

	public static String getDoi(Node node) {
		try {
			JSONObject jo = new JSONObject(node.getMetadata("toscience"));
			if (node.hasDoi() && !jo.has("doi")) {
				jo.put("doi", node.getDoi());
				node.setMetadata("toscience", jo.toString());
				new Modify().updateMetadata("toscience", node, jo.toString());
			}
		} catch (Exception e) {
			play.Logger.debug("Exception in getDoi():" + e);
		}

		return node.getDoi();
	}

}
