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
package actions;

import static archive.fedora.FedoraVocabulary.IS_MEMBER_OF;
import static archive.fedora.FedoraVocabulary.ITEM_ID;
import helper.HttpArchiveException;
import helper.OaiSet;
import helper.OaiSetBuilder;
import helper.URN;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.DublinCoreData;
import models.Globals;
import models.Link;
import models.Node;
import models.Pair;
import models.RegalObject;
import models.Transformer;

import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;

import archive.fedora.CopyUtils;
import archive.fedora.RdfException;
import archive.fedora.RdfUtils;

/**
 * @author Jan Schnasse
 *
 */
public class Modify extends RegalAction {

    /**
     * @param pid
     *            the pid that must be updated
     * @param content
     *            the file content as byte array
     * @param mimeType
     *            the mimetype of the file
     * @param name
     *            the name of the file
     * @param md5Hash
     *            a hash for the content. Can be null.
     * @return A short message
     * @throws IOException
     *             if data can not be written to a tmp file
     */
    public String updateData(String pid, InputStream content, String mimeType,
	    String name, String md5Hash) throws IOException {
	if (content == null) {
	    throw new HttpArchiveException(406, pid
		    + " you've tried to upload an empty stream."
		    + " This action is not supported. Use HTTP DELETE instead.");
	}
	File tmp = File.createTempFile(name, "tmp");
	tmp.deleteOnExit();
	CopyUtils.copy(content, tmp);
	Node node = new Read().readNode(pid);
	if (node != null) {
	    node.setUploadData(tmp.getAbsolutePath(), mimeType);
	    node.setFileLabel(name);
	    node.setMimeType(mimeType);
	    Globals.fedora.updateNode(node);
	} else {
	    throw new HttpArchiveException(500, "Lost Node!");
	}
	new Index().index(node);
	if (md5Hash != null && !md5Hash.isEmpty()) {
	    node = new Read().readNode(pid);
	    String fedoraHash = node.getChecksum();
	    if (!md5Hash.equals(fedoraHash)) {
		throw new HttpArchiveException(417, pid + " expected a MD5 of "
			+ fedoraHash + " but you provided a MD5 value of "
			+ md5Hash);
	    }
	}
	updateIndexAndCache(node);
	return pid + " data successfully updated!";
    }

    /**
     * @param pid
     *            The pid that must be updated
     * @param dc
     *            A dublin core object
     * @return a short message
     */
    public String updateDC(String pid, DublinCoreData dc) {
	Node node = new Read().readNode(pid);
	node.setDublinCoreData(dc);
	Globals.fedora.updateNode(node);
	updateIndexAndCache(node);
	return pid + " dc successfully updated!";
    }

    /**
     * @param pid
     *            the node's pid
     * @param content
     *            a json array to provide ordering information about the
     *            object's children
     * @return a message
     */
    public String updateSeq(String pid, String content) {
	try {
	    if (content == null) {
		throw new HttpArchiveException(406, pid
			+ " You've tried to upload an empty string."
			+ " This action is not supported."
			+ " Use HTTP DELETE instead.\n");
	    }
	    play.Logger.info(content);
	    File file = CopyUtils.copyStringToFile(content);
	    Node node = new Read().readNode(pid);
	    if (node != null) {
		node.setSeqFile(file.getAbsolutePath());
		Globals.fedora.updateNode(node);
	    }
	    updateIndexAndCache(node);
	    return pid + " sequence of child objects updated!";
	} catch (RdfException e) {
	    throw new HttpArchiveException(400, e);
	} catch (IOException e) {
	    throw new UpdateNodeException(e);
	}
    }

    /**
     * @param pid
     *            The pid that must be updated
     * @param content
     *            The metadata as rdf string
     * @return a short message
     */
    public String updateMetadata(String pid, String content) {
	try {
	    Node node = new Read().readNode(pid);
	    return updateMetadata(node, content);
	} catch (Exception e) {
	    throw new UpdateNodeException(e);
	}
    }

