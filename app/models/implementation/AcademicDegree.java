package models.implementation;

import models.model.AbstractSimpleObject;
import models.model.SimpleObject;
import helper.GenericPropertiesLoader;

/**
 * <p>
 * An implementation for ToScience AcademicDegree. ToScience AcademicDegree is
 * always part of an Agent model.
 * </p>
 * <p>
 * AcademicDegree has this structure as JSONObject:
 * 
 * <pre>
 * academicDegree : { "@id" : "String",
 *                 "prefLabel" : "String",
 *                 }
 * 
 * </pre>
 * </p>
 * <p>
 * 
 * @author kuss
 *         </p>
 * 
 */
public class AcademicDegree extends AbstractSimpleObject
		implements SimpleObject {

	/**
	 * the standard constructor for AcademicDegree Sets degree to \"unknown\"
	 */
	public AcademicDegree() {
		this.setById(
				"https://d-nb.info/standards/elementset/gnd#academicDegree/unknown");
	}

	private String academicDegreeProperties = "academicDegree-de.properties";

	/**
	 * <p>
	 * Set a complete academicDegree inferred from the Id expressed as complete
	 * Id-URI. prefLabel is resolved by using the properties file.
	 * </p>
	 * 
	 * @param id URI of academic degree as String
	 */
	@Override
	public AcademicDegree setById(String id) {
		simpleObject.put("@id", id);
		simpleObject.put("prefLabel", new GenericPropertiesLoader()
				.loadVocabMap(academicDegreeProperties).get(id));
		return this;
	}

}