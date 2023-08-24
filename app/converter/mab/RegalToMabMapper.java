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
package converter.mab;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import models.MabRecord;
import models.MabRecord.Person;
import models.MabRecord.PersonType;
import models.MabRecord.Subject;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;

import archive.fedora.RdfUtils;

/**
 * @author Jan Schnasse schnasse@gmx.de
 * 
 */
public class RegalToMabMapper {

	String topic = null;
	MabRecord record = null;
	private Map<String, List<String>> types;

	/**
	 * @param in InputStream in n-triples rdf format
	 * @param topic the id of a resource
	 * @return a mab record
	 */
	public MabRecord map(InputStream in, String topic) {
		record = new MabRecord();
		types = new HashMap<String, List<String>>();
		this.topic = topic;
		mapStatements(in);
		analyseTypes();
		return record;
	}

	private void mapStatements(InputStream in) {
		Collection<Statement> graph =
				RdfUtils.readRdfToGraph(in, RDFFormat.NTRIPLES, "");
		Iterator<Statement> it = graph.iterator();
		while (it.hasNext()) {
			Statement st = it.next();
			mapStatement(st);
		}
	}

	private void mapStatement(Statement st) {

		String pred = st.getPredicate().stringValue();
		String obj = org.apache.commons.lang3.StringEscapeUtils
				.escapeXml(st.getObject().stringValue());
		String subj = st.getSubject().stringValue();

		handleFreeFields(subj, pred, obj);

		handlePersons(subj, pred, obj);

		handleSubjects(subj, pred, obj);

		handleTypes(subj, pred, obj);

	}

	private void handleTypes(String subj, String pred, String obj) {
		if (pred.equals(LobidVocabular.rdfType)) {
			if (types.containsKey(subj)) {
				types.get(subj).add(obj);
			} else {
				List<String> list = new ArrayList<String>();
				list.add(obj);
				types.put(subj, list);
			}
		}
	}

	private void handleFreeFields(String subj, String pred, String obj) {

		if (archive.fedora.Vocabulary.REL_HBZ_ID.equals(pred)) {
			String id = obj;
			if (!id.startsWith("urn")) {
				record.id = id;
			}
		} else if ("http://purl.org/dc/terms/created".equals(pred))
			record.datumDerErsterfassung = obj;
		else if ("http://purl.org/dc/terms/title".equals(pred))
			record.hauptsachtitelVorlage = obj;
		else if (" http://purl.org/dc/elements/1.1/publisher".equals(pred))
			record.ersterVerleger = obj;
		else if ("http://rdvocab.info/Elements/placeOfPublication".equals(pred))
			record.ersterVerlagsort = obj;
		else if ("http://purl.org/dc/terms/issued".equals(pred))
			record.erscheinungsjahrAnsetzung = obj;
		else if ("http://purl.org/dc/elements/1.1/isPartOf".equals(pred))
			record.gesamttitelMitZaehlungDerStuecktitel = obj;
		else if ("http://purl.org/dc/terms/isPartOf".equals(pred)
				&& obj.contains("HT"))
			record.idGesamttitel = obj;
		else if ("http://iflastandards.info/ns/isbd/elements/P1053".equals(pred))
			record.umfang = obj;
		else if ("http://purl.org/lobid/lv#urn".equals(pred))
			record.urn = obj;
		else if ("http://lobid.org/vocab/lobid#fulltextOnline".equals(pred))
			record.httpAdresse = obj;
		else if ("http://purl.org/dc/terms/modified".equals(pred))
			record.datumLetzteKorrektur = obj;
		else if ("http://purl.org/ontology/bibo/doi".equals(pred))
			record.doi = obj;
	}

