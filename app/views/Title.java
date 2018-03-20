/*
 * Copyright 2016 hbz NRW (http://www.hbz-nrw.de/)
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
package views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.Globals;

/**
 * 
 * @author Jan Schnasse
 *
 */
@SuppressWarnings("unchecked")
public class Title {

	public static String getTitle(Map<String, Object> hit) {
		Collection<String> title = (Collection<String>) hit.get("title");
		return title != null && !title.isEmpty() ? String.join("<br/> ", title)
				: hit.get("@id").toString();
	}

	public static String getAuthorNames(Map<String, Object> hit) {
		Collection<Map<String, Object>> authorList =
				(Collection<Map<String, Object>>) hit.get("creator");
		if (authorList == null || authorList.isEmpty()) {
			authorList = getContributorList(hit);
			if (authorList == null || authorList.isEmpty()) {

				return "";

			}
		}
		Collection<String> authorNames = new ArrayList<String>();
		for (Map<String, Object> author : authorList) {
			String authorName = (String) author.get(Globals.profile.getLabelKey());
			authorNames.add(authorName);
		}
		return authorNames != null && !authorNames.isEmpty()
				? String.join(" | ", authorNames) + " . " : "";
	}

	private static Collection<Map<String, Object>> getContributorList(
			Map<String, Object> hit) {
		return (Collection<Map<String, Object>>) hit.get("contributor");
	}

	public static String getIssued(Collection<Map<String, Object>> hit) {
		String issued = Helper.getPublicationMap(hit).get("regal:publishYear");
		return issued != null && !issued.isEmpty() ? String.join(".<br/> ", issued)
				: "";
	}
}