    /**
     * @param node
     *            The node that must be updated
     * @param content
     *            The metadata as rdf string
     * @return a short message
     */
    public String updateMetadata(Node node, String content) {
	try {
	    String pid = node.getPid();
	    if (content == null) {
		throw new HttpArchiveException(406, pid
			+ " You've tried to upload an empty string."
			+ " This action is not supported."
			+ " Use HTTP DELETE instead.\n");
	    }
	    RdfUtils.validate(content);
	    File file = CopyUtils.copyStringToFile(content);
	    node.setMetadataFile(file.getAbsolutePath());
	    if (content.contains(archive.fedora.Vocabulary.REL_MAB_527)) {
		node.addTransformer(new Transformer("aleph"));
	    } else {
		node.removeTransformer("aleph");
	    }
	    Globals.fedora.updateNode(node);
	    updateIndexAndCache(new Read().readNode(pid));
	    return pid + " metadata successfully updated!";
	} catch (RdfException e) {
	    throw new HttpArchiveException(400);
	} catch (IOException e) {
	    throw new UpdateNodeException(e);
	}
    }

    /**
     * @param pid
     *            The pid to which links must be added
     * @param links
     *            list of links
     * @return a short message
     */
    public String addLinks(String pid, List<Link> links) {
	Node node = new Read().readNode(pid);
	for (Link link : links) {
	    node.addRelation(link);
	}
	Globals.fedora.updateNode(node);
	updateIndexAndCache(node);
	return pid + " " + links + " links successfully added.";
    }

    /**
     * @param pid
     *            The pid to which links must be added uses: Vector<Link> v =
     *            new Vector<Link>(); v.add(link); return addLinks(pid, v);
     * @param link
     *            a link
     * @return a short message
     */
    public String addLink(String pid, Link link) {
	Vector<Link> v = new Vector<Link>();
	v.add(link);
	return addLinks(pid, v);
    }

    /**
     * @param p
     *            the id part of a pid
     * @param namespace
     *            the namespace part of a pid
     * @param transformerId
     *            the id of the transformer
     */
    public void addTransformer(String p, String namespace, String transformerId) {
	String pid = namespace + ":" + p;
	Node node = new Read().readNode(pid);
	node.addTransformer(new Transformer(transformerId));
	Globals.fedora.updateNode(node);
    }

    /**
     * Generates a urn
     * 
     * @param id
     *            usually the id of an object without namespace
     * @param namespace
     *            usually the namespace
     * @param snid
     *            the urn subnamespace id e.g."hbz:929:02"
     * @return the urn
     */
    public String addUrn(String id, String namespace, String snid) {
	String pid = namespace + ":" + id;
	return addUrn(pid, snid);
    }

    /**
     * Generates a urn
     * 
     * @param pid
     *            usually the id of an object with namespace
     * @param snid
     *            the urn subnamespace id e.g."hbz:929:02"
     * @return the urn
     */
    public String addUrn(String pid, String snid) {
	Node node = new Read().readNode(pid);
	return addUrn(node, snid);
    }

    /**
     * Generates a urn
     * 
     * @param node
     *            the node to add a urn to
     * @param snid
     *            the urn subnamespace id e.g."hbz:929:02"
     * @return the urn
     */
    public String addUrn(Node node, String snid) {
	String subject = node.getPid();
	String hasUrn = "http://purl.org/lobid/lv#urn";
	String metadata = node.getMetadata();
	if (node.hasUrn())
	    throw new HttpArchiveException(409, subject
		    + " already has a urn: " + metadata);
	String urn = generateUrn(subject, snid);
	metadata = RdfUtils.addTriple(subject, hasUrn, urn, true, metadata,
		RDFFormat.NTRIPLES);
	node.addTransformer(new Transformer("epicur"));
	updateMetadata(node, metadata);
	return "Update " + subject + " metadata " + metadata;
    }

    /**
     * Generates a urn
     * 
     * @param node
     *            the object
     * @param snid
     *            the urn subnamespace id
     * @return the urn
     */
    public String replaceUrn(Node node, String snid) {
	String subject = node.getPid();
	String hasUrn = "http://purl.org/lobid/lv#urn";
	String metadata = node.getMetadata();
	String urn = generateUrn(subject, snid);
	metadata = RdfUtils.replaceTriple(subject, hasUrn, urn, true, metadata);
	node.addTransformer(new Transformer("epicur"));
	updateMetadata(node, metadata);
	return "Update " + subject + " metadata " + metadata;
    }

