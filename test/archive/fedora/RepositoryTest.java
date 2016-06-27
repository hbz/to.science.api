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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import models.Node;
import models.Transformer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import base.BaseModelTest;

/**
 * 
 * @author Jan Schnasse schnasse@hbz-nrw.de
 * 
 */
@SuppressWarnings("javadoc")
public class RepositoryTest extends BaseModelTest {
	final static Logger logger = LoggerFactory.getLogger(RepositoryTest.class);

	FedoraFacade facade = null;
	Node object = null;
	String server = null;

	@Before
	public void setUp() throws IOException {

		Properties properties = new Properties();
		properties.load(Play.application().resourceAsStream("test.properties"));
		facade = FedoraFactory.getFedoraImpl(properties.getProperty("fedoraUrl"),
				properties.getProperty("user"), properties.getProperty("password"));
		server = properties.getProperty("apiUrl");

		object = new Node().setNamespace("test").setPID("test:234")
				.setLabel("Ein Testobjekt").setFileLabel("test")
				.setType(Vocabulary.TYPE_OBJECT);
		object.setContentType("monograph");
		object.dublinCoreData.addTitle("Ein Testtitel");
		object.dublinCoreData.addCreator("Jan Schnasse");
		object.setMetadataFile(Thread.currentThread().getContextClassLoader()
				.getResource("test.nt").getFile());

		object.addTransformer(new Transformer("testepicur", "epicur",
				server + "/resource/(pid).epicur"));
		object.addTransformer(new Transformer("testoaidc", "oaidc",
				server + "/resource/(pid).oaidc"));
		object.addTransformer(
				new Transformer("testpdfa", "pdfa", server + "/resource/(pid).pdfa"));

		URL url =
				Thread.currentThread().getContextClassLoader().getResource("test.pdf");
		play.Logger.info("Upload data from: " + url.getPath());
		object.setUploadFile(url.getPath());
		object.setMimeType("application/pdf");
		cleanUp();
	}

	@Test
	public void createNode() {
		facade.createNode(object);
		Assert.assertTrue(facade.nodeExists(object.getPid()));
	}

	@Test(expected = FedoraFacade.NodeNotFoundException.class)
	public void testNodeNotFoundException() {
		facade.readNode(object.getPid());
	}

	@Test
	public void readNode() {

		facade.createNode(object);
		Node node = facade.readNode(object.getPid());

		Assert.assertEquals(0, node.getNodeType().compareTo(object.getNodeType()));
		Assert.assertEquals("test:234", node.getPid());
		Assert.assertEquals("test", node.getNamespace());
		Assert.assertEquals("Jan Schnasse", node.dublinCoreData.getFirstCreator());
		Assert.assertEquals("Ein Testobjekt", node.getLabel());
		Assert.assertEquals("test", node.getFileLabel());
		Assert.assertEquals("Ein Testtitel", node.dublinCoreData.getFirstTitle());
		Assert.assertEquals("application/pdf", node.getMimeType());

	}

	@Test
	public void updateNode() {

		// Object Creation
		facade.createNode(object);
		object = facade.readNode(object.getPid());
		Assert.assertEquals("Ein Testtitel", object.dublinCoreData.getFirstTitle());
		Assert.assertEquals("test", object.getFileLabel());
		Assert.assertEquals("application/pdf", object.getMimeType());

		// Object update modifying the local instance
		Vector<String> newTitle = new Vector<String>();
		newTitle.add("Neuer Titel");
		object.dublinCoreData.setTitle(newTitle);
		URL url = Thread.currentThread().getContextClassLoader()
				.getResource("HT017297166.xml");
		object.setUploadFile(url.getPath());
		object.setMimeType("text/xml");
		object.setFileLabel("HT017297166.xml");

		facade.updateNode(object);

		object = facade.readNode(object.getPid());
		Assert.assertEquals("Neuer Titel", object.dublinCoreData.getFirstTitle());
		Assert.assertEquals("HT017297166.xml", object.getFileLabel());
		Assert.assertEquals("text/xml", object.getMimeType());

		// Object update on the reread object
		object.setUploadFile(url.getPath());
		object.setMimeType("application/pdf");
		facade.updateNode(object);
		object = facade.readNode(object.getPid());
		Assert.assertEquals("application/pdf", object.getMimeType());
	}

	@Test
	public void findObjects() {
		List<String> result = facade.findPids("test:*", FedoraVocabulary.SIMPLE);
		for (String pid : result)
			facade.deleteNode(pid);
		Assert.assertEquals(0,
				facade.findPids("test:*", FedoraVocabulary.SIMPLE).size());
		if (!facade.nodeExists(object.getPid()))

			facade.createNode(object);

		result = facade.findPids("test:*", FedoraVocabulary.SIMPLE);
		Assert.assertEquals(1, result.size());
	}