	private void handleSubjects(String subj, String pred, String obj) {
		if (pred.equals(LobidVocabular.dceSubject)) {
			if (!record.klassifikationDnb.containsKey(obj))
				record.klassifikationDnb.put(obj, record.new Subject(obj));
			else {
				// do nothing!
			}
		} else if (subj.contains("http://dewey.info/class/")) {
			String mySubject = subj.replace("/2009/08/about.en", "");
			if (!record.klassifikationDdc.containsKey(mySubject)) {
				Subject subject = record.new Subject(mySubject);
				record.klassifikationDdc.put(mySubject, subject);
			}
			Subject subject = record.klassifikationDdc.get(mySubject);
			subject.label = obj;
			record.klassifikationDdc.put(mySubject, subject);
		} else if (obj.contains("http://purl.org/lobid/rpb#n")) {
			String mySubject = obj.replace("http://purl.org/lobid/rpb#n", "rpb");
			if (!record.klassifikationRpb.containsKey(mySubject)) {
				Subject subject = record.new Subject(mySubject);
				record.klassifikationRpb.put(mySubject, subject);
			}
			Subject subject = record.klassifikationRpb.get(mySubject);
			subject.label = mySubject;
			record.klassifikationRpb.put(mySubject, subject);

		}
	}

	private void handlePersons(String subj, String pred, String obj) {
		if (pred.equals(LobidVocabular.dceCreator)) {
			if (!record.personen.containsKey(obj))
				record.personen.put(obj, record.new Person(obj));
			else {
				// do nothing!
			}
		} else if (pred.equals(LobidVocabular.gndPreferredName)
				|| pred.equals(LobidVocabular.gndPreferredNameForTheCorporateBody)) {
			if (!record.personen.containsKey(subj)) {
				Person person = record.new Person(subj);
				record.personen.put(subj, person);
			}
			Person person = record.personen.get(subj);
			person.nameVerweisungsform = obj;
			record.personen.put(subj, person);
		} else if (pred.equals(LobidVocabular.gndDateOfBirth)) {
			if (!record.personen.containsKey(subj)) {
				Person person = record.new Person(subj);
				record.personen.put(subj, person);
			}
			Person person = record.personen.get(subj);
			person.nameVerweisungsform = obj;
			record.personen.put(subj, person);
		} else if (obj.equals(LobidVocabular.gndDifferentiatedPerson)) {
			if (!record.personen.containsKey(subj)) {
				Person person = record.new Person(subj);
				record.personen.put(subj, person);
			}
			Person person = record.personen.get(subj);
			person.type = PersonType.natuerlichePerson;
			record.personen.put(subj, person);
		} else if (obj.equals(LobidVocabular.gndUndifferentiatedPerson)) {
			if (!record.personen.containsKey(subj)) {
				Person person = record.new Person(subj);
				record.personen.put(subj, person);
			}
			Person person = record.personen.get(subj);
			person.type = PersonType.natuerlichePerson;
			record.personen.put(subj, person);
		} else if (obj.equals(LobidVocabular.gndCorporateBody)) {
			if (!record.personen.containsKey(subj)) {
				Person person = record.new Person(subj);
				record.personen.put(subj, person);
			}
			Person person = record.personen.get(subj);
			person.type = PersonType.koerperschaft;
			record.personen.put(subj, person);
		} else if (obj.equals(LobidVocabular.gndOrganOfCorporateBody)) {
			if (!record.personen.containsKey(subj)) {
				Person person = record.new Person(subj);
				record.personen.put(subj, person);
			}
			Person person = record.personen.get(subj);
			person.type = PersonType.koerperschaft;
			record.personen.put(subj, person);
		}
	}

	private String analyseTypes() {
		String mabstring = "";

		List<String> list = types.get(topic);
		if (list == null)
			return mabstring;
		if (list.contains(LobidVocabular.biboCollection)
				&& list.contains(LobidVocabular.biboMultiVolumeBook))
			record.veroeffentlichungsSpezifischeAngabenString = "s|||w|||";
		else
			record.veroeffentlichungsSpezifischeAngabenString = "my|||||||||||||";
		return mabstring;
	}
}
