package models.implementation;

import models.model.AbstractSimpleObject;
import models.model.SimpleObject;
import helper.GenericPropertiesLoader;

/**
 * <p>
 * An implementation for ToScience Affiliation. ToScience Affiliation is always
 * part of an Agent model.
 * </p>
 * <p>
 * Affiliation has this structure as JSONObject:
 * 
 * <pre>
 * affiliation : { "@id" : "String",
 *                 "prefLabel" : "String",
 *                 "type" : "Organization"
 *                 }
 * 
 * </pre>
 * </p>
 * <p>
 * 
 * @author aquast
 *         </p>
 * 
 */
public class Affiliation extends AbstractSimpleObject implements SimpleObject {

	public Affiliation() {
		simpleObject.put("type", "Organization");
	}

	// private ArrayList<HashMap<String,Object>> affiliation = new ArrayList<>();
	private String affiliationList =
			"ResearchOrganizationsRegistry-de.properties";

	/**
	 * <p>
	 * Set a complete affiliation inferred from the Id expressed as complete
	 * Id-URI. Affiliations prefLabel is resolved by using the ROR-Id. Method
	 * currently used the ResearchOrganizationsRegistry-de.properties file for
	 * this mapping. Using the ROR-API is planned
	 * </p>
	 * 
	 * @param id URI of ROR-Id as String
	 */
	@Override
	public Affiliation setById(String id) {
		simpleObject.put("@id", id);
		simpleObject.put("prefLabel",
				new GenericPropertiesLoader().loadVocabMap(affiliationList).get(id));
		return this;
	}

}