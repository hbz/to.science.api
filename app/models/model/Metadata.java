package models.model;

/**
 * An interface for toscience metadata
 * 
 * @author kuss
 *
 */
public interface Metadata extends ToScienceModel {

	/**
	 * 
	 * @param key key of metadata field
	 * @param value content of metadata field
	 */
	public void put(String key, Object value);

}
