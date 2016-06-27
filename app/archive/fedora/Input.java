/*
 * Copyright 2015 hbz NRW (http://www.hbz-nrw.de/)
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
import java.io.Reader;
import java.net.URL;

import org.w3c.dom.ls.LSInput;

import play.Play;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

/**
 * @author Jan Schnasse
 * 
 */
public class Input implements LSInput {

	private String publicId;

	private String systemId;

	public Input(String publicId, String sysId) {
		this.publicId = publicId;
		this.systemId = sysId;
	}

	public String getPublicId() {
		return publicId;
	}

	public void setPublicId(String publicId) {
		this.publicId = publicId;
	}

	public String getBaseURI() {
		return null;
	}

	public InputStream getByteStream() {
		return null;
	}

	public boolean getCertifiedText() {
		return false;
	}

	public Reader getCharacterStream() {
		return null;
	}

	public String getEncoding() {
		return null;
	}

	public synchronized String getStringData() {
		play.Logger
				.info("Load XML Resource with id " + systemId + " , " + publicId);
		try {
			URL url = new URL(systemId);
			return CharStreams.toString(new InputStreamReader(url.openStream()));
		} catch (Exception e1) {

			try {

				return CharStreams.toString(new InputStreamReader(
						Play.application().resourceAsStream("public/schemas/" + systemId),
						Charsets.UTF_8));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void setBaseURI(String baseURI) {
	}

	public void setByteStream(InputStream byteStream) {
	}

	public void setCertifiedText(boolean certifiedText) {
	}

	public void setCharacterStream(Reader characterStream) {
	}

	public void setEncoding(String encoding) {
	}

	public void setStringData(String stringData) {
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

}