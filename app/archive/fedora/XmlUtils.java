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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.xml.XmlEscapers;

import helper.JSONArray;
import helper.JSONException;
import helper.JSONObject;

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
	public Map<String, Object> getLd2Lobidify2DeepGreen(Node n,
			Document content) {
		/* Mapping von DeepGreen.xml nach lobid2.json */
		this.node = n;
		try {
			// Neues JSON-Objekt anlegen; f�r lobid2-Daten
			Map<String, Object> rdf = node.getLd2();

			// DeepGreenDaten nach JSONObject wandeln
			// JSONObject jcontent = new JSONObject(content);
			play.Logger.debug("Start mapping of DeepGreen to lobid2");
			JSONArray arr = null;
			JSONObject obj = null;

			NodeList nodeList = content.getElementsByTagName("journal-title");
			if (nodeList.getLength() > 0) {
				play.Logger.debug("Found journal title: " + nodeList.item(0));
				// eine Struktur {} anlegen:
				Map<String, Object> containedInMap = new TreeMap<>();
				containedInMap.put("prefLabel", nodeList.item(0));
				List<Map<String, Object>> containedIns = new ArrayList<>();
				containedIns.add(containedInMap);
				rdf.put("containedIn", containedIns);
			}

			/**
			 * if (content.getElementsByTagName("journal-title")) { arr =
			 * jcontent.getJSONArray("@context"); play.Logger.debug("Found context: "
			 * + arr.getString(0));
			 * 
			 * 
			 * rdf.put("@context", arr.getString(0)); obj = arr.getJSONObject(1);
			 * String language = obj.getString("@language"); play.Logger.debug("Found
			 * language: " + language);
			 * 
			 * // eine Struktur {} anlegen: Map<String, Object> languageMap = new
			 * TreeMap<>(); if (language != null && !language.trim().isEmpty()) { if
			 * (language.length() == 2) { // vermutlich ISO639-1
			 * languageMap.put("@id", "http://id.loc.gov/vocabulary/iso639-1/" +
			 * language); } else if (language.length() == 3) { // vermutlich ISO639-2
			 * languageMap.put("@id", "http://id.loc.gov/vocabulary/iso639-2/" +
			 * language); } else { play.Logger.warn( "Unbekanntes Vokabluar für
			 * Sprachencode! Code=" + language); } } // languageMap.put("label",
			 * "Deutsch"); // languageMap.put("prefLabel", "Deutsch");
			 * List<Map<String, Object>> languages = new ArrayList<>();
			 * languages.add(languageMap); rdf.put("language", languages); }
			 * 
			 * rdf.put(accessScheme, "public"); rdf.put(publishScheme, "public");
			 * 
			 * arr = jcontent.getJSONArray("type"); rdf.put("contentType",
			 * arr.getString(0));
			 * 
			 * List<String> names = new ArrayList<>();
			 * names.add(jcontent.getString(name)); rdf.put("title", names);
			 * 
			 * if (jcontent.has("creator")) { List<Map<String, Object>> creators = new
			 * ArrayList<>(); arr = jcontent.getJSONArray("creator"); for (int i = 0;
			 * i < arr.length(); i++) { obj = arr.getJSONObject(i); Map<String,
			 * Object> creatorMap = new TreeMap<>(); creatorMap.put("prefLabel",
			 * obj.getString(name)); if (obj.has("id")) { creatorMap.put("@id",
			 * obj.getString("id")); } creators.add(creatorMap); } rdf.put("creator",
			 * creators); }
			 * 
			 * if (jcontent.has("contributor")) { List<Map<String, Object>>
			 * contributors = new ArrayList<>(); arr =
			 * jcontent.getJSONArray("contributor"); for (int i = 0; i < arr.length();
			 * i++) { obj = arr.getJSONObject(i); Map<String, Object> contributorMap =
			 * new TreeMap<>(); contributorMap.put("prefLabel", obj.getString(name));
			 * if (obj.has("id")) { contributorMap.put("@id", obj.getString("id")); }
			 * contributors.add(contributorMap); } rdf.put("contributor",
			 * contributors); }
			 * 
			 * if (jcontent.has("description")) { List<String> abstractTexts = new
			 * ArrayList<>(); // arr = jcontent.getJSONArray("description"); // for
			 * (int i = 0; i < arr.length(); i++) { //
			 * abstractTexts.add(arr.getString(i));
			 * abstractTexts.add(jcontent.getString("description")); // }
			 * rdf.put("abstractText", abstractTexts); }
			 * 
			 * if (jcontent.has("license")) { obj = jcontent.getJSONObject("license");
			 * List<Map<String, Object>> licenses = new ArrayList<>(); // arr =
			 * jcontent.getJSONArray("license"); // for (int i = 0; i < arr.length();
			 * i++) { Map<String, Object> licenseMap = new TreeMap<>(); //
			 * licenseMap.put("@id", arr.getString(i)); // licenseMap.put("@id",
			 * jcontent.getString("license")); licenseMap.put("@id",
			 * obj.getString("id")); licenses.add(licenseMap); // } rdf.put("license",
			 * licenses); }
			 * 
			 * if (jcontent.has("publisher")) { List<Map<String, Object>> institutions
			 * = new ArrayList<>(); arr = jcontent.getJSONArray("publisher"); for (int
			 * i = 0; i < arr.length(); i++) { obj = arr.getJSONObject(i); Map<String,
			 * Object> publisherMap = new TreeMap<>(); publisherMap.put("prefLabel",
			 * obj.getString(name)); publisherMap.put("@id", obj.getString("id"));
			 * institutions.add(publisherMap); } rdf.put("institution", institutions);
			 * }
			 */

			// postprocessing(rdf);

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
