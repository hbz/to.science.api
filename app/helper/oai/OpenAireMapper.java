
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import actions.Read;
import models.Node;
import models.CoarModel;
import models.EmbargoModel;

/**
 * @author Andres Quast
 *
 */
public class OpenAireMapper {

	Node node;
	String uri;
	Document doc = null;

	/**
	 * @param node
	 * @param uri
	 */
	public OpenAireMapper(Node node, String uri) {
		this.node = node;
		this.uri = uri;
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

		Element resource = doc.createElement("resource");
		resource.setAttribute("xmlns:rdf",
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		resource.setAttribute("xmlns:datacite",
				"http://datacite.org/schema/kernel-4");
		resource.setAttribute("xmlns",
				"http://namespace.openaire.eu/schema/oaire/");
		resource.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
		resource.setAttribute("xmlns:dcterms", "http://purl.org/dc/terms/");
		resource.setAttribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		resource.setAttribute("xsi:schemaLocation",
				"http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.1/metadata.xsd \n"
						+ "	http://namespace.openaire.eu/schema/oaire/  https://www.openaire.eu/schema/repo-lit/4.0/openaire.xsd \n"
						+ "	http://purl.org/dc/terms/ http://dublincore.org/schemas/xmls/qdc/dcterms.xsd \n"
						+ "	http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd \n"
						+ "	http://purl.org/dc/elements/1.1/ http://dublincore.org/schemas/xmls/qdc/2003/04/02/dc.xsd \n"
						+ "	http://datacite.org/schema/kernel-4  http://schema.datacite.org/meta/kernel-4.1/metadata.xsd \n"
						+ "");

		Hashtable<String, String> jsonElementList = new Hashtable<>();

		// Element elem = null;

		// generate title
		ArrayList<Hashtable<String, String>> jemList =
				jMapper.getElement("root.title");
		for (int i = 0; i < jemList.size(); i++) {
			Element title = doc.createElement("datacite:title");
			title.appendChild(doc.createTextNode(jemList.get(i).get("root.title")));
			if (i > 0) {
				title.setAttribute("titleType", "Subtitle");
			}
			resource.appendChild(title);
		}

		// generate creators
		Element creators = doc.createElement("datacite:creators");
		jemList = jMapper.getElement("root.creator");
		for (int i = 0; i < jemList.size(); i++) {
			Element sE = doc.createElement("datacite:creator");
			creators.appendChild(sE);
			Element cn = doc.createElement("datacite:creatorName");
			cn.appendChild(doc.createTextNode(jemList.get(i).get("prefLabel")));
			sE.appendChild(cn);

			// prevent record from displaying local ids
			if (jemList.get(i).containsKey("@id")
					&& !jemList.get(i).get("@id").startsWith("https://frl")
					&& !jemList.get(i).get("@id").startsWith("https://api.ellinet")) {
				Element ci = doc.createElement("datacite:creatorIdentifier");
				ci.appendChild(doc.createTextNode(jemList.get(i).get("@id")));
				sE.appendChild(ci);
			}

			creators.appendChild(sE);
			resource.appendChild(creators);

		}

		// generate fundingReference
		Element funding = doc.createElement("fundingReferences");
		jemList = jMapper.getElement("root.joinedFunding");
		for (int i = 0; i < jemList.size(); i++) {
			Element sE = doc.createElement("fundingReference");
			funding.appendChild(sE);
			Element cn = doc.createElement("funderName");
			cn.appendChild(doc.createTextNode(jemList.get(i).get("prefLabel")));
			sE.appendChild(cn);

			if (jemList.get(i).containsKey("@id")
					&& !jemList.get(i).get("@id").startsWith("https://frl")
					&& !jemList.get(i).get("@id").startsWith("https://api.ellinet")) {
				Element ci = doc.createElement("funderIdentifier");
				ci.appendChild(doc.createTextNode(jemList.get(i).get("@id")));
				sE.appendChild(ci);
			}

			Element cp = doc.createElement("fundingStream");
			cp.appendChild(
					doc.createTextNode(jemList.get(i).get("fundingProgramJoined")));
			sE.appendChild(cp);

			Element cpi = doc.createElement("awardNumber");
			cpi.appendChild(
					doc.createTextNode(jemList.get(i).get("projectIdJoined")));
			sE.appendChild(cpi);

			funding.appendChild(sE);
			resource.appendChild(funding);

		}

		// generate alternateIdentifiers
		Element alternate = doc.createElement("alternateIdentifiers");
		jemList = jMapper.getElement("root.bibo:doi");
		for (int i = 0; i < jemList.size(); i++) {
			Element id = doc.createElement("alternateIdentifier");
			id.appendChild(doc.createTextNode(jemList.get(i).get("root.bibo:doi")));
			id.setAttribute("alternateIdentifierType", "DOI");
			alternate.appendChild(id);
		}
		resource.appendChild(alternate);

		// generate language
		jemList = jMapper.getElement("root.language");
		for (int i = 0; i < jemList.size(); i++) {
			Element language = doc.createElement("dc:language");
			language.appendChild(
					doc.createTextNode(jemList.get(i).get("@id").substring(38)));
			resource.appendChild(language);
		}

		// generate description
		jemList = jMapper.getElement("root.abstractText");
		for (int i = 0; i < jemList.size(); i++) {
			Element description = doc.createElement("datacite:description");
			description.appendChild(
					doc.createTextNode(jemList.get(i).get("root.abstractText")));
			resource.appendChild(description);
		}

		// generate identifier
		jemList = jMapper.getElement("root");
		for (int i = 0; i < jemList.size(); i++) {
			if (jemList.get(i).containsKey("@id")) {
				Element identifier = doc.createElement("datacite:identifier");
				identifier.appendChild(
						doc.createTextNode("https://repository.publisso.de/resource/"
								+ jemList.get(i).get("@id")));
				identifier.setAttribute("identifierType", "PURL");
				resource.appendChild(identifier);
			}
		}

		// generate oaire:resourceType
		jemList = jMapper.getElement("root");
		for (int i = 0; i < jemList.size(); i++) {
			if (jemList.get(i).containsKey("contentType")) {
				Element resourceType = doc.createElement("resourceType");
				resourceType.appendChild(doc.createTextNode(
						CoarModel.getElementValue(jemList.get(i).get("contentType"))));
				resourceType.setAttribute("uri",
						CoarModel.getUriAttributeValue(jemList.get(i).get("contentType")));
				resourceType.setAttribute("resourceTypeGeneral",
						CoarModel.getResourceTypeGeneralAttribute(
								jemList.get(i).get("contentType")));
				resource.appendChild(resourceType);
			}
		}

		// generate source
		jemList = jMapper.getElement("root.containedIn");
		for (int i = 0; i < jemList.size(); i++) {
			Element source = doc.createElement("dc:source");
			source.appendChild(doc.createTextNode(jemList.get(i).get("prefLabel")));
			// source.setAttribute("identifierType", "PURL");
			resource.appendChild(source);
		}

		// generate subjects from articles
		Element subjects = doc.createElement("datacite:subjects");

		jemList = jMapper.getElement("root.ddc");
		for (int i = 0; i < jemList.size(); i++) {
			Element sE = doc.createElement("datacite:subject");
			sE.appendChild(doc.createTextNode(jemList.get(i).get("prefLabel")));
			sE.setAttribute("subjectScheme", "DDC");
			sE.setAttribute("schemeURI", "http://dewey.info");
			sE.setAttribute("valueURI", jemList.get(i).get("@id"));

			subjects.appendChild(sE);
		}

		jemList = jMapper.getElement("root.subject");
		for (int i = 0; i < jemList.size(); i++) {
			if (jemList.get(i).containsKey("prefLabel")) {
				Element sE = doc.createElement("datacite:subject");
				sE.appendChild(doc.createTextNode(jemList.get(i).get("prefLabel")));

				// prevent record from displaying local id's
				if (jemList.get(i).containsKey("@id")
						&& !jemList.get(i).get("@id").startsWith("https://frl")
						&& !jemList.get(i).get("@id").startsWith("https://api.ellinet")) {
					sE.setAttribute("valueURI", jemList.get(i).get("@id"));
				}

				subjects.appendChild(sE);
			}
		}

		// generate subjects from monographs
		jemList = jMapper.getElement("root.subject.source");
		for (int i = 0; i < jemList.size(); i++) {
			if (jemList.get(i).containsKey("prefLabel")) {
				Element sE = doc.createElement("datacite:subject");
				sE.appendChild(doc.createTextNode(jemList.get(i).get("prefLabel")));

				if (jemList.get(i).containsKey("label") && jemList.get(i).get("label")
						.equals("Dewey-Dezimalklassifikation")) {
					sE.setAttribute("subjectScheme", "DDC");
					sE.setAttribute("schemeURI", "http://dewey.info");
					sE.setAttribute("valueURI", jemList.get(i).get("@id"));
				} else {
					if (jemList.get(i).containsKey("@id")
							&& !jemList.get(i).get("@id").startsWith("https://frl")
							&& !jemList.get(i).get("@id").startsWith("https://api.ellinet")) {
						sE.setAttribute("valueURI", jemList.get(i).get("@id"));
					}
				}
				subjects.appendChild(sE);
			}
		}
		resource.appendChild(subjects);

		// generate publisher
		jemList = jMapper.getElement("root.containedIn");
		for (int i = 0; i < jemList.size(); i++) {
			Element publisher = doc.createElement("dc:publisher");
			publisher
					.appendChild(doc.createTextNode(jemList.get(i).get("prefLabel")));
			resource.appendChild(publisher);
		}

		// generate version aka green road OA version
		jemList = jMapper.getElement("root.publicationStatus");
		for (int i = 0; i < jemList.size(); i++) {
			Element version = doc.createElement("version");
			version.appendChild(doc.createTextNode(
					CoarModel.getElementValue(jemList.get(i).get("prefLabel"))));
			version.setAttribute("uri",
					CoarModel.getUriAttributeValue(jemList.get(i).get("prefLabel")));
			resource.appendChild(version);
		}

		// generate oaire:file
		jemList = jMapper.getElement("root.hasPart");
		for (int i = 0; i < jemList.size(); i++) {
			// fetch file details from child resources
			JsonLDMapper childMapper =
					new JsonLDMapper(getChildJsonNode(jemList.get(i).get("@id")));

			// Workaround to prevent deleted objects to be displayed :-(
			// TODO: make deleted objects invisible for parent objects in lobid
			ArrayList<Hashtable<String, String>> childJemList =
					childMapper.getElement("root");

			boolean isDeletedChild = false;
			boolean isContentTypeFile = false;
			for (int k = 0; k < childJemList.size(); k++) {
				if (childJemList.get(k).get("notification") != null
						&& childJemList.get(k).get("notification")
								.equals("Dieses Objekt wurde gelöscht")) {
					isDeletedChild = true;
				}
			}

			for (int k = 0; k < childJemList.size(); k++) {
				if (childJemList.get(k).get("contentType") != null
						&& childJemList.get(k).get("contentType").equals("file")) {
					isContentTypeFile = true;
				}
			}

			if (isDeletedChild == false && isContentTypeFile == true) {
				Element oairefile = doc.createElement("file");
				oairefile.appendChild(
						doc.createTextNode("https://repository.publisso.de/resource/"
								+ jemList.get(i).get("@id") + "/data"));

				childJemList = childMapper.getElement("root.hasData");
				for (int j = 0; j < childJemList.size(); j++) {
					if (childJemList.get(j).containsKey("format")) {
						oairefile.setAttribute("mimeType",
								childJemList.get(j).get("format"));
						if (childJemList.get(j).get("format").equals("application/pdf")) {
							oairefile.setAttribute("objectType", "fulltext");
						} else {
							oairefile.setAttribute("objectType", "other");
						}
					}
				}
				childJemList = childMapper.getElement("root");
				for (int j = 0; j < childJemList.size(); j++) {
					if (childJemList.get(j).containsKey("accessScheme")) {
						oairefile.setAttribute("accessRightsURI", CoarModel
								.getUriAttributeValue(childJemList.get(j).get("accessScheme")));
					}
				}
				resource.appendChild(oairefile);
			}
		}

		// generate citation Fields
		jemList = jMapper.getElement("root.bibliographicCitation");
		for (int i = 0; i < jemList.size(); i++) {
			String fullCitation = jemList.get(i).get("root.bibliographicCitation");
			String[] splitCitation = fullCitation.split("[()\\-\\:]");
			if (splitCitation.length > 4) {
				Element volCitation = doc.createElement("citationVolume");
				volCitation.appendChild(doc.createTextNode(splitCitation[0]));
				Element issueCitation = doc.createElement("citationIssue");
				issueCitation.appendChild(doc.createTextNode(splitCitation[1]));
				Element startPageCitation = doc.createElement("citationStartPage");
				startPageCitation.appendChild(doc.createTextNode(splitCitation[3]));
				Element endPageCitation = doc.createElement("citationEndPage");
				endPageCitation.appendChild(doc.createTextNode(splitCitation[4]));
				resource.appendChild(volCitation);
				resource.appendChild(issueCitation);
				resource.appendChild(startPageCitation);
				resource.appendChild(endPageCitation);
			}
		}

		// generate relatedIdentifier
		jemList = jMapper.getElement("root.hasPart");
		Element relIdentifiers = doc.createElement("datacite:relatedIdentifiers");
		for (int i = 0; i < jemList.size(); i++) {
			if (jemList.get(i).containsKey("@id")) {
				Element rIdentifier = doc.createElement("datacite:relatedIdentifier");
				rIdentifier.appendChild(
						doc.createTextNode("https://repository.publisso.de/resource/"
								+ jemList.get(i).get("@id")));
				rIdentifier.setAttribute("relatedIdentifierType", "PURL");
				rIdentifier.setAttribute("relationType", "HasPart");
				relIdentifiers.appendChild(rIdentifier);
			}
			resource.appendChild(relIdentifiers);
		}

		// generate licenseCondition
		jemList = jMapper.getElement("root.license");
		for (int i = 0; i < jemList.size(); i++) {
			Element license = doc.createElement("licenseCondition");
			license.appendChild(doc.createTextNode(jemList.get(i).get("prefLabel")));
			license.setAttribute("uri", jemList.get(i).get("@id"));
			resource.appendChild(license);
		}

		// generate dates
		Element dates = doc.createElement("datacite:dates");

		// generate dateIssued
		jemList = jMapper.getElement("root.publicationYear");
		for (int i = 0; i < jemList.size(); i++) {
			Element issued = doc.createElement("datacite:date");
			issued.appendChild(
					doc.createTextNode(jemList.get(i).get("root.publicationYear")));
			issued.setAttribute("dateType", "Issued");
			dates.appendChild(issued);
		}
		jemList = jMapper.getElement("root.embargoTime");
		for (int i = 0; i < jemList.size(); i++) {
			Element available = doc.createElement("datacite:date");
			available.appendChild(
					doc.createTextNode(jemList.get(i).get("root.embargoTime")));
			available.setAttribute("dateType", "Available");
			dates.appendChild(available);
		}
		resource.appendChild(dates);

		// generate accessRights
		if (jMapper.elementExists("root.embargoTime")) {
			jemList = jMapper.getElement("root.embargoTime");
			for (int i = 0; i < jemList.size(); i++) {
				String acScheme = null;
				EmbargoModel emb = new EmbargoModel();
				acScheme = emb.getAccessScheme(jemList.get(i).get("root.embargoTime"));
				Element rights = doc.createElement("dc:rights");
				rights.appendChild(
						doc.createTextNode(CoarModel.getElementValue(acScheme)));
				rights.setAttribute("uri", CoarModel.getUriAttributeValue(acScheme));
				resource.appendChild(rights);
			}
		} else {
			jemList = jMapper.getElement("root");
			for (int i = 0; i < jemList.size(); i++) {
				if (jemList.get(i).containsKey("accessScheme")) {
					Element rights = doc.createElement("dc:rights");
					rights.appendChild(doc.createTextNode(
							CoarModel.getElementValue(jemList.get(i).get("accessScheme"))));
					rights.setAttribute("uri", CoarModel
							.getUriAttributeValue(jemList.get(i).get("accessScheme")));
					resource.appendChild(rights);
				}
			}
		}

		doc.appendChild(resource);

		return archive.fedora.XmlUtils.docToString(doc);
	}

	private JsonNode getChildJsonNode(String pid) {
		Node childNode = new Read().readNode(pid);
		JsonNode childJsonNode = new ObjectMapper().valueToTree(childNode.getLd2());
		return childJsonNode;
	}

}
