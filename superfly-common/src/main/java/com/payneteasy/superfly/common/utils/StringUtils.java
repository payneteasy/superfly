package com.payneteasy.superfly.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class StringUtils {
	public static String[] commaDelimitedListToStringArray(String s) {
		StringTokenizer tokenizer = new StringTokenizer(s, ",");
		List<String> fragments = new ArrayList<String>();
		while (tokenizer.hasMoreTokens()) {
			fragments.add(tokenizer.nextToken().trim());
		}
		return fragments.toArray(new String[fragments.size()]);
	}
}
