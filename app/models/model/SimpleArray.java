package models.model;

/**
 * Interface defines some methods required to create and modify an simple array
 * of string
 * 
 * @author aquast
 *
 */
public interface SimpleArray {

	/**
	 * add new item to Array
	 */
	public void addItem(Object item);

	/**
	 * @return get item as String
	 */
	public Object getItem(int i);

	/**
	 * @return
	 */
	public int size();
}
