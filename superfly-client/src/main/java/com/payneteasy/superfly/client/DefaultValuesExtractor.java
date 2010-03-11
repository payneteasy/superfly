package com.payneteasy.superfly.client;

/**
 * Default ValuesExtractor implementation which checks that object is an array
 * of strings first. If not, it checks whether it a string and in this case
 * returns an array of one element. Otherwise, it returns an array of one
 * element using object.toString() return value.
 * 
 * @author Roman Puchkovskiy
 */
public class DefaultValuesExtractor implements ValuesExtractor {

	public String[] extract(Object value) {
		String[] values;
		if (value instanceof String[]) {
			values = (String[]) value;
		} else if (value instanceof String) {
			values = new String[]{(String) value};
		} else {
			values = new String[]{value.toString()};
		}
		return values;
	}

}
