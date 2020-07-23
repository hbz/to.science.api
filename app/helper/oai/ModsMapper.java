/*
 * Copyright 2019 hbz NRW (http://www.hbz-nrw.de/)
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
package helper.oai;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import java.lang.Process;
import java.lang.ProcessBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import archive.fedora.XmlUtils;
import models.Node;

/**
 * @author reimer@hbz-nrw.de
 *
 */

public class ModsMapper {

	Node node;
	String uri;
	Document doc = null;

	/**
	 * @param node
	 * @param uri
	 */
	public ModsMapper(Node node, String uri) {
		this.node = node;
		this.uri = uri;
	}

	public ModsMapper(JsonLDMapper jMapper) {
		getData();
	}

	/**
	 * remove \r\n (return, newline), probably originating from copy&paste
	 * 
	 * @param value to be scrubbed
	 *
	 */

	public static String scrub(String value) {
		return value.replaceAll("\r\n", " ");
	}

	/**
	 * @return
	 */
	public String getData() {

		JsonNode jNode = new ObjectMapper().valueToTree(node.getLd2());
		JsonLDMapper jMapper = new JsonLDMapper(jNode);

		DocumentBuilderFactory.newInstance();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.isValidating();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		doc = docBuilder.newDocument();

		Element modsCollection = doc.createElement("modsCollection");
		modsCollection.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
		modsCollection.setAttribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		modsCollection.setAttribute("xmlns", "http://www.loc.gov/mods/v3");
		modsCollection.setAttribute("xsi:schemaLocation",
				"http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-3.xsd");

		Element mods = doc.createElement("mods");
		mods.setAttribute("version", "3.3");
		modsCollection.appendChild(mods);

		Hashtable<String, String> jsonElementList = new Hashtable<>();

		// generate titleInfo
		ArrayList<Hashtable<String, String>> jemList =
				jMapper.getElement("root.title");
		for (int i = 0; i < jemList.size(); i++) {
			Element titleInfo = doc.createElement("titleInfo");
			Element title = doc.createElement("title");
			title.appendChild(
					doc.createTextNode(scrub(jemList.get(i).get("root.title"))));
			titleInfo.appendChild(title);
			if (i > 0) {
				titleInfo.setAttribute("type", "alternative");
			}
			mods.appendChild(titleInfo);
		}

		// generate name
		jemList = jMapper.getElement("root.creator");
		for (int i = 0; i < jemList.size(); i++) {
			Element name = doc.createElement("name");
			name.setAttribute("type", "personal");
			Element namePart = doc.createElement("namePart");
			namePart.appendChild(doc.createTextNode(jemList.get(i).get("prefLabel")));
			name.appendChild(namePart);
			mods.appendChild(name);
		}

		// generate abstract
		jemList = jMapper.getElement("root.abstractText");
		for (int i = 0; i < jemList.size(); i++) {
			Element abstracttext = doc.createElement("abstract"); // 'abstract' seems
																														// to be a reserved
																														// word in java
			abstracttext.appendChild(
					doc.createTextNode(scrub(jemList.get(i).get("root.abstractText"))));
			mods.appendChild(abstracttext);
		}
		// generate typeOfResource
		// TODO: zur Zeit wird alles als "text" behandelt, es gibt aber auch video
		// etc.
		Element typeOfResource = doc.createElement("typeOfResource");
		typeOfResource.appendChild(doc.createTextNode("text"));
		mods.appendChild(typeOfResource);

		// generate genre
		Hashtable<String, String> genres = new Hashtable<String, String>();
		genres.put("Buchkapitel", "book");
		genres.put("article", "academic journal");
		genres.put("Kongressbeitrag", "conference publication");

		jemList = jMapper.getElement("root");
		for (int i = 0; i < jemList.size(); i++) {
			if (jemList.get(i).containsKey("contentType")) {
				String contentType = jemList.get(i).get("contentType");
				if (genres.containsKey(contentType)) {
					Element genre = doc.createElement("genre");
					genre.appendChild(doc.createTextNode(genres.get(contentType)));
					mods.appendChild(genre);
				}
			}
		}

		// generate language
		jemList = jMapper.getElement("root.language");
		for (int i = 0; i < jemList.size(); i++) {
			Element language = doc.createElement("language");
			language.appendChild(
					doc.createTextNode(jemList.get(i).get("@id").substring(38)));
			mods.appendChild(language);
		}

		// generate subjects
		// TODO: use numerical value instead of label
		jemList = jMapper.getElement("root.ddc");
		for (int i = 0; i < jemList.size(); i++) {
			Element classification = doc.createElement("classification");
			classification
					.appendChild(doc.createTextNode(jemList.get(i).get("prefLabel")));
			classification.setAttribute("authority", "ddc");
			mods.appendChild(classification);
		}

		jemList = jMapper.getElement("root.subject");
		Element subject = doc.createElement("subject");
		for (int i = 0; i < jemList.size(); i++) {
			Element topic = doc.createElement("topic");
			topic.appendChild(doc.createTextNode(jemList.get(i).get("prefLabel")));
			subject.appendChild(topic);
		}
		mods.appendChild(subject);

		// generate identifier
		jemList = jMapper.getElement("root");
		for (int i = 0; i < jemList.size(); i++) {
			if (jemList.get(i).containsKey("@id")) {
				Element purl = doc.createElement("identifier");
				purl.appendChild(doc.createTextNode(
						"https://frl.publisso.de/" + jemList.get(i).get("@id")));
				purl.setAttribute("type", "purl");
				mods.appendChild(purl);
				// citekey
				// Element identifier_citekey = doc.createElement("identifier");
				// identifier_citekey.appendChild(doc.createTextNode(jemList.get(i).get("@id")));
				// identifier_citekey.setAttribute("type", "citekey");
				// mods.appendChild(identifier_citekey);
				mods.setAttribute("ID", jemList.get(i).get("@id"));
			}
			if (jemList.get(i).containsKey("urn")) {
				Element urn = doc.createElement("identifier");
				urn.appendChild(doc.createTextNode(jemList.get(i).get("urn")));
				urn.setAttribute("type", "urn");
				mods.appendChild(urn);
			}
		}
		// generate DOI
		jemList = jMapper.getElement("root.bibo:doi");
		for (int i = 0; i < jemList.size(); i++) {
			Element doi = doc.createElement("identifier");
			doi.appendChild(doc.createTextNode(jemList.get(i).get("root.bibo:doi")));
			doi.setAttribute("type", "doi");
			mods.appendChild(doi);
		}

		doc.appendChild(modsCollection);

		return archive.fedora.XmlUtils.docToString(doc);

	}

	/**
	 * return bibtex format of metadata
	 * 
	 * @param format
	 * @return
	 * 
	 */
	public String xml2any(String format) {

		// bibutil commands
		Hashtable<String, String> bibutils = new Hashtable<>();
		bibutils.put("bib", "/usr/bin/xml2bib");
		bibutils.put("end", "/usr/bin/xml2end");
		bibutils.put("ris", "/usr/bin/xml2ris");

		String mods_xml = XmlUtils.docToString(doc);
		String command = "echo '" + mods_xml + "' | " + bibutils.get(format);
		String output = "";
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", command);
			final Process p = pb.start();
			BufferedReader br =
					new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				output = output + "\n" + line;
			}
		} catch (Exception ex) {
			System.out.println(ex);
		}

		return output;
	}

}
