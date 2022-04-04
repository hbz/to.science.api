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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.commons.lang3.*;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.xml.XmlEscapers;

import helper.JsonMapper;
import models.Globals;
import play.libs.ws.WSResponse;

/**
 * @author Jan Schnasse schnasse@hbz-nrw.de
 * @author Ingolf Kuss, hbz
 * @author Alessio Pellerito, hbz
 * 
 */
public class XmlUtils {

	public static XmlSchemaValidator validator = new XmlSchemaValidator();

	@SuppressWarnings({ "javadoc", "serial" })
	public static class XPathException extends RuntimeException {

		public XPathException(Throwable cause) {
			super(cause);
		}
	}

	@SuppressWarnings({ "javadoc", "serial" })
	public static class ReadException extends RuntimeException {
		public ReadException(String message) {
			super(message);
		}

		public ReadException(Throwable cause) {
			super(cause);
		}
	}

	@SuppressWarnings({ "javadoc", "serial" })
	public static class StreamNotClosedException extends RuntimeException {
		public StreamNotClosedException(String message) {
			super(message);
		}

		public StreamNotClosedException(Throwable cause) {
			super(cause);
		}
	}

	final static Logger logger = LoggerFactory.getLogger(XmlUtils.class);

	/**
	 * @param digitalEntityFile the xml file
	 * @return the root element as org.w3c.dom.Element
	 * @throws XmlException RuntimeException if something goes wrong
	 */
	public static Element getDocument(File digitalEntityFile) {
		try {
			return getDocument(new FileInputStream(digitalEntityFile));
		} catch (FileNotFoundException e) {
			throw new XmlException(e);
		}
	}

	/**
	 * @param xmlString a xml string
	 * @return the root element as org.w3c.dom.Element
	 * @throws XmlException RuntimeException if something goes wrong
	 */
	public static Element getDocument(String xmlString) {
		return getDocument(new ByteArrayInputStream(xmlString.getBytes()));

	}

	/**
	 * @param file file to store the string in
	 * @param str the string will be stored in file
	 * @return a file containing the string
	 */
	@Deprecated
	public static File stringToFile(File file, String str) {
		try {
			file.createNewFile();
			try (FileOutputStream writer = new FileOutputStream(file)) {
				// TODO uhh prevent memory overload
				writer
						.write(str.replace("\n", " ").replace("  ", " ").getBytes("utf-8"));
			}
		} catch (IOException e) {
			throw new ReadException(e);
		}
		str = null;
		return file;
	}

	/**
	 * @param file file to store the string in
	 * @param str the string will be stored in file
	 * @return a file containing the string
	 */
	public static File newStringToFile(File file, String str) {
		try {
			file.createNewFile();
			try (FileOutputStream writer = new FileOutputStream(file)) {
				writer.write(str.getBytes("utf-8"));
			}
		} catch (IOException e) {
			throw new ReadException(e);
		}
		str = null;
		return file;
	}

	/**
	 * @param file the contents of this file will be converted to a string
	 * @return a string with the content of the file
	 */
	public static String fileToString(File file) {
		if (file == null || !file.exists()) {
			throw new ReadException("");
		}
		byte[] buffer = new byte[(int) file.length()];
		try (BufferedInputStream f =
				new BufferedInputStream(new FileInputStream(file))) {
			f.read(buffer);
		} catch (IOException e) {
			throw new ReadException(e);
		}
		return new String(buffer);
	}

	/**
	 * @param xPathStr a xpath expression
	 * @param root the xpath is applied to this element
	 * @param nscontext a NamespaceContext
	 * @return a list of elements
	 */
	public static List<Element> getElements(String xPathStr, Element root,
			NamespaceContext nscontext) {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		if (nscontext != null)
			xpath.setNamespaceContext(nscontext);
		NodeList elements;
		try {
			elements =
					(NodeList) xpath.evaluate(xPathStr, root, XPathConstants.NODESET);
			List<Element> result = new Vector<Element>();
			for (int i = 0; i < elements.getLength(); i++) {
				try {
					Element element = (Element) elements.item(i);
					result.add(element);
				} catch (ClassCastException e) {
					logger.warn(e.getMessage());
				}
			}
			return result;
		} catch (XPathExpressionException e1) {
			throw new XPathException(e1);
		}

	}

