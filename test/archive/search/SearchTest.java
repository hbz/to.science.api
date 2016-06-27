package archive.search;

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

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.elasticsearch.search.SearchHits;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import archive.fedora.CopyUtils;
import archive.search.Search.SearchException;
import base.BaseModelTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Jan Schnasse schnasse@hbz-nrw.de
 * 
 */
@SuppressWarnings("javadoc")
public class SearchTest extends BaseModelTest {

	String edoweb2606976 = null;
	String query1 = null;
	SearchMock search = null;
	@SuppressWarnings("unused")
	private String edoweb3273325, edoweb3273325_2007, edoweb3273331,
			edowebMappingTest, drupalOne, drupalTwo, drupalThree, drupalFour,
			drupalFive, drupalSix, v1, v2, v3, v4;

	@Before
	public void setUp() throws IOException {
		edoweb2606976 =
				CopyUtils.copyToString(Thread.currentThread().getContextClassLoader()
						.getResourceAsStream("edoweb2606976.json"), "utf-8");
		edoweb3273325 =
				CopyUtils.copyToString(Thread.currentThread().getContextClassLoader()
						.getResourceAsStream("edoweb3273325.json"), "utf-8");
		edoweb3273325_2007 =
				CopyUtils.copyToString(Thread.currentThread().getContextClassLoader()
						.getResourceAsStream("edoweb3273325-2007.json"), "utf-8");
		edoweb3273331 =
				CopyUtils.copyToString(Thread.currentThread().getContextClassLoader()
						.getResourceAsStream("edoweb3273331.json"), "utf-8");
		edowebMappingTest =
				CopyUtils.copyToString(Thread.currentThread().getContextClassLoader()
						.getResourceAsStream("edowebMappingTest.json"), "utf-8");
		drupalOne = CopyUtils.copyToString(Thread.currentThread()
				.getContextClassLoader().getResourceAsStream("1.json"), "utf-8");

		drupalTwo = CopyUtils.copyToString(Thread.currentThread()
				.getContextClassLoader().getResourceAsStream("2.json"), "utf-8");
		drupalThree = CopyUtils.copyToString(Thread.currentThread()
				.getContextClassLoader().getResourceAsStream("3.json"), "utf-8");
		drupalFour = CopyUtils.copyToString(Thread.currentThread()
				.getContextClassLoader().getResourceAsStream("4.json"), "utf-8");
		drupalFive = CopyUtils.copyToString(Thread.currentThread()
				.getContextClassLoader().getResourceAsStream("5.json"), "utf-8");
		drupalSix = CopyUtils.copyToString(Thread.currentThread()
				.getContextClassLoader().getResourceAsStream("6.json"), "utf-8");
		v1 = CopyUtils.copyToString(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("ZDB126456-4_v1.json"), "utf-8");
		v2 = CopyUtils.copyToString(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("ZDB126456-4_v2.json"), "utf-8");
		v3 = CopyUtils.copyToString(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("ZDB126456-4_v3.json"), "utf-8");
		v4 = CopyUtils.copyToString(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("ZDB126456-4_v4.json"), "utf-8");

		query1 = CopyUtils.copyToString(Thread.currentThread()
				.getContextClassLoader().getResourceAsStream("query-1.json"), "utf-8");
		search = new SearchMock("test", "public-index-config.json");
		search.index("test", "monograph", "edoweb2606976", edoweb2606976);

	}

	@After
	public void tearDown() {
		search.delete("edoweb:123", "test", "type");
		for (int i = 100; i > 0; i--) {
			search.delete("edoweb:" + i, "test", "monograph");
		}
		search.down();
	}

	@Test
	public void testCreation() {
		Assert.assertNotNull(search);
	}

	@Test
	public void testResourceListing() throws InterruptedException {
		SearchHits hits = search.listResources("test", "monograph", 0, 5);
		Assert.assertEquals(1, hits.getTotalHits());
	}

	@Test
	public void testResourceListing_withDefaultValues()
			throws InterruptedException {
		SearchHits hits = search.listResources("", "", 0, 10);
		Assert.assertEquals(1, hits.getTotalHits());
	}

	@Test(expected = Search.InvalidRangeException.class)
	public void testResourceListing_withWrongParameter() {
		search.listResources("test", "monograph", 5, 1);
	}

