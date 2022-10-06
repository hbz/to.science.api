package models.model;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kuss A class for an abstract agent list
 */
public abstract class AbstractAgentList extends AbstractSimpleArray {

	@SuppressWarnings("hiding")
	final static Logger logger = LoggerFactory.getLogger(AbstractAgentList.class);

	@SuppressWarnings("hiding")
	public ArrayList<AbstractAgent> list = new ArrayList<AbstractAgent>();

	public void addItem(AbstractAgent agent) {
		list.add(agent);
	}

	public AbstractAgent getAgentItem(int i) {
		return list.get(i);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public Iterator<AbstractAgent> getIterator() {
		Iterator<AbstractAgent> iterator = list.iterator();
		return iterator;
	}

	@Override
	public String getJson() {
		JSONArray jsonArr = this.getJSONArray();
		String json;
		try {
			json = jsonArr.toString(1);
		} catch (JSONException e) {
			logger.error(e.getMessage());
			throw new RuntimeException(e);
		}
		logger.info(json);
		return json;
	}

	@Override
	public JSONArray getJSONArray() {
		JSONArray jsonArr = new JSONArray();

		Iterator<AbstractAgent> iterator = this.getIterator();
		while (iterator.hasNext()) {
			AbstractAgent agent = iterator.next();
			jsonArr.put(agent.getJSONObject());
		}
		return jsonArr;
	}

	public JSONArray getAmbJSONArray() {
		JSONArray ambJsonArr = new JSONArray();
		Iterator<AbstractAgent> iterator = this.getIterator();
		while (iterator.hasNext()) {
			AbstractAgent agent = iterator.next();
			ambJsonArr.put(agent.getAmbJSONObject());
		}
		return ambJsonArr;
	}

}
