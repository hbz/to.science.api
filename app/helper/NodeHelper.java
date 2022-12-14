/*
 * Copyright 2022 hbz NRW (http://www.hbz-nrw.de/)
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

import org.json.JSONArray;
import org.json.JSONObject;

import actions.Modify;
import actions.Read;
import models.Globals;
import models.Node;
import org.eclipse.rdf4j.rio.RDFFormat;

public class NodeHelper {

	/**
	 * This method calculates the number of ocurrences of the specific word
	 * "encodingFormat" in a String
	 * 
	 * @param lrmiData of a ParenNode
	 * @return Number of searched word(encodingFormat) in String
	 */
	public int calculNumberOfEncodingObjectsInLRMIConent(String lrmiData) {
		if (lrmiData != null) {
			String[] count = lrmiData.split("encodingFormat");
			return count.length - 1;
		} else {
			return -1;
		}

	}

	/**
	 * Method removes an EncodingObject from AmbContent of ParentNode
	 * 
	 * @param childNode: The node whose reference (Encoding-Object) must be
	 *          deleted
	 * @param ambContentOfParentNode in String: the AmbContent of the Parent Node
	 * @return String value: the new AmbContent of ParentNode without the encoding
	 *         object (deleted) or the old ambContentOfParentNode at fail
	 * 
	 */
	public String deleteEncodingObjectFromAmbContentOfParenNode(Node childNode,
			String ambContentOfParentNode) {
		play.Logger.debug(
				"deleteEncodingObjectFromAmbContentOfParenNode wird geloggt fuer pid ="
						+ childNode.getPid());
		JSONObject jsonAmbContent = null;
		JSONArray jsonEncodingObjects = null;
		JSONObject jsonEncodingObject = null;
		String check;

		if (childNode.getParentPid() == null || ambContentOfParentNode == null
				|| ambContentOfParentNode.isEmpty()) {
			// case fail. Return the old AmbContent of the parent node ()
			return ambContentOfParentNode;
		}

		if (!ambContentOfParentNode.contains("encoding")
				|| !ambContentOfParentNode.contains("/" + childNode + "/")) {
			// case fail. Return the old AmbContent of the parent node ()
			return ambContentOfParentNode;
		}

		// String contains exact e.g. /orca:xxxxx/
		// Convert the ambContent from string to json
		jsonAmbContent = new JSONObject(ambContentOfParentNode);

		// Get all encoding objects in json
		jsonEncodingObjects = jsonAmbContent.getJSONArray("encoding");

		for (int i = 0; i < jsonEncodingObjects.length(); i++) {
			jsonEncodingObject = jsonEncodingObjects.getJSONObject(i);
			check = jsonEncodingObject.toString();
			if (check != null && check.contains("/" + childNode + "/")) {
				jsonEncodingObjects.remove(i);
			}
		}
		jsonAmbContent.put("encoding", jsonEncodingObjects);
		// case successful
		// Return the new AmbContent without the deleted Encoding-Object
		return jsonAmbContent.toString();

	}

	/**
	 * Method removes a hasPart-Object(child) from to.science content of
	 * ParentNode
	 * 
	 * @param childNode node whose reference must be deleted (hasPartObject)
	 * @param toScienceContent
	 * @return String of a new to.science content of parentNode or the old
	 *         toScienceContent at fail
	 */
	public String deleteHasPrtObjectFromToScienceContent(Node childNode,
			String toScienceContent) {
		play.Logger
				.debug("deleteHasPrtObjectFromToScienceContent wird geloggt fuer pid ="
						+ childNode.getPid());
		JSONObject jsonToScienceContent = null;
		JSONArray jsonHasPartObjects = null;
		JSONObject jsonHasPartObject = null;
		String check;

		if (childNode.getParentPid() == null || toScienceContent == null
				|| toScienceContent.isEmpty()) {
			// fail case. Return the old to.science content of the parent node ()
			return toScienceContent;
		}

		if (!toScienceContent.contains("hasPart")
				|| !toScienceContent.contains("\"" + childNode + "\"")) {
			// fail case. Return the old to.science content of the parent node ()
			return toScienceContent;
		}

		// String contains exact e.g. "orca:xxxxx"
		// convert the ambContent from string to json
		jsonToScienceContent = new JSONObject(toScienceContent);

		// get all hasPart objects in json
		jsonHasPartObjects = jsonToScienceContent.getJSONArray("hasPart");

		for (int i = 0; i < jsonHasPartObjects.length(); i++) {
			jsonHasPartObject = jsonHasPartObjects.getJSONObject(i);
			check = jsonHasPartObject.toString();
			if (check != null && check.contains("\"" + childNode + "\"")) {
				jsonHasPartObjects.remove(i);
			}
		}
		jsonToScienceContent.put("hasPart", jsonHasPartObjects);

		// case successful
		// Return the new to.science content without the deleted hasPart object
		return jsonToScienceContent.toString();
	}

	/**
	 * Method edits the exchanged encoding object The pid of the old encoding
	 * object and the new one is the same.
	 * 
	 * @param neuChildNode Node of the child (alt = neu)
	 * @param ambContentOfParentNode
	 * @return a LrmiContent of the ParentNode with the added new Encoding-Object
	 */
	public String exchangeEncodingObjects(Node neuChildNode,
			String ambContentOfParentNode) {

		JSONObject jsonAmbContent = null;
		JSONArray jsonEncodingObjects = null;
		JSONObject jsonEncodingObject = null;
		String check;

		if (neuChildNode.getParentPid() == null || ambContentOfParentNode == null
				|| ambContentOfParentNode.isEmpty()) {
			// case fail. Return the old AmbContent of the parent node ()
			return ambContentOfParentNode;
		}

		if (!ambContentOfParentNode.contains("encoding")
				|| !ambContentOfParentNode.contains("/" + neuChildNode + "/")) {
			// case fail. Return the old AmbContent of the parent node ()
			return ambContentOfParentNode;
		}

		// String contains exact e.g. /orca:xxxxx/
		// convert the ambContent from string to json
		jsonAmbContent = new JSONObject(ambContentOfParentNode);

		// get all encoding objects in json
		jsonEncodingObjects = jsonAmbContent.getJSONArray("encoding");

		for (int i = 0; i < jsonEncodingObjects.length(); i++) {
			jsonEncodingObject = jsonEncodingObjects.getJSONObject(i);
			check = jsonEncodingObject.toString();
			if (check != null && check.contains("/" + neuChildNode + "/")) {
				// size will just be edited, contentUrl and type remain in
				// Encoding-Object
				jsonEncodingObject.put("size", neuChildNode.getFileSizeAsString());
				// encodingFormat will just be edited, contentUrl and type remain
				jsonEncodingObject.put("encodingFormat", neuChildNode.getMimeType());
			}
		}
		jsonAmbContent.put("encoding", jsonEncodingObjects);
		// case successful
		// Return the new AmbContent without the deleted Encoding-Object
		return jsonAmbContent.toString();
	}

	/**
	 * Method gets the value between "" in a string
	 * 
	 * @param clientRequest = request().body().asText()
	 * @return the value between "" = the new title of ChildNode
	 */
	public String getTitleFromClientRequest(String s) {

		return s.substring(s.indexOf("\"") + 1, s.lastIndexOf("\""));

	}

	/**
	 * Method refreshes the data streams lrmi & json2
	 * 
	 * @param node
	 */
	public void refreshDataStreamsOfNode(Node node) {
		RDFFormat format = RDFFormat.NTRIPLES;
		// node.setMetadata2(content);
		play.Logger.debug(
				"refreshDataStreamsOfNode wird geloggt fuer pid =" + node.getPid());
		new Modify().updateLobidify2AndEnrichMetadata(node.getPid(),
				node.getMetadata(archive.fedora.Vocabulary.metadata2));

		new Modify().updateLrmifyAndEnrichMetadata(node.getPid(), format,
				node.getMetadata(archive.fedora.Vocabulary.metadata2));

	}

}
