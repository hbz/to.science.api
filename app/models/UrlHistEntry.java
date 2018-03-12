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

package models;

import java.util.Date;

/**
 * Klasse, die einen URL-Historien-Eintrag implementiert.
 * 
 * @author I. Kuss
 */
public class UrlHistEntry {
	String url;
	Date endDate;

	public UrlHistEntry() {

	}

	/**
	 * Konstruktor nur mit url
	 * 
	 * @param url
	 */
	public UrlHistEntry(String urlNew) {
		this.url = urlNew;
		this.endDate = null;
	}

	/**
	 * Konstruktor mit URL und Datum
	 * 
	 * @param url
	 * @param endDate
	 */
	public UrlHistEntry(String urlOld, Date endDateNew) {
		this.url = urlOld;
		this.endDate = endDateNew;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getEndDate() {
		return endDate;
	}
}