	// @Test
	// public void testDelete() throws InterruptedException {
	// SearchHits hits = search.listResources("test", "monograph", 0, 1);
	// Assert.assertEquals(1, hits.getTotalHits());
	// search.delete("test", "monograph", "edoweb2606976");
	// hits = search.listResources("test", "monograph", 0, 1);
	// Assert.assertEquals(0, hits.getTotalHits());
	// }

	@Test
	public void testListIds() throws InterruptedException {
		search.index("test", "monograph", "edoweb2606976", edoweb2606976);
		List<String> list = search.listIds("test", "monograph", 0, 1);
		Assert.assertEquals(1, list.size());
		Assert.assertEquals(list.get(0), "edoweb2606976");
	}

	@Test
	public void testFromUntil() throws InterruptedException {
		for (int i = 100; i > 0; i--) {
			search.index("test", "monograph", "edoweb:" + i, edoweb2606976);
		}
		List<String> list = search.listIds("test", "monograph", 0, 10);
		Assert.assertEquals(10, list.size());
		list = search.listIds("test", "monograph", 10, 50);
		Assert.assertEquals(40, list.size());
		list = search.listIds("test", "monograph", 60, 61);
		Assert.assertEquals(1, list.size());
		list = search.listIds("test", "monograph", 100, 150);
		Assert.assertEquals(1, list.size());
	}

	// @Test
	// public void mappingTest() {
	// search.index("test", "monograph", "edoweb:3273325", edoweb3273325);
	// search.index("test", "monograph", "edoweb:3273325-2007",
	// edoweb3273325_2007);
	// search.index("test", "monograph", "edoweb:3273331", edoweb3273331);
	// SearchHits hits = search.query("test", "@graph.isPartOf",
	// "edoweb:3273325-2007");
	// Assert.assertEquals(1, hits.totalHits());
	// Assert.assertEquals("edoweb:3273331", hits.getHits()[0].getId());
	// }

	@Test
	public void indexTest() {
		search.index("test", "monograph", "edoweb:3273325", edoweb3273325);
		search.index("test", "monograph", "edoweb:3273325-2007",
				edoweb3273325_2007);
		search.index("test", "monograph", "edoweb:3273331", edoweb3273331);
	}

	@Test(expected = SearchException.class)
	public void esSettings_fails() {
		search.down();
		search = new SearchMock("test", "public-index-config_fails.json");
		System.out.println("Fails with: "
				+ printJsonFormatted(search.getSettings("test", "monograph")));
		search.index("test", "monograph",
				"edoweb:f1c9954d-f4d0-4d91-8f47-0a9c8f46df9b", edowebMappingTest);

	}

	@Test
	public void esSettings_succeed() {
		search.down();
		search = new SearchMock("test", "public-index-config_succeed.json");
		search.index("test", "monograph",
				"edoweb:f1c9954d-f4d0-4d91-8f47-0a9c8f46df9b", edowebMappingTest);
		System.out.println("Succeeds with: "
				+ printJsonFormatted(search.getSettings("test", "monograph")));
	}

	@Test(expected = SearchException.class)
	public void esSettingsDrupalBulk_fail() {
		// Make sure everything is fresh
		search.down();

		/*-
		 * Initalize index with very simple mapping
		 * {
		 * "mappings":
		 * {
		 * "monograph":
		 * {
		 * "properties":
		 * {
		 * "@graph":
		 * {
		 * "properties":
		 * {
		 * "creatorName":
		 * {
		 * "type": "string",
		 * "index": "not_analyzed"
		 * }
		 * }
		 * }
		 * }
		 * }
		 * }
		 * }
		 *
		 */
		search = new SearchMock("test", "public-index-config_succeed.json");

		/*-
		 *Index
		 * {
		 * "@graph":
		 * [
		 * {
		 * "@id" : "edoweb:1fe8fb0c-e844-4c07-9df3-7bf28d125e28",
		 * "creatorName":
		 * {
		 * "@id": "http://d-nb.info/gnd/171948629"
		 * }
		 * }
		 * ]
		 * }
		 */
		search.index("test", "monograph",
				"edoweb:1fe8fb0c-e844-4c07-9df3-7bf28d125e28", drupalOne);

		// Print mapping
		System.out.println("\nCurrent Mapping: "
				+ printJsonFormatted(search.getSettings("test", "monograph")) + "\n");

		/*-
		 * Index
		 * {
		 * "@graph":
		 * [
		 * {
		 * "@id": "edoweb:f1c9954d-f4d0-4d91-8f47-0a9c8f46df9b",
		 * "creatorName": "1"
		 * }
		 * ]
		 * }
		 */
		// Here it goes off
		try {
			search.index("test", "monograph",
					"edoweb:f1c9954d-f4d0-4d91-8f47-0a9c8f46df9b", drupalFour);
		} catch (SearchException e) {
			e.printStackTrace();
			throw e;
		}

	}

