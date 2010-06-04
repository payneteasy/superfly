package com.payneteasy.superfly.client;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MultipleAnnotationValuesCachingMetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.client.classreading.AnnotationAttributesSource;
import com.payneteasy.superfly.client.classreading.MethodReadingMetadataReader;
import com.payneteasy.superfly.client.classreading.MethodReadingMetadataReaderFactory;
import com.payneteasy.superfly.client.exception.CollectionException;

/**
 * ActionDescriptionCollector implementation which scans classes in the given
 * packages looking for a given annotation (by default Secured) and extracts
 * action names from its attributes.
 * Descriptions of the resulting actions are null.
 * 
 * @author Roman Puchkovskiy
 */
public class ScanningActionDescriptionCollector implements
		ActionDescriptionCollector {
	
	private static final Logger logger = LoggerFactory.getLogger(ScanningActionDescriptionCollector.class);
	
	protected static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
	
	private String[] basePackages = new String[0];
	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	private String resourcePattern = DEFAULT_RESOURCE_PATTERN;
	private MethodReadingMetadataReaderFactory metadataReaderFactory = new MultipleAnnotationValuesCachingMetadataReaderFactory(this.resourcePatternResolver);
	private Class<? extends Annotation> annotationClass;
	private ValuesExtractor valuesExtractor = new DefaultValuesExtractor();

	@Required
	public void setBasePackages(String[] basePackages) {
		this.basePackages = basePackages;
	}

	public void setResourcePatternResolver(
			ResourcePatternResolver resourcePatternResolver) {
		this.resourcePatternResolver = resourcePatternResolver;
	}

	public void setResourcePattern(String resourcePattern) {
		this.resourcePattern = resourcePattern;
	}

	public void setMetadataReaderFactory(MethodReadingMetadataReaderFactory metadataReaderFactory) {
		this.metadataReaderFactory = metadataReaderFactory;
	}

	public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
		this.annotationClass = annotationClass;
	}

	public void setValuesExtractor(ValuesExtractor valuesExtractor) {
		this.valuesExtractor = valuesExtractor;
	}

	public List<ActionDescription> collect() throws CollectionException {
		Set<String> names = new HashSet<String>();
		for (String basePackage : basePackages) {
			try {
				processPackage(basePackage, names);
			} catch (IOException e) {
				throw new CollectionException(e);
			}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Collected the following actions: " + names);
		}
		
		return buildDescriptions(names);
	}

	protected void processPackage(String basePackage, Set<String> names)
			throws IOException {
		String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
				resolveBasePackage(basePackage) + "/" + this.resourcePattern;
		Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
		for (Resource resource : resources) {
			if (resource.isReadable()) {
				MethodReadingMetadataReader metadataReader = this.metadataReaderFactory.getMethodReadingMetadataReader(resource);
				
				// class annotations
				Map<String, Object> attributes = metadataReader
						.getAnnotationMetadata()
						.getAnnotationAttributes(annotationClass.getName());
				processAnnotationAttributes(names, attributes);
				
				// method annotations
				Map<String, AnnotationAttributesSource> methodsAnnotationMetadata = metadataReader.getMethodAnnotationMetadataSource().getMethodsAnnotationMetadata();
				for (AnnotationAttributesSource attributesSource : methodsAnnotationMetadata.values()) {
					attributes = attributesSource.getAnnotationAttributes(annotationClass.getName());
					processAnnotationAttributes(names, attributes);
				}
			}
		}
	}

	protected void processAnnotationAttributes(Set<String> names,
			Map<String, Object> attributes) {
		if (attributes != null) {
			Object value = attributes.get(null);
			if (value != null) {
				String[] values = valuesExtractor.extract(value);
				for (String v : values) {
					names.add(v);
				}
			}
		}
	}
	
	protected String resolveBasePackage(String basePackage) {
		return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
	}
	
	protected List<ActionDescription> buildDescriptions(Set<String> names) {
		List<ActionDescription> descriptions = new ArrayList<ActionDescription>(names.size());
		for (String name : names) {
			descriptions.add(new ActionDescription(name));
		}
		return descriptions;
	}
}
