package models.model;

/**
 * <p>
 * Interface defines some basic methods required for a minimal to.science object
 * representing data structure of
 * </p>
 * <p>
 * 
 * <pre>
 * "model" : {
 *           "prefLabel" : "label",
 *           "@id" : "objectUri",
 *           "type" : "isOfType"
 *           }
 * </pre>
 * </p>
 * 
 * @author aquast
 *
 */
public interface SimpleObject extends ToScienceModel {

	/**
	 * @return Id as String representing the complete Id-URL of the respective
	 *         resource
	 */
	public String getId();

	/**
	 * @return the prefered name, title or label of the respective resource
	 */
	public String getPrefLabel();

	/**
	 * @return a type in the context of the respective resource
	 */
	public String getType();

	/**
	 * @param id an Id as complete Id-URI representing a respective resource
	 */
	public void setId(String id);

	/**
	 * @param prefLabel a prefered name, title or label for the respective
	 *          resource
	 */
	public void setPrefLabel(String prefLabel);

	/**
	 * @param Type a type for the respective resource
	 */
	public void setType(String Type);

}
