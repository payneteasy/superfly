package com.payneteasy.superfly.web.page;

import java.io.Serializable;

public class SelectObjectWrapper<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	private T object;
	private boolean selected;

	public SelectObjectWrapper(T obj) {
		this.selected = false;
		this.object = obj;
	}

	public T getObject() {
		return object;
	}

	public void setObject(T object) {
		this.object = object;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
