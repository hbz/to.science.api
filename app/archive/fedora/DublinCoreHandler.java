/*
 * Copyright 2012 hbz NRW (http://www.hbz-nrw.de/)
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
package archive.fedora;

import helper.HttpArchiveException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import models.Pair;
import models.Node;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.request.GetDatastreamDissemination;
import com.yourmediashelf.fedora.client.request.ModifyDatastream;
import com.yourmediashelf.fedora.client.response.FedoraResponse;

/**
 * @author Jan Schnasse schnasse@hbz-nrw.de
 * 
 */
public class DublinCoreHandler {

	static void readFedoraDcToNode(Node node) throws FedoraClientException {

		FedoraResponse response =
				new GetDatastreamDissemination(node.getPid(), "DC").download(true)
						.execute();
		try (InputStream ds = response.getEntityInputStream()) {
			readDcToNode(node, ds, "dc");
		} catch (IOException e) {
			throw new FedoraClientException("", e);
		}
	}

	/**
	 * @param node dc stream will be added to this node
	 * @param ds stream containing xml dc data
	 * @param ns namespace of the dc
	 */
	static void readDcToNode(Node node, InputStream ds, String ns) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setExpandEntityReferences(false);
		factory.setIgnoringElementContentWhitespace(true);

		try {
			DocumentBuilder docBuilder = factory.newDocumentBuilder();

			Document doc = docBuilder.parse(new BufferedInputStream(ds)); // TODO
			// Correct?
			// UTF-8?
			Element root = doc.getDocumentElement();
			root.normalize();

			NodeList contributer = root.getElementsByTagName(ns + ":contributer");
			NodeList coverage = root.getElementsByTagName(ns + ":coverage");
			NodeList creator = root.getElementsByTagName(ns + ":creator");
			NodeList date = root.getElementsByTagName(ns + ":date");
			NodeList description = root.getElementsByTagName(ns + ":description");
			NodeList format = root.getElementsByTagName(ns + ":format");
			NodeList identifier = root.getElementsByTagName(ns + ":identifier");
			NodeList language = root.getElementsByTagName(ns + ":language");
			NodeList publisher = root.getElementsByTagName(ns + ":publisher");
			NodeList rights = root.getElementsByTagName(ns + ":rights");
			NodeList source = root.getElementsByTagName(ns + ":source");
			NodeList subject = root.getElementsByTagName(ns + ":subject");
			NodeList title = root.getElementsByTagName(ns + ":title");
			NodeList type = root.getElementsByTagName(ns + ":type");

			if (contributer != null && contributer.getLength() != 0) {
				node.dublinCoreData.setContributor(new Vector<String>());
				for (int i = 0; i < contributer.getLength(); i++) {
					node.dublinCoreData.addContributor(transformFromXMLEntity(
							contributer.item(i).getTextContent().trim()));
				}
			}
			if (coverage != null && coverage.getLength() != 0) {
				node.dublinCoreData.setCoverage(new Vector<String>());
				for (int i = 0; i < coverage.getLength(); i++) {
					node.dublinCoreData.addCoverage(
							transformFromXMLEntity(coverage.item(i).getTextContent().trim()));
				}
			}
			if (creator != null && creator.getLength() != 0) {
				node.dublinCoreData.setCreator(new Vector<String>());
				for (int i = 0; i < creator.getLength(); i++) {
					node.dublinCoreData.addCreator(
							transformFromXMLEntity(creator.item(i).getTextContent().trim()));
				}
			}
			if (date != null && date.getLength() != 0) {
				node.dublinCoreData.setDate(new Vector<String>());
				for (int i = 0; i < date.getLength(); i++) {
					node.dublinCoreData.addDate(
							transformFromXMLEntity(date.item(i).getTextContent().trim()));
				}
			}
			if (description != null && description.getLength() != 0) {
				node.dublinCoreData.setDescription(new Vector<String>());
				for (int i = 0; i < description.getLength(); i++) {
					node.dublinCoreData.addDescription(transformFromXMLEntity(
							description.item(i).getTextContent().trim()));
				}
			}
			if (format != null && format.getLength() != 0) {
				node.dublinCoreData.setFormat(new Vector<String>());
				for (int i = 0; i < format.getLength(); i++) {
					node.dublinCoreData.addFormat(
							transformFromXMLEntity(format.item(i).getTextContent().trim()));
				}
			}
			if (identifier != null && identifier.getLength() != 0) {
				node.dublinCoreData.setIdentifier(new Vector<Pair<String, String>>());
				for (int i = 0; i < identifier.getLength(); i++) {
					node.dublinCoreData.addIdentifier(transformFromXMLEntity(
							identifier.item(i).getTextContent().trim()));
				}
			}

			if (language != null && language.getLength() != 0) {
				node.dublinCoreData.setLanguage(new Vector<String>());
				for (int i = 0; i < language.getLength(); i++) {
					node.dublinCoreData.addLanguage(
							transformFromXMLEntity(language.item(i).getTextContent().trim()));
				}
			}
			if (publisher != null && publisher.getLength() != 0) {
				node.dublinCoreData.setPublisher(new Vector<String>());
				for (int i = 0; i < publisher.getLength(); i++) {
					node.dublinCoreData.addPublisher(transformFromXMLEntity(
							publisher.item(i).getTextContent().trim()));
				}
			}
			if (rights != null && rights.getLength() != 0) {
				node.dublinCoreData.setRights(new Vector<String>());
				for (int i = 0; i < rights.getLength(); i++) {
					node.dublinCoreData.addRights(
							transformFromXMLEntity(rights.item(i).getTextContent().trim()));
				}
			}
			if (source != null && source.getLength() != 0) {
				node.dublinCoreData.setSource(new Vector<String>());
				for (int i = 0; i < source.getLength(); i++) {
					node.dublinCoreData.addSource(
							transformFromXMLEntity(source.item(i).getTextContent().trim()));
				}
			}
			if (subject != null && subject.getLength() != 0) {
				node.dublinCoreData.setSubject(new Vector<String>());
				for (int i = 0; i < subject.getLength(); i++) {
					node.dublinCoreData.addSubject(
							transformFromXMLEntity(subject.item(i).getTextContent().trim()));
				}
			}
			if (title != null && title.getLength() != 0) {
				node.dublinCoreData.setTitle(new Vector<String>());
				for (int i = 0; i < title.getLength(); i++) {
					node.dublinCoreData.addTitle(
							transformFromXMLEntity(title.item(i).getTextContent().trim()));
				}
			}
			if (type != null && type.getLength() != 0) {
				node.dublinCoreData.setType(new Vector<String>());
				for (int i = 0; i < type.getLength(); i++) {
					node.dublinCoreData.addType(
							transformFromXMLEntity(type.item(i).getTextContent().trim()));
				}
			}

		} catch (ParserConfigurationException e) {

			throw new HttpArchiveException(500, e);
		} catch (SAXException e) {

			throw new HttpArchiveException(500, e);
		} catch (IOException e) {

			throw new HttpArchiveException(500, e);
		}
	}

	static void updateDc(Node node) {
		String preamble =
				"" + "<oai_dc:dc xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
						+ "xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\""
						+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
						+ "xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/"
						+ "http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">";
		String fazit = "</oai_dc:dc>";

		String tagStart = "<dc:";
		String tagEnd = ">";
		String endTagStart = "</dc:";

		StringBuffer update = new StringBuffer();

		List<String> contributer = null;
		List<String> coverage = null;
		List<String> creator = null;
		List<String> date = null;
		List<String> description = null;
		List<String> format = null;
		List<Pair<String, String>> identifier = null;
		// String[] label = null;
		List<String> language = null;
		List<String> publisher = null;
		List<String> rights = null;
		List<String> source = null;
		List<String> subject = null;
		List<String> title = null;
		List<String> type = null;

		if ((contributer = node.dublinCoreData.getContributor()) != null) {
			for (String str : contributer) {
				String scontributer = tagStart + "contributer" + tagEnd
						+ transformToXMLEntity(str) + endTagStart + "contributer" + tagEnd;
				update.append(scontributer + "\n");
			}
		}
		if ((coverage = node.dublinCoreData.getCoverage()) != null) {
			for (String str : coverage) {
				String scoverage = tagStart + "coverage" + tagEnd
						+ transformToXMLEntity(str) + endTagStart + "coverage" + tagEnd;
				update.append(scoverage + "\n");
			}
		}
		if ((creator = node.dublinCoreData.getCreator()) != null) {
			for (String str : creator) {

				String screator = tagStart + "creator" + tagEnd
						+ transformToXMLEntity(str) + endTagStart + "creator" + tagEnd;
				update.append(screator + "\n");
			}
		}
		if ((date = node.dublinCoreData.getDate()) != null) {
			for (String str : date) {
				String sdate = tagStart + "date" + tagEnd + transformToXMLEntity(str)
						+ endTagStart + "date"

						+ tagEnd;
				update.append(sdate + "\n");
			}
		}
		if ((description = node.dublinCoreData.getDescription()) != null) {
			for (String str : description) {
				String sdescription = tagStart + "description" + tagEnd
						+ transformToXMLEntity(str) + endTagStart + "description" + tagEnd;
				update.append(sdescription + "\n");
			}
		}
		if ((format = node.dublinCoreData.getFormat()) != null) {
			for (String str : format) {
				String sformat = tagStart + "format" + tagEnd
						+ transformToXMLEntity(str) + endTagStart + "format" + tagEnd;
				update.append(sformat + "\n");
			}
		}
		if ((identifier = node.dublinCoreData.getIdentifier()) != null) {
			for (Pair<String, String> str : identifier) {
				String sidentifier = tagStart + "identifier" + tagEnd
						+ transformToXMLEntity(str.getLeft()) + endTagStart + "identifier"
						+ tagEnd;
				play.Logger.debug("sidentifier: " + sidentifier);
				update.append(sidentifier + "\n");
			}
		}
		/*
		 * if ((label = node.dublinCoreData.getLabel()) != null) { for (int i = 0; i
		 * < label.length; i++) { String slabel = tagStart + "label" + tagEnd +
		 * label[i] + endTagStart + "label" + tagEnd; update.append(label + "\n"); }
		 * }
		 */
		if ((language = node.dublinCoreData.getLanguage()) != null) {
			for (String str : language) {
				String slanguage = tagStart + "language" + tagEnd
						+ transformToXMLEntity(str) + endTagStart + "language" + tagEnd;
				update.append(slanguage + "\n");
			}
		}
		if ((publisher = node.dublinCoreData.getPublisher()) != null) {
			for (String str : publisher) {
				String spublisher = tagStart + "publisher" + tagEnd
						+ transformToXMLEntity(str) + endTagStart + "publisher" + tagEnd;
				update.append(spublisher + "\n");
			}
		}
		if ((rights = node.dublinCoreData.getRights()) != null) {
			for (String str : rights) {
				String srights = tagStart + "rights" + tagEnd
						+ transformToXMLEntity(str) + endTagStart + "rights" + tagEnd;
				update.append(srights + "\n");
			}
		}
		if ((source = node.dublinCoreData.getSource()) != null) {
			for (String str : source) {
				String ssource = tagStart + "source" + tagEnd
						+ transformToXMLEntity(str) + endTagStart + "source" + tagEnd;
				update.append(ssource + "\n");
			}
		}
		if ((subject = node.dublinCoreData.getSubject()) != null) {
			for (String str : subject) {
				String ssubject = tagStart + "subject" + tagEnd
						+ transformToXMLEntity(str) + endTagStart + "subject" + tagEnd;
				update.append(ssubject + "\n");
			}
		}
		if ((title = node.dublinCoreData.getTitle()) != null) {
			for (String str : title) {
				String stitle = tagStart + "title" + tagEnd + transformToXMLEntity(str)
						+ endTagStart + "title" + tagEnd;
				play.Logger.debug("stitle: " + stitle);
				update.append(stitle + "\n");
			}
		}
		if ((type = node.dublinCoreData.getType()) != null) {
			for (String str : type) {
				String stype = tagStart + "type" + tagEnd + transformToXMLEntity(str)
						+ endTagStart + "type" + tagEnd;
				update.append(stype + "\n");
			}
		}

		try {
			String result = preamble + update.toString() + fazit;

			new ModifyDatastream(node.getPid(), "DC").mimeType("text/xml")
					.formatURI("http://www.openarchives.org/OAI/2.0/oai_dc/")
					.versionable(true).content(result).execute();

		} catch (FedoraClientException e) {
			throw new HttpArchiveException(e.getStatus(), e);
		}
	}

	private static String transformFromXMLEntity(String textContent) {
		if (textContent == null)
			return null;
		return textContent.replaceAll("[&]amp;", "&");
	}

	private static String transformToXMLEntity(String string) {
		if (string == null)
			return null;
		final StringBuilder result = new StringBuilder();
		final StringCharacterIterator iterator =
				new StringCharacterIterator(string);
		char character = iterator.current();
		while (character != CharacterIterator.DONE) {
			if (character == '<') {
				result.append("&lt;");
			} else if (character == '>') {
				result.append("&gt;");
			} else if (character == '\"') {
				result.append("&quot;");
			} else if (character == '\'') {
				result.append("&#039;");
			} else if (character == '&') {
				result.append("&amp;");
			} else {

				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();

	}
}
