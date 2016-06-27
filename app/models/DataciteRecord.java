/*
 * Copyright 2014 hbz NRW (http://www.hbz-nrw.de/)
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
package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Jan Schnasse
 *
 */
public class DataciteRecord {

	/**
	 * the doi
	 */
	public String doi = null;

	/**
	 * List of creators. Only left part of pair is used.
	 */
	public Set<Pair<String, String>> creators =
			new HashSet<Pair<String, String>>();
	/**
	 * List of titles. Only left part of pair is used.
	 */
	public List<Pair<String, String>> titles =
			new ArrayList<Pair<String, String>>();
	/**
	 * A Publisher
	 */
	public String publisher = null;
	/**
	 * publication year
	 */
	public String publicationYear = null;
	/**
	 * Subjects. The right part is used to set attribute "subjectScheme"
	 */
	public List<Pair<String, String>> subjects =
			new ArrayList<Pair<String, String>>();
	/**
	 * Dates. The right part is used to set attribute "dateType"
	 */
	public List<Pair<String, String>> dates =
			new ArrayList<Pair<String, String>>();
	/**
	 * Language.
	 */
	public String language = null;
	/**
	 * AlternateIdentifiers.The right part is used to set attribute
	 * "alternateIdentifierType"
	 */
	public List<Pair<String, String>> alternateIdentifiers =
			new ArrayList<Pair<String, String>>();
	/**
	 * Sizes.
	 */
	public List<Pair<String, String>> sizes =
			new ArrayList<Pair<String, String>>();
	/**
	 * Formats.
	 */
	public List<Pair<String, String>> formats =
			new ArrayList<Pair<String, String>>();
	/**
	 * Type.
	 */
	public String type = null;

	/**
	 * @param doi
	 */
	public DataciteRecord(String doi) {
		this.doi = doi;
	}

	public String toString() {
		try {
			DocumentBuilderFactory.newInstance();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.isValidating();
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element root = doc.createElement("resource");
			root.setAttribute("xmlns", "http://datacite.org/schema/kernel-2.2");
			root.setAttribute("xmlns:xsi",
					"http://www.w3.org/2001/XMLSchema-instance");
			root.setAttribute("xsi:schemaLocation",
					"http://datacite.org/schema/kernel-2.2 http://schema.datacite.org/meta/kernel-2.2/metadata.xsd");

			if (doi != null) {
				Element i = doc.createElement("identifier");
				i.setAttribute("identifierType", "DOI");
				i.appendChild(doc.createTextNode(doi));
				root.appendChild(i);
			}
			if (creators != null && !creators.isEmpty()) {
				Element cs = doc.createElement("creators");
				for (Pair<String, String> c : creators) {
					Element cr = doc.createElement("creator");
					Element cn = doc.createElement("creatorName");

					cn.appendChild(doc.createTextNode(c.getLeft()));
					cr.appendChild(cn);
					cs.appendChild(cr);
				}
				root.appendChild(cs);
			}

			if (titles != null && !titles.isEmpty()) {
				Element ts = doc.createElement("titles");
				for (Pair<String, String> c : titles) {
					Element t = doc.createElement("title");
					t.appendChild(doc.createTextNode(c.getLeft()));
					ts.appendChild(t);
				}
				root.appendChild(ts);
			}
			if (publisher != null && !publisher.isEmpty()) {
				Element p = doc.createElement("publisher");
				p.appendChild(doc.createTextNode(publisher));
				root.appendChild(p);
			}
			if (publicationYear != null && !publicationYear.isEmpty()) {
				Element py = doc.createElement("publicationYear");
				py.appendChild(doc.createTextNode(publicationYear));
				root.appendChild(py);
			}
			if (subjects != null && !subjects.isEmpty()) {
				Element s = doc.createElement("subjects");
				for (Pair<String, String> c : subjects) {
					Element sub = doc.createElement("subject");
					if (!c.getRight().isEmpty()) {
						sub.setAttribute("subjectScheme", c.getRight());
					}

					sub.appendChild(doc.createTextNode(c.getLeft()));
					s.appendChild(sub);
				}
				root.appendChild(s);
			}

			if (dates != null && !dates.isEmpty()) {
				Element ds = doc.createElement("dates");

				for (Pair<String, String> c : dates) {
					Element d = doc.createElement("date");
					if (!c.getRight().isEmpty()) {
						d.setAttribute("dateType", c.getRight());
					}

					d.appendChild(doc.createTextNode(c.getLeft()));
					ds.appendChild(d);
				}
				root.appendChild(ds);
			}
			if (language != null && !language.isEmpty()) {
				Element l = doc.createElement("language");
				l.appendChild(doc.createTextNode(language));
				root.appendChild(l);
			}
			if (alternateIdentifiers != null && !alternateIdentifiers.isEmpty()) {
				Element ais = doc.createElement("alternateIdentifiers");
				for (Pair<String, String> c : alternateIdentifiers) {
					Element ai = doc.createElement("alternateIdentifier");
					if (!c.getRight().isEmpty()) {
						ai.setAttribute("alternateIdentifierType", c.getRight());
					}

					ai.appendChild(doc.createTextNode(c.getLeft()));
					ais.appendChild(ai);
				}
				root.appendChild(ais);
			}
			if (sizes != null && !sizes.isEmpty()) {
				Element sizs = doc.createElement("sizes");
				for (Pair<String, String> c : sizes) {
					Element siz = doc.createElement("size");

					siz.appendChild(doc.createTextNode(c.getLeft()));
					sizs.appendChild(siz);
				}
				root.appendChild(sizs);
			}
			if (formats != null && !formats.isEmpty()) {
				Element forms = doc.createElement("formats");
				for (Pair<String, String> c : formats) {
					Element form = doc.createElement("format");
					form.appendChild(doc.createTextNode(c.getLeft()));
					forms.appendChild(form);
				}
				root.appendChild(forms);
			}
			doc.appendChild(root);
			return archive.fedora.XmlUtils.docToString(doc);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
