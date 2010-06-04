package com.payneteasy.superfly.client.classreading;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.MetadataReaderFactory;

public interface MethodReadingMetadataReaderFactory extends
		MetadataReaderFactory {
	MethodReadingMetadataReader getMethodReadingMetadataReader(String className) throws IOException;

	MethodReadingMetadataReader getMethodReadingMetadataReader(Resource resource) throws IOException;
}
