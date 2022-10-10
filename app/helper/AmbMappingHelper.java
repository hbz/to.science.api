/**
 * 
 */
package helper;

import java.io.BufferedReader;
import java.io.StringReader;

import org.json.JSONObject;

import actions.Read;
import models.Node;
import to.science.core.modelx.mapper.AmbMapperImpl;

/**
 * @author aquast
 *
 */
public class AmbMappingHelper {

	AmbMapperImpl ambMapper = new AmbMapperImpl();

	/**
	 * Get a to.science JSONObject from lrmiData within a specific node
	 * 
	 * @param node
	 * @return
	 */
	public JSONObject getJSONObjectFromAmb(Node node) {
		JSONObject tosModel = new JSONObject();

		Read read = new Read();
		String currentLrmiContent = read.readLrmiData(node);
		StringReader ambReader = new StringReader(currentLrmiContent);
		JSONObject ambObject = parseReader(ambReader);
		tosModel = ambMapper.getTosJSONObject(ambObject);

		return tosModel;
	}

	/**
	 * @param ambReader
	 * @return
	 */
	private JSONObject parseReader(StringReader ambReader) {
		JSONObject ambJSONObj = new JSONObject();

		try {
			BufferedReader bReader = new BufferedReader(ambReader);
			StringBuilder jsonStringBuilder = new StringBuilder();

			String inputStr;
			while ((inputStr = bReader.readLine()) != null)
				jsonStringBuilder.append(inputStr);
			ambJSONObj = new JSONObject(jsonStringBuilder.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ambJSONObj;
	}

}