	@Test
	public void esSettingsDrupalBulk_succeed() {
		search.down();
		search = new SearchMock("test", "public-index-config_succeed.json");
		search.index("test", "monograph",
				"edoweb:1fe8fb0c-e844-4c07-9df3-7bf28d125e28", drupalOne);
		System.out.println("Succeeds with: "
				+ printJsonFormatted(search.getSettings("test", "monograph")));
		search = new SearchMock("test", "public-index-config_succeed.json");
		search.index("test", "monograph",
				"edoweb:a601c448-b370-4bc5-b2ba-367b53ecc513", drupalTwo);
		System.out.println("Succeeds with: "
				+ printJsonFormatted(search.getSettings("test", "monograph")));
		search = new SearchMock("test", "public-index-config_succeed.json");
		search.index("test", "monograph",
				"edoweb:ad744673-5ded-43a0-90c0-3f8f2223b4be", drupalThree);
		System.out.println("Succeeds with: "
				+ printJsonFormatted(search.getSettings("test", "monograph")));
		search = new SearchMock("test", "public-index-config_succeed.json");
		search.index("test", "monograph",
				"edoweb:f1c9954d-f4d0-4d91-8f47-0a9c8f46df9b", drupalFour);
		System.out.println("Succeeds with: "
				+ printJsonFormatted(search.getSettings("test", "monograph")));
		search = new SearchMock("test", "public-index-config_succeed.json");
		search.index("test", "monograph",
				"edoweb:ad462c5d-f566-41f9-986d-744c10bbd450", drupalFive);
		System.out.println("Succeeds with: "
				+ printJsonFormatted(search.getSettings("test", "monograph")));
		search = new SearchMock("test", "public-index-config_succeed.json");
		search.index("test", "monograph",
				"edoweb:549b92ee-d0a0-4471-83dc-799b08f3c0f6", drupalSix);
		System.out.println("Succeeds with: "
				+ printJsonFormatted(search.getSettings("test", "monograph")));
	}

	@Test(expected = SearchException.class)
	public void esSettingsDrupalBulk_withNestedSettings_failsDirectly() {
		search.down();
		search =
				new SearchMock("test", "public-index-config_different_succeed.json");

		System.out.println("Succeeds with: "
				+ printJsonFormatted(search.getSettings("test", "monograph")));

		search.index("test", "monograph",
				"edoweb:ad462c5d-f566-41f9-986d-744c10bbd450", drupalFour);

		search.index("test", "monograph",
				"edoweb:a601c448-b370-4bc5-b2ba-367b53ecc513", drupalOne);

	}

	@Test
	public void differentPrefLabelSchemas_mustSucceed() {
		search.down();
		search = new SearchMock("test", "public-index-journal-mapping.json");
		System.out
				.println(printJsonFormatted(search.getSettings("test", "journal")));
		search.index("test", "journal", "3", v3);
		printJsonFormatted(search.getSettings("test", "journal"));
		System.out
				.println(printJsonFormatted(search.getSettings("test", "journal")));
		// search.index("edoweb", "journal", "1", v1);
		// System.out.println(printJsonFormatted(search.getSettings("test",
		// "journal")));
		// search.index("edoweb", "journal", "2", v2);
		// System.out.println(printJsonFormatted(search.getSettings("test",
		// "journal")));
		//
		// search.index("edoweb", "journal", "4", v4);
		// System.out.println(printJsonFormatted(search.getSettings("test",
		// "journal")));

	}

	private String printJsonFormatted(Map<String, Object> map) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			StringWriter w = new StringWriter();
			mapper.writeValue(w, map);
			return w.toString();
		} catch (Exception e) {
			e.printStackTrace();

		}
		return "";
	}
}
