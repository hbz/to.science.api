package controllers;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.PathParam;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.annotations.ApiOperation;

import controllers.MyController.ReadMetadataAction;
import models.Globals;
import models.Node;
import play.libs.F.Promise;
import play.mvc.Result;

public class Browse extends MyController {

	@ApiOperation(produces = "application/json", nickname = "listUrn", value = "listUrn", notes = "Returns infos about urn", httpMethod = "GET")
	public static Promise<Result> browse(
			@PathParam("facetName") String facetName) {
		return Promise.promise(() -> {
			Map<String, Object> aggregationConf = initAggregations();
			Map<String, Object> facet =
					(Map<String, Object>) aggregationConf.get(facetName);
			((Map<String, Object>) facet.get("terms")).put("size", 100);
			SearchResponse response = Globals.search.query(
					new String[] {
							Globals.PUBLIC_INDEX_PREF + Globals.defaultNamespace + "2",
							Globals.PDFBOX_OCR_INDEX_PREF + Globals.defaultNamespace },
					"*", 0, 0, aggregationConf);

			Aggregations aggs = response.getAggregations();
			Terms agg = aggs.get(facetName);
			Collection<Bucket> b = agg.getBuckets();
			return ok(views.html.browse.render(facetName, agg));
		});
	}

	private static Map<String, Object> initAggregations() {
		try (InputStream in =
				play.Play.application().resourceAsStream("aggregations.conf")) {
			return (Map<String, Object>) new ObjectMapper().readValue(in, Map.class)
					.get("aggs");
		} catch (Exception e) {
			return new HashMap<>();
		}
	}
}