    /**
     * @param nodes
     *            a list of nodes
     * @param snid
     *            a urn snid e.g."hbz:929:02"
     * @param fromBefore
     *            only objects created before "fromBefore" will get a urn
     * @return a message
     */
    public String addUrnToAll(List<Node> nodes, String snid, Date fromBefore) {
	return apply(nodes, n -> addUrn(n, snid, fromBefore));
    }

    private String addUrn(Node n, String snid, Date fromBefore) {
	if (n.getCreationDate().before(fromBefore))
	    return addUrn(n, snid);
	return "\n Not Updated " + n.getPid() + " " + n.getCreationDate()
		+ " is not before " + fromBefore;
    }

    /**
     * @param node
     *            the node to be published on the oai interface
     * @return A short message.
     */
    public String makeOAISet(Node node) {
	try {
	    String pid = node.getPid();
	    OaiSetBuilder oaiSetBuilder = new OaiSetBuilder();
	    RepositoryResult<Statement> statements = RdfUtils.getStatements(
		    node.getMetadata(), "fedora:info/");
	    while (statements.hasNext()) {
		Statement st = statements.next();
		String subject = st.getSubject().stringValue();
		String predicate = st.getPredicate().stringValue();
		String object = st.getObject().stringValue();

		OaiSet set = oaiSetBuilder.getSet(subject, predicate, object);
		if (set == null) {
		    continue;
		}
		if (!Globals.fedora.nodeExists(set.getPid())) {
		    createOAISet(set.getName(), set.getSpec(), set.getPid());
		}
		linkObjectToOaiSet(node, set.getSpec(), set.getPid());
	    }

	    if ("public".equals(node.getAccessScheme())) {
		addSet(node, "open_access");
	    }
	    if (node.hasUrn()) {
		addSet(node, "epicur");
		String urn = node.getUrn();
		if (urn.startsWith("urn:nbn:de:hbz:929:01")) {
		    addSet(node, "urn-set-1");
		} else if (urn.startsWith("urn:nbn:de:hbz:929:02")) {
		    addSet(node, "urn-set-2");
		}
	    }
	    if (node.hasLinkToCatalogId()) {
		play.Logger.info(node.getPid() + " add aleph set!");
		addSet(node, "aleph");
		addSet(node, "edo01");
	    }
	    updateIndexAndCache(node);
	    return pid + " successfully created oai sets!";
	} catch (Exception e) {
	    throw new MetadataNotFoundException(e);
	}

    }

    private void addSet(Node node, String name) {
	String spec = name;
	String namespace = "oai";
	String oaipid = namespace + ":" + name;
	if (!Globals.fedora.nodeExists(oaipid)) {
	    createOAISet(name, spec, oaipid);
	}
	linkObjectToOaiSet(node, spec, oaipid);
    }

    private void linkObjectToOaiSet(Node node, String spec, String pid) {
	node.removeRelations(ITEM_ID);
	node.removeRelation(IS_MEMBER_OF, "info:fedora/" + pid);
	Link link = new Link();
	link.setPredicate(IS_MEMBER_OF);
	link.setObject("info:fedora/" + pid, false);
	node.addRelation(link);
	link = new Link();
	link.setPredicate(ITEM_ID);
	link.setObject("info:fedora/" + node.getPid(), false);
	node.addRelation(link);
	Globals.fedora.updateNode(node);
    }

    private void createOAISet(String name, String spec, String pid) {
	String setSpecPred = "http://www.openarchives.org/OAI/2.0/setSpec";
	String setNamePred = "http://www.openarchives.org/OAI/2.0/setName";
	Link setSpecLink = new Link();
	setSpecLink.setPredicate(setSpecPred);
	Link setNameLink = new Link();
	setNameLink.setPredicate(setNamePred);
	String namespace = "oai";
	{
	    Node oaiset = new Node();
	    oaiset.setNamespace(namespace);
	    oaiset.setPID(pid);
	    setSpecLink.setObject(spec, true);
	    oaiset.addRelation(setSpecLink);
	    setNameLink.setObject(name, true);
	    oaiset.addRelation(setNameLink);
	    DublinCoreData dc = oaiset.getDublinCoreData();
	    dc.addTitle(name);
	    oaiset.setDublinCoreData(dc);
	    Globals.fedora.createNode(oaiset);
	}
    }

