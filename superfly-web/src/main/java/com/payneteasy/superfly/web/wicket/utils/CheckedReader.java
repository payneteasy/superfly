package com.payneteasy.superfly.web.wicket.utils;

public interface CheckedReader<T> {
	boolean isChecked(T object);
	void setChecked(T object, boolean checked);
}
