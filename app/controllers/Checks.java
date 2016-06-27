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
package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.QueryParam;

import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.annotations.Api;

import actions.BasicAuth;
import actions.Read;
import models.Globals;
import models.Message;
import play.libs.F.Promise;
import play.mvc.Result;

/**
 * 
 * @author Jan Schnasse, schnasse@hbz-nrw.de
 * 
 *         Api is documented using swagger. See:
 *         https://github.com/wordnik/swagger-ui
 * 
 */
@BasicAuth
@Api(value = "/checks", description = "Performs checks to verify the health of the repository")
@SuppressWarnings("javadoc")
public class Checks extends MyController {

	public static Promise<Result> missingUrn(@QueryParam("from") int from,
			@QueryParam("until") int until) {
		return new BulkActionAccessor().call((userId) -> {
			try {
				QueryBuilder query = QueryBuilders.filteredQuery(
						QueryBuilders.matchAllQuery(), FilterBuilders.missingFilter("urn"));
				SearchHits sh =
						Globals.search.query(Globals.namespaces, query, from, until);
				SearchHit[] hits = sh.getHits();
				List<Map<String, Object>> objects = new ArrayList<>();
				for (SearchHit hit : hits) {
					JsonNode node = mapper.convertValue(hit.getSource(), JsonNode.class);
					Map<String, Object> object = getObject(hit, node);
					objects.add(object);
				}
				return getJsonResult(objects);
			} catch (Exception e) {
				return JsonMessage(new Message(e, 500));
			}
		});
	}

	public static Promise<Result> missingDoi(@QueryParam("from") int from,
			@QueryParam("until") int until) {
		return new BulkActionAccessor().call((userId) -> {
			try {
				QueryBuilder query = QueryBuilders.filteredQuery(
						QueryBuilders.matchAllQuery(), FilterBuilders.missingFilter("doi"));
				SearchHits sh =
						Globals.search.query(Globals.namespaces, query, from, until);
				SearchHit[] hits = sh.getHits();
				List<Map<String, Object>> objects = new ArrayList<>();
				for (SearchHit hit : hits) {
					JsonNode node = mapper.convertValue(hit.getSource(), JsonNode.class);
					Map<String, Object> object = getObject(hit, node);
					objects.add(object);
				}
				return getJsonResult(objects);
			} catch (Exception e) {
				return JsonMessage(new Message(e, 500));
			}
		});
	}

	public static Promise<Result> doiStatus(@QueryParam("from") int from,
			@QueryParam("until") int until) {
		return new BulkActionAccessor().call((userId) -> {
			try {
				ObjectMapper mapper = new ObjectMapper();
				QueryBuilder query = QueryBuilders.matchAllQuery();
				SearchHits sh =
						Globals.search.query(Globals.namespaces, query, from, until);
				SearchHit[] hits = sh.getHits();
				List<Map<String, Object>> objects = new ArrayList<>();
				for (SearchHit hit : hits) {
					JsonNode node = mapper.convertValue(hit.getSource(), JsonNode.class);
					Map<String, Object> object = getObject(hit, node);
					objects.add(object);
				}
				return getJsonResult(objects);
			} catch (Exception e) {
				return JsonMessage(new Message(e, 500));
			}
		});
	}

	private static Map<String, Object> getObject(SearchHit hit, JsonNode node) {
		Map<String, Object> object = new HashMap<String, Object>();
		object.put("id", hit.getId());
		object.put("ht", node.at("/parallelEdition/0/@id").asText());
		object.put("doi", node.at("/doi").asText());
		object.put("urn", node.at("/urn/0").asText());
		object.put("doiTarget", getDoiStatus((String) object.get("doi")));
		object.put("urnTarget", getUrnStatus((String) object.get("urn")));
		return object;
	}

	private static String getUrnStatus(String urn) {
		if (urn == null)
			return "NONE";
		try {
			return new Read().getFinalURL(Globals.urnResolverAddress + urn);
		} catch (Exception e) {
			play.Logger.warn("", e);
		}
		return "NONE";
	}

	private static String getDoiStatus(String doi) {
		if (doi == null)
			return "NONE";
		try {
			return new Read().getFinalURL(Globals.doiResolverAddress + doi);
		} catch (Exception e) {
			play.Logger.warn("", e);
		}
		return "NONE";
	}

}
