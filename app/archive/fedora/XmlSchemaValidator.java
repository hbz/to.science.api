/*
 * Copyright 2018 hbz NRW (http://www.hbz-nrw.de/)
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.xerces.dom.DOMInputImpl;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.SAXException;

/**
 * @author Jan Schnasse schnasse@hbz-nrw.de
 * 
 */
public class XmlSchemaValidator {

	/**
	 * @param xmlStream xml data as a stream
	 * @param schemaStream schema as a stream
	 * @param baseUri to search for relative path
	 * @param localPath to search for schemas
	 * @throws SAXException if validation fails
	 * @throws IOException not further specified
	 */
	public void validate(InputStream xmlStream, InputStream schemaStream,
			String baseUri, String localPath) throws SAXException, IOException {
		Source xmlFile = new StreamSource(xmlStream);
		SchemaFactory factory =
				SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		factory.setResourceResolver(
				(type, namespaceURI, publicId, systemId, baseURI) -> {
					LSInput input = new DOMInputImpl();
					input.setPublicId(publicId);
					input.setSystemId(systemId);
					input.setBaseURI(baseUri);
					input.setCharacterStream(new InputStreamReader(getSchemaAsStream(
							input.getSystemId(), input.getBaseURI(), localPath)));
					return input;
				});
		Schema schema = factory.newSchema(new StreamSource(schemaStream));
		javax.xml.validation.Validator validator = schema.newValidator();
		validator.validate(xmlFile);
	}

	private InputStream getSchemaAsStream(String systemId, String baseUri,
			String localPath) {
		InputStream in = getSchemaFromClasspath(systemId, localPath);
		return in == null ? getSchemaFromWeb(baseUri, systemId) : in;
	}

	private static InputStream getSchemaFromClasspath(String systemId,
			String localPath) {
		return Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(localPath + systemId);
	}

	private InputStream getSchemaFromWeb(String baseUri, String systemId) {
		try {
			URI uri = new URI(systemId);
			if (uri.isAbsolute()) {
				play.Logger.debug("Get stuff from web: " + systemId);
				return urlToInputStream(uri.toURL(), "text/xml");
			}
			play.Logger
					.debug("Get stuff from web: Host: " + baseUri + " Path: " + systemId);
			return getSchemaRelativeToBaseUri(baseUri, systemId);
		} catch (Exception e) {
			// maybe the systemId is not a valid URI or
			// the web has nothing to offer under this address
		}
		return null;
	}

	private InputStream urlToInputStream(URL url, String accept) {
		HttpURLConnection con = null;
		InputStream inputStream = null;
		try {
			con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(15000);
			con.setRequestProperty("User-Agent", "Name of my application.");
			con.setReadTimeout(15000);
			con.setRequestProperty("Accept", accept);
			con.connect();
			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_MOVED_PERM
					|| responseCode == HttpURLConnection.HTTP_MOVED_TEMP
					|| responseCode == 307 || responseCode == 303) {
				String redirectUrl = con.getHeaderField("Location");
				try {
					URL newUrl = new URL(redirectUrl);
					return urlToInputStream(newUrl, accept);
				} catch (MalformedURLException e) {
					URL newUrl =
							new URL(url.getProtocol() + "://" + url.getHost() + redirectUrl);
					return urlToInputStream(newUrl, accept);
				}
			}
			inputStream = con.getInputStream();
			return inputStream;
		} catch (SocketTimeoutException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private InputStream getSchemaRelativeToBaseUri(String baseUri,
			String systemId) {
		try {
			URL url = new URL(baseUri + systemId);
			return urlToInputStream(url, "text/xml");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
