package helper;

/**
 * 
 * @author adoud
 *
 */

public class JsonMapperHelper {

	/**
	 * This method checks if the entered date is in the format yyyy-mm-dd.
	 * 
	 * @param datum
	 * @return True if the entered date matches the format yyyy-mm-dd, otherwise
	 *         false.
	 */
	boolean isDateValid(String datum) {
		boolean result = false;
		if (datum.matches("\\d{4}-?([0-9][0-9])?-?([0-9][0-9])?")) {
			result = true;
		}
		return result;
	}

}
