package com.payneteasy.superfly.web.wicket.repeater;

import java.io.Serializable;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Base for a data provider.
 * 
 * @author Roman Puchkovskiy
 * @param <T>	provided type
 */
public abstract class BaseDataProvider<T extends Serializable> implements IDataProvider<T> {
	
	public IModel<T> model(T object) {
		return new Model<T>(object);
	}

	public void detach() {
	}

}
