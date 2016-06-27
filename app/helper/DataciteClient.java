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
package helper;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import models.Globals;
import models.Node;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.impl.MultiPartWriter;

/**
 * @author Jan Schnasse
 *
 */
public class DataciteClient {

	Client webclient = null;
	boolean testMode = false;
	int status = 200;

	/**
	 * The DataciteClient can mint and register dois via the datacite api
	 */
	public DataciteClient() {
		status = 200;
		String user = Globals.dataCiteUser;
		String password = Globals.dataCitePasswd;
		ClientConfig cc = new DefaultClientConfig();
		cc.getClasses().add(MultiPartWriter.class);
		cc.getClasses().add(FormDataMultiPart.class);
		cc.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
		cc.getFeatures().put(ClientConfig.FEATURE_DISABLE_XML_SECURITY, true);
		cc.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
				new HTTPSProperties(null, initSsl(cc)));
		webclient = Client.create(cc);
		webclient.addFilter(new HTTPBasicAuthFilter(user, password));
	}

	private SSLContext initSsl(ClientConfig cc) {
		try {
			SSLContext ctx = SSLContext.getInstance("SSL");
			KeyStore trustStore;
			trustStore = KeyStore.getInstance("JKS");

			if (Globals.keystoreLocation == null || Globals.keystorePassword == null
					|| Globals.keystoreLocation.isEmpty()
					|| Globals.keystorePassword.isEmpty()) {
				throw new RuntimeException(
						"Keystore is not configured. Set regal-api.keystoreLocation and regal-api.keystorePassword in application.conf");
			}

			try (InputStream in = new FileInputStream(Globals.keystoreLocation)) {
				trustStore.load(in, Globals.keystorePassword.toCharArray());
			}
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(trustStore);
			ctx.init(null, tmf.getTrustManagers(), null);
			return ctx;
		} catch (Exception e) {
			throw new RuntimeException("Can not initiate SSL connection", e);
		}
	}

	/**
	 * @param t if true, all calls will be made in testmode
	 */
	public void setTestMode(boolean t) {
		testMode = t;
	}

	/**
	 * @param doi the identifier
	 * @param objectUrl the url
	 * @return the http response as string
	 */
	public String mintDoiAtDatacite(String doi, String objectUrl) {
		try {
			status = 200;
			String url = testMode ? "https://mds.datacite.org/doi?testMode=true"
					: "https://mds.datacite.org/doi";
			WebResource resource = webclient.resource(url);
			String postBody = "doi=" + doi + "\nurl=" + objectUrl + "\n";
			play.Logger.debug("PostBody:\n" + postBody);
			String response =
					resource.type("application/xml").post(String.class, postBody);
			return response;
		} catch (UniformInterfaceException e) {
			setStatus(e.getResponse().getStatus());
			return e.getMessage();
		}
	}

	/**
	 * @param node the node
	 * @param xml the datacite xml
	 * @return the http response as string
	 */
	public String registerMetadataAtDatacite(Node node, String xml) {
		try {
			status = 200;
			String url = testMode ? "https://mds.datacite.org/metadata?testMode=true"
					: "https://mds.datacite.org/metadata";
			WebResource resource = webclient.resource(url);
			String response = resource.type("application/xml;charset=UTF-8")
					.post(String.class, xml);
			return response;
		} catch (UniformInterfaceException e) {
			setStatus(e.getResponse().getStatus());
			throw new RuntimeException(e);
		}
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
