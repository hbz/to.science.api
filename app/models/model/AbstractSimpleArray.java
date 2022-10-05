package models.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * An abstract model for different implementations of toscience ArrayList of
 * String. Provides basic methods to create new instances and modify them.
 * </p>
 * <p>
 * As JSON model
 * 
 * <pre>
 * "arrayName" : [ "item 1", "item 2", "item 3", ...]
 * </pre>
 * </p>
 * 
 * @author aquast
 *
 */
public abstract class AbstractSimpleArray implements SimpleArray {

	final static Logger logger =
			LoggerFactory.getLogger(AbstractSimpleArray.class);
	public ArrayList<Object> list = new ArrayList<Object>();

	@Override
	public void addItem(Object item) {
		list.add(item);

	}

	@Override
	public Object getItem(int i) {
		return list.get(i);
	}

	@Override
	public int size() {
		return list.size();
	}

	/**
	 * @return
	 */
	public Iterator getIterator() {
		Iterator<Object> iterator = list.iterator();
		return iterator;
	}

	public String getJson() {
		String json = null;
		JSONArray jsonArr = this.getJSONArray();

		try {
			json = jsonArr.toString(1);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		logger.info(json);
		return json;
	}

	public JSONArray getJSONArray() {
		JSONArray jsonArr = new JSONArray();

		Iterator<Object> iterator = this.getIterator();
		while (iterator.hasNext()) {
			Object obj = iterator.next();
			jsonArr.put(obj);
		}
		return jsonArr;
	}

	/**
	 * <p>
	 * Map toscience model into amb model
	 * </p>
	 * 
	 * @return mapped object as JSONArray
	 */
	public JSONArray getAmbJSONObject() {
		JSONArray ambJsonArr = new JSONArray();
		JSONArray jsonArr = this.getJSONArray();

		for (int i = 0; i < jsonArr.length(); i++) {
			Object obj;
			try {
				obj = jsonArr.getString(i);
			} catch (JSONException e) {
				logger.error(e.getMessage());
				throw new RuntimeException(e);
			}
			ambJsonArr.put(obj);
		}
		return ambJsonArr;
	}

}