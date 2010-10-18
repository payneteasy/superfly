package org.springframework.core.type.classreading;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.asm.ClassReader;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;

import com.payneteasy.superfly.client.classreading.MethodReadingMetadataReader;
import com.payneteasy.superfly.client.classreading.MethodReadingMetadataReaderFactory;

public class MultipleAnnotationValuesCachingMetadataReaderFactory implements
		MethodReadingMetadataReaderFactory {
	
	private final Map<Resource, MethodReadingMetadataReader> classReaderCache = new HashMap<Resource, MethodReadingMetadataReader>();
	
	private final ResourceLoader resourceLoader;

	/**
	 * Create a new MultipleAnnotationValueCachingMetadataReaderFactory for the default class loader.
	 */
	public MultipleAnnotationValuesCachingMetadataReaderFactory() {
		this.resourceLoader = new DefaultResourceLoader();
	}

	/**
	 * Create a new MultipleAnnotationValuesCachingMetadataReaderFactory for the given resource loader.
	 * @param resourceLoader the Spring ResourceLoader to use
	 * (also determines the ClassLoader to use)
	 */
	public MultipleAnnotationValuesCachingMetadataReaderFactory(ResourceLoader resourceLoader) {
		this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
	}

	/**
	 * Create a new MultipleAnnotationValuesCachingMetadataReaderFactory for the given class loader.
	 * @param classLoader the ClassLoader to use
	 */
	public MultipleAnnotationValuesCachingMetadataReaderFactory(ClassLoader classLoader) {
		this.resourceLoader =
				(classLoader != null ? new DefaultResourceLoader(classLoader) : new DefaultResourceLoader());
	}


	public MethodReadingMetadataReader getMetadataReader(String className) throws IOException {
		String resourcePath = ResourceLoader.CLASSPATH_URL_PREFIX +
				ClassUtils.convertClassNameToResourcePath(className) + ClassUtils.CLASS_FILE_SUFFIX;
		return getMetadataReader(this.resourceLoader.getResource(resourcePath));
	}
	
	public MethodReadingMetadataReader getMetadataReader(Resource resource) throws IOException {
		synchronized (this.classReaderCache) {
			MethodReadingMetadataReader metadataReader = this.classReaderCache.get(resource);
			if (metadataReader == null) {
				metadataReader = doGetMetadataReader(resource);
				this.classReaderCache.put(resource, metadataReader);
			}
			return metadataReader;
		}
	}
	
	public MethodReadingMetadataReader getMethodReadingMetadataReader(
			String className) throws IOException {
		return getMetadataReader(className);
	}

	public MethodReadingMetadataReader getMethodReadingMetadataReader(
			Resource resource) throws IOException {
		return getMetadataReader(resource);
	}

	
	private MethodReadingMetadataReader doGetMetadataReader(Resource resource) throws IOException {
		InputStream is = resource.getInputStream();
		try {
			return new MultipleAnnotationValuesMetadataReader(resource, new ClassReader(is), this.resourceLoader.getClassLoader());
		}
		finally {
			is.close();
		}
	}

}