	/**
	 * @param inputStream the xml stream
	 * @return the root element as org.w3c.dom.Element
	 * @throws XmlException RuntimeException if something goes wrong
	 */
	public static Element getDocument(InputStream inputStream) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// factory.setNamespaceAware(true);
			// factory.isValidating();
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			Document doc = docBuilder.parse(new BufferedInputStream(inputStream));
			Element root = doc.getDocumentElement();
			root.normalize();
			return root;
		} catch (FileNotFoundException e) {
			throw new XmlException(e);
		} catch (SAXException e) {
			throw new XmlException(e);
		} catch (IOException e) {
			throw new XmlException(e);
		} catch (ParserConfigurationException e) {
			throw new XmlException(e);
		}

	}

	/**
	 * Creates a plain xml string of the node and of all it's children. The xml
	 * string has no XML declaration.
	 * 
	 * @param node a org.w3c.dom.Node
	 * @return a plain string representation of the node it's children
	 */
	public static String nodeToString(Node node) {
		try {
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			StringWriter buffer = new StringWriter(1024);
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(node), new StreamResult(buffer));
			String str = buffer.toString();
			return str;
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error error) {
			error.printStackTrace();
		}
		return "";
	}

	/**
	 * @param file an xml file
	 * @return the root element in a namespace aware manner
	 */
	public static Element getNamespaceAwareDocument(File file) {
		try {
			return getNamespaceAwareDocument(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new XmlException(e);
		}
	}

	private static Element getNamespaceAwareDocument(InputStream inputStream) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.isValidating();
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			Document doc = docBuilder.parse(new BufferedInputStream(inputStream));
			Element root = doc.getDocumentElement();
			root.normalize();
			return root;
		} catch (FileNotFoundException e) {
			throw new XmlException(e);
		} catch (SAXException e) {
			throw new XmlException(e);
		} catch (IOException e) {
			throw new XmlException(e);
		} catch (ParserConfigurationException e) {
			throw new XmlException(e);
		}

	}

	/**
	 * @param doc
	 * @return a xml string representing the passed document
	 */
	public static String docToString(Document doc) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			return writer.toString();
		} catch (TransformerException e) {
			throw new XmlException(e);
		}
	}

	public static List<Element> getElements(String xPathStr, InputStream in,
			NamespaceContext nscontext) {
		return XmlUtils.getElements(xPathStr, XmlUtils.getDocument(in), nscontext);

	}

	public static String escapeContent(String text) {
		if (text == null)
			return "";
		return XmlEscapers.xmlContentEscaper().escape(text);
	}

	/**
	 * Holt Metadaten im Format lobid2 (falls vorhanden) und mappt Felder aus
	 * DeepGreen-Daten darauf.
	 * 
	 * @author Ingolf Kuss, hbz
	 * @author Alessio Pellerito, hbz
	 * @param metadata2 RDF-Metadaten im Format lobid2 als Java-Map
	 * @param embargo_duration : Die Dauer des Embargos in Monaten.
	 * @param content Die DeepGreen-Daten im Format Document (XML)
	 * @date 2022-03-30
	 * 
	 * @return Die Daten im Format lobid2-RDF
	 */
	public Map<String, Object> getLd2Lobidify2DeepGreen(
			Map<String, Object> metadata2, int embargo_duration, Document content) {
		/* Mapping von DeepGreen.xml nach lobid2.json */
		try {
			// Neues JSON-Objekt anlegen; fuer lobid2-Daten
			Map<String, Object> rdf = metadata2;

			play.Logger.debug("Start mapping of DeepGreen to lobid2");

			// rdf.put("@context",
			// "https://w3id.org/kim/lrmi-profile/draft/context.jsonld");

			NamedNodeMap attributes = null;
			Node attrib = null;
			/* Die Zeitschrift bei lobid über die ISSN hinzu lesen */
			String lobidId = null;
			Node node = null;
			NodeList nodeList = content.getElementsByTagName("issn");
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);

				attributes = node.getAttributes();
				if (attributes == null) {
					continue;
				}
				attrib = attributes.getNamedItem("pub-type");
				if (attrib == null) {
					continue;
				}

				if (attrib.getNodeValue().equalsIgnoreCase("epub")) {
					String issn = node.getTextContent();
					play.Logger.debug("Found ISSN: " + issn);
					issn = issn.replaceAll("-", "");
					play.Logger.debug("ISSN ohne Bindestrich: " + issn);
					WSResponse response =
							play.libs.ws.WS
									.url("https://lobid.org/resources/search?q=issn:" + issn
											+ "&format=json")
									.setFollowRedirects(true).get().get(2000);
					InputStream input = response.getBodyAsStream();
					String formsResponseBody = CharStreams
							.toString(new InputStreamReader(input, Charsets.UTF_8));
					Closeables.closeQuietly(input);
					// fetch annoying errors from to.science.forms service
					if (response.getStatus() != 200) {
						play.Logger
								.warn("to.science.api service request ISSN search fails for "
										+ issn + "!");
					} else {
						// Parse out ID value from JSON structure
						// play.Logger.debug("formsResponseBody=" + formsResponseBody);
						JSONObject jFormsResponse = new JSONObject(formsResponseBody);
						JSONArray jArr = jFormsResponse.getJSONArray("member");
						JSONObject jObj = jArr.getJSONObject(0);
						lobidId = new String(jObj.getString("id"));
						play.Logger.debug("Found lobid ID: " + lobidId);
					}
				}
			}

			/* Zeitschriftentitel */
			nodeList = content.getElementsByTagName("journal-title");
			if (nodeList.getLength() > 0) {
				String journalTitle = nodeList.item(0).getTextContent();
				play.Logger.debug("Found journal title: " + journalTitle);
				String titleId = null;
				if (lobidId == null) {
					/* erzeuge adhoc-ID für Zeitschriftentitel */
					String adhocTitleId = Globals.protocol + Globals.server + "/adhoc/"
							+ RdfUtils.urlEncode("uri") + "/"
							+ helper.MyURLEncoding.encode(journalTitle);
					play.Logger.debug(
							"adhocId fuer Titel \"" + journalTitle + "\": " + adhocTitleId);
					titleId = adhocTitleId;
				} else {
					titleId = lobidId;
				}
				// eine Struktur {} anlegen:
				Map<String, Object> containedInMap = new TreeMap<>();
				containedInMap.put("prefLabel", journalTitle);
				containedInMap.put("@id", titleId);
				rdf.put("containedIn", Arrays.asList(containedInMap));
			}

			/* DOI */
			nodeList = content.getElementsByTagName("article-id");
			List<Map<String, Object>> publisherVersions = new ArrayList<>();
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);
				attributes = node.getAttributes();
				if (attributes == null) {
					continue;
				}
				attrib = attributes.getNamedItem("pub-id-type");
				if (attrib == null) {
					continue;
				}
				if (attrib.getNodeValue().equalsIgnoreCase("doi")) {
					String doi = node.getTextContent();
					play.Logger.debug("DOI: " + doi);
					Map<String, Object> publisherVersionDoi = new TreeMap<>();
					publisherVersionDoi.put("@id", "https://doi.org/" + doi);
					publisherVersionDoi.put("prefLabel", "https://doi.org/" + doi);
					publisherVersions.add(publisherVersionDoi);
				}
				if (attrib.getNodeValue().equalsIgnoreCase("pmcid")) {
					String pmcid = node.getTextContent();
					play.Logger.debug("PMCID: " + pmcid);
					Map<String, Object> publisherVersionPmcid = new TreeMap<>();
					publisherVersionPmcid.put("@id",
							"https://www.ncbi.nlm.nih.gov/pmc/articles/" + pmcid + "/");
					publisherVersionPmcid.put("prefLabel",
							"https://www.ncbi.nlm.nih.gov/pmc/articles/" + pmcid + "/");
					publisherVersions.add(publisherVersionPmcid);
				}
			}
			rdf.put("publisherVersion", publisherVersions);

			/* Aufsatztitel */
			nodeList = content.getElementsByTagName("article-title");
			if (nodeList.getLength() > 0) {
				play.Logger
						.debug("Found article title: " + nodeList.item(0).getTextContent());
				rdf.put("title", Arrays.asList(nodeList.item(0).getTextContent()));
			}

			/* Autor */
			String contributorOrder = null;
			nodeList = content.getElementsByTagName("contrib");
			NodeList childNodes = null;
			Node child = null;
			List<Map<String, Object>> creators = new ArrayList<>();
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);
				attributes = node.getAttributes();
				if (attributes == null) {
					continue;
				}
				attrib = attributes.getNamedItem("contrib-type");
				if (attrib == null) {
					continue;
				}
				String surname = "";
				String givenNames = "";
				String prefLabel = "";
				String orcid = null;
				String authorsId = null;
				if (attrib.getNodeValue().equalsIgnoreCase("author")) {
					childNodes = node.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						child = childNodes.item(j);
						if (child.getNodeType() == Node.ELEMENT_NODE) {
							String childName = child.getNodeName();

							if (childName.equalsIgnoreCase("name")
									|| childName.equalsIgnoreCase("string-name")) {
								NodeList subchildNodes = child.getChildNodes();
								for (int k = 0; k < subchildNodes.getLength(); k++) {
									Node subchild = subchildNodes.item(k);
									if (subchild.getNodeType() == Node.ELEMENT_NODE) {
										String subchildName = subchild.getNodeName();
										if (subchildName.equalsIgnoreCase("surname")) {
											surname = subchild.getTextContent();
										}
										if (subchildName.equalsIgnoreCase("given-names")) {
											givenNames = subchild.getTextContent();
										}
									}
								}
								prefLabel = surname + ", " + givenNames;
								play.Logger.debug("Found author: " + prefLabel);
							}

							if (childName.equalsIgnoreCase("contrib-id")) {
								NamedNodeMap childAttributes = child.getAttributes();
								if (childAttributes == null) {
									continue;
								}
								Node childAttrib =
										childAttributes.getNamedItem("contrib-id-type");
								if (childAttrib == null) {
									continue;
								}
								if (childAttrib.getNodeValue().equalsIgnoreCase("orcid")) {
									orcid = child.getTextContent();
									play.Logger.debug("Found orcid: " + orcid);
								}
							}

							if (childName.equalsIgnoreCase("collab")) {
								prefLabel = child.getTextContent();
								play.Logger.debug("Found collab: " + prefLabel);
							}
						}
					} /* end of for Node child */
					Map<String, Object> creator = new TreeMap<>();
					if (orcid == null) {
						/*
						 * So erzeugt man eine "adhoc-URI" ; siehe
						 * controllers.AdhocControllers.getAdhocRdf
						 */
						String adhocAuthorsId = Globals.protocol + Globals.server
								+ "/adhoc/" + RdfUtils.urlEncode("uri") + "/"
								+ helper.MyURLEncoding.encode(prefLabel);
						play.Logger.debug(
								"adhocId fuer Autor \"" + prefLabel + "\": " + adhocAuthorsId);
						authorsId = adhocAuthorsId;
					}

					else {
						authorsId = orcid;
					}

					creator.put("@id", authorsId);
					creator.put("prefLabel", prefLabel);
					creators.add(creator);

					if (contributorOrder == null) {
						contributorOrder = authorsId;
					}

					else {
						contributorOrder = contributorOrder.concat("|" + authorsId);
					}

				} /* end of author */
			} /* end of loop over contrib Nodes (authors) */
			rdf.put("creator", creators);

			/* Reihenfolge der Beitragenden */
			rdf.put("contributorOrder", Arrays.asList(contributorOrder));

			/* Veröffentlichungsdatum */
			String pubYear = null;
			String epubDay = null;
			String epubMonth = null;
			String epubYear = null;
			nodeList = content.getElementsByTagName("pub-date");
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);
				attributes = node.getAttributes();
				if (attributes == null) {
					continue;
				}
				attrib = attributes.getNamedItem("pub-type");
				if (attrib == null) {
					continue;
				}
				if (attrib.getNodeValue().equalsIgnoreCase("subscription-year")) {
					childNodes = node.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						child = childNodes.item(j);
						String childName = child.getNodeName();
						if (childName.equalsIgnoreCase("year")) {
							pubYear = child.getTextContent();
							play.Logger.debug("Found publication year: " + pubYear);
						}
					} /* end of child node */
				} else if (attrib.getNodeValue().equalsIgnoreCase("epub")) {
					childNodes = node.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						child = childNodes.item(j);
						String childName = child.getNodeName();
						if (childName.equalsIgnoreCase("day")) {
							epubDay = child.getTextContent();
							play.Logger.debug("Found e-publication day: " + epubDay);
						} else if (childName.equalsIgnoreCase("month")) {
							epubMonth = child.getTextContent();
							play.Logger.debug("Found e-publication month: " + epubMonth);
						} else if (childName.equalsIgnoreCase("year")) {
							epubYear = child.getTextContent();
							play.Logger.debug("Found e-publication year: " + epubYear);
						}
					}
				}
			} /* end of loop over pub-date nodes) */
			rdf.put("issued", pubYear);
			String publicationDateStr = epubYear + "-" + epubMonth + "-" + epubDay;
			rdf.put("publicationYear", Arrays.asList(publicationDateStr));

			if (embargo_duration > 0) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				Date publicationDate = formatter.parse(publicationDateStr);
				Date embargoDate =
						DateUtils.addMonths(publicationDate, embargo_duration);
				rdf.put("embargoTime", Arrays.asList(formatter.format(embargoDate)));
			}

			/* Zitierangabe */
			NodeList volumes = content.getElementsByTagName("volume");
			if (volumes.getLength() > 0) {
				String volume = "";
				for (int i = 0; i < volumes.getLength(); i++) {
					node = volumes.item(i);
					volume = node.getTextContent();
					break;
				}
				NodeList issues = content.getElementsByTagName("issue");
				String issue = "";
				for (int i = 0; i < issues.getLength(); i++) {
					node = issues.item(i);
					issue = node.getTextContent();
					break;
				}
				NodeList fpages = content.getElementsByTagName("fpage");
				String fpage = "";
				for (int i = 0; i < fpages.getLength(); i++) {
					node = fpages.item(i);
					fpage = node.getTextContent();
					break;
				}
				NodeList lpages = content.getElementsByTagName("lpage");
				String lpage = "";
				for (int i = 0; i < lpages.getLength(); i++) {
					node = lpages.item(i);
					lpage = node.getTextContent();
					break;
				}
				String bibliographicCitation =
						new String(volume + "(" + issue + "):" + fpage + "-" + lpage);
				rdf.put("bibliographicCitation", Arrays.asList(bibliographicCitation));
			} else {
				String bibliographicCitation = new String("Ahead of print");
				rdf.put("bibliographicCitation", Arrays.asList(bibliographicCitation));
			}

			/* Copyright-Jahr */
			nodeList = content.getElementsByTagName("copyright-year");
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);
				rdf.put("yearOfCopyright", Arrays.asList(node.getTextContent()));
				break;
			}

			/* Open-Access Lizenz */
			nodeList = content.getElementsByTagName("ext-link");
			List<Map<String, Object>> licenses = new ArrayList<>();
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);
				String parentNodeName = node.getParentNode().getNodeName();
				if (parentNodeName.equalsIgnoreCase("license-p")
						|| parentNodeName.equalsIgnoreCase("license")) {
					// wir gehen davon aus, dass license-type == open-access ; streng
					// genommen das hier noch prüfen !!
					attributes = node.getAttributes();
					if (attributes == null) {
						continue;
					}
					attrib = attributes.getNamedItem("xlink:href");
					if (attrib == null) {
						continue;
					}
					String licenseId = attrib.getNodeValue();
					play.Logger.debug("Found LicenseId: " + licenseId);
					Map<String, Object> license = new TreeMap<>();
					license.put("@id", licenseId);
					license.put("prefLabel", licenseId);
					licenses.add(license);
				}
			}
			rdf.put("license", licenses);

			/* Abstract */
			nodeList = content.getElementsByTagName("abstract");
			if (nodeList.getLength() > 0) {
				Node paragraphNode = getFirstElementNode(nodeList.item(0));
				Node boldNode = getFirstElementNode(paragraphNode);
				if (boldNode != null) {
					if (boldNode.getNodeName().equalsIgnoreCase("bold"))
						paragraphNode.removeChild(boldNode);
				}
				rdf.put("abstractText", Arrays.asList(paragraphNode.getTextContent()));
			}

			/* Schlagwörter */
			nodeList = content.getElementsByTagName("kwd");
			List<Map<String, Object>> keywords = new ArrayList<>();
			for (int i = 0; i < nodeList.getLength(); i++) {
				node = nodeList.item(i);
				String keywordStr = node.getTextContent();
				String keywordId = Globals.protocol + Globals.server + "/adhoc/"
						+ RdfUtils.urlEncode("uri") + "/"
						+ helper.MyURLEncoding.encode(keywordStr);
				play.Logger.debug(
						"adhocId fuer Schlagwort \"" + keywordStr + "\": " + keywordId);
				Map<String, Object> keyword = new TreeMap<>();
				keyword.put("@id", keywordId);
				keyword.put("prefLabel", keywordStr);
				keywords.add(keyword);
			}
			rdf.put("subject", keywords);
			rdf.put("contentType", "article");

			/* RDF-Type */
			Map<String, Object> rdftype = new TreeMap<>();
			rdftype.put("@id", "http://purl.org/ontology/bibo/Article");
			rdftype.put("prefLabel", "Zeitschriftenartikel");
			rdf.put("rdftype", Arrays.asList(rdftype));

			/* Review-Status */
			Map<String, Object> reviewStatus = new TreeMap<>();
			reviewStatus.put("@id", "http://hbz-nrw.de/regal#peerReviewed");
			reviewStatus.put("prefLabel", "begutachtet (Peer-reviewed)");
			rdf.put("reviewStatus", Arrays.asList(reviewStatus));

			/* Veröffentlichungs-Status */
			Map<String, Object> publicationStatus = new TreeMap<>();
			publicationStatus.put("@id", "http://hbz-nrw.de/regal#original");
			publicationStatus.put("prefLabel", "Postprint Verlagsversion");
			rdf.put("publicationStatus", Arrays.asList(publicationStatus));

			/* Sprache */
			Map<String, Object> language = new TreeMap<>();
			language.put("@id", "http://id.loc.gov/vocabulary/iso639-2/eng");
			language.put("prefLabel", "Englisch");
			rdf.put("language", Arrays.asList(language));

			/* DDC */
			Map<String, Object> ddc = new TreeMap<>();
			ddc.put("@id", "http://dewey.info/class/610/");
			ddc.put("prefLabel", "Medizin & Gesundheit");
			rdf.put("ddc", Arrays.asList(ddc));

			rdf.put("accessScheme", "private");
			rdf.put("publishScheme", "public");

			JsonMapper jsonMapper = new JsonMapper();
			jsonMapper.postprocessing(rdf);

			play.Logger.debug("Done mapping DeepGreen data to lobid2.");
			return rdf;
		} catch (Exception e) {
			play.Logger.error("Content could not be mapped!", e);
			throw new RuntimeException(
					"DeepGreen-XML could not be mapped to lobid2.json", e);
		}

	}

	private static Node getFirstElementNode(Node parentNode) {
		Node node = parentNode.getFirstChild();
		while (node != null && Node.ELEMENT_NODE != node.getNodeType()) {
			node = node.getNextSibling();
		}
		if (node == null) {
			return null;
		}
		return node;
	}

}
