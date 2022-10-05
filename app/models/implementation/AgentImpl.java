package models.implementation;

import models.model.*;

/**
 * Diese Klasse implementiert einen Autor oder Mitwirkenden
 * 
 * @author kuss
 *
 */
public class AgentImpl extends AbstractAgent implements Agent {

	@Override
	public AgentImpl setById(String id) {
		simpleObject.put("@id", id);
		return this;
	}

}
