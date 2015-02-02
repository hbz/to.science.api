package archive.fedora;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import models.Link;
import models.RdfResource;

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

import play.Play;

/**
 * @author Jan Schnasse
 *
 */
public class ApplicationProfile {

    /**
     * prefLabel predicate will be analysed
     */
    public final static String prefLabel = "http://www.w3.org/2004/02/skos/core#prefLabel";

    /**
     * icon predicate will be analyzed
     */
    public final static String icon = "http://www.w3.org/1999/xhtml/vocab#icon";

    /**
     * name predicate will be analyzed
     */
    public final static String name = "http://hbz-nrw.de/regal#jsonName";

    /**
     * A map with URIs as key and labels,icons, shortnames as values
     */
    public Map<String, MapEntry> pMap = new HashMap<String, MapEntry>();

    /**
     * A map with Shortnames as key and labels,icons, uris as values
     */
    public Map<String, MapEntry> nMap = new HashMap<String, MapEntry>();

    private final String defaultMap = "/tmp/regal-default.ntriple";

    /**
     * Associates labels to rdf predicates or known objects
     */
    public ApplicationProfile() {
	loadDefaultConfig();
	loadToMap("regal.turtle");
	loadToMap("rpb.turtle");
	loadNMap();
    }

    private void loadNMap() {
	for (Entry<String, MapEntry> e : pMap.entrySet()) {
	    nMap.put(e.getValue().name, e.getValue());
	}
    }

    private void loadDefaultConfig() {
	try {
	    loadToMap(new FileInputStream(new File(defaultMap)));
	} catch (Exception e) {
	    play.Logger.info("Default config file " + defaultMap
		    + " not found.");
	}
    }

    private void loadToMap(String fileName) {
	try {
	    InputStream in = Play.application().resourceAsStream(fileName);
	    loadToMap(in);
	} catch (Exception e) {
	    e.printStackTrace();
	    play.Logger.info("config file " + fileName + " not found.");
	}
    }

    private void loadToMap(InputStream in) {
	Graph g = RdfUtils.readRdfToGraph(in, RDFFormat.TURTLE, "");
	Iterator<Statement> statements = g.iterator();
	while (statements.hasNext()) {
	    Statement st = statements.next();
	    if (prefLabel.equals(st.getPredicate().stringValue())) {
		String key = st.getSubject().stringValue();
		String labelStr = st.getObject().stringValue();
		addLabel(key, labelStr);
	    }
	    if (icon.equals(st.getPredicate().stringValue())) {
		String key = st.getSubject().stringValue();
		String iconStr = st.getObject().stringValue();
		addIcon(key, iconStr);
	    }
	    if (name.equals(st.getPredicate().stringValue())) {
		String key = st.getSubject().stringValue();
		String nameStr = st.getObject().stringValue();
		addName(key, nameStr);
	    }
	}
    }

    void addLabel(String key, String labelStr) {
	MapEntry e = new MapEntry();
	if (pMap.containsKey(key)) {
	    e = pMap.get(key);
	}
	e.label = labelStr;
	pMap.put(key, e);
    }

    void addIcon(String key, String iconStr) {
	MapEntry e = new MapEntry();
	if (pMap.containsKey(key)) {
	    e = pMap.get(key);
	}
	e.icon = iconStr;
	pMap.put(key, e);
    }

    void addName(String key, String nameStr) {
	MapEntry e = new MapEntry();
	if (pMap.containsKey(key)) {
	    e = pMap.get(key);
	}
	e.uri = key;
	e.name = nameStr;
	pMap.put(key, e);
    }

    /**
     * @param key
     * @return a icon string or null
     */
    public String getIcon(String key) {
	if (pMap.containsKey(key)) {
	    MapEntry value = pMap.get(key);
	    return value.icon;
	}
	return null;
    }

    /**
     * stores the pmap to a file. Each entry is represented in one row forming
     * an ntriple key prefLabel value
     */
    public void saveMap() {
	play.Logger.info("Write labels to map please hold on!!!");
	String result = new String();
	Set<Entry<String, MapEntry>> set = pMap.entrySet();
	for (Entry<String, MapEntry> e : set) {
	    String l = e.getValue().label;
	    String i = e.getValue().icon;

	    if (l != null) {
		result = RdfUtils.addTriple(e.getKey(), prefLabel, l, true,
			result, RDFFormat.TURTLE);
	    }
	    if (i != null) {
		result = RdfUtils.addTriple(e.getKey(), icon, i, true, result,
			RDFFormat.TURTLE);
	    }
	}
	XmlUtils.newStringToFile(new File(defaultMap), result);
    }

    /**
     * Creates new RdfResource with labels for objects and predicates
     * 
     * @param r
     *            a RdfResource
     * 
     * @return new RdfResource with labels for objects and predicates
     */
    public RdfResource addLabels(final RdfResource r) {
	try {
	    RdfResource result = new RdfResource(r.getUri());
	    for (Link l : r.getLinks()) {
		MapEntry entry = pMap.get(l.getPredicate());

		if (entry == null || entry.label == null) {

		} else {
		    l.setPredicateLabel(entry.label);
		}

		if (!l.isLiteral() && l.getObjectLabel() == null) {

		    entry = pMap.get(l.getObject());
		    if (entry == null || entry.label == null) {
			l.setObjectLabel(l.getObject());
			play.Logger.debug("No label for " + l.getObject());
		    } else {
			l.setObjectLabel(entry.label);
		    }
		}
		result.addLink(l);
	    }
	    return result;
	} catch (Exception e) {
	    e.printStackTrace();
	    throw e;
	}
    }
}
