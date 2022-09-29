package models.implementation;

import org.json.JSONException;
import org.json.JSONObject;

import models.model.AbstractSimpleObject;
import models.model.SimpleObject;
import helper.GenericPropertiesLoader;

/**
 * <p>
 * An implementation for toscience license. Id is a complete Id-URI.
 * </p>
 * 
 * <p>
 * As JSONObject:
 * 
 * <pre>
 * subject : { "@id" : "String",
 *             "prefLabel" : "String",
 *           }
 * 
 * </pre>
 * </p>
 * <p>
 * 
 * @author aquast
 *         </p>
 * 
 */
public class License extends AbstractSimpleObject implements SimpleObject {

	private String licenseList = "license-de.properties";

	/**
	 * <p>
	 * Set a complete license inferred from the Id expressed as complete Id-URI.
	 * Licenses prefLabel is resolved by using the License-Id. Method currently
	 * used the Licenses-de.properties file for this mapping.
	 * </p>
	 * 
	 * @param id
	 */
	@Override
	public License setById(String id) {
		simpleObject.put("@id", id);
		simpleObject.put("prefLabel",
				new GenericPropertiesLoader().loadVocabMap(licenseList).get(id));
		return this;

	}

	@Override
	public JSONObject getAmbJSONObject() {
		JSONObject ambJSONObject = new JSONObject();
		try {
			ambJSONObject.put("id", simpleObject.get("@id"));
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return ambJSONObject;
	}

}