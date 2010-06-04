package com.payneteasy.superfly.client.classreading;

import java.util.Map;

public interface MethodAnnotationMetadataSource {
	Map<String, AnnotationAttributesSource> getMethodsAnnotationMetadata();
}
