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
package models;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.core.util.JsonUtil;

/**
 * @author Jan Schnasse
 *
 */
public class RdfResource {

	String uri = null;
	List<Link> links = new ArrayList<Link>();
	List<RdfResource> resolvedLinks = new ArrayList<RdfResource>();

	/**
	 * A new empty RdfResource
	 */
	public RdfResource() {

	}

	/**
	 * @param uri
	 */
	public RdfResource(String uri) {
		this.uri = uri;
	}

	/**
	 * @return the subject uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @param uri
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * @return links
	 */
	public List<Link> getLinks() {
		return links;
	}

	/**
	 * @param links
	 */
	public void setLinks(List<Link> links) {
		this.links = links;
	}

	/**
	 * @return resolvedLinks
	 */
	public List<RdfResource> getResolvedLinks() {
		return resolvedLinks;
	}

	/**
	 * @param resolvedLinks
	 */
	public void setResolvedLinks(List<RdfResource> resolvedLinks) {
		this.resolvedLinks = resolvedLinks;
	}

	/**
	 * @param l
	 */
	public void addLink(Link l) {
		links.add(l);
	}

	/**
	 * @param l
	 */
	public void addResolvedLink(RdfResource l) {
		resolvedLinks.add(l);
	}

	/**
	 * @param links
	 */
	public void addLinks(List<Link> links) {
		this.links.addAll(links);
	}

	@Override
	public String toString() {
		ObjectMapper mapper = JsonUtil.mapper();
		StringWriter w = new StringWriter();
		try {
			mapper.writeValue(w, this);
		} catch (Exception e) {
			e.printStackTrace();
			return super.toString();
		}
		return w.toString();
	}

	/**
	 * @return tries to add labels for linked objects using the list of
	 *         resolvedLinks
	 */
	public RdfResource resolve() {
		for (Link l : links) {
			if (!l.isLiteral) {
				l.setObjectLabel(resolve(l));
			}
		}
		return this;
	}

	private String resolve(Link l) {
		for (RdfResource r : resolvedLinks) {
			if (l.getObject().equals(r.getUri())) {
				return findLabel(r.getLinks());
			}
		}
		return Globals.profile.getEtikett(l.getObject()).getLabel();
	}

	private String findLabel(List<Link> list) {
		Map<String, Link> map = new HashMap<String, Link>();
		for (Link l : list) {
			map.put(l.getPredicate(), l);
		}

		if (map.containsKey(
				"http://d-nb.info/standards/elementset/gnd#preferredNameForThePerson"))
			return map
					.get(
							"http://d-nb.info/standards/elementset/gnd#preferredNameForThePerson")
					.getObject();

		if (map.containsKey(
				"http://d-nb.info/standards/elementset/gnd#preferredNameForTheCorporateBody"))
			return map
					.get(
							"http://d-nb.info/standards/elementset/gnd#preferredNameForTheCorporateBody")
					.getObject();

		if (map.containsKey(
				"http://d-nb.info/standards/elementset/gnd#preferredNameForThePlaceOrGeographicName"))
			return map
					.get(
							"http://d-nb.info/standards/elementset/gnd#preferredNameForThePlaceOrGeographicName")
					.getObject();

		if (map
				.containsKey("http://d-nb.info/standards/elementset/gnd#preferredName"))
			return map.get("http://d-nb.info/standards/elementset/gnd#preferredName")
					.getObject();

		if (map.containsKey("http://www.w3.org/2004/02/skos/core#prefLabel"))
			return map.get("http://www.w3.org/2004/02/skos/core#prefLabel")
					.getObject();

		return null;
	}

}