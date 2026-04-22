/*
 * Copyright 2026 hbz NRW (http://www.hbz-nrw.de/)
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

import com.fasterxml.jackson.databind.JsonNode;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;

import authenticate.BasicAuth;
import models.Message;
import play.libs.F.Promise;
import play.mvc.Result;

/**
 * In dieser Klasse werden API-Calls (Endpoints) definiert, die von externen
 * Anwendungen aufgerufen werden, sogenannte "Webhooks". Siehe die Definitionen
 * der Endpoints in der "routes"-Datei, to.science.api/conf/routes. API is
 * documented using Swagger. See: https://github.com/wordnik/swagger-ui
 * 
 * Zur eigentlichen Verarbeitung der Calls wird an andere Klassen übergeben.
 * 
 * @author Ingolf Kuss, kuss@hbz-nrw.de
 * @date 2026-04-16
 */
@BasicAuth
@Api(value = "/webhooks", description = "Die Webhooks-Endpoints verarbeiten Anfragen (POSTs) von externen Anwendungen.")
@SuppressWarnings("javadoc")
public class Webhooks extends MyController {

	@ApiOperation(produces = "application/json", nickname = "btrixCrawlFinished", value = "btrixCrawlFinished", notes = "Implementing Browsertrix Webhook \"Crawl Finished\".", response = Message.class, httpMethod = "POST")
	@ApiImplicitParams({
			@ApiImplicitParam(value = "Metadata", required = true, dataType = "string", paramType = "body") })
	/**
	 * Dieser Endpoint verarbeitet eine vom Browertrix bereit gestellte neue
	 * Archivdatei (der Endung WACZ)
	 * 
	 * @author I. Kuss
	 * @date 2026-04-22
	 * @return
	 */
	public static Promise<Result> btrixCrawlFinished() {

		return Promise.promise(() -> {
			JsonNode body = request().body().asJson();
			play.Logger.debug("btrix Crawl Finished sent body: " + body);
			String filename =
					body.findValue("filename").toString().replaceAll("^\"|\"$", "");
			play.Logger.debug("filename found: " + filename);
			return ok();
		});
	}

}
