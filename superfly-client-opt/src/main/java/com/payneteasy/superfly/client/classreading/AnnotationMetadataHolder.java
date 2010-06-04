package com.payneteasy.superfly.client.classreading;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AnnotationMetadataHolder {
	private Map<String, Map<String, Object>> attributesMap = new LinkedHashMap<String, Map<String, Object>>();
	private Map<String, Set<String>> metaAnnotationMap = new LinkedHashMap<String, Set<String>>();

	public Map<String, Map<String, Object>> getAttributesMap() {
		return attributesMap;
	}

	public Map<String, Set<String>> getMetaAnnotationMap() {
		return metaAnnotationMap;
	}
}