	@Test
	public void deleteNodeSimple() {
		if (!facade.nodeExists(object.getPid()))
			facade.createNode(object);

		facade.purgeNode(object.getPid());
		Assert.assertFalse(facade.nodeExists(object.getPid()));
	}

	@Test
	public void deleteNodeComplex() throws InterruptedException {
		if (!facade.nodeExists(object.getPid()))
			facade.createNode(object);

		Node child = new Node().setNamespace("test").setPID("test:2345")
				.setLabel("Ein Testobjekt").setFileLabel("test")
				.setType(Vocabulary.TYPE_OBJECT).setParentPid(object.getPid());
		child.setContentType("monograph");
		facade.createNode(child);
		facade.unlinkParent(child);
		facade.linkToParent(child, object.getPid());
		facade.linkParentToNode(object.getPid(), child.getPid());

		Node grandchild = new Node().setNamespace("test").setPID("test:23456")
				.setLabel("Ein Testobjekt").setFileLabel("test")
				.setType(Vocabulary.TYPE_OBJECT).setParentPid(child.getPid());
		grandchild.setContentType("monograph");
		facade.createNode(grandchild);
		facade.unlinkParent(grandchild);
		facade.linkToParent(grandchild, child.getPid());
		facade.linkParentToNode(child.getPid(), grandchild.getPid());

		System.out.println(deleteComplexObject(object.getPid()));

		Assert.assertFalse(facade.nodeExists(object.getPid()));
		System.out.println("Deleted: " + object.getPid());
		Assert.assertFalse(facade.nodeExists(child.getPid()));
		System.out.println("Deleted: " + child.getPid());
		Assert.assertFalse(facade.nodeExists(grandchild.getPid()));
		System.out.println("Deleted: " + grandchild.getPid());
	}

	public List<Node> deleteComplexObject(String rootPID) {

		List<Node> list = facade.listComplexObject(rootPID);
		for (Node n : list) {
			facade.purgeNode(n.getPid());
		}
		return list;
	}

	@Test
	public void createTransformer() {
		facade.createNode(object);
		List<Transformer> transformers = new Vector<Transformer>();
		transformers.add(new Transformer("testepicur", "epicur",
				server + "/resource/(pid).epicur"));
		transformers.add(new Transformer("testoaidc", "oaidc",
				server + "/resource/(pid).oaidc"));
		transformers.add(
				new Transformer("testpdfa", "pdfa", server + "/resource/(pid).pdfa"));
		facade.updateContentModels(transformers);
	}

	@Test
	public void removeNodesTransformer() {
		facade.createNode(object);
		object.removeTransformer("testepicur");

		List<Transformer> ts = object.getTransformer();
		Assert.assertEquals(2, ts.size());
		for (Transformer t : ts) {
			Assert.assertFalse(t.getId().equals("testepicur"));
		}
		facade.updateNode(object);
		object = facade.readNode(object.getPid());

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("testoaidc", "testoaidc");
		map.put("testpdfa", "testpdfa");
		ts = object.getTransformer();
		Assert.assertEquals(2, ts.size());
		for (Transformer t : ts) {
			Assert.assertTrue(map.containsKey(t.getId()));
		}
		for (Transformer t : ts) {
			Assert.assertFalse(t.getId().equals("testepicur"));
		}
	}

	@Test
	public void readNodesTransformer() {
		facade.createNode(object);
		object = facade.readNode(object.getPid());
		List<Transformer> ts = object.getTransformer();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("testepicur", "testepicur");
		map.put("testoaidc", "testoaidc");
		map.put("testpdfa", "testpdfa");
		Assert.assertEquals(3, ts.size());
		for (Transformer t : ts) {
			System.out.println(t.getId());
			Assert.assertTrue(map.containsKey(t.getId()));
		}
	}

	@Test
	public void nodeExists() {
		Assert.assertTrue(!facade.nodeExists(object.getPid()));
		facade.createNode(object);
		Assert.assertTrue(facade.nodeExists(object.getPid()));
	}

	@After
	public void tearDown() {
		cleanUp();
	}

	private void cleanUp() {

		List<String> result = facade.findPids("CM:test*", FedoraVocabulary.SIMPLE);
		for (String pid : result) {
			facade.purgeNode(pid);
		}
		result = facade.findPids("test:*", FedoraVocabulary.SIMPLE);
		for (String pid : result) {
			facade.purgeNode(pid);
		}
	}
}
