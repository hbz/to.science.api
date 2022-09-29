package models.model;

import org.json.JSONObject;

/**
 * <p>
 * Interface for all parts of the ToScience data model. Provides main methods to
 * be implemented from all Classes within ToScience model.
 * </p>
 * <p>
 * The basic kind of ToScience model looks like that in JSON:
 * 
 * <pre>
 * object : { "@id" : "https://example.domain/id",
 *            "prefLabel" : "label of Id"
 *            }
 * </pre>
 * </p>
 * <p>
 * Each ToScience model can be enhanced with additional fields or structures.
 * For Instance:
 * 
 * <pre>
 * object : { "@id" : "https://example.domain/id",
 *            "prefLabel" : "label of object",
 *            <b>"type" : "type of object"</b>
 *            }
 * </pre>
 * </p>
 * 
 * @author aquast
 *
 */
public interface ToScienceModel {

	/**
	 * <p>
	 * Get a AMBmodel object mapped from any ToScience model object
	 * </p>
	 * <p>
	 * For Schema Details see:
	 * <a href="https://dini-ag-kim.github.io/amb/draft/" target="_blank">AMB
	 * Schema</a>
	 * </p>
	 * <p>
	 * 
	 * @return JSONObject with object data mapped to AMBmodel (formely known as
	 *         LRMI)
	 *         </p>
	 */
	public JSONObject getAmbJSONObject();

	/**
	 * <P>
	 * Get a specific ToScience model object by mapping an AMBmodel object into
	 * ToScience model object
	 * </p>
	 * <p>
	 * 
	 * @param ambJSONObject the amb JSON object
	 * @return JSONObject with object data mapped from AMB (formely known as LRMI)
	 *         </p>
	 */
	public JSONObject getFromAmbJSONObject(JSONObject ambJSONObject);

	/**
	 * <p>
	 * Get a specific ToScience model object as String representation of JSON
	 * </p>
	 * <p>
	 * 
	 * @return JSON representation as String
	 */
	public String getJson();

	/**
	 * <p>
	 * Get a specific ToScience model object as org.json.JSONObject
	 * </p>
	 * <p>
	 * 
	 * @return a JSONObject representation of the model object
	 *         </p>
	 */
	public JSONObject getJSONObject();

	/**
	 * <p>
	 * Set a complete Toscience model object inferred from the Id expressed as
	 * complete Id-URI.
	 * </p>
	 * <p>
	 * 
	 * @param id an id expressed as complete Id-URI as String
	 */
	public ToScienceModel setById(String id);

}
