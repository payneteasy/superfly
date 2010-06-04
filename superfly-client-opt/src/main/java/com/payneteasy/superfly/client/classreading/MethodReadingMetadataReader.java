package com.payneteasy.superfly.client.classreading;

import org.springframework.core.type.classreading.MetadataReader;

public interface MethodReadingMetadataReader extends MetadataReader {
	MethodAnnotationMetadataSource getMethodAnnotationMetadataSource();
}
