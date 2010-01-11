package com.payneteasy.superfly.web.wicket.utils;

import java.io.Serializable;

/**
 * Contains both an object and whether it's checked or not.
 * 
 * @author Roman Puchkovskiy
 * @param <T>
 */
public class ObjectCheckedWrapper<T extends Serializable> implements Serializable {
	private T object;
	private boolean checked;
	
	public ObjectCheckedWrapper() {
		super();
	}

	public ObjectCheckedWrapper(T object, boolean checked) {
		super();
		this.object = object;
		this.checked = checked;
	}

	public T getObject() {
		return object;
	}

	public void setObject(T object) {
		this.object = object;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

}
