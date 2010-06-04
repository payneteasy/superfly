package com.payneteasy.superfly.client.classreading;

import java.util.Map;

public interface AnnotationAttributesSource {
	Map<String, Object> getAnnotationAttributes(String annotationClassName);
}
