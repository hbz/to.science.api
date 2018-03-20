package controllers;

import java.util.Base64;
import java.util.Collection;

import javax.ws.rs.PathParam;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.rio.RDFFormat;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import archive.fedora.RdfUtils;
import models.Globals;
import play.libs.F.Promise;
import play.mvc.Result;

@Api(value = "/authors", description = "An enpoint to fake gnd data for authors.")
@SuppressWarnings("javadoc")
public class AdhocController extends MyController {
	@ApiOperation(produces = "application/json,text/html", nickname = "getAuthorsRdf", value = "getAuthorsRdf", notes = "An enpoint to fake gnd data for authors. In fact the data from input path is passed back to the user surrounded by some rdf.", response = play.mvc.Result.class, httpMethod = "GET")
	public static Promise<Result> getAuthorsRdf(
			@PathParam("authorname") String authorname) {
		response().setHeader("Access-Control-Allow-Origin", "*");
		Collection<Statement> g = new TreeModel();
		ValueFactory f = RdfUtils.valueFactory;
		IRI subj = f.createIRI(Globals.protocol + Globals.server + "/authors/"
				+ RdfUtils.urlEncode(authorname));
		IRI pred = f.createIRI("http://www.w3.org/2004/02/skos/core#prefLabel");
		Literal obj = f.createLiteral(RdfUtils.urlDecode(authorname));
		g.add(f.createStatement(subj, pred, obj));
		g.add(f.createStatement(subj,
				f.createIRI(
						"http://d-nb.info/standards/elementset/gnd#preferredNameForThePerson"),
				obj));
		g.add(f.createStatement(subj,
				f.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
				f.createIRI(
						"http://d-nb.info/standards/elementset/gnd#UndifferentiatedPerson")));
		return Promise.promise(() -> {
			String body = "";
			if (request().accepts("application/rdf+xml")) {
				response().setHeader("Content-Type",
						"application/rdf+xml; charset=utf-8");
				body = RdfUtils.graphToString(g, RDFFormat.RDFXML);
			} else if (request().accepts("text/plain")) {
				response().setContentType("text/plain");
				body = RdfUtils.graphToString(g, RDFFormat.NTRIPLES);
			} else {
				response().setContentType("application/json");
				body = RdfUtils.graphToString(g, RDFFormat.JSONLD);
			}
			return ok(body);
		});
	}

	public static Promise<Result> getAdhocRdf(@PathParam("type") String type,
			@PathParam("name") String base64EncodedNameWithMod) {

		response().setHeader("Access-Control-Allow-Origin", "*");
		Collection<Statement> g = new TreeModel();
		ValueFactory f = RdfUtils.valueFactory;
		IRI subj = f.createIRI(Globals.protocol + Globals.server + "/adhoc/"
				+ RdfUtils.urlEncode(type) + "/" + base64EncodedNameWithMod);
		IRI pred = f.createIRI("http://www.w3.org/2004/02/skos/core#prefLabel");
		Literal obj =
				f.createLiteral(helper.MyURLEncoding.decode(base64EncodedNameWithMod));
		g.add(f.createStatement(subj, pred, obj));
		return Promise.promise(() -> {
			String body = "";
			if (request().accepts("application/rdf+xml")) {
				response().setHeader("Content-Type",
						"application/rdf+xml; charset=utf-8");
				body = RdfUtils.graphToString(g, RDFFormat.RDFXML);
			} else if (request().accepts("text/plain")) {
				response().setContentType("text/plain");
				body = RdfUtils.graphToString(g, RDFFormat.NTRIPLES);
			} else {
				response().setContentType("application/json");
				body = RdfUtils.graphToString(g, RDFFormat.JSONLD);
			}
			return ok(body);
		});
	}
}
