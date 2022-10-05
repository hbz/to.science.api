package models.implementation;

import helper.GenericPropertiesLoader;
import models.model.*;

/**
 
 */
public class SimpleObjectImpl extends AbstractSimpleObject
		implements SimpleObject {
	@Override
	public SimpleObjectImpl setById(String id) {
		simpleObject.put("@id", id);
		return this;

	}
}
