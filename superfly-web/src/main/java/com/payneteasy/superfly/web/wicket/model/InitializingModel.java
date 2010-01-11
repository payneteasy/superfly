package com.payneteasy.superfly.web.wicket.model;

import org.apache.wicket.model.IModel;

/**
 * Model which initializes its object on getObject() if setObject() was not
 * called yet.
 * 
 * @author Roman Puchkovskiy
 * @param <T>
 */
public abstract class InitializingModel<T> implements IModel<T> {
	
	private T object;
	private boolean initialized = false;
	
	public InitializingModel() {
		super();
	}

	public InitializingModel(T object) {
		super();
		this.object = object;
	}

	public final T getObject() {
		if (!initialized) {
			object = getInitialValue();
			initialized = true;
		}
		return object;
	}

	public final void setObject(T object) {
		initialized = true;
		this.object = object;
	}

	public void detach() {
	}
	

	protected abstract T getInitialValue();
	
	public void clearInitialized() {
		object = null;
		initialized = false;
	}
}
