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
package helper.oai;

import models.Globals;

/**
 * The OAISetBuilder creates OAISets from rdf statements
 * 
 * @author Jan Schnasse schnasse@hbz-nrw.de
 * 
 */
public class OaiSetBuilder {

	/**
	 * @param subject the subject of rdf triple
	 * @param predicate the predicate of rdf triple
	 * @param object the object of rdf triple
	 * @return a OAISet associated with the statement made by the triple
	 */
	public OaiSet getSet(String subject, String predicate, String object) {
		String name = null;
		String spec = null;
		String pid = null;

		if (predicate.compareTo("http://purl.org/dc/terms/subject") == 0) {
			if (object.startsWith("http://dewey.info/class/")) {
				String ddc = object
						.subSequence(object.length() - 4, object.length() - 1).toString();
				name = Globals.profile.getEtikett(object).getLabel();
				spec = "ddc:" + ddc;
				pid = "oai:" + ddc;
			} else {
				return null;
			}
		} else if (predicate
				.compareTo("http://hbz-nrw.de/regal#contentType") == 0) {
			String docType = object;
			name = docmap(docType);
			spec = "contentType:" + docType;
			pid = "oai:" + docType;

		}

		else {
			return null;
		}

		return new OaiSet(name, spec, pid);
	}

	private String docmap(String type) {
		if (type.compareTo("report") == 0) {
			return "Monograph";
		}
		if (type.compareTo("webpage") == 0) {
			return "Webpage";
		}
		if (type.compareTo("ejournal") == 0) {
			return "EJournal";
		}
		return "";
	}
}
