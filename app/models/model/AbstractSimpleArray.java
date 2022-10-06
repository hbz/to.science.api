package models.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import models.implementation.*;

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
	public ArrayList<SimpleObject> simpleObjectList =
			new ArrayList<SimpleObject>();
	public ArrayList<SimpleObjectImpl> list = new ArrayList<SimpleObjectImpl>();

	public void addItem(SimpleObject item) {
		simpleObjectList.add(item);
	}

	public void addItem(SimpleObjectImpl item) {
		play.Logger.debug("Adding Item to AbstractSimpleArray");
		play.Logger.debug("Adding Item " + item.getJson());
		list.add(item);
	}

	@Override
	public SimpleObjectImpl getItem(int i) {
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
		Iterator<SimpleObjectImpl> iterator = list.iterator();
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
		play.Logger.info("getJson=" + json);
		return json;
	}

	public JSONArray getJSONArray() {
		play.Logger.debug("Start getJSONArray");
		JSONArray jsonArr = new JSONArray();

		Iterator<SimpleObjectImpl> iterator = this.getIterator();
		while (iterator.hasNext()) {
			play.Logger.debug("Reading next list element");
			SimpleObjectImpl obj = iterator.next();
			play.Logger.debug("Found object of class " + obj.getClass());
			jsonArr.put(obj.getJSONObject());
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
			JSONObject obj;
			try {
				obj = (JSONObject) jsonArr.get(i);
			} catch (JSONException e) {
				logger.error(e.getMessage());
				throw new RuntimeException(e);
			}
			ambJsonArr.put(obj);
		}
		return ambJsonArr;
	}

}