    /**
     * Generates a urn
     * 
     * @param niss
     *            usually the pid of an object
     * @param snid
     *            usually the namespace
     * @return the urn
     */
    public String generateUrn(String niss, String snid) {
	URN urn = new URN(snid, niss);
	return urn.toString();
    }

    /**
     * @param node
     *            generate metadatafile with lobid data for this node
     * @return a short message
     */
    public String lobidify(Node node) {
	String pid = node.getPid();
	List<Pair<String, String>> identifier = node.getDublinCoreData()
		.getIdentifier();
	String alephid = "";
	for (Pair<String, String> id : identifier) {
	    if (id.getLeft().startsWith("TT") || id.getLeft().startsWith("HT")) {
		alephid = id.getLeft();
		break;
	    }
	}
	if (alephid.isEmpty()) {
	    throw new HttpArchiveException(500, pid + " no Catalog-Id found");
	}
	String lobidUri = "http://lobid.org/resource/" + alephid;
	try {
	    URL lobidUrl = new URL("http://lobid.org/resource/" + alephid
		    + "/about");
	    RDFFormat inFormat = RDFFormat.TURTLE;
	    String accept = "text/turtle";
	    String str = RdfUtils.readRdfToString(lobidUrl, inFormat,
		    RDFFormat.NTRIPLES, accept);
	    if (str.contains("http://www.w3.org/2002/07/owl#sameAs")) {
		str = RdfUtils.followSameAsAndInclude(lobidUrl, pid, inFormat,
			accept);
	    }
	    str = Pattern.compile(lobidUri).matcher(str)
		    .replaceAll(Matcher.quoteReplacement(pid))
		    + "<"
		    + pid
		    + "> <"
		    + archive.fedora.Vocabulary.REL_MAB_527
		    + "> <" + lobidUri + "> .";

	    return updateMetadata(node, str);

	} catch (MalformedURLException e) {
	    throw new HttpArchiveException(500, e);
	} catch (Exception e) {
	    throw new HttpArchiveException(500, e);
	}

    }

    /**
     * reinits oai sets on every node
     * 
     * @param nodes
     *            a list of nodes
     * @return a message
     */
    public String reinitOaiSets(List<Node> nodes) {
	return apply(nodes, n -> makeOAISet(n));
    }

    /**
     * Imports lobid metadata for each node in the list
     * 
     * @param nodes
     *            list of nodes
     * @return a message
     */
    public String lobidify(List<Node> nodes) {
	return apply(nodes, n -> lobidify(n));
    }

    /**
     * @param node
     *            links the node to it's parents parent.
     * @return the updated node
     */
    public Node moveUp(Node node) {
	String recentParent = node.getParentPid();
	Node parent = new Read().readNode(recentParent);
	String destinyPid = parent.getParentPid();
	if (destinyPid == null || destinyPid.isEmpty())
	    throw new UpdateNodeException(
		    "Can't find valid destiny for move operation. "
			    + node.getParentPid() + " parent of "
			    + node.getPid() + " has no further parent.");
	RegalObject object = new RegalObject();
	object.setParentPid(destinyPid);
	node = new Create().patchResource(node, object);

	play.Logger.info("Move " + node.getPid() + " to new parent "
		+ node.getParentPid() + ". Recent Parent was " + recentParent
		+ ". Calculated destiny was " + destinyPid);
	return node;
    }

    @SuppressWarnings({ "serial", "javadoc" })
    public class MetadataNotFoundException extends RuntimeException {
	public MetadataNotFoundException(Throwable e) {
	    super(e);
	}
    }

    @SuppressWarnings({ "serial" })
    private class UpdateNodeException extends RuntimeException {
	public UpdateNodeException(Throwable cause) {
	    super(cause);
	}

	public UpdateNodeException(String msg) {
	    super(msg);
	}
    }

}
