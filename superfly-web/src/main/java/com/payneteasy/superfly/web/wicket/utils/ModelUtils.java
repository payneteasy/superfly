package com.payneteasy.superfly.web.wicket.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ModelUtils {
	public static <T extends Serializable> List<ObjectCheckedWrapper<T>> wrapListInObjectCheckedWrappers(List<T> list, CheckedReader<T> checkedReader) {
		List<ObjectCheckedWrapper<T>> result = new ArrayList<ObjectCheckedWrapper<T>>(list.size());
		for (T object : list) {
			ObjectCheckedWrapper<T> wrapper = new ObjectCheckedWrapper<T>(object, checkedReader.isChecked(object));
		}
		return result;
	}
}
