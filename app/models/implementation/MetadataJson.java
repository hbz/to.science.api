package models.implementation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import models.model.Metadata;
import models.model.ToScienceModel;

/**
 * <p>
 * An abstract model for different kind of Toscience model implementations.
 * Provides basic methods to create new instances and operate with them.
 * </p>
 * <p>
 * As JSON model:
 * 
 * <pre>
 * "objectName" : {
 *           "prefLabel" : "label",
 *           "@id" : "objectUri",
 *           "type" : "isOfType"
 *           }
 * </pre>
 * </p>
 * 
 * @author kuss
 */
public class MetadataJson implements Metadata {

	final static Logger logger = LoggerFactory.getLogger(MetadataJson.class);

	private JSONObject metadata;
	private String id;

	/**
	 * Der Konstruktor für MetadataJson
	 */
	public MetadataJson() {
		this.metadata = new JSONObject();
		this.id = null;
	}

	@Override
	public String getJson() {
		return metadata.toString();
	}

	@Override
	public JSONObject getJSONObject() {
		return metadata;
	}

	@Override
	public JSONObject getAmbJSONObject() {
		return metadata;
	}

	@Override
	public JSONObject getFromAmbJSONObject(JSONObject ambJSONObject) {
		JSONObject jsonObj = this.getJSONObject();
		Iterator<String> iterator = ambJSONObject.keys();

		while (iterator.hasNext()) {
			String ambKey = iterator.next();
			String key = ambKey;
			key = ambKey.replace("name", "prefLabel");
			// key = key.replace("", "");
			try {
				jsonObj.put(key, ambJSONObject.get(ambKey));
			} catch (JSONException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

		}
		return jsonObj;
	}

	@Override
	public ToScienceModel setById(String id) {
		this.id = id;
		return this;
	}

	@Override
	public void put(String key, Object value) {
		try {
			metadata.put(key, value);
		} catch (JSONException e) {
			logger.error(e.getMessage());
			throw new RuntimeException(e);
		}

	}

}