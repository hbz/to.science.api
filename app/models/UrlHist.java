/*
 * Copyright 2017 hbz NRW (http://www.hbz-nrw.de/)
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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.core.util.JsonUtil;

import helper.WebgatherUtils;
import models.UrlHistEntry;

/**
 * Das Datenmodell für eine URL-Umzugshistorie bei Webpages. Inhalt wird als
 * datastream an den jeweiligen Node (webpage) angehängt.
 * 
 * @author I. Kuss
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UrlHist {

	private List<UrlHistEntry> urlHistEntries;

	/**
	 * Konstruktor. Legt eine neue, leere URL-Umzugshistorie an.
	 */
	public UrlHist() {
		urlHistEntries = new ArrayList<>();
	}

	/**
	 * Konstruktor. Legt eine neue URL-Umzugshistorie mit der ersten URL an.
	 * 
	 * @param urlFirst die am Anfang gültige URL für die Webpage
	 */
	public UrlHist(String urlFirst) {
		urlHistEntries = new ArrayList<>();
		UrlHistEntry urlHistEntry = new UrlHistEntry(urlFirst);
		urlHistEntries.add(urlHistEntry);
	}

	/**
	 * Konstruktor. Legt eine neue URL-Umzugshistorie mit der ersten URL an, die
	 * sofort auf ungültig gesetzt wird. Das braucht man, wenn es noch keine
	 * URL-Historie gab, aber die URL geändert wurde.
	 * 
	 * @param urlFirst die zuletzt gültige URL für die Webpage
	 * @param endDate das Datum, bis zudem diese URL gültig war
	 */
	public UrlHist(String urlFirst, Date endDate) {
		urlHistEntries = new ArrayList<>();
		UrlHistEntry urlHistEntry = new UrlHistEntry(urlFirst, endDate);
		urlHistEntries.add(urlHistEntry);
	}

	/**
	 * Legt einen neuen URL-Historieneintrag mit nur einer URL an. Datum = initial
	 * (null).
	 * 
	 * @param url die URL des neuen Eintrages
	 */
	public void addUrlHistEntry(String url) {
		UrlHistEntry urlHistEntry = new UrlHistEntry(url);
		urlHistEntries.add(urlHistEntry);
	}

	/**
	 * Aktualisiert das Enddatum (= Datum, bis zu dem die URL gültig war) im
	 * neuesten URL-Historieneintrag
	 * 
	 * @param endDate das neue Enddatum
	 */
	public void updateLatestUrlHistEntry(Date endDate) {
		int lastIndex = urlHistEntries.size() - 1;
		UrlHistEntry urlHistEntry =
				new UrlHistEntry(urlHistEntries.get(lastIndex).url, endDate);
		urlHistEntries.set(lastIndex, urlHistEntry);
	}

	/**
	 * Liest einen bestimmtern URL-Historieneintrag anhand des Index
	 * 
	 * @param index der Index des URL-Historieneintrages (0=ältester)
	 * @return ein URL-Historieneintrag
	 */
	public UrlHistEntry getUrlHistEntry(int index) {
		return urlHistEntries.get(index);
	}

	/**
	 * @return die Liste der URL-Historien-Einträge
	 */
	public List<UrlHistEntry> getUrlHistEntries() {
		return urlHistEntries;
	}

	/**
	 * Legt aus einem JSON-String ein neues UrlHist-Objekt an
	 * 
	 * @param json a json representation
	 * @return a new UrlHist build from json
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@JsonIgnore
	public static UrlHist create(String json)
			throws JsonParseException, JsonMappingException, IOException {
		return JsonUtil.mapper().readValue(json, UrlHist.class);
	}

	public void setUrlHistEntries(ArrayList<UrlHistEntry> urlHistEntries) {
		this.urlHistEntries = urlHistEntries;
	}

	@Override
	public String toString() {
		ObjectMapper mapper = JsonUtil.mapper();
		StringWriter writer = new StringWriter();
		try {
			mapper.writeValue(writer, this);
		} catch (Exception e) {
			return super.toString();
		}
		return writer.toString();
	}

}
