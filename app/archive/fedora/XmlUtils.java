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
import java.io.StringWriter;
import java.util.ArrayList;
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

import com.google.common.xml.XmlEscapers;

import helper.JsonMapper;
import models.Globals;

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
	 * @param n The Node of the resource
	 * @param content Die DeepGreen-Daten im Format Document (XML)
	 * @date 2021-10-01
	 * 
	 * @return Die Daten im Format lobid2-RDF
	 */
	public Map<String, Object> getLd2Lobidify2DeepGreen(
			Map<String, Object> metadata2, Document content) {
		/* Mapping von DeepGreen.xml nach lobid2.json */
		try {
			// Neues JSON-Objekt anlegen; fuer lobid2-Daten
			Map<String, Object> rdf = metadata2;

			// DeepGreenDaten nach JSONObject wandeln
			// JSONObject jcontent = new JSONObject(content);
			play.Logger.debug("Start mapping of DeepGreen to lobid2");

			// jsonLD-Context; was ist die Entsprechung in DeepGreen ?
			rdf.put("@context",
					"https://w3id.org/kim/lrmi-profile/draft/context.jsonld");

			/* Zeitschriftentitel */
			NodeList nodeList = content.getElementsByTagName("journal-title");
			if (nodeList.getLength() > 0) {
				String journalTitle = nodeList.item(0).getTextContent();
				play.Logger.debug("Found journal title: " + journalTitle);
				/* erzeuge adhoc-ID für Zeitschriftentitel */
				String adhocTitleId = Globals.protocol + Globals.server + "/adhoc/"
						+ RdfUtils.urlEncode("uri") + "/"
						+ helper.MyURLEncoding.encode(journalTitle);
				play.Logger.debug(
						"adhocId fuer Titel \"" + journalTitle + "\": " + adhocTitleId);
				// eine Struktur {} anlegen:
				Map<String, Object> containedInMap = new TreeMap<>();
				containedInMap.put("prefLabel", journalTitle);
				containedInMap.put("@id", adhocTitleId);
				List<Map<String, Object>> containedIns = new ArrayList<>();
				containedIns.add(containedInMap);
				rdf.put("containedIn", containedIns);
			}

			/* Die Zeitschrift über die ISSN hinzu lesen */
			nodeList = content.getElementsByTagName("issn");
			if (nodeList.getLength() > 0) {
				String issn = nodeList.item(0).getTextContent();
				play.Logger.debug("Found ISSN: " + issn);
				issn = issn.replaceAll("-", "");
				play.Logger.debug("ISSN ohne Bindestrich: " + issn);
				// mit der ISSN in der lobid-API suchen
				// curl "https://lobid.org/resources/search?q=issn:"+issn+"&format=json"
				// | jq -c ".member[0].id"
				// aber das in Java
			}

			/* DOI */
			nodeList = content.getElementsByTagName("article-id");
			Node node = null;
			NamedNodeMap attributes = null;
			Node attrib = null;
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
					Map<String, Object> publisherVersion = new TreeMap<>();
					publisherVersion.put("@id", "http://dx.doi.org/" + doi);
					publisherVersion.put("prefLabel", "http://dx.doi.org/" + doi);
					List<Map<String, Object>> publisherVersions = new ArrayList<>();
					publisherVersions.add(publisherVersion);
					rdf.put("publisherVersion", publisherVersions);
					break;
				}
			}

			/* Aufsatztitel */
			nodeList = content.getElementsByTagName("article-title");
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
					Map<String, Object> publisherVersion = new TreeMap<>();
					publisherVersion.put("@id", "http://dx.doi.org/" + doi);
					publisherVersion.put("prefLabel", "http://dx.doi.org/" + doi);
					List<Map<String, Object>> publisherVersions = new ArrayList<>();
					publisherVersions.add(publisherVersion);
					rdf.put("publisherVersion", publisherVersions);
					break;
				}
			}
			if (nodeList.getLength() > 0) {
				play.Logger
						.debug("Found article title: " + nodeList.item(0).getTextContent());
				List<String> titles = new ArrayList<>();
				titles.add(nodeList.item(0).getTextContent());
				rdf.put("title", titles);
			}

			/* Autor */
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
				if (attrib.getNodeValue().equalsIgnoreCase("author")) {
					childNodes = node.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						child = childNodes.item(j);
						String childName = child.getNodeName();
						if (childName.equals("name")) {
							NodeList subchildNodes = child.getChildNodes();
							for (int k = 0; k < subchildNodes.getLength(); k++) {
								Node subchild = subchildNodes.item(k);
								String subchildName = subchild.getNodeName();
								if (subchildName.equals("surname")) {
									surname = subchild.getTextContent();
								}
								if (subchildName.equals("given-names")) {
									givenNames = subchild.getTextContent();
								}
								prefLabel = surname + ", " + givenNames;
								play.Logger.debug("Found author: " + prefLabel);
							}
						} else if (childName.equals("contrib-id")) {
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
						creator.put("@id", adhocAuthorsId);
					} else {
						creator.put("@id", orcid);
					}
					creator.put("prefLabel", prefLabel);
					creators.add(creator);
				} /* end of author */
			} /* end of loop over contrib Nodes (authors) */
			rdf.put("creator", creators);

			JsonMapper jsonMapper = new JsonMapper();
			jsonMapper.postprocessing(rdf);

			play.Logger.debug("Done mapping DeepGreen data to lobid2.");
			return rdf;
		} catch (

		Exception e) {
			play.Logger.error("Content could not be mapped!", e);
			throw new RuntimeException(
					"DeepGreen-XML could not be mapped to lobid2.json", e);
		}

	}

}
